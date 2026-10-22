package com.ryanjames.sheetmusic.musicxml

import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming

/** Expanded names deliberately compare namespace URI and local name, never the source prefix. */
internal data class XmlName(val namespaceUri: String, val localName: String)

internal data class ScoreXmlAttribute(val name: XmlName, val value: String)

internal enum class ScoreXmlRoot { PARTWISE, TIMEWISE }

internal enum class ScoreXmlEventKind { START_ELEMENT, END_ELEMENT, TEXT }

/** A snapshot of one event. Text includes whitespace and CDATA, without coalescing mixed content. */
internal data class ScoreXmlEvent(
    val kind: ScoreXmlEventKind,
    val location: SourceLocation,
    val name: XmlName? = null,
    val attributes: List<ScoreXmlAttribute> = emptyList(),
    val text: String? = null,
)

/**
 * Parser-local input boundary, not a semantic score import API. The consumer starts on the root
 * start event. Unread input is drained before success, so malformed tails cannot yield a value.
 * Fatal findings discard the consumer's value/provenance. Consumer exceptions propagate unchanged.
 * Input bytes are caller-owned; decoding and individual tokens are bounded by maxInputBytes.
 */
internal fun <T : Any> readScoreXml(
    input: ByteArray,
    options: ParseOptions = ParseOptions(),
    sourceName: String? = null,
    isCancelled: () -> Boolean = { false },
    consume: (ScoreXmlReader) -> T,
): ParseResult<T> {
    val context = ParseContext(options)
    val origin = SourceLocation(sourceName = sourceName)
    var wasCancelled = false
    fun cancellationRequested(): Boolean {
        wasCancelled = wasCancelled || isCancelled()
        return wasCancelled
    }
    fun fail(code: DiagnosticCode, message: String): ParseResult.Failure {
        context.report(code, origin, message)
        return context.failure()
    }
    if (cancellationRequested()) return fail(DiagnosticCode.CANCELLED, "XML reading cancelled")
    if (input.size > options.limits.maxInputBytes) return fail(DiagnosticCode.INPUT_LIMIT, "XML byte limit exceeded")
    val xml = try {
        input.decodeToString(throwOnInvalidSequence = true).removePrefix("\uFEFF")
    } catch (_: CharacterCodingException) {
        return fail(DiagnosticCode.UNSUPPORTED_ENCODING, "Only well-formed UTF-8 input is supported")
    }
    if ('\u0000' in xml) return fail(DiagnosticCode.UNSUPPORTED_ENCODING, "NUL-bearing input is not supported UTF-8 XML")
    // No declaration, resolver or entity table from untrusted input may reach xmlutil. This is
    // intentionally conservative even inside comments/CDATA; see docs/musicxml/xml-reader.md.
    if ("<!DOCTYPE" in xml || "<!ENTITY" in xml) {
        return fail(DiagnosticCode.UNSAFE_XML, "DTD and entity declarations are disabled")
    }
    val encoding = Regex("""^<\?xml\s[^?]*encoding\s*=\s*['"]([^'"]+)['"]""")
        .find(xml)?.groupValues?.get(1)
    if (encoding != null && !encoding.equals("UTF-8", ignoreCase = true)) {
        return fail(DiagnosticCode.UNSUPPORTED_ENCODING, "Only UTF-8 declarations are supported")
    }
    val preflightFailure = preflightXml(xml, options.limits, ::cancellationRequested)
    if (preflightFailure != null) {
        return fail(
            preflightFailure,
            when (preflightFailure) {
                DiagnosticCode.CANCELLED -> "XML reading cancelled"
                DiagnosticCode.ATTRIBUTE_LIMIT -> "XML per-element or total attribute limit exceeded"
                else -> "Invalid XML character or lexical structure"
            },
        )
    }
    if (cancellationRequested()) return fail(DiagnosticCode.CANCELLED, "XML reading cancelled")

    val reader = try {
        xmlStreaming.newGenericReader(xml, expandEntities = true)
    } catch (_: XmlException) {
        return fail(DiagnosticCode.MALFORMED_XML, "Cannot initialize XML reader")
    }
    try {
        val cursor = ScoreXmlReader(reader, context, options.limits, sourceName, ::cancellationRequested)
        if (cursor.next() == null) return context.failure()
        val value = consume(cursor)
        while (cursor.next() != null) {
            // Validate the whole document even when the consumer only needs its header.
        }
        return context.finish(value)
    } finally {
        reader.close()
    }
}

/**
 * Ordered, bounded cursor. No DOM or document-sized event list. A null next event means EOF or
 * fatal failure; only readScoreXml may finalize the result. Semantic adapters share [context].
 */
