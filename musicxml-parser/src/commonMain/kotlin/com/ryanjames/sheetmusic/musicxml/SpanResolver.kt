package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Relationship
import com.ryanjames.sheetmusic.score.RelationshipKind

/** A part-local musical position: one-based measure index, then measure-local onset. */
internal data class SpanPosition(val measure: Int, val onset: Rational) : Comparable<SpanPosition> {
    override fun compareTo(other: SpanPosition): Int =
        compareValuesBy(this, other, { it.measure }, { it.onset })
}

/** One source endpoint on an imported event. [key] pairs endpoints: pitch for ties, number for slurs, tuplets and wedges. */
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
 * Pairs start/stop endpoints of one part's sound ties, notated ties, slurs, tuplets and wedges, each kind
 * independently. A stop takes the earliest open start with its kind and key, preferring the same
 * voice. A stop with no open start waits for a later start whose position is musically earlier,
 * since voices can put a stop before its start in document order. A second start with the same
 * kind, key and voice as an open start leaves the earlier one unmatched. Every unmatched endpoint is
 * reported through [unmatched] and kept as a relationship with a null partner.
 *
 * Each endpoint has a source order, by default its arrival order. Spans are numbered and returned
 * in the source order of their first endpoint, so a caller that passes endpoints out of source order
 * (see [reserveOrders]) keeps stable IDs.
 *
 * Endpoints must arrive in measure order. Pending endpoints are indexed by kind, key and voice so
 * each endpoint resolves in constant or logarithmic time, keeping resolution of untrusted input
 * near-linear.
 */
