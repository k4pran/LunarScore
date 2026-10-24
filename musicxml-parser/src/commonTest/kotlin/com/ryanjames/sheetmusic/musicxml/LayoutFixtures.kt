package com.ryanjames.sheetmusic.musicxml

/**
 * Original F18 source, authored for this repository from the MusicXML 4.0 defaults, print and
 * position references. Expected XSD result: valid (checked by `XsdValidationTest`). Defaults give
 * 7.2 mm per 40 tenths, an A4-like page with separate odd/even margins, system margins and
 * distances, and a staff distance. Measure 1 opens a page with a measure distance; its note is
 * placed with default-x and a negative relative-y, a colored rest follows, and a words direction has
 * a negative default-y. Measure 2 forces a new system with a new staff distance. Measure 3 forces a
 * new page after one blank page, numbered "3", and has a red crescendo wedge closed in measure 3.
 */
internal val f18LayoutHints: String = """
    <score-partwise version="4.0">
      <defaults>
        <scaling><millimeters>7.2</millimeters><tenths>40</tenths></scaling>
        <page-layout>
          <page-height>1650</page-height>
          <page-width>1166.5</page-width>
          <page-margins type="odd">
            <left-margin>80</left-margin><right-margin>60</right-margin>
            <top-margin>70</top-margin><bottom-margin>70</bottom-margin>
          </page-margins>
          <page-margins type="even">
            <left-margin>60</left-margin><right-margin>80</right-margin>
            <top-margin>70</top-margin><bottom-margin>70</bottom-margin>
          </page-margins>
        </page-layout>
        <system-layout>
          <system-margins><left-margin>0</left-margin><right-margin>0</right-margin></system-margins>
          <system-distance>121</system-distance>
          <top-system-distance>70</top-system-distance>
        </system-layout>
        <staff-layout><staff-distance>65</staff-distance></staff-layout>
      </defaults>
      <part-list><score-part id="P1"><part-name>Flute</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <print>
            <system-layout>
              <system-margins><left-margin>50</left-margin><right-margin>0</right-margin></system-margins>
              <top-system-distance>211</top-system-distance>
            </system-layout>
            <measure-layout><measure-distance>12.5</measure-distance></measure-layout>
          </print>
          <attributes><divisions>1</divisions><time><beats>2</beats><beat-type>4</beat-type></time></attributes>
          <direction placement="below">
            <direction-type><words default-y="-80" relative-x="-5">dolce</words></direction-type>
          </direction>
          <note default-x="83" relative-y="-3.5"><pitch><step>A</step><octave>4</octave></pitch><duration>1</duration><type>quarter</type></note>
          <note color="#FF0000"><rest/><duration>1</duration><type>quarter</type></note>
        </measure>
        <measure number="2">
          <print new-system="yes">
            <staff-layout number="1"><staff-distance>80</staff-distance></staff-layout>
          </print>
          <note><pitch><step>B</step><octave>4</octave></pitch><duration>2</duration><type>half</type></note>
        </measure>
        <measure number="3">
          <print new-page="yes" blank-page="1" page-number="3"/>
          <direction>
            <direction-type><wedge type="crescendo" default-y="-70" color="#80FF0000"/></direction-type>
          </direction>
          <note><pitch><step>C</step><octave>5</octave></pitch><duration>2</duration><type>half</type></note>
          <direction>
            <direction-type><wedge type="stop"/></direction-type>
          </direction>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
