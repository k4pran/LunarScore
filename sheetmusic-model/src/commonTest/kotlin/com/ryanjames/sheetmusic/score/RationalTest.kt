package com.ryanjames.sheetmusic.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RationalTest {
    @Test
    fun testCanonicalReductionAndSigns() {
        assertEquals(Rational.of(1, 2), Rational.of(8, 16))
        assertEquals(Rational.of(-1, 2), Rational.of(1, -2))
        assertEquals(Rational.ONE, Rational.of(Int.MIN_VALUE, Int.MIN_VALUE))
        assertEquals(Rational.of(-1, 1073741824), Rational.of(2, Int.MIN_VALUE))
        assertEquals(Rational.ZERO, Rational.of(0, Int.MIN_VALUE))
        assertEquals(Rational.of(1, 2).hashCode(), Rational.of(2, 4).hashCode())
        assertEquals("-1/2", Rational.of(-2, 4).toString())
    }

    @Test
    fun testExactArithmeticAndTuplets() {
        val third = Rational.of(1, 3)
        assertEquals(Rational.ONE, third + third + third)
        assertEquals(Rational.of(1, 6), Rational.of(1, 2) - third)
        assertEquals(Rational.of(2, 9), third * Rational.of(2, 3))
        assertEquals(Rational.of(3, 2), Rational.ONE / Rational.of(2, 3))
        assertEquals(Rational.of(-1, 3), -third)
        assertEquals(Rational.ZERO, Rational.of(Int.MIN_VALUE) - Rational.of(Int.MIN_VALUE))
    }

    @Test
    fun testLargeIntermediatesReduceBeforeBoundsCheck() {
        val almostOne = Rational.of(Int.MAX_VALUE, Int.MAX_VALUE - 1)
        assertEquals(Rational.ONE, almostOne * Rational.of(Int.MAX_VALUE - 1, Int.MAX_VALUE))
        assertEquals(Rational.ONE, almostOne / almostOne)
        assertEquals(Rational.of(Int.MAX_VALUE), Rational.of(Int.MAX_VALUE, 2) + Rational.of(Int.MAX_VALUE, 2))
        assertEquals(Rational.of(-1, Int.MAX_VALUE), Rational.of(Int.MIN_VALUE, Int.MAX_VALUE) + Rational.ONE)
        assertTrue(Rational.of(Int.MAX_VALUE, Int.MAX_VALUE - 1) > Rational.ONE)
        assertTrue(Rational.of(Int.MIN_VALUE, Int.MAX_VALUE) < Rational.of(-1))
    }

    @Test
    fun testOverflowAndZeroHaveStructuredCodes() {
        assertError(RationalError.ZERO_DENOMINATOR) { Rational.of(0, 0) }
        assertError(RationalError.DIVISION_BY_ZERO) { Rational.ONE / Rational.ZERO }
        assertError(RationalError.OVERFLOW) { Rational.of(Int.MAX_VALUE) + Rational.ONE }
        assertError(RationalError.OVERFLOW) { Rational.of(Int.MIN_VALUE) - Rational.ONE }
        assertError(RationalError.OVERFLOW) { Rational.of(Int.MAX_VALUE) * Rational.of(2) }
        assertError(RationalError.OVERFLOW) { Rational.of(1, Int.MAX_VALUE) / Rational.of(Int.MAX_VALUE) }
        assertError(RationalError.OVERFLOW) { -Rational.of(Int.MIN_VALUE) }
        assertError(RationalError.OVERFLOW) { Rational.of(Int.MIN_VALUE, -1) }
        assertError(RationalError.OVERFLOW) { Rational.of(1, Int.MIN_VALUE) }
    }

    private fun assertError(code: RationalError, operation: () -> Unit) {
        assertEquals(code, assertFailsWith<RationalException> { operation() }.code)
    }
}
