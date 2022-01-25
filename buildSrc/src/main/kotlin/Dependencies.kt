// @formatter:off
object Android {
    const val compile_sdk =             30
    const val min_sdk =                 21
    const val target_sdk =              30
    const val build_tools =             "30.0.2"
}

object Version {
    const val kotlin =                  "1.+"
    const val android_tools_build =     "7.0.3"
    const val realm =                   "10.+"
    const val gradle_version =          "0.36.0"

    const val commons_collections4 =    "4.+"
    const val commons_text =            "1.+"
    const val easypermissions =         "3.0.0"
    const val google_play_maps =        "17.0.0"
    const val google_play_location =    "17.1.0"
    const val jackson =                 "2.+"
    const val material =                "1.3.0"
    const val mapbox =                  "9.2.1"
    const val mapbox_annotation =       "0.7.0"
    const val mapbox_marker_view =      "0.3.0"
    const val mapbox_location =         "0.11.0"
    const val mapbox_telemetry =        "6.2.2"
    const val okhttp3 =                 "4.10.0-RC1"
    const val photoview =               "2.3.0"
    const val rx =                      "3.+"
    const val rx_android =              "3.+"
    const val rx_kotlin =               "3.+"
    const val re_kotlin =               "1.+"
    const val retrofit2 =               "2.+"
    const val sliding_up_panel =        "3.4.0"
    const val timber =                  "4.+"
    const val univocity =               "2.+"

    // Update
    const val commons_io =              "2.+"
    const val annimon =                 "1.+"

    // Test
    const val junit =                   "4.13.1"
    const val mockito =                 "3.7.7"
    const val hamcrest =                "1.3"

    // Debug
    const val leakcanary =              "2.6"
}

object Lib {
    // Build
    const val gradle_android =              "com.android.tools.build:gradle:${Version.android_tools_build}"
    const val gradle_realm =                "io.realm:realm-gradle-plugin:${Version.realm}"
    const val gradle_kotlin =               "org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}"
    const val gradle_versions =             "com.github.ben-manes:gradle-versions-plugin:${Version.gradle_version}"

    // Android app
    const val commons_collections4 =        "org.apache.commons:commons-collections4:${Version.commons_collections4}"
    const val commons_text =                "org.apache.commons:commons-text:${Version.commons_text}"
    const val easypermissions =             "pub.devrel:easypermissions:${Version.easypermissions}"
    const val kotlin =                      "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.kotlin}"
    const val kotlin_reflect =              "org.jetbrains.kotlin:kotlin-reflect:${Version.kotlin}"
    const val google_services_map =         "com.google.android.gms:play-services-maps:${Version.google_play_maps}"
    const val google_services_location =    "com.google.android.gms:play-services-location:${Version.google_play_location}"
    const val material =                    "com.google.android.material:material:${Version.material}"
    const val jackson_core =                "com.fasterxml.jackson.core:jackson-core:${Version.jackson}"
    const val jackson_databind =            "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    const val jackson_annotations =         "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    const val jackson_module_kotlin =       "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}"
    const val mapbox =                      "com.mapbox.mapboxsdk:mapbox-android-sdk:${Version.mapbox}"
    const val mapbox_annotation =           "com.mapbox.mapboxsdk:mapbox-android-plugin-annotation-v8:${Version.mapbox_annotation}"
    const val mapbox_marker_view =          "com.mapbox.mapboxsdk:mapbox-android-plugin-markerview-v8:${Version.mapbox_marker_view}"
    const val mapbox_location =             "com.mapbox.mapboxsdk:mapbox-android-plugin-locationlayer:${Version.mapbox_location}"
    const val mapbox_telemetry =            "com.mapbox.mapboxsdk:mapbox-android-telemetry:${Version.mapbox_telemetry}"
    const val okhttp3 =                     "com.squareup.okhttp3:okhttp:${Version.okhttp3}"
    const val okhttp3_logging =             "com.squareup.okhttp3:logging-interceptor:${Version.okhttp3}"
    const val photoview =                   "com.github.chrisbanes:PhotoView:${Version.photoview}"
    const val univocity =                   "com.univocity:univocity-parsers:${Version.univocity}"
    const val rx =                          "io.reactivex.rxjava3:rxjava:${Version.rx}"
    const val rx_android =                  "io.reactivex.rxjava3:rxandroid:${Version.rx_android}"
    const val rx_kotlin =                   "io.reactivex.rxjava3:rxkotlin:${Version.rx_kotlin}"
    const val re_kotlin =                   "org.rekotlin:rekotlin:${Version.re_kotlin}"
    const val retrofit2 =                   "com.squareup.retrofit2:retrofit:${Version.retrofit2}"
    const val retrofit2_converter =         "com.squareup.retrofit2:converter-jackson:${Version.retrofit2}"
    const val retrofit2_rxjava3 =           "com.squareup.retrofit2:adapter-rxjava3:${Version.retrofit2}"
    const val sliding_up_panel =            "com.sothree.slidinguppanel:library:${Version.sliding_up_panel}"
    const val timber =                      "com.jakewharton.timber:timber:${Version.timber}"

    // Test
    const val junit =                       "junit:junit:${Version.junit}"
    const val mockito =                     "org.mockito:mockito-core:${Version.mockito}"
    const val hamcrest =                    "org.hamcrest:hamcrest-all:${Version.hamcrest}"

    // Update
    const val commons_io =                  "commons-io:commons-io:${Version.commons_io}"
    const val annimon =                     "com.annimon:stream:${Version.annimon}"

    // Debug
    const val leakcanary =                  "com.squareup.leakcanary:leakcanary-android:${Version.leakcanary}"
}
// @formatter:on
