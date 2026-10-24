package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.InterchangeableTime
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.MetadataKind
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.SenzaMisura
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.TimeFraction
import com.ryanjames.sheetmusic.score.TimeRelation
import com.ryanjames.sheetmusic.score.TimeSymbol
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
            assertIs<ParseResult.Success<Score>>(strict, "${fixture.file}: ${strict.diagnostics}")
            assertTrue(strict.diagnostics.isEmpty(), "${fixture.file}: ${strict.diagnostics}")
        }
    }

    // F04, 03e: "No <divisions> element. The generally agreed default value is 1, and MusicXML version
    // 4.1 will mention this explicitly. This test holds a single whole note."
    @Test
    fun testF04MissingDivisionsDefaultToOne() {
        val note = notes(part(w3c03eRhythmNoDivisions)).single()
        assertEquals(NoteValue.WHOLE, note.notation.value)
        assertEquals(Rational.of(4), note.duration)
    }

    // F02, 41h: "This piece has two more <part> elements than the <part-list> section contains. One can
    // either convert all the parts present but not listed in the part list, or simply not import or
    // ignore them." Tolerant parsing ignores them with a diagnostic each; strict parsing rejects the loss.
    @Test
    fun testF02PartsMissingFromThePartListAreDiscardedWhenTolerant() {
        val tolerant = parseMusicXml(w3c41hTooManyParts.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))
        val score = assertIs<ParseResult.Success<Score>>(tolerant, tolerant.diagnostics.toString()).value
        assertEquals(listOf("P1"), score.parts.map { it.id })
        assertEquals(Rational.of(4), score.parts.single().measures.single().extent)
        val discarded = tolerant.diagnostics.filter { it.unsupported?.featureId == "S01" }
        assertEquals(listOf("P3", "P4"), discarded.map { it.location.partId })
        assertTrue(discarded.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        val strict = assertIs<ParseResult.Failure>(parseMusicXml(w3c41hTooManyParts.encodeToByteArray(), ParseOptions(ParsePolicy.STRICT)))
        assertTrue(strict.diagnostics.filter { it.code == DiagnosticCode.UNSUPPORTED_FEATURE }.map { it.location.partId }.containsAll(listOf("P3", "P4")))
    }

    // F02, 41g (.invalid): "A part with no id attribute. Since this piece has only one part, it is clear
    // which part is described by the one part element." Tolerant parsing binds it with a warning.
    @Test
    fun testF02PartWithoutIdBindsToTheOnlyScorePartWhenTolerant() {
        val tolerant = parseMusicXml(w3c41gPartNoId.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))
        val score = assertIs<ParseResult.Success<Score>>(tolerant, tolerant.diagnostics.toString()).value
        assertEquals("P1", score.parts.single().id)
        assertEquals(Rational.of(4), assertIs<RestEvent>(score.parts.single().measures.single().events.single()).duration)
        val recovered = tolerant.diagnostics.single { it.code == DiagnosticCode.RECOVERED_VALUE }
        assertEquals(DiagnosticSeverity.WARNING to "P1", recovered.severity to recovered.location.partId)
        val strict = assertIs<ParseResult.Failure>(parseMusicXml(w3c41gPartNoId.encodeToByteArray(), ParseOptions(ParsePolicy.STRICT)))
        assertTrue(strict.diagnostics.any { it.code == DiagnosticCode.RECOVERED_VALUE && it.severity == DiagnosticSeverity.ERROR })
    }

    @Test
    fun testStructureRecoveryFixturesStayDiagnosed() {
        w3cStructureRecoveryFixtures.forEach { fixture ->
            val tolerant = parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT), fixture.file)
            val score = assertIs<ParseResult.Success<Score>>(tolerant, "${fixture.file}: ${tolerant.diagnostics}").value
            assertTrue(score.validate().isEmpty(), fixture.file)
            assertTrue(tolerant.diagnostics.isNotEmpty(), fixture.file)
            assertIs<ParseResult.Failure>(parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.STRICT), fixture.file))
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

    // F07, 11c (both spellings): "Complex time signatures with the same denominator: (3+2)/8 and (5+3+1)/4."
    @Test
    fun testF07AdditiveNumeratorsKeepTheirComponents() {
        listOf(w3c11cTimeSignaturesComplex, w3c11cTimeSignaturesCompoundSimple).forEach { xml ->
            val meters = meters(part(xml))
            assertEquals(listOf(time(listOf(3, 2) to 8), time(listOf(5, 3, 1) to 4)), meters)
            assertEquals(listOf(Rational.of(5, 2), Rational.of(9)), meters.map { it.length })
        }
    }

    // F07, 11d (both spellings): "Complex time signatures with separate fractions displayed:
    // 3/8+2/8+3/4 and 5/2+1/8."
    @Test
    fun testF07MultipleFractionsSumToTheMeasureLength() {
        listOf(w3c11dTimeSignaturesComplexMultiple, w3c11dTimeSignaturesCompoundMultiple).forEach { xml ->
            val meters = meters(part(xml))
            assertEquals(
                listOf(time(listOf(3) to 8, listOf(2) to 8, listOf(3) to 4), time(listOf(5) to 2, listOf(1) to 8)),
                meters,
            )
            assertEquals(listOf(Rational.of(11, 2), Rational.of(21, 2)), meters.map { it.length })
        }
    }

    // F07, 11e (both spellings): "Complex time signatures of mixed type: (3+2)/8+3/4."
    @Test
    fun testF07MixedFractionsKeepAdditiveAndSimpleParts() {
        listOf(w3c11eTimeSignaturesComplexMixed, w3c11eTimeSignaturesCompoundMixed).forEach { xml ->
            val meter = meters(part(xml)).single()
            assertEquals(time(listOf(3, 2) to 8, listOf(3) to 4), meter)
            assertEquals(Rational.of(11, 2), meter.length)
        }
    }

    // The measures of 11c-11e and 11i are filled to their meter's total length.
    @Test
    fun testF07MeasuresFillTheirMeterLength() {
        listOf(
            w3c11cTimeSignaturesComplex, w3c11cTimeSignaturesCompoundSimple, w3c11dTimeSignaturesComplexMultiple,
            w3c11dTimeSignaturesCompoundMultiple, w3c11eTimeSignaturesComplexMixed, w3c11eTimeSignaturesCompoundMixed,
            w3c11iTimeSignaturesAlternate,
        ).forEach { xml ->
            val part = part(xml)
            val states = part.effectiveStates()
            part.measures.forEach { measure ->
                val meter = states.getValue(notes(part).first { it in measure.events }.id).time
                assertEquals(meter?.length, measure.extent, measure.number)
            }
        }
    }

    // F07, 11f: "a 3/8 time signature with symbol="cut" and a complex 1/8+2/4 time signature with
    // symbol="single-number"." The symbol is display only and never changes the meter.
    @Test
    fun testF07SymbolOnAComplexMeterIsDisplayOnly() {
        val meters = meters(part(w3c11fTimeSignaturesSymbolMeaning))
        assertEquals(
            listOf(
                Attribute.TimeSignature(3, 8, TimeSymbol.CUT),
                time(listOf(1) to 8, listOf(2) to 4).copy(symbol = TimeSymbol.SINGLE_NUMBER),
            ),
            meters,
        )
        assertEquals(listOf(Rational.of(3, 2), Rational.of(5, 2)), meters.map { it.length })
    }

    // F07, 11g: "Two time signatures (3/8 and 3+2/8) having the attribute symbol="single-number"."
    @Test
    fun testF07SingleNumberSymbolOnAnAdditiveMeter() {
        assertEquals(
            listOf(
                Attribute.TimeSignature(3, 8, TimeSymbol.SINGLE_NUMBER),
                time(listOf(3, 2) to 8).copy(symbol = TimeSymbol.SINGLE_NUMBER),
            ),
            meters(part(w3c11gTimeSignaturesSingleNumber)),
        )
    }

    // F07, 11h: "The first bar starts with an empty <senza-misura> element ... The third bar starts
    // with a <senza-misura> element containing value X." Bars 2 and 4 keep the previous state.
    @Test
    fun testF07SenzaMisuraHasNoMeterLength() {
        val part = part(w3c11hTimeSignaturesSenzaMisura)
        val states = part.effectiveStates()
        val unmarked = Attribute.TimeSignature(emptyList(), senzaMisura = SenzaMisura())
        val marked = Attribute.TimeSignature(emptyList(), senzaMisura = SenzaMisura("X"))
        val times = part.measures.map { measure -> states.getValue(measure.events.last().id).time }
        assertEquals(listOf(unmarked, unmarked, marked, marked), times)
        assertTrue(times.all { it?.length == null })
        assertEquals(listOf(Rational.of(3, 2), Rational.of(3), Rational.of(2), Rational.of(5, 2)), part.measures.map { it.extent })
    }

    // F07, 11i: "Time signatures with alternates using <interchangeable>: 3/4 (6/8), 2/2 [4/4],
    // 4/4 = 7/8+1/8, 3/4 - 6/8, 2/2 / 4/4, 3/4 6/8."
    @Test
    fun testF07InterchangeableAlternatesKeepTheirRelation() {
        fun alternate(main: Attribute.TimeSignature, relation: TimeRelation, vararg fractions: Pair<List<Int>, Int>) =
            main.copy(interchangeable = InterchangeableTime(fractions.map { (beats, type) -> TimeFraction(beats, type) }, relation))
        val threeFour = Attribute.TimeSignature(3, 4)
        val twoTwo = Attribute.TimeSignature(2, 2)
        assertEquals(
            listOf(
                alternate(threeFour, TimeRelation.PARENTHESES, listOf(6) to 8),
                alternate(twoTwo, TimeRelation.BRACKET, listOf(4) to 4),
                alternate(Attribute.TimeSignature(4, 4), TimeRelation.EQUALS, listOf(7) to 8, listOf(1) to 8),
                alternate(threeFour, TimeRelation.HYPHEN, listOf(6) to 8),
                alternate(twoTwo, TimeRelation.SLASH, listOf(4) to 4),
                alternate(threeFour, TimeRelation.SPACE, listOf(6) to 8),
            ),
            meters(part(w3c11iTimeSignaturesAlternate)),
        )
    }

    private fun score(xml: String): Score =
        assertIs<ParseResult.Success<Score>>(parseMusicXml(xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))).value

    private fun part(xml: String): Part = score(xml).parts.single()

    /** The time signatures in source order, one per attribute change. */
    private fun meters(part: Part): List<Attribute.TimeSignature> =
        part.measures.flatMap { it.events }.filterIsInstance<AttributeEvent>().mapNotNull { it.attribute as? Attribute.TimeSignature }

    private fun time(vararg fractions: Pair<List<Int>, Int>): Attribute.TimeSignature =
        Attribute.TimeSignature(fractions.map { (beats, type) -> TimeFraction(beats, type) })

    private fun notes(part: Part): List<NoteEvent> = part.measures.flatMap { it.events }.filterIsInstance<NoteEvent>()
}
