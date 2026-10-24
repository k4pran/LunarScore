# Instruments and pitch interpretation (MX-16)

Issue [#17](https://github.com/k4pran/LunarScore/issues/17) imports part instruments, instrument
changes, transposition and unpitched percussion, and derives sounding pitch where MusicXML defines
it. The rules follow the MusicXML 4.0
[score-instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-instrument/),
[instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument/),
[midi-instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-instrument/),
[instrument-change](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument-change/),
[transpose](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/transpose/),
[concert-score](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/concert-score/),
[for-part](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/for-part/) and
[unpitched](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/unpitched/) references,
and the [4.0 version history](https://www.w3.org/2021/06/musicxml40/version-history/40/).

## Instruments

| Source | Neutral result |
|---|---|
| `score-part/score-instrument` | `Part.instruments` entry `Instrument(id, name)` in source order; `instrument-name` is required |
| `score-part/midi-instrument/midi-unpitched` (1–128) | `Instrument.percussionKey`, zero-based (0–127) |
| Note `instrument` | `NoteEvent.instrumentId`; it must name a score-instrument of the same part |
| Note without `instrument` in a part with exactly one score-instrument | That instrument, as MusicXML implies |
| Note without `instrument` in a part with several score-instruments | Null: the instrument is unknown, not guessed |
| `direction/sound/instrument-change` | `AttributeEvent(InstrumentChange(id))` on every staff of the part ([MX-09](polyphony.md)) at the direction's onset plus any `offset sound="yes"`, after the direction event |

`effectiveStates()` gives every event the instrument change and transposition in effect, so a
mid-score instrument change forms a timeline per part. A sound `instrument-change` changes the
sound of an existing score-instrument. It does not reassign later notes, which keep the
instrument named by their own `instrument` element.

## Written and sounding pitch

Written pitch (`step`, exact `alter`, `octave`) is never rewritten. Every pitched note also gets a
`SoundingPitch` in exact semitones (MIDI middle C = 60):

```
sounding = 12 × (octave + 1) + step semitones + alter + chromatic + 12 × octave-change
```

`chromatic` and `octave-change` come from the `transpose` in effect on the note's staff at the note's onset ([MX-09](polyphony.md)), so a transpose reached after a `backup` does not affect earlier-onset notes. A
transpose applies at its exact offset and persists across measures. A part with no `transpose`
sounds as written. `diatonic` is kept on `Transposition` for spelling, but sounding semitones use
only `chromatic`, so microtonal alters and chromatic values stay exact.

In a score whose `defaults` contain `concert-score`, written pitch is concert pitch. MusicXML allows
only octave transpositions there, so `octave-change` still applies (for example, a piccolo's written
C5 sounds as C6). Sounding pitch is null (unknown, not unison) in these explicit cases:

| Case | Outcome |
|---|---|
| Non-zero `chromatic` or `diatonic` transpose in a concert score | S16 `DISCARDED` at the `transpose`: written pitch and transpose are kept, but whether the written pitch is concert or transposed is ambiguous, so sounding stays null until the next transpose |
| Sounding semitones outside the exact `Rational` range (an extreme `alter`) | S16 `DISCARDED` at the note; the written pitch is kept |
| Unpitched note | `NotePitch.Unpitched` never has a sounding pitch |

## Unpitched percussion

`unpitched` becomes `NotePitch.Unpitched(displayStep, displayOctave)`. Both display fields must be
present together, or both absent. The note keeps its timing, notation, ties and instrument. Its
percussion key comes from the instrument. Unpitched notes pair ties by display position and
instrument, and unpitched grace notes are imported like pitched ones.

## Explicit unsupported outcomes

| Content | Outcome |
|---|---|
| `instrument-abbreviation`, `instrument-sound`, `solo`/`ensemble` in `score-instrument` | S16 `DISCARDED`; the instrument is kept |
| `virtual-instrument`; other `midi-instrument` children (channel, program, volume, …); `midi-device`, `player` | S26 `DISCARDED` |
| `instrument-change` children (new sound, solo/ensemble, virtual instrument) | S26 `DISCARDED`; the change event is kept |
| `instrument-change` whose sound offset falls before the measure start | S26 `DISCARDED` |
| Measure-level `sound` (including its `instrument-change`) | S26 `DISCARDED` as before |
| Second and later `instrument` on one note (a note shared by several instruments) | S16 `DISCARDED`; the first instrument is kept |
| `instrument` on a rest | S16 `DISCARDED` (rests have no instrument) |
| `transpose/double` | S16 `DISCARDED`; the primary transposition is kept |
| `transpose` for staff ≠ 1 | Applies to that staff since [MX-09](polyphony.md); without a `number`, to every staff |
| `for-part` (concert-score part transposition); `attributes/instruments` count | S16 `DISCARDED` |
| Other `defaults` children | S17 `DISCARDED` per child (previously one record for the whole `defaults`) |
| Missing `instrument-name` or score-instrument `id`; duplicate score-instrument; unknown `instrument`, `midi-instrument` or `instrument-change` ID; `transpose` without `chromatic`; only one of `display-step`/`display-octave` | Fatal `INVALID_STRUCTURE` |
| `midi-unpitched` outside 1–128; non-decimal `chromatic`; non-integer `diatonic`/`octave-change`; invalid display step/octave | Fatal `INVALID_VALUE` |

## Fixtures and verification

`InstrumentFixtures.kt` adds two original fixtures, authored from the references above and
validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)):

- F17: a B-flat clarinet doubling alto saxophone, with a transpose and instrument change at beat 3
  of measure 2, plus unpitched bass drum and tied snare with MIDI percussion keys.
- A concert-score subset of F21: a clarinet whose part transposition appears only in `for-part`,
  and a piccolo with an octave transposition.

The remaining F21 cases (`listening`, `assess`, packaging) are covered by MX-20; see [version-40-changes.md](version-40-changes.md).

`InstrumentImportTest` adds 10 common tests:

- F17 written and sounding pitches, note instruments, mid-measure instrument and transposition
  timeline, and provenance.
- F17 unpitched display positions, percussion keys and unpitched ties.
- F21 concert-score sounding pitch and the `for-part` diagnostic.
- The ambiguous concert-score transpose.
- Exact microtonal transposition persisting across measures.
- Out-of-range sounding pitch.
- Unassigned notes in multi-instrument parts.
- Unsupported instrument content under both policies.
- Fatal structure and value errors.

Existing note, grace, attribute and header tests were updated for derived sounding pitch and for
unpitched notes, transposes and score-instruments that are no longer discarded.

Windows verification on 2026-10-23:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 159 passed per target (10 instrument plus the 149 earlier tests) |
| Model JVM / Android host tests | Passed (model unchanged) |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
