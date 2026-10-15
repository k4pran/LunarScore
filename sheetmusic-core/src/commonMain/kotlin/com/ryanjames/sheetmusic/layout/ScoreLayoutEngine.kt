package com.ryanjames.sheetmusic.layout

import com.ryanjames.sheetmusic.model.Clef
import com.ryanjames.sheetmusic.model.Duration
import com.ryanjames.sheetmusic.model.Note
import com.ryanjames.sheetmusic.model.Score
import com.ryanjames.sheetmusic.smufl.BravuraMetrics
import com.ryanjames.sheetmusic.smufl.SmuflFontMetrics
import com.ryanjames.sheetmusic.smufl.SmuflGlyph

/** Spacing parameters for [layoutScore], in staff spaces. */
public data class LayoutSettings(
    val fontMetrics: SmuflFontMetrics = BravuraMetrics,
    /** Empty space around the content on every side. */
    val padding: Float = 1f,
    /** From the start of the staff to the clef. */
    val clefLeftMargin: Float = 0.75f,
    /** From the right edge of the clef to the start of the first measure. */
    val clefRightMargin: Float = 1f,
    /** From the start of a measure (or the previous barline) to its first notehead. */
    val measureLeftPadding: Float = 1.25f,
    /** Minimum distance between consecutive note origins. Every note gets the same space. */
    val noteSpacing: Float = 3f,
    /** Stem length measured from the notehead centre: one octave. */
    val stemLength: Float = 3.5f,
)

internal const val STAFF_LINE_COUNT = 5
internal const val MIDDLE_LINE_POSITION = 4
internal const val TOP_LINE_POSITION = 8

/** Steps above the bottom staff line at which [note] sits under [clef]. */
public fun staffPosition(note: Note, clef: Clef): Int =
    diatonicIndex(note.pitch.ordinal, note.octave) -
        diatonicIndex(clef.referencePitch.ordinal, clef.referenceOctave) +
        clef.referenceStaffPosition

private fun diatonicIndex(step: Int, octave: Int) = octave * 7 + step

/** Notes on or above the middle line take down-stems; notes below it take up-stems. */
public fun stemDirection(staffPosition: Int): StemDirection =
    if (staffPosition >= MIDDLE_LINE_POSITION) StemDirection.DOWN else StemDirection.UP

/**
 * Positions every element of [score] on a single staff.
 *
 * If [minWidth] (staff spaces) is larger than the natural width, the note spacing is stretched so the
 * layout fills it exactly. The layout is never compressed below its natural width.
 */
public fun layoutScore(
    score: Score,
    minWidth: Float = 0f,
    settings: LayoutSettings = LayoutSettings(),
): ScoreLayout {
    val natural = ScoreLayoutEngine(score, settings, settings.noteSpacing).layout()
    if (natural.width >= minWidth) return natural
    // Width grows linearly with note spacing, one spacing unit per note.
    val noteCount = score.measures.sumOf { it.notes.size }
    val spacing = settings.noteSpacing + (minWidth - natural.width) / noteCount
    return ScoreLayoutEngine(score, settings, spacing).layout()
}

