package com.ryanjames.sheetmusic.musicxml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Original reader fixtures; F22 schema/semantic defects are deliberately separate from XML syntax. */
class ScoreXmlReaderTest {
    @Test
    fun testBothPermittedRoots() {
        for ((name, kind) in listOf("score-partwise" to ScoreXmlRoot.PARTWISE, "score-timewise" to ScoreXmlRoot.TIMEWISE)) {
            val result = readScoreXml("\uFEFF<?xml version='1.0' encoding='utf-8'?><$name version='4.0'/>".encodeToByteArray()) {
                assertEquals(ScoreXmlEventKind.START_ELEMENT, it.current?.kind)
                assertEquals(XmlName("", name), it.current?.name)
                assertEquals(kind, it.root)
                kind
            }
            assertEquals(kind, assertIs<ParseResult.Success<ScoreXmlRoot>>(result).value)
            assertTrue(result.diagnostics.isEmpty())
        }
    }

    @Test
    fun testOrderedMixedContentEmptyElementsAndEscapes() {
        val result = events("<score-partwise><words>A<![CDATA[B]]><!-- ignored -->C<elision/>D &amp; &#x1D11E;</words><backup/><note/></score-partwise>")
        val events = assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(result).value
        assertEquals(listOf("+score-partwise", "+words", "ABC", "+elision", "-elision", "D & 𝄞", "-words", "+backup", "-backup", "+note", "-note", "-score-partwise"),
            compact(events))
    }

    @Test
    fun testExpandedNamesAndUnqualifiedAttributes() {
        val result = events("""<score-partwise xmlns:e="urn:extension" xmlns:f="urn:extension"><e:note e:id="x" id="y" xml:lang="en"/><f:note/><note xmlns="urn:other"/><note/></score-partwise>""")
        val starts = assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(result).value.filter { it.kind == ScoreXmlEventKind.START_ELEMENT }
        assertEquals(XmlName("urn:extension", "note"), starts[1].name)
        assertEquals(listOf(
            ScoreXmlAttribute(XmlName("urn:extension", "id"), "x"), ScoreXmlAttribute(XmlName("", "id"), "y"),
            ScoreXmlAttribute(XmlName("http://www.w3.org/XML/1998/namespace", "lang"), "en"),
        ), starts[1].attributes)
        assertEquals("/score-partwise[1]/{urn:extension}note[2]", starts[2].location.path)
        assertEquals(XmlName("urn:other", "note"), starts[3].name)
        assertEquals(XmlName("", "note"), starts[4].name)
    }

    @Test
    fun testRootAndNamespaceFailuresAreFatalInBothPolicies() {
        for (policy in ParsePolicy.entries) {
            for (root in listOf("opus", "sounds", "unknown")) {
                assertFailure(DiagnosticCode.UNSUPPORTED_ROOT, "<$root/>", ParseOptions(policy))
            }
            assertFailure(DiagnosticCode.UNSUPPORTED_NAMESPACE, "<score-partwise xmlns='urn:foreign'/>", ParseOptions(policy))
            assertFailure(DiagnosticCode.UNSUPPORTED_NAMESPACE, "<m:score-timewise xmlns:m='urn:foreign'/>", ParseOptions(policy))
        }
    }

    @Test
    fun testDtdEntitiesAndExpansionRejectedBeforeConsumer() {
        val declarations = listOf(
            "<!DOCTYPE score-partwise>",
            "<!DOCTYPE score-partwise PUBLIC '-//Recordare//DTD MusicXML 4.0 Partwise//EN' 'http://www.musicxml.org/dtds/partwise.dtd'>",
            "<!DOCTYPE score-partwise SYSTEM 'https://example.invalid/must-not-fetch.dtd'>",
            "<!DOCTYPE score-partwise [<!ENTITY secret SYSTEM 'file:///must-not-read'>]>",
            "<!DOCTYPE score-partwise [<!ENTITY % remote SYSTEM 'https://example.invalid/entity'>%remote;]>",
            "<!DOCTYPE score-partwise [<!ENTITY a 'abc'><!ENTITY b '&a;&a;&a;'><!ENTITY c '&b;&b;&b;'>]>",
            "<!ENTITY x 'standalone declaration'>",
        )
        for (policy in ParsePolicy.entries) {
            for (declaration in declarations) {
                var called = false
                val result = readScoreXml("$declaration<score-partwise/>".encodeToByteArray(), ParseOptions(policy)) { called = true; Unit }
                assertEquals(DiagnosticCode.UNSAFE_XML, assertIs<ParseResult.Failure>(result).diagnostics.single().code)
                assertTrue(!called)
            }
        }
        assertFailure(DiagnosticCode.UNSAFE_XML, "<score-partwise><!-- <!DOCTYPE literal> --></score-partwise>")
        assertFailure(DiagnosticCode.UNSAFE_XML, "<score-partwise><![CDATA[<!ENTITY literal>]]></score-partwise>")
    }

