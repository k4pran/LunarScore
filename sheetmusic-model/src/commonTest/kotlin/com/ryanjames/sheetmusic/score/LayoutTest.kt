package com.ryanjames.sheetmusic.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LayoutTest {
    @Test
    fun testPageHeightAndWidthMustBeGivenTogether() {
        assertFailsWith<IllegalArgumentException> { PageLayout(height = Rational.of(1650)) }
        assertFailsWith<IllegalArgumentException> { PageLayout(width = Rational.of(1200)) }
        PageLayout()
        PageLayout(Rational.of(1650), Rational.of(1200))
    }

    @Test
    fun testStaffLayoutDefaultsToFirstStaff() {
        assertEquals(1, StaffLayout().staff)
        assertFailsWith<IllegalArgumentException> { StaffLayout(0) }
    }
}
