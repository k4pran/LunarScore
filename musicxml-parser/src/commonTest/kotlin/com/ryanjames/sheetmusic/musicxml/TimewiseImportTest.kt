package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TimewiseImportTest {
    @Test
    fun testF12TimewiseAndPartwiseCompareEqualSemantically() {
        val timewise = success(f12TimewiseScore)
        val partwise = success(f12PartwiseScore)
        assertEquals(partwise.value, timewise.value)
        assertTrue(timewise.diagnostics.isEmpty(), timewise.diagnostics.toString())
        assertEquals(partwise.provenance.map { it.modelPath }.toSet(), timewise.provenance.map { it.modelPath }.toSet())
        assertEquals(partwise.provenance.size, timewise.provenance.size)
        val score = timewise.value
        assertTrue(score.validate().isEmpty())
        assertEquals("Pair", score.title)
        assertEquals(listOf(1, 1), score.parts.map { it.staves.size })
        assertEquals(listOf("1", "2", "3"), score.parts[0].measures.map { it.number })
        // Divisions carry per part: the flute's halves and the piano's whole notes are both exact.
        assertEquals(listOf(Rational.of(2), Rational.of(2)), score.parts[0].measures[1].events.filterIsInstance<NoteEvent>().map { it.duration })
        assertEquals(Rational.of(4), score.parts[1].measures[1].events.filterIsInstance<NoteEvent>().last().duration)
        val pickup = score.parts[0].measures[2]
        assertTrue(pickup.implicit && pickup.events.isEmpty() && pickup.extent == Rational.ZERO)
        assertEquals(Rational.ONE, score.parts[1].measures[2].extent)
    }

    @Test
    fun testTimewiseProvenanceUsesMeasureAndPartElements() {
        val mapping = success(f12TimewiseScore).provenance.associateBy { it.modelPath }
        assertEquals("/score-timewise[1]/measure[1]/part[1]", mapping["parts[0]"]?.location?.path)
        assertEquals("/score-timewise[1]/measure[1]/part[2]", mapping["parts[1]"]?.location?.path)
        val measure = assertNotNull(mapping["parts[0].measures[1]"]).location
        assertEquals("/score-timewise[1]/measure[2]", measure.path)
        assertEquals("P1", measure.partId)
        assertEquals(1, measure.measureIndex)
        val missing = assertNotNull(mapping["parts[0].measures[2]"]).location
        assertEquals("/score-timewise[1]/measure[3]", missing.path)
        assertEquals("P1", missing.partId)
        assertEquals("3", missing.measureNumber)
        val event = assertNotNull(mapping["parts[0].measures[1].events[0]"]).location
        assertEquals("/score-timewise[1]/measure[2]/part[2]/note[1]", event.path)
        assertEquals("P1", event.partId)
    }

    @Test
    fun testPartMissingFromLeadingMeasuresGetsEmptyMeasures() {
        val note = "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration></note>"
        val divisions = "<attributes><divisions>1</divisions></attributes>"
        val timewise = success(timewise(
            "<measure number='1'><part id='P1'>$divisions$note</part></measure>" +
                "<measure number='2'><part id='P2'>$divisions$note</part></measure>",
        ))
        val partwise = success(partwise(
            "<part id='P1'><measure number='1'>$divisions$note</measure><measure number='2'/></part>" +
                "<part id='P2'><measure number='1'/><measure number='2'>$divisions$note</measure></part>",
        ))
        assertEquals(partwise.value, timewise.value)
        assertEquals(listOf("measure-1", "measure-2"), timewise.value.parts[1].measures.map { it.id })
        assertEquals("P2:measure-2:event-1", timewise.value.parts[1].measures[1].events.single().id)
    }

    @Test
    fun testMissingPartPaddingIsBoundedByProvenanceLimit() {
        // Padding allocates measures without source elements, so the provenance budget must stop it.
        val measures = (1..50).joinToString("") { "<measure number='$it'><part id='P1'/></measure>" } +
            "<measure number='51'><part id='P2'/></measure>"
        val limits = ParseLimits(maxProvenanceEntries = 60)
        failure(timewise(measures), DiagnosticCode.PROVENANCE_LIMIT, ParseOptions(limits = limits))
        assertEquals(51, success(timewise(measures)).value.parts[1].measures.size)
    }

    @Test
    fun testTimewisePartStructureErrorsAreFatal() {
        for (policy in ParsePolicy.entries) {
            val options = ParseOptions(policy)
            failure("<score-timewise/>", DiagnosticCode.INVALID_STRUCTURE, options)
            failure(timewise("<measure number='1'><part id='P3'/></measure>"), DiagnosticCode.INVALID_STRUCTURE, options)
            failure(timewise("<measure number='1'><part id='P1'/><part id='P1'/><part id='P2'/></measure>"), DiagnosticCode.INVALID_STRUCTURE, options)
            failure(timewise("<measure number='1'><part id='P1'/></measure>"), DiagnosticCode.INVALID_STRUCTURE, options)
            failure(timewise("<measure><part id='P1'/><part id='P2'/></measure>"), DiagnosticCode.INVALID_STRUCTURE, options)
            failure(timewise("<measure number='1'><part/><part id='P2'/></measure>"), DiagnosticCode.INVALID_STRUCTURE, options)
            failure(timewise("<measure number='1' implicit='maybe'><part id='P1'/><part id='P2'/></measure>"), DiagnosticCode.INVALID_VALUE, options)
            val beforePartList = "<score-timewise><measure number='1'/><part-list><score-part id='P1'><part-name>A</part-name></score-part></part-list></score-timewise>"
            failure(beforePartList, DiagnosticCode.INVALID_STRUCTURE, options)
        }
    }

    @Test
    fun testUnlistedTimewisePartsAreDiscardedWhenTolerant() {
        val xml = timewise("<measure number='1'><part id='P1'/><part id='P3'/><part id='P2'/></measure>")
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("P1", "P2"), result.value.parts.map { it.id })
        val warning = result.diagnostics.single()
        assertEquals(UnsupportedContent("S01", ContentRetention.DISCARDED), warning.unsupported)
        assertEquals("/score-timewise[1]/measure[1]/part[2]" to "P3", warning.location.path to warning.location.partId)
    }

    @Test
    fun testUnmappedTimewiseContentIsReportedWithPartContext() {
        val xml = timewise(
            "<part id='P1'/><measure number='1' width='90'><direction/><part id='P1' extra='x'/><part id='P2'/></measure>",
        )
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        val unsupported = result.diagnostics.map { assertNotNull(it.unsupported).featureId to it.location.path }
        assertEquals(listOf(
            "S03" to "/score-timewise[1]/part[1]",
            "S01" to "/score-timewise[1]/measure[1]/@width",
            "S13" to "/score-timewise[1]/measure[1]/direction[1]",
            "S01" to "/score-timewise[1]/measure[1]/part[1]/@extra",
        ), unsupported)
        assertEquals("P1", result.diagnostics.last().location.partId)
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        assertEquals(1, result.value.parts[0].measures.size)
    }

    private fun timewise(measures: String): String =
        "<score-timewise version='4.0'>$PART_LIST$measures</score-timewise>"

    private fun partwise(parts: String): String =
        "<score-partwise version='4.0'>$PART_LIST$parts</score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), options), xml)
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        assertNull(result.value)
        return result
    }

    private companion object {
        const val PART_LIST = "<part-list><score-part id='P1'><part-name>A</part-name></score-part>" +
            "<score-part id='P2'><part-name>B</part-name></score-part></part-list>"
    }
}
