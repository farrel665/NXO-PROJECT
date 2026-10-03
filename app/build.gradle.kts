import java.net.URI
import java.util.zip.ZipInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val imguiVersion = "v1.92.2b"
val imguiDir = layout.projectDirectory.dir("src/main/cpp/third_party/imgui").asFile
val prepareImgui by tasks.registering {
    outputs.dir(imguiDir)
    doLast {
        if (File(imguiDir, "imgui.cpp").exists() && File(imguiDir, "backends/imgui_impl_android.cpp").exists()) return@doLast
        val tmp = layout.buildDirectory.file("imgui-$imguiVersion.zip").get().asFile
        tmp.parentFile.mkdirs()
        URI("https://github.com/ocornut/imgui/archive/refs/tags/$imguiVersion.zip").toURL().openStream().use { input -> tmp.outputStream().use { input.copyTo(it) } }
        imguiDir.deleteRecursively(); imguiDir.mkdirs()
        ZipInputStream(tmp.inputStream().buffered()).use { zis ->
            var e = zis.nextEntry
            while (e != null) {
                val prefix = "imgui-$imguiVersion/"
                if (e.name.startsWith(prefix)) {
                    val rel = e.name.removePrefix(prefix)
                    if (rel.isNotEmpty()) {
                        val out = File(imguiDir, rel)
                        if (e.isDirectory) out.mkdirs() else { out.parentFile.mkdirs(); out.outputStream().use { zis.copyTo(it) } }
                    }
                }
                e = zis.nextEntry
            }
        }
    }
}

// Ensure the vendored ImGui tree exists before CMake configures.
tasks.configureEach { if (name.startsWith("pre") && name.endsWith("Build")) dependsOn(prepareImgui) }

android {
    namespace = "com.example.imguimenu"
    compileSdk = 35
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "com.example.imguimenu"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        externalNativeBuild { cmake { cppFlags += "-std=c++17" } }
    }

    buildTypes {
        release { isMinifyEnabled = false }
        debug { isMinifyEnabled = false }
    }

    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.31.6" } }
}

dependencies { implementation("androidx.core:core-ktx:1.17.0") }
