package com.ryanjames.sheetmusic.musicxml

/**
 * Original F05 source, authored from the MusicXML 4.0 backup/forward references. Expected XSD
 * result: valid (not validated by these tests). Divisions 2. Measure 1: voice 1 has two halves at
 * 0 and 2; a backup returns to 0 for a voice-2 quarter at 0, then a forward leaves a voice-2 gap
 * from 1 to 2 before a half at 2. Measure 2: a voice-1 whole rest, then voice 2 starts after a
 * forward of three quarters.
 */
internal val f05BackupAndForward: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>half</type></note>
          <note><pitch><step>D</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>half</type></note>
          <backup><duration>8</duration></backup>
          <note><pitch><step>E</step><octave>4</octave></pitch><duration>2</duration><voice>2</voice><type>quarter</type></note>
          <forward><duration>2</duration><voice>2</voice></forward>
          <note><pitch><step>F</step><octave>4</octave></pitch><duration>4</duration><voice>2</voice><type>half</type></note>
        </measure>
        <measure number="2">
          <note><rest measure="yes"/><duration>8</duration><voice>1</voice></note>
          <backup><duration>8</duration></backup>
          <forward><duration>6</duration></forward>
          <note><pitch><step>G</step><octave>4</octave></pitch><duration>2</duration><voice>2</voice><type>quarter</type></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original F06 source, authored from the MusicXML 4.0 chord reference. Expected XSD result: valid
 * (not validated by these tests). A two-staff part with divisions 2: a C4 half reference note at 0
 * on staff 1, a shorter E4 quarter and an equal G4 half chord tone, and a cross-staff C3 half chord
 * tone on staff 2. The following D4 starts at 2, the end of the reference note.
 */
internal val f06ChordsAndCrossStaff: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Piano</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <staves>2</staves>
            <clef number="1"><sign>G</sign><line>2</line></clef>
            <clef number="2"><sign>F</sign><line>4</line></clef>
          </attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>half</type><staff>1</staff></note>
          <note><chord/><pitch><step>E</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type><staff>1</staff></note>
          <note><chord/><pitch><step>G</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>half</type><staff>1</staff></note>
          <note><chord/><pitch><step>C</step><octave>3</octave></pitch><duration>4</duration><voice>1</voice><type>half</type><staff>2</staff></note>
          <note><pitch><step>D</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type><staff>1</staff></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original F08 source. Expected XSD result: valid (not validated by these tests). A one-staff
 * flute and a two-staff piano with different measure content. The piano's unnumbered key and time
 * apply to both staves, declared later in the same attributes element; its clefs are numbered.
 * Piano staff 1 (voice 1) and staff 2 (voice 5) both start at 0 via backup.
 */
internal val f08PartsAndPianoStaves: String = """
    <score-partwise version="4.0">
      <part-list>
        <score-part id="P1"><part-name>Flute</part-name></score-part>
        <score-part id="P2"><part-name>Piano</part-name></score-part>
      </part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <key><fifths>0</fifths></key>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type></note>
        </measure>
        <measure number="2">
          <note><pitch><step>B</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type></note>
          <note><pitch><step>A</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type></note>
        </measure>
      </part>
      <part id="P2">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <key><fifths>0</fifths></key>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <staves>2</staves>
            <clef number="1"><sign>G</sign><line>2</line></clef>
            <clef number="2"><sign>F</sign><line>4</line></clef>
          </attributes>
          <note><pitch><step>E</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>half</type><staff>1</staff></note>
          <note><pitch><step>G</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>half</type><staff>1</staff></note>
          <backup><duration>8</duration></backup>
          <note><pitch><step>C</step><octave>3</octave></pitch><duration>8</duration><voice>5</voice><type>whole</type><staff>2</staff></note>
        </measure>
        <measure number="2">
          <note><rest measure="yes"/><duration>8</duration><voice>1</voice><staff>1</staff></note>
          <backup><duration>8</duration></backup>
          <note><pitch><step>C</step><octave>3</octave></pitch><duration>4</duration><voice>5</voice><type>half</type><staff>2</staff></note>
          <note><pitch><step>G</step><octave>2</octave></pitch><duration>4</duration><voice>5</voice><type>half</type><staff>2</staff></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
