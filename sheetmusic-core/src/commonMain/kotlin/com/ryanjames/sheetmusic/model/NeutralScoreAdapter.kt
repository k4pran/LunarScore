package com.ryanjames.sheetmusic.model

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.NotatedValue
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Staff
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.Measure as NeutralMeasure
import com.ryanjames.sheetmusic.score.Score as NeutralScore

/**
 * Lift the existing renderer model into the neutral score vocabulary without changing layout APIs.
 * All current pitches, durations, measure order and the treble clef are preserved. No meter or
 * sounding pitch is inferred. The richer neutral model is not restricted to the renderer subset.
 */
public fun Score.toNeutralScore(): NeutralScore = NeutralScore(
    parts = listOf(
        Part(
            id = "P1",
            staves = listOf(Staff(1)),
            measures = measures.mapIndexed { measureIndex, measure ->
                val measureId = "M${measureIndex + 1}"
                var onset = Rational.ZERO
                val clefEvent = when (clef) {
                    Clef.TREBLE -> AttributeEvent("clef-$measureId", Rational.ZERO, Attribute.Clef(ClefSign.G, 2))
                }
                val notes = measure.notes.mapIndexed { noteIndex, note ->
                    val (value, duration) = when (note.duration) {
                        Duration.WHOLE -> NoteValue.WHOLE to Rational.of(4)
                        Duration.HALF -> NoteValue.HALF to Rational.of(2)
                        Duration.QUARTER -> NoteValue.QUARTER to Rational.ONE
                    }
                    NoteEvent(
                        id = "$measureId-N${noteIndex + 1}",
                        onset = onset,
                        duration = duration,
                        pitch = NotePitch.Pitched(WrittenPitch(Step.valueOf(note.pitch.name), note.octave)),
                        notation = NotatedValue(value),
                    ).also { onset += duration }
                }
                NeutralMeasure(measureId, (measureIndex + 1).toString(), onset, listOf(clefEvent) + notes)
            },
        ),
    ),
)
