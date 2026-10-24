package com.ryanjames.sheetmusic.musicxml

// Files from the W3C MusicXML test suite (https://github.com/w3c-cg/musicxmlTestSuite), commit
// 77c19f7e819154c70ca1a1992e80dcda8ff82fea, xmlFiles/, unmodified apart from line endings. MIT
// License: Copyright (c) 2010-2016 Reinhold Kainhofer and the GNU LilyPond project; Copyright (c)
// 2016-2026 Michael Scott Asato Cuthbert. The full licence text is in THIRD_PARTY_NOTICES.md and each
// file is listed in docs/musicxml/fixture-manifest.md. Do not edit the embedded XML: its expected
// musical facts come from the suite's own descriptions, not from this importer.

/** Independent MX-04 fixtures, keyed by suite file name, with the test-plan fixture each one covers. */
internal val w3cSuiteFixtures: List<W3cSuiteFixture> by lazy {
    listOf(
        W3cSuiteFixture("03a-Rhythm-Durations.musicxml", "F01", w3c03aRhythmDurations),
        W3cSuiteFixture("41a-MultiParts-Partorder.musicxml", "F02", w3c41aMultiPartsPartorder),
        W3cSuiteFixture("51b-Header-Quotes.musicxml", "F02", w3c51bHeaderQuotes),
        W3cSuiteFixture("01d-Pitches-Microtones.musicxml", "F03", w3c01dPitchesMicrotones),
        W3cSuiteFixture("03c-Rhythm-DivisionChange.musicxml", "F04", w3c03cRhythmDivisionChange),
        W3cSuiteFixture("03e-Rhythm-No-Divisions.musicxml", "F04", w3c03eRhythmNoDivisions),
        W3cSuiteFixture("03b-Rhythm-Backup.musicxml", "F05", w3c03bRhythmBackup),
        W3cSuiteFixture("03f-Rhythm-Forward.musicxml", "F05", w3c03fRhythmForward),
        W3cSuiteFixture("21c-Chords-ThreeNotesDuration.musicxml", "F06", w3c21cChordsThreeNotesDuration),
        W3cSuiteFixture("46c-Midmeasure-Clef.musicxml", "F07", w3c46cMidmeasureClef),
        W3cSuiteFixture("13e-KeySignatures-MidMeasure-Change.musicxml", "F07", w3c13eKeySignaturesMidMeasureChange),
        W3cSuiteFixture("43b-MultiStaff-DifferentKeys.musicxml", "F08", w3c43bMultiStaffDifferentKeys),
        // MX-25 time signatures, embedded in W3cTimeSignatureFixtures.kt.
        W3cSuiteFixture("11c-TimeSignatures-Complex.musicxml", "F07", w3c11cTimeSignaturesComplex),
        W3cSuiteFixture("11c-TimeSignatures-CompoundSimple.musicxml", "F07", w3c11cTimeSignaturesCompoundSimple),
        W3cSuiteFixture("11d-TimeSignatures-ComplexMultiple.musicxml", "F07", w3c11dTimeSignaturesComplexMultiple),
        W3cSuiteFixture("11d-TimeSignatures-CompoundMultiple.musicxml", "F07", w3c11dTimeSignaturesCompoundMultiple),
        W3cSuiteFixture("11e-TimeSignatures-ComplexMixed.musicxml", "F07", w3c11eTimeSignaturesComplexMixed),
        W3cSuiteFixture("11e-TimeSignatures-CompoundMixed.musicxml", "F07", w3c11eTimeSignaturesCompoundMixed),
        W3cSuiteFixture("11f-TimeSignatures-SymbolMeaning.musicxml", "F07", w3c11fTimeSignaturesSymbolMeaning),
        W3cSuiteFixture("11g-TimeSignatures-SingleNumber.musicxml", "F07", w3c11gTimeSignaturesSingleNumber),
        W3cSuiteFixture("11h-TimeSignatures-SenzaMisura.musicxml", "F07", w3c11hTimeSignaturesSenzaMisura),
        W3cSuiteFixture("11i-TimeSignatures-Alternate.musicxml", "F07", w3c11iTimeSignaturesAlternate),
        // MX-26 key signatures, embedded in W3cKeySignatureFixtures.kt.
        W3cSuiteFixture("13a-KeySignatures.musicxml", "F07", w3c13aKeySignatures),
        W3cSuiteFixture("13b-KeySignatures-ChurchModes.musicxml", "F07", w3c13bKeySignaturesChurchModes),
        W3cSuiteFixture("13c-KeySignatures-NonTraditional.musicxml", "F07", w3c13cKeySignaturesNonTraditional),
        W3cSuiteFixture("13d-KeySignatures-Microtones.musicxml", "F07", w3c13dKeySignaturesMicrotones),
        W3cSuiteFixture("13e-KeySignatures-Cancel.musicxml", "F07", w3c13eKeySignaturesCancel),
    )
}

