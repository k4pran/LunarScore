package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Accidental
import com.ryanjames.sheetmusic.score.Attribute
import com.ryanjames.sheetmusic.score.BarStyle
import com.ryanjames.sheetmusic.score.Barline
import com.ryanjames.sheetmusic.score.BarlineLocation
import com.ryanjames.sheetmusic.score.Beam
import com.ryanjames.sheetmusic.score.BeamType
import com.ryanjames.sheetmusic.score.AttributeEvent
import com.ryanjames.sheetmusic.score.ArticulationType
import com.ryanjames.sheetmusic.score.ClefSign
import com.ryanjames.sheetmusic.score.DirectionDisplay
import com.ryanjames.sheetmusic.score.DirectionEvent
import com.ryanjames.sheetmusic.score.DirectionMark
import com.ryanjames.sheetmusic.score.DirectionSound
import com.ryanjames.sheetmusic.score.Dynamics
import com.ryanjames.sheetmusic.score.Ending
import com.ryanjames.sheetmusic.score.EndingMark
import com.ryanjames.sheetmusic.score.EndingType
import com.ryanjames.sheetmusic.score.Event
import com.ryanjames.sheetmusic.score.ExtendType
import com.ryanjames.sheetmusic.score.Grace
import com.ryanjames.sheetmusic.score.Instrument
import com.ryanjames.sheetmusic.score.InterchangeableTime
import com.ryanjames.sheetmusic.score.KeyAlteration
import com.ryanjames.sheetmusic.score.KeyCancel
import com.ryanjames.sheetmusic.score.KeyCancelLocation
import com.ryanjames.sheetmusic.score.KeyMode
import com.ryanjames.sheetmusic.score.KeyOctave
import com.ryanjames.sheetmusic.score.LayoutDefaults
import com.ryanjames.sheetmusic.score.Lyric
import com.ryanjames.sheetmusic.score.LyricExtend
import com.ryanjames.sheetmusic.score.LyricSyllable
import com.ryanjames.sheetmusic.score.MarginType
import com.ryanjames.sheetmusic.score.Measure
import com.ryanjames.sheetmusic.score.MeasureRepeat
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.MetadataKind
import com.ryanjames.sheetmusic.score.NotatedValue
import com.ryanjames.sheetmusic.score.NoteExpression
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteRole
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.NoteVisibility
import com.ryanjames.sheetmusic.score.OrnamentType
import com.ryanjames.sheetmusic.score.PageLayout
import com.ryanjames.sheetmusic.score.PageMargins
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Placement
import com.ryanjames.sheetmusic.score.PrintHints
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RationalError
import com.ryanjames.sheetmusic.score.RationalException
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.Repeat
import com.ryanjames.sheetmusic.score.RepeatDirection
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Scaling
import com.ryanjames.sheetmusic.score.SenzaMisura
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.SoundingPitch
import com.ryanjames.sheetmusic.score.Staff
import com.ryanjames.sheetmusic.score.StaffLayout
import com.ryanjames.sheetmusic.score.StemDirection
import com.ryanjames.sheetmusic.score.Syllabic
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.SystemLayout
import com.ryanjames.sheetmusic.score.TimeFraction
import com.ryanjames.sheetmusic.score.TimeModification
import com.ryanjames.sheetmusic.score.TimeRelation
import com.ryanjames.sheetmusic.score.TimeSeparator
import com.ryanjames.sheetmusic.score.TimeSymbol
import com.ryanjames.sheetmusic.score.Transposition
import com.ryanjames.sheetmusic.score.VisualHints
import com.ryanjames.sheetmusic.score.WedgeType
import com.ryanjames.sheetmusic.score.WrittenPitch

/**
 * Imports a UTF-8 partwise score with ordered metadata, parts and measures, plus notes, rests and
 * chords in any voice and staff with exact onsets and durations from divisions, placed by a
 * measure-local cursor that backup/forward move. Key, simple time, clef, staff-line and transpose
 * changes become staff-scoped events at their exact offsets. A timewise
 * score is normalized while streaming to the same projection: a part missing from a timewise
 * measure gets an empty measure there. Stems, beams and cumulative (nested) time modifications are
 * kept as note notation. Sound ties, notated ties, slurs and tuplets on imported events are
 * paired within each part across measures and voices; an unmatched endpoint is reported as
 * `UNMATCHED_SPAN` and kept with a null partner. Articulations, ornaments and note dynamics are
 * kept as note expressions; directions (words, dynamics, metronome marks and wedges, with
 * separate sound tempo/dynamics and display placement/offset) become events at the cursor, and
 * wedges are paired like slurs. Lyrics (verses, syllabic text, elisions and extends) are kept on
 * their notes. Grace notes take no time: they sit at the cursor without a duration and are linked
 * to their principal note by a `GRACE_NOTE` relationship; cue notes are timed. Score-instruments
 * (with MIDI percussion keys) are part instruments; notes keep their instrument, and sound
 * `instrument-change`s and `transpose` become attribute events. Written pitch is kept and
 * sounding pitch is derived from its staff's transposition at the note's onset (as written without one); unpitched
 * notes keep only their display position. Barline styles, repeat signs and ending marks are kept
 * on their measures, ending start/stop marks are paired into part endings (orphans are
 * `UNMATCHED_SPAN`), and staff-1 measure-repeat signs mark their measures; playback order is
 * derived separately by `playbackOrder`. Scaling, page/system/staff layout defaults, measure
 * `print` breaks and layout, and position/colour of notes, rests and direction marks are kept as
 * optional engraving hints in tenths. Other musical content is not implemented yet: meaningful unmapped
 * content rejects strict parsing, or is discarded with warnings under tolerant policy. A part
 * defines staves 1 to its largest `staves` declaration (staff 1 when it only has events); measure
 * extent is the observed end of timed content. This is not an XSD validator.
 * Structural identity/reference errors are fatal under both policies, except that a part missing
 * from the part-list is discarded and, in a one-part score, a part without an `id` is bound to its
 * only score-part, both rejecting strict parsing. Missing divisions default to 1. Input security,
 * resource limits and cooperative [isCancelled] checks are shared with the ordered XML reader.
 */
public fun parseMusicXml(
    input: ByteArray,
    options: ParseOptions = ParseOptions(),
    sourceName: String? = null,
    isCancelled: () -> Boolean = { false },
): ParseResult<Score> = readScoreXml(input, options, sourceName, isCancelled) { reader ->
    ScoreParser(reader).parse()
}

private class ScoreParser(private val reader: ScoreXmlReader) {
    private val context = reader.context
    private val metadata = mutableListOf<MetadataEntry>()
    private val definitions = linkedMapOf<String, PartDefinition>()
    private val bodies = mutableMapOf<String, PartBody>()
    private var declaredPartId: String? = null
    private val declaredStaves = mutableMapOf<String, Int>()
    private val timewise = reader.root == ScoreXmlRoot.TIMEWISE
    private val timewiseMeasures = mutableListOf<MeasureHeader>()

    /** Whether `defaults` declares a concert score, in which written pitches are concert pitches. */
    private var concertScore = false

    /** Layout defaults read from `defaults`, if any. */
    private var layoutDefaults: LayoutDefaults? = null

    /** The part whose measure content is being read; timewise input switches it per measure. */
    private lateinit var body: PartBody

    fun parse(): Score {
        val root = checkNotNull(reader.current)
        attributes(root, setOf("version"), "S01")
        val bodyElement = if (timewise) "measure" else "part"
        val seen = mutableSetOf<String>()
        var rank = -1
        children { event ->
            val name = event.localName().orEmpty()
            val nextRank = when (name) {
                "work" -> 0
                "movement-number" -> 1
                "movement-title" -> 2
                "identification" -> 3
                "defaults" -> 4
                "credit" -> 5
                "part-list" -> 6
                bodyElement -> 7
                else -> null
            }
            if (nextRank != null) {
                if (nextRank < rank || (name != "credit" && name != bodyElement && !seen.add(name))) {
                    invalid(event, "Duplicate or out-of-order score header element: $name")
                    return@children
                }
                rank = nextRank
            }
            when (name) {
                "work" -> work(event)
                "movement-number" -> scoreText(event, MetadataKind.MOVEMENT_NUMBER)
                "movement-title" -> scoreText(event, MetadataKind.MOVEMENT_TITLE)
                "identification" -> identification(event, "metadata", metadata.size, ::addScoreMetadata)
                "defaults" -> defaults(event)
                "credit" -> credit(event)
                "part-list" -> partList(event)
                bodyElement -> when {
                    "part-list" !in seen -> invalid(event, "A part-list is required before ${bodyElement}s")
                    timewise -> timewiseMeasure(event)
                    else -> part(event)
                }
                else -> unsupportedTree(event)
            }
        }
        if (context.canContinue) {
            when {
                "part-list" !in seen -> invalid(root, "Missing required part-list")
                definitions.isEmpty() -> invalid(root, "The part-list must declare a score-part")
                else -> definitions.values.firstOrNull { it.id !in bodies }?.let {
                    invalid(it.location, "Declared score-part ${it.id} has no ${if (timewise) "part in any measure" else "part body"}")
                }
            }
        }
        if (timewise) bodies.values.forEach { padMissingMeasures(it, timewiseMeasures.size) }
        if (context.canContinue) definitions.values.forEach { definition -> bodies[definition.id]?.let(::finishEndings) }
        val spans = if (context.canContinue) definitions.values.flatMap { bodies[it.id]?.spans?.finish().orEmpty() } else emptyList()
        spans.forEachIndexed { index, span ->
            span.startLocation?.let { context.addProvenance(SourceProvenance("relationships[$index].startEventId", it)) }
            span.endLocation?.let { context.addProvenance(SourceProvenance("relationships[$index].endEventId", it)) }
        }
        val title = metadata.filterIsInstance<MetadataEntry.Text>().let { entries ->
            entries.firstOrNull { it.kind == MetadataKind.MOVEMENT_TITLE && it.text.isNotBlank() }?.text
                ?: entries.firstOrNull { it.kind == MetadataKind.WORK_TITLE && it.text.isNotBlank() }?.text
        }
        return Score(
            parts = definitions.values.map { definition ->
                val measures = bodies[definition.id]?.measures.orEmpty().toList()
                Part(
                    id = definition.id,
                    staves = (1..(declaredStaves[definition.id] ?: if (measures.any { it.events.isNotEmpty() }) 1 else 0))
                        .map { Staff(it) },
                    measures = measures,
                    name = definition.name,
                    instruments = definition.instruments.values.map { Instrument(it.id, it.name, it.percussionKey) },
                    abbreviation = definition.abbreviation,
                    metadata = definition.metadata.toList(),
                    endings = bodies[definition.id]?.endings.orEmpty().toList(),
                    nameVisible = definition.nameVisible,
                    abbreviationVisible = definition.abbreviationVisible,
                )
            },
            relationships = spans.map { it.relationship },
            title = title,
            metadata = metadata.toList(),
            layout = layoutDefaults,
        )
    }

    private fun work(start: ScoreXmlEvent) {
        attributes(start)
        val seen = mutableSetOf<String>()
        children { event ->
            when (val name = event.localName()) {
                "work-number", "work-title" -> if (once(seen, name, event)) {
                    scoreText(event, if (name == "work-number") MetadataKind.WORK_NUMBER else MetadataKind.WORK_TITLE)
                }
                else -> unsupportedTree(event)
            }
        }
    }

    private fun scoreText(start: ScoreXmlEvent, kind: MetadataKind) {
        val path = "metadata[${metadata.size}]"
        val value = text(start)
        addScoreMetadata(MetadataEntry.Text(kind, value))
        provenance(path, start)
    }

    private fun addScoreMetadata(entry: MetadataEntry.Text) {
        metadata += entry
    }

    private fun identification(
        start: ScoreXmlEvent,
        path: String,
        startIndex: Int = 0,
        add: (MetadataEntry.Text) -> Unit,
    ) {
        attributes(start)
        var index = startIndex
        val seen = mutableSetOf<String>()
        fun field(event: ScoreXmlEvent, kind: MetadataKind, qualifierName: String? = null) {
            val qualifier = qualifierName?.let { event.attribute(it) }
            if (qualifierName == "name" && qualifier == null) {
                invalid(event, "A miscellaneous field requires a name")
                return
            }
            val value = text(event, qualifierName?.let { setOf(it) }.orEmpty())
            add(MetadataEntry.Text(kind, value, qualifier))
            provenance("$path[${index++}]", event)
        }
        children { event ->
            when (val name = event.localName()) {
                "creator" -> field(event, MetadataKind.CREATOR, "type")
                "rights" -> field(event, MetadataKind.RIGHTS, "type")
                "source" -> if (once(seen, name, event)) field(event, MetadataKind.SOURCE)
                "relation" -> field(event, MetadataKind.RELATION)
                "encoding" -> if (once(seen, name, event)) {
                    attributes(event)
                    children { child ->
                        when (child.localName()) {
                            "encoding-date" -> field(child, MetadataKind.ENCODING_DATE)
                            "encoder" -> field(child, MetadataKind.ENCODER, "type")
                            "software" -> field(child, MetadataKind.SOFTWARE)
                            "encoding-description" -> field(child, MetadataKind.ENCODING_DESCRIPTION)
                            else -> unsupportedTree(child)
                        }
                    }
                }
                "miscellaneous" -> if (once(seen, name, event)) {
                    attributes(event)
                    children { child ->
                        if (child.localName() == "miscellaneous-field") field(child, MetadataKind.MISCELLANEOUS, "name")
                        else unsupportedTree(child)
                    }
                }
                else -> unsupportedTree(event)
            }
        }
    }

