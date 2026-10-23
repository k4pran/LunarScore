package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Beam
import com.ryanjames.sheetmusic.score.BeamType
import com.ryanjames.sheetmusic.score.Grace
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NoteRole
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Relationship
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraceImportTest {
    @Test
    fun testF19GraceNotesDoNotAdvanceTheCursorAndCueNotesDo() {
        val result = success(f19GraceAndCue)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (first, second) = result.value.parts.single().measures
        val notes = first.events.filterIsInstance<NoteEvent>()
        assertEquals(
            listOf(NoteRole.GRACE, NoteRole.NORMAL, NoteRole.GRACE, NoteRole.GRACE, NoteRole.NORMAL, NoteRole.CUE, NoteRole.GRACE),
            notes.map { it.role },
        )
        assertEquals(listOf(0, 0, 1, 1, 1, 2, 4).map { Rational.of(it) }, notes.map { it.onset })
        assertEquals(listOf(null, Rational.ONE, null, null, Rational.ONE, Rational.of(2), null), notes.map { it.duration })
        assertEquals(Rational.of(4), first.extent)
        assertEquals(
            listOf(
                Grace(slash = true, stealTimeFollowing = Rational.of(33)),
                null,
                Grace(),
                Grace(),
                null,
                null,
                Grace(stealTimePrevious = Rational.of(25)),
            ),
            notes.map { it.grace },
        )
        assertEquals(NoteValue.EIGHTH, notes[0].notation.value)
        assertEquals(listOf(Beam(1, BeamType.BEGIN), Beam(2, BeamType.BEGIN)), notes[2].notation.beams)
        assertEquals(Rational.ZERO, assertIs<RestEvent>(second.events.single()).onset)

        val id = { event: Int -> "P1:measure-1:event-$event" }
        assertEquals(
            listOf(
                Relationship("P1:grace-note-1", RelationshipKind.GRACE_NOTE, id(2), id(3)),
                Relationship("P1:grace-note-2", RelationshipKind.GRACE_NOTE, id(4), id(6)),
                Relationship("P1:grace-note-3", RelationshipKind.GRACE_NOTE, id(5), id(6)),
                // The trailing grace is an after-grace of the cue note.
                Relationship("P1:grace-note-4", RelationshipKind.GRACE_NOTE, id(8), id(7)),
            ),
            result.value.relationships.filter { it.kind == RelationshipKind.GRACE_NOTE },
        )
        assertEquals(
            Relationship("P1:slur-1", RelationshipKind.SLUR, id(2), id(3)),
            result.value.relationships.single { it.kind == RelationshipKind.SLUR },
        )
        assertTrue(result.value.validate().isEmpty())
        val graceIndex = result.value.relationships.indexOfFirst { it.id == "P1:grace-note-4" }
        assertEquals(
            listOf("/score-partwise[1]/part[1]/measure[1]/note[7]", "/score-partwise[1]/part[1]/measure[1]/note[6]"),
            listOf("startEventId", "endEventId").map { field ->
                result.provenance.single { it.modelPath == "relationships[$graceIndex].$field" }.location.path
            },
        )
    }

    @Test
    fun testGraceNoteNeedsNoDurationOrDivisions() {
        val xml = "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'>${grace("C")}</measure></part></score-partwise>"
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        val note = assertIs<NoteEvent>(result.value.parts.single().measures.single().events.single())
        assertNull(note.duration)
        assertEquals(Rational.ZERO, result.value.parts.single().measures.single().extent)
        // With no following note the grace note has no principal note.
        assertEquals(listOf(DiagnosticCode.UNMATCHED_SPAN), result.diagnostics.map { it.code })
        assertEquals(Relationship("P1:grace-note-1", RelationshipKind.GRACE_NOTE, note.id, null), result.value.relationships.single())
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    @Test
    fun testGraceDurationIsReportedAndIgnored() {
        val xml = document(
            "<note><grace/><pitch><step>C</step><octave>4</octave></pitch><duration>1</duration></note>" + note("D"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S18"), result.diagnostics.map { it.unsupported?.featureId })
        val (grace, principal) = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertNull(grace.duration)
        assertEquals(listOf(Rational.ZERO, Rational.ZERO), listOf(grace.onset, principal.onset))
        assertEquals(principal.id, result.value.relationships.single().endEventId)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidGraceDurationIsIgnoredWithoutValidation() {
        listOf("0", "-1", "abc", "99999999999999999999999").forEach { duration ->
            val xml = document(
                "<note><grace/><pitch><step>C</step><octave>4</octave></pitch><duration>$duration</duration></note>" + note("D"),
            )
            val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
            assertEquals(listOf("S18"), result.diagnostics.map { it.unsupported?.featureId }, duration)
            val (grace, principal) = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
            assertNull(grace.duration)
            assertEquals(Relationship("P1:grace-note-1", RelationshipKind.GRACE_NOTE, grace.id, principal.id), result.value.relationships.single())
        }
        failure(document("<note><pitch><step>C</step><octave>4</octave></pitch><duration>0</duration></note>"), DiagnosticCode.INVALID_VALUE)
    }

    @Test
    fun testGraceRelationshipPrecedesSpansStartingOnTheGraceNote() {
        val xml = document(
            "<note><grace/><pitch><step>C</step><octave>4</octave></pitch><voice>1</voice>" +
                "<notations><slur type='start'/></notations></note>" +
                "<note><pitch><step>D</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice>" +
                "<notations><slur type='stop'/></notations></note>",
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val id = { event: Int -> "P1:measure-1:event-$event" }
        assertEquals(
            listOf(
                Relationship("P1:grace-note-1", RelationshipKind.GRACE_NOTE, id(1), id(2)),
                Relationship("P1:slur-1", RelationshipKind.SLUR, id(1), id(2)),
            ),
            result.value.relationships,
        )
    }

    @Test
    fun testGraceChordKeepsTheFirstGraceNoteAndDiscardsChordTones() {
        val xml = document(
            grace("C") + "<note><grace/><chord/><pitch><step>E</step><octave>4</octave></pitch></note>" + note("D"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S06"), result.diagnostics.map { it.unsupported?.featureId })
        val events = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(NoteRole.GRACE, NoteRole.NORMAL), events.map { it.role })
        assertEquals(listOf(Rational.ZERO, Rational.ZERO), events.map { it.onset })
        assertEquals(Relationship("P1:grace-note-1", RelationshipKind.GRACE_NOTE, events[0].id, events[1].id), result.value.relationships.single())
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testCueAfterBackupIsDiscardedUntilCursorMovesAreInterpreted() {
        val xml = document(
            note("C", voice = "1") + grace("E", voice = "1") +
                "<backup><duration>1</duration></backup>" +
                "<note><cue/><pitch><step>G</step><octave>4</octave></pitch><duration>1</duration><voice>2</voice></note>",
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S06", "S06"), result.diagnostics.map { it.unsupported?.featureId })
        val events = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(NoteRole.NORMAL, NoteRole.GRACE), events.map { it.role })
        assertEquals(Rational.ONE, events[1].onset)
        // The grace note before the cursor move is an after-grace of the C.
        assertEquals(events[0].id, result.value.relationships.single().endEventId)
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testGraceNotesLinkWithinTheirOwnVoice() {
        val xml = document(
            grace("C", voice = "2") + note("D", voice = "1") + note("E", voice = "2") +
                note("F", voice = "1") + grace("G", voice = "1") + "<note><rest/><duration>1</duration><voice>1</voice></note>",
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val id = { event: Int -> "P1:measure-1:event-$event" }
        assertEquals(
            listOf(
                Relationship("P1:grace-note-1", RelationshipKind.GRACE_NOTE, id(1), id(3)),
                // A grace note followed by a rest in its voice belongs to the previous note.
                Relationship("P1:grace-note-2", RelationshipKind.GRACE_NOTE, id(5), id(4)),
            ),
            result.value.relationships,
        )
    }

    @Test
    fun testGraceBeforeDiscardedNoteHasNoPrincipal() {
        val xml = document(
            grace("C") + "<note><pitch><step>D</step><octave>4</octave></pitch><duration>1</duration><staff>2</staff></note>" + note("E"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticCode.UNMATCHED_SPAN), result.diagnostics.map { it.code })
        val events = result.value.parts.single().measures.single().events
        assertEquals(listOf(Rational.ZERO, Rational.ONE), events.map { it.onset })
        assertNull(result.value.relationships.single().endEventId)
    }

    @Test
    fun testUnsupportedGraceContentIsReported() {
        val xml = document(
            "<note><grace make-time='1'/><pitch><step>C</step><octave>4</octave></pitch></note>" +
                "<note><grace/><cue/><pitch><step>D</step><octave>4</octave></pitch></note>" +
                "<note><grace/><rest/></note>" +
                "<note><grace/><unpitched/></note>" +
                note("E"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S18", "S18", "S18", "S16"), result.diagnostics.map { it.unsupported?.featureId })
        val events = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(NoteRole.GRACE, NoteRole.GRACE, NoteRole.NORMAL), events.map { it.role })
        assertTrue(events.all { it.onset == Rational.ZERO })
        assertEquals(listOf(events[2].id, events[2].id), result.value.relationships.map { it.endEventId })
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidGraceAttributesAreFatal() {
        listOf(
            "<grace slash='maybe'/>",
            "<grace steal-time-previous='101'/>",
            "<grace steal-time-following='-1'/>",
            "<grace steal-time-following='lots'/>",
        ).forEach { element ->
            val xml = document("<note>$element<pitch><step>C</step><octave>4</octave></pitch></note>" + note("D"))
            failure(xml, DiagnosticCode.INVALID_VALUE, ParseOptions(ParsePolicy.TOLERANT))
        }
        failure(document(grace("C") + grace("D").replace("<grace/>", "<grace/><grace/>")), DiagnosticCode.INVALID_STRUCTURE)
    }

    /** A one-division quarter note. */
    private fun note(step: String, voice: String = "1"): String =
        "<note><pitch><step>$step</step><octave>4</octave></pitch><duration>1</duration><voice>$voice</voice></note>"

    /** A grace note without a duration. */
    private fun grace(step: String, voice: String = "1"): String =
        "<note><grace/><pitch><step>$step</step><octave>4</octave></pitch><voice>$voice</voice></note>"

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
