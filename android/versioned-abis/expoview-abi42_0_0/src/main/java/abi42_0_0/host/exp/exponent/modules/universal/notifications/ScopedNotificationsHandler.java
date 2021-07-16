package abi42_0_0.host.exp.exponent.modules.universal.notifications;

import android.content.Context;

import abi42_0_0.expo.modules.notifications.notifications.handling.NotificationsHandler;
import expo.modules.notifications.notifications.model.Notification;
import expo.modules.notifications.notifications.model.NotificationResponse;
import host.exp.exponent.kernel.ExperienceKey;
import host.exp.exponent.notifications.ScopedNotificationsUtils;

public class ScopedNotificationsHandler extends NotificationsHandler {
  private ExperienceKey mExperienceKey;
  private ScopedNotificationsUtils mScopedNotificationsUtils;

  public ScopedNotificationsHandler(Context context, ExperienceKey experienceKey) {
    super(context);
    mExperienceKey = experienceKey;
    mScopedNotificationsUtils = new ScopedNotificationsUtils(context);
  }

  @Override
  public void onNotificationReceived(Notification notification) {
    if (mScopedNotificationsUtils.shouldHandleNotification(notification, mExperienceKey)) {
      super.onNotificationReceived(notification);
    }
  }

  @Override
  public boolean onNotificationResponseReceived(NotificationResponse response) {
    if (mScopedNotificationsUtils.shouldHandleNotification(response.getNotification(), mExperienceKey)) {
      return super.onNotificationResponseReceived(response);
    }
    return false;
  }
}
