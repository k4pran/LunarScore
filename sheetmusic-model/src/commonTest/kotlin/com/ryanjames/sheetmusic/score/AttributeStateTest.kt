package com.ryanjames.sheetmusic.score

import kotlin.test.Test
import kotlin.test.assertEquals

class AttributeStateTest {
    private val treble = Attribute.Clef(ClefSign.G, 2)
    private val bass = Attribute.Clef(ClefSign.F, 4)

    @Test
    fun testStatesFollowOnsetsPersistAcrossMeasuresAndStayPerStaff() {
        val part = Part(
            id = "P1",
            staves = listOf(Staff(1), Staff(2)),
            measures = listOf(
                Measure("m1", "1", Rational.of(2), listOf(
                    // Listed out of onset order: the bass clef at 1 must not affect the note at 0.
                    AttributeEvent("bass", Rational.ONE, bass),
                    AttributeEvent("treble", Rational.ZERO, treble),
                    note("first", Rational.ZERO),
                    note("second", Rational.ONE),
                    AttributeEvent("key-2", Rational.ZERO, Attribute.KeySignature(2), staff = 2),
                    note("lower", Rational.ZERO, staff = 2),
                )),
                Measure("m2", "2", Rational.ONE, listOf(
                    note("next", Rational.ZERO),
                    AttributeEvent("lines", Rational.ZERO, Attribute.StaffLines(1)),
                    note("same-onset", Rational.ZERO),
                )),
            ),
        )
        val states = part.effectiveStates()
        assertEquals(AttributeState(clef = treble), states["first"])
        assertEquals(AttributeState(clef = treble), states["treble"])
        assertEquals(AttributeState(clef = bass), states["second"])
        assertEquals(AttributeState(key = Attribute.KeySignature(2)), states["lower"])
        assertEquals(AttributeState(clef = bass), states["next"])
        assertEquals(AttributeState(clef = bass, staffLines = Attribute.StaffLines(1)), states["same-onset"])
        assertEquals(part.measures.sumOf { it.events.size }, states.size)
    }

    @Test
    fun testWithReplacesOnlyItsDomain() {
        val state = AttributeState(time = Attribute.TimeSignature(3, 4), clef = treble)
            .with(Attribute.KeySignature(-1, KeyMode.MINOR))
            .with(Attribute.Transpose(Transposition(Rational.of(-2), -1)))
            .with(Attribute.InstrumentChange("clarinet"))
            .with(bass)
        assertEquals(
            AttributeState(Attribute.TimeSignature(3, 4), Attribute.KeySignature(-1, KeyMode.MINOR), bass, null,
                Attribute.Transpose(Transposition(Rational.of(-2), -1)), Attribute.InstrumentChange("clarinet")),
            state,
        )
    }

    private fun note(id: String, onset: Rational, staff: Int = 1) =
        NoteEvent(id, onset, Rational.ONE, NotePitch.Pitched(WrittenPitch(Step.C, 4)), staff = staff)
}
