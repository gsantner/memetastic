package io.github.gsantner.memetastic.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;

import io.github.gsantner.memetastic.R;

public class PermissionChecker {

    public static boolean doIfPermissionGranted(final Activity activity) {
        if (!hasExtStoragePerm(activity)) {
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123
            );
            return false;
        }
        return true;
    }

    public static boolean hasExtStoragePerm(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissionResult(final Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        ActivityUtils.get(activity).showSnackBar(R.string.error_storage_permission, true);
        return false;
    }

    public static boolean mkSaveDir(Activity activity) {
        File saveDir = AppSettings.get().getSaveDirectory();
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            ActivityUtils.get(activity).showSnackBar(R.string.error_cannot_create_save_dir, false);
            return false;
        }
        return true;
    }
}
