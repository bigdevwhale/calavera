# Captures the README / Stillcraft screenshots from the demo build on the test phone.
#   bash tools/capture.sh <raw_dir> en|ru
# Needs `./gradlew installDebug -Pdemo`. Wipes app.calavera.demo (a test build) to seed it afresh in the language.
set -e
cd "$(dirname "$0")/.."
source tools/phone.sh
RAW="$1"; LANG_="${2:-en}"; PKG=app.calavera.demo
SHOTS="$RAW"; mkdir -p "$RAW"
SUF=""; [ "$LANG_" = "ru" ] && SUF=".ru"

# Finds a label on screen by key (labels live in tools/labels.py, so Cyrillic never passes through argv).
tap_key() {
  guard "$PKG" || return 1
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  local b
  b=$("$ADB" exec-out cat /sdcard/ui.xml | python tools/labels.py "$1" "$LANG_")
  [ -n "$b" ] || { echo "ABORT: no '$1' on screen"; return 1; }
  "$ADB" shell input tap $b
}
tap() { guard "$PKG" && "$ADB" shell input tap "$1" "$2"; }
snap() { sleep "${2:-1.5}"; shot "$1$SUF"; }

"$ADB" shell pm clear "$PKG" >/dev/null
"$ADB" shell cmd locale set-app-locales "$PKG" --locales "$([ "$LANG_" = ru ] && echo ru-RU || echo en-US)"
"$ADB" shell am start -n "$PKG/app.calavera.MainActivity" >/dev/null
sleep 6
snap casa
tap 540 2315; snap ofrenda
tap_key nana; snap soul
"$ADB" shell input keyevent BACK; sleep 1
tap 886 2315; snap fiesta
tap_key calaveritas; snap calaveritas
"$ADB" shell input keyevent BACK; sleep 1
tap_key loteria; sleep 1.5
tap_key draw; sleep 1.5
tap 540 800; snap card 2
"$ADB" shell input keyevent BACK; sleep 1
tap_key picado; sleep 1.5
for s in "350 800 700 1100" "820 900 820 1400" "400 1400 650 1500" "300 1000 330 1250" "600 750 760 820" "450 1250 560 1150"; do "$ADB" shell input swipe $s 350; done
tap_key unfold; snap picado 2
"$ADB" shell input keyevent BACK; sleep 1
tap 195 2315; sleep 1
tap 855 190; snap wardrobe
echo "done: $RAW ($LANG_)"
