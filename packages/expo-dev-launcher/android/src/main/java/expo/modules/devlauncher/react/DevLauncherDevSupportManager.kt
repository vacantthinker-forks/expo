package expo.modules.devlauncher.react

import android.content.Context
import android.util.Log
import com.facebook.react.devsupport.DevSupportManagerBase
import com.facebook.react.devsupport.ReactInstanceManagerDevHelper
import com.facebook.react.devsupport.RedBoxHandler
import com.facebook.react.devsupport.interfaces.DevBundleDownloadListener
import com.facebook.react.packagerconnection.RequestHandler
import expo.modules.devlauncher.DevLauncherController
import expo.modules.devlauncher.launcher.errors.DevLauncherAppError
import expo.modules.devlauncher.launcher.errors.DevLauncherErrorActivity

class DevLauncherDevSupportManager(
  applicationContext: Context?,
  val reactInstanceManagerHelper: ReactInstanceManagerDevHelper?,
  packagerPathForJSBundleName: String?,
  enableOnCreate: Boolean,
  redBoxHandler: RedBoxHandler?,
  devBundleDownloadListener: DevBundleDownloadListener?,
  minNumShakes: Int,
  customPackagerCommandHandlers: MutableMap<String, RequestHandler>?
) : DevSupportManagerBase(
  applicationContext,
  reactInstanceManagerHelper,
  packagerPathForJSBundleName,
  enableOnCreate,
  redBoxHandler,
  devBundleDownloadListener,
  minNumShakes,
  customPackagerCommandHandlers
) {
  override fun showNewJavaError(message: String?, e: Throwable) {
    if (!DevLauncherController.wasInitialized()) {
      Log.e("DevLauncher", "DevLauncher wasn't initialized. Couldn't intercept native error handling.")
      super.showNewJavaError(message, e)
      return
    }

    val activity = reactInstanceManagerHelper?.currentActivity
    if (activity == null || activity.isFinishing || activity.isDestroyed) {
      return
    }

    DevLauncherController.instance.onAppLoadedWithError()
    DevLauncherErrorActivity.showError(activity, DevLauncherAppError(message, e))
  }
}
