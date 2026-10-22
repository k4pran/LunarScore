package com.ryanjames.sheetmusic.score

/** A measure-local event at an exact quarter-note [onset], with score-global identity. */
public sealed interface Event {
    public val id: String
    public val onset: Rational
    public val staff: Int
}

/** Ordinary/cue notes advance musical time; grace notes have no duration in the main timeline. */
public enum class NoteRole { NORMAL, CUE, GRACE }

/** Performance offsets from the nominal start/end in quarter notes; never inferred from notation. */
public data class Performance(val attack: Rational = Rational.ZERO, val release: Rational = Rational.ZERO)

/**
 * One notehead, including each chord tone independently. [duration] is performed musical time in
 * quarter notes (null only for grace), separate from [notation]. Shared [chordId] groups noteheads
 * without forcing equal durations or staves. Instrument IDs resolve within the containing part.
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
) : Event {
    init {
        validateEvent(id, onset, staff)
        require(voice.isNotBlank())
        require(if (role == NoteRole.GRACE) duration == null else duration != null && duration > Rational.ZERO)
    }
}

/** A timed rest; a gap/forward movement is represented by the absence of events, not a fake rest. */
public data class RestEvent(
    override val id: String,
    override val onset: Rational,
    val duration: Rational,
    override val staff: Int = 1,
    val voice: String = "1",
    val notation: NotatedValue = NotatedValue(),
    val wholeMeasure: Boolean = false,
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

/** Initial neutral state vocabulary; additional domains can be added without XML/UI types. */
public sealed interface Attribute {
    /** A simple meter; additive/interchangeable meters remain future model work. */
    public data class TimeSignature(val beats: Int, val beatUnit: Int) : Attribute {
        init {
            require(beats > 0 && beatUnit > 0)
        }
    }

    /**
     * Clef sign and staff line counted from the bottom; octaveChange is in octaves. A null [line]
     * means no line was given for a sign without a standard line (for example percussion).
     */
    public data class Clef(val sign: ClefSign, val line: Int?, val octaveChange: Int = 0) : Attribute

    /**
     * A traditional key signature: [fifths] counts sharps (positive) or flats (negative) around the
     * circle of fifths. A null [mode] is unspecified. Keys never change a note's written alter.
     */
    public data class KeySignature(val fifths: Int, val mode: KeyMode? = null) : Attribute

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

/** Conventional and non-pitched clef signs. */
public enum class ClefSign { G, F, C, PERCUSSION, TAB, JIANPU, NONE }

/** Key modes named by MusicXML 4.0; [NONE] means explicitly modeless. */
public enum class KeyMode { MAJOR, MINOR, DORIAN, PHRYGIAN, LYDIAN, MIXOLYDIAN, AEOLIAN, IONIAN, LOCRIAN, NONE }

private fun validateEvent(id: String, onset: Rational, staff: Int) {
    require(id.isNotBlank())
    require(onset >= Rational.ZERO)
    require(staff > 0)
}
