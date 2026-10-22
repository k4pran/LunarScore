package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Accidental
import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.AttributeState
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.Event
import com.ryanjames.sheetmusic.score.KeyMode
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AttributeImportTest {
    private val cMajor = Attribute.KeySignature(0, KeyMode.MAJOR)
    private val gMajor = Attribute.KeySignature(1, KeyMode.MAJOR)
    private val treble = Attribute.Clef(ClefSign.G, 2)
    private val bass = Attribute.Clef(ClefSign.F, 4)

    @Test
    fun testF07EachEventSeesStateActiveAtItsOffset() {
        val result = success(f07MidMeasureAttributeChanges)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.value.validate().isEmpty())
        val part = result.value.parts.single()
        val (first, second, third) = part.measures
        assertEquals(
            listOf(cMajor, Attribute.TimeSignature(4, 4), treble, gMajor, Attribute.TimeSignature(2, 4), bass),
            first.events.filterIsInstance<AttributeEvent>().map { it.attribute },
        )
        assertEquals(listOf(0, 0, 0, 0, 1, 2, 2, 2, 2, 3).map { Rational.of(it) }, first.events.map { it.onset })
        assertEquals(Rational.of(4), first.extent)

        val states = part.effectiveStates()
        val before = AttributeState(Attribute.TimeSignature(4, 4), cMajor, treble)
        val after = AttributeState(Attribute.TimeSignature(2, 4), gMajor, bass)
        val notes = first.events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(before, before, after, after), notes.map { states[it.id] })
        assertEquals(after, states[second.events[0].id])
        val endClef = assertIs<AttributeEvent>(second.events[1])
        assertEquals(Rational.of(2), endClef.onset)
        assertEquals(Rational.of(2), second.extent)
        assertEquals(after.copy(clef = treble), states[third.events.single().id])

        // The key never rewrites written alter or infers a printed accidental.
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.F, 3, Rational.ONE)), notes[2].pitch)
        assertNull(notes[2].notation.accidental)
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.F, 3)), notes[3].pitch)
        assertEquals(Accidental("natural"), notes[3].notation.accidental)

        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals("/score-partwise[1]/part[1]/measure[1]/attributes[2]/clef[1]",
            mapping["parts[0].measures[0].events[7]"]?.location?.path)
    }

    @Test
    fun testMissingStateStaysUnknown() {
        val bare = success(document("<note><rest/><duration>1</duration></note>"))
        assertEquals(AttributeState(), bare.value.parts.single().effectiveStates().values.single())
        val keyOnly = success(document("<attributes><key><fifths>-3</fifths></key></attributes><note><rest/><duration>1</duration></note>"))
        val states = keyOnly.value.parts.single().effectiveStates()
        assertEquals(AttributeState(key = Attribute.KeySignature(-3)), states[events(keyOnly)[1].id])
    }

    @Test
    fun testKeyFormsAndUnsupportedKeyContent() {
        val result = success(document(
            "<attributes><key print-object='no'><cancel>2</cancel><fifths>-2</fifths><mode>minor</mode>" +
                "<key-octave number='1'>4</key-octave></key>" +
                "<key><fifths>3</fifths><mode>hypodorian</mode></key>" +
                "<key><key-step>F</key-step><key-alter>1</key-alter></key>" +
                "<key number='2'><fifths>4</fifths></key></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07", "S07", "S07", "S07", "S07", "S08"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        assertEquals(
            listOf(Attribute.KeySignature(-2, KeyMode.MINOR), Attribute.KeySignature(3)),
            events(result).map { assertIs<AttributeEvent>(it).attribute },
        )
        failure(document("<attributes><key><mode>major</mode></key></attributes>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<attributes><key><fifths>sharp</fifths></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><fifths>1</fifths><fifths>2</fifths></key></attributes>"), DiagnosticCode.INVALID_STRUCTURE)
    }

    @Test
    fun testClefFormsAndStandardLines() {
        val result = success(document(
            "<attributes><clef><sign>G</sign></clef><clef after-barline='yes'><sign>F</sign><line>3</line></clef>" +
                "<clef><sign>G</sign><line>2</line><clef-octave-change>-1</clef-octave-change></clef>" +
                "<clef><sign>percussion</sign></clef><clef><sign>C</sign></clef>" +
                "<clef number='2'><sign>F</sign></clef></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07", "S08"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(
            listOf(treble, Attribute.Clef(ClefSign.F, 3), Attribute.Clef(ClefSign.G, 2, -1),
                Attribute.Clef(ClefSign.PERCUSSION, null), Attribute.Clef(ClefSign.C, 3)),
            events(result).map { assertIs<AttributeEvent>(it).attribute },
        )
        for (policy in ParsePolicy.entries) {
            failure(document("<attributes><clef><sign>H</sign></clef></attributes>"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            failure(document("<attributes><clef><line>2</line></clef></attributes>"), DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy))
            failure(document("<attributes><clef><sign>G</sign><line>x</line></clef></attributes>"), DiagnosticCode.INVALID_VALUE,
                ParseOptions(policy))
            failure(document("<attributes><clef number='0'><sign>G</sign></clef></attributes>"), DiagnosticCode.INVALID_VALUE,
                ParseOptions(policy))
        }
    }

    @Test
    fun testStavesAndStaffDetails() {
        val result = success(document(
            "<attributes><staves>1</staves><staff-details><staff-lines>1</staff-lines><staff-tuning line='1'><tuning-step>E</tuning-step>" +
                "<tuning-octave>2</tuning-octave></staff-tuning><staff-size>80</staff-size></staff-details>" +
                "<staff-details number='2'><staff-lines>5</staff-lines></staff-details><staves>2</staves></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S23", "S17", "S08", "S08"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(listOf<Attribute>(Attribute.StaffLines(1)), events(result).map { assertIs<AttributeEvent>(it).attribute })
        failure(document("<attributes><staves>0</staves></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><staff-details><staff-lines>-1</staff-lines></staff-details></attributes>"),
            DiagnosticCode.INVALID_VALUE)
    }

    @Test
    fun testOtherAttributesChildrenAreReportedByFeature() {
        val result = success(document(
            "<attributes><part-symbol>brace</part-symbol><transpose><chromatic>-2</chromatic></transpose>" +
                "<measure-style><slash type='start'/></measure-style><directive>Allegro</directive></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S08", "S16", "S24", "S07"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(events(result).isEmpty())
    }

    @Test
    fun testAttributeChangesAfterCursorMoveAreDiscarded() {
        val result = success(document(
            "<note><rest/><duration>1</duration></note><backup><duration>1</duration></backup>" +
                "<attributes><key><fifths>1</fifths></key><clef><sign>F</sign></clef><staff-details><staff-lines>4</staff-lines>" +
                "</staff-details></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S06", "S06", "S06", "S06"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(1, events(result).size)
    }

    private fun events(result: ParseResult.Success<Score>): List<Event> = result.value.parts.single().measures.first().events

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
