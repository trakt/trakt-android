#!/usr/bin/env bash
set -euo pipefail

# --- config ---
ADB="${ADB:-/Users/michal/Library/Android/sdk/platform-tools/adb}"
DEVICE="emulator-5556"
PACKAGE="tv.trakt.trakt.v3"
APK="../../app/build/outputs/apk/internal/debug/app-internal-debug.apk"
LAUNCH_WAIT=2
DEFAULT_WAIT=1

# --- helpers ---
pass() { echo "  PASS: $1"; }
fail() { echo "  FAIL: $1"; exit 1; }

LAYOUT_SNAPSHOT=""

snapshot_layout() {
  LAYOUT_SNAPSHOT=$(android layout --device "$DEVICE" 2>/dev/null)
}

layout_json() {
  echo "$LAYOUT_SNAPSHOT"
}

has_text() {
  local text="$1"
  layout_json | jq -e --arg t "$text" '[.[] | select(.text != null) | .text | ascii_downcase] | any(contains($t | ascii_downcase))' > /dev/null
}

element_center() {
  local text="$1"
  layout_json | jq -r --arg t "$text" '.[] | select(.text != null) | select(.text | ascii_downcase | contains($t | ascii_downcase)) | .center' | head -1
}

tap_center() {
  local center="$1"
  local x y
  x=$(echo "$center" | tr -d '[]' | cut -d',' -f1)
  y=$(echo "$center" | tr -d '[]' | cut -d',' -f2)
  $ADB -s "$DEVICE" shell input tap "$x" "$y"
}

tap_text() {
  local text="$1"
  local center
  center=$(element_center "$text")
  if [ -z "$center" ]; then
    fail "element with text '$text' not found"
  fi
  tap_center "$center"
}

# --- setup ---
ensure_emulator() {
  echo "==> Ensure emulator running"
  if $ADB devices | grep -q "^$DEVICE[[:space:]]*device$"; then
    echo "  Pixel_5 already running"
  else
    echo "  Starting Pixel_5..."
    android emulator start Pixel_5
    echo "  Pixel_5 ready"
  fi
}

clear_user_apps() {
  echo "==> Clear user apps"
  $ADB -s "$DEVICE" shell pm list packages -3 | cut -d: -f2 | \
    while read -r pkg; do
      $ADB -s "$DEVICE" shell pm uninstall "$pkg" > /dev/null 2>&1 && echo "  Removed $pkg"
    done
}

install_app() {
  echo "==> Install fresh app"
  $ADB -s "$DEVICE" install "$APK" > /dev/null
  echo "  Installed $APK"
}

launch_app() {
  echo "==> Launch app"
  $ADB -s "$DEVICE" shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
  sleep "$LAUNCH_WAIT"
}

ensure_emulator
clear_user_apps
install_app
launch_app

# --- test: onboarding screen ---
echo ""
echo "==> Test: onboarding page 1 content"
snapshot_layout

has_text "Discover" && pass "headline 'Discover' visible" || fail "headline 'Discover' missing"
has_text "Track" && pass "headline 'Track' visible" || fail "headline 'Track' missing"
has_text "Share" && pass "headline 'Share' visible" || fail "headline 'Share' missing"
has_text "Continue" && pass "'Continue' button visible" || fail "'Continue' button missing"

echo ""
echo "==> Test: tap Continue advances to page 2"
tap_text "Continue"
sleep "$DEFAULT_WAIT"
snapshot_layout

has_text "Discover" && fail "page 1 text 'Discover' still visible — Continue did not advance" || pass "page 1 dismissed"

echo ""
echo "==> Test: onboarding page 2 content"

has_text "Everything you watch" && pass "headline 'Everything you watch.' visible" || fail "headline 'Everything you watch.' missing"
has_text "In one place" && pass "headline 'In one place.' visible" || fail "headline 'In one place.' missing"
has_text "Continue" && pass "'Continue' button visible" || fail "'Continue' button missing"
has_text "Login" && pass "'Login' button visible" || fail "'Login' button missing"

echo ""
echo "==> Test: tap Continue advances to page 3"
tap_text "Continue"
sleep "$DEFAULT_WAIT"
snapshot_layout

has_text "Everything you watch" && fail "page 2 text still visible — Continue did not advance" || pass "page 2 dismissed"

echo ""
echo "==> Test: onboarding page 3 content"

has_text "Find where to watch it" && pass "headline 'Find where to watch it.' visible" || fail "headline 'Find where to watch it.' missing"
has_text "Just press play" && pass "headline 'Just press play.' visible" || fail "headline 'Just press play.' missing"
has_text "Continue" && pass "'Continue' button visible" || fail "'Continue' button missing"
has_text "Login" && pass "'Login' button visible" || fail "'Login' button missing"

echo ""
echo "==> Test: tap Continue advances to page 4"
tap_text "Continue"
sleep "$DEFAULT_WAIT"
snapshot_layout

has_text "Find where to watch it" && fail "page 3 text still visible — Continue did not advance" || pass "page 3 dismissed"

echo ""
echo "==> Test: onboarding page 4 content"

has_text "Show how you feel" && pass "headline 'Show how you feel.' visible" || fail "headline 'Show how you feel.' missing"
has_text "Say what you think" && pass "headline 'Say what you think.' visible" || fail "headline 'Say what you think.' missing"
has_text "Continue" && pass "'Continue' button visible" || fail "'Continue' button missing"
has_text "Login" && pass "'Login' button visible" || fail "'Login' button missing"

echo ""
echo "==> Test: tap Continue advances to page 5"
tap_text "Continue"
sleep "$DEFAULT_WAIT"
snapshot_layout

has_text "Show how you feel" && fail "page 4 text still visible — Continue did not advance" || pass "page 4 dismissed"

echo ""
echo "==> Test: onboarding page 5 content"

has_text "There's more to discover" && pass "headline 'There's more to discover.' visible" || fail "headline 'There's more to discover.' missing"
has_text "You've only scratched the surface" && pass "headline 'You've only scratched the surface.' visible" || fail "headline 'You've only scratched the surface.' missing"
has_text "Join Trakt" && pass "'Join Trakt' button visible" || fail "'Join Trakt' button missing"
has_text "Continue" && fail "unexpected 'Continue' button on last page" || pass "no 'Continue' on final page"
has_text "Login" && fail "unexpected 'Login' button on last page" || pass "no 'Login' on final page"

echo ""
echo "All tests passed."

echo ""
echo "==> Cleanup"
$ADB -s "$DEVICE" uninstall "$PACKAGE" > /dev/null
echo "  Uninstalled $PACKAGE"
