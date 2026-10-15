# Risks, gaps and decision log

Status: open design risks, 2026-10-13. Review against the [MusicXML 4.0 reference](https://www.w3.org/2021/06/musicxml40/musicxml-reference/) when implementing each feature. `spec-coverage.md` records support separately from tests.

| Risk | Mitigation and test |
|---|---|
| Public model loses format details | Keep written pitch, performed duration, notation and playback fields separate; log unsupported data with source location. Round-trip is not promised. Test accidentals, print hints and notes with different notated/performed values. |
| Timing drift with changing divisions, backup/forward and tuplets | Rational quarter-note offsets; measure-local cursor; explicit state timeline. Assert exact onset/duration across changes and overlapping voices. |
| Chord children can have different durations | Store noteheads independently at one onset; test heterogeneous chord durations against [4.0 clarification](https://www.w3.org/2021/06/musicxml40/version-history/40/). |
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

Open questions to close in MX-01/MX-02: precise parser input API across all targets; secure entity handling in the chosen xmlutil mode; ZIP library with iOS support; whether renderer adoption uses migration or an adapter; exact unsupported-data retention budget. Decisions should be recorded here with test evidence.
