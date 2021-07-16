# expo-cellular

Information about the user’s cellular service provider, such as its unique identifier, cellular connection type and whether it allows VoIP calls on its network.

# API documentation

- [Documentation for the master branch](https://github.com/expo/expo/blob/master/docs/pages/versions/unversioned/sdk/cellular.md)
- [Documentation for the latest stable release](https://docs.expo.io/versions/latest/sdk/cellular/)

# Installation in managed Expo projects

For managed [managed](https://docs.expo.io/versions/latest/introduction/managed-vs-bare/) Expo projects, please follow the installation instructions in the [API documentation for the latest stable release](https://docs.expo.io/versions/latest/sdk/cellular/).

# Installation in bare React Native projects

For bare React Native projects, you must ensure that you have [installed and configured the `react-native-unimodules` package](https://github.com/expo/expo/tree/master/packages/react-native-unimodules) before continuing.

### Add the package to your npm dependencies

```
expo install expo-cellular
```

### Configure for Android

This package requires the `android.permission.READ_PHONE_STATE` be added to your `AndroidManifest.xml`, this is used for `TelephonyManager` on Android. We **do not** require the more risky `READ_PRIVILEGED_PHONE_STATE` permission.

```xml
<!-- Added permissions -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

# Contributing

Contributions are very welcome! Please refer to guidelines described in the [contributing guide](https://github.com/expo/expo#contributing).
