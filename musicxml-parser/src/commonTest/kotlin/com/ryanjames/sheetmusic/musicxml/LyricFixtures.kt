package com.ryanjames.sheetmusic.musicxml

/**
 * Original F15 source, authored for this repository from the MusicXML 4.0 lyric, syllabic,
 * elision, text and extend references. Expected XSD result: valid (checked by `XsdValidationTest`).
 * In 4/4 with divisions=1, verse 1 sings "A-ve" with a melisma on "ve" extended over two more
 * notes (start, an extend-only continuation and an extend-only stop). Verse 2, named "refrain"
 * and placed below, sings "fa‿un" (two syllables elided with an undertie on one note) and then
 * "glo-ria", leaving the fourth note without a verse-2 lyric. Measure 2 has a lyric without a verse number
 * whose text is split into two runs, "A" and "men", and a rest without lyrics.
 */
internal val f15Lyrics: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <time><beats>4</beats><beat-type>4</beat-type></time>
          </attributes>
          <note>
            <pitch><step>C</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <lyric number="1"><syllabic>begin</syllabic><text>A</text></lyric>
            <lyric number="2" name="refrain" placement="below">
              <syllabic>single</syllabic><text>fa</text><elision>‿</elision><syllabic>single</syllabic><text>un</text>
            </lyric>
          </note>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <lyric number="1"><syllabic>end</syllabic><text>ve</text><extend type="start"/></lyric>
            <lyric number="2" name="refrain" placement="below"><syllabic>begin</syllabic><text>glo</text></lyric>
          </note>
          <note>
            <pitch><step>E</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <lyric number="1"><extend type="continue"/></lyric>
            <lyric number="2" name="refrain" placement="below"><syllabic>end</syllabic><text>ria</text></lyric>
          </note>
          <note>
            <pitch><step>F</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <lyric number="1"><extend type="stop"/></lyric>
          </note>
        </measure>
        <measure number="2">
          <note>
            <pitch><step>G</step><octave>5</octave></pitch><duration>2</duration><voice>1</voice><type>half</type>
            <lyric><syllabic>single</syllabic><text>A</text><text>men</text></lyric>
          </note>
          <note>
            <rest/><duration>2</duration><voice>1</voice><type>half</type>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
