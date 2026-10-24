# Beams, stems and tuplets (MX-12)

Issue [#13](https://github.com/k4pran/LunarScore/issues/13) keeps printed beams and stems, the
[MusicXML 4.0 time-modification](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/time-modification/)
ratio and tuplet brackets on imported notes and rests. Timing still comes only from `duration` and
`divisions`, as exact rationals; notation never changes an onset or duration.

## Mapping

| Source | Model | Notes |
|---|---|---|
| `note/stem` | `NotatedValue.stem`: `StemDirection.UP`, `DOWN`, `DOUBLE`, `NONE` | Null means no `stem` element (unspecified), not stemless |
| `note/beam` | `NotatedValue.beams`: `Beam(level, BeamType)` in source order | `number` 1–8 (default 1) is the level; `begin`, `continue`, `end`, `forward hook`, `backward hook`. Levels are unique per note |
| `note/time-modification` | `NotatedValue.timeModification`: `TimeModification(actualNotes, normalNotes, normalValue, normalDots)` | Positive Int counts; optional `normal-type` and `normal-dot` count |
| `notations/tuplet` | `Relationship` of kind `TUPLET`, IDs such as `P1:tuplet-2` | Paired by `number` (1–16, default 1) through the shared [span resolver](span-import.md), preferring the same voice; may start or stop on a rest and cross barlines |

### Nested tuplets

MusicXML writes each note's cumulative ratio in `time-modification`. In F11 a 3:2 eighth triplet
nested inside a 3:2 quarter triplet carries `9:4` with `normal-type` eighth, and its duration of
2 divisions at divisions 9 is exactly 2/9 quarter. The importer keeps that cumulative ratio as
written and does not factor it into levels. Each level's bracket is its own `TUPLET` relationship,
distinguished by `number`, so nesting is the containment of those relationships.

Durations are not recomputed from the type, dots and ratio. A `duration` that differs from the
notated value is kept, because MusicXML treats `duration` as the sounding length.

## Diagnostics

| Case | Outcome |
|---|---|
| Unknown stem value, beam type or `normal-type`; beam `number` outside 1–8; tuplet `number` outside 1–16 (for example 17); tuplet `type` other than `start`/`stop`; zero or malformed `actual-notes`/`normal-notes` | Fatal `INVALID_VALUE` |
| Missing tuplet `type`; missing `actual-notes` or `normal-notes`; `normal-dot` without `normal-type`; duplicate `stem` or `time-modification`; two beams with the same level | Fatal `INVALID_STRUCTURE` |
| Tuplet start without stop, stop without start, second start with the same number and voice | `UNMATCHED_SPAN`, relationship kept with a null partner (see [span import](span-import.md)) |
| `tuplet-actual`/`tuplet-normal` display ratio; tuplet `bracket`, `show-number`, `show-type`, `line-shape` and `placement` | S11 `DISCARDED`; the tuplet span is kept. Tuplet position is kept on the relationship's `startVisual`/`endVisual` since [MX-28](layout-import.md#typography-and-colour-mx-28) |
| Beam `fan`, `repeater`, `id` | S10 `DISCARDED` per attribute; the beam is kept. Beam `color` (`Beam.visual`) and stem position and `color` (`NotatedValue.stemVisual`) are kept since [MX-28](layout-import.md#typography-and-colour-mx-28) |
| Beam, stem or tuplet on a discarded note (chord, unpitched, staff ≠ 1, after backup/forward) | Dropped with the note; a tuplet partner becomes `UNMATCHED_SPAN` |

Beam continuity (a `begin` without an `end`) and agreement between `time-modification`, tuplet
brackets and durations are not checked.

## Fixtures and verification

`TupletFixtures.kt` adds original F09 and F11, authored from the MusicXML 4.0 beam, stem, tuplet
and time-modification references. Both are validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)).

- F09: 2/4, divisions 6. Two beamed eighths with up stems, then a beamed 3:2 eighth triplet with
  down stems lasting exactly 1/3 quarter each, then a dotted eighth (decimal duration 4.5) beamed
  to a sixteenth with a level-2 backward hook, and a quarter rest.
- F11: an implicit pickup (measure 0) with a 3:2 quarter triplet (tuplet 7) containing a nested
  9:4 eighth triplet (tuplet 8), then a measure with a dotted half and an eighth triplet numbered 16.
  Pickup onsets are 0, 2/3, 8/9, 10/9 and 4/3, and the durations sum to exactly 2 quarters.

`TupletImportTest` adds 6 common tests: F09 and F11 onsets, durations, notation flags,
relationships and provenance with no diagnostics; tuplets on a rest across a barline; fatal value
and structure errors under both policies (including tuplet number 17); discarded S10/S11 display
content with unmatched tuplets; and default flags on a plain note. `ScoreProjectionTest` checks the
beam level and uniqueness invariants.

Chord tones ([MX-09](polyphony.md)) keep their own type, time modification, stem and beams. Mixed
values within a chord are not checked for consistency and have no dedicated fixture yet.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 123 passed per target (6 tuplet, 10 span, 6 timewise, 9 attribute, 14 note, 17 skeleton, 33 reader, 15 contract, 13 probes) |
| Model and core JVM tests | Passed |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
