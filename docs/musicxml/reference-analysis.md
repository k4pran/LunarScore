# Reference importer analysis

Inspected on 2026-10-13: [music21 `xmlToM21.py` at `c2f16ce6bab0f980f113ba67c8c94568b3cf7385`](https://github.com/cuthbertLab/music21/blob/c2f16ce6bab0f980f113ba67c8c94568b3cf7385/music21/musicxml/xmlToM21.py), [MusicXML 4.0](https://www.w3.org/2021/06/musicxml40/) (fixed publication), [xmlutil at `f03b53bf83f608cf184f82c9f1ca5d890334476f`](https://github.com/pdvrieze/xmlutil/tree/f03b53bf83f608cf184f82c9f1ca5d890334476f), and [MusicXML test suite at `77c19f7e819154c70ca1a1992e80dcda8ff82fea`](https://github.com/w3c-cg/musicxmlTestSuite/tree/77c19f7e819154c70ca1a1992e80dcda8ff82fea). Findings below are limited to inspected source and documentation, not a claim of full music21 behaviour.

| Verified observation | Design consequence |
|---|---|
| `MusicXMLImporter` builds a score, parses metadata/defaults/credits and part list, then delegates each part to `PartParser`; its score has a `spannerBundle` for cross-event objects. | Separate score, part and measure responsibilities; resolve references after local event parsing. |
| Its `readFile` and `parseXMLText` paths accept only `score-partwise` roots and raise for another root. | Do not adopt that limitation: MusicXML 4.0 defines both partwise and timewise. |
| When a part ID is absent, it falls back to the first part-list entry; an unknown part ID can be skipped. | Treat these as compatibility policies with diagnostics, not silent standard behaviour. |
| The importer uses ElementTree and a large mutable music21 object graph. | The KMP library needs its own secure XML boundary, bounded state and focused immutable result. |
| The importer has exporter-specific workarounds, including a Finale `forward` flag in the inspected source. | Keep compatibility cases opt-in, named and testable; do not make music21 an oracle. |

Further source audit is MX-02: inspect `PartParser`, `MeasureParser`, timing, voices, chords, attributes, ties/slurs and malformed-input paths at the pinned commit and record exact methods and counterexamples. The current analysis deliberately avoids inventing behaviour that was not inspected. A known published [music21 9.1.0 issue](https://github.com/cuthbertLab/music21/issues/1714) reports a MusicXML import crash on an octave-change case; this is a version-specific report, not evidence that the pinned source still has that defect.

The [test suite README](https://github.com/w3c-cg/musicxmlTestSuite/blob/77c19f7e819154c70ca1a1992e80dcda8ff82fea/README.md) groups fixtures by pitch/rhythm, attributes, notes/chords/tuplets/grace, notation, parts, repeats, layout, lyrics, instruments, MIDI, compressed formats and ambiguity. It describes the files as MIT licensed. Import selected fixtures with attribution and license text, but write semantic expectations independently.