@OptIn(ExperimentalXmlUtilApi::class)
internal class ScoreXmlReader(
    private val reader: XmlReader,
    val context: ParseContext,
    private val limits: ParseLimits,
    private val sourceName: String?,
    private val isCancelled: () -> Boolean,
) {
    var root: ScoreXmlRoot? = null
        private set
    var current: ScoreXmlEvent? = null
        private set
    private val stack = mutableListOf<ElementFrame>()
    private var elements = 0
    private var sourcePathCharacters = 0
    private var ended = false

    /** Advances once; end events carry the same path and musical context as their start event. */
    fun next(): ScoreXmlEvent? {
        current = null
        if (ended || !context.canContinue) return null
        while (context.canContinue) {
            if (isCancelled()) return stop(DiagnosticCode.CANCELLED, location(), "XML reading cancelled")
            val event = try {
                if (!reader.hasNext()) {
                    ended = true
                    if (root == null || stack.isNotEmpty()) {
                        return stop(DiagnosticCode.MALFORMED_XML, location(), "Missing or incomplete score root")
                    }
                    return null
                }
                when (reader.next()) {
                    EventType.START_ELEMENT -> startElement()
                    EventType.END_ELEMENT -> endElement()
                    EventType.TEXT, EventType.CDSECT, EventType.IGNORABLE_WHITESPACE -> {
                        val text = reader.text
                        if (stack.isEmpty()) {
                            if (text.any { it !in " \t\r\n" }) {
                                return stop(DiagnosticCode.MALFORMED_XML, location(), "Text outside score root")
                            }
                            null
                        } else {
                            ScoreXmlEvent(ScoreXmlEventKind.TEXT, location(), text = text)
                        }
                    }
                    EventType.DOCDECL, EventType.ENTITY_REF ->
                        return stop(DiagnosticCode.UNSAFE_XML, location(), "Unsafe XML event")
                    EventType.START_DOCUMENT -> {
                        if (reader.version != null && reader.version != "1.0") {
                            return stop(DiagnosticCode.UNSUPPORTED_XML_VERSION, location(), "Only XML 1.0 is supported")
                        }
                        null
                    }
                    else -> null // Comments, processing instructions and document boundaries have no score content.
                }
            } catch (exception: XmlException) {
                return stop(
                    DiagnosticCode.MALFORMED_XML,
                    location(exception.locationInfo ?: reader.extLocationInfo),
                    "Malformed XML or unresolved entity",
                )
            } catch (_: IllegalArgumentException) {
                // The generic reader can reject invalid character references via argument checks.
                return stop(DiagnosticCode.MALFORMED_XML, location(reader.extLocationInfo), "Invalid XML token")
            } catch (_: IllegalStateException) {
                // xmlutil 1.0.2's buffer reports some truncated tokens/trailing text as state errors.
                // This boundary does not wrap consumer code, so application failures still propagate.
                return stop(DiagnosticCode.MALFORMED_XML, location(reader.extLocationInfo), "Incomplete XML token")
            }
            if (event != null) {
                current = event
                return event
            }
        }
        return null
    }

    private fun startElement(): ScoreXmlEvent? {
        val name = XmlName(reader.namespaceURI, reader.localName)
        val parent = stack.lastOrNull()
        val siblingIndex = parent?.let {
            val count = (it.childCounts[name] ?: 0) + 1
            it.childCounts[name] = count
            count
        } ?: 1
        val siblingLabel = siblingIndex.toString()
        // Include the full parent prefix for every newly allocated path. Long arithmetic avoids
        // overflow when callers raise the budgets; do not concatenate expanded names first.
        val pathLength = (parent?.path?.length ?: 0).toLong() + 1 + name.localName.length +
            (if (name.namespaceUri.isEmpty()) 0L else name.namespaceUri.length.toLong() + 2) +
            2 + siblingLabel.length
        if (pathLength > limits.maxSourcePathCharacters - sourcePathCharacters) {
            // Reuse the nearest existing parent path; even the diagnostic must not build the
            // rejected child path. The budget is cumulative and is not replenished on pop.
            return stop(DiagnosticCode.PATH_LIMIT, location(), "Cumulative source path character limit exceeded")
        }
        sourcePathCharacters += pathLength.toInt()
        val path = buildString(pathLength.toInt()) {
            append(parent?.path.orEmpty())
            append('/')
            if (name.namespaceUri.isNotEmpty()) {
                append('{')
                append(name.namespaceUri)
                append('}')
            }
            append(name.localName)
            append('[')
            append(siblingLabel)
            append(']')
        }
        val startLocation = location().copy(path = path)
        if (stack.size >= limits.maxDepth) return stop(DiagnosticCode.DEPTH_LIMIT, startLocation, "XML depth limit exceeded")
        if (elements >= limits.maxElements) return stop(DiagnosticCode.ELEMENT_LIMIT, startLocation, "XML element limit exceeded")
        elements++
        // Preflight bounds the dependency's token allocation; repeat the per-element check here
        // before building attribute snapshots or namespace/expanded-name sets.
        if (reader.attributeCount.toLong() + reader.namespaceDecls.size > limits.maxAttributesPerElement) {
            return stop(DiagnosticCode.ATTRIBUTE_LIMIT, startLocation, "XML per-element attribute limit exceeded")
        }
        val declaredPrefixes = mutableSetOf<String>()
        for (namespace in reader.namespaceDecls) {
            if (!declaredPrefixes.add(namespace.prefix) || namespace.prefix == "xmlns" ||
                (namespace.prefix.isNotEmpty() && namespace.namespaceURI.isEmpty()) ||
                (namespace.prefix == "xml") != (namespace.namespaceURI == "http://www.w3.org/XML/1998/namespace") ||
                namespace.namespaceURI == "http://www.w3.org/2000/xmlns/"
            ) {
                return stop(DiagnosticCode.MALFORMED_XML, startLocation, "Invalid or duplicate namespace declaration")
            }
        }
        if (parent == null) {
            if (root != null) return stop(DiagnosticCode.MALFORMED_XML, startLocation, "Multiple score roots")
            root = when (name.localName) {
                "score-partwise" -> ScoreXmlRoot.PARTWISE
                "score-timewise" -> ScoreXmlRoot.TIMEWISE
                else -> return stop(DiagnosticCode.UNSUPPORTED_ROOT, startLocation, "Unsupported score root: ${name.localName}")
            }
            if (name.namespaceUri.isNotEmpty()) {
                return stop(DiagnosticCode.UNSUPPORTED_NAMESPACE, startLocation, "MusicXML score elements require no namespace")
            }
        }
        val attributeNames = mutableSetOf<XmlName>()
        val attributes = (0 until reader.attributeCount).map { index ->
            val attributeName = XmlName(reader.getAttributeNamespace(index), reader.getAttributeLocalName(index))
            if (!attributeNames.add(attributeName)) {
                return stop(DiagnosticCode.MALFORMED_XML, startLocation, "Duplicate expanded attribute name")
            }
            ScoreXmlAttribute(attributeName, reader.getAttributeValue(index))
        }
        fun attribute(localName: String): String? = attributes.firstOrNull { it.name == XmlName("", localName) }?.value
        var partId = parent?.partId
        var measureNumber = parent?.measureNumber
        var measureIndex = parent?.measureIndex
        if (name.namespaceUri.isEmpty()) {
            if (name.localName == "part" &&
                (parent?.name == XmlName("", "score-partwise") || parent?.name == XmlName("", "measure"))
            ) {
                partId = attribute("id")
            }
            if (name.localName == "measure" &&
                (parent?.name == XmlName("", "part") || parent?.name == XmlName("", "score-timewise"))
            ) {
                measureNumber = attribute("number")
                measureIndex = siblingIndex - 1
            }
        }
        stack += ElementFrame(name, path, partId, measureNumber, measureIndex)
        return ScoreXmlEvent(
            ScoreXmlEventKind.START_ELEMENT,
            startLocation.copy(partId = partId, measureNumber = measureNumber, measureIndex = measureIndex),
            name,
            attributes,
        )
    }

    private fun endElement(): ScoreXmlEvent? {
        val name = XmlName(reader.namespaceURI, reader.localName)
        if (stack.lastOrNull()?.name != name) {
            return stop(DiagnosticCode.MALFORMED_XML, location(), "Unmatched end element")
        }
        val event = ScoreXmlEvent(ScoreXmlEventKind.END_ELEMENT, location(), name)
        stack.removeAt(stack.lastIndex)
        return event
    }

    private fun location(info: XmlReader.LocationInfo? = reader.startLocationInfo): SourceLocation {
        val position = info as? XmlReader.ExtLocationInfo
        val frame = stack.lastOrNull()
        return SourceLocation(
            sourceName = sourceName,
            path = frame?.path,
            line = position?.line?.takeIf { it > 0 },
            column = position?.col?.takeIf { it > 0 },
            partId = frame?.partId,
            measureNumber = frame?.measureNumber,
            measureIndex = frame?.measureIndex,
        )
    }

    private fun stop(code: DiagnosticCode, location: SourceLocation, message: String): Nothing? {
        context.report(code, location, message)
        ended = true
        return null
    }

    private class ElementFrame(
        val name: XmlName,
        val path: String,
        val partId: String?,
        val measureNumber: String?,
        val measureIndex: Int?,
        val childCounts: MutableMap<XmlName, Int> = mutableMapOf(),
    )
}

