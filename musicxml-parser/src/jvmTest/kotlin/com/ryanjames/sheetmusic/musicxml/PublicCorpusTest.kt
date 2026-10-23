package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.playbackOrder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Opt-in MX-21 corpus run. Set `MUSICXML_CORPUS_DIR` to a directory of `.musicxml`/`.xml` files,
 * such as the pinned W3C test suite's `xmlFiles`, to parse each one under both policies and print
 * an outcome summary. Files are not committed; see `docs/musicxml/compliance-audit.md`.
 */
class PublicCorpusTest {
    @Test
    fun testCorpusOutcomesAreStructured() {
        val directory = System.getenv("MUSICXML_CORPUS_DIR")?.let(::File) ?: return
        val files = directory.walkTopDown()
            .filter { it.isFile && it.extension in setOf("musicxml", "xml") }
            .sortedBy { it.name }
            .toList()
        assertTrue(files.isNotEmpty(), "No MusicXML files in $directory")
        val problems = mutableListOf<String>()
        val outcomes = ParsePolicy.entries.associateWith { mutableMapOf<String, Int>() }
        val features = sortedMapOf<String, Int>()
        files.forEach { file ->
            ParsePolicy.entries.forEach { policy ->
                val result = try {
                    parseMusicXml(file.readBytes(), ParseOptions(policy), file.name)
                } catch (exception: Exception) {
                    problems += "${file.name} $policy threw $exception"
                    return@forEach
                }
                val outcome = when (result) {
                    is ParseResult.Success<Score> -> {
                        result.value.parts.forEach { it.playbackOrder() }
                        if (result.diagnostics.isEmpty()) "accepted" else "accepted with warnings"
                    }
                    is ParseResult.Failure -> {
                        if (result.diagnostics.none { it.severity == DiagnosticSeverity.ERROR }) problems += "${file.name} failed without an error"
                        val error = result.diagnostics.first { it.severity == DiagnosticSeverity.ERROR }
                        println("CORPUS ERROR ${file.name} $policy ${error.code} ${error.location.path} ${error.message}")
                        "rejected: " + error.code
                    }
                }
                outcomes.getValue(policy).merge(outcome, 1, Int::plus)
                if (policy == ParsePolicy.TOLERANT) {
                    result.diagnostics.mapNotNull { it.unsupported?.featureId }.distinct()
                        .forEach { features.merge(it, 1, Int::plus) }
                }
                println("CORPUS ${file.name} $policy $outcome")
            }
        }
        println("CORPUS SUMMARY ${files.size} files")
        outcomes.forEach { (policy, counts) -> println("CORPUS SUMMARY $policy ${counts.toSortedMap()}") }
        println("CORPUS SUMMARY tolerant files per unsupported feature $features")
        if (problems.isNotEmpty()) fail(problems.joinToString("\n"))
    }
}
