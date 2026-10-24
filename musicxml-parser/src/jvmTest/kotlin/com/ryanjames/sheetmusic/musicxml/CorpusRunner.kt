package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.playbackOrder
import java.io.File

/** Corpus file extensions: uncompressed MusicXML is read with [parseMusicXml], `.mxl` with [parseMxl]. */
internal val corpusExtensions: Set<String> = setOf("musicxml", "xml", "mxl")

/**
 * Suite files that must be diagnosed rather than accepted cleanly: those marked `.invalid` and the
 * W3C suite's 11b, whose `backup` moves past the start of its measure.
 */
internal fun isExpectedDiagnosed(fileName: String): Boolean =
    ".invalid." in fileName || fileName.startsWith("11b-")

/** One parse of one file under one policy. [firstError] is null exactly for an accepted file. */
internal data class PolicyOutcome(
    val outcome: String,
    val firstError: ParseDiagnostic?,
    val warnings: Int,
    /** Discarded unsupported findings per feature group. */
    val discarded: Map<String, Int>,
) {
    val accepted: Boolean get() = outcome.startsWith("accepted")
}

/** Both policy outcomes for one corpus file. */
internal data class CorpusFileResult(
    val name: String,
    val expectedDiagnosed: Boolean,
    val outcomes: Map<ParsePolicy, PolicyOutcome>,
)

/** Results of a corpus run. [problems] are outcomes that break the parse contract. */
internal data class CorpusReport(
    val label: String,
    val files: List<CorpusFileResult>,
    val problems: List<String>,
) {
    /** Outcome name to file count, per policy. */
    fun summary(policy: ParsePolicy): Map<String, Int> =
        files.groupingBy { it.outcomes.getValue(policy).outcome }.eachCount().toSortedMap()

    /** Tolerant files that discard each feature group. */
    fun filesPerDiscardedFeature(): Map<String, Int> =
        files.flatMap { it.outcomes.getValue(ParsePolicy.TOLERANT).discarded.keys }
            .groupingBy { it }.eachCount().toSortedMap()

    /** Valid files that the tolerant policy rejects. */
    fun tolerantRejectionsOfValidFiles(): List<String> =
        files.filter { !it.expectedDiagnosed && !it.outcomes.getValue(ParsePolicy.TOLERANT).accepted }
            .map { it.name }

    /** A Markdown report with a summary and one row per file. */
    fun toMarkdown(): String = buildString {
        appendLine("# MusicXML corpus report: $label")
        appendLine()
        appendLine("${files.size} files; ${files.count { it.expectedDiagnosed }} expected to be diagnosed.")
        appendLine()
        ParsePolicy.entries.forEach { policy -> appendLine("- ${policy.label()}: ${summary(policy).format()}") }
        appendLine("- Tolerant files per discarded feature: ${filesPerDiscardedFeature().format()}")
        appendLine("- Valid files rejected under tolerant policy: ${tolerantRejectionsOfValidFiles().ifEmpty { listOf("none") }.joinToString()}")
        appendLine("- Contract problems: ${problems.ifEmpty { listOf("none") }.joinToString("; ")}")
        appendLine()
        appendLine("| File | Expected | Strict | Strict first error | Tolerant | Tolerant first error | Tolerant discarded |")
        appendLine("|---|---|---|---|---|---|---|")
        files.forEach { file ->
            val strict = file.outcomes.getValue(ParsePolicy.STRICT)
            val tolerant = file.outcomes.getValue(ParsePolicy.TOLERANT)
            val expected = if (file.expectedDiagnosed) "diagnosed" else "valid"
            appendLine(
                listOf(
                    "`${file.name}`", expected, strict.outcome, strict.firstError.describe(),
                    tolerant.outcome, tolerant.firstError.describe(), tolerant.discarded.format(),
                ).joinToString(" | ", "| ", " |"),
            )
        }
    }

    private fun ParsePolicy.label() = name.lowercase().replaceFirstChar { it.uppercase() }

    private fun Map<String, Int>.format() =
        if (isEmpty()) "none" else entries.joinToString { (key, count) -> "$key $count" }

    private fun ParseDiagnostic?.describe(): String {
        if (this == null) return ""
        val text = listOfNotNull(code.name, location.path?.let { "`$it`" }, message).joinToString(" ")
        return text.replace("|", "\\|").replace(Regex("\\s+"), " ")
    }
}

/**
 * Parses each file under both policies. A thrown exception, a failure without an error, a strict
 * success with findings and a clean success of an expected-diagnosed file are [CorpusReport.problems].
 */
internal fun runCorpus(label: String, files: List<File>): CorpusReport {
    val problems = mutableListOf<String>()
    val results = files.sortedBy { it.name }.map { file ->
        val bytes = file.readBytes()
        val expectedDiagnosed = isExpectedDiagnosed(file.name)
        val outcomes = ParsePolicy.entries.associateWith { policy ->
            val options = ParseOptions(policy)
            val result = try {
                if (file.extension == "mxl") parseMxl(bytes, options, sourceName = file.name)
                else parseMusicXml(bytes, options, file.name)
            } catch (exception: Exception) {
                problems += "${file.name} $policy threw $exception"
                return@associateWith PolicyOutcome("threw", null, 0, emptyMap())
            }
            val outcome = outcomeOf(file.name, policy, result, problems)
            if (expectedDiagnosed && result.diagnostics.isEmpty()) problems += "${file.name} $policy accepted without a diagnostic"
            outcome
        }
        CorpusFileResult(file.name, expectedDiagnosed, outcomes)
    }
    return CorpusReport(label, results, problems)
}

private fun outcomeOf(
    name: String,
    policy: ParsePolicy,
    result: ParseResult<Score>,
    problems: MutableList<String>,
): PolicyOutcome {
    val discarded = result.diagnostics.mapNotNull { it.unsupported }
        .filter { it.retention == ContentRetention.DISCARDED }
        .groupingBy { it.featureId }.eachCount().toSortedMap()
    val warnings = result.diagnostics.count { it.severity == DiagnosticSeverity.WARNING }
    return when (result) {
        is ParseResult.Success<Score> -> {
            try {
                result.value.parts.forEach { it.playbackOrder() }
            } catch (exception: Exception) {
                problems += "$name $policy playback order threw $exception"
            }
            if (policy == ParsePolicy.STRICT && result.diagnostics.isNotEmpty()) problems += "$name strict success with findings"
            PolicyOutcome(if (warnings == 0) "accepted" else "accepted with warnings", null, warnings, discarded)
        }
        is ParseResult.Failure -> {
            val error = result.diagnostics.firstOrNull { it.severity == DiagnosticSeverity.ERROR }
            if (error == null) {
                problems += "$name $policy failed without an error"
                PolicyOutcome("rejected", null, warnings, discarded)
            } else {
                PolicyOutcome("rejected: ${error.code}", error, warnings, discarded)
            }
        }
    }
}
