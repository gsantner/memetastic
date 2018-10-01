/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License of this file: GNU GPLv3 (Commercial upon request)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
#########################################################*/
package net.gsantner.memetastic.util;

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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        ActivityUtils.get(activity).showSnackBar(R.string.error_storage_permission__appspecific, true);
        return false;
    }

    public static boolean mkSaveDir(Activity activity) {
        File saveDir = AppSettings.get().getSaveDirectory();
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            ActivityUtils.get(activity).showSnackBar(R.string.error_cannot_create_save_directory, false);
            return false;
        }
        return true;
    }
}
