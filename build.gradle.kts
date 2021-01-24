buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
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
        jcenter()
        // Needed for PhotoView
        maven(url = "https://jitpack.io")
    }
}
