package com.ryanjames.sheetmusic.musicxml

import org.w3c.dom.ls.LSInput
import org.w3c.dom.ls.LSResourceResolver
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.security.MessageDigest
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

/**
 * Validates MusicXML documents against the pinned MusicXML 4.0 XSD in `src/jvmTest/resources/musicxml-4.0`,
 * independently of the importer. Nothing is fetched from the network: the schema's `xml.xsd` and
 * `xlink.xsd` imports resolve to the local copies, any other external schema or DTD is refused, and a
 * document's DOCTYPE is never loaded. This checks schema conformance only, not musical meaning.
 */
internal object MusicXmlSchema {
    /** SHA-256 of each bundled schema file, matching `docs/musicxml/element-inventory.md`. */
    val checksums: Map<String, String> = mapOf(
        "musicxml.xsd" to "bfe37ed25a9ec00e6f2591d53df260b84efe12aed209ba3ac0a76f9287665a99",
        "xml.xsd" to "616a3077df5cfc954ac74a75abe9697b95eef7a85dbe09367d995a483e840eb5",
        "xlink.xsd" to "6e601f8eeb41618b50e4c7f944dff754e57ea43b602755470dda24c9c2f6df92",
    )

    private val imports = mapOf(
        "http://www.musicxml.org/xsd/xml.xsd" to "xml.xsd",
        "http://www.musicxml.org/xsd/xlink.xsd" to "xlink.xsd",
    )

    private val schema: Schema by lazy {
        val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        factory.resourceResolver = LSResourceResolver { _, _, _, systemId, _ ->
            val file = imports[systemId] ?: error("Schema import outside the bundled set: $systemId")
            LocalInput(systemId, resource(file))
        }
        factory.newSchema(StreamSource(resource("musicxml.xsd"), "musicxml.xsd"))
    }

    /** The bytes of a bundled schema [file]. */
    fun bytes(file: String): ByteArray = resource(file).use { it.readBytes() }

    /** Lower-case hex SHA-256 of [bytes]. */
    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    /** Schema errors in [xml] with their line numbers; empty when the document is valid. */
    fun validate(xml: String): List<String> {
        val errors = mutableListOf<String>()
        val parsers = SAXParserFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }
        val reader = parsers.newSAXParser().xmlReader
        val validator = schema.newValidator().apply {
            setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
            errorHandler = object : ErrorHandler {
                override fun warning(exception: SAXParseException) = Unit
                override fun error(exception: SAXParseException) {
                    errors += "line ${exception.lineNumber}: ${exception.message}"
                }
                override fun fatalError(exception: SAXParseException) = throw exception
            }
        }
        validator.validate(SAXSource(reader, InputSource(StringReader(xml))))
        return errors
    }

    private fun resource(file: String): InputStream =
        checkNotNull(MusicXmlSchema::class.java.getResourceAsStream("/musicxml-4.0/$file")) { "Missing schema resource $file" }

    /** A schema import served from a bundled resource instead of its URL. */
    private class LocalInput(private val id: String, private val stream: InputStream) : LSInput {
        override fun getByteStream(): InputStream = stream
        override fun getSystemId(): String = id
        override fun getCharacterStream(): Reader? = null
        override fun getStringData(): String? = null
        override fun getPublicId(): String? = null
        override fun getBaseURI(): String? = null
        override fun getEncoding(): String? = null
        override fun getCertifiedText(): Boolean = false
        override fun setCharacterStream(characterStream: Reader?) = Unit
        override fun setByteStream(byteStream: InputStream?) = Unit
        override fun setStringData(stringData: String?) = Unit
        override fun setSystemId(systemId: String?) = Unit
        override fun setPublicId(publicId: String?) = Unit
        override fun setBaseURI(baseURI: String?) = Unit
        override fun setEncoding(encoding: String?) = Unit
        override fun setCertifiedText(certifiedText: Boolean) = Unit
    }
}
