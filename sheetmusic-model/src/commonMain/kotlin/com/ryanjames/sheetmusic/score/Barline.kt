package com.ryanjames.sheetmusic.score

/** Where a barline sits in its measure; a [MIDDLE] barline also has a measure-local onset. */
public enum class BarlineLocation { LEFT, RIGHT, MIDDLE }

/** Printed barline styles named by MusicXML 4.0. */
public enum class BarStyle {
    REGULAR, DOTTED, DASHED, HEAVY, LIGHT_LIGHT, LIGHT_HEAVY, HEAVY_LIGHT, HEAVY_HEAVY, TICK, SHORT, NONE,
}

/** A forward repeat starts a repeated section; a backward repeat ends it. */
public enum class RepeatDirection { FORWARD, BACKWARD }

/**
 * A printed repeat sign. [times] is the total number of times a backward-repeated section is
 * played; null means unspecified (conventionally twice, or as many passes as its endings name).
 */
public data class Repeat(val direction: RepeatDirection, val times: Int? = null) {
    init {
        require(times == null || times >= 0)
    }
}

/** An ending bracket start, a closed stop with a downward jog, or an open (discontinued) stop. */
public enum class EndingType { START, STOP, DISCONTINUE }

/**
 * An ending mark exactly as notated on one barline. [numbers] are the passes the ending is played
 * on, in source order; an empty list means no pass number was given. [text] is printed text that
 * differs from the numbers, if any. Marks are paired into part-level [Ending]s. [visual] is the
 * optional position, colour and font of the bracket and its text.
 */
public data class EndingMark(
    val type: EndingType,
    val numbers: List<Int>,
    val text: String? = null,
    val visual: VisualHints? = null,
) {
    init {
        require(numbers.all { it > 0 })
    }
}

/**
 * A notated barline: style, repeat sign and ending mark are kept as printed, separately from any
 * playback order derived from them. [onset] is the measure-local quarter-note position of a
 * [BarlineLocation.MIDDLE] barline and null otherwise. [styleVisual] is the optional colour of the
 * bar style.
 */
public data class Barline(
    val location: BarlineLocation = BarlineLocation.RIGHT,
    val onset: Rational? = null,
    val style: BarStyle? = null,
    val repeat: Repeat? = null,
    val ending: EndingMark? = null,
    val styleVisual: VisualHints? = null,
) {
    init {
        require((location == BarlineLocation.MIDDLE) == (onset != null))
        require(onset == null || onset >= Rational.ZERO)
    }
}

/**
 * A resolved ending spanning whole measures, from [startMeasureId] through [stopMeasureId] within
 * its part. A null [stopMeasureId] preserves an ending that was never stopped. [discontinued]
 * means it ends without a downward jog (typically a final ending).
 */
public data class Ending(
    val id: String,
    val numbers: List<Int>,
    val startMeasureId: String,
    val stopMeasureId: String?,
    val text: String? = null,
    val discontinued: Boolean = false,
) {
    init {
        require(id.isNotBlank())
        require(numbers.all { it > 0 })
    }
}

/**
 * A measure shown with a measure-repeat sign repeating a pattern of [measures] measures. This is
 * notation only: the measure still contains the music it sounds, so playback is unchanged.
 */
public data class MeasureRepeat(val measures: Int, val slashes: Int = 1) {
    init {
        require(measures > 0 && slashes > 0)
    }
}
