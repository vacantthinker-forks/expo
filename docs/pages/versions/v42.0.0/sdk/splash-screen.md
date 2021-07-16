---
title: SplashScreen
sourceCodeUrl: 'https://github.com/expo/expo/tree/sdk-42/packages/expo-splash-screen'
---

import APISection from '~/components/plugins/APISection';
import InstallSection from '~/components/plugins/InstallSection';
import PlatformsSection from '~/components/plugins/PlatformsSection';

The `SplashScreen` module tells the splash screen to remain visible until it has been explicitly told to hide. This is useful to do some work behind the scenes before displaying your app (eg: make API calls) and to animated your splash screen (eg: fade out or slide away, or switch from a static splash screen to an animated splash screen).

Read more about [creating a splash screen image](../../../guides/splash-screens.md), or [quickly generate an icon and splash screen on the web](https://buildicon.netlify.app/)

<PlatformsSection android emulator ios simulator />

## Installation

<InstallSection packageName="expo-splash-screen" />

## Usage

This example shows how to keep the splash screen visible while loading app resources and then hide the splash screen when the app has rendered some initial content.

```js
import React, { useCallback, useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Entypo } from '@expo/vector-icons';
import * as SplashScreen from 'expo-splash-screen';
import * as Font from 'expo-font';

export default function App() {
  const [appIsReady, setAppIsReady] = useState(false);

  useEffect(() => {
    async function prepare() {
      try {
        // Keep the splash screen visible while we fetch resources
        await SplashScreen.preventAutoHideAsync();
        // Pre-load fonts, make any API calls you need to do here
        await Font.loadAsync(Entypo.font);
        // Artificially delay for two seconds to simulate a slow loading
        // experience. Please remove this if you copy and paste the code!
        await new Promise(resolve => setTimeout(resolve, 2000));
      } catch (e) {
        console.warn(e);
      } finally {
        // Tell the application to render
        setAppIsReady(true);
      }
    }

    prepare();
  }, []);

  const onLayoutRootView = useCallback(async () => {
    if (appIsReady) {
      // This tells the splash screen to hide immediately! If we call this after
      // `setAppIsReady`, then we may see a blank screen while the app is
      // loading its initial state and rendering its first pixels. So instead,
      // we hide the splash screen once we know the root view has already
      // performed layout.
      await SplashScreen.hideAsync();
    }
  }, [appIsReady]);

  if (!appIsReady) {
    return null;
  }

  return (
    <View
      style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}
      onLayout={onLayoutRootView}>
      <Text>SplashScreen Demo! 👋</Text>
      <Entypo name="rocket" size={30} />
    </View>
  );
}
```

### Animating the splash screen

Refer to the [with-splash-screen example](https://github.com/expo/examples/tree/master/with-splash-screen) to see how to apply any arbitrary animations to your splash screen, such as a fade out. You can initialize a new project from this example by running `npx create-react-native-app -t with-splash-screen`.

## API

```js
import * as SplashScreen from 'expo-splash-screen';
```

<APISection packageName="expo-splash-screen" apiName="SplashScreen" />