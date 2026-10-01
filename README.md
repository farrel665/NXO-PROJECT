# NXO DEBLOAT

Android Kotlin + Jetpack Compose + Material 3 + Shizuku, non-root.

## UI
- Entire application UI is Jetpack Compose/Kotlin.
- No XML layout files are used.
- AndroidManifest.xml remains XML because Android requires a manifest.
- Normal Activity only; no floating overlay.

## Features

### FF / FF MAX JIT/AOT
Uses Android ART package compilation:
- `speed-profile` profile-guided compilation
- background dexopt

Packages:
- `com.dts.freefireth`
- `com.dts.freefiremax`

No game-memory injection or asset patching is performed.

### Auto cache cleanup
Uses `pm trim-caches 2G`. This trims package caches without `pm clear`, so game data/login data is not intentionally wiped.

### Refresh rate / SurfaceFlinger
- Detects supported display modes.
- Can request the detected maximum refresh rate using standard `peak_refresh_rate` and `min_refresh_rate` settings when exposed by the device.
- Auto max-refresh toggle is included.
- SurfaceFlinger diagnostics are read through Shizuku.

OEMs may ignore standard settings. The app does not pretend a setting worked when Android/OEM does not expose it.

### FPS / VSync
- Reads real `gfxinfo` frame statistics.
- Checks exposed SurfaceFlinger/VSync properties.
- Requests Android Game Mode `performance` when the device supports the command.
- There is intentionally no fake universal "unlock FPS" or "disable VSync" command. FPS caps and VSync are ultimately controlled by Android/OEM/game rendering paths.

### Device + daemon monitoring
- NotificationListenerService monitors notification events.
- While the Activity is open and Shizuku is authorized, the app also checks the resumed package every ~2.5 seconds.
- When FF/FF MAX is detected, the UI changes to a GAME SESSION / DEVICE + DAEMON MONITORING state.

### Crosshair menu
A Compose crosshair menu/preview is included. It is UI-only:
- Classic
- Dot
- Plus
- Circle

It does not inject an overlay into Free Fire and does not automate aiming/input.

## Build
AGP 8.10.0 requires Gradle 8.11.1 or newer; the included workflow installs Gradle 8.13 and JDK 17. See Android's AGP compatibility documentation.

GitHub Actions workflow:
`.github/workflows/build.yml`

Build command:
`./gradlew assembleDebug --stacktrace`

Artifact:
`app/build/outputs/apk/debug/app-debug.apk`
