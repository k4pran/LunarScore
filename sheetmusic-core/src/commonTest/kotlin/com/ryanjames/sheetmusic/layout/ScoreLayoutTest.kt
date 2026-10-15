package com.ryanjames.sheetmusic.layout

import com.ryanjames.sheetmusic.model.Duration
import com.ryanjames.sheetmusic.model.Measure
import com.ryanjames.sheetmusic.model.Note
import com.ryanjames.sheetmusic.model.Pitch
import com.ryanjames.sheetmusic.model.Score
import com.ryanjames.sheetmusic.smufl.BravuraMetrics
import com.ryanjames.sheetmusic.smufl.SmuflGlyph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScoreLayoutTest {
    private val layout = layoutScore(scaleScore)
    private val engraving = BravuraMetrics.engravingDefaults
    private val notes = layout.measures.flatMap { it.notes }
    private val topLineY = layout.staffLines.first().start.y
    private fun yOf(position: Int) = topLineY + (8 - position) / 2f

    // --- Staff geometry ---

    @Test
    fun staffHasFiveEvenlySpacedLines() {
        assertEquals(5, layout.staffLines.size)
        layout.staffLines.zipWithNext().forEach { (a, b) -> assertNear(1f, b.start.y - a.start.y, "line gap") }
        layout.staffLines.forEach {
            assertEquals(it.start.y, it.end.y)
            assertNear(engraving.staffLineThickness, it.thickness)
            assertNear(layout.staffLines.first().start.x, it.start.x)
            assertNear(layout.staffLines.first().end.x, it.end.x)
        }
    }

    @Test
    fun clefSitsOnTheGLine() {
        assertEquals(SmuflGlyph.G_CLEF, layout.clef.glyph)
        assertNear(yOf(2), layout.clef.origin.y)
        // The G clef's bounding box from the font metadata extends above and below the staff.
        assertNear(yOf(2) - 4.392f, layout.clef.bounds.top)
        assertNear(yOf(2) + 2.632f, layout.clef.bounds.bottom)
        assertTrue(layout.clef.bounds.left > layout.staffLines.first().start.x)
    }

    // --- Notes ---

    @Test
    fun noteheadsSitAtTheirPitchPositions() {
        assertEquals(8, notes.size)
        assertEquals(listOf(-2, -1, 0, 1, 2, 3, 4, 5), notes.map { it.staffPosition })
        notes.forEach { note ->
            assertEquals(SmuflGlyph.NOTEHEAD_BLACK, note.notehead.glyph)
            assertNear(yOf(note.staffPosition), note.notehead.origin.y, "${note.note}")
            // A notehead is exactly one staff space tall, centred on its line or space.
            assertNear(note.notehead.origin.y - 0.5f, note.notehead.bounds.top)
            assertNear(note.notehead.origin.y + 0.5f, note.notehead.bounds.bottom)
        }
    }

    @Test
    fun notesAreUniformlySpacedWithinAndAcrossMeasures() {
        layout.measures.forEach { measure ->
            val gaps = measure.notes.zipWithNext { a, b -> b.notehead.origin.x - a.notehead.origin.x }
            gaps.forEach { assertNear(gaps.first(), it) }
        }
        notes.zipWithNext().forEach { (a, b) -> assertTrue(b.notehead.origin.x > a.notehead.bounds.right) }
    }

    @Test
    fun stemsFollowEngravingConventions() {
        val directions = notes.map { it.stemDirection }
        assertEquals(List(6) { StemDirection.UP } + List(2) { StemDirection.DOWN }, directions)

        val stemUpSE = BravuraMetrics[SmuflGlyph.NOTEHEAD_BLACK].stemUpSE!!
        val stemDownNW = BravuraMetrics[SmuflGlyph.NOTEHEAD_BLACK].stemDownNW!!
        notes.forEach { note ->
            val stem = note.stem!!
            val head = note.notehead.origin
            assertNear(engraving.stemThickness, stem.thickness)
            assertEquals(stem.start.x, stem.end.x, "stems are vertical")
            if (note.stemDirection == StemDirection.UP) {
                // Right edge of the stem on the notehead's stemUpSE anchor, rising one octave.
                assertNear(head.x + stemUpSE.x, stem.bounds.right)
                assertNear(head.y - stemUpSE.y, stem.start.y)
                assertNear(head.y - 3.5f, stem.end.y)
            } else {
                assertNear(head.x + stemDownNW.x, stem.bounds.left)
                assertNear(head.y - stemDownNW.y, stem.start.y)
                assertNear(head.y + 3.5f, stem.end.y)
            }
        }
    }

    @Test
    fun stemsOfDistantNotesReachTheMiddleLine() {
        val score = Score(listOf(Measure(listOf(quarter(Pitch.F, 3), quarter(Pitch.C, 6)))))
        val result = layoutScore(score)
        val middleY = result.staffLines[2].start.y
        val (low, high) = result.measures.single().notes
        assertNear(middleY, low.stem!!.end.y)
        assertNear(middleY, high.stem!!.end.y)
    }

    @Test
    fun wholeNotesHaveNoStem() {
        val score = Score(listOf(Measure(listOf(Note(Pitch.G, 4, Duration.WHOLE)))))
        val note = layoutScore(score).measures.single().notes.single()
        assertEquals(SmuflGlyph.NOTEHEAD_WHOLE, note.notehead.glyph)
        assertNull(note.stem)
        assertNull(note.stemDirection)
    }

    @Test
    fun ledgerLinesAreDrawnOnlyOutsideTheStaff() {
        val middleC = notes.first()
        val ledger = middleC.ledgerLines.single()
        assertNear(yOf(-2), ledger.start.y)
        assertNear(middleC.notehead.bounds.left - engraving.legerLineExtension, ledger.start.x)
        assertNear(middleC.notehead.bounds.right + engraving.legerLineExtension, ledger.end.x)
        assertNear(engraving.legerLineThickness, ledger.thickness)
        notes.drop(1).forEach { assertTrue(it.ledgerLines.isEmpty(), "${it.note}") }

        // B3 hangs below the C4 ledger line; A5 and C6 need one and two lines above the staff.
        val score = Score(listOf(Measure(listOf(quarter(Pitch.B, 3), quarter(Pitch.A, 5), quarter(Pitch.C, 6)))))
        val (b3, a5, c6) = layoutScore(score).measures.single().notes
        assertEquals(1, b3.ledgerLines.size)
        assertEquals(1, a5.ledgerLines.size)
        assertEquals(2, c6.ledgerLines.size)
    }

    // --- Barlines ---

    @Test
    fun measuresAreSeparatedByBarlinesAndEndWithAFinalBarline() {
        assertEquals(listOf(BarlineType.SINGLE, BarlineType.FINAL), layout.measures.map { it.barline.type })

        val single = layout.measures[0].barline
        assertNear(engraving.thinBarlineThickness, single.lines.single().thickness)
        assertTrue(single.bounds.left > layout.measures[0].notes.last().bounds.right)
        assertTrue(single.bounds.right < layout.measures[1].notes.first().bounds.left)

        val (thin, thick) = layout.measures[1].barline.lines
        assertNear(engraving.thinBarlineThickness, thin.thickness)
        assertNear(engraving.thickBarlineThickness, thick.thickness)
        assertNear(engraving.thinThickBarlineSeparation, thick.bounds.left - thin.bounds.right)
        // The staff ends flush with the thick line.
        assertNear(thick.bounds.right, layout.staffLines.first().end.x)
    }

    @Test
    fun barlinesSpanExactlyTheStaff() {
        val staffTop = layout.staffLines.first().bounds.top
        val staffBottom = layout.staffLines.last().bounds.bottom
        layout.measures.flatMap { it.barline.lines }.forEach {
            assertNear(staffTop, it.bounds.top)
            assertNear(staffBottom, it.bounds.bottom)
        }
    }

    // --- Bounds and overlap ---

    @Test
    fun everythingFitsInsideThePaddedBounds() {
        val inner = Rect(1f, 1f, layout.width - 1f, layout.height - 1f)
        (layout.lines.map { it.bounds } + layout.glyphs.map { it.bounds }).forEach {
            assertTrue(inner.contains(it), "$it outside $inner")
        }
        // The padding is tight: the content touches each edge of the inner rectangle.
        val content = (layout.lines.map { it.bounds } + layout.glyphs.map { it.bounds }).reduce(Rect::union)
        assertNear(inner.left, content.left)
        assertNear(inner.top, content.top)
        assertNear(inner.right, content.right)
        assertNear(inner.bottom, content.bottom)
    }

    @Test
    fun clefIsTheTallestElementOfTheExample() {
        // In the example, the clef determines the vertical extent: its top and bottom define the bounds.
        assertNear(1f, layout.clef.bounds.top)
        assertNear(layout.height - 1f, layout.clef.bounds.bottom)
    }

    @Test
    fun clefNotesAndBarlinesDoNotOverlap() {
        val barlines = layout.measures.map { it.barline.bounds }
        val noteBounds = notes.map { it.bounds }
        val noteheads = notes.map { it.notehead.bounds }
        noteBounds.forEach { assertFalse(it.overlaps(layout.clef.bounds), "note overlaps clef: $it") }
        barlines.forEach { assertFalse(it.overlaps(layout.clef.bounds)) }
        for (n in noteBounds) for (b in barlines) assertFalse(n.overlaps(b), "note $n overlaps barline $b")
        noteheads.zipWithNext().forEach { (a, b) -> assertFalse(a.overlaps(b)) }
    }

    // --- Width and scaling ---

    @Test
    fun justificationStretchesNoteSpacingToFillTheWidth() {
        val wide = layoutScore(scaleScore, minWidth = layout.width + 20f)
        assertNear(layout.width + 20f, wide.width)
        // Spacing grows uniformly; vertical geometry is untouched.
        val oldGap = notes[1].notehead.origin.x - notes[0].notehead.origin.x
        val wideNotes = wide.measures.flatMap { it.notes }
        val newGap = wideNotes[1].notehead.origin.x - wideNotes[0].notehead.origin.x
        assertNear(oldGap + 20f / 8, newGap)
        assertNear(layout.height, wide.height)
        assertEquals(notes.map { it.notehead.origin.y }, wideNotes.map { it.notehead.origin.y })
    }

    @Test
    fun layoutIsNeverCompressedBelowItsNaturalWidth() {
        assertEquals(layout, layoutScore(scaleScore, minWidth = layout.width / 2))
    }

    @Test
    fun noteSpacingSettingIsHonoured() {
        val spaced = layoutScore(scaleScore, settings = LayoutSettings(noteSpacing = 5f)).measures.flatMap { it.notes }
        spaced.take(4).zipWithNext().forEach { (a, b) -> assertNear(5f, b.notehead.origin.x - a.notehead.origin.x) }
    }
}
