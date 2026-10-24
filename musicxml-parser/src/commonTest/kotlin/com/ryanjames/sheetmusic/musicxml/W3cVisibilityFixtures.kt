package com.ryanjames.sheetmusic.musicxml

// MX-27 visibility and print-attribute files from the W3C MusicXML test suite
// (https://github.com/w3c-cg/musicxmlTestSuite), commit 77c19f7e819154c70ca1a1992e80dcda8ff82fea,
// xmlFiles/, unmodified apart from line endings. Same licence and rules as W3cSuiteFixtures.kt: do not
// edit the embedded XML.

internal val w3c01ePitchesParenthesizedAccidentals: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Accidentals have the attributes
          ‘cautionary’, ‘editorial’, ‘parenthesized’, and ‘bracketed’.  The
          first two measures each have a cautionary accidental, an editorial, a
          cautionary with parentheses off, and an editorial and cautionary
          accidental.  The next two measures each have a normal accidental, a
          bracketed, a parenthesized, and a bracketed and parenthesized
          accidental.</miscellaneous-field>
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
          <step>D</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental cautionary="yes">flat</accidental>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental editorial="yes">flat</accidental>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental cautionary="yes" parentheses="no">flat</accidental>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental editorial="yes" cautionary="yes">flat</accidental>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental cautionary="yes">sharp</accidental>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental editorial="yes">sharp</accidental>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental cautionary="yes" parentheses="no">sharp</accidental>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental editorial="yes" cautionary="yes">sharp</accidental>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <note>
        <pitch>
          <step>D</step>
          <alter>-2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental>flat-flat</accidental>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental bracket="yes">flat-flat</accidental>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental parentheses="yes">flat-flat</accidental>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <alter>-2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental bracket="yes" parentheses="yes">flat-flat</accidental>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <pitch>
          <step>C</step>
          <alter>2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental>double-sharp</accidental>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <alter>2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental bracket="yes">double-sharp</accidental>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <alter>2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental parentheses="yes">double-sharp</accidental>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <alter>2</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <accidental bracket="yes" parentheses="yes">double-sharp</accidental>
      </note>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c13fKeySignaturesVisible: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Test the ‘print-object’ attribute
          of key signatures.  The signature at the beginning of the second bar
          is a flat major and should be invisible; the following notes d flat,
          e flat, a flat, and b flat shouldn't have a flat
          accidental.</miscellaneous-field>
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
          <fifths>4</fifths>
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
          <step>D</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <attributes>
        <key print-object="no">
          <fifths>-4</fifths>
          <mode>major</mode>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>D</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <alter>-1</alter>
          <octave>4</octave>
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
</score-partwise>
"""

internal val w3c34dNoteAttributes: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Testing print attribute
          combinations of &lt;note&gt;.  The third beat is a chord with the
          pitches D-F-A, all other notes have the same pitch A.

          It is expected that only a subset of the possible combinations are
          supported by applications.

          Measure 1:

          A: No attributes.

          B: print-leger="no" – the ledger line should be omitted.

          C: print-dot="no" (middle note of chord) – the middle dot of the
          chord should be omitted.

          D: print-lyric="no" – the lyric ‘D’ should be omitted.

          E: print-object="no" – everything should be omitted.

          Measure 2:

          F: print-object="no", print-leger="yes" – only the ledger line should
          be visible.

          G: print-object="no", print-dot="yes" – only the dot should be
          visible.

          H: print-object="no", print-lyric="yes" – only the lyric ‘H’ should
          be visible.

          Measure 3:

          I: print-object="no" for &lt;lyric&gt; (middle lyric line ‘I2’) – the
          lyrics ‘I2’ should be omitted.

          J: print-lyric="no", print-object="yes" for &lt;lyric&gt; (middle
          lyric line ‘J2’) – the lyrics ‘J1’ and ‘J3’ should be omitted.

          K: print-object="no" print-lyric="yes", print-object="no" for
          &lt;lyric&gt; (middle lyric line ‘K2’) – only the lyrics ‘K1’ and
          ‘K3’ should be visible.

          L: print-object="no" print-lyric="no", print-object="yes" for
          &lt;lyric&gt; (middle lyric line ‘L2’) – only the lyrics ‘L2’ should
          be visible.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1"><part-name></part-name></score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>8</divisions>
        <time>
          <beats>15</beats>
          <beat-type>8</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>

      <note>
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>A</text>
        </lyric>
      </note>

      <note print-leger="no">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>B</text>
        </lyric>
      </note>

      <note>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>C</text>
        </lyric>
      </note>
      <note print-dot="no">
        <chord/>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>

      <note print-lyric="no">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>D</text>
        </lyric>
      </note>

      <note print-object="no">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>E</text>
        </lyric>
      </note>
    </measure>

    <!--=======================================================-->
    <measure number="2">
      <attributes>
        <time>
          <beats>9</beats>
          <beat-type>8</beat-type>
        </time>
      </attributes>

      <note print-object="no" print-leger="yes">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>F</text>
        </lyric>
      </note>

      <note print-object="no" print-dot="yes">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>G</text>
        </lyric>
      </note>

      <note print-object="no" print-lyric="yes">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>H</text>
        </lyric>
      </note>
    </measure>

    <!--=======================================================-->
    <measure number="3">
      <attributes>
        <time>
          <beats>12</beats>
          <beat-type>8</beat-type>
        </time>
      </attributes>

      <note>
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>I1</text>
        </lyric>
        <lyric number="2" print-object="no">
          <text>I2</text>
        </lyric>
        <lyric number="3">
          <text>I3</text>
        </lyric>
      </note>

      <note print-lyric="no">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>J1</text>
        </lyric>
        <lyric number="2" print-object="yes">
          <text>J2</text>
        </lyric>
        <lyric number="3">
          <text>J3</text>
        </lyric>
      </note>

      <note print-object="no" print-lyric="yes">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>K1</text>
        </lyric>
        <lyric number="2" print-object="no">
          <text>K2</text>
        </lyric>
        <lyric number="3">
          <text>K3</text>
        </lyric>
      </note>

      <note print-object="no" print-lyric="no">
        <pitch>
          <step>A</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text>L1</text>
        </lyric>
        <lyric number="2" print-object="yes">
          <text>L2</text>
        </lyric>
        <lyric number="3">
          <text>L3</text>
        </lyric>
      </note>

    </measure>
  </part>
</score-partwise>
"""

