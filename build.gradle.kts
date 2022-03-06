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
        jcenter() // TODO: Need to replace/remove redux as it's only in jcenter :( https://github.com/ReKotlin/ReKotlin/issues/47
        // Needed for PhotoView
        maven(url = "https://jitpack.io")
    }
}
