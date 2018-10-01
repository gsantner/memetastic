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

import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.opoc.util.FileUtils;

import java.io.File;

public class ThumbnailCleanupTask extends Thread {
    private Context _context;

    public ThumbnailCleanupTask(Context context) {
        _context = context;
    }

    public void run() {
        File cacheDirForFiles = new File(_context.getCacheDir(), AppSettings.get().getSaveDirectory().getAbsolutePath().substring(1));
        FileUtils.deleteRecursive(cacheDirForFiles);
    }
}