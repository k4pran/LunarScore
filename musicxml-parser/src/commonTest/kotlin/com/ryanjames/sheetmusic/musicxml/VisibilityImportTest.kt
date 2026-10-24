package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Accidental
import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.BeamType
import com.ryanjames.sheetmusic.score.DirectionEvent
import com.ryanjames.sheetmusic.score.DirectionMark
import com.ryanjames.sheetmusic.score.Event
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteRole
import com.ryanjames.sheetmusic.score.NoteVisibility
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * MX-27: `print-object`, `print-dot`, `print-leger`, `print-lyric`, `print-spacing` and accidental
 * `parentheses`/`bracket` are display-only hints. Hidden content keeps its timing and relationships.
 */
class VisibilityImportTest {
    @Test
    fun testNotePrintAttributesAreKeptAsVisibility() {
        val result = success(document(
            "<note print-object='no' print-dot='yes' print-leger='yes' print-lyric='no' print-spacing='no'>" +
                "<pitch><step>C</step><octave>4</octave></pitch><duration>3</duration><type>half</type><dot/></note>" +
                "<note print-object='no'><rest/><duration>1</duration></note>" +
                "<note print-object='no'><grace/><pitch><step>D</step><octave>4</octave></pitch><type>eighth</type></note>" +
                "<note><pitch><step>E</step><octave>4</octave></pitch><duration>1</duration></note>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val events = events(result)
        assertEquals(
            listOf(
                NoteVisibility(printObject = false, printDot = true, printLeger = true, printLyric = false, printSpacing = false),
                NoteVisibility(printObject = false),
                NoteVisibility(printObject = false),
                NoteVisibility(),
            ),
            events.map { if (it is RestEvent) it.visibility else assertIs<NoteEvent>(it).visibility },
        )
        // Hidden notes and rests keep their timing; the hidden grace note still precedes its principal.
        assertEquals(listOf(Rational.ZERO, Rational.of(3), Rational.of(4), Rational.of(4)), events.map { it.onset })
        assertEquals(Rational.of(3), assertIs<NoteEvent>(events[0]).duration)
        assertEquals(Rational.ONE, assertIs<RestEvent>(events[1]).duration)
        assertEquals(NoteRole.GRACE, assertIs<NoteEvent>(events[2]).role)
        val grace = result.value.relationships.single { it.kind == RelationshipKind.GRACE_NOTE }
        assertEquals(listOf(events[2].id, events[3].id), listOf(grace.startEventId, grace.endEventId))
        assertEquals(Rational.of(5), result.value.parts.single().measures.single().extent)
    }

    @Test
    fun testVisibilityDefaultsFollowPrintObject() {
        val hidden = NoteVisibility(printObject = false)
        assertFalse(hidden.dotsPrinted)
        assertFalse(hidden.legerLinesPrinted)
        assertFalse(hidden.lyricsPrinted)
        assertTrue(hidden.spacingKept)
        val shown = NoteVisibility(printDot = false, printLeger = false)
        assertFalse(shown.dotsPrinted)
        assertFalse(shown.legerLinesPrinted)
        assertTrue(shown.lyricsPrinted)
    }

    @Test
    fun testInvalidPrintValuesAreRejectedUnderBothPolicies() {
        listOf(
            "<note print-object='hidden'><rest/><duration>1</duration></note>",
            "<note print-dot='1'><rest/><duration>1</duration></note>",
            "<note><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration><accidental parentheses='true'>natural</accidental></note>",
            "<attributes><time print-object='false'><beats>4</beats><beat-type>4</beat-type></time></attributes>",
            "<note><rest/><duration>1</duration><lyric print-object='0'><text>a</text></lyric></note>",
        ).forEach { content ->
            failure(document(content), DiagnosticCode.INVALID_VALUE)
            failure(document(content), DiagnosticCode.INVALID_VALUE, ParseOptions(ParsePolicy.TOLERANT))
        }
    }

    @Test
    fun testHiddenNotesKeepTiesSlursAndTuplets() {
        val result = success(document(
            "<note print-object='no'><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration>" +
                "<tie type='start'/><time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>" +
                "<notations><tied type='start'/><slur type='start'/><tuplet type='start'/></notations></note>" +
                "<note><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><tie type='stop'/>" +
                "<time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>" +
                "<notations><tied type='stop'/></notations></note>" +
                "<note print-object='no'><pitch><step>D</step><octave>4</octave></pitch><duration>2</duration>" +
                "<time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>" +
                "<notations><slur type='stop'/><tuplet type='stop'/></notations></note>",
            divisions = 3,
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val ids = events(result).map { it.id }
        assertEquals(
            listOf(
                RelationshipKind.SOUND_TIE to listOf(ids[0], ids[1]),
                RelationshipKind.NOTATED_TIE to listOf(ids[0], ids[1]),
                RelationshipKind.SLUR to listOf(ids[0], ids[2]),
                RelationshipKind.TUPLET to listOf(ids[0], ids[2]),
            ).sortedBy { it.first },
            result.value.relationships.map { it.kind to listOf(it.startEventId, it.endEventId) }.sortedBy { it.first },
        )
        assertEquals(listOf(Rational.ZERO, Rational.of(2, 3), Rational.of(4, 3)), events(result).map { it.onset })
    }

    @Test
    fun testAccidentalParenthesesAndBracketAreIndependentOfTheName() {
        val result = success(document(
            "<note><pitch><step>B</step><alter>-1</alter><octave>4</octave></pitch><duration>1</duration>" +
                "<accidental cautionary='yes' parentheses='no'>flat</accidental></note>" +
                "<note><pitch><step>B</step><alter>-1</alter><octave>4</octave></pitch><duration>1</duration>" +
                "<accidental bracket='yes'>flat</accidental></note>" +
                "<note><pitch><step>B</step><octave>4</octave></pitch><duration>1</duration>" +
                "<accidental editorial='yes' parentheses='yes' bracket='no'>natural</accidental></note>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf(
                Accidental("flat", cautionary = true, parentheses = false),
                Accidental("flat", bracket = true),
                Accidental("natural", editorial = true, parentheses = true, bracket = false),
            ),
            events(result).map { assertIs<NoteEvent>(it).notation.accidental },
        )
    }

    @Test
    fun testHiddenAttributesStillChangeState() {
        val result = success(document(
            "<attributes><key print-object='no'><fifths>-1</fifths></key><time print-object='no'><beats>3</beats><beat-type>4</beat-type></time>" +
                "<clef print-object='yes'><sign>F</sign><line>4</line></clef></attributes>" +
                "<note><rest/><duration>3</duration></note>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val part = result.value.parts.single()
        val changes = events(result).filterIsInstance<AttributeEvent>()
        assertEquals(listOf(false, false, true), changes.map { it.visible })
        val state = part.effectiveStates().getValue(events(result).last().id)
        assertEquals(Attribute.KeySignature(-1), state.key)
        assertEquals(Attribute.TimeSignature(3, 4), state.time)
    }

    @Test
    fun testStaffDetailsPrintObjectIsStillDiscarded() {
        val result = success(
            document("<attributes><staff-details print-object='no'><staff-lines>1</staff-lines></staff-details></attributes>"),
            ParseOptions(ParsePolicy.TOLERANT),
        )
        assertEquals(listOf("S17"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.single().location.path.orEmpty().endsWith("/@print-object"))
        assertTrue(assertIs<AttributeEvent>(events(result).single()).visible)
    }

    @Test
    fun testLyricPrintObjectOverridesTheNote() {
        val result = success(document(
            "<note print-lyric='no'><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration>" +
                "<lyric number='1'><text>a</text></lyric><lyric number='2' print-object='yes'><text>b</text></lyric></note>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val note = assertIs<NoteEvent>(events(result).single())
        assertEquals(listOf(null, true), note.notation.lyrics.map { it.printObject })
        assertEquals(listOf(false, true), note.notation.lyrics.map { note.visibility.lyricPrinted(it) })
    }

    @Test
    fun testHiddenMetronomeIsKept() {
        val result = success(document(
            "<direction><direction-type><metronome print-object='no'><beat-unit>quarter</beat-unit><per-minute>60</per-minute>" +
                "</metronome></direction-type><sound tempo='60'/></direction>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val direction = assertIs<DirectionEvent>(events(result).single())
        assertFalse(assertIs<DirectionMark.Metronome>(direction.marks.single()).visible)
        assertEquals(Rational.of(60), direction.sound?.tempo)
    }

    @Test
    fun testHiddenPartNameAndAbbreviationAreKept() {
        val xml = document("").replace(
            "<part-name>V</part-name>",
            "<part-name print-object='no'>Violin</part-name><part-abbreviation print-object='yes'>Vln.</part-abbreviation>",
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val part = result.value.parts.single()
        assertEquals("Violin", part.name)
        assertFalse(part.nameVisible)
        assertEquals("Vln.", part.abbreviation)
        assertTrue(part.abbreviationVisible)
    }

    // 01e: "The first two measures each have a cautionary accidental, an editorial, a cautionary with
    // parentheses off, and an editorial and cautionary accidental. The next two measures each have a
    // normal accidental, a bracketed, a parenthesized, and a bracketed and parenthesized accidental."
    @Test
    fun testW3c01eAccidentalFlagsAreIndependentOfTheAccidental() {
        val notes = notes(part(w3c01ePitchesParenthesizedAccidentals))
        val flags = listOf(
            Accidental("", cautionary = true),
            Accidental("", editorial = true),
            Accidental("", cautionary = true, parentheses = false),
            Accidental("", cautionary = true, editorial = true),
        )
        val display = listOf(
            Accidental(""),
            Accidental("", bracket = true),
            Accidental("", parentheses = true),
            Accidental("", parentheses = true, bracket = true),
        )
        val expected = flags.map { it.copy(name = "flat") } + flags.map { it.copy(name = "sharp") } +
            display.map { it.copy(name = "flat-flat") } + display.map { it.copy(name = "double-sharp") }
        assertEquals(expected, notes.map { it.notation.accidental })
        // The written alter is unchanged by the display flags.
        val alters = listOf(-1, 1, -2, 2).flatMap { alter -> List(4) { Rational.of(alter) } }
        assertEquals(alters, notes.map { assertIs<NotePitch.Pitched>(it.pitch).written.alter })
    }

    // 13f: "The signature at the beginning of the second bar is a flat major and should be invisible;
    // the following notes d flat, e flat, a flat, and b flat shouldn't have a flat accidental."
    @Test
    fun testW3c13fHiddenKeySignatureStillApplies() {
        val part = part(w3c13fKeySignaturesVisible)
        val keys = part.measures.flatMap { it.events }.filterIsInstance<AttributeEvent>().filter { it.attribute is Attribute.KeySignature }
        assertEquals(listOf(true, false), keys.map { it.visible })
        val states = part.effectiveStates()
        val secondBar = part.measures[1].events.filterIsInstance<NoteEvent>()
        assertEquals(4, secondBar.size)
        assertTrue(secondBar.all { states.getValue(it.id).key?.fifths == -4 })
        assertTrue(secondBar.all { it.notation.accidental == null })
    }

    // 34d: print-leger, print-dot, print-lyric and print-object on notes, and print-object on lyrics.
    @Test
    fun testW3c34dNotePrintCombinations() {
        val part = part(w3c34dNoteAttributes)
        val notes = notes(part)
        fun lyricNote(text: String) = notes.single { note -> note.notation.lyrics.any { it.text == text } }
        assertTrue(lyricNote("A").visibility.legerLinesPrinted)
        assertFalse(lyricNote("B").visibility.legerLinesPrinted)
        // C: the middle chord tone (F) hides only its dot.
        val chord = notes.filter { it.chordId != null && it.chordId == lyricNote("C").chordId }
        assertEquals(listOf(true, false, true), chord.map { it.visibility.dotsPrinted })
        assertEquals(listOf(true, true, true), chord.map { it.visibility.printObject })
        assertFalse(lyricNote("D").visibility.lyricsPrinted)
        assertTrue(lyricNote("D").visibility.printObject)
        with(lyricNote("E").visibility) {
            assertFalse(printObject || dotsPrinted || legerLinesPrinted || lyricsPrinted)
        }
        // F, G and H are hidden but print only their leger lines, dot and lyric respectively.
        val partial = listOf("F", "G", "H").map { lyricNote(it).visibility }
        assertTrue(partial.none { it.printObject })
        assertEquals(listOf(true, false, false), partial.map { it.legerLinesPrinted })
        assertEquals(listOf(false, true, false), partial.map { it.dotsPrinted })
        assertEquals(listOf(false, false, true), partial.map { it.lyricsPrinted })
        // I-L: a lyric's print-object overrides the note's print-lyric for that line only.
        val printed = listOf("I1", "J1", "K1", "L1").map { first ->
            val note = lyricNote(first)
            note.notation.lyrics.filter { note.visibility.lyricPrinted(it) }.map { it.text }
        }
        assertEquals(listOf(listOf("I1", "I3"), listOf("J2"), listOf("K1", "K3"), listOf("L2")), printed)
        // Hidden notes keep their dotted-quarter duration.
        assertTrue(notes.all { it.duration == Rational.of(3, 2) })
        assertEquals(listOf(Rational.of(15, 2), Rational.of(9, 2), Rational.of(6)), part.measures.map { it.extent })
    }

    @Test
    fun testPartialVisibilityFixturesAreAcceptedWhenTolerant() {
        w3cPartialVisibilityFixtures.forEach { fixture ->
            val result = parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT), fixture.file)
            val score = assertIs<ParseResult.Success<Score>>(result, "${fixture.file}: ${result.diagnostics}").value
            assertTrue(score.validate().isEmpty(), fixture.file)
            // No visibility attribute is discarded any more.
            val paths = result.diagnostics.mapNotNull { it.location.path }
            assertTrue(paths.none { "/@print-" in it || it.endsWith("/@parentheses") || it.endsWith("/@bracket") }, "${fixture.file}: $paths")
            assertIs<ParseResult.Failure>(parseMusicXml(fixture.xml.encodeToByteArray(), ParseOptions(ParsePolicy.STRICT), fixture.file))
        }
    }

    // 34a: "Test various spanner elements ... starting from a <note> object with 'print-object' set
    // to 'no', then test spanners ending with such a note object." The importer keeps every
    // supported spanner on hidden notes; suppressing them is left to the renderer.
    @Test
    fun testW3c34aSpannersOnHiddenNotesAreKept() {
        val score = score(w3c34aPrintObjectSpanners)
        val part = score.parts.single()
        val notes = notes(part)
        val hidden = notes.filterNot { it.visibility.printObject }.map { it.id }.toSet()
        assertEquals(18, hidden.size)
        // Beams stay on hidden notes, and every measure keeps its full four-beat extent.
        val firstBar = part.measures[0].events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(BeamType.BEGIN, BeamType.END), listOf(firstBar.first(), firstBar.last()).map { it.notation.beams.single().type })
        assertEquals(listOf(false, false), listOf(firstBar.first(), firstBar.last()).map { it.visibility.printObject })
        assertTrue(part.measures.all { it.extent == Rational.of(4) })
        val tuplets = score.relationships.filter { it.kind == RelationshipKind.TUPLET }
        val slurs = score.relationships.filter { it.kind == RelationshipKind.SLUR }
        for (spans in listOf(tuplets, slurs)) {
            assertEquals(2, spans.size)
            // The first starts on a hidden note and the second ends on one.
            assertTrue(spans[0].startEventId in hidden && spans[0].endEventId !in hidden)
            assertTrue(spans[1].startEventId !in hidden && spans[1].endEventId in hidden)
        }
        // Tuplet notes keep their 3:2 durations whether or not they are hidden.
        assertTrue(part.measures[1].events.filterIsInstance<NoteEvent>().all { it.duration == Rational.of(2, 3) })
    }

    // 22b: "hidden notes (with and without hidden staff lines). The lyrics ... in bars 4 and 5 are
    // visible due to the 'print-lyric="yes"' attribute of <note>."
    @Test
    fun testW3c22bHiddenNotesPrintTheirLyrics() {
        val part = part(w3c22bStaffNotestyles)
        for (index in listOf(3, 4)) {
            val notes = part.measures[index].events.filterIsInstance<NoteEvent>()
            assertEquals(4, notes.size)
            assertTrue(notes.none { it.visibility.printObject })
            val first = notes.first()
            assertEquals(2, first.notation.lyrics.size)
            assertTrue(first.notation.lyrics.all { first.visibility.lyricPrinted(it) })
            assertEquals(Rational.of(4), part.measures[index].extent)
        }
    }

    // 11b: "The second bar sets a 2/2 time signature with 'print-object="no"' for both staves, and
    // the third bar sets a 4/4 time signature with 'print-object="no"' only for the lower staff."
    @Test
    fun testW3c11bHiddenTimeSignaturesPerStaff() {
        val part = part(w3c11bTimeSignaturesNoTime)
        val times = part.measures.map { measure ->
            measure.events.filterIsInstance<AttributeEvent>().filter { it.attribute is Attribute.TimeSignature }
        }
        assertEquals(emptyList(), times[0])
        assertEquals(listOf(1 to false, 2 to false), times[1].map { it.staff to it.visible })
        assertEquals(listOf(1 to true, 2 to false), times[2].map { it.staff to it.visible })
        assertTrue((times[1] + times[2]).map { it.attribute }.containsAll(listOf(Attribute.TimeSignature(2, 2), Attribute.TimeSignature(4, 4))))
        val states = part.effectiveStates()
        val lastNotes = part.measures[2].events.filterIsInstance<NoteEvent>()
        assertTrue(lastNotes.all { states.getValue(it.id).time == Attribute.TimeSignature(4, 4) })
    }

    // 02d: the part name is printed with 'print-object="no"'.
    @Test
    fun testW3c02dHiddenPartName() {
        val part = part(w3c02dRestsMultimeasureTimeSignatures)
        assertEquals("MusicXML Part", part.name)
        assertFalse(part.nameVisible)
    }

    private fun score(xml: String): Score =
        assertIs<ParseResult.Success<Score>>(parseMusicXml(xml.encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT))).value

    private fun part(xml: String): Part = score(xml).parts.single()

    private fun notes(part: Part): List<NoteEvent> = part.measures.flatMap { it.events }.filterIsInstance<NoteEvent>()

    private fun events(result: ParseResult.Success<Score>): List<Event> = result.value.parts.single().measures.first().events

    private fun document(content: String, divisions: Int = 1): String =
        "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>$divisions</divisions></attributes>$content</measure></part></score-partwise>"

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
