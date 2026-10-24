package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Accidental
import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.AttributeState
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.Event
import com.ryanjames.sheetmusic.score.InterchangeableTime
import com.ryanjames.sheetmusic.score.KeyAlteration
import com.ryanjames.sheetmusic.score.KeyCancel
import com.ryanjames.sheetmusic.score.KeyCancelLocation
import com.ryanjames.sheetmusic.score.KeyMode
import com.ryanjames.sheetmusic.score.KeyOctave
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.SenzaMisura
import com.ryanjames.sheetmusic.score.SoundingPitch
import com.ryanjames.sheetmusic.score.Staff
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.TimeFraction
import com.ryanjames.sheetmusic.score.TimeSeparator
import com.ryanjames.sheetmusic.score.TimeSymbol
import com.ryanjames.sheetmusic.score.WrittenPitch
import com.ryanjames.sheetmusic.score.effectiveStates
import com.ryanjames.sheetmusic.score.validate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AttributeImportTest {
    private val cMajor = Attribute.KeySignature(0, KeyMode.MAJOR)
    private val gMajor = Attribute.KeySignature(1, KeyMode.MAJOR)
    private val treble = Attribute.Clef(ClefSign.G, 2)
    private val bass = Attribute.Clef(ClefSign.F, 4)

    @Test
    fun testF07EachEventSeesStateActiveAtItsOffset() {
        val result = success(f07MidMeasureAttributeChanges)
        assertTrue(result.diagnostics.isEmpty())
        assertTrue(result.value.validate().isEmpty())
        val part = result.value.parts.single()
        val (first, second, third) = part.measures
        assertEquals(
            listOf(cMajor, Attribute.TimeSignature(4, 4), treble, gMajor, Attribute.TimeSignature(2, 4), bass),
            first.events.filterIsInstance<AttributeEvent>().map { it.attribute },
        )
        assertEquals(listOf(0, 0, 0, 0, 1, 2, 2, 2, 2, 3).map { Rational.of(it) }, first.events.map { it.onset })
        assertEquals(Rational.of(4), first.extent)

        val states = part.effectiveStates()
        val before = AttributeState(Attribute.TimeSignature(4, 4), cMajor, treble)
        val after = AttributeState(Attribute.TimeSignature(2, 4), gMajor, bass)
        val notes = first.events.filterIsInstance<NoteEvent>()
        assertEquals(listOf(before, before, after, after), notes.map { states[it.id] })
        assertEquals(after, states[second.events[0].id])
        val endClef = assertIs<AttributeEvent>(second.events[1])
        assertEquals(Rational.of(2), endClef.onset)
        assertEquals(Rational.of(2), second.extent)
        assertEquals(after.copy(clef = treble), states[third.events.single().id])

        // The key never rewrites written alter or infers a printed accidental.
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.F, 3, Rational.ONE), SoundingPitch(Rational.of(54))), notes[2].pitch)
        assertNull(notes[2].notation.accidental)
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.F, 3), SoundingPitch(Rational.of(53))), notes[3].pitch)
        assertEquals(Accidental("natural"), notes[3].notation.accidental)

        val mapping = result.provenance.associateBy { it.modelPath }
        assertEquals("/score-partwise[1]/part[1]/measure[1]/attributes[2]/clef[1]",
            mapping["parts[0].measures[0].events[7]"]?.location?.path)
    }

    @Test
    fun testMissingStateStaysUnknown() {
        val bare = success(document("<note><rest/><duration>1</duration></note>"))
        assertEquals(AttributeState(), bare.value.parts.single().effectiveStates().values.single())
        val keyOnly = success(document("<attributes><key><fifths>-3</fifths></key></attributes><note><rest/><duration>1</duration></note>"))
        val states = keyOnly.value.parts.single().effectiveStates()
        assertEquals(AttributeState(key = Attribute.KeySignature(-3)), states[events(keyOnly)[1].id])
    }

    @Test
    fun testKeyCancelAndOctavesAreDisplayOnly() {
        val result = success(document(
            "<attributes><key><cancel location='before-barline'>-3</cancel><fifths>2</fifths>" +
                "<key-octave number='1' cancel='yes'>5</key-octave><key-octave number='2'>4</key-octave></key></attributes>" +
                "<note><pitch><step>F</step><octave>4</octave></pitch><duration>1</duration></note>",
        ))
        assertTrue(result.diagnostics.isEmpty())
        val key = Attribute.KeySignature(
            2, cancel = KeyCancel(-3, KeyCancelLocation.BEFORE_BARLINE), octaves = listOf(KeyOctave(1, 5, cancel = true), KeyOctave(2, 4)),
        )
        assertEquals(key, assertIs<AttributeEvent>(events(result)[0]).attribute)
        val note = assertIs<NoteEvent>(events(result)[1])
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.F, 4), SoundingPitch(Rational.of(65))), note.pitch)
        assertEquals(key, result.value.parts.single().effectiveStates().getValue(note.id).key)
        failure(document("<attributes><key><cancel location='middle'>1</cancel><fifths>0</fifths></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><cancel>x</cancel><fifths>0</fifths></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><cancel>1</cancel><cancel>2</cancel><fifths>0</fifths></key></attributes>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<attributes><key><cancel>1</cancel></key></attributes>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<attributes><key><fifths>0</fifths><key-octave>4</key-octave></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><fifths>0</fifths><key-octave number='0'>4</key-octave></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><fifths>0</fifths><key-octave number='1'>10</key-octave></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><fifths>0</fifths><key-octave number='1' cancel='maybe'>4</key-octave></key></attributes>"), DiagnosticCode.INVALID_VALUE)
    }

    @Test
    fun testNonTraditionalKeysKeepEveryAlterationInOrder() {
        val result = success(document(
            "<attributes><key><key-step>B</key-step><key-alter>-0.5</key-alter><key-accidental>slash-flat</key-accidental>" +
                "<key-step>E</key-step><key-alter>-1</key-alter><key-step>F</key-step><key-alter>1.5</key-alter>" +
                "<key-octave number='3'>5</key-octave></key></attributes>" +
                "<note><pitch><step>B</step><octave>4</octave></pitch><duration>1</duration></note>",
        ))
        assertTrue(result.diagnostics.isEmpty())
        val key = Attribute.KeySignature.nonTraditional(
            listOf(
                KeyAlteration(Step.B, Rational.of(-1, 2), "slash-flat"),
                KeyAlteration(Step.E, Rational.of(-1)),
                KeyAlteration(Step.F, Rational.of(3, 2)),
            ),
            listOf(KeyOctave(3, 5)),
        )
        assertEquals(key, assertIs<AttributeEvent>(events(result)[0]).attribute)
        assertTrue(key.isNonTraditional)
        assertNull(key.fifths)
        val note = assertIs<NoteEvent>(events(result)[1])
        assertEquals(NotePitch.Pitched(WrittenPitch(Step.B, 4), SoundingPitch(Rational.of(71))), note.pitch)
        assertNull(note.notation.accidental)
    }

    @Test
    fun testMalformedNonTraditionalKeys() {
        fun key(content: String) = document("<attributes><key>$content</key></attributes>")
        failure(key("<key-step>F</key-step>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(key("<key-step>F</key-step><key-step>G</key-step><key-alter>1</key-alter>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(key("<key-alter>1</key-alter>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(key("<key-accidental>sharp</key-accidental><key-step>F</key-step><key-alter>1</key-alter>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(key("<key-step>F</key-step><key-alter>1</key-alter><key-octave number='1'>4</key-octave><key-accidental>sharp</key-accidental>"),
            DiagnosticCode.INVALID_STRUCTURE)
        failure(key("<fifths>1</fifths><key-step>F</key-step><key-alter>1</key-alter>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(key("<key-step>H</key-step><key-alter>1</key-alter>"), DiagnosticCode.INVALID_VALUE)
        failure(key("<key-step>F</key-step><key-alter>sharp</key-alter>"), DiagnosticCode.INVALID_VALUE)
        failure(key("<key-step>F</key-step><key-alter>1</key-alter><key-accidental> </key-accidental>"), DiagnosticCode.INVALID_VALUE)
        val smufl = success(
            key("<key-step>F</key-step><key-alter>1</key-alter><key-accidental smufl='accSagittal5v7KleismaUp'>other</key-accidental>"),
            ParseOptions(ParsePolicy.TOLERANT),
        )
        assertEquals(listOf("S07"), smufl.diagnostics.map { it.unsupported?.featureId })
        assertEquals(
            Attribute.KeySignature.nonTraditional(listOf(KeyAlteration(Step.F, Rational.ONE, "other"))),
            assertIs<AttributeEvent>(events(smufl).single()).attribute,
        )
    }

    @Test
    fun testKeyFormsAndUnsupportedKeyContent() {
        val result = success(document(
            "<attributes><key print-object='no'><cancel>2</cancel><fifths>-2</fifths><mode>minor</mode>" +
                "<key-octave number='1'>4</key-octave></key>" +
                "<key><fifths>3</fifths><mode>hypodorian</mode></key>" +
                "<key><key-step>F</key-step><key-alter>1</key-alter></key></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07", "S07"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(result.diagnostics.all { it.unsupported?.retention == ContentRetention.DISCARDED })
        assertEquals(
            listOf(
                Attribute.KeySignature(-2, KeyMode.MINOR, KeyCancel(2), octaves = listOf(KeyOctave(1, 4))),
                Attribute.KeySignature(3),
                Attribute.KeySignature.nonTraditional(listOf(KeyAlteration(Step.F, Rational.ONE))),
            ),
            events(result).map { assertIs<AttributeEvent>(it).attribute },
        )
        failure(document("<attributes><key><mode>major</mode></key></attributes>"), DiagnosticCode.INVALID_STRUCTURE)
        failure(document("<attributes><key><fifths>sharp</fifths></key></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><key><fifths>1</fifths><fifths>2</fifths></key></attributes>"), DiagnosticCode.INVALID_STRUCTURE)
    }

    @Test
    fun testClefFormsAndStandardLines() {
        val result = success(document(
            "<attributes><clef><sign>G</sign></clef><clef after-barline='yes'><sign>F</sign><line>3</line></clef>" +
                "<clef><sign>G</sign><line>2</line><clef-octave-change>-1</clef-octave-change></clef>" +
                "<clef><sign>percussion</sign></clef><clef><sign>C</sign></clef></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(
            listOf(treble, Attribute.Clef(ClefSign.F, 3), Attribute.Clef(ClefSign.G, 2, -1),
                Attribute.Clef(ClefSign.PERCUSSION, null), Attribute.Clef(ClefSign.C, 3)),
            events(result).map { assertIs<AttributeEvent>(it).attribute },
        )
        for (policy in ParsePolicy.entries) {
            failure(document("<attributes><clef><sign>H</sign></clef></attributes>"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            failure(document("<attributes><clef><line>2</line></clef></attributes>"), DiagnosticCode.INVALID_STRUCTURE, ParseOptions(policy))
            failure(document("<attributes><clef><sign>G</sign><line>x</line></clef></attributes>"), DiagnosticCode.INVALID_VALUE,
                ParseOptions(policy))
            failure(document("<attributes><clef number='0'><sign>G</sign></clef></attributes>"), DiagnosticCode.INVALID_VALUE,
                ParseOptions(policy))
        }
    }

    @Test
    fun testTimeSymbolIsKeptWithTheMeter() {
        val times = listOf("common" to "4</beats><beat-type>4", "cut" to "2</beats><beat-type>2", "single-number" to "3</beats><beat-type>8")
            .joinToString("") { (symbol, meter) -> "<time symbol='$symbol'><beats>$meter</beat-type></time>" }
        val result = success(document("<attributes>$times<time><beats>6</beats><beat-type>8</beat-type></time></attributes>"))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        assertEquals(
            listOf(
                Attribute.TimeSignature(4, 4, TimeSymbol.COMMON),
                Attribute.TimeSignature(2, 2, TimeSymbol.CUT),
                Attribute.TimeSignature(3, 8, TimeSymbol.SINGLE_NUMBER),
                Attribute.TimeSignature(6, 8),
            ),
            events(result).map { assertIs<AttributeEvent>(it).attribute },
        )
        for (policy in ParsePolicy.entries) {
            failure(document("<attributes><time symbol='alla-breve'><beats>2</beats><beat-type>2</beat-type></time></attributes>"),
                DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
        }
    }

    @Test
    fun testComplexTimeFormsAreKeptWithTheirDisplay() {
        val result = success(document(
            "<attributes><time separator='horizontal'><beats> 3 + 2 </beats><beat-type>8</beat-type><beats>3</beats>" +
                "<beat-type>4</beat-type></time><time symbol='cut'><beats>2</beats><beat-type>2</beat-type>" +
                "<interchangeable symbol='normal' separator='none'><beats>4</beats><beat-type>4</beat-type></interchangeable></time>" +
                "<time><senza-misura> X </senza-misura></time></attributes>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val (additive, alternate, senzaMisura) = events(result).map { assertIs<Attribute.TimeSignature>(assertIs<AttributeEvent>(it).attribute) }
        assertEquals(
            Attribute.TimeSignature(listOf(TimeFraction(listOf(3, 2), 8), TimeFraction(listOf(3), 4)), separator = TimeSeparator.HORIZONTAL),
            additive,
        )
        assertEquals(Rational.of(11, 2), additive.length)
        assertEquals(
            Attribute.TimeSignature(2, 2, TimeSymbol.CUT).copy(
                interchangeable = InterchangeableTime(listOf(TimeFraction(listOf(4), 4)), null, TimeSymbol.NORMAL, TimeSeparator.NONE),
            ),
            alternate,
        )
        assertEquals(Rational.of(4), alternate.length)
        assertEquals(Attribute.TimeSignature(emptyList(), senzaMisura = SenzaMisura("X")), senzaMisura)
        assertNull(senzaMisura.length)
    }

    @Test
    fun testUnreadableInterchangeableIsDiscardedAlone() {
        val result = success(document(
            "<attributes><time><beats>3</beats><beat-type>4</beat-type><interchangeable><time-relation>space</time-relation>" +
                "<beats>6</beats></interchangeable></time></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S07"), result.diagnostics.map { it.unsupported?.featureId })
        assertEquals(Attribute.TimeSignature(3, 4), assertIs<AttributeEvent>(events(result).single()).attribute)
    }

    @Test
    fun testInvalidTimeValues() {
        for (policy in ParsePolicy.entries) {
            failure(document("<attributes><time separator='dotted'><beats>2</beats><beat-type>2</beat-type></time></attributes>"),
                DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            failure(document("<attributes><time><beats>2</beats><beat-type>2</beat-type><interchangeable><time-relation>dash" +
                "</time-relation><beats>4</beats><beat-type>4</beat-type></interchangeable></time></attributes>"),
                DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
            failure(document("<attributes><time><beats>2147483647+2147483647</beats><beat-type>1</beat-type></time></attributes>"),
                DiagnosticCode.NUMERIC_OVERFLOW, ParseOptions(policy))
        }
    }

    @Test
    fun testStavesAndStaffDetails() {
        val result = success(document(
            "<attributes><staves>1</staves><staff-details><staff-lines>1</staff-lines><staff-tuning line='1'><tuning-step>E</tuning-step>" +
                "<tuning-octave>2</tuning-octave></staff-tuning><staff-size>80</staff-size></staff-details>" +
                "<staff-details number='2'><staff-lines>5</staff-lines></staff-details><staves>2</staves></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S23", "S17"), result.diagnostics.map { it.unsupported?.featureId })
        // The later staves declaration in the same element scopes the unnumbered details to both staves.
        assertEquals(
            listOf(Attribute.StaffLines(1) to 1, Attribute.StaffLines(1) to 2, Attribute.StaffLines(5) to 2),
            events(result).map { assertIs<AttributeEvent>(it).let { event -> event.attribute to event.staff } },
        )
        assertEquals(listOf(Staff(1), Staff(2)), result.value.parts.single().staves)
        failure(document("<attributes><staves>0</staves></attributes>"), DiagnosticCode.INVALID_VALUE)
        failure(document("<attributes><staff-details><staff-lines>-1</staff-lines></staff-details></attributes>"),
            DiagnosticCode.INVALID_VALUE)
    }

    @Test
    fun testStavesDeclarationRetainsStaffOneWithoutEvents() {
        val declared = success(document("<attributes><staves>1</staves></attributes>")).value.parts.single()
        assertTrue(declared.measures.single().events.isEmpty())
        assertEquals(listOf(Staff(1)), declared.staves)
        val undeclared = success(document("")).value.parts.single()
        assertTrue(undeclared.staves.isEmpty())
    }

    @Test
    fun testNumberedAttributesTargetDeclaredStaves() {
        val numbered = "<key number='2'><fifths>2</fifths></key><time number='2'><beats>3</beats><beat-type>4</beat-type></time>" +
            "<clef number='2'><sign>F</sign></clef><staff-details number='2'><staff-lines>4</staff-lines></staff-details>"
        val result = success(document("<attributes><staves>2</staves>$numbered</attributes>"))
        assertEquals(
            listOf(Attribute.KeySignature(2), Attribute.TimeSignature(3, 4), bass, Attribute.StaffLines(4)),
            events(result).map { assertIs<AttributeEvent>(it).attribute },
        )
        assertTrue(events(result).all { it.staff == 2 })
        // Without a staves declaration, staff 2 does not exist; its content is validated, not skipped.
        for (policy in ParsePolicy.entries) {
            for (element in listOf("key", "time", "clef", "staff-details")) {
                val single = Regex("<$element number='2'>.*?</$element>").find(numbered)!!.value
                val failed = failure(document("<attributes>$single</attributes>"), DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
                assertTrue(failed.diagnostics.single().location.path.orEmpty().endsWith("/$element[1]/@number"))
            }
            failure(document("<attributes><staves>2</staves><key number='2'><fifths>x</fifths></key></attributes>"),
                DiagnosticCode.INVALID_VALUE, ParseOptions(policy))
        }
    }

    @Test
    fun testOtherAttributesChildrenAreReportedByFeature() {
        val result = success(document(
            "<attributes><part-symbol>brace</part-symbol><instruments>2</instruments>" +
                "<measure-style><slash type='start'/></measure-style><directive>Allegro</directive></attributes>",
        ), ParseOptions(ParsePolicy.TOLERANT))
        assertEquals(listOf("S08", "S16", "S24", "S07"), result.diagnostics.map { it.unsupported?.featureId })
        assertTrue(events(result).isEmpty())
    }

    @Test
    fun testAttributeChangesAfterBackupApplyAtTheCursor() {
        val result = success(document(
            "<note><rest/><duration>2</duration></note><backup><duration>1</duration></backup>" +
                "<attributes><key><fifths>1</fifths></key><clef><sign>F</sign></clef><staff-details><staff-lines>4</staff-lines>" +
                "</staff-details></attributes>",
        ))
        assertTrue(result.diagnostics.isEmpty(), result.diagnostics.toString())
        val changes = events(result).drop(1).map { assertIs<AttributeEvent>(it) }
        assertEquals(listOf(Attribute.KeySignature(1), bass, Attribute.StaffLines(4)), changes.map { it.attribute })
        assertTrue(changes.all { it.onset == Rational.ONE })
    }

    private fun events(result: ParseResult.Success<Score>): List<Event> = result.value.parts.single().measures.first().events

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
