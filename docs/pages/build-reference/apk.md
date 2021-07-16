---
title: Building APKs for Android emulators and devices
---

The default file format used when building Android apps with EAS Build is an [Android App Bundle](https://developer.android.com/platform/technology/app-bundle) (AAB / `.aab`). This format is optimized for distributing to the Google Play Store, but AABs can't be installed directly to your device. To install a build directly to your Android device or emulator, you need to build an [Android Package](https://en.wikipedia.org/wiki/Android_application_package) (APK / `.apk`) instead.

## Configuring a profile to build APKs

### Managed projects

By default, EAS Build produces Android App Bundle, you can change it by:
- setting `buildType` to `apk` or `development-client`
- setting `gradleCommand` to `:app:assembleRelease`, `:app:assembleDebug` or any other gradle command that produces APK

```json
{
  "builds": {
    "android": {
      "release": {},
      "preview": {
        "buildType": "apk"
      },
      "preview2": {
        "gradleCommand": ":app:assembleRelease"
      }
    }
  }
}
```

Now, to run your build run `eas build -p android --profile preview`. Remember that you can name the profile whatever you like; we named the profile "preview", but you could call it "local" or "simulator", whatever makes most sense for you.

## Installing your build

### Emulator ("Virtual device")

> If you haven't installed or run an Android emulator before, follow the [Android Studio emulator guide](/workflow/android-studio-emulator.md) before proceeding.

- Once your build is completed, download the APK from the build details page or the link provided when `eas build` is done.
- Open up your emulator.
- Drag the APK file into the emulator.
- The app will be installed in a few seconds. When it's complete, navigate to the app launcher, find the app icon and open it.
- You can share this build, it will run on any Android emulator or device.

### Physical device

#### Download directly to the device

- Once your build is completed, copy the URL to the APK from the build details page or the link provided when `eas build` is done.
- Send that URL to your device. Maybe by email? Up to you.
- Open the URL on your device, install the APK and run it.

#### Install with `adb`

- [Install adb](https://developer.android.com/studio/command-line/adb) if you don't have it installed already.
- Connect your device to your computer and [enable adb debugging on your device](https://developer.android.com/studio/command-line/adb#Enabling) if you haven't already.
- Once your build is completed, download the APK from the build details page or the link provided when `eas build` is done.
- Run `adb install path/to/the/file.apk`.
- Run the app on your device.
