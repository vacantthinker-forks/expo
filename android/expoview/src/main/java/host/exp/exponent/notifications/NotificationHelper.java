package host.exp.exponent.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unimodules.core.errors.InvalidArgumentException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import de.greenrobot.event.EventBus;
import expo.modules.updates.manifest.ManifestFactory;
import expo.modules.updates.manifest.raw.RawManifest;
import host.exp.exponent.Constants;
import host.exp.exponent.ExponentManifest;
import host.exp.exponent.analytics.EXL;
import host.exp.exponent.fcm.FcmRegistrationIntentService;
import host.exp.exponent.kernel.ExperienceKey;
import host.exp.exponent.kernel.ExponentUrls;
import host.exp.exponent.kernel.KernelConstants;
import host.exp.exponent.network.ExpoHttpCallback;
import host.exp.exponent.network.ExpoResponse;
import host.exp.exponent.network.ExponentNetwork;
import host.exp.exponent.storage.ExperienceDBObject;
import host.exp.exponent.storage.ExponentDB;
import host.exp.exponent.storage.ExponentSharedPreferences;
import host.exp.exponent.utils.AsyncCondition;
import host.exp.exponent.utils.ColorParser;
import host.exp.exponent.utils.JSONUtils;
import host.exp.expoview.R;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NotificationHelper {

  private static String TAG = NotificationHelper.class.getSimpleName();

  public interface Listener {
    void onSuccess(int id);

    void onFailure(Exception e);
  }

  public interface TokenListener {
    void onSuccess(String token);

    void onFailure(Exception e);
  }

  public static int getColor(
    @Nullable String colorString,
    RawManifest manifest,
    ExponentManifest exponentManifest) {
    @Nullable JSONObject notificationPreferences = manifest.getNotificationPreferences();

    if (colorString == null) {
      colorString = notificationPreferences == null ? null :
        notificationPreferences.optString(ExponentManifest.MANIFEST_NOTIFICATION_COLOR_KEY);
    }

    int color;

    if (colorString != null && ColorParser.isValid(colorString)) {
      color = Color.parseColor(colorString);
    } else {
      color = exponentManifest.getColorFromManifest(manifest);
    }

    return color;
  }

  public static void loadIcon(String url,
                              RawManifest manifest,
                              ExponentManifest exponentManifest,
                              ExponentManifest.BitmapListener bitmapListener) {
    @Nullable JSONObject notificationPreferences = manifest.getNotificationPreferences();
    String iconUrl;

    if (url == null) {
      iconUrl = manifest.getIconUrl();
      if (notificationPreferences != null) {
        iconUrl = notificationPreferences.optString(ExponentManifest.MANIFEST_NOTIFICATION_ICON_URL_KEY, null);
      }
    } else {
      iconUrl = url;
    }

    exponentManifest.loadIconBitmap(iconUrl, bitmapListener);
  }

  public static void getPushNotificationToken(
    final String deviceId,
    final String experienceId,
    final ExponentNetwork exponentNetwork,
    final ExponentSharedPreferences exponentSharedPreferences,
    final TokenListener listener) {
    if (Constants.FCM_ENABLED) {
      FcmRegistrationIntentService.getTokenAndRegister(exponentSharedPreferences.getContext());
    }

    AsyncCondition.wait(ExponentNotificationIntentService.DEVICE_PUSH_TOKEN_KEY, new AsyncCondition.AsyncConditionListener() {
      @Override
      public boolean isReady() {
        return exponentSharedPreferences.getString(ExponentSharedPreferences.FCM_TOKEN_KEY) != null
          || ExponentNotificationIntentService.hasTokenError();
      }

      @Override
      public void execute() {
        String sharedPreferencesToken = exponentSharedPreferences.getString(ExponentSharedPreferences.FCM_TOKEN_KEY);
        if (sharedPreferencesToken == null || sharedPreferencesToken.length() == 0) {
          String message = "No device token found.";
          if (!Constants.FCM_ENABLED) {
            message += " You need to enable FCM in order to get a push token. Follow this guide to set up FCM for your standalone app: https://docs.expo.io/versions/latest/guides/using-fcm";
          }
          listener.onFailure(new Exception(message));
          return;
        }

        JSONObject params = new JSONObject();
        try {
          params.put("deviceId", deviceId);
          params.put("experienceId", experienceId);
          params.put("appId", exponentSharedPreferences.getContext().getApplicationContext().getPackageName());
          params.put("deviceToken", sharedPreferencesToken);
          params.put("type", "fcm");
          params.put("development", false);
        } catch (JSONException e) {
          listener.onFailure(new Exception("Error constructing request"));
          return;
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), params.toString());
        Request request = ExponentUrls.addExponentHeadersToUrl("https://exp.host/--/api/v2/push/getExpoPushToken")
          .header("Content-Type", "application/json")
          .post(body)
          .build();

        exponentNetwork.getClient().call(request, new ExpoHttpCallback() {
          @Override
          public void onFailure(IOException e) {
            listener.onFailure(e);
          }

          @Override
          public void onResponse(ExpoResponse response) throws IOException {
            if (!response.isSuccessful()) {
              listener.onFailure(new Exception("Couldn't get android push token for device"));
              return;
            }

            try {
              JSONObject result = new JSONObject(response.body().string());
              JSONObject data = result.getJSONObject("data");
              listener.onSuccess(data.getString("expoPushToken"));
            } catch (Exception e) {
              listener.onFailure(e);
            }
          }
        });
      }
    });
  }

  public static void createChannel(
    Context context,
    ExperienceKey experienceKey,
    String channelId,
    String channelName,
    HashMap details) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String description = null;
      String importance = null;
      Boolean sound = null;
      Object vibrate = null;
      Boolean badge = null;

      if (details.containsKey(NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY)) {
        importance = (String) details.get(NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY);
      }
      if (details.containsKey(NotificationConstants.NOTIFICATION_CHANNEL_SOUND)) {
        sound = (Boolean) details.get(NotificationConstants.NOTIFICATION_CHANNEL_SOUND);
      }
      if (details.containsKey(NotificationConstants.NOTIFICATION_CHANNEL_VIBRATE)) {
        vibrate = details.get(NotificationConstants.NOTIFICATION_CHANNEL_VIBRATE);
      }
      if (details.containsKey(NotificationConstants.NOTIFICATION_CHANNEL_DESCRIPTION)) {
        description = (String) details.get(NotificationConstants.NOTIFICATION_CHANNEL_DESCRIPTION);
      }
      if (details.containsKey(NotificationConstants.NOTIFICATION_CHANNEL_BADGE)) {
        badge = (Boolean) details.get(NotificationConstants.NOTIFICATION_CHANNEL_BADGE);
      }

      createChannel(
        context,
        experienceKey,
        channelId,
        channelName,
        description,
        importance,
        sound,
        vibrate,
        badge
      );
    } else {
      // since channels do not exist on Android 7.1 and below, we'll save the settings in shared
      // preferences and apply them to individual notifications that have this channelId from now on
      // this is essentially a "polyfill" of notification channels for Android 7.1 and below
      // and means that devs don't have to worry about supporting both versions of Android at once
      new ExponentNotificationManager(context).saveChannelSettings(experienceKey, channelId, details);
    }
  }

  public static void createChannel(
    Context context,
    ExperienceKey experienceKey,
    String channelId,
    JSONObject details) {
    try {
      // we want to throw immediately if there is no channel name
      String channelName = details.getString(NotificationConstants.NOTIFICATION_CHANNEL_NAME);
      String description = null;
      String priority = null;
      Boolean sound = null;
      Boolean badge = null;

      if (!details.isNull(NotificationConstants.NOTIFICATION_CHANNEL_DESCRIPTION)) {
        description = details.optString(NotificationConstants.NOTIFICATION_CHANNEL_DESCRIPTION);
      }
      if (!details.isNull(NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY)) {
        priority = details.optString(NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY);
      }
      if (!details.isNull(NotificationConstants.NOTIFICATION_CHANNEL_SOUND)) {
        sound = details.optBoolean(NotificationConstants.NOTIFICATION_CHANNEL_SOUND);
      }
      if (!details.isNull(NotificationConstants.NOTIFICATION_CHANNEL_BADGE)) {
        badge = details.optBoolean(NotificationConstants.NOTIFICATION_CHANNEL_BADGE, true);
      }

      Object vibrate;
      JSONArray jsonArray = details.optJSONArray(NotificationConstants.NOTIFICATION_CHANNEL_VIBRATE);
      if (jsonArray != null) {
        ArrayList<Double> vibrateArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
          vibrateArrayList.add(jsonArray.getDouble(i));
        }
        vibrate = vibrateArrayList;
      } else {
        vibrate = details.optBoolean(NotificationConstants.NOTIFICATION_CHANNEL_VIBRATE, false);
      }

      createChannel(
        context,
        experienceKey,
        channelId,
        channelName,
        description,
        priority,
        sound,
        vibrate,
        badge
      );
    } catch (Exception e) {
      EXL.e(TAG, "Could not create channel from stored JSON Object: " + e.getMessage());
    }
  }

  private static void createChannel(
    Context context,
    ExperienceKey experienceKey,
    String channelId,
    String channelName,
    String description,
    String importanceString,
    Boolean sound,
    Object vibrate,
    Boolean badge) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_DEFAULT;

      if (importanceString != null) {
        switch (importanceString) {
          case NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY_MAX:
            importance = NotificationManager.IMPORTANCE_MAX;
            break;
          case NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY_HIGH:
            importance = NotificationManager.IMPORTANCE_HIGH;
            break;
          case NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY_LOW:
            importance = NotificationManager.IMPORTANCE_LOW;
            break;
          case NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY_MIN:
            importance = NotificationManager.IMPORTANCE_MIN;
            break;
          default:
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        }
      }

      NotificationChannel channel = new NotificationChannel(ExponentNotificationManager.getScopedChannelId(experienceKey, channelId), channelName, importance);

      // sound is now on by default for channels
      if (sound == null || !sound) {
        channel.setSound(null, null);
      }

      if (vibrate != null) {
        if (vibrate instanceof ArrayList) {
          ArrayList vibrateArrayList = (ArrayList) vibrate;
          long[] pattern = new long[vibrateArrayList.size()];
          for (int i = 0; i < vibrateArrayList.size(); i++) {
            pattern[i] = ((Double) vibrateArrayList.get(i)).intValue();
          }
          channel.setVibrationPattern(pattern);
        } else if (vibrate instanceof Boolean && (Boolean) vibrate) {
          channel.setVibrationPattern(new long[]{0, 500});
        }
      }

      if (description != null) {
        channel.setDescription(description);
      }

      if (badge != null) {
        channel.setShowBadge(badge);
      }

      new ExponentNotificationManager(context).createNotificationChannel(experienceKey, channel);
    }
  }

  public static void maybeCreateLegacyStoredChannel(Context context, ExperienceKey experienceKey, String channelId, HashMap details) {
    // no version check here because if we're on Android 7.1 or below, we still want to save
    // the channel in shared preferences
    NotificationChannel existingChannel = new ExponentNotificationManager(context).getNotificationChannel(experienceKey, channelId);
    if (existingChannel == null && details.containsKey(NotificationConstants.NOTIFICATION_CHANNEL_NAME)) {
      createChannel(context, experienceKey, channelId, (String) details.get(NotificationConstants.NOTIFICATION_CHANNEL_NAME), details);
    }
  }

  public static void deleteChannel(Context context, ExperienceKey experienceKey, String channelId) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      new ExponentNotificationManager(context).deleteNotificationChannel(experienceKey, channelId);
    } else {
      // deleting a channel on O+ still retains all its settings, so doing nothing here emulates that
    }
  }

  public static void showNotification(
          final Context context,
          final int id,
          final HashMap details,
          final ExponentManifest exponentManifest,
          final Listener listener) {
    final ExponentNotificationManager manager = new ExponentNotificationManager(context);

    String notificationScopeKey = (String) details.get(NotificationConstants.NOTIFICATION_EXPERIENCE_SCOPE_KEY_KEY);
    final String experienceScopeKey = notificationScopeKey != null ? notificationScopeKey : (String) details.get(NotificationConstants.NOTIFICATION_EXPERIENCE_ID_KEY);

    ExponentDB.experienceScopeKeyToExperience(experienceScopeKey, new ExponentDB.ExperienceResultListener() {
      @Override
      public void onSuccess(ExperienceDBObject experience) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            RawManifest manifest;
            ExperienceKey experienceKey;
            try {
              manifest = ManifestFactory.INSTANCE.getRawManifestFromJson(new JSONObject(experience.manifest));
              experienceKey = ExperienceKey.fromRawManifest(manifest);
            } catch (JSONException e) {
              listener.onFailure(new Exception("Couldn't deserialize JSON for experience scope key " + experienceScopeKey));
              return;
            }

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context,
                    ExponentNotificationManager.getScopedChannelId(experienceKey, NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID));

            builder.setSmallIcon(Constants.isStandaloneApp() ? R.drawable.shell_notification_icon : R.drawable.notification_icon);
            builder.setAutoCancel(true);

            final HashMap data = (HashMap) details.get("data");

            if (data.containsKey("channelId")) {
              String channelId = (String) data.get("channelId");
              builder.setChannelId(ExponentNotificationManager.getScopedChannelId(experienceKey, channelId));

              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // if we don't yet have a channel matching this ID, check shared preferences --
                // it's possible this device has just been upgraded to Android 8+ and the channel
                // needs to be created in the system
                if (manager.getNotificationChannel(experienceKey, channelId) == null) {
                  JSONObject storedChannelDetails = manager.readChannelSettings(experienceKey, channelId);
                  if (storedChannelDetails != null) {
                    createChannel(context, experienceKey, channelId, storedChannelDetails);
                  }
                }
              } else {
                // on Android 7.1 and below, read channel settings for sound, priority, and vibrate from shared preferences
                // and apply these settings to the notification individually, since channels do not exist
                JSONObject storedChannelDetails = manager.readChannelSettings(experienceKey, channelId);
                if (storedChannelDetails != null) {
                  if (storedChannelDetails.optBoolean(NotificationConstants.NOTIFICATION_CHANNEL_SOUND, false)) {
                    builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
                  }

                  String priorityString = storedChannelDetails.optString(NotificationConstants.NOTIFICATION_CHANNEL_PRIORITY);
                  int priority;
                  switch (priorityString) {
                    case "max":
                      priority = NotificationCompat.PRIORITY_MAX;
                      break;
                    case "high":
                      priority = NotificationCompat.PRIORITY_HIGH;
                      break;
                    case "low":
                      priority = NotificationCompat.PRIORITY_LOW;
                      break;
                    case "min":
                      priority = NotificationCompat.PRIORITY_MIN;
                      break;
                    default:
                      priority = NotificationCompat.PRIORITY_DEFAULT;
                  }
                  builder.setPriority(priority);

                  try {
                    JSONArray vibrateJsonArray = storedChannelDetails.optJSONArray(NotificationConstants.NOTIFICATION_CHANNEL_VIBRATE);
                    if (vibrateJsonArray != null) {
                      long[] pattern = new long[vibrateJsonArray.length()];
                      for (int i = 0; i < vibrateJsonArray.length(); i++) {
                        pattern[i] = ((Double) vibrateJsonArray.getDouble(i)).intValue();
                      }
                      builder.setVibrate(pattern);
                    } else if (storedChannelDetails.optBoolean(NotificationConstants.NOTIFICATION_CHANNEL_VIBRATE, false)) {
                      builder.setVibrate(new long[]{0, 500});
                    }
                  } catch (Exception e) {
                    EXL.e(TAG, "Failed to set vibrate settings on notification from stored channel: " + e.getMessage());
                  }
                } else {
                  EXL.e(TAG, "No stored channel found for " + experienceScopeKey + ": " + channelId);
                }
              }
            } else {
              // make a default channel so that people don't have to explicitly create a channel to see notifications
              createChannel(
                      context,
                      experienceKey,
                      NotificationConstants.NOTIFICATION_DEFAULT_CHANNEL_ID,
                      context.getString(R.string.default_notification_channel_group),
                      new HashMap());
            }

            if (data.containsKey("title")) {
              String title = (String) data.get("title");
              builder.setContentTitle(title);
              builder.setTicker(title);
            }

            if (data.containsKey("body")) {
              builder.setContentText((String) data.get("body"));
              builder.setStyle(new NotificationCompat.BigTextStyle().
                      bigText((String) data.get("body")));
            }

            if (data.containsKey("count")) {
              builder.setNumber(((Double) data.get("count")).intValue());
            }

            if (data.containsKey("sticky")) {
              builder.setOngoing((Boolean) data.get("sticky"));
            }

            Intent intent;

            if (data.containsKey("link")) {
              intent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) data.get("link")));
            } else {
              Class activityClass = KernelConstants.MAIN_ACTIVITY_CLASS;
              intent = new Intent(context, activityClass);
              intent.putExtra(KernelConstants.NOTIFICATION_MANIFEST_URL_KEY, experience.manifestUrl);
            }

            final String body;
            try {
              body = data.containsKey("data") ? JSONUtils.getJSONString(data.get("data")) : "";
            } catch (JSONException e) {
              listener.onFailure(new Exception("Couldn't deserialize JSON for experience scope key " + experienceScopeKey));
              return;
            }

            final ReceivedNotificationEvent notificationEvent = new ReceivedNotificationEvent(experienceScopeKey, body, id, false, false);

            intent.putExtra(KernelConstants.NOTIFICATION_KEY, body); // deprecated
            intent.putExtra(KernelConstants.NOTIFICATION_OBJECT_KEY, notificationEvent.toJSONObject(null).toString());

            PendingIntent contentIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            if (data.containsKey("categoryId")) {
              final String manifestUrl = experience.manifestUrl;
              NotificationActionCenter.setCategory((String) data.get("categoryId"), builder, context, new IntentProvider() {
                @Override
                public Intent provide() {
                  Class activityClass = KernelConstants.MAIN_ACTIVITY_CLASS;
                  Intent intent = new Intent(context, activityClass);
                  intent.putExtra(KernelConstants.NOTIFICATION_MANIFEST_URL_KEY, manifestUrl);
                  final ReceivedNotificationEvent notificationEvent = new ReceivedNotificationEvent(experienceScopeKey, body, id, false, false);
                  intent.putExtra(KernelConstants.NOTIFICATION_KEY, body); // deprecated
                  intent.putExtra(KernelConstants.NOTIFICATION_OBJECT_KEY, notificationEvent.toJSONObject(null).toString());
                  return intent;
                }
              });
            }

            int color = NotificationHelper.getColor(
                    data.containsKey("color") ? (String) data.get("color") : null,
                    manifest,
                    exponentManifest);

            builder.setColor(color);

            NotificationHelper.loadIcon(
                    data.containsKey("icon") ? (String) data.get("icon") : null,
                    manifest,
                    exponentManifest,
                    new ExponentManifest.BitmapListener() {
                      @Override
                      public void onLoadBitmap(Bitmap bitmap) {
                        if (data.containsKey("icon")) {
                          builder.setLargeIcon(bitmap);
                        }
                        manager.notify(experienceKey, id, builder.build());
                        EventBus.getDefault().post(notificationEvent);
                        listener.onSuccess(id);
                      }
                    });

          }
        }).start();
      }

      @Override
      public void onFailure() {
        listener.onFailure(new Exception("No experience found for scope key " + experienceScopeKey));
      }
    });
  }

  public static void scheduleLocalNotification(
    final Context context,
    final int id,
    final HashMap<String, Object> data,
    final HashMap options,
    final ExperienceKey experienceKey,
    final Listener listener) {

    HashMap<String, java.io.Serializable> details = new HashMap<>();
    details.put("data", data);
    details.put(NotificationConstants.NOTIFICATION_EXPERIENCE_ID_KEY, experienceKey.getScopeKey());
    details.put(NotificationConstants.NOTIFICATION_EXPERIENCE_SCOPE_KEY_KEY, experienceKey.getScopeKey());

    long time = 0;

    if (options.containsKey("time")) {
      try {
        Object suppliedTime = options.get("time");
        if (suppliedTime instanceof Number) {
          time = ((Number) suppliedTime).longValue() - System.currentTimeMillis();
        } else if (suppliedTime instanceof String) { // TODO: DELETE WHEN SDK 32 IS DEPRECATED
          DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
          format.setTimeZone(TimeZone.getTimeZone("UTC"));
          time = format.parse((String) suppliedTime).getTime() - System.currentTimeMillis();
        } else {
          throw new InvalidArgumentException("Invalid time provided: " + suppliedTime);
        }
      } catch (Exception e) {
        listener.onFailure(e);
        return;
      }
    }

    time += SystemClock.elapsedRealtime();

    ExponentNotificationManager manager = new ExponentNotificationManager(context);

    Long interval = null;

    if (options.containsKey("repeat")) {
      switch ((String) options.get("repeat")) {
        case "minute":
          interval = DateUtils.MINUTE_IN_MILLIS;
          break;
        case "hour":
          interval = DateUtils.HOUR_IN_MILLIS;
          break;
        case "day":
          interval = DateUtils.DAY_IN_MILLIS;
          break;
        case "week":
          interval = DateUtils.WEEK_IN_MILLIS;
          break;
        case "month":
          interval = DateUtils.DAY_IN_MILLIS * 30;
          break;
        case "year":
          interval = DateUtils.DAY_IN_MILLIS * 365;
          break;
        default:
          listener.onFailure(new Exception("Invalid repeat interval specified"));
          return;
      }
    } else if (options.containsKey("intervalMs")) {
      interval = (Long) options.get("intervalMs");
    }

    try {
      manager.schedule(experienceKey, id, details, time, interval);
      listener.onSuccess(id);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }
}
