package com.ryanjames.sheetmusic.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Original model-only expectations from test-plan.md; no XML parser or schema claim. */
class ScoreProjectionTest {
    @Test
    fun testF01QuarterNoteAndRestInFourFour() {
        val score = score(
            Measure("m1", "1", Rational.of(2), listOf(
                AttributeEvent("meter", Rational.ZERO, Attribute.TimeSignature(4, 4)),
                note("c4", 0),
                RestEvent("rest", Rational.ONE, Rational.ONE, notation = NotatedValue(NoteValue.QUARTER)),
            )),
        )
        assertEquals(emptyList(), score.validate())
        val events = score.parts.single().measures.single().events
        assertEquals(Attribute.TimeSignature(4, 4), assertIs<AttributeEvent>(events[0]).attribute)
        assertEquals(WrittenPitch(Step.C, 4), assertIs<NotePitch.Pitched>(assertIs<NoteEvent>(events[1]).pitch).written)
        assertEquals(Rational.ONE, assertIs<RestEvent>(events[2]).onset)
        assertEquals(Rational.ONE, assertIs<RestEvent>(events[2]).duration)
    }

    @Test
    fun testF05OverlappingVoicesAndForwardGap() {
        // Voice 1 occupies [0,2); backup to 0 for voice 2, then forward leaves [1,3) empty.
        val measure = Measure("m1", "1", Rational.of(4), listOf(
            note("v1", 0, duration = Rational.of(2), notation = NotatedValue(NoteValue.HALF)),
            note("v2a", 0, voice = "2"),
            note("v2b", 3, voice = "2"),
        ))
        val score = score(measure)
        assertTrue(score.validate().isEmpty())
        val events = measure.events.map { assertIs<NoteEvent>(it) }
        assertEquals(listOf(Rational.ZERO, Rational.ZERO, Rational.of(3)), events.map { it.onset })
        assertEquals(listOf("1", "2", "2"), events.map { it.voice })
        assertEquals(Rational.of(2), events[0].duration)
        assertEquals(Rational.of(4), measure.extent)
    }

    @Test
    fun testF10IndependentCrossMeasureSpansAndOrphans() {
        val score = score(
            Measure("m1", "1", Rational.of(4), listOf(note("start", 3))),
            Measure("m2", "2", Rational.ONE, listOf(note("stop", 0))),
        ).copy(relationships = listOf(
            Relationship("sound", RelationshipKind.SOUND_TIE, "start", "stop"),
            Relationship("printed", RelationshipKind.NOTATED_TIE, "start", "stop"),
            Relationship("slur", RelationshipKind.SLUR, "start", "stop"),
            Relationship("notation-only", RelationshipKind.NOTATED_TIE, "stop", null),
            Relationship("orphan", RelationshipKind.SLUR, "unknown", "stop"),
        ))
        assertEquals(listOf(
            ModelDiagnostic(ModelDiagnosticCode.MISSING_ENDPOINT, "relationships[3].endEventId", null),
            ModelDiagnostic(ModelDiagnosticCode.UNKNOWN_EVENT, "relationships[4].startEventId", "unknown"),
        ), score.validate())
        assertNotEquals(score.relationships[0].kind, score.relationships[1].kind)
        // Voice/source encounter order may put a later musical stop before its start.
        val stopBeforeStart = score(Measure("m", "1", Rational.of(3), listOf(note("stop", 2), note("start", 0))))
            .copy(relationships = listOf(Relationship("slur", RelationshipKind.SLUR, "start", "stop")))
        assertTrue(stopBeforeStart.validate().isEmpty())
        assertEquals(listOf("stop", "start"), stopBeforeStart.parts[0].measures[0].events.map { it.id })
    }