    private fun credit(start: ScoreXmlEvent) {
        attributes(start, setOf("page"))
        val pageText = start.attribute("page")
        val page = pageText?.trim()?.toIntOrNull() ?: if (pageText == null) 1 else null
        if (page == null || page <= 0) {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("page"), "Credit page must be a positive Int")
            return
        }
        val path = "metadata[${metadata.size}]"
        val words = mutableListOf<String>()
        val types = mutableListOf<String>()
        children { event ->
            when (event.localName()) {
                "credit-type" -> {
                    provenance("$path.types[${types.size}]", event)
                    types += text(event)
                }
                "credit-words" -> {
                    provenance("$path.words[${words.size}]", event)
                    words += text(event)
                }
                else -> unsupportedTree(event)
            }
        }
        metadata += MetadataEntry.Credit(words.toList(), types.toList(), page)
        provenance(path, start)
    }

    /**
     * Reads `concert-score`, which declares that written pitches are concert pitches apart from
     * octave transpositions, and scaling plus page, system and staff layout as [LayoutDefaults].
     * Appearance, fonts and lyric languages are reported as S17 discarded.
     */
    private fun defaults(start: ScoreXmlEvent) {
        attributes(start, feature = "S17")
        val seen = mutableSetOf<String>()
        var scaling: Scaling? = null
        var page: PageLayout? = null
        var system: SystemLayout? = null
        val staves = mutableListOf<StaffLayout>()
        children { event ->
            when (val name = event.localName()) {
                "concert-score" -> {
                    attributes(event, feature = "S16")
                    children { child -> unsupportedTree(child, "S16") }
                    concertScore = true
                }
                "scaling" -> if (once(seen, name, event)) scaling = scaling(event)
                "page-layout" -> if (once(seen, name, event)) page = pageLayout(event)
                "system-layout" -> if (once(seen, name, event)) system = systemLayout(event)
                "staff-layout" -> staffLayout(event)?.let { staves += it }
                else -> unsupportedTree(event, "S17")
            }
        }
        if (!context.canContinue || (scaling == null && page == null && system == null && staves.isEmpty())) return
        layoutDefaults = LayoutDefaults(scaling, page, system, staves.toList())
        provenance("layout", start)
    }

    /** Reads the millimetres that correspond to a number of tenths; both are required and positive. */
    private fun scaling(start: ScoreXmlEvent): Scaling? {
        attributes(start, feature = "S17")
        val values = layoutValues(start, setOf("millimeters", "tenths"), positive = true)
        val millimeters = values["millimeters"]
        val tenths = values["tenths"]
        if (!context.canContinue) return null
        if (millimeters == null || tenths == null) {
            invalid(start, "A scaling requires millimeters and tenths")
            return null
        }
        return Scaling(millimeters, tenths)
    }

    /**
     * Reads page height and width (positive, given together) and up to two page-margins: one for
     * both page sides, or one each for odd and even pages.
     */
    private fun pageLayout(start: ScoreXmlEvent): PageLayout? {
        attributes(start, feature = "S17")
        val seen = mutableSetOf<String>()
        var height: Rational? = null
        var width: Rational? = null
        val margins = mutableListOf<PageMargins>()
        children { event ->
            when (val name = event.localName()) {
                "page-height", "page-width" -> if (once(seen, name, event)) {
                    val value = tenths(event, name, positive = true)
                    if (name == "page-height") height = value else width = value
                }
                "page-margins" -> pageMargins(event)?.let { added ->
                    if (margins.any { it.type == added.type || it.type == MarginType.BOTH || added.type == MarginType.BOTH }) {
                        invalid(event, "Page margins must be given once for both pages or once each for odd and even pages")
                    } else {
                        margins += added
                    }
                }
                else -> unsupportedTree(event, "S17")
            }
        }
        if (!context.canContinue) return null
        if ((height == null) != (width == null)) {
            invalid(start, "Page height and width must be given together")
            return null
        }
        return PageLayout(height, width, margins.toList())
    }

    private fun pageMargins(start: ScoreXmlEvent): PageMargins? {
        attributes(start, setOf("type"), "S17")
        val type = when (val text = start.attribute("type")) {
            null, "both" -> MarginType.BOTH
            "odd" -> MarginType.ODD
            "even" -> MarginType.EVEN
            else -> {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("type"), "Unknown margin type: $text")
                return null
            }
        }
        val values = layoutValues(start, setOf("left-margin", "right-margin", "top-margin", "bottom-margin"))
        if (!context.canContinue) return null
        val left = values["left-margin"]
        val right = values["right-margin"]
        val top = values["top-margin"]
        val bottom = values["bottom-margin"]
        if (left == null || right == null || top == null || bottom == null) {
            invalid(start, "Page margins require left, right, top and bottom margins")
            return null
        }
        return PageMargins(type, left, right, top, bottom)
    }

    /** Reads system margins (left and right together) and distances; system dividers are S17 discarded. */
    private fun systemLayout(start: ScoreXmlEvent): SystemLayout? {
        attributes(start, feature = "S17")
        val seen = mutableSetOf<String>()
        var margins: Map<String, Rational> = emptyMap()
        var distance: Rational? = null
        var topDistance: Rational? = null
        children { event ->
            when (val name = event.localName()) {
                "system-margins" -> if (once(seen, name, event)) {
                    attributes(event, feature = "S17")
                    margins = layoutValues(event, setOf("left-margin", "right-margin"))
                    if (context.canContinue && margins.size != 2) invalid(event, "System margins require left and right margins")
                }
                "system-distance" -> if (once(seen, name, event)) distance = tenths(event, name)
                "top-system-distance" -> if (once(seen, name, event)) topDistance = tenths(event, name)
                else -> unsupportedTree(event, "S17")
            }
        }
        if (!context.canContinue) return null
        return SystemLayout(margins["left-margin"], margins["right-margin"], distance, topDistance)
    }

    /** Reads a staff-layout's positive staff `number` (default 1) and staff distance. */
    private fun staffLayout(start: ScoreXmlEvent): StaffLayout? {
        attributes(start, setOf("number"), "S17")
        val numberText = start.attribute("number")
        val staff = if (numberText == null) 1 else positiveInt(numberText)
        if (staff == null) {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "Staff-layout number must be a positive Int")
            return null
        }
        val distance = layoutValues(start, setOf("staff-distance"))["staff-distance"]
        if (!context.canContinue) return null
        return StaffLayout(staff, distance)
    }

    /**
     * Reads the children of a layout element named in [names] once each as decimals; other children
     * are S17 discarded. Missing children are absent from the result.
     */
    private fun layoutValues(start: ScoreXmlEvent, names: Set<String>, positive: Boolean = false): Map<String, Rational> {
        val values = mutableMapOf<String, Rational>()
        val seen = mutableSetOf<String>()
        children { event ->
            val name = event.localName()
            if (name != null && name in names) {
                if (once(seen, name, event)) tenths(event, name, positive)?.let { values[name] = it }
            } else {
                unsupportedTree(event, "S17")
            }
        }
        return values
    }

    /** A decimal element value in tenths (millimetres for scaling); null after `INVALID_VALUE`. */
    private fun tenths(start: ScoreXmlEvent, label: String, positive: Boolean = false): Rational? {
        val text = text(start, feature = "S17")
        if (!context.canContinue) return null
        return if (positive) positiveDecimal(text, start.location, label) else decimal(text, start.location, label)
    }

    /**
     * Reads a measure's `print`: system/page breaks, blank pages, page number and layout overrides.
     * Measure numbering, part name displays, `id` and the deprecated `staff-spacing` are S17 discarded.
     */
    private fun print(start: ScoreXmlEvent): PrintHints? {
        attributes(start, setOf("new-system", "new-page", "blank-page", "page-number"), "S17")
        val newSystem = optionalYesNo(start, "new-system")
        val newPage = optionalYesNo(start, "new-page")
        if (!context.canContinue) return null
        val blankPageText = start.attribute("blank-page")
        val blankPages = blankPageText?.let { positiveInt(it) } ?: 0
        if (blankPageText != null && blankPages == 0) {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("blank-page"), "Blank-page must be a positive Int")
            return null
        }
        val seen = mutableSetOf<String>()
        var page: PageLayout? = null
        var system: SystemLayout? = null
        val staves = mutableListOf<StaffLayout>()
        var measureDistance: Rational? = null
        children { event ->
            when (val name = event.localName()) {
                "page-layout" -> if (once(seen, name, event)) page = pageLayout(event)
                "system-layout" -> if (once(seen, name, event)) system = systemLayout(event)
                "staff-layout" -> staffLayout(event)?.let { staves += it }
                "measure-layout" -> if (once(seen, name, event)) {
                    attributes(event, feature = "S17")
                    measureDistance = layoutValues(event, setOf("measure-distance"))["measure-distance"]
                }
                else -> unsupportedTree(event, "S17")
            }
        }
        if (!context.canContinue) return null
        return PrintHints(newSystem, newPage, blankPages, start.attribute("page-number"), page, system, staves.toList(), measureDistance)
    }

    /**
     * Reads the position (`default-x/y`, `relative-x/y` in tenths, possibly negative) and `color`
     * attributes of a printed element; null when it has none. Callers allow [VISUAL_ATTRIBUTES].
     */
    private fun visualHints(start: ScoreXmlEvent): VisualHints? {
        fun position(name: String): Rational? {
            val text = start.attribute(name) ?: return null
            return decimal(text, start.attributeLocation(name), name)
        }
        val defaultX = position("default-x")
        val defaultY = position("default-y")
        val relativeX = position("relative-x")
        val relativeY = position("relative-y")
        val color = start.attribute("color")?.let { text ->
            text.trim { it in WHITESPACE }.uppercase().takeIf { COLOR.matches(it) } ?: run {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("color"), "Color must be #RRGGBB or #AARRGGBB")
                null
            }
        }
        if (!context.canContinue) return null
        return VisualHints(defaultX, defaultY, relativeX, relativeY, color)
            .takeUnless { defaultX == null && defaultY == null && relativeX == null && relativeY == null && color == null }
    }

    /**
     * Declares a part-local instrument with its required name. Abbreviation, sound, solo/ensemble
     * and virtual-instrument data are reported as discarded.
     */
    private fun scoreInstrument(start: ScoreXmlEvent, definition: PartDefinition, path: String) {
        val id = requiredId(start, "instrument") ?: return
        if (id in definition.instruments) {
            invalid(start.attributeLocation("id"), "Duplicate score-instrument ID: $id")
            return
        }
        attributes(start, setOf("id"), "S16")
        val instrument = InstrumentDefinition(id)
        val seen = mutableSetOf<String>()
        children { event ->
            when (val name = event.localName()) {
                "instrument-name" -> if (once(seen, name, event)) instrument.name = text(event, feature = "S16")
                "virtual-instrument" -> unsupportedTree(event, "S26")
                else -> unsupportedTree(event, "S16")
            }
        }
        if (!context.canContinue) return
        if ("instrument-name" !in seen) {
            invalid(start, "A score-instrument requires an instrument-name")
            return
        }
        provenance("$path.instruments[${definition.instruments.size}]", start, definition.id)
        definition.instruments[id] = instrument
    }

    /**
     * Reads the `midi-unpitched` percussion key (1–128, stored zero-based) of a declared
     * score-instrument; other MIDI settings are reported as S26 discarded.
     */
    private fun midiInstrument(start: ScoreXmlEvent, definition: PartDefinition) {
        val id = start.attribute("id")
        val instrument = id?.let { definition.instruments[it] }
        if (instrument == null) {
            invalid(start.attributeLocation("id"), if (id == null) "A midi-instrument requires an id" else "Unknown score-instrument ID: $id")
            return
        }
        attributes(start, setOf("id"), "S26")
        val seen = mutableSetOf<String>()
        children { event ->
            when (val name = event.localName()) {
                "midi-unpitched" -> if (once(seen, name, event)) {
                    val key = positiveInt(text(event, feature = "S26"))?.takeIf { it <= 128 }
                    if (key == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Midi-unpitched must be 1 to 128")
                    else instrument.percussionKey = key - 1
                }
                else -> unsupportedTree(event, "S26")
            }
        }
    }

    private fun partList(start: ScoreXmlEvent) {
        attributes(start, feature = "S01")
        children { event ->
            if (event.localName() != "score-part") {
                unsupportedTree(event)
                return@children
            }
            val id = requiredId(event) ?: return@children
            if (id in definitions) {
                invalid(event.attributeLocation("id"), "Duplicate score-part ID: $id")
                return@children
            }
            val path = "parts[${definitions.size}]"
            val definition = PartDefinition(id, definitions.size, event.location.copy(partId = id))
            definitions[id] = definition
            declaredPartId = id
            attributes(event, setOf("id"), "S01")
            val seen = mutableSetOf<String>()
            children { child ->
                when (val name = child.localName()) {
                    "part-name", "part-abbreviation" -> if (once(seen, name, child)) {
                        val visible = optionalYesNo(child, "print-object") ?: true
                        val value = text(child, setOf("print-object"))
                        if (name == "part-name") {
                            definition.name = value
                            definition.nameVisible = visible
                        } else {
                            definition.abbreviation = value
                            definition.abbreviationVisible = visible
                        }
                        provenance("$path.${if (name == "part-name") "name" else "abbreviation"}", child, id)
                    }
                    "identification" -> if (once(seen, name, child)) {
                        identification(child, "$path.metadata") { definition.metadata += it }
                    }
                    "score-instrument" -> scoreInstrument(child, definition, path)
                    "midi-instrument" -> midiInstrument(child, definition)
                    else -> unsupportedTree(child)
                }
            }
            if (context.canContinue && "part-name" !in seen) invalid(event, "A score-part requires a part-name")
            provenance("$path.id", event, id)
            declaredPartId = null
        }
    }

    private fun part(start: ScoreXmlEvent) {
        val id = partBodyId(start) ?: return
        val definition = definitions[id] ?: return unlistedPart(start, id)
        if (id in bodies) {
            invalid(start.attributeLocation("id"), "Duplicate part body: $id")
            return
        }
        attributes(start, setOf("id"), "S01")
        body = partBody(id).also { bodies[id] = it }
        val measures = body.measures
        provenance("parts[${definition.index}]", start)
        children { event ->
            if (event.localName() == "measure") {
                val header = measureHeader(event) ?: return@children
                val measurePath = "parts[${definition.index}].measures[${measures.size}]"
                provenance(measurePath, event)
                measures += measure(id, measurePath, measures.size + 1, header.number, header.implicit)
            } else {
                unsupportedTree(event)
            }
        }
    }

    /**
     * Normalizes one timewise measure into the partwise projection: each child part's content is
     * read as that part's next measure, with the part's divisions carried from its previous
     * measure. A declared part absent from a measure gets an empty measure there, so every part
     * keeps the timewise measure sequence, as the MusicXML timepart.xsl conversion does.
     */
    private fun timewiseMeasure(start: ScoreXmlEvent) {
        val header = measureHeader(start) ?: return
        val index = timewiseMeasures.size
        timewiseMeasures += header
        val seen = mutableSetOf<String>()
        children { event ->
            if (event.localName() != "part") {
                unsupportedTree(event)
                return@children
            }
            val id = requiredId(event) ?: return@children
            val definition = definitions[id] ?: return@children unlistedPart(event, id)
            if (!seen.add(id)) {
                invalid(event.attributeLocation("id"), "Duplicate part $id in measure ${header.number}")
                return@children
            }
            attributes(event, setOf("id"), "S01")
            body = bodies.getOrPut(id) {
                provenance("parts[${definition.index}]", event)
                partBody(id)
            }
            padMissingMeasures(body, index)
            val measurePath = "parts[${definition.index}].measures[$index]"
            provenance(measurePath, start, id)
            body.measures += measure(id, measurePath, index + 1, header.number, header.implicit)
        }
    }

    /**
     * A partwise part's ID. A part without an `id` in a score declaring a single score-part can
     * only describe that part (W3C suite 41g), so it is bound to it with `RECOVERED_VALUE`, which
     * fails strict parsing; any other missing or invalid ID is fatal.
     */
    private fun partBodyId(start: ScoreXmlEvent): String? {
        val only = definitions.keys.singleOrNull()
        if (start.attribute("id") != null || only == null || bodies.isNotEmpty()) return requiredId(start)
        context.report(
            DiagnosticCode.RECOVERED_VALUE,
            start.attributeLocation("id").copy(partId = only),
            "A part without an id was bound to the only score-part, $only",
        )
        return only
    }

    /**
     * Discards a part whose ID the part-list does not declare. The suite allows readers to ignore
     * such parts (41h), so the score keeps its listed parts; strict parsing rejects the loss.
     */
    private fun unlistedPart(start: ScoreXmlEvent, id: String) {
        unsupportedTree(start, "S01", "Part $id is not declared in the part-list; discarded")
    }

    private fun partBody(id: String) = PartBody(id, SpanResolver(id) { location, message ->
        context.report(DiagnosticCode.UNMATCHED_SPAN, location, message)
    })

    /**
     * Appends empty measures for the timewise measures before [count] that lacked this part. They
     * keep the part's active measure repeat, as they lie between its start and stop.
     */
    private fun padMissingMeasures(part: PartBody, count: Int) {
        val partIndex = definitions.getValue(part.id).index
        while (part.measures.size < count && context.canContinue) {
            val index = part.measures.size
            val header = timewiseMeasures[index]
            provenance("parts[$partIndex].measures[$index]", header.source, part.id)
            part.measures += Measure("measure-${index + 1}", header.number, Rational.ZERO, emptyList(), header.implicit, measureRepeat = part.measureRepeat)
        }
    }

    /** Reads a measure's number and implicit flag; null means a fatal diagnostic was reported. */
    private fun measureHeader(start: ScoreXmlEvent): MeasureHeader? {
        val number = start.attribute("number") ?: run {
            invalid(start.attributeLocation("number"), "A measure requires a number")
            return null
        }
        val implicit = when (start.attribute("implicit")) {
            null, "no" -> false
            "yes" -> true
            else -> {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("implicit"), "Expected yes or no")
                return null
            }
        }
        attributes(start, setOf("number", "implicit"), "S01")
        return MeasureHeader(number, implicit, start)
    }

    /**
     * Interprets notes, chords, directions, attributes and backup/forward at a measure-local
     * quarter-note cursor. A chord tone starts with its reference note (the preceding non-chord
     * note) and never moves the cursor; a cursor move ends the chord. Divisions, the staff count
     * and transpositions persist across the measures of the current part.
     */
    private fun measure(partId: String, path: String, number: Int, displayNumber: String, implicit: Boolean): Measure {
        val events = mutableListOf<Event>()
        var cursor = Rational.ZERO
        var extent = Rational.ZERO
        var reference: ChordReference? = null
        var chordCount = 0
        val wedges = mutableListOf<WedgeEndpoint>()
        val graces = GraceLinker(body.spans)
        val noteLocations = mutableMapOf<Int, SourceLocation>()
        val barlines = mutableListOf<Barline>()
        var print: PrintHints? = null
        var printRead = false
        fun eventId() = "$partId:measure-$number:event-${events.size + 1}"
        fun add(event: Event, source: ScoreXmlEvent) {
            provenance("$path.events[${events.size}]", source)
            events += event
        }
        // A chord ID is allocated when the first chord tone arrives, then given to the reference note too.
        fun chordId(chord: ChordReference): String = chord.id ?: "$partId:measure-$number:chord-${++chordCount}".also { id ->
            chord.id = id
            val index = chord.eventIndex ?: return@also
            (events[index] as? NoteEvent)?.let { events[index] = it.copy(chordId = id) }
        }
        children { event ->
            when (event.localName()) {
                "note" -> {
                    val note = note(event, cursor, reference, eventId()) { chordId(it) } ?: return@children
                    note.event?.let {
                        add(it, event)
                        noteLocations[events.lastIndex] = event.location
                    }
                    // A grace relationship starts at its enclosing note, so it is ordered before the note's nested spans.
                    graces.add(note, SpanPosition(number, note.onset), event.location)
                    note.event?.let { registerSpans(it, note.marks, number) }
                    if (!note.chord) {
                        reference = ChordReference(
                            note.onset, note.end, events.lastIndex.takeIf { note.event != null }, note.grace, note.event is RestEvent,
                        )
                        cursor = note.end
                    }
                    if (note.end > extent) extent = note.end
                }
                "attributes" -> attributesElement(event) { attribute, staff, visible, source ->
                    add(AttributeEvent(eventId(), cursor, attribute, staff, visible), source)
                }
                "direction" -> {
                    val direction = direction(event, cursor, eventId()) ?: return@children
                    direction.event?.let {
                        add(it, event)
                        wedges += wedgeEndpoints(it, direction.wedges, SpanPosition(number, direction.wedgeOnset))
                    }
                    // An instrument change applies to the whole part, so each staff's state follows it.
                    direction.instrumentChanges.forEach { change ->
                        for (staff in 1..body.staffCount) add(AttributeEvent(eventId(), change.onset, change.attribute, staff), change.source)
                    }
                }
                "backup" -> {
                    val move = cursorMove(event, forward = false) ?: return@children
                    reference = null
                    val target = exact(event.location) { cursor - move } ?: return@children
                    cursor = if (target < Rational.ZERO) {
                        context.report(
                            DiagnosticCode.INVALID_CURSOR_MOVE, event.location.withDeclaredPart(),
                            "Backup of $move quarters from offset $cursor moves before the measure start; the cursor is clamped to 0",
                        )
                        Rational.ZERO
                    } else {
                        target
                    }
                }
                "forward" -> {
                    val move = cursorMove(event, forward = true) ?: return@children
                    reference = null
                    cursor = exact(event.location) { cursor + move } ?: return@children
                    if (cursor > extent) extent = cursor
                }
                "barline" -> barline(event, cursor)?.let { read ->
                    provenance("$path.barlines[${barlines.size}]", event)
                    barlines += read.barline
                    read.barline.ending?.let { resolveEnding(it, "measure-$number", checkNotNull(read.endingSource)) }
                }
                "print" -> if (printRead) {
                    unsupportedTree(event, "S17", "Only the first print in a measure is kept")
                } else {
                    printRead = true
                    print = print(event)?.also { provenance("$path.print", event) }
                }
                else -> unsupportedTree(event)
            }
        }
        registerWedges(wedges)
        graces.finish()
        deriveSoundingPitches(events, noteLocations)
        return Measure(
            "measure-$number", displayNumber, extent, events.toList(), implicit, barlines.toList(), body.measureRepeat, print,
        )
    }

    /**
     * Interprets divisions, staves and key, time, clef, staff-line and transpose state. Each state
     * change becomes an [AttributeEvent] at the current cursor offset, in source order, so
     * intra-measure changes apply exactly where they occur. A change without a `number` applies to
     * every staff (one event per staff, once this element's `staves` is known), except a clef, which
     * defaults to staff 1. A `number` beyond the declared staves is `INVALID_VALUE`. A key, time or
     * clef with `print-object="no"` still changes state; its event is only marked not visible.
     */
    private fun attributesElement(start: ScoreXmlEvent, add: (Attribute, Int, Boolean, ScoreXmlEvent) -> Unit) {
        attributes(start, feature = "S07")
        val changes = mutableListOf<StaffChange>()
        fun scoped(event: ScoreXmlEvent, label: String, allStaves: Boolean, parse: (ScoreXmlEvent) -> Attribute?) {
            val number = event.attribute("number")
            val staff = if (number == null) {
                if (allStaves) null else 1
            } else {
                positiveInt(number) ?: run {
                    context.report(DiagnosticCode.INVALID_VALUE, event.attributeLocation("number"), "$label number must be a positive Int")
                    skipTree()
                    return
                }
            }
            // Only key, time and clef keep print-object; staff-details reports it as discarded.
            val visible = if (event.localName() in PRINTABLE_ATTRIBUTES) optionalYesNo(event, "print-object") ?: true else true
            parse(event)?.let { changes += StaffChange(it, staff, event, label, visible) }
        }
        children { event ->
            when (val name = event.localName()) {
                "divisions" -> positiveDecimal(event, "S05", "Divisions")?.let { body.divisions = it }
                "key" -> scoped(event, "Key signature", allStaves = true, ::key)
                "time" -> scoped(event, "Time signature", allStaves = true, ::time)
                "clef" -> scoped(event, "Clef", allStaves = false, ::clef)
                "staves" -> staves(event)
                "staff-details" -> scoped(event, "Staff details", allStaves = true, ::staffDetails)
                "transpose" -> scoped(event, "Transpose", allStaves = true, ::transpose)
                "measure-style" -> measureStyle(event)
                else -> unsupportedTree(event, ATTRIBUTES_CHILD_FEATURES[name] ?: "S07")
            }
        }
        for (change in changes) {
            if (!context.canContinue) return
            val staff = change.staff
            if (staff != null && staff > body.staffCount) {
                context.report(
                    DiagnosticCode.INVALID_VALUE, change.source.attributeLocation("number").withDeclaredPart(),
                    "${change.label} number $staff exceeds the ${body.staffCount} declared staves",
                )
                return
            }
            val staves = staff?.let { listOf(it) } ?: (1..body.staffCount).toList()
            staves.forEach { add(change.attribute, it, change.visible, change.source) }
            (change.attribute as? Attribute.Transpose)?.takeIf { isAmbiguous(it.transposition) }?.let {
                unsupported(change.source.location, "S16", "A non-octave transpose in a concert score is ambiguous; sounding pitch is not derived")
            }
        }
    }

    /**
     * Interprets a backup or forward duration in quarter notes using the current divisions. A forward
     * gap is the absence of events, so its optional voice and staff are validated but not stored.
     */
    private fun cursorMove(start: ScoreXmlEvent, forward: Boolean): Rational? {
        attributes(start, feature = "S06")
        val seen = mutableSetOf<String>()
        var durationEvent: ScoreXmlEvent? = null
        var duration: Rational? = null
        children { event ->
            when (val name = event.localName()) {
                "duration" -> if (once(seen, name, event)) {
                    durationEvent = event
                    duration = positiveDecimal(event, "S06", "Duration")
                }
                "voice" -> if (!forward) unsupportedTree(event, "S06") else if (once(seen, name, event)) voiceLabel(event, "S06")
                "staff" -> if (!forward) unsupportedTree(event, "S06") else if (once(seen, name, event)) staffNumber(event, "S06")
                "footnote", "level" -> unsupportedTree(event, "S25")
                else -> unsupportedTree(event, "S06")
            }
        }
        if (!context.canContinue) return null
        val durationSource = durationEvent ?: run {
            invalid(start, "A ${start.localName()} requires a duration")
            return null
        }
        val sourceDuration = duration ?: return null
        val currentDivisions = divisionsOrDefault(body)
        return exact(durationSource.location) { sourceDuration / currentDivisions }
    }

    /**
     * Interprets a time signature: one or more beats/beat-type fractions, each with an additive
     * numerator such as `3+2`, an optional `interchangeable` alternate, or `senza-misura`, with the
     * display `symbol` and `separator`. A main form that is not positive integer fractions is
     * reported as discarded with the whole time; an unreadable alternate is discarded alone. An
     * unknown symbol, separator or time relation is `INVALID_VALUE`; an overflowing length is
     * `NUMERIC_OVERFLOW`.
     */
    private fun time(start: ScoreXmlEvent): Attribute.TimeSignature? {
        attributes(start, setOf("number", "symbol", "separator", "print-object"), "S07")
        val symbol = timeSymbol(start)
        val separator = timeSeparator(start)
        val parts = mutableListOf<Pair<String, String>>()
        var interchangeable: InterchangeableTime? = null
        var interchangeables = 0
        var senzaMisura: SenzaMisura? = null
        var senzaMisuras = 0
        var readable = true
        children { event ->
            when (val name = event.localName()) {
                "beats", "beat-type" -> parts += name to text(event, feature = "S07")
                "interchangeable" -> {
                    interchangeables++
                    interchangeable = interchangeable(event)
                }
                "senza-misura" -> {
                    senzaMisuras++
                    senzaMisura = SenzaMisura(text(event, feature = "S07").trim { it in WHITESPACE }.ifEmpty { null })
                }
                // Foreign extensions are reported on their own and keep the understood meter.
                null -> unsupportedTree(event)
                else -> {
                    readable = false
                    skipTree()
                }
            }
        }
        if (!context.canContinue) return null
        val fractions = timeFractions(parts)
        val understood = readable && interchangeables <= 1 && when (senzaMisuras) {
            0 -> !fractions.isNullOrEmpty()
            1 -> parts.isEmpty() && interchangeables == 0
            else -> false
        }
        if (!understood) {
            unsupported(start.location, "S07", "Only positive integer beats/beat-type fractions or senza-misura are interpreted; time discarded")
            return null
        }
        var signature: Attribute.TimeSignature? = null
        exact(start.location) {
            Attribute.TimeSignature(fractions.orEmpty(), symbol, separator, interchangeable, senzaMisura).also { signature = it }.length
        }
        return signature
    }

    /**
     * Interprets an interchangeable alternate meter with its `time-relation`, `symbol` and
     * `separator`. Fractions that are not positive integers are reported and the alternate
     * discarded; the main meter is kept.
     */
    private fun interchangeable(start: ScoreXmlEvent): InterchangeableTime? {
        attributes(start, setOf("symbol", "separator"), "S07")
        val symbol = timeSymbol(start)
        val separator = timeSeparator(start)
        val parts = mutableListOf<Pair<String, String>>()
        val seen = mutableSetOf<String>()
        var relation: TimeRelation? = null
        children { event ->
            when (val name = event.localName()) {
                "time-relation" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S07")
                    relation = TIME_RELATIONS[text.trim { it in WHITESPACE }]
                    if (relation == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown time relation: $text")
                }
                "beats", "beat-type" -> parts += name to text(event, feature = "S07")
                else -> unsupportedTree(event, "S07")
            }
        }
        if (!context.canContinue) return null
        val fractions = timeFractions(parts)?.takeIf { it.isNotEmpty() } ?: run {
            unsupported(start.location, "S07", "Only positive integer beats/beat-type fractions are interpreted; alternate discarded")
            return null
        }
        return InterchangeableTime(fractions, relation, symbol, separator)
    }

    private fun timeSymbol(start: ScoreXmlEvent): TimeSymbol? {
        val text = start.attribute("symbol") ?: return null
        return TIME_SYMBOLS[text.trim { it in WHITESPACE }] ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("symbol"), "Unknown time symbol: $text")
            null
        }
    }

    private fun timeSeparator(start: ScoreXmlEvent): TimeSeparator? {
        val text = start.attribute("separator") ?: return null
        return TIME_SEPARATORS[text.trim { it in WHITESPACE }] ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("separator"), "Unknown time separator: $text")
            null
        }
    }

    /**
     * Interprets a traditional key (optional `cancel` with its `location`, `fifths` and optional
     * `mode`) or a non-traditional key (`key-step`/`key-alter` pairs in order, each with an optional
     * `key-accidental`), either with optional `key-octave` placements. Every value is display or key
     * data only and never changes a note's written pitch. Mixing the two forms, or a step, alter or
     * accidental out of sequence, is `INVALID_STRUCTURE`; an unknown mode is discarded alone.
     */
    private fun key(start: ScoreXmlEvent): Attribute.KeySignature? {
        attributes(start, setOf("number", "print-object"), "S07")
        val seen = mutableSetOf<String>()
        var fifths: Int? = null
        var mode: KeyMode? = null
        var cancel: KeyCancel? = null
        val alterations = mutableListOf<KeyAlteration>()
        val octaves = mutableListOf<KeyOctave>()
        var nonTraditional = false
        // The step awaiting its alter, and whether the last child completed an alteration.
        var pendingStep: Step? = null
        var stepPending = false
        var accidentalAllowed = false
        children { event ->
            val name = event.localName()
            val completesAlteration = name == "key-alter"
            when (name) {
                "fifths" -> if (once(seen, name, event)) {
                    fifths = integer(event, "Fifths")
                }
                "mode" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S07").trim { it in WHITESPACE }
                    mode = KeyMode.entries.firstOrNull { it.name.lowercase() == text }
                    if (mode == null) unsupported(event.location, "S07", "Key mode $text is not interpreted; mode discarded")
                }
                "cancel" -> if (once(seen, name, event)) cancel = keyCancel(event)
                "key-step" -> {
                    nonTraditional = true
                    if (stepPending) invalid(event, "A key-step requires a key-alter")
                    val text = text(event, feature = "S07").trim { it in WHITESPACE }
                    pendingStep = Step.entries.firstOrNull { it.name == text }
                    if (pendingStep == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Key step must be A to G")
                    stepPending = true
                }
                "key-alter" -> {
                    nonTraditional = true
                    if (!stepPending) invalid(event, "A key-alter must follow a key-step")
                    val alter = decimal(event, "S07", "Key alter")
                    val step = pendingStep
                    if (step != null && alter != null) alterations += KeyAlteration(step, alter)
                    stepPending = false
                }
                "key-accidental" -> {
                    nonTraditional = true
                    if (!accidentalAllowed) invalid(event, "A key-accidental must follow a key-alter")
                    val text = text(event, feature = "S07").trim { it in WHITESPACE }
                    if (text.isBlank()) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Key accidental must be nonblank")
                    else if (alterations.isNotEmpty()) alterations[alterations.lastIndex] = alterations.last().copy(accidental = text)
                }
                "key-octave" -> keyOctave(event)?.let { octaves += it }
                else -> unsupportedTree(event, "S07")
            }
            accidentalAllowed = completesAlteration
        }
        if (!context.canContinue) return null
        if (stepPending) invalid(start, "A key-step requires a key-alter")
        if (nonTraditional && seen.isNotEmpty()) invalid(start, "A key is either traditional or non-traditional")
        if (!context.canContinue) return null
        if (nonTraditional) return Attribute.KeySignature.nonTraditional(alterations.toList(), octaves.toList())
        val keyFifths = fifths ?: run {
            invalid(start, "A traditional key requires fifths")
            return null
        }
        return Attribute.KeySignature(keyFifths, mode, cancel, octaves = octaves.toList())
    }

    /** Interprets the cancelled fifths of the previous key and their display `location`. */
    private fun keyCancel(start: ScoreXmlEvent): KeyCancel? {
        val locationText = start.attribute("location")
        val location = locationText?.let { text ->
            KEY_CANCEL_LOCATIONS[text.trim { it in WHITESPACE }] ?: run {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("location"), "Unknown cancel location: $text")
                return null
            }
        }
        val text = text(start, setOf("location"), "S07")
        if (!context.canContinue) return null
        val fifths = text.trim { it in WHITESPACE }.toIntOrNull() ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Cancel must be an Int")
            return null
        }
        return KeyCancel(fifths, location)
    }

    /** Interprets the displayed octave (0–9) of the key or cancel accidental counted by `number`. */
    private fun keyOctave(start: ScoreXmlEvent): KeyOctave? {
        val number = start.attribute("number")?.let(::positiveInt) ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "Key octave number must be a positive Int")
            return null
        }
        val cancel = yesNo(start, "cancel") ?: return null
        val text = text(start, setOf("number", "cancel"), "S07")
        if (!context.canContinue) return null
        val octave = text.trim { it in WHITESPACE }.toIntOrNull()?.takeIf { it in 0..9 } ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Key octave must be 0 to 9")
            return null
        }
        return KeyOctave(number, octave, cancel)
    }

    /** Interprets a clef sign, line and octave change; a missing G/F/C line uses its standard line. */
    private fun clef(start: ScoreXmlEvent): Attribute.Clef? {
        attributes(start, setOf("number", "print-object"), "S07")
        val seen = mutableSetOf<String>()
        var sign: ClefSign? = null
        var line: Int? = null
        var octaveChange = 0
        children { event ->
            when (val name = event.localName()) {
                "sign" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S07").trim { it in WHITESPACE }
                    sign = CLEF_SIGNS[text]
                    if (sign == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown clef sign: $text")
                }
                "line" -> if (once(seen, name, event)) line = integer(event, "Clef line")
                "clef-octave-change" -> if (once(seen, name, event)) {
                    integer(event, "Clef octave change")?.let { octaveChange = it }
                }
                else -> unsupportedTree(event, "S07")
            }
        }
        if (!context.canContinue) return null
        val clefSign = sign ?: run {
            invalid(start, "A clef requires a sign")
            return null
        }
        return Attribute.Clef(clefSign, line ?: STANDARD_CLEF_LINES[clefSign], octaveChange)
    }

    /**
     * Sets the staff count for later content of the current part. The part declares staves up to
     * its largest count, even when it has no events. Declared staves across all parts are bounded
     * before any count is applied, since staves and unnumbered attributes expand per staff.
     */
    private fun staves(start: ScoreXmlEvent) {
        val text = text(start, feature = "S08")
        if (!context.canContinue) return
        when (val count = positiveInt(text)) {
            null -> context.report(DiagnosticCode.INVALID_VALUE, start.location, "Staves must be a positive Int")
            else -> {
                val otherStaves = declaredStaves.entries.sumOf { (id, staves) -> if (id == body.id) 0L else staves.toLong() }
                if (otherStaves + count > context.options.limits.maxStaves) {
                    context.report(DiagnosticCode.STAFF_LIMIT, start.location, "Declared staff limit exceeded")
                    return
                }
                body.staffCount = count
                declaredStaves[body.id] = maxOf(declaredStaves[body.id] ?: 0, count)
            }
        }
    }

    /** Interprets the staff-line count; other staff details are reported as discarded. */
    private fun staffDetails(start: ScoreXmlEvent): Attribute.StaffLines? {
        attributes(start, setOf("number"), "S17")
        val seen = mutableSetOf<String>()
        var lines: Int? = null
        children { event ->
            when (val name = event.localName()) {
                "staff-lines" -> if (once(seen, name, event)) {
                    lines = text(event, feature = "S17").trim { it in WHITESPACE }.toIntOrNull()?.takeIf { it >= 0 }
                    if (lines == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Staff lines must be a non-negative Int")
                }
                "staff-tuning", "capo" -> unsupportedTree(event, "S23")
                else -> unsupportedTree(event, "S17")
            }
        }
        if (!context.canContinue) return null
        return lines?.let { Attribute.StaffLines(it) }
    }

    /**
     * Interprets a written-to-sounding transposition: exact `chromatic` semitones, optional
     * `diatonic` steps and `octave-change`. A `double` (octave doubling) is reported as discarded.
     */
    private fun transpose(start: ScoreXmlEvent): Attribute.Transpose? {
        attributes(start, setOf("number"), "S16")
        val seen = mutableSetOf<String>()
        var chromatic: Rational? = null
        var diatonic: Int? = null
        var octaveChange = 0
        children { event ->
            when (val name = event.localName()) {
                "diatonic" -> if (once(seen, name, event)) diatonic = integer(event, "Diatonic", "S16")
                "chromatic" -> if (once(seen, name, event)) chromatic = decimal(event, "S16", "Chromatic")
                "octave-change" -> if (once(seen, name, event)) {
                    integer(event, "Octave change", "S16")?.let { octaveChange = it }
                }
                "double" -> unsupportedTree(event, "S16", "Octave doubling is not interpreted; only the primary sounding pitch is derived")
                else -> unsupportedTree(event, "S16")
            }
        }
        if (!context.canContinue) return null
        val semitones = chromatic ?: run {
            // A present but invalid value has already been reported.
            if ("chromatic" !in seen) invalid(start, "A transpose requires chromatic")
            return null
        }
        return Attribute.Transpose(Transposition(semitones, diatonic, octaveChange))
    }

    /**
     * Whether [transposition] is ambiguous: a concert score may only carry octave transpositions, so
     * a non-octave one there is kept as written, and sounding pitch on its staves stays unknown
     * until their next transpose.
     */
    private fun isAmbiguous(transposition: Transposition): Boolean =
        concertScore && (transposition.chromatic != Rational.ZERO || (transposition.diatonic ?: 0) != 0)

    /**
     * Gives each pitched note in [events] its sounding pitch under its staff's transposition at the
     * note's onset; [locations] holds each note's source by event index. Events are visited in onset
     * order, keeping source order at equal onsets as `effectiveStates` does, so a transpose
     * reached after a backup applies only from its own offset. Each staff's transposition at the
     * end of the measure carries into the next.
     */
    private fun deriveSoundingPitches(events: MutableList<Event>, locations: Map<Int, SourceLocation>) {
        for (index in events.indices.sortedBy { events[it].onset }) {
            when (val event = events[index]) {
                is AttributeEvent -> (event.attribute as? Attribute.Transpose)?.let { transpose ->
                    body.transpositions[event.staff] = transpose.transposition
                    if (isAmbiguous(transpose.transposition)) body.ambiguousStaves += event.staff else body.ambiguousStaves -= event.staff
                }
                is NoteEvent -> (event.pitch as? NotePitch.Pitched)?.let { pitch ->
                    val sounding = sounding(pitch.written, event.staff, locations.getValue(index))
                    events[index] = event.copy(pitch = pitch.copy(sounding = sounding))
                }
                else -> Unit
            }
        }
    }

    /**
     * The sounding pitch of [written] on [staff] under that staff's current transposition: written
     * semitones plus chromatic plus 12 per octave change. A staff without a transpose sounds as
     * written. Null when the transposition is ambiguous, or when the result is outside the exact
     * range; the latter is reported, since the written pitch itself is still valid.
     */
    private fun sounding(written: WrittenPitch, staff: Int, location: SourceLocation): SoundingPitch? {
        if (staff in body.ambiguousStaves) return null
        val transposition = body.transpositions[staff]
        return try {
            val base = Rational.of((written.octave + 1) * 12 + STEP_SEMITONES.getValue(written.step)) + written.alter
            SoundingPitch(
                if (transposition == null) base
                else base + transposition.chromatic + Rational.of(12) * Rational.of(transposition.octaveChange),
            )
        } catch (exception: RationalException) {
            unsupported(location, "S16", "Sounding pitch is outside the supported exact range; it is not derived")
            null
        }
    }

    /**
     * Reads a barline's location, style, repeat sign and ending mark as notated. Segno, coda,
     * fermata and wavy-line children, playback attributes and display attributes are S15
     * discarded. A middle barline sits at the cursor.
     */
    private fun barline(start: ScoreXmlEvent, cursor: Rational): ReadBarline? {
        val location = when (val text = start.attribute("location")) {
            null, "right" -> BarlineLocation.RIGHT
            "left" -> BarlineLocation.LEFT
            "middle" -> BarlineLocation.MIDDLE
            else -> {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("location"), "Unknown barline location: $text")
                return null
            }
        }
        attributes(start, setOf("location"), "S15")
        val seen = mutableSetOf<String>()
        var style: BarStyle? = null
        var repeat: Repeat? = null
        var ending: EndingMark? = null
        var endingSource: ScoreXmlEvent? = null
        children { event ->
            when (val name = event.localName()) {
                "bar-style" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S15").trim { it in WHITESPACE }
                    style = BAR_STYLES[text]
                    if (style == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown bar-style: $text")
                }
                "repeat" -> if (once(seen, name, event)) repeat = repeat(event)
                "ending" -> if (once(seen, name, event)) {
                    ending = ending(event)
                    endingSource = event
                }
                "segno", "coda" -> unsupportedTree(event, "S15", "Barline $name navigation is not interpreted; it is discarded")
                "fermata", "wavy-line" -> unsupportedTree(event, "S15", "Barline $name is not interpreted; it is discarded")
                "footnote", "level" -> unsupportedTree(event, "S25")
                else -> unsupportedTree(event, "S15")
            }
        }
        if (!context.canContinue) return null
        return ReadBarline(Barline(location, cursor.takeIf { location == BarlineLocation.MIDDLE }, style, repeat, ending), endingSource)
    }

    /** A repeat's direction and `times`; `after-jump` and `winged` are S15 discarded. */
    private fun repeat(start: ScoreXmlEvent): Repeat? {
        attributes(start, setOf("direction", "times"), "S15")
        children { event -> unsupportedTree(event, "S15") }
        val direction = when (val text = start.attribute("direction")) {
            null -> {
                invalid(start, "A repeat requires a direction")
                return null
            }
            "forward" -> RepeatDirection.FORWARD
            "backward" -> RepeatDirection.BACKWARD
            else -> {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("direction"), "Unknown repeat direction: $text")
                return null
            }
        }
        val times = start.attribute("times")?.let { text ->
            text.trim { it in WHITESPACE }.toIntOrNull()?.takeIf { it >= 0 } ?: run {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("times"), "Repeat times must be a non-negative Int")
                return null
            }
        }
        return Repeat(direction, times)
    }

    /** An ending's pass numbers, type and differing text; display attributes are S15 discarded. */
    private fun ending(start: ScoreXmlEvent): EndingMark? {
        val text = text(start, setOf("number", "type"), "S15").trim { it in WHITESPACE }
        if (!context.canContinue) return null
        val number = start.attribute("number") ?: run {
            invalid(start, "An ending requires a number")
            return null
        }
        val numbers = endingNumbers(number) ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "Invalid ending number: $number")
            return null
        }
        val type = when (val value = start.attribute("type")) {
            null -> {
                invalid(start, "An ending requires a type")
                return null
            }
            "start" -> EndingType.START
            "stop" -> EndingType.STOP
            "discontinue" -> EndingType.DISCONTINUE
            else -> {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("type"), "Unknown ending type: $value")
                return null
            }
        }
        return EndingMark(type, numbers, text.ifEmpty { null })
    }

    /**
     * Pairs ending marks in part order into whole-measure endings. Endings cannot nest: a start
     * while another ending is open leaves that one unstopped. A stop whose numbers differ from the
     * open ending, or with nothing open, is an orphan. Both cases are `UNMATCHED_SPAN`; the marks
     * stay on their barlines and an unstopped ending keeps a null stop.
     */
    private fun resolveEnding(mark: EndingMark, measureId: String, source: ScoreXmlEvent) {
        val open = body.openEnding
        if (mark.type == EndingType.START) {
            if (open != null) reportOpenEnding(body, open)
            val index = body.endings.size
            provenance("parts[${definitions.getValue(body.id).index}].endings[$index]", source)
            body.openEnding = index to source
            body.endings += Ending("${body.id}:ending-${index + 1}", mark.numbers, measureId, null, mark.text)
            return
        }
        val ending = open?.let { body.endings[it.first] }
        if (open == null || ending == null || ending.numbers != mark.numbers) {
            val numbers = mark.numbers.joinToString(",")
            context.report(DiagnosticCode.UNMATCHED_SPAN, source.location.withDeclaredPart(), "Ending $numbers stop has no matching start")
            return
        }
        body.endings[open.first] = ending.copy(stopMeasureId = measureId, discontinued = mark.type == EndingType.DISCONTINUE)
        body.openEnding = null
    }

    /** Reports an ending still open at the end of its part. */
    private fun finishEndings(part: PartBody) {
        part.openEnding?.let { reportOpenEnding(part, it) }
        part.openEnding = null
    }

    private fun reportOpenEnding(part: PartBody, open: Pair<Int, ScoreXmlEvent>) {
        val numbers = part.endings[open.first].numbers.joinToString(",")
        context.report(DiagnosticCode.UNMATCHED_SPAN, open.second.location.withDeclaredPart(), "Ending $numbers has no stop")
    }

    /**
     * Interprets a `measure-repeat`: a start marks this and following measures until a stop, which
     * is the first measure without the sign. Multiple rests, beat repeats and slashes stay S24
     * discarded. A stop with no sign in effect is `UNMATCHED_SPAN`. The sign marks whole measures,
     * so a style numbered for one staff of a multi-staff part is S24 discarded.
     */
    private fun measureStyle(start: ScoreXmlEvent) {
        attributes(start, setOf("number"), "S24")
        val number = start.attribute("number")
        if (number != null) {
            val staff = positiveInt(number)
            if (staff == null || staff > body.staffCount) {
                val message = if (staff == null) "Measure style number must be a positive Int"
                else "Measure style number $staff exceeds the ${body.staffCount} declared staves"
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), message)
                skipTree()
                return
            }
            if (body.staffCount > 1) {
                unsupportedTree(start, "S24", "A measure style for staff $staff alone is not represented; it is discarded")
                return
            }
        }
        children { event ->
            if (event.localName() != "measure-repeat") {
                unsupportedTree(event, "S24")
                return@children
            }
            val text = text(event, setOf("type", "slashes"), "S24").trim { it in WHITESPACE }
            if (!context.canContinue) return@children
            val slashes = event.attribute("slashes")?.let { value ->
                positiveInt(value) ?: run {
                    context.report(DiagnosticCode.INVALID_VALUE, event.attributeLocation("slashes"), "Slashes must be a positive Int")
                    return@children
                }
            } ?: 1
            when (val type = event.attribute("type")) {
                "start" -> {
                    // An empty count means one measure (positive-integer-or-empty).
                    val measures = if (text.isEmpty()) 1 else positiveInt(text) ?: run {
                        context.report(DiagnosticCode.INVALID_VALUE, event.location, "A measure-repeat start count must be a positive Int")
                        return@children
                    }
                    body.measureRepeat = MeasureRepeat(measures, slashes)
                }
                "stop" -> if (body.measureRepeat == null) {
                    context.report(DiagnosticCode.UNMATCHED_SPAN, event.location.withDeclaredPart(), "Measure-repeat stop has no matching start")
                } else {
                    body.measureRepeat = null
                }
                null -> invalid(event, "A measure-repeat requires a type")
                else -> context.report(DiagnosticCode.INVALID_VALUE, event.attributeLocation("type"), "Unknown measure-repeat type: $type")
            }
        }
    }

    private fun integer(start: ScoreXmlEvent, label: String, feature: String = "S07"): Int? {
        val text = text(start, feature = feature)
        if (!context.canContinue) return null
        val value = text.trim { it in WHITESPACE }.toIntOrNull()
        if (value == null) context.report(DiagnosticCode.INVALID_VALUE, start.location, "$label must be an Int")
        return value
    }

    /**
     * Parses a note at the [cursor], or, for a chord tone, at the onset of its [reference] note; a
     * chord tone keeps its own duration and staff and gets the chord ID from [chordId]. A null result
     * means a fatal diagnostic was reported. A grace note has no duration: it is placed at its onset
     * and leaves the cursor there; a `duration` on it is reported and ignored. A grace chord tone's
     * reference is the preceding grace note.
     */
    private fun note(
        start: ScoreXmlEvent,
        cursor: Rational,
        reference: ChordReference?,
        id: String,
        chordId: (ChordReference) -> String,
    ): TimedNote? {
        attributes(start, VISUAL_ATTRIBUTES + NOTE_PRINT_ATTRIBUTES, "S04")
        val visual = visualHints(start)
        val visibility = noteVisibility(start)
        if (!context.canContinue) return null
        val seen = mutableSetOf<String>()
        var content: NoteContent? = null
        var durationText = ""
        var durationEvent: ScoreXmlEvent? = null
        var role = NoteRole.NORMAL
        var grace: Grace? = null
        var cue: ScoreXmlEvent? = null
        var voice = "1"
        var staff = 1
        var chord = false
        var value: NoteValue? = null
        var dots = 0
        var accidental: Accidental? = null
        var timeModification: TimeModification? = null
        var stem: StemDirection? = null
        val beams = mutableListOf<Beam>()
        var instrumentId: String? = null
        var instrumentLocation: SourceLocation? = null
        val marks = mutableListOf<SpanMark>()
        val expressions = mutableListOf<NoteExpression>()
        val lyrics = mutableListOf<Lyric>()
        children { event ->
            when (val name = event.localName()) {
                "chord" -> if (once(seen, name, event)) {
                    attributes(event, feature = "S06")
                    skipTree()
                    chord = true
                }
                "grace" -> if (once(seen, name, event)) grace = grace(event)
                "cue" -> if (once(seen, name, event)) {
                    attributes(event, feature = "S18")
                    skipTree()
                    cue = event
                    role = NoteRole.CUE
                }
                "pitch", "unpitched", "rest" -> if ("content" in seen) {
                    invalid(event, "A note has exactly one of pitch, unpitched or rest")
                } else {
                    seen += "content"
                    when (name) {
                        "pitch" -> content = pitch(event)
                        "rest" -> content = rest(event)
                        else -> content = unpitched(event)
                    }
                }
                "instrument" -> noteInstrument(event)?.let { id ->
                    instrumentLocation = instrumentLocation ?: event.location
                    if (instrumentId == null) instrumentId = id
                    else unsupported(event.location, "S16", "A note shared by several instruments keeps only its first instrument")
                }
                "duration" -> if (once(seen, name, event)) {
                    durationEvent = event
                    // Validated only once the note is known not to be a grace note.
                    durationText = text(event, feature = "S05")
                }
                "voice" -> if (once(seen, name, event)) voiceLabel(event, "S06")?.let { voice = it }
                "type" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S05").trim { it in WHITESPACE }
                    value = NOTE_TYPES[text]
                    if (value == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown note type: $text")
                }
                "dot" -> {
                    attributes(event, feature = "S17")
                    skipTree()
                    dots++
                }
                "accidental" -> if (once(seen, name, event)) accidental = accidental(event)
                "time-modification" -> if (once(seen, name, event)) timeModification = timeModification(event)
                "stem" -> if (once(seen, name, event)) stem = stem(event)
                "beam" -> beam(event)?.let { beam ->
                    if (beams.any { it.level == beam.level }) invalid(event, "Duplicate beam number ${beam.level}")
                    else beams += beam
                }
                "tie" -> tie(event)?.let { marks += it }
                "notations" -> notations(event, marks, expressions)
                "lyric" -> lyric(event)?.let { lyrics += it }
                "staff" -> if (once(seen, name, event)) staffNumber(event, "S06")?.let { staff = it }
                else -> unsupportedTree(event, NOTE_CHILD_FEATURES[name] ?: "S04")
            }
        }
        if (!context.canContinue) return null
        if ("content" !in seen) {
            invalid(start, "A note requires pitch, unpitched or rest")
            return null
        }
        val isGrace = "grace" in seen
        val chordReference = if (chord) {
            reference ?: run {
                invalid(start, "A chord note requires a preceding non-chord note in the measure without an intervening cursor move")
                return null
            }
        } else {
            null
        }
        if (chordReference != null && chordReference.grace != isGrace) {
            invalid(start, "A chord tone must be a grace note exactly when its reference note is")
            return null
        }
        if (chordReference != null && (content is NoteContent.Rest || chordReference.rest)) {
            unsupported(start.location, "S06", "Chord grouping with a rest is not represented; members keep their shared onset")
        }
        val onset = chordReference?.onset ?: cursor
        // With a single score-instrument, MusicXML implies it for notes without an instrument element.
        val noteInstrumentId = instrumentId ?: definitions.getValue(body.id).instruments.keys.singleOrNull()
        if (isGrace) {
            durationEvent?.let { unsupported(it.location, "S18", "A grace note has no duration; the duration is ignored") }
            cue?.let { unsupported(it.location, "S18", "Cue size of a grace note is not interpreted; it is discarded") }
            if (content is NoteContent.Rest) unsupported(start.location, "S18", "A grace rest is not interpreted; the note is discarded")
            val pitch = (content as? NoteContent.Pitch)?.pitch
            if (pitch == null || grace == null || !context.canContinue) return TimedNote(null, onset, onset, emptyList(), chord = chord, grace = true)
            val notation = NotatedValue(value, dots, timeModification, accidental, stem, beams.toList(), expressions.toList(), lyrics.toList())
            val event = NoteEvent(
                id, onset, null, pitch, staff = staff, voice = voice, notation = notation,
                role = NoteRole.GRACE, chordId = chordReference?.let(chordId), instrumentId = noteInstrumentId, grace = grace,
                visual = visual, visibility = visibility,
            )
            return if (context.canContinue) TimedNote(event, onset, onset, marks.toList(), chord = chord, grace = true) else null
        }
        val durationSource = durationEvent ?: run {
            invalid(start, "A non-grace note requires a duration")
            return null
        }
        val currentDivisions = divisionsOrDefault(body)
        val sourceDuration = positiveDecimal(durationText, durationSource.location, "Duration") ?: return null
        val quarters = exact(durationSource.location) { sourceDuration / currentDivisions } ?: return null
        val end = exact(durationSource.location) { onset + quarters } ?: return null
        if (chordReference != null && end > chordReference.end) {
            context.report(
                DiagnosticCode.INCONSISTENT_CHORD_DURATION, durationSource.location.withDeclaredPart(),
                "Chord tone ends at $end, after its reference note (${chordReference.end}); " +
                    "its duration is kept and the cursor follows the reference note",
            )
        }
        val notation = NotatedValue(value, dots, timeModification, accidental, stem, beams.toList(), expressions.toList(), lyrics.toList())
        val event = when (val noteContent = content ?: return null) {
            is NoteContent.Rest -> {
                instrumentLocation?.let { unsupported(it, "S16", "An instrument on a rest is not represented; it is discarded") }
                RestEvent(
                    id, onset, quarters, staff = staff, voice = voice, notation = notation, wholeMeasure = noteContent.wholeMeasure,
                    visual = visual, visibility = visibility,
                )
            }
            is NoteContent.Pitch -> NoteEvent(
                id, onset, quarters, noteContent.pitch, staff = staff, voice = voice,
                notation = notation, role = role, chordId = chordReference?.let(chordId), instrumentId = noteInstrumentId,
                visual = visual, visibility = visibility,
            )
        }
        if (!context.canContinue) return null
        return TimedNote(event, onset, end, marks.toList(), principalVoice = voice, chord = chord)
    }

    /**
     * Reads a note's display-only `print-object`, `print-dot`, `print-leger`, `print-lyric` and
     * `print-spacing`; an omitted value is unspecified and an invalid one `INVALID_VALUE`.
     */
    private fun noteVisibility(start: ScoreXmlEvent) = NoteVisibility(
        printObject = optionalYesNo(start, "print-object") ?: true,
        printDot = optionalYesNo(start, "print-dot"),
        printLeger = optionalYesNo(start, "print-leger"),
        printLyric = optionalYesNo(start, "print-lyric"),
        printSpacing = optionalYesNo(start, "print-spacing"),
    )

    /** A nonblank voice label; blank text, including a no-break space, is `INVALID_VALUE`. */
    private fun voiceLabel(start: ScoreXmlEvent, feature: String): String? {
        val text = text(start, feature = feature).trim { it in WHITESPACE }
        if (!context.canContinue) return null
        if (text.isBlank()) {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Voice must be nonblank")
            return null
        }
        return text
    }

    /** A staff number within the staves currently declared for the part (one when undeclared). */
    private fun staffNumber(start: ScoreXmlEvent, feature: String): Int? {
        val text = text(start, feature = feature)
        if (!context.canContinue) return null
        val staff = positiveInt(text) ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Staff must be a positive Int")
            return null
        }
        if (staff > body.staffCount) {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Staff $staff exceeds the ${body.staffCount} declared staves")
            return null
        }
        return staff
    }

    /** A note's `instrument` reference; it must name a score-instrument of the current part. */
    private fun noteInstrument(start: ScoreXmlEvent): String? {
        attributes(start, setOf("id"), "S16")
        children { event -> unsupportedTree(event, "S16") }
        val id = start.attribute("id")
        if (id == null || id !in definitions.getValue(body.id).instruments) {
            invalid(start.attributeLocation("id"), if (id == null) "An instrument requires an id" else "Unknown instrument ID: $id")
            return null
        }
        return id
    }

    /**
     * Reads a percussion note's optional display step and octave, which position it on the staff
     * without giving it a sounding pitch. Both or neither must be present.
     */
    private fun unpitched(start: ScoreXmlEvent): NoteContent? {
        attributes(start, feature = "S16")
        val seen = mutableSetOf<String>()
        var step: Step? = null
        var octave: Int? = null
        children { event ->
            when (val name = event.localName()) {
                "display-step" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S16").trim { it in WHITESPACE }
                    step = Step.entries.firstOrNull { it.name == text }
                    if (step == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Display step must be A to G")
                }
                "display-octave" -> if (once(seen, name, event)) {
                    octave = text(event, feature = "S16").trim { it in WHITESPACE }.toIntOrNull()?.takeIf { it in 0..9 }
                    if (octave == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Display octave must be 0 to 9")
                }
                else -> unsupportedTree(event, "S16")
            }
        }
        if (!context.canContinue) return null
        if (("display-step" in seen) != ("display-octave" in seen)) {
            invalid(start, "An unpitched display position requires both display-step and display-octave")
            return null
        }
        return NoteContent.Pitch(NotePitch.Unpitched(step, octave))
    }

    /**
     * Reads a `grace` element's slash and steal-time percentages; `make-time` and other attributes
     * are reported as discarded. Returns null after reporting an invalid value.
     */
    private fun grace(start: ScoreXmlEvent): Grace? {
        attributes(start, setOf("slash", "steal-time-previous", "steal-time-following"), "S18")
        var valid = true
        fun percent(name: String): Rational? {
            val text = start.attribute(name) ?: return null
            val location = start.attributeLocation(name)
            val value = exact(location) { parseDecimal(text) ?: Rational.of(-1) }
            if (value != null && (value < Rational.ZERO || value > HUNDRED)) {
                context.report(DiagnosticCode.INVALID_VALUE, location, "$name must be a percentage from 0 to 100")
            }
            return value?.takeIf { it >= Rational.ZERO && it <= HUNDRED } ?: run {
                valid = false
                null
            }
        }
        val slash = yesNo(start, "slash")
        val previous = percent("steal-time-previous")
        val following = percent("steal-time-following")
        children { event -> unsupportedTree(event, "S18") }
        return if (slash != null && valid) Grace(slash, previous, following) else null
    }

    /**
     * Passes an event's span marks to the part's resolver: stops (and the stop half of a tied
     * `continue`) first, then slur continue points, then starts, so a span can end and another
     * begin on one note. A tuplet that starts and then stops on one note in document order is
     * paired on that note. Ties pair by written pitch; a tie on a rest is discarded. Slurs and
     * tuplets pair by number and may start or stop on a rest.
     */
    private fun registerSpans(event: Event, marks: List<SpanMark>, measure: Int) {
        if (marks.isEmpty()) return
        val (voice, tieKey) = when (event) {
            is NoteEvent -> event.voice to tieKey(event)
            is RestEvent -> event.voice to null
            is AttributeEvent, is DirectionEvent -> return
        }
        val endpoints = marks.mapNotNull { mark ->
            val key = when {
                mark.kind == RelationshipKind.SLUR || mark.kind == RelationshipKind.TUPLET -> mark.number
                tieKey == null -> {
                    unsupported(mark.location, "S09", "A tie on a rest is not interpreted; the tie is discarded")
                    return@mapNotNull null
                }
                else -> tieKey + if (mark.number == "1") "" else " (number ${mark.number})"
            }
            mark to SpanEndpoint(mark.kind, key, event.id, voice, SpanPosition(measure, event.onset), mark.location)
        }
        val resolver = body.spans
        val paired = pairSameNoteTuplets(endpoints)
        val remaining = endpoints.filterNot { (mark, _) -> mark in paired }
        fun splitsContinue(mark: SpanMark) = mark.type == SpanType.CONTINUE && mark.kind != RelationshipKind.SLUR
        remaining.forEach { (mark, endpoint) -> if (mark.type == SpanType.STOP || splitsContinue(mark)) resolver.stop(endpoint) }
        remaining.forEach { (mark, endpoint) ->
            if (mark.type == SpanType.CONTINUE && mark.kind == RelationshipKind.SLUR) resolver.continuePoint(endpoint)
        }
        remaining.forEach { (mark, endpoint) -> if (mark.type == SpanType.START || splitsContinue(mark)) resolver.start(endpoint) }
    }

    /**
     * Pairs each tuplet stop with a start of the same number earlier on the same note, in document
     * order, since MusicXML orders same-tag elements on one note that way. Such a pair is a
     * one-note tuplet rather than the stop of an earlier tuplet. Returns the paired marks.
     */
    private fun pairSameNoteTuplets(endpoints: List<Pair<SpanMark, SpanEndpoint>>): Set<SpanMark> {
        val openStarts = mutableMapOf<String, Pair<SpanMark, SpanEndpoint>>()
        val paired = mutableSetOf<SpanMark>()
        endpoints.filter { (mark, _) -> mark.kind == RelationshipKind.TUPLET }.forEach { endpoint ->
            val (mark, end) = endpoint
            when (mark.type) {
                SpanType.START -> openStarts[mark.number] = endpoint
                SpanType.STOP -> openStarts.remove(mark.number)?.let { (startMark, start) ->
                    body.spans.pair(start, end)
                    paired += startMark
                    paired += mark
                }
                else -> Unit
            }
        }
        return paired
    }

    /** A sound `tie`; one restricted by `time-only` to some repeat passes is discarded. */
    private fun tie(start: ScoreXmlEvent): SpanMark? {
        if (start.attribute("time-only") != null) {
            unsupportedTree(start, "S09", "A tie restricted by time-only is not interpreted; the tie is discarded")
            return null
        }
        attributes(start, setOf("type"), "S09")
        val type = spanType(start, "Tie", TIE_TYPES)
        children { event -> unsupportedTree(event, "S09") }
        return type?.let { SpanMark(RelationshipKind.SOUND_TIE, it, "1", start.location) }
    }

    /**
     * Interprets `tied`, `slur` and `tuplet` notations as span marks, and articulations, ornaments
     * and dynamics as [expressions]; other notations are discarded under their feature group.
     * Display attributes of the interpreted marks are reported as discarded.
     */
    private fun notations(start: ScoreXmlEvent, marks: MutableList<SpanMark>, expressions: MutableList<NoteExpression>) {
        attributes(start, feature = "S12")
        children { event ->
            when (val name = event.localName()) {
                "tied", "slur" -> {
                    attributes(event, setOf("type", "number"), "S09")
                    val label = if (name == "tied") "Tied" else "Slur"
                    val type = spanType(event, label, if (name == "tied") TIED_TYPES else SLUR_TYPES)
                    val number = numberLevel(event, label)
                    children { child -> unsupportedTree(child, "S09") }
                    if (type == SpanType.LET_RING) {
                        unsupported(event.location, "S09", "A let-ring tied mark is not interpreted; it is discarded")
                    } else if (type != null && number != null) {
                        val kind = if (name == "tied") RelationshipKind.NOTATED_TIE else RelationshipKind.SLUR
                        marks += SpanMark(kind, type, number, event.location)
                    }
                }
                "tuplet" -> tuplet(event)?.let { marks += it }
                "articulations" -> expressionGroup(event, "Articulation", ARTICULATIONS) { type, placement ->
                    NoteExpression.Articulation(type, placement)
                }.let { expressions += it }
                "ornaments" -> expressionGroup(event, "Ornament", ORNAMENTS) { type, placement ->
                    NoteExpression.Ornament(type, placement)
                }.let { expressions += it }
                "dynamics" -> dynamics(event, "S12")?.let { expressions += it }
                "footnote", "level" -> unsupportedTree(event, "S25")
                else -> unsupportedTree(event, "S12")
            }
        }
    }

    /**
     * A tuplet bracket endpoint, paired by `number` like a slur. The note's `time-modification`
     * carries its timing; the `tuplet-actual`/`tuplet-normal` display ratio and presentation
     * attributes are reported as discarded.
     */
    private fun tuplet(start: ScoreXmlEvent): SpanMark? {
        attributes(start, setOf("type", "number"), "S11")
        val type = spanType(start, "Tuplet", TIE_TYPES)
        val number = numberLevel(start, "Tuplet")
        children { event -> unsupportedTree(event, "S11", "Tuplet display content is not interpreted: ${event.name}") }
        return if (type != null && number != null) SpanMark(RelationshipKind.TUPLET, type, number, start.location) else null
    }

    /**
     * Reads `articulations` or `ornaments`: each child named in [types] becomes one expression with
     * its placement; other children (for example `breath-mark` or `wavy-line`) and display or
     * playback attributes are reported as S12 discarded.
     */
    private fun <T> expressionGroup(
        start: ScoreXmlEvent,
        label: String,
        types: Map<String, T>,
        create: (T, Placement?) -> NoteExpression,
    ): List<NoteExpression> {
        attributes(start, feature = "S12")
        val expressions = mutableListOf<NoteExpression>()
        children { event ->
            val type = event.localName()?.let { types[it] } ?: run {
                unsupportedTree(event, "S12", "$label is not interpreted: ${event.name}")
                return@children
            }
            attributes(event, setOf("placement"), "S12")
            val placement = placement(event)
            children { child -> unsupportedTree(child, "S12") }
            expressions += create(type, placement)
        }
        return expressions
    }

    /**
     * Reads a `dynamics` element in notations or a direction. Each standard marking keeps its
     * element name and `other-dynamics` its text, in source order. Without a marking it is discarded.
     * In a direction its own placement is validated but ignored, as MusicXML specifies; the direction's governs.
     */
    private fun dynamics(start: ScoreXmlEvent, feature: String, inDirection: Boolean = false): Dynamics? {
        attributes(start, setOf("placement") + VISUAL_ATTRIBUTES, feature)
        val placement = placement(start).takeUnless { inDirection }
        val visual = visualHints(start)
        val markings = mutableListOf<String>()
        children { event ->
            val name = event.localName()
            when {
                name == "other-dynamics" -> markings += text(event, feature = feature)
                name != null && name in DYNAMIC_MARKINGS -> {
                    attributes(event, feature = feature)
                    skipTree()
                    markings += name
                }
                else -> unsupportedTree(event, feature)
            }
        }
        if (!context.canContinue) return null
        if (markings.isEmpty()) {
            unsupported(start.location, feature, "Dynamics without a marking are discarded")
            return null
        }
        return Dynamics(markings.toList(), placement, visual)
    }

    /**
     * Reads a `lyric` as one verse line on its note. Syllables keep their syllabic value and the
     * elision joining them, in source order; consecutive text runs without an elision form one
     * syllable. An `extend` marks a melisma line and may stand alone on a continuation note. A
     * missing `number` leaves the verse unspecified. `laughing`, `humming`, `end-line`,
     * `end-paragraph`, typography and display attributes are S14 discarded (`footnote`/`level` S25),
     * and a lyric restricted by `time-only` is discarded. Out-of-order content is invalid. A null
     * result means the lyric was discarded or a fatal diagnostic was reported.
     */
    private fun lyric(start: ScoreXmlEvent): Lyric? {
        if (start.attribute("time-only") != null) {
            unsupportedTree(start, "S14", "A lyric restricted by time-only is not interpreted; the lyric is discarded")
            return null
        }
        attributes(start, setOf("number", "name", "placement", "print-object"), "S14")
        val printObject = optionalYesNo(start, "print-object")
        val verse = start.attribute("number")?.trim { it in WHITESPACE }
        // The model rejects any Unicode blank, such as a no-break space, not only XML whitespace.
        if (verse != null && verse.isBlank()) {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "Lyric number must be nonblank")
            return null
        }
        val placement = placement(start)
        val syllables = mutableListOf<LyricSyllable>()
        // Each syllable's text runs, joined once at the end so many runs do not recopy the text so far.
        val texts = mutableListOf<StringBuilder>()
        var syllabic: Syllabic? = null
        var syllabicPending = false
        var elision: String? = null
        var extend: LyricExtend? = null
        var discarded = false
        // Set once the text content is complete; only end-line, end-paragraph and editorial content may follow.
        var closed = false
        fun hasContent() = syllables.isNotEmpty() || extend != null || discarded
        children { event ->
            val name = event.localName()
            if (closed && name in LYRIC_CONTENT) {
                invalid(event, "Lyric $name must precede extend, end-line, end-paragraph and editorial content")
                return@children
            }
            when (name) {
                "syllabic" -> {
                    if (syllabicPending || (syllables.isNotEmpty() && elision == null)) {
                        invalid(event, "A syllabic must start the lyric or follow an elision, before its text")
                        return@children
                    }
                    val text = text(event, feature = "S14").trim { it in WHITESPACE }
                    syllabic = SYLLABICS[text] ?: run {
                        context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown syllabic: $text")
                        return@children
                    }
                    syllabicPending = true
                }
                "elision" -> {
                    if (syllables.isEmpty() || elision != null || syllabicPending) {
                        invalid(event, "An elision must follow a lyric text")
                        return@children
                    }
                    elision = text(event, feature = "S14")
                }
                "text" -> {
                    val text = text(event, feature = "S14")
                    if (syllables.isEmpty() || elision != null) {
                        syllables += LyricSyllable("", syllabic, elision)
                        texts += StringBuilder(text)
                        syllabic = null
                        syllabicPending = false
                        elision = null
                    } else {
                        texts.last().append(text)
                    }
                }
                "extend" -> {
                    if (elision != null || syllabicPending) {
                        invalid(event, "An extend cannot follow an elision or syllabic without text")
                        return@children
                    }
                    extend = lyricExtend(event) ?: return@children
                    closed = true
                }
                "laughing", "humming" -> {
                    if (hasContent() || syllabicPending) {
                        invalid(event, "A $name lyric cannot have other lyric content")
                        return@children
                    }
                    unsupportedTree(event, "S14", "A $name lyric is not interpreted; the lyric is discarded")
                    discarded = true
                    closed = true
                }
                "end-line", "end-paragraph", "footnote", "level" -> {
                    if (!hasContent() || elision != null || syllabicPending) {
                        invalid(event, "Lyric $name must follow the lyric text, extend, laughing or humming")
                        return@children
                    }
                    unsupportedTree(event, if (name == "footnote" || name == "level") "S25" else "S14")
                    closed = true
                }
                else -> unsupportedTree(event, "S14")
            }
        }
        if (!context.canContinue) return null
        if (elision != null || syllabicPending) {
            invalid(start, "A lyric elision or syllabic requires a following text")
            return null
        }
        if (!hasContent()) {
            invalid(start, "A lyric requires text, extend, laughing or humming")
            return null
        }
        if (discarded) return null
        val joined = syllables.mapIndexed { index, syllable -> syllable.copy(text = texts[index].toString()) }
        return Lyric(joined, verse, start.attribute("name"), extend, placement, printObject)
    }

    /** A lyric `extend` with its optional start/continue/stop type; display attributes are S14 discarded. */
    private fun lyricExtend(start: ScoreXmlEvent): LyricExtend? {
        attributes(start, setOf("type"), "S14")
        val text = start.attribute("type")
        val type = text?.let { EXTEND_TYPES[it] }
        if (text != null && type == null) {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("type"), "Unknown extend type: $text")
            return null
        }
        children { event -> unsupportedTree(event, "S14") }
        if (!context.canContinue) return null
        return LyricExtend(type)
    }

    /**
     * Reads a direction at [onset]. Printed direction types become [DirectionMark]s in source
     * order, the `sound` child becomes separate [DirectionSound] playback data, and placement,
     * directive and `offset` become [DirectionDisplay]. An offset with `sound="yes"` also shifts the
     * sound. A direction with nothing interpretable has a null event. A null result means a fatal
     * diagnostic was reported.
     */
    private fun direction(start: ScoreXmlEvent, onset: Rational, id: String): TimedDirection? {
        attributes(start, setOf("placement", "directive"), "S13")
        val placement = placement(start)
        val directive = yesNo(start, "directive") ?: return null
        val seen = mutableSetOf<String>()
        val marks = mutableListOf<DirectionMark>()
        val wedges = mutableListOf<SpanMark>()
        var voice: String? = null
        var staff = 1
        var offset: Rational? = null
        var offsetSource: ScoreXmlEvent? = null
        var offsetAffectsSound = false
        var sound: DirectionSound? = null
        val instrumentChanges = mutableListOf<Pair<String, ScoreXmlEvent>>()
        children { event ->
            when (val name = event.localName()) {
                "direction-type" -> {
                    seen += name
                    directionType(event, marks, wedges)
                }
                "offset" -> if (once(seen, name, event)) {
                    offsetAffectsSound = yesNo(event, "sound") ?: return@children
                    offsetSource = event
                    offset = decimal(event, "S13", "Offset", setOf("sound"))
                }
                "voice" -> if (once(seen, name, event)) voiceLabel(event, "S13")?.let { voice = it }
                "staff" -> if (once(seen, name, event)) staffNumber(event, "S13")?.let { staff = it }
                "sound" -> if (once(seen, name, event)) sound = sound(event, instrumentChanges)
                "footnote", "level" -> unsupportedTree(event, "S25")
                "listening" -> unsupportedTree(event, "S26")
                else -> unsupportedTree(event, "S13")
            }
        }
        if (!context.canContinue) return null
        if ("direction-type" !in seen) {
            invalid(start, "A direction requires a direction-type")
            return null
        }
        val quarters = offsetSource?.let { source ->
            val value = offset ?: return null
            val divisions = divisionsOrDefault(body)
            exact(source.location) { value / divisions } ?: return null
        } ?: Rational.ZERO
        val soundOffset = if (offsetAffectsSound) quarters else Rational.ZERO
        val changes = instrumentChanges.mapNotNull { (instrumentId, source) ->
            val changeOnset = exact(source.location) { onset + soundOffset } ?: return null
            if (changeOnset < Rational.ZERO) {
                unsupported(source.location, "S26", "An instrument change before the start of the measure is discarded")
                return@mapNotNull null
            }
            InstrumentChangeAt(changeOnset, Attribute.InstrumentChange(instrumentId), source)
        }
        if (marks.isEmpty() && sound == null) return TimedDirection(null, emptyList(), onset, changes)
        val wedgeOnset = if (wedges.isEmpty()) onset else exact(start.location) { onset + quarters } ?: return null
        val event = DirectionEvent(
            id,
            onset,
            marks.toList(),
            staff = staff,
            voice = voice,
            sound = sound?.copy(offset = soundOffset),
            display = DirectionDisplay(placement, directive, quarters),
        )
        return TimedDirection(event, wedges.toList(), wedgeOnset, changes)
    }

    /**
     * Reads words, dynamics, simple metronome marks and wedges; other direction types are S13
     * discarded. An empty direction-type is invalid.
     */
    private fun directionType(start: ScoreXmlEvent, marks: MutableList<DirectionMark>, wedges: MutableList<SpanMark>) {
        attributes(start, feature = "S13")
        var empty = true
        children { event ->
            empty = false
            when (event.localName()) {
                "words" -> {
                    val visual = visualHints(event)
                    marks += DirectionMark.Words(text(event, VISUAL_ATTRIBUTES, "S13"), visual)
                }
                "dynamics" -> dynamics(event, "S13", inDirection = true)?.let { marks += it }
                "metronome" -> metronome(event)?.let { marks += it }
                "wedge" -> wedge(event)?.let { (mark, span) ->
                    marks += mark
                    wedges += span
                }
                else -> unsupportedTree(event, "S13", "Direction type is not interpreted: ${event.name}")
            }
        }
        if (empty && context.canContinue) invalid(start, "A direction-type requires content")
    }

    /**
     * Reads a `beat-unit = per-minute` metronome mark. Metric modulations, tied beat units and
     * `metronome-note` forms are reported as discarded, as are `parentheses` and display attributes
     * other than position, colour and `print-object`.
     */
    private fun metronome(start: ScoreXmlEvent): DirectionMark.Metronome? {
        attributes(start, VISUAL_ATTRIBUTES + "print-object", "S13")
        val visual = visualHints(start)
        val visible = optionalYesNo(start, "print-object") ?: true
        var beatUnits = 0
        var beatUnit: NoteValue? = null
        var dots = 0
        var perMinute: String? = null
        var simple = true
        val seen = mutableSetOf<String>()
        children { event ->
            when (val name = event.localName()) {
                "beat-unit" -> {
                    beatUnits++
                    val text = text(event, feature = "S13").trim { it in WHITESPACE }
                    val value = NOTE_TYPES[text]
                    if (value == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown beat unit: $text")
                    if (beatUnits == 1) beatUnit = value
                }
                "beat-unit-dot" -> {
                    attributes(event, feature = "S13")
                    skipTree()
                    if (beatUnits == 1) dots++
                }
                "per-minute" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S13").trim { it in WHITESPACE }
                    if (text.isEmpty()) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Per-minute must be nonblank")
                    perMinute = text
                }
                null -> unsupportedTree(event)
                else -> {
                    simple = false
                    skipTree()
                }
            }
        }
        if (!context.canContinue) return null
        if (!simple || beatUnits > 1) {
            unsupported(start.location, "S13", "Only a beat-unit = per-minute metronome mark is interpreted; metronome discarded")
            return null
        }
        val unit = beatUnit
        val rate = perMinute
        if (unit == null || rate == null) {
            invalid(start, "A metronome mark requires a beat-unit and per-minute")
            return null
        }
        return DirectionMark.Metronome(unit, rate, dots, visual, visible)
    }

    /** A hairpin endpoint paired by `number` (1 to 16) like a slur; display attributes are S13 discarded. */
    private fun wedge(start: ScoreXmlEvent): Pair<DirectionMark.Wedge, SpanMark>? {
        attributes(start, setOf("type", "number") + VISUAL_ATTRIBUTES, "S13")
        val visual = visualHints(start)
        val text = start.attribute("type") ?: run {
            invalid(start, "A wedge requires a type")
            return null
        }
        val type = WEDGE_TYPES[text]
        if (type == null) context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("type"), "Unknown wedge type: $text")
        val number = numberLevel(start, "Wedge")
        children { event -> unsupportedTree(event, "S13") }
        if (type == null || number == null || !context.canContinue) return null
        val spanType = when (type) {
            WedgeType.CRESCENDO, WedgeType.DIMINUENDO -> SpanType.START
            WedgeType.CONTINUE -> SpanType.CONTINUE
            WedgeType.STOP -> SpanType.STOP
        }
        return DirectionMark.Wedge(type, visual) to SpanMark(RelationshipKind.WEDGE, spanType, number, start.location)
    }

    /**
     * Reads the `tempo` and `dynamics` playback attributes of a direction's `sound`, and adds the
     * score-instrument named by each `instrument-change` to [instrumentChanges]. Other sound
     * attributes and children are reported as S26 discarded; a sound with neither attribute is null.
     */
    private fun sound(start: ScoreXmlEvent, instrumentChanges: MutableList<Pair<String, ScoreXmlEvent>>): DirectionSound? {
        attributes(start, setOf("tempo", "dynamics"), "S26")
        val tempo = nonNegativeDecimalAttribute(start, "tempo", "Sound tempo")
        val dynamics = nonNegativeDecimalAttribute(start, "dynamics", "Sound dynamics")
        children { event ->
            if (event.localName() == "instrument-change") instrumentChange(event)?.let { instrumentChanges += it to event }
            else unsupportedTree(event, "S26")
        }
        if (!context.canContinue || (tempo == null && dynamics == null)) return null
        return DirectionSound(tempo, dynamics)
    }

    /**
     * The score-instrument of the current part that an `instrument-change` refers to. Its new
     * sound, solo/ensemble and virtual-instrument settings are reported as S26 discarded.
     */
    private fun instrumentChange(start: ScoreXmlEvent): String? {
        attributes(start, setOf("id"), "S26")
        children { event -> unsupportedTree(event, "S26", "Instrument change sound data is not interpreted: ${event.name}") }
        val id = start.attribute("id")
        if (id == null || id !in definitions.getValue(body.id).instruments) {
            invalid(start.attributeLocation("id"), if (id == null) "An instrument-change requires an id" else "Unknown instrument ID: $id")
            return null
        }
        return id
    }

    /**
     * A direction's wedge endpoints at [position]: stops, then continue points, then starts. Their
     * source orders are reserved now in document order, as the direction is read, so they interleave correctly with
     * note spans in the same measure even though registration waits for [registerWedges].
     */
    private fun wedgeEndpoints(event: DirectionEvent, marks: List<SpanMark>, position: SpanPosition): List<WedgeEndpoint> {
        val matchOrder = listOf(SpanType.STOP, SpanType.CONTINUE, SpanType.START)
        val wedgeMarks = marks.filter { it.type in matchOrder }
        val firstOrder = body.spans.reserveOrders(wedgeMarks.size)
        return wedgeMarks
            .mapIndexed { index, mark ->
                val endpoint = SpanEndpoint(mark.kind, mark.number, event.id, event.voice.orEmpty(), position, mark.location)
                WedgeEndpoint(mark.type, endpoint, firstOrder + index)
            }
            .sortedBy { matchOrder.indexOf(it.type) }
    }

    /**
     * Passes one measure's wedge endpoints to the part's resolver in display-position order, keeping
     * document order at equal positions, so an offset cannot pair a stop with a later-displayed start.
     * Each endpoint keeps its reserved source order for numbering, so offsets do not change wedge IDs.
     */
    private fun registerWedges(endpoints: List<WedgeEndpoint>) {
        val resolver = body.spans
        endpoints.sortedBy { it.endpoint.position }.forEach { wedge ->
            when (wedge.type) {
                SpanType.STOP -> resolver.stop(wedge.endpoint, wedge.order)
                SpanType.CONTINUE -> resolver.continuePoint(wedge.endpoint)
                SpanType.START -> resolver.start(wedge.endpoint, wedge.order)
                SpanType.LET_RING -> Unit
            }
        }
    }

    private fun placement(start: ScoreXmlEvent): Placement? = when (val text = start.attribute("placement")) {
        null -> null
        "above" -> Placement.ABOVE
        "below" -> Placement.BELOW
        else -> {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("placement"), "Unknown placement: $text")
            null
        }
    }

    /** An optional non-negative decimal attribute; null when absent or after an `INVALID_VALUE`. */
    private fun nonNegativeDecimalAttribute(start: ScoreXmlEvent, name: String, label: String): Rational? {
        val text = start.attribute(name) ?: return null
        val location = start.attributeLocation(name)
        val value = exact(location) { parseDecimal(text) }
        if (value == null || value < Rational.ZERO) {
            if (context.canContinue) context.report(DiagnosticCode.INVALID_VALUE, location, "$label must be a non-negative decimal")
            return null
        }
        return value
    }

    /**
     * Reads the actual/normal note counts and optional normal type and dots. The counts are the
     * note's cumulative ratio, so a nested tuplet's counts already include its enclosing tuplets.
     */
    private fun timeModification(start: ScoreXmlEvent): TimeModification? {
        attributes(start, feature = "S11")
        val seen = mutableSetOf<String>()
        var actual: Int? = null
        var normal: Int? = null
        var normalValue: NoteValue? = null
        var normalDots = 0
        children { event ->
            when (val name = event.localName()) {
                "actual-notes", "normal-notes" -> if (once(seen, name, event)) {
                    val count = positiveInt(text(event, feature = "S11"))
                    if (count == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "$name must be a positive Int")
                    if (name == "actual-notes") actual = count else normal = count
                }
                "normal-type" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S11").trim { it in WHITESPACE }
                    normalValue = NOTE_TYPES[text]
                    if (normalValue == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Unknown normal type: $text")
                }
                "normal-dot" -> {
                    attributes(event, feature = "S11")
                    skipTree()
                    normalDots++
                }
                else -> unsupportedTree(event, "S11")
            }
        }
        if (!context.canContinue) return null
        val actualNotes = actual
        val normalNotes = normal
        if (actualNotes == null || normalNotes == null) {
            // A present but invalid count has already been reported.
            if ("actual-notes" !in seen || "normal-notes" !in seen) {
                invalid(start, "A time modification requires actual-notes and normal-notes")
            }
            return null
        }
        if (normalDots > 0 && "normal-type" !in seen) {
            invalid(start, "A normal-dot requires a normal-type")
            return null
        }
        return TimeModification(actualNotes, normalNotes, normalValue, normalDots)
    }

    /** A printed stem direction; position and color attributes are reported as discarded. */
    private fun stem(start: ScoreXmlEvent): StemDirection? {
        val text = text(start, feature = "S10").trim { it in WHITESPACE }
        if (!context.canContinue) return null
        return STEM_DIRECTIONS[text] ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Unknown stem direction: $text")
            null
        }
    }

    /** One beam level (`number` 1 to 8, default 1) and its type; fan and display attributes are discarded. */
    private fun beam(start: ScoreXmlEvent): Beam? {
        val numberText = start.attribute("number")
        val text = text(start, setOf("number"), "S10").trim { it in WHITESPACE }
        if (!context.canContinue) return null
        val level = if (numberText == null) 1 else positiveInt(numberText)?.takeIf { it <= 8 }
        if (level == null) {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "Beam number must be 1 to 8")
            return null
        }
        val type = BEAM_TYPES[text] ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Unknown beam type: $text")
            return null
        }
        return Beam(level, type)
    }

    /** Reads a required span `type`; a missing type is invalid structure and an unknown one an invalid value. */
    private fun spanType(start: ScoreXmlEvent, label: String, allowed: Map<String, SpanType>): SpanType? {
        val text = start.attribute("type") ?: run {
            invalid(start, "A ${label.lowercase()} requires a type")
            return null
        }
        return allowed[text] ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("type"), "Unknown ${label.lowercase()} type: $text")
            null
        }
    }

    /** A MusicXML number-level, 1 to 16; absent means 1. */
    private fun numberLevel(start: ScoreXmlEvent, label: String): String? {
        val text = start.attribute("number") ?: return "1"
        val number = positiveInt(text)?.takeIf { it <= 16 } ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "$label number must be 1 to 16")
            return null
        }
        return number.toString()
    }

    private fun pitch(start: ScoreXmlEvent): NoteContent? {
        attributes(start, feature = "S04")
        val seen = mutableSetOf<String>()
        var step: Step? = null
        var octave: Int? = null
        var alter = Rational.ZERO
        children { event ->
            when (val name = event.localName()) {
                "step" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S04").trim { it in WHITESPACE }
                    step = Step.entries.firstOrNull { it.name == text }
                    if (step == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Step must be A to G")
                }
                "alter" -> if (once(seen, name, event)) decimal(event, "S04", "Alter")?.let { alter = it }
                "octave" -> if (once(seen, name, event)) {
                    octave = text(event, feature = "S04").trim { it in WHITESPACE }.toIntOrNull()?.takeIf { it in 0..9 }
                    if (octave == null) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Octave must be 0 to 9")
                }
                else -> unsupportedTree(event, "S04")
            }
        }
        if (!context.canContinue) return null
        val writtenStep = step
        val writtenOctave = octave
        if (writtenStep == null || writtenOctave == null) {
            invalid(start, "A pitch requires step and octave")
            return null
        }
        return NoteContent.Pitch(NotePitch.Pitched(WrittenPitch(writtenStep, writtenOctave, alter)))
    }

    private fun rest(start: ScoreXmlEvent): NoteContent? {
        attributes(start, setOf("measure"), "S04")
        val wholeMeasure = yesNo(start, "measure") ?: return null
        children { event -> unsupportedTree(event, "S17", "Rest display position is not interpreted") }
        return NoteContent.Rest(wholeMeasure)
    }

    /**
     * A printed accidental with its cautionary and editorial flags and its display-only
     * `parentheses` and `bracket`, which are kept independently of the name; other display
     * attributes are S04 discarded.
     */
    private fun accidental(start: ScoreXmlEvent): Accidental? {
        val cautionary = yesNo(start, "cautionary") ?: return null
        val editorial = yesNo(start, "editorial") ?: return null
        val parentheses = optionalYesNo(start, "parentheses")
        val bracket = optionalYesNo(start, "bracket")
        val name = text(start, setOf("cautionary", "editorial", "parentheses", "bracket"), "S04").trim { it in WHITESPACE }
        if (name.isEmpty()) {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Accidental must be nonblank")
            return null
        }
        return Accidental(name, cautionary, editorial, parentheses, bracket)
    }

    /** A yes/no attribute that stays null when omitted, unlike [yesNo]; null also after `INVALID_VALUE`. */
    private fun optionalYesNo(start: ScoreXmlEvent, name: String): Boolean? =
        start.attribute(name)?.let { yesNo(start, name) }

    private fun yesNo(start: ScoreXmlEvent, name: String): Boolean? = when (start.attribute(name)) {
        null, "no" -> false
        "yes" -> true
        else -> {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation(name), "Expected yes or no")
            null
        }
    }

    private fun positiveDecimal(start: ScoreXmlEvent, feature: String, label: String): Rational? {
        val text = text(start, feature = feature)
        if (!context.canContinue) return null
        return positiveDecimal(text, start.location, label)
    }

    private fun positiveDecimal(text: String, location: SourceLocation, label: String): Rational? {
        val value = decimal(text, location, label) ?: return null
        if (value <= Rational.ZERO) {
            context.report(DiagnosticCode.INVALID_VALUE, location, "$label must be positive")
            return null
        }
        return value
    }

    private fun decimal(start: ScoreXmlEvent, feature: String, label: String, allowed: Set<String> = emptySet()): Rational? {
        val text = text(start, allowed, feature)
        if (!context.canContinue) return null
        return decimal(text, start.location, label)
    }

    private fun decimal(text: String, location: SourceLocation, label: String): Rational? = exact(location) {
        parseDecimal(text) ?: run {
            context.report(DiagnosticCode.INVALID_VALUE, location, "$label must be a decimal number")
            null
        }
    }

    /** Translates exact-arithmetic failures to source-located diagnostics; nothing is rounded. */
    private fun exact(location: SourceLocation, compute: () -> Rational?): Rational? = try {
        compute()
    } catch (exception: RationalException) {
        val code = if (exception.code == RationalError.OVERFLOW) DiagnosticCode.NUMERIC_OVERFLOW else DiagnosticCode.INVALID_VALUE
        context.report(code, location.withDeclaredPart(), "Exact value is outside the supported range: ${exception.code}")
        null
    }

    /** Each handler consumes its subtree, leaving the cursor on its end event. */
    private fun children(consume: (ScoreXmlEvent) -> Unit) {
        while (context.canContinue) {
            val event = reader.next() ?: return
            when (event.kind) {
                ScoreXmlEventKind.END_ELEMENT -> return
                ScoreXmlEventKind.START_ELEMENT -> consume(event)
                ScoreXmlEventKind.TEXT -> if (!event.text.orEmpty().all { it in " \t\r\n" }) {
                    invalid(event, "Unexpected text in a structural container")
                }
            }
        }
    }

    private fun text(start: ScoreXmlEvent, allowed: Set<String> = emptySet(), feature: String = "S03"): String {
        attributes(start, allowed, feature)
        return buildString {
            while (context.canContinue) {
                val event = reader.next() ?: break
                when (event.kind) {
                    ScoreXmlEventKind.END_ELEMENT -> break
                    ScoreXmlEventKind.TEXT -> append(event.text)
                    ScoreXmlEventKind.START_ELEMENT -> unsupportedTree(event, feature)
                }
            }
        }
    }

    private fun attributes(start: ScoreXmlEvent, allowed: Set<String> = emptySet(), feature: String = "S03") {
        for (attribute in start.attributes) {
            if (!context.canContinue) return
            if (attribute.name.namespaceUri.isNotEmpty() || attribute.name.localName !in allowed) {
                unsupported(start.attributeLocation(attribute.name), feature, "Unmapped attribute: ${attribute.name}")
            }
        }
    }

    /** Reports and skips a subtree; foreign names always use the foreign-extension feature. */
    private fun unsupportedTree(
        start: ScoreXmlEvent,
        feature: String = feature(start),
        message: String = "Unmapped subtree: ${start.name}",
    ) {
        val foreign = start.name?.namespaceUri?.isNotEmpty() == true
        unsupported(start.location, if (foreign) "foreign-extension" else feature, message)
        skipTree()
    }

    /** Consumes the remainder of the current element, leaving the cursor on its end event. */
    private fun skipTree() {
        var depth = 1
        while (depth > 0 && context.canContinue) {
            when (reader.next()?.kind ?: return) {
                ScoreXmlEventKind.START_ELEMENT -> depth++
                ScoreXmlEventKind.END_ELEMENT -> depth--
                ScoreXmlEventKind.TEXT -> Unit
            }
        }
    }

    private fun unsupported(location: SourceLocation, feature: String, message: String) {
        context.unsupported(UnsupportedContent(feature, ContentRetention.DISCARDED), location.withDeclaredPart(), message)
    }

    private fun feature(event: ScoreXmlEvent): String = when (event.localName()) {
        "note" -> "S04"
        "backup", "forward" -> "S06"
        "attributes" -> "S07"
        "direction" -> "S13"
        "barline" -> "S15"
        "score-instrument" -> "S16"
        "defaults", "print", "part-name-display", "part-abbreviation-display" -> "S17"
        "harmony", "figured-bass" -> "S22"
        "grouping", "link", "bookmark", "part-link", "group", "part-group" -> "S25"
        "sound", "listening", "player", "midi-device", "midi-instrument" -> "S26"
        "opus" -> "S27"
        else -> if (event.name?.namespaceUri?.isNotEmpty() == true) "foreign-extension" else "S03"
    }

    private fun requiredId(event: ScoreXmlEvent, label: String = "part"): String? {
        val id = event.attribute("id")?.trim { it in " \t\r\n" }
        // Model IDs reject any Unicode blank, such as a no-break space, not only XML whitespace.
        if (id.isNullOrEmpty() || id.any { it.isWhitespace() }) {
            invalid(event.attributeLocation("id"), "A nonblank $label ID without whitespace is required")
            return null
        }
        return id
    }

    private fun once(seen: MutableSet<String>, name: String, event: ScoreXmlEvent): Boolean {
        if (seen.add(name)) return true
        invalid(event, "Duplicate $name")
        return false
    }

    private fun provenance(path: String, event: ScoreXmlEvent, partId: String? = event.location.partId) {
        context.addProvenance(SourceProvenance(path, event.location.copy(partId = partId).withDeclaredPart()))
    }

    /**
     * The part's divisions. Exporters sometimes omit `divisions`; the generally agreed default of 1,
     * which MusicXML 4.1 documents, then applies under both policies without a diagnostic.
     */
    private fun divisionsOrDefault(body: PartBody): Rational = body.divisions ?: Rational.ONE

    private fun invalid(event: ScoreXmlEvent, message: String) = invalid(event.location, message)

    private fun invalid(location: SourceLocation, message: String) {
        context.report(DiagnosticCode.INVALID_STRUCTURE, location.withDeclaredPart(), message)
    }

    private fun SourceLocation.withDeclaredPart(): SourceLocation = copy(partId = declaredPartId ?: partId)

    /**
     * A read note from [onset] to [end] (equal for a grace note); a null [event] was discarded with a
     * diagnostic. [principalVoice] is the voice of a timed (non-grace) note, which can be the
     * principal of grace notes. A [chord] tone never moves the cursor or becomes a chord reference.
     */
    private class TimedNote(
        val event: Event?,
        val onset: Rational,
        val end: Rational,
        val marks: List<SpanMark>,
        val principalVoice: String? = null,
        val chord: Boolean = false,
        val grace: Boolean = false,
    )

    /**
     * The non-chord note that following chord tones share: its [onset] and [end], its index in the
     * measure's events (null when discarded), whether it is a [grace] note or a [rest]. [id] is the
     * chord ID, allocated when the first chord tone arrives.
     */
    private class ChordReference(val onset: Rational, val end: Rational, val eventIndex: Int?, val grace: Boolean, val rest: Boolean) {
        var id: String? = null
    }

    /** A parsed staff-scoped attribute; a null [staff] applies to every staff. */
    private class StaffChange(
        val attribute: Attribute,
        val staff: Int?,
        val source: ScoreXmlEvent,
        val label: String,
        val visible: Boolean,
    )

    /**
     * Links each imported grace note in one measure to its principal note: the next imported note
     * in the same voice. Grace notes followed in their voice by a rest or the end of the measure
     * are after-graces of the previous note in that voice. Grace notes whose next timed note was
     * discarded, or that have no note to attach to, keep a null principal and are reported as
     * `UNMATCHED_SPAN`. Relationship numbers follow the grace notes' source order.
     */
    private class GraceLinker(private val spans: SpanResolver) {
        private val pending = mutableMapOf<String, MutableList<Pair<SpanEndpoint, Int>>>()
        private val previousNotes = mutableMapOf<String, SpanEndpoint>()

        fun add(note: TimedNote, position: SpanPosition, location: SourceLocation) {
            val event = note.event
            if (event is NoteEvent && event.role == NoteRole.GRACE) {
                val endpoint = SpanEndpoint(RelationshipKind.GRACE_NOTE, event.id, event.id, event.voice, position, location)
                pending.getOrPut(event.voice) { mutableListOf() } += endpoint to spans.reserveOrders(1)
                return
            }
            val voice = note.principalVoice ?: return
            when (event) {
                is NoteEvent -> {
                    val principal = SpanEndpoint(RelationshipKind.GRACE_NOTE, event.id, event.id, voice, position, location)
                    pending.remove(voice)?.forEach { (grace, order) -> spans.pair(grace, principal, order) }
                    previousNotes[voice] = principal
                }
                is RestEvent -> attachAfter(voice)
                else -> pending.remove(voice)?.forEach { (grace, order) ->
                    spans.unresolved(grace, "Grace note ${grace.eventId} precedes a discarded note and has no principal note", order)
                }
            }
        }

        /** Resolves the grace notes still waiting at the end of the measure. */
        fun finish() = pending.keys.toList().forEach(::attachAfter)

        private fun attachAfter(voice: String) {
            val previous = previousNotes[voice]
            pending.remove(voice)?.forEach { (grace, order) ->
                if (previous != null) spans.pair(grace, previous, order)
                else spans.unresolved(grace, "Grace note ${grace.eventId} has no principal note in voice $voice", order)
            }
        }
    }

    /** A read barline and the `ending` element its ending mark came from, if any. */
    private class ReadBarline(val barline: Barline, val endingSource: ScoreXmlEvent?)

    /**
     * A read direction; a null [event] means it was discarded with a diagnostic. [wedgeOnset] is
     * the measure-local position, including the display offset, that orders its wedge endpoints.
     */
    private class TimedDirection(
        val event: DirectionEvent?,
        val wedges: List<SpanMark>,
        val wedgeOnset: Rational,
        val instrumentChanges: List<InstrumentChangeAt> = emptyList(),
    )

    /** A sound `instrument-change` at its measure-local [onset], which includes any sound offset. */
    private class InstrumentChangeAt(val onset: Rational, val attribute: Attribute.InstrumentChange, val source: ScoreXmlEvent)

    /** A wedge endpoint awaiting display-position registration, with its reserved source [order]. */
    private class WedgeEndpoint(val type: SpanType, val endpoint: SpanEndpoint, val order: Int)

    /** A span endpoint read from a note, before the note's event identity is known. */
    private class SpanMark(val kind: RelationshipKind, val type: SpanType, val number: String, val location: SourceLocation)

    private sealed interface NoteContent {
        class Pitch(val pitch: NotePitch) : NoteContent
        class Rest(val wholeMeasure: Boolean) : NoteContent
    }

    /**
     * A part's measures plus the divisions, staff count, open spans and per-staff transpositions
     * that persist across its measures. Sounding pitch cannot be derived on [ambiguousStaves].
     */
    private class PartBody(val id: String, val spans: SpanResolver) {
        val measures = mutableListOf<Measure>()
        var divisions: Rational? = null

        /** The count from the latest `staves`, which bounds later staff numbers; one when undeclared. */
        var staffCount = 1
        val transpositions = mutableMapOf<Int, Transposition>()
        val ambiguousStaves = mutableSetOf<Int>()
        val endings = mutableListOf<Ending>()

        /** The index in [endings] of an ending started but not yet stopped, and its start mark. */
        var openEnding: Pair<Int, ScoreXmlEvent>? = null

        /** The measure-repeat sign in effect, which marks each measure until it is stopped. */
        var measureRepeat: MeasureRepeat? = null
    }

    private class MeasureHeader(val number: String, val implicit: Boolean, val source: ScoreXmlEvent)

    private class PartDefinition(val id: String, val index: Int, val location: SourceLocation) {
        var name: String? = null
        var abbreviation: String? = null
        var nameVisible = true
        var abbreviationVisible = true
        val metadata = mutableListOf<MetadataEntry.Text>()
        val instruments = linkedMapOf<String, InstrumentDefinition>()
    }

    private class InstrumentDefinition(val id: String) {
        var name: String? = null
        var percussionKey: Int? = null
    }
}