private class ScoreLayoutEngine(
    private val score: Score,
    private val settings: LayoutSettings,
    private val noteSpacing: Float,
) {
    private val metrics = settings.fontMetrics
    private val engraving = metrics.engravingDefaults

    // Layout is built with the top staff line at y = 0 and the staff starting at x = 0, then shifted into place.
    private val staffTop = 0f
    private val staffBottom = staffTop + (STAFF_LINE_COUNT - 1)

    private fun yOf(staffPosition: Int): Float = staffTop + (TOP_LINE_POSITION - staffPosition) / 2f

    fun layout(): ScoreLayout {
        val clef = placeGlyph(SmuflGlyph.G_CLEF, settings.clefLeftMargin, yOf(score.clef.referenceStaffPosition))

        var x = clef.bounds.right + settings.clefRightMargin
        val measures = score.measures.mapIndexed { index, measure ->
            val firstNoteX = x + settings.measureLeftPadding
            val notes = measure.notes.mapIndexed { i, note -> layoutNote(note, firstNoteX + i * noteSpacing) }
            val barlineX = firstNoteX + measure.notes.size * noteSpacing
            val barline = if (index == score.measures.lastIndex) finalBarline(barlineX) else singleBarline(barlineX)
            x = barline.bounds.right
            MeasureLayout(measure, notes, barline)
        }

        val staffLines = (0 until STAFF_LINE_COUNT).map { line ->
            val y = staffTop + line
            LineSegment(Point(0f, y), Point(x, y), engraving.staffLineThickness)
        }

        val content = (staffLines.map { it.bounds } + clef.bounds + measures.flatMap { m ->
            m.notes.map { it.bounds } + m.barline.bounds
        }).reduce(Rect::union)

        val dx = settings.padding - content.left
        val dy = settings.padding - content.top
        return ScoreLayout(
            width = content.width + 2 * settings.padding,
            height = content.height + 2 * settings.padding,
            staffLines = staffLines.map { it.translate(dx, dy) },
            clef = clef.translate(dx, dy),
            measures = measures.map { it.translate(dx, dy) },
        )
    }

    private fun placeGlyph(glyph: SmuflGlyph, x: Float, y: Float): PositionedGlyph {
        val m = metrics[glyph]
        // SMuFL y points up; layout y points down.
        return PositionedGlyph(glyph, Point(x, y), Rect(x + m.bBoxSW.x, y - m.bBoxNE.y, x + m.bBoxNE.x, y - m.bBoxSW.y))
    }

    private fun layoutNote(note: Note, x: Float): NoteLayout {
        val position = staffPosition(note, score.clef)
        val y = yOf(position)
        val glyph = when (note.duration) {
            Duration.WHOLE -> SmuflGlyph.NOTEHEAD_WHOLE
            Duration.HALF -> SmuflGlyph.NOTEHEAD_HALF
            Duration.QUARTER -> SmuflGlyph.NOTEHEAD_BLACK
        }
        val notehead = placeGlyph(glyph, x, y)

        val direction = if (note.duration == Duration.WHOLE) null else stemDirection(position)
        val stem = direction?.let { stem(glyph, x, y, it) }

        return NoteLayout(note, position, notehead, direction, stem, ledgerLines(position, notehead.bounds))
    }

    private fun stem(glyph: SmuflGlyph, x: Float, y: Float, direction: StemDirection): LineSegment {
        val m = metrics[glyph]
        val half = engraving.stemThickness / 2
        val middleY = yOf(MIDDLE_LINE_POSITION)
        return when (direction) {
            StemDirection.UP -> {
                val anchor = checkNotNull(m.stemUpSE)
                // The stem's right edge sits on the anchor; stems reach at least the middle line.
                val stemX = x + anchor.x - half
                LineSegment(Point(stemX, y - anchor.y), Point(stemX, minOf(y - settings.stemLength, middleY)), engraving.stemThickness)
            }
            StemDirection.DOWN -> {
                val anchor = checkNotNull(m.stemDownNW)
                val stemX = x + anchor.x + half
                LineSegment(Point(stemX, y - anchor.y), Point(stemX, maxOf(y + settings.stemLength, middleY)), engraving.stemThickness)
            }
        }
    }

    private fun ledgerLines(position: Int, head: Rect): List<LineSegment> {
        val positions: Iterable<Int> = when {
            position <= -2 -> -2 downTo position step 2
            position >= TOP_LINE_POSITION + 2 -> TOP_LINE_POSITION + 2..position step 2
            else -> emptyList()
        }
        val left = head.left - engraving.legerLineExtension
        val right = head.right + engraving.legerLineExtension
        return positions.map { p -> LineSegment(Point(left, yOf(p)), Point(right, yOf(p)), engraving.legerLineThickness) }
    }

    /** Barline strokes span the staff, covering the full thickness of the outer staff lines. */
    private fun verticalStroke(leftEdge: Float, thickness: Float): LineSegment {
        val x = leftEdge + thickness / 2
        val overhang = engraving.staffLineThickness / 2
        return LineSegment(Point(x, staffTop - overhang), Point(x, staffBottom + overhang), thickness)
    }

    private fun singleBarline(x: Float) =
        BarlineLayout(BarlineType.SINGLE, listOf(verticalStroke(x, engraving.thinBarlineThickness)))

    private fun finalBarline(x: Float): BarlineLayout {
        val thickX = x + engraving.thinBarlineThickness + engraving.thinThickBarlineSeparation
        return BarlineLayout(
            BarlineType.FINAL,
            listOf(verticalStroke(x, engraving.thinBarlineThickness), verticalStroke(thickX, engraving.thickBarlineThickness)),
        )
    }
}
