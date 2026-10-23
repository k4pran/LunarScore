package com.ryanjames.sheetmusic.score

/** One measure played in order: its index and ID in the part, and the one-based repeat [pass]. */
public data class PlaybackMeasure(val measureIndex: Int, val measureId: String, val pass: Int) {
    init {
        require(measureIndex >= 0 && pass > 0)
    }
}

/** Notation that playback navigation could not follow as written; nothing in the score is changed. */
public enum class PlaybackDiagnosticCode {
    /** A forward repeat not on a left barline, or a backward repeat not on a right barline; it is ignored. */
    MISPLACED_REPEAT,
    /** An ending with no stop; it is treated as covering only its start measure. */
    OPEN_ENDING,
    /** An ending without pass numbers; it is played on every pass. */
    UNNUMBERED_ENDING,
    /** An ending with an unknown or reversed measure range, or overlapping another ending; it is ignored. */
    INVALID_ENDING,
    /** The playback order reached its length limit and was truncated. */
    LENGTH_LIMIT,
}

/** A navigation finding at a zero-based measure index of the part. */
public data class PlaybackDiagnostic(val code: PlaybackDiagnosticCode, val measureIndex: Int)

/** Measures in playback order, derived separately from the notated structure, plus findings. */
public data class PlaybackOrder(val measures: List<PlaybackMeasure>, val diagnostics: List<PlaybackDiagnostic>)

/**
 * Unfolds this part's repeats and endings into playback order, leaving the notation untouched.
 *
 * A backward repeat jumps back to the most recent forward repeat reached in order, or else to the
 * measure after the previous completed repeat, or the first measure (a pickup is replayed). Outside
 * an ending the section is played [Repeat.times] times (twice when unspecified). Within an ending
 * the repeat is taken while the pass is below the highest number in its group of consecutive
 * endings, and `times` is ignored. An ending is skipped on passes it does not name; leaving an
 * ending that does not repeat completes the section. Da capo, dal segno, coda and fine are not
 * interpreted. At most [maxMeasures] measures are returned.
 */
public fun Part.playbackOrder(maxMeasures: Int = 10_000): PlaybackOrder {
    require(maxMeasures >= 0)
    val diagnostics = mutableListOf<PlaybackDiagnostic>()
    val forward = BooleanArray(measures.size)
    val backward = arrayOfNulls<Repeat>(measures.size)
    measures.forEachIndexed { index, measure ->
        for (barline in measure.barlines) {
            val repeat = barline.repeat ?: continue
            when {
                repeat.direction == RepeatDirection.FORWARD && barline.location == BarlineLocation.LEFT -> forward[index] = true
                repeat.direction == RepeatDirection.BACKWARD && barline.location == BarlineLocation.RIGHT -> backward[index] = repeat
                else -> diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.MISPLACED_REPEAT, index)
            }
        }
    }
    val ranges = endingRanges(diagnostics)
    val startingAt = ranges.associateBy { it.start }
    val containing = arrayOfNulls<EndingRange>(measures.size)
    ranges.forEach { range -> (range.start..range.stop).forEach { containing[it] = range } }

    val order = mutableListOf<PlaybackMeasure>()
    val jumps = mutableMapOf<Int, Int>()
    var index = 0
    var pass = 1
    var repeatStart = 0
    var jumped = false
    while (index < measures.size) {
        if (!jumped && forward[index]) {
            repeatStart = index
            pass = 1
        }
        jumped = false
        val starting = startingAt[index]
        if (starting != null && starting.numbers.isNotEmpty() && pass !in starting.numbers) {
            index = starting.stop + 1
            continue
        }
        if (order.size == maxMeasures) {
            diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.LENGTH_LIMIT, index)
            break
        }
        order += PlaybackMeasure(index, measures[index].id, pass)
        val ending = containing[index]?.takeIf { it.groupMax > 0 }
        val repeat = backward[index]
        if (repeat != null) {
            val done = jumps[index] ?: 0
            val again = if (ending != null) pass < ending.groupMax else done < (repeat.times ?: 2) - 1
            if (again) {
                if (ending == null) jumps[index] = done + 1
                pass++
                index = repeatStart
                jumped = true
                continue
            }
            jumps.remove(index)
            repeatStart = index + 1
            pass = 1
        } else if (ending != null && ending.stop == index) {
            repeatStart = index + 1
            pass = 1
        }
        index++
    }
    return PlaybackOrder(order, diagnostics)
}

/** An ending's inclusive measure-index range; [groupMax] is the highest number in its consecutive group. */
private class EndingRange(val start: Int, val stop: Int, val numbers: List<Int>) {
    var groupMax = 0
}

private fun Part.endingRanges(diagnostics: MutableList<PlaybackDiagnostic>): List<EndingRange> {
    val indices = mutableMapOf<String, Int>()
    measures.forEachIndexed { index, measure -> indices.getOrPut(measure.id) { index } }
    val ranges = mutableListOf<EndingRange>()
    for (ending in endings) {
        val start = indices[ending.startMeasureId]
        if (start == null) {
            diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.INVALID_ENDING, 0)
            continue
        }
        val stop = if (ending.stopMeasureId == null) {
            diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.OPEN_ENDING, start)
            start
        } else {
            indices[ending.stopMeasureId]
        }
        if (stop == null || stop < start || ranges.any { start <= it.stop && it.start <= stop }) {
            diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.INVALID_ENDING, start)
            continue
        }
        if (ending.numbers.isEmpty()) diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.UNNUMBERED_ENDING, start)
        ranges += EndingRange(start, stop, ending.numbers)
    }
    val sorted = ranges.sortedBy { it.start }
    var groupStart = 0
    sorted.indices.forEach { index ->
        if (index + 1 == sorted.size || sorted[index + 1].start != sorted[index].stop + 1) {
            val group = sorted.subList(groupStart, index + 1)
            val max = group.maxOf { range -> range.numbers.maxOrNull() ?: 0 }
            group.forEach { it.groupMax = max }
            groupStart = index + 1
        }
    }
    return ranges
}
