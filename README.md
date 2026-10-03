# ImGui Overlay Menu - Android

Standalone Android overlay template using Kotlin + C++17 + Dear ImGui + OpenGL ES 3 + JNI.

## Build locally

Open the folder in Android Studio with Android SDK 35, NDK 27.2.12479018 and CMake 3.22.1 installed. The CMake script downloads Dear ImGui 1.92.2b during configuration.

## GitHub Actions

Push the project to GitHub and run **Actions → Build APK → Run workflow**. The workflow installs the required SDK/NDK/CMake packages and builds `app-debug.apk`.

## Runtime

1. Install APK.
2. Open the app.
3. Grant **Display over other apps**.
4. Tap **Start floating menu**.
5. Tap/drag the `IM` bubble.
6. Hide returns the overlay to the 64x64 bubble, so the hidden panel does not leave a large transparent touch-blocking window.

The menu is intentionally generic. It is a UI foundation; application-specific actions should be implemented behind the controls you choose.
