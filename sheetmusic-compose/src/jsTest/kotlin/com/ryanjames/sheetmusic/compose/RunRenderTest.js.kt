package com.ryanjames.sheetmusic.compose

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.test.TestResult
import org.jetbrains.skiko.wasm.onWasmReady
import kotlin.js.Promise

/** On Kotlin/JS, Skia's WebAssembly module loads asynchronously and must be ready before a scene is created. */
@OptIn(ExperimentalTestApi::class)
internal actual fun runRenderTest(block: suspend ComposeUiTest.() -> Unit): TestResult =
    Promise<Unit> { resolve, reject ->
        onWasmReady {
            runComposeUiTest(block = block).unsafeCast<Promise<Unit>>().then({ resolve(it) }, { reject(it) })
        }
    }.unsafeCast<TestResult>()
