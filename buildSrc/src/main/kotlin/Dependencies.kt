object Android {
    const val compile_sdk = 28
    const val build_tools = "28.0.3"
    const val min_sdk = 21
    const val target_sdk = 28
}

object Version {
    const val kotlin = "1.3.31"
    const val android_tools_build = "3.4.1"
    const val realm = "5.11.0"

    const val butterknife = "10.1.0"
    const val commons_collections4 = "4.1"
    const val commons_text = "1.1"
    const val easypermissions = "1.3.0"
    const val google_play_maps = "16.1.0"
    const val google_play_location = "16.0.0"
    const val jackson = "2.9.9"
    const val material = "1.0.0"
    const val mapbox = "6.5.0" // TODO: Migrate to last mapbox version https://github.com/mapbox/mapbox-gl-native/wiki/Android-6.x-to-7.x-migration-guide
    const val mapbox_location = "0.10.0"
    const val photoview = "v2.0.0"
    const val rx = "2.2.8"
    const val rx_android = "2.1.1"
    const val rx_kotlin = "2.3.0"
    const val re_kotlin = "1.0.3"
    const val sliding_up_panel = "3.4.0"
    const val timber = "4.7.1"
    const val univocity = "2.8.2"

    // Update
    const val commons_io = "2.5"
    const val annimon = "1.1.9"

    // Test
    const val junit = "4.12"
    const val mockito = "2.10.0"
    const val hamcrest = "1.3"
}

object Lib {
    // Build https://dl.google.com/dl/android/maven2/index.html
    const val gradle_android = "com.android.tools.build:gradle:${Version.android_tools_build}"
    const val gradle_realm = "io.realm:realm-gradle-plugin:${Version.realm}"
    const val gradle_kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}"

    // Android app
    const val butterknife = "com.jakewharton:butterknife:${Version.butterknife}"
    const val butterknife_compiler = "com.jakewharton:butterknife-compiler:${Version.butterknife}"
    const val commons_collections4 = "org.apache.commons:commons-collections4:${Version.commons_collections4}"
    const val commons_text = "org.apache.commons:commons-text:${Version.commons_text}"
    const val easypermissions = "pub.devrel:easypermissions:${Version.easypermissions}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.kotlin}"
    const val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Version.kotlin}"
    const val google_services_map = "com.google.android.gms:play-services-maps:${Version.google_play_maps}"
    const val google_services_location = "com.google.android.gms:play-services-location:${Version.google_play_location}"
    const val material = "com.google.android.material:material:${Version.material}"
    const val jackson_core = "com.fasterxml.jackson.core:jackson-core:${Version.jackson}"
    const val jackson_databind = "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    const val jackson_annotations = "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    const val jackson_module_kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}"
    const val mapbox = "com.mapbox.mapboxsdk:mapbox-android-sdk:${Version.mapbox}"
    const val mapbox_location = "com.mapbox.mapboxsdk:mapbox-android-plugin-locationlayer:${Version.mapbox_location}"
    const val photoview = "com.github.chrisbanes:PhotoView:${Version.photoview}"
    const val univocity = "com.univocity:univocity-parsers:${Version.univocity}"
    const val rx = "io.reactivex.rxjava2:rxjava:${Version.rx}"
    const val rx_android = "io.reactivex.rxjava2:rxandroid:${Version.rx_android}"
    const val rx_kotlin = "io.reactivex.rxjava2:rxkotlin:${Version.rx_kotlin}"
    const val re_kotlin = "org.rekotlin:rekotlin:${Version.re_kotlin}"
    const val sliding_up_panel = "com.sothree.slidinguppanel:library:${Version.sliding_up_panel}"
    const val timber = "com.jakewharton.timber:timber:${Version.timber}"

    // Test
    const val junit = "junit:junit:${Version.junit}"
    const val mockito = "org.mockito:mockito-core:${Version.mockito}"
    const val hamcrest = "org.hamcrest:hamcrest-all:${Version.hamcrest}"

    // Update
    const val commons_io = "commons-io:commons-io:${Version.commons_io}"
    const val annimon = "com.annimon:stream:${Version.annimon}"
}
