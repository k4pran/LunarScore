package com.ryanjames.sheetmusic.musicxml

// MX-28 typography and colour files from the W3C MusicXML test suite
// (https://github.com/w3c-cg/musicxmlTestSuite), commit 77c19f7e819154c70ca1a1992e80dcda8ff82fea,
// xmlFiles/, unmodified apart from line endings. Same licence and rules as W3cSuiteFixtures.kt: do not
// edit the embedded XML.

internal val w3c03eRhythmSecondaryBeamBreaks: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 3.1 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="3.1">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A piece with four beamed scales, showing different secondary beam breaks.</miscellaneous-field>
      </miscellaneous>
    </identification>
  <part-list>
    <score-part id="P1">
        <part-name>MusicXML Part</part-name>
      </score-part>
    </part-list>
  <part id="P1">
    <measure number="1" width="1077.49">
      <print>
        <system-layout>
          <system-margins>
            <left-margin>0.00</left-margin>
            <right-margin>-0.00</right-margin>
            </system-margins>
          <top-system-distance>70.00</top-system-distance>
          </system-layout>
        </print>
      <attributes>
        <divisions>8</divisions>
        <key>
          <fifths>0</fifths>
          </key>
        <time>
          <beats>4</beats>
          <beat-type>4</beat-type>
          </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
          </clef>
        </attributes>
      <note default-x="83.07" default-y="-50.00">
        <pitch>
          <step>C</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">begin</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="114.09" default-y="-45.00">
        <pitch>
          <step>D</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="145.12" default-y="-40.00">
        <pitch>
          <step>E</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="176.15" default-y="-35.00">
        <pitch>
          <step>F</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="207.17" default-y="-30.00">
        <pitch>
          <step>G</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="238.20" default-y="-25.00">
        <pitch>
          <step>A</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="269.22" default-y="-20.00">
        <pitch>
          <step>B</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="300.25" default-y="-15.00">
        <pitch>
          <step>C</step>
          <octave>5</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">end</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="331.28" default-y="-50.00">
        <pitch>
          <step>C</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">begin</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="362.30" default-y="-45.00">
        <pitch>
          <step>D</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="393.33" default-y="-40.00">
        <pitch>
          <step>E</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="424.35" default-y="-35.00">
        <pitch>
          <step>F</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="455.38" default-y="-30.00">
        <pitch>
          <step>G</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="486.40" default-y="-25.00">
        <pitch>
          <step>A</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="517.43" default-y="-20.00">
        <pitch>
          <step>B</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="548.46" default-y="-15.00">
        <pitch>
          <step>C</step>
          <octave>5</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">end</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="579.48" default-y="-50.00">
        <pitch>
          <step>C</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">begin</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="610.51" default-y="-45.00">
        <pitch>
          <step>D</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">continue</beam>
        </note>
      <note default-x="641.53" default-y="-40.00">
        <pitch>
          <step>E</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">continue</beam>
        </note>
      <note default-x="672.56" default-y="-35.00">
        <pitch>
          <step>F</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="703.58" default-y="-30.00">
        <pitch>
          <step>G</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="734.61" default-y="-25.00">
        <pitch>
          <step>A</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">continue</beam>
        </note>
      <note default-x="765.64" default-y="-20.00">
        <pitch>
          <step>B</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">continue</beam>
        </note>
      <note default-x="796.66" default-y="-15.00">
        <pitch>
          <step>C</step>
          <octave>5</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>down</stem>
        <beam number="1">end</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="827.69" default-y="-50.00">
        <pitch>
          <step>C</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">begin</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="858.71" default-y="-45.00">
        <pitch>
          <step>D</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">continue</beam>
        </note>
      <note default-x="889.74" default-y="-40.00">
        <pitch>
          <step>E</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="920.77" default-y="-35.00">
        <pitch>
          <step>F</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">begin</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="951.79" default-y="-30.00">
        <pitch>
          <step>G</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">continue</beam>
        </note>
      <note default-x="982.82" default-y="-25.00">
        <pitch>
          <step>A</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">end</beam>
        </note>
      <note default-x="1013.84" default-y="-20.00">
        <pitch>
          <step>B</step>
          <octave>4</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">continue</beam>
        <beam number="2">continue</beam>
        <beam number="3">begin</beam>
        </note>
      <note default-x="1044.87" default-y="-15.00">
        <pitch>
          <step>C</step>
          <octave>5</octave>
          </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>32nd</type>
        <stem>up</stem>
        <beam number="1">end</beam>
        <beam number="2">end</beam>
        <beam number="3">end</beam>
        </note>
      </measure>
    </part>
  </score-partwise>
