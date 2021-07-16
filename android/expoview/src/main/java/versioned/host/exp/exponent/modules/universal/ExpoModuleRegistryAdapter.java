package versioned.host.exp.exponent.modules.universal;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;

import org.unimodules.adapters.react.ModuleRegistryAdapter;
import org.unimodules.adapters.react.ReactModuleRegistryProvider;
import org.unimodules.core.ModuleRegistry;
import org.unimodules.core.interfaces.InternalModule;
import org.unimodules.core.interfaces.RegistryLifecycleListener;

import java.util.List;
import java.util.Map;

import expo.modules.updates.manifest.raw.RawManifest;
import host.exp.exponent.kernel.ExperienceKey;
import versioned.host.exp.exponent.modules.api.notifications.channels.ScopedNotificationsChannelsProvider;
import host.exp.exponent.utils.ScopedContext;
import versioned.host.exp.exponent.modules.api.notifications.ScopedNotificationsCategoriesSerializer;
import versioned.host.exp.exponent.modules.universal.av.SharedCookiesDataSourceFactoryProvider;
import versioned.host.exp.exponent.modules.universal.notifications.ScopedExpoNotificationCategoriesModule;
import versioned.host.exp.exponent.modules.universal.notifications.ScopedExpoNotificationPresentationModule;
import versioned.host.exp.exponent.modules.universal.notifications.ScopedServerRegistrationModule;
import versioned.host.exp.exponent.modules.universal.notifications.ScopedNotificationScheduler;
import versioned.host.exp.exponent.modules.universal.notifications.ScopedNotificationsEmitter;
import versioned.host.exp.exponent.modules.universal.notifications.ScopedNotificationsHandler;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedAccelerometerService;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedGravitySensorService;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedGyroscopeService;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedLinearAccelerationSensorService;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedMagnetometerService;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedMagnetometerUncalibratedService;
import versioned.host.exp.exponent.modules.universal.sensors.ScopedRotationVectorSensorService;

public class ExpoModuleRegistryAdapter extends ModuleRegistryAdapter implements ScopedModuleRegistryAdapter {
  public ExpoModuleRegistryAdapter(ReactModuleRegistryProvider moduleRegistryProvider) {
    super(moduleRegistryProvider);
  }

  public List<NativeModule> createNativeModules(ScopedContext scopedContext, ExperienceKey experienceKey, Map<String, Object> experienceProperties, RawManifest manifest, String experienceStableLegacyId, List<NativeModule> otherModules) {
    ModuleRegistry moduleRegistry = mModuleRegistryProvider.get(scopedContext);

    // Overriding sensor services from expo-sensors for scoped implementations using kernel services
    moduleRegistry.registerInternalModule(new ScopedAccelerometerService(experienceKey));
    moduleRegistry.registerInternalModule(new ScopedGravitySensorService(experienceKey));
    moduleRegistry.registerInternalModule(new ScopedGyroscopeService(experienceKey));
    moduleRegistry.registerInternalModule(new ScopedLinearAccelerationSensorService(experienceKey));
    moduleRegistry.registerInternalModule(new ScopedMagnetometerService(experienceKey));
    moduleRegistry.registerInternalModule(new ScopedMagnetometerUncalibratedService(experienceKey));
    moduleRegistry.registerInternalModule(new ScopedRotationVectorSensorService(experienceKey));
    moduleRegistry.registerInternalModule(new SharedCookiesDataSourceFactoryProvider());

    // Overriding expo-constants/ConstantsService -- binding provides manifest and other expo-related constants
    moduleRegistry.registerInternalModule(new ConstantsBinding(scopedContext, experienceProperties, manifest));

    // Overriding expo-file-system FilePermissionModule
    moduleRegistry.registerInternalModule(new ScopedFilePermissionModule(scopedContext));

    // Overriding expo-file-system FileSystemModule
    moduleRegistry.registerExportedModule(new ScopedFileSystemModule(scopedContext));

    // Overriding expo-error-recovery ErrorRecoveryModule
    moduleRegistry.registerExportedModule(new ScopedErrorRecoveryModule(scopedContext, manifest, experienceKey));

    // Overriding expo-permissions ScopedPermissionsService
    moduleRegistry.registerInternalModule(new ScopedPermissionsService(scopedContext, experienceKey));

    // Overriding expo-updates UpdatesService
    moduleRegistry.registerInternalModule(new UpdatesBinding(scopedContext, experienceProperties));

    // Overriding expo-facebook
    moduleRegistry.registerExportedModule(new ScopedFacebookModule(scopedContext));

    // Scoping Amplitude
    moduleRegistry.registerExportedModule(new ScopedAmplitudeModule(scopedContext, experienceStableLegacyId));

    // Overriding expo-firebase-core
    moduleRegistry.registerInternalModule(new ScopedFirebaseCoreService(scopedContext, manifest, experienceKey));

    // Overriding expo-notifications classes
    moduleRegistry.registerExportedModule(new ScopedNotificationsEmitter(scopedContext, experienceKey));
    moduleRegistry.registerExportedModule(new ScopedNotificationsHandler(scopedContext, experienceKey));
    moduleRegistry.registerExportedModule(new ScopedNotificationScheduler(scopedContext, experienceKey));
    moduleRegistry.registerExportedModule(new ScopedExpoNotificationCategoriesModule(scopedContext, experienceKey));
    moduleRegistry.registerExportedModule(new ScopedExpoNotificationPresentationModule(scopedContext, experienceKey));
    moduleRegistry.registerExportedModule(new ScopedServerRegistrationModule(scopedContext));
    moduleRegistry.registerInternalModule(new ScopedNotificationsChannelsProvider(scopedContext, experienceKey));
    moduleRegistry.registerInternalModule(new ScopedNotificationsCategoriesSerializer());

    // Overriding expo-secure-stoore
    moduleRegistry.registerExportedModule(new ScopedSecureStoreModule(scopedContext));

    // ReactAdapterPackage requires ReactContext
    ReactApplicationContext reactContext = (ReactApplicationContext) scopedContext.getContext();
    for (InternalModule internalModule : mReactAdapterPackage.createInternalModules(reactContext)) {
      moduleRegistry.registerInternalModule(internalModule);
    }

    // Overriding ScopedUIManagerModuleWrapper from ReactAdapterPackage
    moduleRegistry.registerInternalModule(new ScopedUIManagerModuleWrapper(reactContext));

    // Adding other modules (not universal) to module registry as consumers.
    // It allows these modules to refer to universal modules.
    for (NativeModule otherModule : otherModules) {
      if (otherModule instanceof RegistryLifecycleListener) {
        moduleRegistry.registerExtraListener((RegistryLifecycleListener) otherModule);
      }
    }

    return getNativeModulesFromModuleRegistry(reactContext, moduleRegistry);
  }

  @Override
  public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    throw new RuntimeException("Use createNativeModules(ReactApplicationContext, ExperienceId, JSONObject, List<NativeModule>) to get a list of native modules.");
  }
}
