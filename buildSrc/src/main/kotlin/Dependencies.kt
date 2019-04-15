object Android {
    val compile_sdk = 28
    val build_tools = "28.0.3"
    val min_sdk = 21
    val target_sdk = 28
}

object Version {
    val kotlin = "1.3.20"
    val android_tools_build = "3.3.2"
    val realm = "5.10.0"

    val butterknife = "10.1.0"
    val commons_collections4 = "4.1"
    val commons_text = "1.1"
    val easypermissions = "1.3.0"
    val google_play_maps = "16.1.0"
    val google_play_location = "16.0.0"
    val jackson = "2.9.8"
    val material = "1.0.0"
    val mapbox = "6.5.0" // TODO: Migrate to last mapbox version
    val mapbox_location = "0.10.0"
    val photoview = "v2.0.0"
    val rx = "2.2.7"
    val rx_android = "2.1.1"
    val sliding_up_panel = "3.4.0"
    val univocity = "2.8.1"

    // Update
    val commons_io = "2.5"
    val annimon = "1.1.9"

    // Test
    val junit = "4.12"
    val mockito = "2.10.0"
    val hamcrest = "1.3"
}

object Lib {
    // Build https://dl.google.com/dl/android/maven2/index.html
    val gradle_android = "com.android.tools.build:gradle:${Version.android_tools_build}"
    val gradle_realm = "io.realm:realm-gradle-plugin:${Version.realm}"
    val gradle_kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}"

    // Android app
    val butterknife = "com.jakewharton:butterknife:${Version.butterknife}"
    val butterknife_compiler = "com.jakewharton:butterknife-compiler:${Version.butterknife}"
    val commons_collections4 = "org.apache.commons:commons-collections4:${Version.commons_collections4}"
    val commons_text = "org.apache.commons:commons-text:${Version.commons_text}"
    val easypermissions = "pub.devrel:easypermissions:${Version.easypermissions}"
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.kotlin}"
    val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Version.kotlin}"
    val google_services_map = "com.google.android.gms:play-services-maps:${Version.google_play_maps}"
    val google_services_location = "com.google.android.gms:play-services-location:${Version.google_play_location}"
    val material = "com.google.android.material:material:${Version.material}"
    val jackson_core = "com.fasterxml.jackson.core:jackson-core:${Version.jackson}"
    val jackson_databind = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    val jackson_annotations = "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    val jackson_module_kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}"
    val mapbox = "com.mapbox.mapboxsdk:mapbox-android-sdk:${Version.mapbox}"
    val mapbox_location = "com.mapbox.mapboxsdk:mapbox-android-plugin-locationlayer:${Version.mapbox_location}"
    val photoview = "com.github.chrisbanes:PhotoView:${Version.photoview}"
    val univocity = "com.univocity:univocity-parsers:${Version.univocity}"
    val rx = "io.reactivex.rxjava2:rxjava:${Version.rx}"
    val rx_android = "io.reactivex.rxjava2:rxandroid:${Version.rx_android}"
    val sliding_up_panel = "com.sothree.slidinguppanel:library:${Version.sliding_up_panel}"

    // Test
    val junit = "junit:junit:${Version.junit}"
    val mockito = "org.mockito:mockito-core:${Version.mockito}"
    val hamcrest = "org.hamcrest:hamcrest-all:${Version.hamcrest}"

    // Update
    val commons_io = "commons-io:commons-io:${Version.commons_io}"
    val annimon = "com.annimon:stream:${Version.annimon}"
}
