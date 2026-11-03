#!/usr/bin/env bash
# Compiles every .java file in this folder and puts the .class files
# in the ./out folder. The SQLite JDBC driver is added to the classpath
# so that DatabaseHandler.java compiles.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JDBC_JAR="libs/sqlite-jdbc.jar"
OUT_DIR="out"

if [ ! -f "$JDBC_JAR" ]; then
    echo "ERROR: $JDBC_JAR not found."
    echo "       Download it from https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/"
    exit 1
fi

echo "Compiling Java sources..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"
javac -encoding UTF-8 -cp "$JDBC_JAR" -d "$OUT_DIR" *.java
echo "Done. Class files are in $OUT_DIR/"
