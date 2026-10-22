package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Accidental
import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.NotatedValue
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteRole
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RationalError
import com.ryanjames.sheetmusic.score.RationalException
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.Staff
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoteImportTest {
    @Test
    fun testF01ImportsQuarterNoteAndRestAtExactOffsets() {
        val result = success(f01QuarterNoteAndRest, sourceName = "F01.musicxml")
        assertTrue(result.diagnostics.isEmpty())
        val part = result.value.parts.single()
        assertEquals(listOf(Staff(1)), part.staves)
        val measure = part.measures.single()
        assertEquals(Rational.of(2), measure.extent)
        assertEquals(listOf(
            AttributeEvent("P1:measure-1:event-1", Rational.ZERO, Attribute.TimeSignature(4, 4)),
            NoteEvent("P1:measure-1:event-2", Rational.ZERO, Rational.ONE, pitched(Step.C, 4),
                notation = NotatedValue(NoteValue.QUARTER)),
            RestEvent("P1:measure-1:event-3", Rational.ONE, Rational.ONE, notation = NotatedValue(NoteValue.QUARTER)),
        ), measure.events)
        assertTrue(result.value.validate().isEmpty())
        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals("/score-partwise[1]/part[1]/measure[1]/attributes[1]/time[1]",
            mapping["parts[0].measures[0].events[0]"]?.location?.path)
        assertEquals("/score-partwise[1]/part[1]/measure[1]/note[2]", mapping["parts[0].measures[0].events[2]"]?.location?.path)
        assertEquals("1", mapping["parts[0].measures[0].events[1]"]?.location?.measureNumber)
    }

    @Test
    fun testF03PreservesAlterAndPrintedAccidentalIndependently() {
        val note = assertIs<NoteEvent>(success(f03AlterWithPrintedNatural).value.parts.single().measures.single().events.single())
        assertEquals(pitched(Step.F, 4, Rational.ONE), note.pitch)
        assertEquals(NotatedValue(NoteValue.QUARTER, accidental = Accidental("natural", cautionary = true)), note.notation)
        val microtone = success(document("<note><pitch><step>B</step><alter>-0.5</alter><octave>3</octave></pitch><duration>1</duration></note>"))
        assertEquals(pitched(Step.B, 3, Rational.of(-1, 2)), assertIs<NoteEvent>(events(microtone).single()).pitch)
        val minimum = success(document("<note><pitch><step>C</step><alter>-2147483648</alter><octave>4</octave></pitch><duration>1</duration></note>"))
        assertEquals(pitched(Step.C, 4, Rational.of(Int.MIN_VALUE)), assertIs<NoteEvent>(events(minimum).single()).pitch)
    }

    @Test
    fun testF04DivisionsChangeKeepsExactOnsetsAndPersists() {
        val measures = success(f04DivisionsChange).value.parts.single().measures
        val notes = measures[0].events.map { assertIs<NoteEvent>(it) }
        assertEquals(listOf(Rational.ZERO, Rational.ONE, Rational.of(3, 2)), notes.map { it.onset })
        assertEquals(listOf(Rational.ONE, Rational.of(1, 2), Rational.of(3, 2)), notes.map { it.duration })
        assertEquals(NotatedValue(NoteValue.QUARTER, dots = 1), notes[2].notation)
        assertEquals(Rational.of(3), measures[0].extent)
        val rest = assertIs<RestEvent>(measures[1].events.single())
        assertEquals(Rational.ZERO, rest.onset)
        assertEquals(Rational.of(4), rest.duration)
        assertTrue(rest.wholeMeasure)
        assertNull(rest.notation.value)
        assertEquals("P1:measure-2:event-1", rest.id)
    }

    @Test
    fun testFractionalDivisionsCueRoleAndVoiceAreExact() {
        val result = success(document(
            "<note><cue/><pitch><step>G</step><octave>5</octave></pitch><duration>0.75</duration><voice>A</voice><staff>1</staff></note>",
            divisions = "1.5",
        ))
        val note = assertIs<NoteEvent>(events(result).single())
        assertEquals(Rational.of(1, 2), note.duration)
        assertEquals(NoteRole.CUE, note.role)
        assertEquals("A", note.voice)
    }

    @Test
    fun testZeroNegativeAndMalformedDurationsAreInvalidUnderBothPolicies() {
        val cases = listOf(
            document("<note><rest/><duration>0</duration></note>"),
            document("<note><rest/><duration>-1</duration></note>"),
            document("<note><rest/><duration>1e2</duration></note>"),
            document("<note><rest/><duration>.</duration></note>"),
            document("<note><rest/><duration>1</duration></note>", divisions = "0"),
        )
        for (xml in cases) {
            for (policy in ParsePolicy.entries) {
                val result = failure(xml, DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
                assertTrue(result.diagnostics.last().location.path.orEmpty().let { it.endsWith("/duration[1]") || it.endsWith("/divisions[1]") })
            }
        }
    }

    @Test
    fun testOverflowingDurationsReportNumericOverflow() {
        val cases = listOf(
            document("<note><rest/><duration>99999999999</duration></note>"),
            document("<note><rest/><duration>1</duration></note>", divisions = "0.0000000001"),
            document("<note><rest/><duration>2147483647</duration></note>", divisions = "0.5"),
            document("<note><rest/><duration>2147483647</duration></note><note><rest/><duration>1</duration></note>"),
        )
        for (xml in cases) {
            for (policy in ParsePolicy.entries) {
                val result = failure(xml, DiagnosticCode.NUMERIC_OVERFLOW, ParseOptions(policy))
                assertEquals("P1", result.diagnostics.last().location.partId)
            }
        }
        val boundary = success(document("<note><rest/><duration>2147483647</duration></note>"))
        assertEquals(Rational.of(Int.MAX_VALUE), events(boundary).single().let { assertIs<RestEvent>(it).duration })
    }

    @Test
    fun testMissingRequiredNoteStructureIsFatal() {
        failure(document("<note><duration>1</duration></note>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<note><rest/></note>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<note><rest/><rest/><duration>1</duration></note>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<note><rest/><duration>1</duration><duration>1</duration></note>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<note><pitch><step>C</step></pitch><duration>1</duration></note>"), DiagnosticCode.INVALID_STRUCTURE)
        val noDivisions = "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part>" +
            "</part-list><part id='P1'><measure number='1'><note><rest/><duration>1</duration></note></measure></part></score-partwise>"
        for (policy in ParsePolicy.entries) {
            val result = failure(noDivisions, DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy))
            assertTrue(result.diagnostics.single().location.path.orEmpty().endsWith("/note[1]/duration[1]"))
        }
    }

    @Test
    fun testInvalidPitchTypeAndFlagValuesAreFatal() {
        listOf(
            "<note><pitch><step>H</step><octave>4</octave></pitch><duration>1</duration></note>",
            "<note><pitch><step>C</step><octave>10</octave></pitch><duration>1</duration></note>",
            "<note><pitch><step>C</step><alter>sharp</alter><octave>4</octave></pitch><duration>1</duration></note>",
            "<note><rest/><duration>1</duration><type>quaver</type></note>",
            "<note><rest measure='maybe'/><duration>1</duration></note>",
            "<note><rest/><duration>1</duration><voice> </voice></note>",
            "<note><rest/><duration>1</duration><staff>0</staff></note>",
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><accidental editorial='1'>flat</accidental></note>",
        ).forEach { failure(document(it), DiagnosticCode.INVALID_VALUE, ParseOptions(ParsePolicy.TOLERANT)) }
    }

    @Test
    fun testUnsupportedNoteContentIsReportedWhileLaterOnsetsStayExact() {
        val xml = document(
            "<note default-x='10'><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><stem>up</stem></note>" +
                "<note><chord/><pitch><step>E</step><octave>4</octave></pitch><duration>1</duration></note>" +
                "<note><grace/><pitch><step>D</step><octave>4</octave></pitch></note>" +
                "<note><unpitched/><duration>1</duration></note>" +
                "<note><pitch><step>F</step><octave>4</octave></pitch><duration>1</duration><staff>2</staff></note>" +
                "<note><rest><display-step>B</display-step></rest><duration>1</duration><notations><fermata/></notations></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S04", "S10", "S06", "S18", "S16", "S06", "S17", "S12"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING && it.unsupported?.retention == ContentRetention.DISCARDED })
        val events = events(result)
        assertEquals(listOf(Rational.ZERO, Rational.of(3)), events.map { it.onset })
        assertIs<RestEvent>(events[1])
        assertEquals(Rational.of(4), result.value.parts.single().measures.single().extent)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testUninterpretedCursorMoveDiscardsLaterTimedContent() {
        val xml = document(
            "<note><rest/><duration>1</duration></note><backup><duration>1</duration></backup>" +
                "<note><rest/><duration>1</duration></note><attributes><time><beats>3</beats><beat-type>4</beat-type></time></attributes>",
        )
        val result = success(xml.replace("</part>", "<measure number='2'><note><rest/><duration>2</duration></note></measure></part>"),
            ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S06", "S06", "S06"), result.diagnostics.map { it.unsupported?.featureId })
        val measures = result.value.parts.single().measures
        assertEquals(1, measures[0].events.size)
        assertEquals(Rational.ONE, measures[0].extent)
        assertEquals(Rational.of(2), assertIs<RestEvent>(measures[1].events.single()).duration)
    }

    @Test
    fun testUnsupportedAttributesAndTimeFormsAreDiscarded() {
        val xml = document(
            "<attributes><key><fifths>0</fifths></key><time symbol='common'><beats>4</beats><beat-type>4</beat-type></time>" +
                "<time><beats>3+2</beats><beat-type>8</beat-type></time><time><senza-misura/></time>" +
                "<clef><sign>G</sign><line>2</line></clef></attributes><note><rest/><duration>1</duration></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07", "S07", "S07", "S07", "S07"), result.diagnostics.map { it.unsupported?.featureId })
        val events = events(result)
        assertEquals(Attribute.TimeSignature(4, 4), assertIs<AttributeEvent>(events[0]).attribute)
        assertIs<RestEvent>(events[1])
    }

    @Test
    fun testForeignTimeChildKeepsSimpleMeter() {
        val xml = document(
            "<attributes><time xmlns:e='urn:extension'><beats>3</beats><e:hint>x</e:hint><beat-type>4</beat-type></time>" +
                "</attributes><note><rest/><duration>1</duration></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("foreign-extension"), result.diagnostics.map { it.unsupported?.featureId })
        val events = events(result)
        assertEquals(Attribute.TimeSignature(3, 4), assertIs<AttributeEvent>(events[0]).attribute)
        assertIs<RestEvent>(events[1])
    }

    @Test
    fun testTimeNumberScopesMeterToStaff() {
        val xml = document(
            "<attributes><time number='2'><beats>3</beats><beat-type>4</beat-type></time>" +
                "<time number='1'><beats>2</beats><beat-type>4</beat-type></time></attributes><note><rest/><duration>1</duration></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07"), result.diagnostics.map { it.unsupported?.featureId })
        val events = events(result)
        assertEquals(2, events.size)
        assertEquals(Attribute.TimeSignature(2, 4), assertIs<AttributeEvent>(events[0]).attribute)
        assertIs<RestEvent>(events[1])
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
        failure(xml.replace("number='2'", "number='0'"), DiagnosticCode.INVALID_VALUE, ParseOptions(ParsePolicy.TOLERANT))
    }

    @Test
    fun testParseDecimalIsExact() {
        assertEquals(Rational.of(5, 2), parseDecimal(" 2.50 "))
        assertEquals(Rational.of(1, 2), parseDecimal(".5"))
        assertEquals(Rational.of(3), parseDecimal("+3."))
        assertEquals(Rational.of(-1, 4), parseDecimal("-0.25"))
        assertEquals(Rational.of(1, 1_000_000_000), parseDecimal("0.000000001"))
        assertEquals(Rational.of(Int.MAX_VALUE), parseDecimal("0002147483647.000"))
        assertEquals(Rational.of(Int.MIN_VALUE), parseDecimal("-2147483648"))
        assertEquals(Rational.of(Int.MIN_VALUE), parseDecimal("-0002147483648.000"))
        assertEquals(Rational.of(1, 9_765_625), parseDecimal("0.0000001024"))
        assertEquals(Rational.of(-1, 1 shl 30), parseDecimal("-0.000000000931322574615478515625"))
        assertEquals(Rational.of(Int.MAX_VALUE, 2), parseDecimal("1073741823.5"))
        listOf("", ".", "-", "1e3", "1,5", "0x10", "1 2").forEach { assertNull(parseDecimal(it), it) }
        listOf("2147483648", "-2147483649", "-2147483648.5", "0.0000000001", "12345678901", "2147483647.5",
            "0.0000000004656612873077392578125", "0.00000000000001",).forEach {
            assertEquals(RationalError.OVERFLOW, assertFailsWith<RationalException> { parseDecimal(it) }.code)
        }
    }

    private fun pitched(step: Step, octave: Int, alter: Rational = Rational.ZERO) =
        NotePitch.Pitched(WrittenPitch(step, octave, alter))

    private fun events(result: ParseResult.Success<Score>) = result.value.parts.single().measures.first().events

    private fun document(content: String, divisions: String = "1"): String =
        "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>$divisions</divisions></attributes>$content</measure></part></score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions(), sourceName: String? = null): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options, sourceName)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), options, "invalid.musicxml"))
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        assertNull(result.value)
        assertTrue(result.provenance.isEmpty())
        return result
    }
}
