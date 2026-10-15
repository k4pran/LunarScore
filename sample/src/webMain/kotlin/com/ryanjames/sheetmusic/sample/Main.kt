package com.ryanjames.sheetmusic.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

/** Browser entry point, shared by the Kotlin/JS and Kotlin/Wasm builds. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        SampleScreen()
    }
}
