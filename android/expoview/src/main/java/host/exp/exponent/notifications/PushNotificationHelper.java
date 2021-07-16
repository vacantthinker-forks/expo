package host.exp.exponent.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import expo.modules.updates.manifest.ManifestFactory;
import expo.modules.updates.manifest.raw.RawManifest;
import host.exp.exponent.Constants;
import host.exp.exponent.ExponentManifest;
import host.exp.exponent.analytics.EXL;
import host.exp.exponent.di.NativeModuleDepsProvider;
import host.exp.exponent.kernel.ExperienceKey;
import host.exp.exponent.kernel.KernelConstants;
import host.exp.exponent.storage.ExperienceDBObject;
import host.exp.exponent.storage.ExponentDB;
import host.exp.exponent.storage.ExponentSharedPreferences;
import host.exp.expoview.R;

public class PushNotificationHelper {

  private static final String TAG = PushNotificationHelper.class.getSimpleName();

  private static PushNotificationHelper sInstance = null;

  public static PushNotificationHelper getInstance() {
    if (sInstance == null) {
      sInstance = new PushNotificationHelper();
    }

    return sInstance;
  }

  private enum Mode {
    DEFAULT,
    COLLAPSE
  }

  @Inject
  ExponentManifest mExponentManifest;

  @Inject
  ExponentSharedPreferences mExponentSharedPreferences;

  public PushNotificationHelper() {
    NativeModuleDepsProvider.getInstance().inject(PushNotificationHelper.class, this);
  }

  public void onMessageReceived(final Context context, final String experienceScopeKey, final String channelId, final String message, final String body, final String title, final String categoryId) {
    ExponentDB.experienceScopeKeyToExperience(experienceScopeKey, new ExponentDB.ExperienceResultListener() {
      @Override
      public void onSuccess(ExperienceDBObject experience) {
        try {
          RawManifest manifest = ManifestFactory.INSTANCE.getRawManifestFromJson(new JSONObject(experience.manifest));
          sendNotification(context, message, channelId, experience.manifestUrl, manifest, body, title, categoryId);
        } catch (JSONException e) {
          EXL.e(TAG, "Couldn't deserialize JSON for experience scope key " + experienceScopeKey);
        }
      }

      @Override
      public void onFailure() {
        EXL.e(TAG, "No experience found for scope key " + experienceScopeKey);
      }
    });
  }


