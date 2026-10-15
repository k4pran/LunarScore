package com.ryanjames.sheetmusic.layout

import com.ryanjames.sheetmusic.model.Duration
import com.ryanjames.sheetmusic.model.Measure
import com.ryanjames.sheetmusic.model.Note
import com.ryanjames.sheetmusic.model.Pitch
import com.ryanjames.sheetmusic.model.Score
import kotlin.math.abs
import kotlin.test.assertTrue

internal fun quarter(pitch: Pitch, octave: Int) = Note(pitch, octave, Duration.QUARTER)

/** The milestone example: an ascending C major scale from C4 to C5. */
internal val scaleScore = Score(
    measures = listOf(
        Measure(listOf(quarter(Pitch.C, 4), quarter(Pitch.D, 4), quarter(Pitch.E, 4), quarter(Pitch.F, 4))),
        Measure(listOf(quarter(Pitch.G, 4), quarter(Pitch.A, 4), quarter(Pitch.B, 4), quarter(Pitch.C, 5))),
    ),
)

internal const val EPS = 1e-4f

internal fun assertNear(expected: Float, actual: Float, message: String? = null) =
    assertTrue(abs(expected - actual) < EPS, "${message ?: ""} expected <$expected> but was <$actual>")
