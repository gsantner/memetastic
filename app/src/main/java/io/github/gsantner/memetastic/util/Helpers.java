package io.github.gsantner.memetastic.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.data.MemeLibConfig;

public class Helpers extends io.github.gsantner.opoc.util.Helpers {
    protected Helpers(Context context) {
        super(context);
    }


    public static Helpers get() {
        return new Helpers(App.get());
    }

    /**
     * Calculates the scaling factor so the bitmap is maximal as big as the reqSize
     *
     * @param options Bitmap-options that contain the current dimensions of the bitmap
     * @param reqSize the maximal size of the Bitmap
     * @return the scaling factor that needs to be applied to the bitmap
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqSize) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (Math.max(height, width) > reqSize) {
            inSampleSize = Math.round(1f * Math.max(height, width) / reqSize);
        }
        //Log.i("MEME", "scaleBy::" + inSampleSize);
        return inSampleSize;
    }

    public Bitmap createThumbnail(Bitmap bitmap) {
        int thumbnailSize = 300;
        int picSize = Math.min(bitmap.getHeight(), bitmap.getWidth());
        float scale = 1.f * thumbnailSize / picSize;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public File saveBitmapToFile(String pathToFile, String filename, Bitmap bitmapToSave) {
        new File(pathToFile).mkdirs();
        File imageFile = new File(pathToFile, filename);

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(imageFile); // overwrites this image every time
            bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 95, stream);
            return imageFile;
        } catch (FileNotFoundException ignored) {
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public Bitmap loadImageFromFilesystem(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = calculateInSampleSize(options, MemeLibConfig.MEME_FULLSCREEN_IMAGESIZE);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }


    public int getImmersiveUiVisibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int statusBarFlag = View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                statusBarFlag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            return statusBarFlag
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        return 0;
    }

    public void enableImmersiveMode(final View decorViewOfActivity) {
        decorViewOfActivity.setSystemUiVisibility(getImmersiveUiVisibility());
        decorViewOfActivity.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorViewOfActivity.setSystemUiVisibility(getImmersiveUiVisibility());
                }
            }
        });
    }

    /**
     * Calculates the scaling factor to convert font size to size in pixels
     *
     * @param w width of the bitmap where a text should be written on
     * @param h height of the bitmap where a text should be written on
     * @return the size of the font in pixels
     */
    public float getScalingFactorInPixelsForWritingOnPicture(int w, int h) {
        final float fontScaler = (float) 133;
        final int raster = 50;
        int size = Math.min(w, h);
        int rest = size % raster;

        // Round
        int addl = rest >= raster / 2 ? raster - rest : -rest;

        return (size + addl) / (fontScaler);
    }

    public static void setDrawableWithColorToImageView(ImageView imageView, @DrawableRes int drawableResId, @ColorRes int colorResId) {
        imageView.setImageResource(drawableResId);
        imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), colorResId));
    }
}
