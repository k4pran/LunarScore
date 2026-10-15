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

The XML/ZIP dependency decisions and neutral model contract are recorded below. Public parser
input policy and unsupported-data retention still need MX-03/MX-05 decisions; the raw ZIP safety
layer remains MX-19 work. MX-02 chooses a staged renderer adapter.

## MX-01 dependency decisions and runtime limitations

MX-01 closes the initial dependency/API investigation; see [dependency evidence](dependency-spike.md).
The renderer migration is recorded under MX-02 below; the unsupported-data retention budget
remains an MX-03 decision.

These are actual internal spike outcomes; they do not finalize the public MX-03 diagnostic contract.
Every failing probe returns one typed `ProbeDiagnostic` and clears partial output.

| Case | Current structured outcome / evidence | Remaining work |
|---|---|---|
| DTD, external/internal entity declarations | `UNSAFE_XML` before reader creation; network/file/parameter/expansion smoke tests pass | MX-05: decide whether to accept safe MusicXML DTD headers without resolution |
| Literal declaration token in comments/CDATA | Conservative `UNSAFE_XML`; comment regression test | MX-05: refine lexical policy if needed |
| Undeclared entity or malformed XML | `MALFORMED_XML`, no partial events | MX-03/MX-05: source locations and stable public error codes |
| Non-UTF-8/invalid UTF-8/NUL-bearing input | `UNSUPPORTED_ENCODING` | MX-05: encoding detection and bounded streaming policy |
| Namespace-bearing score root | `UNSUPPORTED_NAMESPACE`; prefixed/default namespace tests | MX-05: finalize compatibility policy; foreign child namespaces are event evidence only |
| Auxiliary/unknown root | `UNSUPPORTED_ROOT`; opus/sounds tests | MX-05/MX-19: document-kind handling; no automatic reference fetching |
| XML byte/depth/element bound | `INPUT_LIMIT` / `DEPTH_LIMIT` / `ELEMENT_LIMIT`; boundary tests | MX-03/MX-05: cancellation/time/retention policy |
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

## MX-00 audit findings and diagnostic obligations

Evidence is in the pinned method links in [reference-analysis.md](reference-analysis.md); the [element inventory](element-inventory.md) and S22–S27 cover newly enumerated groups. **There is no importer or diagnostic API yet.** The records below specify future outcomes for MX-03 and feature owners; they do not claim runtime handling. All fixture references are planned. Unsupported content must not be silently accepted just because its parent element is recognized.

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

No new runtime unsupported branch was introduced in MX-00. Diagnostic emission and assertions remain unimplemented and must land with the parser; the separate implementation/test statuses in the matrix stay unchanged. The audit does not settle XML/ZIP security or model/API choices reserved for MX-01/MX-02.
