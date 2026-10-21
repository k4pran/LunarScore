package com.ryanjames.sheetmusic.score

/** Ordered format-neutral score. Event and relationship IDs are unique across the score. */
public data class Score(
    val parts: List<Part> = emptyList(),
    val relationships: List<Relationship> = emptyList(),
    val title: String? = null,
    val metadata: List<MetadataEntry> = emptyList(),
)

/** Ordered measures and staff/instrument definitions; IDs are stable, not display names. */
public data class Part(
    val id: String,
    val staves: List<Staff>,
    val measures: List<Measure> = emptyList(),
    val name: String? = null,
    val instruments: List<Instrument> = emptyList(),
    val abbreviation: String? = null,
    val metadata: List<MetadataEntry.Text> = emptyList(),
) {
    init {
        require(id.isNotBlank())
    }
}

/** Part-local staff number, retained on every event (including cross-staff chord tones). */
public data class Staff(val number: Int, val name: String? = null) {
    init {
        require(number > 0)
    }
}

/** Part-local instrument identity; optional percussion key uses zero-based MIDI numbering, 0–127. */
public data class Instrument(val id: String, val name: String? = null, val percussionKey: Int? = null) {
    init {
        require(id.isNotBlank())
        require(percussionKey == null || percussionKey in 0..127)
    }
}

/**
 * A measure with a stable part-local [id] and a separate display [number] (which need not be numeric).
 * [extent] is the observed time extent in quarter notes, not an inferred meter length. Empty and
 * pickup measures are legal. Event order is retained; it need not be sorted by onset or voice.
 */
public data class Measure(
    val id: String,
    val number: String,
    val extent: Rational,
    val events: List<Event> = emptyList(),
    val implicit: Boolean = false,
) {
    init {
        require(id.isNotBlank())
        require(extent >= Rational.ZERO)
    }
}

/** A cross-event span; null endpoints preserve unresolved spans for explicit validation. */
public data class Relationship(
    val id: String,
    val kind: RelationshipKind,
    val startEventId: String?,
    val endEventId: String?,
) {
    init {
        require(id.isNotBlank())
    }
}

/** Sound ties and printed ties are independent; concurrent/nested spans have separate IDs. */
public enum class RelationshipKind { SOUND_TIE, NOTATED_TIE, SLUR, TUPLET, WEDGE, PEDAL, ENDING }
