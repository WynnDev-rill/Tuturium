#!/usr/bin/env bash
set -euo pipefail
apk=$(mktemp --suffix=.apk)
trap 'unlink "$apk"' EXIT
curl --fail --silent --show-error --location https://f-droid.org/repo/com.reecedunn.espeak_22.apk -o "$apk"
printf '%s  %s\n' 0a7822fec54d7f7ae759ffedc9afeaa77f809587b171fb2101998787821ec790 "$apk" | sha256sum --check
adb install -r "$apk"
adb shell cmd connectivity airplane-mode enable
adb shell am start -W -a android.speech.tts.engine.INSTALL_TTS_DATA -n com.reecedunn.espeak/.DownloadVoiceData
for attempt in {1..30}; do
    if ! adb shell dumpsys activity activities | grep -E '(topResumedActivity|mResumedActivity).*DownloadVoiceData' >/dev/null; then
        break
    fi
    sleep 1
done
adb shell settings put secure tts_default_synth com.reecedunn.espeak
adb shell input keyevent KEYCODE_HOME
if ./gradlew --no-daemon connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.tuturiumPlayback=true; then
    exit 0
fi
adb logcat -d -s TextToSpeech:V eSpeak:V AndroidRuntime:E
python3 - <<'PYREPORT'
from pathlib import Path
from xml.etree import ElementTree
for report in Path("app/build/outputs/androidTest-results").rglob("TEST-*.xml"):
    for failure in ElementTree.parse(report).getroot().iter("failure"):
        print(failure.text or failure.get("message", "Android test failed"))
PYREPORT
exit 1
