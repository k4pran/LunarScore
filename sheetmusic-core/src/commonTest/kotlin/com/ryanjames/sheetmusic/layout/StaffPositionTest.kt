package com.ryanjames.sheetmusic.layout

import com.ryanjames.sheetmusic.model.Clef
import com.ryanjames.sheetmusic.model.Pitch
import kotlin.test.Test
import kotlin.test.assertEquals

class StaffPositionTest {
    private fun position(pitch: Pitch, octave: Int) = staffPosition(quarter(pitch, octave), Clef.TREBLE)

    @Test
    fun trebleStaffLinesAreEGBDF() {
        assertEquals(0, position(Pitch.E, 4))
        assertEquals(2, position(Pitch.G, 4))
        assertEquals(4, position(Pitch.B, 4))
        assertEquals(6, position(Pitch.D, 5))
        assertEquals(8, position(Pitch.F, 5))
    }

    @Test
    fun trebleStaffSpacesAreFACE() {
        assertEquals(1, position(Pitch.F, 4))
        assertEquals(3, position(Pitch.A, 4))
        assertEquals(5, position(Pitch.C, 5))
        assertEquals(7, position(Pitch.E, 5))
    }

    @Test
    fun notesOutsideTheStaff() {
        assertEquals(-1, position(Pitch.D, 4))
        assertEquals(-2, position(Pitch.C, 4)) // middle C: first ledger line below
        assertEquals(-3, position(Pitch.B, 3)) // octave boundary
        assertEquals(10, position(Pitch.A, 5)) // first ledger line above
        assertEquals(12, position(Pitch.C, 6))
    }

    @Test
    fun anOctaveIsSevenSteps() {
        for (pitch in Pitch.entries) {
            assertEquals(7, position(pitch, 5) - position(pitch, 4), "$pitch")
        }
    }

    @Test
    fun stemDirectionFollowsTheMiddleLine() {
        assertEquals(StemDirection.UP, stemDirection(3))
        assertEquals(StemDirection.DOWN, stemDirection(4)) // middle-line notes take down-stems
        assertEquals(StemDirection.DOWN, stemDirection(5))
    }
}
