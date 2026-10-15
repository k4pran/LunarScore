package com.ryanjames.sheetmusic.compose

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlinx.coroutines.test.TestResult

/** Runs a Compose UI test once the platform's Skia renderer is ready. */
@OptIn(ExperimentalTestApi::class)
internal expect fun runRenderTest(block: suspend ComposeUiTest.() -> Unit): TestResult
