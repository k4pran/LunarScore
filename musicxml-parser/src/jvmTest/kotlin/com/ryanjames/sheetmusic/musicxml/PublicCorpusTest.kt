package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.playbackOrder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Opt-in MX-21 corpus run. Set `MUSICXML_CORPUS_DIR` to a directory of `.musicxml`/`.xml`/`.mxl`
 * files, such as the pinned W3C test suite's `xmlFiles`, to parse each one under both policies and
 * print an outcome summary. With `MUSICXML_CORPUS_REPORT` set, a per-file report is also written to
 * that path. `scripts/run-w3c-suite.sh` fetches the suite and runs this test; files are not
 * committed. See `docs/musicxml/compliance-audit.md`.
 */
class PublicCorpusTest {
    @Test
    fun testCorpusOutcomesAreStructured() {
        val directory = System.getenv("MUSICXML_CORPUS_DIR")?.let(::File) ?: return
        val files = directory.walkTopDown()
            .filter { it.isFile && it.extension in setOf("musicxml", "xml", "mxl") }
            .sortedBy { it.name }
            .toList()
        assertTrue(files.isNotEmpty(), "No MusicXML files in $directory")
        val problems = mutableListOf<String>()
        val outcomes = ParsePolicy.entries.associateWith { mutableMapOf<String, Int>() }
        val features = sortedMapOf<String, Int>()
        val report = StringBuilder("file\tstrict\ttolerant\tfirst error\ttolerant discarded features\n")
        files.forEach { file ->
            val row = mutableListOf(file.name)
            var firstError = ""
            var discarded = ""
            ParsePolicy.entries.forEach { policy ->
                val result = try {
                    val options = ParseOptions(policy)
                    if (file.extension == "mxl") parseMxl(file.readBytes(), options) else parseMusicXml(file.readBytes(), options, file.name)
                } catch (exception: Exception) {
                    problems += "${file.name} $policy threw $exception"
                    row += "threw"
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
                        if (firstError.isEmpty()) firstError = "${error.code} ${error.location.path}"
                        "rejected: " + error.code
                    }
                }
                row += outcome
                outcomes.getValue(policy).merge(outcome, 1, Int::plus)
                if (policy == ParsePolicy.TOLERANT) {
                    val ids = result.diagnostics.mapNotNull { it.unsupported?.featureId }.distinct()
                    ids.forEach { features.merge(it, 1, Int::plus) }
                    discarded = ids.sorted().joinToString(" ")
                }
                println("CORPUS ${file.name} $policy $outcome")
            }
            report.append((row + firstError + discarded).joinToString("\t")).append('\n')
        }
        val summary = buildList {
            add("CORPUS SUMMARY ${files.size} files")
            outcomes.forEach { (policy, counts) -> add("CORPUS SUMMARY $policy ${counts.toSortedMap()}") }
            add("CORPUS SUMMARY tolerant files per unsupported feature $features")
        }
        summary.forEach(::println)
        System.getenv("MUSICXML_CORPUS_REPORT")?.let { path ->
            File(path).apply { parentFile?.mkdirs() }.writeText(report.toString() + "\n" + summary.joinToString("\n") + "\n")
        }
        if (problems.isNotEmpty()) fail(problems.joinToString("\n"))
    }
}
