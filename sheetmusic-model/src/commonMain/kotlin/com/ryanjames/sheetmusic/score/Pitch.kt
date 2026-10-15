package com.ryanjames.sheetmusic.score

/** Written diatonic pitch step, independent of accidental display. */
public enum class Step { C, D, E, F, G, A, B }

/** Scientific pitch spelling; [alter] is an exact semitone displacement, including microtones. */
public data class WrittenPitch(
    val step: Step,
    val octave: Int,
    val alter: Rational = Rational.ZERO,
)

/** A sounding pitch measured in semitones, with MIDI middle C = 60; fractional values are legal. */
public data class SoundingPitch(val semitones: Rational)

/** Pitched and unpitched notes remain distinct; missing sounding pitch means unknown, not unison. */
public sealed interface NotePitch {
    /** Written spelling is preserved even when the instrument transposes. */
    public data class Pitched(val written: WrittenPitch, val sounding: SoundingPitch? = null) : NotePitch

    /** Staff position for percussion display; this is never a sounding pitch. */
    public data class Unpitched(val displayStep: Step? = null, val displayOctave: Int? = null) : NotePitch
}

/** Written-to-sounding displacement; diatonic steps and chromatic semitones are independent. */
public data class Transposition(
    val chromatic: Rational,
    val diatonic: Int? = null,
    val octaveChange: Int = 0,
)

/** Supported notated base values; performed duration is stored separately on each event. */
public enum class NoteValue { MAXIMA, LONG, BREVE, WHOLE, HALF, QUARTER, EIGHTH, N16, N32, N64, N128, N256, N512, N1024 }

/** Actual versus normal note counts; normal note value/dots may differ from the printed value. */
public data class TimeModification(
    val actualNotes: Int,
    val normalNotes: Int,
    val normalValue: NoteValue? = null,
    val normalDots: Int = 0,
) {
    init {
        require(actualNotes > 0 && normalNotes > 0)
        require(normalDots >= 0)
    }
}

/** Printed accidental name and cautionary/editorial flags, independent of [WrittenPitch.alter]. */
public data class Accidental(val name: String, val cautionary: Boolean = false, val editorial: Boolean = false)

/** Notation without inferred playback; absent [value] means unspecified, rather than quarter. */
public data class NotatedValue(
    val value: NoteValue? = null,
    val dots: Int = 0,
    val timeModification: TimeModification? = null,
    val accidental: Accidental? = null,
) {
    init {
        require(dots >= 0)
    }
}
