package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F21: each MusicXML 4.0 addition is either mapped or reported; see `docs/musicxml/version-40-changes.md`. */
class VersionFourImportTest {
    @Test
    fun testF21UnsupportedAdditionsAreEachReported() {
        val result = assertIs<ParseResult.Success<Score>>(parseMusicXml(f21VersionFourAdditions.encodeToByteArray(), TOLERANT))
        assertTrue(result.diagnostics.all { it.code == DiagnosticCode.UNSUPPORTED_FEATURE }, result.diagnostics.toString())
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        assertTrue(result.diagnostics.all { it.severity == DiagnosticSeverity.WARNING })
        val measure = "/score-partwise[1]/part[1]/measure[1]"
        val scorePart = "/score-partwise[1]/part-list[1]/score-part[1]"
        assertEquals(
            listOf(
                "S25" to "$scorePart/part-link[1]",
                "S16" to "$scorePart/score-instrument[1]/instrument-sound[1]",
                "S26" to "$scorePart/player[1]",
                "S17" to "$measure/print[1]/measure-numbering[1]",
                "S17" to "$measure/attributes[1]/staff-details[1]/line-detail[1]",
                "S17" to "$measure/attributes[1]/staff-details[1]/staff-size[1]",
                "S16" to "$measure/attributes[1]/transpose[1]/double[1]",
                "S16" to "$measure/attributes[1]/for-part[1]",
                "S24" to "$measure/attributes[1]/measure-style[1]/multiple-rest[1]",
                "S26" to "$measure/listening[1]",
                "S26" to "$measure/sound[1]",
                "S13" to "$measure/direction[1]/@system",
                "S13" to "$measure/direction[1]/direction-type[1]/metronome[1]/@print-object",
                "S13" to "$measure/direction[1]/direction-type[2]/words[1]/@enclosure",
                "S13" to "$measure/direction[1]/direction-type[3]/pedal[1]",
                "S13" to "$measure/direction[1]/direction-type[4]/pedal[1]",
                "S13" to "$measure/direction[1]/direction-type[5]/percussion[1]",
                "S26" to "$measure/direction[1]/sound[1]/swing[1]",
                "S26" to "$measure/direction[1]/listening[1]",
                "S22" to "$measure/harmony[1]",
                "S22" to "$measure/figured-bass[1]",
                "S16" to "$measure/note[1]/instrument[2]",
                "S04" to "$measure/note[1]/accidental[1]/@smufl",
                "S12" to "$measure/note[1]/notations[1]/arpeggiate[1]",
                "S12" to "$measure/note[1]/notations[1]/ornaments[1]/wavy-line[1]",
                "S12" to "$measure/note[1]/notations[1]/technical[1]",
                "S25" to "$measure/note[1]/level[1]",
                "S26" to "$measure/note[1]/play[1]",
                "S26" to "$measure/note[1]/listen[1]",
                "S15" to "$measure/barline[2]/ending[1]/@system",
                "S15" to "$measure/barline[2]/repeat[1]/@after-jump",
            ),
            result.diagnostics.map { checkNotNull(it.unsupported).featureId to it.location.path },
        )
        assertTrue(result.value.validate().isEmpty())
    }

    @Test
    fun testF21SupportedAdditionsAreMapped() {
        val result = assertIs<ParseResult.Success<Score>>(parseMusicXml(f21VersionFourAdditions.encodeToByteArray(), TOLERANT))
        val score = result.value
        // The 4.0 `part name` credit-type value is kept as source text.
        assertEquals(listOf(MetadataEntry.Credit(listOf("Flute"), listOf("part name"), 1)), score.metadata)
        val events = score.parts.single().measures.single().events
        // The deprecated `none` clef sign is still valid 4.0 and stays a clef without a staff line.
        val clef = assertIs<Attribute.Clef>(events.filterIsInstance<AttributeEvent>().first().attribute)
        assertEquals(ClefSign.NONE, clef.sign)
        assertNull(clef.line)
        // A note with several instruments keeps its first; a slur may use the 4.0 maximum number-level 16.
        val notes = events.filterIsInstance<NoteEvent>()
        assertEquals(listOf("P1-I1", "P1-I1"), notes.map { it.instrumentId })
        val slur = score.relationships.single()
        assertEquals(RelationshipKind.SLUR, slur.kind)
        assertEquals(notes.map { it.id }, listOf(slur.startEventId, slur.endEventId))
    }

    @Test
    fun testF21AdditionsFailUnderStrictPolicy() {
        val result = assertIs<ParseResult.Failure>(parseMusicXml(f21VersionFourAdditions.encodeToByteArray()))
        assertEquals(31, result.diagnostics.size)
        assertTrue(result.diagnostics.all { it.code == DiagnosticCode.UNSUPPORTED_FEATURE && it.severity == DiagnosticSeverity.ERROR })
    }

    @Test
    fun testNumberLevelAboveSixteenIsInvalid() {
        val xml = f21VersionFourAdditions.replace("number=\"16\"", "number=\"17\"")
        val result = assertIs<ParseResult.Failure>(parseMusicXml(xml.encodeToByteArray(), TOLERANT))
        assertTrue(result.diagnostics.any { it.code == DiagnosticCode.INVALID_VALUE && "1 to 16" in it.message })
    }

    private companion object {
        val TOLERANT = ParseOptions(ParsePolicy.TOLERANT)
    }
}
