package com.ryanjames.sheetmusic.musicxml

/**
 * Original F12 timewise source, authored from the MusicXML 4.0 score-timewise reference. Expected
 * XSD result: valid (not validated by these tests). A flute (divisions 1) and a piano (divisions 2)
 * whose divisions carry from measure 1 into later measures. Measure 2 lists the piano before the
 * flute, and pickup measure 3 has no flute part at all.
 */
internal val f12TimewiseScore: String = """
    <score-timewise version="4.0">
      <work><work-title>Pair</work-title></work>
      <part-list>
        <score-part id="P1"><part-name>Flute</part-name></score-part>
        <score-part id="P2"><part-name>Piano</part-name></score-part>
      </part-list>
      <measure number="1">
        <part id="P1">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type></note>
        </part>
        <part id="P2">
          <attributes>
            <divisions>2</divisions>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>E</step><octave>4</octave></pitch><duration>8</duration><voice>1</voice><type>whole</type><staff>1</staff></note>
        </part>
      </measure>
      <measure number="2">
        <part id="P2">
          <note><pitch><step>G</step><octave>3</octave></pitch><duration>8</duration><voice>1</voice><type>whole</type><staff>1</staff></note>
        </part>
        <part id="P1">
          <note><pitch><step>B</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type></note>
          <note><pitch><step>A</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type></note>
        </part>
      </measure>
      <measure number="3" implicit="yes">
        <part id="P2">
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type><staff>1</staff></note>
        </part>
      </measure>
    </score-timewise>
""".trimIndent()

/** The partwise equivalent of [f12TimewiseScore]; the flute has an explicit empty measure 3. */
internal val f12PartwiseScore: String = """
    <score-partwise version="4.0">
      <work><work-title>Pair</work-title></work>
      <part-list>
        <score-part id="P1"><part-name>Flute</part-name></score-part>
        <score-part id="P2"><part-name>Piano</part-name></score-part>
      </part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type></note>
        </measure>
        <measure number="2">
          <note><pitch><step>B</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type></note>
          <note><pitch><step>A</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>half</type></note>
        </measure>
        <measure number="3" implicit="yes"/>
      </part>
      <part id="P2">
        <measure number="1">
          <attributes>
            <divisions>2</divisions>
            <clef><sign>G</sign><line>2</line></clef>
          </attributes>
          <note><pitch><step>E</step><octave>4</octave></pitch><duration>8</duration><voice>1</voice><type>whole</type><staff>1</staff></note>
        </measure>
        <measure number="2">
          <note><pitch><step>G</step><octave>3</octave></pitch><duration>8</duration><voice>1</voice><type>whole</type><staff>1</staff></note>
        </measure>
        <measure number="3" implicit="yes">
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type><staff>1</staff></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
