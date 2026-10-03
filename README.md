# ImGui Overlay Menu (Android)

Template Android project with:
- Dear ImGui fetched automatically during Gradle build
- C++17 + JNI
- Android overlay permission + floating service
- Draggable bubble; hidden state shrinks the WindowManager view so it does not leave a large invisible touch blocker
- Generic tabs, toggles, slider, demo window
- CMake/NDK and GitHub Actions APK build

## Build
1. Open the project in Android Studio or push it to GitHub.
2. Use JDK 17, Android SDK 35, NDK 27.2.12479018, CMake 3.31.6.
3. Run `./gradlew assembleDebug`.
4. The first build downloads Dear ImGui v1.92.2b automatically.

## Important renderer note
The included service/view skeleton deliberately separates overlay/window behavior from native ImGui state. For a production GPU overlay, replace `ImGuiOverlayView` with a `GLSurfaceView`/`SurfaceView`-backed WindowManager view and call `ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData())` after `ImGui::Render()`. This avoids pretending that Android Canvas can render ImGui's OpenGL draw lists.

## GitHub Actions
Push the extracted project to a GitHub repository. The workflow installs Gradle 8.13, Android SDK/NDK/CMake, fetches Dear ImGui automatically, builds the debug APK, and publishes the APK as an artifact.
