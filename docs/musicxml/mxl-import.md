# Compressed `.mxl` import (MX-19)

Issue [#20](https://github.com/k4pran/LunarScore/issues/20) adds the public
`parseMxl(input, options, archiveLimits, sourceName, isCancelled): ParseResult<Score>` adapter.
It follows the MusicXML 4.0 [compressed file format](https://www.w3.org/2021/06/musicxml40/tutorial/compressed-mxl-files/):
it validates the ZIP archive, reads `META-INF/container.xml`, selects the **first** `rootfile` and
passes that entry's bytes unchanged to `parseMusicXml`. The resulting score therefore equals the
score parsed from the uncompressed file; only diagnostic and provenance source names differ.

## Archive validation

The archive is held in memory (`ArchiveLimits.maxArchiveBytes`, default 16 MiB). The whole central
directory is validated from raw bytes before any entry is expanded. Okio's `openZip` is not used,
because it normalizes paths and collapses duplicate entries; Okio's `Inflater` is used only to
expand a single validated DEFLATE entry.

| Check | Outcome (fatal under both policies) |
|---|---|
| No end record, trailing data after the end record, truncated or inconsistent records, central directory not ending at the end record | `ARCHIVE_READ_FAILED` |
| Entry name that is empty, absolute, contains `\`, `:` or a control character, or has an empty, `.` or `..` segment | `UNSAFE_ARCHIVE` |
| Non-ASCII name without the UTF-8 flag, or invalid UTF-8 with it | `UNSAFE_ARCHIVE` |
| Two entries whose names match case-insensitively (a directory `a/` and a file `a` also clash) | `UNSAFE_ARCHIVE` |
| Local header name, method or encryption flag differs from the central record; entry data overlapping another entry | `UNSAFE_ARCHIVE` |
| Encryption (flag bits 0, 6, 13 or AES method 99), ZIP64 records or sizes, multi-disk archives, methods other than STORE/DEFLATE | `UNSUPPORTED_ARCHIVE` |
| Archive bytes, entry count (1,000), total declared expansion (64 MiB) or per-entry ratio (200:1) over budget | `ARCHIVE_LIMIT` |
| Read entry declared larger than its budget (`maxContainerBytes` 64 KiB for the container, `ParseLimits.maxInputBytes` for the score) | `ARCHIVE_ENTRY_LIMIT` |
| Expanded size differs from the directory, CRC-32 mismatch, corrupt DEFLATE data | `ARCHIVE_READ_FAILED` |

Inflation reads at most the declared size plus one byte, so a directory that understates a size
cannot expand further. Only the container, the optional `mimetype` and the selected score are
expanded; other renditions are checked from metadata only.

## Container and mimetype rules

| Rule | Outcome |
|---|---|
| `META-INF/container.xml` missing | `INVALID_STRUCTURE` |
| Container XML | Read with the same secure reader as scores: UTF-8 only, no DTD/entities, same limits. Root must be a no-namespace `container` (`UNSUPPORTED_ROOT` / `UNSUPPORTED_NAMESPACE`) |
| No `rootfiles`, more than one `rootfiles`, no `rootfile`, `rootfile` without `full-path` | `INVALID_STRUCTURE` |
| First `rootfile` `media-type` other than `application/vnd.recordare.musicxml+xml` (omitted means MusicXML) | `INVALID_STRUCTURE` |
| Unsafe or duplicate (case-insensitive) `full-path` in any rootfile | `UNSAFE_ARCHIVE` at the rootfile element |
| First rootfile entry missing or a directory | `INVALID_STRUCTURE` |
| `mimetype` present with content other than `application/vnd.recordare.musicxml` | `INVALID_STRUCTURE` |
| `mimetype` correct but not the first entry, or compressed | `RECOVERED_VALUE`: strict failure, tolerant warning before the score's diagnostics |
| No `mimetype` | Accepted; it is optional |

Archive warnings share `ParseLimits.maxDiagnostics` with the score; overflow returns a failure ending
in `DIAGNOSTIC_LIMIT`. Locations name the archive entry as `sourceName!/entry`, or the entry alone
when no `sourceName` is given. Cancellation is checked before reading, between directory records
and throughout the XML reads.

## Tests

`MxlImportTest` uses F20 (`MxlFixtures.kt`), an original archive generated with .NET `ZipArchive`
(an independent ZIP writer) containing `mimetype`, a container with two renditions, the score and
a PDF placeholder. Further original archives are built in the test with a small ZIP writer:
STORE/DEFLATE without `mimetype`, traversal and unsafe names, duplicate entries and rootfiles,
encrypted/AES/BZIP2/ZIP64/multi-disk records, a 1 MiB zero-filled DEFLATE bomb, exact/excess
budgets, an understated size, overlapping entries, CRC and DEFLATE corruption, container and
mimetype rules, the shared diagnostic cap and cancellation. Tests run on JVM and Android host;
iOS test klibs compile, but iOS runtime needs macOS/Xcode.

## Not supported

Encrypted, ZIP64, multi-disk and non-DEFLATE archives; reading renditions other than the first
rootfile; streaming input larger than memory; non-UTF-8 container encodings and archive comments.
