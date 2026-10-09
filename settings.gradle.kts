pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://api.rikka.app/repository/maven-public/") } // Shizuku repo
    }
}
rootProject.name = "Auto Tap Macro"
include(":app")
