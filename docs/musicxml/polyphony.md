# Polyphony, chords and staves (MX-09)

Issue [#10](https://github.com/k4pran/LunarScore/issues/10) replaces MX-07's single sequential
voice with a measure-local cursor that `backup` and `forward` move, so voices, chords, staves and
parts each get exact onsets. The result is still a format-neutral `Score`; cursor state stays
inside the parser, as planned in the [architecture](architecture.md).

This work was first merged into a stale branch and never reached master. It was ported onto the
MX-21 parser on 2026-10-23, so it also replaces the "discarded after an uninterpreted cursor move"
and "staff ≠ 1 discarded" rules that MX-13, MX-15, MX-16 and MX-17 had added in the meantime.

```kotlin
val piano = (parseMusicXml(xmlBytes) as ParseResult.Success).value.parts[1]
piano.staves                                    // [Staff(1), Staff(2)]
val notes = piano.measures[0].events.filterIsInstance<NoteEvent>()
notes.map { Triple(it.onset, it.voice, it.staff) } // exact quarter-note onsets per voice/staff
```

## Cursor

- Each measure starts its cursor at 0. A non-chord note starts at the cursor and advances it by
  its duration. Divisions apply to backup/forward durations exactly as to notes, including the
  MX-21 recovery of missing divisions as 1 (`RECOVERED_VALUE`).
- `backup` moves the cursor back and `forward` moves it ahead by `duration / divisions`. A forward
  gap is the absence of events, never a fake rest; its optional `voice`/`staff` are validated but
  not stored. A forward counts towards `Measure.extent`; a backup never does.
- Everything after a move takes the cursor offset: notes, grace and cue notes, attribute changes,
  directions (with their wedge endpoints) and middle barlines.
- A backup that would go before 0 is `INVALID_CURSOR_MOVE` at the `backup` element. Strict rejects;
  tolerant warns, and the message states that the cursor was clamped to 0.
- Missing or duplicate backup/forward `duration` is `INVALID_STRUCTURE`. Zero, negative or malformed
  durations are `INVALID_VALUE`; values beyond `Int` bounds are `NUMERIC_OVERFLOW`. Overfilled or
  underfilled measures are not checked yet.

## Chords

Per the MusicXML 4.0 [chord](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chord/)
definition, a `<chord/>` note is an extra tone of the preceding non-chord note (the reference note).

- Each chord tone is its own `NoteEvent` at the reference onset, with its own duration, voice,
  staff, notation, ties and expressions, so cross-staff tones and shorter tones are retained exactly.
- Chord tones never move the cursor: the next note starts at the reference note's end.
- Every pitched or unpitched member, including the reference note, shares a generated `chordId`
  (`<partId>:measure-<n>:chord-<k>`). Unchorded notes have no chord ID.
- A tone longer than its reference note is `INCONSISTENT_CHORD_DURATION` at its `duration`. Strict
  rejects; tolerant warns and keeps the source duration, while the cursor still follows the reference.
- A chord note with no reference note earlier in the measure, or after a backup/forward, is
  `INVALID_STRUCTURE`. A duplicate `<chord/>` is `INVALID_STRUCTURE`.
- A grace chord tone's reference is the preceding grace note: the tones share a chord ID, sit at
  the grace onset and are each linked to the same principal note. A grace tone after a timed
  reference, or a timed tone after a grace reference, is `INVALID_STRUCTURE`.
- A rest has no chord ID. A rest chord tone, or a tone whose reference note is a rest, is S06
  `DISCARDED` (the grouping is lost); both events keep the shared onset.

## Voices, staves and parts

- `voice` is kept as a label (default `"1"`); events need not be sorted by voice or onset. Grace
  notes link to the next note in their voice, whatever its staff, and ties pair by written pitch
  across staves.
- `staves` sets the part's staff count for later content. `Part.staves` lists 1 to the largest
  count declared; a part with events but no declaration has staff 1.
- A note, direction or forward `staff` beyond the current count is `INVALID_VALUE`.
- `key`, `time`, `staff-details` and `transpose` without `number` apply to every staff. They yield
  one `AttributeEvent` per staff, using the staff count at the end of their `attributes` element,
  since `staves` follows `key`/`time` in schema order. A `clef` without `number` applies to staff 1.
- A `number` beyond the declared staves is `INVALID_VALUE` at the attribute.
- Transposition is per staff, so sounding pitch uses the transpose of the note's staff. A sound
  `instrument-change` applies to the whole part, so it yields one event per staff.
- A `measure-style` numbered for one staff of a multi-staff part is S24 `DISCARDED`, because the
  model's measure repeat marks whole measures. On a single staff, `number="1"` is the whole measure.
- Declared staves summed across parts are bounded by `ParseLimits.maxStaves` (default 1,024)
  before a count is applied, because staves and unnumbered attributes expand per staff. Excess is a
  fatal `STAFF_LIMIT` under both policies.
- Parts are independent. Divisions, staff count and transpositions reset at each part body (and
  carry across timewise measures of the same part), and event IDs include the part ID
  (`P2:measure-1:event-7`). `Part.effectiveStates()` resolves state per staff, so each staff sees
  only its own clef.

## Remaining limits

`part-symbol` remains S08 `DISCARDED`. Measure fullness against the time signature, and voice/staff
consistency between a chord tone and its reference, are not checked.

## Fixtures and verification

`PolyphonyFixtures.kt` adds original F05, F06 and F08, authored from the MusicXML 4.0
[backup](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/backup/),
[forward](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/forward/) and
[chord](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chord/) references.
They are validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)). All three join the MX-21
audit corpus, so their adversarial mutants run under both policies.

- F05 has two voices with backup and a forward gap, plus a voice that starts after a forward.
- F06 has a reference note with shorter, equal and cross-staff chord tones; the next note starts
  at the reference end. A longer-tone variant is also tested.
- F08 has a flute and a two-staff piano, with unnumbered key/time applied to both staves.

`PolyphonyImportTest` adds 11 common tests: F05/F06/F08 semantics and provenance, the longer chord
tone under both policies, backup before zero (strict failure, tolerant clamp, exact-zero success),
invalid move content and recovered divisions, move extras (S06/S25), chords without a reference,
rest chord members, staff numbers beyond the declared staves and the declared staff limit. Tests
that asserted the earlier discard behaviour now assert the interpreted result: attributes, notes,
directions and middle barlines after a backup, grace chords, cue notes in another voice, grace
principals and ties across staves, per-staff transposes and staff-scoped attributes, and the MX-21
mixed pairs (divisions change + backup, tied chord across staves, timewise two staves).

Windows verification on 2026-10-23, after porting onto MX-21:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 216 passed per target, including 11 polyphony, 10 audit and 5 mixed-feature tests |
| Model tests | Passed on JVM, Android host, JS and Wasm Node |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
