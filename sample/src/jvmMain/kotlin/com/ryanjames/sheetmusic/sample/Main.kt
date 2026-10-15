package com.ryanjames.sheetmusic.sample

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sheet music sample",
        state = rememberWindowState(size = DpSize(900.dp, 520.dp)),
    ) {
        SampleScreen()
    }
}
