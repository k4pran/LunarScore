# Partwise score skeleton (MX-06)

Issue [#7](https://github.com/k4pran/LunarScore/issues/7) adds the first public semantic entry point
in `:musicxml-parser`. It builds on MX-05's [secure ordered reader](xml-reader.md) and shares its
diagnostic collector, resource limits, cancellation, source coordinates and final tail validation.

```kotlin
val result = parseMusicXml(
    input = xmlBytes,
    sourceName = "score.musicxml",
    options = ParseOptions(), // strict by default
)
when (result) {
    is ParseResult.Success -> useScore(result.value)
    is ParseResult.Failure -> showDiagnostics(result.diagnostics)
}
```

This entry point currently imports a **skeleton**, not musical events. Strict parsing accepts
the implemented header/part/empty-measure subset; notes and other unmapped musical material
produce errors. Explicit tolerant parsing returns a skeleton with loss warnings. Neither outcome
implies full MusicXML coverage, XSD validation or a renderable/playable score.

## Structure and identities

- An unnamespaced `score-partwise` root is required. The reader can recognize `score-timewise`,
  but this semantic entry point rejects it with fatal `UNSUPPORTED_ROOT` until MX-10.
- Exactly one nonempty `part-list` must precede the part bodies. Each `score-part` requires a
  nonblank, unique `id` and a `part-name` (empty text is allowed). The corresponding body must
  exist exactly once. Missing/blank/whitespace-bearing IDs, duplicate declarations or bodies,
  unknown body references and declarations without bodies produce fatal `INVALID_STRUCTURE`
  under **both** policies. No IDs are invented and no parts are merged or silently removed.
- XML whitespace surrounding a part ID is normalized before lookup. Complete XSD ID lexical
  validation and document-wide checking of optional IDs are not implemented.
- Neutral parts follow **part-list order**, even if the body order differs. Names, abbreviations
  and part identification metadata are matched by ID. Measures follow each body's encounter order.
- A measure requires `number`; nonnumeric/repeated numbers remain distinct. `implicit` accepts
  `yes`/`no`, defaulting to false; other values produce fatal `INVALID_VALUE`. Stable neutral
  measure IDs are generated as `measure-1`, `measure-2`, etc. within each part, independently of
  source/display numbers. Optional source `id` and measure `text` are currently unmapped attributes.
- Empty measures and zero-measure part bodies are accepted by this skeleton API. An absent or
  empty part-list is rejected. Zero-measure bodies are an intentional neutral-model convenience,
  not a claim of schema conformance. Events/staff definitions remain empty and extent is zero;
  MX-07/MX-08/MX-09 will supply musical interpretation and alignment.

Recognized top-level header elements must appear in specification order, with singleton fields
checked for duplicates. Selected nested singleton fields are also checked. Unexpected structural
text is fatal. This is deliberately not a complete schema validator: nested content ordering,
dates, all ID lexical rules, encoding declarations such as `supports`, and optional attribute
value constraints remain outside this slice. Root `version` is recognized as an advisory marker;
it does not enable a different grammar or certify conformance to that version.

## Metadata and source mappings

The format-neutral model adds ordered `Score.metadata`, `Part.metadata` and `Part.abbreviation`.
`MetadataEntry.Text` carries a semantic `MetadataKind`, exact decoded text, and an optional
qualifier. It retains:

- Work number/title and movement number/title.
- Identification creators/rights (including their `type`), source, repeated relations, and named
  miscellaneous fields.
- Encoding date, encoder/type, software and description in encounter order, including repeats.
- Visible credits as `MetadataEntry.Credit`: ordered credit types and word runs, page default 1.
  Page values must fit a positive `Int`; invalid/out-of-range values produce `INVALID_VALUE`.

Work/identification/encoding containers are flattened into semantic fields, preserving leaf order.
Credits retain their grouping and position among score metadata. Empty strings and text whitespace
are preserved. CDATA/entity decoding follows the reader. `Score.title` selects a nonblank movement
title, falling back to a nonblank work title; both original fields remain in metadata.

The provenance sidecar maps metadata/credit runs, part declarations/names/abbreviations/bodies,
and measures to neutral paths. Part declaration metadata also receives its declared part ID.
Mappings use the **final** part-list indices when bodies arrive out of order. Source measure
indices distinguish repeated numbers. Fatal errors clear the value and all model mappings.
No source/XML dependency is introduced into `sheetmusic-model`.

## Explicit unsupported outcomes

Each unhandled subtree produces one `UNSUPPORTED_FEATURE` record covering that subtree; each
unmapped attribute of a handled element produces its own record. Retention is `DISCARDED`, with
source location and a feature ID. Strict mode rejects; tolerant mode warns. No opaque XML is
captured, and links/images/opus references are never fetched.

| Unmapped content | Feature ID |
|---|---|
| Notes/rests; backup/forward; attributes | S04; S06; S07 |
| Directions; barlines; score-instrument | S13; S15; S16 |
| Defaults/print and part-name/abbreviation display formatting | S17 |
| Harmony/figured-bass | S22 |
| Grouping, links/bookmarks, part-link, group, part-group | S25 |
| Sound/listening/player/MIDI declarations | S26 |
| Work opus references | S27 |
| Credit images/symbols, encoding supports, remaining header extensions/attributes | S03 |
| Unmapped structural root/part-list/part/measure attributes | S01 |
| Foreign namespace subtrees | `foreign-extension` |

Foreign expanded names never alias MusicXML names. Unmapped attributes on metadata text (including
typography or language) are reported as S03, even when the plain text is retained. A diagnostic
for a skipped parent covers its children; it does not assert independent child feature support.

## Fixtures and verification

`PartwiseFixtures.kt` supplies an original F02 XML fixture with two parts, reversed body order,
work/movement metadata, repeated creators/rights/software/relations, credits, part identification,
pickup flags and repeated/nonnumeric measure numbers. It is authored for this repository from
the [MusicXML 4.0 structure reference](https://www.w3.org/2021/06/musicxml40/tutorial/structure-of-musicxml-files/).
`MusicXmlParserTest` adds 18 tests for explicit semantic expectations, empty structures,
ID/reference/required-field failures under both policies, source mappings, explicit loss outcomes,
namespaces, malformed tails, security, cancellation and diagnostic/provenance budgets.
These implementation-authored fixtures are not the independent MX-04 corpus; XSD validation
has **not** been run on F02 or its variants.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 79 passed per target (18 skeleton, 33 reader, 15 contract, 13 probes) |
| Parser build, common metadata, Android AAR, JVM jar | Passed |
| Parser iosArm64 / iosSimulatorArm64 main and test klibs | Cross-compiled |
| Neutral model / core regression tests | Passed on JVM, Android host, JS Node and Wasm Node |
| iOS runtime/linking | Skipped on Windows; requires macOS/Xcode |
| Browser tests | Disabled by existing model/core configuration; parser has no JS/Wasm targets |

Commands use the Gradle wrapper:

```text
./gradlew :musicxml-parser:build :musicxml-parser:allTests
  :musicxml-parser:compileTestKotlinIosArm64 :musicxml-parser:compileTestKotlinIosSimulatorArm64
  :sheetmusic-model:allTests :sheetmusic-core:allTests
```

No dependency versions or web lockfiles changed. Existing Compose Android host-test and npm
configuration-time resolution warnings are unrelated to this slice.
