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
| F06 | Two notes in a chord at the same onset plus a cross-staff note; chord children retain individual durations. |
| F07 | Mid-measure clef/key/time change; each event sees state active at its offset. |
| F08 | Two parts with different measure content and a two-staff piano part; IDs and offsets stay separate. |
| F09 | Eighth notes with beams and triplet; exact 1/3 or 2/3 rational duration as specified by fixture. |
| F10 | Tie and slur crossing a measure; endpoints resolve by ID and notation/playback types stay distinct. |
| F11 | Nested tuplet/time-modification, dotted note and pickup measure; no floating-point drift. |
| F12 | Partwise/timewise equivalent pair; canonical semantic projections are equal. |
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

Add mixed-feature pairs: changed divisions + backup; tied chord + changing staff; grace + lyric; mid-measure transposition + sounding pitch; repeat ending + pickup; timewise + two staves. Include legal exporter files only when redistribution rights are known. Run memory/security tests with oversized XML, entity/DTD attempts and ZIP expansion limits separately from small semantics fixtures.

The independent test author owns expected values. Implementation PRs may add fixtures but should not silently rewrite expected semantics; disputes cite the exact specification section and receive a review note. Differential music21 output can highlight questions, never replace specification-based assertions.
