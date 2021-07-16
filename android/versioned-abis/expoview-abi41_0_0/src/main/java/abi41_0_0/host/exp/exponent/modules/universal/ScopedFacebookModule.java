package abi41_0_0.host.exp.exponent.modules.universal;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;
import abi41_0_0.org.unimodules.core.Promise;
import abi41_0_0.org.unimodules.core.arguments.ReadableArguments;
import abi41_0_0.org.unimodules.core.interfaces.LifecycleEventListener;

import abi41_0_0.expo.modules.facebook.FacebookModule;
import host.exp.exponent.kernel.ExperienceKey;

public class ScopedFacebookModule extends FacebookModule implements LifecycleEventListener {
  private final static String ERR_FACEBOOK_UNINITIALIZED = "ERR_FACEBOOK_UNINITIALIZED";

  private boolean mIsInitialized = false;

  public ScopedFacebookModule(Context context) {
    super(context);
  }

  @Override
  public void onHostResume() {
    if (mAppId != null) {
      FacebookSdk.setApplicationId(mAppId);
    }
    if (mAppName != null) {
      FacebookSdk.setApplicationName(mAppName);
    }
  }

  @Override
  public void initializeAsync(ReadableArguments options, final Promise promise) {
    mIsInitialized = true;
    super.initializeAsync(options, promise);
  }

  @Override
  public void logInWithReadPermissionsAsync(ReadableArguments config, Promise promise) {
    if (!mIsInitialized) {
      promise.reject(ERR_FACEBOOK_UNINITIALIZED, "Facebook SDK has not been initialized yet.");
    }
    super.logInWithReadPermissionsAsync(config, promise);
  }

  @Override
  public void getAuthenticationCredentialAsync(Promise promise) {
    if (!mIsInitialized) {
      promise.reject(ERR_FACEBOOK_UNINITIALIZED, "Facebook SDK has not been initialized yet.");
    }
    super.getAuthenticationCredentialAsync(promise);
  }

  @Override
  public void logOutAsync(final Promise promise) {
    if (!mIsInitialized) {
      promise.reject(ERR_FACEBOOK_UNINITIALIZED, "Facebook SDK has not been initialized yet.");
    }
    super.logOutAsync(promise);
  }

  @Override
  public void onHostPause() {
    FacebookSdk.setApplicationId(null);
    FacebookSdk.setApplicationName(null);
  }

  @Override
  public void onHostDestroy() {
    // do nothing
  }
}
