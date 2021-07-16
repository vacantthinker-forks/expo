// Copyright 2015-present 650 Industries. All rights reserved.

package abi42_0_0.expo.modules.constants;

import android.content.Context;

import java.util.Map;

import abi42_0_0.org.unimodules.core.ExportedModule;
import abi42_0_0.org.unimodules.core.ModuleRegistry;
import abi42_0_0.org.unimodules.core.Promise;
import abi42_0_0.org.unimodules.core.interfaces.ExpoMethod;

import abi42_0_0.expo.modules.interfaces.constants.ConstantsInterface;

public class ConstantsModule extends ExportedModule {
  private ModuleRegistry mModuleRegistry;

  public ConstantsModule(Context context) {
    super(context);
  }

  @Override
  public Map<String, Object> getConstants() {
    ConstantsInterface constantsService = mModuleRegistry.getModule(ConstantsInterface.class);
    return constantsService.getConstants();
  }

  @Override
  public String getName() {
    return "ExponentConstants";
  }

  @Override
  public void onCreate(ModuleRegistry moduleRegistry) {
    mModuleRegistry = moduleRegistry;
  }

  @ExpoMethod
  public void getWebViewUserAgentAsync(Promise promise) {
    String userAgent = System.getProperty("http.agent");
    promise.resolve(userAgent);
  }
}
