package io.github.gsantner.memetastic.data;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles the memes (loading, count of memes, etc.) inside a category in the assets
 */
public class MemeOriginAssets implements MemeOriginInterface {
    protected MemeCategory mMemeCategory;
    protected AssetManager mAssetManager;
    protected int length;

    /**
     * Constructor that takes and saves the meme category, an instance of the asset manager
     * and calculates the length of the available memes
     *
     * @param memeCategory the category of the memes
     * @param assetManager the asset manager which will be used to load the memes of the category
     */
    public MemeOriginAssets(MemeCategory memeCategory, AssetManager assetManager) {
        mMemeCategory = memeCategory;
        mAssetManager = assetManager;
        if (mMemeCategory != null) {
            try {
                length = mAssetManager.list(mMemeCategory.getFolderPath(false)).length;
            } catch (IOException e) {
                length = 0;
            }
        }
    }

    @Override
    public String getPath(int position, boolean bThumbnail) {
        return getFilepath(position);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean isAsset() {
        return true;
    }

    @Override
    public String getFilepath(int position) {
        return mMemeCategory.getImagePath(position);
    }

    @Override
    public String getThumbnailPath(int position) {
        return getFilepath(position);
    }

    @Override
    public boolean showFavButton() {
        return true;
    }

    public boolean fileExists(int position) {
        try {
            InputStream is = mAssetManager.open(getFilepath(position));
            is.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public void shuffleList() {
        mMemeCategory.shuffleList();
    }

    @Override
    public boolean isTemplate() {
        return true;
    }
}
