# Secure ordered XML reader (MX-05)

Issue [#6](https://github.com/k4pran/LunarScore/issues/6) implements the internal
`readScoreXml(ByteArray, ParseOptions, sourceName, isCancelled, consume)` boundary and
`ScoreXmlReader` in common Kotlin. It uses the pinned xmlutil 1.0.2 generic reader on all targets;
XML types remain inside `:musicxml-parser`. This is infrastructure for MX-06/MX-07, with no
public score import entry point or XSD/semantic validation claim.

## Cursor and finalization

The consumer begins with `current` on the root start event and a `PARTWISE` or `TIMEWISE` root
kind. `next()` yields start, text and end snapshots in document order. Empty elements have both
start and end events; whitespace and CDATA text are retained without trimming or reordering.
Comments and processing instructions are ignored without following links or executing them.
Expanded element/attribute names preserve namespace URI and local name. Default namespaces do
not apply to unqualified attributes. Foreign children reach semantic adapters as distinct names,
never as same-named MusicXML elements; adapters must diagnose meaningful unsupported content.
The wrapper explicitly rejects empty URI bindings for nonempty prefixes with `MALFORMED_XML`,
including unused declarations and nested rebinding. Empty default namespace resets (`xmlns=""`)
remain valid. These rules follow [Namespaces in XML 1.0](https://www.w3.org/TR/REC-xml-names/#nsc-NoPrefixUndecl).

No document-sized event list or DOM is built. The cursor stores the open-element stack and
sibling counters, bounded by depth/element budgets. Each snapshot survives advancement. Paths
use indexed expanded names, for example `/score-partwise[1]/part[1]/measure[2]/note[1]` or
`/score-partwise[1]/{urn:extension}note[1]`. Prefix aliases share a sibling counter. Start/text/end
locations include source name and available one-based reader coordinates, plus lexical part ID,
printed measure number and zero-based measure ordinal. Timewise children inherit the enclosing
measure coordinates. These locations are not neutral model provenance until a semantic adapter
adds a mapping through the shared `ParseContext`.

Full paths have a separate cumulative `maxSourcePathCharacters` budget (default 8,388,608 UTF-16
code units, at most 16 MiB of character payload on UTF-16-backed targets). Before concatenation,
the cursor checks the full parent prefix, expanded namespace/local name, separators and sibling
index using overflow-safe arithmetic. Every newly allocated path counts, including paths for
siblings whose frames have already closed; text/end events reuse their frame's path. Overflow
reports `PATH_LIMIT` at the nearest existing parent location without constructing the rejected
path. This bounds retained path payload even for long nested names or reused namespace URIs.

Attributes are bounded separately by `maxAttributesPerElement` (default 256) and
`maxTotalAttributes` (default 100,000). Preflight counts unquoted assignment operators in markup
before reader creation, including namespace declarations and XML declaration fields. Counts do
not include quoted `=`, comments, CDATA, other processing instructions or ordinary text.
Both budgets are positive and accept exact limits; the total never resets when tags close.
The cursor also checks the per-element count before allocating snapshots and name sets.
Excess yields fatal `ATTRIBUTE_LIMIT` under both policies; preflight locations have the source
name but no fabricated reader coordinates. Because preflight covers the whole input, a late
oversized tag rejects before the consumer runs or any dependency attribute token is materialized.

After the consumer returns, the boundary drains the unread tail through the same guards. Only
complete, accepted input produces `ParseResult.Success<T>`. Fatal syntax/security/resource
findings stop consumption, discard the consumer value and all model provenance, and preserve
ordered diagnostics. Both policies treat these findings as errors. A `finally` closes the reader;
consumer exceptions propagate unchanged rather than becoming XML diagnostics.

## Safety and deliberate unsupported cases

| Case | Structured outcome / handling |
|---|---|
| Input exceeds `maxInputBytes` | Fatal `INPUT_LIMIT` before decoding/reader creation; equality is allowed |
| Non-UTF-8 declaration, malformed UTF-8, UTF-16/32 bytes, NUL-bearing input | Fatal `UNSUPPORTED_ENCODING`; UTF-8 with optional BOM is accepted |
| XML version other than 1.0 | Fatal `UNSUPPORTED_XML_VERSION`; 1.1 is deliberately unsupported |
| Standard MusicXML DTD header (MX-21) | The exact Recordare `PUBLIC` partwise/timewise header for any version, first in the document after an optional XML declaration and without an internal subset, is blanked (line breaks kept) before reader creation and never resolved; see [compliance audit](compliance-audit.md) |
| Any other DOCTYPE or ENTITY declaration | Fatal `UNSAFE_XML` before reader creation, including internal subsets, `SYSTEM` or other hosts, mismatched flavours and a second header; no resolver or untrusted entity table, so entity references still fail as `MALFORMED_XML` |
| Literal `<!DOCTYPE` or `<!ENTITY` in comments, CDATA or processing instructions | Conservative fatal `UNSAFE_XML`; this policy remains intentional |
| Predefined escapes and valid numeric references | Decoded in text/attributes; references in comments/CDATA/PIs remain literal |
| Invalid XML 1.0 scalars or numeric-reference overflow | Fatal `MALFORMED_XML` before passing them to the dependency |
| Unknown entity, truncated/mismatched XML, multiple/missing roots, duplicate attributes/namespaces, invalid reserved namespace bindings | Fatal `MALFORMED_XML` |
| Roots other than unqualified `score-partwise` / `score-timewise` | Fatal `UNSUPPORTED_ROOT`; no opus/sounds import or linked resource retrieval |
| Namespace-bearing score root | Fatal `UNSUPPORTED_NAMESPACE`; no compatibility namespace rewriting |
| Depth or start-element count exceeds budget | Fatal `DEPTH_LIMIT` / `ELEMENT_LIMIT` before producing an event snapshot; exact limits accepted |
| Cumulative source paths exceed `maxSourcePathCharacters` | Fatal `PATH_LIMIT` before concatenation; exact budget accepted; parent path reused for the failure location |
| Per-element/declaration or cumulative attribute count exceeds its budget | Fatal `ATTRIBUTE_LIMIT` in preflight before xmlutil reads the input; includes namespace declarations and XML declaration fields |
| Cancellation callback returns true | Fatal `CANCELLED` before decoding, during preflight or between reader events, including tail draining |

Bytes are already materialized by the caller. The byte guard bounds subsequent decoding and
token allocation, including large text/attributes. Bounded stream acquisition and wall-clock
deadlines remain input-adapter work; cancellation is cooperative and cannot interrupt a single
dependency token read or string scan. Opaque fragment capture remains future semantic work and
must enforce retention budgets before allocation. MXL remains MX-19. JS/Wasm are not declared by
this parser module; iOS runtime verification requires macOS/Xcode.

The wrapper compensates for observed xmlutil 1.0.2 behavior: some truncated text raises
`IllegalStateException`, XML characters/references need extra validation, and duplicate/reserved
namespace declarations and expanded attribute names need explicit checks. A bounded lexical
preflight also rejects missing attribute separators, literal `<` in attribute values, `]]>` in
ordinary character data and invalid comment delimiters. It supplements the pull parser rather
than implementing a second XML grammar. See [the regression note](../SOLUTIONS.md).

## Verification

`ScoreXmlReaderTest` contains original inline XML fixtures and expectations for both roots,
mixed content/empty elements, namespace expansion and scope, source coordinates/repeated measure
numbers, both policies, DTD/file/network/parameter/expansion attacks, malformed/encoding/character
cases, exact/excess byte/depth/element/path budgets, deep/wide input, cancellation, final tail draining,
warning/error ordering, diagnostic caps and consumer exceptions.
Path regressions cover long nested names/namespaces under all other default limits, UTF-16
expanded-name accounting, multi-digit sibling indices, root overflow and diagnostic caps.
Attribute regressions cover a wide root, many small tags, exact/excess per-element and cumulative
limits, namespace and XML declaration fields, literal/quoted equals, and rejection before consumer
invocation and dependency entity handling. The wide-root regression failed before the guard.
Empty namespace URI regressions cover both roots, child elements, attributes, unused declarations
and nested rebinding under both policies; positive fixtures verify default namespace reset and
scope restoration. These already passed with xmlutil 1.0.2 before the explicit wrapper check;
the review's claimed input-acceptance bypass was not reproduced.

F22 wrong-root and malformed-syntax variants now exercise the actual reader. Missing part-list
and illegal duration remain well-formed at this layer: a test records that semantic/XSD detection
is still pending. The independent MX-04 corpus and offline XSD harness are separate work.

Windows verification on 2026-10-22:

| Target/check | Result |
|---|---|
| JVM / Android host tests | 61 tests passed per target: 33 reader, 15 contract, 13 dependency probes |
| Parser build / common metadata / Android AAR / JVM jar | Passed |
| iosArm64 / iosSimulatorArm64 main and test klibs | Cross-compiled |
| iOS linking/runtime | Skipped on Windows; macOS/Xcode required |

Command: `./gradlew :musicxml-parser:build :musicxml-parser:allTests
:musicxml-parser:compileTestKotlinIosArm64 :musicxml-parser:compileTestKotlinIosSimulatorArm64`.
No dependency versions or web lockfiles changed. The existing Compose Android host-test warning
is unrelated to the parser.
