package com.ryanjames.sheetmusic.score

/**
 * One lyric line's text under a note. [verse] identifies the line (for example "1" or "2"); null
 * means the source did not number it, which is not assumed to be verse 1. [name] is an optional
 * label such as "chorus". [syllables] are in source order; more than one means they are joined by
 * elisions. A melisma continuation has no syllables and only an [extend]. [placement] is
 * presentation only. [printObject] overrides whether this line is printed; null follows the note's
 * [NoteVisibility.lyricsPrinted].
 */
public data class Lyric(
    val syllables: List<LyricSyllable>,
    val verse: String? = null,
    val name: String? = null,
    val extend: LyricExtend? = null,
    val placement: Placement? = null,
    val printObject: Boolean? = null,
) {
    init {
        require(verse == null || verse.isNotBlank())
        require(syllables.isNotEmpty() || extend != null)
        require(syllables.drop(1).all { it.elision != null })
        require(syllables.firstOrNull()?.elision == null)
    }

    /** The printed text: each syllable's text, preceded by its elision symbol when it has one. */
    public val text: String
        get() = syllables.joinToString("") { it.elision.orEmpty() + it.text }
}

/**
 * A syllable of a [Lyric]. [text] joins the source's consecutive text runs, whose differences are
 * only typographic, with whitespace preserved. [syllabic] is its position in a word, null when
 * unspecified. [elision] is the symbol that joins it to the previous syllable on the same note, for
 * example an undertie; an empty string is an elision without a stated symbol.
 */
public data class LyricSyllable(
    val text: String,
    val syllabic: Syllabic? = null,
    val elision: String? = null,
)

/** A syllable's position in its word: a whole word, or the begin, middle or end of a hyphenated word. */
public enum class Syllabic { SINGLE, BEGIN, MIDDLE, END }

/**
 * A word extension (melisma) line. [type] marks its start, continuation or stop; null means the
 * source gave no type.
 */
public data class LyricExtend(val type: ExtendType? = null)

/** The role of a note in a lyric extension line. */
public enum class ExtendType { START, CONTINUE, STOP }
