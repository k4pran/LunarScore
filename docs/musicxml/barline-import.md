# Barlines, repeats and endings (MX-17)

MX-17 keeps barlines, repeat signs, endings and measure-repeat signs **as notated** on the neutral
model and derives playback order **separately**, so notation structure and playback navigation
can always be told apart.

## Notation structure

| MusicXML | Neutral model |
|---|---|
| `barline` (`location` left/right/middle, default right) | `Measure.barlines`, in source order: `Barline(location, onset, style, repeat, ending)`; `onset` is the cursor position of a middle barline and null otherwise |
| `bar-style` | `BarStyle` (all eleven 4.0 values) |
| `repeat direction`, `times` | `Repeat(RepeatDirection, times)`; `times` is the total number of plays, null when unspecified |
| `ending number`, `type`, text | `EndingMark(EndingType, numbers, text)` on the barline; blank text becomes null |
| Paired ending marks | `Part.endings`: `Ending(id, numbers, startMeasureId, stopMeasureId, text, discontinued)` spanning whole measures |
| `measure-style/measure-repeat` (whole measure; a style numbered for one staff of several is S24 discarded since [MX-09](polyphony.md)) | `Measure.measureRepeat = MeasureRepeat(measures, slashes)` |

Barlines take no time and never move the cursor. `ending number` follows the 4.0
`ending-number` pattern, `([ ]*)|([1-9][0-9]*(, ?[1-9][0-9]*)*)`, so `"1, 2"` gives `[1, 2]` and
a blank number gives an empty list (no pass number).

Ending marks are paired in part order (both partwise and timewise input), giving IDs
`<part>:ending-<n>` in start order. Endings cannot nest:

- A start while another ending is open leaves the open ending with a null stop.
- A stop whose numbers differ from the open ending, or with no ending open, is an orphan.
- An ending still open at the end of its part keeps a null stop.

All three are `UNMATCHED_SPAN` (an error in strict mode, a warning in tolerant mode) at the
`ending` element. The marks stay on their barlines either way. `Score.validate()` reports an
unstopped ending as `MISSING_ENDPOINT` and an unknown measure as `UNKNOWN_MEASURE`.

A `measure-repeat` start marks its measure and the following measures until a stop. The stop is
placed in the first measure that no longer shows the sign. The measures keep the music they
sound, as MusicXML requires, so measure repeats never change playback. A stop with no sign in
effect is `UNMATCHED_SPAN`.

## Playback navigation

`Part.playbackOrder(maxMeasures = 10_000)` in `sheetmusic-model` unfolds the notation into
`PlaybackMeasure(measureIndex, measureId, pass)` records without changing the score:

- A forward repeat (left barline) starts a section, and a backward repeat (right barline) returns
  to it. Without a forward repeat, playback returns to the measure after the previous completed
  repeat, or to the first measure. A pickup is replayed in that case and not when a forward
  repeat follows it (F16).
- Outside an ending, a section is played `times` times, or twice when `times` is unspecified.
  `times` of 0 or 1 does not repeat.
- An ending is skipped on passes it does not name. Inside an ending, the backward repeat is taken
  while the pass is below the highest number in its group of consecutive endings, and `times` is
  ignored, as the 4.0 reference specifies. Leaving an ending that does not repeat completes the
  section.

Navigation findings are returned as `PlaybackDiagnostic`s, separate from parser diagnostics:

| Code | Meaning |
|---|---|
| `MISPLACED_REPEAT` | A forward repeat that is not on a left barline, or a backward repeat that is not on a right barline; ignored |
| `OPEN_ENDING` | An ending without a stop covers only its start measure |
| `UNNUMBERED_ENDING` | An ending without numbers is played on every pass |
| `INVALID_ENDING` | An unknown or reversed measure range, or an overlap with an earlier ending; ignored |
| `LENGTH_LIMIT` | Output truncated at `maxMeasures` |

## Explicit unsupported outcomes

| Source content | Outcome |
|---|---|
| Barline `segno`, `coda`, `divisions` and `id` attributes; `segno`, `coda`, `fermata`, `wavy-line` children; `bar-style color` | S15 `DISCARDED` |
| `repeat after-jump`, `winged`; ending `print-object`, `end-length`, `system`, position and font attributes | S15 `DISCARDED`; the repeat or ending is kept |
| Barline `footnote`, `level` | S25 `DISCARDED` |
| Middle barline after `backup`/`forward` | At the cursor after the move since [MX-09](polyphony.md) |
| `measure-style` for staff ≠ 1 | S08 `DISCARDED` |
| `multiple-rest`, `beat-repeat`, `slash`; `measure-style` display attributes | S24 `DISCARDED` (MX-21) |
| Unknown location, bar-style, repeat direction, ending type or measure-repeat type; negative `times`; invalid ending number; non-positive measure count or slashes | Fatal `INVALID_VALUE` |
| Missing repeat direction, ending number/type or measure-repeat type; duplicate `bar-style`, `repeat` or `ending` | Fatal `INVALID_STRUCTURE` |

Da capo, dal segno, coda, fine and `after-jump` playback are not interpreted. `RelationshipKind.ENDING`
is unused because endings span measures, not events.

## Evidence

`BarlineFixtures.kt` holds F16: a pickup, a heavy-light forward repeat, a first ending closed by a
light-heavy backward repeat, a discontinued second ending and a dashed middle barline. Written
order is 0–4 and playback order is 0, 1, 2, 1, 3, 4. `BarlineImportTest` covers F16 notation,
provenance and playback, and inline variants:

- orphan, unstopped, nested and mismatched endings under both policies;
- ending-number pattern cases;
- invalid values and structure;
- discarded content;
- a middle barline after `backup`;
- measure-repeat start/stop, orphan stop, other staves and variants;
- timewise ending pairing.

`PlaybackOrderTest` covers navigation rules, diagnostics, ending validation and model invariants.
All fixtures are implementation-authored; F16 is validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)).
