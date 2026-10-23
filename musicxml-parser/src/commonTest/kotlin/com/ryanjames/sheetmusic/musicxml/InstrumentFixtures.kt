package com.ryanjames.sheetmusic.musicxml

/**
 * Original F17 source, authored for this repository from the MusicXML 4.0 score-instrument,
 * instrument, transpose, unpitched, midi-instrument and instrument-change references. Expected XSD
 * result: valid (not validated by these tests). In 4/4 with divisions=1, part P1 is a B-flat
 * clarinet doubling alto saxophone: measure 1 has written D5 and E4 halves for the clarinet
 * (transposing down a major second, so sounding C5 and D4); measure 2 has a written B4 half for the
 * clarinet, then at beat 3 a transpose to the alto saxophone (down a major sixth), a "To Alto Sax."
 * direction with an instrument change, and a written A4 half for the saxophone (sounding C4).
 * Part P2 is unpitched percussion with MIDI keys: a bass drum quarter on F4, two tied snare
 * quarters on C5 and a quarter rest, then a whole-measure rest.
 */
internal val f17InstrumentsAndTransposition: String = """
    <score-partwise version="4.0">
      <part-list>
        <score-part id="P1">
          <part-name>Clarinet</part-name>
          <score-instrument id="P1-I1"><instrument-name>B-flat Clarinet</instrument-name></score-instrument>
          <score-instrument id="P1-I2"><instrument-name>Alto Saxophone</instrument-name></score-instrument>
        </score-part>
        <score-part id="P2">
          <part-name>Percussion</part-name>
          <score-instrument id="P2-I36"><instrument-name>Bass Drum</instrument-name></score-instrument>
          <score-instrument id="P2-I39"><instrument-name>Snare Drum</instrument-name></score-instrument>
          <midi-instrument id="P2-I36"><midi-unpitched>36</midi-unpitched></midi-instrument>
          <midi-instrument id="P2-I39"><midi-unpitched>39</midi-unpitched></midi-instrument>
        </score-part>
      </part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <transpose><diatonic>-1</diatonic><chromatic>-2</chromatic></transpose>
          </attributes>
          <note><pitch><step>D</step><octave>5</octave></pitch><duration>2</duration><instrument id="P1-I1"/><voice>1</voice><type>half</type></note>
          <note><pitch><step>E</step><octave>4</octave></pitch><duration>2</duration><instrument id="P1-I1"/><voice>1</voice><type>half</type></note>
        </measure>
        <measure number="2">
          <note><pitch><step>B</step><octave>4</octave></pitch><duration>2</duration><instrument id="P1-I1"/><voice>1</voice><type>half</type></note>
          <attributes>
            <transpose><diatonic>-5</diatonic><chromatic>-9</chromatic></transpose>
          </attributes>
          <direction placement="above">
            <direction-type><words>To Alto Sax.</words></direction-type>
            <sound><instrument-change id="P1-I2"/></sound>
          </direction>
          <note><pitch><step>A</step><octave>4</octave></pitch><duration>2</duration><instrument id="P1-I2"/><voice>1</voice><type>half</type></note>
        </measure>
      </part>
      <part id="P2">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
            <clef><sign>percussion</sign></clef>
          </attributes>
          <note>
            <unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched>
            <duration>1</duration><instrument id="P2-I36"/><voice>1</voice><type>quarter</type>
          </note>
          <note>
            <unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched>
            <duration>1</duration><tie type="start"/><instrument id="P2-I39"/><voice>1</voice><type>quarter</type>
          </note>
          <note>
            <unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched>
            <duration>1</duration><tie type="stop"/><instrument id="P2-I39"/><voice>1</voice><type>quarter</type>
          </note>
          <note><rest/><duration>1</duration><voice>1</voice><type>quarter</type></note>
        </measure>
        <measure number="2">
          <note><rest measure="yes"/><duration>4</duration><voice>1</voice></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original concert-score subset of F21, authored from the MusicXML 4.0 concert-score, for-part and
 * transpose references. Expected XSD result: valid (not validated by these tests). The score is in
 * concert pitch: the B-flat clarinet part P1 keeps its part transposition only in `for-part` (for
 * generating a transposed part), so its written C5 sounds as written. The piccolo part P2 keeps
 * its octave transposition, which concert scores allow, so its written C5 sounds as C6.
 */
internal val f21ConcertScore: String = """
    <score-partwise version="4.0">
      <defaults><concert-score/></defaults>
      <part-list>
        <score-part id="P1">
          <part-name>Clarinet</part-name>
          <score-instrument id="P1-I1"><instrument-name>B-flat Clarinet</instrument-name></score-instrument>
        </score-part>
        <score-part id="P2">
          <part-name>Piccolo</part-name>
          <score-instrument id="P2-I1"><instrument-name>Piccolo</instrument-name></score-instrument>
        </score-part>
      </part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <for-part><part-transpose><diatonic>-1</diatonic><chromatic>-2</chromatic></part-transpose></for-part>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type></note>
        </measure>
      </part>
      <part id="P2">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <transpose><diatonic>0</diatonic><chromatic>0</chromatic><octave-change>1</octave-change></transpose>
          </attributes>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
