package com.ryanjames.sheetmusic.score

/** Printed side of the staff for a mark. This is presentation only; null means unspecified. */
public enum class Placement { ABOVE, BELOW }

/**
 * A printed expression mark attached to one note or rest. The mark type is its semantic component;
 * [placement] and [visual] (optional position, colour and font) are its separate visual components
 * and never change the meaning.
 */
public sealed interface NoteExpression {
    public val placement: Placement?
    public val visual: VisualHints?

    /** An articulation such as staccato or accent. */
    public data class Articulation(
        val type: ArticulationType,
        override val placement: Placement? = null,
        override val visual: VisualHints? = null,
    ) : NoteExpression

    /** An ornament such as a trill mark or mordent; its realized playback is not inferred. */
    public data class Ornament(
        val type: OrnamentType,
        override val placement: Placement? = null,
        override val visual: VisualHints? = null,
    ) : NoteExpression
}

/** Articulations whose meaning is fully given by their type. */
public enum class ArticulationType {
    ACCENT, STRONG_ACCENT, STACCATO, TENUTO, DETACHED_LEGATO, STACCATISSIMO, SPICCATO, STRESS, UNSTRESS, SOFT_ACCENT,
}

/** Ornaments whose meaning is given by their type; realization details are not modeled. */
public enum class OrnamentType {
    TRILL_MARK, TURN, DELAYED_TURN, INVERTED_TURN, DELAYED_INVERTED_TURN, VERTICAL_TURN, INVERTED_VERTICAL_TURN,
    SHAKE, MORDENT, INVERTED_MORDENT, HAYDN, SCHLEIFER,
}

/**
 * A printed dynamic, either on a note or in a direction. [markings] are kept in source order, so
 * `sf` followed by `p` is an sfp; a nonstandard dynamic keeps its text verbatim. No playback
 * velocity is inferred from the markings. [visual] is optional position, colour and text formatting.
 */
public data class Dynamics(
    val markings: List<String>,
    override val placement: Placement? = null,
    override val visual: VisualHints? = null,
) : NoteExpression, DirectionMark {
    init {
        require(markings.isNotEmpty())
    }
}

/** One printed component of a direction, in source order, with its optional position, colour and formatting. */
public sealed interface DirectionMark {
    public val visual: VisualHints?

    /**
     * Free text such as "dolce" or "rit."; whitespace, including the line breaks of multi-line text,
     * is preserved. Its font, alignment and enclosure are part of [visual].
     */
    public data class Words(val text: String, override val visual: VisualHints? = null) : DirectionMark

    /**
     * A printed metronome mark: [beatUnit] with [beatUnitDots] equals [perMinute], which is kept as
     * text because it may be a range or approximation such as "c. 120". Playback tempo is separate.
     * [visible] is false for a mark that is not printed.
     */
    public data class Metronome(
        val beatUnit: NoteValue,
        val perMinute: String,
        val beatUnitDots: Int = 0,
        override val visual: VisualHints? = null,
        val visible: Boolean = true,
    ) : DirectionMark {
        init {
            require(beatUnitDots >= 0)
        }
    }

    /** A hairpin endpoint; the start and stop directions are linked by a `WEDGE` relationship. */
    public data class Wedge(val type: WedgeType, override val visual: VisualHints? = null) : DirectionMark
}

/** A hairpin opening ([CRESCENDO]) or closing ([DIMINUENDO]) at its start, a [CONTINUE] point or its [STOP]. */
public enum class WedgeType { CRESCENDO, DIMINUENDO, CONTINUE, STOP }

/**
 * Playback semantics of a direction, independent of what is printed. [tempo] is in quarter notes
 * per minute and [dynamics] a percentage of a forte velocity. [offset] is the playback displacement
 * from the direction's onset in quarter notes, which is zero unless the source applies it to sound.
 */
public data class DirectionSound(
    val tempo: Rational? = null,
    val dynamics: Rational? = null,
    val offset: Rational = Rational.ZERO,
) {
    init {
        require(tempo == null || tempo >= Rational.ZERO)
        require(dynamics == null || dynamics >= Rational.ZERO)
    }
}

/**
 * Presentation of a direction: its [placement], whether it is a [directive] such as a tempo
 * heading aligned with the time signature, and its visual [offset] from the onset in quarter notes.
 */
public data class DirectionDisplay(
    val placement: Placement? = null,
    val directive: Boolean = false,
    val offset: Rational = Rational.ZERO,
)