    @Test
    fun testMalformedF22SyntaxAndUnresolvedReferences() {
        for (xml in listOf(
            "", "   ", "<!-- no root -->", "<score-partwise>", "<score-partwise><note></score-partwise>",
            "<score-partwise/><score-timewise/>", "text<score-partwise/>", "<score-partwise/>tail",
            "<score-partwise>&unknown;</score-partwise>", "<score-partwise value='&unknown;'/>",
            "<score-partwise><x:note/></score-partwise>", "<score-partwise a='1' a='2'/>",
            "<score-partwise xmlns:a='urn:x' xmlns:b='urn:x' a:id='1' b:id='2'/>",
            "<score-partwise xmlns:a='urn:x' xmlns:a='urn:y'/>",
            "<score-partwise xmlns:xml='urn:wrong'/>",
            "<score-partwise xmlns:xmlns='urn:wrong'/>",
            "<score-partwise x:id='1'/>",
            "<score-partwise a='1'b='2'/>",
            "<score-partwise a='<'/>",
            "<score-partwise>]]></score-partwise>",
            "<score-partwise><!-- invalid -- comment --></score-partwise>",
            "<score-partwise><![CDATA[unfinished</score-partwise>", "<score-partwise><!-- unfinished</score-partwise>",
        )) {
            for (policy in ParsePolicy.entries) assertFailure(DiagnosticCode.MALFORMED_XML, xml, ParseOptions(policy))
        }
    }

    @Test
    fun testInvalidXmlCharactersAndNumericReferences() {
        for (text in listOf("\u0001", "\uFFFF", "&#0;", "&#xD800;", "&#x110000;", "&#999999999999999999999;", "&#x;")) {
            assertFailure(DiagnosticCode.MALFORMED_XML, "<score-partwise>$text</score-partwise>")
            assertFailure(DiagnosticCode.MALFORMED_XML, "<score-partwise value='$text'/>")
        }
    }

    @Test
    fun testEncodingRejectionsAndValidMultibyteInput() {
        for (encoding in listOf("UTF-16", "UTF-32", "ISO-8859-1")) {
            assertFailure(DiagnosticCode.UNSUPPORTED_ENCODING, "<?xml version='1.0' encoding='$encoding'?><score-partwise/>")
        }
        for (bytes in listOf(byteArrayOf(0xC3.toByte()), byteArrayOf(0xFF.toByte(), 0xFE.toByte()), byteArrayOf(0))) {
            val result = readScoreXml(bytes) { Unit }
            assertEquals(DiagnosticCode.UNSUPPORTED_ENCODING, assertIs<ParseResult.Failure>(result).diagnostics.single().code)
        }
        assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(events("<score-partwise title='é𝄞'>&lt;&gt;&amp;&apos;&quot;&#65;</score-partwise>"))
    }

    @Test
    fun testUnsupportedXmlVersionsFailUnderBothPolicies() {
        for (policy in ParsePolicy.entries) {
            for (version in listOf("1.1", "9.9")) {
                assertFailure(DiagnosticCode.UNSUPPORTED_XML_VERSION, "<?xml version='$version'?><score-partwise/>", ParseOptions(policy))
            }
        }
    }

    @Test
    fun testReferencesInsideLiteralContextsAreNotExpanded() {
        val result = events("<?test &#xD800;?><score-partwise><!-- &#0; --><![CDATA[&#999999999999999; &unknown;]]></score-partwise>")
        val text = assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(result).value.mapNotNull { it.text }.joinToString("")
        assertEquals("&#999999999999999; &unknown;", text)
    }

