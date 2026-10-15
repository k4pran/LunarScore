package com.ryanjames.sheetmusic.layout

import com.ryanjames.sheetmusic.model.Measure
import com.ryanjames.sheetmusic.model.Note
import com.ryanjames.sheetmusic.smufl.SmuflGlyph

// All layout coordinates are in staff spaces, with the origin at the top-left of the layout and y pointing down.

public data class Point(val x: Float, val y: Float)

public data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    /** True if the two rectangles share interior area (touching edges do not count). */
    public fun overlaps(other: Rect): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom

    public fun contains(other: Rect): Boolean =
        other.left >= left && other.right <= right && other.top >= top && other.bottom <= bottom

    public fun union(other: Rect): Rect =
        Rect(minOf(left, other.left), minOf(top, other.top), maxOf(right, other.right), maxOf(bottom, other.bottom))

    internal fun translate(dx: Float, dy: Float): Rect = Rect(left + dx, top + dy, right + dx, bottom + dy)
}

/** A straight horizontal or vertical stroke, centred on the segment from [start] to [end], with butt ends. */
public data class LineSegment(val start: Point, val end: Point, val thickness: Float) {
    val bounds: Rect
        get() {
            val half = thickness / 2
            val horizontal = start.y == end.y
            val dx = if (horizontal) 0f else half
            val dy = if (horizontal) half else 0f
            return Rect(
                minOf(start.x, end.x) - dx,
                minOf(start.y, end.y) - dy,
                maxOf(start.x, end.x) + dx,
                maxOf(start.y, end.y) + dy,
            )
        }

    internal fun translate(dx: Float, dy: Float): LineSegment =
        LineSegment(Point(start.x + dx, start.y + dy), Point(end.x + dx, end.y + dy), thickness)
}

/** A SMuFL glyph whose baseline origin sits at [origin]. [bounds] is its ink bounding box from the font metadata. */
public data class PositionedGlyph(val glyph: SmuflGlyph, val origin: Point, val bounds: Rect) {
    internal fun translate(dx: Float, dy: Float): PositionedGlyph =
        PositionedGlyph(glyph, Point(origin.x + dx, origin.y + dy), bounds.translate(dx, dy))
}

public enum class StemDirection { UP, DOWN }

public data class NoteLayout(
    val note: Note,
    /** Steps above the bottom staff line: 0 is the bottom line, 1 the first space, 8 the top line. */
    val staffPosition: Int,
    val notehead: PositionedGlyph,
    val stemDirection: StemDirection?,
    val stem: LineSegment?,
    val ledgerLines: List<LineSegment>,
) {
    val bounds: Rect
        get() = (listOfNotNull(stem) + ledgerLines).fold(notehead.bounds) { acc, line -> acc.union(line.bounds) }

    internal fun translate(dx: Float, dy: Float): NoteLayout = copy(
        notehead = notehead.translate(dx, dy),
        stem = stem?.translate(dx, dy),
        ledgerLines = ledgerLines.map { it.translate(dx, dy) },
    )
}

public enum class BarlineType { SINGLE, FINAL }

public data class BarlineLayout(val type: BarlineType, val lines: List<LineSegment>) {
    val bounds: Rect get() = lines.map { it.bounds }.reduce(Rect::union)

    internal fun translate(dx: Float, dy: Float): BarlineLayout = copy(lines = lines.map { it.translate(dx, dy) })
}

public data class MeasureLayout(
    val measure: Measure,
    val notes: List<NoteLayout>,
    /** The barline closing this measure. */
    val barline: BarlineLayout,
) {
    internal fun translate(dx: Float, dy: Float): MeasureLayout =
        copy(notes = notes.map { it.translate(dx, dy) }, barline = barline.translate(dx, dy))
}

/** A fully positioned score, ready to be drawn by any renderer. */
public data class ScoreLayout(
    /** Total size including padding, in staff spaces. */
    val width: Float,
    val height: Float,
    /** The five staff lines, top line first. */
    val staffLines: List<LineSegment>,
    val clef: PositionedGlyph,
    val measures: List<MeasureLayout>,
) {
    val bounds: Rect get() = Rect(0f, 0f, width, height)

    /** Every stroke to draw: staff lines, ledger lines, stems and barlines. */
    val lines: List<LineSegment>
        get() = staffLines + measures.flatMap { m ->
            m.notes.flatMap { it.ledgerLines + listOfNotNull(it.stem) } + m.barline.lines
        }

    /** Every glyph to draw. */
    val glyphs: List<PositionedGlyph>
        get() = listOf(clef) + measures.flatMap { m -> m.notes.map { it.notehead } }
}
