import { EventEmitter, Subscription } from '@unimodules/core';

import {
  BatteryLevelEvent,
  BatteryState,
  BatteryStateEvent,
  PowerModeEvent,
  PowerState,
} from './Battery.types';
import ExpoBattery from './ExpoBattery';

const BatteryEventEmitter = new EventEmitter(ExpoBattery);

// @needsAudit
/**
 * Resolves with whether the battery API is available on the current device. The value of this
 * property is `true` on Android and physical iOS devices and `false` on iOS simulators. On web,
 * it depends on whether the browser supports the web battery API.
 */
export async function isAvailableAsync(): Promise<boolean> {
  return Promise.resolve((ExpoBattery && ExpoBattery.isSupported) || false);
}

// @needsAudit
/**
 * Gets the battery level of the device as a number between `0` and `1`, inclusive. If the device
 * does not support retrieving the battery level, this method returns `-1`. On web, this method
 * always returns `-1`.
 * @return A `Promise` that fulfils with a number between `0` and `1` representing the battery level,
 * or `-1` if the device does not provide it.
 * # Example
 * ```ts
 * await Battery.getBatteryLevelAsync();
 * // 0.759999
 * ```
 */
export async function getBatteryLevelAsync(): Promise<number> {
  if (!ExpoBattery.getBatteryLevelAsync) {
    return -1;
  }
  return await ExpoBattery.getBatteryLevelAsync();
}

// @needsAudit
/**
 * Tells the battery's current state. On web, this always returns `BatteryState.UNKNOWN`.
 * @return Returns a `Promise` which fulfills with a [`Battery.BatteryState`](#batterystate) enum
 * value for whether the device is any of the four states.
 * # Example
 * ```ts
 * await Battery.getBatteryStateAsync();
 * // BatteryState.CHARGING
 * ```
 */
export async function getBatteryStateAsync(): Promise<BatteryState> {
  if (!ExpoBattery.getBatteryStateAsync) {
    return BatteryState.UNKNOWN;
  }
  return await ExpoBattery.getBatteryStateAsync();
}

// @needsAudit
/**
 * Gets the current status of Low Power mode on iOS and Power Saver mode on Android. If a platform
 * doesn't support Low Power mode reporting (like web, older Android devices), the reported low-power
 * state is always `false`, even if the device is actually in low-power mode.
 * @return Returns a `Promise` which fulfills with a `boolean` value of either `true` or `false`,
 * indicating whether low power mode is enabled or disabled, respectively.
 * # Example
 * Low Power Mode (iOS) or Power Saver Mode (Android) are enabled.
 * ```ts
 * await Battery.isLowPowerModeEnabledAsync();
 * // true
 * ```
 */
export async function isLowPowerModeEnabledAsync(): Promise<boolean> {
  if (!ExpoBattery.isLowPowerModeEnabledAsync) {
    return false;
  }
  return await ExpoBattery.isLowPowerModeEnabledAsync();
}

// @needsAudit
/**
 * Checks whether battery optimization is enabled for your application.
 * If battery optimization is enabled for your app, background tasks might be affected
 * when your app goes into doze mode state. (only on Android 6.0 or later)
 * @return Returns a `Promise` which fulfills with a `boolean` value of either `true` or `false`,
 * indicating whether the battery optimization is enabled or disabled, respectively. (Android only)
 * # Example
 * ```ts
 * await Battery.isBatteryOptimizationEnabledAsync();
 * // true
 * ```
 */
export async function isBatteryOptimizationEnabledAsync(): Promise<boolean> {
  if (!ExpoBattery.isBatteryOptimizationEnabledAsync) {
    return false;
  }
  return await ExpoBattery.isBatteryOptimizationEnabledAsync();
}

/**
 * Gets the power state of the device including the battery level, whether it is plugged in, and if
 * the system is currently operating in Low Power Mode (iOS) or Power Saver Mode (Android). This
 * method re-throws any errors that occur when retrieving any of the power-state information.
 * @return Returns a `Promise` which fulfills with [`PowerState`](#powerstate) object.
 * # Example
 * ```ts
 * await Battery.getPowerStateAsync();
 * // {
 * //   batteryLevel: 0.759999,
 * //   batteryState: BatteryState.UNPLUGGED,
 * //   lowPowerMode: true,
 * // }
 * ```
 */
export async function getPowerStateAsync(): Promise<PowerState> {
  const [batteryLevel, batteryState, lowPowerMode] = await Promise.all([
    getBatteryLevelAsync(),
    getBatteryStateAsync(),
    isLowPowerModeEnabledAsync(),
  ]);
  return {
    batteryLevel,
    batteryState,
    lowPowerMode,
  };
}

// @needsAudit
/**
 * Subscribe to the battery level change updates.
 *
 * On iOS devices, the event fires when the battery level drops one percent or more, but is only
 * fired once per minute at maximum.
 *
 * On Android devices, the event fires only when significant changes happens, which is when the
 * battery level drops below [`"android.intent.action.BATTERY_LOW"`](https://developer.android.com/reference/android/content/Intent#ACTION_BATTERY_LOW)
 * or rises above [`"android.intent.action.BATTERY_OKAY"`](https://developer.android.com/reference/android/content/Intent#ACTION_BATTERY_OKAY)
 * from a low battery level. See [here](https://developer.android.com/training/monitoring-device-state/battery-monitoring)
 * to read more from the Android docs.
 *
 * On web, the event never fires.
 * @param listener A callback that is invoked when battery level changes. The callback is provided a
 * single argument that is an object with a `batteryLevel` key.
 * @return A `Subscription` object on which you can call `remove()` to unsubscribe from the listener.s
 */
export function addBatteryLevelListener(
  listener: (event: BatteryLevelEvent) => void
): Subscription {
  return BatteryEventEmitter.addListener('Expo.batteryLevelDidChange', listener);
}

// @needsAudit
/**
 * Subscribe to the battery state change updates to receive an object with a [`Battery.BatteryState`](#batterystate)
 * enum value for whether the device is any of the four states.
 *
 * On web, the event never fires.
 * @param listener A callback that is invoked when battery state changes. The callback is provided a
 * single argument that is an object with a `batteryState` key.
 * @return A `Subscription` object on which you can call `remove()` to unsubscribe from the listener.
 */
export function addBatteryStateListener(
  listener: (event: BatteryStateEvent) => void
): Subscription {
  return BatteryEventEmitter.addListener('Expo.batteryStateDidChange', listener);
}

// @needsAudit
/**
 * Subscribe to Low Power Mode (iOS) or Power Saver Mode (Android) updates. The event fires whenever
 * the power mode is toggled.
 *
 * On web, the event never fires.
 * @param listener A callback that is invoked when Low Power Mode (iOS) or Power Saver Mode (Android)
 * changes. The callback is provided a single argument that is an object with a `lowPowerMode` key.
 * @return A `Subscription` object on which you can call `remove()` to unsubscribe from the listener.
 */
export function addLowPowerModeListener(listener: (event: PowerModeEvent) => void): Subscription {
  return BatteryEventEmitter.addListener('Expo.powerModeDidChange', listener);
}

export {
  BatteryLevelEvent,
  BatteryState,
  BatteryStateEvent,
  PowerModeEvent,
  PowerState,
  Subscription,
};
