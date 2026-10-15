package com.ryanjames.sheetmusic.model

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NeutralScoreAdapterTest {
    @Test
    fun testEveryLegacyPitchAndDurationMigrates() {
        val notes = Pitch.entries.flatMap { pitch -> Duration.entries.map { Note(pitch, 4, it) } }
        val original = Score(listOf(Measure(notes), Measure(listOf(Note(Pitch.C, 5, Duration.QUARTER)))))
        val neutral = original.toNeutralScore()
        assertTrue(neutral.validate().isEmpty())
        val measures = neutral.parts.single().measures
        assertEquals(Rational.of(49), measures[0].extent)
        assertEquals(Rational.ONE, measures[1].extent)
        assertEquals(Attribute.Clef(ClefSign.G, 2), assertIs<AttributeEvent>(measures[0].events.first()).attribute)
        val migrated = measures[0].events.filterIsInstance<NoteEvent>()
        notes.zip(migrated).forEach { (source, target) ->
            assertEquals(WrittenPitch(Step.valueOf(source.pitch.name), source.octave), assertIs<NotePitch.Pitched>(target.pitch).written)
            assertNull(assertIs<NotePitch.Pitched>(target.pitch).sounding)
        }
        assertEquals(listOf(Rational.ZERO, Rational.of(4), Rational.of(6)), migrated.take(3).map { it.onset })
        assertEquals(listOf(NoteValue.WHOLE, NoteValue.HALF, NoteValue.QUARTER), migrated.take(3).map { it.notation.value })
        assertEquals(Rational.ZERO, measures[1].events.filterIsInstance<NoteEvent>().single().onset)
    }
}
