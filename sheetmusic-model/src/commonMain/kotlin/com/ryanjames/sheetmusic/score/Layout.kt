package com.ryanjames.sheetmusic.score

/*
 * Optional engraving hints. They never change musical meaning, and a renderer may ignore any of
 * them. Every length is in tenths: one tenth of the space between two adjacent staff lines, so a
 * five-line staff is 40 tenths high. [Scaling] relates tenths to millimetres.
 */

/** [millimeters] millimetres correspond to [tenths] tenths. */
public data class Scaling(val millimeters: Rational, val tenths: Rational) {
    init {
        require(millimeters > Rational.ZERO && tenths > Rational.ZERO)
    }
}

/** The pages a set of [PageMargins] applies to. */
public enum class MarginType { ODD, EVEN, BOTH }

/** Page margins in tenths for odd, even or all pages. */
public data class PageMargins(
    val type: MarginType,
    val left: Rational,
    val right: Rational,
    val top: Rational,
    val bottom: Rational,
)

/** Page size and margins in tenths; null or empty means unspecified. [height] and [width] are given together or not at all. */
public data class PageLayout(
    val height: Rational? = null,
    val width: Rational? = null,
    val margins: List<PageMargins> = emptyList(),
) {
    init {
        require(height == null || height > Rational.ZERO)
        require(width == null || width > Rational.ZERO)
        require((height == null) == (width == null)) { "Page height and width must be given together" }
    }
}

/**
 * System layout in tenths. [leftMargin] and [rightMargin] are relative to the page margins.
 * [systemDistance] is from the bottom line of one system to the top line of the next, and
 * [topSystemDistance] from the top page margin to the top line of the first system on a page.
 */
public data class SystemLayout(
    val leftMargin: Rational? = null,
    val rightMargin: Rational? = null,
    val systemDistance: Rational? = null,
    val topSystemDistance: Rational? = null,
)

/**
 * Distance in tenths from the bottom line of the previous staff to the top line of [staff] within
 * a system. [staff] is 1-based and defaults to the first staff.
 */
public data class StaffLayout(val staff: Int = 1, val staffDistance: Rational? = null) {
    init {
        require(staff > 0)
    }
}

/** Score-wide layout defaults; any part may be absent. */
public data class LayoutDefaults(
    val scaling: Scaling? = null,
    val page: PageLayout? = null,
    val system: SystemLayout? = null,
    val staves: List<StaffLayout> = emptyList(),
)

/**
 * Layout requested at the start of a measure: system and page breaks, [blankPages] inserted before
 * it, the displayed [pageNumber], and layout overriding [LayoutDefaults] from here on.
 * [measureDistance] is the horizontal distance in tenths from the previous measure, for the first
 * measure of a system only. [newSystem] and [newPage] are true to force a break, false to forbid
 * one, and null to leave breaking to the renderer.
 */
public data class PrintHints(
    val newSystem: Boolean? = null,
    val newPage: Boolean? = null,
    val blankPages: Int = 0,
    val pageNumber: String? = null,
    val page: PageLayout? = null,
    val system: SystemLayout? = null,
    val staves: List<StaffLayout> = emptyList(),
    val measureDistance: Rational? = null,
) {
    init {
        require(blankPages >= 0)
    }
}

/**
 * Position and colour of a printed element. [defaultX] and [defaultY] are in tenths from the
 * element's reference point (horizontally usually the start of the measure, vertically the top
 * staff line); [relativeX] and [relativeY] shift the element from its default position. Positive y
 * is up; any value may be negative. [color] is `#RRGGBB` or `#AARRGGBB` in upper case.
 */
public data class VisualHints(
    val defaultX: Rational? = null,
    val defaultY: Rational? = null,
    val relativeX: Rational? = null,
    val relativeY: Rational? = null,
    val color: String? = null,
) {
    init {
        require(color == null || COLOR.matches(color))
    }
}

private val COLOR = Regex("#[0-9A-F]{6}([0-9A-F]{2})?")
