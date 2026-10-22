package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Beam
import com.ryanjames.sheetmusic.score.BeamType
import com.ryanjames.sheetmusic.score.NotatedValue
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Relationship
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.StemDirection
import com.ryanjames.sheetmusic.score.TimeModification
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TupletImportTest {
    @Test
    fun testF09KeepsBeamsStemsAndExactTripletTiming() {
        val result = success(f09BeamsAndTriplet)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (first, second) = result.value.parts.single().measures
        val triplet = TimeModification(3, 2)
        val notes = first.events.filterIsInstance<NoteEvent>()
        assertEquals(
            listOf(Rational.ZERO, Rational.of(1, 2), Rational.ONE, Rational.of(4, 3), Rational.of(5, 3)),
            notes.map { it.onset },
        )
        assertEquals(listOf(Rational.of(1, 2), Rational.of(1, 2)) + List(3) { Rational.of(1, 3) }, notes.map { it.duration })
        assertEquals(listOf(
            NotatedValue(NoteValue.EIGHTH, stem = StemDirection.UP, beams = listOf(Beam(1, BeamType.BEGIN))),
            NotatedValue(NoteValue.EIGHTH, stem = StemDirection.UP, beams = listOf(Beam(1, BeamType.END))),
            NotatedValue(NoteValue.EIGHTH, 0, triplet, stem = StemDirection.DOWN, beams = listOf(Beam(1, BeamType.BEGIN))),
            NotatedValue(NoteValue.EIGHTH, 0, triplet, stem = StemDirection.DOWN, beams = listOf(Beam(1, BeamType.CONTINUE))),
            NotatedValue(NoteValue.EIGHTH, 0, triplet, stem = StemDirection.DOWN, beams = listOf(Beam(1, BeamType.END))),
        ), notes.map { it.notation })
        assertEquals(Rational.of(2), first.extent)

        val (dotted, sixteenth, rest) = second.events
        assertEquals(Rational.of(3, 4), assertIs<NoteEvent>(dotted).duration)
        assertEquals(NotatedValue(NoteValue.EIGHTH, 1, stem = StemDirection.DOWN, beams = listOf(Beam(1, BeamType.BEGIN))), dotted.notation)
        assertEquals(Rational.of(3, 4), sixteenth.onset)
        assertEquals(
            listOf(Beam(1, BeamType.END), Beam(2, BeamType.BACKWARD_HOOK)),
            assertIs<NoteEvent>(sixteenth).notation.beams,
        )
        assertEquals(Rational.ONE to Rational.ONE, rest.onset to assertIs<RestEvent>(rest).duration)
        assertEquals(Rational.of(2), second.extent)

        assertEquals(
            listOf(Relationship("P1:tuplet-1", RelationshipKind.TUPLET, "P1:measure-1:event-4", "P1:measure-1:event-6")),
            result.value.relationships,
        )
        assertTrue(result.value.validate().isEmpty())
        assertEquals(
            "/score-partwise[1]/part[1]/measure[1]/note[5]/notations[1]/tuplet[1]",
            result.provenance.single { it.modelPath == "relationships[0].endEventId" }.location.path,
        )
    }

    @Test
    fun testF11NestedTupletsKeepCumulativeRatiosAndExactOffsets() {
        val result = success(f11NestedTuplets)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (pickup, full) = result.value.parts.single().measures
        assertTrue(pickup.implicit)
        val outer = pickup.events.filterIsInstance<NoteEvent>()
        assertEquals(
            listOf(Rational.ZERO, Rational.of(2, 3), Rational.of(8, 9), Rational.of(10, 9), Rational.of(4, 3)),
            outer.map { it.onset },
        )
        assertEquals(
            listOf(Rational.of(2, 3), Rational.of(2, 9), Rational.of(2, 9), Rational.of(2, 9), Rational.of(2, 3)),
            outer.map { it.duration },
        )
        assertEquals(
            listOf(TimeModification(3, 2)) + List(3) { TimeModification(9, 4, NoteValue.EIGHTH) } + TimeModification(3, 2),
            outer.map { it.notation.timeModification },
        )
        // Exact sums: the pickup is exactly two quarters with no residue from thirds or ninths.
        assertEquals(Rational.of(2), outer.fold(Rational.ZERO) { sum, note -> sum + note.duration!! })
        assertEquals(Rational.of(2), pickup.extent)

        val notes = full.events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(Rational.ZERO, Rational.of(3), Rational.of(10, 3), Rational.of(11, 3)), notes.map { it.onset })
        assertEquals(NotatedValue(NoteValue.HALF, 1), notes.first().notation)
        assertEquals(Rational.of(3), notes.first().duration)
        assertEquals(Rational.of(4), full.extent)

        assertEquals(listOf(
            Relationship("P1:tuplet-1", RelationshipKind.TUPLET, "P1:measure-1:event-2", "P1:measure-1:event-6"),
            Relationship("P1:tuplet-2", RelationshipKind.TUPLET, "P1:measure-1:event-3", "P1:measure-1:event-5"),
            Relationship("P1:tuplet-3", RelationshipKind.TUPLET, "P1:measure-2:event-2", "P1:measure-2:event-4"),
        ), result.value.relationships)
        assertTrue(result.value.validate().isEmpty())
    }

    @Test
    fun testTupletsPairByNumberAcrossRestsAndBarlines() {
        val xml = "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name></score-part></part-list>" +
            "<part id='P1'><measure number='1'><attributes><divisions>3</divisions></attributes>" +
            "<note><rest/><duration>2</duration><time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes>" +
            "</time-modification><notations><tuplet type='start'/></notations></note></measure>" +
            "<measure number='2'>${note("C", "<tuplet type='stop'/>")}</measure></part></score-partwise>"
        val result = success(xml)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(Rational.of(2, 3), assertIs<RestEvent>(result.value.parts.single().measures.first().events.single()).duration)
        assertEquals(
            listOf(Relationship("P1:tuplet-1", RelationshipKind.TUPLET, "P1:measure-1:event-1", "P1:measure-2:event-1")),
            result.value.relationships,
        )
    }

    @Test
    fun testInvalidBeamStemTupletAndTimeModificationValuesAreFatal() {
        for (policy in ParsePolicy.entries) {
            val options = ParseOptions(policy)
            listOf(
                note("C", "<tuplet type='start' number='17'/>"),
                note("C", "<tuplet type='continue'/>"),
                note("C", "<tuplet type='start' number='0'/>"),
                note("C", stem = "<stem>sideways</stem>"),
                note("C", beams = "<beam number='9'>begin</beam>"),
                note("C", beams = "<beam>middle</beam>"),
                note("C", modification = "<time-modification><actual-notes>0</actual-notes><normal-notes>2</normal-notes></time-modification>"),
                note("C", modification = "<time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes>" +
                    "<normal-type>triplet</normal-type></time-modification>"),
            ).forEach { failure(document(it), DiagnosticCode.INVALID_VALUE, options) }
            listOf(
                note("C", "<tuplet/>"),
                note("C", stem = "<stem>up</stem><stem>down</stem>"),
                note("C", beams = "<beam>begin</beam><beam number='1'>end</beam>"),
                note("C", modification = "<time-modification><actual-notes>3</actual-notes></time-modification>"),
                note("C", modification = "<time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes>" +
                    "<normal-dot/></time-modification>"),
            ).forEach { failure(document(it), DiagnosticCode.INVALID_STRUCTURE, options) }
        }
    }

    @Test
    fun testTupletDisplayAndBeamPresentationAreDiscardedWithFeatureIds() {
        val xml = document(
            note(
                "C",
                "<tuplet type='start' bracket='yes'><tuplet-actual><tuplet-number>3</tuplet-number></tuplet-actual></tuplet>",
                stem = "<stem default-y='10'>double</stem>",
                beams = "<beam fan='accel'>begin</beam>",
            ) + note("D", "<tuplet type='start' number='2'/>", stem = "<stem>none</stem>", beams = "<beam>forward hook</beam>"),
        )
        val result = success(xml, ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S10", "S10", "S11", "S11"), result.diagnostics.mapNotNull { it.unsupported?.featureId })
        assertTrue(result.diagnostics.filter { it.unsupported != null }.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        // Both tuplets stay open, so each start is reported as unmatched and kept with a null end.
        assertEquals(List(2) { DiagnosticCode.UNMATCHED_SPAN }, result.diagnostics.filter { it.unsupported == null }.map { it.code })
        val notes = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(StemDirection.DOUBLE, StemDirection.NONE), notes.map { it.notation.stem })
        assertEquals(listOf(listOf(Beam(1, BeamType.BEGIN)), listOf(Beam(1, BeamType.FORWARD_HOOK))), notes.map { it.notation.beams })
        assertEquals(listOf(event(1) to null, event(2) to null), result.value.relationships.map { it.startEventId to it.endEventId })
        failure(xml, DiagnosticCode.UNSUPPORTED_FEATURE)
    }

    @Test
    fun testNotesWithoutNotationKeepDefaultFlags() {
        val note = assertIs<NoteEvent>(success(document(note("C"))).value.parts.single().measures.single().events.single())
        assertNull(note.notation.stem)
        assertTrue(note.notation.beams.isEmpty())
        assertNull(note.notation.timeModification)
    }

    private fun event(index: Int) = "P1:measure-1:event-$index"

    /** A one-division quarter note with optional time modification, stem, beams and tuplet notations. */
    private fun note(
        step: String,
        tuplets: String = "",
        modification: String = "",
        stem: String = "",
        beams: String = "",
    ): String =
        "<note><pitch><step>$step</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice>" +
            "$modification$stem$beams${if (tuplets.isEmpty()) "" else "<notations>$tuplets</notations>"}</note>"

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
