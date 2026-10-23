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
        val result = success(mixedDivisionsAndBackup)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val measure = result.value.parts.single().measures.single()
        val notes = measure.events.filterIsInstance<NoteEvent>()
        // The backup is measured in the divisions in effect when it is read.
        assertEquals(listOf(Rational.ZERO, Rational.ONE, Rational.ZERO), notes.map { it.onset })
        assertEquals(listOf(Rational.ONE, Rational.of(1, 2), Rational.of(3, 2)), notes.map { it.duration })
        assertEquals(listOf("1", "1", "2"), notes.map { it.voice })
        assertEquals(Rational.of(3, 2), measure.extent)
    }

    @Test
    fun testTiedChordChangingStaffKeepsBothTies() {
        val result = success(mixedTiedChordAcrossStaves)
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val part = result.value.parts.single()
        val (first, second) = part.measures.map { measure -> measure.events.filterIsInstance<NoteEvent>() }
        for (chord in listOf(first, second)) {
            assertEquals(listOf(1, 2), chord.map { it.staff })
            assertTrue(chord.all { it.duration == Rational.of(2) && it.onset == Rational.ZERO })
            assertEquals(1, chord.map { it.chordId }.distinct().size)
        }
        // Each tone's sound and notated ties link it to the same pitch in the next measure.
        val links = result.value.relationships.map { it.kind to (it.startEventId to it.endEventId) }.toSet()
        assertEquals(
            first.indices.flatMap { index ->
                listOf(RelationshipKind.SOUND_TIE, RelationshipKind.NOTATED_TIE).map { it to (first[index].id to second[index].id) }
            }.toSet(),
            links,
        )
        assertEquals(4, result.value.relationships.size)
        assertTrue(result.value.validate().isEmpty())
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
        val timewise = success(mixedTimewiseTwoStaves)
        val partwise = success(mixedPartwiseTwoStaves)
        assertEquals(partwise.value, timewise.value)
        assertTrue(timewise.diagnostics.isEmpty() && partwise.diagnostics.isEmpty())
        val notes = timewise.value.parts.single().measures.single().events.map { assertIs<NoteEvent>(it) }
        assertEquals(listOf(Triple(Rational.ZERO, 1, "1"), Triple(Rational.ZERO, 2, "5")), notes.map { Triple(it.onset, it.staff, it.voice) })
        assertTrue(notes.all { it.duration == Rational.of(4) })
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
