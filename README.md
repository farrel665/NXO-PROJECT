# NXO DEBLOAT

Kotlin + Jetpack Compose Material 3 + Shizuku, normal Activity only.

## Features
- Free Fire / Free Fire MAX detection
- AOT-style ART speed compilation
- speed-profile compilation
- app cache cleanup
- global Android cache trim
- automatic maximum supported display refresh-rate request
- display/FPS/SurfaceFlinger diagnostics
- touch diagnostics
- notification monitoring
- daemon/device status
- in-activity crosshair menu
- no XML UI
- no floating overlay

## Important technical behavior

### JIT / AOT
Android ART manages JIT at runtime. A normal app cannot universally force a permanent
"JIT ON" mode. The project therefore exposes Android package compilation modes rather
than pretending to control JIT.

### Refresh rate / FPS
Android exposes supported display modes to apps. NXO detects the maximum mode and
requests it through Android settings. The system/OEM scheduler may still choose a
different mode.

### SurfaceFlinger
The app reads SurfaceFlinger diagnostics. It does not write undocumented debug
properties such as arbitrary `debug.sf.*` values because those are OEM/build
dependent and can be ignored or destabilize devices.

### VSYNC
There is no universal supported non-root switch that disables VSYNC system-wide.
NXO therefore reports VSYNC/SurfaceFlinger state instead of displaying a fake
"VSYNC OFF" status.

### Cache
`pm clear --cache-only` is used for the selected game. It does not clear user data
or log the user out. A global `pm trim-caches` action is also available in the
backend for future UI expansion.

## Build
The GitHub Actions workflow installs Gradle 9.1 and JDK 17, then builds:
`./gradlew assembleDebug`

No XML layout files are used.

## Shizuku
Install/start Shizuku separately and grant NXO DEBLOAT permission.
