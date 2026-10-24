package com.ryanjames.sheetmusic.musicxml

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Opt-in corpus run. Set `MUSICXML_CORPUS_DIR` to a directory of `.musicxml`/`.xml`/`.mxl` files,
 * such as the pinned W3C test suite's `xmlFiles`, to parse each one under both policies. The
 * summary is printed; `MUSICXML_CORPUS_REPORT` names a Markdown file for the per-file report and
 * `MUSICXML_CORPUS_LABEL` its title. `scripts/run-w3c-suite.sh` downloads, runs and removes the
 * W3C suite. Files are not committed; see `docs/musicxml/compliance-audit.md`.
 */
class PublicCorpusTest {
    @Test
    fun testCorpusOutcomesAreStructured() {
        val directory = System.getenv("MUSICXML_CORPUS_DIR")?.let(::File) ?: return
        val files = directory.walkTopDown()
            .filter { it.isFile && it.extension in corpusExtensions }
            .toList()
        assertTrue(files.isNotEmpty(), "No MusicXML files in $directory")
        val report = runCorpus(System.getenv("MUSICXML_CORPUS_LABEL") ?: directory.name, files)
        System.getenv("MUSICXML_CORPUS_REPORT")?.let(::File)?.let { output ->
            output.absoluteFile.parentFile.mkdirs()
            output.writeText(report.toMarkdown())
            println("CORPUS REPORT ${output.absolutePath}")
        }
        println("CORPUS SUMMARY ${files.size} files")
        ParsePolicy.entries.forEach { println("CORPUS SUMMARY $it ${report.summary(it)}") }
        println("CORPUS SUMMARY tolerant files per discarded feature ${report.filesPerDiscardedFeature()}")
        println("CORPUS SUMMARY valid files rejected under tolerant policy ${report.tolerantRejectionsOfValidFiles()}")
        if (report.problems.isNotEmpty()) fail(report.problems.joinToString("\n"))
    }
}
