package com.ryanjames.sheetmusic.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlaybackOrderTest {
    @Test
    fun testUnrepeatedPartPlaysInWrittenOrder() {
        val playback = part(measure(), measure(), measure()).playbackOrder()
        assertEquals(listOf(0, 1, 2), playback.measures.map { it.measureIndex })
        assertEquals(listOf(1, 1, 1), playback.measures.map { it.pass })
        assertTrue(playback.diagnostics.isEmpty())
    }

    @Test
    fun testBackwardRepeatWithoutForwardRepeatReturnsToStartIncludingPickup() {
        val part = part(measure(implicit = true), measure(), measure(backward()), measure())
        assertEquals(listOf(0, 1, 2, 0, 1, 2, 3), indices(part))
    }

    @Test
    fun testRepeatTimesCountsTotalPlays() {
        assertEquals(listOf(0, 1, 1, 1, 2), indices(part(measure(), measure(forward(), backward(3)), measure())))
        assertEquals(listOf(0, 1), indices(part(measure(), measure(backward(1)))))
        assertEquals(listOf(0, 1), indices(part(measure(), measure(backward(0)))))
    }

    @Test
    fun testConsecutiveRepeatsReturnToTheMeasureAfterThePreviousRepeat() {
        assertEquals(listOf(0, 0, 1, 1), indices(part(measure(backward()), measure(backward()))))
    }

    @Test
    fun testEndingsSelectPassesAndFinalEndingCompletesTheSection() {
        // |: A [1,2. B :| [3. C | D
        val part = part(
            measure(forward()),
            measure(backward()),
            measure(),
            measure(),
            endings = listOf(Ending("e1", listOf(1, 2), "m2", "m2"), Ending("e2", listOf(3), "m3", "m3", discontinued = true)),
        )
        val playback = part.playbackOrder()
        assertEquals(listOf(0, 1, 0, 1, 0, 2, 3), playback.measures.map { it.measureIndex })
        assertEquals(listOf(1, 1, 2, 2, 3, 3, 1), playback.measures.map { it.pass })
    }

    @Test
    fun testEachRepeatingEndingReturnsUntilTheLastEnding() {
        // |: A [1. B :| [2. C :| [3. D
        val part = part(
            measure(forward()),
            measure(backward()),
            measure(backward()),
            measure(),
            endings = listOf(
                Ending("e1", listOf(1), "m2", "m2"),
                Ending("e2", listOf(2), "m3", "m3"),
                Ending("e3", listOf(3), "m4", "m4"),
            ),
        )
        assertEquals(listOf(0, 1, 0, 2, 0, 3), indices(part))
    }

    @Test
    fun testMultiMeasureEndingIsSkippedAsAWhole() {
        val part = part(
            measure(),
            measure(),
            measure(backward()),
            measure(),
            endings = listOf(Ending("e1", listOf(1), "m2", "m3"), Ending("e2", listOf(2), "m4", "m4")),
        )
        assertEquals(listOf(0, 1, 2, 0, 3), indices(part))
    }

    @Test
    fun testIgnoredNotationIsDiagnosed() {
        val misplaced = part(measure(Barline(BarlineLocation.RIGHT, repeat = Repeat(RepeatDirection.FORWARD))), measure(Barline(BarlineLocation.LEFT, repeat = Repeat(RepeatDirection.BACKWARD))))
        val playback = misplaced.playbackOrder()
        assertEquals(listOf(0, 1), playback.measures.map { it.measureIndex })
        assertEquals(
            listOf(PlaybackDiagnostic(PlaybackDiagnosticCode.MISPLACED_REPEAT, 0), PlaybackDiagnostic(PlaybackDiagnosticCode.MISPLACED_REPEAT, 1)),
            playback.diagnostics,
        )

        val endings = part(
            measure(),
            measure(),
            measure(),
            endings = listOf(
                Ending("open", listOf(1), "m1", null),
                Ending("reversed", listOf(1), "m3", "m2"),
                Ending("unknown", listOf(1), "m9", "m9"),
                Ending("overlap", listOf(2), "m1", "m2"),
                Ending("blank", emptyList(), "m3", "m3"),
            ),
        )
        assertEquals(
            listOf(
                PlaybackDiagnostic(PlaybackDiagnosticCode.OPEN_ENDING, 0),
                PlaybackDiagnostic(PlaybackDiagnosticCode.INVALID_ENDING, 2),
                PlaybackDiagnostic(PlaybackDiagnosticCode.INVALID_ENDING, 0),
                PlaybackDiagnostic(PlaybackDiagnosticCode.INVALID_ENDING, 0),
                PlaybackDiagnostic(PlaybackDiagnosticCode.UNNUMBERED_ENDING, 2),
            ),
            endings.playbackOrder().diagnostics,
        )
        // The open ending covers only its first measure; the unnumbered one is always played.
        assertEquals(listOf(0, 1, 2), indices(endings))
    }

    @Test
    fun testLengthLimitTruncates() {
        val playback = part(measure(), measure(backward(1000))).playbackOrder(maxMeasures = 10)
        assertEquals(10, playback.measures.size)
        assertEquals(listOf(PlaybackDiagnostic(PlaybackDiagnosticCode.LENGTH_LIMIT, 0)), playback.diagnostics)
    }

    @Test
    fun testEndingMeasuresAreValidated() {
        val score = Score(
            listOf(part(measure(), endings = listOf(Ending("e1", listOf(1), "m1", null), Ending("e2", listOf(1), "m7", "m1")))),
        )
        assertEquals(
            listOf(
                ModelDiagnostic(ModelDiagnosticCode.MISSING_ENDPOINT, "parts[0].endings[0].stopMeasureId", null),
                ModelDiagnostic(ModelDiagnosticCode.UNKNOWN_MEASURE, "parts[0].endings[1].startMeasureId", "m7"),
            ),
            score.validate(),
        )
    }

    @Test
    fun testBarlineInvariants() {
        assertFailsWith<IllegalArgumentException> { Barline(BarlineLocation.MIDDLE) }
        assertFailsWith<IllegalArgumentException> { Barline(BarlineLocation.LEFT, Rational.ZERO) }
        assertFailsWith<IllegalArgumentException> { Repeat(RepeatDirection.BACKWARD, -1) }
        assertFailsWith<IllegalArgumentException> { EndingMark(EndingType.START, listOf(0)) }
        assertFailsWith<IllegalArgumentException> { MeasureRepeat(0) }
    }

    private fun indices(part: Part): List<Int> = part.playbackOrder().measures.map { it.measureIndex }

    private fun forward() = Barline(BarlineLocation.LEFT, repeat = Repeat(RepeatDirection.FORWARD))

    private fun backward(times: Int? = null) = Barline(repeat = Repeat(RepeatDirection.BACKWARD, times))

    private fun measure(vararg barlines: Barline, implicit: Boolean = false) = barlines.toList() to implicit

    private fun part(vararg measures: Pair<List<Barline>, Boolean>, endings: List<Ending> = emptyList()) = Part(
        id = "P1",
        staves = listOf(Staff(1)),
        measures = measures.mapIndexed { index, (barlines, implicit) ->
            Measure("m${index + 1}", "${index + 1}", Rational.ZERO, implicit = implicit, barlines = barlines)
        },
        endings = endings,
    )
}
