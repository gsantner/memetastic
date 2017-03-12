package io.github.gsantner.memetastic.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeOriginStorage;

public class ThumbnailCleanupTask extends Thread implements FilenameFilter {
    private String strres_dotThumbnails, strres_appName;

    public ThumbnailCleanupTask(Context context) {
        strres_appName = context.getString(R.string.app_name);
        strres_dotThumbnails = context.getString(R.string.dot_thumbnails);
    }

    public void run() {
        File picPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), strres_appName);
        File thumbPath = new File(picPath, strres_dotThumbnails);

        // Scan for unused Thumbnails
        List<File> thumbFiles = new LinkedList<File>(Arrays.asList(thumbPath.listFiles(this)));
        for (File picFile : picPath.listFiles(this)) {
            String filename = picFile.getName();
            for (int i = 0; i < thumbFiles.size(); i++) {
                if (thumbFiles.get(i).getName().equals(filename)) {
                    thumbFiles.remove(i);
                    break;
                }
            }
        }

        // Delete unused thumbnails
        for (File thumbFile : thumbFiles) {
            thumbFile.delete();
        }

        // Create not existing thumbs
        MemeOriginStorage memeOriginStorage = new MemeOriginStorage(picPath, strres_dotThumbnails);
        Map<String, String> missing = memeOriginStorage.getMissingThumbnails();
        for (Map.Entry<String, String> entry : missing.entrySet()) {
            Bitmap bitmap = Helpers.loadImageFromFilesystem(entry.getKey());
            if (bitmap != null) {
                File thumbFp = new File(entry.getValue());
                bitmap = Helpers.createThumbnail(bitmap);
                Helpers.saveBitmapToFile(thumbFp.getParent(), thumbFp.getName(), bitmap);
            }
        }
    }

    @Override
    public boolean accept(File dir, String filename) {
        return new File(dir, filename).isFile();
    }
}