// @formatter:off
object Android {
    const val compile_sdk =             32
    const val min_sdk =                 21
    const val target_sdk =              32
    const val build_tools =             "32.0.0"
}

object Version {
    const val kotlin =                      "1.7.0"
    const val android_tools_build =         "7.1.2"
    const val realm =                       "10.11.0"
    const val gradle_version =              "0.42.0"

    const val androidx_activity_compose =   "1.6.0-alpha05"
    const val androidx_constraint =         "1.1.0-alpha03"
    const val androidx_lifecycle =          "2.5.0-rc01"
    const val androidx_navigation =         "2.5.0"
    const val androidx_work =               "2.8.0-alpha02"
    const val accompanist =                 "0.24.13-rc"
    const val commons_collections4 =        "4.+"
    const val commons_text =                "1.+"
    const val compose =                     "1.3.0-alpha01"
    const val compose_compiler =            "1.2.0"
    const val compose_material3 =           "1.0.0-alpha14"
    const val google_play_maps =            "18.0.2"
    const val google_play_location =        "19.0.1"
    const val google_maps_compose =         "2.1.1"
    const val hilt =                        "1.0.+"
    const val jackson =                     "2.13.1" // 2.13.2 is not working on android 7 (24)
    const val material =                    "1.7.0-alpha02"
    const val okhttp3 =                     "4.10.0-RC1"
    const val process_phoenix =             "2.1.+"
    const val rx =                          "3.+"
    const val rx_android =                  "3.+"
    const val rx_kotlin =                   "3.+"
    const val re_kotlin =                   "1.0.4"
    const val retrofit2 =                   "2.+"
    const val timber =                      "5.+"
    const val univocity =                   "2.+"

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

object Plugin {
    const val android_application =     "com.android.application"
    const val kotlin_android =          "org.jetbrains.kotlin.android"
    const val kotlin_apt =              "org.jetbrains.kotlin.kapt"
    const val versions =                "com.github.ben-manes.versions"
}

object Lib {
    // Build
    const val gradle_realm =                    "io.realm:realm-gradle-plugin:${Version.realm}"

    // Android app
    const val androidx_activity_compose =       "androidx.activity:activity-compose:${Version.androidx_activity_compose}"
    const val androidx_constraint =             "androidx.constraintlayout:constraintlayout-compose:${Version.androidx_constraint}"
    const val androidx_lifecycle_compose =      "androidx.lifecycle:lifecycle-viewmodel-compose:${Version.androidx_lifecycle}"
    const val androidx_lifecycle_ktc =          "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.androidx_lifecycle}"
    const val androidx_navigation_compose =     "androidx.navigation:navigation-compose:${Version.androidx_navigation}"
    const val androidx_navigation_runtime =     "androidx.navigation:navigation-runtime-ktx:${Version.androidx_navigation}"
    const val androidx_work =                   "androidx.work:work-runtime-ktx:${Version.androidx_work}"
    const val accompanist_theme =               "com.google.accompanist:accompanist-appcompat-theme:${Version.accompanist}"
    const val accompanist_swiperefresh =        "com.google.accompanist:accompanist-swiperefresh:${Version.accompanist}"
    const val accompanist_systemui =            "com.google.accompanist:accompanist-systemuicontroller:${Version.accompanist}"
    const val accompanist_animation =           "com.google.accompanist:accompanist-navigation-animation:${Version.accompanist}"
    const val accompanist_pager =               "com.google.accompanist:accompanist-pager:${Version.accompanist}"
    const val accompanist_permissions =          "com.google.accompanist:accompanist-permissions:${Version.accompanist}"
    const val commons_collections4 =            "org.apache.commons:commons-collections4:${Version.commons_collections4}"
    const val commons_text =                    "org.apache.commons:commons-text:${Version.commons_text}"
    const val compose_animation =               "androidx.compose.animation:animation:${Version.compose}"
    const val compose_foundation =              "androidx.compose.foundation:foundation:${Version.compose}"
    const val compose_foundation_layout =       "androidx.compose.foundation:foundation-layout:${Version.compose}"
    const val compose_material =                "androidx.compose.material:material:${Version.compose}"
    const val compose_material_icon_core =      "androidx.compose.material:material-icons-core:${Version.compose}"
    const val compose_material_icon_extended =  "androidx.compose.material:material-icons-extended:${Version.compose}"
    const val compose_runtime =                 "androidx.compose.runtime:runtime:${Version.compose}"
    const val compose_ui =                      "androidx.compose.ui:ui:${Version.compose}"
    const val compose_ui_tooling =              "androidx.compose.ui:ui-tooling:${Version.compose}"
    const val compose_material3 =               "androidx.compose.material3:material3:${Version.compose_material3}"
    const val hilt_work =                       "androidx.hilt:hilt-work:${Version.hilt}"
    const val kotlin =                          "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Version.kotlin}"
    const val kotlin_reflect =                  "org.jetbrains.kotlin:kotlin-reflect:${Version.kotlin}"
    const val google_services_map =             "com.google.android.gms:play-services-maps:${Version.google_play_maps}"
    const val google_services_location =        "com.google.android.gms:play-services-location:${Version.google_play_location}"
    const val google_maps_compose =             "com.google.maps.android:maps-compose:${Version.google_maps_compose}"
    const val material =                        "com.google.android.material:material:${Version.material}"
    const val jackson_core =                    "com.fasterxml.jackson.core:jackson-core:${Version.jackson}"
    const val jackson_databind =                "com.fasterxml.jackson.core:jackson-databind:${Version.jackson}"
    const val jackson_annotations =             "com.fasterxml.jackson.core:jackson-annotations:${Version.jackson}"
    const val jackson_module_kotlin =           "com.fasterxml.jackson.module:jackson-module-kotlin:${Version.jackson}"
    const val okhttp3 =                         "com.squareup.okhttp3:okhttp:${Version.okhttp3}"
    const val okhttp3_logging =                 "com.squareup.okhttp3:logging-interceptor:${Version.okhttp3}"
    const val process_phoenix =                 "com.jakewharton:process-phoenix:${Version.process_phoenix}"
    const val univocity =                       "com.univocity:univocity-parsers:${Version.univocity}"
    const val rx =                              "io.reactivex.rxjava3:rxjava:${Version.rx}"
    const val rx_android =                      "io.reactivex.rxjava3:rxandroid:${Version.rx_android}"
    const val rx_kotlin =                       "io.reactivex.rxjava3:rxkotlin:${Version.rx_kotlin}"
    const val re_kotlin =                       "com.github.ReKotlin:ReKotlin:${Version.re_kotlin}"
    const val retrofit2 =                       "com.squareup.retrofit2:retrofit:${Version.retrofit2}"
    const val retrofit2_converter =             "com.squareup.retrofit2:converter-jackson:${Version.retrofit2}"
    const val retrofit2_rxjava3 =               "com.squareup.retrofit2:adapter-rxjava3:${Version.retrofit2}"
    const val timber =                          "com.jakewharton.timber:timber:${Version.timber}"

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
