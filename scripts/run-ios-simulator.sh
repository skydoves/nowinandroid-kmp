#!/usr/bin/env bash
# Builds the iOS app and runs it on a booted simulator.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEVICE="${1:-iPhone 17 Pro}"
DERIVED_DATA="$ROOT/build/ios-derived-data"

"$ROOT/scripts/generate-xcodeproj.sh"

xcodebuild \
  -project "$ROOT/app/iosApp/NowInAndroid.xcodeproj" \
  -scheme NowInAndroid \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination "generic/platform=iOS Simulator" \
  -derivedDataPath "$DERIVED_DATA" \
  build

xcrun simctl boot "$DEVICE" 2>/dev/null || true
open -a Simulator
xcrun simctl install "$DEVICE" "$DERIVED_DATA/Build/Products/Debug-iphonesimulator/NowInAndroid.app"
xcrun simctl launch "$DEVICE" com.skydoves.nowinandroid
