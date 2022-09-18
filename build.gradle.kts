buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
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
        // Needed for ReKotlin
        maven(url = "https://jitpack.io")
    }
}