    @Test
    fun testExactAndExceededByteDepthAndElementBudgets() {
        val xml = "<score-partwise><note>é</note><rest/></score-partwise>"
        val byteCount = xml.encodeToByteArray().size
        assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(events(xml, ParseOptions(limits = ParseLimits(maxInputBytes = byteCount, maxDepth = 2, maxElements = 3))))
        assertFailure(DiagnosticCode.INPUT_LIMIT, xml, ParseOptions(limits = ParseLimits(maxInputBytes = byteCount - 1)))
        val depth = assertFailure(DiagnosticCode.DEPTH_LIMIT, xml, ParseOptions(limits = ParseLimits(maxDepth = 1)))
        assertEquals("/score-partwise[1]/note[1]", depth.diagnostics.single().location.path)
        val count = assertFailure(DiagnosticCode.ELEMENT_LIMIT, xml, ParseOptions(limits = ParseLimits(maxElements = 2)))
        assertEquals("/score-partwise[1]/rest[1]", count.diagnostics.single().location.path)
    }

    @Test
    fun testDeepAndWideInputsStopAtLimits() {
        val deep = "<score-partwise>" + "<a>".repeat(1000) + "</a>".repeat(1000) + "</score-partwise>"
        assertFailure(DiagnosticCode.DEPTH_LIMIT, deep, ParseOptions(limits = ParseLimits(maxDepth = 8)))
        val wide = "<score-partwise>" + "<note/>".repeat(1000) + "</score-partwise>"
        assertFailure(DiagnosticCode.ELEMENT_LIMIT, wide, ParseOptions(limits = ParseLimits(maxElements = 8)))
    }

    @Test
    fun testCumulativePathBudgetExactBoundaryAndOverflow() {
        val xml = "<score-partwise><a>text</a>" + "<a/>".repeat(9) + "</score-partwise>"
        val rootPath = "/score-partwise[1]"
        val exactBudget = rootPath.length + (1..10).sumOf { "$rootPath/a[$it]".length }
        for (policy in ParsePolicy.entries) {
            assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(
                events(xml, ParseOptions(policy, ParseLimits(maxSourcePathCharacters = exactBudget))),
            )
            val failure = assertFailure(
                DiagnosticCode.PATH_LIMIT, xml,
                ParseOptions(policy, ParseLimits(maxSourcePathCharacters = exactBudget - 1)),
            )
            assertEquals(rootPath, failure.diagnostics.single().location.path)
            val rootFailure = assertFailure(
                DiagnosticCode.PATH_LIMIT, "<score-partwise/>",
                ParseOptions(policy, ParseLimits(maxSourcePathCharacters = rootPath.length - 1)),
            )
            assertNull(rootFailure.diagnostics.single().location.path)
        }
    }

    @Test
    fun testPathBudgetCountsExpandedNamesAndUtf16Characters() {
        val xml = "<score-partwise xmlns:e='urn:𝄞'><e:é/></score-partwise>"
        val rootPath = "/score-partwise[1]"
        val childPath = "$rootPath/{urn:𝄞}é[1]"
        val exactBudget = rootPath.length + childPath.length
        val result = assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(
            events(xml, ParseOptions(limits = ParseLimits(maxSourcePathCharacters = exactBudget))),
        )
        assertEquals(childPath, result.value.first { it.name?.localName == "é" }.location.path)
        assertFailure(
            DiagnosticCode.PATH_LIMIT, xml,
            ParseOptions(limits = ParseLimits(maxSourcePathCharacters = exactBudget - 1)),
        )
    }

