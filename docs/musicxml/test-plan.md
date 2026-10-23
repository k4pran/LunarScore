# Independent MusicXML test plan

The test author works from the [MusicXML 4.0 specification](https://www.w3.org/2021/06/musicxml40/) and this catalogue, without reading implementation code. Use a stable semantic expectation format (JSON/YAML or Kotlin builder) that names part, measure, voice, staff, rational onset/duration, written pitch, sounding pitch if known, event role and relationship endpoints. Assertions compare the canonical semantic projection rather than Kotlin object layout. Every fixture records source URL/version, license, expected XSD result, parse result, semantic assertions, preservation requirements and expected diagnostics.

## Validation layers

MX-01 adds dependency smoke tests in `musicxml-parser/src/commonTest`: `XmlDependencyProbeTest`
and `ZipDependencyProbeTest`. Their original inline inputs cover roots/namespaces/order/entities,
limits and ZIP access; they do not replace independent F01–F29 semantic fixtures or XSD checks.
See [dependency-spike.md](dependency-spike.md) for exact test scope and target evidence.

MX-02 adds original Kotlin **model-only** projections of F01, F05, F10 and F17 in
`sheetmusic-model/src/commonTest/kotlin/com/ryanjames/sheetmusic/score/ScoreProjectionTest.kt`.
These define expected meter/note/rest facts, overlapping voices and gaps, independent cross-measure
sound/notation spans (including missing endpoints), and transposition/percussion fields. Additional
checks cover unequal cross-staff chord durations, grace, tuplets, exact arithmetic and graph errors.
`RationalTest` covers reduction, signs, Int boundary values, reducible large intermediates and
typed overflow/zero failures. `NeutralScoreAdapterTest` checks the existing renderer vocabulary's
migration. These do not parse XML, perform XSD validation, calculate transposition or resolve spans;
the independent source fixtures and importer assertions remain MX-04 and later feature work.
Target evidence is recorded in [model.md](model.md).

MX-03 adds `ParseContractTest` in `musicxml-parser/src/commonTest`, with original **contract-only**
F22/F23 projections: supplied fatal/semantic findings, source coordinates, both policies, an
unmapped but valid S25 grouping, discarded/opaque/converted outcomes, ordered fatal-after-warning
results, cap=1/2/3 overflow, cumulative UTF-8 retention and provenance bounds. These tests do not
parse source XML, classify it against XSD, resolve spans or interpret cursors. Independent XML
F22/F23 fixtures remain MX-04 and actual diagnostic emission remains MX-05/feature work.
See [diagnostics.md](diagnostics.md) for the contract and target evidence.

MX-05 adds original inline reader fixtures in `ScoreXmlReaderTest`: actual F22 wrong-root and
malformed-syntax diagnostics, DTD/entity/encoding attacks, namespace expansion, source locations,
ordered mixed content, exact/excess resource limits, cancellation and failure finalization.
PR #26 regressions also exercise preflight attribute budgets (per element and cumulative), counting
namespace/XML declaration fields and excluding literal or quoted equals before dependency reads.
Namespace regressions also reject empty URI bindings for nonempty prefixes under both policies
and accept valid empty default namespace resets with scope restoration.
Missing part-list and illegal duration are explicitly accepted as well-formed at this layer;
their schema/semantic rejection remains future work. These tests do not replace MX-04's independent
corpus or offline XSD harness. See [reader policy and target evidence](xml-reader.md).

MX-06 adds original F02 XML in `PartwiseFixtures.kt` and 18 `MusicXmlParserTest` assertions for
metadata, part-list order versus body order, measures, source mappings, broken IDs/references,
missing part-list, unsupported content and security/resource finalization. These are implementation-
authored common tests, not the independent MX-04 corpus; F02 is intended schema-valid but has not
been XSD-validated. See [mapping, limits and target evidence](partwise-import.md).

MX-07 adds original F01, F03 basic and F04 XML in `NoteFixtures.kt` and 12 `NoteImportTest`
assertions for exact onsets/durations, divisions changes and persistence, pitch alter versus printed
accidental, zero/negative/malformed/overflow durations, missing structure, unsupported note content
and cursor-move discarding. These are implementation-authored, not the independent MX-04 corpus,
and have not been XSD-validated. See [note/rest scope](note-import.md).

MX-08 adds original F07 XML in `AttributeFixtures.kt` and 7 `AttributeImportTest` assertions for
key/time/clef/staff-line changes at exact offsets (including mid-measure and end-of-measure),
effective state per event, missing state left unknown, key changes that leave written alter and
printed accidentals untouched, invalid values and staff-scoped/unsupported attribute content.
`AttributeStateTest` checks the model resolver directly. These are implementation-authored and
not XSD-validated. See [attribute state](attribute-state.md).

MX-09 adds original F05, F06 and F08 XML in `PolyphonyFixtures.kt` and 11 `PolyphonyImportTest`
assertions for backup/forward onsets and voice gaps, chord tones with shorter, equal, longer and
cross-staff durations, two parts with a two-staff piano, backup before zero under both policies,
invalid cursor moves, staff numbers beyond the declared staves and the declared staff limit. Tests
that asserted earlier discards after a cursor move or on staff ≠ 1 now assert the interpreted
result. These are implementation-authored and not XSD-validated. See [polyphony](polyphony.md).

1. Run independent XSD validation on JVM/CI using the official `musicxml.xsd` and its local catalog/imports, with network access disabled. This proves schema conformance only. Android/iOS tests need not run the validator.
2. Run importer success/failure and diagnostic assertions on Android, iOS and JVM. Verify ordered error codes and source locations, not prose messages.
3. Compare exact musical semantics using reduced rational values; compare presentation fields only where the fixture calls for them.
4. Add a regression fixture for every discovered defect. Existing [W3C MusicXML test-suite](https://github.com/w3c-cg/musicxmlTestSuite) files are useful broad inputs, not semantic oracles by themselves. Pin `77c19f7e819154c70ca1a1992e80dcda8ff82fea` and carry its MIT license when copying fixtures.

## Initial fixture catalogue

Each `F` fixture is a small hand-verifiable file unless noted. Create F01–F08 before parser work, then add the rest before the issue they gate. All timestamps below are quarter-note units.

| ID | Fixture and independent assertion |
|---|---|
| F01 | One part, 4/4, divisions=4, C4 quarter at 0 then quarter rest at 1. |
| F02 | Header and two score-parts; preserve part IDs/names/order, work and movement titles. |
| F03 | F4 with `<alter>1</alter>` and printed natural accidental; preserve pitch alteration and printed accidental independently. |
| F04 | Divisions change 4→8 midway; following duration=4 starts at the exact preceding end and lasts 1/2 quarter. |
| F05 | Two voices with backup; voice 2 event starts at 0 while voice 1 advances; forward creates a gap. Implemented in `PolyphonyFixtures.kt` (MX-09). |
| F06 | Two notes in a chord at the same onset plus a cross-staff note; chord children retain individual durations. Include a shorter child and equal child, assert following onset uses the reference note; a longer child must receive a semantic diagnostic. Implemented in `PolyphonyFixtures.kt` (MX-09); the longer child is an inline variant in `PolyphonyImportTest`. |
| F07 | Mid-measure clef/key/time change; each event sees state active at its offset. |
| F08 | Two parts with different measure content and a two-staff piano part; IDs and offsets stay separate. Implemented in `PolyphonyFixtures.kt` (MX-09). |
| F09 | Eighth notes with beams and triplet; exact 1/3 or 2/3 rational duration as specified by fixture. Implemented in `TupletFixtures.kt` (MX-12). |
| F10 | Tie and slur crossing a measure; endpoints resolve by ID and notation/playback types stay distinct. Add notation-only tied, orphan endpoints and a slur stop preceding its musical start in document order. Executable XML in `SpanFixtures.kt` (MX-11); stop-before-start is a resolver test until backup/forward is interpreted. |
| F11 | Nested tuplet/time-modification, dotted note and pickup measure; no floating-point drift. Include valid tuplet numbers 7,8,16 and invalid 17 independently of nesting depth. Implemented in `TupletFixtures.kt`; 17 is an inline variant (MX-12). |
| F12 | Partwise/timewise equivalent pair; canonical semantic projections are equal. The schema-validated [MX-00 example](reference-analysis.md#concrete-specification--importer-disagreement) is a seed; the executable pair is `TimewiseFixtures.kt` (MX-10), which also has a measure missing one part. |
| F13 | Staccato, trill and dynamic on a note; annotations retained and scoped correctly. Implemented in `DirectionFixtures.kt` (MX-13). |
| F14 | Tempo/metronome, text direction, wedge and offset; event order and span retained. Implemented in `DirectionFixtures.kt` (MX-13); a direction after backup is an inline test of its cursor offset (MX-09). |
| F15 | Two lyric verses, elision/syllabic text and melisma; ordered text and note association. Implemented in `LyricFixtures.kt` (MX-14), with an unnumbered lyric split into text runs. |
| F16 | Repeat start/end, two endings and a nonstandard barline; structural and playback fields distinct. Implemented in `BarlineFixtures.kt` (MX-17) with a pickup; nested, orphan and mismatched endings are inline variants in `BarlineImportTest`, and navigation rules are model tests in `PlaybackOrderTest`. |
| F17 | Transposing instrument plus unpitched percussion; written and sounding fields must not be conflated. Model-only (MX-02) and XML with a mid-measure instrument change (MX-16, `InstrumentFixtures.kt`). |
| F18 | Page/system/staff layout, print break and explicit positioning; retain supported hints. Implemented in `LayoutFixtures.kt` (MX-18) with negative offsets and colours; omitted defaults, discarded and invalid layout content are inline variants in `LayoutImportTest`. |
| F19 | Grace before a duration-bearing note and cue note; grace does not advance main cursor. Implemented in `GraceFixtures.kt` (MX-15); grace chord and cue after backup are inline tests (MX-09). |
| F20 | `.mxl` with container and two renditions; select first MusicXML rootfile and reject unsafe paths. Implemented in `MxlFixtures.kt` (MX-19), generated with .NET `ZipArchive`; traversal, duplicate, encrypted, bomb and corrupt archives are built inline in `MxlImportTest`. |
| F21 | At least `concert-score`, `for-part`, `listening`, `assess` and packaging cases from 4.0 history; supported mapping or explicit unsupported diagnostic. The `concert-score`/`for-part` subset is implemented in `InstrumentFixtures.kt` (MX-16). The rest is `VersionFourFixtures.kt` (MX-20): one measure carrying every score-document addition, with ordered diagnostics asserted in `VersionFourImportTest`. Packaging cases wait for the MX-19 `.mxl` adapter. See the [checklist](version-40-changes.md). |
| F22 | XSD-invalid: wrong root, missing required part-list, illegal duration and malformed XML; expected rejection category. |
| F23 | XSD-valid but semantically inconsistent: orphan span, overlapping cursor, incompatible measure length; expected diagnostics/policy. |
| F24 | Harmony with numeral/root, inversion, degrees, bass separator and a frame; separate figured-bass event/extension. Preserve timing and fields or assert `UnsupportedFeature` S22. |
| F25 | Tablature staff tuning/capo plus string/fret and bend/harmonic technical marks; preserve written pitch separately from fingering/staff presentation or assert S23. |
| F26 | Staff-scoped multiple rest, beat-repeat, measure-repeat and slash styles; preserve actual notes and distinguish style from playback navigation, or assert S24. |
| F27 | Links/bookmarks in measure and credit, grouping/features, footnote/level; verify source order, bounded retention or S25 loss diagnostics, and no external resolution. |
| F28 | Visible direction plus sound/MIDI/swing/instrument-change and listening/listen/assess/player controls; assert supported independent timelines and S26 for each meaningful unmapped item. |
| F29 | Separate schema-valid opus and sound-definition documents; expect typed unsupported-root failures while these document kinds are outside the score API. No linked score or resource fetched. |

MX-00 adds F24–F29 as planned obligations for omitted coverage groups; no fixture files or importer assertions exist yet. Feature tasks must author these before claiming support, or assert the [proposed structured unsupported outcome](risks-and-gaps.md#mx-00-audit-findings-and-diagnostic-obligations). Schema-valid examples alone do not move coverage test status from `None`.

Add mixed-feature pairs: changed divisions + backup; tied chord + changing staff; grace + lyric; mid-measure transposition + sounding pitch; repeat ending + pickup; timewise + two staves. Implemented in `MixedFeatureFixtures.kt` (MX-21; repeat ending + pickup is F16); adversarial mutants of every fixture and the opt-in public-corpus run are described in [compliance-audit.md](compliance-audit.md). Include legal exporter files only when redistribution rights are known. Run memory/security tests with oversized XML, entity/DTD attempts and ZIP expansion limits separately from small semantics fixtures.

The independent test author owns expected values. Implementation PRs may add fixtures but should not silently rewrite expected semantics; disputes cite the exact specification section and receive a review note. Differential music21 output can highlight questions, never replace specification-based assertions.
