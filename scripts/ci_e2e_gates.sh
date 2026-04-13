#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-pr}"

case "$MODE" in
  pr|daily|nightly) ;;
  *)
    echo "Usage: $0 [pr|daily|nightly]"
    exit 1
    ;;
esac

declare -i flaky_suites=0
declare -i total_suites=0

run_suite() {
  local label="$1"
  local class_name="$2"
  local cap_seconds="$3"
  local allow_retry="$4"

  total_suites+=1
  echo
  echo "=== GATE: $label ==="
  echo "Suite: $class_name"
  echo "Duration cap: ${cap_seconds}s"

  local start elapsed
  start=$(date +%s)
  if ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$class_name"; then
    elapsed=$(( $(date +%s) - start ))
    echo "[GATE][PASS] $label completed in ${elapsed}s"
  else
    elapsed=$(( $(date +%s) - start ))
    echo "[GATE][FAIL] $label failed in first attempt (${elapsed}s)"
    if [[ "$allow_retry" -eq 1 ]]; then
      echo "[GATE][RETRY] Re-running $label once to detect flakiness"
      start=$(date +%s)
      if ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class="$class_name"; then
        elapsed=$(( $(date +%s) - start ))
        flaky_suites+=1
        echo "[GATE][FLAKY] $label passed on retry (${elapsed}s)"
      else
        echo "[GATE][FAIL] $label failed again on retry"
        exit 1
      fi
    else
      exit 1
    fi
  fi

  if [[ "$elapsed" -gt "$cap_seconds" ]]; then
    echo "[GATE][FAIL] $label exceeded cap: ${elapsed}s > ${cap_seconds}s"
    exit 1
  fi
}

if [[ "$MODE" == "pr" ]]; then
  MAX_FLAKY_SUITES=0
  run_suite "PR Smoke" "com.arise.habitquest.e2e.SmokeE2ESuite" 900 0
elif [[ "$MODE" == "daily" ]]; then
  MAX_FLAKY_SUITES=1
  run_suite "Daily Smoke" "com.arise.habitquest.e2e.SmokeE2ESuite" 900 0
  run_suite "Daily Full Functional" "com.arise.habitquest.e2e.FullFunctionalE2ESuite" 3600 1
else
  MAX_FLAKY_SUITES=1
  run_suite "Nightly Smoke" "com.arise.habitquest.e2e.SmokeE2ESuite" 900 0
  run_suite "Nightly Full Functional" "com.arise.habitquest.e2e.FullFunctionalE2ESuite" 3600 1
  run_suite "Nightly Boundary" "com.arise.habitquest.e2e.BoundaryNightlySuite" 5400 1
fi

echo
echo "=== GATE SUMMARY ==="
echo "Mode: $MODE"
echo "Suites run: $total_suites"
echo "Flaky suites (passed on retry): $flaky_suites"
echo "Allowed flaky suites: $MAX_FLAKY_SUITES"

if [[ "$flaky_suites" -gt "$MAX_FLAKY_SUITES" ]]; then
  echo "[GATE][FAIL] Flake threshold exceeded"
  exit 1
fi

echo "[GATE][PASS] All gates satisfied"