private fun ScoreXmlEvent.localName(): String? = name?.takeIf { it.namespaceUri.isEmpty() }?.localName

private fun ScoreXmlEvent.attribute(name: String): String? =
    attributes.firstOrNull { it.name == XmlName("", name) }?.value

private fun ScoreXmlEvent.attributeLocation(name: String): SourceLocation = attributeLocation(XmlName("", name))

private fun ScoreXmlEvent.attributeLocation(name: XmlName): SourceLocation = location.copy(
    path = location.path + "/@" + if (name.namespaceUri.isEmpty()) name.localName else "{${name.namespaceUri}}${name.localName}",
)

private const val WHITESPACE = " \t\r\n"

private val HUNDRED = Rational.of(100)

private val NOTE_TYPES: Map<String, NoteValue> = mapOf(
    "maxima" to NoteValue.MAXIMA, "long" to NoteValue.LONG, "breve" to NoteValue.BREVE,
    "whole" to NoteValue.WHOLE, "half" to NoteValue.HALF, "quarter" to NoteValue.QUARTER,
    "eighth" to NoteValue.EIGHTH, "16th" to NoteValue.N16, "32nd" to NoteValue.N32,
    "64th" to NoteValue.N64, "128th" to NoteValue.N128, "256th" to NoteValue.N256,
    "512th" to NoteValue.N512, "1024th" to NoteValue.N1024,
)

