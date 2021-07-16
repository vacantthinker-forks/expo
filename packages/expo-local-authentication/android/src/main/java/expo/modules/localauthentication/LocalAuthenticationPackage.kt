package expo.modules.localauthentication

import android.content.Context
import org.unimodules.core.BasePackage
import org.unimodules.core.ExportedModule

class LocalAuthenticationPackage : BasePackage() {
  override fun createExportedModules(context: Context): List<ExportedModule> {
    return listOf<ExportedModule>(LocalAuthenticationModule(context))
  }
}
