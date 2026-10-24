package com.ryanjames.sheetmusic.musicxml

/**
 * Original F16 source, authored for this repository from the MusicXML 4.0 barline, repeat and
 * ending references. Expected XSD result: valid (checked by `XsdValidationTest`). In 4/4 with
 * divisions=1, implicit measure 0 is a one-quarter pickup. Measure 1 opens a repeat with a
 * heavy-light forward-repeat barline. Measure 2 is the first ending ("1."), closed by a light-heavy
 * backward repeat. Measure 3 is the second ending, discontinued (open) at its end. Measure 4 has a
 * nonstandard dashed middle barline between two half notes and a final light-heavy barline.
 * Written order is 0, 1, 2, 3, 4; playback order is 0, 1, 2, 1, 3, 4 (the pickup is not replayed).
 */
internal val f16RepeatsAndEndings: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Oboe</part-name></score-part></part-list>
      <part id="P1">
        <measure number="0" implicit="yes">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>1</duration><type>quarter</type></note>
        </measure>
        <measure number="1">
          <barline location="left">
            <bar-style>heavy-light</bar-style>
            <repeat direction="forward"/>
          </barline>
          <note><pitch><step>D</step><octave>5</octave></pitch><duration>4</duration><type>whole</type></note>
        </measure>
        <measure number="2">
          <barline location="left">
            <ending number="1" type="start">1.</ending>
          </barline>
          <note><pitch><step>E</step><octave>5</octave></pitch><duration>4</duration><type>whole</type></note>
          <barline location="right">
            <bar-style>light-heavy</bar-style>
            <ending number="1" type="stop"/>
            <repeat direction="backward"/>
          </barline>
        </measure>
        <measure number="3">
          <barline location="left">
            <ending number="2" type="start"/>
          </barline>
          <note><pitch><step>F</step><octave>5</octave></pitch><duration>4</duration><type>whole</type></note>
          <barline location="right">
            <ending number="2" type="discontinue"/>
          </barline>
        </measure>
        <measure number="4">
          <note><pitch><step>G</step><octave>5</octave></pitch><duration>2</duration><type>half</type></note>
          <barline location="middle">
            <bar-style>dashed</bar-style>
          </barline>
          <note><pitch><step>A</step><octave>5</octave></pitch><duration>2</duration><type>half</type></note>
          <barline location="right">
            <bar-style>light-heavy</bar-style>
          </barline>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
