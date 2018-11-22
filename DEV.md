## Build

* Duplicate `keystore.properties.template` and rename the copy to `keystore.properties`. Update the new file with correct information.
* Do the same thing for `app.properties.template`.
* You are ready to build it!

Build debug APK:

`./gradlew clean assembleDebug`

Build prod APK:

`./gradlew clean build`

Build Fdroid APK:

`./gradlew clean assembleFoss`


## Issues

To fix the emulator not starting in arch linux: `ln -sf /usr/lib/libstdc++.so.6  $ANDROID_SDK/emulator/lib64/libstdc++`
