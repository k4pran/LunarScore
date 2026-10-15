package com.ryanjames.sheetmusic.model

/** The diatonic step name of a pitch. Accidentals are not supported yet. */
public enum class Pitch { C, D, E, F, G, A, B }

/** A note value. Only stemmed and unstemmed note values that need no flags are supported. */
public enum class Duration { WHOLE, HALF, QUARTER }

/** A clef, defined by the pitch it fixes to one staff line. */
public enum class Clef(
    internal val referencePitch: Pitch,
    internal val referenceOctave: Int,
    /** Staff position of the reference pitch, counted in steps up from the bottom line. */
    internal val referenceStaffPosition: Int,
) {
    /** G clef with G4 on the second line from the bottom. */
    TREBLE(Pitch.G, 4, 2),
}

/**
 * A single note.
 *
 * @param octave octave in scientific pitch notation, where middle C is `C4`.
 */
public data class Note(
    val pitch: Pitch,
    val octave: Int,
    val duration: Duration,
)

public data class Measure(val notes: List<Note>) {
    init {
        require(notes.isNotEmpty()) { "A measure must contain at least one note" }
    }
}

/** A single-staff score. */
public data class Score(
    val measures: List<Measure>,
    val clef: Clef = Clef.TREBLE,
) {
    init {
        require(measures.isNotEmpty()) { "A score must contain at least one measure" }
    }
}
