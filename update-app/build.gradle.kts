plugins {
    java
}

dependencies {
    repositories {
        jcenter()
    }

    implementation(Lib.commons_io)
    implementation(Lib.annimon)
    implementation(Lib.univocity)
}

val updateBusStops by tasks.creating(JavaExec::class) {
    classpath(sourceSets["main"].runtimeClasspath)
    main = "fr.cph.chicago.update.UpdateBusStops"
    args("$buildDir/tmp/", "${parent!!.projectDir}/android-app/src/main/assets/")
}

val updateTrainStops by tasks.creating(JavaExec::class) {
    classpath(sourceSets["main"].runtimeClasspath)
    main = "fr.cph.chicago.update.UpdateTrainStops"
    args("$buildDir/tmp/", "${parent!!.projectDir}/android-app/src/main/assets/")
}

val update by tasks.creating {
    dependsOn(updateBusStops, updateTrainStops)
}
