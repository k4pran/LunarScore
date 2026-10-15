package com.ryanjames.sheetmusic.compose

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

/** Writes the rendered example to `build/render` so it can be inspected by eye. */
@OptIn(ExperimentalTestApi::class)
class RenderSnapshotTest {
    @Test
    fun writeSnapshots() = runComposeUiTest {
        val sizes = intArrayOf(10, 20)
        showExampleScore(*sizes)
        File("build/render").mkdirs()
        sizes.forEachIndexed { i, ss ->
            ImageIO.write(captureDrawnScore(i).toAwtImage(), "png", File("build/render/scale-$ss.png"))
        }
    }
}
