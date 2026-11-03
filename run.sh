#!/usr/bin/env bash
# Runs the Library Management System with the SQLite JDBC driver on the
# classpath. The database file is created in ./data/library.db on first
# run. If the sources are not yet compiled, this script compiles them.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JDBC_JAR="libs/sqlite-jdbc.jar"
OUT_DIR="out"
MAIN_CLASS="Menu"

if [ ! -f "$JDBC_JAR" ]; then
    echo "ERROR: $JDBC_JAR not found."
    echo "       Download it from https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/"
    exit 1
fi

if [ ! -d "$OUT_DIR" ] || [ ! -f "$OUT_DIR/$MAIN_CLASS.class" ]; then
    echo "Class files not found, compiling first..."
    bash ./compile.sh
fi

mkdir -p data
java -cp "$OUT_DIR:$JDBC_JAR" "$MAIN_CLASS"
