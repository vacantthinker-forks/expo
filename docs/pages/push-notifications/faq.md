---
title: Push Notifications Troubleshooting & FAQ
sidebar_title: Troubleshooting and FAQ
---

This is a collection of FAQs and common issues when setting up push notifications with Expo. This document covers the `expo-notifications` client-side library, as well as Expo's push notification service.

### How much does Expo's push notification service cost?

There is no cost associated with sending notifications through Expo's classic push notification service. EAS Notify, a push notification service with new features coming in 2021, will be part of EAS's pricing plans.

### How many notifications can I send through Expo?

We don't impose any limit on the number of push notifications you can send. For best results, we do recommend you add throttling (handled automatically in the [`expo-server-sdk-node`](https://github.com/expo/expo-server-sdk-node)) and retry logic to your server.

Total notifications is much less important than your maximum notification throughput—how many notifications you send per second at peak. If this number is more than a couple hundred, reach out to us because we'd love to hear about what you're working on!

### How do I set up push notifications?

You should read **all** the relevant guides (this won't take longer than 10 minutes):

- [Initial Setup](/push-notifications/push-notifications-setup.md)
  - [Android-specific setup](/push-notifications/using-fcm.md)
- [Receiving notifications in your app](/push-notifications/receiving-notifications.md)
- [Sending notifications](/push-notifications/sending-notifications.md)
- [`expo-notifications` client-side library documentation](/versions/latest/sdk/notifications.md)

### Do I have to use Expo's push notification service?

No, you can use any push notification service for both managed and bare workflow apps. The [`getDevicePushTokenAsync` method from `expo-notifications`](/versions/v40.0.0/sdk/notifications.md#getdevicepushtokenasync-devicepushtoken) allows you to get the native device push token, which you can then use with other services, or even [send your notifications through APNs and FCM directly](/push-notifications/sending-notifications-custom.md).

That being said, we think sending notifications through Expo is the fastest and easiest way to do it, and millions of notifications are sent through Expo every day.

### My push notifications aren't working

Push notifications have a lot of moving parts, so this can be due to a wide variety of reasons. To narrow things down, check the [push ticket](/push-notifications/sending-notifications.md#push-tickets) and [push receipt](/push-notifications/sending-notifications.md#push-receipts) for error messages. This information (and maybe a little bit of Googling) will help narrow down the problem so that you can solve it.

You can also narrow things even further by testing [local notifications](/versions/v40.0.0/sdk/notifications.md#schedulenotificationasyncnotificationrequest-notificationrequestinput-promisestring) in your app. This will ensure all of your client-side logic is correct, and narrow things down to the server side or app credentials.

<details><summary><h4>See here for some quick terminal commands you can use to get the push receipt.</h4></summary>
<p>
1. Send a notification:

```sh
curl -H "Content-Type: application/json" -X POST "https://exp.host/--/api/v2/push/send" -d '{
  "to": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]",
  "title":"hello",
  "body": "world"
}'
```

2. Use the resulting ticket `id` to request the push receipt:

```sh
curl -H "Content-Type: application/json" -X POST "https://exp.host/--/api/v2/push/getReceipts" -d '{
  "ids": [
    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
  ]
}'
```

</p>
</details>

### How often does the `ExpoPushToken` change?

The `ExpoPushToken` will remain the same across app upgrades, and `eject`ing to the bare workflow. On iOS, it will also remain the same even after uninstalling the app and reinstalling (on Android, this results in the push token changing). It will also change if you change your [`applicationId`](/versions/latest/sdk/application.md#applicationapplicationid) or `experienceId` (usually `@expoUsername/projectSlug`).

The `ExpoPushToken` will never "expire" but if one of your users uninstalls the app, you'll receive a `DeviceNotRegistered` error back from Expo's servers, meaning you should stop sending notifications to this app.

### Push notifications work in development, but not after I build the app

This strongly indicates that you have either misconfigured your credentials, or didn't configure them at all. In the Expo Go app, you rely on Expo's credentials so that you don't need to worry about it, and setup is as easy as possible. But when you build your own app for the stores, you need to use your own credentials. On iOS, this is handled via your [push key](/distribution/app-signing/#push-notification-keys.md) (revoking the push key associated with your app **will result in your notifications failing to be delivered**. To fix that, add a new push key with `expo credentials:manager`). On Android, all you need to do is follow [this guide](/push-notifications/using-fcm.md). **Please note** that after setting up Android FCM credentials, you will need to rebuild your app.

Expo abstracts the majority of credential management away so that you can focus on building your app, but if you want to understand it on a deeper level, read our [guide to app signing](/distribution/app-signing.md).

### Push notifications _occasionally_ stop coming through on Android

This is likely due to the `priority` level of the notifications you're sending. You can learn more about Android priority [here](https://firebase.google.com/docs/cloud-messaging/http-server-ref#downstream-http-messages-json), but as for how it relates to Expo- [Expo accepts four priorities](https://docs.expo.io/push-notifications/sending-notifications/#message-request-format):

- `default`: manually mapped to the default priority documented by Apple and Google
- `high`: mapped to the high priority level documented by Apple and Google
- `normal`: mapped to the normal priority level documented by Apple and Google
- (priority omitted): treated exactly as if `default` were specified

And setting the priority to `high` gives your notification the greatest likelihood that the Android OS will display the notification.

### Does Expo store the contents of push notifications?

Expo does not store the contents of push notifications any longer than it takes to deliver the notifications to the push notification services operated by Apple, Google, etc... Push notifications are stored only in memory and in message queues and **not** stored in databases.

### Does Expo read or share the contents of push notifications?

Expo does not read or share the contents of push notifications and our services keep push notifications only as long as needed to deliver them to push notification services run by Apple and Google. If the Expo team is actively debugging the push notifications service, we may see notification contents (ex: at a breakpoint) but Expo cannot see push notification contents otherwise.

### How does Expo encrypt connections to push notification services, like Apple's and Google's?

Expo's connections to Apple and Google are encrypted and use HTTPS.

### How do I handle expired push notification credentials?

When your push notification credentials have expired, run `expo credentials:manager -p ios` which will provide a list of actions to choose from. Select the removal of your expired credentials and then select "Add new Push Notifications Key".

### What delivery guarantees are there for push notifications?

Expo makes a best effort to deliver notifications to the push notification services operated by Apple and Google. Expo's infrastructure is designed for at-least-once delivery to the underlying push notification services; it is more likely for a notification to be delivered to Apple or Google more than once rather than not at all, though both are uncommon but possible.

After a notification has been handed off to an underlying push notification service, Expo creates a "push receipt" that records whether the handoff was successful; a push receipt denotes whether the underlying push notification service received the notification.

Finally, the push notification services from Apple, Google, etc... make a best effort to deliver the notification to the device according to their own policies.

### My notification icon on Android is a grey or white square

This indicates an issue with the image asset you're providing. The image should be all white with a transparent background (this is required and enforced by Google, not Expo). [See here for more information](https://clevertap.com/blog/fixing-notification-icon-for-android-lollipop-and-above/).

### I'm getting back an error message when I send a notification

Check the `details` property of the returned push ticket or receipt for more information which you can use to debug further. [Read here for common error code responses and their associated solutions](/push-notifications/sending-notifications.md#errors).

### I want to play a custom sound when I send a notification

The Expo push notification service currently doesn't support custom sounds. You will need to use APNs and FCM directly with the native device tokens received from `expo-notifications` in standalone apps.