    @Test
    fun testF17WrittenSoundingAndPercussionStayIndependent() {
        val written = WrittenPitch(Step.C, 4, Rational.of(1, 2))
        val sounding = SoundingPitch(Rational.of(117, 2)) // C half-sharp, transposed down two semitones.
        val part = Part("winds", listOf(Staff(1), Staff(2)), instruments = listOf(
            Instrument("clarinet", "B-flat clarinet"), Instrument("drum", "Bass drum", 35),
        ), measures = listOf(Measure("m1", "1", Rational.ONE, listOf(
            AttributeEvent("transpose", Rational.ZERO, Attribute.Transpose(Transposition(Rational.of(-2), -1))),
            AttributeEvent("instrument", Rational.ZERO, Attribute.InstrumentChange("clarinet")),
            note("pitched", 0).copy(
                pitch = NotePitch.Pitched(written, sounding), instrumentId = "clarinet",
                notation = NotatedValue(NoteValue.QUARTER, accidental = Accidental("natural", cautionary = true)),
            ),
            note("percussion", 0).copy(pitch = NotePitch.Unpitched(Step.F, 4), instrumentId = "drum", staff = 2),
        ))))
        val score = Score(listOf(part))
        assertTrue(score.validate().isEmpty())
        val pitched = assertIs<NotePitch.Pitched>(assertIs<NoteEvent>(part.measures[0].events[2]).pitch)
        assertEquals(Rational.of(1, 2), pitched.written.alter)
        assertEquals(Rational.of(117, 2), pitched.sounding?.semitones)
        val percussion = assertIs<NoteEvent>(part.measures[0].events[3])
        assertIs<NotePitch.Unpitched>(percussion.pitch)
        assertEquals(2, percussion.staff)
        assertNull(assertIs<NotePitch.Pitched>(note("unknown-sounding", 0).pitch).sounding)
    }

    @Test
    fun testChordTonesGraceAndNotationDoNotForcePerformedTime() {
        val modification = TimeModification(3, 2, NoteValue.EIGHTH)
        val first = note("n1", 0, Rational.of(2, 3), notation = NotatedValue(NoteValue.QUARTER, 1, modification))
            .copy(chordId = "chord", performance = Performance(Rational.of(-1, 12), Rational.of(1, 12)))
        val second = first.copy(id = "n2", duration = Rational.of(1, 3), staff = 2)
        val grace = first.copy(id = "grace", duration = null, role = NoteRole.GRACE, chordId = null)
        val score = Score(listOf(Part("p", listOf(Staff(1), Staff(2)), listOf(
            Measure("pickup", "0a", Rational.of(2, 3), listOf(grace, first, second), implicit = true),
        ))))
        assertTrue(score.validate().isEmpty())
        assertEquals(first.onset, second.onset)
        assertNotEquals(first.duration, second.duration)
        assertEquals(NoteValue.QUARTER, first.notation.value)
        assertEquals(Rational.of(-1, 12), first.performance.attack)
        assertNull(grace.duration)
    }

    @Test
    fun testEmptyStructuresAndMultiplePartsAreLegal() {
        assertTrue(Score().validate().isEmpty())
        val emptyMeasure = Measure("empty", "X", Rational.ZERO)
        val score = Score(listOf(
            Part("p1", listOf(Staff(1)), listOf(emptyMeasure)),
            Part("p2", listOf(Staff(1)), listOf(emptyMeasure)),
        ))
        assertEquals(listOf("p1", "p2"), score.parts.map { it.id })
        assertTrue(score.validate().isEmpty())
    }

    @Test
    fun testInvalidGraphHasStructuredFindingsWithoutRepair() {
        val score = Score(listOf(Part("p", listOf(Staff(1), Staff(1)), listOf(
            Measure("m1", "1", Rational.ONE, listOf(
                note("same", 0).copy(staff = 2, instrumentId = "missing"),
                note("same", 0),
                AttributeEvent("change", Rational.ZERO, Attribute.InstrumentChange("missing")),
            )),
        ))))
        assertEquals(listOf(
            ModelDiagnosticCode.DUPLICATE_ID, ModelDiagnosticCode.UNKNOWN_STAFF,
            ModelDiagnosticCode.UNKNOWN_INSTRUMENT, ModelDiagnosticCode.DUPLICATE_ID,
            ModelDiagnosticCode.UNKNOWN_INSTRUMENT,
        ), score.validate().map { it.code })
        assertEquals(3, score.parts.single().measures.single().events.size)
    }

