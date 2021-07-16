import Constants from 'expo-constants';
import * as GoogleSignIn from 'expo-google-sign-in';
import React from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';

import { getGUID } from '../api/guid';
import GoogleSignInButton from '../components/GoogleSignInButton';

GoogleSignIn.allowInClient();

interface State {
  user?: {
    [key: string]: any;
    photoURL: string;
    displayName: string;
    email: string;
  };
}

// See: https://github.com/expo/expo/pull/10229#discussion_r490961694
// eslint-disable-next-line @typescript-eslint/ban-types
export default class GoogleSignInScreen extends React.Component<{}, State> {
  static navigationOptions = {
    title: 'Native Google Sign-In',
  };

  readonly state: State = {};

  componentDidMount() {
    this._configureAsync();
  }

  _configureAsync = async () => {
    try {
      await GoogleSignIn.initAsync({
        isOfflineEnabled: false,
        isPromptEnabled: true,
        clientId: `${getGUID()}.apps.googleusercontent.com`,
      });
    } catch ({ message }) {
      console.error('Demo: Error: init: ' + message);
    }
    this._syncUserWithStateAsync();
  };

  _syncUserWithStateAsync = async () => {
    /*
      const user = await GoogleSignIn.signInSilentlyAsync();
      this.setState({ user });
    */

    if (await GoogleSignIn.signInSilentlyAsync()) {
      const photoURL = await GoogleSignIn.getPhotoAsync(256);
      const user = await GoogleSignIn.getCurrentUserAsync();
      if (user) {
        this.setState({
          user: {
            ...(user.toJSON() as { displayName: string; email: string }),
            photoURL: photoURL || user.photoURL!,
          },
        });
      }
    } else {
      this.setState({ user: undefined });
    }
  };

  get buttonTitle() {
    return this.state.user ? 'Sign-Out of Google' : 'Sign-In with Google';
  }

  render() {
    const { user } = this.state;
    const isInExpoGo = Constants.executionEnvironment === 'storeClient';

    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        {isInExpoGo && (
          <Text style={{ padding: 12 }}>
            This library cannot be used in Expo Go as the native GoogleSignIn library expects your
            REVERSED_CLIENT_ID in the info.plist at build-time.
          </Text>
        )}
        {user && <GoogleProfile {...user} />}
        <GoogleSignInButton disabled={isInExpoGo} onPress={this._toggleAuth}>
          {this.buttonTitle}
        </GoogleSignInButton>
      </View>
    );
  }

  _toggleAuth = () => {
    if (this.state.user) {
      this._signOutAsync();
    } else {
      this._signInAsync();
    }
  };

  _signOutAsync = async () => {
    try {
      // await GoogleSignIn.disconnectAsync();
      await GoogleSignIn.signOutAsync();
      console.log('Log out successful');
    } catch ({ message }) {
      console.error('Demo: Error: logout: ' + message);
    } finally {
      this.setState({ user: undefined });
    }
  };

  _signInAsync = async () => {
    try {
      await GoogleSignIn.askForPlayServicesAsync();
      const { type, user } = await GoogleSignIn.signInAsync();
      console.log({ type, user });
      if (type === 'success') {
        this._syncUserWithStateAsync();
      }
    } catch ({ message }) {
      console.error('login: Error:' + message);
    }
  };
}

const GoogleProfile: React.FunctionComponent<{
  photoURL: string;
  displayName: string;
  email: string;
}> = ({ photoURL, displayName, email }) => (
  <View style={styles.container}>
    {photoURL && (
      <Image
        source={{
          uri: photoURL,
        }}
        style={styles.image}
      />
    )}
    <View style={{ marginLeft: 12 }}>
      <Text style={styles.text}>{displayName}</Text>
      <Text style={styles.text}>{email}</Text>
    </View>
  </View>
);

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 24,
  },
  image: { width: 128, borderRadius: 64, aspectRatio: 1 },
  text: { color: 'black', fontSize: 16, fontWeight: '600' },
});
