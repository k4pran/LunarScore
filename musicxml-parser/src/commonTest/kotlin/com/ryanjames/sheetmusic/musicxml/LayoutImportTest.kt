package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.DirectionEvent
import com.ryanjames.sheetmusic.score.DirectionMark
import com.ryanjames.sheetmusic.score.Dynamics
import com.ryanjames.sheetmusic.score.LayoutDefaults
import com.ryanjames.sheetmusic.score.MarginType
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.PageLayout
import com.ryanjames.sheetmusic.score.PageMargins
import com.ryanjames.sheetmusic.score.Placement
import com.ryanjames.sheetmusic.score.PrintHints
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Scaling
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.StaffLayout
import com.ryanjames.sheetmusic.score.SystemLayout
import com.ryanjames.sheetmusic.score.VisualHints
import com.ryanjames.sheetmusic.score.WedgeType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LayoutImportTest {
    @Test
    fun testF18RetainsDefaultsInTenths() {
        val result = success(f18LayoutHints)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            LayoutDefaults(
                scaling = Scaling(Rational.of(36, 5), Rational.of(40)),
                page = PageLayout(
                    height = Rational.of(1650),
                    width = Rational.of(2333, 2),
                    margins = listOf(
                        PageMargins(MarginType.ODD, Rational.of(80), Rational.of(60), Rational.of(70), Rational.of(70)),
                        PageMargins(MarginType.EVEN, Rational.of(60), Rational.of(80), Rational.of(70), Rational.of(70)),
                    ),
                ),
                system = SystemLayout(Rational.ZERO, Rational.ZERO, Rational.of(121), Rational.of(70)),
                staves = listOf(StaffLayout(staffDistance = Rational.of(65))),
            ),
            result.value.layout,
        )
        assertTrue(result.provenance.any { it.modelPath == "layout" })
    }

    @Test
    fun testF18RetainsPrintBreaksAndLayoutPerMeasure() {
        val result = success(f18LayoutHints)
        assertEquals(
            listOf(
                PrintHints(
                    system = SystemLayout(leftMargin = Rational.of(50), rightMargin = Rational.ZERO, topSystemDistance = Rational.of(211)),
                    measureDistance = Rational.of(25, 2),
                ),
                PrintHints(newSystem = true, staves = listOf(StaffLayout(1, Rational.of(80)))),
                PrintHints(newPage = true, blankPages = 1, pageNumber = "3"),
            ),
            result.value.parts.single().measures.map { it.print },
        )
        assertTrue(result.provenance.any { it.modelPath == "parts[0].measures[2].print" })
    }

    @Test
    fun testF18RetainsNegativePositionsAndColours() {
        val measures = success(f18LayoutHints).value.parts.single().measures
        val first = measures[0].events
        assertEquals(
            listOf(DirectionMark.Words("dolce", VisualHints(defaultY = Rational.of(-80), relativeX = Rational.of(-5)))),
            assertIs<DirectionEvent>(first.single { it is DirectionEvent }).marks,
        )
        assertEquals(
            VisualHints(defaultX = Rational.of(83), relativeY = Rational.of(-7, 2)),
            assertIs<NoteEvent>(first.single { it is NoteEvent }).visual,
        )
        assertEquals(VisualHints(color = "#FF0000"), assertIs<RestEvent>(first.single { it is RestEvent }).visual)
        assertNull(assertIs<NoteEvent>(measures[1].events.single()).visual)
        val wedges = measures[2].events.filterIsInstance<DirectionEvent>().map { it.marks.single() }
        assertEquals(
            listOf(
                DirectionMark.Wedge(WedgeType.CRESCENDO, VisualHints(defaultY = Rational.of(-70), color = "#80FF0000")),
                DirectionMark.Wedge(WedgeType.STOP),
            ),
            wedges,
        )
    }

    @Test
    fun testExplicitNoBreaksDifferFromOmittedBreaks() {
        val forbidden = success(document("<print new-system='no' new-page='no'/>"))
        assertEquals(PrintHints(newSystem = false, newPage = false), forbidden.value.parts.single().measures.single().print)
        val omitted = success(document("<print/>"))
        assertEquals(PrintHints(newSystem = null, newPage = null), omitted.value.parts.single().measures.single().print)
    }

    @Test
    fun testOmittedDefaultsAndPrintLeaveHintsAbsent() {
        val plain = success(document("<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration></note>"))
        assertNull(plain.value.layout)
        assertNull(plain.value.parts.single().measures.single().print)
        assertNull(assertIs<NoteEvent>(plain.value.parts.single().measures.single().events.single()).visual)
        val concertOnly = success(document("", defaults = "<defaults><concert-score/></defaults>"))
        assertNull(concertOnly.value.layout)
        val partial = success(document("<print/>", defaults = "<defaults><staff-layout number='2'/></defaults>"))
        assertEquals(LayoutDefaults(staves = listOf(StaffLayout(2))), partial.value.layout)
        assertEquals(PrintHints(), partial.value.parts.single().measures.single().print)
    }

    @Test
    fun testNoteDynamicsAndMetronomeKeepPositions() {
        val xml = document(
            "<direction><direction-type><metronome default-y='40' relative-x='-12.25'><beat-unit>quarter</beat-unit>" +
                "<per-minute>96</per-minute></metronome><dynamics color='#00ff00'><p/></dynamics></direction-type></direction>" +
                "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration>" +
                "<notations><dynamics placement='below' default-y='-60'><f/></dynamics></notations></note>",
        )
        val events = success(xml).value.parts.single().measures.single().events
        assertEquals(
            listOf(
                DirectionMark.Metronome(
                    NoteValue.QUARTER, "96",
                    visual = VisualHints(defaultY = Rational.of(40), relativeX = Rational.of(-49, 4)),
                ),
                Dynamics(listOf("p"), visual = VisualHints(color = "#00FF00")),
            ),
            assertIs<DirectionEvent>(events[0]).marks,
        )
        assertEquals(
            listOf(Dynamics(listOf("f"), Placement.BELOW, VisualHints(defaultY = Rational.of(-60)))),
            assertIs<NoteEvent>(events[1]).notation.expressions,
        )
    }

    @Test
    fun testUnsupportedLayoutContentIsDiscardedWithS17() {
        val defaults = "<defaults><scaling><millimeters>7</millimeters><tenths>40</tenths></scaling>" +
            "<system-layout><system-distance>100</system-distance><system-dividers/></system-layout>" +
            "<appearance><line-width type='stem'>1</line-width></appearance><music-font font-family='Bravura'/></defaults>"
        val xml = document(
            "<print staff-spacing='60' new-system='yes'><measure-numbering>system</measure-numbering>" +
                "<part-name-display><display-text>Fl.</display-text></part-name-display></print><print new-page='yes'/>",
            defaults = defaults,
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(List(7) { "S17" }, result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        assertTrue(result.diagnostics.any { it.location.path?.endsWith("/@staff-spacing") == true })
        assertEquals(
            LayoutDefaults(Scaling(Rational.of(7), Rational.of(40)), system = SystemLayout(systemDistance = Rational.of(100))),
            result.value.layout,
        )
        // Only the first print in a measure is kept.
        assertEquals(PrintHints(newSystem = true), result.value.parts.single().measures.single().print)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidLayoutValuesAreFatal() {
        val invalidValues = listOf(
            document("<note color='red'><rest/><duration>1</duration></note>"),
            document("<note default-x='left'><rest/><duration>1</duration></note>"),
            document("<direction><direction-type><words relative-y='1e3'>a</words></direction-type></direction>"),
            document("<print new-system='true'/>"),
            document("<print blank-page='0'/>"),
            document("<print><staff-layout number='0'/></print>"),
            document("", defaults = "<defaults><page-layout><page-height>0</page-height><page-width>10</page-width></page-layout></defaults>"),
            document("", defaults = "<defaults><scaling><millimeters>-7</millimeters><tenths>40</tenths></scaling></defaults>"),
            document("", defaults = "<defaults><page-layout>${margins("left")}</page-layout></defaults>"),
        )
        val invalidStructures = listOf(
            document("", defaults = "<defaults><scaling><tenths>40</tenths></scaling></defaults>"),
            document("", defaults = "<defaults><page-layout><page-height>10</page-height></page-layout></defaults>"),
            document("", defaults = "<defaults><page-layout>${margins("both")}${margins("odd")}</page-layout></defaults>"),
            document("", defaults = "<defaults><page-layout>${margins("odd")}${margins("odd")}</page-layout></defaults>"),
            document("", defaults = "<defaults><system-layout><system-margins><left-margin>1</left-margin></system-margins></system-layout></defaults>"),
            document("", defaults = "<defaults><scaling/><scaling/></defaults>"),
        )
        for (policy in ParsePolicy.entries) {
            invalidValues.forEach { failure(it, DiagnosticCode.INVALID_VALUE, ParseOptions(policy)) }
            invalidStructures.forEach { failure(it, DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy)) }
        }
    }

    private fun margins(type: String): String =
        "<page-margins type='$type'><left-margin>1</left-margin><right-margin>1</right-margin>" +
            "<top-margin>1</top-margin><bottom-margin>1</bottom-margin></page-margins>"

    private fun document(content: String, defaults: String = ""): String =
        "<score-partwise version='4.0'>$defaults<part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>1</divisions></attributes>$content</measure></part></score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), options), xml)
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        return result
    }
}
