package com.ryanjames.sheetmusic.score

/** A measure-local event at an exact quarter-note [onset], with score-global identity. */
public sealed interface Event {
    public val id: String
    public val onset: Rational
    public val staff: Int
}

/** Ordinary/cue notes advance musical time; grace notes have no duration in the main timeline. */
public enum class NoteRole { NORMAL, CUE, GRACE }

/**
 * Grace-note presentation and playback hints. [slash] is a printed slash (acciaccatura).
 * [stealTimePrevious] and [stealTimeFollowing] are the percentages (0–100) of the previous or
 * following note's time taken by the grace note in playback; null means unspecified. A grace note
 * is linked to its principal note by a [RelationshipKind.GRACE_NOTE] relationship.
 */
public data class Grace(
    val slash: Boolean = false,
    val stealTimePrevious: Rational? = null,
    val stealTimeFollowing: Rational? = null,
) {
    init {
        require(stealTimePrevious == null || stealTimePrevious in Rational.ZERO..Rational.of(100))
        require(stealTimeFollowing == null || stealTimeFollowing in Rational.ZERO..Rational.of(100))
    }
}

/** Performance offsets from the nominal start/end in quarter notes; never inferred from notation. */
public data class Performance(val attack: Rational = Rational.ZERO, val release: Rational = Rational.ZERO)

/**
 * One notehead, including each chord tone independently. [duration] is performed musical time in
 * quarter notes (null only for grace), separate from [notation]. Shared [chordId] groups noteheads
 * without forcing equal durations or staves. Instrument IDs resolve within the containing part.
 * [grace] details are only allowed on a [NoteRole.GRACE] note. [visual] is optional position and colour.
 */
public data class NoteEvent(
    override val id: String,
    override val onset: Rational,
    val duration: Rational?,
    val pitch: NotePitch,
    override val staff: Int = 1,
    val voice: String = "1",
    val notation: NotatedValue = NotatedValue(),
    val role: NoteRole = NoteRole.NORMAL,
    val chordId: String? = null,
    val instrumentId: String? = null,
    val performance: Performance = Performance(),
    val grace: Grace? = null,
    val visual: VisualHints? = null,
) : Event {
    init {
        validateEvent(id, onset, staff)
        require(voice.isNotBlank())
        require(if (role == NoteRole.GRACE) duration == null else duration != null && duration > Rational.ZERO)
        require(grace == null || role == NoteRole.GRACE)
    }
}

/**
 * A timed rest; a gap/forward movement is represented by the absence of events, not a fake rest.
 * [visual] is optional position and colour.
 */
public data class RestEvent(
    override val id: String,
    override val onset: Rational,
    val duration: Rational,
    override val staff: Int = 1,
    val voice: String = "1",
    val notation: NotatedValue = NotatedValue(),
    val wholeMeasure: Boolean = false,
    val visual: VisualHints? = null,
) : Event {
    init {
        validateEvent(id, onset, staff)
        require(voice.isNotBlank())
        require(duration > Rational.ZERO)
    }
}

/** A staff-scoped state change, applied at its exact offset rather than just at measure start. */
public data class AttributeEvent(
    override val id: String,
    override val onset: Rational,
    val attribute: Attribute,
    override val staff: Int = 1,
) : Event {
    init {
        validateEvent(id, onset, staff)
    }
}

/**
 * A direction such as words, a dynamic, a metronome mark or a hairpin endpoint at an exact
 * measure-local [onset] on one staff. [marks] are the printed components in source order; [sound]
 * holds separate playback semantics and [display] presentation only. A direction may have no
 * printed marks when it only carries sound. [voice] is null when the direction applies to all voices.
 */
public data class DirectionEvent(
    override val id: String,
    override val onset: Rational,
    val marks: List<DirectionMark>,
    override val staff: Int = 1,
    val voice: String? = null,
    val sound: DirectionSound? = null,
    val display: DirectionDisplay = DirectionDisplay(),
) : Event {
    init {
        validateEvent(id, onset, staff)
        require(voice == null || voice.isNotBlank())
        require(marks.isNotEmpty() || sound != null)
    }
}

/** Initial neutral state vocabulary; additional domains can be added without XML/UI types. */
public sealed interface Attribute {
    /**
     * A meter of one or more [fractions] in display order, such as 3/4, (3+2)/8 or 3/8+2/8+3/4, or
     * an explicit absence of meter when [senzaMisura] is set (then [fractions] is empty).
     *
     * [symbol] and [separator] are how the signature is displayed and never change the meter; null
     * means none was given. [interchangeable] is a displayed alternate of the same meter, such as
     * the 6/8 in 3/4 (6/8); it never changes [length].
     */
    public data class TimeSignature(
        val fractions: List<TimeFraction>,
        val symbol: TimeSymbol? = null,
        val separator: TimeSeparator? = null,
        val interchangeable: InterchangeableTime? = null,
        val senzaMisura: SenzaMisura? = null,
    ) : Attribute {
        /** A simple meter of [beats] [beatUnit] notes, such as 3/4. */
        public constructor(beats: Int, beatUnit: Int, symbol: TimeSymbol? = null) :
            this(listOf(TimeFraction(listOf(beats), beatUnit)), symbol)

        /**
         * The total measure length in quarter notes, summed over every fraction, or null without a
         * meter (senza misura). Construction fails with [RationalException] if it overflows.
         */
        val length: Rational? = if (senzaMisura == null) fractions.fold(Rational.ZERO) { sum, it -> sum + it.length } else null

        init {
            require(fractions.isEmpty() == (senzaMisura != null))
            require(senzaMisura == null || interchangeable == null)
        }
    }

