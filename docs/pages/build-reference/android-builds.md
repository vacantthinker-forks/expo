---
title: Android build process
---

This page describes the process of building Android projects with EAS Build. You may want to read this if you are interested in the implementation details of the build service.

## Build Process

Let's take a closer look at the steps for building Android projects with EAS Build. We'll first run some steps on your local machine to prepare the project, and then we'll actually build the project on a remote service.

### Local Steps

The first phase happens on your computer. EAS CLI is in charge of completing the following steps:

1. Check if the git index is clean - this means that there aren't any uncommitted changes. If it's not clean, an error is thrown. We use git to prepare a tarball of your project to upload to the build service.
1. Prepare the credentials needed for the build unless `builds.android.PROFILE_NAME.withoutCredentials` is set to `true`.

   - Depending on the value of `builds.android.PROFILE_NAME.credentialsSource`, the credentials are obtained from either the local `credentials.json` file or from the EAS servers. If the `remote` mode is selected but no credentials exist yet, you're prompted to generate a new keystore.

1. Additional step for **bare** projects: Check if the Android project is configured to be buildable on the EAS servers.

   > In this step, EAS CLI checks whether `android/app/build.gradle` contains `apply from: "./eas-build.gradle"`.
   > If the project is not configured, EAS CLI runs auto-configuration steps ([learn more below](#project-auto-configuration)).

1. Create the tarball containing a shallow clone of your local repository (`git clone --depth 1 ...`).
1. Upload the project tarball to a private AWS S3 bucket and send the build request to EAS Build.

### Remote Steps

Next, this is what happens when EAS Build picks up your request:

1. Create a new Docker container for the build.

   - Every build gets its own fresh container with all build tools installed there (Java JDK, Android SDK, NDK, and so on).

1. Download the project tarball from a private AWS S3 bucket and unpack it.
1. Run the `eas-build-pre-install` script from package.json if defined.
1. Run `yarn install` in the project root (or `npm install` if `yarn.lock` does not exist).
1. Additional steps for **managed** projects:
   - Run `expo prebuild` to convert the project to a bare one.
   - Configure the Android similarly to the step 3 from [Local Steps](#local-steps).
1. Restore a previously saved cache identified by the `cache.key` value in the build profile. ([Learn more](../build/eas-json/).)
1. Run the `eas-build-post-install` script from package.json if defined.
1. Restore the keystore (if it was included in the build request).
1. Run `./gradlew COMMAND` in the `android` directory inside your project.
   - `COMMAND` is the command defined in your `eas.json` at `builds.android.PROFILE_NAME.gradleCommand`. It defaults to `:app:bundleRelease` which produces the AAB (Android App Bundle).

1. Run the `eas-build-pre-upload-artifacts` script from package.json if defined.
1. Upload the build artifact to AWS S3.

   - The artifact path can be configured in `eas.json` at `builds.android.PROFILE_NAME.artifactPath`. It defaults to `android/app/build/outputs/**/*.{apk,aab}`. We're using the [fast-glob](https://github.com/mrmlnc/fast-glob#pattern-syntax) package for pattern matching.

1. Store a cache of files and directories defined in the build profile. `Podfile.lock` is cached by default. Subsequent builds will restore this cache. ([Learn more](../build/eas-json/).)

## Project Auto-Configuration

Every time you want to build a new Android app binary, we validate that the project is set up correctly so we can seamlessly run the build process on our servers. This mainly applies to bare projects, but similar steps are run when building managed projects.

### Android Keystore

Android requires you to sign your application with a certificate. That certificate is stored in your keystore. The Google Play Store identifies applications based on the certificate. This means that if you lose your keystore, you may not be able to update your application in the store. However, with [Play App Signing](https://developer.android.com/studio/publish/app-signing#app-signing-google-play), you can mitigate the risk of losing your keystore.

Your application's keystore should be kept private. **Under no circumstances should you check it in to your repository.** Debug keystores are the only exception because we don't use them for uploading apps to the Google Play Store.

### Configuring Gradle

Let's focus on building a release app binary. Like we previously mentioned, your app binary needs to be signed with the keystore. Since we're building the project on a remote server, we had to come up with a way to provide Gradle with credentials which aren't, for security reasons, checked in to your repository. When running `eas build:configure`, we're writing the `android/app/eas-build.gradle` file with the following contents:

<!-- prettier-ignore -->
```groovy
// Build integration with EAS

import java.nio.file.Paths

android {
  signingConfigs {
    release {
      // This is necessary to avoid needing the user to define a release signing config manually
      // If no release config is defined, and this is not present, build for assembleRelease will crash
    }
  }

  buildTypes {
    release {
      // This is necessary to avoid needing the user to define a release build type manually
    }
  }
}

def isEasBuildConfigured = false

tasks.whenTaskAdded {
  /* @info Don't read credentials.json if the task name contains debug */
  def debug = gradle.startParameter.taskNames.any { it.toLowerCase().contains('debug') }

  if (debug) {
    return
  }
  /* @end */

  // We need to configure EAS Build only once
  if (isEasBuildConfigured) {
    return
  }

  isEasBuildConfigured = true;

  /* @info This is where we configure the release signing config */android.signingConfigs.release/* @end */  {
    def credentialsJson = rootProject.file("../credentials.json");

    if (credentialsJson.exists()) {
      if (storeFile && !System.getenv("EAS_BUILD")) {
        println("Path to release keystore file is already set, ignoring 'credentials.json'")
      } else {
        try {
          def credentials = new groovy.json.JsonSlurper().parse(credentialsJson)
          def keystorePath = Paths.get(credentials.android.keystore.keystorePath);
          def storeFilePath = keystorePath.isAbsolute()
            ? keystorePath
            : rootProject.file("..").toPath().resolve(keystorePath);

          /* @info Use the data from credentials.json for the signing config */
          storeFile storeFilePath.toFile()
          storePassword credentials.android.keystore.keystorePassword
          keyAlias credentials.android.keystore.keyAlias
          if (credentials.android.keystore.containsKey("keyPassword")) {
            keyPassword credentials.android.keystore.keyPassword
          } else {
            // key password is required by Gradle, but PKCS keystores don't have one
            // using the keystore password seems to satisfy the requirement
            keyPassword credentials.android.keystore.keystorePassword
          } /* @end */

        } catch (Exception e) {
          println("An error occurred while parsing 'credentials.json': " + e.message)
        }
      }
    } else {
      if (storeFile == null) {
        println("Couldn't find a 'credentials.json' file, skipping release keystore configuration")
      }
    }
  }

  /* @info Use the above signing config for the release build type */
  android.buildTypes.release {
    signingConfig android.signingConfigs.release
  } /* @end */

}
```

The most important part is the `release` signing config. It's configured to read the keystore and passwords from the `credentials.json` file at the project root. Even though you're not required to create this file on your own, it's created and populated with your credentials by EAS Build before running the build.

This file is imported in `android/app/build.gradle` like this:

```groovy
// ...

apply from: "./eas-build.gradle"
```
