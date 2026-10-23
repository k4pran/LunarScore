package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.ModelDiagnosticCode
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.playbackOrder
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * MX-21 adversarial audit. Every fixture and every deterministic mutation of it must end in a
 * well-formed result under both policies: no exception escapes, failures carry an error, strict
 * success carries no findings, unsupported content names a coverage group and successful scores
 * pass model validation, state resolution and playback ordering.
 */
class ComplianceAuditTest {
    @Test
    fun testFixturesImportUnderBothPolicies() {
        assertWellFormed { xml -> listOf(Mutant("original", xml)) }
    }

    @Test
    fun testTextMutantsNeverEscapeDiagnostics() {
        assertWellFormed(::textMutants)
    }

    @Test
    fun testAttributeMutantsNeverEscapeDiagnostics() {
        assertWellFormed(::attributeMutants)
    }

    @Test
    fun testElementMutantsNeverEscapeDiagnostics() {
        assertWellFormed { xml -> elementMutants(xml) + swapMutants(xml) }
    }

    @Test
    fun testTruncatedFixturesFail() {
        auditFixtures.forEach { (id, xml) ->
            truncationMutants(xml).forEach { mutant ->
                ParsePolicy.entries.forEach { policy ->
                    val result = parse("$id ${mutant.description}", mutant.xml, ParseOptions(policy))
                    assertTrue(result is ParseResult.Failure, "$id ${mutant.description} $policy was accepted")
                }
            }
        }
    }

    @Test
    fun testNoBreakSpaceVoiceIsInvalidValue() {
        // Regression: XML whitespace trimming left a no-break space, which the model rejects by throwing.
        val note = f01QuarterNoteAndRest.replaceFirst("<voice>1</voice>", "<voice> </voice>")
        // The first voice on a line of its own in F14 belongs to a direction.
        val direction = f14Directions.replaceFirst(Regex("\n(\\s*)<voice>1</voice>\n"), "\n\$1<voice> </voice>\n")
        assertTrue(direction != f14Directions)
        listOf(note, direction).forEach { xml ->
            ParsePolicy.entries.forEach { policy ->
                val result = parse("voice", xml, ParseOptions(policy))
                assertTrue(result is ParseResult.Failure, "$policy accepted a blank voice")
                assertTrue(result.diagnostics.any { it.code == DiagnosticCode.INVALID_VALUE && it.location.path.orEmpty().endsWith("/voice[1]") })
            }
        }
    }

    @Test
    fun testNoBreakSpaceIdIsInvalidStructure() {
        // Regression: a part ID of only a no-break space passed the XML whitespace check and the model threw.
        listOf("id=\" \"", "id=\"P 1\"").forEach { id ->
            val xml = f01QuarterNoteAndRest.replace("id=\"P1\"", id)
            ParsePolicy.entries.forEach { policy ->
                val result = parse(id, xml, ParseOptions(policy))
                assertTrue(result is ParseResult.Failure, "$policy accepted $id")
                assertTrue(result.diagnostics.any { it.code == DiagnosticCode.INVALID_STRUCTURE }, result.diagnostics.toString())
            }
        }
    }

    @Test
    fun testForeignExtensionsAreNeverSilentlyAccepted() {
        assertWellFormed(::foreignExtensionMutants)
        auditFixtures.forEach { (id, xml) ->
            foreignExtensionMutants(xml).forEach { mutant ->
                val result = parse("$id ${mutant.description}", mutant.xml, TOLERANT)
                assertTrue(result.diagnostics.isNotEmpty(), "$id ${mutant.description} lost a foreign element silently")
            }
        }
    }

