---
title: Speech
sourceCodeUrl: 'https://github.com/expo/expo/tree/sdk-42/packages/expo-speech'
---

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

## Constants

### `Speech.maxSpeechInputLength`

Maximum possible text length acceptable by `Speech.speak()` method. It is platform-dependent. On iOS, this returns `Number.MAX_VALUE`.

## Methods

### `Speech.speak(text, options)`

Speak out loud the `text` given `options`. Calling this when another text is being spoken adds an utterance to queue.

#### Arguments

- **text (_string_)** -- The text to be spoken. Cannot be longer than [`Speech.maxSpeechInputLength`](#speechmaxspeechinputlength).
- **options (_object_)** --

  A map of options:

  - **voice (_string_)** -- Voice identifier
  - **language (_string_)** -- The code of a language that should be used to read the `text`, check out IETF BCP 47 to see valid codes.
  - **pitch (_number_)** -- Pitch of the voice to speak `text`. 1.0 is the normal pitch.
  - **rate (_number_)** -- Rate of the voice to speak `text`. 1.0 is the normal rate.
  - **onStart (_function_)** -- A callback that is invoked when speaking starts.
  - **onDone (_function_)** -- A callback that is invoked when speaking finishes.
  - **onStopped (_function_)** -- A callback that is invoked when speaking is stopped by calling `Speech.stop()`.
  - **onError (_function_)** -- (Android only). A callback that is invoked when an error occurred while speaking.

#### Error Codes

- [`ERR_SPEECH_INPUT_LENGTH`](#err_speech_input_length)

### `Speech.stop()`

Interrupts current speech and deletes all in queue.

### `Speech.pause()`

Pauses current speech. This method is not available on Android.

### `Speech.resume()`

Resumes speaking previously paused speech or does nothing if there's none. This method is not available on Android.

### `Speech.isSpeakingAsync()`

Determine whether the Text-to-speech utility is currently speaking. Will return `true` if speaker is paused.

#### Returns

Returns a Promise that resolves to a boolean, `true` if speaking, `false` if not.

### `Speech.getAvailableVoicesAsync()`

Returns list of all available voices.

#### Returns

List of `Voice` objects.

##### `Voice`

|   Field    |           Type           |
| :--------: | :----------------------: |
| identifier |          string          |
|    name    |          string          |
|  quality   | enum Speech.VoiceQuality |
|  language  |          string          |

## Enum Types

### `Speech.VoiceQuality`

- **`VoiceQuality.Default`**
- **`VoiceQuality.Enhanced`**

## Error Codes

### `ERR_SPEECH_INPUT_LENGTH`

An error occurred when length of `text` parameter provided to `Speech.speak()` exceeds its limit. To see the limit, use `Speech.maxSpeechInputLength`.
