# Ties and slurs (MX-11)

Issue [#12](https://github.com/k4pran/LunarScore/issues/12) imports sound ties, printed ties and
slurs from the [MusicXML 4.0 notations](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notations/)
into `Score.relationships`. Sound and notation stay distinct: a `tie` element becomes a
`SOUND_TIE`, a `notations/tied` element becomes a `NOTATED_TIE`, and a `notations/slur` becomes a
`SLUR`. A note with both a `tie` and a `tied` gets two relationships; a `tied` without a `tie`
(notation only) gets only a `NOTATED_TIE`.

## Pairing

Spans are resolved per part, as measures stream in, so they cross barlines and work the same for
partwise and timewise input. Each kind is paired on its own:

| Kind | Source | Pairing key | Types |
|---|---|---|---|
| `SOUND_TIE` | `note/tie` | Written pitch (step, alter, octave) | `start`, `stop` |
| `NOTATED_TIE` | `notations/tied` | Written pitch plus `number` (1–16, default 1) | `start`, `stop`; `continue` is a stop and a start on one note |
| `SLUR` | `notations/slur` | `number` (1–16, default 1) | `start`, `stop`; `continue` is a mid-slur point, not an endpoint |

- A stop pairs with the earliest open start of the same kind and key, preferring one in the same
  voice. That lets ties and slurs cross voices while concurrent same-pitch ties in two voices keep
  their own partners.
- On each note, stops are applied before starts, so one note can end a tie or slur and begin the
  next.
- A stop with no open start waits for a later start whose position (measure index, then onset) is
  musically earlier. Voices can put a stop before its start in document order. With backup/forward
  still uninterpreted (MX-09), this case only arises once cursor moves are supported; resolver unit
  tests cover it already.
- IDs are part-scoped and numbered per kind in the order the first endpoint was read, for example
  `P1:slur-2` or `P1:sound-tie-1`. Relationships are listed in part-list order.
- Provenance maps `relationships[i].startEventId` and `relationships[i].endEventId` to the source
  `tie`, `tied` or `slur` element.

## Diagnostics

| Case | Outcome |
|---|---|
| Start with no stop by the end of the part; stop with no start; a slur `continue` with no open slur | `UNMATCHED_SPAN` at the endpoint element; error under strict, warning under tolerant. The relationship is kept with a null partner, which `Score.validate()` reports as `MISSING_ENDPOINT` |
| A second start with the same kind, key and voice while one is open (duplicate endpoint) | The earlier start gets `UNMATCHED_SPAN` straight away and keeps a null end; the new start opens |
| Tie pitches that differ (for example C4 to C#4) | Not paired: both ends are unmatched |
| Endpoint on a discarded note (chord, grace, unpitched, staff other than 1, after backup/forward) | No endpoint is registered, so its partner is reported as `UNMATCHED_SPAN` |
| Missing `type` | Fatal `INVALID_STRUCTURE` |
| Unknown `type` (including `continue` on `tie` and `let-ring` on `slur`); `number` outside 1–16 | Fatal `INVALID_VALUE` |
| `tie` with `time-only`; `tied type="let-ring"`; a `tie` or `tied` on a rest | S09 `DISCARDED`; that mark is dropped. A slur on a rest is kept |
| Display attributes on `tie`/`tied`/`slur` (`placement`, `orientation`, `line-type`, bezier, color, `id`, …) | S09 `DISCARDED` per attribute; the span is kept |
| Other `notations` children: `tuplet`; `footnote`/`level`; articulations, ornaments, fermata, dynamics, technical and the rest | S11; S25; S12 `DISCARDED`. `notations` attributes are S12 |

Ties are not checked against durations or onsets: a stop pairs even when it does not start where
the tied note ends. Enharmonic ties (for example C#4 to Db4) are not paired.

## Fixtures and verification

`SpanFixtures.kt` adds original F10, authored from the MusicXML 4.0 tie, tied and slur references:
slur 1 crosses the barline with a `continue` point, nested slur 2 and a sound tie plus printed tie
join two G4s across the barline, and two A4s carry a notation-only tied mark. It is intended to be
schema-valid but has not been XSD-validated.

`SpanImportTest` adds 8 common tests: F10 relationships, IDs and provenance with no diagnostics;
orphan and duplicate endpoints under both policies; pitch and voice pairing; discarded S09/S11/S12
content; fatal type and number errors; a partner on a discarded note; partwise/timewise equality
with interleaved parts; and resolver stop-before-start ordering.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 115 passed per target (8 span, 6 timewise, 9 attribute, 14 note, 17 skeleton, 33 reader, 15 contract, 13 probes) |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
