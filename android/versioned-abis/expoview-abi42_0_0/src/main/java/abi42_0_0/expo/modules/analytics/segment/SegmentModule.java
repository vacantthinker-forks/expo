// Copyright 2015-present 650 Industries. All rights reserved.

package abi42_0_0.expo.modules.analytics.segment;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.segment.analytics.Analytics;
import com.segment.analytics.Options;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;

import java.util.HashMap;
import java.util.Map;

import abi42_0_0.org.unimodules.core.ExportedModule;
import abi42_0_0.org.unimodules.core.ModuleRegistry;
import abi42_0_0.org.unimodules.core.Promise;
import abi42_0_0.org.unimodules.core.interfaces.ExpoMethod;

import abi42_0_0.expo.modules.interfaces.constants.ConstantsInterface;

public class SegmentModule extends ExportedModule {
  private static final String NAME = "ExponentSegment";
  private static final String ENABLED_PREFERENCE_KEY = "enabled";
  private static final String TAG = SegmentModule.class.getSimpleName();

  private static int sCurrentTag = 0;

  private Context mContext;
  private Analytics mClient;
  private ConstantsInterface mConstants;
  // We have to keep track of `enabled` on our own.
  // Since we have to change tag every time (see commit 083f051), Segment may or may not properly apply
  // remembered preference to the instance. The module in a standalone app would start disabled
  // (settings = { 0 => disabled }, tag = 0) but after OTA update it would reload with
  // (settings = { 0 => disabled }, tag = 1) and segment would become enabled if the app does not
  // disable it on start.
  private SharedPreferences mSharedPreferences;

  public SegmentModule(Context context) {
    super(context);
    mContext = context;
    mSharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
  }

  private static Traits readableMapToTraits(Map<String, Object> properties) {
    Traits traits = new Traits();
    for (String key : properties.keySet()) {
      Object value = properties.get(key);
      if (value instanceof Map) {
        traits.put(key, coalesceAnonymousMapToJsonObject((Map) value));
      } else {
        traits.put(key, value);
      }
    }
    return traits;
  }

  private static Map<String, Object> coalesceAnonymousMapToJsonObject(Map map) {
    Map<String, Object> validObject = new HashMap<>();
    for (Object key : map.keySet()) {
      if (key instanceof String) {
        Object value = map.get(key);
        if (value instanceof Map) {
          validObject.put((String) key, coalesceAnonymousMapToJsonObject((Map) value));
        } else {
          validObject.put((String) key, value);
        }
      }
    }
    return validObject;
  }

  private static Options readableMapToOptions(Map<String, Object> properties) {
    Options options = new Options();
    if (properties != null) {
      for (Map.Entry<String, Object> entry : properties.entrySet()) {
        String keyName = entry.getKey();
        if (keyName.equals("context") && entry.getValue() != null) {
          Map<String, Object> contexts = (Map) entry.getValue();
          for (Map.Entry<String, Object> context : contexts.entrySet()) {
            options.putContext(context.getKey(), context.getValue());
          }
        } else if (keyName.equals("integrations") && entry.getValue() != null) {
          options = addIntegrationsToOptions(options, (Map) entry.getValue());
        }
      }
    }
    return options;
  }

  private static Options addIntegrationsToOptions(Options options, Map<String,Object> integrations) {
    for (Map.Entry<String, Object> integration : integrations.entrySet()) {
      String integrationKey = integration.getKey();
      if (integration.getValue() instanceof Map) {
        Map integrationOptions = (Map) integration.getValue();
        if (integrationOptions.get("enabled") instanceof Boolean) {
          boolean enabled = (Boolean) integrationOptions.get("enabled");
          options.setIntegration(integrationKey, enabled);
        } else if (integrationOptions.get("enabled") instanceof String) {
          String enabled = (String) integrationOptions.get("enabled");
          options.setIntegration(integrationKey, Boolean.valueOf(enabled));
        }
        if (integrationOptions.get("options") instanceof Map) {
          Map<String, Object> jsonOptions = coalesceAnonymousMapToJsonObject((Map) integrationOptions.get("options"));
          options.setIntegrationOptions(integrationKey, jsonOptions);
        }
      }
    }
    return options;
  }

