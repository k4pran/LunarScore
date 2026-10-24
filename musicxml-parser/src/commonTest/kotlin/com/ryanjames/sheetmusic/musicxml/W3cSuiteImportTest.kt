package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.MetadataKind
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * MX-04 independent fixtures: W3C test-suite files whose expected musical facts come from the
 * suite's own descriptions and source, not from this importer. Parse outcomes and semantics are
 * asserted separately; schema validity is checked on the JVM by `XsdValidationTest`.
 */
class W3cSuiteImportTest {
    @Test
    fun testParseOutcomesUnderBothPolicies() {
        w3cSuiteFixtures.forEach { fixture ->
            val tolerant = parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT), fixture.file)
            val score = assertIs<ParseResult.Success<Score>>(tolerant, "${fixture.file}: ${tolerant.diagnostics}").value
            assertTrue(score.validate().isEmpty(), fixture.file)
            val strict = parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.STRICT), fixture.file)
            if (fixture.file in RECOVERED_STRICT_FAILURES) {
                val failure = assertIs<ParseResult.Failure>(strict, fixture.file)
                assertEquals(DiagnosticCode.RECOVERED_VALUE, failure.diagnostics.first { it.severity == DiagnosticSeverity.ERROR }.code)
            } else {
                assertIs<ParseResult.Success<Score>>(strict, "${fixture.file}: ${strict.diagnostics}")
                assertTrue(strict.diagnostics.isEmpty(), "${fixture.file}: ${strict.diagnostics}")
            }
        }
    }

    // F01, 03a: "All note durations, from maxima, long, brevis, whole, etc., until 1024th. First with
    // their plain values, then dotted, and finally double-dotted."
    @Test
    fun testF01DurationsFromMaximaTo1024thPlainDottedAndDoubleDotted() {
        val notes = notes(part(w3c03aRhythmDurations))
        val values = NoteValue.entries
        for (dots in 0..2) {
            val group = notes.filter { it.notation.dots == dots }
            assertEquals(values, group.mapNotNull { it.notation.value }.distinct(), "dots=$dots")
        }
        // A maxima is 32 quarters; each dot adds half of the previous value.
        notes.forEach { note ->
            val base = Rational.of(32) / Rational.of(1 shl values.indexOf(note.notation.value))
            val factor = Rational.of(2) - Rational.of(1, 1 shl note.notation.dots)
            assertEquals(base * factor, note.duration, "${note.notation}")
        }
    }

    // F02, 41a: "A piece with four parts named 'P0', 'P1', 'P2', and 'P3' (in that order)."
    @Test
    fun testF02FourPartsKeepTheirOrder() {
        assertEquals(listOf("P0", "P1", "P2", "P3"), score(w3c41aMultiPartsPartorder).parts.map { it.id })
    }

    // F02, 51b: header fields and part names "can contain double quotes" and must be imported as written.
    @Test
    fun testF02HeaderFieldsKeepDoubleQuotes() {
        val score = score(w3c51bHeaderQuotes)
        val text = score.metadata.filterIsInstance<MetadataEntry.Text>().associate { it.kind to it.text }
        assertEquals("\"Quotes\" in header fields", text[MetadataKind.MOVEMENT_TITLE])
        assertEquals("Some \"Tester\" Name", text[MetadataKind.CREATOR])
        assertEquals("Free for anyone (\"Public Domain\")", text[MetadataKind.RIGHTS])
        assertEquals("\"Hand-crafted\" MusicXML file", text[MetadataKind.SOFTWARE])
        assertEquals("Staff \"Test\"", score.parts.single().name)
    }

    // F03, 01d: "c flat-and-a-half, d half-flat, e half-sharp, f sharp-and-a-half; once in the lower
    // and once in the upper region of the staff."
    @Test
    fun testF03MicrotonalAltersAreExact() {
        val pitches = notes(part(w3c01dPitchesMicrotones)).map { assertIs<NotePitch.Pitched>(it.pitch).written }
        val alters = listOf(Rational.of(-3, 2), Rational.of(-1, 2), Rational.of(1, 2), Rational.of(3, 2))
        assertEquals(listOf(Step.C, Step.D, Step.E, Step.F, Step.C, Step.D, Step.E, Step.F), pitches.map { it.step })
        assertEquals(alters + alters, pitches.map { it.alter })
        assertEquals(1, pitches[4].octave - pitches[0].octave)
    }

    // F04, 03c: divisions 1, then 8 mid-measure 1, then 38 mid-measure 2; the music is four quarters
    // then two halves.
    @Test
    fun testF04DivisionChangesKeepExactOnsets() {
        val measures = part(w3c03cRhythmDivisionChange).measures
        val first = measures[0].events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(0, 1, 2, 3).map(Rational::of), first.map { it.onset })
        assertTrue(first.all { it.duration == Rational.ONE })
        val second = measures[1].events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(Rational.ZERO, Rational.of(2)), second.map { it.onset })
        assertTrue(second.all { it.duration == Rational.of(2) })
    }

    // F05, 03b: "Voice 1 covers beats 1 and 2, and voice 2 covers beats 2 and 3. There is no rest or
    // note for uncovered beats."
    @Test
    fun testF05BackupToTheSecondBeat() {
        val part = part(w3c03bRhythmBackup)
        val notes = notes(part)
        assertEquals(listOf(Rational.ZERO, Rational.ONE), notes.filter { it.voice == "1" }.map { it.onset })
        assertEquals(listOf(Rational.ONE, Rational.of(2)), notes.filter { it.voice == "2" }.map { it.onset })
        assertTrue(part.measures.flatMap { it.events }.none { it is RestEvent })
    }

    // F05, 03f: "a quarter note on the second beat, a 16th note on the fourth beat, then filling up the
    // measure to have the correct length. There are no visible rests."
    @Test
    fun testF05ForwardPlacesNotesAndFillsTheMeasure() {
        val part = part(w3c03fRhythmForward)
        val first = part.measures[0]
        val notes = first.events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(Rational.ONE, Rational.of(3)), notes.map { it.onset })
        assertEquals(listOf(Rational.ONE, Rational.of(1, 4)), notes.map { it.duration })
        assertEquals(Rational.of(4), first.extent)
        assertTrue(part.measures.flatMap { it.events }.none { it is RestEvent })
    }

    // F06, 21c: "Some three-note chords, with various durations."
    @Test
    fun testF06ThreeNoteChordsShareOnsetAndDuration() {
        val part = part(w3c21cChordsThreeNotesDuration)
        val chords = notes(part).groupBy { it.chordId }
        assertTrue(chords.keys.none { it == null })
        assertEquals(7, chords.size)
        chords.values.forEach { chord ->
            assertEquals(3, chord.size)
            assertEquals(1, chord.map { it.onset to it.duration }.distinct().size)
        }
        val durations = chords.values.map { it.first().duration }
        assertEquals(listOf(Rational.of(3, 2), Rational.of(1, 2), Rational.ONE, Rational.ONE, Rational.ONE, Rational.ONE, Rational.of(2)), durations)
    }

    // F07, 46c: a clef change in the middle of a measure, via an implicit measure (second half of
    // measure 2) or an attributes element mid-measure (measure 3).
    @Test
    fun testF07MidMeasureClefChangeAppliesFromItsOffset() {
        val part = part(w3c46cMidmeasureClef)
        val states = part.effectiveStates()
        val treble = Attribute.Clef(ClefSign.G, 2)
        val cClef = Attribute.Clef(ClefSign.C, 2)
        assertEquals(listOf("1", "2", "X1", "3"), part.measures.map { it.number })
        assertTrue(part.measures[2].implicit)
        fun clefs(index: Int) = part.measures[index].events.filterIsInstance<NoteEvent>().map { states.getValue(it.id).clef }
        assertEquals(listOf(treble, treble), clefs(1))
        assertEquals(listOf(cClef, cClef), clefs(2))
        assertEquals(listOf(cClef, cClef, treble, treble), clefs(3))
    }

    // F07, 13e: "Mid-Measure Key-Change: 2 sharps, 2 flats, 0 sharps/flats, 7 sharps, no mode."
    @Test
    fun testF07MidMeasureKeyChangesApplyToEachNote() {
        val part = part(w3c13eKeySignaturesMidMeasureChange)
        val states = part.effectiveStates()
        assertEquals(listOf(2, -2, 0, 7), notes(part).map { states.getValue(it.id).key?.fifths })
        assertEquals(listOf(0, 1, 2, 3).map(Rational::of), notes(part).map { it.onset })
    }

    // F08, 43b: "A piano staff with different keys and clefs for each of its staves."
    @Test
    fun testF08EachStaffKeepsItsOwnKeyAndClef() {
        val part = part(w3c43bMultiStaffDifferentKeys)
        val states = part.effectiveStates()
        assertEquals(2, part.staves.size)
        val byStaff = notes(part).associateBy { it.staff }
        assertEquals(Rational.ZERO, byStaff.getValue(2).onset)
        assertEquals(0, states.getValue(byStaff.getValue(1).id).key?.fifths)
        assertEquals(Attribute.Clef(ClefSign.G, 2), states.getValue(byStaff.getValue(1).id).clef)
        assertEquals(2, states.getValue(byStaff.getValue(2).id).key?.fifths)
        assertEquals(Attribute.Clef(ClefSign.F, 4), states.getValue(byStaff.getValue(2).id).clef)
    }

    private fun score(xml: String): Score =
        assertIs<ParseResult.Success<Score>>(parseMusicXml(xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))).value

    private fun part(xml: String): Part = score(xml).parts.single()

    private fun notes(part: Part): List<NoteEvent> = part.measures.flatMap { it.events }.filterIsInstance<NoteEvent>()

    private companion object {
        /** Files without `divisions`: 1 is recovered (MusicXML 4.1's default), which strict parsing rejects (MX-24). */
        val RECOVERED_STRICT_FAILURES = setOf("51b-Header-Quotes.musicxml")
    }
}
