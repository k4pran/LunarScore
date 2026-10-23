package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.ArticulationType
import com.ryanjames.sheetmusic.score.DirectionDisplay
import com.ryanjames.sheetmusic.score.DirectionEvent
import com.ryanjames.sheetmusic.score.DirectionMark
import com.ryanjames.sheetmusic.score.DirectionSound
import com.ryanjames.sheetmusic.score.Dynamics
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NoteExpression
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.OrnamentType
import com.ryanjames.sheetmusic.score.Placement
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Relationship
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.WedgeType
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DirectionImportTest {
    @Test
    fun testF13KeepsArticulationsOrnamentsAndDynamicsOnTheirNotes() {
        val result = success(f13NoteExpressions)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val notes = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(
            listOf(
                NoteExpression.Articulation(ArticulationType.STACCATO, Placement.ABOVE),
                Dynamics(listOf("f"), Placement.BELOW),
            ),
            listOf(
                NoteExpression.Ornament(OrnamentType.TRILL_MARK, Placement.ABOVE),
                NoteExpression.Articulation(ArticulationType.ACCENT),
                NoteExpression.Articulation(ArticulationType.TENUTO, Placement.BELOW),
            ),
            listOf(
                NoteExpression.Ornament(OrnamentType.INVERTED_MORDENT),
                Dynamics(listOf("sf", "p")),
                Dynamics(listOf("pocof")),
            ),
        ), notes.map { it.notation.expressions })
        // Expressions are notation only: timing and the printed value are unchanged.
        assertEquals(listOf(Rational.ZERO, Rational.ONE, Rational.of(2)), notes.map { it.onset })
        assertEquals(listOf(NoteValue.QUARTER, NoteValue.QUARTER, NoteValue.HALF), notes.map { it.notation.value })
        assertTrue(result.value.relationships.isEmpty())
    }

    @Test
    fun testF14KeepsDirectionOffsetsSoundDisplayAndWedgeSpan() {
        val result = success(f14Directions)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (first, second) = result.value.parts.single().measures
        assertEquals(listOf(
            DirectionEvent(
                event(1, 2),
                Rational.ZERO,
                listOf(DirectionMark.Words("Allegro"), DirectionMark.Metronome(NoteValue.QUARTER, "120")),
                sound = DirectionSound(tempo = Rational.of(120)),
                display = DirectionDisplay(Placement.ABOVE, directive = true),
            ),
            DirectionEvent(event(1, 3), Rational.ZERO, listOf(Dynamics(listOf("p"))), display = DirectionDisplay(Placement.BELOW)),
            DirectionEvent(
                event(1, 5),
                Rational.ONE,
                listOf(DirectionMark.Wedge(WedgeType.CRESCENDO)),
                voice = "1",
                display = DirectionDisplay(Placement.BELOW, offset = Rational.of(1, 2)),
            ),
        ), first.events.filterIsInstance<DirectionEvent>())
        assertEquals(listOf(Rational.ZERO, Rational.ONE, Rational.of(2)), first.events.filterIsInstance<NoteEvent>().map { it.onset })
        // Directions do not advance the cursor or the observed extent.
        assertEquals(Rational.of(4), first.extent)

        val (stop, whole, words) = second.events
        assertEquals(
            DirectionEvent(
                event(2, 1),
                Rational.ZERO,
                listOf(DirectionMark.Wedge(WedgeType.STOP), Dynamics(listOf("f"))),
                voice = "1",
                sound = DirectionSound(dynamics = Rational.of(10667, 100), offset = Rational.of(-1, 2)),
                display = DirectionDisplay(Placement.BELOW, offset = Rational.of(-1, 2)),
            ),
            stop,
        )
        assertIs<NoteEvent>(whole)
        assertEquals(DirectionEvent(event(2, 3), Rational.of(4), listOf(DirectionMark.Words("rit."))), words)

        assertEquals(
            listOf(Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 5), event(2, 1))),
            result.value.relationships,
        )
        assertTrue(result.value.validate().isEmpty())
        assertEquals(
            "/score-partwise[1]/part[1]/measure[2]/direction[1]",
            result.provenance.single { it.modelPath == "parts[0].measures[1].events[0]" }.location.path,
        )
        assertEquals(
            "/score-partwise[1]/part[1]/measure[2]/direction[1]/direction-type[1]/wedge[1]",
            result.provenance.single { it.modelPath == "relationships[0].endEventId" }.location.path,
        )
    }

    @Test
    fun testDirectionAfterBackupIsPlacedAtTheCursor() {
        val xml = document(
            direction("<wedge type='crescendo'/>") + note("C") + note("D") + "<backup><duration>1</duration></backup>" +
                direction("<wedge type='stop'/>", "<voice>2</voice>") + note("E"),
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val directions = result.value.parts.single().measures.single().events.filterIsInstance<DirectionEvent>()
        assertEquals(listOf(Rational.ZERO, Rational.ONE), directions.map { it.onset })
        assertEquals("2", directions[1].voice)
        // The stop after the backup is at offset 1, after the crescendo starts, so they pair.
        assertEquals(listOf(event(1, 1) to event(1, 4)), result.value.relationships.map { it.startEventId to it.endEventId })
    }

    @Test
    fun testDirectionKeepsItsDeclaredStaff() {
        val directions = direction("<words>arco</words>", "<staff>2</staff>") + direction("<words>dolce</words>", "<staff>1</staff>") + note("C")
        val result = success(document("<attributes><staves>2</staves></attributes>$directions"))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val events = result.value.parts.single().measures.single().events.filterIsInstance<DirectionEvent>()
        assertEquals(
            listOf(listOf(DirectionMark.Words("arco")) to 2, listOf(DirectionMark.Words("dolce")) to 1),
            events.map { it.marks to it.staff },
        )
        // Staff 2 does not exist without a staves declaration.
        for (policy in ParsePolicy.entries) failure(document(directions), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
    }

    @Test
    fun testWedgesPairByNumberAndReportUnmatchedEndpoints() {
        val xml = document(
            direction("<wedge type='diminuendo' number='2'/>") + direction("<wedge type='crescendo'/>") + note("C") +
                direction("<wedge type='continue' number='2'/>") + direction("<wedge type='stop' number='2'/>") +
                direction("<wedge type='continue' number='3'/>") + note("D"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf(DiagnosticCode.UNMATCHED_SPAN, DiagnosticCode.UNMATCHED_SPAN),
            result.diagnostics.map { it.code },
        )
        assertEquals(
            listOf(
                Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 1), event(1, 5)),
                Relationship("P1:wedge-2", RelationshipKind.WEDGE, event(1, 2), null),
            ),
            result.value.relationships,
        )
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    @Test
    fun testWedgeEndpointsAreOrderedByDisplayOffset() {
        val xml = document(
            direction("<wedge type='stop'/>", "<offset>2</offset>") + note("C") +
                direction("<wedge type='crescendo'/>") + note("D") + note("E"),
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf(Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 3), event(1, 1))),
            result.value.relationships,
        )
    }

    @Test
    fun testWedgeIdsFollowSourceOrderWhenOffsetsReverseDisplayOrder() {
        val xml = document(
            direction("<wedge type='crescendo'/>", "<offset>2</offset>") +
                direction("<wedge type='diminuendo' number='2'/>") + note("C") + note("D") +
                direction("<wedge type='stop' number='2'/>") + direction("<wedge type='stop'/>") + note("E"),
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf(
                Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 1), event(1, 6)),
                Relationship("P1:wedge-2", RelationshipKind.WEDGE, event(1, 2), event(1, 5)),
            ),
            result.value.relationships,
        )
    }

    @Test
    fun testWedgeIdsFollowSourceOrderWithinOneDirection() {
        val xml = document(
            "<direction><direction-type><wedge type='crescendo'/></direction-type>" +
                "<direction-type><wedge type='stop' number='2'/></direction-type></direction>" +
                note("C") + direction("<wedge type='stop'/>") + note("D"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf(DiagnosticCode.UNMATCHED_SPAN), result.diagnostics.map { it.code })
        assertEquals(
            listOf(
                Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 1), event(1, 3)),
                Relationship("P1:wedge-2", RelationshipKind.WEDGE, null, event(1, 1)),
            ),
            result.value.relationships,
        )
    }

    @Test
    fun testWedgeBeforeNoteSpanKeepsSourceOrder() {
        val xml = document(
            direction("<wedge type='crescendo'/>") + note("C", "<slur type='start'/>") +
                note("D", "<slur type='stop'/>") + direction("<wedge type='stop'/>") + note("E"),
        )
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf(
                Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 1), event(1, 4)),
                Relationship("P1:slur-1", RelationshipKind.SLUR, event(1, 2), event(1, 3)),
            ),
            result.value.relationships,
        )
        assertEquals(
            "/score-partwise[1]/part[1]/measure[1]/direction[1]/direction-type[1]/wedge[1]",
            result.provenance.single { it.modelPath == "relationships[0].startEventId" }.location.path,
        )
    }

    @Test
    fun testUnsupportedVariantsAreDiscardedWithFeatureIds() {
        val xml = document(
            direction("<rehearsal>A</rehearsal><words font-weight='bold'>cresc.</words>") +
                direction(
                    "<metronome><beat-unit>quarter</beat-unit><beat-unit>half</beat-unit></metronome>",
                    "<sound tempo='60' damper-pedal='yes'><swing/></sound><listening/>",
                ) +
                direction("<segno/>") +
                note(
                    "C",
                    "<articulations><strong-accent type='down'/><breath-mark/></articulations>" +
                        "<ornaments><wavy-line type='start'/><turn/></ornaments><fermata/>",
                ),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf("S13", "S13", "S13", "S26", "S26", "S26", "S13", "S12", "S12", "S12", "S12"),
            result.diagnostics.map { it.unsupported?.featureId },
        )
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        val events = result.value.parts.single().measures.single().events
        // The segno-only direction has nothing interpretable and produces no event.
        assertEquals(3, events.size)
        assertEquals(listOf(DirectionMark.Words("cresc.")), assertIs<DirectionEvent>(events[0]).marks)
        val tempoOnly = assertIs<DirectionEvent>(events[1])
        assertTrue(tempoOnly.marks.isEmpty())
        assertEquals(DirectionSound(tempo = Rational.of(60)), tempoOnly.sound)
        assertEquals(
            listOf(NoteExpression.Articulation(ArticulationType.STRONG_ACCENT), NoteExpression.Ornament(OrnamentType.TURN)),
            assertIs<NoteEvent>(events[2]).notation.expressions,
        )
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testInvalidDirectionAndExpressionValuesAreFatal() {
        for (policy in ParsePolicy.entries) {
            val options = ParseOptions(policy)
            listOf(
                direction("<wedge type='louder'/>"),
                direction("<wedge type='crescendo' number='17'/>"),
                direction("<words>x</words>", attributes = " placement='middle'"),
                direction("<dynamics placement='sideways'><p/></dynamics>"),
                direction("<words>x</words>", attributes = " directive='maybe'"),
                direction("<metronome><beat-unit>crotchet</beat-unit><per-minute>60</per-minute></metronome>"),
                direction("<metronome><beat-unit>quarter</beat-unit><per-minute> </per-minute></metronome>"),
                direction("<words>x</words>", "<sound tempo='-1'/>"),
                direction("<words>x</words>", "<sound dynamics='loud'/>"),
                direction("<words>x</words>", "<offset>ahead</offset>"),
                direction("<words>x</words>", "<staff>0</staff>"),
                note("C", "<articulations><staccato placement='left'/></articulations>"),
            ).forEach { failure(document(it), DiagnosticCode.INVALID_VALUE, options) }
            listOf(
                "<direction><sound tempo='60'/></direction>",
                "<direction><direction-type/><sound tempo='60'/></direction>",
                "<direction><direction-type/></direction>",
                direction("<wedge/>"),
                direction("<metronome><beat-unit>quarter</beat-unit></metronome>"),
                direction("<words>x</words>", "<offset>1</offset><offset>2</offset>"),
            ).forEach { failure(document(it), DiagnosticCode.INVALID_STRUCTURE, options) }
            // An offset with no divisions assumes 1, a repair that only tolerant parsing accepts.
            val noDivisions = document("").replace("<attributes><divisions>1</divisions></attributes>", direction("<words>x</words>", "<offset>1</offset>"))
            if (policy == ParsePolicy.STRICT) {
                failure(noDivisions, DiagnosticCode.RECOVERED_VALUE, options)
            } else {
                assertEquals(listOf(DiagnosticCode.RECOVERED_VALUE), success(noDivisions, options).diagnostics.map { it.code })
            }
        }
    }

    @Test
    fun testDirectionDynamicsIgnoreTheirOwnPlacement() {
        val xml = document(direction("<dynamics placement='below'><p/></dynamics>", attributes = " placement='above'"))
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val direction = assertIs<DirectionEvent>(result.value.parts.single().measures.single().events.single())
        assertEquals(listOf(Dynamics(listOf("p"))), direction.marks)
        assertEquals(Placement.ABOVE, direction.display.placement)
    }

    @Test
    fun testPlainNoteAndMinimalDirectionKeepDefaults() {
        val events = success(document(direction("<words></words>") + note("C"))).value.parts.single().measures.single().events
        val direction = assertIs<DirectionEvent>(events.first())
        assertEquals(listOf(DirectionMark.Words("")), direction.marks)
        assertNull(direction.voice)
        assertNull(direction.sound)
        assertEquals(DirectionDisplay(), direction.display)
        assertTrue(assertIs<NoteEvent>(events.last()).notation.expressions.isEmpty())
    }

    @Test
    fun testOffsetStartDisplayedAfterStopDoesNotPairBackwards() {
        val xml = document(
            direction("<wedge type='crescendo'/>", "<offset>2</offset>") + note("C") +
                direction("<wedge type='stop'/>") + note("D") + note("E"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(
            listOf(DiagnosticCode.UNMATCHED_SPAN, DiagnosticCode.UNMATCHED_SPAN),
            result.diagnostics.map { it.code },
        )
        assertEquals(
            listOf(
                Relationship("P1:wedge-1", RelationshipKind.WEDGE, event(1, 1), null),
                Relationship("P1:wedge-2", RelationshipKind.WEDGE, null, event(1, 3)),
            ),
            result.value.relationships,
        )
        failure(xml, DiagnosticCode.UNMATCHED_SPAN)
    }

    private fun event(measure: Int, index: Int) = "P1:measure-$measure:event-$index"

    private fun direction(types: String, children: String = "", attributes: String = ""): String =
        "<direction$attributes><direction-type>$types</direction-type>$children</direction>"

    /** A one-division quarter note with optional notations. */
    private fun note(step: String, notations: String = ""): String =
        "<note><pitch><step>$step</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice>" +
            (if (notations.isEmpty()) "" else "<notations>$notations</notations>") + "</note>"

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
