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
    public interface OnImageLoadedListener {
        void onImageLoaded(Bitmap bitmap, GridRecycleAdapter.ViewHolder holder);
    }

    private final int _sampleSizeThumbnails;
    private final OnImageLoadedListener _listener;
    private final GridRecycleAdapter.ViewHolder _holder;
    private final AssetManager _assetManager;
    private final boolean _isThumbnail;

    public ImageLoaderTask(OnImageLoadedListener listener, GridRecycleAdapter.ViewHolder holder, boolean isThumbnail) {
        this(listener, holder, isThumbnail, null);
    }

    public ImageLoaderTask(OnImageLoadedListener listener, GridRecycleAdapter.ViewHolder holder, boolean isThumbnail, AssetManager assetManager) {
        _listener = listener;
        _holder = holder;
        _isThumbnail = isThumbnail;
        _assetManager = assetManager;
        _sampleSizeThumbnails = AppSettings.get().getThumbnailQualityReal();
    }

    private Bitmap loadStorageImage(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        if (_isThumbnail)
            options.inSampleSize = ContextUtils.get().calculateInSampleSize(options, _sampleSizeThumbnails);
        else
            options.inSampleSize = ContextUtils.get().calculateInSampleSize(options, MemeLibConfig.MEME_FULLSCREEN_MAX_IMAGESIZE);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    private Bitmap loadAssetImage(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = null;
        try {
            inputStream = _assetManager.open(imagePath);
            BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
            if (_isThumbnail) {
                options.inSampleSize = ContextUtils.get().calculateInSampleSize(options, _sampleSizeThumbnails);
            } else {
                options.inSampleSize = ContextUtils.get().calculateInSampleSize(options, MemeLibConfig.MEME_FULLSCREEN_MAX_IMAGESIZE);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            inputStream = _assetManager.open(imagePath);
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
        if (_assetManager == null) {
            return loadStorageImage(params[0]);
        }
        return loadAssetImage(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (_listener != null)
            _listener.onImageLoaded(bitmap, _holder);
    }
}
