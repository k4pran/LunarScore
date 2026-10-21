package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Measure
import com.ryanjames.sheetmusic.score.MetadataEntry
import com.ryanjames.sheetmusic.score.MetadataKind
import com.ryanjames.sheetmusic.score.Part
import com.ryanjames.sheetmusic.score.Rational
import com.ryanjames.sheetmusic.score.Score

/**
 * Imports a UTF-8 partwise score skeleton with ordered metadata, parts and measures.
 * Musical events and staff state are not implemented yet: meaningful unmapped content rejects
 * strict parsing, or is discarded with warnings under tolerant policy. Measures then have zero
 * extent and no events; parts have no staff definitions. This is not an XSD validator.
 * Structural identity/reference errors are fatal under both policies. Input security, resource
 * limits and cooperative [isCancelled] checks are shared with the ordered XML reader.
 */
public fun parseMusicXml(
    input: ByteArray,
    options: ParseOptions = ParseOptions(),
    sourceName: String? = null,
    isCancelled: () -> Boolean = { false },
): ParseResult<Score> = readScoreXml(input, options, sourceName, isCancelled) { reader ->
    PartwiseScoreParser(reader).parse()
}

private class PartwiseScoreParser(private val reader: ScoreXmlReader) {
    private val context = reader.context
    private val metadata = mutableListOf<MetadataEntry>()
    private val definitions = linkedMapOf<String, PartDefinition>()
    private val bodies = mutableMapOf<String, List<Measure>>()
    private var declaredPartId: String? = null

    fun parse(): Score {
        val root = checkNotNull(reader.current)
        if (reader.root != ScoreXmlRoot.PARTWISE) {
            context.report(DiagnosticCode.UNSUPPORTED_ROOT, root.location, "Timewise normalization is not implemented")
            return Score()
        }
        attributes(root, setOf("version"), "S01")
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
                "part" -> 7
                else -> null
            }
            if (nextRank != null) {
                if (nextRank < rank || (name != "credit" && name != "part" && !seen.add(name))) {
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
                "part" -> {
                    if ("part-list" !in seen) invalid(event, "A part-list is required before parts")
                    else part(event)
                }
                else -> unsupportedTree(event)
            }
        }
        if (context.canContinue) {
            when {
                "part-list" !in seen -> invalid(root, "Missing required part-list")
                definitions.isEmpty() -> invalid(root, "The part-list must declare a score-part")
                else -> definitions.values.firstOrNull { it.id !in bodies }?.let {
                    invalid(it.location, "Declared score-part ${it.id} has no part body")
                }
            }
        }
        val title = metadata.filterIsInstance<MetadataEntry.Text>().let { entries ->
            entries.firstOrNull { it.kind == MetadataKind.MOVEMENT_TITLE && it.text.isNotBlank() }?.text
                ?: entries.firstOrNull { it.kind == MetadataKind.WORK_TITLE && it.text.isNotBlank() }?.text
        }
        return Score(
            parts = definitions.values.map { definition ->
                Part(
                    id = definition.id,
                    staves = emptyList(),
                    measures = bodies[definition.id].orEmpty().toList(),
                    name = definition.name,
                    abbreviation = definition.abbreviation,
                    metadata = definition.metadata.toList(),
                )
            },
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
        val path = "parts[${definition.index}]"
        val measures = mutableListOf<Measure>()
        bodies[id] = measures
        provenance(path, start)
        children { event ->
            if (event.localName() == "measure") {
                val number = event.attribute("number")
                if (number == null) {
                    invalid(event.attributeLocation("number"), "A measure requires a number")
                    return@children
                }
                val implicit = when (event.attribute("implicit")) {
                    null, "no" -> false
                    "yes" -> true
                    else -> {
                        context.report(DiagnosticCode.INVALID_VALUE, event.attributeLocation("implicit"), "Expected yes or no")
                        return@children
                    }
                }
                attributes(event, setOf("number", "implicit"), "S01")
                provenance("$path.measures[${measures.size}]", event)
                measures += Measure("measure-${measures.size + 1}", number, Rational.ZERO, implicit = implicit)
                children(::unsupportedTree)
            } else {
                unsupportedTree(event)
            }
        }
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

    private fun text(start: ScoreXmlEvent, allowed: Set<String> = emptySet()): String {
        attributes(start, allowed)
        return buildString {
            while (context.canContinue) {
                val event = reader.next() ?: break
                when (event.kind) {
                    ScoreXmlEventKind.END_ELEMENT -> break
                    ScoreXmlEventKind.TEXT -> append(event.text)
                    ScoreXmlEventKind.START_ELEMENT -> unsupportedTree(event)
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

    private fun unsupportedTree(start: ScoreXmlEvent) {
        unsupported(start.location, feature(start), "Unmapped subtree: ${start.name}")
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
