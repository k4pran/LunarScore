package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Score
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Contract-level findings supplied by an adapter; these tests do not parse XML or validate XSD. */
class ParseContractTest {
    private val location = SourceLocation(
        sourceName = "scores/main.musicxml",
        path = "/score-partwise/part[1]/measure[2]/note[3]/notations/slur[1]",
        line = 18,
        column = 9,
        partId = "P1",
        measureNumber = "pickup",
        measureIndex = 1,
    )

    @Test
    fun testCleanResultCarriesScoreOptionsAndProvenance() {
        val context = ParseContext()
        val score = Score()
        val source = SourceProvenance("parts[0].measures[1].events[2]", location)
        context.addProvenance(source)
        val result = assertIs<ParseResult.Success<Score>>(context.finish(score))
        assertSame(score, result.value)
        assertEquals(ParsePolicy.STRICT, result.options.policy)
        assertEquals(listOf(source), result.provenance)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(context.canContinue)
    }

    @Test
    fun testF22InvalidInputCategoriesRejectUnderBothPolicies() {
        // Wrong root, missing part-list, illegal duration, malformed XML respectively.
        val findings = listOf(
            DiagnosticCode.UNSUPPORTED_ROOT to "/unknown-root",
            DiagnosticCode.INVALID_STRUCTURE to "/score-partwise",
            DiagnosticCode.INVALID_VALUE to "/score-partwise/part[1]/measure[1]/note[1]/duration",
            DiagnosticCode.MALFORMED_XML to null,
        )
        for (policy in ParsePolicy.entries) {
            for ((code, path) in findings) {
                val context = ParseContext(ParseOptions(policy))
                val source = SourceLocation(sourceName = "F22.musicxml", path = path)
                context.report(code, source, "Rejected input")
                val result = context.failure()
                assertNull(result.value)
                assertTrue(result.provenance.isEmpty())
                assertEquals(code, result.diagnostics.single().code)
                assertEquals(DiagnosticSeverity.ERROR, result.diagnostics.single().severity)
                assertEquals(source, result.diagnostics.single().location)
                assertFalse(context.canContinue)
            }
        }
    }

    @Test
    fun testF23SemanticFindingsAreOrderedAndPolicyControlled() {
        val codes = listOf(
            DiagnosticCode.UNMATCHED_SPAN,
            DiagnosticCode.INVALID_CURSOR_MOVE,
            DiagnosticCode.INCONSISTENT_MEASURE_DURATION,
        )
        for (policy in ParsePolicy.entries) {
            val context = ParseContext(ParseOptions(policy))
            codes.forEach { context.report(it, location, "Semantic inconsistency") }
            val result = context.finish(Score())
            if (policy == ParsePolicy.STRICT) {
                assertIs<ParseResult.Failure>(result)
                assertNull(result.value)
            } else {
                assertIs<ParseResult.Success<Score>>(result)
            }
            assertEquals(codes, result.diagnostics.map { it.code })
            assertEquals(List(3) { location }, result.diagnostics.map { it.location })
            val severity = if (policy == ParsePolicy.STRICT) DiagnosticSeverity.ERROR else DiagnosticSeverity.WARNING
            assertEquals(List(3) { severity }, result.diagnostics.map { it.severity })
        }
    }

    @Test
    fun testValidButUnmappedGroupingIsObservableUnderBothPolicies() {
        // grouping is valid MusicXML 4.0 (S25); validity does not mean the adapter understands it.
        val source = location.copy(path = "/score-partwise/part[1]/measure[2]/grouping[1]")
        val content = UnsupportedContent("S25", ContentRetention.DISCARDED)
        for (policy in ParsePolicy.entries) {
            val context = ParseContext(ParseOptions(policy))
            context.unsupported(content, source, "Grouping is not mapped")
            val result = context.finish(Score())
            assertEquals(content, result.diagnostics.single().unsupported)
            assertEquals(DiagnosticCode.UNSUPPORTED_FEATURE, result.diagnostics.single().code)
            assertEquals(source, result.diagnostics.single().location)
            if (policy == ParsePolicy.STRICT) assertIs<ParseResult.Failure>(result)
            else assertIs<ParseResult.Success<Score>>(result)
        }
    }

    @Test
    fun testOpaqueAndConvertedContentStillRejectInStrictMode() {
        val contents = listOf(
            UnsupportedContent("S25", ContentRetention.OPAQUE, sourceXml = "<grouping type=\"single\"/>"),
            UnsupportedContent("S25", ContentRetention.CONVERTED, conversion = "Kept text; dropped grouping boundaries"),
        )
        for (policy in ParsePolicy.entries) {
            val context = ParseContext(ParseOptions(policy))
            contents.forEach { context.unsupported(it, location, "Unmapped grouping") }
            val result = context.finish(Score())
            assertEquals(contents, result.diagnostics.map { it.unsupported })
            if (policy == ParsePolicy.STRICT) assertIs<ParseResult.Failure>(result)
            else assertIs<ParseResult.Success<Score>>(result)
        }
    }

