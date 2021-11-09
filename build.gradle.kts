buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath(Lib.gradle_android)
        classpath(Lib.gradle_realm)
        classpath(Lib.gradle_kotlin)
        classpath(Lib.gradle_kotlin)
        classpath(Lib.gradle_versions)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // Need to migrate to new version of Mapbox to remove that
        // Needed for PhotoView
        maven(url = "https://jitpack.io")
    }
}
