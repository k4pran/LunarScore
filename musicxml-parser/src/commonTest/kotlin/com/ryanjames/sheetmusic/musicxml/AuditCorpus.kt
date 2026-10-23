package com.ryanjames.sheetmusic.musicxml

/**
 * MX-21 audit corpus: every executable fixture by ID, and deterministic adversarial mutations of
 * them. Mutations are generated from source text, so they need no XML library and run on every
 * target. See `docs/musicxml/compliance-audit.md`.
 */
internal val auditFixtures: List<Pair<String, String>> = listOf(
    "F01" to f01QuarterNoteAndRest,
    "F02" to f02PartwiseScore,
    "F03" to f03AlterWithPrintedNatural,
    "F04" to f04DivisionsChange,
    "F05" to f05BackupAndForward,
    "F06" to f06ChordsAndCrossStaff,
    "F07" to f07MidMeasureAttributeChanges,
    "F08" to f08PartsAndPianoStaves,
    "F09" to f09BeamsAndTriplet,
    "F10" to f10TiesAndSlurs,
    "F11" to f11NestedTuplets,
    "F12-timewise" to f12TimewiseScore,
    "F12-partwise" to f12PartwiseScore,
    "F13" to f13NoteExpressions,
    "F14" to f14Directions,
    "F16" to f16RepeatsAndEndings,
    "F17" to f17InstrumentsAndTransposition,
    "F18" to f18LayoutHints,
    "F19" to f19GraceAndCue,
    "F21" to f21VersionFourAdditions,
    "F21-concert" to f21ConcertScore,
    "M-divisions-backup" to mixedDivisionsAndBackup,
    "M-tied-chord-staves" to mixedTiedChordAcrossStaves,
    "M-grace-lyric" to mixedGraceAndLyric,
    "M-transposition-divisions" to mixedTranspositionAndDivisions,
    "M-timewise-staves" to mixedTimewiseTwoStaves,
    "M-partwise-staves" to mixedPartwiseTwoStaves,
)

/** One named variant of a fixture. */
internal data class Mutant(val description: String, val xml: String)

private val textNode = Regex(">([^<]+)<")
private val attributeValue = Regex("""(\s[A-Za-z][\w:.-]*=")([^"]*)(")""")
private val tag = Regex("""<(/?)([A-Za-z][\w:.-]*)[^>]*?(/?)>""")

/** Boundary, malformed and out-of-range lexical values for element text. */
internal val adversarialTexts: List<String> = listOf(
    "", " ", "0", "-1", "-0.5", "2147483648", "99999999999999999999", "0.000000000000000000000000000001",
    "1e3", "NaN", "x", "17", " ",
)

/** Values that are enumerations of other attributes, numbers out of range or empty. */
internal val adversarialAttributeValues: List<String> = listOf(
    "", " ", " ", "0", "-1", "99999999999", "17", "x", "start", "stop", "no",
)

/** Replaces each non-blank text node, one at a time, with each adversarial value. */
internal fun textMutants(xml: String): List<Mutant> = textNode.findAll(xml)
    .filter { it.groupValues[1].isNotBlank() }
    .flatMap { match ->
        val range = match.groups[1]!!.range
        adversarialTexts.map { value ->
            Mutant("text@${range.first}='$value'", xml.replaceRange(range, value))
        }
    }.toList()

/** Replaces each attribute value, one at a time, with each adversarial value. */
internal fun attributeMutants(xml: String): List<Mutant> = attributeValue.findAll(xml)
    .filterNot { it.groupValues[1].trimStart().startsWith("xmlns") || it.groupValues[1].trimStart().startsWith("version") }
    .flatMap { match ->
        val range = match.groups[2]!!.range
        adversarialAttributeValues.map { value ->
            Mutant("attribute${match.groupValues[1].trim()}@${range.first}='$value'", xml.replaceRange(range, value))
        }
    }.toList()

/** Removes, then duplicates, each element below the root, one at a time. */
internal fun elementMutants(xml: String): List<Mutant> {
    val ranges = elementRanges(xml).drop(1)
    return ranges.flatMap { range ->
        val element = xml.substring(range)
        listOf(
            Mutant("remove@${range.first}", xml.removeRange(range)),
            Mutant("duplicate@${range.first}", xml.replaceRange(range, element + element)),
        )
    }
}

/** Swaps each pair of adjacent sibling elements. */
internal fun swapMutants(xml: String): List<Mutant> {
    val ranges = elementRanges(xml)
    return ranges.mapNotNull { first ->
        val second = ranges.firstOrNull { it.first > first.last && xml.substring(first.last + 1, it.first).isBlank() }
            ?: return@mapNotNull null
        val swapped = xml.substring(second) + xml.substring(first.last + 1, second.first) + xml.substring(first)
        Mutant("swap@${first.first}", xml.replaceRange(first.first..second.last, swapped))
    }
}

/** Inserts a foreign-namespace element as the first child of each element, one at a time. */
internal fun foreignExtensionMutants(xml: String): List<Mutant> = elementRanges(xml)
    .map { range -> range.first to xml.indexOf('>', range.first) }
    .filter { (_, startTagEnd) -> xml[startTagEnd - 1] != '/' }
    .map { (start, startTagEnd) ->
        Mutant("foreign@$start", xml.substring(0, startTagEnd + 1) + FOREIGN_ELEMENT + xml.substring(startTagEnd + 1))
    }

private const val FOREIGN_ELEMENT = "<x:extension xmlns:x=\"urn:example:extension\">data</x:extension>"

/** Truncates the document at regular intervals; every prefix must fail without throwing. */
internal fun truncationMutants(xml: String, step: Int = 7): List<Mutant> =
    (1 until xml.trimEnd().length step step).map { Mutant("truncate@$it", xml.substring(0, it)) }

/** Character ranges of every element in document order, root first. */
private fun elementRanges(xml: String): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    val open = mutableListOf<Pair<String, Int>>()
    val starts = mutableListOf<Int>()
    for (match in tag.findAll(xml)) {
        val (closing, name, selfClosing) = match.destructured
        when {
            selfClosing.isNotEmpty() -> {
                starts += match.range.first
                ranges += match.range
            }
            closing.isEmpty() -> {
                starts += match.range.first
                ranges += IntRange.EMPTY
                open += name to ranges.lastIndex
            }
            else -> {
                val (openName, index) = open.removeAt(open.lastIndex)
                check(openName == name) { "Fixture tags must nest: $openName/$name" }
                ranges[index] = starts[index]..match.range.last
            }
        }
    }
    return ranges
}
