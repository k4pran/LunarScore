package com.ryanjames.sheetmusic.musicxml

/** Strict rejects semantic defects, repairs and unsupported material; tolerant returns warnings. */
public enum class ParsePolicy { STRICT, TOLERANT }

/**
 * Per-parse resource budgets. XML byte limits precede decoding; depth/element limits precede
 * event snapshots. Attribute counts are bounded before reader creation. Diagnostic count includes
 * a terminal limit marker; zero retained bytes disables opaque retention. Provenance count is
 * independent of XML element count.
 */
public data class ParseLimits(
    val maxInputBytes: Int = 8_388_608,
    val maxDepth: Int = 128,
    val maxElements: Int = 100_000,
    val maxDiagnostics: Int = 1_000,
    val maxRetainedSourceBytes: Int = 65_536,
    val maxProvenanceEntries: Int = 100_000,
    /** Cumulative source path UTF-16 code units; checked before each full path is allocated. */
    val maxSourcePathCharacters: Int = 8_388_608,
    /** Attributes per element or XML declaration, including namespace declarations; checked in preflight. */
    val maxAttributesPerElement: Int = 256,
    /** Attributes across the whole input, including namespace and XML declaration attributes. */
    val maxTotalAttributes: Int = 100_000,
    /** Declared staves summed across parts; checked before a staff count is applied or expanded. */
    val maxStaves: Int = 1_024,
) {
    init {
        require(maxInputBytes > 0 && maxDepth > 0 && maxElements > 0 && maxDiagnostics > 0)
        require(maxRetainedSourceBytes >= 0 && maxProvenanceEntries >= 0)
        require(maxSourcePathCharacters > 0)
        require(maxAttributesPerElement > 0 && maxTotalAttributes > 0)
        require(maxStaves > 0)
    }
}

/** Default to strict interpretation so unsupported/lossy input requires an explicit choice. */
public data class ParseOptions(val policy: ParsePolicy = ParsePolicy.STRICT, val limits: ParseLimits = ParseLimits())

/**
 * A completed parse with ordered diagnostics. Success means accepted under [options], not complete
 * MusicXML coverage or XSD validation. Failure exposes no partial value or dangling provenance.
 * The generic value supports both score and archive/input adapters without XML types in the model.
 */
public sealed class ParseResult<out T> {
    /** Accepted value, absent on every failure. */
    public abstract val value: T?
    /** Findings in encounter order, including any terminal limit marker. */
    public abstract val diagnostics: List<ParseDiagnostic>
    /** Source mappings for the accepted value only. */
    public abstract val provenance: List<SourceProvenance>
    /** Policy and budgets used to interpret this result. */
    public abstract val options: ParseOptions

    /** Accepted value with zero or more warnings; no error diagnostics. */
    public class Success<out T : Any> internal constructor(
        override val value: T,
        override val diagnostics: List<ParseDiagnostic>,
        override val provenance: List<SourceProvenance>,
        override val options: ParseOptions,
    ) : ParseResult<T>()

    /** Rejected input; diagnostics always include an error. */
    public class Failure internal constructor(
        override val diagnostics: List<ParseDiagnostic>,
        override val options: ParseOptions,
    ) : ParseResult<Nothing>() {
        override val value: Nothing? = null
        override val provenance: List<SourceProvenance> = emptyList()
    }
}
