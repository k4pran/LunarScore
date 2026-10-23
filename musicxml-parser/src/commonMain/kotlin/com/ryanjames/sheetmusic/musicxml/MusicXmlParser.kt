package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Accidental
import com.ryanjames.sheetmusic.score.Attribute
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
import com.ryanjames.sheetmusic.score.Event
import com.ryanjames.sheetmusic.score.KeyMode
import com.ryanjames.sheetmusic.score.Measure
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.MetadataKind
import com.ryanjames.sheetmusic.score.NotatedValue
import com.ryanjames.sheetmusic.score.NoteExpression
import com.ryanjames.sheetmusic.score.NoteEvent
import com.ryanjames.sheetmusic.score.NotePitch
import com.ryanjames.sheetmusic.score.NoteRole
import com.ryanjames.sheetmusic.score.NoteValue
import com.ryanjames.sheetmusic.score.OrnamentType
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Placement
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.RationalError
import com.ryanjames.sheetmusic.score.RationalException
import com.ryanjames.sheetmusic.score.RelationshipKind
import com.ryanjames.sheetmusic.score.RestEvent
import com.ryanjames.sheetmusic.score.Score
import com.ryanjames.sheetmusic.score.Staff
import com.ryanjames.sheetmusic.score.StemDirection
import com.ryanjames.sheetmusic.score.Step
import com.ryanjames.sheetmusic.score.TimeModification
import com.ryanjames.sheetmusic.score.WedgeType
import com.ryanjames.sheetmusic.score.WrittenPitch