  private void sendNotification(final Context context, final String message, final String channelId,
                                final String manifestUrl, final RawManifest manifest, final String body,
                                final String title, final String categoryId) throws JSONException {
    ExperienceKey experienceKey = ExperienceKey.fromRawManifest(manifest);
    final String name = manifest.getName();
    if (name == null) {
      EXL.e(TAG, "No name found for experience scope key " + experienceKey.getScopeKey());
      return;
    }

    final ExponentNotificationManager manager = new ExponentNotificationManager(context);
    @Nullable final JSONObject notificationPreferences = manifest.getNotificationPreferences();

    NotificationHelper.loadIcon(null, manifest, mExponentManifest, new ExponentManifest.BitmapListener() {
      @Override
      public void onLoadBitmap(final Bitmap bitmap) {
        Mode mode = Mode.DEFAULT;
        String collapsedTitle = null;
        JSONArray unreadNotifications = new JSONArray();

        // Modes
        if (notificationPreferences != null) {
          String modeString = notificationPreferences.optString(ExponentManifest.MANIFEST_NOTIFICATION_ANDROID_MODE);
          if (NotificationConstants.NOTIFICATION_COLLAPSE_MODE.equals(modeString)) {
            mode = Mode.COLLAPSE;
          }
        }

        // Update metadata
        final int notificationId = mode == Mode.COLLAPSE ? experienceKey.getScopeKey().hashCode() : new Random().nextInt();
        addUnreadNotificationToMetadata(experienceKey, message, notificationId);

        // Collapse mode fields
        if (mode == Mode.COLLAPSE) {
          unreadNotifications = getUnreadNotificationsFromMetadata(experienceKey);

          String collapsedTitleRaw = notificationPreferences.optString(ExponentManifest.MANIFEST_NOTIFICATION_ANDROID_COLLAPSED_TITLE);
          if (collapsedTitleRaw != null) {
            collapsedTitle = collapsedTitleRaw.replace(NotificationConstants.NOTIFICATION_UNREAD_COUNT_KEY, "" + unreadNotifications.length());
          }
        }

        String scopedChannelId;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (channelId != null) {
          scopedChannelId = ExponentNotificationManager.getScopedChannelId(experienceKey, channelId);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // if we don't yet have a channel matching this ID, check shared preferences --
            // it's possible this device has just been upgraded to Android 8+ and the channel
            // needs to be created in the system
            if (manager.getNotificationChannel(experienceKey, channelId) == null) {
              JSONObject storedChannelDetails = manager.readChannelSettings(experienceKey, channelId);
              if (storedChannelDetails != null) {
                NotificationHelper.createChannel(context, experienceKey, channelId, storedChannelDetails);
              }
            }
          } else {
            // on Android 7.1 and below, read channel settings for sound from shared preferences
            // and apply this to the notification individually, since channels do not exist
            JSONObject storedChannelDetails = manager.readChannelSettings(experienceKey, channelId);
            if (storedChannelDetails != null) {
              // Default to `sound: true` if nothing is stored for this channel
              // to match old behavior of push notifications on Android 7.1 and below (always had sound)
              if (!storedChannelDetails.optBoolean(NotificationConstants.NOTIFICATION_CHANNEL_SOUND, true)) {
                defaultSoundUri = null;
              }
            }
          }
        } else {
          scopedChannelId = ExponentNotificationManager.getScopedChannelId(experienceKey, NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID);
          NotificationHelper.createChannel(
              context,
              experienceKey,
              NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID,
              context.getString(R.string.default_notification_channel_group),
              new HashMap());
        }

        int color = NotificationHelper.getColor(null, manifest, mExponentManifest);

        // Create notification object
        boolean isMultiple = mode == Mode.COLLAPSE && unreadNotifications.length() > 1;
        final ReceivedNotificationEvent notificationEvent = new ReceivedNotificationEvent(experienceKey.getScopeKey(), body, notificationId, isMultiple, true);

        // Create pending intent
        Intent intent = new Intent(context, KernelConstants.MAIN_ACTIVITY_CLASS);
        intent.putExtra(KernelConstants.NOTIFICATION_MANIFEST_URL_KEY, manifestUrl);
        intent.putExtra(KernelConstants.NOTIFICATION_KEY, body); // deprecated
        intent.putExtra(KernelConstants.NOTIFICATION_OBJECT_KEY, notificationEvent.toJSONObject(null).toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        // Build notification
        final NotificationCompat.Builder notificationBuilder;

        if (isMultiple) {
          NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
              .setBigContentTitle(collapsedTitle);

          for (int i = 0; i < Math.min(unreadNotifications.length(), NotificationConstants.MAX_COLLAPSED_NOTIFICATIONS); i++) {
            try {
              JSONObject unreadNotification = (JSONObject) unreadNotifications.get(i);
              style.addLine(unreadNotification.getString(NotificationConstants.NOTIFICATION_MESSAGE_KEY));
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }

          if (unreadNotifications.length() > NotificationConstants.MAX_COLLAPSED_NOTIFICATIONS) {
            style.addLine("and " + (unreadNotifications.length() - NotificationConstants.MAX_COLLAPSED_NOTIFICATIONS) + " more...");
          }

          notificationBuilder = new NotificationCompat.Builder(context, scopedChannelId)
              .setSmallIcon(Constants.isStandaloneApp() ? R.drawable.shell_notification_icon : R.drawable.notification_icon)
              .setContentTitle(collapsedTitle)
              .setColor(color)
              .setContentText(name)
              .setAutoCancel(true)
              .setSound(defaultSoundUri)
              .setContentIntent(pendingIntent)
              .setStyle(style);
        } else {
          String contentTitle;
          if (title == null) {
            contentTitle = name;
          } else {
            contentTitle = Constants.isStandaloneApp() ? title : name + " - " + title;
          }

          notificationBuilder = new NotificationCompat.Builder(context, scopedChannelId)
              .setSmallIcon(Constants.isStandaloneApp() ? R.drawable.shell_notification_icon : R.drawable.notification_icon)
              .setContentTitle(contentTitle)
              .setColor(color)
              .setContentText(message)
              .setStyle(new NotificationCompat.BigTextStyle()
                  .bigText(message))
              .setAutoCancel(true)
              .setSound(defaultSoundUri)
              .setContentIntent(pendingIntent);
        }



        new Thread(new Runnable() {
          @Override
          public void run() {
            // Add actions
            if (categoryId != null) {
              NotificationActionCenter.setCategory(categoryId, notificationBuilder, context, new IntentProvider() {
                @Override
                public Intent provide() {
                  Intent intent = new Intent(context, KernelConstants.MAIN_ACTIVITY_CLASS);
                  intent.putExtra(KernelConstants.NOTIFICATION_MANIFEST_URL_KEY, manifestUrl);
                  intent.putExtra(KernelConstants.NOTIFICATION_KEY, body); // deprecated
                  intent.putExtra(KernelConstants.NOTIFICATION_OBJECT_KEY, notificationEvent.toJSONObject(null).toString());
                  return intent;
                }
              });
            }

            // Add icon
            Notification notification;
            if (!manifestUrl.equals(Constants.INITIAL_URL)) {
              notification = notificationBuilder.setLargeIcon(bitmap).build();
            } else {
              // TODO: don't actually need to load bitmap in this case
              notification = notificationBuilder.build();
            }

            // Display
            manager.notify(experienceKey, notificationId, notification);

            // Send event. Will be consumed if experience is already open.
            EventBus.getDefault().post(notificationEvent);
          }
        }).start();
      }
    });
  }

  private void addUnreadNotificationToMetadata(ExperienceKey experienceKey, String message, int notificationId) {
    try {
      JSONObject notification = new JSONObject();
      notification.put(NotificationConstants.NOTIFICATION_MESSAGE_KEY, message);
      notification.put(NotificationConstants.NOTIFICATION_ID_KEY, notificationId);

      JSONObject metadata = mExponentSharedPreferences.getExperienceMetadata(experienceKey);
      if (metadata == null) {
        metadata = new JSONObject();
      }

      JSONArray unreadNotifications = metadata.optJSONArray(ExponentSharedPreferences.EXPERIENCE_METADATA_UNREAD_REMOTE_NOTIFICATIONS);
      if (unreadNotifications == null) {
        unreadNotifications = new JSONArray();
      }

      unreadNotifications.put(notification);

      metadata.put(ExponentSharedPreferences.EXPERIENCE_METADATA_UNREAD_REMOTE_NOTIFICATIONS, unreadNotifications);
      mExponentSharedPreferences.updateExperienceMetadata(experienceKey, metadata);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private JSONArray getUnreadNotificationsFromMetadata(ExperienceKey experienceKey) {
    JSONObject metadata = mExponentSharedPreferences.getExperienceMetadata(experienceKey);
    if (metadata != null) {
      if (metadata.has(ExponentSharedPreferences.EXPERIENCE_METADATA_UNREAD_REMOTE_NOTIFICATIONS)) {
        try {
          return metadata.getJSONArray(ExponentSharedPreferences.EXPERIENCE_METADATA_UNREAD_REMOTE_NOTIFICATIONS);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }

    return new JSONArray();
  }

  public void removeNotifications(Context context, JSONArray unreadNotifications) {
    if (unreadNotifications == null) {
      return;
    }

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    for (int i = 0; i < unreadNotifications.length(); i++) {
      try {
        notificationManager.cancel(Integer.parseInt(((JSONObject) unreadNotifications.get(i)).getString(NotificationConstants.NOTIFICATION_ID_KEY)));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
}
