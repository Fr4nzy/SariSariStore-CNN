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
        gradlePluginPortal()
        maven { uri("https://jitpack.io").also { url = it } }
    }
}

rootProject.name = "SariSariStorePOS"
include(":app")
