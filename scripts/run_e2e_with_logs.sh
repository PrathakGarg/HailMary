#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <test-class-or-package>"
  echo "Example: $0 com.arise.habitquest.e2e.SmokeE2ESuite"
  exit 1
fi

TARGET="$1"

adb logcat -c

./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class="$TARGET"

echo
printf '=== Concise E2E Test Log ===\n'
adb logcat -d -s E2E:I | sed -n '/\[E2E\]/p'
