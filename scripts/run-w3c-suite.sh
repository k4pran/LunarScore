#!/usr/bin/env sh
# Runs the pinned W3C MusicXML test suite (https://github.com/w3c-cg/musicxmlTestSuite, MIT)
# through parseMusicXml/parseMxl under both policies, writes a Markdown report and removes the
# downloaded files.
#
# Usage: scripts/run-w3c-suite.sh [revision]
#   revision  suite commit or tag; defaults to W3C_SUITE_REVISION or the pinned commit below.
# The report is written to W3C_SUITE_REPORT, by default
# musicxml-parser/build/reports/w3c-suite/<revision>.md. Requires curl and tar.
set -eu

pinned_revision=77c19f7e819154c70ca1a1992e80dcda8ff82fea
revision=${1:-${W3C_SUITE_REVISION:-$pinned_revision}}
root=$(cd "$(dirname "$0")/.." && pwd)
report=${W3C_SUITE_REPORT:-$root/musicxml-parser/build/reports/w3c-suite/$revision.md}
# Gradle runs the test from the module directory, so a relative path is taken from here instead.
case $report in
    /* | [A-Za-z]:[/\\]*) ;;
    *) report=$PWD/$report ;;
esac

download=$(mktemp -d)
trap 'rm -rf "$download"' EXIT INT TERM

# Gradle runs on the JVM, which needs native paths on Windows.
native_path() {
    if command -v cygpath > /dev/null; then cygpath -w "$1"; else echo "$1"; fi
}

echo "Downloading musicxmlTestSuite $revision"
curl -fsSL -o "$download/suite.tar.gz" \
    "https://github.com/w3c-cg/musicxmlTestSuite/archive/$revision.tar.gz"
mkdir "$download/suite"
tar -xzf "$download/suite.tar.gz" -C "$download/suite"
suite=$(find "$download/suite" -type d -name xmlFiles | head -n 1)
if [ -z "$suite" ]; then
    echo "No xmlFiles directory in musicxmlTestSuite $revision" >&2
    exit 1
fi

cd "$root"
status=0
MUSICXML_CORPUS_DIR=$(native_path "$suite") \
MUSICXML_CORPUS_REPORT=$(native_path "$report") \
MUSICXML_CORPUS_LABEL="W3C musicxmlTestSuite $revision" \
    sh ./gradlew :musicxml-parser:jvmTest --tests '*PublicCorpusTest*' --rerun -i > "$download/gradle.log" 2>&1 ||
    status=$?
grep 'CORPUS SUMMARY' "$download/gradle.log" || true
if [ "$status" -ne 0 ]; then
    tail -n 60 "$download/gradle.log" >&2
    echo "Corpus run failed; see the output above" >&2
    exit "$status"
fi
echo "Report: $report"
