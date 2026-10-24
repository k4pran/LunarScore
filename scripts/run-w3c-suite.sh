#!/usr/bin/env bash
# Runs the W3C MusicXML test suite (https://github.com/w3c-cg/musicxmlTestSuite, MIT) through the
# importer under both parse policies and writes a per-file report.
#
# Usage: scripts/run-w3c-suite.sh [revision]
#   revision  Suite commit, tag or branch to test (default: the pinned commit below; "main" for the latest).
#
# Environment:
#   W3C_SUITE_REPORT  Report path (default: build/w3c-suite/report.tsv).
#   W3C_SUITE_KEEP=1  Keep the downloaded suite instead of deleting it (its path is printed).
#
# The suite is shallow-fetched into a temporary directory and deleted on exit. The run fails if any
# file throws or fails without an error diagnostic; rejections and discarded features are reported,
# not failures. Needs git, bash (Git Bash on Windows) and a JDK for Gradle.
set -euo pipefail

PINNED_REVISION=77c19f7e819154c70ca1a1992e80dcda8ff82fea
REPOSITORY=https://github.com/w3c-cg/musicxmlTestSuite.git
revision=${1:-$PINNED_REVISION}

root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
report=${W3C_SUITE_REPORT:-$root/build/w3c-suite/report.tsv}
# Gradle runs the test from the module directory, so a relative path is taken from here instead.
[[ "$report" == /* ]] || report=$PWD/$report
suite=$(mktemp -d "${TMPDIR:-/tmp}/w3c-musicxml-suite.XXXXXX")

cleanup() {
    if [[ "${W3C_SUITE_KEEP:-0}" == 1 ]]; then
        echo "Suite kept at $suite"
    else
        rm -rf "$suite"
    fi
}
trap cleanup EXIT

# Gradle runs on the JVM, which needs native paths on Windows.
native_path() {
    if command -v cygpath > /dev/null; then cygpath -w "$1"; else echo "$1"; fi
}

echo "Fetching $REPOSITORY at $revision"
git -C "$suite" init --quiet
git -C "$suite" fetch --quiet --depth 1 "$REPOSITORY" "$revision"
git -C "$suite" -c advice.detachedHead=false checkout --quiet FETCH_HEAD
echo "Suite commit $(git -C "$suite" rev-parse HEAD), $(find "$suite/xmlFiles" -type f \( -name '*.musicxml' -o -name '*.mxl' \) | wc -l | tr -d ' ') files"

mkdir -p "$(dirname "$report")"
rm -f "$report"
status=0
MUSICXML_CORPUS_DIR=$(native_path "$suite/xmlFiles") \
MUSICXML_CORPUS_REPORT=$(native_path "$report") \
    "$root/gradlew" -p "$root" --quiet :musicxml-parser:jvmTest --tests '*PublicCorpusTest*' --rerun || status=$?

if [[ -f "$report" ]]; then
    echo
    grep '^CORPUS SUMMARY' "$report" | sed 's/^CORPUS SUMMARY //'
    echo "Per-file report: $report"
else
    echo "No report was written; see the Gradle output above." >&2
    status=${status/#0/1}
fi
exit "$status"
