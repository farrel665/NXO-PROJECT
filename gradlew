#!/bin/sh
set -eu
echo "This source package intentionally does not bundle Gradle Wrapper binaries."
echo "Use Android Studio's Gradle integration, or install Gradle 8.13 and run:"
echo "  gradle assembleDebug"
exit 2
