package abi42_0_0.expo.modules.gl;

import android.content.Context;

import java.util.Collections;
import java.util.List;

import abi42_0_0.org.unimodules.core.ExportedModule;
import abi42_0_0.org.unimodules.core.BasePackage;
import abi42_0_0.org.unimodules.core.ViewManager;

public class GLPackage extends BasePackage {
  @Override
  public List<ExportedModule> createExportedModules(Context context) {
    return Collections.singletonList((ExportedModule) new GLObjectManagerModule(context));
  }

  @Override
  public List<ViewManager> createViewManagers(Context context) {
    return Collections.singletonList((ViewManager) new GLViewManager());
  }
}
