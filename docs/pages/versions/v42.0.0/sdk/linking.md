---
title: Linking
sourceCodeUrl: 'https://github.com/expo/expo/tree/sdk-42/packages/expo/src/Linking'
---

import InstallSection from '~/components/plugins/InstallSection';
import PlatformsSection from '~/components/plugins/PlatformsSection';

`expo-linking` provides utilities for your app to interact with other installed apps using deep links. It also provides helper methods for constructing and parsing deep links into your app. This module is an extension of the React Native [Linking](https://reactnative.dev/docs/linking.html) module.

For a more comprehensive explanation of how to use `expo-linking`, refer to the [Linking guide](../../../guides/linking.md).

<PlatformsSection android emulator ios simulator web />

## Installation

<InstallSection packageName="expo-linking" />

## API

```js
import * as Linking from 'expo-linking';
```

## Methods

### `Linking.addEventListener(type, handler)`

Add a handler to `Linking` changes by listening to the `url` event type and providing the handler. It is recommended to use the [useURL() hook](#linkinguseurl) instead.

#### Arguments

- **type (_string_)** -- The only valid type is `'url'`.
- **handler (_function_)** -- A function that takes an `event` object of the type `{ url: string }`.

### `Linking.canOpenURL(url)`

Determine whether or not an installed app can handle a given URL.

#### Arguments

- **url (_string_)** -- The URL that you want to test can be opened.

#### Returns

A `Promise` object that is fulfilled with `true` if the URL can be handled, otherwise it `false` if not.

The `Promise` will reject on Android if it was impossible to check if the URL can be opened, and on iOS if you didn't [add the specific scheme in the `LSApplicationQueriesSchemes` key inside `Info.plist`](../../../guides/linking.md##opening-links-to-other-apps).

### `Linking.createURL(path, options)`

Helper method for constructing a deep link into your app, given an optional path and set of query parameters. Creates a URI scheme with two slashes by default.

#### Arguments

- **path (_string_)** -- Any path into your app.
- **options**:
  - **queryParams (_object_)** -- An object with a set of query parameters. These will be merged with any Expo-specific parameters that are needed (e.g. release channel) and then appended to the url as a query string.
  - **scheme (_string_)** -- Optional URI protocol to use in the URL `<scheme>://`, when undefined the scheme will be chosen from the Expo config (app.config.js or app.json).

#### Returns

A URL string which points to your app with the given deep link information.

### `Linking.getInitialURL()`

Get the URL that was used to launch the app if it was launched by a link.

#### Returns

The URL string that launched your app, or `null`.

### `Linking.makeUrl(path, options, scheme)`

> An alias for `Linking.createURL()`. This method is deprecated and will be removed in a future SDK version.

Helper method for constructing a deep link into your app, given an optional path and set of query parameters. Creates a URI scheme with three slashes for legacy purposes.

#### Arguments

- **path (_string_)** -- Any path into your app.
- **queryParams (_object_)** -- An object with a set of query parameters. These will be merged with any Expo-specific parameters that are needed (e.g. release channel) and then appended to the url as a query string.
- **scheme (_string_)** -- Optional URI protocol to use in the URL `<scheme>:///`, when undefined the scheme will be chosen from the Expo config (app.config.js or app.json).

#### Returns

A URL string which points to your app with the given deep link information.

### `Linking.openSettings()`

Open the operating system settings app and displays the app’s custom settings, if it has any.

### `Linking.openURL(url)`

Attempt to open the given URL with an installed app. See the [Linking guide](../../../guides/linking.md) for more information.

#### Arguments

- **url (_string_)** -- A URL for the operating system to open, eg: `tel:5555555`, `exp://`.

#### Returns

A `Promise` that is fulfilled with `true` if the link is opened operating system automatically or the user confirms the prompt to open the link. The `Promise` rejects if there are no applications registered for the URL or the user cancels the dialog.

### `Linking.parse(url)`

Helper method for parsing out deep link information from a URL.

#### Arguments

- **url (_string_)** -- A URL that points to the currently running experience (e.g. an output of `Linking.createURL()`).

#### Returns

An object with the following keys:

- **path (_string_)** -- The path into the app specified by the url.
- **queryParams (_object_)** -- The set of query parameters specified by the query string of the url.

### `Linking.parseInitialURLAsync()`

Helper method which wraps React Native's `Linking.getInitialURL()` in `Linking.parse()`. Parses the deep link information out of the URL used to open the experience initially.

#### Returns

A promise that resolves to an object with the following keys:

- **path (_string_)** -- The path specified by the url used to open the app.
- **queryParams (_object_)** -- The set of query parameters specified by the query string of the url used to open the app.

### `Linking.removeEventListener(type, handler)`

Remove a handler by passing the `url` event type and the handler.

#### Arguments

- **type (_string_)** -- The only valid type is `'url'`.
- **handler (_function_)** -- A function that takes an `event` object of the type `{ url: string }`. This handler should be the same function that you passed in to `Linking.addEventListener`.

### `Linking.sendIntent(action, extras)`

**Android only.** Launch an Android intent with extras. Use [IntentLauncher](../intent-launcher.md) instead, `sendIntent` is only included in `Linking` for API compatibility with React Native's Linking API.

#### Arguments

- **action (_string_)** - The intent action, eg: `'android.settings.ACCESSIBILITY_SETTINGS'`.
- **extras (_array_)** - Extra data to pass in to the intent, each item in the area must be of type `{ key: string, value: string|number|boolean } `.

## Hooks

### `Linking.useURL()`

Returns the initial URL followed by any subsequent changes to the URL.

### `Linking.useUrl()`

> An alias for `Linking.useURL()`. This method is deprecated and will be removed in a future SDK version.**

Returns the initial URL followed by any subsequent changes to the URL.