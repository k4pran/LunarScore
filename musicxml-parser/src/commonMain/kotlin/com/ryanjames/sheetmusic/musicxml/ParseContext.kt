package com.ryanjames.sheetmusic.musicxml

/** Shared policy/limit enforcement for future reader and semantic adapters. One context per parse. */
internal class ParseContext(val options: ParseOptions = ParseOptions()) {
    private val diagnostics = mutableListOf<ParseDiagnostic>()
    private val provenance = mutableListOf<SourceProvenance>()
    private var retainedBytes = 0
    private var failed = false
    private var stopped = false

    /** False means the adapter must stop consuming input. Errors can still be collected in order. */
    val canContinue: Boolean get() = !stopped

    fun report(code: DiagnosticCode, location: SourceLocation, message: String) {
        require(code != DiagnosticCode.UNSUPPORTED_FEATURE) { "Use unsupported() to record meaningful content loss" }
        record(code, location, message)
    }

    fun unsupported(content: UnsupportedContent, location: SourceLocation, message: String) {
        if (stopped) return
        val source = content.sourceXml
        if (source != null) {
            val remainingBytes = options.limits.maxRetainedSourceBytes - retainedBytes
            // UTF-8 has at least as many bytes as UTF-16 code units for well-formed source XML.
            // Check length first to bound the temporary encoding allocation by the retention budget.
            if (source.length > remainingBytes) {
                record(DiagnosticCode.RETENTION_LIMIT, location, "Opaque source retention byte limit exceeded")
                return
            }
            val byteCount = source.encodeToByteArray().size
            if (byteCount > remainingBytes) {
                record(DiagnosticCode.RETENTION_LIMIT, location, "Opaque source retention byte limit exceeded")
                return
            }
            retainedBytes += byteCount
        }
        record(DiagnosticCode.UNSUPPORTED_FEATURE, location, message, content)
    }

    fun addProvenance(entry: SourceProvenance) {
        if (stopped) return
        if (provenance.size >= options.limits.maxProvenanceEntries) {
            record(DiagnosticCode.PROVENANCE_LIMIT, entry.location, "Source provenance entry limit exceeded")
        } else {
            provenance += entry
        }
    }

    fun <T : Any> finish(value: T): ParseResult<T> = if (failed) {
        failure()
    } else {
        ParseResult.Success(value, diagnostics.toList(), provenance.toList(), options)
    }

    /** Allows syntax/security failure before a model value can be constructed. */
    fun failure(): ParseResult.Failure {
        check(failed) { "A failure must contain an error diagnostic" }
        return ParseResult.Failure(diagnostics.toList(), options)
    }

    private fun record(
        code: DiagnosticCode,
        location: SourceLocation,
        message: String,
        unsupported: UnsupportedContent? = null,
    ) {
        if (stopped) return
        if (diagnostics.size == options.limits.maxDiagnostics) {
            // On overflow, replace the last finding so the marker itself fits within the hard cap.
            diagnostics[diagnostics.lastIndex] = ParseDiagnostic(
                DiagnosticCode.DIAGNOSTIC_LIMIT, DiagnosticSeverity.ERROR, location,
                "Diagnostic limit exceeded; the last finding and subsequent findings are omitted",
            )
            failed = true
            stopped = true
            return
        }
        val recoverable = when (code) {
            DiagnosticCode.UNMATCHED_SPAN, DiagnosticCode.INVALID_CURSOR_MOVE,
            DiagnosticCode.INCONSISTENT_MEASURE_DURATION, DiagnosticCode.INCONSISTENT_CHORD_DURATION,
            DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticCode.RECOVERED_VALUE -> true
            else -> false
        }
        val severity = if (recoverable && options.policy == ParsePolicy.TOLERANT) {
            DiagnosticSeverity.WARNING
        } else {
            DiagnosticSeverity.ERROR
        }
        diagnostics += ParseDiagnostic(code, severity, location, message, unsupported)
        if (severity == DiagnosticSeverity.ERROR) failed = true
        // Fatal syntax/security/resource failures always stop, even in tolerant mode.
        if (!recoverable) stopped = true
    }
}
