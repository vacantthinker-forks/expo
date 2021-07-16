---
title: Speech
sourceCodeUrl: 'https://github.com/expo/expo/tree/master/packages/expo-speech'
---

import APISection from '~/components/plugins/APISection';
import InstallSection from '~/components/plugins/InstallSection';
import PlatformsSection from '~/components/plugins/PlatformsSection';
import SnackInline from '~/components/plugins/SnackInline';

**`expo-speech`** provides an API that allows you to utilize Text-to-speech functionality in your app.

<PlatformsSection android emulator ios simulator web />

## Installation

<InstallSection packageName="expo-speech" />

## Usage

<SnackInline label='Speech' dependencies={['expo-speech']}>

```jsx
import * as React from 'react';
import { View, StyleSheet, Button } from 'react-native';
import * as Speech from 'expo-speech';

export default function App() {
  const speak = () => {
    const thingToSay = '1';
    Speech.speak(thingToSay);
  };

  return (
    <View style={styles.container}>
      <Button title="Press to hear some words" onPress={speak} />
    </View>
  );
}

/* @hide const styles = StyleSheet.create({ ... }); */
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor: '#ecf0f1',
    padding: 8,
  },
});
/* @end */
```

</SnackInline>

## API

```js
import * as Speech from 'expo-speech';
```

<APISection packageName="expo-speech" apiName="Speech" />