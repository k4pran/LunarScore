# Neutral score model (MX-02)

`:sheetmusic-model` uses package `com.ryanjames.sheetmusic.score`, separate from the legacy
renderer package `com.ryanjames.sheetmusic.model`. Its common production code depends only on
Kotlin; there are no XML, archive, Compose or platform APIs. It declares the same Android/JVM/iOS/
JS/Wasm targets as `sheetmusic-core`. This is an initial vocabulary, not a complete MusicXML model
or an importer. MX-01's dependency spike is available in `:musicxml-parser`; the neutral model
does not depend on that module.

## Data and units

- `Score` contains ordered `Part`s, each with staff/instrument definitions and ordered `Measure`s.
  Empty scores and measures are allowed. Measure IDs differ from printed measure numbers;
  pickups and nonnumeric/repeated display numbers are legal.
- Each `Event` has a stable score-global ID, measure-local rational onset and part-local staff.
  Notes/rests also carry voice IDs. Lists preserve encounter order independently of onset order.
  Backup/forward are source operations: their model projection is overlapping events or a gap.
  Measure `extent` is observed musical extent, not an inferred time signature duration.
- Rational time uses quarter-note units: whole = 4, eighth = 1/2. `Rational.of(n, d)` reduces
  exactly, normalizes the denominator positive and represents zero as 0/1. Reduced numerator and
  denominator fit signed 32-bit Int (denominator 1 through Int.MAX_VALUE). Exact Long intermediates
  cover all products, sums and differences of those components on every target. Reduction happens
  before range checks; e.g. MAX/2 + MAX/2 succeeds. There is no rounding, saturation or wraparound.
- Construction/arithmetic throw `RationalException` with `ZERO_DENOMINATOR`, `DIVISION_BY_ZERO` or
  `OVERFLOW`. A reduced result outside the component bound is unsupported, even if a wider numeric
  representation could hold it. Future input adapters must translate this into source-located
  parser diagnostics (MX-03/MX-07), never approximate the value or return a silently truncated score.
- Written pitch uses step, octave and rational semitone alteration. Sounding pitch is an optional
  independent semitone number (middle C = 60). Absence means unknown. Transposition is a separate
  staff-scoped attribute event; the model does not derive or respell pitches. Unpitched percussion
  keeps display step/octave and instrument identity separately; MIDI percussion keys are 0–127.
- Note duration is performed musical duration, distinct from printed base value, dots, accidental
  and time modification. Explicit performance attack/release offsets are in quarter notes.
  Grace duration is null and does not advance the main timeline. Cue duration is positive.
  A chord is independently timed noteheads sharing a chord ID; staff and duration may differ.
- Attribute events retain exact offsets for simple time signatures, clefs, transposition and
  instrument changes. They are staff-scoped; part-wide source attributes must be expanded explicitly.
  These are data records, not a state resolver.
- Relationships have their own IDs and typed endpoint event IDs. Sound ties, printed ties and slurs
  remain independent; multiple/nested spans may share endpoints. Missing endpoints remain null.
  IDs must be assigned independently of source XML IDs so anonymous elements remain addressable.

## Invariants and diagnostics

Constructors reject intrinsic invalid values (blank required IDs, nonpositive staff numbers,
negative onsets/extents, nonpositive timed duration/counts and negative dots) with
`IllegalArgumentException`. Keep graph assembly separate from validation so forward references and
orphan spans can be represented without discarding data. Call `Score.validate()` for ordered
`ModelDiagnostic(code, path, reference)` records:

| Code | Meaning |
|---|---|
| `DUPLICATE_ID` | Duplicate part, staff, instrument, measure, event or relationship identity |
| `UNKNOWN_STAFF` | Event references an undeclared staff in its part |
| `UNKNOWN_INSTRUMENT` | Note/instrument change references an undeclared instrument in its part |
| `MISSING_ENDPOINT` | Span has a null start or end |
| `UNKNOWN_EVENT` | Span endpoint does not resolve to an event |

Part IDs are score-local; staff numbers, instrument IDs and measure IDs are part-local; event IDs
and relationship IDs each have a separate score-global namespace. Voice/chord labels belong to
their containing part/measure. Validation reports all findings in encounter order and does not
repair or omit records. Source positions, strict/tolerant import policy and unsupported source
retention belong to MX-03, outside the neutral model. Chord-reference duration rules, meter fullness,
pitch agreement for ties and other musical consistency checks remain feature tasks.

Additive meters, keys, directions, layout hints, lyrics, rich annotations and opaque source content
are not accepted as import input by this change; no parser API exists here. Their future adapters
must implement them or emit the S01–S27 unsupported diagnostics in the coverage/risk ledger.

## Renderer migration

`sheetmusic-core` has an `api` dependency on the model so consumers of the public adapter can
resolve its types on every target and through Maven metadata. `Score.toNeutralScore()` preserves
every currently supported renderer note, measure and clef, assigns deterministic IDs and explicit
onsets/durations, and infers neither meter nor sounding pitch. The existing layout and Compose
APIs/tests remain unchanged. A later neutral-to-layout adapter must diagnose unsupported richer
data; silently dropping rests, voices, instruments or spans is not a migration strategy.

## Verification

`ScoreProjectionTest` contains original model-only F01/F05/F10/F17 expectations from
[test-plan.md](test-plan.md). `RationalTest` exercises exact arithmetic and numeric failure codes.
`NeutralScoreAdapterTest` covers all legacy pitch/duration values and per-measure cursor reset.
These do not claim parsing or XSD conformance.

Verified on Windows on 2026-10-15 with the Gradle wrapper:

| Check | Result |
|---|---|
| Model common tests (12) and core tests (22, including the adapter) | Passed on JVM, Android host, JS Node and Wasm Node |
| Compose renderer regressions | Passed: 4 JVM tests, 3 JS browser tests and 3 Wasm browser tests in headless Chrome |
| iOS model main/test klibs | Compiled for iosArm64 and iosSimulatorArm64 |
| Android/JVM/web integration, metadata and production sample distributions | Compiled/built successfully |
| iOS linking and runtime tests | Skipped on Windows; require macOS/Xcode |

Final command: `./gradlew build allTests` — **BUILD SUCCESSFUL**. Before that, targeted model/core
`allTests`, both model iOS main/test compilations, Compose Android compilation and sample JVM
compilation also passed. Existing Compose Android-host-test configuration and webpack bundle-size
warnings remain; they did not fail the build. No new npm dependency or lockfile change was needed.