"""

internal val w3c31fDirectionMultilineCompounds: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">A test that checks how newlines in
          &lt;words&gt; elements are supported.

          Within a single &lt;direction&gt; element to be placed above the
          staff, the ‘p’ dynamic symbol gets followed by a single space, which
          in turn is followed by some boxed text that spreads over three lines
          and has the attribute ‘valign="center"’.  The latter two also set the
          attribute ‘xml:space="preserve"’.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <direction placement="above">
        <direction-type>
          <dynamics default-y="-80">
            <p/>
          </dynamics>
        </direction-type>
        <direction-type>
          <words default-y="-80"
                 font-family="Times New Roman"
                 font-size="10.25"
                 xml:space="preserve"> </words>
        </direction-type>
        <direction-type>
          <words default-y="-80"
                 font-family="Times New Roman"
                 font-size="10.25"
                 xml:space="preserve"
                 enclosure="rectangle"
                 valign="middle">text
in
a small box</words>
        </direction-type>
       </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c32bArticulationsTexts: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Text markup with different CSS
          font sizes, weights, horizontal positions (using ‘default-x’), and
          vertical positions (using ‘default-y’), seven in total.  The four
          markups below the staff are positioned immediately before
          &lt;measure&gt; elements; they should be thus associated with the
          following bar line.  One markup is also drawn in red.

          A trailing up-arrow indicates a ‘placement="above"’ attribute, a
          down-arrow means ‘placement="below"’.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name></part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>96</divisions>
        <key>
          <fifths>0</fifths>
        </key>
        <time>
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <staves>1</staves>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <direction placement="above">
        <direction-type>
          <words default-x="0" default-y="15" font-size="medium"
                 font-weight="normal">(0,15) normal, medium ↑</words>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>384</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <direction placement="below">
        <direction-type>
          <words default-x="-50" default-y="-100" font-size="medium"
                 font-weight="bold">(-50,-100) bold, medium ↓</words>
        </direction-type>
      </direction>
    </measure>
    <measure number="2">
      <direction placement="above">
        <direction-type>
          <words default-x="50" default-y="35" font-size="large"
                 font-weight="normal">(50,35) normal, large ↑</words>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>G</step>
          <octave>4</octave>
        </pitch>
        <duration>384</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <direction placement="below">
        <direction-type>
          <words default-x="0" default-y="-120" font-size="large"
                 font-weight="bold">(0,-120) bold, large ↓</words>
        </direction-type>
      </direction>
    </measure>
    <measure number="3">
      <direction placement="above">
        <direction-type>
          <words default-x="0" default-y="55" font-size="small"
                 font-weight="normal">(0,55) normal, small ↑</words>
        </direction-type>
      </direction>
      <note>
        <rest measure="yes"/>
        <duration>384</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <direction placement="below">
        <direction-type>
          <words default-x="50" default-y="-140" font-size="small"
                 font-weight="bold">(50,-140) bold, small ↓</words>
        </direction-type>
      </direction>
      <direction placement="below">
        <direction-type>
          <words default-x="100" default-y="-160" font-size="small"
                 color="#FF0000">(100,-160) normal, small, red ↓</words>
        </direction-type>
      </direction>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c51aHeaderCredits: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Check multiple &lt;credit&gt;
          elements with various &lt;credit-type&gt; children (also testing the
          case without such a child and having two).  There is also a
          &lt;defaults&gt; block to properly set up (quite small) page
          dimensions and a ‘tenth’ value, making the ‘default-x’ and ‘default-y’
          attributes of &lt;credit-words&gt; children meaningful.

          The following &lt;credit&gt; elements are present, with the given
          &lt;credit-type&gt; type.

          – ‘page number’ (two &lt;credit&gt; elements, one at the top left for
          page ‘2’ and another one at the top right for page ‘2a’ using
          attribute ‘page="2"’; valign: top).

          – ‘rights’ (at the bottom; halign: center, justify: right, valign:
          bottom).

          – ‘title’ (two &lt;credit-words&gt; elements, attributes only set for
          the first one, almost at the top; justify: center, valign: middle,
          enclosure: rectangle).

          – No &lt;credit-type&gt; element (below the title; halign: center,
          valign: middle).

          – Two &lt;credit-type&gt; elements (types ‘foo’ and ‘bar’, at the top;
          justify: center, valign: top, font-stype: italic).

          – ‘composer’ (below the type-less element; justify: right, valign:
          middle).

          – ‘lyricist’ (below the type-less element; valign: middle).

          There are no metadata elements.

          Measure 1 contains &lt;print blank-page="1"/&gt; to make the music
          start on page 2 (i.e., all &lt;credit&gt; elements in this test go to
          page 1).

          Chances are high that an application ignores all &lt;credit&gt;
          elements, using only metadata to derive titles, subtitles, and
          more.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <defaults>
    <scaling>
      <millimeters>6.35</millimeters>
      <tenths>40</tenths>
    </scaling>
    <page-layout>
      <page-height>660</page-height>
      <page-width>860</page-width>
    </page-layout>
  </defaults>
  <credit>
    <credit-type>page number</credit-type>
    <credit-words default-x="40"
                  default-y="630"
                  font-size="12"
                  valign="top">2</credit-words>
  </credit>
  <credit page="2">
    <credit-type>page number</credit-type>
    <credit-words default-x="800"
                  default-y="630"
                  font-size="12"
                  valign="top">2a</credit-words>
  </credit>
  <credit>
    <credit-type>rights</credit-type>
    <credit-words default-x="430"
                  default-y="80"
                  font-size="9"
                  halign="center"
                  justify="right"
                  valign="bottom">The copyright comes here.</credit-words>
  </credit>
  <credit>
    <credit-type>title</credit-type>
    <credit-words default-x="430"
                  default-y="440"
                  font-size="24"
                  justify="center"
                  valign="middle"
                  enclosure="rectangle">The </credit-words>
    <credit-words>Title</credit-words>
  </credit>
  <credit>
    <credit-words default-x="430"
                  default-y="360"
                  font-size="14"
                  halign="center"
                  valign="middle">A subtitle without a type</credit-words>
  </credit>
  <credit>
    <credit-type>foo</credit-type>
    <credit-type>bar</credit-type>
    <credit-words default-x="430"
                  default-y="600"
                  font-size="12"
                  font-style="italic"
                  justify="center"
                  valign="top">Two types (foo + bar)</credit-words>
  </credit>
  <credit>
    <credit-type>composer</credit-type>
    <credit-words default-x="780"
                  default-y="280"
                  font-size="10"
                  justify="right"
                  valign="middle">The Composer</credit-words>
  </credit>
  <credit>
    <credit-type>lyricist</credit-type>
    <credit-words default-x="80"
                  default-y="280"
                  font-size="10"
                  valign="middle">The Lyricist</credit-words>
  </credit>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <print blank-page="1"/>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c61hLyricsBeamsMelismata: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">In standard music notation,
          beaming or slurs normally indicate melismata for lyrics (in MusicXML
          there is no such interdependency).  Also make sure that notes without
          an explicit syllable are treated as if they were part of a melisma.

          In all measures, the melisma starts on note 1 and ends on note 7;
          there are lyrics on notes 1, 4, and 5, followed by an extender line
          from note 5 to 7.

          Measure 1 has beams from notes 1 to 3 and 5 to 7.

          In measure 2, there are neither beams nor slurs.  ‘start’, ‘continue’,
          and ‘stop’ types are used for &lt;extend&gt;, together with a red
          color for the extender line.

          Measure 3 uses slurs (and no beams) for notes 1 to 3 and 5 to 7.

          Measure 4 uses a single slur (and no beams) from note 1
          to 7.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name></part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>2</divisions>
      </attributes>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
        <lyric number="1">
          <syllabic>begin</syllabic>
          <text>Me</text>
        </lyric>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>middle</syllabic>
          <text>lis</text>
        </lyric>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
        <lyric number="1">
          <syllabic>end</syllabic>
          <text>ma</text>
          <extend/>
        </lyric>
      </note>
      <note>
        <pitch><step>G</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch><step>B</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <note>
        <rest/>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>begin</syllabic>
          <text>Me</text>
        </lyric>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>middle</syllabic>
          <text>lis</text>
        </lyric>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>end</syllabic>
          <text>ma</text>
          <extend type="start" color="#FF0000"/>
        </lyric>
      </note>
      <note>
        <pitch><step>G</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <extend type="continue"/>
        </lyric>
      </note>
      <note>
        <pitch><step>B</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <extend type="stop"/>
        </lyric>
      </note>
      <note>
        <rest/>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <slur number="1" type="start"/>
        </notations>
        <lyric number="1">
          <syllabic>begin</syllabic>
          <text>Me</text>
        </lyric>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <slur number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>middle</syllabic>
          <text>lis</text>
        </lyric>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <slur number="1" type="start"/>
        </notations>
        <lyric number="1">
          <syllabic>end</syllabic>
          <text>ma</text>
          <extend/>
        </lyric>
      </note>
      <note>
        <pitch><step>G</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>B</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <slur number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <rest/>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <slur number="1" type="start"/>
        </notations>
        <lyric number="1">
          <syllabic>begin</syllabic>
          <text>Me</text>
        </lyric>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>A</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>middle</syllabic>
          <text>lis</text>
        </lyric>
      </note>
      <note>
        <pitch><step>C</step><octave>5</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <lyric number="1">
          <syllabic>end</syllabic>
          <text>ma</text>
          <extend/>
        </lyric>
      </note>
      <note>
        <pitch><step>G</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <note>
        <pitch><step>B</step><octave>4</octave></pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <slur number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <rest/>
        <duration>1</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c61jLyricsElisions: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Multiple lyrics syllables assigned
          to a single note are implemented either using a space in the
          &lt;text&gt; child of &lt;lyric&gt;, or by using &lt;elision&gt;.

          The first note has a single syllable, the second note has two
          syllables separated by a space, the third has two syllables with
          &lt;elision&gt; set to an undertie, and the fourth has three syllables
          (the first and third one in red, and the second one being in italic
          and overriding the color with blue), with a green undertie between the
          first and second syllable and an empty &lt;elision&gt; element between
          the second and third syllable, causing an application-specific elision
          glyph.

          None of the &lt;lyric&gt; elements has the ‘number’ attribute
          set.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <lyric>
          <text>a</text>
        </lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <lyric>
          <text>b c</text>
        </lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <lyric>
          <text>d</text>
          <elision>‿</elision>
          <text>e</text>
        </lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>whole</type>
        <lyric color="#FF0000">
          <text>f</text>
          <elision color="#00FF00">‿</elision>
          <text color="#0000FF" font-style="italic">g</text>
          <elision/>
          <text>h</text>
        </lyric>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c61kLyricsSpannersExtenders: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Lyrics spanners: continued
          syllables and extenders, possibly spanning multiple notes.

          In the following, measure-beat values are shown with the assigned
          MusicXML elements.  Beat 4 in measure 3 is a rest.

          1/1: Single syllable ‘AAA’ (left-justified), extend.

          1/3: Start syllable ‘bbb’.

          2/2: End syllable ‘CCC’ (right-justified), extend.

          3/1: Single syllable ‘eee’ (left-justified), extend.

      </miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric justify="left" number="1">
          <syllabic>single</syllabic>
          <text>AAA</text>
          <extend/>
        </lyric>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric number="1">
          <syllabic>begin</syllabic>
          <text>bbb</text>
        </lyric>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric justify="right" number="1">
          <syllabic>end</syllabic>
          <text>CCC</text>
          <extend/>
        </lyric>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
        <lyric justify="left" number="1">
          <syllabic>single</syllabic>
          <text>eee</text>
          <extend/>
        </lyric>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <rest/>
        <duration>1</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""

internal val w3c31bDirectionsOrder: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Using &lt;offset&gt; it is
          possible to make successive &lt;direction&gt; elements look like being
          concatenated.  However, it is a bad idea in general to do that because
          it makes the rendering dependent on a program's score formatting.

          In the first bar, the first &lt;direction&gt; element contains a tempo
          mark and is directly followed by another &lt;direction&gt; element
          containing a metronome mark that is moved to the right by a positive
          &lt;offset&gt; element.

          In the second bar, the first &lt;direction&gt; element holds the start
          of a diminuendo wedge; it is directly followed by another
          &lt;direction&gt; element containing a dynamics mark.  Since the
          wedge has an &lt;offset&gt; element with a positive value while the
          dynamics has no such element at all, the dynamics precedes the
          wedge.

          The wedge ends at or right before the final bar line (i.e., the
          &lt;direction&gt; element ending the wedge comes after the last
          &lt;note&gt; element).</miscellaneous-field>
    </miscellaneous>
  </identification>
  <defaults/>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>8</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <direction directive="yes" placement="above">
        <direction-type>
          <words default-y="33"
                 font-weight="bold">Lento</words>
        </direction-type>
      </direction>
      <direction placement="above">
        <direction-type>
          <metronome default-y="33" parentheses="yes">
            <beat-unit>quarter</beat-unit>
            <per-minute>56</per-minute>
          </metronome>
        </direction-type>
        <offset>4</offset>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>32</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <direction placement="below">
        <direction-type>
          <wedge default-y="-73" type="diminuendo"/>
        </direction-type>
        <offset>8.1</offset>
      </direction>
      <direction placement="below">
        <direction-type>
          <dynamics default-y="-76.5">
            <f/>
          </dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>32</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>
      <direction>
        <direction-type>
          <wedge type="stop"/>
        </direction-type>
      </direction>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c31dDirectionsCompounds: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">This tests various combinations of
          &lt;direction&gt; children.  The lyrics for each note describe the
          compound elements assigned to that note.

          The first ‘molto f’ uses &lt;words&gt;molto &lt;words&gt;, i.e., it
          omits the ‘xml:space’ attribute.   This makes the rendering of the
          space between ‘molto’ and ‘f’ implementation-dependent (and might be
          thus not rendered).  The second ‘molto f’, together with ‘meno f’,
          uses ‘xml:space="preserve"’.

          For demonstration purposes, there is no space between the words ‘bold’
          and ‘italic’.

          A trailing up-arrow indicates a ‘placement="above"’ attribute, a
          down-arrow means ‘placement="below"’.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <direction>
        <direction-type>
          <words>Adagio</words>
        </direction-type>
        <direction-type>
          <metronome>
            <beat-unit>long</beat-unit>
            <per-minute>100</per-minute>
          </metronome>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>Adagio</text></lyric>
        <lyric number="2"><text>long=100</text></lyric>
      </note>
      <direction placement="below">
        <direction-type>
          <words default-y="-80" font-family="Times New Roman" font-size="10.25"
                 font-style="italic">molto </words>
        </direction-type>
        <direction-type>
          <dynamics default-y="-80">
            <f/>
          </dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>molto↓</text></lyric>
        <lyric number="2"><text>f</text></lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <direction placement="below">
        <direction-type>
          <dynamics default-y="-80">
            <p/>
          </dynamics>
        </direction-type>
        <direction-type>
          <words default-y="-80" font-family="Times New Roman" font-size="10.25"
                 font-style="italic" xml:space="preserve"> subito</words>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>p↓</text></lyric>
        <lyric number="2"><text>subito</text></lyric>
      </note>
      <direction placement="above">
        <direction-type>
          <words default-y="30" font-family="Times New Roman" font-size="10.25"
                 font-style="italic" enclosure="rectangle"
                 xml:space="preserve">molto </words>
        </direction-type>
        <direction-type>
          <dynamics default-y="30">
            <f/>
          </dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>molto f↑</text></lyric>
        <lyric number="2"><text>(rectangle)</text></lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <direction placement="below">
        <direction-type>
          <dynamics><ppp/></dynamics>
        </direction-type>
        <direction-type>
          <wedge type="crescendo"/>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><syllabic>begin</syllabic><text>ppp↓</text></lyric>
        <lyric number="2"><syllabic>begin</syllabic><text>cresc.</text></lyric>
      </note>
      <direction placement="below">
        <direction-type>
          <wedge type="stop"/>
        </direction-type>
        <direction-type>
          <dynamics><fff/></dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><syllabic>end</syllabic><text>to</text></lyric>
        <lyric number="2"><syllabic>end</syllabic><text>fff↓</text></lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <print new-system="yes"/>
      <direction placement="above">
        <direction-type>
          <words font-style="italic">cresc.</words>
        </direction-type>
        <direction-type>
          <dashes type="start" number="1"/>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><syllabic>begin</syllabic><text>cresc.↑</text></lyric>
        <lyric number="2"><syllabic>begin</syllabic><text>dashes</text></lyric>
      </note>
      <direction placement="above">
        <direction-type>
          <dashes type="stop" number="1"/>
        </direction-type>
        <direction-type>
          <words font-style="italic" xml:space="preserve">meno </words>
        </direction-type>
        <direction-type>
          <dynamics>
            <f/>
          </dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><syllabic>end</syllabic><text>to</text></lyric>
        <lyric number="2"><syllabic>end</syllabic><text>meno f↑</text></lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <direction>
        <direction-type>
          <rehearsal>12</rehearsal>
          <rehearsal font-style="italic">bis</rehearsal>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>reh. 12+bis</text></lyric>
        <lyric number="2"><text>(square)</text></lyric>
      </note>
      <direction placement="below">
        <direction-type>
          <words font-weight="bold">bold</words>
          <words font-weight="normal" font-style="italic">italic</words>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>bold+</text></lyric>
        <lyric number="2"><text>italic</text></lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <direction>
        <direction-type>
          <segno/>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>segno</text></lyric>
      </note>
      <direction>
        <direction-type>
          <coda/>
          <coda/>
        </direction-type>
      </direction>
      <note>
        <pitch><step>C</step><octave>4</octave></pitch>
        <duration>2</duration>
        <voice>1</voice>
        <type>half</type>
        <lyric number="1"><text>coda</text></lyric>
        <lyric number="2"><text>×2</text></lyric>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c34bColors: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Colors.  The elements in the first
          bar have the ‘color’ attribute set to red for &lt;note&gt;,
          &lt;notehead&gt;, &lt;stem&gt;, &lt;dot&gt;, and &lt;accidental&gt;,
          respectively.

          The elements in the second bar have the ‘color’ attribute set to red
          for &lt;down-bow&gt;, &lt;tremolo&gt;, &lt;accent&gt;, and
          &lt;note&gt; again (for the rest), respectively, followed by a red
          unpitched note.

          The third and fourth bar consists of a red two-bar rest.

          The fifth bar has a red rehearsal mark on its starting bar line.  The
          first note has three fingerings, with the middle one in red; also
          attached is a red ‘ff’ sign.  The second note has three plucks, with
          the first one in red.  A red ‘Adagio’ tempo indication is on top of
          the third beat, which consists of a quadruplet with a red number.  The
          fourth beat holds a red arpeggio, and the fifth beat demonstrates
          lyrics in red.

          The sixth bar holds a red beamlet, a red beam, a red slur, a red pedal
          marker that gets continued with a blue one, and a red octave shift.

          Measure 7 contains a red trill with a black wavy line, a black
          trill with a red wavy line, a red bracket, a red glissando, and a red
          wedge.

          Bar 8 starts with a red and black tie connecting two chords,
          followed by ‘cresc.’ and dashes in red, followed by ‘dim.’ in black
          with red dashes.

          The ninth measure demonstrates a red 6/8 time signature, followed by a
          red two-stem tremolo, a red breath mark.

          Bar 10 begins with a red, non-traditional key change, followed by a
          red, traditional one.

          The eleventh bar starts with a red bass clef (actually, still in bar
          ten), followed by a blue percussion clef on a red two-line staff
          (where the middle line is omitted).

          Measures 12 to 14 hold a repeat structure with two endings, with red
          bar lines at the beginning of measures 12 and 14, a blue bar line at
          the beginning of measure 13, and a red prima-volta bracket and
          number.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1"><part-name></part-name></score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>8</divisions>
        <key>
          <fifths>0</fifths>
        </key>
        <time>
          <beats>15</beats>
          <beat-type>8</beat-type>
        </time>
        <staves>1</staves>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note color="#FF0000">
        <pitch>
          <step>G</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notehead color="#FF0000">normal</notehead>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <stem color="#FF0000">up</stem>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot color="#FF0000"/>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <accidental color="#FF0000">sharp</accidental>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>G</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow color="#FF0000"/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo color="#FF0000">3</tremolo>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow/>
          </technical>
          <ornaments>
            <tremolo>3</tremolo>
          </ornaments>
          <articulations>
            <accent color="#FF0000"/>
          </articulations>
        </notations>
      </note>
      <note color="#FF0000">
        <rest/>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note color="#FF0000">
        <unpitched/>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <attributes>
        <measure-style color="#FF0000">
          <multiple-rest>2</multiple-rest>
        </measure-style>
      </attributes>
      <note>
        <rest/>
        <duration>60</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <rest/>
        <duration>60</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <print new-system="yes"/>
      <direction>
        <direction-type>
          <rehearsal color="#FF0000">A</rehearsal>
        </direction-type>
      </direction>
      <direction>
        <direction-type>
          <dynamics color="#FF0000"><ff/></dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <fingering>0</fingering>
            <fingering color="#FF0000">1</fingering>
            <fingering>2</fingering>
          </technical>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <pluck color="#FF0000" placement="above">a</pluck>
            <pluck placement="above">m</pluck>
            <pluck placement="above">i</pluck>
          </technical>
        </notations>
      </note>
      <direction placement="above">
        <direction-type>
          <words font-weight="bold" color="#FF0000">Adagio</words>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
        <beam number="1">begin</beam>
        <notations>
          <tuplet number="1" type="start">
            <tuplet-actual>
              <tuplet-number color="#FF0000">4</tuplet-number>
            </tuplet-actual>
          </tuplet>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>3</duration>
        <voice>1</voice>
        <type>eighth</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
        <beam number="1">end</beam>
        <notations>
          <tuplet number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step><octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <arpeggiate color="#FF0000"/>
        </notations>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>C</step><octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <arpeggiate/>
        </notations>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>F</step><octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <arpeggiate/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step><octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <lyric number="1" color="#FF0000">
          <text>lyrics</text>
        </lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <stem color="#FF0000">up</stem>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1" color="#FF0000">begin</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
        <notations>
          <slur number="1" color="#FF0000" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
        <notations>
          <slur number="1" type="stop"/>
        </notations>
      </note>
      <direction>
        <direction-type>
          <pedal type="start" color="#FF0000" line="yes"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <direction>
        <direction-type>
          <pedal type="change" color="#0000FF" line="yes"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <direction>
        <direction-type>
          <pedal type="stop" line="yes"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <direction>
        <direction-type>
          <octave-shift size="8" color="#FF0000" type="down"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <direction>
        <direction-type>
          <octave-shift size="8" type="stop"/>
        </direction-type>
      </direction>
    </measure>
    <!--=======================================================-->
    <measure number="7">
      <print new-system="yes"/>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <ornaments>
            <trill-mark color="#FF0000"/>
            <wavy-line number="1" type="start"/>
            <wavy-line number="1" type="stop"/>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <ornaments>
            <trill-mark/>
            <wavy-line number="1" color="#FF0000" type="start"/>
            <wavy-line number="1" type="stop"/>
          </ornaments>
        </notations>
      </note>
      <direction placement="below">
        <direction-type>
          <bracket type="start" color="#FF0000" line-end="up"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <direction placement="below">
        <direction-type>
          <bracket type="stop" line-end="up"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <note>
        <pitch>
          <step>B</step>
          <octave>4</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <glissando number="1" color="#FF0000" type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <glissando number="1" type="stop"/>
        </notations>
      </note>
      <direction placement="above">
        <direction-type>
          <wedge spread="0" color="#FF0000" type="crescendo"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>quarter</type>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
      </note>
      <direction>
        <direction-type>
          <wedge spread="15" type="stop"/>
        </direction-type>
      </direction>
    </measure>
    <!--=======================================================-->
    <measure number="8">
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <tied type="start" color="#FF0000"/>
        </notations>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>8</duration>
        <voice>1</voice>
        <type>quarter</type>
        <notations>
          <tied type="start"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <tied type="stop"/>
        </notations>
      </note>
      <note>
        <chord/>
        <pitch>
          <step>A</step>
          <octave>4</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <notations>
          <tied type="stop"/>
        </notations>
      </note>
      <direction placement="above">
        <direction-type>
          <words font-style="italic" color="#FF0000">cresc.</words>
        </direction-type>
        <direction-type>
          <dashes type="start" number="1"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <direction placement="above">
        <direction-type>
          <dashes type="stop" number="1"/>
        </direction-type>
        <direction-type>
          <words font-style="italic">dim.</words>
        </direction-type>
        <direction-type>
          <dashes type="start" color="#FF0000" number="1"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <direction>
        <direction-type>
          <dashes type="stop" number="1"/>
        </direction-type>
      </direction>
    </measure>
    <!--=======================================================-->
    <measure number="9">
      <print new-system="yes"/>
      <attributes>
        <time color="#FF0000">
          <beats>6</beats>
          <beat-type>8</beat-type>
        </time>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
        <time-modification>
          <actual-notes>2</actual-notes>
          <normal-notes>1</normal-notes>
        </time-modification>
        <beam number="1" color="#FF0000">begin</beam>
        <notations>
          <ornaments>
            <tremolo type="start">2</tremolo>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
        <time-modification>
          <actual-notes>2</actual-notes>
          <normal-notes>1</normal-notes>
        </time-modification>
        <beam number="1">end</beam>
        <notations>
          <ornaments>
            <tremolo type="stop">2</tremolo>
          </ornaments>
          <articulations>
            <breath-mark color="#FF0000"/>
          </articulations>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="10">
      <attributes>
        <key color="#FF0000">
          <key-step>D</key-step>
          <key-alter>1</key-alter>
          <key-step>A</key-step>
          <key-alter>1</key-alter>
          <key-step>C</key-step>
          <key-alter>1</key-alter>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <attributes>
        <key color="#FF0000">
          <fifths>-2</fifths>
        </key>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="11">
      <attributes>
        <clef number="1" color="#FF0000">
          <sign>F</sign>
          <line>4</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>3</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <attributes>
        <clef number="1" color="#0000FF">
          <sign>percussion</sign>
        </clef>
        <staff-details>
          <staff-lines>3</staff-lines>
          <line-detail line="1" color="#FF0000"/>
          <line-detail line="2" print-object="no"/>
          <line-detail line="3" color="#FF0000"/>
        </staff-details>
      </attributes>
      <note>
        <unpitched/>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="12">
      <attributes>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
        <staff-details>
          <staff-lines>5</staff-lines>
        </staff-details>
      </attributes>
      <barline location="left">
        <bar-style color="#FF0000">heavy-light</bar-style>
        <repeat direction="forward"/>
      </barline>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>24</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="13">
      <barline location="left">
        <bar-style color="#0000FF">regular</bar-style>
        <ending number="1" color="#FF0000" type="start">1.</ending>
      </barline>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>24</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
      </note>
      <barline location="right">
        <bar-style color="#FF0000">light-heavy</bar-style>
        <ending number="1" type="stop"/>
        <repeat direction="backward"/>
      </barline>
    </measure>
    <!--=======================================================-->
    <measure number="14">
      <barline location="left">
        <ending number="2" type="start">2.</ending>
      </barline>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>24</duration>
        <voice>1</voice>
        <type>half</type>
        <dot/>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
        <ending number="2" type="stop"/>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c34cFontSize: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Font sizes.  The elements in the
          first bar have the ‘font-size’ attribute set to a larger value for
          &lt;note&gt;, &lt;notehead&gt;, &lt;trill-mark&gt;, &lt;dot&gt;, and
          &lt;accidental&gt;, respectively.

          The elements in the second bar have the ‘font-size’ attribute set to a
          larger value for &lt;down-bow&gt;, &lt;accidental-mark&gt;,
          &lt;accent&gt;, and &lt;note&gt; again (for the rest), respectively,
          followed by a larger percussion clef and a larger unpitched note.

          The third and fourth bar consists of an oversized two-bar rest.

          The fifth bar has an oversized rehearsal mark on its starting bar
          line.  The first note has three fingerings, with the middle one
          oversized; also attached is an oversized ‘ff’ sign.  The second note
          has three plucks, with the first one oversized.  An oversized ‘Adagio’
          tempo indication is on top of the third and fourth beats, which
          consist of a quadruplet with an oversized number.  The fifth beat
          demonstrates oversized lyrics.

          The sixth bar holds an oversized pedal marker with an oversized octave
          shift, an oversized trill with a wavy line, and an oversized ‘cresc.’
          with dashes.

          The seventh measure demonstrates an oversized, non-traditional key
          change, followed by an oversized 6/8 time signature, an oversized
          breath mark, an oversized bass clef, and an oversized, traditional key
          change.</miscellaneous-field>
    </miscellaneous>
  </identification>
  <part-list>
    <score-part id="P1"><part-name></part-name></score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>8</divisions>
        <key>
          <fifths>0</fifths>
        </key>
        <time>
          <beats>15</beats>
          <beat-type>8</beat-type>
        </time>
        <staves>1</staves>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note font-size="30">
        <pitch>
          <step>G</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notehead font-size="30">normal</notehead>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <stem>up</stem>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark font-size="30"/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot font-size="30"/>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <accidental font-size="30">sharp</accidental>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2">
      <note>
        <pitch>
          <step>G</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow placement="below" font-size="30"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <alter>-1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above"
                             font-size="30">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent/>
          </articulations>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <alter>1</alter>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <down-bow placement="below"/>
          </technical>
          <ornaments>
            <trill-mark/>
            <accidental-mark placement="above">double-sharp</accidental-mark>
          </ornaments>
          <articulations>
            <accent font-size="30"/>
          </articulations>
        </notations>
      </note>
      <note font-size="30">
        <rest/>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <attributes>
        <clef number="1" font-size="30">
          <sign>percussion</sign>
        </clef>
      </attributes>
      <note font-size="30">
        <unpitched/>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3">
      <print new-system="yes"/>
      <attributes>
        <measure-style font-size="30">
          <multiple-rest>2</multiple-rest>
        </measure-style>
      </attributes>
      <note>
        <rest/>
        <duration>60</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4">
      <note>
        <rest/>
        <duration>60</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5">
      <attributes>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <direction>
        <direction-type>
          <rehearsal font-size="30">A</rehearsal>
        </direction-type>
      </direction>
      <direction>
        <direction-type>
          <dynamics font-size="30"><ff/></dynamics>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <fingering>0</fingering>
            <fingering font-size="30">1</fingering>
            <fingering>2</fingering>
          </technical>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <technical>
            <pluck font-size="30" placement="above">a</pluck>
            <pluck placement="above">m</pluck>
            <pluck placement="above">i</pluck>
          </technical>
        </notations>
      </note>
      <direction placement="above">
        <direction-type>
          <words font-weight="bold" font-size="30">Adagio</words>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
        <notations>
          <tuplet number="1" show-number="both" show-type="actual" type="start">
            <tuplet-actual>
              <tuplet-number font-size="30">4</tuplet-number>
              <tuplet-type>quarter</tuplet-type>
            </tuplet-actual>
            <tuplet-normal>
              <tuplet-number>2</tuplet-number>
              <tuplet-type>quarter</tuplet-type>
              <tuplet-dot/>
            </tuplet-normal>
          </tuplet>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>6</duration>
        <voice>1</voice>
        <type>quarter</type>
        <time-modification>
          <actual-notes>4</actual-notes>
          <normal-notes>3</normal-notes>
        </time-modification>
        <notations>
          <tuplet number="1" type="stop"/>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step><octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <lyric number="1">
          <text font-size="30">lyrics</text>
        </lyric>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="6">
      <print new-system="yes"/>
      <direction>
        <direction-type>
          <pedal type="start" font-size="30" line="no"/>
        </direction-type>
        <direction-type>
          <octave-shift size="8" font-size="30" type="down"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">begin</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">continue</beam>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <voice>1</voice>
        <type>eighth</type>
        <beam number="1">end</beam>
      </note>
      <direction>
        <direction-type>
          <pedal type="stop" line="yes"/>
        </direction-type>
        <direction-type>
          <octave-shift size="8" type="stop"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <ornaments>
            <trill-mark font-size="30"/>
            <wavy-line number="1" type="start"/>
          </ornaments>
        </notations>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <ornaments>
            <wavy-line number="1" type="stop"/>
          </ornaments>
        </notations>
      </note>
      <direction placement="above">
        <direction-type>
          <words font-style="italic" font-size="30">cresc.</words>
        </direction-type>
        <direction-type>
          <dashes type="start" number="1"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="7">
      <attributes>
        <key font-size="30">
          <key-step>D</key-step>
          <key-alter>1</key-alter>
          <key-step>A</key-step>
          <key-alter>1</key-alter>
          <key-step>C</key-step>
          <key-alter>1</key-alter>
        </key>
        <time font-size="30">
          <beats>6</beats>
          <beat-type>8</beat-type>
        </time>
      </attributes>
      <direction>
        <direction-type>
          <dashes type="stop" number="1"/>
        </direction-type>
      </direction>
      <note>
        <pitch>
          <step>F</step>
          <octave>4</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
        <notations>
          <articulations>
            <breath-mark font-size="30"/>
          </articulations>
        </notations>
      </note>
      <attributes>
        <key font-size="30">
          <fifths>-2</fifths>
        </key>
        <clef number="1" font-size="30">
          <sign>F</sign>
          <line>4</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>F</step>
          <octave>3</octave>
        </pitch>
        <duration>12</duration>
        <voice>1</voice>
        <type>quarter</type>
        <dot/>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>
"""

