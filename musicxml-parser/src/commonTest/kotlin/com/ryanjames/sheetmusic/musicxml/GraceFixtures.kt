package com.ryanjames.sheetmusic.musicxml

/**
 * Original F19 source, authored for this repository from the MusicXML 4.0 note, grace and cue
 * references. Expected XSD result: valid (checked by `XsdValidationTest`). In 4/4 with divisions=2,
 * measure 1 has a slashed G4 eighth grace note slurred to its principal A4 quarter, taking 33% of
 * the A4's time; two beamed 16th grace notes B4 and C5 before a D5 quarter; an E5 half-note cue;
 * and an F5 after-grace that ends the measure and takes 25% of the cue note's time. None of the
 * grace notes has a duration, so the quarter, quarter and half fill the measure from 0, 1 and 2.
 * Measure 2 is a whole-measure rest.
 */
internal val f19GraceAndCue: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Flute</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note>
            <grace slash="yes" steal-time-following="33"/>
            <pitch><step>G</step><octave>4</octave></pitch><voice>1</voice><type>eighth</type>
            <notations><slur type="start" number="1"/></notations>
          </note>
          <note>
            <pitch><step>A</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type>
            <notations><slur type="stop" number="1"/></notations>
          </note>
          <note>
            <grace/>
            <pitch><step>B</step><octave>4</octave></pitch><voice>1</voice><type>16th</type>
            <beam number="1">begin</beam><beam number="2">begin</beam>
          </note>
          <note>
            <grace/>
            <pitch><step>C</step><octave>5</octave></pitch><voice>1</voice><type>16th</type>
            <beam number="1">end</beam><beam number="2">end</beam>
          </note>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type>
          </note>
          <note>
            <cue/>
            <pitch><step>E</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>half</type>
          </note>
          <note>
            <grace steal-time-previous="25"/>
            <pitch><step>F</step><octave>5</octave></pitch><voice>1</voice><type>eighth</type>
          </note>
        </measure>
        <measure number="2">
          <note><rest measure="yes"/><duration>8</duration><voice>1</voice></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
