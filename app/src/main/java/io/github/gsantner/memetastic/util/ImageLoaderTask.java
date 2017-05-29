package io.github.gsantner.memetastic.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

import io.github.gsantner.memetastic.data.MemeLibConfig;
import io.github.gsantner.memetastic.ui.GridRecycleAdapter;

public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    public static interface OnImageLoadedListener {
        void onImageLoaded(Bitmap bitmap, GridRecycleAdapter.ViewHolder holder);
    }

    private OnImageLoadedListener listener;
    private GridRecycleAdapter.ViewHolder holder;
    private AssetManager assetManager;
    private boolean bThumbnail;

    public ImageLoaderTask(OnImageLoadedListener listener, GridRecycleAdapter.ViewHolder holder, boolean bThumbnail) {
        this.listener = listener;
        this.holder = holder;
        this.bThumbnail = bThumbnail;
        this.assetManager = null;
    }

    public ImageLoaderTask(OnImageLoadedListener listener, GridRecycleAdapter.ViewHolder holder, boolean bThumbnail, AssetManager assetManager) {
        this.listener = listener;
        this.holder = holder;
        this.bThumbnail = bThumbnail;
        this.assetManager = assetManager;
    }

    private Bitmap loadStorageImage(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        if (bThumbnail)
            options.inSampleSize = Helpers.get().calculateInSampleSize(options, MemeLibConfig.MEME_SHOWCASE_GRID_MAX_IMAGESIZE);
        else
            options.inSampleSize = Helpers.get().calculateInSampleSize(options, MemeLibConfig.MEME_FULLSCREEN_IMAGESIZE);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public Bitmap loadAssetImage(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(imagePath);
            BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
            if (bThumbnail) {
                options.inSampleSize = Helpers.get().calculateInSampleSize(options, MemeLibConfig.MEME_SHOWCASE_GRID_MAX_IMAGESIZE);
            } else {
                options.inSampleSize = Helpers.get().calculateInSampleSize(options, MemeLibConfig.MEME_FULLSCREEN_IMAGESIZE);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            inputStream = assetManager.open(imagePath);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        if (assetManager == null) {
            return loadStorageImage(params[0]);
        }
        return loadAssetImage(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (this.listener != null)
            this.listener.onImageLoaded(bitmap, holder);
    }
}
