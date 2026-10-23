package com.ryanjames.sheetmusic.musicxml

import okio.Buffer
import okio.IOException
import okio.Inflater
import okio.InflaterSource
import okio.use

/** A rejected archive: [entryName] identifies the raw entry when one is known. */
internal class ArchiveException(
    val code: DiagnosticCode,
    val entryName: String?,
    override val message: String,
) : Exception(message)

/** Raw central-directory metadata for one validated entry; offsets lie inside the archive bytes. */
internal class ZipEntry(
    val name: String,
    val method: Int,
    val crc: Int,
    val compressedSize: Int,
    val size: Int,
    val localOffset: Int,
    val dataStart: Int,
) {
    val isDirectory: Boolean get() = name.endsWith('/')
    val dataEnd: Int get() = dataStart + compressedSize
}

/**
 * Bounded in-memory ZIP reader for untrusted `.mxl` input. Every central-directory record is
 * validated from raw bytes before any entry is expanded: names (no normalization), duplicates,
 * encryption, method, ZIP64/multi-disk markers, declared sizes and ratios, local-header agreement
 * and overlapping data. Entries are inflated with Okio only up to their declared size and CRC-32
 * checked. Okio's `openZip` is deliberately not used because it normalizes and collapses names.
 */
internal class ZipArchive private constructor(private val bytes: ByteArray, val entries: List<ZipEntry>) {

    /** Exact raw-name lookup; names were already checked to be unique case-insensitively. */
    fun entry(name: String): ZipEntry? = entries.firstOrNull { it.name == name }

    /** Expands [entry] after checking its declared size against [maxBytes]. */
    fun read(entry: ZipEntry, maxBytes: Int): ByteArray {
        if (entry.size > maxBytes) {
            throw ArchiveException(DiagnosticCode.ARCHIVE_ENTRY_LIMIT, entry.name, "Archive entry exceeds its expanded byte limit")
        }
        val content = if (entry.method == METHOD_STORED) {
            bytes.copyOfRange(entry.dataStart, entry.dataEnd)
        } else {
            inflate(entry)
        }
        if (crc32(content) != entry.crc) {
            throw ArchiveException(DiagnosticCode.ARCHIVE_READ_FAILED, entry.name, "Archive entry CRC-32 mismatch")
        }
        return content
    }

    private fun inflate(entry: ZipEntry): ByteArray {
        val compressed = Buffer().write(bytes, entry.dataStart, entry.compressedSize)
        val expanded = Buffer()
        try {
            InflaterSource(compressed, Inflater(true)).use { source ->
                // Read one byte beyond the declared size so a lying directory cannot expand further.
                val readLimit = entry.size.toLong() + 1
                while (expanded.size < readLimit) {
                    if (source.read(expanded, readLimit - expanded.size) == -1L) break
                }
            }
        } catch (_: IOException) {
            throw ArchiveException(DiagnosticCode.ARCHIVE_READ_FAILED, entry.name, "Archive entry cannot be inflated")
        }
        if (expanded.size != entry.size.toLong()) {
            throw ArchiveException(DiagnosticCode.ARCHIVE_READ_FAILED, entry.name, "Expanded size differs from the central directory")
        }
        return expanded.readByteArray()
    }

