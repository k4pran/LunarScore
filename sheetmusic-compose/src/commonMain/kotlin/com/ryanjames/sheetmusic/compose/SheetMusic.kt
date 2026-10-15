package com.ryanjames.sheetmusic.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ryanjames.sheetmusic.layout.PositionedGlyph
import com.ryanjames.sheetmusic.layout.ScoreLayout
import com.ryanjames.sheetmusic.layout.layoutScore
import com.ryanjames.sheetmusic.model.Score

/**
 * Renders [score] on a single staff.
 *
 * The staff takes its natural width, or stretches to fill the minimum width imposed by [modifier]
 * (for example `Modifier.fillMaxWidth()`). Its height is determined by the score's content.
 * Content wider than the available space is clipped: system breaking is not supported yet.
 *
 * @param staffSpace distance between two adjacent staff lines; every element scales with it.
 * @param color colour of all lines and symbols.
 */
@Composable
public fun SheetMusic(
    score: Score,
    modifier: Modifier = Modifier,
    staffSpace: Dp = 10.dp,
    color: Color = Color.Black,
) {
    val density = LocalDensity.current
    val staffSpacePx = with(density) { staffSpace.toPx() }
    val glyphStyle = rememberBravuraFontFamily()?.let { bravura ->
        TextStyle(
            fontFamily = bravura,
            // SMuFL fonts are scaled so that one em is four staff spaces.
            fontSize = with(density) { (4 * staffSpacePx).toSp() },
        )
    }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier.semantics { contentDescription = "Sheet music" }) {
        val minWidth = constraints.minWidth / staffSpacePx
        val layout = remember(score, minWidth) { layoutScore(score, minWidth) }
        val size = with(density) { Modifier.size((layout.width * staffSpacePx).toDp(), (layout.height * staffSpacePx).toDp()) }
        // The space is reserved immediately; drawing waits for the font (only ever pending on the web).
        Canvas(size.clipToBounds()) {
            if (glyphStyle != null) drawScore(layout, staffSpacePx, color, textMeasurer, glyphStyle)
        }
    }
}

private fun DrawScope.drawScore(
    layout: ScoreLayout,
    staffSpacePx: Float,
    color: Color,
    textMeasurer: TextMeasurer,
    glyphStyle: TextStyle,
) {
    for (line in layout.lines) {
        drawLine(
            color = color,
            start = Offset(line.start.x * staffSpacePx, line.start.y * staffSpacePx),
            end = Offset(line.end.x * staffSpacePx, line.end.y * staffSpacePx),
            strokeWidth = line.thickness * staffSpacePx,
        )
    }
    for (glyph in layout.glyphs) {
        drawGlyph(glyph, staffSpacePx, color, textMeasurer, glyphStyle)
    }
}

/**
 * Draws a glyph so that its SMuFL origin (on the text baseline) lands on the layout position.
 * The text layout's first baseline gives the offset from the top of the laid-out line to that origin.
 */
private fun DrawScope.drawGlyph(
    glyph: PositionedGlyph,
    staffSpacePx: Float,
    color: Color,
    textMeasurer: TextMeasurer,
    style: TextStyle,
) {
    val text = textMeasurer.measure(glyph.glyph.codepoint.toString(), style, softWrap = false, maxLines = 1)
    drawText(
        textLayoutResult = text,
        color = color,
        topLeft = Offset(glyph.origin.x * staffSpacePx, glyph.origin.y * staffSpacePx - text.firstBaseline),
    )
}
