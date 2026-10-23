# Lyrics and text runs (MX-14)

Issue [#15](https://github.com/k4pran/LunarScore/issues/15) imports
[MusicXML 4.0 lyrics](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/lyric/) as
`Lyric` values on the note or rest that carries them. A lyric is linked to its note by sitting in
that event's `NotatedValue.lyrics`, so the event ID is the association; no separate lyric event or
relationship is created. Lyrics never change onsets, durations or the cursor.

## Mapping

| Source | Model |
|---|---|
| `lyric` | One `Lyric` in `NotatedValue.lyrics`, in source order on the note or rest |
| `lyric/@number` | `Lyric.verse`, kept as text; a missing number is `null` and is not assumed to be verse 1 |
| `lyric/@name` | `Lyric.name`, for example "refrain" |
| `lyric/@placement` | `Lyric.placement` (presentation only) |
| `syllabic` | `LyricSyllable.syllabic`: `SINGLE`, `BEGIN`, `MIDDLE`, `END`; null when absent |
| `text` | `LyricSyllable.text`, whitespace preserved. Consecutive `text` runs without an `elision` between them join into one syllable, since they differ only in typography |
| `elision` | Starts a new `LyricSyllable` on the same note; its `elision` holds the symbol (for example `‿`), or `""` for an elision without a stated symbol |
| `extend` | `Lyric.extend = LyricExtend(type)` with `START`, `CONTINUE`, `STOP`, or null type when absent |
| `extend` without text | A melisma continuation: a `Lyric` with no syllables and only `extend` |

`Lyric.text` is the canonical printed text of one lyric: each syllable preceded by its elision
symbol, for example `fa‿un`. The canonical text sequence of a verse is obtained by walking events in
order and taking each event's lyric with that `verse`, paired with the event ID; F15 asserts this
sequence for verse 1, verse 2 and the unnumbered lyric.

Melisma lines are kept as each note's `extend` value; they are not resolved into relationships, and
notes under a melisma without their own `lyric` element simply have no lyric for that verse.
Syllabic values are not checked for word consistency across notes (for example a `BEGIN` without a
later `END`).

## Diagnostics

| Case | Outcome |
|---|---|
| `laughing`, `humming` | S14 `DISCARDED`; the lyric is dropped |
| `lyric` with `time-only` | S14 `DISCARDED`; the lyric is dropped, as for `tie` |
| `end-line`, `end-paragraph` | S14 `DISCARDED`; the lyric is kept |
| `footnote`, `level` | S25 `DISCARDED`; the lyric is kept |
| Display and typography attributes on `lyric`, `text`, `elision` and `extend` (`justify`, `default-x`/`y`, `font-*`, `color`, `xml:lang`, `print-object`, `id`, …) | S14 `DISCARDED` per attribute; the lyric is kept |
| Unknown `syllabic`, extend `type` or `placement`; blank `number` | Fatal `INVALID_VALUE` |
| Empty lyric; `elision` first or doubled; `syllabic` after text without an `elision`; `syllabic` or `elision` without a following `text`; text content after `extend`, `end-line`, `end-paragraph` or editorial content; `laughing`/`humming` with other content; trailing content before any text | Fatal `INVALID_STRUCTURE` |

A lyric on a discarded note (chord, grace, unpitched, another staff, or after backup/forward) is
dropped with its note; its note's diagnostic covers it.

## Fixtures and verification

`LyricFixtures.kt` adds original F15, authored from the MusicXML 4.0 lyric, syllabic, elision, text
and extend references and not XSD-validated: verse 1 "A-ve" with a melisma over two more notes
(extend start, an extend-only continue and an extend-only stop), verse 2 named "refrain" placed below
with "fa‿un" elided on one note followed by "glo-ria", and in measure 2 an unnumbered lyric whose text
is split into two runs, plus a rest without lyrics.

`LyricImportTest` adds 5 common tests: F15 values, IDs and provenance with no diagnostics; the
canonical per-verse text sequence linked to note IDs; elisions without a symbol and runs after an
elision; S14/S25 discard of unsupported content under tolerant and strict policies; and fatal value
and structure errors under both policies. `ScoreProjectionTest` checks lyric invariants.

Windows verification on 2026-10-23:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 143 passed per target (5 lyric, 14 direction, 7 tuplet, 10 span, 6 timewise, 9 attribute, 14 note, 17 skeleton, 33 reader, 15 contract, 13 probes) |
| Model JVM / Android host tests; core JVM tests | Passed (16 model tests per target) |
| Parser iosArm64 / iosSimulatorArm64 test klibs; model JS/Wasm compile; compose and sample JVM compile | Compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
