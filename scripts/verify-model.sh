#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/out/model-smoke"
rm -rf "$OUT"
mkdir -p "$OUT"

mapfile -t MAIN_SOURCES < <(find "$ROOT/src/main/java" -name '*.java' \
  ! -path '*/view/*' \
  ! -path '*/controller/*' \
  ! -name 'App.java' | sort)

javac -encoding UTF-8 -d "$OUT" "${MAIN_SOURCES[@]}" \
  "$ROOT/src/test/java/ir/fum/siliconvalley/ModelSmokeTest.java"

java -cp "$OUT" ir.fum.siliconvalley.ModelSmokeTest