internal class SpanResolver(
    private val partId: String,
    private val unmatched: (SourceLocation, String) -> Unit,
) {
    private val spans = mutableListOf<PendingSpan>()
    private val openStarts = mutableMapOf<SpanKey, LinkedHashSet<PendingSpan>>()
    private val openStartsByVoice = mutableMapOf<VoiceKey, PendingSpan>()
    private val waitingStops = mutableMapOf<SpanKey, WaitingQueue>()
    private val waitingStopsByVoice = mutableMapOf<VoiceKey, WaitingQueue>()
    private var waitingMeasure = 0
    private val counters = mutableMapOf<RelationshipKind, Int>()
    private var nextOrder = 0

    /** Reserves [count] consecutive source orders for endpoints passed out of source order, returning the first. */
    fun reserveOrders(count: Int): Int = nextOrder.also { nextOrder += count }

    fun start(endpoint: SpanEndpoint, order: Int = nextOrder++) {
        dropStaleStops(endpoint)
        val stop = waitingStopsByVoice[endpoint.voiceKey()]?.firstAfter(endpoint.position)
            ?: waitingStops[endpoint.spanKey()]?.firstAfter(endpoint.position)
        if (stop != null) {
            removeWaitingStop(stop)
            stop.start = endpoint
            stop.order = minOf(stop.order, order)
            return
        }
        openStartsByVoice[endpoint.voiceKey()]?.let { duplicate ->
            removeOpenStart(duplicate)
            duplicate.reported = true
            unmatched(duplicate.start!!.location, "${label(endpoint)} start has no stop before the next start of the same span")
        }
        val span = PendingSpan(endpoint.kind, order, start = endpoint).also { spans += it }
        openStarts.getOrPut(endpoint.spanKey()) { LinkedHashSet() } += span
        openStartsByVoice[endpoint.voiceKey()] = span
    }

    fun stop(endpoint: SpanEndpoint, order: Int = nextOrder++) {
        dropStaleStops(endpoint)
        val start = openStartsByVoice[endpoint.voiceKey()] ?: openStarts[endpoint.spanKey()]?.firstOrNull()
        if (start != null) {
            removeOpenStart(start)
            start.end = endpoint
            start.order = minOf(start.order, order)
        } else {
            val span = PendingSpan(endpoint.kind, order, end = endpoint).also { spans += it }
            span.waitingIndex = waitingStops.getOrPut(endpoint.spanKey()) { WaitingQueue() }.add(span)
            span.waitingVoiceIndex = waitingStopsByVoice.getOrPut(endpoint.voiceKey()) { WaitingQueue() }.add(span)
        }
    }

    /** Records a span whose endpoints are already paired, such as a tuplet on a single note. */
    fun pair(start: SpanEndpoint, end: SpanEndpoint) {
        spans += PendingSpan(start.kind, nextOrder++, start = start, end = end)
    }

    /** Checks a mid-span point such as a slur `continue`; it is not an endpoint. */
    fun continuePoint(endpoint: SpanEndpoint) {
        if (openStarts[endpoint.spanKey()].isNullOrEmpty()) {
            unmatched(endpoint.location, "${label(endpoint)} continue has no open start")
        }
    }

    /** Reports the remaining unmatched endpoints in source order and returns all spans. */
    fun finish(): List<ResolvedSpan> {
        val spans = spans.sortedBy { it.order }
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

    /** No start in a later measure can precede an earlier measure's stop, so those stops stop waiting. */
    private fun dropStaleStops(endpoint: SpanEndpoint) {
        if (endpoint.position.measure > waitingMeasure) {
            waitingMeasure = endpoint.position.measure
            waitingStops.clear()
            waitingStopsByVoice.clear()
        }
    }

    private fun removeOpenStart(span: PendingSpan) {
        val start = span.start!!
        val group = openStarts.getValue(start.spanKey())
        group -= span
        if (group.isEmpty()) openStarts -= start.spanKey()
        if (openStartsByVoice[start.voiceKey()] === span) openStartsByVoice -= start.voiceKey()
    }

    private fun removeWaitingStop(span: PendingSpan) {
        val end = span.end!!
        waitingStops.getValue(end.spanKey()).remove(span.waitingIndex)
        waitingStopsByVoice.getValue(end.voiceKey()).remove(span.waitingVoiceIndex)
    }

    private fun SpanEndpoint.spanKey() = SpanKey(kind, key)

    private fun SpanEndpoint.voiceKey() = VoiceKey(kind, key, voice)

    private fun label(endpoint: SpanEndpoint) = "${LABELS.getValue(endpoint.kind)} ${endpoint.key}"

    private data class SpanKey(val kind: RelationshipKind, val key: String)

    private data class VoiceKey(val kind: RelationshipKind, val key: String, val voice: String)

    private class PendingSpan(val kind: RelationshipKind, var order: Int, var start: SpanEndpoint? = null, var end: SpanEndpoint? = null) {
        var reported = false
        var waitingIndex = -1
        var waitingVoiceIndex = -1
    }

    /**
     * Waiting stops in arrival order, finding the earliest-arrived stop after a position in
     * logarithmic time through a segment tree of the latest stop position over arrival indices.
     * A removed stop leaves an empty leaf.
     */
    private class WaitingQueue {
        private val stops = mutableListOf<PendingSpan?>()
        private var capacity = 1
        private var latest = arrayOfNulls<SpanPosition>(2)

        /** Appends [span] and returns its arrival index. */
        fun add(span: PendingSpan): Int {
            val index = stops.size
            stops += span
            if (index < capacity) {
                set(index, span.end!!.position)
            } else {
                capacity *= 2
                latest = arrayOfNulls(2 * capacity)
                stops.forEachIndexed { i, stop -> latest[capacity + i] = stop?.end?.position }
                for (node in capacity - 1 downTo 1) latest[node] = later(latest[2 * node], latest[2 * node + 1])
            }
            return index
        }

        fun remove(index: Int) {
            stops[index] = null
            set(index, null)
        }

        fun firstAfter(position: SpanPosition): PendingSpan? {
            if (!isAfter(1, position)) return null
            var node = 1
            while (node < capacity) node = if (isAfter(2 * node, position)) 2 * node else 2 * node + 1
            return stops[node - capacity]
        }

        private fun isAfter(node: Int, position: SpanPosition) = latest[node]?.let { it > position } == true

        private fun set(index: Int, position: SpanPosition?) {
            var node = capacity + index
            latest[node] = position
            while (node > 1) {
                node /= 2
                latest[node] = later(latest[2 * node], latest[2 * node + 1])
            }
        }

        private fun later(first: SpanPosition?, second: SpanPosition?) =
            if (first == null || (second != null && second > first)) second else first
    }

    private companion object {
        val SLUGS = mapOf(
            RelationshipKind.SOUND_TIE to "sound-tie",
            RelationshipKind.NOTATED_TIE to "notated-tie",
            RelationshipKind.SLUR to "slur",
            RelationshipKind.TUPLET to "tuplet",
            RelationshipKind.WEDGE to "wedge",
        )
        val LABELS = mapOf(
            RelationshipKind.SOUND_TIE to "Sound tie on",
            RelationshipKind.NOTATED_TIE to "Notated tie on",
            RelationshipKind.SLUR to "Slur",
            RelationshipKind.TUPLET to "Tuplet",
            RelationshipKind.WEDGE to "Wedge",
        )
    }
}
