package com.ryanjames.sheetmusic.musicxml

/** Stable machine-readable codes. Messages are explanatory and are not part of the contract. */
public enum class DiagnosticCode {
    /** XML syntax or an unresolved entity is invalid. */
    MALFORMED_XML,
    /** DTD/entity processing or another unsafe XML construct was rejected. */
    UNSAFE_XML,
    /** Input encoding cannot be decoded safely. */
    UNSUPPORTED_ENCODING,
    /** Only XML 1.0 syntax is supported by the secure input boundary. */
    UNSUPPORTED_XML_VERSION,
    /** The document kind is outside the score input API. */
    UNSUPPORTED_ROOT,
    /** A score namespace is not supported. */
    UNSUPPORTED_NAMESPACE,
    /** Required structure, identity or references are invalid. */
    INVALID_STRUCTURE,
    /** A required source value is invalid, including zero denominators. */
    INVALID_VALUE,
    /** An exact numeric result exceeds the neutral model's bounds. */
    NUMERIC_OVERFLOW,
    /** A span endpoint could not be resolved. */
    UNMATCHED_SPAN,
    /** A backup/forward operation cannot be interpreted consistently. */
    INVALID_CURSOR_MOVE,
    /** Observed measure timing conflicts with the declared musical structure. */
    INCONSISTENT_MEASURE_DURATION,
    /** A chord tone lasts longer than the reference note that advances the cursor. */
    INCONSISTENT_CHORD_DURATION,
    /** Meaningful content could not be mapped fully; carries [UnsupportedContent]. */
    UNSUPPORTED_FEATURE,
    /** A recoverable source defect was repaired; the message describes the change. */
    RECOVERED_VALUE,
    /** Input byte budget exceeded. */
    INPUT_LIMIT,
    /** XML nesting budget exceeded. */
    DEPTH_LIMIT,
    /** XML element budget exceeded. */
    ELEMENT_LIMIT,
    /** Cumulative source path character budget exceeded. */
    PATH_LIMIT,
    /** Per-element or cumulative XML attribute budget exceeded, including namespace declarations. */
    ATTRIBUTE_LIMIT,
    /** More findings exist than can be returned; parsing must stop. */
    DIAGNOSTIC_LIMIT,
    /** Opaque source retention byte budget exceeded. */
    RETENTION_LIMIT,
    /** Model-to-source mapping budget exceeded. */
    PROVENANCE_LIMIT,
    /** Declared staff budget exceeded. */
    STAFF_LIMIT,
    /** The caller cancelled parsing. */
    CANCELLED,
    /** An archive could not be read safely. */
    ARCHIVE_READ_FAILED,
    /** An archive entry exceeded its allowed expansion budget. */
    ARCHIVE_ENTRY_LIMIT,
    /** Archive size, entry count, total expansion or compression ratio budget exceeded. */
    ARCHIVE_LIMIT,
    /** An archive entry name is unsafe or ambiguous, or archive records overlap or disagree. */
    UNSAFE_ARCHIVE,
    /** Encryption, ZIP64, multi-disk archives or a compression method other than STORE/DEFLATE. */
    UNSUPPORTED_ARCHIVE,
}

/** Errors prohibit a score; warnings accompany an explicitly tolerated result. */
public enum class DiagnosticSeverity { WARNING, ERROR }

/**
 * Source coordinates, never inferred from neutral model order. Line/column are one-based when
 * available; null means unknown. [path] is an absolute descriptive element/attribute path with
 * sibling indices where needed, not an executable XPath. [sourceName] may identify an archive
 * entry. [measureIndex] is zero-based within its part, disambiguating repeated display numbers.
 * A pre-reader failure may have only a source name or no coordinates at all.
 */
public data class SourceLocation(
    val sourceName: String? = null,
    val path: String? = null,
    val line: Int? = null,
    val column: Int? = null,
    val partId: String? = null,
    val measureNumber: String? = null,
    val measureIndex: Int? = null,
) {
    init {
        require(path == null || path.startsWith('/'))
        require(line == null || line > 0)
        require(column == null || column > 0)
        require(measureIndex == null || measureIndex >= 0)
    }
}

/** Outcome for meaningful unmapped source content. Opaque retention does not imply understanding. */
public enum class ContentRetention { DISCARDED, OPAQUE, CONVERTED }

/**
 * Explicit loss/preservation record. [featureId] names a coverage group (S01–S27), or a descriptive
 * extension identifier when no group applies. Opaque [sourceXml] is a bounded source fragment,
 * not a lossless byte copy of the document. [conversion] describes what changed or was lost.
 */
public data class UnsupportedContent(
    val featureId: String,
    val retention: ContentRetention,
    val sourceXml: String? = null,
    val conversion: String? = null,
) {
    init {
        require(featureId.isNotBlank())
        require(if (retention == ContentRetention.OPAQUE) !sourceXml.isNullOrBlank() else sourceXml == null)
        require(if (retention == ContentRetention.CONVERTED) !conversion.isNullOrBlank() else conversion == null)
    }
}

/** Ordered finding. Unsupported content is mandatory exactly for [DiagnosticCode.UNSUPPORTED_FEATURE]. */
public data class ParseDiagnostic(
    val code: DiagnosticCode,
    val severity: DiagnosticSeverity,
    val location: SourceLocation,
    val message: String,
    val unsupported: UnsupportedContent? = null,
) {
    init {
        require(message.isNotBlank())
        require((code == DiagnosticCode.UNSUPPORTED_FEATURE) == (unsupported != null))
    }
}

/** Parser-owned sidecar mapping; [modelPath] uses neutral paths such as parts[0].measures[0].events[0]. */
public data class SourceProvenance(val modelPath: String, val location: SourceLocation) {
    init {
        require(modelPath.isNotBlank())
    }
}
