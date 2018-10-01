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
package net.gsantner.memetastic.service;

import android.content.Context;
import android.os.Environment;

import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.memetastic.util.PermissionChecker;
import net.gsantner.opoc.util.FileUtils;

import java.io.File;

import io.github.gsantner.memetastic.R;

public class MigrationThread extends Thread {
    private final Context _context;

    public MigrationThread(Context context) {
        _context = context;
    }

    @Override
    public void run() {
        super.run();
        AppSettings appSettings = AppSettings.get();
        if (!PermissionChecker.hasExtStoragePerm(_context) || appSettings.isMigrated()) {
            return;
        }

        File newMemesDir = AssetUpdater.getMemesDir(AppSettings.get());
        File newTemplatesDir = AssetUpdater.getCustomAssetsDir(AppSettings.get());
        File oldMemesDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), _context.getString(R.string.app_name));
        File oldTemplatesDir = new File(oldMemesDir, "templates");
        File oldTemplatesCustomDir = new File(oldMemesDir, "custom");
        String thumbnails = ".thumbnails";

        if (!oldMemesDir.exists()) {
            return;
        }

        FileUtils.deleteRecursive(new File(oldMemesDir, thumbnails));

        try {
            FileUtils.deleteRecursive(new File(oldTemplatesCustomDir, thumbnails));
            for (File file : new File(oldTemplatesDir, "custom").listFiles()) {
                if (file.isFile()) {
                    FileUtils.renameFile(file, new File(newTemplatesDir, file.getName()));
                }
            }
        } catch (Exception ignored) {
        }

        try {
            for (File file : oldMemesDir.listFiles()) {
                if (file.isFile()) {
                    FileUtils.renameFile(file, new File(newMemesDir, file.getName()));
                }
            }
        } catch (Exception ignored) {
        }
        FileUtils.deleteRecursive(oldTemplatesCustomDir);
        appSettings.setMigrated(true);
    }
}
