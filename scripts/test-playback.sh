#!/usr/bin/env bash
set -euo pipefail

# The app now explicitly selects Google Speech Services. Installing eSpeak and
# forcing it as the system default would no longer test the production engine.
# Instrumented tests validate safe first-play behavior even when Google's
# Indonesian offline voice pack is absent on a CI emulator.
adb shell settings put global window_animation_scale 0 || true
adb shell settings put global transition_animation_scale 0 || true
adb shell settings put global animator_duration_scale 0 || true
adb shell settings put secure show_ime_with_hard_keyboard 1 || true

if ./gradlew --no-daemon connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.offlineVoiceSmoke=true; then
    adb logcat -d -s TuturiumVoiceBenchmark:I
    exit 0
fi

adb logcat -d -s TextToSpeech:V AndroidRuntime:E TuturiumPlayback:E TuturiumVoice:E
python3 - <<'PYREPORT'
from pathlib import Path
from xml.etree import ElementTree
for report in Path("app/build/outputs/androidTest-results").rglob("TEST-*.xml"):
    for failure in ElementTree.parse(report).getroot().iter("failure"):
        print(failure.text or failure.get("message", "Android test failed"))
PYREPORT
exit 1
