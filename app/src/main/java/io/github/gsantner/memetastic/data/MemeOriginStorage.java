package io.github.gsantner.memetastic.data;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the already created memes (loading, count of memes, etc.) that are stored in the pictures folder
 */
public class MemeOriginStorage implements MemeOriginInterface {
    private File[] mFiles;
    private String mFilePath;
    private String mThumbnailPath;
    private Map<String, String> mMissingThumbnails = new HashMap<>();
    private int mLength;

    /**
     * Constructor that takes the path to the created memes and path to the thumbnail folder of the created memes
     *
     * @param folderPathContainingPics path to the already created memes
     * @param subfolderThumbnails      path to the thumbnails of the created memes
     */
    public MemeOriginStorage(File folderPathContainingPics, final String subfolderThumbnails) {
        mFilePath = folderPathContainingPics.getAbsolutePath();
        mFiles = folderPathContainingPics.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File thumbnailFile = new File(new File(dir, subfolderThumbnails), filename);
                File memeFile = new File(dir, filename);
                boolean ok = thumbnailFile.isFile() && memeFile.isFile();
                if (memeFile.isFile() && !thumbnailFile.exists()) {
                    mMissingThumbnails.put(memeFile.getAbsolutePath(), thumbnailFile.getAbsolutePath());
                }

                return ok;
            }
        });
        Arrays.sort(mFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return (int) (f2.lastModified() - f1.lastModified());
            }
        });
        mThumbnailPath = subfolderThumbnails;
        mLength = mFiles.length;
    }

    @Override
    public String getPath(int position, boolean bThumbnail) {
        if (bThumbnail)
            return getThumbnailPath(position);
        return getFilepath(position);
    }

    @Override
    public int getLength() {
        return mLength;
    }

    @Override
    public boolean isAsset() {
        return false;
    }

    @Override
    public String getFilepath(int position) {
        return mFilePath + File.separator + mFiles[position].getName();
    }

    @Override
    public String getThumbnailPath(int position) {
        return mFilePath + File.separator + mThumbnailPath + File.separator + mFiles[position].getName();
    }

    @Override
    public boolean showFavButton() {
        return false;
    }

    public Map<String, String> getMissingThumbnails() {
        return mMissingThumbnails;
    }
}