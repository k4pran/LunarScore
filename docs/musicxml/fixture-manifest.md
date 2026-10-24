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
file states what a reader should produce.

| Suite file | Covers | Schema | Strict / tolerant | Musical facts asserted (from the suite description) |
|---|---|---|---|---|
| `03a-Rhythm-Durations` | F01 | Valid | Accepted / accepted | Every value from maxima to 1024th, plain, dotted and double-dotted, with exact durations |
| `41a-MultiParts-Partorder` | F02 | Valid | Accepted / accepted | Four parts P0–P3 in order |
| `51b-Header-Quotes` | F02 | Valid | `RECOVERED_VALUE` (no `divisions`, [MX-24](https://github.com/k4pran/LunarScore/issues/53)) / accepted with a warning | Movement title, composer, rights, software and part name keep their double quotes |
| `01d-Pitches-Microtones` | F03 | Valid | Accepted / accepted | Alters −3/2, −1/2, 1/2, 3/2 on C, D, E, F, repeated an octave higher |
| `03c-Rhythm-DivisionChange` | F04 | Valid | Accepted / accepted | Divisions 1 → 8 → 38 keep quarter onsets 0–3 and halves at 0 and 2 |
| `03b-Rhythm-Backup` | F05 | Valid | Accepted / accepted | Voice 1 on beats 1–2, voice 2 on beats 2–3 after a partial backup, no rests |
| `03f-Rhythm-Forward` | F05 | Valid | Accepted / accepted | Quarter on beat 2, 16th on beat 4, measure filled to 4 quarters, no rests |
| `21c-Chords-ThreeNotesDuration` | F06 | Valid | Accepted / accepted | Seven three-note chords; each shares onset and duration |
| `46c-Midmeasure-Clef` | F07 | Valid | Accepted / accepted | Clef change via an implicit measure and mid-measure; each note sees its clef |
| `13e-KeySignatures-MidMeasure-Change` | F07 | Valid | Accepted / accepted | Keys 2, −2, 0 and 7 fifths take effect note by note |
| `43b-MultiStaff-DifferentKeys` | F08 | Valid | Accepted / accepted | Two staves with their own key and clef |

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
