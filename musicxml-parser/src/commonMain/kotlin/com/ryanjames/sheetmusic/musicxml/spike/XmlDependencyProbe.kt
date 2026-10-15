package com.ryanjames.sheetmusic.musicxml.spike

import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.xmlStreaming

// MX-01 evidence only. These types are internal until MX-03/MX-05 define the importer contract.
internal enum class ProbeCode {
    INPUT_LIMIT,
    DEPTH_LIMIT,
    ELEMENT_LIMIT,
    UNSAFE_XML,
    UNSUPPORTED_ENCODING,
    UNSUPPORTED_ROOT,
    UNSUPPORTED_NAMESPACE,
    MALFORMED_XML,
    ARCHIVE_READ_FAILED,
    ARCHIVE_ENTRY_LIMIT,
}

internal data class ProbeDiagnostic(val code: ProbeCode, val message: String)

internal data class XmlProbeLimits(
    val maxBytes: Int = 1_048_576,
    val maxDepth: Int = 64,
    val maxElements: Int = 10_000,
) {
    init {
        require(maxBytes > 0 && maxDepth > 0 && maxElements > 0)
    }
}

internal data class ExpandedName(val namespace: String, val localName: String)

internal data class ProbeAttribute(val name: ExpandedName, val value: String)

internal data class ProbeEvent(
    val kind: EventType,
    val name: ExpandedName? = null,
    val text: String? = null,
    val attributes: List<ProbeAttribute> = emptyList(),
)

internal data class XmlProbeResult(
    val root: String? = null,
    val events: List<ProbeEvent> = emptyList(),
    val diagnostic: ProbeDiagnostic? = null,
)

/** Exercises the generic pull reader on bounded UTF-8 bytes; does not import score semantics. */
internal fun probeXml(input: ByteArray, limits: XmlProbeLimits = XmlProbeLimits()): XmlProbeResult {
    fun failure(code: ProbeCode, message: String): XmlProbeResult =
        XmlProbeResult(diagnostic = ProbeDiagnostic(code, message))

    if (input.size > limits.maxBytes) return failure(ProbeCode.INPUT_LIMIT, "XML byte limit exceeded")
    val xml = try {
        input.decodeToString(throwOnInvalidSequence = true).removePrefix("\uFEFF")
    } catch (_: CharacterCodingException) {
        return failure(ProbeCode.UNSUPPORTED_ENCODING, "The spike accepts UTF-8 only")
    }
    // Conservative preflight: do not let even a DTD token reach the dependency. This deliberately
    // also rejects these literal tokens in comments/CDATA; MX-05 will define the production policy.
    if ("<!DOCTYPE" in xml || "<!ENTITY" in xml) {
        return failure(ProbeCode.UNSAFE_XML, "DTD and entity declarations are disabled")
    }
    if ('\u0000' in xml) return failure(ProbeCode.UNSUPPORTED_ENCODING, "The spike accepts UTF-8 only")
    val encoding = Regex("""^<\?xml\s[^?]*encoding\s*=\s*['"]([^'"]+)['"]""")
        .find(xml)?.groupValues?.get(1)
    if (encoding != null && !encoding.equals("UTF-8", ignoreCase = true)) {
        return failure(ProbeCode.UNSUPPORTED_ENCODING, "The spike accepts UTF-8 only")
    }

    val events = mutableListOf<ProbeEvent>()
    var root: String? = null
    var elements = 0
    val reader = xmlStreaming.newGenericReader(xml, expandEntities = true)
    try {
        while (reader.hasNext()) {
            when (val kind = reader.next()) {
                EventType.START_ELEMENT -> {
                    if (reader.depth > limits.maxDepth) {
                        return failure(ProbeCode.DEPTH_LIMIT, "XML depth limit exceeded")
                    }
                    if (++elements > limits.maxElements) {
                        return failure(ProbeCode.ELEMENT_LIMIT, "XML element limit exceeded")
                    }
                    if (root == null) {
                        if (reader.localName !in setOf("score-partwise", "score-timewise")) {
                            return failure(ProbeCode.UNSUPPORTED_ROOT, "Unsupported score root: ${reader.localName}")
                        }
                        if (reader.namespaceURI.isNotEmpty()) {
                            return failure(ProbeCode.UNSUPPORTED_NAMESPACE, "MusicXML 4.0 score elements have no namespace")
                        }
                        root = reader.localName
                    }
                    events += ProbeEvent(
                        kind,
                        ExpandedName(reader.namespaceURI, reader.localName),
                        attributes = (0 until reader.attributeCount).map { index ->
                            ProbeAttribute(
                                ExpandedName(reader.getAttributeNamespace(index), reader.getAttributeLocalName(index)),
                                reader.getAttributeValue(index),
                            )
                        },
                    )
                }
                EventType.END_ELEMENT -> events += ProbeEvent(kind, ExpandedName(reader.namespaceURI, reader.localName))
                EventType.TEXT, EventType.CDSECT -> events += ProbeEvent(kind, text = reader.text)
                EventType.DOCDECL, EventType.ENTITY_REF -> return failure(ProbeCode.UNSAFE_XML, "Unsafe XML event")
                else -> Unit
            }
        }
    } catch (_: XmlException) {
        return failure(ProbeCode.MALFORMED_XML, "Malformed XML or unresolved entity")
    } finally {
        reader.close()
    }
    return if (root == null) failure(ProbeCode.MALFORMED_XML, "Missing score root") else XmlProbeResult(root, events)
}
