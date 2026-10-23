package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteRole
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.SoundingPitch
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** MX-21 mixed-feature pairs: supported content stays exact next to other features. */
class MixedFeatureImportTest {
    @Test
    fun testDivisionsChangeBeforeBackupKeepsExactTiming() {
        val result = success(mixedDivisionsAndBackup, TOLERANT)
        val measure = result.value.parts.single().measures.single()
        val notes = measure.events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(Rational.ZERO, Rational.ONE), notes.map { it.onset })
        assertEquals(listOf(Rational.ONE, Rational.of(1, 2)), notes.map { it.duration })
        assertEquals(Rational.of(3, 2), measure.extent)
        assertEquals(
            listOf("S06" to "$MEASURE/backup[1]", "S06" to "$MEASURE/note[3]"),
            features(result),
        )
        rejectedWhenStrict(mixedDivisionsAndBackup)
    }

    @Test
    fun testTiedChordChangingStaffKeepsStaffOneTie() {
        val result = success(mixedTiedChordAcrossStaves, TOLERANT)
        val part = result.value.parts.single()
        val notes = part.measures.map { it.events.filterIsInstance<NoteEvent>().single() }
        assertTrue(notes.all { it.staff == 1 && it.duration == Rational.of(2) })
        assertEquals(
            listOf(RelationshipKind.SOUND_TIE, RelationshipKind.NOTATED_TIE),
            result.value.relationships.map { it.kind },
        )
        assertTrue(result.value.relationships.all { it.startEventId == notes[0].id && it.endEventId == notes[1].id })
        assertTrue(result.value.validate().isEmpty())
        assertEquals(
            listOf(
                "S08" to "$MEASURE/attributes[1]/staves[1]",
                "S06" to "$MEASURE/note[2]/chord[1]",
                "S06" to "/score-partwise[1]/part[1]/measure[2]/note[2]/chord[1]",
            ),
            features(result),
        )
        rejectedWhenStrict(mixedTiedChordAcrossStaves)
    }

    @Test
    fun testGraceWithLyricsStaysLinkedAndUntimed() {
        val result = success(mixedGraceAndLyric, TOLERANT)
        val (grace, principal) = result.value.parts.single().measures.single().events.map { assertIs<NoteEvent>(it) }
        assertEquals(NoteRole.GRACE, grace.role)
        assertEquals(null, grace.duration)
        assertEquals(Rational.ZERO, principal.onset)
        assertEquals(Rational.ONE, principal.duration)
        val link = result.value.relationships.single()
        assertEquals(RelationshipKind.GRACE_NOTE, link.kind)
        assertEquals(grace.id to principal.id, link.startEventId to link.endEventId)
        assertEquals(
            listOf("S14" to "$MEASURE/note[1]/lyric[1]", "S14" to "$MEASURE/note[2]/lyric[1]"),
            features(result),
        )
        rejectedWhenStrict(mixedGraceAndLyric)
    }

    @Test
    fun testMidMeasureTranspositionAfterDivisionsChangeDerivesSoundingPitch() {
        val result = success(mixedTranspositionAndDivisions)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val notes = result.value.parts.single().measures.single().events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(Rational.ZERO, Rational.ONE), notes.map { it.onset })
        assertEquals(listOf(Rational.ONE, Rational.of(1, 2)), notes.map { it.duration })
        assertEquals(
            listOf(SoundingPitch(Rational.of(72)), SoundingPitch(Rational.of(62))),
            notes.map { assertIs<NotePitch.Pitched>(it.pitch).sounding },
        )
    }

    @Test
    fun testTimewiseTwoStavesMatchesPartwise() {
        val timewise = success(mixedTimewiseTwoStaves, TOLERANT)
        val partwise = success(mixedPartwiseTwoStaves, TOLERANT)
        assertEquals(partwise.value, timewise.value)
        assertEquals(features(partwise).map { it.first }, features(timewise).map { it.first })
        assertEquals(listOf("S08", "S06", "S06"), features(timewise).map { it.first })
        val note = assertIs<NoteEvent>(timewise.value.parts.single().measures.single().events.single())
        assertEquals(Rational.of(4), note.duration)
        rejectedWhenStrict(mixedTimewiseTwoStaves)
    }

    private fun features(result: ParseResult<Score>): List<Pair<String?, String?>> =
        result.diagnostics.map { it.unsupported?.featureId to it.location.path }

    private fun rejectedWhenStrict(xml: String) {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray()))
        assertTrue(result.diagnostics.all { it.code == DiagnosticCode.UNSUPPORTED_FEATURE && it.severity == DiagnosticSeverity.ERROR })
    }

    private fun success(xml: String, options: ParseOptions = ParseOptions()): ParseResult.Success<Score> {
        val result = parseMusicXml(xml.encodeToByteArray(), options)
        return assertIs<ParseResult.Success<Score>>(result, (result as? ParseResult.Failure)?.diagnostics.toString())
    }

    private companion object {
        val TOLERANT = ParseOptions(ParsePolicy.TOLERANT)
        const val MEASURE = "/score-partwise[1]/part[1]/measure[1]"
    }
}
