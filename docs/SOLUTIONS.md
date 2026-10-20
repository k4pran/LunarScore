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
