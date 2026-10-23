# Risks, gaps and decision log

Status: open design risks, audited 2026-10-15 for MX-00. Review against the [MusicXML 4.0 reference](https://www.w3.org/2021/06/musicxml40/musicxml-reference/) when implementing each feature. `spec-coverage.md` records support separately from tests.

| Risk | Mitigation and test |
|---|---|
| Public model loses format details | Keep written pitch, performed duration, notation and playback fields separate; log unsupported data with source location. Round-trip is not promised. Test accidentals, print hints and notes with different notated/performed values. |
| Timing drift with changing divisions, backup/forward and tuplets | Rational quarter-note offsets; measure-local cursor; explicit state timeline. Assert exact onset/duration across changes and overlapping voices. |
| Chord children can have different durations | Store noteheads independently at one onset; a chord tone may be shorter but cannot be longer than its reference note per the [chord definition](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chord/). Test shorter/equal members and diagnose a longer member. |
| Written versus sounding pitch confusion | Preserve written pitch and transposition separately; derive sounding pitch only when possible. Test transposing and concert-score cases. |
| Spans missing an endpoint or crossing measures | Stable event IDs and a resolver with explicit unmatched-span diagnostics; test ties, slurs and endings across boundaries. |
| Valid XML but inconsistent music | Separate XSD validation, semantic validation and tolerant import. Test overfilled/underfilled measures, orphaned part IDs and conflicting attributes with documented policy. |
| Malicious XML or large scores | Disable external entities/DTD, bound bytes/depth/elements/time/diagnostics, and test bombs and malformed encoding. |
| Compressed archive attacks | Bound ZIP expansion and entries; reject traversal/duplicate rootfiles and oversized container; test malicious archives. |
| iOS XML/ZIP support differs from JVM | Keep both behind small interfaces and run compile plus device/simulator smoke tests on macOS before declaring support. |
| Renderer currently handles only simple single-staff music | Keep importer model independent; add adapter incrementally without forcing import data into current renderer types. |
| music21 differs from specification | Specification wins; keep documented compatibility mode only for real exporter cases. Differential tests are advisory, not acceptance criteria. |
| Licensing of borrowed code and fixtures | Do not copy music21 code mechanically; keep its BSD notice if code is adapted. Carry MIT notice for imported test-suite fixtures. Verify each real-world score's redistribution rights. |
| MusicXML 4.1 may change semantics | Pin 4.0 references and version in tests; catalogue 4.1 changes only after publication. |

The XML/ZIP dependency decisions, neutral model and MX-03 diagnostics contract are recorded below.
Public policy and retention budgets are defined; MX-05 implements reader enforcement, and the raw
ZIP safety layer remains MX-19 work. MX-02 chooses a staged renderer adapter.

## MX-01 dependency decisions and runtime limitations

MX-01 closes the initial dependency/API investigation; see [dependency evidence](dependency-spike.md).
The renderer migration is recorded under MX-02 below; the public retention budget is now defined
by [MX-03](diagnostics.md). Existing probes still use their original internal types and limits.

These are actual internal spike outcomes; they do not finalize the public MX-03 diagnostic contract.
Every failing probe returns one typed `ProbeDiagnostic` and clears partial output.

| Case | Current structured outcome / evidence | Remaining work |
|---|---|---|
| DTD, external/internal entity declarations | `UNSAFE_XML` before reader creation; network/file/parameter/expansion smoke tests pass | MX-05: decide whether to accept safe MusicXML DTD headers without resolution |
| Literal declaration token in comments/CDATA | Conservative `UNSAFE_XML`; comment regression test | MX-05: refine lexical policy if needed |
| Undeclared entity or malformed XML | `MALFORMED_XML`, no partial events | MX-03 defines the public code/location contract; MX-05 must populate it from the reader |
| Non-UTF-8/invalid UTF-8/NUL-bearing input | `UNSUPPORTED_ENCODING` | MX-05: encoding detection and bounded streaming policy |
| Namespace-bearing score root | `UNSUPPORTED_NAMESPACE`; prefixed/default namespace tests | MX-05: finalize compatibility policy; foreign child namespaces are event evidence only |
| Auxiliary/unknown root | `UNSUPPORTED_ROOT`; opus/sounds tests | MX-05/MX-19: document-kind handling; no automatic reference fetching |
| XML byte/depth/element bound | `INPUT_LIMIT` / `DEPTH_LIMIT` / `ELEMENT_LIMIT`; boundary tests | MX-03 defines public budgets; MX-05 enforces input/cancellation/deadline policy |
| ZIP entry exceeds expanded-byte bound | `ARCHIVE_ENTRY_LIMIT`; DEFLATE boundary test | MX-19: global entry count, bytes and ratio bounds |
| Missing entry / invalid ZIP | `ARCHIVE_READ_FAILED`; tests also check source cleanup | MX-19: stable detailed archive diagnostics |
| Unsafe ZIP names/duplicates and hidden compressed sizes | No untrusted archive API exists; Okio source inspection shows normalized/collapsed metadata | MX-19 must validate raw directory metadata before using Okio, or replace it; do not ship the probe as an MXL reader |
| iOS runtime differences | Main/test klibs compile for both iOS targets; no runtime claim | macOS/Xcode simulator/device smoke runs required |

ZIP CRC/encryption/container/mimetype/rootfile validation and semantic feature diagnostics remain
unimplemented. The probes do not import any musical content. JS/Wasm are intentionally deferred
for this module; the existing rendering targets are unaffected.

## MX-02 model decisions and remaining limits

The [neutral model contract](model.md) records the implemented vocabulary and renderer adapter.
Model-only F01/F05/F10/F17 projections now exist; no XML importer or source diagnostic API is added.

| Case | Current structured outcome / evidence | Remaining work |
|---|---|---|
| Exact rational exceeds reduced Int component bounds | `RationalException(OVERFLOW)`; reduction/boundary tests, no wrap or rounding | MX-03/MX-07 must translate numeric failures to source-located diagnostics; wider numeric support is not promised |
| Invalid fraction or division by zero | `ZERO_DENOMINATOR` / `DIVISION_BY_ZERO`; independent codes tested | Parser must distinguish malformed input from a resource/numeric limit |
| Orphan or forward relationship endpoints | Retained IDs/nulls; `MISSING_ENDPOINT` / `UNKNOWN_EVENT` model diagnostics; F10 projection | Source resolution, pitch pairing, locations and strict/tolerant policy done in MX-11 (see below) |
| Duplicate IDs, unknown staff/instrument | `DUPLICATE_ID`, `UNKNOWN_STAFF`, `UNKNOWN_INSTRUMENT` with neutral paths; graph tests | MX-03 source locations; MX-06/MX-09 importer policies |
| Simple meter/state vocabulary only | No source import or implicit loss; additive meters/keys/other domains remain unimplemented | Their feature adapters must retain data or emit structured unsupported diagnostics before claiming support |
| Rich model cannot yet be rendered | Existing renderer-to-neutral adapter preserves its complete current subset; legacy tests retained | Later neutral-to-layout adapter must report each unsupported case; no reverse adapter is exposed now |
| Transposition, spans and chord consistency | Separate fields represent facts exactly; model does not infer sounding pitches or repair spans | MX-09/MX-11/MX-16 semantic validation and derivation remain |

No new source-content discard branch is introduced. `ModelDiagnostic` is a neutral graph check,
not the MX-03 importer diagnostic/provenance contract. The model has no dependency on the
MX-01 XML/ZIP probes; their implementation and test evidence are preserved separately above.

## MX-03 diagnostic and provenance decisions

The [public contract](diagnostics.md) now exists, with an internal policy/budget collector and
contract-level F22/F23 tests. There is no semantic importer or production XML reader yet. The
following outcomes are tested by supplying findings to the collector, not by parsing XML.

| Case | Structured contract outcome / evidence | Remaining work |
|---|---|---|
| Unknown-to-adapter but valid element/attribute, including S25 grouping | `UNSUPPORTED_FEATURE` with feature ID, source location and `DISCARDED`, `OPAQUE` or `CONVERTED`; all reject strict, warn tolerant; grouping contract test | MX-05/feature owners must emit for every unmapped meaningful subtree/attribute, including children of recognized parents |
| Missing required structure/value or invalid root/syntax (F22) | Fatal `INVALID_STRUCTURE`, `INVALID_VALUE`, `UNSUPPORTED_ROOT`, `MALFORMED_XML` under both policies; null result value | MX-04 source fixtures; MX-05/MX-06/MX-07 actual classification, not blanket XSD validation |
| Orphan span, invalid cursor, incompatible measure extent (F23) | `UNMATCHED_SPAN`, `INVALID_CURSOR_MOVE`, `INCONSISTENT_MEASURE_DURATION`; errors strict, warnings tolerant | MX-09/MX-11 detect and explicitly describe repair/loss; no silent cursor clamp |
| Exact rational overflow / invalid rational value | Stable fatal `NUMERIC_OVERFLOW` / `INVALID_VALUE`; fatal code policy tests | MX-07 must translate `RationalException` and attach real source coordinates; no approximation |
| Opaque source exceeds aggregate retention budget | Fatal `RETENTION_LIMIT`; cumulative multibyte UTF-8 and zero-budget tests | Reader must bound capture before allocating a fragment; no round-trip or unlimited opaque storage promise |
| Findings exceed cap | Fatal `DIAGNOSTIC_LIMIT` replaces last finding within hard cap; cap=1/2/3 tests; no accepted partial score | Reader/semantic adapters must stop consumption when collector stops |
| Too many source mappings | Fatal `PROVENANCE_LIMIT`; zero/exact/excess tests | Feature adapters supply neutral paths and actual source coordinates |
| Fatal after warnings or strict rejection | Failure retains ordered diagnostics but no score/model provenance; snapshot tests | Wire reader cleanup/cancellation to same result contract in MX-05 |
| Unsupported document kinds, encoding, namespace and archives | Stable fatal codes defined; no new input acceptance | MX-05/MX-19 finalize input/namespace/archive policy and emission; auxiliary documents stay unsupported |

Defaults are strict, 1,000 diagnostics, 65,536 retained UTF-8 source bytes and 100,000 provenance
entries. MX-05 enforces input byte/depth/element limits and supplies cooperative cancellation.
Deadlines, bounded stream acquisition and global ZIP safety budgets remain adapter work.
Locations may lack unavailable coordinates; they never fabricate them.
No new source discard path is exposed by MX-03, and tests do not upgrade parser feature coverage.

## MX-05 reader decisions and remaining gaps

The internal [secure ordered reader](xml-reader.md) now emits the public diagnostic contract.
It provides no semantic score importer, XSD validator or archive adapter. These decisions supersede
MX-01's provisional reader-policy entries above; the original probes remain unchanged.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| All DTD/entity declarations, including benign MusicXML DTD headers | Fatal `UNSAFE_XML` before reader construction; external network/file, parameter and expansion fixtures under both policies | Supporting selected headers without resolution is deliberately deferred |
| Declaration tokens inside comments/CDATA/PIs | Conservative fatal `UNSAFE_XML`; comment/CDATA regressions | Lexically relaxing this compatibility restriction is deferred |
| Non-UTF-8, invalid UTF-8, NUL-bearing source | Fatal `UNSUPPORTED_ENCODING`; optional UTF-8 BOM accepted | Broader encoding support / bounded stream acquisition deferred |
| XML version other than 1.0 | New fatal `UNSUPPORTED_XML_VERSION`, tested in both policies | XML 1.1 deliberately unsupported |
| Namespace-bearing score root; opus/sounds/unknown root | Fatal `UNSUPPORTED_NAMESPACE` / `UNSUPPORTED_ROOT` with available source coordinates | No namespace rewriting, auxiliary-document import or reference fetching |
| Foreign child names/attributes | Expanded names preserved in ordered snapshots; prefix/default scope tests | Semantic adapters must report unsupported meaningful content; reader success is not musical support |
| Malformed syntax/character references/namespaces | Fatal `MALFORMED_XML`; dependency EOF exception translation and lexical/namespace guards have regression fixtures | Broader exporter/security corpus remains MX-21 |
| Nonempty namespace prefix declared with an empty URI | Fatal `MALFORMED_XML`; explicit wrapper guard and root/child/attribute/rebinding tests in both policies; current dependency already rejected these inputs before the guard | Valid empty default namespace resets and scope restoration remain accepted; the claimed existing bypass was not reproduced |
| Byte/depth/element excess; cancelled parse | Fatal `INPUT_LIMIT` / `DEPTH_LIMIT` / `ELEMENT_LIMIT` / `CANCELLED`; exact/excess, deep/wide and tail-drain tests | Caller bytes are already materialized; cancellation is cooperative between bounded scans/tokens; deadlines deferred |
| Long nested names or namespace URIs amplify parent-prefixed source paths | Fatal `PATH_LIMIT` before exceeding the cumulative 8,388,608 UTF-16 code-unit budget; exact/excess/root/sibling/Unicode and default-budget adversarial tests | Larger legitimate scores can configure `maxSourcePathCharacters`; frames are not allowed to bypass the cumulative budget when closed |
| Many tiny attributes amplify XML token and snapshot allocations | Fatal `ATTRIBUTE_LIMIT` during preflight, before reader creation; defaults 256 per element/declaration and 100,000 total, including namespace/XML declaration fields; wide-root, many-small-tag, exact/excess and literal-text regressions | Legitimate extensions can configure both budgets explicitly; XML byte, depth, element and path bounds still apply |
| Fatal reader finding after consumer warnings/provenance | Ordered diagnostics, null value and empty model mappings; diagnostic-cap and malformed-tail tests | Semantic adapters must continue using shared context |
| F22 missing part-list / illegal duration | Well-formed XML reaches later adapters; explicit reader test | MX-06/MX-07 semantic detection and MX-04 XSD assertions still required |

JVM/Android host tests and iOS cross-compilation are recorded in [xml-reader.md](xml-reader.md).
Native runtime still requires macOS/Xcode. Opaque capture must bound fragments before allocation;
MXL safety remains MX-19 and JS/Wasm targets remain undeclared for this module.

## MX-15 grace and cue note decisions and remaining gaps

The importer now keeps [grace notes](grace-import.md) as untimed `GRACE` events linked to their
principal notes. This supersedes the MX-07 S18 discard of grace notes.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Missing grace `duration` | Normal per the 4.0 schema: null duration, cursor and extent unchanged, no divisions required (F19) | None |
| `duration` on a grace note | S18 `DISCARDED`; grace note kept without a duration | None |
| Principal note | Next imported note in the voice; else the previous note (after-grace) at a rest or measure end (F19) | Cross-measure principals; explicit after-grace semantics when MX-09 interprets backup |
| Grace before a discarded timed note, or with no note in its voice | `UNMATCHED_SPAN`; relationship kept with a null end (model `MISSING_ENDPOINT`) | MX-09 staves, MX-16 percussion |
| `steal-time-previous`/`-following` | Kept as exact percentages on `Grace`; playback time is not stolen | Performance timeline |
| `make-time`; cue-sized grace; grace rest | S18 `DISCARDED` (attribute, cue size, or whole rest) | Model if a consumer needs them |
| Grace chord tones | S06 `DISCARDED` with other chord notes; first grace kept | MX-09 chords |
| Cue or grace notes after `backup`/`forward` | S06 `DISCARDED` with other post-move content | MX-09 cursor interpretation |
| Invalid `slash` or steal-time; duplicate `grace` | Fatal `INVALID_VALUE`; fatal `INVALID_STRUCTURE` | None |

## MX-13 direction and note expression decisions and remaining gaps

The importer now keeps [directions and note expressions](direction-import.md). This supersedes the
MX-06 S13 discard of a measure's `direction` element, MX-11's S12 discard of `articulations`,
`ornaments` and `dynamics` notations, and the S26 discard of a direction's `sound` tempo/dynamics.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Semantic versus visual components | Mark type (`ArticulationType`, `OrnamentType`, dynamics markings, words, metronome, wedge type) kept apart from `placement`, `directive` and display `offset`; `sound` tempo/dynamics in `DirectionSound`, never derived from printed marks (F13/F14) | Renderer does not consume expressions or directions yet |
| Direction after backup/forward | S06 `DISCARDED`; no event, so it is never mistimed; a wedge partner becomes `UNMATCHED_SPAN` (inline test) | MX-09 cursor interpretation, then an exact-offset-after-backup fixture |
| Direction on staff ≠ 1 | S06 `DISCARDED`, like notes | MX-09 multiple staves |
| `offset` | Exact rational quarter notes, negative allowed; display only unless `sound="yes"`, which also sets `DirectionSound.offset`; offset before divisions fatal `INVALID_STRUCTURE` | None |
| Other direction types: `rehearsal`, `segno`, `coda`, `pedal`, `octave-shift`, `dashes`, `bracket`, `damp`, `eyeglasses`, `image`, `principal-voice`, `percussion`, `accordion-registration`, `harp-pedals`, `scordatura`, `string-mute`, `staff-divide`, `symbol`, `other-direction` | S13 `DISCARDED` per element; the rest of the direction is kept, and a direction with nothing interpretable has no event | Model and import each type; `PEDAL` relationship is reserved |
| Metric modulation, `beat-unit-tied`, `metronome-note` metronome forms | S13 `DISCARDED`; metronome dropped | Complex metronome model |
| Display attributes (fonts, enclosure, position, color, `parentheses`, wedge `spread`/`niente`/line style, `strong-accent type`, trill-sound and mordent attributes) | S13 or S12 `DISCARDED` per attribute; mark kept | Layout hints (MX-18); ornament realization |
| Other notations: `fermata`, `technical`, `arpeggiate`, `glissando`, `slide`, `accidental-mark`, `non-arpeggiate`, `other-notation`; other articulations/ornaments (`breath-mark`, `caesura`, `scoop`, `plop`, `doit`, `falloff`, `wavy-line`, `tremolo`, `other-ornament`, …) | S12 `DISCARDED` per element | Fermata, tremolo and wavy-line models |
| Other `sound` attributes and children; `listening` in a direction | S26 `DISCARDED`; tempo/dynamics kept | MX-16 instrument change, MX-20/21 listening and playback |
| Unmatched wedge endpoints or `continue` without an open wedge | `UNMATCHED_SPAN`; relationship kept with a null partner | None |
| Unknown placement, wedge type or beat unit; wedge number outside 1–16; malformed offset or sound values; blank per-minute | Fatal `INVALID_VALUE` | None |
| Direction without `direction-type`; wedge without type; metronome without beat-unit/per-minute; duplicate offset/voice/staff/sound | Fatal `INVALID_STRUCTURE` | None |

F13 and F14 are implementation-authored and not XSD-validated; iOS runtime still requires macOS.

## MX-12 beam, stem and tuplet decisions and remaining gaps

The importer now keeps [stems, beams, time modifications and tuplet spans](tuplet-import.md) on
imported notes and rests. This supersedes MX-07's S10/S11 discard of `stem`, `beam` and
`time-modification`, and MX-11's S11 discard of `notations/tuplet`.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Nested tuplets | Each note keeps its cumulative `time-modification` (F11 9:4); each bracket level is a separate `TUPLET` relationship by number; durations stay exact rationals from `duration` | Per-level ratios are not factored or stored separately |
| Tuplet numbers 1–16 | Accepted, including 7, 8 and 16 (F11); 0 and 17 fatal `INVALID_VALUE`. No fixed-size number state, so no index failure | None |
| Unmatched or duplicate tuplet endpoints | `UNMATCHED_SPAN`; relationship kept with a null partner | None |
| Unknown stem, beam type or normal type; beam number outside 1–8; nonpositive actual/normal counts | Fatal `INVALID_VALUE` | None |
| Missing tuplet type or actual/normal notes; `normal-dot` without `normal-type`; duplicate stem/time modification/beam level | Fatal `INVALID_STRUCTURE` | None |
| `tuplet-actual`/`tuplet-normal`; tuplet `bracket`, `show-number`, `show-type`, `line-shape`, `placement`, position | S11 `DISCARDED`; tuplet span kept | Tuplet display model (MX-18) |
| Beam `fan`, `repeater`, `color`, `id`; stem position and color | S10 `DISCARDED` per attribute; beam or stem kept | Feathered beams and layout (MX-18) |
| Beam continuity (begin without end), tuplet/duration agreement | Not checked; duration stays authoritative | Optional semantic check |
| Mixed chord values: chord tones with different type, time modification, stems or beams | Not reachable yet: chord notes are still S06 `DISCARDED` because MX-09 chord import is not on this branch | MX-09 chords, then a mixed-value chord fixture |

F09 and F11 are implementation-authored and not XSD-validated; iOS runtime still requires macOS.

## MX-11 tie and slur decisions and remaining gaps

The importer now [resolves ties and slurs](span-import.md) into `Score.relationships`. This
supersedes MX-07's S09 discard of `tie` and its S12 discard of the whole `notations` element.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Start without stop, stop without start, slur `continue` without an open slur | `UNMATCHED_SPAN` at the endpoint element; strict rejects, tolerant warns and keeps the relationship with a null partner (model `MISSING_ENDPOINT`) | None |
| Duplicate start (same kind, key and voice while open) | Earlier start reported as `UNMATCHED_SPAN` immediately and kept with a null end | None |
| Stop before its start in document order | Paired when the start is musically earlier (measure, onset); resolver unit test | XML evidence needs MX-09 backup/forward |
| Ties across voices; concurrent same-pitch ties | Pair by written pitch, preferring the same voice | Chord-tone ties need MX-09 chords |
| Endpoint on a discarded chord/unpitched/other-staff note (grace notes are imported since MX-15) | Not registered; the partner becomes `UNMATCHED_SPAN` | MX-09, MX-16 |
| Enharmonic ties; tie stop not at the tied note's end | Not paired / not checked | Optional semantic check |
| Missing `type`; unknown `type`; `number` outside 1–16 | Fatal `INVALID_STRUCTURE`; fatal `INVALID_VALUE` | None |
| `tie time-only`; `tied type="let-ring"`; tie or tied on a rest | S09 `DISCARDED`; mark dropped (a slur on a rest is kept) | Repeat-pass playback (MX-17); let-ring model |
| Display attributes on `tie`/`tied`/`slur` | S09 `DISCARDED` per attribute; span kept | MX-18 layout |
| Other `notations` children and `notations` attributes | S12 (S25 `footnote`/`level`) `DISCARDED` per child; `tuplet` superseded by MX-12 and articulations, ornaments and dynamics by MX-13 above | Remaining notations (fermata, technical, …) |

F10 is implementation-authored and not XSD-validated; iOS runtime still requires macOS.

## MX-10 timewise normalization decisions and remaining gaps

The importer now [normalizes timewise scores](timewise-import.md) to the partwise projection. This
supersedes the MX-06 fatal `UNSUPPORTED_ROOT` for timewise roots.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Declared part missing from a timewise measure | Empty measure with that measure's number and implicit flag, no diagnostic (valid MusicXML); F12 and leading-gap variant compare equal to explicit empty partwise measures; padding is bounded by `PROVENANCE_LIMIT` | None |
| Unknown, blank or duplicate part ID in a measure; declared part in no measure; measure before the part-list or without a number | Fatal `INVALID_STRUCTURE` in both policies | None |
| Parts in a different order within a measure | Part-list order; divisions carry per part across interleaved measures (F12) | None |
| Non-part children of a timewise measure, `part` directly under the root, unmapped timewise `part`/`measure` attributes | Feature-group `UNSUPPORTED_FEATURE` / `DISCARDED` (for example S13, S03, S01); strict rejects, tolerant warns | Measure-level semantics belong to their feature tasks |
| Measure attributes that differ per part (partwise allows a different `implicit` or `number` per part) | Timewise has one set per measure, so the projection is identical; partwise differences are kept as written | None |

F12 is implementation-authored and not XSD-validated; iOS runtime still requires macOS.

## MX-08 attribute state decisions and remaining gaps

The importer now emits [key, clef and staff-line state](attribute-state.md) alongside MX-07's
divisions and simple time, and the model resolves effective state per event. This supersedes
MX-07's S07 discard of key, clef, staves and staff details.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Notes before any key/clef/time | Effective state field is null; no treble, C major or 4/4 default; missing divisions stays fatal | Renderer adapter must choose any display fallback explicitly |
| Mid-measure and end-of-measure changes | Events at exact cursor offsets; resolver applies changes in onset order, same-onset in list order, persisting across measures (F07) | Same-onset ordering across voices is revisited with MX-09 cursor interpretation |
| Key change versus written pitch | `alter` and printed accidental are never inferred from or rewritten by the key (F07) | Accidental display inference is not planned in the importer |
| Clef without line | Standard line for G/F/C per MusicXML; null for percussion/TAB/jianpu/none | None |
| Invalid fifths/line/octave change/sign/staves/staff-lines/attribute `number` | Fatal `INVALID_VALUE`; missing fifths or sign `INVALID_STRUCTURE` | Full XSD lexical validation remains separate |
| `staves` > 1, `part-symbol`, key/time/clef/staff details scoped to staff ≠ 1 | S08 `DISCARDED` (time scope moved from S07 to S08) | MX-09 multi-staff |
| Non-traditional keys; unknown mode; key `cancel`/`key-octave`; key/clef display attributes | S07 `DISCARDED`; non-traditional key dropped, otherwise key/clef kept (unknown mode null) | Non-traditional key model |
| `staff-tuning`/`capo`; other staff details | S23 / S17 `DISCARDED`; staff-line count kept | MX-13/MX-18/MX-21 |
| `transpose`, `instruments`, `for-part`; `measure-style`; `directive` | S16 / S24 / S07 `DISCARDED` | MX-16, MX-17, MX-13 |
| State change after uninterpreted backup/forward | S06 `DISCARDED` for each key/time/clef/staff-lines change | MX-09 |

F07 is implementation-authored and not XSD-validated; iOS runtime still requires macOS.

## MX-07 note/rest decisions and remaining gaps

The public importer now produces [single-voice notes and rests](note-import.md) with exact onsets.
This supersedes MX-06's parent-level S04/S07 discard branches; MX-02's `RationalException` translation
obligation is met for durations and divisions.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Zero, negative or malformed duration/divisions; invalid step/octave/alter/type/voice/staff/yes-no flags | Fatal `INVALID_VALUE` at the element/attribute location under both policies | Full XSD lexical validation remains separate |
| Duration or divisions outside reduced Int bounds; overflowing quotient or cursor sum | Fatal `NUMERIC_OVERFLOW` at the duration/divisions location; boundary `Int.MAX_VALUE` accepted | Wider numeric support is not promised |
| Missing divisions before a duration; missing duration/pitch-or-rest/step/octave; duplicates | Fatal `INVALID_STRUCTURE`; no default divisions or duration inferred | MX-08 state resolution across parts/staves |
| Chord and grace notes | S06/S18 `DISCARDED`; whole note dropped and cursor not advanced (grace superseded by MX-15 above) | MX-09 chords |
| Unpitched notes; staff other than 1 | S16/S06 `DISCARDED`; note dropped, cursor still advances so later onsets stay exact | MX-16 percussion, MX-09 staves |
| Backup/forward | S06 `DISCARDED`; later notes/time signatures in that measure are each reported and dropped instead of mistimed; divisions still apply | MX-09 cursor interpretation and `INVALID_CURSOR_MOVE` |
| Ties, tuplet time-modification, stems/beams (superseded by MX-11/MX-12 above), notations, lyrics, noteheads, rest display position, instruments, editorial, play/listen, note print/playback attributes | Feature-specific child/attribute `DISCARDED` findings (S09/S11/S10/S12/S14/S17/S16/S25/S26/S04); the note and its exact duration are kept | Owning feature tasks |
| Key, clef, staves and other attributes children; complex or annotated time | S07 `DISCARDED`; a simple time keeps its signature even if its attributes are discarded | Key/clef/staves/staff details superseded by MX-08 above; complex time remains |
| Voice labels without backup | Labels retained; events stay sequential in one timeline | MX-09 |
| Measure fullness versus time signature | Not checked; extent is the observed end of timed content | `INCONSISTENT_MEASURE_DURATION` policy remains later work |

F01/F03 basic/F04 are implementation-authored and not XSD-validated; iOS runtime still requires macOS.

## MX-06 partwise skeleton decisions and remaining gaps

The public [partwise skeleton importer](partwise-import.md) now parses metadata and ordered
part/measure structures. This supersedes earlier statements that no semantic importer exists.

| Case | Implemented structured outcome / evidence | Remaining work |
|---|---|---|
| Missing/blank/duplicate declaration/body IDs, unknown references, declared parts without bodies | Fatal `INVALID_STRUCTURE` in both policies; no repair, score or provenance; F02 variants | Full XSD ID lexical/document-wide optional-ID validation remains deferred |
| Body order differs from part-list order; repeated measure numbers | Part-list defines neutral part order, body references retain the correct measures; generated part-local measure IDs and source indices disambiguate numbers | Cross-part temporal alignment and staff state remain MX-08/MX-09 |
| Missing/duplicate part-list, missing part-name/measure number, duplicate header fields | Fatal `INVALID_STRUCTURE`; actual missing-part-list F22 semantic detection now exists | Complete nested ordering/cardinality and XSD validation remain separate work |
| Zero-measure bodies | Accepted neutral empty parts; explicit test and documentation | Not an XSD-conformance claim |
| Invalid measure implicit flag or nonpositive/out-of-Int credit page | Fatal `INVALID_VALUE` | General schema value validation remains separate work |
| Timewise root through public score API | Superseded by MX-10: normalized to the partwise projection | None |
| Credit images/symbols, text typography/language, encoding supports and unknown header content | S03 `UNSUPPORTED_FEATURE` / `DISCARDED`; plain supported text still retained; strict rejects, tolerant warns | Rich credit/layout and encoding capability semantics remain unimplemented |
| Defaults/print/part-name displays; optional measure ID/text/width/non-controlling and other unmapped structural attributes | S17 or S01 discarded-content findings at subtree/attribute locations | Layout MX-18; source IDs/display text/multimetric measure semantics need explicit model mappings |
| Part groups, part links, grouping, links/bookmarks | S25 discarded-content findings; actual grouping/link importer tests; no targets fetched | Group/editorial/link semantics and full F27 remain pending |
| Notes, backup/forward, attributes, direction, barline, score-instrument, harmony/figured-bass, sound/listening/player/MIDI, opus | Feature-specific discarded-content findings (S04/S06/S07/S13/S15/S16/S22/S26/S27); one record covers a skipped subtree | Musical feature tasks must replace these branches; no event, instrument, timing or auxiliary import is claimed |
| Foreign subtrees or namespaced attributes resemble supported names | Expanded-name lookup prevents aliasing; `foreign-extension` subtree or owner-group attribute finding | No namespace rewriting or extension semantics |
| Work/identification/encoding source containers | Flattened ordered semantic text metadata; credits remain grouped; source sidecar retains original leaf locations | No source-container round-trip, empty-container preservation or formatting promise |
| F02 and implementation-authored variants | 18 common semantic tests on JVM/Android host; iOS cross-compiles | Independent MX-04 fixtures/XSD validation and macOS runtime evidence still required |

All new unsupported paths use the shared policy, diagnostics cap and source/provenance limits.
No opaque capture is introduced. See [target results and exact scope](partwise-import.md).

## MX-00 audit findings and diagnostic obligations

Evidence is in the pinned method links in [reference-analysis.md](reference-analysis.md); the [element inventory](element-inventory.md) and S22–S27 cover newly enumerated groups. **There is no importer yet.** MX-03 now supplies the [diagnostic API](diagnostics.md); the historical proposed outcomes below remain feature-owner obligations, not runtime handling. Source fixture references are planned. Unsupported content must not be silently accepted just because its parent element is recognized.

Proposed record shape: `UnsupportedFeature(featureId, sourceLocation, retention)`, with feature IDs from S01–S27, a location identifying the element/attribute path and available part/measure/line/column, and retention `discarded`, `opaque` or `converted`. For example, an unmapped harmony is `UnsupportedFeature("S22", path="/score-partwise/part[@id='P1']/measure[@number='1']/harmony", retention="discarded")`. This is illustrative field notation, not a Kotlin signature. `converted` requires an explicit description of the conversion/loss; opaque retention needs bounded size. Until a retention mechanism exists, report `discarded` honestly. Fatal root rejection returns failure with no score; unsupported feature handling is observable in tolerant mode and rejects under a strict unsupported-content policy. MX-03 must finalize stable code names, severity and limits.

| Case / evidence | Required structured outcome (proposed) | Owner / independent check |
|---|---|---|
| Timewise root is valid in 4.0 but rejected by both inspected music21 entry points | Normalized since MX-10, with no unsupported warning (`UNSUPPORTED_ROOT` remains only for other roots) | MX-05,MX-10; F12 pair tested. Source guards verified; music21 runtime not executed. |
| Missing/unknown/duplicate part ID is repaired, skipped or overwritten in reference source | `MissingPartId`, `UnknownPartId`, `DuplicatePartId`; any tolerant repair also records what was changed/lost | MX-06; F02/F22 variants |
| Backup before measure start is clamped in reference; forward overfill allowed locally | `InvalidCursorMove` / `InconsistentMeasureDuration`, not silent clamp or inferred notation | MX-09; F05/F23; also divisions-boundary moves |
| Numbered tuplet state uses fixed lists although 4.0 number-level permits 1–16 | Implemented in MX-12: labels 1–16 pair through the span resolver with no fixed-size state; 17 is fatal `INVALID_VALUE` | MX-12,MX-20; F11 numbers 7,8,16 and inline invalid 17 tested. Index failure is a source-based inference, not a reproduced crash. |
| Sound tie and notation-only tied mark collapse in the inspected tie helper | Implemented in MX-11: separate `SOUND_TIE`/`NOTATED_TIE`, orphans `UNMATCHED_SPAN` | MX-11; F10 notation-only and orphan XML cases; stop-before-start at resolver level until MX-09 |
| Harmony/figured bass and tablature/technical variants exceed initial parser scope; figured bass dispatch is explicitly skipped in reference | `UnsupportedFeature("S22" / "S23", location, retention)` for each unsupported subtree/attribute | MX-08,MX-13,MX-20,MX-21; F24,F25 |
| Beat/measure repeat and slash measure styles are TODOs in reference | `UnsupportedFeature("S24", location, retention)`; retain actual events even when style cannot be represented | MX-17,MX-21; F26 |
| Link/bookmark/grouping dispatch is skipped; editorial preservation needs explicit scope | `UnsupportedFeature("S25", location, retention)` for unmapped content; no network dereference | MX-03,MX-06,MX-18,MX-21; F27 |
| Listening lacks measure dispatch; delegated sound parser leaves swing/instrument-change and controls as TODOs | `UnsupportedFeature("S26", location, retention)`, including unmapped attributes under recognized sound/direction containers | MX-13,MX-16,MX-20,MX-21; F21,F28 |
| Opus/sound-definition roots are schema-defined but outside the current score input API | Fatal `UnsupportedRoot` with document kind/location; do not treat as malformed XML or return empty score | MX-05,MX-19,MX-21; F29 |

No new runtime unsupported branch was introduced in MX-00. MX-05 now emits fatal XML reader diagnostics;
semantic feature emission and independent source fixtures remain later work. MX-03 contract assertions
are recorded separately above. The historical audit did not settle XML/ZIP security or model/API choices.
