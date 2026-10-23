package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.BarStyle
import com.ryanjames.sheetmusic.score.Barline
import com.ryanjames.sheetmusic.score.BarlineLocation
import com.ryanjames.sheetmusic.score.Ending
import com.ryanjames.sheetmusic.score.EndingMark
import com.ryanjames.sheetmusic.score.EndingType
import com.ryanjames.sheetmusic.score.MeasureRepeat
import com.ryanjames.sheetmusic.score.ModelDiagnostic
import com.ryanjames.sheetmusic.score.ModelDiagnosticCode
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.PlaybackDiagnostic
import com.ryanjames.sheetmusic.score.PlaybackDiagnosticCode
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Repeat
import com.ryanjames.sheetmusic.score.RepeatDirection
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.playbackOrder
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BarlineImportTest {
    @Test
    fun testF16RetainsBarStylesRepeatsAndEndingsAsNotated() {
        val result = success(f16RepeatsAndEndings)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val part = result.value.parts.single()
        assertEquals(listOf("0", "1", "2", "3", "4"), part.measures.map { it.number })
        assertTrue(part.measures.first().implicit)
        assertEquals(
            listOf(
                emptyList(),
                listOf(Barline(BarlineLocation.LEFT, style = BarStyle.HEAVY_LIGHT, repeat = Repeat(RepeatDirection.FORWARD))),
                listOf(
                    Barline(BarlineLocation.LEFT, ending = EndingMark(EndingType.START, listOf(1), "1.")),
                    Barline(
                        style = BarStyle.LIGHT_HEAVY,
                        repeat = Repeat(RepeatDirection.BACKWARD),
                        ending = EndingMark(EndingType.STOP, listOf(1)),
                    ),
                ),
                listOf(
                    Barline(BarlineLocation.LEFT, ending = EndingMark(EndingType.START, listOf(2))),
                    Barline(ending = EndingMark(EndingType.DISCONTINUE, listOf(2))),
                ),
                listOf(
                    Barline(BarlineLocation.MIDDLE, Rational.of(2), BarStyle.DASHED),
                    Barline(style = BarStyle.LIGHT_HEAVY),
                ),
            ),
            part.measures.map { it.barlines },
        )
        assertEquals(
            listOf(
                Ending("P1:ending-1", listOf(1), "measure-3", "measure-3", "1."),
                Ending("P1:ending-2", listOf(2), "measure-4", "measure-4", discontinued = true),
            ),
            part.endings,
        )
        // Barlines take no time and do not split the measure's notes.
        assertEquals(listOf(Rational.ZERO, Rational.of(2)), part.measures[4].events.map { it.onset })
        assertTrue(result.value.validate().isEmpty())
        assertEquals(
            "/score-partwise[1]/part[1]/measure[3]/barline[1]/ending[1]",
            result.provenance.single { it.modelPath == "parts[0].endings[0]" }.location.path,
        )
        assertEquals(
            "/score-partwise[1]/part[1]/measure[5]/barline[1]",
            result.provenance.single { it.modelPath == "parts[0].measures[4].barlines[0]" }.location.path,
        )
    }

    @Test
    fun testF16PlaybackOrderIsDerivedSeparatelyFromNotation() {
        val part = success(f16RepeatsAndEndings).value.parts.single()
        val playback = part.playbackOrder()
        assertTrue(playback.diagnostics.isEmpty())
        assertEquals(listOf(0, 1, 2, 1, 3, 4), playback.measures.map { it.measureIndex })
        assertEquals(listOf(1, 1, 1, 2, 2, 1), playback.measures.map { it.pass })
        assertEquals(listOf("measure-1", "measure-2", "measure-3", "measure-2", "measure-4", "measure-5"), playback.measures.map { it.measureId })
        // The notated structure is unchanged: five measures in written order.
        assertEquals(5, part.measures.size)
    }

    @Test
    fun testOrphanEndingStopIsDiagnosed() {
        val xml = document(measure(right = "<barline><ending number='1' type='stop'/></barline>"))
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        val diagnostic = result.diagnostics.single()
        assertEquals(DiagnosticCode.UNMATCHED_SPAN, diagnostic.code)
        assertEquals(DiagnosticSeverity.WARNING, diagnostic.severity)
        assertEquals("/score-partwise[1]/part[1]/measure[1]/barline[1]/ending[1]", diagnostic.location.path)
        assertTrue(result.value.parts.single().endings.isEmpty())
        // The orphan mark stays on its barline.
        assertEquals(EndingMark(EndingType.STOP, listOf(1)), result.value.parts.single().measures.single().barlines.single().ending)
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    @Test
    fun testUnstoppedEndingKeepsNullStop() {
        val xml = document(measure(left = "<barline location='left'><ending number='1' type='start'/></barline>"), measure())
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(DiagnosticCode.UNMATCHED_SPAN), result.diagnostics.map { it.code })
        val part = result.value.parts.single()
        assertEquals(listOf(Ending("P1:ending-1", listOf(1), "measure-1", null)), part.endings)
        assertEquals(
            listOf(ModelDiagnostic(ModelDiagnosticCode.MISSING_ENDPOINT, "parts[0].endings[0].stopMeasureId", null)),
            result.value.validate(),
        )
        assertEquals(listOf(PlaybackDiagnostic(PlaybackDiagnosticCode.OPEN_ENDING, 0)), part.playbackOrder().diagnostics)
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    @Test
    fun testNestedAndMismatchedEndingsAreDiagnosed() {
        val xml = document(
            measure(left = "<barline location='left'><ending number='1' type='start'/></barline>"),
            measure(
                left = "<barline location='left'><ending number='2' type='start'/></barline>",
                right = "<barline><ending number='1' type='stop'/></barline>",
            ),
            measure(right = "<barline><ending number='2' type='stop'/></barline>"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        // Ending 1 is left open by the nested start, and its later stop no longer matches.
        assertEquals(listOf(DiagnosticCode.UNMATCHED_SPAN, DiagnosticCode.UNMATCHED_SPAN), result.diagnostics.map { it.code })
        assertEquals(
            listOf("/score-partwise[1]/part[1]/measure[1]/barline[1]/ending[1]", "/score-partwise[1]/part[1]/measure[2]/barline[2]/ending[1]"),
            result.diagnostics.map { it.location.path },
        )
        assertEquals(
            listOf(
                Ending("P1:ending-1", listOf(1), "measure-1", null),
                Ending("P1:ending-2", listOf(2), "measure-2", "measure-3"),
            ),
            result.value.parts.single().endings,
        )
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    @Test
    fun testEndingNumbersFollowTheEndingNumberPattern() {
        fun numbers(number: String): List<Int> {
            val xml = document(
                measure(
                    left = "<barline location='left'><ending number='$number' type='start'/></barline>",
                    right = "<barline><ending number='$number' type='stop'/></barline>",
                ),
            )
            return success(xml).value.parts.single().endings.single().numbers
        }
        assertEquals(listOf(1, 2), numbers("1,2"))
        assertEquals(listOf(1, 2, 10), numbers("1, 2, 10"))
        assertEquals(emptyList(), numbers(""))
        assertEquals(emptyList(), numbers("  "))
        for (invalid in listOf("0", "1,,2", "1,  2", " 1", "a", "1.", "01")) {
            failure(document(measure(right = "<barline><ending number='$invalid' type='stop'/></barline>")), DiagnosticCode.INVALID_VALUE)
        }
    }

    @Test
    fun testInvalidBarlineValuesAreReported() {
        for (policy in ParsePolicy.entries) {
            val options = ParseOptions(policy)
            val values = listOf(
                "<barline location='top'/>",
                "<barline><bar-style>thick</bar-style></barline>",
                "<barline><repeat direction='up'/></barline>",
                "<barline><repeat direction='backward' times='-1'/></barline>",
                "<barline><ending number='1' type='begin'/></barline>",
            )
            values.forEach { failure(document(measure(right = it)), DiagnosticCode.INVALID_VALUE, options) }
            val structures = listOf(
                "<barline><repeat/></barline>",
                "<barline><ending type='start'/></barline>",
                "<barline><ending number='1'/></barline>",
                "<barline><bar-style>light-light</bar-style><bar-style>heavy</bar-style></barline>",
            )
            structures.forEach { failure(document(measure(right = it)), DiagnosticCode.INVALID_STRUCTURE, options) }
        }
    }

    @Test
    fun testUninterpretedBarlineContentIsDiscarded() {
        val xml = document(
            measure(
                right = "<barline segno='s1' id='b1'><bar-style color='#000000'>light-heavy</bar-style><footnote>f</footnote>" +
                    "<wavy-line type='stop'/><segno/><coda/><fermata/>" +
                    "<ending number='1' type='stop' print-object='no'/><repeat direction='backward' times='3' winged='straight' after-jump='yes'/>" +
                    "</barline>",
            ),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf("S15", "S15", "S15", "S25", "S15", "S15", "S15", "S15", "S15", "S15", "S15"),
            result.diagnostics.filter { it.code == DiagnosticCode.UNSUPPORTED_FEATURE }.map { it.unsupported?.featureId },
        )
        val barline = result.value.parts.single().measures.single().barlines.single()
        assertEquals(Barline(style = BarStyle.LIGHT_HEAVY, repeat = Repeat(RepeatDirection.BACKWARD, 3), ending = EndingMark(EndingType.STOP, listOf(1))), barline)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testMiddleBarlineAfterCursorMoveIsDiscarded() {
        val xml = document(
            measure(
                right = "<backup><duration>1</duration></backup><barline location='middle'><bar-style>dashed</bar-style></barline>" +
                    "<barline><bar-style>light-heavy</bar-style></barline>",
            ),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S06", "S06"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(listOf(Barline(style = BarStyle.LIGHT_HEAVY)), result.value.parts.single().measures.single().barlines)
    }

    @Test
    fun testMeasureRepeatMarksMeasuresUntilItsStop() {
        val xml = document(
            measure(),
            measure(left = "<attributes><measure-style><measure-repeat type='start' slashes='2'>2</measure-repeat></measure-style></attributes>"),
            measure(),
            measure(left = "<attributes><measure-style><measure-repeat type='stop'/></measure-style></attributes>"),
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val measures = result.value.parts.single().measures
        assertEquals(listOf(null, MeasureRepeat(2, 2), MeasureRepeat(2, 2), null), measures.map { it.measureRepeat })
        // The repeated measures still contain the music they sound.
        assertTrue(measures.all { it.events.filterIsInstance<NoteEvent>().size == 1 })
        assertEquals(listOf(0, 1, 2, 3), result.value.parts.single().playbackOrder().measures.map { it.measureIndex })
    }

    @Test
    fun testMeasureStyleVariantsAndInvalidRepeats() {
        val orphan = document(measure(left = "<attributes><measure-style><measure-repeat type='stop'/></measure-style></attributes>"))
        assertEquals(listOf(DiagnosticCode.UNMATCHED_SPAN), success(orphan, ParseOptions(ParsePolicy.TOLERANT)).diagnostics.map { it.code })
        failure(orphan, DiagnosticCode.UNMATCHED_SPAN)

        val other = document(
            measure(
                left = "<attributes><measure-style number='2'><measure-repeat type='start'>1</measure-repeat></measure-style>" +
                    "<measure-style><multiple-rest>4</multiple-rest></measure-style></attributes>",
            ),
        )
        val result = success(other, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S08", "S24"), result.diagnostics.map { it.unsupported?.featureId })
        assertNull(result.value.parts.single().measures.single().measureRepeat)

        for (invalid in listOf("<measure-repeat type='start'>0</measure-repeat>", "<measure-repeat type='start' slashes='0'>1</measure-repeat>", "<measure-repeat type='begin'>1</measure-repeat>")) {
            failure(document(measure(left = "<attributes><measure-style>$invalid</measure-style></attributes>")), DiagnosticCode.INVALID_VALUE)
        }
        failure(document(measure(left = "<attributes><measure-style><measure-repeat>1</measure-repeat></measure-style></attributes>")), DiagnosticCode.INVALID_STRUCTURE)
    }

    @Test
    fun testEmptyMeasureRepeatCountDefaultsToOne() {
        val xml = document(measure(left = "<attributes><measure-style><measure-repeat type='start'/></measure-style></attributes>"))
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(MeasureRepeat(1, 1), result.value.parts.single().measures.single().measureRepeat)
    }

    @Test
    fun testTimewisePaddedMeasuresKeepActiveMeasureRepeat() {
        val note = "<note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>"
        val xml = "<score-timewise version='4.0'><part-list>" +
            "<score-part id='P1'><part-name>A</part-name></score-part><score-part id='P2'><part-name>B</part-name></score-part></part-list>" +
            "<measure number='1'><part id='P1'><attributes><divisions>1</divisions></attributes>$note</part>" +
            "<part id='P2'><attributes><divisions>1</divisions><measure-style><measure-repeat type='start'>1</measure-repeat></measure-style></attributes>$note</part></measure>" +
            "<measure number='2'><part id='P1'>$note</part></measure>" +
            "<measure number='3'><part id='P1'>$note</part>" +
            "<part id='P2'><attributes><measure-style><measure-repeat type='stop'/></measure-style></attributes>$note</part></measure>" +
            "</score-timewise>"
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        val measures = result.value.parts.single { it.id == "P2" }.measures
        assertEquals(listOf(MeasureRepeat(1, 1), MeasureRepeat(1, 1), null), measures.map { it.measureRepeat })
    }

    @Test
    fun testTimewiseEndingsPairAcrossMeasures() {
        val xml = "<score-timewise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<measure number='1'><part id='P1'><barline location='left'><ending number='1, 2' type='start'/></barline></part></measure>" +
            "<measure number='2'><part id='P1'><barline><ending number='1, 2' type='stop'/><repeat direction='backward'/></barline></part></measure>" +
            "</score-timewise>"
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(listOf(Ending("P1:ending-1", listOf(1, 2), "measure-1", "measure-2")), result.value.parts.single().endings)
    }

    private fun measure(left: String = "", right: String = ""): String =
        "$left<note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration></note>$right"

    private fun document(vararg measures: String): String =
        "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list><part id='P1'>" +
            measures.mapIndexed { index, content ->
                val divisions = if (index == 0) "<attributes><divisions>1</divisions></attributes>" else ""
                "<measure number='${index + 1}'>$divisions$content</measure>"
            }.joinToString("") + "</part></score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), options))
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        return result
    }
}
