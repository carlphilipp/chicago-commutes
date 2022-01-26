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
    id(Plugin.android_application)  version Version.android_tools_build     apply false
    id(Plugin.kotlin_android)       version Version.kotlin                  apply false
    id(Plugin.kotlin_apt)           version Version.kotlin                  apply false
    id(Plugin.versions)             version Version.gradle_version          apply false
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

