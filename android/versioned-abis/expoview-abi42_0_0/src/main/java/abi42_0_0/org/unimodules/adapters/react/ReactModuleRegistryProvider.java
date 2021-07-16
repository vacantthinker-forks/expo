package abi42_0_0.org.unimodules.adapters.react;

import android.content.Context;

import abi42_0_0.com.facebook.react.ReactPackage;
import abi42_0_0.com.facebook.react.bridge.ReactApplicationContext;

import abi42_0_0.org.unimodules.core.ExportedModule;
import abi42_0_0.org.unimodules.core.ModuleRegistry;
import abi42_0_0.org.unimodules.core.ModuleRegistryProvider;
import abi42_0_0.org.unimodules.core.ViewManager;
import abi42_0_0.org.unimodules.core.interfaces.Function;
import abi42_0_0.org.unimodules.core.interfaces.InternalModule;
import abi42_0_0.org.unimodules.core.interfaces.Package;
import org.unimodules.core.interfaces.SingletonModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Since React Native v0.55, {@link abi42_0_0.com.facebook.react.ReactPackage#createViewManagers(ReactApplicationContext)}
 * gets called only once per lifetime of {@link abi42_0_0.com.facebook.react.ReactInstanceManager}.
 * <p>
 * To make @unimodules/react-native-adapter compatible with this change we have to remember view managers collection
 * which is returned in {@link ModuleRegistryAdapter#createViewManagers(ReactApplicationContext)}
 * only once (and managers returned this one time will persist "forever").
 */
public class ReactModuleRegistryProvider extends ModuleRegistryProvider {
  private Collection<ViewManager> mViewManagers;
  private Collection<abi42_0_0.com.facebook.react.uimanager.ViewManager> mReactViewManagers;
  private Collection<SingletonModule> mSingletonModules;

  public ReactModuleRegistryProvider(List<Package> initialPackages) {
    this(initialPackages, null);
  }

  public ReactModuleRegistryProvider(List<Package> initialPackages, List<SingletonModule> singletonModules) {
    super(initialPackages);
    mSingletonModules = singletonModules;
  }

  @Override
  public ModuleRegistry get(Context context) {
    Collection<InternalModule> internalModules = new ArrayList<>();
    Collection<ExportedModule> exportedModules = new ArrayList<>();

    ReactPackagesProvider reactPackagesProvider = new ReactPackagesProvider();

    for (Package pkg : getPackages()) {
      internalModules.addAll(pkg.createInternalModules(context));
      exportedModules.addAll(pkg.createExportedModules(context));

      if (pkg instanceof ReactPackage) {
        reactPackagesProvider.addPackage((ReactPackage) pkg);
      }
    }
    internalModules.add(reactPackagesProvider);

    return new ModuleRegistry(internalModules, exportedModules, getViewManagers(context), getSingletonModules(context));
  }

  private Collection<SingletonModule> getSingletonModules(Context context) {
    // If singleton modules were provided to registry provider, then just pass them to module registry.

    if (mSingletonModules != null) {
      return mSingletonModules;
    }
    Collection<SingletonModule> singletonModules = new ArrayList<>();

    for (Package pkg : getPackages()) {
      singletonModules.addAll(pkg.createSingletonModules(context));
    }
    return singletonModules;
  }

  /* package */ Collection<ViewManager> getViewManagers(Context context) {
    if (mViewManagers != null) {
      return mViewManagers;
    }

    mViewManagers = new HashSet<>();
    mViewManagers.addAll(createViewManagers(context));
    return mViewManagers;
  }

  /* package */ Collection<abi42_0_0.com.facebook.react.uimanager.ViewManager> getReactViewManagers(ReactApplicationContext context) {
    if (mReactViewManagers != null) {
      return mReactViewManagers;
    }

    mReactViewManagers = new HashSet<>();
    for (Package pkg : getPackages()) {
      if (pkg instanceof ReactPackage) {
        mReactViewManagers.addAll(((ReactPackage) pkg).createViewManagers(context));
      }
    }
    return mReactViewManagers;
  }
}
