buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // TODO migrate to room https://developer.android.com/jetpack/androidx/releases/room
        classpath(Lib.gradle_realm)
    }
}

// @formatter:off
plugins {
    id("com.android.application")       version Version.android_tools_build     apply false
    id("org.jetbrains.kotlin.android")  version Version.kotlin                  apply false
    id("org.jetbrains.kotlin.kapt")     version Version.kotlin                  apply false
    id("com.github.ben-manes.versions") version Version.gradle_version          apply false
}
// @formatter:on

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // Need to migrate to new version of Mapbox to remove that
        // Needed for PhotoView
        maven(url = "https://jitpack.io")
    }
}