internal val w3c52aPageLayout: String = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 4.0 Partwise//EN"
                                "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="4.0">
  <movement-title>Layout options</movement-title>
  <identification>
    <miscellaneous>
      <miscellaneous-field name="description">Several page layout settings:
          paper size (16cm×9cm), page margins (left / right = 3cm / 2cm,
          top / bottom = 0.5cm / 0.7cm), system margins (left / right = 1.5cm /
          2cm), distances (top-system / system-system = 4cm / 3cm), and
          different fonts.

          There is a line break before bar 3 and a page break before bar 5.

          Finally, we also test four CSS font sizes for &lt;words&gt; elements
          in bars 2 and 3.

          The page number on the first page is set to 8 (and to 12 on the
          second page).</miscellaneous-field>
    </miscellaneous>
  </identification>
  <defaults>
    <scaling>
      <millimeters>3.7703</millimeters>
      <tenths>40</tenths>
    </scaling>
    <page-layout>
      <page-height>955</page-height>
      <page-width>1697.48</page-width>
      <page-margins type="both">
        <left-margin>159.1</left-margin>
        <right-margin>212.2</right-margin>
        <top-margin>53</top-margin>
        <bottom-margin>74</bottom-margin>
      </page-margins>
    </page-layout>
    <system-layout>
      <system-margins>
        <left-margin>248</left-margin>
        <right-margin>206</right-margin>
      </system-margins>
      <system-distance>318.3</system-distance>
      <top-system-distance>424.4</top-system-distance>
    </system-layout>
    <music-font font-family="Maestro" font-size="10.7"/>
    <word-font font-family="Times New Roman" font-size="5.3"/>
  </defaults>
  <credit>
    <credit-words default-x="955"
                  default-y="876"
                  font-size="18"
                  font-weight="bold"
                  justify="center"
                  valign="top">Layout options</credit-words>
  </credit>
  <part-list>
    <score-part id="P1">
      <part-name print-object="no">MusicXML Part</part-name>
      <score-instrument id="P1-I1">
        <instrument-name>Grand Piano</instrument-name>
      </score-instrument>
      <midi-instrument id="P1-I1">
        <midi-channel>1</midi-channel>
        <midi-program>1</midi-program>
      </midi-instrument>
    </score-part>
  </part-list>
  <!--=========================================================-->
  <part id="P1">
    <measure number="1" width="441">
      <print new-page="yes" page-number="8"/>
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
          <mode>major</mode>
        </key>
        <time symbol="common">
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="2" width="378">
      <direction placement="above">
        <direction-type>
          <words font-size="small">‘small’ size</words>
        </direction-type>
      </direction>
      <direction placement="below">
        <direction-type>
          <words font-size="large">‘large’ size</words>
        </direction-type>
      </direction>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="3" width="378">
      <print new-system="yes"/>
      <direction placement="above">
        <direction-type>
          <words font-size="xx-small">‘xx-small’ size</words>
        </direction-type>
      </direction>
      <direction placement="below">
        <direction-type>
          <words font-size="xx-large">‘xx-large’ size</words>
        </direction-type>
      </direction>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="4" width="378">
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
    </measure>
    <!--=======================================================-->
    <measure number="5" width="819">
      <print new-page="yes" page-number="12"/>
      <note>
        <rest/>
        <duration>4</duration>
        <voice>1</voice>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
  <!--=========================================================-->
</score-partwise>
"""
