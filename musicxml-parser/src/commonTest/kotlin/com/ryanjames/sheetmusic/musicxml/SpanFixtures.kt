package com.ryanjames.sheetmusic.musicxml

/**
 * Original F10 source, authored for this repository from the MusicXML 4.0 tie, tied and slur
 * references. Expected XSD result: valid (not validated by these tests). In 4/4 with
 * divisions=1: slur 1 runs from E4 in measure 1 to G4 in measure 2 with a continue point on the
 * first G4; nested slur 2 and a sound tie plus its printed tie join the two G4s across the
 * barline; the two A4s carry a notation-only tied mark without a sound tie.
 */
internal val f10TiesAndSlurs: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note>
            <pitch><step>E</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type>
            <notations><slur type="start" number="1"/></notations>
          </note>
          <note>
            <pitch><step>G</step><octave>4</octave></pitch><duration>2</duration><tie type="start"/><voice>1</voice><type>half</type>
            <notations>
              <tied type="start"/>
              <slur type="continue" number="1"/>
              <slur type="start" number="2"/>
            </notations>
          </note>
        </measure>
        <measure number="2">
          <note>
            <pitch><step>G</step><octave>4</octave></pitch><duration>2</duration><tie type="stop"/><voice>1</voice><type>half</type>
            <notations>
              <tied type="stop"/>
              <slur type="stop" number="2"/>
              <slur type="stop" number="1"/>
            </notations>
          </note>
          <note>
            <pitch><step>A</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <notations><tied type="start"/></notations>
          </note>
          <note>
            <pitch><step>A</step><octave>4</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <notations><tied type="stop"/></notations>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
