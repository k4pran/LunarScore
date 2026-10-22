package com.ryanjames.sheetmusic.score

/** Format-neutral graph problems; parser/source/security diagnostics belong to the importer. */
public enum class ModelDiagnosticCode { DUPLICATE_ID, UNKNOWN_STAFF, UNKNOWN_INSTRUMENT, MISSING_ENDPOINT, UNKNOWN_EVENT }

/** A graph validation finding with stable code, neutral path and offending reference. */
public data class ModelDiagnostic(val code: ModelDiagnosticCode, val path: String, val reference: String?)

/**
 * Check IDs, staff/instrument references and span endpoints without dropping or repairing data.
 * Empty structures are allowed. This does not validate a source schema, meter fullness, matching
 * tie pitches or chord-duration rules; those semantic policies belong to later importer tasks.
 */
public fun Score.validate(): List<ModelDiagnostic> {
    val diagnostics = mutableListOf<ModelDiagnostic>()
    fun register(ids: MutableSet<String>, id: String, path: String) {
        if (!ids.add(id)) diagnostics += ModelDiagnostic(ModelDiagnosticCode.DUPLICATE_ID, path, id)
    }
    val partIds = mutableSetOf<String>()
    val eventIds = mutableSetOf<String>()
    parts.forEachIndexed { partIndex, part ->
        val partPath = "parts[$partIndex]"
        register(partIds, part.id, partPath)
        val staffNumbers = mutableSetOf<String>()
        part.staves.forEachIndexed { index, staff ->
            register(staffNumbers, staff.number.toString(), "$partPath.staves[$index]")
        }
        val instrumentIds = mutableSetOf<String>()
        part.instruments.forEachIndexed { index, instrument ->
            register(instrumentIds, instrument.id, "$partPath.instruments[$index]")
        }
        val measureIds = mutableSetOf<String>()
        part.measures.forEachIndexed { measureIndex, measure ->
            val measurePath = "$partPath.measures[$measureIndex]"
            register(measureIds, measure.id, measurePath)
            measure.events.forEachIndexed { eventIndex, event ->
                val path = "$measurePath.events[$eventIndex]"
                register(eventIds, event.id, path)
                if (event.staff.toString() !in staffNumbers) {
                    diagnostics += ModelDiagnostic(ModelDiagnosticCode.UNKNOWN_STAFF, path, event.staff.toString())
                }
                val instrument = when (event) {
                    is NoteEvent -> event.instrumentId
                    is AttributeEvent -> (event.attribute as? Attribute.InstrumentChange)?.instrumentId
                    is RestEvent, is DirectionEvent -> null
                }
                if (instrument != null && instrument !in instrumentIds) {
                    diagnostics += ModelDiagnostic(ModelDiagnosticCode.UNKNOWN_INSTRUMENT, path, instrument)
                }
            }
        }
    }
    val relationshipIds = mutableSetOf<String>()
    relationships.forEachIndexed { index, relationship ->
        val path = "relationships[$index]"
        register(relationshipIds, relationship.id, path)
        val endpoints = listOf("startEventId" to relationship.startEventId, "endEventId" to relationship.endEventId)
        endpoints.forEach { (endpoint, id) ->
            val code = when {
                id == null -> ModelDiagnosticCode.MISSING_ENDPOINT
                id !in eventIds -> ModelDiagnosticCode.UNKNOWN_EVENT
                else -> null
            }
            if (code != null) diagnostics += ModelDiagnostic(code, "$path.$endpoint", id)
        }
    }
    return diagnostics
}
