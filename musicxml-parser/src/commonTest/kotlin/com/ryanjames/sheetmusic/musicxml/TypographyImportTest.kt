package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.CssFontSize
import com.ryanjames.sheetmusic.score.DirectionEvent
import com.ryanjames.sheetmusic.score.DirectionMark
import com.ryanjames.sheetmusic.score.Dynamics
import com.ryanjames.sheetmusic.score.Enclosure
import com.ryanjames.sheetmusic.score.FontHints
import com.ryanjames.sheetmusic.score.FontSize
import com.ryanjames.sheetmusic.score.FontStyle
import com.ryanjames.sheetmusic.score.FontWeight
import com.ryanjames.sheetmusic.score.HorizontalAlignment
import com.ryanjames.sheetmusic.score.Measure
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NoteExpression
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.VerticalAlignment
import com.ryanjames.sheetmusic.score.VisualHints
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * MX-28 typography and colour: font, colour, alignment, enclosure, `xml:space` and position on the
 * elements the model represents, the score's music and word fonts, and measure widths. Expected
 * facts for the W3C files come from the suite's own descriptions and source.
 */
class TypographyImportTest {
    @Test
    fun testWordsKeepEveryTextFormattingAttribute() {
        val words = "<words default-x='-5' relative-y='2.5' color='#00ff00' font-family='Times New Roman, serif' " +
            "font-size='x-large' font-style='italic' font-weight='bold' halign='right' valign='baseline' justify='center' " +
            "enclosure='inverted-bracket' xml:space='default'>dolce</words>"
        val result = success(document(direction(words)))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            DirectionMark.Words(
                "dolce",
                VisualHints(
                    defaultX = Rational.of(-5),
                    relativeY = Rational.of(5, 2),
                    color = "#00FF00",
                    font = FontHints(listOf("Times New Roman", "serif"), FontSize.Css(CssFontSize.X_LARGE), FontStyle.ITALIC, FontWeight.BOLD),
                    halign = HorizontalAlignment.RIGHT,
                    valign = VerticalAlignment.BASELINE,
                    justify = HorizontalAlignment.CENTER,
                    enclosure = Enclosure.INVERTED_BRACKET,
                    preserveWhitespace = false,
                ),
            ),
            directions(result).single().marks.single(),
        )
    }

    @Test
    fun testNotationElementsKeepPositionColourAndFont() {
        val note = "<note default-y='5' font-size='12'><pitch><step>C</step><alter>1</alter><octave>4</octave></pitch>" +
            "<duration>1</duration><type>eighth</type><accidental color='#0000FF' font-family='Bravura' relative-x='-2'>sharp</accidental>" +
            "<stem default-y='30' color='#FF0000'>up</stem><beam color='#00FF00'>begin</beam><notations>" +
            "<slur type='start' color='#123456' default-y='12'/><tuplet type='start' relative-y='4'/>" +
            "<articulations><accent placement='above' font-size='20' color='#FF0000'/></articulations>" +
            "<ornaments><trill-mark default-y='25' font-weight='bold'/></ornaments>" +
            "<dynamics halign='left' enclosure='circle' font-style='italic'><p/></dynamics></notations>" +
            "<lyric number='1' justify='right' color='#FF0000'><text font-family='Arial' color='#0000FF'>la</text>" +
            "<extend type='start' relative-x='3' color='#00FF00'/></lyric></note>" +
            "<note><pitch><step>D</step><octave>4</octave></pitch><duration>1</duration>" +
            "<notations><slur type='stop' relative-x='-1'/><tuplet type='stop' default-x='8'/></notations></note>"
        val result = success(document(note))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (first, second) = notes(result.value)
        assertEquals(VisualHints(defaultY = Rational.of(5), font = FontHints(size = FontSize.Points(Rational.of(12)))), first.visual)
        val notation = first.notation
        assertEquals(VisualHints(relativeX = Rational.of(-2), color = "#0000FF", font = FontHints(listOf("Bravura"))), notation.accidental?.visual)
        assertEquals(VisualHints(defaultY = Rational.of(30), color = "#FF0000"), notation.stemVisual)
        assertEquals(VisualHints(color = "#00FF00"), notation.beams.single().visual)
        assertEquals(
            listOf(
                VisualHints(color = "#FF0000", font = FontHints(size = FontSize.Points(Rational.of(20)))),
                VisualHints(defaultY = Rational.of(25), font = FontHints(weight = FontWeight.BOLD)),
                VisualHints(font = FontHints(style = FontStyle.ITALIC), halign = HorizontalAlignment.LEFT, enclosure = Enclosure.CIRCLE),
            ),
            notation.expressions.map { it.visual },
        )
        val lyric = notation.lyrics.single()
        assertEquals(VisualHints(color = "#FF0000", justify = HorizontalAlignment.RIGHT), lyric.visual)
        assertEquals(VisualHints(color = "#0000FF", font = FontHints(listOf("Arial"))), lyric.syllables.single().visual)
        assertEquals(VisualHints(relativeX = Rational.of(3), color = "#00FF00"), lyric.extend?.visual)
        assertNull(second.visual)
        val slur = result.value.relationships.single { it.kind == RelationshipKind.SLUR }
        assertEquals(VisualHints(defaultY = Rational.of(12), color = "#123456") to VisualHints(relativeX = Rational.of(-1)), slur.startVisual to slur.endVisual)
        val tuplet = result.value.relationships.single { it.kind == RelationshipKind.TUPLET }
        assertEquals(VisualHints(relativeY = Rational.of(4)) to VisualHints(defaultX = Rational.of(8)), tuplet.startVisual to tuplet.endVisual)
    }

    @Test
    fun testAttributesBarlinesAndMeasureKeepTheirStyle() {
        val xml = document(
            "<barline location='left'><ending number='1' type='start'/></barline>" +
                "<attributes><divisions>1</divisions><key color='#FF0000'><fifths>0</fifths></key>" +
                "<time font-size='30' halign='center'><beats>4</beats><beat-type>4</beat-type></time>" +
                "<clef default-x='-3'><sign>G</sign><line>2</line></clef></attributes>" +
                "<note><rest/><duration>4</duration></note>" +
                "<barline><bar-style color='#0000ff'>light-heavy</bar-style>" +
                "<ending number='1' type='stop' font-style='italic' default-y='40'/></barline>",
            measureAttributes = " width='441.5'",
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val measure = result.value.parts.single().measures.single()
        assertEquals(Rational.of(883, 2), measure.width)
        assertEquals(
            listOf(
                VisualHints(color = "#FF0000"),
                VisualHints(font = FontHints(size = FontSize.Points(Rational.of(30))), halign = HorizontalAlignment.CENTER),
                VisualHints(defaultX = Rational.of(-3)),
            ),
            measure.events.filterIsInstance<AttributeEvent>().map { it.visual },
        )
        val barline = measure.barlines.last()
        assertEquals(VisualHints(color = "#0000FF"), barline.styleVisual)
        assertEquals(VisualHints(defaultY = Rational.of(40), font = FontHints(style = FontStyle.ITALIC)), barline.ending?.visual)
    }

    @Test
    fun testLaterLyricTextRunTypographyIsDiscarded() {
        val lyric = "<lyric><text font-weight='bold'>la</text><text font-weight='bold'>a</text><text color='#FF0000'>h</text></lyric>"
        val xml = document("<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration>$lyric</note>")
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        // A run matching the first run's typography loses nothing; a differing one is reported.
        assertEquals(listOf(UnsupportedContent("S14", ContentRetention.DISCARDED)), result.diagnostics.map { it.unsupported })
        assertTrue(result.diagnostics.single().location.path.orEmpty().endsWith("/lyric[1]/text[3]"))
        val syllable = notes(result.value).single().notation.lyrics.single().syllables.single()
        assertEquals("laah", syllable.text)
        assertEquals(VisualHints(font = FontHints(weight = FontWeight.BOLD)), syllable.visual)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testLaterLyricTextRunWithoutTypographyLosesTheDistinction() {
        // A later run with default formatting differs from an explicitly formatted first run.
        val lyric = "<lyric><text font-weight='bold'>la</text><text>a</text></lyric>"
        val xml = document("<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration>$lyric</note>")
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(UnsupportedContent("S14", ContentRetention.DISCARDED)), result.diagnostics.map { it.unsupported })
        assertTrue(result.diagnostics.single().location.path.orEmpty().endsWith("/lyric[1]/text[2]"))
        val syllable = notes(result.value).single().notation.lyrics.single().syllables.single()
        assertEquals("laa", syllable.text)
        assertEquals(VisualHints(font = FontHints(weight = FontWeight.BOLD)), syllable.visual)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)

        val plain = document(
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><lyric><text>la</text><text>a</text></lyric></note>",
        )
        assertTrue(success(plain).diagnostics.isEmpty())
    }

    @Test
    fun testFormattingWithoutAModelElementIsStillDiscarded() {
        val xml = document(
            direction("<words letter-spacing='2' xml:lang='it' underline='1'>dolce</words>") +
                "<note><pitch><step>C</step><octave>4</octave></pitch><duration>3</duration><dot color='#FF0000'/></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf("@letter-spacing" to "S13", "@{http://www.w3.org/XML/1998/namespace}lang" to "S13", "@underline" to "S13", "@color" to "S17"),
            result.diagnostics.map { "@" + it.location.path.orEmpty().substringAfterLast("/@") to it.unsupported?.featureId },
        )
        assertEquals(DirectionMark.Words("dolce"), directions(result).single().marks.single())
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidFormattingValuesAreFatal() {
        listOf(
            direction("<words font-size='0'>a</words>"),
            direction("<words font-size='huge'>a</words>"),
            direction("<words font-style='oblique'>a</words>"),
            direction("<words font-weight='heavy'>a</words>"),
            direction("<words font-family=' , serif'>a</words>"),
            direction("<words halign='middle'>a</words>"),
            direction("<words valign='center'>a</words>"),
            direction("<words justify='both'>a</words>"),
            direction("<words enclosure='star'>a</words>"),
            direction("<words xml:space='keep'>a</words>"),
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><stem color='red'>up</stem></note>",
        ).forEach { content ->
            for (policy in ParsePolicy.entries) failure(document(content), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
        }
        for (policy in ParsePolicy.entries) {
            failure(document("", measureAttributes = " width='-1'"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            failure(document("", measureAttributes = " width='wide'"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
        }
    }

    @Test
    fun testDuplicateDefaultFontsAreInvalid() {
        val xml = document("", defaults = "<defaults><music-font font-family='Bravura'/><music-font font-family='Petaluma'/></defaults>")
        failure(xml, DiagnosticCode.INVALID_STRUCTURE, ParseOptions(ParsePolicy.TOLERANT))
    }

    // 31f: "Within a single <direction> element ... the 'p' dynamic symbol gets followed by a single
    // space, which in turn is followed by some boxed text that spreads over three lines and has the
    // attribute 'valign="center"'. The latter two also set the attribute 'xml:space="preserve"'."
    @Test
    fun testW3c31fMultilineWordsKeepTheirLineBreaks() {
        val marks = directions(score(w3c31fDirectionMultilineCompounds)).single().marks
        val font = FontHints(listOf("Times New Roman"), FontSize.Points(Rational.of(41, 4)))
        assertEquals(
            listOf(
                Dynamics(listOf("p"), visual = VisualHints(defaultY = Rational.of(-80))),
                DirectionMark.Words(" ", VisualHints(defaultY = Rational.of(-80), font = font, preserveWhitespace = true)),
                DirectionMark.Words(
                    "text\nin\na small box",
                    VisualHints(
                        defaultY = Rational.of(-80), font = font, valign = VerticalAlignment.MIDDLE,
                        enclosure = Enclosure.RECTANGLE, preserveWhitespace = true,
                    ),
                ),
            ),
            marks,
        )
    }

    // 32b: words with explicit positions, CSS font sizes, weights and one red colour.
    @Test
    fun testW3c32bWordsKeepPositionsSizesAndWeights() {
        val words = directions(score(w3c32bArticulationsTexts)).flatMap { it.marks }.filterIsInstance<DirectionMark.Words>()
        val last = words.last()
        assertEquals("(100,-160) normal, small, red ↓", last.text)
        assertEquals(
            VisualHints(Rational.of(100), Rational.of(-160), color = "#FF0000", font = FontHints(size = FontSize.Css(CssFontSize.SMALL))),
            last.visual,
        )
        val bold = words.single { it.text.startsWith("(-50,-100)") }
        assertEquals(
            VisualHints(Rational.of(-50), Rational.of(-100), font = FontHints(size = FontSize.Css(CssFontSize.MEDIUM), weight = FontWeight.BOLD)),
            bold.visual,
        )
        assertTrue(words.all { it.visual?.font?.size is FontSize.Css })
    }

    // 51a: "... credit-words with various attributes ... meaningful": positions, sizes, alignment,
    // justification and a boxed title; the title's second credit-words has no attributes.
    @Test
    fun testW3c51aCreditsKeepPositionsAndFormatting() {
        val credits = score(w3c51aHeaderCredits).metadata.filterIsInstance<MetadataEntry.Credit>()
        assertEquals(8, credits.size)
        val rights = credits.single { it.types == listOf("rights") }
        assertEquals(
            listOf(
                VisualHints(
                    Rational.of(430), Rational.of(80), font = FontHints(size = FontSize.Points(Rational.of(9))),
                    halign = HorizontalAlignment.CENTER, valign = VerticalAlignment.BOTTOM, justify = HorizontalAlignment.RIGHT,
                ),
            ),
            rights.visuals,
        )
        val title = credits.single { it.types == listOf("title") }
        assertEquals(listOf("The ", "Title"), title.words)
        assertEquals(
            listOf(
                VisualHints(
                    Rational.of(430), Rational.of(440), font = FontHints(size = FontSize.Points(Rational.of(24))),
                    valign = VerticalAlignment.MIDDLE, justify = HorizontalAlignment.CENTER, enclosure = Enclosure.RECTANGLE,
                ),
                null,
            ),
            title.visuals,
        )
        val typed = credits.single { it.types == listOf("foo", "bar") }
        assertEquals(FontStyle.ITALIC, typed.visuals.single()?.font?.style)
        assertEquals(2, credits.single { it.words == listOf("2a") }.page)
    }

    // 52a: page layout with music and word fonts in the defaults and explicit measure widths.
    @Test
    fun testW3c52aKeepsDefaultFontsAndMeasureWidths() {
        val result = parseMusicXml(w3c52aPageLayout.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))
        val score = assertIs<ParseResult.Success<Score>>(result).value
        // Only the MIDI channel and program are discarded.
        assertEquals(listOf("S26", "S26"), result.diagnostics.map { it.unsupported?.featureId })
        val layout = score.layout
        assertEquals(FontHints(listOf("Maestro"), FontSize.Points(Rational.of(107, 10))), layout?.musicFont)
        assertEquals(FontHints(listOf("Times New Roman"), FontSize.Points(Rational.of(53, 10))), layout?.wordFont)
        assertEquals(listOf(441, 378, 378, 378, 819).map { Rational.of(it) }, score.parts.single().measures.map(Measure::width))
        val credit = score.metadata.filterIsInstance<MetadataEntry.Credit>().single()
        assertEquals(FontWeight.BOLD, credit.visuals.single()?.font?.weight)
    }

    // 34b: "The elements in the first bar have the 'color' attribute set to red for <note>,
    // <notehead>, <stem>, <dot>, and <accidental> ..." and so on for every colourable element.
    @Test
    fun testW3c34bColoursAreKeptOnModeledElements() {
        val result = parseMusicXml(w3c34bColors.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))
        val score = assertIs<ParseResult.Success<Score>>(result).value
        val red = "#FF0000"
        val blue = "#0000FF"
        val events = score.parts.single().measures.flatMap { it.events }
        val notes = events.filterIsInstance<NoteEvent>()
        val rests = events.filterIsInstance<RestEvent>()
        // The red note, the red rest and the red unpitched note.
        assertEquals(listOf(red, red), notes.mapNotNull { it.visual?.color })
        assertEquals(listOf(red), rests.mapNotNull { it.visual?.color })
        val notations = events.mapNotNull { (it as? NoteEvent)?.notation ?: (it as? RestEvent)?.notation }
        assertEquals(listOf(red, red), notations.mapNotNull { it.stemVisual?.color })
        assertEquals(listOf(red), notations.mapNotNull { it.accidental?.visual?.color })
        assertEquals(listOf(red, red), notations.flatMap { it.beams }.mapNotNull { it.visual?.color })
        assertEquals(listOf(red), notations.flatMap { it.lyrics }.mapNotNull { it.visual?.color })
        // The red accent and trill mark; the down-bow, tremolo and breath mark are not modeled.
        val expressions = notations.flatMap { it.expressions }.filter { it.visual?.color == red }
        assertEquals(2, expressions.size)
        assertTrue(expressions.any { it is NoteExpression.Articulation } && expressions.any { it is NoteExpression.Ornament })
        val marks = directions(result).flatMap { it.marks }
        assertEquals(
            listOf("Dynamics", "Words", "Wedge", "Words"),
            marks.filter { it.visual?.color == red }.map { it::class.simpleName },
        )
        assertEquals(FontWeight.BOLD, marks.filterIsInstance<DirectionMark.Words>().first().visual?.font?.weight)
        val slurAndTie = score.relationships.filter { it.kind == RelationshipKind.SLUR || it.kind == RelationshipKind.NOTATED_TIE }
        assertEquals(listOf(red, red), slurAndTie.mapNotNull { it.startVisual?.color })
        val attributes = events.filterIsInstance<AttributeEvent>().filter { it.visual != null }
        assertEquals(
            listOf("TimeSignature" to red, "KeySignature" to red, "KeySignature" to red, "Clef" to red, "Clef" to blue),
            attributes.map { it.attribute::class.simpleName to it.visual?.color },
        )
        val barlines = score.parts.single().measures.flatMap { it.barlines }
        assertEquals(listOf(red, blue, red), barlines.mapNotNull { it.styleVisual?.color })
        assertEquals(listOf(red), barlines.mapNotNull { it.ending?.visual?.color })
        // No colour of a modeled element is discarded.
        val discardedColours = result.diagnostics.mapNotNull { it.location.path }.filter { it.endsWith("/@color") }
        assertEquals(listOf("dot", "measure-style"), discardedColours.map { it.substringBeforeLast("/@").substringAfterLast('/').substringBefore('[') })
    }

    // 34c: "The elements in the first bar have the 'font-size' attribute set to a larger value for
    // <note>, <notehead>, <trill-mark>, <dot>, and <accidental> ..." and so on.
    @Test
    fun testW3c34cFontSizesAreKeptOnModeledElements() {
        val result = parseMusicXml(w3c34cFontSize.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))
        val score = assertIs<ParseResult.Success<Score>>(result).value
        val large = FontSize.Points(Rational.of(30))
        val events = score.parts.single().measures.flatMap { it.events }
        val notes = events.filterIsInstance<NoteEvent>()
        // The larger note and unpitched note, and the larger rest.
        assertEquals(2, notes.count { it.visual?.font?.size == large })
        assertEquals(1, events.filterIsInstance<RestEvent>().count { it.visual?.font?.size == large })
        assertEquals(listOf(large), notes.mapNotNull { it.notation.accidental?.visual?.font?.size })
        // The two larger trill marks and the larger accent.
        assertEquals(3, notes.flatMap { it.notation.expressions }.count { it.visual?.font?.size == large })
        assertEquals(listOf(large), notes.flatMap { it.notation.lyrics }.flatMap { it.syllables }.mapNotNull { it.visual?.font?.size })
        val attributes = events.filterIsInstance<AttributeEvent>().filter { it.visual?.font?.size == large }
        assertEquals(listOf("Clef", "KeySignature", "TimeSignature", "KeySignature", "Clef"), attributes.map { it.attribute::class.simpleName })
        val marks = directions(result).flatMap { it.marks }.filter { it.visual?.font?.size == large }
        assertEquals(listOf("Dynamics", "Words", "Words"), marks.map { it::class.simpleName })
        assertTrue(attributes.first().attribute is Attribute.Clef)
        val discardedSizes = result.diagnostics.mapNotNull { it.location.path }.filter { it.endsWith("/@font-size") }
        assertEquals(listOf("dot", "measure-style"), discardedSizes.map { it.substringBeforeLast("/@").substringAfterLast('/').substringBefore('[') })
    }

    // 61j: "... the last syllable 'f‿g' ... the elision and the second syllable have different
    // colours and the second syllable is italic."
    @Test
    fun testW3c61jElisionAndSyllableKeepTheirOwnFormatting() {
        val lyric = notes(score(w3c61jLyricsElisions)).last().notation.lyrics.single()
        assertEquals(VisualHints(color = "#FF0000"), lyric.visual)
        val (f, g, h) = lyric.syllables
        assertNull(f.visual)
        assertEquals("‿" to VisualHints(color = "#00FF00"), g.elision to g.elisionVisual)
        assertEquals(VisualHints(color = "#0000FF", font = FontHints(style = FontStyle.ITALIC)), g.visual)
        assertEquals("" to null, h.elision to h.elisionVisual)
    }

    // 61h: a red melisma extender; 61k: left- and right-justified lyrics before extenders.
    @Test
    fun testW3c61hAnd61kKeepExtenderColourAndLyricJustification() {
        val extenders = notes(score(w3c61hLyricsBeamsMelismata)).flatMap { it.notation.lyrics }.mapNotNull { it.extend?.visual }
        assertEquals(listOf(VisualHints(color = "#FF0000")), extenders)
        val justified = notes(score(w3c61kLyricsSpannersExtenders)).flatMap { it.notation.lyrics }.mapNotNull { it.visual?.justify }
        assertEquals(listOf(HorizontalAlignment.LEFT, HorizontalAlignment.RIGHT, HorizontalAlignment.LEFT), justified)
    }

    // 03e-SecondaryBeamBreaks and the compressed 90a file only lost their measure width before MX-28.
    @Test
    fun testW3c03eSecondaryBeamBreaksKeepsItsMeasureWidth() {
        assertEquals(Rational.of(107749, 100), score(w3c03eRhythmSecondaryBeamBreaks).parts.single().measures.first().width)
    }

    @Test
    fun testPartialTypographyFixturesAreAcceptedWhenTolerant() {
        w3cPartialTypographyFixtures.forEach { fixture ->
            val result = parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT), fixture.file)
            val score = assertIs<ParseResult.Success<Score>>(result, "${fixture.file}: ${result.diagnostics}").value
            assertTrue(score.validate().isEmpty(), fixture.file)
            assertIs<ParseResult.Failure>(parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.STRICT), fixture.file))
        }
    }

    private fun score(xml: String): Score {
        val result = parseMusicXml(xml.encodeToByteArray())
        return assertIs<ParseResult.Success<Score>>(result, result.diagnostics.toString()).value
    }

    private fun notes(score: Score): List<NoteEvent> = score.parts.single().measures.flatMap { it.events }.filterIsInstance<NoteEvent>()

    private fun directions(result: ParseResult<Score>): List<DirectionEvent> =
        checkNotNull(result.value).parts.single().measures.flatMap { it.events }.filterIsInstance<DirectionEvent>()

    private fun directions(score: Score): List<DirectionEvent> =
        score.parts.single().measures.flatMap { it.events }.filterIsInstance<DirectionEvent>()

    private fun direction(types: String): String = "<direction><direction-type>$types</direction-type></direction>"

    private fun document(content: String, measureAttributes: String = "", defaults: String = ""): String =
        "<score-partwise version='4.0'>$defaults<part-list><score-part id='P1'><part-name>Music</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'$measureAttributes>$content</measure></part></score-partwise>"

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, result.diagnostics.toString())
    }

    private fun failure(xml: String, code: DiagnosticCode, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        val failure = assertIs<ParseResult.Failure>(result, "Expected $code")
        assertTrue(failure.diagnostics.any { it.code == code }, failure.diagnostics.toString())
        return failure
    }
}
