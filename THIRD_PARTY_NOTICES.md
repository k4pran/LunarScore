# Third-party notices

LunarScore is licensed under the [MIT License](LICENSE). It includes the third-party material below,
each under its own licence. Keep these notices when copying or redistributing the material.

## Bravura music font

- **Files:** `sheetmusic-compose/src/commonMain/composeResources/font/bravura.otf` (unmodified,
  version 1.482), shipped in the `sheetmusic-compose` library.
- **Copyright:** © 2015 Steinberg Media Technologies GmbH, with Reserved Font Name "Bravura".
- **Licence:** SIL Open Font License 1.1, distributed with the font in
  [`BRAVURA-OFL.txt`](sheetmusic-compose/src/commonMain/composeResources/files/BRAVURA-OFL.txt).

## MusicXML 4.0 schemas (tests only)

- **Files:** `musicxml-parser/src/jvmTest/resources/musicxml-4.0/musicxml.xsd`, `xml.xsd` and
  `xlink.xsd`, copied byte-for-byte from
  [w3c/musicxml `799e2def`](https://github.com/w3c/musicxml/tree/799e2defb2ece0ae7bafe08dcbcac25b2c631d53/schema)
  (release v4.0). SHA-256 checksums are in
  [`docs/musicxml/element-inventory.md`](docs/musicxml/element-inventory.md) and are checked by
  `XsdValidationTest`.
- **Copyright:** `musicxml.xsd` and `xlink.xsd`: © 2004–2021 the Contributors to the MusicXML
  Specification, published by the W3C Music Notation Community Group under the
  [W3C Community Final Specification Agreement](https://www.w3.org/community/about/agreements/final/).
  `xml.xsd` is the W3C schema for the XML namespace (`http://www.w3.org/2001/xml.xsd`), as bundled
  with MusicXML; it carries no separate notice and is used under the
  [W3C Software and Document License](https://www.w3.org/copyright/software-license/).
- **Use:** test-time schema validation only; not included in any published library.

## W3C MusicXML test suite fixtures (tests only)

- **Files:** `musicxml-parser/src/commonTest/kotlin/com/ryanjames/sheetmusic/musicxml/W3cSuiteFixtures.kt` and
  `W3cTimeSignatureFixtures.kt` in the same directory, which embed unmodified files from
  [w3c-cg/musicxmlTestSuite `77c19f7e`](https://github.com/w3c-cg/musicxmlTestSuite/tree/77c19f7e819154c70ca1a1992e80dcda8ff82fea).
  Each embedded file is listed in [`docs/musicxml/fixture-manifest.md`](docs/musicxml/fixture-manifest.md).
- **Copyright:** © 2010–2016 Reinhold Kainhofer and the GNU LilyPond project; © 2016–2026 Michael
  Scott Asato Cuthbert; stewarded by the W3C Music Notation Community Group.
- **Licence:** MIT License:

```text
MIT License

Copyright (c) 2016-2026 Michael Scott Asato Cuthbert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
