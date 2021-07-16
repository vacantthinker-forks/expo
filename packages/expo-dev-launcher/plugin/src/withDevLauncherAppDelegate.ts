import { ConfigPlugin, WarningAggregator, withAppDelegate } from '@expo/config-plugins';
import semver from 'semver';

import { resolveExpoUpdatesVersion } from './resolveExpoUpdatesVersion';

const DEV_LAUNCHER_APP_DELEGATE_SOURCE_FOR_URL = `  #if defined(EX_DEV_LAUNCHER_ENABLED)
  return [[EXDevLauncherController sharedInstance] sourceUrl];
  #else
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
  #endif`;
const DEV_LAUNCHER_APP_DELEGATE_ON_DEEP_LINK = `#if defined(EX_DEV_LAUNCHER_ENABLED)
  if ([EXDevLauncherController.sharedInstance onDeepLink:url options:options]) {
    return true;
  }
  #endif
  return [RCTLinkingManager application:application openURL:url options:options];`;
const DEV_LAUNCHER_APP_DELEGATE_IOS_IMPORT = `
#if defined(EX_DEV_LAUNCHER_ENABLED)
#include <EXDevLauncher/EXDevLauncherController.h>
#endif`;
const DEV_LAUNCHER_UPDATES_APP_DELEGATE_IOS_IMPORT = `
#if defined(EX_DEV_LAUNCHER_ENABLED)
#include <EXDevLauncher/EXDevLauncherController.h>
#import <EXUpdates/EXUpdatesDevLauncherController.h>
#endif`;
const DEV_LAUNCHER_APP_DELEGATE_CONTROLLER_DELEGATE = `
#if defined(EX_DEV_LAUNCHER_ENABLED)
@implementation AppDelegate (EXDevLauncherControllerDelegate)

- (void)devLauncherController:(EXDevLauncherController *)developmentClientController
    didStartWithSuccess:(BOOL)success
{
  developmentClientController.appBridge = [self initializeReactNativeApp];
  EXSplashScreenService *splashScreenService = (EXSplashScreenService *)[UMModuleRegistryProvider getSingletonModuleForClass:[EXSplashScreenService class]];
  [splashScreenService showSplashScreenFor:self.window.rootViewController];
}

@end
#endif
`;
const DEV_LAUNCHER_APP_DELEGATE_INIT = `#if defined(EX_DEV_LAUNCHER_ENABLED)
        EXDevLauncherController *controller = [EXDevLauncherController sharedInstance];
        [controller startWithWindow:self.window delegate:(id<EXDevLauncherControllerDelegate>)self launchOptions:launchOptions];
      #else
        [self initializeReactNativeApp];
      #endif`;
const DEV_LAUNCHER_UPDATES_APP_DELEGATE_INIT = `EXDevLauncherController *controller = [EXDevLauncherController sharedInstance];
        controller.updatesInterface = [EXUpdatesDevLauncherController sharedInstance];`;

const DEV_LAUNCHER_APP_DELEGATE_BRIDGE = `#if defined(EX_DEV_LAUNCHER_ENABLED)
    NSDictionary *launchOptions = [EXDevLauncherController.sharedInstance getLaunchOptions];
  #else
    NSDictionary *launchOptions = self.launchOptions;
  #endif
  
    RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];`;

const DEV_MENU_IMPORT = `@import EXDevMenu;`;
const DEV_MENU_IOS_INIT = `
#if defined(EX_DEV_MENU_ENABLED)
  [DevMenuManager configureWithBridge:bridge];
#endif`;

export function modifyAppDelegate(appDelegate: string, expoUpdatesVersion: string | null = null) {
  const shouldAddUpdatesIntegration =
    expoUpdatesVersion != null && semver.gt(expoUpdatesVersion, '0.6.0');

  if (
    !appDelegate.includes(DEV_LAUNCHER_APP_DELEGATE_IOS_IMPORT) &&
    !appDelegate.includes(DEV_LAUNCHER_UPDATES_APP_DELEGATE_IOS_IMPORT)
  ) {
    const lines = appDelegate.split('\n');
    lines.splice(
      1,
      0,
      shouldAddUpdatesIntegration
        ? DEV_LAUNCHER_UPDATES_APP_DELEGATE_IOS_IMPORT
        : DEV_LAUNCHER_APP_DELEGATE_IOS_IMPORT
    );

    appDelegate = lines.join('\n');
  }

  if (!appDelegate.includes(DEV_LAUNCHER_APP_DELEGATE_INIT)) {
    appDelegate = appDelegate.replace(
      /(didFinishLaunchingWithOptions([^}])*)\[self initializeReactNativeApp\];(([^}])*})/,
      `$1${DEV_LAUNCHER_APP_DELEGATE_INIT}$3`
    );
  }

  if (
    shouldAddUpdatesIntegration &&
    !appDelegate.includes(DEV_LAUNCHER_UPDATES_APP_DELEGATE_INIT)
  ) {
    appDelegate = appDelegate.replace(
      'EXDevLauncherController *controller = [EXDevLauncherController sharedInstance];',
      DEV_LAUNCHER_UPDATES_APP_DELEGATE_INIT
    );
  }

  if (!appDelegate.includes(DEV_LAUNCHER_APP_DELEGATE_BRIDGE)) {
    appDelegate = appDelegate.replace(
      'RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:self.launchOptions];',
      DEV_LAUNCHER_APP_DELEGATE_BRIDGE
    );
  }

  if (!appDelegate.includes(DEV_LAUNCHER_APP_DELEGATE_SOURCE_FOR_URL)) {
    appDelegate = appDelegate.replace(
      'return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];',
      DEV_LAUNCHER_APP_DELEGATE_SOURCE_FOR_URL
    );
  }

  if (!appDelegate.includes(DEV_LAUNCHER_APP_DELEGATE_ON_DEEP_LINK)) {
    appDelegate = appDelegate.replace(
      'return [RCTLinkingManager application:application openURL:url options:options];',
      DEV_LAUNCHER_APP_DELEGATE_ON_DEEP_LINK
    );
  }

  if (!appDelegate.includes(DEV_LAUNCHER_APP_DELEGATE_CONTROLLER_DELEGATE)) {
    appDelegate += DEV_LAUNCHER_APP_DELEGATE_CONTROLLER_DELEGATE;
  }

  if (!appDelegate.includes(DEV_MENU_IMPORT)) {
    // expo-dev-launcher is responsible for initializing the expo-dev-menu.
    // We need to remove init block from AppDelegate.
    appDelegate = appDelegate.replace(DEV_MENU_IOS_INIT, '');
  }

  return appDelegate;
}

export const withDevLauncherAppDelegate: ConfigPlugin = config => {
  return withAppDelegate(config, config => {
    if (config.modResults.language === 'objc') {
      let expoUpdatesVersion;
      try {
        expoUpdatesVersion = resolveExpoUpdatesVersion(config.modRequest.projectRoot);
      } catch (e) {
        WarningAggregator.addWarningIOS(
          'expo-dev-launcher',
          `Failed to check compatibility with expo-updates - ${e}`
        );
      }
      config.modResults.contents = modifyAppDelegate(
        config.modResults.contents,
        expoUpdatesVersion
      );
    } else {
      WarningAggregator.addWarningIOS(
        'expo-dev-launcher',
        'Swift AppDelegate files are not supported yet.'
      );
    }
    return config;
  });
};
