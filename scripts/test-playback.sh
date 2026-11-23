#!/usr/bin/env bash
set -euo pipefail
apk=$(mktemp --suffix=.apk)
trap 'unlink "$apk"' EXIT
curl --fail --silent --show-error --location https://f-droid.org/repo/com.reecedunn.espeak_22.apk -o "$apk"
printf '%s  %s\n' 0a7822fec54d7f7ae759ffedc9afeaa77f809587b171fb2101998787821ec790 "$apk" | sha256sum --check
adb install -r "$apk"
adb shell settings put secure tts_default_synth com.reecedunn.espeak
adb shell settings put global airplane_mode_on 1
adb shell cmd connectivity airplane-mode enable
./gradlew --no-daemon connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.tuturiumPlayback=true
