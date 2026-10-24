package com.ryanjames.sheetmusic.musicxml

/**
 * Original F07 source, authored for this repository from the MusicXML 4.0 attributes reference.
 * Expected XSD result: valid (checked by `XsdValidationTest`). Divisions=2. Measure 1 starts in
 * C major, 4/4, treble clef; at offset 2 it changes to G major, 2/4 and bass clef. The F3 sounding
 * sharp from the key has alter 1 and no printed accidental; the next F3 is natural with a printed
 * natural. Measure 2 inherits that state and ends with a treble clef at its end offset, which
 * measure 3 inherits.
 */
internal val f07MidMeasureAttributeChanges: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <key><fifths>0</fifths><mode>major</mode></key>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><type>quarter</type></note>
          <note><pitch><step>D</step><octave>4</octave></pitch><duration>2</duration><type>quarter</type></note>
          <attributes>
            <key><fifths>1</fifths><mode>major</mode></key>
            <time><beats>2</beats><beat-type>4</beat-type></time>
            <clef><sign>F</sign><line>4</line></clef>
          </attributes>
          <note><pitch><step>F</step><alter>1</alter><octave>3</octave></pitch><duration>2</duration><type>quarter</type></note>
          <note>
            <pitch><step>F</step><octave>3</octave></pitch><duration>2</duration><type>quarter</type>
            <accidental>natural</accidental>
          </note>
        </measure>
        <measure number="2">
          <note><pitch><step>G</step><octave>3</octave></pitch><duration>4</duration><type>half</type></note>
          <attributes><clef><sign>G</sign><line>2</line></clef></attributes>
        </measure>
        <measure number="3">
          <note><pitch><step>B</step><octave>4</octave></pitch><duration>4</duration><type>half</type></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
