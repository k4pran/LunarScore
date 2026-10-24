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
    /** An ending without pass numbers; it stands for the pass after the earlier endings in its group. */
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
 * ending that does not repeat completes the section. A forward repeat inside an ending, including
 * one at its first measure, starts a nested section that is played `times` times on each pass
 * through the ending; when it completes within the ending, the enclosing section and its pass
 * resume, so a backward repeat closing the ending returns to the enclosing section's start. When it
 * completes after its ending, the enclosing section restarts after it, as after any completed
 * repeat. An ending without numbers stands for the pass after the highest number of the earlier
 * endings in its group (so a first unnumbered ending is pass 1 and a second is pass 2). Da capo,
 * dal segno, coda and fine are not interpreted. At most [maxMeasures] measures are returned.
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
    // The innermost section is last; sections nested inside an ending sit above the one it belongs to.
    val sections = mutableListOf(Section(0, null))
    var index = 0
    var jumped = false
    while (index < measures.size) {
        val starting = startingAt[index]
        if (starting != null && sections.last().ending !== starting && sections.last().pass !in starting.numbers) {
            index = starting.stop + 1
            continue
        }
        if (!jumped && forward[index]) {
            val enclosing = containing[index]
            if (enclosing == null) sections[sections.lastIndex] = Section(index, null) else sections += Section(index, enclosing)
        }
        jumped = false
        if (order.size == maxMeasures) {
            diagnostics += PlaybackDiagnostic(PlaybackDiagnosticCode.LENGTH_LIMIT, index)
            break
        }
        val section = sections.last()
        order += PlaybackMeasure(index, measures[index].id, section.pass)
        val repeat = backward[index]
        if (repeat != null) {
            val ending = containing[index]?.takeIf { it !== section.ending }
            val done = jumps[index] ?: 0
            val again = if (ending != null) section.pass < ending.groupMax else done < (repeat.times ?: 2) - 1
            if (again) {
                if (ending == null) jumps[index] = done + 1
                section.pass++
                index = section.start
                jumped = true
                continue
            }
            jumps.remove(index)
            sections.removeAt(sections.lastIndex)
            if (section.ending == null) {
                sections += Section(index + 1, null)
            } else if (containing[index] !== section.ending) {
                // A nested repeat that finished past its ending completes the enclosing stretch up to here.
                sections[sections.lastIndex] = Section(index + 1, sections.last().ending)
            }
        }
        val closing = containing[index]?.takeIf { it.stop == index }
        if (closing != null) {
            val owner = sections.indexOfLast { it.ending !== closing }
            sections[owner] = Section(index + 1, sections[owner].ending)
        }
        index++
    }
    return PlaybackOrder(order, diagnostics)
}

/** A repeated stretch from [start], on its current [pass]; [ending] is set when it is nested inside that ending. */
private class Section(val start: Int, val ending: EndingRange?) {
    var pass = 1
}

/** An ending's inclusive measure-index range; [groupMax] is the highest number in its consecutive group. */
private class EndingRange(val start: Int, val stop: Int, var numbers: List<Int>) {
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
            var max = 0
            group.forEach { range ->
                if (range.numbers.isEmpty()) range.numbers = listOf(max + 1)
                max = maxOf(max, range.numbers.max())
            }
            group.forEach { it.groupMax = max }
            groupStart = index + 1
        }
    }
    return ranges
}