/** Position and colour attributes kept as [VisualHints]. */
private val VISUAL_ATTRIBUTES: Set<String> = setOf("default-x", "default-y", "relative-x", "relative-y", "color")

/** Display-only visibility attributes of a note, kept as [NoteVisibility]. */
private val NOTE_PRINT_ATTRIBUTES: Set<String> = setOf("print-object", "print-dot", "print-leger", "print-lyric", "print-spacing")

/** Attributes children whose `print-object` is kept on their [AttributeEvent]. */
private val PRINTABLE_ATTRIBUTES: Set<String> = setOf("key", "time", "clef")

/** MusicXML 4.0 `color`, accepted case-insensitively and stored in upper case. */
private val COLOR = Regex("#[0-9A-F]{6}([0-9A-F]{2})?")

private val NOTE_CHILD_FEATURES: Map<String, String> = mapOf(
    "footnote" to "S25", "level" to "S25",
    "notehead" to "S17", "notehead-text" to "S17",
    "play" to "S26", "listen" to "S26",
)

private val STEM_DIRECTIONS: Map<String, StemDirection> = mapOf(
    "up" to StemDirection.UP, "down" to StemDirection.DOWN, "double" to StemDirection.DOUBLE, "none" to StemDirection.NONE,
)

private val BEAM_TYPES: Map<String, BeamType> = mapOf(
    "begin" to BeamType.BEGIN, "continue" to BeamType.CONTINUE, "end" to BeamType.END,
    "forward hook" to BeamType.FORWARD_HOOK, "backward hook" to BeamType.BACKWARD_HOOK,
)

