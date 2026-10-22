package com.ryanjames.sheetmusic.score

/**
 * Staff attribute state in effect at an event. A null field means no such attribute has been
 * seen on that staff yet; it is unknown and never defaulted (for example to treble clef or 4/4).
 */
public data class AttributeState(
    val time: Attribute.TimeSignature? = null,
    val key: Attribute.KeySignature? = null,
    val clef: Attribute.Clef? = null,
    val staffLines: Attribute.StaffLines? = null,
    val transpose: Attribute.Transpose? = null,
    val instrument: Attribute.InstrumentChange? = null,
) {
    /** This state with [attribute] applied, replacing the previous value of the same domain. */
    public fun with(attribute: Attribute): AttributeState = when (attribute) {
        is Attribute.TimeSignature -> copy(time = attribute)
        is Attribute.KeySignature -> copy(key = attribute)
        is Attribute.Clef -> copy(clef = attribute)
        is Attribute.StaffLines -> copy(staffLines = attribute)
        is Attribute.Transpose -> copy(transpose = attribute)
        is Attribute.InstrumentChange -> copy(instrument = attribute)
    }
}

/**
 * Resolves the attribute state in effect for every event of this part, keyed by event ID.
 *
 * State is tracked per staff and persists across measures in order. Within a measure, events are
 * applied in onset order; events at the same onset keep their list order, so an attribute change
 * listed before a note at the same offset applies to that note. An [AttributeEvent]'s own entry
 * includes its change. Events on a staff with no preceding attributes get an empty state.
 */
public fun Part.effectiveStates(): Map<String, AttributeState> {
    val states = mutableMapOf<Int, AttributeState>()
    val result = linkedMapOf<String, AttributeState>()
    for (measure in measures) {
        for (event in measure.events.sortedBy { it.onset }) {
            val current = states[event.staff] ?: AttributeState()
            val state = if (event is AttributeEvent) current.with(event.attribute) else current
            states[event.staff] = state
            result[event.id] = state
        }
    }
    return result
}
