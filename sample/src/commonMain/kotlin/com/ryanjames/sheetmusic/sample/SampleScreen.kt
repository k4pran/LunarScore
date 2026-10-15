package com.ryanjames.sheetmusic.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ryanjames.sheetmusic.compose.SheetMusic
import com.ryanjames.sheetmusic.model.Duration
import com.ryanjames.sheetmusic.model.Measure
import com.ryanjames.sheetmusic.model.Note
import com.ryanjames.sheetmusic.model.Pitch
import com.ryanjames.sheetmusic.model.Score

private val cMajorScale = Score(
    measures = listOf(
        Measure(
            notes = listOf(
                Note(Pitch.C, 4, Duration.QUARTER),
                Note(Pitch.D, 4, Duration.QUARTER),
                Note(Pitch.E, 4, Duration.QUARTER),
                Note(Pitch.F, 4, Duration.QUARTER),
            ),
        ),
        Measure(
            notes = listOf(
                Note(Pitch.G, 4, Duration.QUARTER),
                Note(Pitch.A, 4, Duration.QUARTER),
                Note(Pitch.B, 4, Duration.QUARTER),
                Note(Pitch.C, 5, Duration.QUARTER),
            ),
        ),
    ),
)

/** The sample UI, shared by the desktop and browser entry points. */
@Composable
fun SampleScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BasicText("Full width, default staff space (10 dp)")
        SheetMusic(score = cMajorScale, modifier = Modifier.fillMaxWidth())

        BasicText("Natural width, staff space 16 dp")
        SheetMusic(score = cMajorScale, staffSpace = 16.dp)
    }
}
