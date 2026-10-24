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
| `PublicCorpusTest` | Opt-in. With `MUSICXML_CORPUS_DIR` set, parses every `.xml`/`.musicxml`/`.mxl` file under both policies, fails on an escaped exception or an error-free failure, and prints per-file outcomes, the first error, and per-feature unsupported counts. Corpus files are not committed. | JVM only |

Run the pinned [W3C MusicXML test suite](https://github.com/w3c-cg/musicxmlTestSuite) with:

```bash
scripts/run-w3c-suite.sh
```

The script shallow-fetches the suite at the pinned commit (or the revision given as its first
argument, such as `main`) into a temporary directory, runs `PublicCorpusTest`, writes a per-file
report to `build/w3c-suite/report.tsv` (strict and tolerant outcome, first error and discarded
feature groups per file) and deletes the download. Set `W3C_SUITE_REPORT` to change the report
path and `W3C_SUITE_KEEP=1` to keep the files. It needs git, bash (Git Bash on Windows) and a JDK.
Progress against the suite is tracked in [MX-22](https://github.com/k4pran/LunarScore/issues/51).

Run any other corpus directly with, for example:

```bash
MUSICXML_CORPUS_DIR=/path/to/musicxml MUSICXML_CORPUS_REPORT=/path/to/report.tsv ./gradlew :musicxml-parser:jvmTest --tests '*PublicCorpusTest*' --rerun -i
```

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

## Gaps found and resolved

| Finding | Before | After |
|---|---|---|
| Every exporter-written file starts with the standard `<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML x.y Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">` header | All 165 LilyPond files and 11 of the 29 W3C files failed with `UNSAFE_XML` | The exact Recordare public header (any version, partwise or timewise consistently, optional XML declaration first, no internal subset) is blanked before the reader and never resolved. Any other DOCTYPE, a second header, an internal subset, another host, a declaration after a comment or inside content stays `UNSAFE_XML`; entity references remain `MALFORMED_XML`. Line numbers are kept. |
| Exporters omit `divisions` (LilyPond 03e, 41h, 51a–d); the suite notes the agreed default is 1 | Fatal `INVALID_STRUCTURE` under both policies | `RECOVERED_VALUE` at the first duration or offset, once per part, with divisions 1: a warning when tolerant, a failure when strict. |
| A voice of only a no-break space (U+00A0) passed the XML-whitespace check | The model constructor threw `IllegalArgumentException` | `INVALID_VALUE` at the voice, on notes and directions |
| A part ID of, or containing, a no-break space | The model constructor threw | `INVALID_STRUCTURE` at `@id` |

## Target matrix

| Target | Status on 2026-10-23 (Windows host) |
|---|---|
| JVM (`jvmTest`) | 204 tests, 0 failures (`PublicCorpusTest` passes without a corpus) |
| Android host (`testAndroidHostTest`) | 203 tests, 0 failures |
| iOS (`iosArm64`, `iosSimulatorArm64`) | Main and test klibs compile (`compileTestKotlinIosSimulatorArm64`); linking and simulator runs need macOS with Xcode and are unverified |
| JS / Wasm | Not declared for `musicxml-parser` |

## Not covered

- Schema validation covers the fixture files only (`XsdValidationTest`, [manifest](fixture-manifest.md)); inline test variants and mutants are not schema-validated. F21 is knowingly schema-invalid.
- F05, F06 and F08 are covered by `PolyphonyImportTest` (MX-09) and F15 by `LyricImportTest` (MX-14); F20 by `MxlImportTest` (MX-19); F22/F23 are covered inline and by the mutants above, not as files.
- No legal exporter corpus beyond the two public suites: real-world exporter files are only used when redistribution rights are known.
- Mutations are lexical, not schema-aware, and do not prove that every invalid input is rejected; they prove that every outcome is structured.