internal val w3c34aPrintObjectSpanners: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Test various spanner elements
          (mostly from &lt;notations&gt;) starting from a &lt;note&gt; object
          with ‘print-object’ set to ‘no’, then test spanners ending with such
          a note object: beam, tuplet, slur, trill + wavy-line, glissando
          (wavy), slide (solid), two-note tremolo, hammer-on, pull-off.
          Spanners starting from an invisible object should be
          suppressed.</miscellaneous-field>
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
        <divisions>6</divisions>
        <key>
          <fifths>0</fifths>
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
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>3</actual-notes>
          <normal-notes>2</normal-notes>
        </time-modification>
        <notations>
          <tuplet number="1" placement="above" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>3</actual-notes>
          <normal-notes>2</normal-notes>
        </time-modification>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>3</actual-notes>
          <normal-notes>2</normal-notes>
        </time-modification>
        <notations>
          <tuplet number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>3</actual-notes>
          <normal-notes>2</normal-notes>
        </time-modification>
        <notations>
          <tuplet number="1" placement="below" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>3</actual-notes>
          <normal-notes>2</normal-notes>
        </time-modification>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>3</actual-notes>
          <normal-notes>2</normal-notes>
        </time-modification>
        <notations>
          <tuplet number="1" type="stop"/>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <note print-object="no">
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slur number="1" placement="above" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slur number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slur number="1" placement="below" type="start"/>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slur number="1" type="stop"/>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <ornaments>
            <trill-mark/>
            <wavy-line number="1" type="start"/>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <ornaments>
            <wavy-line number="1" type="stop"/>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <ornaments>
            <trill-mark/>
            <wavy-line number="1" type="start"/>
          </ornaments>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <ornaments>
            <wavy-line number="1" type="stop"/>
          </ornaments>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <note print-object="no">
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <glissando number="1" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <glissando number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <glissando number="1" type="start"/>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <glissando number="1" type="stop"/>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <note print-object="no">
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slide number="1" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slide number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slide number="1" type="start"/>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>C</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <slide number="1" type="stop"/>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="7">
      <note print-object="no">
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>half</type>
        <time-modification>
          <actual-notes>2</actual-notes>
          <normal-notes>1</normal-notes>
        </time-modification>
        <beam number="1">begin</beam>
        <notations>
          <ornaments>
            <tremolo type="start">2</tremolo>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>half</type>
        <time-modification>
          <actual-notes>2</actual-notes>
          <normal-notes>1</normal-notes>
        </time-modification>
        <beam number="1">end</beam>
        <notations>
          <ornaments>
            <tremolo type="stop">2</tremolo>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>half</type>
        <time-modification>
          <actual-notes>2</actual-notes>
          <normal-notes>1</normal-notes>
        </time-modification>
        <beam number="1">begin</beam>
        <notations>
          <ornaments>
            <tremolo type="start">2</tremolo>
          </ornaments>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>half</type>
        <time-modification>
          <actual-notes>2</actual-notes>
          <normal-notes>1</normal-notes>
        </time-modification>
        <beam number="1">end</beam>
        <notations>
          <ornaments>
            <tremolo type="stop">2</tremolo>
          </ornaments>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="8">
      <note print-object="no">
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <hammer-on type="start"/>
          </technical>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <hammer-on type="stop"/>
          </technical>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <hammer-on type="start"/>
          </technical>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <hammer-on type="stop"/>
          </technical>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="9">
      <note print-object="no">
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <pull-off type="start"/>
          </technical>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <pull-off type="stop"/>
          </technical>
        </notations>
      </note>
      <note>
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <pull-off type="start"/>
          </technical>
        </notations>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step><octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <technical>
            <pull-off type="stop"/>
          </technical>
        </notations>
      </note>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c22bStaffNotestyles: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Staff-connected note styles: slash
          notation, hidden notes (with and without hidden staff
          lines).

          The lyrics (used as descriptive text) in bars 4 and 5 are visible due
          to the ‘print-lyric="yes"’ attribute of
          &lt;note&gt;.</miscellaneous-field>
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
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>normal</text></lyric>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
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
      <note>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <attributes>
        <measure-style>
          <slash type="start" use-stems="no">
            <slash-type>quarter</slash-type>
          </slash>
        </measure-style>
      </attributes>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>slashes,</text></lyric>
        <lyric number="2"><text>no stem</text></lyric>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
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
      <note>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <attributes>
        <measure-style>
          <slash type="stop">
            <slash-type>quarter</slash-type>
          </slash>
        </measure-style>
      </attributes>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <attributes>
        <measure-style>
          <slash type="start" use-stems="yes">
            <slash-type>quarter</slash-type>
          </slash>
        </measure-style>
      </attributes>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>slashes,</text></lyric>
        <lyric number="2"><text>with stem</text></lyric>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
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
      <note>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <attributes>
        <measure-style>
          <slash type="stop">
            <slash-type>quarter</slash-type>
          </slash>
        </measure-style>
      </attributes>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <print new-system="yes"/>
      <note print-object="no" print-lyric="yes">
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>hidden</text></lyric>
        <lyric number="2"><text>notes</text></lyric>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note print-object="no">
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note print-object="no">
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <attributes>
        <staff-details>
          <staff-lines>0</staff-lines>
        </staff-details>
      </attributes>
      <note print-object="no" print-lyric="yes">
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>hidden notes,</text></lyric>
        <lyric number="2"><text>no staff lines</text></lyric>
      </note>
      <note print-object="no">
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note print-object="no">
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note print-object="no">
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <attributes>
        <staff-details>
          <staff-lines>5</staff-lines>
        </staff-details>
      </attributes>
      <note>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1"><text>normal</text></lyric>
        <lyric number="2"><text>settings</text></lyric>
        <lyric number="3"><text>restored</text></lyric>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
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
      <note>
        <pitch>
          <step>D</step>
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

