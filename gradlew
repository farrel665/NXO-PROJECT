#!/bin/sh
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle is not installed. The included GitHub Actions workflow installs Gradle 8.13." >&2
exit 1