private val ARTICULATIONS: Map<String, ArticulationType> = mapOf(
    "accent" to ArticulationType.ACCENT, "strong-accent" to ArticulationType.STRONG_ACCENT,
    "staccato" to ArticulationType.STACCATO, "tenuto" to ArticulationType.TENUTO,
    "detached-legato" to ArticulationType.DETACHED_LEGATO, "staccatissimo" to ArticulationType.STACCATISSIMO,
    "spiccato" to ArticulationType.SPICCATO, "stress" to ArticulationType.STRESS,
    "unstress" to ArticulationType.UNSTRESS, "soft-accent" to ArticulationType.SOFT_ACCENT,
)

private val ORNAMENTS: Map<String, OrnamentType> = mapOf(
    "trill-mark" to OrnamentType.TRILL_MARK, "turn" to OrnamentType.TURN, "delayed-turn" to OrnamentType.DELAYED_TURN,
    "inverted-turn" to OrnamentType.INVERTED_TURN, "delayed-inverted-turn" to OrnamentType.DELAYED_INVERTED_TURN,
    "vertical-turn" to OrnamentType.VERTICAL_TURN, "inverted-vertical-turn" to OrnamentType.INVERTED_VERTICAL_TURN,
    "shake" to OrnamentType.SHAKE, "mordent" to OrnamentType.MORDENT, "inverted-mordent" to OrnamentType.INVERTED_MORDENT,
    "haydn" to OrnamentType.HAYDN, "schleifer" to OrnamentType.SCHLEIFER,
)

