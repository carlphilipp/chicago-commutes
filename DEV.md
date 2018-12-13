## Build

* Duplicate `keystore.properties.template` and rename the copy to `keystore.properties`. Update the new file with correct information.
* Do the same thing for `app.properties.template`.
* You are ready to build it!

Bump version:

`./gradlew bumpVersion`

Build debug APK:

`./gradlew clean assembleDebug`

Build prod APK:

`./gradlew clean build`

## F-Droid

Build F-Droid APK:

`./gradlew clean assembleFoss`

Build with F-Droid tools:

`fdroid checkupdates -v fr.cph.chicago.foss`
`fdroid build -v -l fr.cph.chicago.foss:<versionCode>`
`fdroid publish -v fr.cph.chicago.foss:<versionCode>`

## Issues

To fix the emulator not starting in arch linux: `ln -sf /usr/lib/libstdc++.so.6  $ANDROID_SDK/emulator/lib64/libstdc++`
