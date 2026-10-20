# Diagnostics and provenance contract (MX-03)

Issue [#4](https://github.com/k4pran/LunarScore/issues/4) adds public common Kotlin types in
`com.ryanjames.sheetmusic.musicxml`, in `:musicxml-parser`. It exposes `:sheetmusic-model` through
an API dependency, without adding source/XML concerns to the neutral model. `ParseResult<Score>`
is the intended score result. There is **no parse entry point or semantic XML reader yet**.
MX-01's internal dependency probes keep their original API and tests; MX-05 will connect the
reader to this contract. This change does not claim XSD validation or new MusicXML feature support.
The [4.0 XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/) defines valid source
structure; schema validity and supported musical interpretation remain separate questions.

## Result and policy

`ParseResult.Success<T>` contains an accepted non-null value, ordered warning diagnostics,
source provenance and the `ParseOptions` used. `ParseResult.Failure` always contains an error,
has a null value and no model provenance. Partial scores are never exposed as successful results.
Diagnostics (including opaque unsupported fragments already collected) survive failure. Result
lists are snapshots; later collection does not mutate a returned result.

`ParseOptions()` defaults to `STRICT`. `TOLERANT` must be selected explicitly. Both modes reject
syntax, required-structure/value, encoding, unsafe XML, unsupported root/namespace, numeric,
cancellation and resource failures. They never approximate an overflowing rational or return a
truncated score. Failure codes and their meaning are stable; message prose is not a machine API.

| Codes | Strict | Tolerant | Adapter obligation |
|---|---|---|---|
| `MALFORMED_XML`, `UNSAFE_XML`, `UNSUPPORTED_ENCODING`, `UNSUPPORTED_ROOT`, `UNSUPPORTED_NAMESPACE` | Error/failure, stop | Error/failure, stop | Supply available coordinates; do not invent line/column before reader creation |
| `INVALID_STRUCTURE`, `INVALID_VALUE`, `NUMERIC_OVERFLOW` | Error/failure, stop | Error/failure, stop | Missing required structure/references or uninterpretable required values cannot yield an accepted score |
| `UNMATCHED_SPAN`, `INVALID_CURSOR_MOVE`, `INCONSISTENT_MEASURE_DURATION` | Error/failure | Warning/accepted candidate | Preserve unresolved facts where representable; describe any repair/discard explicitly; do not silently clamp a cursor |
| `UNSUPPORTED_FEATURE` | Error/failure | Warning/accepted candidate | Include coverage feature ID, location and explicit retention outcome for each meaningful unmapped subtree/attribute |
| `RECOVERED_VALUE` | Error/failure | Warning/accepted candidate | Message describes the actual repair; use unsupported-content records as well when meaningful source material is lost |
| `INPUT_LIMIT`, `DEPTH_LIMIT`, `ELEMENT_LIMIT`, `DIAGNOSTIC_LIMIT`, `RETENTION_LIMIT`, `PROVENANCE_LIMIT` | Error/failure, stop | Error/failure, stop | Stop consumption; never hide a limit behind success |
| `CANCELLED`, `ARCHIVE_READ_FAILED`, `ARCHIVE_ENTRY_LIMIT` | Error/failure, stop | Error/failure, stop | Cancellation and detailed archive detection remain reader/archive adapter work |

Semantic findings may be collected after strict rejection to provide useful ordered diagnostics;
fatal findings stop collection. A tolerant candidate is accepted only if it contains no error.
Success does not certify full MusicXML coverage, XSD validity, renderability or absence of loss:
consumers must inspect diagnostics and retention records. No entry point is exposed that accepts
input while omitting these diagnostics.

## Source and loss

`SourceLocation` holds optional source/entry name, absolute descriptive element/attribute path,
one-based line/column, part ID, printed measure number and zero-based part-local measure index.
Use sibling indices in paths to distinguish repeated elements. Unknown coordinates stay null;
a root/decoding failure need not have an element path. A path is descriptive data, never executed
as XPath. Locations are values, so diagnostics remain usable after reader/source cleanup.

`SourceProvenance(modelPath, location)` maps neutral paths (for example
`parts[0].measures[1].events[2]`) back to source without changing the neutral model. It can also
address parts, measures, attributes or relationships, and multiple entries may describe the same
object. Failure drops these model mappings because no accepted value exists. Source names and
link attributes never authorize network access.

`UNSUPPORTED_FEATURE` requires `UnsupportedContent(featureId, retention, sourceXml, conversion)`:

| Retention | Required payload | Meaning |
|---|---|---|
| `DISCARDED` | No fragment or conversion text | Meaningful material was dropped explicitly |
| `OPAQUE` | Nonblank `sourceXml` | A bounded source fragment was retained, without semantic understanding or round-trip guarantees |
| `CONVERTED` | Nonblank `conversion` | Description of the mapping and what changed or was lost |

Use S01–S27 from the coverage ledger; an unknown extension can use a descriptive identifier such
as `extension:vendor:element`. A recognized parent never permits silently ignoring its children
or attributes. Opaque or converted content still rejects in strict mode. Unknown-to-the-adapter
but schema-valid `grouping` (S25) is the contract test example; there is no grouping parser yet.

## Limits and collection

| Budget | Default | Current enforcement |
|---|---|---|
| Input bytes | 8,388,608 | Public configuration; MX-05 reader must enforce before materialization |
| XML depth | 128 | Public configuration; MX-05 reader must enforce while consuming |
| XML element count | 100,000 | Public configuration; MX-05 reader must enforce while consuming |
| Diagnostics | 1,000 | Internal shared `ParseContext` |
| Opaque retained source UTF-8 bytes | 65,536 total | Internal shared `ParseContext`; zero disables opaque retention |
| Provenance entries | 100,000 | Internal shared `ParseContext`; zero prohibits mappings |

All limits are finite; XML and diagnostic limits must be positive. Retention/provenance may be
zero. Exactly reaching a budget is allowed. At the first diagnostic beyond the cap, replace the
last stored finding with fatal `DIAGNOSTIC_LIMIT`, located at the overflowing finding. The marker
counts toward the hard cap (including cap=1); it explains that the replaced and later findings
are omitted. Keep earlier findings in encounter order and stop. This avoids both an unbounded
diagnostic list and false success when omitted findings include errors.

Opaque retention counts cumulative UTF-8 bytes, including multibyte text. Check UTF-16 length
before encoding to bound the temporary allocation, then check encoded bytes. Overflow returns
fatal `RETENTION_LIMIT`, rather than silently dropping the fragment. The future reader must also
bound capture before constructing a fragment; this collector alone is not a streaming XML guard.
Provenance overflow is fatal `PROVENANCE_LIMIT`. Returned snapshots stay within their budgets.
The collector is internal so public callers cannot manufacture accepted parse results directly.

Archive count/ratio/global expansion budgets remain MX-19 work. Wall-clock deadlines and actual
cancellation hooks remain MX-05/MX-19 input-adapter work; `CANCELLED` reserves their failure outcome.
The old dependency probes do not automatically inherit these public option defaults.

## Verification scope

`ParseContractTest` supplies adapter findings directly, using original Kotlin expectations. Its
F22 cases distinguish wrong root, missing part-list, illegal duration and malformed syntax. F23
cases cover orphan spans, invalid cursor movement and inconsistent measure duration. Other checks
cover a valid unmapped grouping, all retention outcomes, strict/tolerant behavior, fatal-after-warning
ordering, null failure value/provenance, source-coordinate invariants, result snapshots, diagnostic
caps 1/2/3, cumulative UTF-8 retention and zero/exact/excess provenance budgets.

These are **contract-level F22/F23 projections**, not XML fixtures, parser classification tests or
XSD assertions. Independent source files remain MX-04; actual reader/semantic emission remains
MX-05 and the feature tasks.

Verified on Windows on 2026-10-15 using the Gradle wrapper:

| Check | Result |
|---|---|
| JVM and Android host tests | Passed: 15 contract tests and 13 existing XML/ZIP probe tests per target |
| Parser Android, JVM, common metadata and iOS main klibs | Compiled; parser module build passed |
| iosArm64 and iosSimulatorArm64 test klibs | Compiled |
| iOS linking and runtime tests | Skipped on Windows; require macOS/Xcode |
| Parser JS/Wasm | Not declared by this module; deferred since MX-01 |

Command: `./gradlew :musicxml-parser:build :musicxml-parser:allTests
:musicxml-parser:compileTestKotlinIosArm64 :musicxml-parser:compileTestKotlinIosSimulatorArm64`.
The existing Compose Android-host-test configuration warning remains unrelated to this change.
No dependency version, npm package or web lockfile changed.
