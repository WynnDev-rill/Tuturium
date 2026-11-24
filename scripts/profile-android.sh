#!/usr/bin/env bash
set -euo pipefail
app=id.tuturium.app
output=app/build/reports/android-performance
mkdir -p "$output"
# Instrumentation skips animations for deterministic interactions. Re-enable them for this run.
for setting in window_animation_scale transition_animation_scale animator_duration_scale; do
  adb shell settings put global "$setting" 1
done
adb shell am force-stop "$app"
adb shell dumpsys gfxinfo "$app" reset > /dev/null
adb shell am start -W -n "$app/.MainActivity" | tee "$output/cold-start.txt"
sleep 2
read -r width height < <(adb shell wm size | tail -1 | tr -d '\r' | sed -E 's/.* ([0-9]+)x([0-9]+)/\1 \2/')
for _ in {1..5}; do
  adb shell input tap "$((width * 3 / 5))" "$((height * 93 / 100))"
  sleep 0.35
  adb shell input tap "$((width / 5))" "$((height * 93 / 100))"
  sleep 0.35
done
for _ in {1..3}; do
  adb shell input swipe "$((width / 2))" "$((height * 4 / 5))" "$((width / 2))" "$((height / 3))" 300
  adb shell input swipe "$((width / 2))" "$((height / 3))" "$((width / 2))" "$((height * 4 / 5))" 300
done
adb shell uiautomator dump /sdcard/tuturium-profile.xml > /dev/null
adb shell cat /sdcard/tuturium-profile.xml > "$output/ui.xml"
adb shell dumpsys gfxinfo "$app" > "$output/gfxinfo.txt"
adb shell dumpsys meminfo "$app" > "$output/meminfo.txt"
sed -n '/Total frames rendered:/,/Slow issue draw commands:/p' "$output/gfxinfo.txt"
