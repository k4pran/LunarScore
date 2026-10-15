# GitHub issue bodies for MusicXML 4.0

These 22 bodies correspond one-to-one with the tasks in [implementation-plan.md](implementation-plan.md). They were posted to [k4pran/LunarScore issues](https://github.com/k4pran/LunarScore/issues) on 2026-10-15: MX-00 is issue #1, through MX-21 as issue #22. This file remains as a local copy of the original issue text.

## MX-00 Pin references and audit importer

**Milestone:** M0: evidence and contracts  
**Complexity and risk:** M / large source surface

### Scope

Expand `reference-analysis.md` with exact music21 methods for part/measure, timing, state and spans; record full 4.0 element inventory and license evidence.

### Dependencies

—

### MusicXML 4.0 references

[spec](https://www.w3.org/2021/06/musicxml40/), [XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/)

### Required fixtures and edge cases

Document one spec/music21 disagreement; source links at pinned commit.

### Acceptance criteria

- [ ] Findings verified with links; assumptions labelled; coverage matrix expanded for omitted groups.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-01 Prove KMP XML and archive dependencies

**Milestone:** M0: evidence and contracts  
**Complexity and risk:** M / Native behaviour

### Scope

Add module skeleton and Android/iOS/JVM compile spike; verify xmlutil pull order, namespaces, entities, input limits and candidate ZIP APIs.

### Dependencies

MX-00

### MusicXML 4.0 references

[structure](https://www.w3.org/2021/06/musicxml40/tutorial/structure-of-musicxml-files/), [compressed](https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/)

### Required fixtures and edge cases

Root/namespace/entity smoke inputs on all targets.

### Acceptance criteria

- [ ] Document pinned versions/licenses and choose implementation interfaces; builds compile; unsafe entity attempt rejected.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-02 Define neutral score model

**Milestone:** M0: evidence and contracts  
**Complexity and risk:** M / premature public API

### Scope

Add `:sheetmusic-model` or equivalent neutral package with Score/Part/Staff/Measure/Event, rational time, pitch, notated/performed fields and relationships.

### Dependencies

MX-00,MX-01

### MusicXML 4.0 references

[note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/), [duration](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/duration/)

### Required fixtures and edge cases

Model-only F01,F05,F10,F17 projections; reduction/overflow.

### Acceptance criteria

- [ ] No XML/Compose dependency; can represent all examples exactly; old renderer tests retain a migration path.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-03 Diagnostics and provenance contract

**Milestone:** M0: evidence and contracts  
**Complexity and risk:** S / false success

### Scope

Add stable error/warning/unsupported codes, source location, limits and strict/tolerant policy to parse result.

### Dependencies

MX-02

### MusicXML 4.0 references

[XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/)

### Required fixtures and edge cases

F22,F23; unknown but valid element; diagnostic cap.

### Acceptance criteria

- [ ] Failure and loss are observable; no silent meaningful discard.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-04 Independent fixture and XSD harness

**Milestone:** M0: evidence and contracts  
**Complexity and risk:** M / circular tests

### Scope

Create F01–F08 and semantic expectations, local-catalog JVM validation, license manifest and common-target assertion runner. Test author owns expectations.

### Dependencies

MX-00,MX-02

### MusicXML 4.0 references

[XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/)

### Required fixtures and edge cases

F01–F08 valid/invalid labels; external resolution disabled.

### Acceptance criteria

- [ ] Expected musical facts are written before importer code; validation, parse and semantics have separate outcomes.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-05 Secure ordered XML reader

**Milestone:** M1: first end-to-end import  
**Complexity and risk:** M / parser security

### Scope

Implement bounded pull reader wrapper, root detection, namespaces, source positions, and fatal XML diagnostics.

### Dependencies

MX-01,MX-03

### MusicXML 4.0 references

[structure](https://www.w3.org/2021/06/musicxml40/tutorial/structure-of-musicxml-files/)

### Required fixtures and edge cases

F22 plus DTD/entity/depth/size cases.

### Acceptance criteria

- [ ] Both permitted roots recognized; malformed/unsafe input fails predictably.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-06 Partwise score skeleton

**Milestone:** M1: first end-to-end import  
**Complexity and risk:** M / ID handling

### Scope

Parse header, part-list IDs, parts and measures into neutral structure; retain metadata and order.

### Dependencies

MX-02,MX-04,MX-05

### MusicXML 4.0 references

[structure](https://www.w3.org/2021/06/musicxml40/tutorial/structure-of-musicxml-files/)

### Required fixtures and edge cases

F02; missing/duplicate/unreferenced part ID.

### Acceptance criteria

- [ ] Empty and multi-part measure structures parsed with diagnostics for broken references.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-07 Notes and rests end to end

**Milestone:** M1: first end-to-end import  
**Complexity and risk:** M / duration semantics

### Scope

Parse divisions, pitch, rest, note duration and simple note type; produce exact onset and duration in one voice.

### Dependencies

MX-06

### MusicXML 4.0 references

[note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/), [duration](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/duration/)

### Required fixtures and edge cases

F01,F03 basic,F04; zero/overflow duration.

### Acceptance criteria

- [ ] F01 passes on Android/iOS/JVM; a one-measure score imports without renderer dependency.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-08 Attribute state and pitch

**Milestone:** M2: core score semantics  
**Complexity and risk:** M / implicit state

### Scope

Apply divisions/key/time/clef/staff details at exact offsets, including intra-measure changes; preserve pitch alter versus displayed accidental.

### Dependencies

MX-07

### MusicXML 4.0 references

[attributes](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/attributes/)

### Required fixtures and edge cases

F03,F04,F07; missing state and key changes.

### Acceptance criteria

- [ ] Events expose effective state and exact timing across changes.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-09 Polyphony, chords and staves

**Milestone:** M2: core score semantics  
**Complexity and risk:** M / cursor scope

### Scope

Interpret backup/forward, voice IDs, chord onset, heterogeneous chord durations, multiple staves and parts.

### Dependencies

MX-08

### MusicXML 4.0 references

[structure](https://www.w3.org/2021/06/musicxml40/tutorial/structure-of-musicxml-files/), [4.0 clarification](https://www.w3.org/2021/06/musicxml40/version-history/40/)

### Required fixtures and edge cases

F05,F06,F08; backup before zero, voice gaps.

### Acceptance criteria

- [ ] Canonical events have correct onset/voice/staff; invalid cursor moves diagnosed.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-10 Timewise normalization

**Milestone:** M2: core score semantics  
**Complexity and risk:** M / sparse parts

### Scope

Parse score-timewise into same semantic projection as partwise without XSLT at runtime.

### Dependencies

MX-09

### MusicXML 4.0 references

[score-timewise](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-timewise/)

### Required fixtures and edge cases

F12 and missing-part measure.

### Acceptance criteria

- [ ] Equivalent partwise/timewise fixtures compare equal semantically.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-11 Ties and slurs

**Milestone:** M3: notation and expression  
**Complexity and risk:** M / ambiguous pairing

### Scope

Resolve sound ties and notation slurs/tied marks across measures, voices and numbered spans.

### Dependencies

MX-09

### MusicXML 4.0 references

[notations](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notations/)

### Required fixtures and edge cases

F10; duplicate/orphan endpoints.

### Acceptance criteria

- [ ] All endpoints resolve or produce typed diagnostics; sound and notation stay distinct.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-12 Beams, stems and tuplets

**Milestone:** M3: notation and expression  
**Complexity and risk:** M / nested ratios

### Scope

Preserve beam/stem notation and exact tuplet/time-modification durations, including nested cases.

### Dependencies

MX-09

### MusicXML 4.0 references

[beam](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beam/), [time modification](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/time-modification/)

### Required fixtures and edge cases

F09,F11; mixed chord values.

### Acceptance criteria

- [ ] Exact rational offsets and notation flags match fixture; no float drift.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-13 Directions and note expression

**Milestone:** M3: notation and expression  
**Complexity and risk:** M / broad variants

### Scope

Parse direction types, tempo, words, dynamics, articulations and ornaments; attach correct offset/staff.

### Dependencies

MX-09

### MusicXML 4.0 references

[direction](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/direction/), [notations](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notations/)

### Required fixtures and edge cases

F13,F14; direction offset after backup.

### Acceptance criteria

- [ ] Semantic and visual components retained separately; unsupported variants diagnosed.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-14 Lyrics and text runs

**Milestone:** M3: notation and expression  
**Complexity and risk:** S / mixed content

### Scope

Parse verses, syllabic values, elision, extends and ordered mixed text; link to note IDs.

### Dependencies

MX-09

### MusicXML 4.0 references

[lyric](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/lyric/)

### Required fixtures and edge cases

F15; missing verse number, melisma.

### Acceptance criteria

- [ ] Canonical text sequence and note associations match expectations.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-15 Grace and cue notes

**Milestone:** M3: notation and expression  
**Complexity and risk:** S / role timing

### Scope

Add note roles and non-advancing grace semantics, cue duration and relationship to principal notes.

### Dependencies

MX-09

### MusicXML 4.0 references

[note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/)

### Required fixtures and edge cases

F19; grace chord and cue with backup.

### Acceptance criteria

- [ ] Cursor and display role correct; missing grace duration handled per spec.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-16 Instruments and pitch interpretation

**Milestone:** M4: broad 4.0 interchange  
**Complexity and risk:** M / transposition rules

### Scope

Part/instrument timelines, transposition, concert score and unpitched percussion; derive sounding pitch when defined.

### Dependencies

MX-08,MX-09

### MusicXML 4.0 references

[score-instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-instrument/), [4.0 history](https://www.w3.org/2021/06/musicxml40/version-history/40/)

### Required fixtures and edge cases

F17,F21; mid-score instrument change.

### Acceptance criteria

- [ ] Written pitch preserved; sounding mapping and ambiguity explicit.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-17 Barlines and repeat graph

**Milestone:** M4: broad 4.0 interchange  
**Complexity and risk:** M / playback ambiguity

### Scope

Retain bar styles, repeat signs, endings and measure-repeat semantics; build playback navigation separately.

### Dependencies

MX-09,MX-11

### MusicXML 4.0 references

[barline](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/barline/)

### Required fixtures and edge cases

F16; nested/invalid endings, pickup.

### Acceptance criteria

- [ ] Notation structure and playback order distinguishable; orphan endings diagnosed.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-18 Layout and visual hints

**Milestone:** M4: broad 4.0 interchange  
**Complexity and risk:** M / information loss

### Scope

Parse defaults, print, page/system/staff layout, positioning and colour as optional engraving hints.

### Dependencies

MX-06,MX-09

### MusicXML 4.0 references

[print](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/print/)

### Required fixtures and edge cases

F18; omitted defaults, negative offsets.

### Acceptance criteria

- [ ] Hints retained without requiring renderer support; units documented.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-19 Bounded `.mxl` adapter

**Milestone:** M4: broad 4.0 interchange  
**Complexity and risk:** M / ZIP portability

### Scope

Parse ZIP container, rootfile and mimetype rules with path and expansion controls; feed selected XML to same importer.

### Dependencies

MX-05,MX-06

### MusicXML 4.0 references

[compressed files](https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/)

### Required fixtures and edge cases

F20; traversal, duplicate, encrypted, bomb archive.

### Acceptance criteria

- [ ] Equivalent `.mxl`/XML scores compare equal; unsafe archive rejected.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-20 MusicXML 4.0 additions

**Milestone:** M4: broad 4.0 interchange  
**Complexity and risk:** L / breadth; split PRs

### Scope

Enumerate every 4.0 addition/change and map supported semantics or issue explicit unsupported diagnostics.

### Dependencies

MX-13,MX-16,MX-18,MX-19

### MusicXML 4.0 references

[4.0 history](https://www.w3.org/2021/06/musicxml40/version-history/40/)

### Required fixtures and edge cases

F21 plus each changed element/value.

### Acceptance criteria

- [ ] Change-list checklist complete; no silent new-feature loss.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---

## MX-21 Compliance, compatibility and security audit

**Milestone:** M5: compliance hardening  
**Complexity and risk:** L / incomplete coverage; split PRs

### Scope

Run selected public suite, legal exporter corpus and adversarial inputs; resolve gaps, update docs and target evidence.

### Dependencies

MX-10–MX-20

### MusicXML 4.0 references

[spec](https://www.w3.org/2021/06/musicxml40/), [XSD](https://www.w3.org/2021/06/musicxml40/listings/musicxml.xsd/)

### Required fixtures and edge cases

F01–F23, mixed-feature pairs and every regression.

### Acceptance criteria

- [ ] Coverage statuses evidence-backed; remaining unsupported features listed with diagnostics; Android/iOS/JVM matrix recorded.
- [ ] Relevant rows in `docs/musicxml/spec-coverage.md` reflect implementation and test status separately.
- [ ] Any new unsupported cases produce structured diagnostics and are recorded in `docs/musicxml/risks-and-gaps.md`.

---