    @Test
    fun testDefaultPathBudgetRejectsDeepLongNamesAndNamespaces() {
        val name = "n".repeat(4096)
        val namespace = "urn:" + "n".repeat(8192)
        val inputs = listOf(
            "<score-partwise>" + "<$name>".repeat(100) + "</$name>".repeat(100) + "</score-partwise>",
            "<score-partwise xmlns:e='$namespace'>" + "<e:n>".repeat(100) + "</e:n>".repeat(100) + "</score-partwise>",
        )
        for (policy in ParsePolicy.entries) {
            for (xml in inputs) {
                val input = xml.encodeToByteArray()
                assertTrue(input.size < ParseLimits().maxInputBytes)
                var consumedRoot = false
                val result = readScoreXml(input, ParseOptions(policy)) {
                    consumedRoot = true
                    it.context.addProvenance(SourceProvenance("parts[0]", assertNotNull(it.current).location))
                    "discarded on failure"
                }
                assertTrue(consumedRoot)
                assertIs<ParseResult.Failure>(result)
                assertEquals(DiagnosticCode.PATH_LIMIT, result.diagnostics.single().code)
                assertEquals(DiagnosticSeverity.ERROR, result.diagnostics.single().severity)
                assertNull(result.value)
                assertTrue(result.provenance.isEmpty())
            }
        }
    }

    @Test
    fun testPathLimitDuringDrainRespectsDiagnosticCap() {
        val result = readScoreXml(
            "<score-partwise><note/></score-partwise>".encodeToByteArray(),
            ParseOptions(ParsePolicy.TOLERANT, ParseLimits(
                maxDiagnostics = 1, maxSourcePathCharacters = "/score-partwise[1]".length,
            )),
        ) {
            it.context.unsupported(
                UnsupportedContent("extension:test", ContentRetention.DISCARDED),
                assertNotNull(it.current).location, "Test warning",
            )
            Unit
        }
        assertEquals(DiagnosticCode.DIAGNOSTIC_LIMIT, assertIs<ParseResult.Failure>(result).diagnostics.single().code)
    }

    @Test
    fun testSourcePathsPositionsAndRepeatedMeasureNumbers() {
        val xml = "<score-partwise>\n  <part id='P1'>\n    <measure number='1'><note/><note/></measure>\n    <measure number='1'><rest/></measure>\n  </part>\n</score-partwise>"
        val result = assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(events(xml))
        val note = result.value.first { it.name?.localName == "note" }
        assertEquals("score.musicxml", note.location.sourceName)
        assertEquals("/score-partwise[1]/part[1]/measure[1]/note[1]", note.location.path)
        assertEquals("P1", note.location.partId)
        assertEquals("1", note.location.measureNumber)
        assertEquals(0, note.location.measureIndex)
        assertEquals(3, note.location.line)
        assertEquals(25, note.location.column)
        val rest = result.value.first { it.name?.localName == "rest" }
        assertEquals(1, rest.location.measureIndex)
        assertEquals("/score-partwise[1]/part[1]/measure[2]/rest[1]", rest.location.path)
        val noteEnd = result.value.first { it.name?.localName == "note" && it.kind == ScoreXmlEventKind.END_ELEMENT }
        assertEquals(note.location.path, noteEnd.location.path)
    }

    @Test
    fun testTimewiseMusicalContext() {
        val xml = "<score-timewise><measure number='A'><part id='P1'><note/></part><part id='P2'><rest/></part></measure><measure number='A'><part id='P1'><note/></part></measure></score-timewise>"
        val starts = assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(events(xml)).value.filter { it.kind == ScoreXmlEventKind.START_ELEMENT }
        val rest = starts.first { it.name?.localName == "rest" }
        assertEquals("P2", rest.location.partId)
        assertEquals("A", rest.location.measureNumber)
        assertEquals(0, rest.location.measureIndex)
        assertEquals(1, starts.last().location.measureIndex)
        assertEquals("P1", starts.last().location.partId)
    }

    @Test
    fun testMalformedTailDiscardsValueAndProvenanceAfterWarning() {
        val result = readScoreXml("<score-partwise><note/></score-partwise>bad".encodeToByteArray(), ParseOptions(ParsePolicy.TOLERANT)) {
            val location = assertNotNull(it.current).location
            it.context.unsupported(UnsupportedContent("extension:test", ContentRetention.DISCARDED), location, "Test warning")
            it.context.addProvenance(SourceProvenance("parts[0]", location))
            "must not escape"
        }
        assertIs<ParseResult.Failure>(result)
        assertNull(result.value)
        assertTrue(result.provenance.isEmpty())
        assertEquals(listOf(DiagnosticCode.UNSUPPORTED_FEATURE, DiagnosticCode.MALFORMED_XML), result.diagnostics.map { it.code })
        assertEquals(listOf(DiagnosticSeverity.WARNING, DiagnosticSeverity.ERROR), result.diagnostics.map { it.severity })
    }

