package com.ryanjames.sheetmusic.musicxml

import okio.ByteString.Companion.decodeHex
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CorpusRunnerTest {
    private val directory: File = createTempDirectory("corpus-runner").toFile()

    @AfterTest
    fun deleteCorpus() {
        directory.deleteRecursively()
    }

    private fun file(name: String, content: ByteArray): File = directory.resolve(name).apply { writeBytes(content) }

    private fun file(name: String, content: String): File = file(name, content.encodeToByteArray())

    @Test
    fun testReportsBothPoliciesFirstErrorAndDiscardedFeatures() {
        val files = listOf(
            file("c-width.musicxml", f20Score.replace("<measure number=\"1\">", "<measure number=\"1\" width=\"200\">")),
            file("a-clean.musicxml", f20Score),
            file("b-packed.mxl", f20MxlHex.decodeHex().toByteArray()),
            file("d-blank-part-id.invalid.musicxml", f20Score.replace("<part id=\"P1\">", "<part id=\"\">")),
        )
        val report = runCorpus("fixture corpus", files)

        assertEquals(listOf("a-clean.musicxml", "b-packed.mxl", "c-width.musicxml", "d-blank-part-id.invalid.musicxml"), report.files.map { it.name })
        assertEquals(emptyList(), report.problems)
        val (clean, packed, width, blankPartId) = report.files
        listOf(clean, packed).forEach { file ->
            assertTrue(file.outcomes.values.all { it.outcome == "accepted" && it.discarded.isEmpty() }, file.name)
        }
        assertEquals("rejected: UNSUPPORTED_FEATURE", width.outcomes.getValue(ParsePolicy.STRICT).outcome)
        assertEquals("accepted with warnings", width.outcomes.getValue(ParsePolicy.TOLERANT).outcome)
        assertEquals(mapOf("S01" to 1), width.outcomes.getValue(ParsePolicy.TOLERANT).discarded)
        assertTrue(blankPartId.expectedDiagnosed)
        assertTrue(blankPartId.outcomes.values.all { it.firstError?.code == DiagnosticCode.INVALID_STRUCTURE })

        assertEquals(mapOf("accepted" to 2, "rejected: INVALID_STRUCTURE" to 1, "rejected: UNSUPPORTED_FEATURE" to 1), report.summary(ParsePolicy.STRICT))
        assertEquals(mapOf("S01" to 1), report.filesPerDiscardedFeature())
        assertEquals(emptyList(), report.tolerantRejectionsOfValidFiles())
        val markdown = report.toMarkdown()
        assertTrue("# MusicXML corpus report: fixture corpus" in markdown)
        assertTrue("4 files; 1 expected to be diagnosed." in markdown)
        assertTrue(
            markdown.lines().any {
                it.startsWith("| `c-width.musicxml` | valid | rejected: UNSUPPORTED_FEATURE | UNSUPPORTED_FEATURE `/score-partwise[1]/part[1]/measure[1]/@width`") &&
                    it.endsWith("| accepted with warnings |  | S01 1 |")
            },
            markdown,
        )
    }

    @Test
    fun testFlagsCleanAcceptanceOfExpectedDiagnosedFiles() {
        val report = runCorpus("fixture corpus", listOf(file("a-clean.invalid.musicxml", f20Score)))

        assertEquals(
            ParsePolicy.entries.map { "a-clean.invalid.musicxml $it accepted without a diagnostic" },
            report.problems,
        )
    }

    @Test
    fun testListsValidFilesRejectedUnderTolerantPolicy() {
        val report = runCorpus("fixture corpus", listOf(file("a-unknown-part.musicxml", f20Score.replace("<part id=\"P1\">", "<part id=\"P9\">"))))

        assertEquals(listOf("a-unknown-part.musicxml"), report.tolerantRejectionsOfValidFiles())
        assertTrue(isExpectedDiagnosed("11b-TimeSignatures-NoTime.musicxml"))
        assertTrue(!isExpectedDiagnosed("11a-TimeSignatures.musicxml"))
    }
}
