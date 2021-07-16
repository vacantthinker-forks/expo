---
title: AppAuth
sourceCodeUrl: 'https://github.com/expo/expo/tree/sdk-42/packages/expo-app-auth'
---

import InstallSection from '~/components/plugins/InstallSection';
import SnackInline from '~/components/plugins/SnackInline';

import PlatformsSection from '~/components/plugins/PlatformsSection';
import { H3 } from '~/components/plugins/Headings';
import { InlineCode } from '~/components/base/code';

> ⚠️ This package is deprecated in favor of [**AuthSession**](auth-session.md). Check out the [authentication guides](../../../guides/authentication.md) to learn how to migrate your existing authentication today.

**`expo-app-auth`** allows you to authenticate and authorize your users through the native OAuth library AppAuth by [OpenID](https://github.com/openid).

Many services that let you authenticate with them or login with them, like GitHub, Google, GitLab, etc., use the OAuth 2.0 protocol. It's the industry standard.

If you are trying to implement sign in with [Google](google-sign-in.md) or [Facebook](facebook.md), there are special modules in the Expo SDK for those (though this module will work).

<PlatformsSection android emulator ios simulator />

## Installation

<InstallSection packageName="expo-app-auth" />

## Managed Workflow

> These steps are nearly identical to our **Managed Workflow** guide on [deep linking in React Navigation](https://reactnavigation.org/docs/deep-linking/#set-up-with-expo-projects).

You will want to decide on a URI scheme for your app, this will correspond to the prefix before `://` in a URI. Ex: If your scheme is `mychat` then a link to your app would be `mychat://`.

The scheme only applies to standalone apps and you need to re-build the standalone app for the change to take effect. In the Expo Go app you can deep link using `exp://ADDRESS:PORT` where `ADDRESS` is often `127.0.0.1` and `PORT` is often `19000` - the URL is printed when you run `expo start`.

If you want to test with your custom scheme you will need to run `expo build:ios -t simulator` or `expo build:android` and install the resulting binaries in your emulators. You can register for a scheme in your `app.json` by adding a string under the scheme key:

```json
{
  "expo": {
    "scheme": "myapp"
  }
}
```

To create a scheme that is appropriate for the environment, be sure to use `Linking` from `expo`:

```js
import { Linking } from 'expo';

const prefix = Linking.createURL('/');
// Expo Go: `exp://ADDRESS:PORT`
// Standalone: `myapp://`
```

For more info on [Linking in Expo](../../../guides/linking.md).

## Bare Workflow

> ⚠️ This module may not work as expected in managed EAS build, migrate to AuthSession for a seamless experience.

Carefully follow our in-depth **Bare Workflow** guide for [deep linking](https://reactnavigation.org/docs/deep-linking/#set-up-with-react-native-init-projects).

For more customization (like https redirects) please refer to the native docs: [capturing the authorization redirect](https://github.com/openid/AppAuth-android#capturing-the-authorization-redirect).

## Usage

> ⚠️ Use the dedicated [Authentication guides](../../../guides/authentication.md) instead.

Below is a set of example functions that demonstrate how to use `expo-app-auth` with the Google OAuth sign in provider.

<SnackInline>

```js
import React, { useEffect, useState } from 'react';
import { AsyncStorage, Button, StyleSheet, Text, View } from 'react-native';
import * as AppAuth from 'expo-app-auth';

export default function App() {
  let [authState, setAuthState] = useState(null);

  useEffect(() => {
    (async () => {
      let cachedAuth = await getCachedAuthAsync();
      if (cachedAuth && !authState) {
        setAuthState(cachedAuth);
      }
    })();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Expo AppAuth Example</Text>
      <Button
        title="Sign In with Google "
        onPress={async () => {
          const _authState = await signInAsync();
          setAuthState(_authState);
        }}
      />
      <Button
        title="Sign Out "
        onPress={async () => {
          await signOutAsync(authState);
          setAuthState(null);
        }}
      />
      <Text>{JSON.stringify(authState, null, 2)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

let config = {
  issuer: 'https://accounts.google.com',
  scopes: ['openid', 'profile'],
  /* This is the CLIENT_ID generated from a Firebase project */
  clientId: '603386649315-vp4revvrcgrcjme51ebuhbkbspl048l9.apps.googleusercontent.com',
};

let StorageKey = '@MyApp:CustomGoogleOAuthKey';

export async function signInAsync() {
  let authState = await AppAuth.authAsync(config);
  await cacheAuthAsync(authState);
  console.log('signInAsync', authState);
  return authState;
}

async function cacheAuthAsync(authState) {
  return await AsyncStorage.setItem(StorageKey, JSON.stringify(authState));
}

export async function getCachedAuthAsync() {
  let value = await AsyncStorage.getItem(StorageKey);
  let authState = JSON.parse(value);
  console.log('getCachedAuthAsync', authState);
  if (authState) {
    if (checkIfTokenExpired(authState)) {
      return refreshAuthAsync(authState);
    } else {
      return authState;
    }
  }
  return null;
}

function checkIfTokenExpired({ accessTokenExpirationDate }) {
  return new Date(accessTokenExpirationDate) < new Date();
}

async function refreshAuthAsync({ refreshToken }) {
  let authState = await AppAuth.refreshAsync(config, refreshToken);
  console.log('refreshAuth', authState);
  await cacheAuthAsync(authState);
  return authState;
}

export async function signOutAsync({ accessToken }) {
  try {
    await AppAuth.revokeAsync(config, {
      token: accessToken,
      isClientIdProvided: true,
    });
    await AsyncStorage.removeItem(StorageKey);
    return null;
  } catch (e) {
    alert(`Failed to revoke token: ${e.message}`);
  }
}
```

</SnackInline>

## Comparison

There are a couple different methods for authenticating your app in React Native and Expo, this should help you know if `expo-app-auth` is right for your needs.

### AuthSession

The [`AuthSession`](auth-session.md) API is built on top of [`expo-web-browser`](webbrowser.md) and cuts out a lot of the tricky steps involved with web authentication. Both `AppAuth` and `AuthSession` use `SFAuthenticationSession` and `ChromeCustomTabs` to authenticate natively, but AppAuth has built in support for [OpenID](https://github.com/openid). AuthSession uses an extra Expo service that makes development easier (especially across teams) but this can have some extra [security considerations](auth-session.md#security-considerations).

### react-native-app-auth

The `expo-app-auth` module is based on [react-native-app-auth](https://github.com/FormidableLabs/react-native-app-auth) by the incredible React.js consulting firm [Formidable](https://formidable.com/). The documentation and questions there may prove helpful. `expo-app-auth` provides a few extra features to make native app auth work inside a sandboxed Expo Go environment.

## API

```js
import * as AppAuth from 'expo-app-auth';
```

## Methods

### `AppAuth.authAsync()`

> ⚠️ Use [`AuthSession.useAuthRequest`](auth-session.md#useauthrequest) instead.

```js
AppAuth.authAsync(props: OAuthProps): Promise<TokenResponse>
```

Starts an OAuth flow and returns authorization credentials.

#### Parameters

| Name  | Type         | Description                      |
| ----- | ------------ | -------------------------------- |
| props | `OAuthProps` | Configuration for the OAuth flow |

#### Return

| Name          | Type                     | Description                  |
| ------------- | ------------------------ | ---------------------------- |
| tokenResponse | `Promise<TokenResponse>` | Authenticated response token |

#### Example

```js
const config = {
  issuer: 'https://accounts.google.com',
  clientId: '<CLIENT_ID>',
  scopes: ['profile'],
};

const tokenResponse = await AppAuth.authAsync(config);
```

### `AppAuth.refreshAsync()`

> ⚠️ Use [`AuthSession.refreshAsync()`](auth-session.md#authsessionrefreshasync) instead.

```js
AppAuth.refreshAsync(props: OAuthProps, refreshToken: string): Promise<TokenResponse>
```

Renew the authorization credentials (access token). Some providers may not return a new refresh token.

#### Parameters

| Name         | Type         | Description                                   |
| ------------ | ------------ | --------------------------------------------- |
| props        | `OAuthProps` | Configuration for the OAuth flow              |
| refreshToken | `string`     | Refresh token to exchange for an Access Token |

#### Return

| Name          | Type                     | Description                             |
| ------------- | ------------------------ | --------------------------------------- |
| tokenResponse | `Promise<TokenResponse>` | Refreshed authentication response token |

#### Example

```js
const config = {
  issuer: 'https://accounts.google.com',
  clientId: '<CLIENT_ID>',
  scopes: ['profile'],
};

const tokenResponse = await AppAuth.refreshAsync(config, refreshToken);
```

### `AppAuth.revokeAsync()`

> ⚠️ Use [`AuthSession.revokeAsync()`](auth-session.md#authsessionrevokeasync) instead.

```js
AppAuth.revokeAsync(props: OAuthBaseProps, options: OAuthRevokeOptions): Promise<any>
```

A fully JS function which revokes the provided access token or refresh token.
Use this method for signing-out. Returns a fetch request.

#### Parameters

| Name    | Type                 | Description                                            |
| ------- | -------------------- | ------------------------------------------------------ |
| props   | `OAuthBaseProps`     | The same OAuth configuration used for the initial flow |
| options | `OAuthRevokeOptions` | Refresh token or access token to revoke                |

### Example

```js
const config = {
  issuer: 'https://accounts.google.com',
  clientId: '<CLIENT_ID>',
};

const options = {
  token: accessToken, // or a refreshToken
  isClientIdProvided: true,
};

// Sign out...
await AppAuth.revokeAsync(config, options);
```

## Constants

### `AppAuth.OAuthRedirect`

> ⚠️ Use [`Application.applicationId`](application.md##applicationapplicationid) instead.

Redirect scheme used to assemble the `redirectUrl` prop. In standalone apps, this is either the `android.package` (for Android) or `ios.bundleIdentifier` (for iOS) value from your `app.json`. However, for apps running in Expo Go, `AppAuth.OAuthRedirect` is `host.exp.exponent`.

### `AppAuth.URLSchemes`

> iOS only

A list of URL Schemes from the `info.plist`

## Types

### `TokenResponse`

Return value of the following `AppAuth` methods:

- `AppAuth.authAsync()`
- `AppAuth.refreshAsync()`

| Name                      | Type                                                   | Description                                                                               |
| ------------------------- | ------------------------------------------------------ | ----------------------------------------------------------------------------------------- |
| accessToken               | <InlineCode>string \| null</InlineCode>                | Access token generated by the auth server                                                 |
| accessTokenExpirationDate | <InlineCode>string \| null</InlineCode>                | Approximate expiration date and time of the access token                                  |
| additionalParameters      | <InlineCode>\{ \[string\]: any \} \| null</InlineCode> | Additional parameters returned from the auth server                                       |
| idToken                   | <InlineCode>string \| null</InlineCode>                | ID Token value associated with the authenticated session                                  |
| tokenType                 | <InlineCode>string \| null</InlineCode>                | Typically "Bearer" when defined or a value the client has negotiated with the auth Server |
| refreshToken              | <InlineCode>string \| undefined</InlineCode>           | The most recent refresh token received from the auth server                               |

### `OAuthBaseProps`

| Name                                                                                                                               | Type                        | Description                                                                                                 |
| ---------------------------------------------------------------------------------------------------------------------------------- | --------------------------- | ----------------------------------------------------------------------------------------------------------- |
| clientId                                                                                                                           | `string`                    | The client identifier                                                                                       |
| [issuer](http://openid.github.io/AppAuth-iOS/docs/latest/interface_o_i_d_service_discovery.html#a7bd40452bb3a0094f251934fd85a8fd6) | `string`                    | URL using the https scheme with no query or fragment component that the OP asserts as its Issuer Identifier |
| serviceConfiguration                                                                                                               | `OAuthServiceConfiguration` | specifies how to connect to a particular OAuth provider                                                     |

### `OAuthProps`

extends `OAuthBaseProps`, is used to create OAuth flows.

| Name                                                                                                                               | Type                                                 | Description                                                                                                 |
| ---------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| clientId                                                                                                                           | `string`                                             | The client identifier                                                                                       |
| [issuer](http://openid.github.io/AppAuth-iOS/docs/latest/interface_o_i_d_service_discovery.html#a7bd40452bb3a0094f251934fd85a8fd6) | `string`                                             | URL using the https scheme with no query or fragment component that the OP asserts as its Issuer Identifier |
| serviceConfiguration                                                                                                               | `OAuthServiceConfiguration`                          | specifies how to connect to a particular OAuth provider                                                     |
| clientSecret                                                                                                                       | <InlineCode>string \| undefined</InlineCode>         | used to prove that identity of the client when exchaning an authorization code for an access token          |
| [scopes](https://tools.ietf.org/html/rfc6749#section-3.3)                                                                          | <InlineCode>Array<string\> \| undefined</InlineCode> | a list of space-delimited, case-sensitive strings define the scope of the access requested                  |
| redirectUrl                                                                                                                        | <InlineCode>string \| undefined</InlineCode>         | The client's redirect URI. Default: `AppAuth.OAuthRedirect + ':/oauthredirect'`                             |
| [additionalParameters](https://tools.ietf.org/html/rfc6749#section-3.1)                                                            | `OAuthParameters`                                    | Extra props passed to the OAuth server request                                                              |
| canMakeInsecureRequests                                                                                                            | <InlineCode>boolean \| undefined</InlineCode>        | **Android: Only** enables the use of HTTP requests                                                          |

### `OAuthRevokeOptions`

| Name               | Type      | Description                                                        |
| ------------------ | --------- | ------------------------------------------------------------------ |
| token              | `string`  | The access token or refresh token to revoke                        |
| isClientIdProvided | `boolean` | Denotes the availability of the Client ID for the token revocation |

### `OAuthServiceConfiguration`

| Name                                                                                                                                             | Type                                         | Description                                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------- | ------------------------------------------------------------- |
| [authorizationEndpoint](https://openid.net/specs/openid-connect-core-1_0.html#AuthorizationEndpoint)                                             | <InlineCode>string \| undefined</InlineCode> | Optional URL of the OP's OAuth 2.0 Authorization Endpoint     |
| [registrationEndpoint](http://openid.github.io/AppAuth-iOS/docs/latest/interface_o_i_d_service_discovery.html#ab6a4608552978d3bce67b93b45321555) | <InlineCode>string \| undefined</InlineCode> | Optional URL of the OP's Dynamic Client Registration Endpoint |
| revocationEndpoint                                                                                                                               | <InlineCode>string \| undefined</InlineCode> | Optional URL of the OAuth server used for revoking tokens     |
| tokenEndpoint                                                                                                                                    | `string`                                     | URL of the OP's OAuth 2.0 Token Endpoint                      |

### `OAuthParameters`

Learn more about OAuth Parameters on this exciting page: [openid-connect-core](https://openid.net/specs/openid-connect-core-1_0.html).
To save time I've copied over some of the relevant information, which you can find below.

| Name          | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| nonce         | <InlineCode>OAuthNonceParameter \| undefined</InlineCode>       |
| display       | <InlineCode>OAuthParametersDisplay \| undefined</InlineCode>    |
| prompt        | <InlineCode>OAuthPromptParameter \| undefined</InlineCode>      |
| max_age       | <InlineCode>OAuthMaxAgeParameter \| undefined</InlineCode>      |
| ui_locales    | <InlineCode>OAuthUILocalesParameter \| undefined</InlineCode>   |
| id_token_hint | <InlineCode>OAuthIDTokenHintParameter \| undefined</InlineCode> |
| login_hint    | <InlineCode>OAuthLoginHintParameter \| undefined</InlineCode>   |
| acr_values    | <InlineCode>OAuthACRValuesParameter \| undefined</InlineCode>   |

Other parameters MAY be sent. See Sections [3.2.2](https://openid.net/specs/openid-connect-core-1_0.html#ImplicitAuthorizationEndpoint), [3.3.2](https://openid.net/specs/openid-connect-core-1_0.html#HybridAuthorizationEndpoint), [5.2](https://openid.net/specs/openid-connect-core-1_0.html#ClaimsLanguagesAndScripts), [5.5](https://openid.net/specs/openid-connect-core-1_0.html#ClaimsParameter), [6](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests), and [7.2.1](https://openid.net/specs/openid-connect-core-1_0.html#RegistrationParameter) for additional Authorization Request parameters and parameter values defined by this specification.

<H3 sidebarType="inlineCode">

[`OAuthDisplayParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthDisplayParameter = 'page' | 'popup' | 'touch' | 'wap';
```

ASCII string value that specifies how the Authorization Server displays the authentication and consent user interface pages to the End-User.

| Value   | Description                                                                                                                                                                                                                                                                       |
| ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `page`  | The Authorization Server SHOULD display the authentication and consent UI consistent with a full User Agent page view. If the display parameter is not specified, this is the default display mode.                                                                               |
| `popup` | The Authorization Server SHOULD display the authentication and consent UI consistent with a popup User Agent window. The popup User Agent window should be of an appropriate size for a login-focused dialog and should not obscure the entire window that it is popping up over. |
| `touch` | The Authorization Server SHOULD display the authentication and consent UI consistent with a device that leverages a touch interface.                                                                                                                                              |
| `wap`   | The Authorization Server SHOULD display the authentication and consent UI consistent with a "feature phone" type display.                                                                                                                                                         |

The Authorization Server MAY also attempt to detect the capabilities of the User Agent and present an appropriate display.

<H3 sidebarType="inlineCode">

[`OAuthPromptParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthPromptParameter = 'none' | 'login' | 'consent' | 'select_account';
```

Space delimited, case sensitive list of ASCII string values that specifies whether the Authorization Server prompts the End-User for reauthentication and consent.

| Value            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| ---------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `none`           | The Authorization Server MUST NOT display any authentication or consent user interface pages. An error is returned if an End-User is not already authenticated or the Client does not have pre-configured consent for the requested Claims or does not fulfill other conditions for processing the request. The error code will typically be `login_required`, `interaction_required`, or another code defined in [Section 3.1.2.6](https://openid.net/specs/openid-connect-core-1_0.html#AuthError). This can be used as a method to check for existing authentication and/or consent. |
| `login`          | The Authorization Server SHOULD prompt the End-User for reauthentication. If it cannot reauthenticate the End-User, it MUST return an error, typically `login_required`.                                                                                                                                                                                                                                                                                                                                                                                                                |
| `consent`        | The Authorization Server SHOULD prompt the End-User for consent before returning information to the Client. If it cannot obtain consent, it MUST return an error, typically `consent_required`.                                                                                                                                                                                                                                                                                                                                                                                         |
| `select_account` | The Authorization Server SHOULD prompt the End-User to select a user account. This enables an End-User who has multiple accounts at the Authorization Server to select amongst the multiple accounts that they might have current sessions for. If it cannot obtain an account selection choice made by the End-User, it MUST return an error, typically `account_selection_required`.                                                                                                                                                                                                  |

The `prompt` parameter can be used by the Client to make sure that the End-User is still present for the current session or to bring attention to the request. If this parameter contains `none` with any other value, an error is returned.

<H3 sidebarType="inlineCode">

[`OAuthNonceParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthNonceParameter = string;
```

String value used to associate a Client session with an ID Token, and to mitigate replay attacks. The value is passed through unmodified from the Authentication Request to the ID Token. Sufficient entropy MUST be present in the `nonce` values used to prevent attackers from guessing values. For implementation notes, see [Section 15.5.2](https://openid.net/specs/openid-connect-core-1_0.html#NonceNotes).

<H3 sidebarType="inlineCode">

[`OAuthUILocalesParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthUILocalesParameter = string;
```

End-User's preferred languages and scripts for the user interface, represented as a space-separated list of [BCP47](https://openid.net/specs/openid-connect-core-1_0.html#RFC5646) [RFC5646] language tag values, ordered by preference. For instance, the value "fr-CA fr en" represents a preference for French as spoken in Canada, then French (without a region designation), followed by English (without a region designation). An error SHOULD NOT result if some or all of the requested locales are not supported by the OpenID Provider.

<H3 sidebarType="inlineCode">

[`OAuthIDTokenHintParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthIDTokenHintParameter = string;
```

ID Token previously issued by the Authorization Server being passed as a hint about the End-User's current or past authenticated session with the Client. If the End-User identified by the ID Token is logged in or is logged in by the request, then the Authorization Server returns a positive response; otherwise, it SHOULD return an error, such as login_required. When possible, an `id_token_hint` SHOULD be present when `prompt=none` is used and an `invalid_request` error MAY be returned if it is not; however, the server SHOULD respond successfully when possible, even if it is not present. The Authorization Server need not be listed as an audience of the ID Token when it is used as an `id_token_hint` value.
If the ID Token received by the RP from the OP is encrypted, to use it as an `id_token_hint`, the Client MUST decrypt the signed ID Token contained within the encrypted ID Token. The Client MAY re-encrypt the signed ID token to the Authentication Server using a key that enables the server to decrypt the ID Token, and use the re-encrypted ID token as the `id_token_hint` value.

<H3 sidebarType="inlineCode">

[`OAuthMaxAgeParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthMaxAgeParameter = string;
```

Maximum Authentication Age. Specifies the allowable elapsed time in seconds since the last time the End-User was actively authenticated by the OP. If the elapsed time is greater than this value, the OP MUST attempt to actively re-authenticate the End-User. (The `max_age` request parameter corresponds to the OpenID 2.0 [PAPE](https://openid.net/specs/openid-connect-core-1_0.html#OpenID.PAPE) [OpenID.PAPE] `max_auth_age` request parameter.) When `max_age` is used, the ID Token returned MUST include an `auth_time` Claim Value.

<H3 sidebarType="inlineCode">

[`OAuthLoginHintParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthLoginHintParameter = string;
```

OPTIONAL. Hint to the Authorization Server about the login identifier the End-User might use to log in (if necessary). This hint can be used by an RP if it first asks the End-User for their e-mail address (or other identifier) and then wants to pass that value as a hint to the discovered authorization service. It is RECOMMENDED that the hint value match the value used for discovery. This value MAY also be a phone number in the format specified for the `phone_number` Claim. The use of this parameter is left to the OP's discretion.

<H3 sidebarType="inlineCode">

[`OAuthACRValuesParameter`](https://openid.net/specs/openid-connect-core-1_0.html)

</H3>

```js
type OAuthACRValuesParameter = string;
```

Requested Authentication Context Class Reference values. Space-separated string that specifies the acr values that the Authorization Server is being requested to use for processing this Authentication Request, with the values appearing in order of preference. The Authentication Context Class satisfied by the authentication performed is returned as the acr Claim Value, as specified in Section 2. The acr Claim is requested as a Voluntary Claim by this parameter.