/**
 * xmlutil 1.0.2 does not reject every XML 1.0 character and may overflow numeric references.
 * Validate scalars/lexical gaps and bound attributes before it sees them. References in
 * comments/CDATA/PIs are literal. Namespace/declaration attributes count toward the same budgets.
 * This is a linear preflight over bounded input, not a replacement XML grammar parser.
 */
private fun preflightXml(xml: String, limits: ParseLimits, isCancelled: () -> Boolean): DiagnosticCode? {
    for (index in xml.indices) {
        if (index % 4096 == 0 && isCancelled()) return DiagnosticCode.CANCELLED
        val character = xml[index]
        if ((character.code < 0x20 && character !in "\t\r\n") || character == '\uFFFE' || character == '\uFFFF') {
            return DiagnosticCode.MALFORMED_XML
        }
    }
    var index = 0
    var inTag = false
    var quote: Char? = null
    var attributesInTag = 0
    var totalAttributes = 0
    while (index < xml.length) {
        if (isCancelled()) return DiagnosticCode.CANCELLED
        val character = xml[index]
        if (quote != null) {
            if (character == '<') return DiagnosticCode.MALFORMED_XML
            if (character == quote) {
                quote = null
                if (index + 1 < xml.length && xml[index + 1] !in " \t\r\n/>?") return DiagnosticCode.MALFORMED_XML
                index++
                continue
            }
        }
        val xmlDeclaration = xml.startsWith("<?xml", index) &&
            index + 5 < xml.length && xml[index + 5] in " \t\r\n?"
        val terminator = when {
            !inTag && xml.startsWith("<!--", index) -> "-->"
            !inTag && xml.startsWith("<![CDATA[", index) -> "]]>"
            !inTag && xml.startsWith("<?", index) && !xmlDeclaration -> "?>"
            else -> null
        }
        if (terminator != null) {
            val contentStart = index + when (terminator) {
                "-->" -> 4
                "]]>" -> 9
                else -> 2
            }
            val end = xml.indexOf(terminator, contentStart)
            if (end < 0) return DiagnosticCode.MALFORMED_XML
            if (terminator == "-->" && xml.indexOf("--", contentStart) != end) return DiagnosticCode.MALFORMED_XML
            index = end + terminator.length
        } else if (xml.startsWith("&#", index)) {
            index += 2
            val radix = if (index < xml.length && xml[index] == 'x') {
                index++
                16
            } else {
                10
            }
            val start = index
            var codePoint = 0
            while (index < xml.length && xml[index] != ';') {
                if (index % 4096 == 0 && isCancelled()) return DiagnosticCode.CANCELLED
                val digitCharacter = xml[index]
                if (digitCharacter !in '0'..'9' &&
                    !(radix == 16 && (digitCharacter in 'a'..'f' || digitCharacter in 'A'..'F'))
                ) return DiagnosticCode.MALFORMED_XML
                val digit = digitCharacter.digitToInt(radix)
                if (codePoint > (0x10FFFF - digit) / radix) return DiagnosticCode.MALFORMED_XML
                codePoint = codePoint * radix + digit
                index++
            }
            if (index == start || index == xml.length || !isXml10CodePoint(codePoint)) return DiagnosticCode.MALFORMED_XML
            index++
        } else {
            if (quote == null) {
                when {
                    character == '<' -> {
                        if (inTag) return DiagnosticCode.MALFORMED_XML
                        inTag = true
                        attributesInTag = 0
                    }
                    character == '>' -> inTag = false
                    inTag && (character == '\'' || character == '"') -> quote = character
                    inTag && character == '=' -> {
                        // In well-formed markup each unquoted '=' starts one attribute value.
                        // Count namespace and declaration attributes too, without allocating
                        // names/values or invoking xmlutil. Check before increment to avoid overflow.
                        if (attributesInTag >= limits.maxAttributesPerElement || totalAttributes >= limits.maxTotalAttributes) {
                            return DiagnosticCode.ATTRIBUTE_LIMIT
                        }
                        attributesInTag++
                        totalAttributes++
                    }
                    !inTag && xml.startsWith("]]>", index) -> return DiagnosticCode.MALFORMED_XML
                }
            }
            index++
        }
    }
    return null
}

private fun isXml10CodePoint(codePoint: Int): Boolean =
    codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD ||
        codePoint in 0x20..0xD7FF || codePoint in 0xE000..0xFFFD || codePoint in 0x10000..0x10FFFF
