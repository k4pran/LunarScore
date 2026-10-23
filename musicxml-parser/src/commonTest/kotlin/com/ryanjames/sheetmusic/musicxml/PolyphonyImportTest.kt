package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.Event
import com.ryanjames.sheetmusic.score.NotatedValue
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.SoundingPitch
import com.ryanjames.sheetmusic.score.Staff
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PolyphonyImportTest {
    private val treble = Attribute.Clef(ClefSign.G, 2)
    private val bass = Attribute.Clef(ClefSign.F, 4)

    @Test
    fun testF05BackupAndForwardPlaceVoicesAtExactOnsets() {
        val result = success(f05BackupAndForward)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.value.validate().isEmpty())
        val (first, second) = result.value.parts.single().measures
        assertEquals(listOf(
            AttributeEvent("P1:measure-1:event-1", Rational.ZERO, Attribute.TimeSignature(4, 4)),
            AttributeEvent("P1:measure-1:event-2", Rational.ZERO, treble),
            NoteEvent("P1:measure-1:event-3", Rational.ZERO, Rational.of(2), pitched(Step.C, 5), notation = half),
            NoteEvent("P1:measure-1:event-4", Rational.of(2), Rational.of(2), pitched(Step.D, 5), notation = half),
            NoteEvent("P1:measure-1:event-5", Rational.ZERO, Rational.ONE, pitched(Step.E, 4), voice = "2", notation = quarter),
            // The forward leaves voice 2 empty from 1 to 2; a gap is not a rest event.
            NoteEvent("P1:measure-1:event-6", Rational.of(2), Rational.of(2), pitched(Step.F, 4), voice = "2", notation = half),
        ), first.events)
        assertEquals(Rational.of(4), first.extent)
        val rest = assertIs<RestEvent>(second.events[0])
        assertEquals(Rational.of(4), rest.duration)
        val late = assertIs<NoteEvent>(second.events[1])
        assertEquals(Rational.of(3), late.onset)
        assertEquals("2", late.voice)
        assertEquals(Rational.of(4), second.extent)
        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals("/score-partwise[1]/part[1]/measure[1]/note[3]", mapping["parts[0].measures[0].events[4]"]?.location?.path)
    }

    @Test
    fun testF06ChordTonesShareReferenceOnsetAndKeepTheirDurations() {
        val result = success(f06ChordsAndCrossStaff)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.value.validate().isEmpty())
        val part = result.value.parts.single()
        assertEquals(listOf(Staff(1), Staff(2)), part.staves)
        val measure = part.measures.single()
        val clefs = measure.events.take(2).map { assertIs<AttributeEvent>(it) }
        assertEquals(listOf(treble to 1, bass to 2), clefs.map { it.attribute to it.staff })
        val notes = measure.events.drop(2).map { assertIs<NoteEvent>(it) }
        val chordId = "P1:measure-1:chord-1"
        assertEquals(listOf(0, 0, 0, 0, 2).map { Rational.of(it) }, notes.map { it.onset })
        assertEquals(listOf(Rational.of(2), Rational.ONE, Rational.of(2), Rational.of(2), Rational.ONE), notes.map { it.duration })
        assertEquals(listOf(1, 1, 1, 2, 1), notes.map { it.staff })
        assertEquals(listOf(chordId, chordId, chordId, chordId, null), notes.map { it.chordId })
        assertEquals(pitched(Step.C, 3), notes[3].pitch)
        assertEquals(Rational.of(3), measure.extent)
        val states = part.effectiveStates()
        assertEquals(bass, states[notes[3].id]?.clef)
        assertEquals(treble, states[notes[0].id]?.clef)
    }

    @Test
    fun testLongerChordToneIsDiagnosedAndCursorFollowsReference() {
        val xml = f06ChordsAndCrossStaff.replace(
            "<step>E</step><octave>4</octave></pitch><duration>2</duration>",
            "<step>E</step><octave>4</octave></pitch><duration>6</duration>",
        )
        val strict = failure(xml, DiagnosticCode.INCONSISTENT_CHORD_DURATION)
        assertTrue(strict.diagnostics.single().location.path.orEmpty().endsWith("/note[2]/duration[1]"))
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        val diagnostic = result.diagnostics.single()
        assertEquals(DiagnosticCode.INCONSISTENT_CHORD_DURATION, diagnostic.code)
        assertEquals(DiagnosticSeverity.WARNING, diagnostic.severity)
        val notes = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(Rational.of(3), notes[1].duration)
        assertEquals(Rational.of(2), notes[4].onset)
        assertEquals(Rational.of(3), result.value.parts.single().measures.single().extent)
    }

    @Test
    fun testF08PartsAndStavesStaySeparate() {
        val result = success(f08PartsAndPianoStaves)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.value.validate().isEmpty())
        val (flute, piano) = result.value.parts
        assertEquals(listOf(Staff(1)), flute.staves)
        assertEquals(listOf(Staff(1), Staff(2)), piano.staves)
        assertEquals(listOf(Rational.ZERO), flute.measures[0].events.filterIsInstance<NoteEvent>().map { it.onset })
        assertEquals(listOf(Rational.ZERO, Rational.of(2)), flute.measures[1].events.map { it.onset })

        val pianoFirst = piano.measures[0].events
        val key = Attribute.KeySignature(0)
        val attributes = pianoFirst.filterIsInstance<AttributeEvent>().map { it.attribute to it.staff }
        // Unnumbered key and time apply to both staves; staves is declared after them.
        assertEquals(
            listOf(key to 1, key to 2, Attribute.TimeSignature(4, 4) to 1, Attribute.TimeSignature(4, 4) to 2, treble to 1, bass to 2),
            attributes,
        )
        val notes = pianoFirst.filterIsInstance<NoteEvent>()
        assertEquals(listOf(Triple(0, 1, "1"), Triple(2, 1, "1"), Triple(0, 2, "5")),
            notes.map { Triple(it.onset.numerator, it.staff, it.voice) })
        assertEquals(listOf("P2:measure-1:event-7", "P2:measure-1:event-8", "P2:measure-1:event-9"), notes.map { it.id })
        assertEquals(Rational.of(4), piano.measures[0].extent)

        val pianoSecond = piano.measures[1].events
        assertEquals(listOf(Pair(0, 1), Pair(0, 2), Pair(2, 2)), pianoSecond.map { it.onset.numerator to it.staff })
        assertTrue(assertIs<RestEvent>(pianoSecond[0]).wholeMeasure)

        val states = piano.effectiveStates()
        assertEquals(bass, states[notes[2].id]?.clef)
        assertEquals(key, states[notes[2].id]?.key)
        assertEquals(bass, states[pianoSecond[2].id]?.clef)
        assertEquals(treble, flute.effectiveStates()[flute.measures[1].events.last().id]?.clef)
        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals("/score-partwise[1]/part[2]/measure[1]/attributes[1]/key[1]", mapping["parts[1].measures[0].events[1]"]?.location?.path)
    }

    @Test
    fun testBackupBeforeMeasureStartIsDiagnosed() {
        val xml = document(
            "<note><rest/><duration>1</duration></note><backup><duration>2</duration></backup>" +
                "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><voice>2</voice></note>",
        )
        val strict = failure(xml, DiagnosticCode.INVALID_CURSOR_MOVE)
        assertTrue(strict.diagnostics.single().location.path.orEmpty().endsWith("/backup[1]"))
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        val diagnostic = result.diagnostics.single()
        assertEquals(DiagnosticCode.INVALID_CURSOR_MOVE, diagnostic.code)
        assertEquals(DiagnosticSeverity.WARNING, diagnostic.severity)
        assertTrue("clamped" in diagnostic.message)
        assertEquals(Rational.ZERO, events(result)[1].onset)
        // Backing up exactly to the measure start is valid.
        assertTrue(success(xml.replace("<duration>2</duration></backup>", "<duration>1</duration></backup>")).diagnostics.isEmpty())
    }

    @Test
    fun testInvalidCursorMoveContentIsFatal() {
        val rest = "<note><rest/><duration>2</duration></note>"
        listOf(
            "$rest<backup/>" to DiagnosticCode.INVALID_STRUCTURE,
            "$rest<backup><duration>1</duration><duration>1</duration></backup>" to DiagnosticCode.INVALID_STRUCTURE,
            "$rest<backup><duration>0</duration></backup>" to DiagnosticCode.INVALID_VALUE,
            "<forward><duration>-1</duration></forward>" to DiagnosticCode.INVALID_VALUE,
            "<forward><duration>x</duration></forward>" to DiagnosticCode.INVALID_VALUE,
            "<forward><duration>1</duration><voice> </voice></forward>" to DiagnosticCode.INVALID_VALUE,
            "<forward><duration>1</duration><staff>2</staff></forward>" to DiagnosticCode.INVALID_VALUE,
            "<forward><duration>99999999999</duration></forward>" to DiagnosticCode.NUMERIC_OVERFLOW,
        ).forEach { (content, code) ->
            for (policy in ParsePolicy.entries) failure(document(content), code, ParseOptions(policy))
        }
        // Missing divisions are recovered as 1, as for note durations.
        val noDivisions = document("<forward><duration>1</duration></forward>").replace("<attributes><divisions>1</divisions></attributes>", "")
        failure(noDivisions, DiagnosticCode.RECOVERED_VALUE)
        val recovered = success(noDivisions, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(DiagnosticCode.RECOVERED_VALUE, recovered.diagnostics.single().code)
        assertEquals(Rational.ONE, recovered.value.parts.single().measures.single().extent)
    }

    @Test
    fun testCursorMoveExtrasAreReportedWhileMovesApply() {
        val result = success(document(
            "<note><rest/><duration>2</duration></note><backup><duration>2</duration><voice>1</voice><footnote>x</footnote></backup>" +
                "<forward><duration>1</duration><voice>2</voice><staff>1</staff><level>y</level></forward>" +
                "<note><rest/><duration>1</duration></note>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S06", "S25", "S25"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(Rational.ONE, events(result)[1].onset)
    }

    @Test
    fun testChordWithoutReferenceNoteIsInvalid() {
        val chord = "<note><chord/><pitch><step>E</step><octave>4</octave></pitch><duration>1</duration></note>"
        val rest = "<note><rest/><duration>1</duration></note>"
        listOf(
            chord,
            "$rest<backup><duration>1</duration></backup>$chord",
            "$rest<forward><duration>1</duration></forward>$chord",
        ).forEach { content ->
            for (policy in ParsePolicy.entries) failure(document(content), DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy))
        }
        failure(document("$rest<note><chord/><chord/><rest/><duration>1</duration></note>"), DiagnosticCode.INVALID_STRUCTURE)
    }

    @Test
    fun testRestChordMembersKeepSharedOnset() {
        val result = success(document(
            "<note><rest/><duration>2</duration></note>" +
                "<note><chord/><pitch><step>E</step><octave>4</octave></pitch><duration>1</duration></note>" +
                "<note><unpitched><display-step>E</display-step><display-octave>4</display-octave></unpitched><duration>2</duration></note>" +
                "<note><chord/><pitch><step>G</step><octave>4</octave></pitch><duration>2</duration></note>" +
                "<note><chord/><pitch><step>B</step><octave>4</octave></pitch><duration>1</duration></note>" +
                "<note><pitch><step>D</step><octave>5</octave></pitch><duration>1</duration></note>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        // Only the rest grouping is lost; an unpitched note can be a chord reference.
        assertEquals(listOf("S06"), result.diagnostics.map { it.unsupported?.featureId })
        val events = events(result)
        assertEquals(listOf(0, 0, 2, 2, 2, 4).map { Rational.of(it) }, events.map { it.onset })
        val notes = events.filterIsInstance<NoteEvent>()
        assertEquals(
            listOf("P1:measure-1:chord-1", "P1:measure-1:chord-2", "P1:measure-1:chord-2", "P1:measure-1:chord-2", null),
            notes.map { it.chordId },
        )
        failure(document("<note><rest/><duration>1</duration></note>" +
            "<note><chord/><rest/><duration>1</duration></note>"), DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testNoteStaffBeyondDeclaredStavesIsInvalid() {
        val note = "<note><rest/><duration>1</duration><staff>2</staff></note>"
        for (policy in ParsePolicy.entries) {
            val result = failure(document(note), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            assertTrue(result.diagnostics.single().location.path.orEmpty().endsWith("/note[1]/staff[1]"))
        }
        val declared = success(document("<attributes><staves>2</staves></attributes>$note<attributes><staves>1</staves></attributes>"))
        assertEquals(2, events(declared).single().staff)
        assertEquals(listOf(Staff(1), Staff(2)), declared.value.parts.single().staves)
        failure(document("<attributes><staves>2</staves></attributes><attributes><staves>1</staves></attributes>$note"),
            DiagnosticCode.INVALID_VALUE)
    }

    @Test
    fun testDeclaredStavesAreBoundedAcrossParts() {
        for (policy in ParsePolicy.entries) {
            failure(document("<attributes><staves>2147483647</staves><key><fifths>0</fifths></key></attributes>"),
                DiagnosticCode.STAFF_LIMIT, ParseOptions(policy))
        }
        val limits = ParseLimits(maxStaves = 3)
        val exact = success(document("<attributes><staves>3</staves></attributes><attributes><staves>2</staves></attributes>"),
            ParseOptions(limits = limits))
        assertEquals(listOf(Staff(1), Staff(2), Staff(3)), exact.value.parts.single().staves)
        val twoParts = "<score-partwise version='4.0'><part-list>" +
            "<score-part id='P1'><part-name>A</part-name></score-part><score-part id='P2'><part-name>B</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><staves>2</staves></attributes></measure></part>" +
            "<part id='P2'><measure number='1'><attributes><staves>2</staves></attributes></measure></part></score-partwise>"
        failure(twoParts, DiagnosticCode.STAFF_LIMIT, ParseOptions(limits = limits))
    }

    private val half = NotatedValue(NoteValue.HALF)
    private val quarter = NotatedValue(NoteValue.QUARTER)

    /** A written pitch that sounds as written, since these fixtures have no transposition. */
    private fun pitched(step: Step, octave: Int): NotePitch.Pitched {
        val semitones = (octave + 1) * 12 + listOf(0, 2, 4, 5, 7, 9, 11)[Step.entries.indexOf(step)]
        return NotePitch.Pitched(WrittenPitch(step, octave), SoundingPitch(Rational.of(semitones)))
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
        assertTrue(result.provenance.isEmpty())
        return result
    }
}
