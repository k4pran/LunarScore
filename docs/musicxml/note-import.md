# Notes and rests end to end (MX-07)

Issue [#8](https://github.com/k4pran/LunarScore/issues/8) extends the public
[partwise importer](partwise-import.md) from a skeleton to the first musical import: one
sequential voice of pitched notes and rests, with exact onsets and durations. The result is a
format-neutral `Score` from `:sheetmusic-model`; no renderer or Compose dependency is involved.

```kotlin
val score = (parseMusicXml(xmlBytes) as ParseResult.Success).value
val events = score.parts.single().measures.single().events // AttributeEvent, NoteEvent, RestEvent
```

## Timing

- `divisions` (in `attributes`) is parsed as an exact positive `xs:decimal`, so fractional values
  such as `1.5` are legal. It is part state: a change applies to later notes in the same measure
  and persists into later measures of the same part. Each part body starts without divisions.
- A note's `duration` is also an exact positive decimal. Performed duration in quarter notes is
  `duration / divisions`; onset is the measure-local cursor, which then advances by that duration.
  The cursor restarts at zero in each measure. `Measure.extent` is the furthest observed end.
- Zero, negative or malformed durations/divisions produce fatal `INVALID_VALUE`. A duration with no
  preceding divisions, a missing duration, a missing pitch/rest, or a duplicate pitch/rest/duration
  produces fatal `INVALID_STRUCTURE`. No default divisions or duration is ever inferred.
- Exact values whose reduced components exceed `Int` (long integers, more than nine significant
  fraction digits, or an overflowing quotient/cursor sum) produce fatal `NUMERIC_OVERFLOW` at the
  duration/divisions location. Nothing is rounded.

## Note content

| Source | Neutral result |
|---|---|
| `pitch` step/alter/octave | `NotePitch.Pitched(WrittenPitch)`; `alter` is an exact decimal (microtones allowed), default 0; octave 0–9 |
| `rest`, `rest measure="yes"` | `RestEvent`, `wholeMeasure` flag |
| `type` | `NotatedValue.value` (maxima … 1024th); absent type stays null, never inferred from duration |
| `dot` | `NotatedValue.dots` count |
| `accidental` with `cautionary`/`editorial` | `NotatedValue.accidental`, independent of `alter` (F03) |
| `voice` | Event voice label, default `"1"`; voices are not separated in time (no backup/forward) |
| `cue` | `NoteRole.CUE`; cue notes advance time like normal notes |
| `staff` = 1 | Staff 1; parts containing events declare `Staff(1)` |
| `time` with one positive integer `beats`/`beat-type` pair | `AttributeEvent(TimeSignature)` at the cursor offset |

Event IDs are generated as `<partId>:measure-<n>:event-<k>`, score-global and independent of
source IDs. Provenance maps `parts[i].measures[j].events[k]` to the note or `time` element.

## Explicit unsupported outcomes

All of these are `UNSUPPORTED_FEATURE` with `DISCARDED` retention: strict rejects, tolerant warns.

| Content | Feature | Effect on timing |
|---|---|---|
| `chord` notes | S06 | Whole note discarded; cursor not advanced (chord tones share the previous onset) |
| `grace` notes | S18 | Whole note discarded; no duration |
| `unpitched` notes; `staff` other than 1 | S16; S06 | Note discarded; cursor still advances by its duration so later onsets stay exact |
| `backup`/`forward` | S06 | Later notes and time signatures in that measure are each reported and discarded rather than placed at a wrong onset; divisions changes still apply |
| `time-modification`, `stem`/`beam`, `lyric` | S11, S10, S14 | Child discarded; note and exact duration kept. `tie` and `notations` are interpreted by [MX-11](span-import.md) |
| `instrument`; `footnote`/`level`; `play`/`listen` | S16; S25; S26 | Child discarded |
| `notehead`, rest display-step/octave, dot attributes | S17 | Child/attribute discarded |
| Note attributes (`default-x`, `dynamics`, `print-object`, …), unknown note children | S04 | Discarded |
| `time` with `number` other than 1 | S08 (S07 before MX-08) | Whole time signature discarded so a meter scoped to another staff is never applied to staff 1; a non-positive `number` is `INVALID_VALUE` |
| Time attributes (`symbol`, …); additive, multiple, interchangeable or `senza-misura` time | S07 | Discarded (a simple time with an unmapped attribute still yields its signature). Key, clef, staves and staff details are interpreted by [MX-08](attribute-state.md) |

Measure fullness against the time signature is not checked. Key, clef and staff-detail state is
[MX-08](attribute-state.md); multi-voice/cursor interpretation remains MX-09.

## Fixtures and verification

`NoteFixtures.kt` adds original F01 (C4 quarter then quarter rest in 4/4, divisions 4), F03 basic
(F4 with `alter` 1 and a printed cautionary natural) and F04 (divisions 4→8 mid-measure, persisting
into the next measure). They are authored from the MusicXML 4.0
[note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/) and
[duration](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/duration/) references,
intended schema-valid but not XSD-validated. `NoteImportTest` adds 12 common tests: exact F01/F03/F04
semantics and provenance, fractional divisions, cue/voice, zero/negative/malformed durations under
both policies, overflow (huge duration, tiny divisions, overflowing quotient and cursor sum), missing
structure, invalid values, unsupported note/attribute content with exact later onsets, and cursor-move
discarding. `parseDecimal` lexical and bound cases are tested directly.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 91 passed per target (12 note, 18 skeleton, 33 reader, 15 contract, 13 probes) |
| Parser build, Android AAR, JVM jar | Passed |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| Neutral model / core regression tests | Passed |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
