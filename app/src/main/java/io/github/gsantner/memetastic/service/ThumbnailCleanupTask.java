/*
 * MemeTastic by Gregor Santner (http://gsantner.net)
 * Copyright (C) 2016-2018
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
 */
package io.github.gsantner.memetastic.service;

import android.content.Context;

import net.gsantner.opoc.util.FileUtils;

import java.io.File;

import io.github.gsantner.memetastic.util.AppSettings;

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