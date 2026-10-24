package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.DirectionEvent
import com.ryanjames.sheetmusic.score.Instrument
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.SoundingPitch
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.Transposition
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InstrumentImportTest {
    private val clarinet = Transposition(Rational.of(-2), -1)
    private val altoSax = Transposition(Rational.of(-9), -5)

    @Test
    fun testF17TransposingInstrumentKeepsWrittenPitchAndDerivesSounding() {
        val result = success(f17InstrumentsAndTransposition)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertTrue(result.value.validate().isEmpty())
        val part = result.value.parts[0]
        assertEquals(listOf(Instrument("P1-I1", "B-flat Clarinet"), Instrument("P1-I2", "Alto Saxophone")), part.instruments)

        val notes = part.measures.flatMap { it.events }.filterIsInstance<NoteEvent>()
        assertEquals(
            listOf(WrittenPitch(Step.D, 5), WrittenPitch(Step.E, 4), WrittenPitch(Step.B, 4), WrittenPitch(Step.A, 4)),
            notes.map { assertIs<NotePitch.Pitched>(it.pitch).written },
        )
        assertEquals(
            listOf(72, 62, 69, 60).map { SoundingPitch(Rational.of(it)) },
            notes.map { assertIs<NotePitch.Pitched>(it.pitch).sounding },
        )
        assertEquals(listOf("P1-I1", "P1-I1", "P1-I1", "P1-I2"), notes.map { it.instrumentId })

        // The mid-measure change applies at beat 3 of measure 2, after the clarinet's half note.
        val second = part.measures[1]
        assertEquals(
            listOf(Attribute.Transpose(altoSax), Attribute.InstrumentChange("P1-I2")),
            second.events.filterIsInstance<AttributeEvent>().map { it.attribute },
        )
        assertTrue(second.events.filterIsInstance<AttributeEvent>().all { it.onset == Rational.of(2) })
        assertEquals(Rational.of(2), assertIs<DirectionEvent>(second.events[2]).onset)
        val states = part.effectiveStates()
        assertEquals(Attribute.Transpose(clarinet), states.getValue(notes[2].id).transpose)
        assertNull(states.getValue(notes[2].id).instrument)
        assertEquals(Attribute.Transpose(altoSax), states.getValue(notes[3].id).transpose)
        assertEquals(Attribute.InstrumentChange("P1-I2"), states.getValue(notes[3].id).instrument)

        val instrumentPath = result.provenance.single { it.modelPath == "parts[0].instruments[1]" }.location.path
        assertTrue(instrumentPath.orEmpty().endsWith("/score-instrument[2]"), instrumentPath)
        val changePath = result.provenance.single { it.modelPath == "parts[0].measures[1].events[3]" }.location.path
        assertTrue(changePath.orEmpty().endsWith("/instrument-change[1]"), changePath)
    }

    @Test
    fun testF17UnpitchedPercussionKeepsDisplayPositionAndMidiKeys() {
        val result = success(f17InstrumentsAndTransposition)
        val part = result.value.parts[1]
        assertEquals(listOf(Instrument("P2-I36", "Bass Drum", 35), Instrument("P2-I39", "Snare Drum", 38)), part.instruments)
        val events = part.measures[0].events
        val notes = events.filterIsInstance<NoteEvent>()
        assertEquals(
            listOf(NotePitch.Unpitched(Step.F, 4), NotePitch.Unpitched(Step.C, 5), NotePitch.Unpitched(Step.C, 5)),
            notes.map { it.pitch },
        )
        assertEquals(listOf("P2-I36", "P2-I39", "P2-I39"), notes.map { it.instrumentId })
        assertEquals(listOf(0, 1, 2).map { Rational.of(it) }, notes.map { it.onset })
        assertIs<RestEvent>(events.last())
        val tie = result.value.relationships.single { it.kind == RelationshipKind.SOUND_TIE }
        assertEquals(notes[1].id to notes[2].id, tie.startEventId to tie.endEventId)
    }

    @Test
    fun testF21ConcertScoreSoundsAsWrittenExceptOctaveTranspositions() {
        val result = success(f21ConcertScore, ParseOptions(ParsePolicy.TOLERANT))
        // The for-part transposition only describes a generated part; it is reported, not applied.
        assertEquals(listOf("S16"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.single().location.path.orEmpty().endsWith("/for-part[1]"))
        val (clarinet, piccolo) = result.value.parts.map { part ->
            part.measures.single().events.filterIsInstance<NoteEvent>().single()
        }
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.C, 5), SoundingPitch(Rational.of(72))), clarinet.pitch)
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.C, 5), SoundingPitch(Rational.of(84))), piccolo.pitch)
        assertEquals(listOf("P1-I1", "P2-I1"), listOf(clarinet.instrumentId, piccolo.instrumentId))
        failure(f21ConcertScore, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testNonOctaveTransposeInConcertScoreLeavesSoundingUnknown() {
        val xml = document(
            "<attributes><transpose><diatonic>-1</diatonic><chromatic>-2</chromatic></transpose></attributes>" +
                note("D", 5) +
                "<attributes><transpose><chromatic>0</chromatic><octave-change>-1</octave-change></transpose></attributes>" +
                note("D", 5),
            defaults = "<defaults><concert-score/></defaults>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S16"), result.diagnostics.map { it.unsupported?.featureId })
        val (ambiguous, octave) = notes(result)
        // The written pitch and the transpose are both kept; only the sounding pitch is unknown.
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.D, 5)), ambiguous.pitch)
        assertEquals(
            Attribute.Transpose(Transposition(Rational.of(-2), -1)),
            result.value.parts.single().measures.single().events.filterIsInstance<AttributeEvent>()[0].attribute,
        )
        assertEquals(SoundingPitch(Rational.of(62)), assertIs<NotePitch.Pitched>(octave.pitch).sounding)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testTranspositionIsExactAndPersistsAcrossMeasures() {
        val xml = "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>1</divisions>" +
            "<transpose><diatonic>-1</diatonic><chromatic>-2</chromatic><octave-change>-1</octave-change></transpose></attributes>" +
            "<note><pitch><step>C</step><alter>0.5</alter><octave>4</octave></pitch><duration>1</duration></note></measure>" +
            "<measure number='2'>${note("C", 4)}</measure></part></score-partwise>"
        val result = success(xml)
        val notes = result.value.parts.single().measures.flatMap { it.events }.filterIsInstance<NoteEvent>()
        assertEquals(Rational.of(1, 2), assertIs<NotePitch.Pitched>(notes[0].pitch).written.alter)
        // A bass clarinet: down a major ninth, from a quarter-sharp C4.
        assertEquals(
            listOf(SoundingPitch(Rational.of(93, 2)), SoundingPitch(Rational.of(46))),
            notes.map { assertIs<NotePitch.Pitched>(it.pitch).sounding },
        )
        // Without score-instruments, notes have no instrument.
        assertTrue(notes.all { it.instrumentId == null })
    }

    @Test
    fun testUnrepresentableSoundingPitchIsReportedAndLeftUnknown() {
        val xml = document(
            "<note><pitch><step>B</step><alter>2147483647</alter><octave>9</octave></pitch><duration>1</duration></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S16"), result.diagnostics.map { it.unsupported?.featureId })
        val pitch = assertIs<NotePitch.Pitched>(notes(result).single().pitch)
        assertEquals(Rational.of(Int.MAX_VALUE), pitch.written.alter)
        assertNull(pitch.sounding)
    }

    @Test
    fun testNotesWithoutInstrumentInMultiInstrumentPartStayUnassigned() {
        val result = success(document(note("C", 4), instruments = INSTRUMENTS))
        assertNull(notes(result).single().instrumentId)
    }

    @Test
    fun testUnsupportedInstrumentContentIsReported() {
        val xml = document(
            "<attributes><instruments>2</instruments>" +
                "<transpose><chromatic>-2</chromatic><double/></transpose></attributes>" +
                "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><instrument id='I1'/><instrument id='I2'/></note>" +
                "<note><rest/><duration>1</duration><instrument id='I1'/></note>" +
                "<direction><direction-type><words>Solo</words></direction-type>" +
                "<sound><instrument-change id='I2'><solo/></instrument-change><midi-instrument id='I2'/></sound></direction>",
            instruments = "<score-instrument id='I1'><instrument-name>A</instrument-name><instrument-abbreviation>a</instrument-abbreviation></score-instrument>" +
                "<score-instrument id='I2'><instrument-name>B</instrument-name><virtual-instrument/></score-instrument>" +
                "<midi-instrument id='I1'><midi-channel>1</midi-channel></midi-instrument>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf("S16", "S26", "S26", "S16", "S16", "S16", "S16", "S26", "S26"),
            result.diagnostics.map { it.unsupported?.featureId },
        )
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        val events = result.value.parts.single().measures.single().events
        // The doubled transpose is kept.
        assertEquals(
            listOf(Attribute.Transpose(Transposition(Rational.of(-2))), Attribute.InstrumentChange("I2")),
            events.filterIsInstance<AttributeEvent>().map { it.attribute },
        )
        assertEquals(Rational.of(2), events.filterIsInstance<AttributeEvent>()[1].onset)
        assertEquals("I1", notes(result).single().instrumentId)
        assertTrue(result.value.validate().isEmpty())
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidInstrumentStructureIsFatal() {
        listOf(
            document("", instruments = "<score-instrument id='I1'/>"),
            document("", instruments = "<score-instrument><instrument-name>A</instrument-name></score-instrument>"),
            document("", instruments = INSTRUMENTS + "<score-instrument id='I1'><instrument-name>C</instrument-name></score-instrument>"),
            document("", instruments = INSTRUMENTS + "<midi-instrument id='I9'/>"),
            document("<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><instrument id='I9'/></note>", instruments = INSTRUMENTS),
            document("<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><instrument/></note>", instruments = INSTRUMENTS),
            document("<direction><direction-type><words>x</words></direction-type><sound><instrument-change id='I9'/></sound></direction>", instruments = INSTRUMENTS),
            document("<attributes><transpose><diatonic>-1</diatonic></transpose></attributes>"),
            document("<note><unpitched><display-step>E</display-step></unpitched><duration>1</duration></note>"),
        ).forEach { failure(it, DiagnosticCode.INVALID_STRUCTURE, ParseOptions(ParsePolicy.TOLERANT)) }
    }

    @Test
    fun testInvalidInstrumentValuesAreFatal() {
        listOf(
            document("", instruments = INSTRUMENTS + "<midi-instrument id='I1'><midi-unpitched>0</midi-unpitched></midi-instrument>"),
            document("", instruments = INSTRUMENTS + "<midi-instrument id='I1'><midi-unpitched>129</midi-unpitched></midi-instrument>"),
            document("<attributes><transpose><chromatic>down</chromatic></transpose></attributes>"),
            document("<attributes><transpose><diatonic>1.5</diatonic><chromatic>2</chromatic></transpose></attributes>"),
            document("<note><unpitched><display-step>H</display-step><display-octave>4</display-octave></unpitched><duration>1</duration></note>"),
            document("<note><unpitched><display-step>E</display-step><display-octave>10</display-octave></unpitched><duration>1</duration></note>"),
        ).forEach { failure(it, DiagnosticCode.INVALID_VALUE, ParseOptions(ParsePolicy.TOLERANT)) }
    }

    @Test
    fun testTransposeTargetsItsStaff() {
        val xml = document(
            "<attributes><staves>2</staves><transpose number='2'><chromatic>-2</chromatic></transpose></attributes>" +
                note("D", 5) + "<backup><duration>1</duration></backup>" +
                "<note><pitch><step>D</step><octave>5</octave></pitch><duration>1</duration><staff>2</staff></note>",
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(2, result.value.parts.single().measures.single().events.filterIsInstance<AttributeEvent>().single().staff)
        // Written D5 (74) sounds as written on staff 1 and a tone lower on staff 2.
        assertEquals(
            listOf(74, 72).map { SoundingPitch(Rational.of(it)) },
            notes(result).map { assertIs<NotePitch.Pitched>(it.pitch).sounding },
        )
        failure(xml.replace("<staves>2</staves>", ""), DiagnosticCode.INVALID_VALUE)
    }

    @Test
    fun testTransposeAppliesFromItsOnsetAfterBackup() {
        val xml = "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>1</divisions></attributes>" +
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice></note>" +
            "<attributes><transpose><chromatic>-2</chromatic></transpose></attributes>" +
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice></note>" +
            "<backup><duration>3</duration></backup>" +
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><voice>2</voice></note></measure>" +
            "<measure number='2'>${note("C", 4)}</measure></part></score-partwise>"
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val part = result.value.parts.single()
        val notes = part.measures.flatMap { it.events }.filterIsInstance<NoteEvent>()
        // The voice-2 note at offset 0 precedes the transpose at offset 2, though it follows it in the source.
        assertEquals(
            listOf(60, 58, 60, 58).map { SoundingPitch(Rational.of(it)) },
            notes.map { assertIs<NotePitch.Pitched>(it.pitch).sounding },
        )
        val states = part.effectiveStates()
        val chromatic = listOf(null, Rational.of(-2), null, Rational.of(-2))
        assertEquals(chromatic, notes.map { states.getValue(it.id).transpose?.transposition?.chromatic })
    }

    private fun note(step: String, octave: Int) =
        "<note><pitch><step>$step</step><octave>$octave</octave></pitch><duration>1</duration></note>"

    private fun notes(result: ParseResult.Success<Score>) =
        result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()

    private fun document(content: String, instruments: String = "", defaults: String = ""): String =
        "<score-partwise version='4.0'>$defaults<part-list><score-part id='P1'><part-name>V</part-name>$instruments</score-part></part-list>" +
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

    private companion object {
        const val INSTRUMENTS = "<score-instrument id='I1'><instrument-name>A</instrument-name></score-instrument>" +
            "<score-instrument id='I2'><instrument-name>B</instrument-name></score-instrument>"
    }
}
