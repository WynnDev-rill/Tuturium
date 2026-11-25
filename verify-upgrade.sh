#!/usr/bin/env bash
set -euo pipefail
app=id.tuturium.app
trap 'if [[ -s window.xml ]]; then python3 -c "from xml.etree import ElementTree as ET; print([(n.get(\"text\"), n.get(\"content-desc\")) for n in ET.parse(\"window.xml\").iter(\"node\") if n.get(\"text\") or n.get(\"content-desc\")])"; fi; adb logcat -d -s AndroidRuntime:E || true' ERR
device_abis=$(adb shell getprop ro.product.cpu.abilist | tr -d '\r')
echo "Emulator ABIs: $device_abis"
[[ "$device_abis" == *arm64-v8a* ]]
# Suppress background widget/launcher ANRs during emulator startup.
adb shell settings put global hide_error_dialogs 1
adb shell settings put secure anr_show_background 0
adb shell am force-stop com.google.android.apps.nexuslauncher
curl -fLsS https://github.com/WynnDev-rill/Tuturium/releases/download/v1.3.24/Tuturium.apk -o before.apk
adb install before.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
adb shell am start -W -n "$app/.MainActivity"
find_text() {
  local label=$1
  for ((attempt=0; attempt<${2:-8}; attempt++)); do
    adb shell uiautomator dump /sdcard/tuturium-window.xml >/dev/null 2>&1 || continue
    adb pull /sdcard/tuturium-window.xml window.xml >/dev/null 2>&1 || continue
    if grep -Fq "Pixel Launcher isn't responding" window.xml; then
      adb shell am force-stop com.google.android.apps.nexuslauncher
      adb shell am start -n "$app/.MainActivity" >/dev/null
      continue
    fi
    if coordinates=$(python3 - "$label" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET
for n in ET.parse('window.xml').getroot().iter('node'):
    labels = (n.get('text', ''), n.get('content-desc', ''))
    if any(sys.argv[1].casefold() in label.casefold() for label in labels) and n.get('enabled') == 'true':
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

profile() {
  local name=$1
  local options
  sleep 2
  options=$(find_text 'Opsi pembaca' 8) || {
    echo "$name could not locate reader options"
    return 1
  }
  for key in window_animation_scale transition_animation_scale animator_duration_scale; do
    adb shell settings put global "$key" 1 || return 1
  done
  adb shell input tap $options || return 1
  find_text 'Ubah Nama' 8 >/dev/null || {
    echo "$name could not verify the warmup menu"
    return 1
  }
  adb shell input keyevent KEYCODE_BACK || return 1
  sleep 1
  adb shell dumpsys gfxinfo "$app" reset >/dev/null || return 1
  for sample in {1..5}; do
    adb shell input tap $options || return 1
    find_text 'Ubah Nama' 6 >/dev/null || {
      echo "$name menu $sample did not open"
      return 1
    }
    adb shell input keyevent KEYCODE_BACK || return 1
    sleep 1
  done
  find_text 'Opsi pembaca' 8 >/dev/null || {
    echo "$name menu did not close"
    return 1
  }
  adb shell dumpsys gfxinfo "$app" > "$name-menu-gfxinfo.txt" || return 1
  sed -n -E '/Total frames rendered:|Janky frames:|50th percentile:|95th percentile:|99th percentile:/p' "$name-menu-gfxinfo.txt" | head -n 7 | sed "s/^/$name menu /"
}
echo "Creating a text document on the public 1.3.24 APK."
paste=$(find_text 'Tempel' 12)
adb shell input tap $paste
editor=$(find_text 'Teks bacaan')
adb shell input tap $editor
adb shell input text MigrasiTuturiumMusimDingin
save=$(find_text 'Simpan')
adb shell input tap $save
find_text 'MigrasiTuturiumMusimDingin' >/dev/null
echo "Old version opened the seeded text."
if ! profile before; then echo "Before menu sample was not collected."; fi
for key in window_animation_scale transition_animation_scale animator_duration_scale; do
  adb shell settings put global "$key" 0
done
adb shell dumpsys package "$app" > package-before.txt
grep -F 'versionName=1.3.24' package-before.txt
before=$(sed -n 's/^[[:space:]]*firstInstallTime=//p' package-before.txt | head -n1)
test -n "$before"
adb shell am force-stop "$app"
adb install -r Tuturium.apk
adb shell am start -W -n "$app/.MainActivity" | tee launch.txt
grep -F 'Status: ok' launch.txt
adb shell dumpsys package "$app" > package-after.txt
grep -F 'versionName=1.3.25' package-after.txt
after=$(sed -n 's/^[[:space:]]*firstInstallTime=//p' package-after.txt | head -n1)
test "$before" = "$after"
lib=$(find_text 'Perpustakaan')
adb shell input tap $lib
book=$(find_text 'Teks pribadi' 12)
adb shell input tap $book
find_text 'MigrasiTuturiumMusimDingin' >/dev/null
echo "Upgrade 1.3.24 -> 1.3.25 kept the original install time and opened the old document."
if ! profile after; then echo "After menu sample was not collected."; fi