/**
 * MX-24 structure-recovery files. They are not schema-valid (41g's part has no `id` and 41h's extra
 * parts reference undeclared IDs), so they are kept out of [w3cSuiteFixtures].
 */
internal val w3cStructureRecoveryFixtures: List<W3cSuiteFixture> by lazy {
    listOf(
        W3cSuiteFixture("41g-PartNoId.invalid.musicxml", "F02", w3c41gPartNoId),
        W3cSuiteFixture("41h-TooManyParts.musicxml", "F02", w3c41hTooManyParts),
    )
}

/** One embedded suite [file] and the test-plan fixture ID ([covers]) it stands in for. */
internal class W3cSuiteFixture(val file: String, val covers: String, val xml: String)

internal val w3c03aRhythmDurations: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">All note durations, from maxima,
          long, brevis, whole, etc., until 1024th.  First with their plain
          values, then dotted, and finally
          double-dotted.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1024</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time>
          <beats>16</beats>
          <beat-type>2</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>32768</duration>
        <voice>1</voice>
        <type>maxima</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>16384</duration>
        <voice>1</voice>
        <type>long</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>8192</duration>
        <voice>1</voice>
        <type>breve</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4096</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>2048</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1024</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>512</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>256</duration>
        <voice>1</voice>
        <type>16th</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>128</duration>
        <voice>1</voice>
        <type>32nd</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>64</duration>
        <voice>1</voice>
        <type>64th</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>32</duration>
        <voice>1</voice>
        <type>128th</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>16</duration>
        <voice>1</voice>
        <type>256th</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>512th</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>1024th</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>1024th</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <attributes>
        <time>
          <beats>24</beats>
          <beat-type>2</beat-type>
        </time>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>49152</duration>
        <voice>1</voice>
        <type>maxima</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>24576</duration>
        <voice>1</voice>
        <type>long</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>12288</duration>
        <voice>1</voice>
        <type>breve</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6144</duration>
        <voice>1</voice>
        <type>whole</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>3072</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1536</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>768</duration>
        <voice>1</voice>
        <type>eighth</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>384</duration>
        <voice>1</voice>
        <type>16th</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>192</duration>
        <voice>1</voice>
        <type>32nd</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>96</duration>
        <voice>1</voice>
        <type>64th</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>48</duration>
        <voice>1</voice>
        <type>128th</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>24</duration>
        <voice>1</voice>
        <type>256th</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>512th</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>1024th</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>1024th</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <attributes>
        <time>
          <beats>28</beats>
          <beat-type>2</beat-type>
        </time>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>57344</duration>
        <voice>1</voice>
        <type>maxima</type>
        <dot/>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>28672</duration>
        <voice>1</voice>
        <type>long</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>14336</duration>
        <voice>1</voice>
        <type>breve</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>7168</duration>
        <voice>1</voice>
        <type>whole</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>3584</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1792</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>896</duration>
        <voice>1</voice>
        <type>eighth</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>448</duration>
        <voice>1</voice>
        <type>16th</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>224</duration>
        <voice>1</voice>
        <type>32nd</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>112</duration>
        <voice>1</voice>
        <type>64th</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>56</duration>
        <voice>1</voice>
        <type>128th</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>28</duration>
        <voice>1</voice>
        <type>256th</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>14</duration>
        <voice>1</voice>
        <type>512th</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>7</duration>
        <voice>1</voice>
        <type>1024th</type>
        <dot/>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>7</duration>
        <voice>1</voice>
        <type>1024th</type>
        <dot/>
        <dot/>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c41aMultiPartsPartorder: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A piece with four parts named
          ‘P0’, ‘P1’, ‘P2’, and ‘P3’ (in that order).</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P0">
      <part-name>Part 1</part-name>
    </score-part>
    <score-part id="P1">
      <part-name>Part 2</part-name>
    </score-part>
    <score-part id="P2">
      <part-name>Part 3</part-name>
    </score-part>
    <score-part id="P3">
      <part-name>Part 4</part-name>
    </score-part>
  </part-list>
  <part id="P0">
    <measure number="1">
      <attributes>
        <divisions>960</divisions>
        <key>
        <fifths>1</fifths>
        <mode>major</mode>
        </key>
        <time>
        <beats>4</beats>
        <beat-type>4</beat-type>
        </time>
        <clef>
        <sign>G</sign>
        <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
  </part>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>960</divisions>
        <key>
        <fifths>1</fifths>
        <mode>major</mode>
        </key>
        <time>
        <beats>4</beats>
        <beat-type>4</beat-type>
        </time>
        <clef>
        <sign>G</sign>
        <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>E</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
  </part>
  <part id="P2">
    <measure number="1">
      <attributes>
        <divisions>960</divisions>
        <key>
        <fifths>1</fifths>
        <mode>major</mode>
        </key>
        <time>
        <beats>4</beats>
        <beat-type>4</beat-type>
        </time>
        <clef>
        <sign>G</sign>
        <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
  </part>
  <part id="P3">
    <measure number="1">
      <attributes>
        <divisions>960</divisions>
        <key>
        <fifths>1</fifths>
        <mode>major</mode>
        </key>
        <time>
        <beats>4</beats>
        <beat-type>4</beat-type>
        </time>
        <clef>
        <sign>G</sign>
        <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c51bHeaderQuotes: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <movement-title>"Quotes" in header fields</movement-title>
  <identification>
    <creator type="composer">Some "Tester" Name</creator>
    <rights>Free for anyone ("Public Domain")</rights>
    <encoding>
      <software>"Hand-crafted" MusicXML file</software>
      <encoding-date>2008-02-06</encoding-date>
    </encoding>
    <miscellaneous>
      <miscellaneous-field name="description">Header fields and part names can
          contain double quotes (").  This test checks some fields
          (&lt;movement-title&gt;, &lt;creator type="composer"&gt;,
          &lt;rights&gt;, &lt;software&gt;, &lt;part-name&gt;) whether they are
          converted or imported without problems (i.e., whether they are
          correctly escaped when converting).</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>Staff "Test"</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c01dPitchesMicrotones: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Some microtones:
          c flat-and-a-half, d half-flat, e half-sharp, f sharp-and-a-half;
          once in the lower and once in the upper region of the
          staff.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <alter>-1.5</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-0.5</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <alter>0.5</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>1.5</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <alter>-1.5</alter>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-0.5</alter>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <alter>0.5</alter>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>1.5</alter>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c03cRhythmDivisionChange: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Although uncommon, the divisions
          of a quarter note can change somewhere in the middle of a MusicXML
          file.  Here, the first half measure uses a division of 1, which then
          changes to 8 in the middle of the first measure and to 38 in the
          middle of the second measure.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <attributes>
        <divisions>8</divisions>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>16</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <attributes>
        <divisions>38</divisions>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>76</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c03bRhythmBackup: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Two voices with a &lt;backup&gt;
          element that does not jump to the beginning of the measure for voice 2
          but to the second beat.  Voice 1 covers beats 1 and 2, and voice 2
          covers beats 2 and 3.  There is no rest or note for uncovered
          beats.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name></part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>2</divisions>
        <key><fifths>0</fifths></key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <backup>
        <duration>2</duration>
      </backup>
      <note>
        <pitch>
          <step>A</step>
          <octave>3</octave>
        </pitch>
        <duration>2</duration>
        <voice>2</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>3</octave>
        </pitch>
        <duration>2</duration>
        <voice>2</voice>
        <type>quarter</type>
      </note>
    </measure>
  </part>
</score-partwise>

"""

internal val w3c03fRhythmForward: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A voice with three &lt;forward&gt;
          elements in the first measure, putting a quarter note on the second
          beat, a 16th note on the fourth beat, then filling up the measure to
          have the correct length.  There are no visible
          rests.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name></part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>4</divisions>
        <key><fifths>0</fifths></key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <forward>
        <duration>4</duration>
      </forward>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <forward>
        <duration>4</duration>
      </forward>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>16th</type>
      </note>
      <forward>
        <duration>3</duration>
      </forward>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c21cChordsThreeNotesDuration: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Some three-note
          chords, with various durations.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P0">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <part id="P0">
    <measure number="1">
      <attributes>
        <divisions>960</divisions>
        <time>
        <beats>4</beats>
        <beat-type>4</beat-type>
        </time>
        <clef>
        <sign>G</sign>
        <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>1440</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1440</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1440</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>480</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>480</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>G</step>
          <octave>5</octave>
        </pitch>
        <duration>480</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>

    <measure number="2">
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>960</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1920</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c46cMidmeasureClef: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A clef change in the middle of a
          measure, using either an implicit measure (as done for the second half
          of measure 2) with an &lt;attributes&gt; element at its beginning, or
          placing an &lt;attributes&gt; element at the middle of the measure (as
          done in measure 3).</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <barline location="right">
        <bar-style>none</bar-style>
      </barline>
    </measure>
    <!--=======================================================-->
    <measure implicit="yes" number="X1">
      <attributes>
        <clef>
          <sign>C</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <attributes>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c13eKeySignaturesMidMeasureChange: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 1.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise>
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Mid-Measure Key-Change: 2 sharps, 2 flats, 0 sharps/flats, 7 sharps, no mode.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>2</fifths>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>2s</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>-2</fifths>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>2f</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>0</fifths>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>0</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>7</fifths>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>7s</text></lyric>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c43bMultiStaffDifferentKeys: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A piano staff with different keys
          and clefs for each of its staves.  The keys and clefs for both staves
          are given at the very beginning of the measure.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>96</divisions>
        <key number="1">
          <fifths>0</fifths>
        </key>
        <key number="2">
          <fifths>2</fifths>
        </key>
        <time>
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <staves>2</staves>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
        <clef number="2">
          <sign>F</sign>
          <line>4</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>384</duration>
        <voice>1</voice>
        <type>whole</type>
        <staff>1</staff>
      </note>
      <backup>
        <duration>384</duration>
      </backup>
      <note>
        <pitch>
          <step>B</step>
          <octave>2</octave>
        </pitch>
        <duration>384</duration>
        <voice>2</voice>
        <type>whole</type>
        <staff>2</staff>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c03eRhythmNoDivisions: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">No &lt;divisions&gt;
          element.  The generally agreed default value is 1, and MusicXML
          version 4.1 will mention this explicitly.

          This test holds a single whole note.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <key>
          <fifths>0</fifths>
        </key>
        <time>
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <type>whole</type>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c41gPartNoId: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 2.0 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="2.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A part with no id attribute.
            Since this piece has only one part, it is clear which part 
            is described by the one part element.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
    </score-part>
  </part-list>
  <part>
    <measure number="1">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c41hTooManyParts: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">This piece has two more
          &lt;part&gt; elements than the &lt;part-list&gt; section contains.
          One can either convert all the parts present but not listed in the
          part list, or simply not import or ignore them.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
    </measure>
  </part>
  <part id="P3">
    <measure number="1">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
    </measure>
  </part>
  <part id="P4">
    <measure number="1">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
    </measure>
  </part>
</score-partwise>
"""
