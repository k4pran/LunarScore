package com.ryanjames.sheetmusic.musicxml.spike

import nl.adaptivity.xmlutil.EventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XmlDependencyProbeTest {
    @Test
    fun testBothScoreRoots() {
        for (root in listOf("score-partwise", "score-timewise")) {
            val result = probeXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><$root version=\"4.0\"/>".encodeToByteArray())
            assertNull(result.diagnostic)
            assertEquals(root, result.root)
        }
    }

    @Test
    fun testPullOrderIncludesTextAndEmptyElements() {
        val result = probeXml(
            "<score-partwise><measure><note>A</note><backup/><note>B</note></measure></score-partwise>".encodeToByteArray(),
        )
        assertNull(result.diagnostic)
        assertEquals(
            listOf("+score-partwise", "+measure", "+note", "A", "-note", "+backup", "-backup", "+note", "B", "-note", "-measure", "-score-partwise"),
            result.events.map { event ->
                when (event.kind) {
                    EventType.START_ELEMENT -> "+${event.name?.localName}"
                    EventType.END_ELEMENT -> "-${event.name?.localName}"
                    else -> event.text
                }
            },
        )
    }

    @Test
    fun testExpandedNamesAndAttributeNamespaces() {
        val result = probeXml(
            """<score-partwise xmlns:e="urn:extension"><e:note e:id="x" id="y"/><note xmlns="urn:other"/></score-partwise>""".encodeToByteArray(),
        )
        assertNull(result.diagnostic)
        val starts = result.events.filter { it.kind == EventType.START_ELEMENT }
        assertEquals(ExpandedName("urn:extension", "note"), starts[1].name)
        assertEquals(
            listOf(ProbeAttribute(ExpandedName("urn:extension", "id"), "x"), ProbeAttribute(ExpandedName("", "id"), "y")),
            starts[1].attributes,
        )
        assertEquals(ExpandedName("urn:other", "note"), starts[2].name)
    }

    @Test
    fun testUnsupportedRootsAndNamespacesHaveDiagnostics() {
        assertCode(ProbeCode.UNSUPPORTED_ROOT, "<opus/>")
        assertCode(ProbeCode.UNSUPPORTED_ROOT, "<sounds/>")
        assertCode(ProbeCode.UNSUPPORTED_NAMESPACE, "<score-partwise xmlns=\"urn:foreign\"/>")
        assertCode(ProbeCode.UNSUPPORTED_NAMESPACE, "<m:score-timewise xmlns:m=\"urn:foreign\"/>")
    }

    @Test
    fun testExternalAndInternalEntityDeclarationsRejected() {
        for (declaration in listOf(
            "<!DOCTYPE score-partwise>",
            "<!DOCTYPE score-partwise SYSTEM 'https://example.invalid/score.dtd'>",
            "<!DOCTYPE score-partwise [<!ENTITY x SYSTEM 'file:///must-not-read'>]>",
            "<!DOCTYPE score-partwise [<!ENTITY % x SYSTEM 'https://example.invalid/entity'>%x;]>",
            "<!DOCTYPE score-partwise [<!ENTITY a 'abc'><!ENTITY x '&a;&a;&a;'>]>",
        )) {
            assertCode(ProbeCode.UNSAFE_XML, "$declaration<score-partwise>&x;</score-partwise>")
        }
        assertCode(ProbeCode.MALFORMED_XML, "<score-partwise>&undeclared;</score-partwise>")
        assertCode(ProbeCode.MALFORMED_XML, "<score-partwise value='&undeclared;'/>")
    }

    @Test
    fun testPredefinedAndCharacterReferencesRemainUsable() {
        val result = probeXml("<score-partwise title='A &amp; B'>&lt;&gt;&amp;&apos;&quot;&#65;&#x1D11E;</score-partwise>".encodeToByteArray())
        assertNull(result.diagnostic)
        assertEquals("A & B", result.events.first().attributes.single().value)
        assertEquals("<>&'\"A\uD834\uDD1E", result.events.mapNotNull { it.text }.joinToString(""))
    }

    @Test
    fun testInputDepthAndElementLimits() {
        val input = "<score-partwise>é</score-partwise>".encodeToByteArray()
        assertNull(probeXml(input, XmlProbeLimits(maxBytes = input.size)).diagnostic)
        assertEquals(ProbeCode.INPUT_LIMIT, probeXml(input, XmlProbeLimits(maxBytes = input.size - 1)).diagnostic?.code)
        val nested = "<score-partwise><part/></score-partwise>".encodeToByteArray()
        assertNull(probeXml(nested, XmlProbeLimits(maxDepth = 2, maxElements = 2)).diagnostic)
        assertEquals(ProbeCode.DEPTH_LIMIT, probeXml(nested, XmlProbeLimits(maxDepth = 1)).diagnostic?.code)
        assertEquals(ProbeCode.ELEMENT_LIMIT, probeXml(nested, XmlProbeLimits(maxElements = 1)).diagnostic?.code)
    }

    @Test
    fun testMalformedInputsFailWithoutPartialSuccess() {
        for (xml in listOf("", "<score-partwise><note></score-partwise>", "<score-partwise>", "<score-partwise/><score-timewise/>")) {
            assertCode(ProbeCode.MALFORMED_XML, xml)
        }
    }

    @Test
    fun testEncodingPolicy() {
        assertCode(ProbeCode.UNSUPPORTED_ENCODING, "<?xml version='1.0' encoding='ISO-8859-1'?><score-partwise/>")
        assertEquals(ProbeCode.UNSUPPORTED_ENCODING, probeXml(byteArrayOf(0xC3.toByte())).diagnostic?.code)
        assertCode(ProbeCode.UNSUPPORTED_ENCODING, "\u0000<score-partwise/>")
        assertNull(probeXml("\uFEFF<score-partwise/>".encodeToByteArray()).diagnostic)
    }

    @Test
    fun testConservativeDtdPolicyIsExplicit() {
        assertCode(ProbeCode.UNSAFE_XML, "<score-partwise><!-- <!DOCTYPE example> --></score-partwise>")
        assertCode(ProbeCode.UNSAFE_XML, "<score-partwise><![CDATA[<!ENTITY example>]]></score-partwise>")
    }

    private fun assertCode(code: ProbeCode, xml: String) {
        val result = probeXml(xml.encodeToByteArray())
        assertEquals(code, assertNotNull(result.diagnostic).code, xml)
        assertTrue(result.events.isEmpty())
        assertNull(result.root)
    }
}