/** The empty dynamics elements of MusicXML 4.0; `other-dynamics` carries its own text. */
private val DYNAMIC_MARKINGS: Set<String> = setOf(
    "p", "pp", "ppp", "pppp", "ppppp", "pppppp", "f", "ff", "fff", "ffff", "fffff", "ffffff",
    "mp", "mf", "sf", "sfp", "sfpp", "fp", "rf", "rfz", "sfz", "sffz", "fz", "n", "pf", "sfzp",
)

private val WEDGE_TYPES: Map<String, WedgeType> = mapOf(
    "crescendo" to WedgeType.CRESCENDO, "diminuendo" to WedgeType.DIMINUENDO,
    "continue" to WedgeType.CONTINUE, "stop" to WedgeType.STOP,
)

/** Lyric children that make up its text content; none may follow `extend` or trailing content. */
private val LYRIC_CONTENT: Set<String?> = setOf("syllabic", "elision", "text", "extend", "laughing", "humming")

private val SYLLABICS: Map<String, Syllabic> = mapOf(
    "single" to Syllabic.SINGLE, "begin" to Syllabic.BEGIN, "middle" to Syllabic.MIDDLE, "end" to Syllabic.END,
)

private val EXTEND_TYPES: Map<String, ExtendType> = mapOf(
    "start" to ExtendType.START, "continue" to ExtendType.CONTINUE, "stop" to ExtendType.STOP,
)

