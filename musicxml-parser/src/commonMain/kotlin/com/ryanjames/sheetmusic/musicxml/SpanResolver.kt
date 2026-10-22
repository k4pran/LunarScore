package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Relationship
import com.ryanjames.sheetmusic.score.RelationshipKind

/** A part-local musical position: one-based measure index, then measure-local onset. */
internal data class SpanPosition(val measure: Int, val onset: Rational) : Comparable<SpanPosition> {
    override fun compareTo(other: SpanPosition): Int =
        compareValuesBy(this, other, { it.measure }, { it.onset })
}

/** One source endpoint on an imported event. [key] pairs endpoints: pitch for ties, number for slurs. */
internal data class SpanEndpoint(
    val kind: RelationshipKind,
    val key: String,
    val eventId: String,
    val voice: String,
    val position: SpanPosition,
    val location: SourceLocation,
)

/** A resolved or unresolved span with the source locations of its endpoints, for provenance. */
internal class ResolvedSpan(val relationship: Relationship, val startLocation: SourceLocation?, val endLocation: SourceLocation?)

/**
 * Pairs start/stop endpoints of one part's sound ties, notated ties and slurs, each kind
 * independently. A stop takes the earliest open start with its kind and key, preferring the same
 * voice. A stop with no open start waits for a later start whose position is musically earlier,
 * since voices can put a stop before its start in document order. A second start with the same
 * kind, key and voice as an open start leaves the earlier one unmatched. Every unmatched endpoint is
 * reported through [unmatched] and kept as a relationship with a null partner.
 */
internal class SpanResolver(
    private val partId: String,
    private val unmatched: (SourceLocation, String) -> Unit,
) {
    private val spans = mutableListOf<PendingSpan>()
    private val openStarts = mutableListOf<PendingSpan>()
    private val waitingStops = mutableListOf<PendingSpan>()
    private val counters = mutableMapOf<RelationshipKind, Int>()

    fun start(endpoint: SpanEndpoint) {
        val stop = waitingStops.preferVoice(endpoint) { it.end!!.position > endpoint.position }
        if (stop != null) {
            waitingStops -= stop
            stop.start = endpoint
            return
        }
        openStarts.firstOrNull { it.start!!.matches(endpoint) && it.start!!.voice == endpoint.voice }?.let { duplicate ->
            openStarts -= duplicate
            duplicate.reported = true
            unmatched(duplicate.start!!.location, "${label(endpoint)} start has no stop before the next start of the same span")
        }
        openStarts += PendingSpan(endpoint.kind, start = endpoint).also { spans += it }
    }

    fun stop(endpoint: SpanEndpoint) {
        val start = openStarts.preferVoice(endpoint) { true }
        if (start != null) {
            openStarts -= start
            start.end = endpoint
        } else {
            waitingStops += PendingSpan(endpoint.kind, end = endpoint).also { spans += it }
        }
    }

    /** Checks a mid-span point such as a slur `continue`; it is not an endpoint. */
    fun continuePoint(endpoint: SpanEndpoint) {
        if (openStarts.none { it.start!!.matches(endpoint) }) {
            unmatched(endpoint.location, "${label(endpoint)} continue has no open start")
        }
    }

    /** Reports the remaining unmatched endpoints in source order and returns all spans. */
    fun finish(): List<ResolvedSpan> {
        spans.filterNot { it.reported }.forEach { span ->
            when {
                span.start == null -> unmatched(span.end!!.location, "${label(span.end!!)} stop has no start")
                span.end == null -> unmatched(span.start!!.location, "${label(span.start!!)} start has no stop")
            }
        }
        return spans.map { span ->
            val number = counters.getOrElse(span.kind) { 0 } + 1
            counters[span.kind] = number
            ResolvedSpan(
                Relationship("$partId:${SLUGS.getValue(span.kind)}-$number", span.kind, span.start?.eventId, span.end?.eventId),
                span.start?.location,
                span.end?.location,
            )
        }
    }

    private fun MutableList<PendingSpan>.preferVoice(endpoint: SpanEndpoint, accept: (PendingSpan) -> Boolean): PendingSpan? {
        val candidates = filter { (it.start ?: it.end)!!.matches(endpoint) && accept(it) }
        return candidates.firstOrNull { (it.start ?: it.end)!!.voice == endpoint.voice } ?: candidates.firstOrNull()
    }

    private fun SpanEndpoint.matches(other: SpanEndpoint) = kind == other.kind && key == other.key

    private fun label(endpoint: SpanEndpoint) = "${LABELS.getValue(endpoint.kind)} ${endpoint.key}"

    private class PendingSpan(val kind: RelationshipKind, var start: SpanEndpoint? = null, var end: SpanEndpoint? = null) {
        var reported = false
    }

    private companion object {
        val SLUGS = mapOf(
            RelationshipKind.SOUND_TIE to "sound-tie",
            RelationshipKind.NOTATED_TIE to "notated-tie",
            RelationshipKind.SLUR to "slur",
        )
        val LABELS = mapOf(
            RelationshipKind.SOUND_TIE to "Sound tie on",
            RelationshipKind.NOTATED_TIE to "Notated tie on",
            RelationshipKind.SLUR to "Slur",
        )
    }
}
