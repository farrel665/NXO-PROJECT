# FF Optimizer

Android Java + XML application using Material 3 styled views and Shizuku.
No floating overlay is used.

## What it does

### Shizuku
- Detects whether Shizuku is running.
- Requests Shizuku permission.
- Runs selected Android shell commands through Shizuku.

Shizuku is an external dependency. The user must install/start Shizuku separately.

### Free Fire / Free Fire MAX compile
Targets:
- `com.dts.freefireth`
- `com.dts.freefiremax`

The Compile buttons call Android's package compiler:

`cmd package compile -m speed-profile -f <package>`

This uses Android's real package compilation mechanism. It does not patch game files, inject code, change game memory, or bypass game protections.

### Touch diagnostics
The app reads Android input diagnostics through `dumpsys input` when Shizuku allows it.

Important:
- Android does not provide a universal shell command that can force a touchscreen's physical sampling rate.
- `pointer_speed` is not a touchscreen polling-rate control.
- The app therefore does not fake a "120/240 Hz touch speed" number.
- If the OEM/kernel exposes a real touch control, it can be added as a device-specific backend later.

### Notification monitoring
Uses Android `NotificationListenerService`.
The user must manually grant Notification Access in Android Settings.

## Build

Open in Android Studio or run:

`./gradlew assembleDebug`

APK:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions is included in `.github/workflows/build.yml`.

## Requirements

- Android 8.0+ (API 26)
- Shizuku installed and running
- Non-root operation is supported when Shizuku is started through wireless debugging/ADB.
- For Android 11+, Shizuku can generally be started on-device using Wireless Debugging.

## Notes

This project intentionally avoids fake "touch booster" values. Actual touch behavior is controlled by the Android input stack, OEM configuration and touchscreen controller/kernel. Shizuku can provide elevated shell access, but it cannot create a hardware capability that the device does not expose.
