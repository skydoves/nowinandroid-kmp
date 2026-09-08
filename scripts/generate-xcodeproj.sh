#!/usr/bin/env bash
# Regenerates app/iosApp/NowInAndroid.xcodeproj from project.yml.
# The .xcodeproj is generated rather than committed, so project.yml stays the single source of truth.
set -euo pipefail

if ! command -v xcodegen >/dev/null 2>&1; then
  echo "XcodeGen is required: brew install xcodegen" >&2
  exit 1
fi

cd "$(dirname "$0")/../app/iosApp"
xcodegen generate
echo "Generated app/iosApp/NowInAndroid.xcodeproj"