    @Test
    fun testDrainingAlsoHonorsResourceBudgetsAndDiagnosticCap() {
        val result = readScoreXml("<score-partwise><note/></score-partwise>".encodeToByteArray(),
            ParseOptions(ParsePolicy.TOLERANT, ParseLimits(maxElements = 1, maxDiagnostics = 1))) {
            it.context.unsupported(UnsupportedContent("extension:test", ContentRetention.DISCARDED), assertNotNull(it.current).location, "Test warning")
            Unit
        }
        assertEquals(DiagnosticCode.DIAGNOSTIC_LIMIT, assertIs<ParseResult.Failure>(result).diagnostics.single().code)
    }

    @Test
    fun testCancellationBeforeInputAndDuringDrain() {
        var called = false
        val before = readScoreXml(byteArrayOf(), isCancelled = { true }) { called = true; Unit }
        assertTrue(!called)
        assertEquals(DiagnosticCode.CANCELLED, assertIs<ParseResult.Failure>(before).diagnostics.single().code)
        var cancel = false
        val during = readScoreXml("<score-partwise><note/></score-partwise>".encodeToByteArray(), isCancelled = { cancel }) {
            cancel = true
            Unit
        }
        assertEquals(DiagnosticCode.CANCELLED, assertIs<ParseResult.Failure>(during).diagnostics.single().code)
    }

    @Test
    fun testCancellationDuringPreflightIsLatched() {
        var polls = 0
        val result = readScoreXml("<score-partwise/>".encodeToByteArray(), isCancelled = { ++polls == 3 }) { Unit }
        assertEquals(DiagnosticCode.CANCELLED, assertIs<ParseResult.Failure>(result).diagnostics.single().code)
    }

    @Test
    fun testConsumerExceptionsAreNotReportedAsXmlSyntaxErrors() {
        assertFailsWith<IllegalArgumentException> {
            readScoreXml("<score-partwise/>".encodeToByteArray()) { throw IllegalArgumentException("consumer bug") }
        }
    }

    @Test
    fun testF22SchemaAndSemanticDefectsRemainForLaterAdapters() {
        // Missing part-list and illegal duration are well-formed XML. This layer makes no XSD claim.
        assertIs<ParseResult.Success<List<ScoreXmlEvent>>>(events("<score-partwise><part id='P1'><measure number='1'><note><duration>illegal</duration></note></measure></part></score-partwise>"))
    }

    private fun events(xml: String, options: ParseOptions = ParseOptions()): ParseResult<List<ScoreXmlEvent>> =
        readScoreXml(xml.encodeToByteArray(), options, sourceName = "score.musicxml") { cursor ->
            buildList {
                add(assertNotNull(cursor.current))
                while (true) add(cursor.next() ?: break)
                assertNull(cursor.next()) // EOF/failure is stable.
            }
        }

    private fun assertFailure(code: DiagnosticCode, xml: String, options: ParseOptions = ParseOptions()): ParseResult.Failure {
        val result = assertIs<ParseResult.Failure>(events(xml, options), xml)
        assertEquals(code, result.diagnostics.single().code, xml)
        assertEquals(DiagnosticSeverity.ERROR, result.diagnostics.single().severity)
        assertNull(result.value)
        assertTrue(result.provenance.isEmpty())
        assertEquals("score.musicxml", result.diagnostics.single().location.sourceName)
        return result
    }

    private fun compact(events: List<ScoreXmlEvent>): List<String> = buildList {
        var previousWasText = false
        for (event in events) {
            when (event.kind) {
                ScoreXmlEventKind.START_ELEMENT -> add("+${event.name?.localName}")
                ScoreXmlEventKind.END_ELEMENT -> add("-${event.name?.localName}")
                ScoreXmlEventKind.TEXT -> {
                    if (previousWasText) {
                        this[lastIndex] = last() + event.text
                    } else add(assertNotNull(event.text))
                }
            }
            previousWasText = event.kind == ScoreXmlEventKind.TEXT
        }
    }
}