    @Test
    fun testTightResourceLimitsFailWithTypedCode() {
        val budgets = listOf(
            ParseLimits(maxInputBytes = 64) to DiagnosticCode.INPUT_LIMIT,
            ParseLimits(maxDepth = 2) to DiagnosticCode.DEPTH_LIMIT,
            ParseLimits(maxElements = 5) to DiagnosticCode.ELEMENT_LIMIT,
            ParseLimits(maxSourcePathCharacters = 40) to DiagnosticCode.PATH_LIMIT,
            ParseLimits(maxTotalAttributes = 1) to DiagnosticCode.ATTRIBUTE_LIMIT,
            ParseLimits(maxProvenanceEntries = 1) to DiagnosticCode.PROVENANCE_LIMIT,
        )
        auditFixtures.forEach { (id, xml) ->
            budgets.forEach { (limits, code) ->
                ParsePolicy.entries.forEach { policy ->
                    val result = parse(id, xml, ParseOptions(policy, limits))
                    assertTrue(result is ParseResult.Failure, "$id $code $policy was accepted")
                    assertTrue(result.diagnostics.any { it.code == code }, "$id $policy ${result.diagnostics}")
                }
            }
        }
        val capped = parse("F21", f21VersionFourAdditions, ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxDiagnostics = 1)))
        assertEquals(listOf(DiagnosticCode.DIAGNOSTIC_LIMIT), capped.diagnostics.map { it.code })
    }

    @Test
    fun testCancellationAtEveryCheckFailsCleanly() {
        auditFixtures.forEach { (id, xml) ->
            var checks = 0
            parseMusicXml(xml.encodeToByteArray(), TOLERANT) { checks++; false }
            (0..checks).forEach { limit ->
                var calls = 0
                val result = try {
                    parseMusicXml(xml.encodeToByteArray(), TOLERANT) { calls++ >= limit }
                } catch (exception: Exception) {
                    fail("$id cancelled at $limit threw $exception")
                }
                if (limit < checks) {
                    assertTrue(result is ParseResult.Failure, "$id cancelled at $limit was accepted")
                    assertEquals(DiagnosticCode.CANCELLED, result.diagnostics.last().code, "$id at $limit")
                }
            }
        }
    }

    private fun assertWellFormed(mutate: (String) -> List<Mutant>) {
        val problems = mutableListOf<String>()
        auditFixtures.forEach { (id, xml) ->
            mutate(xml).forEach { mutant ->
                ParsePolicy.entries.forEach { policy ->
                    problems += outcomeProblems("$id ${mutant.description} $policy", mutant.xml, ParseOptions(policy))
                }
            }
        }
        if (problems.isNotEmpty()) fail("${problems.size} problems, first:\n" + problems.take(400).joinToString("\n"))
    }

    private fun outcomeProblems(label: String, xml: String, options: ParseOptions): List<String> {
        val result = try {
            parseMusicXml(xml.encodeToByteArray(), options)
        } catch (exception: Exception) {
            return listOf("$label threw $exception")
        }
        val problems = mutableListOf<String>()
        result.diagnostics.mapNotNull { it.unsupported }.filterNot { it.featureId.matches(featureIdPattern) }
            .forEach { problems += "$label unknown feature ${it.featureId}" }
        when (result) {
            is ParseResult.Failure -> if (result.diagnostics.none { it.severity == DiagnosticSeverity.ERROR }) {
                problems += "$label failed without an error"
            }
            is ParseResult.Success<Score> -> {
                if (result.diagnostics.any { it.severity == DiagnosticSeverity.ERROR }) problems += "$label succeeded with an error"
                if (options.policy == ParsePolicy.STRICT && result.diagnostics.isNotEmpty()) {
                    problems += "$label strict success with findings ${result.diagnostics.map { it.code }}"
                }
                try {
                    // An unmatched span is kept with a null partner, which the model reports as a missing endpoint.
                    val unmatched = result.diagnostics.any { it.code == DiagnosticCode.UNMATCHED_SPAN }
                    val modelFindings = result.value.validate()
                        .filterNot { unmatched && it.code == ModelDiagnosticCode.MISSING_ENDPOINT && it.reference == null }
                    if (modelFindings.isNotEmpty()) problems += "$label model findings $modelFindings"
                    result.value.parts.forEach { part ->
                        part.effectiveStates()
                        part.playbackOrder()
                    }
                } catch (exception: Exception) {
                    problems += "$label model use threw $exception"
                }
            }
        }
        return problems
    }

    private fun parse(label: String, xml: String, options: ParseOptions): ParseResult<Score> = try {
        parseMusicXml(xml.encodeToByteArray(), options)
    } catch (exception: Exception) {
        fail("$label threw $exception")
    }

    private companion object {
        val TOLERANT = ParseOptions(ParsePolicy.TOLERANT)
        val featureIdPattern = Regex("S(0[1-9]|1[0-9]|2[0-7])|foreign-extension")
    }
}
