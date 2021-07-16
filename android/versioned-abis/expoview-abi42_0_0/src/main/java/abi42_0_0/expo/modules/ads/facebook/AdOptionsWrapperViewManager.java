package abi42_0_0.expo.modules.ads.facebook;

import android.content.Context;
import android.graphics.Color;

import com.facebook.ads.AdOptionsView;

import abi42_0_0.org.unimodules.core.ModuleRegistry;
import abi42_0_0.org.unimodules.core.ViewManager;
import abi42_0_0.org.unimodules.core.interfaces.ExpoProp;
import abi42_0_0.org.unimodules.core.interfaces.services.UIManager;

public class AdOptionsWrapperViewManager extends ViewManager<AdOptionsWrapperView> {
  private ModuleRegistry mModuleRegistry;

  @Override
  public String getName() {
    return "AdOptionsView";
  }

  @Override
  public AdOptionsWrapperView createViewInstance(Context context) {
    return new AdOptionsWrapperView(context);
  }

  @Override
  public ViewManagerType getViewManagerType() {
    return ViewManagerType.SIMPLE;
  }

  @ExpoProp(name = "iconColor")
  public void setIconColor(final AdOptionsWrapperView view, final String iconColor) {
    try {
      if (iconColor == null) {
        view.setIconColor(null);
      } else {
        view.setIconColor(Color.parseColor(iconColor));
      }
    } catch (IllegalArgumentException e) {
      // do nothing
    }
  }

  @ExpoProp(name = "iconSize")
  public void setIconSize(final AdOptionsWrapperView view, final int iconSize) {
    view.setIconSize(iconSize);
  }

  @ExpoProp(name = "orientation")
  public void setOrientation(final AdOptionsWrapperView view, final int orientation) {
    view.setOrientation(orientation == 0 ? AdOptionsView.Orientation.HORIZONTAL : AdOptionsView.Orientation.VERTICAL);
  }

  @ExpoProp(name = "nativeAdViewTag")
  public void setNativeAdViewTag(final AdOptionsWrapperView view, final int nativeAdTag) {
    if (nativeAdTag == -1) {
      return;
    }
    mModuleRegistry.getModule(UIManager.class).addUIBlock(new UIManager.GroupUIBlock() {
      @Override
      public void execute(UIManager.ViewHolder viewHolder) {
        view.setNativeAdView((NativeAdView) viewHolder.get(nativeAdTag));
      }
    });
  }

  @Override
  public void onCreate(ModuleRegistry moduleRegistry) {
    mModuleRegistry = moduleRegistry;
  }
}
