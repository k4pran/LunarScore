# Directions and note expressions (MX-13)

Issue [#14](https://github.com/k4pran/LunarScore/issues/14) imports common
[MusicXML 4.0 directions](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/direction/)
as `DirectionEvent`s and keeps articulations, ornaments and dynamics from
[notations](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notations/) as note
expressions. Each item keeps its semantic and visual components in separate fields: what a mark
means, how it sounds and how it is displayed are never merged or inferred from each other.

## Direction mapping

A direction becomes one `DirectionEvent` at the current measure-local cursor, in source order with
notes and attribute changes, and takes the next event ID. It never advances the cursor or the
measure extent.

| Source | Model | Component |
|---|---|---|
| `direction-type/words` | `DirectionMark.Words(text)`, whitespace preserved | Printed |
| `direction-type/dynamics` | `Dynamics(markings, placement)`: element names such as `p`, or `other-dynamics` text, in source order | Printed |
| `direction-type/metronome` with `beat-unit`, `beat-unit-dot`*, `per-minute` | `DirectionMark.Metronome(beatUnit, perMinute, beatUnitDots)`; `per-minute` stays text, for example `c. 120` | Printed |
| `direction-type/wedge` | `DirectionMark.Wedge(CRESCENDO / DIMINUENDO / CONTINUE / STOP)` plus a `WEDGE` relationship such as `P1:wedge-1` | Printed; span |
| `sound/@tempo`, `sound/@dynamics` | `DirectionSound.tempo` (quarter notes per minute), `DirectionSound.dynamics` (percentage of forte) | Playback |
| `direction/@placement`, `direction/@directive` | `DirectionDisplay.placement`, `DirectionDisplay.directive` | Display |
| `offset` (divisions) | `DirectionDisplay.offset` in quarter notes; also `DirectionSound.offset` when `sound="yes"` | Display; playback when stated |
| `voice` | `DirectionEvent.voice`; null means the direction applies to every voice | Scope |
| `staff` | Staff 1 kept; another staff discards the direction (S06), as for notes | Scope |

A metronome mark and a `sound` tempo are independent: F14's quarter = 120 mark is printed, while
`sound tempo="120"` is the playback tempo. Neither is derived from the other, and dynamics markings
do not imply a playback velocity. A direction that only carries a `sound` tempo or dynamics is kept
with no printed marks. A direction whose content is all discarded produces no event.

Wedges pair by `number` (1–16, default 1) through the shared [span resolver](span-import.md),
preferring the same voice; `continue` is a mid-span check like a slur continue. A wedge may cross
barlines. Within a measure, endpoints pair in display position (onset plus offset) order, so an
offset cannot pair a stop with a later-displayed start; IDs still follow source order. Unmatched
endpoints are `UNMATCHED_SPAN` and keep a null partner.

### Offsets and backup

The `offset` element moves a direction's display (and, with `sound="yes"`, its playback) without
changing the onset where it sits in the measure. Negative offsets are kept exactly as rationals.

Backup and forward are not interpreted yet (MX-09 is not on this branch). As with notes and
attribute changes, a direction after an uninterpreted backup/forward is reported as S06
`DISCARDED` instead of being placed at a wrong offset; a wedge endpoint inside it is dropped, so its
partner becomes `UNMATCHED_SPAN`. Exact direction offsets after backup become testable with MX-09.

## Note expression mapping

`NotatedValue.expressions` lists a note's or rest's expressions in source order.

| Source | Model |
|---|---|
| `articulations/accent`, `strong-accent`, `staccato`, `tenuto`, `detached-legato`, `staccatissimo`, `spiccato`, `stress`, `unstress`, `soft-accent` | `NoteExpression.Articulation(ArticulationType, placement)` |
| `ornaments/trill-mark`, `turn`, `delayed-turn`, `inverted-turn`, `delayed-inverted-turn`, `vertical-turn`, `inverted-vertical-turn`, `shake`, `mordent`, `inverted-mordent`, `haydn`, `schleifer` | `NoteExpression.Ornament(OrnamentType, placement)` |
| `notations/dynamics` | `Dynamics(markings, placement)`, the same type as in directions |

The type is the semantic component and `placement` (above/below) is the visual component. Ornament
realization (trill speed, beats, accidentals) is not modeled.

## Diagnostics

| Case | Outcome |
|---|---|
| Other direction types (`rehearsal`, `segno`, `coda`, `pedal`, `octave-shift`, `dashes`, `bracket`, `image`, …) | S13 `DISCARDED` per element; the rest of the direction is kept |
| Metric modulation (two beat units), `beat-unit-tied`, `metronome-note` forms | S13 `DISCARDED`; the metronome mark is dropped |
| Display attributes on `direction`, `direction-type`, `words`, `dynamics`, `metronome` (for example `parentheses`), `wedge` (`spread`, `niente`, line style) | S13 `DISCARDED` per attribute; the mark is kept. Position and `color` on `words`, `dynamics`, `metronome` and `wedge` are kept as `VisualHints` since MX-18 ([layout-import.md](layout-import.md)) |
| Other `sound` attributes and children (`damper-pedal`, `swing`, `midi-instrument`, `instrument-change`, `play`, …); `listening` | S26 `DISCARDED`; tempo and dynamics are kept |
| `footnote`, `level` in a direction | S25 `DISCARDED` |
| Other articulations (`breath-mark`, `caesura`, `scoop`, `plop`, `doit`, `falloff`, `other-articulation`) and ornaments (`wavy-line`, `tremolo`, `accidental-mark`, `other-ornament`); other notations (`fermata`, `technical`, `arpeggiate`, `glissando`, …) | S12 `DISCARDED` per element |
| Articulation/ornament attributes other than `placement` (for example `strong-accent type`, trill-sound or mordent `long`) | S12 `DISCARDED` per attribute; the expression is kept |
| Dynamics without a marking | Discarded under the containing feature (S12 or S13) |
| Direction on staff ≠ 1, or after an uninterpreted backup/forward | S06 `DISCARDED`; no event |
| Unknown `placement`, wedge `type` or beat unit; wedge `number` outside 1–16; non-`yes`/`no` `directive` or offset `sound`; malformed offset; negative or malformed sound tempo/dynamics; blank `per-minute` or voice; nonpositive staff | Fatal `INVALID_VALUE` |
| Direction without `direction-type`; wedge without `type`; metronome without `beat-unit` or `per-minute`; duplicate `offset`, `voice`, `staff`, `sound` or `per-minute`; an offset before any divisions | Fatal `INVALID_STRUCTURE` |
| Wedge start without stop, stop without start, `continue` without an open wedge | `UNMATCHED_SPAN`; relationship kept with a null partner |

## Fixtures and verification

`DirectionFixtures.kt` adds original F13 and F14, authored from the MusicXML 4.0 direction,
notations, articulations, ornaments, dynamics, metronome, wedge, offset and sound references.
Neither has been XSD-validated.

- F13: a staccato above and an f below on C5, a trill mark, accent and tenuto on D5, and an inverted
  mordent, an `sf`+`p` and an `other-dynamics` "pocof" on E5, with unchanged exact timing.
- F14: a directive "Allegro" heading with a quarter = 120 metronome mark and sound tempo 120, a p,
  a crescendo whose display offset is +1/2 quarter, crossing the barline to a stop with an f whose
  sound dynamics apply 1/2 quarter early, and "rit." at the end of measure 2.

`DirectionImportTest` adds 8 common tests: F13 and F14 values, IDs, relationships and provenance
with no diagnostics; a direction after backup discarded with S06 and its wedge partner unmatched;
a direction for staff 2 discarded; wedge pairing by number with unmatched start and continue; S12,
S13 and S26 discard of unsupported variants under tolerant and strict policies; fatal value and
structure errors under both policies; and defaults for minimal directions and plain notes.
`ScoreProjectionTest` checks direction invariants and `WEDGE` validation.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 132 passed per target (8 direction, 7 tuplet, 10 span, 6 timewise, 9 attribute, 14 note, 17 skeleton, 33 reader, 15 contract, 13 probes) |
| Model JVM / Android host tests; core JVM tests | Passed (15 model tests per target) |
| Parser iosArm64 / iosSimulatorArm64 test klibs; model JS/Wasm compile; compose and sample JVM compile | Compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
