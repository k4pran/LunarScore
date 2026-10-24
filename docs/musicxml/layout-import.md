# Layout and visual hints (MX-18, MX-28)

MX-18 keeps `defaults` layout, measure `print` settings and the position and colour of notes,
rests and direction marks as **optional engraving hints** in `sheetmusic-model` (`Layout.kt`).
MX-28 extends them with [typography and colour](#typography-and-colour-mx-28) on every printed
element the model represents.
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

## Typography and colour (MX-28)

`VisualHints` also holds a `FontHints` (`families` in preference order, `size` as
`FontSize.Points` or `FontSize.Css`, `style`, `weight`), `halign`, `valign`, `justify`, an
`Enclosure` shape and `preserveWhitespace`. Every field is null when the source omits it, and
`VisualHints` stays null when an element has none of the attributes it keeps. Text is always kept
exactly, including the line breaks of multi-line `words` and `credit-words`; `preserveWhitespace`
records `xml:space` (`preserve` true, `default` false) for the renderer.

| MusicXML element | Kept attributes | Neutral model |
|---|---|---|
| `credit-words` | position, `color`, font, `halign`, `valign`, `justify`, `enclosure`, `xml:space` | `MetadataEntry.Credit.visuals`, one entry per `words` item (empty when no item has any) |
| direction `words` | same as `credit-words` | `DirectionMark.Words.visual` |
| `dynamics` (direction and notation) | position, `color`, font, `halign`, `valign`, `enclosure` | `Dynamics.visual` |
| `metronome` | position, `color`, font, `halign`, `valign`, `justify` | `DirectionMark.Metronome.visual` |
| `note` | position, `color`, font | `NoteEvent.visual`, `RestEvent.visual` |
| `accidental` | position, `color`, font | `Accidental.visual` |
| `stem` | position, `color` | `NotatedValue.stemVisual` |
| `beam` | `color` | `Beam.visual` |
| articulations and ornaments | position, `color`, font | `NoteExpression.visual` |
| `slur`, `tied`, `tuplet` (tuplet: position only) | position, `color` | `Relationship.startVisual` and `endVisual`, from the start and stop marks |
| `lyric` | position, `color`, `justify` | `Lyric.visual` |
| lyric `text`, `elision` | `color`, font | `LyricSyllable.visual` (first text run), `LyricSyllable.elisionVisual` |
| lyric `extend` | position, `color` | `LyricExtend.visual` |
| `key`, `clef`; `time` | position, `color`, font; `time` also `halign`, `valign` | `AttributeEvent.visual` |
| `bar-style`; `ending` | `color`; position, `color`, font | `Barline.styleVisual`; `EndingMark.visual` |
| `defaults/music-font`, `defaults/word-font` | font | `LayoutDefaults.musicFont`, `LayoutDefaults.wordFont` |
| `measure` `width` | tenths, not negative | `Measure.width` |

`font-family` is a comma-separated list, split and trimmed; `font-size` is a positive decimal in
points or a CSS size name (`xx-small` to `xx-large`). A blank family, a non-positive or unknown size,
and unknown `font-style`, `font-weight`, `halign`, `valign`, `justify`, `enclosure` or `xml:space`
values are fatal `INVALID_VALUE`, like invalid colours. `music-font` and `word-font` may each appear
once.

A lyric syllable made of several text runs keeps the first run's formatting; a later run with
different formatting is S14 `DISCARDED` (the text is still joined). A slur `continue` point is not
an endpoint, so its position and colour are S09 `DISCARDED`. Wedge relationships keep no visual
hints, because the wedge marks already hold them.

## Explicit unsupported outcomes

| Source content | Outcome |
|---|---|
| `appearance`, `lyric-font`, `lyric-language` in `defaults` (music and word fonts are kept since MX-28) | S17 `DISCARDED` |
| `system-dividers` | S17 `DISCARDED`; the rest of the system layout is kept |
| `print` `measure-numbering`, `part-name-display`, `part-abbreviation-display`; `staff-spacing` and `id` attributes | S17 `DISCARDED`; the rest of the print is kept |
| A second or later `print` in one measure | S17 `DISCARDED`; the first is kept |
| Typography, colour and position of elements the model does not represent: `dot`, `notehead`, `measure-style`, technical marks, unmodeled articulations and ornaments, other direction types (rehearsal, pedal, octave shift …), `tuplet-number`/`tuplet-type`, `line-detail`, part names | Feature-specific `DISCARDED` as before (S04, S11, S12, S13, S17, S24 …) |
| Text decoration (`underline`, `overline`, `line-through`), `rotation`, `letter-spacing`, `line-height`, `xml:lang`, `dir` on kept text; metronome `parentheses`; wedge `spread`/`niente`/line style; beam `fan`/`repeater`; slur and tie line style, placement, orientation and bezier; tuplet `bracket`, `show-number`, `show-type`, `line-shape`, `placement` | Feature-specific `DISCARDED` per attribute; the element and its kept hints remain |
| A lyric text run whose formatting differs from the syllable's first run; a slur `continue` point's position or colour | S14 / S09 `DISCARDED`; the text and slur are kept |
| `rest` display position; `notehead`; `staff-details` display content | S17 `DISCARDED`. Note `print-object`, `print-dot`, `print-spacing` and `print-leger` are kept as `NoteVisibility` since [MX-27](note-import.md#visibility-and-print-attributes-mx-27) |
| Invalid decimal, colour, margin type, `new-system`/`new-page`, non-positive `blank-page`, staff-layout `number`, scaling or page size; invalid font, alignment, enclosure or `xml:space` value; negative measure width | Fatal `INVALID_VALUE` |
| Scaling missing a value, page height without width, duplicate or conflicting page margins, incomplete page or system margins, duplicate singleton children (including `music-font` and `word-font`) | Fatal `INVALID_STRUCTURE` |

## Evidence

`LayoutFixtures.kt` holds F18: scaling, page size with odd/even margins, system and staff layout
defaults; a measure with system-margin and measure-distance overrides; a forced system break with a
staff distance; a forced page break after one blank page with a page number; a note with
`default-x` and negative `relative-y`, a coloured rest, words with negative `default-y`, and a
crescendo wedge with an alpha colour. `LayoutImportTest` covers F18, omitted defaults and print,
`concert-score`-only defaults, positions of metronome marks and note/direction dynamics, discarded
content under both policies, and invalid values and structure. All fixtures are
implementation-authored; F18 is validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)).

`TypographyImportTest` covers MX-28: every text-formatting attribute on words; position, colour and
font on notes, accidentals, stems, beams, articulations, ornaments, note dynamics, lyrics, slurs and
tuplets; key, time, clef, bar-style and ending hints and measure widths; the later-text-run and
unmodeled-element discards under both policies; and invalid values under both policies. It also
asserts the W3C suite files 31f (multi-line words), 32b, 51a (credits), 52a (fonts and widths), 34b
(colours), 34c (font sizes), 61h, 61j, 61k (lyrics) and 03e-SecondaryBeamBreaks, embedded in
`W3cTypographyFixtures.kt` and validated against the XSD ([manifest](fixture-manifest.md)). The
compressed 90a is checked only by the [W3C suite run](compliance-audit.md#w3c-musicxml-test-suite-mx-22).
