package com.ryanjames.sheetmusic.musicxml

import okio.Buffer
import okio.Deflater
import okio.DeflaterSink
import okio.use

/**
 * Original F20 score, authored for this repository. Expected XSD result: valid (not validated by
 * these tests). It is the exact UTF-8 content of `scores/f20.musicxml` in [f20MxlHex].
 */
internal const val f20Score: String = "<score-partwise version=\"4.0\"><work><work-title>Packed</work-title></work>" +
    "<part-list><score-part id=\"P1\"><part-name>Flute</part-name></score-part></part-list><part id=\"P1\">" +
    "<measure number=\"1\"><attributes><divisions>1</divisions><time><beats>4</beats><beat-type>4</beat-type></time>" +
    "<clef><sign>G</sign><line>2</line></clef></attributes><note><pitch><step>C</step><octave>5</octave></pitch>" +
    "<duration>4</duration><type>whole</type></note></measure></part></score-partwise>"

/**
 * Original F20 `.mxl`, generated for this project with .NET `System.IO.Compression.ZipArchive`
 * (an independent ZIP writer). Entries in order: `mimetype` (STORE,
 * `application/vnd.recordare.musicxml`), `META-INF/container.xml` (DEFLATE) listing two
 * renditions, `scores/f20.musicxml` ([f20Score], DEFLATE) then `application/pdf`
 * `scores/f20.pdf` (DEFLATE, the text `%PDF-1.4 placeholder rendition`).
 */
internal const val f20MxlHex: String =
    "504b03041400000000000060575d2e4215822200000022000000080000006d696d65747970656170706c69636174696f6e2f766e642e" +
        "7265636f72646172652e6d75736963786d6c504b03041400000008000060575db76079bd91000000f6000000160000004d4554412d49" +
        "4e462f636f6e7461696e65722e786d6c858f410ec2201444af42d89a96eaca05b43b4f500f40e0a33f013ef9d0a6dede7463dd1877" +
        "93cccccb8c9eb614c50a5c91b291e77e9002b2238ff961e47dbe7557398dda516e1633f0a899a8058c500f29c21263576c7b1a591d" +
        "3154152e439f968a6e4b518a041e6dd75e058cb4a54474b62165b566df3338626f193ef9d3de517ff1c587dfe4dd54a3565f6bd571" +
        "e20d504b03041400000008000060575d6dfb159eec000000d50100001300000073636f7265732f6632302e6d75736963786d6c5551" +
        "416ec3300cfb4a917ba675e86eac2e03d66bbfe026da2a34b5035b49b0df0f8ebda5bd582225d3048dd48528ede8a22d9a64374b4c" +
        "1afcb139bcbc368c25c45b395b531b84cfaebb490f7aa00a60648d76d0648c4d74a7fdb139ef9b3af6ee2efc394c26a08d006d17b8" +
        "0e8ad0b3c45d5c9aa2ecfc74bf483c36997366512f934962f43a6b769f780fda004cf323177196f8002acd8a5bfb19e58f2b0054d6" +
        "bb41be1849bf3d9f406bc5a05ef80db4565059a1470b3e983046b5eeca4826237f80d68ad0999b85df41b503d5bd7e8ace34f86ce4" +
        "bfc7ea66b9864140d55951a79a438dea29befc89fc0b504b03041400000008000060575d35be361a200000001e0000000e00000073" +
        "636f7265732f6632302e706466530d7071d335d4335128c8494c4ecdc8cf49492d52284acd4bc92cc9cccf0300504b010214001400" +
        "000000000060575d2e42158222000000220000000800000000000000000000000000000000006d696d6574797065504b0102140014" +
        "00000008000060575db76079bd91000000f60000001600000000000000000000000000480000004d4554412d494e462f636f6e7461" +
        "696e65722e786d6c504b010214001400000008000060575d6dfb159eec000000d501000013000000000000000000000000000d0100" +
        "0073636f7265732f6632302e6d75736963786d6c504b010214001400000008000060575d35be361a200000001e0000000e00000000" +
        "0000000000000000002a02000073636f7265732f6632302e706466504b05060000000004000400f7000000760200000000"

internal const val MUSICXML_TYPE: String = "application/vnd.recordare.musicxml+xml"

