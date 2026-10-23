package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Score
import okio.ByteString.Companion.decodeHex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MxlImportTest {
    private val f20Mxl = f20MxlHex.decodeHex().toByteArray()

    @Test
    fun testF20SelectsFirstRootfileAndEqualsUncompressedScore() {
        val expected = assertIs<ParseResult.Success<Score>>(parseMusicXml(f20Score.encodeToByteArray())).value
        val result = assertIs<ParseResult.Success<Score>>(parseMxl(f20Mxl, sourceName = "f20.mxl"))
        assertEquals(expected, result.value)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.provenance.isNotEmpty())
        assertTrue(result.provenance.all { it.location.sourceName == "f20.mxl!/scores/f20.musicxml" })
        assertEquals("Packed", result.value.title)
    }

    @Test
    fun testStoredAndDeflatedArchivesWithoutMimetypeEqualXml() {
        val expected = assertIs<ParseResult.Success<Score>>(parseMusicXml(f20Score.encodeToByteArray())).value
        for (deflate in listOf(false, true)) {
            assertEquals(expected, success(buildZip(scoreEntries(deflate = deflate))).value)
        }
        // An omitted media type defaults to MusicXML; a missing later rendition is never read.
        val renditions = listOf(
            TestZipEntry("META-INF/container.xml", containerXml("score.musicxml" to null, "score.pdf" to "application/pdf")),
            TestZipEntry("score.musicxml", f20Score),
            TestZipEntry("META-INF/", ""),
        )
        assertEquals(expected, success(buildZip(renditions)).value)
    }

    @Test
    fun testTraversalAndUnsafeEntryNamesRejected() {
        val names = listOf(
            "../score.musicxml", "/score.musicxml", "scores\\score.musicxml", "C:/score.musicxml", "a/./score.musicxml",
            "a//score.musicxml", "a/../../score.musicxml", "score.musicxml\u0001", "",
        )
        for (name in names) failure(buildZip(scoreEntries() + TestZipEntry(name, "x")), DiagnosticCode.UNSAFE_ARCHIVE)
        val traversal = failure(buildZip(scoreEntries() + TestZipEntry(names[0], "x")), DiagnosticCode.UNSAFE_ARCHIVE)
        assertEquals("song.mxl!/../score.musicxml", traversal.diagnostics.single().location.sourceName)
        val latin1 = TestZipEntry("caf.xml", "x".encodeToByteArray(), rawName = byteArrayOf(0x63, 0x61, 0x66, 0xE9.toByte()))
        failure(buildZip(scoreEntries() + latin1), DiagnosticCode.UNSAFE_ARCHIVE)
        val invalidUtf8 = TestZipEntry("bad", "x".encodeToByteArray(), flags = 0x0800, rawName = byteArrayOf(0xC3.toByte()))
        failure(buildZip(scoreEntries() + invalidUtf8), DiagnosticCode.UNSAFE_ARCHIVE)
        val utf8 = TestZipEntry("café.xml", "x".encodeToByteArray(), flags = 0x0800)
        success(buildZip(scoreEntries() + utf8))
    }

    @Test
    fun testDuplicateEntriesAndRootfilesRejected() {
        for (duplicate in listOf("score.musicxml", "SCORE.musicxml")) {
            failure(buildZip(scoreEntries() + TestZipEntry(duplicate, "<other/>")), DiagnosticCode.UNSAFE_ARCHIVE)
        }
        failure(buildZip(scoreEntries() + TestZipEntry("score.musicxml/", "")), DiagnosticCode.UNSAFE_ARCHIVE)
        val container = containerXml("score.musicxml" to MUSICXML_TYPE, "Score.musicxml" to "application/pdf")
        val result = failure(buildZip(withContainer(container)), DiagnosticCode.UNSAFE_ARCHIVE)
        assertEquals("song.mxl!/META-INF/container.xml", result.diagnostics.single().location.sourceName)
        assertEquals("/container[1]/rootfiles[1]/rootfile[2]", result.diagnostics.single().location.path)
        for (path in listOf("../score.musicxml", "/score.musicxml", "scores/../score.musicxml")) {
            failure(buildZip(withContainer(containerXml(path to MUSICXML_TYPE))), DiagnosticCode.UNSAFE_ARCHIVE)
        }
    }

    @Test
    fun testEncryptedAndUnsupportedEntriesRejected() {
        val encrypted = listOf(
            TestZipEntry("secret.xml", "x".encodeToByteArray(), flags = 0x0001),
            TestZipEntry("strong.xml", "x".encodeToByteArray(), flags = 0x0041),
            TestZipEntry("aes.xml", "x".encodeToByteArray(), deflate = false, method = 99),
        )
        for (entry in encrypted) {
            val result = failure(buildZip(scoreEntries() + entry), DiagnosticCode.UNSUPPORTED_ARCHIVE)
            assertTrue("Encrypted" in result.diagnostics.single().message)
        }
        val bzip2 = TestZipEntry("score.bz2", "x".encodeToByteArray(), deflate = false, method = 12)
        failure(buildZip(scoreEntries() + bzip2), DiagnosticCode.UNSUPPORTED_ARCHIVE)
        val zip64Size = TestZipEntry("big.xml", "x".encodeToByteArray(), deflate = false, declaredSize = 0xFFFFFFFFL)
        failure(buildZip(scoreEntries() + zip64Size), DiagnosticCode.UNSUPPORTED_ARCHIVE)
        val archive = buildZip(scoreEntries())
        val zip64Locator = byteArrayOf(0x50, 0x4b, 0x06, 0x07) + ByteArray(16)
        val withLocator = archive.copyOfRange(0, archive.size - 22) + zip64Locator + archive.copyOfRange(archive.size - 22, archive.size)
        failure(withLocator, DiagnosticCode.UNSUPPORTED_ARCHIVE)
        failure(buildZip(scoreEntries()) { it[4] = 1 }, DiagnosticCode.UNSUPPORTED_ARCHIVE)
    }

    @Test
    fun testExpansionBombsAndArchiveBudgetsRejected() {
        val zeros = TestZipEntry("zeros.bin", ByteArray(1_048_576))
        val ratio = failure(buildZip(scoreEntries() + zeros), DiagnosticCode.ARCHIVE_LIMIT)
        assertEquals("song.mxl!/zeros.bin", ratio.diagnostics.single().location.sourceName)
        success(buildZip(scoreEntries() + zeros), archiveLimits = ArchiveLimits(maxCompressionRatio = 2_000))

        val archive = buildZip(scoreEntries())
        val total = scoreEntries().sumOf { it.content.size }.toLong()
        success(archive, archiveLimits = ArchiveLimits(maxTotalExpandedBytes = total))
        failure(archive, DiagnosticCode.ARCHIVE_LIMIT, archiveLimits = ArchiveLimits(maxTotalExpandedBytes = total - 1))
        success(archive, archiveLimits = ArchiveLimits(maxEntries = 2))
        failure(archive, DiagnosticCode.ARCHIVE_LIMIT, archiveLimits = ArchiveLimits(maxEntries = 1))
        success(archive, archiveLimits = ArchiveLimits(maxArchiveBytes = archive.size))
        failure(archive, DiagnosticCode.ARCHIVE_LIMIT, archiveLimits = ArchiveLimits(maxArchiveBytes = archive.size - 1))

        // Declared expansion is checked before inflating; selected XML also obeys maxInputBytes.
        val scoreBytes = f20Score.encodeToByteArray().size
        success(archive, ParseOptions(limits = ParseLimits(maxInputBytes = scoreBytes)))
        val entry = failure(archive, DiagnosticCode.ARCHIVE_ENTRY_LIMIT, ParseOptions(limits = ParseLimits(maxInputBytes = scoreBytes - 1)))
        assertEquals("song.mxl!/score.musicxml", entry.diagnostics.single().location.sourceName)
        val container = failure(archive, DiagnosticCode.ARCHIVE_ENTRY_LIMIT, archiveLimits = ArchiveLimits(maxContainerBytes = 10))
        assertEquals("song.mxl!/META-INF/container.xml", container.diagnostics.single().location.sourceName)

        // A directory that understates the size cannot expand past it.
        val lying = listOf(scoreEntries()[0], TestZipEntry("score.musicxml", f20Score.encodeToByteArray(), declaredSize = 100))
        failure(buildZip(lying), DiagnosticCode.ARCHIVE_READ_FAILED)
    }

    @Test
    fun testOverlappingEntriesRejected() {
        val inner = TestZipEntry("score.musicxml", f20Score, deflate = false)
        val outerName = "outer.bin"
        val outer = TestZipEntry(outerName, inner.localRecord(), deflate = false)
        val overlapping = TestZipEntry("score.musicxml", f20Score.encodeToByteArray(), deflate = false, offset = 30 + outerName.length)
        val result = failure(buildZip(listOf(outer, overlapping, scoreEntries()[0])), DiagnosticCode.UNSAFE_ARCHIVE)
        assertEquals("song.mxl!/score.musicxml", result.diagnostics.single().location.sourceName)
        // Two central records sharing one local record disagree on the name.
        val shared = TestZipEntry("copy.musicxml", f20Score.encodeToByteArray(), deflate = false, offset = 0)
        failure(buildZip(listOf(inner, shared, scoreEntries()[0])), DiagnosticCode.UNSAFE_ARCHIVE)
    }

    @Test
    fun testCorruptArchivesRejected() {
        failure("not a ZIP".encodeToByteArray(), DiagnosticCode.ARCHIVE_READ_FAILED)
        failure(ByteArray(0), DiagnosticCode.ARCHIVE_READ_FAILED)
        val archive = buildZip(scoreEntries())
        failure(archive.copyOf(archive.size - 1), DiagnosticCode.ARCHIVE_READ_FAILED)
        failure(archive + byteArrayOf(0), DiagnosticCode.ARCHIVE_READ_FAILED)
        failure(buildZip(scoreEntries(), prefix = byteArrayOf(1, 2, 3)) { it[16] = 0 }, DiagnosticCode.ARCHIVE_READ_FAILED)
        val badCrc = listOf(scoreEntries()[0], TestZipEntry("score.musicxml", f20Score.encodeToByteArray(), crc = 1))
        failure(buildZip(badCrc), DiagnosticCode.ARCHIVE_READ_FAILED)
        val corruptDeflate = archive.copyOf().also { bytes ->
            val start = bytes.indexOfSequence("score.musicxml".encodeToByteArray(), from = 40) + "score.musicxml".length
            bytes.fill(0xFF.toByte(), start, start + 8)
        }
        failure(corruptDeflate, DiagnosticCode.ARCHIVE_READ_FAILED)
    }

    @Test
    fun testContainerRules() {
        failure(buildZip(scoreEntries().drop(1)), DiagnosticCode.INVALID_STRUCTURE)
        val cases = listOf(
            "<container><rootfiles>" to DiagnosticCode.MALFORMED_XML,
            "<!DOCTYPE container><container/>" to DiagnosticCode.UNSAFE_XML,
            "<score-partwise/>" to DiagnosticCode.UNSUPPORTED_ROOT,
            "<container xmlns='urn:oasis:names:tc:opendocument:xmlns:container'/>" to DiagnosticCode.UNSUPPORTED_NAMESPACE,
            "<container><rootfiles/></container>" to DiagnosticCode.INVALID_STRUCTURE,
            "<container/>" to DiagnosticCode.INVALID_STRUCTURE,
            "<container><rootfiles><rootfile/></rootfiles></container>" to DiagnosticCode.INVALID_STRUCTURE,
            "<container><rootfiles/><rootfiles/></container>" to DiagnosticCode.INVALID_STRUCTURE,
            containerXml("score.pdf" to "application/pdf", "score.musicxml" to MUSICXML_TYPE) to DiagnosticCode.INVALID_STRUCTURE,
            containerXml("missing.musicxml" to MUSICXML_TYPE) to DiagnosticCode.INVALID_STRUCTURE,
            containerXml("META-INF/" to MUSICXML_TYPE) to DiagnosticCode.INVALID_STRUCTURE,
        )
        for ((container, code) in cases) {
            val result = failure(buildZip(withContainer(container) + TestZipEntry("META-INF/", "")), code)
            assertEquals("song.mxl!/META-INF/container.xml", result.diagnostics.single().location.sourceName)
        }
        // Score-level findings name the selected entry.
        val malformed = failure(buildZip(scoreEntries("<score-partwise>")), DiagnosticCode.MALFORMED_XML)
        assertEquals("song.mxl!/score.musicxml", malformed.diagnostics.single().location.sourceName)
    }

    @Test
    fun testLaterMusicXmlRootfilesReportedAsDiscarded() {
        val expected = success(buildZip(scoreEntries())).value
        // 4.0 score-and-parts packaging: the score is imported, each later MusicXML part is S19 loss.
        val container = containerXml(
            "score.musicxml" to MUSICXML_TYPE,
            "score.pdf" to "application/pdf",
            "part1.musicxml" to MUSICXML_TYPE,
            "part2.musicxml" to null,
        )
        val archive = buildZip(withContainer(container))
        val tolerant = success(archive, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(expected, tolerant.value)
        assertEquals(
            listOf("/container[1]/rootfiles[1]/rootfile[3]", "/container[1]/rootfiles[1]/rootfile[4]"),
            tolerant.diagnostics.map { it.location.path },
        )
        for (diagnostic in tolerant.diagnostics) {
            assertEquals(DiagnosticCode.UNSUPPORTED_FEATURE to DiagnosticSeverity.WARNING, diagnostic.code to diagnostic.severity)
            assertEquals(UnsupportedContent("S19", ContentRetention.DISCARDED), diagnostic.unsupported)
            assertEquals("song.mxl!/META-INF/container.xml", diagnostic.location.sourceName)
        }
        failure(archive, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testMimetypeRules() {
        val mimetype = TestZipEntry("mimetype", "application/vnd.recordare.musicxml", deflate = false)
        val expected = success(buildZip(scoreEntries())).value
        assertEquals(expected, success(buildZip(listOf(mimetype) + scoreEntries())).value)
        for (content in listOf("application/vnd.recordare.musicxml+xml", "application/zip", "")) {
            val wrong = TestZipEntry("mimetype", content, deflate = false)
            val result = failure(buildZip(listOf(wrong) + scoreEntries()), DiagnosticCode.INVALID_STRUCTURE)
            assertEquals("song.mxl!/mimetype", result.diagnostics.single().location.sourceName)
        }
        val compressed = TestZipEntry("mimetype", "application/vnd.recordare.musicxml")
        for (archive in listOf(buildZip(scoreEntries() + mimetype), buildZip(listOf(compressed) + scoreEntries()))) {
            failure(archive, DiagnosticCode.RECOVERED_VALUE)
            val tolerant = success(archive, ParseOptions(ParsePolicy.TOLERANT))
            assertEquals(expected, tolerant.value)
            val warning = tolerant.diagnostics.single()
            assertEquals(DiagnosticCode.RECOVERED_VALUE to DiagnosticSeverity.WARNING, warning.code to warning.severity)
        }
        // "First" means physically first in the archive, not first in the central directory.
        val physicallyFirst = buildZip(listOf(mimetype) + scoreEntries(), directoryOrder = listOf(1, 2, 0))
        assertEquals(expected, success(physicallyFirst).value)
        val listedFirst = buildZip(scoreEntries() + mimetype, directoryOrder = listOf(2, 0, 1))
        failure(listedFirst, DiagnosticCode.RECOVERED_VALUE)
    }

    @Test
    fun testArchiveWarningsShareTheDiagnosticCap() {
        val keyCancel = f20Score.replace("<divisions>1</divisions>", "<divisions>1</divisions><key><cancel>2</cancel><fifths>0</fifths></key>")
        val mimetype = TestZipEntry("mimetype", "application/vnd.recordare.musicxml")
        val archive = buildZip(listOf(mimetype) + scoreEntries(keyCancel))
        val tolerant = success(archive, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(DiagnosticCode.RECOVERED_VALUE, DiagnosticCode.UNSUPPORTED_FEATURE), tolerant.diagnostics.map { it.code })
        val capped = ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxDiagnostics = 1))
        assertEquals(DiagnosticCode.DIAGNOSTIC_LIMIT, failure(archive, DiagnosticCode.DIAGNOSTIC_LIMIT, capped).diagnostics.single().code)
    }

    @Test
    fun testCancellation() {
        failure(f20Mxl, DiagnosticCode.CANCELLED, isCancelled = { true })
        var checks = 0
        val result = parseMxl(f20Mxl, isCancelled = { ++checks > 3 })
        assertEquals(DiagnosticCode.CANCELLED, assertIs<ParseResult.Failure>(result).diagnostics.single().code)
    }

    private fun withContainer(container: String): List<TestZipEntry> =
        listOf(TestZipEntry("META-INF/container.xml", container)) + scoreEntries().drop(1)

    private fun success(
        archive: ByteArray,
        options: ParseOptions = ParseOptions(),
        archiveLimits: ArchiveLimits = ArchiveLimits(),
    ): ParseResult.Success<Score> = assertIs(parseMxl(archive, options, archiveLimits, "song.mxl"))

    private fun failure(
        archive: ByteArray,
        code: DiagnosticCode,
        options: ParseOptions = ParseOptions(),
        archiveLimits: ArchiveLimits = ArchiveLimits(),
        isCancelled: () -> Boolean = { false },
    ): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMxl(archive, options, archiveLimits, "song.mxl", isCancelled))
        assertEquals(code, result.diagnostics.last().code, result.diagnostics.toString())
        return result
    }

    private fun ByteArray.indexOfSequence(sequence: ByteArray, from: Int): Int =
        (from..size - sequence.size).first { start -> sequence.indices.all { this[start + it] == sequence[it] } }
}
