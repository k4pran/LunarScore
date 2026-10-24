package com.ryanjames.sheetmusic.musicxml

/**
 * Original F01 source, authored for this repository from the MusicXML 4.0 note/duration reference.
 * Expected XSD result: valid (checked by `XsdValidationTest`). One part in 4/4 with divisions=4:
 * a C4 quarter at 0, then a quarter rest at 1.
 */
internal val f01QuarterNoteAndRest: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>4</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>quarter</type></note>
          <note><rest/><duration>4</duration><voice>1</voice><type>quarter</type></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original F03 basic source. Expected XSD result: valid (checked by `XsdValidationTest`).
 * F4 is sounded sharp by alter while a natural is printed; both facts are kept independently.
 */
internal val f03AlterWithPrintedNatural: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes><divisions>1</divisions></attributes>
          <note>
            <pitch><step>F</step><alter>1</alter><octave>4</octave></pitch>
            <duration>1</duration><type>quarter</type><accidental cautionary="yes">natural</accidental>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original F04 source. Expected XSD result: valid (checked by `XsdValidationTest`). Divisions
 * change 4→8 mid-measure: the following duration=4 starts at 1 and lasts 1/2 quarter. The new
 * divisions persist into the next measure.
 */
internal val f04DivisionsChange: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes><divisions>4</divisions></attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><type>quarter</type></note>
          <attributes><divisions>8</divisions></attributes>
          <note><pitch><step>D</step><octave>4</octave></pitch><duration>4</duration><type>eighth</type></note>
          <note><pitch><step>E</step><octave>4</octave></pitch><duration>12</duration><type>quarter</type><dot/></note>
        </measure>
        <measure number="2">
          <note><rest measure="yes"/><duration>32</duration></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
