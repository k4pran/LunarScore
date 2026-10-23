# Grace and cue notes (MX-15)

Issue [#16](https://github.com/k4pran/LunarScore/issues/16) imports grace notes, which the importer
previously discarded (S18), and records how grace and cue notes relate to the main timeline. The
rules follow the MusicXML 4.0 [note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/),
[grace](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/grace/) and
[cue](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/cue/) references.

## Timing and roles

| Source | Neutral result |
|---|---|
| `grace` on a pitched note | `NoteEvent` with `NoteRole.GRACE`, null `duration`, onset at the current cursor; the cursor and measure extent do not move |
| `grace` attributes `slash`, `steal-time-previous`, `steal-time-following` | `NoteEvent.grace`: `Grace(slash, stealTimePrevious, stealTimeFollowing)`; percentages are exact 0–100, null when absent |
| `cue` | `NoteRole.CUE`; a cue note has a positive duration and advances the cursor like a normal note |
| Type, dots, accidental, stem, beams, ties and notations on a grace note | Kept exactly as on other notes; grace notes can be span endpoints (for example a slur to the principal note) |

The 4.0 schema gives a grace note no `duration` element, so a missing duration is the normal case
and no divisions are required. A grace note never infers a duration from its type.

## Principal notes

Each imported grace note is linked to its principal note with a `GRACE_NOTE` relationship (start =
grace note, end = principal), numbered `<partId>:grace-note-<n>` in the grace notes' source order:

- The principal is the next imported normal or cue note in the same voice in the same measure.
  Consecutive grace notes share that principal.
- Grace notes followed in their voice by a rest, or by the end of the measure (including an
  uninterpreted backup/forward), are after-graces of the previous note in that voice.
- Grace notes whose next timed note in the voice is discarded (unpitched or another staff), or that
  have no note in their voice to attach to, keep a null end and produce `UNMATCHED_SPAN`
  (strict rejects, tolerant warns; the model reports `MISSING_ENDPOINT`).

Playback time stealing is not applied: performed onsets and durations of principal and neighbouring
notes are unchanged, and the steal-time percentages are retained for consumers.

## Explicit unsupported outcomes

| Content | Outcome |
|---|---|
| `duration` on a grace note (schema-invalid) | S18 `DISCARDED`; the grace note is kept without a duration and the cursor does not move |
| `make-time` and other `grace` attributes; `grace` children | S18 `DISCARDED`; the grace note is kept |
| `cue` on a grace note | S18 `DISCARDED` (cue size); imported as a grace note |
| Grace rest | S18 `DISCARDED`; the note is dropped without moving the cursor |
| Grace chord tone (`grace` plus `chord`) | S06 `DISCARDED` until MX-09 imports chords; the first grace note of the chord is kept |
| Unpitched grace note, grace note on staff other than 1 | S16 / S06 `DISCARDED`; no cursor movement |
| Grace or cue note after `backup`/`forward` | S06 `DISCARDED` with other content after the uninterpreted cursor move, until MX-09 |
| Invalid `slash`; steal-time outside 0–100 or not a decimal | Fatal `INVALID_VALUE` |
| Duplicate `grace` | Fatal `INVALID_STRUCTURE` |

## Fixtures and verification

`GraceFixtures.kt` adds original F19, authored from the references above and not XSD-validated: a
slashed grace note slurred to its principal with `steal-time-following`, two beamed grace notes
before one principal, a half-note cue note and a trailing after-grace with `steal-time-previous`,
then a whole-measure rest.

`GraceImportTest` adds 9 common tests: F19 roles, onsets, null grace durations, extent, grace
details, `GRACE_NOTE` and slur relationships and provenance; a grace note with no divisions or
principal; an ignored grace duration; a grace chord; a cue and an after-grace around a backup;
voice-scoped linking and after-graces before a rest; a grace before a discarded note; unsupported
grace content; and fatal attribute errors. `ScoreProjectionTest` checks `Grace` invariants.

Windows verification on 2026-10-23:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 152 passed per target (9 grace plus the 143 earlier tests) |
| Model JVM / Android host tests | Passed |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
