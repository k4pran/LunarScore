package com.ryanjames.sheetmusic.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.ryanjames.sheetmusic.layout.layoutScore
import com.ryanjames.sheetmusic.model.Duration
import com.ryanjames.sheetmusic.model.Measure
import com.ryanjames.sheetmusic.model.Note
import com.ryanjames.sheetmusic.model.Pitch
import com.ryanjames.sheetmusic.model.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

internal val exampleScore = Score(
    listOf(
        Measure(listOf(Pitch.C, Pitch.D, Pitch.E, Pitch.F).map { Note(it, 4, Duration.QUARTER) }),
        Measure(listOf(Pitch.G to 4, Pitch.A to 4, Pitch.B to 4, Pitch.C to 5).map { (p, o) -> Note(p, o, Duration.QUARTER) }),
    ),
)

/** Shows [exampleScore] once per staff size, stacked vertically, at a density of 1 so dp equal px. */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.showExampleScore(vararg staffSpacesPx: Int) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(1f)) {
            Column(Modifier.background(Color.White)) {
                staffSpacesPx.forEach { SheetMusic(exampleScore, staffSpace = it.dp) }
            }
        }
    }
}

/**
 * Captures the [index]th score once it has been drawn. On the web the font arrives asynchronously, so this
 * polls with a real-time delay, which lets the browser complete the fetch on its single thread.
 */
@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.captureDrawnScore(index: Int = 0): ImageBitmap {
    repeat(400) {
        awaitIdle()
        val image = onAllNodesWithContentDescription("Sheet music")[index].captureToImage()
        if (image.toPixelMap().hasInk()) return image
        withContext(Dispatchers.Default) { delay(25) }
    }
    fail("The score was never drawn")
}

private fun PixelMap.isInk(x: Int, y: Int): Boolean {
    val c = this[x, y]
    return c.alpha > 0.5f && (c.red + c.green + c.blue) / 3 < 0.5f
}

private fun PixelMap.hasInk(): Boolean = (0 until height).any { y -> (0 until width).any { x -> isInk(x, y) } }

/** Ink extent of a row between two x positions, or null if empty. */
private fun PixelMap.inkInRow(y: Int, fromX: Int, toX: Int): IntRange? {
    val xs = (fromX..toX).filter { isInk(it, y) }
    return if (xs.isEmpty()) null else xs.first()..xs.last()
}

private fun PixelMap.inkInColumn(x: Int, fromY: Int, toY: Int): IntRange? {
    val ys = (fromY..toY).filter { isInk(x, it) }
    return if (ys.isEmpty()) null else ys.first()..ys.last()
}

/**
 * Renders the score with the real bundled Bravura font and checks the ink in the resulting pixels
 * against the positions computed by the layout. Runs on every platform with a Skia-based test host:
 * JVM desktop, the browser (JS and Wasm) and iOS.
 */
@OptIn(ExperimentalTestApi::class)
class SheetMusicRenderTest {
    private val layout = layoutScore(exampleScore)

    private fun assertWithin(expected: Float, actual: Int, tolerancePx: Float, what: String) =
        assertTrue(abs(expected - actual) <= tolerancePx, "$what: expected ~$expected px, was $actual px")

    @Test
    fun imageSizeMatchesLayout() = runRenderTest {
        val ss = 10
        showExampleScore(ss)
        val pixels = captureDrawnScore().toPixelMap()
        assertEquals((layout.width * ss).roundToInt().toFloat(), pixels.width.toFloat(), absoluteTolerance = 1f)
        assertEquals((layout.height * ss).roundToInt().toFloat(), pixels.height.toFloat(), absoluteTolerance = 1f)
    }

    @Test
    fun glyphInkMatchesSmuflMetrics() = runRenderTest {
        val ss = 20
        showExampleScore(ss)
        val pixels = captureDrawnScore().toPixelMap()
        val tolerance = 1.5f

        // Every black notehead is filled at its centre.
        layout.measures.flatMap { it.notes }.forEach { note ->
            val b = note.notehead.bounds
            val cx = ((b.left + b.right) / 2 * ss).toInt()
            val cy = (note.notehead.origin.y * ss).toInt()
            assertTrue(pixels.isInk(cx, cy), "notehead ${note.note} not drawn at ($cx, $cy)")
        }

        // D4 hangs below the staff, so its left, right and bottom edges are free of staff lines.
        val d4 = layout.measures[0].notes[1].notehead
        val row = pixels.inkInRow((d4.origin.y * ss).toInt(), ((d4.bounds.left - 0.5f) * ss).toInt(), ((d4.bounds.right + 0.5f) * ss).toInt())!!
        assertWithin(d4.bounds.left * ss, row.first, tolerance, "D4 notehead left")
        assertWithin(d4.bounds.right * ss, row.last + 1, tolerance, "D4 notehead right")
        val column = pixels.inkInColumn(((d4.bounds.left + d4.bounds.right) / 2 * ss).toInt(), (d4.origin.y * ss).toInt(), pixels.height - 1)!!
        assertWithin(d4.bounds.bottom * ss, column.last + 1, tolerance, "D4 notehead bottom")

        // The clef extends above and below the staff, so its vertical ink extent is unambiguous.
        val clef = layout.clef.bounds
        val clefInk = ((clef.left * ss).toInt()..(clef.right * ss).toInt())
            .mapNotNull { x -> pixels.inkInColumn(x, 0, pixels.height - 1) }
        assertWithin(clef.top * ss, clefInk.minOf { it.first }, tolerance, "clef top")
        assertWithin(clef.bottom * ss, clefInk.maxOf { it.last } + 1, tolerance, "clef bottom")
    }

    @Test
    fun renderingScalesWithStaffSpace() = runRenderTest {
        showExampleScore(10, 20)
        val small = captureDrawnScore(0).toPixelMap()
        val large = captureDrawnScore(1).toPixelMap()
        assertEquals(2f * small.width, large.width.toFloat(), absoluteTolerance = 2f)
        assertEquals(2f * small.height, large.height.toFloat(), absoluteTolerance = 2f)

        // The same staff line lands at twice the y coordinate.
        val x = (layout.staffLines.first().start.x + 0.5f).let { (it * 10).toInt() }
        val smallLines = (0 until small.height).filter { small.isInk(x, it) }
        val largeLines = (0 until large.height).filter { large.isInk(2 * x, it) }
        assertEquals(2f * smallLines.first(), largeLines.first().toFloat(), absoluteTolerance = 2f)
        assertEquals(2f * smallLines.last(), largeLines.last().toFloat(), absoluteTolerance = 3f)
    }
}
