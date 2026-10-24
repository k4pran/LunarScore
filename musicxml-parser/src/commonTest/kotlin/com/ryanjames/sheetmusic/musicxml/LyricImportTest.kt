package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.ExtendType
import com.ryanjames.sheetmusic.score.FontHints
import com.ryanjames.sheetmusic.score.FontSize
import com.ryanjames.sheetmusic.score.FontStyle
import com.ryanjames.sheetmusic.score.HorizontalAlignment
import com.ryanjames.sheetmusic.score.Lyric
import com.ryanjames.sheetmusic.score.LyricExtend
import com.ryanjames.sheetmusic.score.LyricSyllable
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.Placement
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.Syllabic
import com.ryanjames.sheetmusic.score.VisualHints
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LyricImportTest {
    @Test
    fun testF15KeepsVersesSyllablesElisionAndMelismaOnTheirNotes() {
        val result = success(f15Lyrics)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (first, second) = result.value.parts.single().measures
        val refrain = { syllables: List<LyricSyllable> -> Lyric(syllables, "2", "refrain", placement = Placement.BELOW) }
        assertEquals(listOf(
            listOf(
                Lyric(listOf(LyricSyllable("A", Syllabic.BEGIN)), "1"),
                refrain(listOf(LyricSyllable("fa", Syllabic.SINGLE), LyricSyllable("un", Syllabic.SINGLE, "‿"))),
            ),
            listOf(
                Lyric(listOf(LyricSyllable("ve", Syllabic.END)), "1", extend = LyricExtend(ExtendType.START)),
                refrain(listOf(LyricSyllable("glo", Syllabic.BEGIN))),
            ),
            listOf(
                Lyric(emptyList(), "1", extend = LyricExtend(ExtendType.CONTINUE)),
                refrain(listOf(LyricSyllable("ria", Syllabic.END))),
            ),
            listOf(Lyric(emptyList(), "1", extend = LyricExtend(ExtendType.STOP))),
        ), first.events.filterIsInstance<NoteEvent>().map { it.notation.lyrics })
        // Lyrics are notation only: onsets are unchanged.
        assertEquals((0..3).map { Rational.of(it) }, first.events.filterIsInstance<NoteEvent>().map { it.onset })

        val (amen, rest) = second.events
        // A missing number leaves the verse unspecified; text runs join into one syllable.
        assertEquals(listOf(Lyric(listOf(LyricSyllable("Amen", Syllabic.SINGLE)))), assertIs<NoteEvent>(amen).notation.lyrics)
        assertTrue(assertIs<RestEvent>(rest).notation.lyrics.isEmpty())
        assertTrue(result.value.validate().isEmpty())
        assertTrue(result.value.relationships.isEmpty())
        assertEquals(
            "/score-partwise[1]/part[1]/measure[1]/note[2]",
            result.provenance.single { it.modelPath == "parts[0].measures[0].events[2]" }.location.path,
        )
    }

    @Test
    fun testF15CanonicalTextSequenceFollowsNoteIdsPerVerse() {
        val events = success(f15Lyrics).value.parts.single().measures.flatMap { it.events }.filterIsInstance<NoteEvent>()
        val lines = events
            .flatMap { note -> note.notation.lyrics.map { lyric -> Triple(lyric.verse, note.id, lyric.text) } }
            .groupBy({ it.first }, { it.second to it.third })
        assertEquals(listOf("1", "2", null), lines.keys.toList())
        assertEquals(
            listOf(event(1, 2) to "A", event(1, 3) to "ve", event(1, 4) to "", event(1, 5) to ""),
            lines["1"],
        )
        assertEquals(listOf(event(1, 2) to "fa‿un", event(1, 3) to "glo", event(1, 4) to "ria"), lines["2"])
        assertEquals(listOf(event(2, 1) to "Amen"), lines[null])
    }

    @Test
    fun testElisionWithoutSymbolAndTextRunsAfterElision() {
        val lyric = singleLyric(
            "<lyric number='1'><text>a</text><elision/><text>b</text><text>c</text><extend/></lyric>",
        )
        assertEquals(
            Lyric(listOf(LyricSyllable("a"), LyricSyllable("bc", elision = "")), "1", extend = LyricExtend()),
            lyric,
        )
        assertEquals("abc", lyric.text)
        // Whitespace inside text is preserved.
        assertEquals(" la ", singleLyric("<lyric><text> la </text></lyric>").text)
    }

    @Test
    fun testUnsupportedLyricContentIsDiscardedWithFeatureIds() {
        val xml = document(
            note(
                "C",
                "<lyric number='1' justify='left' default-y='-80'>" +
                    "<text font-style='italic' xml:lang='it'>la</text><elision font-size='8'>_</elision><text>a</text>" +
                    "<extend type='start' default-x='4'/><end-line/><end-paragraph/><footnote>x</footnote></lyric>" +
                    "<lyric number='2'><laughing/></lyric><lyric number='3'><humming/><end-line/></lyric>" +
                    "<lyric number='4' time-only='1'><text>da</text></lyric>",
            ),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf("S14", "S14", "S14", "S25", "S14", "S14", "S14", "S14"),
            result.diagnostics.map { it.unsupported?.featureId },
        )
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        val note = assertIs<NoteEvent>(result.value.parts.single().measures.single().events.single())
        assertEquals(
            listOf(
                Lyric(
                    listOf(
                        LyricSyllable("la", visual = VisualHints(font = FontHints(style = FontStyle.ITALIC))),
                        LyricSyllable("a", elision = "_", elisionVisual = VisualHints(font = FontHints(size = FontSize.Points(Rational.of(8))))),
                    ),
                    "1",
                    extend = LyricExtend(ExtendType.START, VisualHints(defaultX = Rational.of(4))),
                    visual = VisualHints(defaultY = Rational.of(-80), justify = HorizontalAlignment.LEFT),
                ),
            ),
            note.notation.lyrics,
        )
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidLyricValuesAndStructureAreFatal() {
        for (policy in ParsePolicy.entries) {
            val options = ParseOptions(policy)
            listOf(
                "<lyric number=' '><text>a</text></lyric>",
                // A no-break space is not XML whitespace but is still a blank verse number.
                "<lyric number=' '><text>a</text></lyric>",
                "<lyric><syllabic>start</syllabic><text>a</text></lyric>",
                "<lyric><text>a</text><extend type='begin'/></lyric>",
                "<lyric placement='middle'><text>a</text></lyric>",
            ).forEach { failure(document(note("C", it)), DiagnosticCode.INVALID_VALUE, options) }
            listOf(
                "<lyric/>",
                "<lyric><syllabic>single</syllabic></lyric>",
                "<lyric><elision/><text>a</text></lyric>",
                "<lyric><text>a</text><elision/></lyric>",
                "<lyric><text>a</text><syllabic>single</syllabic><text>b</text></lyric>",
                "<lyric><text>a</text><elision/><elision/><text>b</text></lyric>",
                "<lyric><extend/><text>a</text></lyric>",
                "<lyric><text>a</text><extend/><extend/></lyric>",
                "<lyric><text>a</text><end-line/><text>b</text></lyric>",
                "<lyric><end-line/></lyric>",
                "<lyric><text>a</text><humming/></lyric>",
                "<lyric><syllabic>single</syllabic><extend/></lyric>",
            ).forEach { failure(document(note("C", it)), DiagnosticCode.INVALID_STRUCTURE, options) }
        }
    }

    private fun event(measure: Int, index: Int) = "P1:measure-$measure:event-$index"

    private fun singleLyric(lyric: String): Lyric {
        val result = success(document(note("C", lyric)))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        return assertIs<NoteEvent>(result.value.parts.single().measures.single().events.single()).notation.lyrics.single()
    }

    /** A one-division quarter note with optional lyrics. */
    private fun note(step: String, lyrics: String = ""): String =
        "<note><pitch><step>$step</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice>$lyrics</note>"

    private fun document(content: String): String =
        "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>1</divisions></attributes>$content</measure></part></score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), options))
        assertTrue(result.diagnostics.any { it.code == code }, result.diagnostics.toString())
        assertNull(result.value)
        return result
    }
}
