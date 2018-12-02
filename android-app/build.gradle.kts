import com.android.ide.common.repository.main
import org.jetbrains.kotlin.resolve.calls.components.InferenceSession.Companion.default
import java.util.Properties
import java.io.FileInputStream
import kotlin.collections.listOf
import java.io.FileWriter
import java.io.BufferedWriter

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("realm-android")
}

if (JavaVersion.current() != JavaVersion.VERSION_1_8) {
    throw GradleException("This build must be run with java 8")
}

android {
    compileSdkVersion(Version.compileSdkVersion)
    buildToolsVersion(Version.buildToolsVersion)

    val version = version()

    defaultConfig {
        applicationId = Version.applicationId
        versionCode = Integer.parseInt(version.getProperty("version.code"))
        versionName = version.getProperty("version.name")

        minSdkVersion(Version.minSdkVersion)
        targetSdkVersion(Version.targetSdkVersion)
        multiDexEnabled = true

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        val props = properties()

        resValue("string", "google_maps_api_key", props.getOrDefault("google.streetmap.key", "") as String)
        resValue("string", "cta_train_key", props.getOrDefault("cta.train.key", "") as String)
        resValue("string", "cta_bus_key", props.getOrDefault("cta.bus.key", "") as String)

        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = false
            }
        }
    }

    compileOptions {
        val javaVersion = JavaVersion.VERSION_1_8
        setSourceCompatibility(javaVersion)
        setTargetCompatibility(javaVersion)
    }

    signingConfigs {
        create("release") {
            val propsFile = file("keystore.properties")
            if (propsFile.exists() && gradle.startParameter.taskNames.any {
                    it.contains("Release") || it.contains("build")
                }) {
                val props = Properties()
                props.load(FileInputStream(propsFile))
                val storeF = File(props["storeFile"] as String)

                storeFile = storeF
                storePassword = String(System.console().readPassword("\n\$ Enter keystore password: "))
                keyAlias = props["keyAlias"] as String
                keyPassword = String(System.console().readPassword("\n\$ Enter alias password: "))
            } else {
                storeFile = file("default")
                storePassword = "default"
                keyAlias = "default"
                keyPassword = "default"
            }
        }
    }

    buildTypes {
        getByName("release") {
            // FIXME: this should be turned on (to delete if it works)
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt")
            if (signingConfigs["release"] != null) {
                if (signingConfigs["release"].storePassword != "default") {
                    signingConfig = signingConfigs["release"]
                }
            }
        }
    }

    flavorDimensions("env")

    productFlavors {
        create("googleplay") {
            dimension = "env"
        }
        create("foss") {
            dimension = "env"
            applicationIdSuffix = ".foss"
            resValue("string", "mapbox_token", properties().getOrDefault("mapbox.token", "") as String)
        }
    }

    sourceSets.getByName("main").java.srcDir("src/main/kotlin")
    sourceSets.getByName("googleplay").java.srcDir("src/googleplay/kotlin")
    sourceSets.getByName("foss").java.srcDir("src/foss/kotlin")

    lintOptions {
        isCheckReleaseBuilds = true
        isAbortOnError = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES.txt")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/LICENSE")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/notice.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/dependencies.txt")
        exclude("META-INF/LGPL2.1")
    }
}

dependencies {
    // Kotlin
    implementation(Lib.kotlin)
    implementation(Lib.kotlin_reflect)

    // Google
    implementation(Lib.google_support_design)
    implementation(Lib.google_support_compat)
    implementation(Lib.google_support_vector)
    implementation(Lib.google_support_media_compat)
    implementation(Lib.google_support_v4)

    // Jackson
    implementation(Lib.jackson_core)
    implementation(Lib.jackson_databind)
    implementation(Lib.jackson_annotations)
    implementation(Lib.jackson_module_kotlin)

    // RxJava
    implementation(Lib.rx)
    implementation(Lib.rx_android)

    // Tools
    implementation(Lib.butterknife)
    implementation(Lib.commons_collections4)
    implementation(Lib.commons_text)
    implementation(Lib.sliding_up_panel)
    implementation(Lib.univocity)
    implementation(Lib.easypermissions)
    implementation(Lib.photoview)

    val googleplayImplementation by configurations
    dependencies {
        googleplayImplementation(Lib.google_services_map)
        googleplayImplementation(Lib.google_services_location)
    }

    val fossImplementation by configurations
    dependencies {
        fossImplementation(Lib.mapbox)
        fossImplementation(Lib.mapbox_location)
    }

    kapt(Lib.butterknife_compiler)

    testImplementation(Lib.junit) { exclude(group = "org.hamcrest", module = "hamcrest-core") }
    testImplementation(Lib.mockito) { exclude(group = "org.hamcrest", module = "hamcrest-core") }
    testImplementation(Lib.hamcrest)
}

val bumpVersion by tasks.creating {
    doLast {
        val props = Properties()
        val version: File = file("version.properties")
        props.load(FileInputStream(version))
        props["version.code"] = (Integer.parseInt(props["version.code"] as String) + 1).toString()

        val type = System.console().readLine("\n\$ Bump type? [0: Bug fix | 1: Feature | 2: Major]")
        val versionName = (props["version.name"] as String).split(".")
        var newVersionName = ""
        when (type) {
            "0" -> newVersionName = versionName[0] + "." + versionName[1] + "." + (Integer.parseInt(versionName[2]) + 1)
            "1" -> newVersionName = versionName[0] + "." + (Integer.parseInt(versionName[1]) + 1) + ".0"
            "2" -> {
                val majorVersion = (Integer.parseInt(versionName[0]) + 1)
                newVersionName = "$majorVersion.0.0"
            }
            else -> AssertionError("Wrong type [$type]")
        }
        props["version.name"] = newVersionName
        props.store(BufferedWriter(FileWriter(version)), null)
    }
}

val currentVersion by tasks.creating {
    doLast {
        val props = Properties()
        val version = file("version.properties")
        props.load(FileInputStream(version))
        println("Version name: " + props["version.name"])
        println("Version code: " + props["version.code"])
        println("FDroid tag: " + props["version.name"] + "-fdroid")
    }
}

bumpVersion.finalizedBy(currentVersion)

fun properties(): Properties {
    val props = Properties()
    val appProperties = this.file("app.properties")
    if (appProperties.exists()) {
        props.load(FileInputStream(appProperties))
    } else {
        println("No app.properties found")
    }
    return props
}

fun version(): Properties {
    val props = Properties()
    val version = this.file("version.properties")
    props.load(FileInputStream(version))
    return props
}
