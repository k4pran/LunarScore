package com.ryanjames.sheetmusic.musicxml

/**
 * Original F21, authored from the MusicXML 4.0
 * [version history](https://www.w3.org/2021/06/musicxml40/version-history/40/) and the element
 * references it links. XSD result: invalid in element order, by design (`XsdValidationTest`). One measure holds
 * every 4.0 addition that a score document can carry, so each must be mapped or reported; the
 * expected outcome of each is asserted in `VersionFourImportTest` and listed in
 * `docs/musicxml/version-40-changes.md`. The `concert-score`/`for-part` pitch subset is
 * [f21ConcertScore]. Packaging cases need the `.mxl` adapter (MX-19) and are not covered here.
 */
internal val f21VersionFourAdditions: String = """
    <score-partwise version="4.0" xmlns:xlink="http://www.w3.org/1999/xlink">
      <credit page="1"><credit-type>part name</credit-type><credit-words>Flute</credit-words></credit>
      <part-list>
        <score-part id="P1">
          <part-name>Flute</part-name>
          <part-link xlink:href="parts/flute.musicxml"/>
          <score-instrument id="P1-I1">
            <instrument-name>Flute</instrument-name>
            <instrument-sound>wind.flutes.whistle.tin.c</instrument-sound>
          </score-instrument>
          <score-instrument id="P1-I2"><instrument-name>Piccolo</instrument-name></score-instrument>
          <player id="P1-M1"><player-name>Flute 1</player-name></player>
        </score-part>
      </part-list>
      <part id="P1">
        <measure number="1">
          <print><measure-numbering system="only-top" staff="1" multiple-rest-always="yes" multiple-rest-range="yes">system</measure-numbering></print>
          <attributes>
            <divisions>1</divisions>
            <clef><sign>none</sign></clef>
            <staff-details><line-detail line="1" color="#FF0000"/><staff-size scaling="100">80</staff-size></staff-details>
            <transpose><chromatic>0</chromatic><octave-change>1</octave-change><double above="yes"/></transpose>
            <for-part><part-clef><sign>G</sign><line>2</line></part-clef><part-transpose><chromatic>0</chromatic></part-transpose></for-part>
            <measure-style><multiple-rest>2</multiple-rest></measure-style>
          </attributes>
          <barline location="left"><ending number="1" type="start">1.</ending></barline>
          <listening><sync type="none"/><other-listening type="mode">x</other-listening></listening>
          <sound><swing><first>2</first><second>1</second><swing-type>eighth</swing-type><swing-style>hard</swing-style></swing></sound>
          <direction system="only-top">
            <direction-type><metronome print-object="no"><beat-unit>quarter</beat-unit><per-minute>96</per-minute></metronome></direction-type>
            <direction-type><words enclosure="inverted-bracket">dolce</words></direction-type>
            <direction-type><pedal type="discontinue"/></direction-type>
            <direction-type><pedal type="resume"/></direction-type>
            <direction-type><percussion><membrane smufl="pictSnareDrum">snare drum</membrane></percussion></direction-type>
            <sound><swing><straight/></swing></sound>
            <listening><wait player="P1-M1"/></listening>
          </direction>
          <harmony arrangement="vertical">
            <numeral><numeral-root>5</numeral-root><numeral-alter>0</numeral-alter><numeral-key><numeral-fifths>0</numeral-fifths><numeral-mode>major</numeral-mode></numeral-key></numeral>
            <kind>dominant</kind>
            <inversion text="6/5">1</inversion>
            <bass arrangement="horizontal"><bass-separator>/</bass-separator><bass-step>B</bass-step></bass>
          </harmony>
          <figured-bass placement="below" halign="left" valign="top"><figure><figure-number>6</figure-number></figure></figured-bass>
          <note>
            <pitch><step>C</step><octave>5</octave></pitch>
            <duration>1</duration>
            <instrument id="P1-I1"/>
            <instrument id="P1-I2"/>
            <voice>1</voice>
            <type>quarter</type>
            <accidental smufl="medRenFlatSoftB">other</accidental>
            <notations>
              <slur type="start" number="16"/>
              <arpeggiate unbroken="yes"/>
              <ornaments><wavy-line type="start" smufl="wiggleTrill"/></ornaments>
              <technical><bend shape="curved"><bend-alter>2</bend-alter><release offset="1"/></bend></technical>
            </notations>
            <level type="editorial">x</level>
            <play><ipa>a</ipa></play>
            <listen><assess type="no"/><other-listen type="mode">x</other-listen></listen>
          </note>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch>
            <duration>3</duration>
            <instrument id="P1-I1"/>
            <voice>1</voice>
            <type>half</type>
            <dot/>
            <notations><slur type="stop" number="16"/></notations>
          </note>
          <barline location="right">
            <ending number="1" type="stop" system="only-top"/>
            <repeat direction="backward" after-jump="yes"/>
          </barline>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
