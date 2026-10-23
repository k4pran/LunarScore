package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.ModelDiagnosticCode
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Relationship
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpanImportTest {
    @Test
    fun testF10ResolvesCrossMeasureTiesAndSlursIndependently() {
        val result = success(f10TiesAndSlurs)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val e4 = "P1:measure-1:event-2"
        val g4 = "P1:measure-1:event-3"
        val tiedG4 = "P1:measure-2:event-1"
        assertEquals(listOf(
            Relationship("P1:slur-1", RelationshipKind.SLUR, e4, tiedG4),
            Relationship("P1:sound-tie-1", RelationshipKind.SOUND_TIE, g4, tiedG4),
            Relationship("P1:notated-tie-1", RelationshipKind.NOTATED_TIE, g4, tiedG4),
            Relationship("P1:slur-2", RelationshipKind.SLUR, g4, tiedG4),
            Relationship("P1:notated-tie-2", RelationshipKind.NOTATED_TIE, "P1:measure-2:event-2", "P1:measure-2:event-3"),
        ), result.value.relationships)
        assertTrue(result.value.validate().isEmpty())
        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals("/score-partwise[1]/part[1]/measure[1]/note[1]/notations[1]/slur[1]",
            mapping["relationships[0].startEventId"]?.location?.path)
        assertEquals("/score-partwise[1]/part[1]/measure[2]/note[1]/tie[1]", mapping["relationships[1].endEventId"]?.location?.path)
        assertEquals("2", mapping["relationships[3].endEventId"]?.location?.measureNumber)
    }

    @Test
    fun testOrphanAndDuplicateEndpointsReportUnmatchedSpan() {
        val xml = document(
            note("C", "<tie type='start'/>") +
                note("D", notations = "<slur type='stop'/>") +
                note("E", notations = "<slur type='start'/>") +
                note("F", notations = "<slur type='start'/><tied type='stop'/>") +
                note("G", notations = "<slur type='stop'/>"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(List(4) { DiagnosticCode.UNMATCHED_SPAN }, result.diagnostics.map { it.code })
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        // The duplicate start is reported when the next start appears; the rest at the end of the part.
        assertEquals(listOf("note[3]/notations[1]/slur[1]", "note[1]/tie[1]", "note[2]/notations[1]/slur[1]",
            "note[4]/notations[1]/tied[1]"), result.diagnostics.map { it.location.path.orEmpty().substringAfter("measure[1]/") })
        // Relationships follow the order their first endpoint was read.
        assertEquals(listOf(
            Relationship("P1:sound-tie-1", RelationshipKind.SOUND_TIE, event(1), null),
            Relationship("P1:slur-1", RelationshipKind.SLUR, null, event(2)),
            Relationship("P1:slur-2", RelationshipKind.SLUR, event(3), null),
            Relationship("P1:notated-tie-1", RelationshipKind.NOTATED_TIE, null, event(4)),
            Relationship("P1:slur-3", RelationshipKind.SLUR, event(4), event(5)),
        ), result.value.relationships)
        assertEquals(List(4) { ModelDiagnosticCode.MISSING_ENDPOINT }, result.value.validate().map { it.code })
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    @Test
    fun testTiesPairByPitchPreferringTheSameVoice() {
        val xml = document(
            note("C", "<tie type='start'/>", voice = "1") +
                note("C", "<tie type='start'/>", voice = "2") +
                note("C", "<tie type='stop'/>", voice = "2") +
                note("C", "<tie type='stop'/><tie type='start'/>", voice = "1") +
                note("C", "<tie type='stop'/>", voice = "1", alter = "1"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(
            Relationship("P1:sound-tie-1", RelationshipKind.SOUND_TIE, event(1), event(4)),
            Relationship("P1:sound-tie-2", RelationshipKind.SOUND_TIE, event(2), event(3)),
            Relationship("P1:sound-tie-3", RelationshipKind.SOUND_TIE, event(4), null),
            Relationship("P1:sound-tie-4", RelationshipKind.SOUND_TIE, null, event(5)),
        ), result.value.relationships)
        assertEquals(listOf("Sound tie on C4 start has no stop", "Sound tie on C#4 stop has no start"),
            result.diagnostics.map { it.message })
    }

    @Test
    fun testUnsupportedSpanContentIsDiscardedWithFeatureIds() {
        val xml = document(
            note("C", "<tie type='start' time-only='1'/>", notations = "<tied type='let-ring'/><fermata/><footnote>a</footnote>") +
                note("D", notations = "<slur type='start' placement='above'/>") +
                "<note><rest/><duration>1</duration><tie type='start'/><notations><slur type='stop'/></notations></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S09", "S09", "S12", "S25", "S09", "S09"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        // The slur keeps its endpoints, including a rest; ties on rests and restricted ties are dropped.
        assertEquals(listOf(Relationship("P1:slur-1", RelationshipKind.SLUR, event(2), event(3))), result.value.relationships)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidSpanTypesAndNumbersAreFatal() {
        for (policy in ParsePolicy.entries) {
            val options = ParseOptions(policy)
            failure(document(note("C", "<tie/>")), DiagnosticCode.INVALID_STRUCTURE, options)
            failure(document(note("C", "<tie type='continue'/>")), DiagnosticCode.INVALID_VALUE, options)
            failure(document(note("C", notations = "<slur type='let-ring'/>")), DiagnosticCode.INVALID_VALUE, options)
            failure(document(note("C", notations = "<slur type='start' number='0'/>")), DiagnosticCode.INVALID_VALUE, options)
            failure(document(note("C", notations = "<tied type='start' number='17'/>")), DiagnosticCode.INVALID_VALUE, options)
        }
        val maximum = success(document(note("C", notations = "<slur type='start' number='16'/>") +
            note("D", notations = "<slur type='stop' number='16'/>")))
        assertEquals(listOf(event(1) to event(2)), maximum.value.relationships.map { it.startEventId to it.endEventId })
    }

    @Test
    fun testTieAcrossStavesPairsByPitch() {
        val xml = document(
            "<attributes><staves>2</staves></attributes>" + note("C", "<tie type='start'/>") +
                "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><tie type='stop'/><staff>2</staff></note>",
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(listOf(Relationship("P1:sound-tie-1", RelationshipKind.SOUND_TIE, event(1), event(2))), result.value.relationships)
    }

    @Test
    fun testTimewiseSpansMatchPartwisePerPart() {
        fun part(id: String, step: String, tie: String) =
            "<part id='$id'>${if (tie == "start") "<attributes><divisions>1</divisions></attributes>" else ""}" +
                note(step, "<tie type='$tie'/>") + "</part>"
        val header = "<part-list><score-part id='P1'><part-name>A</part-name></score-part>" +
            "<score-part id='P2'><part-name>B</part-name></score-part></part-list>"
        val timewise = "<score-timewise version='4.0'>$header" +
            "<measure number='1'>${part("P2", "D", "start")}${part("P1", "C", "start")}</measure>" +
            "<measure number='2'>${part("P1", "C", "stop")}${part("P2", "D", "stop")}</measure></score-timewise>"
        fun body(id: String, step: String) = "<part id='$id'>" +
            "<measure number='1'><attributes><divisions>1</divisions></attributes>${note(step, "<tie type='start'/>")}</measure>" +
            "<measure number='2'>${note(step, "<tie type='stop'/>")}</measure></part>"
        val partwise = "<score-partwise version='4.0'>$header${body("P1", "C")}${body("P2", "D")}</score-partwise>"
        val expected = success(partwise).value
        assertEquals(expected, success(timewise).value)
        assertEquals(listOf(
            Relationship("P1:sound-tie-1", RelationshipKind.SOUND_TIE, "P1:measure-1:event-1", "P1:measure-2:event-1"),
            Relationship("P2:sound-tie-1", RelationshipKind.SOUND_TIE, "P2:measure-1:event-1", "P2:measure-2:event-1"),
        ), expected.relationships)
    }

    @Test
    fun testResolverPairsStopBeforeStartOnlyWhenMusicallyLater() {
        val reports = mutableListOf<String>()
        val resolver = SpanResolver("P1") { _, message -> reports += message }
        fun slur(id: String, onset: Int) =
            SpanEndpoint(RelationshipKind.SLUR, "1", id, "1", SpanPosition(1, Rational.of(onset)), SourceLocation(path = "/$id"))
        resolver.stop(slur("late-stop", 2))
        resolver.start(slur("early-start", 0))
        resolver.stop(slur("early-stop", 1))
        resolver.start(slur("later-start", 3))
        val spans = resolver.finish()
        assertEquals(listOf(
            Relationship("P1:slur-1", RelationshipKind.SLUR, "early-start", "late-stop"),
            Relationship("P1:slur-2", RelationshipKind.SLUR, null, "early-stop"),
            Relationship("P1:slur-3", RelationshipKind.SLUR, "later-start", null),
        ), spans.map { it.relationship })
        assertEquals(listOf("Slur 1 stop has no start", "Slur 1 start has no stop"), reports)
        assertEquals("/later-start", spans[2].startLocation?.path)
        assertNull(spans[2].endLocation)
    }

    @Test
    fun testResolverPairsWaitingStopsByArrivalAndVoice() {
        val resolver = SpanResolver("P1") { _, _ -> }
        fun slur(id: String, voice: String, measure: Int, onset: Int) =
            SpanEndpoint(RelationshipKind.SLUR, "1", id, voice, SpanPosition(measure, Rational.of(onset)), SourceLocation(path = "/$id"))
        listOf(5, 1, 4, 2, 3).forEach { resolver.stop(slur("stop-$it", "1", 1, it)) }
        resolver.stop(slur("other-voice-stop", "2", 1, 6))
        resolver.start(slur("start-a", "1", 1, 3))
        resolver.start(slur("start-b", "2", 1, 3))
        resolver.start(slur("start-c", "2", 1, 0))
        resolver.start(slur("start-d", "1", 2, 0))
        resolver.stop(slur("stop-d", "1", 2, 1))
        val pairs = resolver.finish().map { it.relationship }.filter { it.startEventId != null && it.endEventId != null }
            .associate { it.startEventId!! to it.endEventId!! }
        assertEquals(mapOf("start-a" to "stop-5", "start-b" to "other-voice-stop", "start-c" to "stop-1", "start-d" to "stop-d"), pairs)
    }

    @Test
    fun testResolverHandlesManyPendingEndpointsInLinearTime() {
        val resolver = SpanResolver("P1") { _, _ -> }
        val count = 50_000
        fun tie(id: String, voice: String, onset: Int) =
            SpanEndpoint(RelationshipKind.SOUND_TIE, "C4", id, voice, SpanPosition(1, Rational.of(onset)), SourceLocation(path = "/$id"))
        repeat(count) { resolver.stop(tie("stop-$it", "$it", it)) }
        repeat(count) { resolver.start(tie("start-$it", "start-$it", count)) }
        repeat(count) { resolver.stop(tie("late-stop-$it", "late-$it", count + 1)) }
        val spans = resolver.finish()
        assertEquals(2 * count, spans.size)
        assertEquals("late-stop-0", spans.first { it.relationship.startEventId == "start-0" }.relationship.endEventId)
    }

    private fun event(index: Int) = "P1:measure-1:event-$index"

    private fun note(step: String, ties: String = "", notations: String = "", voice: String = "1", alter: String? = null): String =
        "<note><pitch><step>$step</step>${alter?.let { "<alter>$it</alter>" }.orEmpty()}<octave>4</octave></pitch>" +
            "<duration>1</duration>$ties<voice>$voice</voice>${if (notations.isEmpty()) "" else "<notations>$notations</notations>"}</note>"

    private fun document(content: String): String =
        "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>1</divisions></attributes>$content</measure></part></score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), options))
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        assertNull(result.value)
        return result
    }
}