    @Test
    fun testInvalidLocalTimeAndNotationAreRejected() {
        assertFailsWith<IllegalArgumentException> { note("n", -1) }
        assertFailsWith<IllegalArgumentException> { note("n", 0, Rational.ZERO) }
        assertFailsWith<IllegalArgumentException> { note("n", 0).copy(role = NoteRole.GRACE) }
        assertFailsWith<IllegalArgumentException> { RestEvent("r", Rational.ZERO, Rational.of(-1)) }
        assertFailsWith<IllegalArgumentException> { TimeModification(0, 2) }
        assertFailsWith<IllegalArgumentException> { NotatedValue(dots = -1) }
        assertFailsWith<IllegalArgumentException> { Beam(0, BeamType.BEGIN) }
        assertFailsWith<IllegalArgumentException> { Beam(9, BeamType.END) }
        assertFailsWith<IllegalArgumentException> {
            NotatedValue(beams = listOf(Beam(1, BeamType.BEGIN), Beam(1, BeamType.END)))
        }
    }

    @Test
    fun testDirectionsKeepSoundAndDisplaySeparately() {
        val crescendo = DirectionEvent("d1", Rational.ZERO, listOf(DirectionMark.Wedge(WedgeType.CRESCENDO)))
        val stop = DirectionEvent(
            "d2",
            Rational.of(2),
            listOf(DirectionMark.Wedge(WedgeType.STOP), Dynamics(listOf("f"))),
            sound = DirectionSound(dynamics = Rational.of(106)),
            display = DirectionDisplay(Placement.BELOW, offset = Rational.of(-1, 2)),
        )
        val tempoOnly = DirectionEvent("d3", Rational.ZERO, emptyList(), sound = DirectionSound(tempo = Rational.of(120)))
        val score = score(Measure("m1", "1", Rational.of(2), listOf(crescendo, stop, tempoOnly)))
            .copy(relationships = listOf(Relationship("w1", RelationshipKind.WEDGE, "d1", "d2")))
        assertTrue(score.validate().isEmpty())
        assertEquals(Rational.ZERO, stop.sound?.offset)
        assertFailsWith<IllegalArgumentException> { DirectionEvent("d", Rational.ZERO, emptyList()) }
        assertFailsWith<IllegalArgumentException> { DirectionEvent("d", Rational.ZERO, listOf(DirectionMark.Words("a")), voice = " ") }
        assertFailsWith<IllegalArgumentException> { Dynamics(emptyList()) }
        assertFailsWith<IllegalArgumentException> { DirectionSound(tempo = Rational.of(-1)) }
        assertFailsWith<IllegalArgumentException> { DirectionMark.Metronome(NoteValue.QUARTER, "60", beatUnitDots = -1) }
    }

    @Test
    fun testLyricsJoinElidedSyllablesAndAllowMelismaContinuations() {
        val elided = Lyric(
            listOf(
                LyricSyllable("che", Syllabic.SINGLE),
                LyricSyllable("a", Syllabic.SINGLE, elision = "‿"),
            ),
            verse = "1",
        )
        assertEquals("che‿a", elided.text)
        val continuation = Lyric(emptyList(), verse = "1", extend = LyricExtend(ExtendType.CONTINUE))
        assertEquals("", continuation.text)
        val score = score(Measure("m1", "1", Rational.of(2), listOf(
            note("n1", 0, notation = NotatedValue(NoteValue.QUARTER, lyrics = listOf(elided))),
            note("n2", 1, notation = NotatedValue(NoteValue.QUARTER, lyrics = listOf(continuation))),
        )))
        assertTrue(score.validate().isEmpty())
        assertFailsWith<IllegalArgumentException> { Lyric(emptyList()) }
        assertFailsWith<IllegalArgumentException> { Lyric(listOf(LyricSyllable("a")), verse = " ") }
        assertFailsWith<IllegalArgumentException> { Lyric(listOf(LyricSyllable("a", elision = "_"))) }
        assertFailsWith<IllegalArgumentException> { Lyric(listOf(LyricSyllable("a"), LyricSyllable("b"))) }
    }

    private fun score(vararg measures: Measure): Score = Score(listOf(Part("P1", listOf(Staff(1)), measures.toList())))

    private fun note(
        id: String,
        onset: Int,
        duration: Rational = Rational.ONE,
        voice: String = "1",
        notation: NotatedValue = NotatedValue(NoteValue.QUARTER),
    ): NoteEvent = NoteEvent(id, Rational.of(onset), duration, NotePitch.Pitched(WrittenPitch(Step.C, 4)), voice = voice, notation = notation)
}