private val ATTRIBUTES_CHILD_FEATURES: Map<String, String> = mapOf(
    "part-symbol" to "S08", "instruments" to "S16", "for-part" to "S16",
)

private enum class SpanType { START, STOP, CONTINUE, LET_RING }

private val BAR_STYLES: Map<String, BarStyle> = mapOf(
    "regular" to BarStyle.REGULAR, "dotted" to BarStyle.DOTTED, "dashed" to BarStyle.DASHED, "heavy" to BarStyle.HEAVY,
    "light-light" to BarStyle.LIGHT_LIGHT, "light-heavy" to BarStyle.LIGHT_HEAVY, "heavy-light" to BarStyle.HEAVY_LIGHT,
    "heavy-heavy" to BarStyle.HEAVY_HEAVY, "tick" to BarStyle.TICK, "short" to BarStyle.SHORT, "none" to BarStyle.NONE,
)

/**
 * Parses a MusicXML ending-number, `([ ]*)|([1-9][0-9]*(, ?[1-9][0-9]*)*)`: blank means no
 * numbers. Null means the value does not match.
 */
private fun endingNumbers(text: String): List<Int>? {
    if (text.all { it == ' ' }) return emptyList()
    return text.split(',').mapIndexed { index, part ->
        val digits = if (index > 0 && part.startsWith(' ')) part.substring(1) else part
        if (digits.isEmpty() || digits.first() !in '1'..'9' || !digits.all { it in '0'..'9' }) return null
        digits.toIntOrNull() ?: return null
    }
}

