package com.ryanjames.sheetmusic.score

/** Stable reasons an exact rational operation cannot produce a value. */
public enum class RationalError { ZERO_DENOMINATOR, DIVISION_BY_ZERO, OVERFLOW }

/** Numeric failure with a machine-readable [code]; values are never rounded or wrapped. */
public class RationalException(public val code: RationalError) : ArithmeticException(code.name)

/**
 * An exact reduced fraction with a positive denominator, used for quarter-note time and semitones.
 *
 * Both reduced components fit in [Int]. Operations use exact [Long] intermediates, reduce first,
 * and throw [RationalException] only if the reduced result is outside that range. This bound is
 * deliberate and identical on every target, including JS; there is no floating-point conversion.
 * Use [of] to construct a value. Zero is always 0/1; a negative sign belongs to the numerator.
 */
public class Rational private constructor(
    public val numerator: Int,
    public val denominator: Int,
) : Comparable<Rational> {
    /** Exact sum, reduced before checking the component bounds. */
    public operator fun plus(other: Rational): Rational = reduced(
        numerator.toLong() * other.denominator + other.numerator.toLong() * denominator,
        denominator.toLong() * other.denominator,
    )

    /** Exact difference, including subtraction of [Int.MIN_VALUE] without premature negation. */
    public operator fun minus(other: Rational): Rational = reduced(
        numerator.toLong() * other.denominator - other.numerator.toLong() * denominator,
        denominator.toLong() * other.denominator,
    )

    /** Exact product. */
    public operator fun times(other: Rational): Rational = reduced(
        numerator.toLong() * other.numerator,
        denominator.toLong() * other.denominator,
    )

    /** Exact quotient; division by zero has its own [RationalError]. */
    public operator fun div(other: Rational): Rational {
        if (other.numerator == 0) throw RationalException(RationalError.DIVISION_BY_ZERO)
        return reduced(numerator.toLong() * other.denominator, denominator.toLong() * other.numerator)
    }

    /** Negation, checked against the reduced component bounds. */
    public operator fun unaryMinus(): Rational = reduced(-numerator.toLong(), denominator.toLong())

    /** Exact ordering using cross multiplication in [Long]. */
    public override fun compareTo(other: Rational): Int =
        (numerator.toLong() * other.denominator).compareTo(other.numerator.toLong() * denominator)

    public override fun equals(other: Any?): Boolean =
        other is Rational && numerator == other.numerator && denominator == other.denominator

    public override fun hashCode(): Int = 31 * numerator + denominator

    public override fun toString(): String = "$numerator/$denominator"

    public companion object {
        /** Additive identity. */
        public val ZERO: Rational = Rational(0, 1)

        /** One quarter note when used as a duration. */
        public val ONE: Rational = Rational(1, 1)

        /** Normalize a fraction; a zero denominator fails even when the numerator is zero. */
        public fun of(numerator: Int, denominator: Int = 1): Rational =
            reduced(numerator.toLong(), denominator.toLong())

        // Inputs are Int values or products/sums of two Int components. Neither can be Long.MIN_VALUE.
        private fun reduced(numerator: Long, denominator: Long): Rational {
            if (denominator == 0L) throw RationalException(RationalError.ZERO_DENOMINATOR)
            if (numerator == 0L) return ZERO
            val sign = if (denominator < 0) -1 else 1
            var divisor = if (numerator < 0) -numerator else numerator
            var remainder = if (denominator < 0) -denominator else denominator
            while (remainder != 0L) {
                val next = divisor % remainder
                divisor = remainder
                remainder = next
            }
            val reducedNumerator = numerator / divisor * sign
            val reducedDenominator = denominator / divisor * sign
            if (reducedNumerator !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
                reducedDenominator > Int.MAX_VALUE
            ) {
                throw RationalException(RationalError.OVERFLOW)
            }
            return Rational(reducedNumerator.toInt(), reducedDenominator.toInt())
        }
    }
}
