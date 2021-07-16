package host.exp.exponent.kernel.services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import host.exp.exponent.Constants;
import host.exp.exponent.kernel.ExperienceKey;
import host.exp.exponent.storage.ExponentSharedPreferences;

public class PermissionsKernelService extends BaseKernelService {

  private ExponentSharedPreferences mExponentSharedPreferences;

  public PermissionsKernelService(Context context, ExponentSharedPreferences exponentSharedPreferences) {
    super(context);
    mExponentSharedPreferences = exponentSharedPreferences;
  }

  public void grantScopedPermissions(String permission, ExperienceKey experienceKey) {
    try {
      JSONObject metadata = mExponentSharedPreferences.getExperienceMetadata(experienceKey);
      if (metadata == null) {
        metadata = new JSONObject();
      }

      JSONObject permissions;
      if (metadata.has(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS)) {
        permissions = metadata.getJSONObject(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS);
      } else {
        permissions = new JSONObject();
      }

      JSONObject permissionObject;
      if (permissions.has(permission)) {
        permissionObject = permissions.getJSONObject(permission);
      } else {
        permissionObject = new JSONObject();
      }

      permissionObject.put("status", "granted");
      permissions.put(permission, permissionObject);
      metadata.put(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS, permissions);

      mExponentSharedPreferences.updateExperienceMetadata(experienceKey, metadata);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void revokeScopedPermissions(String permission, ExperienceKey experienceKey) {
    try {
      JSONObject metadata = mExponentSharedPreferences.getExperienceMetadata(experienceKey);
      if (metadata == null) {
        return;
      }

      if (metadata.has(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS)) {
        JSONObject permissions = metadata.getJSONObject(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS);
        if (permissions.has(permission)) {
          permissions.remove(permission);
          metadata.put(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS, permissions);
          mExponentSharedPreferences.updateExperienceMetadata(experienceKey, metadata);
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public boolean hasGrantedPermissions(String permission, ExperienceKey experienceKey) {
    // we don't want to worry about per-experience permissions for shell apps
    if (Constants.isStandaloneApp()) {
      return true;
    }
    JSONObject metadata = mExponentSharedPreferences.getExperienceMetadata(experienceKey);
    if (metadata == null) {
      return false;
    }
    try {
      if (metadata.has(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS)) {
        JSONObject permissions = metadata.getJSONObject(ExponentSharedPreferences.EXPERIENCE_METADATA_PERMISSIONS);
        if (permissions.has(permission)) {
          JSONObject permissionsObject = permissions.getJSONObject(permission);
          return permissionsObject.has("status") &&
            permissionsObject.getString("status").equals("granted");
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return false;
  }

  public int getPermissions(int globalPermissionStatus, PackageManager packageManager, String permission, ExperienceKey experienceKey) {
    // only these permissions, which show a dialog to the user should be scoped.
    boolean isDangerousPermission;
    try {
      isDangerousPermission = isDangerousPermission(permission, packageManager);
    } catch (PackageManager.NameNotFoundException e) {
      return PackageManager.PERMISSION_DENIED;
    }

    if (Constants.isStandaloneApp() || !isDangerousPermission) {
      return globalPermissionStatus;
    }

    if (globalPermissionStatus == PackageManager.PERMISSION_GRANTED &&
      hasGrantedPermissions(permission, experienceKey)) {
      return PackageManager.PERMISSION_GRANTED;
    } else {
      return PackageManager.PERMISSION_DENIED;
    }
  }

  private boolean isDangerousPermission(String permission, PackageManager packageManager) throws PackageManager.NameNotFoundException {
    PermissionInfo permissionInfo = packageManager.getPermissionInfo(permission, PackageManager.GET_META_DATA);
    return (permissionInfo.protectionLevel & PermissionInfo.PROTECTION_DANGEROUS) != 0;
  }

  @Override
  public void onExperienceForegrounded(ExperienceKey experienceKey) {

  }

  @Override
  public void onExperienceBackgrounded(ExperienceKey experienceKey) {

  }
}
