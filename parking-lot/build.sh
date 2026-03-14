#!/bin/bash
# =============================================================================
# build.sh — Parking Lot LLD Build Script
# Works with plain javac/java (no Maven Central needed)
# =============================================================================

set -e  # Exit immediately on any error

SRC_MAIN="src/main/java"
SRC_TEST="src/test/java"
OUT="target/classes"
MAIN_CLASS="parkinglot.Main"
FEE_TEST="parkinglot.FeeCalculatorTest"
SPOT_TEST="parkinglot.SpotAssignmentStrategyTest"
JAR="target/parking-lot.jar"

mkdir -p "$OUT"

case "$1" in

  # ---------------------------------------------------------------------------
  compile)
    echo "[BUILD] Compiling main sources..."
    find "$SRC_MAIN" -name "*.java" > target/main_sources.txt
    javac -d "$OUT" @target/main_sources.txt
    echo "[BUILD] Compile SUCCESS — classes in $OUT"
    ;;

  # ---------------------------------------------------------------------------
  test)
    echo "[BUILD] Compiling main + test sources..."
    find "$SRC_MAIN" "$SRC_TEST" -name "*.java" > target/all_sources.txt
    javac -d "$OUT" @target/all_sources.txt
    echo "[BUILD] Running FeeCalculatorTest..."
    java -cp "$OUT" "$FEE_TEST"
    echo "[BUILD] Running SpotAssignmentStrategyTest..."
    java -cp "$OUT" "$SPOT_TEST"
    echo "[BUILD] All tests PASSED"
    ;;

  # ---------------------------------------------------------------------------
  run)
    echo "[BUILD] Compiling and running $MAIN_CLASS..."
    find "$SRC_MAIN" -name "*.java" > target/main_sources.txt
    javac -d "$OUT" @target/main_sources.txt
    java -cp "$OUT" "$MAIN_CLASS"
    ;;

  # ---------------------------------------------------------------------------
  package)
    echo "[BUILD] Compiling..."
    find "$SRC_MAIN" -name "*.java" > target/main_sources.txt
    javac -d "$OUT" @target/main_sources.txt
    echo "[BUILD] Packaging JAR: $JAR"
    jar cfe "$JAR" "$MAIN_CLASS" -C "$OUT" .
    echo "[BUILD] Package SUCCESS — run with: java -jar $JAR"
    ;;

  # ---------------------------------------------------------------------------
  clean)
    echo "[BUILD] Cleaning target/..."
    rm -rf target/
    echo "[BUILD] Clean done"
    ;;

  # ---------------------------------------------------------------------------
  all)
    bash "$0" clean
    bash "$0" compile
    bash "$0" test
    bash "$0" package
    echo "[BUILD] Full build complete — run with: java -jar $JAR"
    ;;

  # ---------------------------------------------------------------------------
  *)
    echo ""
    echo "Usage: bash build.sh <command>"
    echo ""
    echo "  compile   Compile source files"
    echo "  test      Compile and run all tests"
    echo "  run       Compile and run the demo (Main.java)"
    echo "  package   Compile and build executable JAR"
    echo "  clean     Delete target/ directory"
    echo "  all       clean + compile + test + package"
    echo ""
    ;;
esac
