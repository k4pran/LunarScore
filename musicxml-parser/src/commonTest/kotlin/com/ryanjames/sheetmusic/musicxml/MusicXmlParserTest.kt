package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.MetadataKind
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MusicXmlParserTest {
    @Test
    fun testF02OrderedScoreSkeletonAndMetadata() {
        val result = success(f02PartwiseScore)
        val score = result.value
        assertTrue(result.diagnostics.isEmpty())
        assertEquals("Evening", score.title)
        assertEquals(listOf("P1", "P2"), score.parts.map { it.id })
        assertEquals(listOf("Flute", "Cello"), score.parts.map { it.name })
        assertEquals("Fl.", score.parts[0].abbreviation)
        assertNull(score.parts[1].abbreviation)
        assertEquals(listOf("0", "1"), score.parts[0].measures.map { it.number })
        assertEquals(listOf("A", "A"), score.parts[1].measures.map { it.number })
        assertEquals(listOf("measure-1", "measure-2"), score.parts[1].measures.map { it.id })
        assertTrue(score.parts[0].measures[0].implicit)
        assertFalse(score.parts[0].measures[1].implicit)
        assertTrue(score.parts.all { part ->
            part.staves.isEmpty() && part.measures.all { it.events.isEmpty() && it.extent == Rational.ZERO }
        })
        assertTrue(score.validate().isEmpty())
        val entries = score.metadata.filterIsInstance<MetadataEntry.Text>()
        assertEquals(listOf(
            MetadataKind.WORK_NUMBER, MetadataKind.WORK_TITLE, MetadataKind.MOVEMENT_NUMBER, MetadataKind.MOVEMENT_TITLE,
            MetadataKind.CREATOR, MetadataKind.CREATOR, MetadataKind.RIGHTS, MetadataKind.RIGHTS,
            MetadataKind.SOFTWARE, MetadataKind.ENCODER, MetadataKind.ENCODING_DATE, MetadataKind.SOFTWARE,
            MetadataKind.ENCODING_DESCRIPTION, MetadataKind.SOURCE, MetadataKind.RELATION, MetadataKind.RELATION,
            MetadataKind.MISCELLANEOUS,
        ), entries.map { it.kind })
        assertEquals(listOf("Op. 7", "Suite & Song", "II", "Evening", "Ada", "Ben", "Original fixture",
            "Repository license", "LunarScore", "Cora", "2026-10-21", "Fixture tool", "Hand authored",
            "Original test", "Companion", "Second companion", "F02"), entries.map { it.text })
        assertEquals(listOf("composer", "poet", "music", "words", "editor", "catalogue"), entries.mapNotNull { it.qualifier })
        assertEquals(listOf(
            MetadataEntry.Credit(listOf("Suite", " & Song"), listOf("title", "subtitle"), 2),
            MetadataEntry.Credit(listOf("Ada"), listOf("composer")),
        ), score.metadata.filterIsInstance<MetadataEntry.Credit>())
        assertEquals(listOf(
            MetadataEntry.Text(MetadataKind.CREATOR, "Dee", "arranger"),
            MetadataEntry.Text(MetadataKind.SOURCE, "Part source"),
        ), score.parts[0].metadata)
    }

    @Test
    fun testF02ProvenanceFollowsModelPathsWhenBodiesHaveDifferentOrder() {
        val result = success(f02PartwiseScore, sourceName = "F02.musicxml")
        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals(result.provenance.size, mapping.size)
        assertEquals("/score-partwise[1]/part[2]", mapping["parts[0]"]?.location?.path)
        assertEquals("/score-partwise[1]/part-list[1]/score-part[1]", mapping["parts[0].id"]?.location?.path)
        assertEquals("P1", mapping["parts[0].metadata[0]"]?.location?.partId)
        assertEquals("/score-partwise[1]/movement-title[1]", mapping["metadata[3]"]?.location?.path)
        assertEquals("/score-partwise[1]/credit[1]/credit-words[2]", mapping["metadata[17].words[1]"]?.location?.path)
        val measure = assertNotNull(mapping["parts[1].measures[1]"]).location
        assertEquals("/score-partwise[1]/part[1]/measure[2]", measure.path)
        assertEquals("P2", measure.partId)
        assertEquals("A", measure.measureNumber)
        assertEquals(1, measure.measureIndex)
        assertTrue(result.provenance.all { it.location.sourceName == "F02.musicxml" && it.location.line != null })
    }

    @Test
    fun testEmptyMeasuresAndEmptyPartBody() {
        val emptyMeasure = success(document("<measure number='1'/>"))
        assertTrue(emptyMeasure.value.parts.single().measures.single().events.isEmpty())
        // Neutral empty parts are supported deliberately; this is not full XSD validation.
        assertTrue(success(document("")).value.parts.single().measures.isEmpty())
        assertNull(emptyMeasure.value.title)
        assertTrue(emptyMeasure.value.metadata.isEmpty())
    }

    @Test
    fun testWorkTitleFallbackAndExactTextIncludingCdata() {
        val result = success(document(header = "<work><work-title> A<![CDATA[B]]><!-- ignored -->C &amp; &#x1D11E; </work-title></work><movement-title/>"))
        assertEquals(" ABC & 𝄞 ", result.value.title)
        assertEquals(listOf(" ABC & 𝄞 ", ""), result.value.metadata.filterIsInstance<MetadataEntry.Text>().map { it.text })
        assertEquals("", success(document(name = "")).value.parts.single().name)
    }

    @Test
    fun testPartIdentityFailuresUnderBothPolicies() {
        val variants = listOf(
            "<score-partwise><part-list><score-part><part-name>A</part-name></score-part></part-list></score-partwise>",
            document().replace("score-part id='P1'", "score-part id='  '"),
            document().replace("<part id='P1'", "<part"),
            document().replace("<part id='P1'", "<part id=''"),
            document().replace("<part id='P1'", "<part id='unknown'"),
            document().replace("</part-list>", "<score-part id='P1'><part-name>B</part-name></score-part></part-list>"),
            document().replace("</score-partwise>", "<part id='P1'/></score-partwise>"),
            document().replace("</part-list>", "<score-part id='P2'><part-name>B</part-name></score-part></part-list>"),
            document().replace("id='P1'", "id='P 1'"),
        )
        for (policy in ParsePolicy.entries) {
            variants.forEach { failure(it, DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy)) }
        }
    }

    @Test
    fun testDuplicateAndUnreferencedIdLocations() {
        val duplicate = document().replace("</part-list>", "<score-part id='P1'><part-name>B</part-name></score-part></part-list>")
        assertEquals("/score-partwise[1]/part-list[1]/score-part[2]/@id",
            failure(duplicate, DiagnosticCode.INVALID_STRUCTURE).diagnostics.single().location.path)
        val unused = document().replace("</part-list>", "<score-part id='P2'><part-name>B</part-name></score-part></part-list>")
        val location = failure(unused, DiagnosticCode.INVALID_STRUCTURE).diagnostics.single().location
        assertEquals("/score-partwise[1]/part-list[1]/score-part[2]", location.path)
        assertEquals("P2", location.partId)
        val unknown = failure(document().replace("<part id='P1'", "<part id='P9'"), DiagnosticCode.INVALID_STRUCTURE)
        assertEquals("P9", unknown.diagnostics.single().location.partId)
    }

    @Test
    fun testIdTokenWhitespaceAndNonAsciiNames() {
        val source = document().replace("score-part id='P1'", "score-part id='  木管  '").replace("part id='P1'", "part id='木管'")
        assertEquals("木管", success(source).value.parts.single().id)
    }

    @Test
    fun testRequiredStructureAndDuplicateFields() {
        val variants = listOf(
            "<score-partwise/>",
            "<score-partwise><part-list/></score-partwise>",
            document().replace("<part-list>", "<part-list/><part-list>"),
            document().replace("<part-name>Flute</part-name>", ""),
            document().replace("<part-name>Flute</part-name>", "<part-name>A</part-name><part-name>B</part-name>"),
            document().replace("<measure number='1'/>", "<measure/>"),
            document().replace("</score-partwise>", "<work/></score-partwise>"),
            document(header = "<work/><work/>"),
            document(header = "<work><work-title>A</work-title><work-title>B</work-title></work>"),
            document(header = "<identification><source>A</source><source>B</source></identification>"),
            document(header = "<identification><miscellaneous><miscellaneous-field>value</miscellaneous-field></miscellaneous></identification>"),
            document().replace("<part-list>", "unexpected<part-list>"),
            "<score-partwise><part id='P1'/><part-list><score-part id='P1'><part-name>A</part-name></score-part></part-list></score-partwise>",
        )
        for (policy in ParsePolicy.entries) {
            variants.forEach { failure(it, DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy)) }
        }
    }

    @Test
    fun testInvalidMeasureFlagsAndCreditPage() {
        for (policy in ParsePolicy.entries) {
            failure(document("<measure number='1' implicit='true'/>"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            for (page in listOf("0", "-1", "many", "2147483648")) {
                failure(document(header = "<credit page='$page'><credit-words>A</credit-words></credit>"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            }
        }
    }

    @Test
    fun testTimewiseIsExplicitlyRejectedBySemanticEntryPoint() {
        for (policy in ParsePolicy.entries) {
            failure("<score-timewise/>", DiagnosticCode.UNSUPPORTED_ROOT, ParseOptions(policy))
        }
    }

    @Test
    fun testMusicalSubtreesProduceFeatureDiagnostics() {
        // Interpreted note/attribute content is covered by NoteImportTest; an unmapped attributes child stays S07.
        val features = listOf("backup" to "S06", "attributes" to "S07", "direction" to "S13",
            "barline" to "S15", "print" to "S17", "harmony" to "S22", "figured-bass" to "S22",
            "grouping" to "S25", "sound" to "S26", "listening" to "S26")
        val xml = document("<measure number='A'/><measure number='A'>" +
            features.joinToString("") { "<${it.first}><unmapped-nested-content/></${it.first}>" } + "</measure>")
        for (policy in ParsePolicy.entries) {
            val result = parse(xml, ParseOptions(policy))
            assertEquals(features.map { it.second }, result.diagnostics.map { it.unsupported?.featureId })
            assertTrue(result.diagnostics.all {
                it.code == DiagnosticCode.UNSUPPORTED_FEATURE && it.unsupported?.retention == ContentRetention.DISCARDED &&
                    it.location.partId == "P1" && it.location.measureNumber == "A" && it.location.measureIndex == 1
            })
            if (policy == ParsePolicy.STRICT) assertIs<ParseResult.Failure>(result)
            else assertIs<ParseResult.Success<Score>>(result)
        }
    }

    @Test
    fun testUnmappedHeaderContentAndAttributesAreObservable() {
        val xml = document(header = """
            <work><opus href='https://example.invalid/never-fetch'/></work>
            <identification><encoding><supports element='note' type='yes'/></encoding></identification>
            <defaults><scaling/></defaults>
            <credit><link href='file:///never-read'/><credit-image source='https://example.invalid/never-fetch'/></credit>
        """).replace("<part-name>Flute", "<part-name print-object='no'>Flute")
            .replace("<measure number='1'/>", "<measure number='1' width='20'/>")
            .replace("<part-name print-object='no'>", "<score-instrument id='I1'><instrument-name>Flute</instrument-name></score-instrument><part-name print-object='no'>")
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S27", "S03", "S17", "S25", "S03", "S16", "S03", "S01"),
            result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertTrue(result.diagnostics.any { it.location.path?.endsWith("/@print-object") == true })
        assertTrue(result.value.parts.single().instruments.isEmpty())
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testForeignNamesNeverAliasMusicXmlNames() {
        val xml = document().replace("<part-list>", "<part-list xmlns:e='urn:extension'><e:score-part id='P9'/>")
            .replace("<part-name>", "<part-name xmlns:e='urn:extension' e:style='fancy'>")
            .replace("<measure number='1'/>", "<measure number='1' xmlns:e='urn:extension'><e:note/><note xmlns='urn:other'/></measure>")
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("P1"), result.value.parts.map { it.id })
        assertEquals(listOf("foreign-extension", "S03", "foreign-extension", "foreign-extension"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics[1].location.path.orEmpty().endsWith("/@{urn:extension}style"))
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testNestedTextExtensionIsDiscardedWithoutReorderingRemainingText() {
        val xml = document(header = "<movement-title> A<extension>hidden</extension>B<![CDATA[C]]> </movement-title>")
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(" ABC ", result.value.title)
        assertEquals(ContentRetention.DISCARDED, result.diagnostics.single().unsupported?.retention)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testFatalTailDiscardsSkeletonAndProvenance() {
        failure(document() + "<tail/>", DiagnosticCode.MALFORMED_XML)
        failure(document().dropLast(4), DiagnosticCode.MALFORMED_XML)
        val result = failure(document("<measure number='1'><direction/></measure>") + "<tail/>",
            DiagnosticCode.MALFORMED_XML, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticCode.MALFORMED_XML), result.diagnostics.map { it.code })
    }

    @Test
    fun testProvenanceBudgetExactAndExceeded() {
        val xml = document()
        val count = success(xml).provenance.size
        assertEquals(count, success(xml, ParseOptions(limits = ParseLimits(maxProvenanceEntries = count))).provenance.size)
        failure(xml, DiagnosticCode.PROVENANCE_LIMIT, ParseOptions(limits = ParseLimits(maxProvenanceEntries = count - 1)))
        failure(xml, DiagnosticCode.PROVENANCE_LIMIT, ParseOptions(limits = ParseLimits(maxProvenanceEntries = 0)))
    }

    @Test
    fun testDiagnosticBudgetStopsUnsupportedContent() {
        val result = failure(document("<measure number='1'><direction/><direction/><direction/></measure>"), DiagnosticCode.DIAGNOSTIC_LIMIT,
            ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxDiagnostics = 2)))
        assertEquals(listOf(DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticCode.DIAGNOSTIC_LIMIT), result.diagnostics.map { it.code })
    }

    @Test
    fun testPublicEntryPointKeepsReaderSecurityLimitsAndCancellation() {
        failure("<!DOCTYPE score-partwise>" + document(), DiagnosticCode.UNSAFE_XML)
        failure(document(), DiagnosticCode.INPUT_LIMIT, ParseOptions(limits = ParseLimits(maxInputBytes = 10)))
        failure(document(), DiagnosticCode.ELEMENT_LIMIT, ParseOptions(limits = ParseLimits(maxElements = 2)))
        failure(document(), DiagnosticCode.DEPTH_LIMIT, ParseOptions(limits = ParseLimits(maxDepth = 2)))
        failure(document(), DiagnosticCode.PATH_LIMIT, ParseOptions(limits = ParseLimits(maxSourcePathCharacters = 10)))
        val cancelled = parseMusicXml(document().encodeToByteArray(), isCancelled = { true })
        assertEquals(DiagnosticCode.CANCELLED, assertIs<ParseResult.Failure>(cancelled).diagnostics.single().code)
        var checks = 0
        val later = parseMusicXml(document("<measure number='1'/>".repeat(100)).encodeToByteArray(), isCancelled = { ++checks > 100 })
        assertEquals(DiagnosticCode.CANCELLED, assertIs<ParseResult.Failure>(later).diagnostics.last().code)
        assertNull(later.value)
        assertTrue(later.provenance.isEmpty())
    }

    private fun document(measures: String = "<measure number='1'/>", header: String = "", name: String = "Flute"): String =
        "<score-partwise version='4.0'>$header<part-list><score-part id='P1'><part-name>$name</part-name></score-part></part-list><part id='P1'>$measures</part></score-partwise>"

    private fun parse(xml: String, options: ParseOptions = ParseOptions(), sourceName: String? = null): ParseResult<Score> =
        parseMusicXml(xml.encodeToByteArray(), options, sourceName)

    private fun success(xml: String, options: ParseOptions = ParseOptions(), sourceName: String? = null): ParseResult.Success<Score> =
        assertIs<ParseResult.Success<Score>>(parse(xml, options, sourceName))

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parse(xml, options, "invalid.musicxml"))
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        assertNull(result.value)
        assertTrue(result.provenance.isEmpty())
        assertTrue(result.diagnostics.all { it.location.sourceName == "invalid.musicxml" })
        return result
    }
}
