package com.ryanjames.sheetmusic.musicxml

/**
 * Original MX-21 mixed-feature pairs from the test plan, authored for this repository from the
 * MusicXML 4.0 reference. Expected XSD result: valid (not validated by these tests). Each pair
 * combines a supported feature with another feature, supported or not, to check that one does not
 * corrupt the other. The repeat ending + pickup pair is F16 itself.
 */

/**
 * Divisions change 2→4 mid-measure, then a backup for a second voice. C4 lasts 1 at 0 and D4 lasts
 * 1/2 at 1; the backup of 6 (in the new divisions) returns to 0, where voice-2 E3 lasts 3/2.
 */
internal val mixedDivisionsAndBackup: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Piano</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes><divisions>2</divisions></attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>quarter</type></note>
          <attributes><divisions>4</divisions></attributes>
          <note><pitch><step>D</step><octave>4</octave></pitch><duration>2</duration><voice>1</voice><type>eighth</type></note>
          <backup><duration>6</duration></backup>
          <note><pitch><step>E</step><octave>3</octave></pitch><duration>6</duration><voice>2</voice><type>quarter</type><dot/></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * A two-staff part with a tied C4/E4 chord whose upper tone changes to staff 2. The C4 tie on
 * staff 1 and the E4 tie on staff 2 each resolve across the barline; each chord's tones share a
 * chord ID while keeping their own staves.
 */
internal val mixedTiedChordAcrossStaves: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Piano</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes><divisions>1</divisions><staves>2</staves></attributes>
          <note>
            <pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><tie type="start"/><voice>1</voice>
            <type>half</type><staff>1</staff><notations><tied type="start"/></notations>
          </note>
          <note>
            <chord/><pitch><step>E</step><octave>4</octave></pitch><duration>2</duration><tie type="start"/><voice>1</voice>
            <type>half</type><staff>2</staff><notations><tied type="start"/></notations>
          </note>
        </measure>
        <measure number="2">
          <note>
            <pitch><step>C</step><octave>4</octave></pitch><duration>2</duration><tie type="stop"/><voice>1</voice>
            <type>half</type><staff>1</staff><notations><tied type="stop"/></notations>
          </note>
          <note>
            <chord/><pitch><step>E</step><octave>4</octave></pitch><duration>2</duration><tie type="stop"/><voice>1</voice>
            <type>half</type><staff>2</staff><notations><tied type="stop"/></notations>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * A grace note and its principal, each with a lyric. The grace links to D5 at 0, which lasts 1;
 * lyrics are not implemented (S14).
 */
internal val mixedGraceAndLyric: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Voice</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes><divisions>1</divisions></attributes>
          <note>
            <grace slash="yes"/><pitch><step>C</step><octave>5</octave></pitch><voice>1</voice><type>eighth</type>
            <lyric number="1"><syllabic>begin</syllabic><text>Al</text></lyric>
          </note>
          <note>
            <pitch><step>D</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type>
            <lyric number="1"><syllabic>end</syllabic><text>le</text><extend/></lyric>
          </note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * A B-flat clarinet transposition, then a mid-measure divisions change and octave-down
 * transposition at 1. Written D5 sounds C5 (72) at 0 for 1, then D4 (62) at 1 for 1/2.
 */
internal val mixedTranspositionAndDivisions: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Clarinet</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes>
            <divisions>1</divisions>
            <transpose><diatonic>-1</diatonic><chromatic>-2</chromatic></transpose>
          </attributes>
          <note><pitch><step>D</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>quarter</type></note>
          <attributes>
            <divisions>2</divisions>
            <transpose><diatonic>0</diatonic><chromatic>0</chromatic><octave-change>-1</octave-change></transpose>
          </attributes>
          <note><pitch><step>D</step><octave>5</octave></pitch><duration>1</duration><voice>1</voice><type>eighth</type></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()

/**
 * A timewise two-staff part: staff 1 holds a C4 whole note and staff 2 a C3 whole note after a
 * backup. Its partwise twin must give the same projection.
 */
internal val mixedTimewiseTwoStaves: String = """
    <score-timewise version="4.0">
      <part-list><score-part id="P1"><part-name>Piano</part-name></score-part></part-list>
      <measure number="1">
        <part id="P1">
          <attributes><divisions>1</divisions><staves>2</staves></attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type><staff>1</staff></note>
          <backup><duration>4</duration></backup>
          <note><pitch><step>C</step><octave>3</octave></pitch><duration>4</duration><voice>5</voice><type>whole</type><staff>2</staff></note>
        </part>
      </measure>
    </score-timewise>
""".trimIndent()

/** The partwise twin of [mixedTimewiseTwoStaves]. */
internal val mixedPartwiseTwoStaves: String = """
    <score-partwise version="4.0">
      <part-list><score-part id="P1"><part-name>Piano</part-name></score-part></part-list>
      <part id="P1">
        <measure number="1">
          <attributes><divisions>1</divisions><staves>2</staves></attributes>
          <note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><voice>1</voice><type>whole</type><staff>1</staff></note>
          <backup><duration>4</duration></backup>
          <note><pitch><step>C</step><octave>3</octave></pitch><duration>4</duration><voice>5</voice><type>whole</type><staff>2</staff></note>
        </measure>
      </part>
    </score-partwise>
""".trimIndent()
