# Attribute state and pitch (MX-08)

Issue [#9](https://github.com/k4pran/LunarScore/issues/9) extends the
[single-voice note/rest import](note-import.md) with staff attribute state. Key, time, clef and
staff-line changes become `AttributeEvent`s at the exact measure-local cursor offset where they
appear, including mid-measure changes. The neutral model resolves the state in effect for every
event; it has no XML or Compose dependency.

```kotlin
val part = (parseMusicXml(xmlBytes) as ParseResult.Success).value.parts.single()
val states = part.effectiveStates()           // event ID -> AttributeState
val clefAtNote = states[noteEvent.id]?.clef   // null means no clef has been seen yet
```

## Attribute mapping

| Source (`attributes` child) | Neutral result |
|---|---|
| `divisions` | Part state for duration conversion (MX-07); persists across measures, not an event |
| `key` with `fifths` and optional `mode` | `Attribute.KeySignature(fifths, KeyMode?)`; absent mode stays null |
| `time` with one positive `beats`/`beat-type` pair | `Attribute.TimeSignature` (MX-07) |
| `clef` `sign`/`line`/`clef-octave-change` | `Attribute.Clef`; signs G, F, C, percussion, TAB, jianpu, none. A missing line uses the MusicXML standard line for G (2), F (4) and C (3); otherwise it stays null |
| `staves` = 1 | No event; staff 1 is already declared |
| `staff-details` `staff-lines` | `Attribute.StaffLines(count)`, zero allowed |

Each change is emitted in source order at the cursor offset, so one `attributes` element can yield
several events at the same onset. A `number` attribute on `key`, `time`, `clef` or `staff-details`
scopes the change to that staff; only staff 1 is interpreted.

## Effective state

`Part.effectiveStates()` in `:sheetmusic-model` returns an `AttributeState` (time, key, clef,
staff lines, transposition and instrument) for every event ID:

- State is tracked per staff and persists across measures in measure order.
- Within a measure, events are applied in onset order. Events at the same onset keep list order,
  so a change listed before a note at that offset applies to the note; a change at the end of a
  measure affects the next measure.
- An `AttributeEvent`'s own entry includes its change.
- Missing state is `null`, never defaulted to treble clef, C major or 4/4. Missing divisions
  remain a fatal `INVALID_STRUCTURE` before any duration (MX-07).

Key signatures never rewrite written pitch. `WrittenPitch.alter` comes only from `<alter>` and
`NotatedValue.accidental` only from `<accidental>`; neither is inferred from the key (F03, F07).

## Diagnostics

| Content | Outcome |
|---|---|
| Non-integer `fifths`, `line`, `clef-octave-change`; unknown clef sign; non-positive `staves` or attribute `number`; negative `staff-lines` | Fatal `INVALID_VALUE` |
| Key without `fifths` (traditional form), clef without `sign`, duplicate `fifths`/`mode`/`sign`/`line`/`staff-lines` | Fatal `INVALID_STRUCTURE` |
| `key`/`time`/`clef`/`staff-details` for staff ≠ 1; `staves` > 1; `part-symbol` | S08 `DISCARDED` |
| Non-traditional keys (`key-step`/`key-alter`/`key-accidental`) | S07 `DISCARDED`, whole key dropped |
| Key `cancel`, `key-octave`, an unknown `mode` value; key/clef display attributes (`print-object`, `after-barline`, `size`, …) | S07 `DISCARDED`; the key or clef itself is kept (unknown mode becomes null) |
| `staff-tuning`, `capo` | S23 `DISCARDED` |
| `staff-type`, `line-detail`, `staff-size`, staff-details attributes | S17 `DISCARDED` |
| `instruments`, `for-part` | S16 `DISCARDED` (`transpose` is interpreted since [MX-16](instrument-import.md)) |
| `measure-style` | S24 `DISCARDED` |
| `directive` and other unknown children | S07 `DISCARDED` |
| Any state change after an uninterpreted `backup`/`forward` | S06 `DISCARDED`, rather than placed at a wrong offset |

## Fixtures and verification

`AttributeFixtures.kt` adds original F07: C major, 4/4 and treble clef change at offset 2 to
G major, 2/4 and bass clef; a following F3 with `alter` 1 and no accidental, and an F3 natural
with a printed natural; a clef at the end of measure 2 that measure 3 inherits. It is authored
from the MusicXML 4.0 [attributes](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/attributes/),
[key](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/key/) and
[clef](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/clef/) references, intended
schema-valid but not XSD-validated. F03 and F04 remain covered by `NoteImportTest`.

`AttributeImportTest` adds 9 common tests: F07 effective state and exact offsets, missing state,
key forms and unsupported key content, clef forms and standard lines, staves/staff details,
other attributes children by feature, changes after a cursor move, staff 1 retained from a
`staves` declaration, and discarded-staff content left unvalidated. `AttributeStateTest` adds
2 model tests for onset ordering, cross-measure persistence, per-staff scope and domain replacement.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 102 passed per target (9 attribute, 14 note, 18 skeleton, 33 reader, 15 contract, 13 probes) |
| Model tests | 14 passed (2 new state tests) on JVM, Android host, JS and Wasm Node |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