    @Test
    fun testExplicitRepairRequiresTolerantPolicyAndDescription() {
        for (policy in ParsePolicy.entries) {
            val context = ParseContext(ParseOptions(policy))
            context.report(DiagnosticCode.RECOVERED_VALUE, location, "Replaced missing voice with 1")
            val result = context.finish(Score())
            assertEquals("Replaced missing voice with 1", result.diagnostics.single().message)
            if (policy == ParsePolicy.STRICT) assertIs<ParseResult.Failure>(result)
            else assertIs<ParseResult.Success<Score>>(result)
        }
    }

    @Test
    fun testFatalAfterWarningsClearsValueAndProvenance() {
        val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT))
        context.addProvenance(SourceProvenance("parts[0]", location))
        context.report(DiagnosticCode.UNMATCHED_SPAN, location, "Missing end")
        context.report(DiagnosticCode.UNSAFE_XML, location, "Unsafe XML")
        val result = assertIs<ParseResult.Failure>(context.finish(Score()))
        assertNull(result.value)
        assertTrue(result.provenance.isEmpty())
        assertEquals(listOf(DiagnosticSeverity.WARNING, DiagnosticSeverity.ERROR), result.diagnostics.map { it.severity })
    }

    @Test
    fun testDiagnosticCapIncludesTerminalMarkerAndCannotHideFailure() {
        for (cap in listOf(1, 2, 3)) {
            val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxDiagnostics = cap)))
            repeat(cap) { context.report(DiagnosticCode.UNMATCHED_SPAN, location, "Missing endpoint $it") }
            assertIs<ParseResult.Success<Score>>(context.finish(Score()))
            val overflowLocation = location.copy(line = 30)
            context.report(DiagnosticCode.UNMATCHED_SPAN, overflowLocation, "One too many")
            // Subsequent findings and mappings must neither grow the result nor resume parsing.
            repeat(5) { context.report(DiagnosticCode.INVALID_VALUE, location, "Ignored after stop") }
            context.addProvenance(SourceProvenance("parts[0]", location))
            val result = assertIs<ParseResult.Failure>(context.finish(Score()))
            assertEquals(cap, result.diagnostics.size)
            assertEquals(List(cap - 1) { DiagnosticCode.UNMATCHED_SPAN } + DiagnosticCode.DIAGNOSTIC_LIMIT,
                result.diagnostics.map { it.code })
            assertEquals(overflowLocation, result.diagnostics.last().location)
            assertEquals(DiagnosticSeverity.ERROR, result.diagnostics.last().severity)
            assertFalse(context.canContinue)
        }
    }

    @Test
    fun testRetentionBudgetCountsCumulativeUtf8Bytes() {
        val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxRetainedSourceBytes = 4)))
        val content = UnsupportedContent("extension:test", ContentRetention.OPAQUE, sourceXml = "é")
        repeat(2) { context.unsupported(content, location, "Retain two UTF-8 bytes") }
        assertIs<ParseResult.Success<Score>>(context.finish(Score()))
        context.unsupported(content, location, "Over budget")
        val result = context.failure()
        assertEquals(listOf(DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticCode.UNSUPPORTED_FEATURE,
            DiagnosticCode.RETENTION_LIMIT), result.diagnostics.map { it.code })
        assertNull(result.diagnostics.last().unsupported)
        assertFalse(context.canContinue)
    }

    @Test
    fun testRetentionRejectsMultibyteFragmentWhenCharacterCountFits() {
        // Both fragments fit by UTF-16 length but exceed the stated UTF-8 byte budget.
        for ((source, budget) in listOf("é" to 1, "𝄞" to 3)) {
            val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxRetainedSourceBytes = budget)))
            context.unsupported(UnsupportedContent("extension:test", ContentRetention.OPAQUE, sourceXml = source),
                location, "Retain Unicode source")
            val result = context.failure()
            assertEquals(DiagnosticCode.RETENTION_LIMIT, result.diagnostics.single().code)
            assertNull(result.diagnostics.single().unsupported)
        }
    }

    @Test
    fun testZeroRetentionBudgetAllowsDiscardButRejectsOpaqueContent() {
        val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxRetainedSourceBytes = 0)))
        context.unsupported(UnsupportedContent("S25", ContentRetention.DISCARDED), location, "Explicit discard")
        context.unsupported(UnsupportedContent("S25", ContentRetention.OPAQUE, sourceXml = "<grouping/>"),
            location, "Cannot retain")
        assertEquals(DiagnosticCode.RETENTION_LIMIT, context.failure().diagnostics.last().code)
    }

    @Test
    fun testProvenanceBudgetIsFatalAtFirstExcessEntry() {
        for (cap in listOf(0, 1)) {
            val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxProvenanceEntries = cap)))
            repeat(cap) { context.addProvenance(SourceProvenance("parts[$it]", location)) }
            assertIs<ParseResult.Success<Score>>(context.finish(Score()))
            context.addProvenance(SourceProvenance("parts[$cap]", location))
            assertEquals(DiagnosticCode.PROVENANCE_LIMIT, context.failure().diagnostics.single().code)
            assertFalse(context.canContinue)
        }
    }

    @Test
    fun testResourceNumericAndCancellationErrorsAlwaysStop() {
        for (code in listOf(DiagnosticCode.INPUT_LIMIT, DiagnosticCode.DEPTH_LIMIT, DiagnosticCode.ELEMENT_LIMIT, DiagnosticCode.PATH_LIMIT,
            DiagnosticCode.ATTRIBUTE_LIMIT, DiagnosticCode.NUMERIC_OVERFLOW, DiagnosticCode.CANCELLED, DiagnosticCode.ARCHIVE_READ_FAILED,
            DiagnosticCode.ARCHIVE_ENTRY_LIMIT, DiagnosticCode.UNSUPPORTED_ENCODING, DiagnosticCode.UNSUPPORTED_NAMESPACE)) {
            val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT))
            context.report(code, SourceLocation(), "Fatal failure")
            assertEquals(DiagnosticSeverity.ERROR, context.failure().diagnostics.single().severity)
            assertFalse(context.canContinue)
        }
    }

    @Test
    fun testResultListsAreSnapshots() {
        val context = ParseContext(ParseOptions(ParsePolicy.TOLERANT))
        context.addProvenance(SourceProvenance("parts[0]", location))
        context.report(DiagnosticCode.UNMATCHED_SPAN, location, "First")
        val first = context.finish(Score())
        context.report(DiagnosticCode.INVALID_VALUE, location, "Second")
        assertEquals(1, first.diagnostics.size)
        assertEquals(1, first.provenance.size)
        assertIs<ParseResult.Success<Score>>(first)
        assertIs<ParseResult.Failure>(context.finish(Score()))
    }

    @Test
    fun testInvalidContractValuesAreRejected() {
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxInputBytes = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxDepth = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxElements = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxDiagnostics = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxRetainedSourceBytes = -1) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxProvenanceEntries = -1) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxSourcePathCharacters = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxSourcePathCharacters = -1) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxAttributesPerElement = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxAttributesPerElement = -1) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxTotalAttributes = 0) }
        assertFailsWith<IllegalArgumentException> { ParseLimits(maxTotalAttributes = -1) }
        assertFailsWith<IllegalArgumentException> { SourceLocation(line = 0) }
        assertFailsWith<IllegalArgumentException> { SourceLocation(column = 0) }
        assertFailsWith<IllegalArgumentException> { SourceLocation(measureIndex = -1) }
        assertFailsWith<IllegalArgumentException> { SourceLocation(path = "relative") }
        assertFailsWith<IllegalArgumentException> { SourceProvenance("", location) }
        assertFailsWith<IllegalArgumentException> { UnsupportedContent("", ContentRetention.DISCARDED) }
        assertFailsWith<IllegalArgumentException> { UnsupportedContent("S25", ContentRetention.OPAQUE) }
        assertFailsWith<IllegalArgumentException> { UnsupportedContent("S25", ContentRetention.CONVERTED) }
        assertFailsWith<IllegalArgumentException> {
            UnsupportedContent("S25", ContentRetention.DISCARDED, sourceXml = "<grouping/>")
        }
        assertFailsWith<IllegalArgumentException> {
            ParseContext().report(DiagnosticCode.UNSUPPORTED_FEATURE, location, "Missing loss record")
        }
        assertFailsWith<IllegalArgumentException> {
            ParseDiagnostic(DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticSeverity.WARNING, location, "Missing loss record")
        }
        assertFailsWith<IllegalArgumentException> {
            ParseDiagnostic(DiagnosticCode.INVALID_VALUE, DiagnosticSeverity.ERROR, location, " ")
        }
        assertFailsWith<IllegalStateException> { ParseContext().failure() }
    }
}
