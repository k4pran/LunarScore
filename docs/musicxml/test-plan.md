# Independent MusicXML test plan

The test author works from the [MusicXML 4.0 specification](https://www.w3.org/2021/06/musicxml40/) and this catalogue, without reading implementation code. Use a stable semantic expectation format (JSON/YAML or Kotlin builder) that names part, measure, voice, staff, rational onset/duration, written pitch, sounding pitch if known, event role and relationship endpoints. Assertions compare the canonical semantic projection rather than Kotlin object layout. Every fixture records source URL/version, license, expected XSD result, parse result, semantic assertions, preservation requirements and expected diagnostics.

## Validation layers

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
| F05 | Two voices with backup; voice 2 event starts at 0 while voice 1 advances; forward creates a gap. |
| F06 | Two notes in a chord at the same onset plus a cross-staff note; chord children retain individual durations. Include a shorter child and equal child, assert following onset uses the reference note; a longer child must receive a semantic diagnostic. |
| F07 | Mid-measure clef/key/time change; each event sees state active at its offset. |
| F08 | Two parts with different measure content and a two-staff piano part; IDs and offsets stay separate. |
| F09 | Eighth notes with beams and triplet; exact 1/3 or 2/3 rational duration as specified by fixture. |
| F10 | Tie and slur crossing a measure; endpoints resolve by ID and notation/playback types stay distinct. Add notation-only tied, orphan endpoints and a slur stop preceding its musical start in document order. |
| F11 | Nested tuplet/time-modification, dotted note and pickup measure; no floating-point drift. Include valid tuplet numbers 7,8,16 and invalid 17 independently of nesting depth. |
| F12 | Partwise/timewise equivalent pair; canonical semantic projections are equal. The schema-validated [MX-00 example](reference-analysis.md#concrete-specification--importer-disagreement) is a seed, not an executable importer test. |
| F13 | Staccato, trill and dynamic on a note; annotations retained and scoped correctly. |
| F14 | Tempo/metronome, text direction, wedge and offset; event order and span retained. |
| F15 | Two lyric verses, elision/syllabic text and melisma; ordered text and note association. |
| F16 | Repeat start/end, two endings and a nonstandard barline; structural and playback fields distinct. |
| F17 | Transposing instrument plus unpitched percussion; written and sounding fields must not be conflated. |
| F18 | Page/system/staff layout, print break and explicit positioning; retain supported hints. |
| F19 | Grace before a duration-bearing note and cue note; grace does not advance main cursor. |
| F20 | `.mxl` with container and two renditions; select first MusicXML rootfile and reject unsafe paths. |
| F21 | At least `concert-score`, `for-part`, `listening`, `assess` and packaging cases from 4.0 history; supported mapping or explicit unsupported diagnostic. |
| F22 | XSD-invalid: wrong root, missing required part-list, illegal duration and malformed XML; expected rejection category. |
| F23 | XSD-valid but semantically inconsistent: orphan span, overlapping cursor, incompatible measure length; expected diagnostics/policy. |
| F24 | Harmony with numeral/root, inversion, degrees, bass separator and a frame; separate figured-bass event/extension. Preserve timing and fields or assert `UnsupportedFeature` S22. |
| F25 | Tablature staff tuning/capo plus string/fret and bend/harmonic technical marks; preserve written pitch separately from fingering/staff presentation or assert S23. |
| F26 | Staff-scoped multiple rest, beat-repeat, measure-repeat and slash styles; preserve actual notes and distinguish style from playback navigation, or assert S24. |
| F27 | Links/bookmarks in measure and credit, grouping/features, footnote/level; verify source order, bounded retention or S25 loss diagnostics, and no external resolution. |
| F28 | Visible direction plus sound/MIDI/swing/instrument-change and listening/listen/assess/player controls; assert supported independent timelines and S26 for each meaningful unmapped item. |
| F29 | Separate schema-valid opus and sound-definition documents; expect typed unsupported-root failures while these document kinds are outside the score API. No linked score or resource fetched. |

MX-00 adds F24–F29 as planned obligations for omitted coverage groups; no fixture files or importer assertions exist yet. Feature tasks must author these before claiming support, or assert the [proposed structured unsupported outcome](risks-and-gaps.md#mx-00-audit-findings-and-diagnostic-obligations). Schema-valid examples alone do not move coverage test status from `None`.

Add mixed-feature pairs: changed divisions + backup; tied chord + changing staff; grace + lyric; mid-measure transposition + sounding pitch; repeat ending + pickup; timewise + two staves. Include legal exporter files only when redistribution rights are known. Run memory/security tests with oversized XML, entity/DTD attempts and ZIP expansion limits separately from small semantics fixtures.

The independent test author owns expected values. Implementation PRs may add fixtures but should not silently rewrite expected semantics; disputes cite the exact specification section and receive a review note. Differential music21 output can highlight questions, never replace specification-based assertions.
