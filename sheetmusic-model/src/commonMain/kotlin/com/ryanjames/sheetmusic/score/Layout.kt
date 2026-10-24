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

/**
 * Score-wide layout defaults; any part may be absent. [musicFont] is the default font for music
 * symbols and [wordFont] the default font for words and other text.
 */
public data class LayoutDefaults(
    val scaling: Scaling? = null,
    val page: PageLayout? = null,
    val system: SystemLayout? = null,
    val staves: List<StaffLayout> = emptyList(),
    val musicFont: FontHints? = null,
    val wordFont: FontHints? = null,
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
 * Position, colour and text formatting of a printed element; null means unspecified. [defaultX]
 * and [defaultY] are in tenths from the element's reference point (horizontally usually the start
 * of the measure, vertically the top staff line); [relativeX] and [relativeY] shift the element
 * from its default position. Positive y is up; any value may be negative. [color] is `#RRGGBB` or
 * `#AARRGGBB` in upper case.
 *
 * [font] is the element's font. [halign] and [valign] align the element on its position, and
 * [justify] aligns the lines of multi-line text with each other. [enclosure] is a shape printed
 * around the element. [preserveWhitespace] is true when the text's whitespace, including line
 * breaks, is significant (`xml:space="preserve"`) and false when the source leaves whitespace
 * handling to the application; the text itself is always kept exactly.
 */
public data class VisualHints(
    val defaultX: Rational? = null,
    val defaultY: Rational? = null,
    val relativeX: Rational? = null,
    val relativeY: Rational? = null,
    val color: String? = null,
    val font: FontHints? = null,
    val halign: HorizontalAlignment? = null,
    val valign: VerticalAlignment? = null,
    val justify: HorizontalAlignment? = null,
    val enclosure: Enclosure? = null,
    val preserveWhitespace: Boolean? = null,
) {
    init {
        require(color == null || COLOR.matches(color))
    }
}

/**
 * A font: [families] in order of preference (a family may be a generic name such as `serif`),
 * [size], [style] and [weight]; empty or null means unspecified.
 */
public data class FontHints(
    val families: List<String> = emptyList(),
    val size: FontSize? = null,
    val style: FontStyle? = null,
    val weight: FontWeight? = null,
) {
    init {
        require(families.all { it.isNotBlank() })
    }
}

/** A font size, either in points or as a CSS size name. */
public sealed interface FontSize {
    /** A positive size in points. */
    public data class Points(val points: Rational) : FontSize {
        init {
            require(points > Rational.ZERO)
        }
    }

    /** A CSS size name such as `large`. */
    public data class Css(val size: CssFontSize) : FontSize
}

/** CSS font size names, from smallest to largest. */
public enum class CssFontSize { XX_SMALL, X_SMALL, SMALL, MEDIUM, LARGE, X_LARGE, XX_LARGE }

/** Upright or italic text. */
public enum class FontStyle { NORMAL, ITALIC }

/** Normal or bold text. */
public enum class FontWeight { NORMAL, BOLD }

/** Horizontal alignment of an element on its position, or of text lines with each other. */
public enum class HorizontalAlignment { LEFT, CENTER, RIGHT }

/** Vertical alignment of an element on its position. */
public enum class VerticalAlignment { TOP, MIDDLE, BOTTOM, BASELINE }

/** A shape printed around text, such as a rehearsal box; [NONE] explicitly prints none. */
public enum class Enclosure {
    RECTANGLE, SQUARE, OVAL, CIRCLE, BRACKET, INVERTED_BRACKET, TRIANGLE, DIAMOND,
    PENTAGON, HEXAGON, HEPTAGON, OCTAGON, NONAGON, DECAGON, NONE,
}

private val COLOR = Regex("#[0-9A-F]{6}([0-9A-F]{2})?")
