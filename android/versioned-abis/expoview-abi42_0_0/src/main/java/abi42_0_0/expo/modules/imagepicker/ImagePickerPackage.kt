package abi42_0_0.expo.modules.imagepicker

import android.content.Context
import abi42_0_0.org.unimodules.core.BasePackage

class ImagePickerPackage : BasePackage() {
  override fun createExportedModules(context: Context) = listOf(ImagePickerModule(context))
}
