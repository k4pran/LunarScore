# Solutions

## Bounded XML parsing needs guards around the generic reader (issue #6)

**Symptom**: Malformed tails could escape as state exceptions; invalid XML characters and duplicate
namespace declarations could be accepted by the XML dependency.

**Cause**: xmlutil 1.0.2's generic reader does not enforce every lexical/namespace constraint and
reports some unexpected EOF paths through buffer state checks. Returning after a root/header
also leaves malformed trailing input unchecked.

**Fix**: Use the common secure reader boundary: bounded UTF-8 preflight, declaration rejection,
character/reference and namespace/attribute guards, localized dependency exception translation,
and full tail draining before finalizing a result. Keep consumer exceptions outside the XML
exception boundary. `ScoreXmlReaderTest` records the regressions; see
[reader policy and verification](musicxml/xml-reader.md) and the pinned
[upstream reader](https://github.com/pdvrieze/xmlutil/blob/6c42903bcb981d0e2047be757ffdcde6778ac756/core/base/src/commonMain/kotlin/nl/adaptivity/xmlutil/core/KtXmlReader.kt).

## Nested source paths amplify bounded XML input (PR #26)

**Symptom**: Long nested element names or reused namespace URIs could retain hundreds of MiB
of complete paths while remaining inside the input byte, depth and element limits.

**Cause**: Every frame retained a full parent-prefixed path. Repeated ancestor text accumulated
quadratically with nesting depth; limiting source bytes did not bound these derived strings.

**Fix**: Check a separate cumulative `maxSourcePathCharacters` budget using `Long` arithmetic
before allocating each full path. Count expanded names, separators and sibling indices, never
refund closed frames, and report fatal `PATH_LIMIT` using an already allocated parent path.
Regression tests cover default-budget long names/namespaces, exact limits, Unicode and draining.

## Small XML attributes amplify token and snapshot allocations (PR #26)

**Symptom**: A root with thousands of tiny attributes remained under the byte limit and reached
the consumer, allowing large dependency token buffers and per-attribute objects/sets/lists.

**Cause**: Depth, element and path limits did not bound attribute count; checking after xmlutil
read the token would still leave the dependency's own attribute materialization unbounded.

**Fix**: Enforce `maxAttributesPerElement` and `maxTotalAttributes` in lexical preflight before
reader creation, counting namespace declarations and XML declaration fields. Skip literal text
and quoted equals. Report fatal `ATTRIBUTE_LIMIT` before the consumer runs, and repeat the
per-element check before snapshot allocation. The wide-root regression failed before the fix;
boundary, namespace/declaration and pre-reader entity-order regressions now cover the guard.

## Exporter MusicXML was rejected as unsafe XML (issue #22)

**Symptom**: Every file from the public LilyPond MusicXML test suite failed with `UNSAFE_XML`.

**Cause**: Exporters write the standard `<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML
x.y Partwise//EN" ...>` header, and the reader rejected any DOCTYPE to keep DTDs away from xmlutil.

**Fix**: Blank only the exact Recordare header, first in the document and without an internal
subset, before the reader; the DTD is never resolved and line numbers are kept. Every other DOCTYPE
is still `UNSAFE_XML`. `ScoreXmlReaderTest` covers near-standard variants; see
[the compliance audit](musicxml/compliance-audit.md).

## Model constructors threw on no-break-space voices and IDs (issue #22)

**Symptom**: A voice of only a no-break space (U+00A0) or a part ID containing U+00A0 threw `IllegalArgumentException`
out of `parseMusicXml`.

**Cause**: The parser trimmed and checked only XML whitespace, while the model rejects any Unicode
blank.

**Fix**: Check with `isBlank()`/`isWhitespace()` so the parser reports `INVALID_VALUE` /
`INVALID_STRUCTURE` first. The adversarial mutants in `ComplianceAuditTest` found this.

## Stacked pull requests merged into a stale parent never reach master (issues #10, #15, #20)

**Symptom**: PRs for MX-09, MX-14 and MX-19 showed as merged and their issues looked done, but
`master` had none of their code, and later features were built around its absence.

**Cause**: Each PR targeted its parent feature branch (`agent/issue-9`, `-14`, `-19`). The parent was
rebase-merged into `master` first, which rewrote its commits, and the child was then merged into
the old parent branch, which nothing merges any more.

**Fix**: Base every PR on `master` (see `AGENTS.md`), or retarget a stacked PR to `master` before
its parent merges. Check with `git merge-base --is-ancestor origin/<branch> origin/master` before
closing an issue. The three branches were ported onto MX-21 as `lunar-score-10-…`, `-15-…` and
`-20-…`.
