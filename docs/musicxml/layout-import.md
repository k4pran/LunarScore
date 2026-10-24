# Layout and visual hints (MX-18)

MX-18 keeps `defaults` layout, measure `print` settings and the position and colour of notes,
rests and direction marks as **optional engraving hints** in `sheetmusic-model` (`Layout.kt`).
They never change musical meaning, and no renderer is required to support them. The current
Compose renderer ignores them.

## Units

All lengths are MusicXML **tenths**: one tenth of the space between two adjacent staff lines, so
a five-line staff is 40 tenths high. Values are exact `Rational`s parsed from `xs:decimal`; nothing
is rounded. `Scaling(millimeters, tenths)` relates tenths to millimetres, for example 7.2 mm per
40 tenths. Scaling is kept only when the source gives it; no default is assumed. Positions use
MusicXML's axes: positive y is up, and `default-x`/`default-y` are measured from the element's
reference point (usually the start of the measure and the top staff line).

## Mapping

| MusicXML | Neutral model |
|---|---|
| `defaults` (`scaling`, `page-layout`, `system-layout`, `staff-layout`) | `Score.layout: LayoutDefaults?`; null when none of these is present (`concert-score` alone gives null) |
| `scaling` `millimeters`, `tenths` | `Scaling`, both required and positive |
| `page-layout` | `PageLayout(height, width, margins)`; height and width are positive and given together |
| `page-margins type` (`odd`, `even`, `both`, default `both`) | `PageMargins(MarginType, left, right, top, bottom)`; one `both`, or at most one each of `odd` and `even` |
| `system-layout` | `SystemLayout(leftMargin, rightMargin, systemDistance, topSystemDistance)`; system margins need left and right |
| `staff-layout number`, `staff-distance` | `StaffLayout(staff, staffDistance)`; an omitted `number` gives staff 1 |
| `print` (first one in a measure) | `Measure.print: PrintHints?`; null when the measure has no `print` |
| `print new-system`, `new-page` | `newSystem`, `newPage`: `yes` is true (force a break), `no` is false (forbid one), omitted is null (renderer decides) |
| `print blank-page`, `page-number` | `blankPages` (positive, default 0), `pageNumber` (text as given) |
| `print` `page-layout`, `system-layout`, `staff-layout` | Same types as in `defaults`, overriding them from this measure |
| `print/measure-layout/measure-distance` | `PrintHints.measureDistance` |
| `default-x`, `default-y`, `relative-x`, `relative-y`, `color` on `note` | `NoteEvent.visual` / `RestEvent.visual: VisualHints?` |
| The same attributes on direction `words`, `dynamics`, `metronome`, `wedge` and on note `dynamics` | `DirectionMark.visual` (`Words`, `Dynamics`, `Metronome`, `Wedge`) |

`VisualHints` is null when an element has none of the five attributes. Positions may be negative
and are kept exactly. `color` accepts `#RRGGBB` or `#AARRGGBB` (alpha first) case-insensitively and
is stored in upper case.

A `print` is kept wherever it occurs in the measure. Each part keeps its own `print` per
measure, as partwise MusicXML repeats it in each part. Timewise padding measures have none.

## Explicit unsupported outcomes

| Source content | Outcome |
|---|---|
| `appearance`, `music-font`, `word-font`, `lyric-font`, `lyric-language` in `defaults` | S17 `DISCARDED` |
| `system-dividers` | S17 `DISCARDED`; the rest of the system layout is kept |
| `print` `measure-numbering`, `part-name-display`, `part-abbreviation-display`; `staff-spacing` and `id` attributes | S17 `DISCARDED`; the rest of the print is kept |
| A second or later `print` in one measure | S17 `DISCARDED`; the first is kept |
| Position and colour of stems, beams, accidentals, articulations, ornaments, barlines, endings, other direction types and other elements; font, justification and enclosure attributes everywhere | Feature-specific `DISCARDED` as before (S10, S12, S13, S15, S17 …) |
| `rest` display position; `notehead`; `staff-details` display content | S17 `DISCARDED`. Note `print-object`, `print-dot`, `print-spacing` and `print-leger` are kept as `NoteVisibility` since [MX-27](note-import.md#visibility-and-print-attributes-mx-27) |
| Invalid decimal, colour, margin type, `new-system`/`new-page`, non-positive `blank-page`, staff-layout `number`, scaling or page size | Fatal `INVALID_VALUE` |
| Scaling missing a value, page height without width, duplicate or conflicting page margins, incomplete page or system margins, duplicate singleton children | Fatal `INVALID_STRUCTURE` |

## Evidence

`LayoutFixtures.kt` holds F18: scaling, page size with odd/even margins, system and staff layout
defaults; a measure with system-margin and measure-distance overrides; a forced system break with a
staff distance; a forced page break after one blank page with a page number; a note with
`default-x` and negative `relative-y`, a coloured rest, words with negative `default-y`, and a
crescendo wedge with an alpha colour. `LayoutImportTest` covers F18, omitted defaults and print,
`concert-score`-only defaults, positions of metronome marks and note/direction dynamics, discarded
content under both policies, and invalid values and structure. All fixtures are
implementation-authored; F18 is validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)).
