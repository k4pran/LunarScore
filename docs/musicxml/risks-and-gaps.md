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
| Orphan or forward relationship endpoints | Retained IDs/nulls; `MISSING_ENDPOINT` / `UNKNOWN_EVENT` model diagnostics; F10 projection | MX-11 source resolution, tie pitch checks, locations and strict/tolerant policy |
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
| Byte/depth/element excess; cancelled parse | Fatal `INPUT_LIMIT` / `DEPTH_LIMIT` / `ELEMENT_LIMIT` / `CANCELLED`; exact/excess, deep/wide and tail-drain tests | Caller bytes are already materialized; cancellation is cooperative between bounded scans/tokens; deadlines deferred |
| Long nested names or namespace URIs amplify parent-prefixed source paths | Fatal `PATH_LIMIT` before exceeding the cumulative 8,388,608 UTF-16 code-unit budget; exact/excess/root/sibling/Unicode and default-budget adversarial tests | Larger legitimate scores can configure `maxSourcePathCharacters`; frames are not allowed to bypass the cumulative budget when closed |
| Many tiny attributes amplify XML token and snapshot allocations | Fatal `ATTRIBUTE_LIMIT` during preflight, before reader creation; defaults 256 per element/declaration and 100,000 total, including namespace/XML declaration fields; wide-root, many-small-tag, exact/excess and literal-text regressions | Legitimate extensions can configure both budgets explicitly; XML byte, depth, element and path bounds still apply |
| Fatal reader finding after consumer warnings/provenance | Ordered diagnostics, null value and empty model mappings; diagnostic-cap and malformed-tail tests | Semantic adapters must continue using shared context |
| F22 missing part-list / illegal duration | Well-formed XML reaches later adapters; explicit reader test | MX-06/MX-07 semantic detection and MX-04 XSD assertions still required |

JVM/Android host tests and iOS cross-compilation are recorded in [xml-reader.md](xml-reader.md).
Native runtime still requires macOS/Xcode. Opaque capture must bound fragments before allocation;
MXL safety remains MX-19 and JS/Wasm targets remain undeclared for this module.

## MX-00 audit findings and diagnostic obligations

Evidence is in the pinned method links in [reference-analysis.md](reference-analysis.md); the [element inventory](element-inventory.md) and S22–S27 cover newly enumerated groups. **There is no importer yet.** MX-03 now supplies the [diagnostic API](diagnostics.md); the historical proposed outcomes below remain feature-owner obligations, not runtime handling. Source fixture references are planned. Unsupported content must not be silently accepted just because its parent element is recognized.

Proposed record shape: `UnsupportedFeature(featureId, sourceLocation, retention)`, with feature IDs from S01–S27, a location identifying the element/attribute path and available part/measure/line/column, and retention `discarded`, `opaque` or `converted`. For example, an unmapped harmony is `UnsupportedFeature("S22", path="/score-partwise/part[@id='P1']/measure[@number='1']/harmony", retention="discarded")`. This is illustrative field notation, not a Kotlin signature. `converted` requires an explicit description of the conversion/loss; opaque retention needs bounded size. Until a retention mechanism exists, report `discarded` honestly. Fatal root rejection returns failure with no score; unsupported feature handling is observable in tolerant mode and rejects under a strict unsupported-content policy. MX-03 must finalize stable code names, severity and limits.

| Case / evidence | Required structured outcome (proposed) | Owner / independent check |
|---|---|---|
| Timewise root is valid in 4.0 but rejected by both inspected music21 entry points | `UnsupportedRoot(rootName, sourceLocation)` until MX-10; thereafter normalize, not an unsupported warning | MX-05,MX-10; F12 pair. Source guards verified; music21 runtime not executed. |
| Missing/unknown/duplicate part ID is repaired, skipped or overwritten in reference source | `MissingPartId`, `UnknownPartId`, `DuplicatePartId`; any tolerant repair also records what was changed/lost | MX-06; F02/F22 variants |
| Backup before measure start is clamped in reference; forward overfill allowed locally | `InvalidCursorMove` / `InconsistentMeasureDuration`, not silent clamp or inferred notation | MX-09; F05/F23; also divisions-boundary moves |
| Numbered tuplet state uses fixed lists although 4.0 number-level permits 1–16 | Support valid labels through 16; if temporarily limited, `UnsupportedFeature("S11", location, retention)`. Never an indexing exception | MX-12,MX-20; F11 numbers 7,8,16 and invalid 17. Index failure is a source-based inference, not a reproduced crash. |
| Sound tie and notation-only tied mark collapse in the inspected tie helper | Preserve both; until notation-only support, `UnsupportedFeature("S09", location, retention)`; orphan relationships use `UnmatchedSpan` | MX-11; F10 notation-only, stop-before-start and orphan cases |
| Harmony/figured bass and tablature/technical variants exceed initial parser scope; figured bass dispatch is explicitly skipped in reference | `UnsupportedFeature("S22" / "S23", location, retention)` for each unsupported subtree/attribute | MX-08,MX-13,MX-20,MX-21; F24,F25 |
| Beat/measure repeat and slash measure styles are TODOs in reference | `UnsupportedFeature("S24", location, retention)`; retain actual events even when style cannot be represented | MX-17,MX-21; F26 |
| Link/bookmark/grouping dispatch is skipped; editorial preservation needs explicit scope | `UnsupportedFeature("S25", location, retention)` for unmapped content; no network dereference | MX-03,MX-06,MX-18,MX-21; F27 |
| Listening lacks measure dispatch; delegated sound parser leaves swing/instrument-change and controls as TODOs | `UnsupportedFeature("S26", location, retention)`, including unmapped attributes under recognized sound/direction containers | MX-13,MX-16,MX-20,MX-21; F21,F28 |
| Opus/sound-definition roots are schema-defined but outside the current score input API | Fatal `UnsupportedRoot` with document kind/location; do not treat as malformed XML or return empty score | MX-05,MX-19,MX-21; F29 |

No new runtime unsupported branch was introduced in MX-00. MX-05 now emits fatal XML reader diagnostics;
semantic feature emission and independent source fixtures remain later work. MX-03 contract assertions
are recorded separately above. The historical audit did not settle XML/ZIP security or model/API choices.