internal val w3c11bTimeSignaturesNoTime: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A score without
          a time signature at the beginning (but with &lt;key&gt; and
          &lt;clef&gt; elements).  The second bar sets a 2/2 time signature
          with ‘print-object="no"’ for both staves, and the third bar sets a
          4/4 time signature with ‘print-object="no"’ only for the lower staff.

          Whether a time signature is actually displayed at the very beginning
          (if there is no &lt;time&gt; element) is
          application-dependent.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name></part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key><fifths>0</fifths></key>
        <staves>2</staves>
        <clef number="1"><sign>G</sign><line>2</line></clef>
        <clef number="2"><sign>F</sign><line>4</line></clef>
      </attributes>
      <note>
        <pitch><step>F</step><octave>4</octave></pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <staff>1</staff>
      </note>
      <backup><duration>384</duration></backup>
      <note>
        <pitch><step>B</step><octave>2</octave></pitch>
        <duration>4</duration>
        <voice>2</voice>
        <type>whole</type>
        <staff>2</staff>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <attributes>
        <time print-object="no">
          <beats>2</beats>
          <beat-type>2</beat-type>
        </time>
      </attributes>
      <note>
        <pitch><step>F</step><octave>4</octave></pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <staff>1</staff>
      </note>
      <backup><duration>384</duration></backup>
      <note>
        <pitch><step>B</step><octave>2</octave></pitch>
        <duration>4</duration>
        <voice>2</voice>
        <type>whole</type>
        <staff>2</staff>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <attributes>
        <time number="1">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <time number="2" print-object="no">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
      </attributes>
      <note>
        <pitch><step>F</step><octave>4</octave></pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <staff>1</staff>
      </note>
      <backup><duration>384</duration></backup>
      <note>
        <pitch><step>B</step><octave>2</octave></pitch>
        <duration>4</duration>
        <voice>2</voice>
        <type>whole</type>
        <staff>2</staff>
      </note>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c02dRestsMultimeasureTimeSignatures: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Multi-measure rests should always
          be converted into durations that are multiples of the time
          signature.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
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
        <measure-style>
          <multiple-rest>2</multiple-rest>
        </measure-style>
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
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <attributes>
        <time>
          <beats>3</beats>
          <beat-type>4</beat-type>
        </time>
        <measure-style>
          <multiple-rest>3</multiple-rest>
        </measure-style>
      </attributes>
      <note>
        <rest/>
        <duration>3</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <rest/>
        <duration>3</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <note>
        <rest/>
        <duration>3</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <attributes>
        <time>
          <beats>2</beats>
          <beat-type>4</beat-type>
        </time>
        <measure-style>
          <multiple-rest>2</multiple-rest>
        </measure-style>
      </attributes>
      <note>
        <rest/>
        <duration>2</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="7">
      <note>
        <rest/>
        <duration>2</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="8">
      <attributes>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <measure-style>
          <multiple-rest>2</multiple-rest>
        </measure-style>
      </attributes>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="9">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="10">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
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
