# MX-21 compliance, compatibility and security audit

Audit date: 2026-10-23. Scope: the `musicxml-parser` score importer after MX-10–MX-20, against the
[MusicXML 4.0 spec](https://www.w3.org/2021/06/musicxml40/) and
[XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/). Results feed the
[coverage matrix](spec-coverage.md) and [risks and gaps](risks-and-gaps.md#mx-21-compliance-audit-decisions-and-remaining-gaps).

## Committed evidence

| Suite | What it asserts | Runs on |
|---|---|---|
| `ComplianceAuditTest` (`AuditCorpus.kt`) | Every executable fixture (F01–F19, F21, the F12 pair, the F21 concert-score subset and the six mixed-feature fixtures) imports under both policies. Deterministic mutants of each fixture: every non-blank text node and attribute value replaced by boundary values (empty, blanks, no-break space, zero, negative, `Int` overflow, exponent, `NaN`, 17, enumeration words), every element removed, duplicated and swapped with its sibling, a foreign-namespace element inserted in every element, and a truncation every 7 characters. Each outcome must be well formed: no exception escapes, a failure carries an error, a strict success carries no findings, every unsupported finding names an S01–S27 group or `foreign-extension`, and a successful score passes model `validate()`, `effectiveStates()` and `playbackOrder()`. Every truncation fails, every foreign element produces a diagnostic, each resource budget fails with its typed code and cancellation at every check fails with `CANCELLED`. | Common: JVM, Android host; iOS compiled |
| `MixedFeatureImportTest` (`MixedFeatureFixtures.kt`) | Test-plan pairs: divisions change + backup, tied chord + changing staff, grace + lyric, mid-measure transposition + divisions change, timewise + two staves (equal to its partwise twin). Repeat ending + pickup is F16. Supported content keeps exact timing, links and sounding pitch; the other feature gets exactly the expected `UNSUPPORTED_FEATURE` paths, and strict parsing rejects it. Since MX-09 the backup, tied-chord and two-staff pairs import fully with no diagnostics. | Common |
| `ScoreXmlReaderTest`, `NoteImportTest`, `DirectionImportTest` regressions | Standard DOCTYPE header, missing divisions (see below). | Common |
| `PublicCorpusTest`, `CorpusRunner.kt` | Opt-in. With `MUSICXML_CORPUS_DIR` set, parses every `.xml`/`.musicxml` file with `parseMusicXml` and every `.mxl` with `parseMxl` under both policies, fails on an escaped exception, an error-free failure, a strict success with findings, a playback-order exception or a clean acceptance of a file expected to be diagnosed, and prints the summary. `MUSICXML_CORPUS_REPORT` writes a Markdown report with per-file strict/tolerant outcomes, the first error and per-group discard counts. `CorpusRunnerTest` covers the runner on inline fixtures. Corpus files are not committed. | JVM only |

Run any local corpus with, for example:

```bash
MUSICXML_CORPUS_DIR=/path/to/musicxml MUSICXML_CORPUS_REPORT=/tmp/report.md ./gradlew :musicxml-parser:jvmTest --tests '*PublicCorpusTest*' --rerun -i
```

The W3C suite has its own command; see [below](#w3c-musicxml-test-suite-mx-22).

## Public corpus results

Files were downloaded to a temporary directory and not redistributed.

| Corpus | Files | Strict | Tolerant |
|---|---|---|---|
| [Unofficial MusicXML test suite](https://gitlab.com/lilypond/lilypond/-/tree/master/input/regression/musicxml) (Reinhold Kainhofer, MIT), LilyPond `master` on 2026-10-23 | 165 | 14 accepted; 148 `UNSUPPORTED_FEATURE`; 3 `RECOVERED_VALUE` | 14 accepted; 150 accepted with warnings; 1 `INVALID_STRUCTURE` |
| [W3C MusicXML test suite](https://github.com/w3c-cg/musicxmlTestSuite) (MIT), pinned `77c19f7`, run on 2026-10-24 after time symbols were kept | 182 + 1 `.mxl` | 75 accepted; 101 `UNSUPPORTED_FEATURE`; 4 `RECOVERED_VALUE`; 2 `UNMATCHED_SPAN`; 1 `INVALID_CURSOR_MOVE` | 75 accepted; 106 accepted with warnings; 2 `INVALID_STRUCTURE` (41g, 41h) |
| [W3C MusicXML repository](https://github.com/w3c/musicxml) `gh-pages`, `tests/files` (MusicXML 4.1 drafts) | 18 + `catalog.xml` | 18 `UNSUPPORTED_FEATURE`; `catalog.xml` `UNSAFE_XML` | 17 accepted with warnings; 1 `INVALID_STRUCTURE`; `catalog.xml` `UNSAFE_XML` |
| W3C `docs/src/data/examples/musicxml` tutorials | 11 | 11 `UNSUPPORTED_FEATURE` | 11 accepted with warnings |

No file threw and no failure lacked an error. Remaining tolerant rejections are intended:

- `41h-TooManyParts.xml` declares a part that is not in the part list; `INVALID_STRUCTURE`, as the suite expects of a strict reader.
- `accidentals.musicxml` repeats `<accidental>` on one note, which only MusicXML 4.1 allows; under 4.0 this is `INVALID_STRUCTURE` (recorded as a gap).
- `catalog.xml` is an OASIS catalog with its own DOCTYPE, not a score.

The most frequent tolerant losses in the LilyPond suite, by files reporting each group, are S07 (96,
mostly `time/@symbol`), S06 (50, voices/backup/chords), S14 (31, lyrics), S08 (21, `part-group` and
staves), S03 (19, credits and part-name display), S13 (16), S12 (15) and S17 (15). Each loss is an
`UNSUPPORTED_FEATURE` with a source path; see the domain documents for the per-element list.
These counts were taken before MX-09 and MX-14 imported voices, chords, staves and lyrics and have not been re-run.

## W3C MusicXML test suite (MX-22)

One command downloads the [W3C MusicXML test suite](https://github.com/w3c-cg/musicxmlTestSuite)
(MIT) at the pinned commit `77c19f7e819154c70ca1a1992e80dcda8ff82fea`, runs every file in
`xmlFiles` through `PublicCorpusTest`, writes the report and deletes the download, also when the
run fails:

```bash
scripts/run-w3c-suite.sh            # pinned revision
scripts/run-w3c-suite.sh <revision> # or W3C_SUITE_REVISION=<revision>
```

It needs `curl`, `tar` and a POSIX shell (Git Bash on Windows). The report goes to
`musicxml-parser/build/reports/w3c-suite/<revision>.md`, or `W3C_SUITE_REPORT`. Files whose name
contains `.invalid.` (41g, 74b, 75b) and 11b, whose `backup` moves before the start of its measure,
are expected to be diagnosed: the run fails if either policy accepts one without a diagnostic.

Results on 2026-10-24 at `fe69988`, 183 files (182 `.musicxml` and `90a-Compressed-MusicXML.mxl`):

| Policy | Outcome |
|---|---|
| Strict | 75 accepted; 101 `UNSUPPORTED_FEATURE`; 4 `RECOVERED_VALUE`; 2 `UNMATCHED_SPAN`; 1 `INVALID_CURSOR_MOVE` |
| Tolerant | 75 accepted; 106 accepted with warnings; 2 `INVALID_STRUCTURE` (41g `.invalid`, 41h) |

No file threw, no failure lacked an error and every expected-diagnosed file was diagnosed. 41h is
the only valid file the tolerant policy rejects (#53). 74b and 75b are diagnosed only because their
`figured-bass` and `accordion-registration` content is discarded; the invalidity itself is not
detected without XSD validation (#5).

Tolerant files discarding content, per group: S01 3, S03 21, S04 8, S07 17, S08 2, S09 9, S10 1,
S11 7, S12 17, S13 17, S14 6, S15 3, S16 1, S17 17, S22 10, S23 1, S24 7, S25 9, S26 1.

Each file that is not accepted cleanly under both policies is listed below with the open MX issues
whose fixture list or scope covers its losses; the strict column shows only the first error. 45a–45k
are listed because their playback order is tracked by #52 even when they import cleanly. The other
accepted files are omitted. Strict rejections marked intended are correct for strict parsing and
are kept as regressions. Re-run the command after each tracked issue lands and update this table.

| File | Strict (first error) | Tolerant | Tolerant discarded per group | Tracked by |
|---|---|---|---|---|
| `01e-Pitches-ParenthesizedAccidentals.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/accidental/@parentheses` | warnings | S04 10 | #56 |
| `02a-Rests-Durations.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/measure-style/multiple-rest` | warnings | S24 1 | #64 |
| `02b-Rests-PitchedRests.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/rest/display-step` | warnings | S17 10 | #65 |
| `02c-Rests-MultiMeasureRests.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/measure-style/multiple-rest` | warnings | S24 4 | #64 |
| `02d-Rests-Multimeasure-TimeSignatures.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S24 4 | #56, #64 |
| `02f-Rests-Placement.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/rest/display-step` | warnings | S17 24 | #65 |
| `03d-Rhythm-DottedDurations-Factors.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/measure-style/multiple-rest` | warnings | S24 8 | #64 |
| `03e-Rhythm-No-Divisions.musicxml` | RECOVERED_VALUE `part/measure/note/duration` | warnings | none | #53; strict policy decided in #53 |
| `03e-Rhythm-SecondaryBeamBreaks.musicxml` | UNSUPPORTED_FEATURE `part/measure/@width` | warnings | S01 1 | #57 |
| `11b-TimeSignatures-NoTime.musicxml` | INVALID_CURSOR_MOVE `part/measure/backup` | warnings | S07 2 | #56; intended: backup before measure start |
| `11c-TimeSignatures-Complex.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 2 | #54 |
| `11c-TimeSignatures-CompoundSimple.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 2 | #54 |
| `11d-TimeSignatures-ComplexMultiple.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 2 | #54 |
| `11d-TimeSignatures-CompoundMultiple.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 2 | #54 |
| `11e-TimeSignatures-ComplexMixed.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 1 | #54 |
| `11e-TimeSignatures-CompoundMixed.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 1 | #54 |
| `11f-TimeSignatures-SymbolMeaning.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 1 | #54 |
| `11g-TimeSignatures-SingleNumber.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 1 | #54 |
| `11h-TimeSignatures-SenzaMisura.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 2 | #54 |
| `11i-TimeSignatures-Alternate.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/time` | warnings | S07 6 | #54 |
| `13c-KeySignatures-NonTraditional.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/key` | warnings | S07 7 | #55 |
| `13d-KeySignatures-Microtones.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/key` | warnings | S07 1 | #55 |
| `13e-KeySignatures-Cancel.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/key/cancel` | warnings | S07 4 | #55 |
| `13e-KeySignatures-MidMeasure-Change.musicxml` | accepted | accepted | none | #55  |
| `13f-KeySignatures-Visible.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/key/@print-object` | warnings | S07 1 | #56 |
| `14a-StaffDetails-LineChanges.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S17 2, S25 2 | #58 |
| `21b-Chords-TwoNotes.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/tied/@placement` | warnings | S09 4 | #62 |
| `21d-Chords-SchubertStabatMater.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/words/@font-weight` | warnings | S12 1, S13 1 | #60 |
| `21f-Chord-ElementInBetween.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/segno` | warnings | S13 1 | #63 |
| `21g-Chords-Tremolos.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/ornaments/tremolo` | warnings | S12 5 | #60 |
| `22a-Noteheads.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notehead` | warnings | S17 114 | #65 |
| `22b-Staff-Notestyles.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/measure-style/slash` | warnings | S04 10, S24 4 | #56, #64 |
| `22c-Noteheads-Chords.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notehead` | warnings | S17 11 | #65 |
| `22d-Parenthesized-Noteheads.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notehead` | warnings | S17 10 | #65 |
| `23b-Tuplets-Styles.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S11 41 | #56, #66 |
| `23c-Tuplet-Display-NonStandard.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S11 37 | #56, #66 |
| `23d-Tuplets-Nested.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S11 16 | #56, #66 |
| `23e-Tuplets-Tremolo.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S12 8 | #56, #60 |
| `31a-Directions.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S13 30 | #56, #63 |
| `31b-Directions-Order.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S13 2 | #56, #57 |
| `31c-MetronomeMarks.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/metronome` | warnings | S13 4 | #63 |
| `31d-Directions-Compounds.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S13 25 | #56, #57, #63 |
| `31f-Direction-Multiline-Compounds.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S13 8 | #56, #57 |
| `32a-Notations.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/fermata` | warnings | S12 80 | #60, #61 |
| `32b-Articulations-Texts.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/words/@font-size` | warnings | S13 13 | #57 |
| `32d-Arpeggio.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/arpeggiate` | warnings | S12 15 | #60 |
| `32e-Arpeggios-Cross.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/arpeggiate` | warnings | S12 30 | #60 |
| `32e-Fermatas.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/fermata` | warnings | S12 7 | #60 |
| `33a-Spanners.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/tuplet/@placement` | warnings | S09 2, S11 1, S12 17, S13 25, S25 2 | #61, #62, #68 |
| `33c-Spanners-Slurs.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S09 6 | #56, #62 |
| `33d-Spanners-OctaveShifts.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/octave-shift` | warnings | S13 8 | #62 |
| `33e-Spanners-OctaveShifts-InvalidSize.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/octave-shift` | warnings | S13 4 | #62 |
| `33f-Trill-EndingOnGraceNote.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/slur/@placement` | warnings | S09 1, S12 3 | #62 |
| `33g-Slur-ChordedNotes.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S09 2 | #56, #62 |
| `33h-Spanners-Glissando.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/glissando` | warnings | S12 20 | #62 |
| `33i-Ties-NotEnded.musicxml` | UNMATCHED_SPAN `part/measure/note/tie` | warnings | none | #67; intended: tie without stop |
| `33j-Beams-Tremolos.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/ornaments/tremolo` | warnings | S12 12 | #60 |
| `33k-Tie-Types.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/rest/display-step` | warnings | S09 1, S17 2 | #65, #67 |
| `34a-Print-Object-Spanners.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/@print-object` | warnings | S04 18, S09 2, S11 2, S12 24 | #56, #61, #62 |
| `34b-Colors.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/technical` | warnings | S04 1, S07 6, S09 2, S10 4, S11 1, S12 32, S13 17, S14 1, S15 4, S17 5, S24 2 | #57, #61, #63 |
| `34c-Font-Size.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/@font-size` | warnings | S04 4, S07 6, S11 4, S12 24, S13 12, S14 1, S17 2, S24 2 | #57, #61, #63 |
| `34d-Note-Attributes.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/@print-leger` | warnings | S04 15, S14 4 | #56 |
| `41c-StaffGroups.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S08 1, S16 1, S17 3, S25 22 | #58, #68 |
| `41d-StaffGroups-Nested.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S25 4 | #58 |
| `41e-StaffGroups-InstrumentNames-Linebroken.musicxml` | accepted | accepted | none | #58  |
| `41f-StaffGroups-Overlapping.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S25 4 | #58 |
| `41g-PartNoId.invalid.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | rejected INVALID_STRUCTURE | S03 1 | #53, #56; intended: `.invalid`, part without `id` |
| `41g-StaffGroups-NestingOrder.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S25 14 | #58 |
| `41h-TooManyParts.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | rejected INVALID_STRUCTURE | S03 1 | #53, #56, #58 |
| `41i-PartNameDisplay-Override.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name-display` | warnings | S17 2 | #58 |
| `41j-PartNameDisplay-Multiple-DisplayText-Children.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name-display` | warnings | S17 1 | #58 |
| `41k-PartName-Print.musicxml` | UNSUPPORTED_FEATURE `part/measure/print/part-name-display` | warnings | S17 2 | #58 |
| `41l-GroupNameDisplay-Override.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S25 4 | #58 |
| `42a-MultiVoice-TwoVoicesOnStaff-Lyrics.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/fermata` | warnings | S12 2, S17 4 | #60, #65 |
| `42b-MultiVoice-MidMeasureClefChange.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/slur/@placement` | warnings | S09 3 | #62 |
| `43e-Multistaff-ClefDynamics.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/wedge/@spread` | warnings | S13 2 | #63 |
| `43g-MultiStaff-PartSymbol.musicxml` | UNSUPPORTED_FEATURE `part/measure/attributes/part-symbol` | warnings | S08 1 | #58 |
| `45a-SimpleRepeat.musicxml` | accepted | accepted | none | #52 |
| `45b-RepeatWithAlternatives.musicxml` | accepted | accepted | none | #52 |
| `45c-RepeatMultipleTimes.musicxml` | accepted | accepted | none | #52 |
| `45c-SimpleRepeat-Nested.musicxml` | accepted | accepted | none | #52 |
| `45d-Repeats-MultipleEndings.musicxml` | accepted | accepted | none | #52 |
| `45d-Repeats-Nested-Alternatives.musicxml` | accepted | accepted | none | #52 |
| `45e-Repeats-Combination.musicxml` | accepted | accepted | none | #52 |
| `45e-Repeats-Nested-Alternatives.musicxml` | accepted | accepted | none | #52 |
| `45f-Repeats-InvalidEndings.musicxml` | UNMATCHED_SPAN `part/measure/barline/ending` | warnings | none | #52; intended: ending stop without start |
| `45g-Repeats-NotEnded.musicxml` | accepted | accepted | none | #52 |
| `45h-Repeats-Partial.musicxml` | UNSUPPORTED_FEATURE `part/measure/barline/repeat/@winged` | warnings | S15 2 | #52, #68 |
| `45i-Repeats-Nested.musicxml` | accepted | accepted | none | #52 |
| `45j-SimpleRepeat-Start.musicxml` | accepted | accepted | none | #52 |
| `45k-Repeats-Chords-FiguredBass.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 8 | #52, #59 |
| `46g-PickupMeasure-Chordnames-FiguredBass.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 4 | #59 |
| `46h-Barline-Fermata.musicxml` | UNSUPPORTED_FEATURE `part/measure/barline/fermata` | warnings | S15 3 | #60 |
| `51a-Header-Credits.musicxml` | UNSUPPORTED_FEATURE `credit/credit-words/@default-x` | warnings | S03 40 | #53, #57 |
| `51b-Header-Quotes.musicxml` | RECOVERED_VALUE `part/measure/note/duration` | warnings | none | #53; strict policy decided in #53 |
| `51c-MultipleMetadata.musicxml` | RECOVERED_VALUE `part/measure/note/duration` | warnings | none | #53; strict policy decided in #53 |
| `51c-MultipleRights.musicxml` | RECOVERED_VALUE `part/measure/note/duration` | warnings | none | #53; strict policy decided in #53 |
| `51d-EmptyTitle.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1 | #53 |
| `52a-PageLayout.musicxml` | UNSUPPORTED_FEATURE `defaults/music-font` | warnings | S01 5, S03 7, S13 4, S17 2, S26 2 | #57, #68 |
| `52b-Breaks.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1 | #56 |
| `61h-Lyrics-BeamsMelismata.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/lyric/extend/@color` | warnings | S14 1 | #57 |
| `61j-Lyrics-Elisions.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/lyric/@color` | warnings | S14 4 | #57 |
| `61k-Lyrics-SpannersExtenders.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1, S14 3 | #56, #57 |
| `71a-Chordnames.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 8 | #59 |
| `71b-Chordnames-Kinds.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 32 | #59 |
| `71c-ChordsFrets.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 8 | #59 |
| `71d-ChordsFrets-Multistaff.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 4 | #59 |
| `71e-TabStaves.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S04 3, S12 65, S17 1, S23 43, S25 2 | #58, #61 |
| `71f-AllChordTypes.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 38 | #59 |
| `71g-MultipleChordnames.musicxml` | UNSUPPORTED_FEATURE `part/measure/harmony` | warnings | S22 4 | #59 |
| `72b-TransposingInstruments-Full.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1 | #56 |
| `72c-TransposingInstruments-Change.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name-display` | warnings | S17 4 | #58 |
| `72d-TransposingInstruments-scorePitch.musicxml` | UNSUPPORTED_FEATURE `part-list/score-part/part-name/@print-object` | warnings | S03 1 | #56 |
| `73a-Percussion.musicxml` | UNSUPPORTED_FEATURE `part-list/part-group` | warnings | S25 2 | #58 |
| `74a-FiguredBass.musicxml` | UNSUPPORTED_FEATURE `part/measure/figured-bass` | warnings | S22 4 | #59 |
| `74b-FiguredBass.invalid.musicxml` | UNSUPPORTED_FEATURE `part/measure/figured-bass` | warnings | S22 5 | #59; `.invalid` diagnosed only through discarded `figured-bass` |
| `75a-AccordionRegistrations.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/accordion-registration` | warnings | S13 16 | #63 |
| `75b-Accordion.invalid.musicxml` | UNSUPPORTED_FEATURE `part/measure/direction/direction-type/accordion-registration` | warnings | S13 4 | #63; `.invalid` diagnosed only through discarded `accordion-registration` |
| `90a-Compressed-MusicXML.mxl` | UNSUPPORTED_FEATURE `part/measure/@width` | warnings | S01 1 | #57 |
| `99a-Sibelius5-IgnoreBeaming.musicxml` | UNSUPPORTED_FEATURE `identification/encoding/supports` | warnings | S03 2, S04 6 | #68 |
| `99c-Wavy-Lines-No-Numbers.musicxml` | UNSUPPORTED_FEATURE `part/measure/note/notations/ornaments/wavy-line` | warnings | S12 4 | #62 |

## Gaps found and resolved

| Finding | Before | After |
|---|---|---|
| Every exporter-written file starts with the standard `<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML x.y Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">` header | All 165 LilyPond files and 11 of the 29 W3C files failed with `UNSAFE_XML` | The exact Recordare public header (any version, partwise or timewise consistently, optional XML declaration first, no internal subset) is blanked before the reader and never resolved. Any other DOCTYPE, a second header, an internal subset, another host, a declaration after a comment or inside content stays `UNSAFE_XML`; entity references remain `MALFORMED_XML`. Line numbers are kept. |
| Exporters omit `divisions` (LilyPond 03e, 41h, 51a–d); the suite notes the agreed default is 1 | Fatal `INVALID_STRUCTURE` under both policies | `RECOVERED_VALUE` at the first duration or offset, once per part, with divisions 1: a warning when tolerant, a failure when strict. |
| A voice of only a no-break space (U+00A0) passed the XML-whitespace check | The model constructor threw `IllegalArgumentException` | `INVALID_VALUE` at the voice, on notes and directions |
| A part ID of, or containing, a no-break space | The model constructor threw | `INVALID_STRUCTURE` at `@id` |

## Target matrix

| Target | Status on 2026-10-24 (Windows host) |
|---|---|
| JVM (`jvmTest`) | 239 tests, 0 failures (`PublicCorpusTest` passes without a corpus; with the W3C suite it passes over 183 files) |
| Android host (`testAndroidHostTest`) | 235 tests, 0 failures (the corpus runner and its tests are JVM only) |
| iOS (`iosArm64`, `iosSimulatorArm64`) | Main and test klibs compile (`compileTestKotlinIosSimulatorArm64`); linking and simulator runs need macOS with Xcode and are unverified |
| JS / Wasm | Not declared for `musicxml-parser` |

## Not covered

- Schema validation covers the fixture files only (`XsdValidationTest`, [manifest](fixture-manifest.md)); inline test variants and mutants are not schema-validated. F21 is knowingly schema-invalid.
- F05, F06 and F08 are covered by `PolyphonyImportTest` (MX-09) and F15 by `LyricImportTest` (MX-14); F20 by `MxlImportTest` (MX-19); F22/F23 are covered inline and by the mutants above, not as files.
- No legal exporter corpus beyond the two public suites: real-world exporter files are only used when redistribution rights are known.
- Mutations are lexical, not schema-aware, and do not prove that every invalid input is rejected; they prove that every outcome is structured.