    companion object {
        /** Validates the whole directory; throws [ArchiveException] on the first unsafe or unsupported record. */
        fun open(bytes: ByteArray, limits: ArchiveLimits, isCancelled: () -> Boolean): ZipArchive {
            if (bytes.size > limits.maxArchiveBytes) throw failure(DiagnosticCode.ARCHIVE_LIMIT, null, "Archive byte limit exceeded")
            val reader = RecordReader(bytes)
            val end = findEndRecord(reader)
            if (end >= 20 && reader.u32(end - 20) == ZIP64_LOCATOR_SIGNATURE) {
                throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, null, "ZIP64 archives are not supported")
            }
            val disk = reader.u16(end + 4)
            val directoryDisk = reader.u16(end + 6)
            val diskEntries = reader.u16(end + 8)
            val entryCount = reader.u16(end + 10)
            val directorySize = reader.u32(end + 12)
            val directoryOffset = reader.u32(end + 16)
            if (entryCount == 0xFFFF || directorySize == 0xFFFFFFFFL || directoryOffset == 0xFFFFFFFFL) {
                throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, null, "ZIP64 archives are not supported")
            }
            if (disk != 0 || directoryDisk != 0 || diskEntries != entryCount) {
                throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, null, "Multi-disk archives are not supported")
            }
            if (entryCount > limits.maxEntries) throw failure(DiagnosticCode.ARCHIVE_LIMIT, null, "Archive entry count limit exceeded")
            if (directoryOffset + directorySize != end.toLong()) {
                throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, null, "Central directory does not end at its end record")
            }

            val entries = mutableListOf<ZipEntry>()
            val keys = mutableSetOf<String>()
            var totalExpanded = 0L
            var position = directoryOffset.toInt()
            repeat(entryCount) {
                if (isCancelled()) throw failure(DiagnosticCode.CANCELLED, null, "Archive reading cancelled")
                reader.require(position, CENTRAL_HEADER_SIZE, end)
                if (reader.u32(position) != CENTRAL_HEADER_SIGNATURE) {
                    throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, null, "Invalid central directory record")
                }
                val flags = reader.u16(position + 8)
                val method = reader.u16(position + 10)
                val crc = reader.u32(position + 16)
                val compressedSize = reader.u32(position + 20)
                val size = reader.u32(position + 24)
                val nameLength = reader.u16(position + 28)
                val recordLength = CENTRAL_HEADER_SIZE + nameLength + reader.u16(position + 30) + reader.u16(position + 32)
                val startDisk = reader.u16(position + 34)
                val localOffset = reader.u32(position + 42)
                reader.require(position, recordLength, end)
                val rawName = bytes.copyOfRange(position + CENTRAL_HEADER_SIZE, position + CENTRAL_HEADER_SIZE + nameLength)
                val name = decodeName(rawName, flags)
                unsafeArchivePath(name)?.let { throw failure(DiagnosticCode.UNSAFE_ARCHIVE, name, "Unsafe archive entry name: $it") }
                if (!keys.add(name.removeSuffix("/").lowercase())) {
                    throw failure(DiagnosticCode.UNSAFE_ARCHIVE, name, "Duplicate archive entry name")
                }
                if (flags and ENCRYPTION_FLAGS != 0 || method == METHOD_AES) {
                    throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, name, "Encrypted archive entries are not supported")
                }
                if (compressedSize == 0xFFFFFFFFL || size == 0xFFFFFFFFL || localOffset == 0xFFFFFFFFL || startDisk == 0xFFFF) {
                    throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, name, "ZIP64 entries are not supported")
                }
                if (startDisk != 0) throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, name, "Multi-disk archives are not supported")
                if (method != METHOD_STORED && method != METHOD_DEFLATED) {
                    throw failure(DiagnosticCode.UNSUPPORTED_ARCHIVE, name, "Only STORE and DEFLATE entries are supported")
                }
                if (name.endsWith('/') && size != 0L) {
                    throw failure(DiagnosticCode.UNSAFE_ARCHIVE, name, "Directory entry has content")
                }
                totalExpanded += size
                if (totalExpanded > limits.maxTotalExpandedBytes) {
                    throw failure(DiagnosticCode.ARCHIVE_LIMIT, name, "Archive total expanded byte limit exceeded")
                }
                if (size > compressedSize * limits.maxCompressionRatio) {
                    throw failure(DiagnosticCode.ARCHIVE_LIMIT, name, "Archive entry compression ratio limit exceeded")
                }
                if (method == METHOD_STORED && size != compressedSize) {
                    throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, name, "Stored entry sizes differ")
                }
                val local = localOffset.toInt()
                reader.require(local, LOCAL_HEADER_SIZE, directoryOffset.toInt())
                if (reader.u32(local) != LOCAL_HEADER_SIGNATURE) {
                    throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, name, "Invalid local entry header")
                }
                val localNameLength = reader.u16(local + 26)
                val dataStart = local.toLong() + LOCAL_HEADER_SIZE + localNameLength + reader.u16(local + 28)
                if (dataStart + compressedSize > directoryOffset) {
                    throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, name, "Entry data overlaps the central directory")
                }
                val localName = bytes.copyOfRange(local + LOCAL_HEADER_SIZE, local + LOCAL_HEADER_SIZE + localNameLength)
                if (!localName.contentEquals(rawName) || reader.u16(local + 8) != method ||
                    (reader.u16(local + 6) xor flags) and ENCRYPTION_FLAGS != 0
                ) {
                    throw failure(DiagnosticCode.UNSAFE_ARCHIVE, name, "Local entry header disagrees with the central directory")
                }
                entries += ZipEntry(name, method, crc.toInt(), compressedSize.toInt(), size.toInt(), local, dataStart.toInt())
                position += recordLength
            }
            if (position != end) throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, null, "Central directory size mismatch")
            // Overlapping entries let a small archive expand the same compressed bytes repeatedly.
            entries.sortedBy { it.localOffset }.zipWithNext { previous, next ->
                if (next.localOffset < previous.dataEnd) {
                    throw failure(DiagnosticCode.UNSAFE_ARCHIVE, next.name, "Archive entry data overlaps another entry")
                }
            }
            return ZipArchive(bytes, entries)
        }

        private fun findEndRecord(reader: RecordReader): Int {
            val size = reader.size
            var position = size - END_RECORD_SIZE
            val earliest = maxOf(0, size - END_RECORD_SIZE - 0xFFFF)
            while (position >= earliest) {
                // The end record and its comment must finish the archive exactly; no trailing data.
                if (reader.u32(position) == END_RECORD_SIGNATURE && position + END_RECORD_SIZE + reader.u16(position + 20) == size) {
                    return position
                }
                position--
            }
            throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, null, "Missing ZIP end of central directory record")
        }

        private fun decodeName(raw: ByteArray, flags: Int): String {
            if (flags and UTF8_FLAG != 0) {
                return try {
                    raw.decodeToString(throwOnInvalidSequence = true)
                } catch (_: CharacterCodingException) {
                    throw failure(DiagnosticCode.UNSAFE_ARCHIVE, null, "Archive entry name is not valid UTF-8")
                }
            }
            // Without the UTF-8 flag the name is CP437; only its unambiguous ASCII subset is accepted.
            if (raw.any { it < 0x20 || it > 0x7E }) {
                throw failure(DiagnosticCode.UNSAFE_ARCHIVE, null, "Non-ASCII archive entry name without the UTF-8 flag")
            }
            return raw.decodeToString()
        }

        private fun failure(code: DiagnosticCode, entryName: String?, message: String) = ArchiveException(code, entryName, message)
    }

    private class RecordReader(private val bytes: ByteArray) {
        val size: Int get() = bytes.size

        fun require(position: Int, length: Int, limit: Int) {
            if (position < 0 || position.toLong() + length > limit) {
                throw failure(DiagnosticCode.ARCHIVE_READ_FAILED, null, "Truncated archive record")
            }
        }

        fun u16(position: Int): Int = (bytes[position].toInt() and 0xFF) or ((bytes[position + 1].toInt() and 0xFF) shl 8)

        fun u32(position: Int): Long = u16(position).toLong() or (u16(position + 2).toLong() shl 16)
    }
}

