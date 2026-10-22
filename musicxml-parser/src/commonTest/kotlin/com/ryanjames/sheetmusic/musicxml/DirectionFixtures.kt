package com.ryanjames.sheetmusic.musicxml

/**
 * Original F13 source, authored for this repository from the MusicXML 4.0 notations,
 * articulations, ornaments and dynamics references. Expected XSD result: valid (not validated by
 * these tests). In 4/4 with divisions=2: a C5 quarter with a staccato above and an f below, a D5
 * quarter with a trill mark, accent and tenuto, and an E5 half with an inverted mordent, an sfp
 * written as sf then p, and a nonstandard "pocof" dynamic.
 */
internal val f13NoteExpressions: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Violin</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note>
            <pitch><step>C</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type>
            <notations>
              <articulations><staccato placement="above"/></articulations>
              <dynamics placement="below"><f/></dynamics>
            </notations>
          </note>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type>
            <notations>
              <ornaments><trill-mark placement="above"/></ornaments>
              <articulations><accent/><tenuto placement="below"/></articulations>
            </notations>
          </note>
          <note>
            <pitch><step>E</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>half</type>
            <notations>
              <ornaments><inverted-mordent/></ornaments>
              <dynamics><sf/><p/></dynamics>
              <dynamics><other-dynamics>pocof</other-dynamics></dynamics>
            </notations>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original F14 source, authored for this repository from the MusicXML 4.0 direction, metronome,
 * words, wedge, offset and sound references. Expected XSD result: valid (not validated by these
 * tests). In 4/4 with divisions=2, measure 1 opens with a directive "Allegro" heading and a
 * quarter = 120 metronome mark whose sound tempo is 120, then a p. A crescendo starts at the second
 * note, displayed half a quarter later through a visual-only offset of 1 division. In measure 2 the
 * hairpin stops with an f whose sound dynamics apply half a quarter early (offset -1 with
 * sound="yes"), and "rit." follows the whole note at the end of the measure.
 */
internal val f14Directions: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Violin</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <direction placement="above" directive="yes">
            <direction-type><words>Allegro</words></direction-type>
            <direction-type>
              <metronome><beat-unit>quarter</beat-unit><per-minute>120</per-minute></metronome>
            </direction-type>
            <sound tempo="120"/>
          </direction>
          <direction placement="below">
            <direction-type><dynamics><p/></dynamics></direction-type>
            <staff>1</staff>
          </direction>
          <note>
            <pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type>
          </note>
          <direction placement="below">
            <direction-type><wedge type="crescendo"/></direction-type>
            <offset>1</offset>
            <voice>1</voice>
          </direction>
          <note>
            <pitch><step>D</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type>
          </note>
          <note>
            <pitch><step>E</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>half</type>
          </note>
        </measure>
        <measure number="2">
          <direction placement="below">
            <direction-type><wedge type="stop"/></direction-type>
            <direction-type><dynamics><f/></dynamics></direction-type>
            <offset sound="yes">-1</offset>
            <voice>1</voice>
            <sound dynamics="106.67"/>
          </direction>
          <note>
            <pitch><step>F</step><octave>4</octave></pitch><duration>8</duration><voice>1</voice><type>whole</type>
          </note>
          <direction>
            <direction-type><words>rit.</words></direction-type>
          </direction>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
