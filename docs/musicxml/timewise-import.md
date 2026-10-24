# Timewise normalization (MX-10)

Issue [#11](https://github.com/k4pran/LunarScore/issues/11) lets `parseMusicXml` accept an
unnamespaced [`score-timewise`](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-timewise/)
root. It produces the same `Score` as the equivalent partwise document. No XSLT runs, and there is
no DOM or second pass: measures are normalized as the ordered reader streams them.

## Mapping

- The header (work, movement, identification, defaults, credits, part-list) is parsed exactly as in
  partwise. Repeated top-level `measure` elements take the place of `part` bodies and must follow
  the part-list.
- Each `part` inside a timewise `measure` is read as that part's next measure, with the same note
  and attribute handling as a partwise measure. Measure `number`, `implicit`
  and unmapped measure attributes come from the timewise `measure`; the inner `part` accepts only `id`.
- Divisions persist per part from one of its measures to the next, even when
  other parts are read in between. Neutral parts still follow part-list order, whatever order the
  parts have inside a measure.
- **Missing-part measure:** a declared part absent from a timewise measure gets an empty measure
  there (no events, zero extent, the measure's number and implicit flag). Like the MusicXML
  timewise-to-partwise stylesheet, this keeps every part on the same measure sequence, so
  generated IDs (`measure-3`) align across parts. The equivalent partwise document has an
  explicit empty `<measure>`. This is valid MusicXML, so no diagnostic is reported. Each padded
  measure adds a provenance entry, so `maxProvenanceEntries` bounds the padding: many declared
  parts that are each absent from many small measures fail with `PROVENANCE_LIMIT` rather than
  allocating parts × measures without limit.

## Errors

Under both policies these are fatal `INVALID_STRUCTURE`: a part ID that is missing or blank, a part
that appears twice in one measure, a declared part that appears in no measure, a measure without a
`number` or before the part-list, and a timewise root without a part-list. An invalid `implicit`
value is fatal `INVALID_VALUE`. Other content under a timewise root or measure that is not a part,
such as a `part` directly under the root, produces the usual structured `UNSUPPORTED_FEATURE` /
`DISCARDED` finding for its feature group. A part whose ID the part-list does not declare is skipped in that
measure as S01 `UNSUPPORTED_FEATURE` / `DISCARDED` (MX-24): tolerant parsing warns, strict fails.

## Provenance

Model paths are the same as for partwise. `parts[i]` maps to the part's first timewise `part`
element. `parts[i].measures[j]` maps to the timewise `measure` element, tagged with the part ID; a
padded measure maps there too. Events map to their elements inside the timewise `part`, for
example `/score-timewise[1]/measure[2]/part[2]/note[1]`.

## Fixtures and verification

`TimewiseFixtures.kt` adds original F12, authored from the MusicXML 4.0 score-timewise
reference: a flute and a piano with different divisions, a measure that lists the piano
first, and an implicit measure 3 with no flute part. It is paired with a partwise equivalent. Both
are validated against the pinned 4.0 XSD by `XsdValidationTest` ([MX-04](fixture-manifest.md)).

`TimewiseImportTest` adds 6 common tests: equal F12 scores and model-path sets for both roots
with no timewise diagnostics; timewise provenance; a part missing from leading measures; padding
stopped by the provenance limit; structural failures under both policies; unmapped timewise
content under strict and tolerant policy. The MX-06 test that
expected `UNSUPPORTED_ROOT` for timewise roots has been removed.

Windows verification on 2026-10-22:

| Check | Result |
|---|---|
| Parser JVM / Android host tests | 107 passed per target (6 timewise, 9 attribute, 14 note, 17 skeleton, 33 reader, 15 contract, 13 probes) |
| Parser iosArm64 / iosSimulatorArm64 test klibs | Cross-compiled |
| iOS runtime | Skipped on Windows; requires macOS/Xcode |
