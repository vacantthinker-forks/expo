---
title: Random
sourceCodeUrl: 'https://github.com/expo/expo/tree/sdk-42/packages/expo-random'
---

import APISection from '~/components/plugins/APISection';
import InstallSection from '~/components/plugins/InstallSection';
import PlatformsSection from '~/components/plugins/PlatformsSection';

**`expo-random`** provides a native interface for creating strong random bytes. With `Random` you can create values equivalent to `Node.js` core `crypto.randomBytes` API. `expo-random` also works with `expo-standard-web-crypto`, which implements the W3C Crypto API for generating random bytes.

<PlatformsSection android emulator ios simulator web />

## Installation

<InstallSection packageName="expo-random" />

## API

```js
import * as Random from 'expo-random';
```

<APISection packageName="expo-random" apiName="Random" />