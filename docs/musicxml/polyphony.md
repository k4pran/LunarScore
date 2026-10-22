# Polyphony, chords and staves (MX-09)

Issue [#10](https://github.com/k4pran/LunarScore/issues/10) replaces MX-07's single sequential
voice with a measure-local cursor that `backup` and `forward` move, so voices, chords, staves and
parts each get exact onsets. The result is still a format-neutral `Score`; cursor state stays
inside the parser, as planned in the [architecture](architecture.md).

```kotlin
val piano = (parseMusicXml(xmlBytes) as ParseResult.Success).value.parts[1]
piano.staves                                    // [Staff(1), Staff(2)]
val notes = piano.measures[0].events.filterIsInstance<NoteEvent>()
notes.map { Triple(it.onset, it.voice, it.staff) } // exact quarter-note onsets per voice/staff
```

## Cursor

- Each measure starts its cursor at 0. A non-chord note starts at the cursor and advances it by
  its duration. Divisions apply to backup/forward durations exactly as to notes.
- `backup` moves the cursor back and `forward` moves it ahead by `duration / divisions`. A forward
  gap is the absence of events, never a fake rest; its optional `voice`/`staff` are validated but
  not stored. A forward counts towards `Measure.extent`; a backup never does.
- Attribute changes (and later notes) take the cursor offset after a move.
- A backup that would go before 0 is `INVALID_CURSOR_MOVE` at the `backup` element. Strict rejects;
  tolerant warns, and the message states that the cursor was clamped to 0.
- Missing or duplicate backup/forward `duration` is `INVALID_STRUCTURE`, as is a move with no
  preceding divisions. Zero, negative or malformed durations are `INVALID_VALUE`; values beyond
  `Int` bounds are `NUMERIC_OVERFLOW`. Overfilled or underfilled measures are not checked yet.

## Chords

Per the MusicXML 4.0 [chord](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chord/)
definition, a `<chord/>` note is an extra tone of the preceding non-chord note (the reference note).

- Each chord tone is its own `NoteEvent` at the reference onset, with its own duration, voice and
  staff, so cross-staff tones and shorter tones are retained exactly.
- Chord tones never move the cursor: the next note starts at the reference note's end.
- Every pitched member, including the reference note, shares a generated `chordId`
  (`<partId>:measure-<n>:chord-<k>`). Unchorded notes have no chord ID.
- A tone longer than its reference note is `INCONSISTENT_CHORD_DURATION` at its `duration`. Strict
  rejects; tolerant warns and keeps the source duration, while the cursor still follows the reference.
- A chord note with no reference note earlier in the measure, or after a backup/forward, is
  `INVALID_STRUCTURE`. A duplicate `<chord/>` is `INVALID_STRUCTURE`.
- A rest has no chord ID. A rest chord tone, or a tone whose reference note is a rest, is S06
  `DISCARDED` (the grouping is lost); both events keep the shared onset.
- An unpitched reference note is discarded (S16) but still times its pitched chord tones.

## Voices, staves and parts

- `voice` is kept as a label (default `"1"`); events need not be sorted by voice or onset.
- `staves` sets the part's staff count for later content. `Part.staves` lists 1 to the largest
  count declared; a part with events but no declaration has staff 1.
- A note or forward `staff` beyond the current count is `INVALID_VALUE`.
- `key`, `time` and `staff-details` without `number` apply to every staff. They yield one
  `AttributeEvent` per staff, using the staff count at the end of their `attributes` element, since
  `staves` follows `key`/`time` in schema order. A `clef` without `number` applies to staff 1.
- A `number` beyond the declared staves is `INVALID_VALUE` at the attribute.
- Declared staves summed across parts are bounded by `ParseLimits.maxStaves` (default 1,024)
  before a count is applied, because staves and unnumbered attributes expand per staff. Excess is a
  fatal `STAFF_LIMIT` under both policies.
- Parts are independent. Divisions and staff count reset at each part body, and event IDs include
  the part ID (`P2:measure-1:event-7`). `Part.effectiveStates()` resolves state per staff, so each
  staff sees only its own clef.

## Remaining limits

`part-symbol` remains S08 `DISCARDED`. Grace notes remain S18 `DISCARDED` and do not change the
chord reference. Measure fullness against the time signature, and voice/staff consistency between
a chord tone and its reference, are not checked. Timewise normalization is MX-10.

## Fixtures and verification

`PolyphonyFixtures.kt` adds original F05, F06 and F08, authored from the MusicXML 4.0
[backup](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/backup/),
[forward](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/forward/) and
[chord](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chord/) references.
They are intended to be schema-valid but have not been XSD-validated.

- F05 has two voices with backup and a forward gap, plus a voice that starts after a forward.
- F06 has a reference note with shorter, equal and cross-staff chord tones; the next note starts
  at the reference end. A longer-tone variant is also tested.
- F08 has a flute and a two-staff piano, with unnumbered key/time applied to both staves.

`PolyphonyImportTest` adds 11 common tests: F05/F06/F08 semantics and provenance, the longer chord
tone under both policies, backup before zero (strict failure, tolerant clamp, exact-zero success),
invalid move content, move extras (S06/S25), chords without a reference, rest and unpitched chord
members, staff numbers beyond the declared staves and the declared staff limit. Tests that asserted MX-07/MX-08 discard
behaviour now assert the interpreted result.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 111 passed per target (10 polyphony, 9 attribute, 13 note, 18 skeleton, 33 reader, 15 contract, 13 probes) |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| Model tests | Passed on JVM, Android host, JS and Wasm Node |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
