#!/usr/bin/env bash
set -euo pipefail
trap 'adb shell dumpsys activity activities | grep -i -E "resumed|mFocused" || true; python3 -c "from xml.etree import ElementTree as E; print([n.get(\"text\") for n in E.parse(\"window.xml\").iter(\"node\") if n.get(\"text\")])" || true' ERR
curl -fLsS https://github.com/WynnDev-rill/Tuturium/releases/download/v1.3.10/Tuturium.apk -o baseline.apk
adb shell settings put global hide_error_dialogs 1
adb shell settings put secure anr_show_background 0
adb shell am force-stop com.google.android.apps.nexuslauncher
adb install baseline.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
adb shell am start -W -n id.tuturium.app/.MainActivity
find_text() {
  local label=$1
  for ((attempt=0; attempt<${2:-8}; attempt++)); do
    adb shell uiautomator dump /sdcard/tuturium-window.xml >/dev/null 2>&1 || continue
    adb pull /sdcard/tuturium-window.xml window.xml >/dev/null 2>&1 || continue
    if coordinates=$(python3 - "$label" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET
for n in ET.parse('window.xml').getroot().iter('node'):
    if n.get('text', '').casefold() == sys.argv[1].casefold() and n.get('enabled') == 'true':
        points = [int(x) for x in re.findall(r'\d+', n.get('bounds', ''))]
        if len(points) == 4:
            print((points[0] + points[2]) // 2, (points[1] + points[3]) // 2)
            sys.exit(0)
sys.exit(1)
PY
    ); then
      printf '%s\n' "$coordinates"
      return
    fi
    sleep 1
  done
  return 1
}
button=$(find_text 'Unduh pembaruan')
echo 'The previous app exposed its download button for the published release.'
adb shell input tap $button
if permission=$(find_text 'Allow from this source' 4); then
  echo 'The verified package reached Android’s installation permission page.'
  adb shell input tap $permission
  adb shell input keyevent KEYCODE_BACK
fi
if ! install=$(find_text 'Update' 4) && ! install=$(find_text 'Install' 2); then
  if ready=$(find_text 'Pasang pembaruan' 2); then adb shell input tap $ready; fi
  install=$(find_text 'Update') || install=$(find_text 'Install')
fi
echo 'Android displayed an installation confirmation for the verified download.'
adb shell input tap $install
for attempt in {1..30}; do
  adb shell dumpsys package id.tuturium.app > package.txt
  if grep -F 'versionName=1.3.11' package.txt; then
    echo 'In-app installation completed.'
    exit 0
  fi
  sleep 2
done
exit 1
