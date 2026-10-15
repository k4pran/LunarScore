# MX-01: XML and archive dependency evidence

Recorded 2026-10-15 for [issue #2](https://github.com/k4pran/LunarScore/issues/2).
`:musicxml-parser` is an internal spike, with no score import API, publication, Compose dependency,
or dependency on the renderer model. MX-02/MX-03 own the public model/result contract;
MX-05 owns the production XML reader; MX-19 owns the MXL adapter.

## Pins and licenses

Direct versions live in `gradle/libs.versions.toml`; no dynamic versions or new repositories.

| Dependency | Version | Decision | License evidence |
|---|---|---|---|
| `io.github.pdvrieze.xmlutil:core` | 1.0.2 | Select its generic Kotlin pull reader on every target | [Apache-2.0](https://github.com/pdvrieze/xmlutil/blob/6c42903bcb981d0e2047be757ffdcde6778ac756/COPYING), resolved Maven POM |
| `com.squareup.okio:okio` | 3.9.1 | Prove common seekable ZIP and STORE/DEFLATE reads; conditional MX-19 candidate | [Apache-2.0](https://github.com/lysine-dev/okio/blob/8b870e8eaacecb1c1ceffbbb47246112604a1f92/LICENSE.txt), resolved Maven POM |
| `com.squareup.okio:okio-fakefilesystem` | 3.9.1 | Tests only; in-memory archives and handle-leak assertions | Same Okio license, resolved Maven POM |

Okio 3.9.1 is a fixed compatibility baseline, not the latest-version recommendation. Its pinned
[target wiring](https://github.com/lysine-dev/okio/blob/8b870e8eaacecb1c1ceffbbb47246112604a1f92/okio/build.gradle.kts)
shares ZIP/zlib code between JVM and Native. Revisit the pin for MX-19 and rerun these probes.
Production transitives resolve to Kotlin stdlib 2.4.21 and kotlinx-serialization-core 1.11.0
(Apache-2.0). Tests also resolve kotlinx-datetime 0.6.1 and Kotlin test 2.4.21 (Apache-2.0),
JUnit 4.13.2 (EPL-1.0) and Hamcrest 1.3 (BSD-3-Clause). These are observed transitive versions,
not separate catalog pins. POM licenses were checked for the new XML, Okio, serialization and
datetime dependencies. No upstream code or score fixtures were copied; smoke inputs are original
project test data. ZIP generation is recorded beside their hex bytes. Preserve dependency notices
when distributing artifacts.

## Selected implementation interfaces

* XML: `xmlStreaming.newGenericReader(CharSequence, expandEntities = true): XmlReader`, from
  pinned [IXmlStreaming](https://github.com/pdvrieze/xmlutil/blob/6c42903bcb981d0e2047be757ffdcde6778ac756/core/base/src/commonMain/kotlin/nl/adaptivity/xmlutil/IXmlStreaming.kt).
  Avoid platform-dependent `newReader`. Decode bounded bytes before constructing the reader.
  The spike boundary is `probeXml(ByteArray, XmlProbeLimits): XmlProbeResult`: one typed diagnostic
  and no partial events on failure. MX-05 should replace its document-sized event list with a
  parser-local pull cursor. Keep xmlutil types internal and out of public APIs.
* ZIP: injected `FileSystem`, archive `Path` and entry `Path`, using `openZip` and a
  `Source`. The probe reads the entry limit plus one overflow sentinel and closes each source.
  This ZIP FileSystem has no close operation or persistent open archive handle. Byte input needs
  a bounded seekable backing store, since `openZip` is random access. The future safety interface
  needs **raw names, compressed/expanded sizes, flags, method, entry count and bounded sources**.

The ZIP decision is conditional. Okio's public filesystem view is insufficient: its pinned
[directory implementation](https://github.com/lysine-dev/okio/blob/8b870e8eaacecb1c1ceffbbb47246112604a1f92/okio/src/zlibMain/kotlin/okio/internal/ZipFiles.kt)
normalizes names, replaces duplicate paths and hides compressed sizes. `probeZipEntry` is for
trusted test archives only. MX-19 must add a bounded raw central-directory validator before
`openZip`, or replace the candidate if it cannot meet the contract. `java.util.zip` has no iOS API;
platform bindings would require a second implementation. No MXL input API is exposed by the spike.

## XML policy and test evidence

`XmlDependencyProbeTest` covers both no-namespace score roots, ordered start/text/end events,
empty elements, prefix expansion, and namespaced versus unqualified attributes. Namespace-bearing
score roots yield `UNSUPPORTED_NAMESPACE`; `opus`/`sounds` yield `UNSUPPORTED_ROOT`. Foreign child
namespaces are preserved as events only, without semantic support. MusicXML 4.0 score elements
have no namespace; see [structure](https://www.w3.org/2021/06/musicxml40/tutorial/structure-of-musicxml-files/)
and [schema](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/).

DTD/entity declarations fail **before reader construction**: network DTDs, file entities, parameter
entities and internal expansion attempts yield `UNSAFE_XML`. The preflight is conservative: literal
`<!DOCTYPE`/`<!ENTITY` inside comments/CDATA is also rejected. All DOCTYPE forms, including benign
MusicXML DTD headers, are currently unsupported. Unknown references yield `MALFORMED_XML`;
predefined escapes and numeric character references work. No resolver, file lookup or network
access is supplied. `expandEntities = false` alone is insufficient: attribute references still
expand and the [generic reader](https://github.com/pdvrieze/xmlutil/blob/6c42903bcb981d0e2047be757ffdcde6778ac756/core/base/src/commonMain/kotlin/nl/adaptivity/xmlutil/core/KtXmlReader.kt)
can process declarations.

Only strict UTF-8 is accepted (optional BOM). Other declared encodings, NUL-bearing input and
invalid UTF-8 yield `UNSUPPORTED_ENCODING`. Exact-boundary tests cover bytes before decoding
(default 1 MiB), depth (64) and element count (10,000). Failures clear partial output and return
at most one diagnostic. Missing/multiple roots and mismatched/unclosed tags fail. The byte limit
also bounds token size. Streaming, time/cancellation budgets and locations remain MX-03/MX-05 work.

`ZipDependencyProbeTest` covers original STORE/DEFLATE archives, exact expanded-byte limits,
overflow, missing entries, invalid ZIP and handle cleanup on success/failure. It does not implement
container/mimetype/rootfile rules, raw path/duplicate checks, entry-count/ratio/total-expansion
limits, encryption or CRC validation. These remain MX-19 work under the
[compressed format](https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/).

## Validation on Windows / JDK 21

| Target | Compile evidence | Runtime evidence |
|---|---|---|
| JVM | Main/tests and module assembly | 13/13 tests passed |
| Android | Main/AAR and host tests | 13/13 host tests passed; device runtime unverified |
| iosArm64 | Main/test klibs cross-compiled | Unverified: needs macOS/Xcode and device |
| iosSimulatorArm64 | Main/test klibs cross-compiled | Unverified: needs macOS/Xcode and simulator |
| JS / Wasm | Not configured for this module in MX-01 | Unverified; renderer targets unchanged |

Commands (use `gradlew.bat` in PowerShell):

```text
./gradlew :musicxml-parser:jvmTest :musicxml-parser:testAndroidHostTest
./gradlew :musicxml-parser:compileKotlinIosArm64 :musicxml-parser:compileKotlinIosSimulatorArm64
./gradlew :musicxml-parser:compileTestKotlinIosArm64 :musicxml-parser:compileTestKotlinIosSimulatorArm64
./gradlew :musicxml-parser:assemble
./gradlew :musicxml-parser:build
./gradlew :musicxml-parser:dependencies --configuration jvmTestRuntimeClasspath
```

The existing `:sheetmusic-core:jvmTest` baseline also passed. Compile evidence is not iOS runtime
verification. Run `:musicxml-parser:iosSimulatorArm64Test` on macOS before declaring Native runtime
support. No web dependencies were added; the Yarn lock is unchanged. A pre-existing Compose
host-test configuration warning is unrelated.
