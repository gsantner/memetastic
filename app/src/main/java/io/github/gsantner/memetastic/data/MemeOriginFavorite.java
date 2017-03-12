package io.github.gsantner.memetastic.data;

import android.content.res.AssetManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Handles the favorite memes (loading, count of favorites, etc.)
 */
public class MemeOriginFavorite extends MemeOriginAssets {
    ArrayList<String> mFiles;

    /**
     * Constructor that takes the favorites as string array and an instance of the assetmanager
     * to load the favorites later
     *
     * @param files        the paths to the favorite memes
     * @param assetManager the instance of the asset manager which will be used later to load the memes
     */
    public MemeOriginFavorite(String[] files, AssetManager assetManager) {
        super(null, assetManager);
        setFiles(files);
    }

    /**
     * used to set the favorites to new data in case that something has changed (added/removed a favorite)
     *
     * @param files the new paths to the favorite memes
     */
    public void setFiles(String[] files) {
        if (files == null) {
            length = 0;
            mFiles = new ArrayList<>();
            return;
        }
        mFiles = new ArrayList<String>(Arrays.asList(files));
        for (int x = mFiles.size() - 1; x >= 0; x--) {
            if (!fileExists(x)) {
                mFiles.remove(x);
            }
        }
        length = mFiles.size();
    }

    @Override
    public String getFilepath(int position) {
        return mFiles.get(position);
    }
}
