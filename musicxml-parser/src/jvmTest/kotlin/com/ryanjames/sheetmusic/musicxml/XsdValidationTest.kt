package com.ryanjames.sheetmusic.musicxml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * MX-04 schema harness: validates fixtures against the pinned MusicXML 4.0 XSD on the JVM, with no
 * network access. Schema validity is reported separately from import (`W3cSuiteImportTest`) and
 * from each fixture's semantic assertions; a valid document can still be unsupported, and the
 * importer still diagnoses invalid ones on every target.
 */
class XsdValidationTest {
    @Test
    fun testBundledSchemasMatchPinnedChecksums() {
        MusicXmlSchema.checksums.forEach { (file, checksum) ->
            assertEquals(checksum, MusicXmlSchema.sha256(MusicXmlSchema.bytes(file)), file)
        }
    }

    @Test
    fun testIndependentSuiteFixturesAreSchemaValid() {
        assertEquals(11, w3cSuiteFixtures.size)
        w3cSuiteFixtures.forEach { fixture ->
            assertEquals(emptyList(), MusicXmlSchema.validate(fixture.xml), fixture.file)
        }
    }

    @Test
    fun testImplementationFixturesSchemaOutcomes() {
        (auditFixtures + ("F20" to f20Score)).forEach { (id, xml) ->
            val errors = MusicXmlSchema.validate(xml)
            val expected = KNOWN_INVALID[id]
            if (expected == null) {
                assertEquals(emptyList(), errors, id)
            } else {
                assertEquals(expected, errors.map { it.substringBefore(":") }, "$id: $errors")
            }
        }
    }

    @Test
    fun testSchemaRejectsInvalidMusicXml() {
        val missingPartList = "<score-partwise version='4.0'><part id='P1'><measure number='1'/></part></score-partwise>"
        assertTrue(MusicXmlSchema.validate(missingPartList).isNotEmpty())
        val badDuration = SKELETON.replace("<duration>1</duration>", "<duration>-1</duration>")
        assertTrue(MusicXmlSchema.validate(badDuration).isNotEmpty())
        assertEquals(emptyList(), MusicXmlSchema.validate(SKELETON))
    }

    @Test
    fun testDoctypeIsNeverFetched() {
        // An unreachable DTD would fail or hang if it were loaded; it is skipped instead.
        val xml = "<!DOCTYPE score-partwise SYSTEM \"http://unreachable.invalid/partwise.dtd\">$SKELETON"
        assertEquals(emptyList(), MusicXmlSchema.validate(xml))
    }

    private companion object {
        const val SKELETON = "<score-partwise version='4.0'><part-list><score-part id='P1'><part-name>V</part-name>" +
            "</score-part></part-list><part id='P1'><measure number='1'><attributes><divisions>1</divisions></attributes>" +
            "<note><rest/><duration>1</duration></note></measure></part></score-partwise>"

        /**
         * Implementation-authored fixtures that are knowingly not schema-valid, with the line of each
         * error. F21 places 4.0 additions out of schema order (`part-link`, `line-detail` without
         * `staff-lines`, `for-part` beside `transpose`, `wait` in a direction's `listening`, and a
         * misplaced `level` with an invalid type); its import assertions depend on that order.
         */
        val KNOWN_INVALID = mapOf(
            "F21" to listOf("line 6", "line 21", "line 23", "line 36", "line 59", "line 59", "line 59"),
        )
    }
}
