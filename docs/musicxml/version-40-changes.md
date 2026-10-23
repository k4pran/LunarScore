# MusicXML 4.0 change-list audit (MX-20)

Issue [#21](https://github.com/k4pran/LunarScore/issues/21) checks every entry in the
[MusicXML 4.0 version history](https://www.w3.org/2021/06/musicxml40/version-history/40/) against
the importer. Each addition is either mapped to the model or reported as a structured
`UNSUPPORTED_FEATURE`. None is lost silently. Under the default strict policy an unsupported
addition fails the parse; in tolerant mode it is a warning and the score is still returned.

Evidence:

- **F21:** `f21VersionFourAdditions` in `VersionFourFixtures.kt`. It is one original measure holding
  every addition that a score document can carry. `VersionFourImportTest` asserts the ordered
  feature IDs and source paths of its 31 diagnostics, the supported mappings and the
  strict-policy failure. It also covers number-level 17.
- **F21 concert-score subset:** `f21ConcertScore` in `InstrumentFixtures.kt` (MX-16).

Every case below runs on the JVM and Android host. iOS test klibs compile, but running them
needs macOS.

"Discarded" means `UNSUPPORTED_FEATURE` with `ContentRetention.DISCARDED`, reported under the
coverage group shown at the path given.

## Packaging

| Change | Outcome | Evidence |
|---|---|---|
| Score and its parts in one `.mxl` | Not yet imported: `.mxl` input waits for the bounded adapter (MX-19). Once it exists, a later MusicXML rootfile (a linked part or score) must be reported as S19 discarded rather than skipped. | Pending MX-19 |
| XML catalog for local validation | Not applicable. The importer does not validate against the XSD and never fetches external resources. | — |
| XSD versions of the container and sounds schemas | The container is not read until MX-19. A sounds document is rejected with a fatal `UNSUPPORTED_ROOT` (S27). | `ScoreXmlReaderTest` |

## New elements

| Element | Outcome | Evidence |
|---|---|---|
| `assess`, `wait`, `sync`, `other-listen`, `other-listening` (inside `listen`/`listening`) | S26 discarded with their parent | F21 |
| `listen` (note) | S26 discarded | F21 |
| `listening` (measure and direction) | S26 discarded | F21 |
| `bass-separator`, `numeral`, `numeral-root`, `numeral-alter`, `numeral-key`, `numeral-fifths`, `numeral-mode` | S22 discarded with `harmony` | F21 |
| `concert-score` | **Supported:** written pitch is concert pitch. Only octave transpositions change the sounding pitch. | F21 concert-score subset |
| `for-part`, `part-clef`, `part-transpose` | S16 discarded with `for-part` | F21, F21 concert-score subset |
| `instrument-change` | **Supported** in a direction `sound`: an `InstrumentChange` attribute event at its exact offset. Its sound data is S26 discarded. | F17, `InstrumentImportTest` |
| `swing`, `first`, `second`, `straight`, `swing-type`, `swing-style` | S26 discarded (a `swing` in a direction `sound`, or a whole measure-level `sound`) | F21 |
| `line-detail` | S17 discarded | F21 |
| `part-link` | S25 discarded; the target is not fetched | F21 |
| `player` | S26 discarded | F21 |

## New attributes

| Attribute | Outcome | Evidence |
|---|---|---|
| `double/@above` | S16 discarded with `double` | F21 |
| `bass/@arrangement`, `harmony/@arrangement`, `inversion/@text` | S22 discarded with `harmony` | F21 |
| `repeat/@after-jump` | S15 discarded attribute; the repeat is kept | F21 |
| `figured-bass/@halign`, `@valign`, `@placement` | S22 discarded with `figured-bass` | F21 |
| `measure-numbering/@multiple-rest-always`, `@multiple-rest-range`, `@staff`, `@system` | S17 discarded with `measure-numbering` | F21 |
| `direction/@system` | S13 discarded attribute; the direction is kept | F21 |
| `ending/@system` | S15 discarded attribute; the ending is kept | F21 |
| `harmony/@system` | S22 discarded with `harmony` | F21 |
| `release/@offset`, `bend/@shape` | S12 discarded with `technical` | F21 |
| `metronome/@print-object` | S13 discarded attribute; the mark is kept, so a hidden metronome would be imported as visible | F21 |
| `staff-size/@scaling` | S17 discarded with `staff-size` | F21 |
| `@smufl` on `effect`, `membrane`, `metal`, `timpani`, `wood` | S13 discarded with `percussion` | F21 (`membrane`) |
| `wavy-line/@smufl` | S12 discarded with `wavy-line` | F21 |
| `level/@type` | S25 discarded with `level` | F21 |
| `arpeggiate/@unbroken` | S12 discarded with `arpeggiate` | F21 |

## New values

| Value | Outcome | Evidence |
|---|---|---|
| `smufl-accidental-glyph-name` (early-music and Kievan glyphs) | `accidental/@smufl` is S04 discarded; the accidental text is kept | F21 |
| `credit-type` `part name` | **Supported:** kept in order as credit-type text | F21 |
| `enclosure-shape` `inverted-bracket` | `@enclosure` is S13 discarded on `words` | F21 |
| Eight new `sounds.xml` IDs | `instrument-sound` is S16 discarded | F21 (`wind.flutes.whistle.tin.c`) |
| `ipa` updated to IPA 2015 | `play` is S26 discarded on notes | F21 |
| `number-level` maximum 16 | **Supported:** slur, tied and tuplet numbers 1–16 pair; 17 is fatal `INVALID_VALUE` | F21, F11 |
| `pedal-type` `discontinue`, `resume` | `pedal` is S13 discarded | F21 |

## Changed elements

| Change | Outcome | Evidence |
|---|---|---|
| A `note` may contain several `instrument` elements | The first instrument is kept; each later one is S16 discarded | F21, `InstrumentImportTest` |

## Removed and deprecated features

| Change | Outcome | Evidence |
|---|---|---|
| DTDs deprecated | The standard Recordare header is ignored without resolution (MX-21); any other DOCTYPE is fatal `UNSAFE_XML` (MX-05 security decision) | `ScoreXmlReaderTest` |
| Empty `multiple-rest` no longer allowed | `multiple-rest` is S24 discarded whatever its content | F21 |
| `function` deprecated in favour of `numeral` | S22 discarded with `harmony` | `MusicXmlParserTest` harmony discard |
| Clef sign `none` deprecated | **Supported:** `ClefSign.NONE` with no standard line | F21 |

## Documentation changes

These clarify existing elements. Their owners are listed below; this audit does not claim more
support than those rows record.

| Clarification | Status |
|---|---|
| `chord`/`duration`: chord notes with different durations | Implemented (MX-09): each tone keeps its duration; a tone longer than its reference is `INCONSISTENT_CHORD_DURATION` |
| `measure-repeat`: the music is repeated in each measure; `type` behaviour | Implemented (MX-17), `Measure.measureRepeat` |
| `beat-repeat` `type` behaviour | `beat-repeat` is S24 discarded |
| `diatonic`/`octave-change` for transpositions of an octave or more | Implemented (MX-16): sounding pitch = `chromatic` + 12 × `octave-change` |
| Layout elements missing from `print` | Implemented (MX-18): a `print` keeps only the layout it contains (null otherwise), and `Score.layout` defaults are left for the renderer to apply |
| `system-layout` within `print` | Implemented (MX-18) |
| `dynamics` placement within a direction | Implemented (MX-13): the direction's placement governs |
| `ending` number description | Implemented (MX-17) |
| `offset`: current location at the end of a measure | Implemented (MX-13): rational and negative offsets |
| `number-level`: overlapping elements in document versus score order; start-stop types in score order | Implemented (MX-11/MX-12): resolver pairs stop before start |
| `line`: needed only with G, F and C signs | Implemented (MX-08): standard lines only for those signs |
| `arpeggiate` line length, `bend-alter` with several bends, `notehead` SMuFL 1.4 range | S12/S17 discarded |
| `kind` suspended and N.C. chord symbols | S22 discarded with `harmony` |
| `level` purpose | S25 discarded |
| `lyric` alignment | S14 discarded |
| `rehearsal` section names | S13 discarded direction type |
| `staff-type` options | S17 discarded within `staff-details` |
| Report, code-generation help | Not applicable |

## Remaining gaps

All unsupported 4.0 additions are reported, not modelled. Listening, swing and players (S26),
numerals (S22), part links and packaged parts (S19/S25), `for-part` (S16) and `system` scoping
need model support before they can be imported. Each still has to be removed from this list by
its owning task.