private val TIE_TYPES: Map<String, SpanType> = mapOf("start" to SpanType.START, "stop" to SpanType.STOP)

private val TIED_TYPES: Map<String, SpanType> = TIE_TYPES + mapOf("continue" to SpanType.CONTINUE, "let-ring" to SpanType.LET_RING)

private val SLUR_TYPES: Map<String, SpanType> = TIE_TYPES + ("continue" to SpanType.CONTINUE)

/**
 * Label used to pair ties: the written pitch, or for an unpitched note its display position and
 * instrument, for example `unpitched F4 (P2-I1)`.
 */
private fun tieKey(note: NoteEvent): String = when (val pitch = note.pitch) {
    is NotePitch.Pitched -> pitchKey(pitch.written)
    is NotePitch.Unpitched -> "unpitched ${pitch.displayStep ?: ""}${pitch.displayOctave ?: ""} (${note.instrumentId.orEmpty()})"
}

/** Written pitch label used to pair ties, for example `C#4`, or `B(-1/2)3` for a microtone. */
private fun pitchKey(pitch: WrittenPitch): String {
    val alter = when (pitch.alter) {
        Rational.ZERO -> ""
        Rational.ONE -> "#"
        Rational.of(-1) -> "b"
        else -> "(${pitch.alter})"
    }
    return "${pitch.step}$alter${pitch.octave}"
}

private val TIME_SYMBOLS: Map<String, TimeSymbol> = mapOf(
    "common" to TimeSymbol.COMMON,
    "cut" to TimeSymbol.CUT,
    "single-number" to TimeSymbol.SINGLE_NUMBER,
    "note" to TimeSymbol.NOTE,
    "dotted-note" to TimeSymbol.DOTTED_NOTE,
    "normal" to TimeSymbol.NORMAL,
)

private val TIME_SEPARATORS: Map<String, TimeSeparator> = mapOf(
    "none" to TimeSeparator.NONE,
    "horizontal" to TimeSeparator.HORIZONTAL,
    "diagonal" to TimeSeparator.DIAGONAL,
    "vertical" to TimeSeparator.VERTICAL,
    "adjacent" to TimeSeparator.ADJACENT,
)

private val TIME_RELATIONS: Map<String, TimeRelation> = mapOf(
    "parentheses" to TimeRelation.PARENTHESES,
    "bracket" to TimeRelation.BRACKET,
    "equals" to TimeRelation.EQUALS,
    "slash" to TimeRelation.SLASH,
    "space" to TimeRelation.SPACE,
    "hyphen" to TimeRelation.HYPHEN,
)

/**
 * Pairs beats/beat-type texts in source order into fractions, splitting additive numerators such
 * as `3+2`; null unless they alternate beats then beat-type and every number is a positive Int.
 */
private fun timeFractions(parts: List<Pair<String, String>>): List<TimeFraction>? {
    if (parts.size % 2 != 0) return null
    return parts.chunked(2).map { (beats, beatType) ->
        if (beats.first != "beats" || beatType.first != "beat-type") return null
        val numerators = beats.second.split('+').map { positiveInt(it) ?: return null }
        TimeFraction(numerators, positiveInt(beatType.second) ?: return null)
    }
}

private val KEY_CANCEL_LOCATIONS: Map<String, KeyCancelLocation> = mapOf(
    "left" to KeyCancelLocation.LEFT,
    "right" to KeyCancelLocation.RIGHT,
    "before-barline" to KeyCancelLocation.BEFORE_BARLINE,
)

private val CLEF_SIGNS: Map<String, ClefSign> = mapOf(
    "G" to ClefSign.G, "F" to ClefSign.F, "C" to ClefSign.C, "percussion" to ClefSign.PERCUSSION,
    "TAB" to ClefSign.TAB, "jianpu" to ClefSign.JIANPU, "none" to ClefSign.NONE,
)

/** Semitones of each natural step above C in the same octave. */
private val STEP_SEMITONES: Map<Step, Int> = mapOf(
    Step.C to 0, Step.D to 2, Step.E to 4, Step.F to 5, Step.G to 7, Step.A to 9, Step.B to 11,
)

/** Standard lines MusicXML assigns when a clef omits its line. */
private val STANDARD_CLEF_LINES: Map<ClefSign, Int> = mapOf(ClefSign.G to 2, ClefSign.F to 4, ClefSign.C to 3)

private val DECIMAL = Regex("""([+-]?)([0-9]*)(?:\.([0-9]*))?""")

/**
 * Exact xs:decimal value after whitespace collapse, or null if the lexical form is invalid.
 * Values whose reduced components exceed [Int] throw [RationalException] rather than rounding.
 */
internal fun parseDecimal(text: String): Rational? {
    val match = DECIMAL.matchEntire(text.trim { it in WHITESPACE }) ?: return null
    val (sign, whole, fraction) = match.destructured
    if (whole.isEmpty() && fraction.isEmpty()) return null
    val fractionDigits = fraction.trimEnd('0')
    // The last fraction digit is non-zero, so the scaled numerator lacks a factor of 2 or 5 and the
    // reduced denominator keeps 2^n or 5^n; beyond 2^30 neither fits in Int.
    if (whole.trimStart('0').length > 10 || fractionDigits.length > 30) throw RationalException(RationalError.OVERFLOW)
    // Reduce value * 10^n by the shared factors of 2 and 5 as digits so wide numerators are not rejected early.
    var numeratorDigits = (whole + fractionDigits).trimStart('0').ifEmpty { "0" }
    var twos = fractionDigits.length
    var fives = fractionDigits.length
    while (twos > 0 && numeratorDigits.last().digitToInt() % 2 == 0) {
        numeratorDigits = divideDigits(numeratorDigits, 2)
        twos--
    }
    while (fives > 0 && numeratorDigits.last().digitToInt() % 5 == 0) {
        numeratorDigits = divideDigits(numeratorDigits, 5)
        fives--
    }
    if (numeratorDigits.length > 10 || twos > 30 || fives > 13) throw RationalException(RationalError.OVERFLOW)
    // Apply the sign before the range check so Int.MIN_VALUE stays representable.
    val numerator = (if (sign == "-") -1L else 1L) * numeratorDigits.toLong()
    val denominator = (1L shl twos) * POWERS_OF_FIVE[fives]
    if (numerator !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() || denominator > Int.MAX_VALUE) {
        throw RationalException(RationalError.OVERFLOW)
    }
    return Rational.of(numerator.toInt(), denominator.toInt())
}

/** Exact quotient of a non-negative decimal digit string by a divisor that divides it. */
private fun divideDigits(digits: String, divisor: Int): String {
    var remainder = 0
    val quotient = buildString {
        for (digit in digits) {
            val current = remainder * 10 + digit.digitToInt()
            append('0' + current / divisor)
            remainder = current % divisor
        }
    }
    return quotient.trimStart('0').ifEmpty { "0" }
}

private val POWERS_OF_FIVE = LongArray(14).also { powers ->
    powers[0] = 1
    for (index in 1 until powers.size) powers[index] = powers[index - 1] * 5
}

private fun positiveInt(text: String): Int? = text.trim { it in WHITESPACE }.toIntOrNull()?.takeIf { it > 0 }
