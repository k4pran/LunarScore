package com.ryanjames.sheetmusic.musicxml

/**
 * Original F09 source, authored for this repository from the MusicXML 4.0 beam, stem and
 * time-modification references. Expected XSD result: valid (not validated by these tests). In 2/4
 * with divisions=6, measure 1 has two beamed eighths with up stems, then a beamed 3:2 eighth triplet
 * with down stems whose notes last exactly 1/3 quarter. Measure 2 beams a dotted eighth (decimal
 * duration 4.5) to a sixteenth with a backward hook, followed by a quarter rest.
 */
internal val f09BeamsAndTriplet: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Flute</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>6</divisions>
            <time><beats>2</beats><beat-type>4</beat-type></time>
          </attributes>
          <note>
            <pitch><step>E</step><octave>4</octave></pitch><duration>3</duration><voice>1</voice><type>eighth</type>
            <stem>up</stem><beam number="1">begin</beam>
          </note>
          <note>
            <pitch><step>G</step><octave>4</octave></pitch><duration>3</duration><voice>1</voice><type>eighth</type>
            <stem>up</stem><beam number="1">end</beam>
          </note>
          <note>
            <pitch><step>A</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type>
            <time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>
            <stem>down</stem><beam number="1">begin</beam>
            <notations><tuplet type="start"/></notations>
          </note>
          <note>
            <pitch><step>B</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type>
            <time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>
            <stem>down</stem><beam number="1">continue</beam>
          </note>
          <note>
            <pitch><step>C</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type>
            <time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>
            <stem>down</stem><beam number="1">end</beam>
            <notations><tuplet type="stop"/></notations>
          </note>
        </measure>
        <measure number="2">
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>4.5</duration><voice>1</voice><type>eighth</type><dot/>
            <stem>down</stem><beam number="1">begin</beam>
          </note>
          <note>
            <pitch><step>B</step><octave>4</octave></pitch><duration>1.5</duration><voice>1</voice><type>16th</type>
            <stem>down</stem><beam number="1">end</beam><beam number="2">backward hook</beam>
          </note>
          <note><rest/><duration>6</duration><voice>1</voice><type>quarter</type></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * Original F11 source, authored for this repository from the MusicXML 4.0 tuplet and
 * time-modification references. Expected XSD result: valid (not validated by these tests). With
 * divisions=9, the implicit pickup measure 0 holds a 3:2 quarter triplet (tuplet 7) whose middle
 * quarter is a nested 3:2 eighth triplet (tuplet 8), so its eighths carry the cumulative 9:4 ratio
 * and last 2/9 quarter each. Measure 1 in 4/4 has a dotted half, then a beamed eighth triplet
 * numbered 16, the largest valid number-level.
 */
internal val f11NestedTuplets: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Violin</part-name></score-part></part-list>
      <part id="P1">
        <measure number="0" implicit="yes">
          <attributes>
            <divisions>9</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>6</duration><voice>1</voice><type>quarter</type>
            <time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>
            <notations><tuplet type="start" number="7"/></notations>
          </note>
          <note>
            <pitch><step>E</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type>
            <time-modification>
              <actual-notes>9</actual-notes><normal-notes>4</normal-notes><normal-type>eighth</normal-type>
            </time-modification>
            <beam number="1">begin</beam>
            <notations><tuplet type="start" number="8"/></notations>
          </note>
          <note>
            <pitch><step>F</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type>
            <time-modification>
              <actual-notes>9</actual-notes><normal-notes>4</normal-notes><normal-type>eighth</normal-type>
            </time-modification>
            <beam number="1">continue</beam>
          </note>
          <note>
            <pitch><step>G</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type>
            <time-modification>
              <actual-notes>9</actual-notes><normal-notes>4</normal-notes><normal-type>eighth</normal-type>
            </time-modification>
            <beam number="1">end</beam>
            <notations><tuplet type="stop" number="8"/></notations>
          </note>
          <note>
            <pitch><step>A</step><octave>5</octave></pitch><duration>6</duration><voice>1</voice><type>quarter</type>
            <time-modification><actual-notes>3</actual-notes><normal-notes>2</normal-notes></time-modification>
            <notations><tuplet type="stop" number="7"/></notations>
          </note>
        </measure>
        <measure number="1">
          <note>
            <pitch><step>B</step><octave>4</octave></pitch><duration>27</duration><voice>1</voice><type>half</type><dot/>
          </note>
          <note>
            <pitch><step>C</step><octave>5</octave></pitch><duration>3</duration><voice>1</voice><type>eighth</type>
            <time-modification>
              <actual-notes>3</actual-notes><normal-notes>2</normal-notes><normal-type>eighth</normal-type>
            </time-modification>
            <beam number="1">begin</beam>
            <notations><tuplet type="start" number="16"/></notations>
          </note>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>3</duration><voice>1</voice><type>eighth</type>
            <time-modification>
              <actual-notes>3</actual-notes><normal-notes>2</normal-notes><normal-type>eighth</normal-type>
            </time-modification>
            <beam number="1">continue</beam>
          </note>
          <note>
            <pitch><step>E</step><octave>5</octave></pitch><duration>3</duration><voice>1</voice><type>eighth</type>
            <time-modification>
              <actual-notes>3</actual-notes><normal-notes>2</normal-notes><normal-type>eighth</normal-type>
            </time-modification>
            <beam number="1">end</beam>
            <notations><tuplet type="stop" number="16"/></notations>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