    /**
     * Clef sign and staff line counted from the bottom; octaveChange is in octaves. A null [line]
     * means no line was given for a sign without a standard line (for example percussion).
     */
    public data class Clef(val sign: ClefSign, val line: Int?, val octaveChange: Int = 0) : Attribute

    /**
     * A key signature, either traditional or non-traditional. Keys never change a note's written
     * alter.
     *
     * A traditional key has [fifths], counting sharps (positive) or flats (negative) around the
     * circle of fifths, an optional [mode] (null is unspecified) and an optional [cancel] of the
     * previous key. A non-traditional key has no fifths and lists its [alterations] in display
     * order. [octaves] optionally place individual accidentals of either form; they are display
     * data only.
     */
    public data class KeySignature(
        val fifths: Int?,
        val mode: KeyMode? = null,
        val cancel: KeyCancel? = null,
        val alterations: List<KeyAlteration> = emptyList(),
        val octaves: List<KeyOctave> = emptyList(),
    ) : Attribute {
        init {
            require((fifths == null) != alterations.isEmpty())
            require(fifths != null || (mode == null && cancel == null))
        }

        /** Whether this key lists its own [alterations] rather than a number of [fifths]. */
        val isNonTraditional: Boolean get() = fifths == null

        public companion object {
            /** A non-traditional key of [alterations] in display order, with optional [octaves]. */
            public fun nonTraditional(alterations: List<KeyAlteration>, octaves: List<KeyOctave> = emptyList()): KeySignature =
                KeySignature(null, alterations = alterations, octaves = octaves)
        }
    }

    /** Number of staff lines in effect; zero is legal. */
    public data class StaffLines(val count: Int) : Attribute {
        init {
            require(count >= 0)
        }
    }

    /** Written-to-sounding displacement effective on this staff. */
    public data class Transpose(val transposition: Transposition) : Attribute

    /** Instrument reference resolved within the containing part. */
    public data class InstrumentChange(val instrumentId: String) : Attribute
}

/**
 * Time signature display: common (C) and cut (alla breve) symbols, numerator only, a note in place
 * of the denominator (plain or dotted), or the usual pair of numbers.
 */
public enum class TimeSymbol { COMMON, CUT, SINGLE_NUMBER, NOTE, DOTTED_NOTE, NORMAL }

/**
 * One fraction of a time signature: the additive numerator [beats] (one entry for 3/8, three for
 * (5+3+1)/4) over [beatType].
 */
public data class TimeFraction(val beats: List<Int>, val beatType: Int) {
    init {
        require(beats.isNotEmpty() && beats.all { it > 0 } && beatType > 0)
    }

    /** This fraction's length in quarter notes. Fails with [RationalException] if it overflows. */
    val length: Rational get() = beats.fold(Rational.ZERO) { sum, beat -> sum + Rational.of(beat) } * Rational.of(4, beatType)
}

/** How the fractions of a compound time signature are separated when displayed. */
public enum class TimeSeparator { NONE, HORIZONTAL, DIAGONAL, VERTICAL, ADJACENT }

/** How an interchangeable alternate is displayed beside the main time signature. */
public enum class TimeRelation { PARENTHESES, BRACKET, EQUALS, SLASH, SPACE, HYPHEN }

/**
 * A displayed alternate meter, such as the 6/8 in 3/4 (6/8), with its own display [symbol] and
 * [separator]; null means none was given.
 */
public data class InterchangeableTime(
    val fractions: List<TimeFraction>,
    val relation: TimeRelation? = null,
    val symbol: TimeSymbol? = null,
    val separator: TimeSeparator? = null,
) {
    init {
        require(fractions.isNotEmpty())
    }
}

/**
 * An explicit absence of meter. [symbol] is the text displayed in place of a time signature, such
 * as `X`; null means nothing is displayed.
 */
public data class SenzaMisura(val symbol: String? = null) {
    init {
        require(symbol == null || symbol.isNotBlank())
    }
}

/** Conventional and non-pitched clef signs. */
public enum class ClefSign { G, F, C, PERCUSSION, TAB, JIANPU, NONE }

/** Key modes named by MusicXML 4.0; [NONE] means explicitly modeless. */
public enum class KeyMode { MAJOR, MINOR, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, AEOLIAN, IONIAN, LOCRIAN, NONE }

/**
 * One accidental of a non-traditional key: [step] altered by [alter] semitones (exact, including
 * microtones), with an optional printed [accidental] name such as `slash-flat`.
 */
public data class KeyAlteration(val step: Step, val alter: Rational, val accidental: String? = null) {
    init {
        require(accidental == null || accidental.isNotBlank())
    }
}

/**
 * Display of the naturals cancelling the previous key's [fifths] before a key change; a null
 * [location] is unspecified (MusicXML's default is [KeyCancelLocation.LEFT]).
 */
public data class KeyCancel(val fifths: Int, val location: KeyCancelLocation? = null)

/** Where key cancellation naturals are displayed relative to the new key. */
public enum class KeyCancelLocation { LEFT, RIGHT, BEFORE_BARLINE }

/**
 * The displayed [octave] (0–9) of one key accidental, where [number] counts accidentals from 1 in
 * display order; with [cancel] it counts the cancellation naturals instead.
 */
public data class KeyOctave(val number: Int, val octave: Int, val cancel: Boolean = false) {
    init {
        require(number > 0)
        require(octave in 0..9)
    }
}

private fun validateEvent(id: String, onset: Rational, staff: Int) {
    require(id.isNotBlank())
    require(onset >= Rational.ZERO)
    require(staff > 0)
}