/**
 * Returns why an archive entry or rootfile path is unsafe, or null. Paths are relative, use `/`,
 * and contain no empty, `.` or `..` segment, backslash, drive/stream colon or control character.
 * A single trailing `/` marks a directory entry.
 */
internal fun unsafeArchivePath(path: String): String? = when {
    path.isEmpty() -> "empty path"
    path.startsWith('/') -> "absolute path"
    path.any { it == '\\' || it == ':' || it < ' ' || it == '\u007F' } -> "backslash, colon or control character"
    path.removeSuffix("/").split('/').any { it.isEmpty() || it == "." || it == ".." } -> "empty, current or parent directory segment"
    else -> null
}

private val CRC_TABLE = IntArray(256) { index ->
    var value = index
    repeat(8) { value = if (value and 1 != 0) (value ushr 1) xor 0xEDB88320.toInt() else value ushr 1 }
    value
}

/** CRC-32 (ISO-HDLC), as stored in ZIP records. */
internal fun crc32(data: ByteArray): Int {
    var crc = -1
    for (byte in data) crc = CRC_TABLE[(crc xor byte.toInt()) and 0xFF] xor (crc ushr 8)
    return crc.inv()
}

private const val END_RECORD_SIGNATURE = 0x06054b50L
private const val ZIP64_LOCATOR_SIGNATURE = 0x07064b50L
private const val CENTRAL_HEADER_SIGNATURE = 0x02014b50L
private const val LOCAL_HEADER_SIGNATURE = 0x04034b50L
private const val END_RECORD_SIZE = 22
private const val CENTRAL_HEADER_SIZE = 46
private const val LOCAL_HEADER_SIZE = 30
internal const val METHOD_STORED = 0
private const val METHOD_DEFLATED = 8
private const val METHOD_AES = 99
private const val UTF8_FLAG = 0x0800

/** Traditional (bit 0), strong (bit 6) and central-directory (bit 13) encryption flags. */
private const val ENCRYPTION_FLAGS = 0x2041
