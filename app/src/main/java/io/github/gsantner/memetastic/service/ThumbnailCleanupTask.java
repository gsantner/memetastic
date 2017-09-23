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