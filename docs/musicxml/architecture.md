# MusicXML 4.0 importer architecture

Status: design proposal, updated 2026-10-15. MX-01 adds an internal dependency spike;
no semantic score importer has been implemented. See [dependency evidence](dependency-spike.md)
for selected APIs, pins, security limitations and target verification. The XML/ZIP choices below
are superseded by that evidence where they differ.

## Scope and sources

The target is MusicXML 4.0 as published in [June 2021](https://www.w3.org/2021/06/musicxml40/), interpreted with its [reference](https://www.w3.org/2021/06/musicxml40/musicxml-reference/) and [XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/). The [4.0 history](https://www.w3.org/2021/06/musicxml40/version-history/40/) is the change list. MusicXML 4.1 had a roadmap under development in [July 2026](https://www.w3.org/community/music-notation/2026/07/27/co-chair-meeting-minutes-july-27-2026/); it is outside this plan until released. The current local renderer model is `sheetmusic-core/src/commonMain/kotlin/com/ryanjames/sheetmusic/model/Score.kt`: one staff, nonempty measures, treble clef, and whole/half/quarter notes. It cannot express the required importer semantics.

## Module boundary

Add `:musicxml-parser` depending on a new, format-neutral `:sheetmusic-model` module. Refactor `:sheetmusic-core` to consume the richer model through a small adapter or a staged migration; preserve existing renderer tests during the transition. Keep XML types, parser state and diagnostics out of the public music model. `:musicxml-parser` targets Android, iOS and JVM first, with common code and API shapes that remain usable on JS/Wasm. No Python or music21 runtime dependency.

`parseMusicXml(input, options): ParseResult<Score>` should expose a score plus ordered diagnostics. Input should support text/bytes and bounded streams where the chosen XML library permits. `parseMxl` is a separate archive adapter using the same XML parser. Fatal syntax/security errors return failure; unsupported valid material returns a structured diagnostic and, when possible, retained source information. Never imply that successful parsing equals full 4.0 coverage.

## Model contract (MX-02)

The initial neutral vocabulary is implemented in `:sheetmusic-model`, package
`com.ryanjames.sheetmusic.score`; see [model.md](model.md) for bounds, ID scopes,
diagnostics and the renderer migration. The table below remains the broader target design:
metadata, directions, presentation and other later feature domains are not yet implemented.
`sheetmusic-core` now exposes the model through an API dependency and provides a lossless
legacy-to-neutral adapter. XML/Compose dependencies are absent from the neutral module.

## Broader model proposal

MX-03 implements the public [diagnostic/result/provenance contract](diagnostics.md) in
`:musicxml-parser`, with an internal collector for policy and diagnostic/retention/provenance
budgets. Source concerns stay outside the neutral model. No parse entry point is exposed yet;
MX-05 and semantic adapters must emit findings through this contract.

| Concept | Proposed representation |
|---|---|
| Score | Metadata, ordered parts, global defaults, optional provenance, unsupported extensions |
| Part | Stable ID, names, instrument timeline, ordered measures and staves |
| Measure | Number/text/implicit flags, duration extent, event list and attribute changes at rational offsets |
| Staff / voice | Stable staff number and voice ID; events keep both, rather than nesting time exclusively in XML order |
| Event | Stable ID, rational onset and duration in quarter-note units; note, rest, direction, barline and similar typed variants |
| Note / chord | Individual noteheads share onset but retain independent durations, ties, accidentals and staff assignment |
| Pitch | Written step, octave and rational alteration; separate optional sounding pitch derived from transposition |
| Notation / playback | Separate notated type, dots, time modification, beams and glyph hints from performed duration, tempo and dynamics |
| Relationships | Typed start/end IDs for ties, slurs, tuplets, wedges, pedals, endings and other spans; unresolved spans carry diagnostics |
| Presentation | Optional engraving hints such as placement, offsets, colour, notehead, print/layout settings; renderer may ignore them |
| Provenance | Source path, part/measure/element position and raw known-but-unmapped attributes when useful for diagnostics |

Use a normalized semantic model plus **small parser-local intermediate records** for ordered XML events and unresolved references. This keeps `backup`, `forward` and XML-specific state outside the public model while preserving enough source order to calculate timing. A single direct conversion risks discarding information before cross-measure relationships and partwise/timewise normalization are resolved; a full XSD-shaped public object graph would bind every consumer to MusicXML. Rational values should use reduced numerator/denominator with checked arithmetic, not `Double`, so tuplets and changing divisions remain exact. Implicit attributes (divisions, key, time, clef, transposition) are tracked per part/staff and applied at the event offset at which they change.

## XML and archive strategy

Start with [xmlutil core 1.0.2](https://github.com/pdvrieze/xmlutil) as the candidate pull reader, not generated serialization classes for the whole schema. A pull reader preserves document order and allows bounded measure-level materialization. Confirm the exact API and KMP target compatibility in MX-01 before locking the dependency. Alternatives are platform XML APIs via `expect/actual` (more platform code) and DOM (simpler but score-sized memory); neither is preferred initially. The xmlutil project describes Native support as beta and its core as cross-platform, so iOS smoke tests are mandatory. Do not enable DTD/external entity resolution; reject entity declarations and external references if the reader cannot guarantee safe handling. Put byte count, nesting depth, element count and diagnostic count limits at the input boundary.

For `.mxl`, read `META-INF/container.xml`, select its first rootfile and enforce archive entry count, compressed/uncompressed byte and expansion-ratio bounds; reject traversal and duplicate root paths. MusicXML 4.0 permits multiple renditions in a container and has packaging rules for `mimetype`; see the [compressed format tutorial](https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/). Choose a multiplatform ZIP implementation only after an Android/iOS/JVM spike. The archive adapter is independent of XML semantics.

## Parser flow

1. Secure input and root recognition (`score-partwise` or `score-timewise`).
2. Parse header and part list into part IDs and metadata.
3. Normalize each part's measure stream while retaining source order. For timewise, collect each measure's part payload into the same internal structure.
4. Interpret events with a per-part current cursor, divisions and staff attribute state. A note advances the cursor unless marked `chord`; `backup` moves it backward and `forward` moves it forward. Directions use their specified offset rules.
5. Resolve spanning relationships by key and scope, then validate invariants and emit diagnostics.
6. Produce immutable semantic objects and a coverage report. Renderer integration is a later adapter, with no Compose dependency in the parser.

The first integration slice is one partwise part, one measure, divisions, a pitched quarter note and a rest. Later slices add polyphony and cross-event semantics. See [implementation-plan.md](implementation-plan.md).