/** A container listing [rootfiles] (path, optional media type) in order. */
internal fun containerXml(vararg rootfiles: Pair<String, String?>): String = buildString {
    append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><container><rootfiles>")
    for ((path, mediaType) in rootfiles) {
        append("<rootfile full-path=\"").append(path).append('"')
        if (mediaType != null) append(" media-type=\"").append(mediaType).append('"')
        append("/>")
    }
    append("</rootfiles></container>")
}

/**
 * One test entry. Overrides write deliberately inconsistent or unsafe metadata. [rawName] replaces
 * the encoded name bytes; [offset] points the central record at existing bytes instead of writing a
 * local record for this entry.
 */
internal class TestZipEntry(
    val name: String,
    val content: ByteArray,
    val deflate: Boolean = true,
    val flags: Int = 0,
    val method: Int? = null,
    val declaredSize: Long? = null,
    val crc: Int? = null,
    val rawName: ByteArray? = null,
    val offset: Int? = null,
) {
    constructor(name: String, content: String, deflate: Boolean = true) : this(name, content.encodeToByteArray(), deflate)

    val nameBytes: ByteArray get() = rawName ?: name.encodeToByteArray()
    val data: ByteArray by lazy { if (deflate) deflateRaw(content) else content }
    val recordedMethod: Int get() = method ?: if (deflate) 8 else 0
    val recordedCrc: Int get() = crc ?: crc32(content)
    val recordedSize: Long get() = declaredSize ?: content.size.toLong()

    /** Local header followed by the entry data. */
    fun localRecord(): ByteArray = Buffer().apply {
        writeIntLe(0x04034b50); writeShortLe(20); writeShortLe(flags); writeShortLe(recordedMethod)
        writeIntLe(0); writeIntLe(recordedCrc); writeIntLe(data.size); writeIntLe(recordedSize.toInt())
        writeShortLe(nameBytes.size); writeShortLe(0); write(nameBytes); write(data)
    }.readByteArray()
}

/** The standard F20-style layout: a container selecting `score.musicxml` and that score. */
internal fun scoreEntries(score: String = f20Score, deflate: Boolean = true): List<TestZipEntry> = listOf(
    TestZipEntry("META-INF/container.xml", containerXml("score.musicxml" to MUSICXML_TYPE), deflate),
    TestZipEntry("score.musicxml", score, deflate),
)

/** Writes a single-disk ZIP; [endRecordEdit] may patch the 22-byte end record before [trailing]. */
internal fun buildZip(
    entries: List<TestZipEntry>,
    prefix: ByteArray = ByteArray(0),
    trailing: ByteArray = ByteArray(0),
    endRecordEdit: (ByteArray) -> Unit = {},
): ByteArray {
    val body = Buffer().write(prefix)
    val offsets = entries.map { entry ->
        entry.offset ?: body.size.toInt().also { body.write(entry.localRecord()) }
    }
    val directoryOffset = body.size.toInt()
    entries.forEachIndexed { index, entry ->
        body.writeIntLe(0x02014b50); body.writeShortLe(20); body.writeShortLe(20); body.writeShortLe(entry.flags)
        body.writeShortLe(entry.recordedMethod); body.writeIntLe(0); body.writeIntLe(entry.recordedCrc)
        body.writeIntLe(entry.data.size); body.writeIntLe(entry.recordedSize.toInt()); body.writeShortLe(entry.nameBytes.size)
        body.writeShortLe(0); body.writeShortLe(0); body.writeShortLe(0); body.writeShortLe(0); body.writeIntLe(0)
        body.writeIntLe(offsets[index]); body.write(entry.nameBytes)
    }
    val directorySize = body.size.toInt() - directoryOffset
    val end = Buffer().apply {
        writeIntLe(0x06054b50); writeShortLe(0); writeShortLe(0); writeShortLe(entries.size); writeShortLe(entries.size)
        writeIntLe(directorySize); writeIntLe(directoryOffset); writeShortLe(0)
    }.readByteArray()
    endRecordEdit(end)
    return body.write(end).write(trailing).readByteArray()
}

internal fun deflateRaw(content: ByteArray): ByteArray {
    val buffer = Buffer()
    DeflaterSink(buffer, Deflater(9, true)).use { it.write(Buffer().write(content), content.size.toLong()) }
    return buffer.readByteArray()
}
