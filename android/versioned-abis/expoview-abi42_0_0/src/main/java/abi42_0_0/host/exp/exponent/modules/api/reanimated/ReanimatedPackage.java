package abi42_0_0.host.exp.exponent.modules.api.reanimated;

import abi42_0_0.com.facebook.react.ReactPackage;
import abi42_0_0.com.facebook.react.bridge.NativeModule;
import abi42_0_0.com.facebook.react.bridge.ReactApplicationContext;
import abi42_0_0.com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.List;

public class ReanimatedPackage implements ReactPackage {
  @Override
  public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    return Arrays.<NativeModule>asList(new ReanimatedModule(reactContext));
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    return Arrays.asList();
  }
}