  private static Properties readableMapToProperties(Map<String, Object> properties) {
    Properties result = new Properties();
    for (String key : properties.keySet()) {
      Object value = properties.get(key);
      if (value instanceof Map) {
        result.put(key, coalesceAnonymousMapToJsonObject((Map) value));
      } else {
        result.put(key, value);
      }
    }
    return result;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @ExpoMethod
  public void initialize(final String writeKey, Promise promise) {
    Analytics.Builder builder = new Analytics.Builder(mContext, writeKey);
    builder.experimentalUseNewLifecycleMethods(false);
    builder.tag(Integer.toString(sCurrentTag++));
    builder.use(FirebaseIntegration.FACTORY);
    mClient = builder.build();
    mClient.optOut(!getEnabledPreferenceValue());
    promise.resolve(null);
  }

  @ExpoMethod
  public void identify(final String userId, Promise promise) {
    if (mClient != null) {
      mClient.identify(userId);
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void identifyWithTraits(final String userId, final Map<String, Object> properties, @Nullable final Map<String, Object> options, Promise promise) {
    if (mClient != null) {
      mClient.identify(userId, readableMapToTraits(properties), readableMapToOptions(options));
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void track(final String eventName, Promise promise) {
    if (mClient != null) {
      mClient.track(eventName);
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void trackWithProperties(final String eventName, final Map<String, Object> properties, @Nullable final Map<String, Object> options, Promise promise) {
    if (mClient != null) {
      mClient.track(eventName, readableMapToProperties(properties), readableMapToOptions(options));
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void alias(final String newId, final Map<String, Object> options, Promise promise) {
    Analytics client = mClient;
    if (client != null) {
      client.alias(newId, readableMapToOptions(options));
      promise.resolve(null);
    } else {
      promise.reject("E_NO_SEG", "Segment instance has not been initialized yet, have you tried calling Segment.initialize prior to calling Segment.alias?");
    }
  }

  @ExpoMethod
  public void group(final String groupId, Promise promise) {
    if (mClient != null) {
      mClient.group(groupId);
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void groupWithTraits(final String groupId, final Map<String, Object> properties, @Nullable final Map<String, Object> options, Promise promise) {
    if (mClient != null) {
      mClient.group(groupId, readableMapToTraits(properties), readableMapToOptions(options));
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void screen(final String screenName, Promise promise) {
    if (mClient != null) {
      mClient.screen(screenName);
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void screenWithProperties(final String screenName, final Map<String, Object> properties, @Nullable final Map<String, Object> options, Promise promise) {
    if (mClient != null) {
      mClient.screen(null, screenName, readableMapToProperties(properties), readableMapToOptions(options));
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void flush(Promise promise) {
    if (mClient != null) {
      mClient.flush();
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void reset(Promise promise) {
    if (mClient != null) {
      mClient.reset();
    }
    promise.resolve(null);
  }

  @ExpoMethod
  public void getEnabledAsync(final Promise promise) {
    promise.resolve(getEnabledPreferenceValue());
  }

  @ExpoMethod
  public void setEnabledAsync(final boolean enabled, final Promise promise) {
    if (mConstants.getAppOwnership().equals("expo")) {
      promise.reject("E_UNSUPPORTED", "Setting Segment's `enabled` is not supported in Expo Go.");
      return;
    }
    mSharedPreferences.edit().putBoolean(ENABLED_PREFERENCE_KEY, enabled).apply();
    if (mClient != null) {
      mClient.optOut(!enabled);
    }
    promise.resolve(null);
  }

  @Override
  public void onCreate(ModuleRegistry moduleRegistry) {
    mConstants = null;
    if (moduleRegistry != null) {
      mConstants = moduleRegistry.getModule(ConstantsInterface.class);
    }
  }

  private boolean getEnabledPreferenceValue() {
    return mSharedPreferences.getBoolean(ENABLED_PREFERENCE_KEY, true);
  }
}
