package com.ryanjames.sheetmusic.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

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

    @Test
    fun testTimeSignatureLengthSumsEveryFraction() {
        val simple = Attribute.TimeSignature(3, 4)
        assertEquals(Attribute.TimeSignature(listOf(TimeFraction(listOf(3), 4))), simple)
        assertEquals(Rational.of(3), simple.length)
        val mixed = Attribute.TimeSignature(listOf(TimeFraction(listOf(3, 2), 8), TimeFraction(listOf(3), 4)))
        assertEquals(Rational.of(11, 2), mixed.length)
        val alternate = simple.copy(interchangeable = InterchangeableTime(listOf(TimeFraction(listOf(6), 8)), TimeRelation.PARENTHESES))
        assertEquals(Rational.of(3), alternate.length)
        assertNull(Attribute.TimeSignature(emptyList(), senzaMisura = SenzaMisura()).length)
    }

    @Test
    fun testTimeSignatureInvariants() {
        assertFailsWith<IllegalArgumentException> { Attribute.TimeSignature(0, 4) }
        assertFailsWith<IllegalArgumentException> { TimeFraction(emptyList(), 4) }
        assertFailsWith<IllegalArgumentException> { TimeFraction(listOf(3, 0), 8) }
        assertFailsWith<IllegalArgumentException> { Attribute.TimeSignature(emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            Attribute.TimeSignature(listOf(TimeFraction(listOf(3), 4)), senzaMisura = SenzaMisura())
        }
        assertFailsWith<IllegalArgumentException> { SenzaMisura(" ") }
        assertFailsWith<IllegalArgumentException> { InterchangeableTime(emptyList()) }
        assertFailsWith<RationalException> { Attribute.TimeSignature(listOf(TimeFraction(listOf(Int.MAX_VALUE, Int.MAX_VALUE), 1))) }
    }

    private fun note(id: String, onset: Rational, staff: Int = 1) =
        NoteEvent(id, onset, Rational.ONE, NotePitch.Pitched(WrittenPitch(Step.C, 4)), staff = staff)
}
