package com.ryanjames.sheetmusic.musicxml.spike

import okio.ByteString.Companion.decodeHex
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ZipDependencyProbeTest {
    @Test
    fun testStoredAndDeflatedEntries() {
        for (fixture in listOf(storedZip, deflatedZip)) {
            withFileSystem { fileSystem ->
                val archive = "/fixture.zip".toPath()
                fileSystem.write(archive) { write(fixture.decodeHex()) }
                val result = probeZipEntry(fileSystem, archive, "/score.musicxml".toPath(), 17)
                assertNull(result.diagnostic)
                assertEquals("<score-partwise/>", result.bytes?.decodeToString())
            }
        }
    }

    @Test
    fun testEntryLimitEnforcedOnDecompressedBytes() {
        withFileSystem { fileSystem ->
            val archive = "/fixture.zip".toPath()
            fileSystem.write(archive) { write(deflatedZip.decodeHex()) }
            val result = probeZipEntry(fileSystem, archive, "/score.musicxml".toPath(), 16)
            assertEquals(ProbeCode.ARCHIVE_ENTRY_LIMIT, result.diagnostic?.code)
            assertNull(result.bytes)
        }
    }

    @Test
    fun testMissingEntryAndInvalidArchiveHaveDiagnostics() {
        withFileSystem { fileSystem ->
            val archive = "/fixture.zip".toPath()
            fileSystem.write(archive) { write(storedZip.decodeHex()) }
            assertEquals(ProbeCode.ARCHIVE_READ_FAILED, probeZipEntry(fileSystem, archive, "/missing".toPath(), 100).diagnostic?.code)
            fileSystem.write(archive) { writeUtf8("not a ZIP") }
            assertEquals(ProbeCode.ARCHIVE_READ_FAILED, probeZipEntry(fileSystem, archive, "/score.musicxml".toPath(), 100).diagnostic?.code)
        }
    }

    private fun withFileSystem(block: (FakeFileSystem) -> Unit) {
        val fileSystem = FakeFileSystem()
        try {
            block(fileSystem)
        } finally {
            fileSystem.checkNoOpenFiles()
        }
    }

    // Original fixtures generated for this project with .NET ZipArchive. Each contains one
    // score.musicxml entry with the 17 UTF-8 bytes <score-partwise/>. Methods: STORE and DEFLATE.
    private val storedZip =
        "504b03041400000000000000215c52481d1011000000110000000e00000073636f72652e6d75736963786d6c3c73636f72652d70617274776973652f3e504b010214001400000000000000215c52481d1011000000110000000e000000000000000000000000000000000073636f72652e6d75736963786d6c504b050600000000010001003c0000003d0000000000"
    private val deflatedZip =
        "504b03041400000008000000215c52481d1019000000110000000e00000073636f72652e6d75736963786d6cb2294ece2f4ad52d482c2a29cf2c4ed5b703000000ffff0300504b010214001400000008000000215c52481d1019000000110000000e000000000000000000000000000000000073636f72652e6d75736963786d6c504b050600000000010001003c000000450000000000"
}
