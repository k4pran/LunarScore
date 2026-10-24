package com.ryanjames.sheetmusic.musicxml

/**
 * Original F02 source, authored for this repository from the MusicXML 4.0 structure reference.
 * Expected XSD result: valid (checked by `XsdValidationTest`). Reversing body order is intentional:
 * part-list order defines score order; body references supply each part's measures.
 */
internal val f02PartwiseScore: String = """
    <score-partwise version="4.0">
      <work><work-number>Op. 7</work-number><work-title>Suite &amp; Song</work-title></work>
      <movement-number>II</movement-number>
      <movement-title>Evening</movement-title>
      <identification>
        <creator type="composer">Ada</creator><creator type="poet">Ben</creator>
        <rights type="music">Original fixture</rights><rights type="words">Repository license</rights>
        <encoding>
          <software>LunarScore</software><encoder type="editor">Cora</encoder>
          <encoding-date>2026-10-21</encoding-date><software>Fixture tool</software>
          <encoding-description>Hand authored</encoding-description>
        </encoding>
        <source>Original test</source><relation>Companion</relation><relation>Second companion</relation>
        <miscellaneous><miscellaneous-field name="catalogue">F02</miscellaneous-field></miscellaneous>
      </identification>
      <credit page="2"><credit-type>title</credit-type><credit-type>subtitle</credit-type>
        <credit-words>Suite</credit-words><credit-words> &amp; Song</credit-words>
      </credit>
      <credit><credit-type>composer</credit-type><credit-words>Ada</credit-words></credit>
      <part-list>
        <score-part id="P1">
          <identification><creator type="arranger">Dee</creator><source>Part source</source></identification>
          <part-name>Flute</part-name><part-abbreviation>Fl.</part-abbreviation>
        </score-part>
        <score-part id="P2"><part-name>Cello</part-name></score-part>
      </part-list>
      <part id="P2"><measure number="A"/><measure number="A"/></part>
      <part id="P1"><measure number="0" implicit="yes"/><measure number="1" implicit="no"/></part>
    </score-partwise>
""".trimIndent()
