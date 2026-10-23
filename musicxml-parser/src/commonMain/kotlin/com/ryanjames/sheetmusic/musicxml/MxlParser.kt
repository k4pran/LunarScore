package com.ryanjames.sheetmusic.musicxml

import com.ryanjames.sheetmusic.score.Score

/**
 * Budgets for a compressed `.mxl` archive, checked against raw ZIP metadata before any entry is
 * expanded. [maxTotalExpandedBytes] sums declared sizes of every entry, including renditions that
 * are never read. [maxCompressionRatio] bounds each entry's expanded/compressed size. The selected
 * score is additionally bounded by [ParseLimits.maxInputBytes].
 */
public data class ArchiveLimits(
    val maxArchiveBytes: Int = 16_777_216,
    val maxEntries: Int = 1_000,
    val maxTotalExpandedBytes: Long = 67_108_864,
    val maxCompressionRatio: Int = 200,
    val maxContainerBytes: Int = 65_536,
) {
    init {
        require(maxArchiveBytes > 0 && maxEntries > 0 && maxTotalExpandedBytes > 0)
        require(maxCompressionRatio > 0 && maxContainerBytes > 0)
    }
}

/**
 * Imports a compressed MusicXML 4.0 `.mxl` archive. The whole ZIP directory is validated first
 * (see [ArchiveLimits]); unsafe names, duplicates, encryption, ZIP64 and other unsupported archives
 * are fatal under both policies. An optional `mimetype` entry must contain
 * `application/vnd.recordare.musicxml`; when it is not the first, uncompressed entry this is a
 * `RECOVERED_VALUE`. `META-INF/container.xml` is read with the same secure XML reader, and its first
 * `rootfile` must be MusicXML; later rootfiles (other renditions) are validated but not read. The
 * selected entry is passed to [parseMusicXml] unchanged, so the score equals that of the
 * uncompressed file. Archive locations use `sourceName!/entry`, or the entry name alone.
 */
public fun parseMxl(
    input: ByteArray,
    options: ParseOptions = ParseOptions(),
    archiveLimits: ArchiveLimits = ArchiveLimits(),
    sourceName: String? = null,
    isCancelled: () -> Boolean = { false },
): ParseResult<Score> {
    fun label(entryName: String?): String? = when {
        entryName == null -> sourceName
        sourceName == null -> entryName
        else -> "$sourceName!/$entryName"
    }
    val context = ParseContext(options)
    fun fail(code: DiagnosticCode, entryName: String?, message: String): ParseResult.Failure {
        context.report(code, SourceLocation(sourceName = label(entryName)), message)
        return context.failure()
    }
    if (isCancelled()) return fail(DiagnosticCode.CANCELLED, null, "Archive reading cancelled")
    try {
        val archive = ZipArchive.open(input, archiveLimits, isCancelled)
        val mimetype = archive.entry(MIMETYPE_PATH)
        if (mimetype != null) {
            val expected = MXL_MIMETYPE.encodeToByteArray()
            if (mimetype.size != expected.size || !archive.read(mimetype, expected.size).contentEquals(expected)) {
                return fail(DiagnosticCode.INVALID_STRUCTURE, MIMETYPE_PATH, "mimetype is not $MXL_MIMETYPE")
            }
            if (archive.entries.first() !== mimetype || mimetype.method != METHOD_STORED) {
                context.report(
                    DiagnosticCode.RECOVERED_VALUE,
                    SourceLocation(sourceName = label(MIMETYPE_PATH)),
                    "mimetype must be the first, uncompressed entry; its MusicXML type was accepted",
                )
            }
        }
        val archiveWarnings = when (val archiveResult = context.finish(Unit)) {
            is ParseResult.Failure -> return archiveResult
            is ParseResult.Success -> archiveResult.diagnostics
        }

        val container = archive.entry(CONTAINER_PATH)?.takeUnless { it.isDirectory }
            ?: return fail(DiagnosticCode.INVALID_STRUCTURE, CONTAINER_PATH, "Missing $CONTAINER_PATH")
        val containerResult = readScoreXml(
            archive.read(container, archiveLimits.maxContainerBytes),
            options,
            label(CONTAINER_PATH),
            isCancelled,
            setOf(ScoreXmlRoot.CONTAINER),
            ::readRootfiles,
        )
        val rootfilePath = when (containerResult) {
            is ParseResult.Failure -> return containerResult.withLeadingDiagnostics(archiveWarnings)
            is ParseResult.Success -> containerResult.value.first()
        }
        val rootfile = archive.entry(rootfilePath)?.takeUnless { it.isDirectory }
            ?: return fail(DiagnosticCode.INVALID_STRUCTURE, CONTAINER_PATH, "Rootfile entry is missing: $rootfilePath")
        val score = archive.read(rootfile, options.limits.maxInputBytes)
        return parseMusicXml(score, options, label(rootfilePath), isCancelled).withLeadingDiagnostics(archiveWarnings)
    } catch (exception: ArchiveException) {
        return fail(exception.code, exception.entryName, exception.message)
    }
}