/**
 * Imports a UTF-8 partwise score with ordered metadata, parts and measures, plus one sequential
 * voice of pitched notes and rests on staff 1 with exact onsets and durations from divisions,
 * plus staff-1 key, simple time, clef and staff-line changes at their exact offsets. A timewise
 * score is normalized while streaming to the same projection: a part missing from a timewise
 * measure gets an empty measure there. Stems, beams and cumulative (nested) time modifications are
 * kept as note notation. Sound ties, notated ties, slurs and tuplets on imported events are
 * paired within each part across measures and voices; an unmatched endpoint is reported as
 * `UNMATCHED_SPAN` and kept with a null partner. Articulations, ornaments and note dynamics are
 * kept as note expressions; staff-1 directions (words, dynamics, metronome marks and wedges, with
 * separate sound tempo/dynamics and display placement/offset) become events at the cursor, and
 * wedges are paired like slurs. Other musical content is not implemented yet: meaningful unmapped
 * content rejects strict parsing, or is discarded with warnings under tolerant policy. Parts with
 * events or a valid staves declaration define staff 1; measure extent is the observed end of timed content. This is not an XSD validator.
 * Structural identity/reference errors are fatal under both policies. Input security, resource
 * limits and cooperative [isCancelled] checks are shared with the ordered XML reader.
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
    private val partsDeclaringStaves = mutableSetOf<String>()
    private val timewise = reader.root == ScoreXmlRoot.TIMEWISE
    private val timewiseMeasures = mutableListOf<MeasureHeader>()

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
                    staves = if (definition.id in partsDeclaringStaves || measures.any { it.events.isNotEmpty() }) {
                        listOf(Staff(1))
                    } else {
                        emptyList()
                    },
                    measures = measures,
                    name = definition.name,
                    abbreviation = definition.abbreviation,
                    metadata = definition.metadata.toList(),
                )
            },
            relationships = spans.map { it.relationship },
            title = title,
            metadata = metadata.toList(),
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
                        val value = text(child)
                        if (name == "part-name") definition.name = value else definition.abbreviation = value
                        provenance("$path.${if (name == "part-name") "name" else "abbreviation"}", child, id)
                    }
                    "identification" -> if (once(seen, name, child)) {
                        identification(child, "$path.metadata") { definition.metadata += it }
                    }
                    else -> unsupportedTree(child)
                }
            }
            if (context.canContinue && "part-name" !in seen) invalid(event, "A score-part requires a part-name")
            provenance("$path.id", event, id)
            declaredPartId = null
        }
    }

    private fun part(start: ScoreXmlEvent) {
        val id = requiredId(start) ?: return
        val definition = definitions[id]
        if (definition == null || id in bodies) {
            invalid(start.attributeLocation("id"), if (definition == null) "Unknown part ID: $id" else "Duplicate part body: $id")
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
            val definition = definitions[id]
            if (definition == null || !seen.add(id)) {
                invalid(
                    event.attributeLocation("id"),
                    if (definition == null) "Unknown part ID: $id" else "Duplicate part $id in measure ${header.number}",
                )
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

    private fun partBody(id: String) = PartBody(id, SpanResolver(id) { location, message ->
        context.report(DiagnosticCode.UNMATCHED_SPAN, location, message)
    })

    /** Appends empty measures for the timewise measures before [count] that lacked this part. */
    private fun padMissingMeasures(part: PartBody, count: Int) {
        val partIndex = definitions.getValue(part.id).index
        while (part.measures.size < count && context.canContinue) {
            val index = part.measures.size
            val header = timewiseMeasures[index]
            provenance("parts[$partIndex].measures[$index]", header.source, part.id)
            part.measures += Measure("measure-${index + 1}", header.number, Rational.ZERO, emptyList(), header.implicit)
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
     * Interprets one voice of sequential notes/rests at a measure-local quarter-note cursor.
     * Divisions persist across the measures of the current part. After an uninterpreted
     * backup/forward, later timed content in the measure is discarded rather than mistimed.
     */
    private fun measure(partId: String, path: String, number: Int, displayNumber: String, implicit: Boolean): Measure {
        val events = mutableListOf<Event>()
        var cursor = Rational.ZERO
        var extent = Rational.ZERO
        var cursorLost = false
        val wedges = mutableListOf<Pair<SpanType, SpanEndpoint>>()
        fun eventId() = "$partId:measure-$number:event-${events.size + 1}"
        fun add(event: Event, source: ScoreXmlEvent) {
            provenance("$path.events[${events.size}]", source)
            events += event
        }
        children { event ->
            when (event.localName()) {
                "note" -> if (cursorLost) {
                    unsupportedTree(event, "S06", "Note after an uninterpreted cursor move is discarded")
                } else {
                    val note = note(event, cursor, eventId()) ?: return@children
                    note.event?.let {
                        add(it, event)
                        registerSpans(it, note.marks, number)
                    }
                    cursor = note.end
                    if (cursor > extent) extent = cursor
                }
                "attributes" -> attributesElement(event, cursorLost) { attribute, source ->
                    add(AttributeEvent(eventId(), cursor, attribute), source)
                }
                "direction" -> if (cursorLost) {
                    unsupportedTree(event, "S06", "Direction after an uninterpreted cursor move is discarded")
                } else {
                    val direction = direction(event, cursor, eventId()) ?: return@children
                    direction.event?.let {
                        add(it, event)
                        wedges += wedgeEndpoints(it, direction.wedges, SpanPosition(number, direction.wedgeOnset))
                    }
                }
                "backup", "forward" -> {
                    unsupportedTree(event, "S06", "Cursor move is not interpreted; later timed content in this measure is discarded")
                    cursorLost = true
                }
                else -> unsupportedTree(event)
            }
        }
        registerWedges(wedges)
        return Measure("measure-$number", displayNumber, extent, events.toList(), implicit)
    }

    /**
     * Interprets divisions and staff-1 key, time, clef and staff-line state. Each state change
     * becomes an [AttributeEvent] at the current cursor offset, in source order, so intra-measure
     * changes apply exactly where they occur.
     */
    private fun attributesElement(start: ScoreXmlEvent, cursorLost: Boolean, add: (Attribute, ScoreXmlEvent) -> Unit) {
        attributes(start, feature = "S07")
        fun emit(attribute: Attribute?, source: ScoreXmlEvent) {
            if (attribute == null) return
            if (cursorLost) unsupported(source.location, "S06", "Attribute change after an uninterpreted cursor move is discarded")
            else add(attribute, source)
        }
        children { event ->
            when (val name = event.localName()) {
                "divisions" -> positiveDecimal(event, "S05", "Divisions")?.let { body.divisions = it }
                "key" -> emit(key(event), event)
                "time" -> emit(time(event), event)
                "clef" -> emit(clef(event), event)
                "staves" -> staves(event)
                "staff-details" -> emit(staffDetails(event), event)
                else -> unsupportedTree(event, ATTRIBUTES_CHILD_FEATURES[name] ?: "S07")
            }
        }
    }

    /**
     * Returns whether an attributes child scoped by its optional `number` attribute applies to staff 1.
     * Another staff is reported as discarded; a non-positive number is `INVALID_VALUE`. Callers skip
     * the content of an element that does not apply without validating it.
     */
    private fun appliesToStaffOne(start: ScoreXmlEvent, label: String): Boolean {
        val number = start.attribute("number") ?: return true
        return when (val staff = positiveInt(number)) {
            null -> {
                context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation("number"), "$label number must be a positive Int")
                false
            }
            1 -> true
            else -> {
                unsupported(start.location, "S08", "$label for staff $staff is not interpreted and is discarded")
                false
            }
        }
    }

    /** Consumes a discarded element without interpreting its children, so they cannot fail the parse. */
    private fun skipDiscarded(): Nothing? {
        skipTree()
        return null
    }

    /**
     * Interprets one simple beats/beat-type pair for staff 1; other time forms, and meters scoped
     * by `number` to another staff, are reported as discarded.
     */
    private fun time(start: ScoreXmlEvent): Attribute.TimeSignature? {
        attributes(start, setOf("number"), "S07")
        if (!appliesToStaffOne(start, "Time signature")) return skipDiscarded()
        val beats = mutableListOf<String>()
        val beatTypes = mutableListOf<String>()
        var simple = true
        children { event ->
            when (event.localName()) {
                "beats" -> beats += text(event, feature = "S07")
                "beat-type" -> beatTypes += text(event, feature = "S07")
                // Foreign extensions are reported on their own and keep the understood meter.
                null -> unsupportedTree(event)
                else -> {
                    simple = false
                    skipTree()
                }
            }
        }
        if (!context.canContinue) return null
        val beatCount = beats.singleOrNull()?.let(::positiveInt)
        val beatUnit = beatTypes.singleOrNull()?.let(::positiveInt)
        if (!simple || beatCount == null || beatUnit == null) {
            unsupported(start.location, "S07", "Only one positive integer beats/beat-type pair is interpreted; time discarded")
            return null
        }
        return Attribute.TimeSignature(beatCount, beatUnit)
    }

    /**
     * Interprets a traditional key (fifths and optional mode). Non-traditional keys are discarded;
     * cancellation and accidental octave display are reported while the key itself is kept.
     */
    private fun key(start: ScoreXmlEvent): Attribute.KeySignature? {
        attributes(start, setOf("number"), "S07")
        if (!appliesToStaffOne(start, "Key signature")) return skipDiscarded()
        val seen = mutableSetOf<String>()
        var fifths: Int? = null
        var mode: KeyMode? = null
        var traditional = true
        children { event ->
            when (val name = event.localName()) {
                "fifths" -> if (once(seen, name, event)) {
                    fifths = integer(event, "Fifths")
                }
                "mode" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S07").trim { it in WHITESPACE }
                    mode = KeyMode.entries.firstOrNull { it.name.lowercase() == text }
                    if (mode == null) unsupported(event.location, "S07", "Key mode $text is not interpreted; mode discarded")
                }
                "key-step", "key-alter", "key-accidental" -> {
                    traditional = false
                    skipTree()
                }
                "cancel" -> unsupportedTree(event, "S07", "Key cancellation display is not interpreted")
                "key-octave" -> unsupportedTree(event, "S07", "Key accidental octave display is not interpreted")
                else -> unsupportedTree(event, "S07")
            }
        }
        if (!context.canContinue) return null
        if (!traditional) {
            unsupported(start.location, "S07", "Non-traditional keys are not interpreted; key discarded")
            return null
        }
        val keyFifths = fifths ?: run {
            invalid(start, "A traditional key requires fifths")
            return null
        }
        return Attribute.KeySignature(keyFifths, mode)
    }

    /** Interprets a clef sign, line and octave change; a missing G/F/C line uses its standard line. */
    private fun clef(start: ScoreXmlEvent): Attribute.Clef? {
        attributes(start, setOf("number"), "S07")
        if (!appliesToStaffOne(start, "Clef")) return skipDiscarded()
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
     * Only one staff is interpreted; a larger staff count is reported as discarded. Any valid count
     * declares staff 1 for the current part, even when the part has no events.
     */
    private fun staves(start: ScoreXmlEvent) {
        val text = text(start, feature = "S08")
        if (!context.canContinue) return
        when (val count = positiveInt(text)) {
            null -> context.report(DiagnosticCode.INVALID_VALUE, start.location, "Staves must be a positive Int")
            else -> {
                partsDeclaringStaves += body.id
                if (count > 1) unsupported(start.location, "S08", "$count staves are not interpreted; only staff 1 is imported")
            }
        }
    }

    /** Interprets the staff-line count; other staff details are reported as discarded. */
    private fun staffDetails(start: ScoreXmlEvent): Attribute.StaffLines? {
        attributes(start, setOf("number"), "S17")
        if (!appliesToStaffOne(start, "Staff details")) return skipDiscarded()
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

    private fun integer(start: ScoreXmlEvent, label: String): Int? {
        val text = text(start, feature = "S07")
        if (!context.canContinue) return null
        val value = text.trim { it in WHITESPACE }.toIntOrNull()
        if (value == null) context.report(DiagnosticCode.INVALID_VALUE, start.location, "$label must be an Int")
        return value
    }

    /**
     * Parses a note in the current voice. A null result means it cannot be timed (an error, or an
     * untimed chord/grace note that was reported as discarded). A timed but discarded note keeps
     * its end cursor without an event, so later onsets stay exact.
     */
    private fun note(start: ScoreXmlEvent, onset: Rational, id: String): TimedNote? {
        attributes(start, feature = "S04")
        val seen = mutableSetOf<String>()
        var content: NoteContent? = null
        var duration: Rational? = null
        var durationEvent: ScoreXmlEvent? = null
        var role = NoteRole.NORMAL
        var voice = "1"
        var value: NoteValue? = null
        var dots = 0
        var accidental: Accidental? = null
        var timeModification: TimeModification? = null
        var stem: StemDirection? = null
        val beams = mutableListOf<Beam>()
        var discarded = false
        var untimed = false
        val marks = mutableListOf<SpanMark>()
        val expressions = mutableListOf<NoteExpression>()
        children { event ->
            // An untimed note's diagnostic covers its remaining children.
            if (untimed) {
                skipTree()
                return@children
            }
            when (val name = event.localName()) {
                "chord", "grace" -> {
                    val feature = if (name == "chord") "S06" else "S18"
                    unsupportedTree(event, feature, "A $name note is not interpreted; the note is discarded")
                    untimed = true
                }
                "cue" -> if (once(seen, name, event)) {
                    attributes(event, feature = "S18")
                    skipTree()
                    role = NoteRole.CUE
                }
                "pitch", "unpitched", "rest" -> if ("content" in seen) {
                    invalid(event, "A note has exactly one of pitch, unpitched or rest")
                } else {
                    seen += "content"
                    when (name) {
                        "pitch" -> content = pitch(event)
                        "rest" -> content = rest(event)
                        else -> {
                            unsupportedTree(event, "S16", "Unpitched notes are not interpreted; the note is discarded")
                            discarded = true
                        }
                    }
                }
                "duration" -> if (once(seen, name, event)) {
                    durationEvent = event
                    duration = positiveDecimal(event, "S05", "Duration")
                }
                "voice" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S06").trim { it in WHITESPACE }
                    if (text.isEmpty()) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Voice must be nonblank")
                    else voice = text
                }
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
                "staff" -> if (once(seen, name, event)) {
                    when (val staff = positiveInt(text(event, feature = "S06"))) {
                        null -> context.report(DiagnosticCode.INVALID_VALUE, event.location, "Staff must be a positive Int")
                        1 -> Unit
                        else -> {
                            unsupported(event.location, "S06", "Staff $staff is not interpreted; the note is discarded")
                            discarded = true
                        }
                    }
                }
                else -> unsupportedTree(event, NOTE_CHILD_FEATURES[name] ?: "S04")
            }
        }
        if (untimed || !context.canContinue) return null
        if ("content" !in seen) {
            invalid(start, "A note requires pitch, unpitched or rest")
            return null
        }
        val durationSource = durationEvent ?: run {
            invalid(start, "A non-grace note requires a duration")
            return null
        }
        val currentDivisions = body.divisions ?: run {
            invalid(durationSource, "A duration requires preceding divisions")
            return null
        }
        val sourceDuration = duration ?: return null
        val quarters = exact(durationSource.location) { sourceDuration / currentDivisions } ?: return null
        val end = exact(durationSource.location) { onset + quarters } ?: return null
        if (discarded) return TimedNote(null, end, emptyList())
        val notation = NotatedValue(value, dots, timeModification, accidental, stem, beams.toList(), expressions.toList())
        val event = when (val noteContent = content ?: return null) {
            is NoteContent.Rest -> RestEvent(id, onset, quarters, voice = voice, notation = notation, wholeMeasure = noteContent.wholeMeasure)
            is NoteContent.Pitch -> NoteEvent(id, onset, quarters, noteContent.pitch, voice = voice, notation = notation, role = role)
        }
        return TimedNote(event, end, marks.toList())
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
        val (voice, pitch) = when (event) {
            is NoteEvent -> event.voice to (event.pitch as? NotePitch.Pitched)?.written
            is RestEvent -> event.voice to null
            is AttributeEvent, is DirectionEvent -> return
        }
        val endpoints = marks.mapNotNull { mark ->
            val key = when {
                mark.kind == RelationshipKind.SLUR || mark.kind == RelationshipKind.TUPLET -> mark.number
                pitch == null -> {
                    unsupported(mark.location, "S09", "A tie on a rest is not interpreted; the tie is discarded")
                    return@mapNotNull null
                }
                else -> pitchKey(pitch) + if (mark.number == "1") "" else " (number ${mark.number})"
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
     * In a direction its own placement is ignored, as MusicXML specifies; the direction's governs.
     */
    private fun dynamics(start: ScoreXmlEvent, feature: String, inDirection: Boolean = false): Dynamics? {
        attributes(start, setOf("placement"), feature)
        val placement = if (inDirection) null else placement(start)
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
        return Dynamics(markings.toList(), placement)
    }

    /**
     * Reads a direction at [onset]. Printed direction types become [DirectionMark]s in source
     * order, the `sound` child becomes separate [DirectionSound] playback data, and placement,
     * directive and `offset` become [DirectionDisplay]. An offset with `sound="yes"` also shifts the
     * sound. A direction for another staff, or with nothing interpretable, has a null event. A null
     * result means a fatal diagnostic was reported.
     */
    private fun direction(start: ScoreXmlEvent, onset: Rational, id: String): TimedDirection? {
        attributes(start, setOf("placement", "directive"), "S13")
        val placement = placement(start)
        val directive = yesNo(start, "directive") ?: return null
        val seen = mutableSetOf<String>()
        val marks = mutableListOf<DirectionMark>()
        val wedges = mutableListOf<SpanMark>()
        var voice: String? = null
        var offset: Rational? = null
        var offsetSource: ScoreXmlEvent? = null
        var offsetAffectsSound = false
        var sound: DirectionSound? = null
        var discarded = false
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
                "voice" -> if (once(seen, name, event)) {
                    val text = text(event, feature = "S13").trim { it in WHITESPACE }
                    if (text.isEmpty()) context.report(DiagnosticCode.INVALID_VALUE, event.location, "Voice must be nonblank")
                    else voice = text
                }
                "staff" -> if (once(seen, name, event)) {
                    when (val staff = positiveInt(text(event, feature = "S13"))) {
                        null -> context.report(DiagnosticCode.INVALID_VALUE, event.location, "Staff must be a positive Int")
                        1 -> Unit
                        else -> {
                            unsupported(event.location, "S06", "Staff $staff is not interpreted; the direction is discarded")
                            discarded = true
                        }
                    }
                }
                "sound" -> if (once(seen, name, event)) sound = sound(event)
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
            val divisions = body.divisions ?: run {
                invalid(source, "An offset requires preceding divisions")
                return null
            }
            exact(source.location) { value / divisions } ?: return null
        } ?: Rational.ZERO
        if (discarded || (marks.isEmpty() && sound == null)) return TimedDirection(null, emptyList(), onset)
        val wedgeOnset = if (wedges.isEmpty()) onset else exact(start.location) { onset + quarters } ?: return null
        val event = DirectionEvent(
            id,
            onset,
            marks.toList(),
            voice = voice,
            sound = sound?.copy(offset = if (offsetAffectsSound) quarters else Rational.ZERO),
            display = DirectionDisplay(placement, directive, quarters),
        )
        return TimedDirection(event, wedges.toList(), wedgeOnset)
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
                "words" -> marks += DirectionMark.Words(text(event, feature = "S13"))
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
     * `metronome-note` forms are reported as discarded, as are `parentheses` and display attributes.
     */
    private fun metronome(start: ScoreXmlEvent): DirectionMark.Metronome? {
        attributes(start, feature = "S13")
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
        return DirectionMark.Metronome(unit, rate, dots)
    }

    /** A hairpin endpoint paired by `number` (1 to 16) like a slur; display attributes are S13 discarded. */
    private fun wedge(start: ScoreXmlEvent): Pair<DirectionMark.Wedge, SpanMark>? {
        attributes(start, setOf("type", "number"), "S13")
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
        return DirectionMark.Wedge(type) to SpanMark(RelationshipKind.WEDGE, spanType, number, start.location)
    }

    /**
     * Reads the `tempo` and `dynamics` playback attributes of a direction's `sound`. Other sound
     * attributes and children are reported as S26 discarded; a sound with neither attribute is null.
     */
    private fun sound(start: ScoreXmlEvent): DirectionSound? {
        attributes(start, setOf("tempo", "dynamics"), "S26")
        val tempo = nonNegativeDecimalAttribute(start, "tempo", "Sound tempo")
        val dynamics = nonNegativeDecimalAttribute(start, "dynamics", "Sound dynamics")
        children { event -> unsupportedTree(event, "S26") }
        if (!context.canContinue || (tempo == null && dynamics == null)) return null
        return DirectionSound(tempo, dynamics)
    }

    /** A direction's wedge endpoints at [position]: stops, then continue points, then starts. */
    private fun wedgeEndpoints(event: DirectionEvent, marks: List<SpanMark>, position: SpanPosition): List<Pair<SpanType, SpanEndpoint>> =
        listOf(SpanType.STOP, SpanType.CONTINUE, SpanType.START).flatMap { type ->
            marks.filter { it.type == type }.map { mark ->
                type to SpanEndpoint(mark.kind, mark.number, event.id, event.voice.orEmpty(), position, mark.location)
            }
        }

    /**
     * Passes one measure's wedge endpoints to the part's resolver in display-position order, keeping
     * document order at equal positions, so an offset cannot pair a stop with a later-displayed start.
     * Each endpoint keeps its document order for numbering, so offsets do not change wedge IDs.
     */
    private fun registerWedges(endpoints: List<Pair<SpanType, SpanEndpoint>>) {
        val resolver = body.spans
        val firstOrder = resolver.reserveOrders(endpoints.size)
        endpoints.withIndex().sortedBy { (_, entry) -> entry.second.position }.forEach { (index, entry) ->
            val (type, endpoint) = entry
            val order = firstOrder + index
            when (type) {
                SpanType.STOP -> resolver.stop(endpoint, order)
                SpanType.CONTINUE -> resolver.continuePoint(endpoint)
                SpanType.START -> resolver.start(endpoint, order)
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

    private fun accidental(start: ScoreXmlEvent): Accidental? {
        val cautionary = yesNo(start, "cautionary") ?: return null
        val editorial = yesNo(start, "editorial") ?: return null
        val name = text(start, setOf("cautionary", "editorial"), "S04").trim { it in WHITESPACE }
        if (name.isEmpty()) {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "Accidental must be nonblank")
            return null
        }
        return Accidental(name, cautionary, editorial)
    }

    private fun yesNo(start: ScoreXmlEvent, name: String): Boolean? = when (start.attribute(name)) {
        null, "no" -> false
        "yes" -> true
        else -> {
            context.report(DiagnosticCode.INVALID_VALUE, start.attributeLocation(name), "Expected yes or no")
            null
        }
    }

    private fun positiveDecimal(start: ScoreXmlEvent, feature: String, label: String): Rational? {
        val value = decimal(start, feature, label) ?: return null
        if (value <= Rational.ZERO) {
            context.report(DiagnosticCode.INVALID_VALUE, start.location, "$label must be positive")
            return null
        }
        return value
    }

    private fun decimal(start: ScoreXmlEvent, feature: String, label: String, allowed: Set<String> = emptySet()): Rational? {
        val text = text(start, allowed, feature)
        if (!context.canContinue) return null
        return exact(start.location) {
            parseDecimal(text) ?: run {
                context.report(DiagnosticCode.INVALID_VALUE, start.location, "$label must be a decimal number")
                null
            }
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

    private fun requiredId(event: ScoreXmlEvent): String? {
        val id = event.attribute("id")?.trim { it in " \t\r\n" }
        if (id.isNullOrEmpty() || id.any { it in " \t\r\n" }) {
            invalid(event.attributeLocation("id"), "A nonblank part ID without whitespace is required")
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

    private fun invalid(event: ScoreXmlEvent, message: String) = invalid(event.location, message)

    private fun invalid(location: SourceLocation, message: String) {
        context.report(DiagnosticCode.INVALID_STRUCTURE, location.withDeclaredPart(), message)
    }

    private fun SourceLocation.withDeclaredPart(): SourceLocation = copy(partId = declaredPartId ?: partId)

    private class TimedNote(val event: Event?, val end: Rational, val marks: List<SpanMark>)

    /**
     * A read direction; a null [event] means it was discarded with a diagnostic. [wedgeOnset] is
     * the measure-local position, including the display offset, that orders its wedge endpoints.
     */
    private class TimedDirection(val event: DirectionEvent?, val wedges: List<SpanMark>, val wedgeOnset: Rational)

    /** A span endpoint read from a note, before the note's event identity is known. */
    private class SpanMark(val kind: RelationshipKind, val type: SpanType, val number: String, val location: SourceLocation)

    private sealed interface NoteContent {
        class Pitch(val pitch: NotePitch) : NoteContent
        class Rest(val wholeMeasure: Boolean) : NoteContent
    }

    /** A part's measures plus the divisions and open spans that persist across its measures. */
    private class PartBody(val id: String, val spans: SpanResolver) {
        val measures = mutableListOf<Measure>()
        var divisions: Rational? = null
    }

    private class MeasureHeader(val number: String, val implicit: Boolean, val source: ScoreXmlEvent)

    private class PartDefinition(val id: String, val index: Int, val location: SourceLocation) {
        var name: String? = null
        var abbreviation: String? = null
        val metadata = mutableListOf<MetadataEntry.Text>()
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

private val NOTE_TYPES: Map<String, NoteValue> = mapOf(
    "maxima" to NoteValue.MAXIMA, "long" to NoteValue.LONG, "breve" to NoteValue.BREVE,
    "whole" to NoteValue.WHOLE, "half" to NoteValue.HALF, "quarter" to NoteValue.QUARTER,
    "eighth" to NoteValue.EIGHTH, "16th" to NoteValue.N16, "32nd" to NoteValue.N32,
    "64th" to NoteValue.N64, "128th" to NoteValue.N128, "256th" to NoteValue.N256,
    "512th" to NoteValue.N512, "1024th" to NoteValue.N1024,
)

private val NOTE_CHILD_FEATURES: Map<String, String> = mapOf(
    "instrument" to "S16", "footnote" to "S25", "level" to "S25",
    "notehead" to "S17", "notehead-text" to "S17", "lyric" to "S14",
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

private val ATTRIBUTES_CHILD_FEATURES: Map<String, String> = mapOf(
    "part-symbol" to "S08", "instruments" to "S16", "transpose" to "S16", "for-part" to "S16",
    "measure-style" to "S24",
)

private enum class SpanType { START, STOP, CONTINUE, LET_RING }

private val TIE_TYPES: Map<String, SpanType> = mapOf("start" to SpanType.START, "stop" to SpanType.STOP)

private val TIED_TYPES: Map<String, SpanType> = TIE_TYPES + mapOf("continue" to SpanType.CONTINUE, "let-ring" to SpanType.LET_RING)

private val SLUR_TYPES: Map<String, SpanType> = TIE_TYPES + ("continue" to SpanType.CONTINUE)

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

private val CLEF_SIGNS: Map<String, ClefSign> = mapOf(
    "G" to ClefSign.G, "F" to ClefSign.F, "C" to ClefSign.C, "percussion" to ClefSign.PERCUSSION,
    "TAB" to ClefSign.TAB, "jianpu" to ClefSign.JIANPU, "none" to ClefSign.NONE,
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
