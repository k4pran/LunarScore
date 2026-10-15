# MusicXML 4.0 element inventory

Audited 2026-10-15 for MX-00. This is a complete inventory of named `xs:element` declarations in the four document schemas at [release commit `799e2defb2ece0ae7bafe08dcbcac25b2c631d53`](https://github.com/w3c/musicxml/tree/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema), the resolved `v4.0` tag. See [reference-analysis.md](reference-analysis.md) for publication cross-checks and license evidence.

The score schema has **480 declarations / 442 distinct element names**. Container has **3 / 3**, opus **5 / 4**, and sounds **5 / 5**. Counts are per schema: `opus`, `sound`, `solo` and `ensemble` occur in more than one document vocabulary. Imported `xml.xsd` and `xlink.xsd` declare attributes, not score elements. Catalogs, DTDs, XSL transformations and the sound-ID data file are not additional element vocabularies.

Each table has one row per distinct name. Contexts identify the enclosing top-level XSD type/group/root that physically declares the element, **not all possible XML parents**; group/type references may make it legal elsewhere. A count in parentheses means repeated declarations in that context, including alternative branches. Both roots declare their own `part` and `measure`. Related S-IDs link to [spec-coverage.md](spec-coverage.md); these are audit routing groups, not implementation promises. Every row currently inherits **Implementation: Not implemented; Tests: None**. Attribute/value checks, 4.0 change-list auditing and semantic assertions remain separate obligations.

## Reproducible source pin

Download each file from `https://raw.githubusercontent.com/w3c/musicxml/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/` using the filename below. SHA-256 is over the downloaded bytes, including original line endings. These are research inputs, not vendored runtime dependencies.

| File | SHA-256 |
|---|---|
| [musicxml.xsd](https://github.com/w3c/musicxml/blob/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/musicxml.xsd) | `bfe37ed25a9ec00e6f2591d53df260b84efe12aed209ba3ac0a76f9287665a99` |
| [container.xsd](https://github.com/w3c/musicxml/blob/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/container.xsd) | `deddcc2f51e856de21397bbe25e2cf304ca9e3253b0d25dcc6349c390bc22fa6` |
| [opus.xsd](https://github.com/w3c/musicxml/blob/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/opus.xsd) | `e8256607b9255075f16c1d70ed53f8384ab6e3b9dc4c1f81291cdd1d56b0474a` |
| [sounds.xsd](https://github.com/w3c/musicxml/blob/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/sounds.xsd) | `a3a423bcdf9f385f74d6bdf6024f7a29e70c4745de4825d33327f903124533e1` |
| [xml.xsd](https://github.com/w3c/musicxml/blob/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/xml.xsd) | `616a3077df5cfc954ac74a75abe9697b95eef7a85dbe09367d995a483e840eb5` |
| [xlink.xsd](https://github.com/w3c/musicxml/blob/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema/xlink.xsd) | `6e601f8eeb41618b50e4c7f944dff754e57ea43b602755470dda24c9c2f6df92` |

To verify inventory completeness offline after downloading those files into `build/reference-audit`, run this Python 3 snippet from the repository root. It checks pinned bytes, declaration counts, names and multiplicity per declaration context. It does not validate MusicXML semantics or importer behaviour.

```python
from collections import Counter, defaultdict
from pathlib import Path
import hashlib
import re
import xml.etree.ElementTree as ET

document = Path("docs/musicxml/element-inventory.md").read_text(encoding="utf-8")
pins = re.findall(r"\| \[([\w.-]+\.xsd)\]\([^\n]+?\) \| `([0-9a-f]{64})`", document)
assert len(pins) == 6
for filename, digest in pins:
    assert hashlib.sha256((Path("build/reference-audit") / filename).read_bytes()).hexdigest() == digest
for schema, expected in [("musicxml", (480, 442)), ("container", (3, 3)), ("opus", (5, 4)), ("sounds", (5, 5))]:
    root = ET.parse(Path("build/reference-audit") / (schema + ".xsd")).getroot()
    contexts = defaultdict(Counter)
    for owner in root:
        kind = owner.tag.split("}")[-1]
        for element in owner.iter("{http://www.w3.org/2001/XMLSchema}element"):
            name = element.get("name")
            if name:
                label = {"complexType": "type", "group": "group", "element": "root"}[kind]
                contexts[name][f"{label} {owner.get('name')}"] += 1
    section = document.split(f"## {schema}.xsd\n", 1)[1].split("\n## ", 1)[0]
    rows = re.findall(r"^\| \[([^\]]+)\]\([^\n]+?\) \| ([^|]+) \| ([^|]+) \|$", section, re.M)
    assert len(rows) == len(contexts) == expected[1]
    assert {name for name, _, _ in rows} == set(contexts)
    assert sum(sum(counts.values()) for counts in contexts.values()) == expected[0]
    for name, actual_contexts, groups in rows:
        expected_contexts = "; ".join(f"`{key}`" + (f" ({count})" if count > 1 else "") for key, count in sorted(contexts[name].items()))
        assert actual_contexts.strip() == expected_contexts, name
        assert all(f"| {group} |" in Path("docs/musicxml/spec-coverage.md").read_text(encoding="utf-8") for group in groups.strip().split(","))
    print(schema, "verified:", expected)
```

## musicxml.xsd

| Element | Declaration contexts | Related coverage |
|---|---|---|
| [accent](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accent/) | `type articulations` | S12 |
| [accidental](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accidental/) | `type note` | S04 |
| [accidental-mark](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accidental-mark/) | `type notations`; `type ornaments` | S12 |
| [accidental-text](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accidental-text/) | `type name-display`; `type notehead-text` | S03,S04,S17 |
| [accord](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accord/) | `type scordatura` | S13 |
| [accordion-high](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accordion-high/) | `type accordion-registration` | S13 |
| [accordion-low](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accordion-low/) | `type accordion-registration` | S13 |
| [accordion-middle](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accordion-middle/) | `type accordion-registration` | S13 |
| [accordion-registration](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/accordion-registration/) | `type direction-type` | S13 |
| [actual-notes](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/actual-notes/) | `type time-modification` | S05,S11 |
| [alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/alter/) | `type pitch` | S04 |
| [appearance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/appearance/) | `type defaults` | S17 |
| [arpeggiate](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/arpeggiate/) | `type notations` | S12 |
| [arrow](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/arrow/) | `type technical` | S12,S23 |
| [arrow-direction](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/arrow-direction/) | `type arrow` | S12,S23 |
| [arrow-style](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/arrow-style/) | `type arrow` | S12,S23 |
| [arrowhead](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/arrowhead/) | `type arrow` | S12,S23 |
| [articulations](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/articulations/) | `type notations` | S12 |
| [artificial](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/artificial/) | `type harmonic` | S12,S23 |
| [assess](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/assess/) | `type listen` | S20,S26 |
| [attributes](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/attributes/) | `group music-data` | S07 |
| [backup](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/backup/) | `group music-data` | S06 |
| [bar-style](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bar-style/) | `type barline` | S15 |
| [barline](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/barline/) | `group music-data` | S15 |
| [barre](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/barre/) | `type frame-note` | S22,S23 |
| [base-pitch](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/base-pitch/) | `type harmonic` | S12,S23 |
| [bass](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bass/) | `group harmony-chord` | S22 |
| [bass-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bass-alter/) | `type bass` | S22 |
| [bass-separator](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bass-separator/) | `type bass` | S20,S22 |
| [bass-step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bass-step/) | `type bass` | S22 |
| [beam](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beam/) | `type note` | S10 |
| [beat-repeat](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beat-repeat/) | `type measure-style` | S24 |
| [beat-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beat-type/) | `group time-signature` | S07 |
| [beat-unit](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beat-unit/) | `group beat-unit` | S13 |
| [beat-unit-dot](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beat-unit-dot/) | `group beat-unit` | S13 |
| [beat-unit-tied](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beat-unit-tied/) | `type metronome` (2) | S13 |
| [beater](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beater/) | `type percussion` | S13,S16 |
| [beats](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/beats/) | `group time-signature` | S07 |
| [bend](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bend/) | `type technical` | S12,S23 |
| [bend-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bend-alter/) | `type bend` | S12,S23 |
| [bookmark](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bookmark/) | `group music-data`; `type credit` (2) | S25 |
| [bottom-margin](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bottom-margin/) | `group all-margins` | S17 |
| [bracket](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/bracket/) | `type direction-type` | S13 |
| [brass-bend](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/brass-bend/) | `type technical` | S12,S23 |
| [breath-mark](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/breath-mark/) | `type articulations` | S12 |
| [caesura](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/caesura/) | `type articulations` | S12 |
| [cancel](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/cancel/) | `group traditional-key` | S07 |
| [capo](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/capo/) | `type staff-details` | S07,S23 |
| [chord](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chord/) | `group full-note` | S06 |
| [chromatic](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/chromatic/) | `group transpose` | S16 |
| [circular-arrow](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/circular-arrow/) | `type arrow` | S12,S23 |
| [clef](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/clef/) | `type attributes` | S07 |
| [clef-octave-change](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/clef-octave-change/) | `group clef` | S07 |
| [coda](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/coda/) | `type barline`; `type direction-type` | S13,S15 |
| [concert-score](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/concert-score/) | `type defaults` | S16,S20 |
| [creator](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/creator/) | `type identification` | S03 |
| [credit](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/credit/) | `group score-header` | S03 |
| [credit-image](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/credit-image/) | `type credit` | S03,S17,S25 |
| [credit-symbol](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/credit-symbol/) | `type credit` (2) | S03,S17,S25 |
| [credit-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/credit-type/) | `type credit` | S03,S17,S25 |
| [credit-words](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/credit-words/) | `type credit` (2) | S03,S17,S25 |
| [cue](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/cue/) | `type note` (2) | S18 |
| [damp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/damp/) | `type direction-type` | S13 |
| [damp-all](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/damp-all/) | `type direction-type` | S13 |
| [dashes](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/dashes/) | `type direction-type` | S13 |
| [defaults](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/defaults/) | `group score-header` | S17 |
| [degree](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/degree/) | `group harmony-chord` | S22 |
| [degree-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/degree-alter/) | `type degree` | S22 |
| [degree-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/degree-type/) | `type degree` | S22 |
| [degree-value](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/degree-value/) | `type degree` | S22 |
| [delayed-inverted-turn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/delayed-inverted-turn/) | `type ornaments` | S12 |
| [delayed-turn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/delayed-turn/) | `type ornaments` | S12 |
| [detached-legato](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/detached-legato/) | `type articulations` | S12 |
| [diatonic](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/diatonic/) | `group transpose` | S16 |
| [direction](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/direction/) | `group music-data` | S13 |
| [direction-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/direction-type/) | `type direction` | S13 |
| [directive](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/directive/) | `type attributes` | S07 |
| [display-octave](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/display-octave/) | `group display-step-octave` | S04 |
| [display-step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/display-step/) | `group display-step-octave` | S04 |
| [display-text](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/display-text/) | `type name-display`; `type notehead-text` | S03,S04,S17 |
| [distance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/distance/) | `type appearance` | S17 |
| [divisions](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/divisions/) | `type attributes` | S05 |
| [doit](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/doit/) | `type articulations` | S12 |
| [dot](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/dot/) | `type note` | S05 |
| [double](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/double/) | `group transpose` | S16 |
| [double-tongue](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/double-tongue/) | `type technical` | S12,S23 |
| [down-bow](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/down-bow/) | `type technical` | S12,S23 |
| [duration](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/duration/) | `group duration` | S05 |
| [dynamics](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/dynamics/) | `type direction-type`; `type notations` | S12,S13 |
| [effect](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/effect/) | `type percussion` | S13,S16 |
| [elevation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/elevation/) | `type midi-instrument` | S26 |
| [elision](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/elision/) | `type lyric` | S14 |
| [encoder](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/encoder/) | `type encoding` | S03 |
| [encoding](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/encoding/) | `type identification` | S03 |
| [encoding-date](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/encoding-date/) | `type encoding` | S03 |
| [encoding-description](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/encoding-description/) | `type encoding` | S03 |
| [end-line](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/end-line/) | `type lyric` | S14 |
| [end-paragraph](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/end-paragraph/) | `type lyric` | S14 |
| [ending](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ending/) | `type barline` | S15 |
| [ensemble](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ensemble/) | `group virtual-instrument-data` | S16 |
| [except-voice](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/except-voice/) | `group slash` | S24 |
| [extend](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/extend/) | `type figure`; `type lyric` (2) | S14,S22 |
| [eyeglasses](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/eyeglasses/) | `type direction-type` | S13 |
| [f](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/f/) | `type dynamics` | S12,S13 |
| [falloff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/falloff/) | `type articulations` | S12 |
| [feature](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/feature/) | `type grouping` | S25 |
| [fermata](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fermata/) | `type barline`; `type notations` | S12,S15 |
| [ff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ff/) | `type dynamics` | S12,S13 |
| [fff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fff/) | `type dynamics` | S12,S13 |
| [ffff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ffff/) | `type dynamics` | S12,S13 |
| [fffff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fffff/) | `type dynamics` | S12,S13 |
| [ffffff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ffffff/) | `type dynamics` | S12,S13 |
| [fifths](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fifths/) | `group traditional-key` | S07 |
| [figure](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/figure/) | `type figured-bass` | S22 |
| [figure-number](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/figure-number/) | `type figure` | S22 |
| [figured-bass](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/figured-bass/) | `group music-data` | S22 |
| [fingering](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fingering/) | `type frame-note`; `type technical` | S12,S22,S23 |
| [fingernails](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fingernails/) | `type technical` | S12,S23 |
| [first](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/first/) | `type swing` | S20,S26 |
| [first-fret](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/first-fret/) | `type frame` | S22 |
| [flip](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/flip/) | `type technical` | S12,S23 |
| [footnote](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/footnote/) | `group footnote` | S25 |
| [for-part](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/for-part/) | `type attributes` | S16,S20 |
| [forward](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/forward/) | `group music-data` | S06 |
| [fp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fp/) | `type dynamics` | S12,S13 |
| [frame](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/frame/) | `type harmony` | S22 |
| [frame-frets](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/frame-frets/) | `type frame` | S22 |
| [frame-note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/frame-note/) | `type frame` | S22 |
| [frame-strings](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/frame-strings/) | `type frame` | S22 |
| [fret](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fret/) | `type frame-note`; `type technical` | S12,S22,S23 |
| [function](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/function/) | `group harmony-chord` | S22 |
| [fz](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/fz/) | `type dynamics` | S12,S13 |
| [glass](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/glass/) | `type percussion` | S13,S16 |
| [glissando](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/glissando/) | `type notations` | S12 |
| [glyph](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/glyph/) | `type appearance` | S17 |
| [golpe](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/golpe/) | `type technical` | S12,S23 |
| [grace](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/grace/) | `type note` | S18 |
| [group](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group/) | `type score-part` | S03 |
| [group-abbreviation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-abbreviation/) | `type part-group` | S03 |
| [group-abbreviation-display](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-abbreviation-display/) | `type part-group` | S03 |
| [group-barline](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-barline/) | `type part-group` | S03 |
| [group-link](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-link/) | `type part-link` | S19,S25 |
| [group-name](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-name/) | `type part-group` | S03 |
| [group-name-display](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-name-display/) | `type part-group` | S03 |
| [group-symbol](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-symbol/) | `type part-group` | S03 |
| [group-time](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/group-time/) | `type part-group` | S03 |
| [grouping](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/grouping/) | `group music-data` | S25 |
| [half-muted](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/half-muted/) | `type technical` | S12,S23 |
| [hammer-on](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/hammer-on/) | `type technical` | S12,S23 |
| [handbell](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/handbell/) | `type technical` | S12,S23 |
| [harmon-closed](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/harmon-closed/) | `type harmon-mute` | S12,S23 |
| [harmon-mute](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/harmon-mute/) | `type technical` | S12,S23 |
| [harmonic](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/harmonic/) | `type technical` | S12,S23 |
| [harmony](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/harmony/) | `group music-data` | S22 |
| [harp-pedals](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/harp-pedals/) | `type direction-type` | S13 |
| [haydn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/haydn/) | `type ornaments` | S12 |
| [heel](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/heel/) | `type technical` | S12,S23 |
| [hole](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/hole/) | `type technical` | S12,S23 |
| [hole-closed](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/hole-closed/) | `type hole` | S12,S23 |
| [hole-shape](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/hole-shape/) | `type hole` | S12,S23 |
| [hole-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/hole-type/) | `type hole` | S12,S23 |
| [humming](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/humming/) | `type lyric` | S14 |
| [identification](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/identification/) | `group score-header`; `type score-part` | S03 |
| [image](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/image/) | `type direction-type` | S13 |
| [instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument/) | `type note` | S16 |
| [instrument-abbreviation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument-abbreviation/) | `type score-instrument` | S16 |
| [instrument-change](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument-change/) | `type sound` | S20,S26 |
| [instrument-link](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument-link/) | `type part-link` | S19,S25 |
| [instrument-name](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument-name/) | `type score-instrument` | S16 |
| [instrument-sound](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instrument-sound/) | `group virtual-instrument-data` | S16 |
| [instruments](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/instruments/) | `type attributes` | S07 |
| [interchangeable](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/interchangeable/) | `type time` | S07 |
| [inversion](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/inversion/) | `group harmony-chord` | S22 |
| [inverted-mordent](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/inverted-mordent/) | `type ornaments` | S12 |
| [inverted-turn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/inverted-turn/) | `type ornaments` | S12 |
| [inverted-vertical-turn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/inverted-vertical-turn/) | `type ornaments` | S12 |
| [ipa](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ipa/) | `type play` | S26 |
| [key](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/key/) | `type attributes` | S07 |
| [key-accidental](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/key-accidental/) | `group non-traditional-key` | S07 |
| [key-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/key-alter/) | `group non-traditional-key` | S07 |
| [key-octave](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/key-octave/) | `type key` | S07 |
| [key-step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/key-step/) | `group non-traditional-key` | S07 |
| [kind](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/kind/) | `group harmony-chord` | S22 |
| [laughing](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/laughing/) | `type lyric` | S14 |
| [left-divider](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/left-divider/) | `type system-dividers` | S17 |
| [left-margin](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/left-margin/) | `group left-right-margins` | S17 |
| [level](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/level/) | `group level` | S25 |
| [line](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/line/) | `group clef` | S07 |
| [line-detail](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/line-detail/) | `type staff-details` | S20,S23 |
| [line-width](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/line-width/) | `type appearance` | S17 |
| [link](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/link/) | `group music-data`; `type credit` (2) | S25 |
| [listen](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/listen/) | `type note` | S20,S26 |
| [listening](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/listening/) | `group music-data`; `type direction` | S20,S26 |
| [lyric](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/lyric/) | `type note` | S14 |
| [lyric-font](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/lyric-font/) | `type defaults` | S17 |
| [lyric-language](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/lyric-language/) | `type defaults` | S17 |
| [measure](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/measure-partwise/) | `root score-partwise`; `root score-timewise` | S01,S02,S08 |
| [measure-distance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/measure-distance/) | `type measure-layout` | S17 |
| [measure-layout](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/measure-layout/) | `type print` | S17 |
| [measure-numbering](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/measure-numbering/) | `type print` | S17 |
| [measure-repeat](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/measure-repeat/) | `type measure-style` | S24 |
| [measure-style](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/measure-style/) | `type attributes` | S24 |
| [membrane](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/membrane/) | `type percussion` | S13,S16 |
| [metal](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metal/) | `type percussion` | S13,S16 |
| [metronome](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome/) | `type direction-type` | S13 |
| [metronome-arrows](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-arrows/) | `type metronome` | S13 |
| [metronome-beam](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-beam/) | `type metronome-note` | S13 |
| [metronome-dot](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-dot/) | `type metronome-note` | S13 |
| [metronome-note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-note/) | `type metronome` (2) | S13 |
| [metronome-relation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-relation/) | `type metronome` | S13 |
| [metronome-tied](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-tied/) | `type metronome-note` | S13 |
| [metronome-tuplet](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-tuplet/) | `type metronome-note` | S13 |
| [metronome-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/metronome-type/) | `type metronome-note` | S13 |
| [mf](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/mf/) | `type dynamics` | S12,S13 |
| [midi-bank](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-bank/) | `type midi-instrument` | S26 |
| [midi-channel](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-channel/) | `type midi-instrument` | S26 |
| [midi-device](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-device/) | `type score-part`; `type sound` | S26 |
| [midi-instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-instrument/) | `type score-part`; `type sound` | S26 |
| [midi-name](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-name/) | `type midi-instrument` | S26 |
| [midi-program](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-program/) | `type midi-instrument` | S26 |
| [midi-unpitched](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/midi-unpitched/) | `type midi-instrument` | S26 |
| [millimeters](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/millimeters/) | `type scaling` | S17 |
| [miscellaneous](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/miscellaneous/) | `type identification` | S03 |
| [miscellaneous-field](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/miscellaneous-field/) | `type miscellaneous` | S03 |
| [mode](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/mode/) | `group traditional-key` | S07 |
| [mordent](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/mordent/) | `type ornaments` | S12 |
| [movement-number](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/movement-number/) | `group score-header` | S03 |
| [movement-title](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/movement-title/) | `group score-header` | S03 |
| [mp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/mp/) | `type dynamics` | S12,S13 |
| [multiple-rest](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/multiple-rest/) | `type measure-style` | S24 |
| [music-font](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/music-font/) | `type defaults` | S17 |
| [mute](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/mute/) | `type play` | S26 |
| [n](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/n/) | `type dynamics` | S12,S13 |
| [natural](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/natural/) | `type harmonic` | S12,S23 |
| [non-arpeggiate](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/non-arpeggiate/) | `type notations` | S12 |
| [normal-dot](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/normal-dot/) | `type time-modification` | S05,S11 |
| [normal-notes](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/normal-notes/) | `type time-modification` | S05,S11 |
| [normal-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/normal-type/) | `type time-modification` | S05,S11 |
| [notations](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notations/) | `type note` | S04,S05 |
| [note](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note/) | `group music-data` | S04,S05 |
| [note-size](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/note-size/) | `type appearance` | S17 |
| [notehead](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notehead/) | `type note` | S04,S17 |
| [notehead-text](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/notehead-text/) | `type note` | S04,S17 |
| [numeral](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/numeral/) | `group harmony-chord` | S20,S22 |
| [numeral-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/numeral-alter/) | `type numeral` | S20,S22 |
| [numeral-fifths](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/numeral-fifths/) | `type numeral-key` | S20,S22 |
| [numeral-key](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/numeral-key/) | `type numeral` | S20,S22 |
| [numeral-mode](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/numeral-mode/) | `type numeral-key` | S20,S22 |
| [numeral-root](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/numeral-root/) | `type numeral` | S20,S22 |
| [octave](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/octave/) | `type pitch` | S04 |
| [octave-change](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/octave-change/) | `group transpose` | S16 |
| [octave-shift](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/octave-shift/) | `type direction-type` | S13 |
| [offset](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/offset/) | `type direction`; `type harmony`; `type listening`; `type sound` | S13,S22,S26 |
| [open](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/open/) | `type technical` | S12,S23 |
| [open-string](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/open-string/) | `type technical` | S12,S23 |
| [opus](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/opus-reference/) | `type work` | S27 |
| [ornaments](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ornaments/) | `type notations` | S12 |
| [other-appearance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-appearance/) | `type appearance` | S17 |
| [other-articulation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-articulation/) | `type articulations` | S12 |
| [other-direction](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-direction/) | `type direction-type` | S13 |
| [other-dynamics](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-dynamics/) | `type dynamics` | S12,S13 |
| [other-listen](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-listen/) | `type listen` | S20,S26 |
| [other-listening](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-listening/) | `type listening` | S20,S26 |
| [other-notation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-notation/) | `type notations` | S12 |
| [other-ornament](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-ornament/) | `type ornaments` | S12 |
| [other-percussion](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-percussion/) | `type percussion` | S13,S16 |
| [other-play](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-play/) | `type play` | S26 |
| [other-technical](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/other-technical/) | `type technical` | S12,S23 |
| [p](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/p/) | `type dynamics` | S12,S13 |
| [page-height](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/page-height/) | `type page-layout` | S17 |
| [page-layout](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/page-layout/) | `group layout` | S17 |
| [page-margins](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/page-margins/) | `type page-layout` | S17 |
| [page-width](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/page-width/) | `type page-layout` | S17 |
| [pan](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pan/) | `type midi-instrument` | S26 |
| [part](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-partwise/) | `root score-partwise`; `root score-timewise` | S01,S02,S08 |
| [part-abbreviation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-abbreviation/) | `type score-part` | S03 |
| [part-abbreviation-display](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-abbreviation-display/) | `type print`; `type score-part` | S03,S17 |
| [part-clef](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-clef/) | `type for-part` | S16,S20 |
| [part-group](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-group/) | `group part-group` | S03 |
| [part-link](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-link/) | `type score-part` | S19,S20,S25 |
| [part-list](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-list/) | `group score-header` | S03 |
| [part-name](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-name/) | `type score-part` | S03 |
| [part-name-display](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-name-display/) | `type print`; `type score-part` | S03,S17 |
| [part-symbol](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-symbol/) | `type attributes` | S07 |
| [part-transpose](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/part-transpose/) | `type for-part` | S16,S20 |
| [pedal](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pedal/) | `type direction-type` | S13 |
| [pedal-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pedal-alter/) | `type pedal-tuning` | S13 |
| [pedal-step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pedal-step/) | `type pedal-tuning` | S13 |
| [pedal-tuning](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pedal-tuning/) | `type harp-pedals` | S13 |
| [per-minute](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/per-minute/) | `type metronome` | S13 |
| [percussion](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/percussion/) | `type direction-type` | S13 |
| [pf](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pf/) | `type dynamics` | S12,S13 |
| [pitch](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pitch/) | `group full-note` | S04 |
| [pitched](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pitched/) | `type percussion` | S13,S16 |
| [play](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/play/) | `type note`; `type sound` | S26 |
| [player](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/player/) | `type score-part` | S20,S26 |
| [player-name](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/player-name/) | `type player` | S26 |
| [plop](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/plop/) | `type articulations` | S12 |
| [pluck](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pluck/) | `type technical` | S12,S23 |
| [pp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pp/) | `type dynamics` | S12,S13 |
| [ppp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ppp/) | `type dynamics` | S12,S13 |
| [pppp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pppp/) | `type dynamics` | S12,S13 |
| [ppppp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/ppppp/) | `type dynamics` | S12,S13 |
| [pppppp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pppppp/) | `type dynamics` | S12,S13 |
| [pre-bend](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pre-bend/) | `type bend` | S12,S23 |
| [prefix](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/prefix/) | `type figure` | S22 |
| [principal-voice](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/principal-voice/) | `type direction-type` | S13 |
| [print](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/print/) | `group music-data` | S17 |
| [pull-off](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/pull-off/) | `type technical` | S12,S23 |
| [rehearsal](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/rehearsal/) | `type direction-type` | S13 |
| [relation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/relation/) | `type identification` | S03 |
| [release](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/release/) | `type bend` | S12,S23 |
| [repeat](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/repeat/) | `type barline` | S15 |
| [rest](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/rest/) | `group full-note` | S04 |
| [rf](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/rf/) | `type dynamics` | S12,S13 |
| [rfz](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/rfz/) | `type dynamics` | S12,S13 |
| [right-divider](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/right-divider/) | `type system-dividers` | S17 |
| [right-margin](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/right-margin/) | `group left-right-margins` | S17 |
| [rights](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/rights/) | `type identification` | S03 |
| [root](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/root/) | `group harmony-chord` | S22 |
| [root-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/root-alter/) | `type root` | S22 |
| [root-step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/root-step/) | `type root` | S22 |
| [scaling](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/scaling/) | `type defaults` | S17 |
| [schleifer](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/schleifer/) | `type ornaments` | S12 |
| [scoop](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/scoop/) | `type articulations` | S12 |
| [scordatura](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/scordatura/) | `type direction-type` | S13 |
| [score-instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-instrument/) | `type score-part` | S16 |
| [score-part](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-part/) | `group score-part` | S03 |
| [score-partwise](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-partwise/) | `root score-partwise` | S01 |
| [score-timewise](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/score-timewise/) | `root score-timewise` | S02 |
| [second](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/second/) | `type swing` | S20,S26 |
| [segno](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/segno/) | `type barline`; `type direction-type` | S13,S15 |
| [semi-pitched](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/semi-pitched/) | `type play` | S26 |
| [senza-misura](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/senza-misura/) | `type time` | S07 |
| [sf](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sf/) | `type dynamics` | S12,S13 |
| [sffz](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sffz/) | `type dynamics` | S12,S13 |
| [sfp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sfp/) | `type dynamics` | S12,S13 |
| [sfpp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sfpp/) | `type dynamics` | S12,S13 |
| [sfz](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sfz/) | `type dynamics` | S12,S13 |
| [sfzp](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sfzp/) | `type dynamics` | S12,S13 |
| [shake](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/shake/) | `type ornaments` | S12 |
| [sign](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sign/) | `group clef` | S07 |
| [slash](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/slash/) | `type measure-style` | S24 |
| [slash-dot](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/slash-dot/) | `group slash` | S24 |
| [slash-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/slash-type/) | `group slash` | S24 |
| [slide](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/slide/) | `type notations` | S12 |
| [slur](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/slur/) | `type notations` | S09 |
| [smear](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/smear/) | `type technical` | S12,S23 |
| [snap-pizzicato](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/snap-pizzicato/) | `type technical` | S12,S23 |
| [soft-accent](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/soft-accent/) | `type articulations` | S12 |
| [software](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/software/) | `type encoding` | S03 |
| [solo](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/solo/) | `group virtual-instrument-data` | S16 |
| [sound](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sound/) | `group music-data`; `type direction` | S26 |
| [sounding-pitch](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sounding-pitch/) | `type harmonic` | S12,S23 |
| [source](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/source/) | `type identification` | S03 |
| [spiccato](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/spiccato/) | `type articulations` | S12 |
| [staccatissimo](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staccatissimo/) | `type articulations` | S12 |
| [staccato](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staccato/) | `type articulations` | S12 |
| [staff](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff/) | `group staff` | S06,S08 |
| [staff-details](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-details/) | `type attributes` | S07,S23 |
| [staff-distance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-distance/) | `type staff-layout` | S17 |
| [staff-divide](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-divide/) | `type direction-type` | S13 |
| [staff-layout](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-layout/) | `group layout` | S17 |
| [staff-lines](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-lines/) | `type staff-details` | S07,S23 |
| [staff-size](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-size/) | `type staff-details` | S07,S23 |
| [staff-tuning](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-tuning/) | `type staff-details` | S07,S23 |
| [staff-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staff-type/) | `type staff-details` | S07,S23 |
| [staves](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/staves/) | `type attributes` | S06,S08 |
| [stem](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stem/) | `type note` | S10 |
| [step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/step/) | `type pitch` | S04 |
| [stick](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stick/) | `type percussion` | S13,S16 |
| [stick-location](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stick-location/) | `type percussion` | S13,S16 |
| [stick-material](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stick-material/) | `type stick` | S13,S16 |
| [stick-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stick-type/) | `type stick` | S13,S16 |
| [stopped](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stopped/) | `type technical` | S12,S23 |
| [straight](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/straight/) | `type swing` | S20,S26 |
| [stress](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/stress/) | `type articulations` | S12 |
| [string](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/string/) | `type frame-note`; `type technical` | S12,S22,S23 |
| [string-mute](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/string-mute/) | `type direction-type` | S13 |
| [strong-accent](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/strong-accent/) | `type articulations` | S12 |
| [suffix](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/suffix/) | `type figure` | S22 |
| [supports](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/supports/) | `type encoding` | S03 |
| [swing](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/swing/) | `type sound` | S20,S26 |
| [swing-style](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/swing-style/) | `type swing` | S20,S26 |
| [swing-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/swing-type/) | `type swing` | S20,S26 |
| [syllabic](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/syllabic/) | `type lyric` (2) | S14 |
| [symbol](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/symbol/) | `type direction-type` | S13 |
| [sync](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/sync/) | `type listening` | S20,S26 |
| [system-distance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/system-distance/) | `type system-layout` | S17 |
| [system-dividers](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/system-dividers/) | `type system-layout` | S17 |
| [system-layout](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/system-layout/) | `group layout` | S17 |
| [system-margins](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/system-margins/) | `type system-layout` | S17 |
| [tap](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tap/) | `type technical` | S12,S23 |
| [technical](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/technical/) | `type notations` | S12,S23 |
| [tenths](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tenths/) | `type scaling` | S17 |
| [tenuto](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tenuto/) | `type articulations` | S12 |
| [text](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/text/) | `type lyric` (2) | S14 |
| [thumb-position](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/thumb-position/) | `type technical` | S12,S23 |
| [tie](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tie/) | `type note` (2) | S09 |
| [tied](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tied/) | `type notations` | S09 |
| [time](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/time/) | `type attributes` | S07 |
| [time-modification](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/time-modification/) | `type note` | S11 |
| [time-relation](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/time-relation/) | `type interchangeable` | S07 |
| [timpani](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/timpani/) | `type percussion` | S13,S16 |
| [toe](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/toe/) | `type technical` | S12,S23 |
| [top-margin](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/top-margin/) | `group all-margins` | S17 |
| [top-system-distance](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/top-system-distance/) | `type system-layout` | S17 |
| [touching-pitch](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/touching-pitch/) | `type harmonic` | S12,S23 |
| [transpose](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/transpose/) | `type attributes` | S16 |
| [tremolo](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tremolo/) | `type ornaments` | S12 |
| [trill-mark](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/trill-mark/) | `type ornaments` | S12 |
| [triple-tongue](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/triple-tongue/) | `type technical` | S12,S23 |
| [tuning-alter](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuning-alter/) | `group tuning` | S07,S23 |
| [tuning-octave](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuning-octave/) | `group tuning` | S07,S23 |
| [tuning-step](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuning-step/) | `group tuning` | S07,S23 |
| [tuplet](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuplet/) | `type notations` | S11 |
| [tuplet-actual](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuplet-actual/) | `type tuplet` | S05,S11 |
| [tuplet-dot](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuplet-dot/) | `type tuplet-portion` | S05,S11 |
| [tuplet-normal](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuplet-normal/) | `type tuplet` | S05,S11 |
| [tuplet-number](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuplet-number/) | `type tuplet-portion` | S05,S11 |
| [tuplet-type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/tuplet-type/) | `type tuplet-portion` | S05,S11 |
| [turn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/turn/) | `type ornaments` | S12 |
| [type](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/type/) | `type note` | S05 |
| [unpitched](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/unpitched/) | `group full-note` | S04 |
| [unstress](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/unstress/) | `type articulations` | S12 |
| [up-bow](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/up-bow/) | `type technical` | S12,S23 |
| [vertical-turn](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/vertical-turn/) | `type ornaments` | S12 |
| [virtual-instrument](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/virtual-instrument/) | `group virtual-instrument-data` | S16 |
| [virtual-library](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/virtual-library/) | `type virtual-instrument` | S16 |
| [virtual-name](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/virtual-name/) | `type virtual-instrument` | S16 |
| [voice](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/voice/) | `group voice` | S06 |
| [volume](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/volume/) | `type midi-instrument` | S26 |
| [wait](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/wait/) | `type listen` | S20,S26 |
| [wavy-line](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/wavy-line/) | `type barline`; `type ornaments` | S12,S15 |
| [wedge](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/wedge/) | `type direction-type` | S13 |
| [with-bar](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/with-bar/) | `type bend` | S12,S23 |
| [wood](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/wood/) | `type percussion` | S13,S16 |
| [word-font](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/word-font/) | `type defaults` | S17 |
| [words](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/words/) | `type direction-type` | S13 |
| [work](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/work/) | `group score-header` | S03 |
| [work-number](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/work-number/) | `type work` | S03 |
| [work-title](https://www.w3.org/2021/06/musicxml40/musicxml-reference/elements/work-title/) | `type work` | S03 |

## container.xsd

| Element | Declaration contexts | Related coverage |
|---|---|---|
| [container](https://www.w3.org/2021/06/musicxml40/container-reference/elements/container/) | `root container` | S19 |
| [rootfile](https://www.w3.org/2021/06/musicxml40/container-reference/elements/rootfile/) | `type rootfiles` | S19 |
| [rootfiles](https://www.w3.org/2021/06/musicxml40/container-reference/elements/rootfiles/) | `type container` | S19 |

## opus.xsd

| Element | Declaration contexts | Related coverage |
|---|---|---|
| [opus](https://www.w3.org/2021/06/musicxml40/opus-reference/elements/opus/) | `root opus`; `type opus` | S27 |
| [opus-link](https://www.w3.org/2021/06/musicxml40/opus-reference/elements/opus-link/) | `type opus` | S27 |
| [score](https://www.w3.org/2021/06/musicxml40/opus-reference/elements/score/) | `type opus` | S27 |
| [title](https://www.w3.org/2021/06/musicxml40/opus-reference/elements/title/) | `type opus` | S27 |

## sounds.xsd

| Element | Declaration contexts | Related coverage |
|---|---|---|
| [any](https://www.w3.org/2021/06/musicxml40/sounds-reference/elements/any/) | `type sound` | S27 |
| [ensemble](https://www.w3.org/2021/06/musicxml40/sounds-reference/elements/ensemble/) | `type sound` | S27 |
| [solo](https://www.w3.org/2021/06/musicxml40/sounds-reference/elements/solo/) | `type sound` | S27 |
| [sound](https://www.w3.org/2021/06/musicxml40/sounds-reference/elements/sound/) | `type sounds` | S27 |
| [sounds](https://www.w3.org/2021/06/musicxml40/sounds-reference/elements/sounds/) | `root sounds` | S27 |
