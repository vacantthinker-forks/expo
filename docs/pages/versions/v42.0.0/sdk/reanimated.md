---
title: Reanimated
sourceCodeUrl: 'https://github.com/software-mansion/react-native-reanimated'
---

import InstallSection from '~/components/plugins/InstallSection';
import PlatformsSection from '~/components/plugins/PlatformsSection';

**`react-native-reanimated`** provides an API that greatly simplifies the process of creating smooth, powerful, and maintainable animations.

<PlatformsSection android emulator ios simulator web />

## Installation

<InstallSection packageName="react-native-reanimated" href="https://docs.swmansion.com/react-native-reanimated/docs/installation" />

After the installation completed, add the Babel plugin to `babel.config.js`:

```jsx
module.exports = function(api) {
  api.cache(true);
  return {
    presets: ['babel-preset-expo'],
    plugins: ['react-native-reanimated/plugin'],
  };
};
```
> Note: If you load other Babel plugins, the Reanimated plugin has to be the last item in the plugins array.

> 🚨 **The new APIs in `react-native-reanimated@2` use React Native APIs that are incompatible with Remote JS Debugging**. Consequently, you can only debug apps using these APIs using Flipper, which is not yet available in the Expo managed workflow. **You will be unable to use Remote JS Debugging if you use the new APIs from Reanimated 2**. Remote JS Debugging will continue to work if you only use the APIs that were also available in Reanimated 1.

## API Usage

You should refer to the [react-native-reanimated docs](https://docs.swmansion.com/react-native-reanimated/docs/) for more information on the API and its usage. But the following example (courtesy of that repo) is a quick way to get started.

```js
import Animated, {
  useSharedValue,
  withTiming,
  useAnimatedStyle,
  Easing,
} from 'react-native-reanimated';
import { View, Button } from 'react-native';
import React from 'react';

export default function AnimatedStyleUpdateExample(props) {
  const randomWidth = useSharedValue(10);

  const config = {
    duration: 500,
    easing: Easing.bezier(0.5, 0.01, 0, 1),
  };

  const style = useAnimatedStyle(() => {
    return {
      width: withTiming(randomWidth.value, config),
    };
  });

  return (
    <View
      style={{
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
      }}>
      <Animated.View
        style={[{ width: 100, height: 80, backgroundColor: 'black', margin: 30 }, style]}
      />
      <Button
        title="toggle"
        onPress={() => {
          randomWidth.value = Math.random() * 350;
        }}
      />
    </View>
  );
}
```