/** Collects `container/rootfiles/rootfile/@full-path` in order; the reader starts on the container root. */
private fun readRootfiles(reader: ScoreXmlReader): List<String> {
    val rootLocation = checkNotNull(reader.current).location
    val rootfiles = mutableListOf<String>()
    var depth = 1
    var rootfilesCount = 0
    var inRootfiles = false
    while (depth > 0) {
        val event = reader.next() ?: break
        when (event.kind) {
            ScoreXmlEventKind.START_ELEMENT -> {
                depth++
                val name = event.name?.takeIf { it.namespaceUri.isEmpty() }?.localName
                if (depth == 2 && name == "rootfiles") {
                    inRootfiles = true
                    if (++rootfilesCount > 1) {
                        reader.context.report(DiagnosticCode.INVALID_STRUCTURE, event.location, "Container has more than one rootfiles element")
                    }
                } else if (depth == 3 && inRootfiles && name == "rootfile") {
                    readRootfile(reader, event, rootfiles)
                }
            }
            ScoreXmlEventKind.END_ELEMENT -> {
                if (depth == 2) inRootfiles = false
                depth--
            }
            ScoreXmlEventKind.TEXT -> Unit
        }
    }
    if (reader.context.canContinue && rootfiles.isEmpty()) {
        reader.context.report(DiagnosticCode.INVALID_STRUCTURE, rootLocation, "Container lists no rootfile")
    }
    return rootfiles
}

private fun readRootfile(reader: ScoreXmlReader, event: ScoreXmlEvent, rootfiles: MutableList<String>) {
    fun attribute(name: String): String? = event.attributes.firstOrNull { it.name == XmlName("", name) }?.value
    val fullPath = attribute("full-path")
    val unsafeReason = fullPath?.let(::unsafeArchivePath)
    val mediaType = attribute("media-type") ?: MUSICXML_MEDIA_TYPE
    val (code, message) = when {
        fullPath == null -> DiagnosticCode.INVALID_STRUCTURE to "rootfile requires full-path"
        unsafeReason != null -> DiagnosticCode.UNSAFE_ARCHIVE to "Unsafe rootfile path: $unsafeReason"
        rootfiles.any { it.lowercase() == fullPath.lowercase() } -> DiagnosticCode.UNSAFE_ARCHIVE to "Duplicate rootfile path"
        rootfiles.isEmpty() && mediaType != MUSICXML_MEDIA_TYPE ->
            DiagnosticCode.INVALID_STRUCTURE to "First rootfile must be $MUSICXML_MEDIA_TYPE"
        else -> {
            rootfiles += fullPath
            return
        }
    }
    reader.context.report(code, event.location, message)
}

/** Prepends archive-level warnings, keeping the diagnostic cap and its terminal marker. */
private fun <T : Any> ParseResult<T>.withLeadingDiagnostics(leading: List<ParseDiagnostic>): ParseResult<T> {
    if (leading.isEmpty()) return this
    val combined = leading + diagnostics
    val maxDiagnostics = options.limits.maxDiagnostics
    if (combined.size > maxDiagnostics) {
        val marker = ParseDiagnostic(
            DiagnosticCode.DIAGNOSTIC_LIMIT, DiagnosticSeverity.ERROR, combined[maxDiagnostics - 1].location,
            "Diagnostic limit exceeded; the last finding and subsequent findings are omitted",
        )
        return ParseResult.Failure(combined.take(maxDiagnostics - 1) + marker, options)
    }
    return when (this) {
        is ParseResult.Success -> ParseResult.Success(value, combined, provenance, options)
        is ParseResult.Failure -> ParseResult.Failure(combined, options)
    }
}

private const val MIMETYPE_PATH = "mimetype"
private const val CONTAINER_PATH = "META-INF/container.xml"
private const val MXL_MIMETYPE = "application/vnd.recordare.musicxml"
private const val MUSICXML_MEDIA_TYPE = "application/vnd.recordare.musicxml+xml"
