#!/bin/sh
set -eu
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi
echo "Gradle is not installed. Use the included GitHub Actions workflow or Android Studio."
exit 1
