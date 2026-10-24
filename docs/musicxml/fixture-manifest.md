# Fixture manifest (MX-04)

This lists every MusicXML fixture used by the importer tests, with its author, licence and schema
status. Schema validity, import outcome and musical meaning are checked by separate tests:

- **Schema:** `XsdValidationTest` (JVM) validates each fixture against the pinned
  [MusicXML 4.0 XSD](https://github.com/w3c/musicxml/tree/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema)
  bundled in `musicxml-parser/src/jvmTest/resources/musicxml-4.0`. Its imports (`xml.xsd`,
  `xlink.xsd`) resolve to the bundled copies, other external schemas and DTDs are refused, and a
  document's DOCTYPE is never loaded, so the check needs no network. The test also checks the
  bundled files against the SHA-256 checksums in [element-inventory.md](element-inventory.md).
- **Import:** each fixture's import test asserts the strict and tolerant outcomes and diagnostics
  on every target.
- **Semantics:** exact musical facts. For the independent fixtures these come from the W3C suite's
  own descriptions and source, not from this importer.

Licences for bundled material are in [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md).

## Independent fixtures (W3C MusicXML test suite)

Unmodified files (apart from line endings) from
[w3c-cg/musicxmlTestSuite `77c19f7e`](https://github.com/w3c-cg/musicxmlTestSuite/tree/77c19f7e819154c70ca1a1992e80dcda8ff82fea/xmlFiles),
MIT License, © 2010–2016 Reinhold Kainhofer and the GNU LilyPond project and © 2016–2026 Michael
Scott Asato Cuthbert. Embedded in `W3cSuiteFixtures.kt` and asserted by `W3cSuiteImportTest`. They
stand in for the test-plan fixtures F01–F08: their authors are independent of the importer, and each
file states what a reader should produce. The 11c–11i time-signature files (MX-25) are embedded in
`W3cTimeSignatureFixtures.kt`, the 13a–13e key-signature files (MX-26) in `W3cKeySignatureFixtures.kt`,
and the MX-27 visibility files in `W3cVisibilityFixtures.kt`, asserted by `VisibilityImportTest`.

| Suite file | Covers | Schema | Strict / tolerant | Musical facts asserted (from the suite description) |
|---|---|---|---|---|
| `03a-Rhythm-Durations` | F01 | Valid | Accepted / accepted | Every value from maxima to 1024th, plain, dotted and double-dotted, with exact durations |
| `41a-MultiParts-Partorder` | F02 | Valid | Accepted / accepted | Four parts P0–P3 in order |
| `51b-Header-Quotes` | F02 | Valid | Accepted / accepted (no `divisions`: default 1) | Movement title, composer, rights, software and part name keep their double quotes |
| `01d-Pitches-Microtones` | F03 | Valid | Accepted / accepted | Alters −3/2, −1/2, 1/2, 3/2 on C, D, E, F, repeated an octave higher |
| `03c-Rhythm-DivisionChange` | F04 | Valid | Accepted / accepted | Divisions 1 → 8 → 38 keep quarter onsets 0–3 and halves at 0 and 2 |
| `03e-Rhythm-No-Divisions` | F04 | Valid | Accepted / accepted | No `divisions`: the agreed default 1 makes the single whole note four quarters |
| `03b-Rhythm-Backup` | F05 | Valid | Accepted / accepted | Voice 1 on beats 1–2, voice 2 on beats 2–3 after a partial backup, no rests |
| `03f-Rhythm-Forward` | F05 | Valid | Accepted / accepted | Quarter on beat 2, 16th on beat 4, measure filled to 4 quarters, no rests |
| `21c-Chords-ThreeNotesDuration` | F06 | Valid | Accepted / accepted | Seven three-note chords; each shares onset and duration |
| `46c-Midmeasure-Clef` | F07 | Valid | Accepted / accepted | Clef change via an implicit measure and mid-measure; each note sees its clef |
| `13e-KeySignatures-MidMeasure-Change` | F07 | Valid | Accepted / accepted | Keys 2, −2, 0 and 7 fifths take effect note by note |
| `43b-MultiStaff-DifferentKeys` | F08 | Valid | Accepted / accepted | Two staves with their own key and clef |
| `11c-TimeSignatures-Complex` | F07 | Valid | Accepted / accepted | (3+2)/8 and (5+3+1)/4 as additive fractions, lengths 5/2 and 9 quarters |
| `11c-TimeSignatures-CompoundSimple` | F07 | Valid | Accepted / accepted | Same facts as 11c-Complex (older spelling) |
| `11d-TimeSignatures-ComplexMultiple` | F07 | Valid | Accepted / accepted | 3/8+2/8+3/4 and 5/2+1/8 as separate fractions, lengths 11/2 and 21/2 |
| `11d-TimeSignatures-CompoundMultiple` | F07 | Valid | Accepted / accepted | Same facts as 11d-ComplexMultiple (older spelling) |
| `11e-TimeSignatures-ComplexMixed` | F07 | Valid | Accepted / accepted | (3+2)/8+3/4, length 11/2 |
| `11e-TimeSignatures-CompoundMixed` | F07 | Valid | Accepted / accepted | Same facts as 11e-ComplexMixed (older spelling) |
| `11f-TimeSignatures-SymbolMeaning` | F07 | Valid | Accepted / accepted | 3/8 with `cut` and 1/8+2/4 with `single-number`; symbols are display only |
| `11g-TimeSignatures-SingleNumber` | F07 | Valid | Accepted / accepted | 3/8 and (3+2)/8 with `single-number` |
| `11h-TimeSignatures-SenzaMisura` | F07 | Valid | Accepted / accepted | Empty senza-misura, then one with `X`; no meter length |
| `11i-TimeSignatures-Alternate` | F07 | Valid | Accepted / accepted | Six interchangeable alternates with their relations (parentheses, bracket, equals, hyphen, slash, space) |
| `13a-KeySignatures` | F07 | Valid | Accepted / accepted | Fifths −11 to 11, each in major then minor |
| `13b-KeySignatures-ChurchModes` | F07 | Valid | Accepted / accepted | Two sharps in every mode, from major to locrian, then none |
| `13c-KeySignatures-NonTraditional` | F07 | Valid | Accepted / accepted | [F♯, A♭, B♭], then [C𝄫, G𝄪, D♭, B♯, F♮] with octaves 2 to 6 |
| `13d-KeySignatures-Microtones` | F07 | Valid | Accepted / accepted | Seven alterations from −1.5 to 1.5 semitones, exact; note pitches unchanged |
| `13e-KeySignatures-Cancel` | F07 | Valid | Accepted / accepted | Cancel of 3, −5, −3 and 4 fifths at default, right, before-barline and left |
| `01e-Pitches-ParenthesizedAccidentals` | F03 | Valid | Accepted / accepted | Cautionary/editorial flags, then bracket and parentheses flags, each independent of the accidental name and the written alter |
| `13f-KeySignatures-Visible` | F07 | Valid | Accepted / accepted | Hidden A♭ major key still applies to the following notes, which have no accidentals |
| `34d-Note-Attributes` | F18 | Valid | Accepted / accepted | `print-leger`, `print-dot`, `print-lyric` and `print-object` combinations A–H; lyric `print-object` overriding the note's `print-lyric` (I–L); hidden notes keep their dotted-quarter durations |

Two structure-recovery files (MX-24) are kept apart in `w3cStructureRecoveryFixtures` because they
are not schema-valid:

| Suite file | Covers | Schema | Strict / tolerant | Musical facts asserted (from the suite description) |
|---|---|---|---|---|
| `41g-PartNoId.invalid` | F02 | Invalid (part without `id`) | `RECOVERED_VALUE` / accepted with warnings | The single part is bound to the only score-part, P1, with its whole rest |
| `41h-TooManyParts` | F02 | Invalid (`part` IDs P3 and P4 are undeclared) | `UNSUPPORTED_FEATURE` / accepted with warnings | Only the listed part P1 is imported; P3 and P4 are discarded with a diagnostic each |

Four MX-27 files are kept apart in `w3cPartialVisibilityFixtures` because they also hold content
tracked by other issues. They are schema-valid and no visibility attribute is discarded:

| Suite file | Covers | Schema | Strict / tolerant | Musical facts asserted (from the suite description) |
|---|---|---|---|---|
| `34a-Print-Object-Spanners` | F18 | Valid | `UNSUPPORTED_FEATURE` / accepted with warnings | 18 hidden notes; beams, tuplets and slurs starting and ending on hidden notes are kept, and 3:2 durations unchanged |
| `22b-Staff-Notestyles` | F26 | Valid | `UNSUPPORTED_FEATURE` / accepted with warnings | Hidden notes in bars 4 and 5 print their two lyric lines through `print-lyric` |
| `11b-TimeSignatures-NoTime` | F07 | Valid | `INVALID_CURSOR_MOVE` / accepted with warnings | Hidden 2/2 on both staves, then 4/4 hidden only on staff 2; both still apply |
| `02d-Rests-Multimeasure-TimeSignatures` | F26 | Valid | `UNSUPPORTED_FEATURE` / accepted with warnings | The part name is kept and marked not printed |

The whole suite (183 files) runs as a broad corpus with `scripts/run-w3c-suite.sh`; see
[compliance-audit.md](compliance-audit.md).

## Implementation-authored fixtures

Written for this project (MIT, see [LICENSE](../../LICENSE)) while implementing each feature. They
are regression fixtures, not independent oracles. All are in `AuditCorpus.kt`.

| Fixtures | Source file | Schema |
|---|---|---|
| F01, F03, F04 | `NoteFixtures.kt` | Valid |
| F02 | `PartwiseFixtures.kt` | Valid |
| F05, F06, F08 | `PolyphonyFixtures.kt` | Valid |
| F07 | `AttributeFixtures.kt` | Valid |
| F09, F11 | `TupletFixtures.kt` | Valid |
| F10 | `SpanFixtures.kt` | Valid |
| F12 (timewise and partwise) | `TimewiseFixtures.kt` | Valid |
| F13, F14 | `DirectionFixtures.kt` | Valid |
| F15 | `LyricFixtures.kt` | Valid |
| F16 | `BarlineFixtures.kt` | Valid |
| F17, F21 concert-score subset | `InstrumentFixtures.kt` | Valid |
| F18 | `LayoutFixtures.kt` | Valid (rest colour corrected to upper-case hex in MX-04) |
| F19 | `GraceFixtures.kt` | Valid |
| F21 | `VersionFourFixtures.kt` | **Invalid**, knowingly: `part-link` after `part-name`, `line-detail` without `staff-lines`, `for-part` beside `transpose`, `wait` inside a direction's `listening`, and `level` misplaced with type `editorial`. Its ordered diagnostic assertions depend on this layout; reordering it is future work |
| Six mixed-feature pairs | `MixedFeatureFixtures.kt` | Valid |
| F20 `.mxl` | `MxlFixtures.kt` | Archive; its score is the F01-style document checked by `MxlImportTest` |

Inline variants in individual tests and the MX-21 mutants are deliberately varied inputs and are
not schema-validated.
