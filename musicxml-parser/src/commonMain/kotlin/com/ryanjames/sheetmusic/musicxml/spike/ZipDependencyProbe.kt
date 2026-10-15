package com.ryanjames.sheetmusic.musicxml.spike

import okio.Buffer
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.openZip
import okio.use

internal data class ZipProbeResult(
    val bytes: ByteArray? = null,
    val diagnostic: ProbeDiagnostic? = null,
)

/**
 * Compile/runtime evidence for seekable ZIP input and bounded entry reads on JVM/Android/Native.
 * Only for trusted test archives: openZip normalizes paths and collapses duplicate entries, so this
 * is deliberately not an MXL adapter. MX-19 must validate raw directory metadata before openZip.
 */
internal fun probeZipEntry(
    fileSystem: FileSystem,
    archive: Path,
    entry: Path,
    maxBytes: Int,
): ZipProbeResult {
    require(maxBytes > 0 && maxBytes < Int.MAX_VALUE)
    return try {
        val zip = fileSystem.openZip(archive)
        zip.source(entry).use { source ->
            val buffer = Buffer()
            val readLimit = maxBytes.toLong() + 1
            // Read one byte beyond the allowed size to distinguish an exact fit from overflow.
            while (buffer.size < readLimit) {
                if (source.read(buffer, readLimit - buffer.size) == -1L) {
                    return@use ZipProbeResult(bytes = buffer.readByteArray())
                }
            }
            ZipProbeResult(diagnostic = ProbeDiagnostic(ProbeCode.ARCHIVE_ENTRY_LIMIT, "ZIP entry byte limit exceeded"))
        }
    } catch (_: IOException) {
        ZipProbeResult(diagnostic = ProbeDiagnostic(ProbeCode.ARCHIVE_READ_FAILED, "ZIP entry could not be read"))
    }
}
