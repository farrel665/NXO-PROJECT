#!/bin/sh
set -eu

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

echo "Gradle is not installed."
echo "The included GitHub Actions workflow installs Gradle 8.13 automatically."
echo "For local builds, install Gradle 8.13+ or open the project in Android Studio."
exit 1
