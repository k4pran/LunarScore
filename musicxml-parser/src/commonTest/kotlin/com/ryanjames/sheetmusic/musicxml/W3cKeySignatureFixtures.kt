package com.ryanjames.sheetmusic.musicxml

// MX-26 key-signature files from the W3C MusicXML test suite (https://github.com/w3c-cg/musicxmlTestSuite),
// commit 77c19f7e819154c70ca1a1992e80dcda8ff82fea, xmlFiles/, unmodified apart from line endings. Same
// licence and rules as W3cSuiteFixtures.kt: do not edit the embedded XML.

internal val w3c13aKeySignatures: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Various key signatures: from
          11 flats to 11 sharps.  Each signature is shown twice, with one
          measure in major and the other measure in minor.</miscellaneous-field>
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
          <fifths>-11</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>2</beats>
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
        <type>half</type>
      </note>
    </measure>
    <measure number="2">
      <attributes>
        <key>
          <fifths>-11</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="3">
      <attributes>
        <key>
          <fifths>-10</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="4">
      <attributes>
        <key>
          <fifths>-10</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="5">
      <attributes>
        <key>
          <fifths>-9</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="6">
      <attributes>
        <key>
          <fifths>-9</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="7">
      <attributes>
        <key>
          <fifths>-8</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="8">
      <attributes>
        <key>
          <fifths>-8</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="9">
      <attributes>
        <key>
          <fifths>-7</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="10">
      <attributes>
        <key>
          <fifths>-7</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="11">
      <attributes>
        <key>
          <fifths>-6</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="12">
      <attributes>
        <key>
          <fifths>-6</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="13">
      <attributes>
        <key>
          <fifths>-5</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="14">
      <attributes>
        <key>
          <fifths>-5</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="15">
      <attributes>
        <key>
          <fifths>-4</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="16">
      <attributes>
        <key>
          <fifths>-4</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="17">
      <attributes>
        <key>
          <fifths>-3</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="18">
      <attributes>
        <key>
          <fifths>-3</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="19">
      <attributes>
        <key>
          <fifths>-2</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="20">
      <attributes>
        <key>
          <fifths>-2</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="21">
      <attributes>
        <key>
          <fifths>-1</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="22">
      <attributes>
        <key>
          <fifths>-1</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="23">
      <attributes>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="24">
      <attributes>
        <key>
          <fifths>0</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="25">
      <attributes>
        <key>
          <fifths>1</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="26">
      <attributes>
        <key>
          <fifths>1</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="27">
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="28">
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="29">
      <attributes>
        <key>
          <fifths>3</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="30">
      <attributes>
        <key>
          <fifths>3</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="31">
      <attributes>
        <key>
          <fifths>4</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="32">
      <attributes>
        <key>
          <fifths>4</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="33">
      <attributes>
        <key>
          <fifths>5</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="34">
      <attributes>
        <key>
          <fifths>5</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="35">
      <attributes>
        <key>
          <fifths>6</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="36">
      <attributes>
        <key>
          <fifths>6</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="37">
      <attributes>
        <key>
          <fifths>7</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="38">
      <attributes>
        <key>
          <fifths>7</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="39">
      <attributes>
        <key>
          <fifths>8</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="40">
      <attributes>
        <key>
          <fifths>8</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="41">
      <attributes>
        <key>
          <fifths>9</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="42">
      <attributes>
        <key>
          <fifths>9</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="43">
      <attributes>
        <key>
          <fifths>10</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="44">
      <attributes>
        <key>
          <fifths>10</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="45">
      <attributes>
        <key>
          <fifths>11</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="46">
      <attributes>
        <key>
          <fifths>11</fifths>
          <mode>minor</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c13bKeySignaturesChurchModes: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">All different modes: ‘major’,
          ‘minor’, ‘ionian’, ‘dorian’, ‘phrygian’, ‘lydian’, ‘mixolydian’,
          ‘aeolian’, ‘locrian’, and ‘none’.  All modes are given with two
          sharps.</miscellaneous-field>
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
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>major</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>minor</mode>
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
        <lyric number="1"><text>minor</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>ionian</mode>
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
        <lyric number="1"><text>ionian</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>dorian</mode>
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
        <lyric number="1"><text>dorian</text></lyric>
      </note>
    </measure>
    <measure number="2">
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>phrygian</mode>
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
        <lyric number="1"><text>phrygian</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>lydian</mode>
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
        <lyric number="1"><text>lydian</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>mixolydian</mode>
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
        <lyric number="1"><text>mixolydian</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>aeolian</mode>
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
        <lyric number="1"><text>aeolian</text></lyric>
      </note>
    </measure>
    <measure number="3">
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>locrian</mode>
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
        <lyric number="1"><text>locrian</text></lyric>
      </note>
      <attributes>
        <key>
          <fifths>2</fifths>
          <mode>none</mode>
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
        <lyric number="1"><text>none</text></lyric>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c13cKeySignaturesNonTraditional: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Non-traditional key signatures,
          where each alteration is separately given.  The first signature is
          [f sharp, a flat, b flat].  The second one is [c flat-flat,
          g sharp-sharp, d flat, b sharp, f natural], with explicitly selected
          octaves for each alteration.</miscellaneous-field>
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
          <key-step>F</key-step>
          <key-alter>1</key-alter>
          <key-step>A</key-step>
          <key-alter>-1</key-alter>
          <key-step>B</key-step>
          <key-alter>-1</key-alter>
        </key>
        <time>
          <beats>2</beats>
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
        <type>half</type>
      </note>
    </measure>
    <measure number="2">
      <attributes>
        <key>
          <key-step>C</key-step>
          <key-alter>-2</key-alter>
          <key-step>G</key-step>
          <key-alter>2</key-alter>
          <key-step>D</key-step>
          <key-alter>-1</key-alter>
          <key-step>B</key-step>
          <key-alter>1</key-alter>
          <key-step>F</key-step>
          <key-alter>0</key-alter>
          <key-octave number="1">2</key-octave>
          <key-octave number="2">3</key-octave>
          <key-octave number="3">4</key-octave>
          <key-octave number="4">5</key-octave>
          <key-octave number="5">6</key-octave>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c13dKeySignaturesMicrotones: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Non-traditional key signatures
          with microtone alterations: [g flat-and-a-half, a flat, b half-flat,
          c natural, d half-sharp, e sharp,
          f sharp-and-a-half].</miscellaneous-field>
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
          <key-step>G</key-step>
          <key-alter>-1.5</key-alter>
          <key-step>A</key-step>
          <key-alter>-1</key-alter>
          <key-step>B</key-step>
          <key-alter>-0.5</key-alter>
          <key-step>C</key-step>
          <key-alter>0</key-alter>
          <key-step>D</key-step>
          <key-alter>0.5</key-alter>
          <key-step>E</key-step>
          <key-alter>1</key-alter>
          <key-step>F</key-step>
          <key-alter>1.5</key-alter>
        </key>
        <time>
          <beats>2</beats>
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
        <type>half</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c13eKeySignaturesCancel: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Tests of key signature
          cancellation: in measure 2 it is positioned at the default location
          (which usually means that the cancellation comes immediately after the
          bar line), in measure 3 it uses the attribute ‘location="right"’
          (i.e., the cancellation comes after the new key signature), in
          measure 4 it uses ‘location="before-barline"’, and in measure 5 it
          uses the attribute ‘location="left"’ (the cancellation comes before
          the new key signature).

          The &lt;cancel&gt; element's value (4) in the last measure
          intentionally doesn't correspond to the value of the previous
          &lt;fifths&gt; value (2) for testing purposes.</miscellaneous-field>
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
          <fifths>3</fifths>
          <mode>major</mode>
        </key>
        <time>
          <beats>2</beats>
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
        <type>half</type>
      </note>
    </measure>
    <measure number="2">
      <attributes>
        <key>
          <cancel>3</cancel>
          <fifths>-5</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="3">
      <attributes>
        <key>
	  <cancel location="right">-5</cancel>
          <fifths>-3</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="4">
      <attributes>
        <key>
	  <cancel location="before-barline">-3</cancel>
          <fifths>2</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
    </measure>
    <measure number="5">
      <attributes>
        <key>
	  <cancel location="left">4</cancel>
          <fifths>-2</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""
