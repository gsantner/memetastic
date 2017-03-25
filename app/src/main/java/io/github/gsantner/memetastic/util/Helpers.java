package io.github.gsantner.memetastic.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeLibConfig;

public class Helpers {

    /**
     * Calculates the scaling factor so the bitmap is maximal as big as the reqSize
     *
     * @param options Bitmap-options that contain the current dimensions of the bitmap
     * @param reqSize the maximal size of the Bitmap
     * @return the scaling factor that needs to be applied to the bitmap
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqSize) {
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

    public static double getEstimatedScreenSizeInches(Context c) {
        DisplayMetrics dm = c.getResources().getDisplayMetrics();

        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);
        double screenInches = Math.sqrt(x + y) * 1.16;  // 1.16 = est. Nav/Statusbar
        screenInches = screenInches < 4.0 ? 4.0 : screenInches;
        screenInches = screenInches > 12.0 ? 12.0 : screenInches;
        return screenInches;
    }

    public static boolean isInPortraitMode(Context c) {
        return c.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static void animateToActivity(Activity from, Class to, int requestCode, boolean finishFromActivity) {
        Intent intent = new Intent(from, to);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        from.startActivityForResult(intent, requestCode);
        from.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        if (finishFromActivity) {
            from.finish();
        }
    }

    public static void animateToActivity(Activity from, Intent intent, int requestCode) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        from.startActivityForResult(intent, requestCode);
        from.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * create thumbnail to save it to speed up viewing in the gridlayout
     *
     * @return Creates a thumbnail from the last bitmap
     */
    public static Bitmap createThumbnail(Bitmap bitmap) {
        int thumbnailSize = 300;
        int picSize = Math.min(bitmap.getHeight(), bitmap.getWidth());
        float scale = 1.f * thumbnailSize / picSize;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Calculates the scaling factor to convert font size to size in pixels
     *
     * @param w width of the bitmap where a text should be written on
     * @param h height of the bitmap where a text should be written on
     * @return the size of the font in pixels
     */
    public static float getScalingFactor(int w, int h) {
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


    public static int getImmersiveUiVisibility() {
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


    public static void enableImmersiveMode(final View decorViewOfActivity) {
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static File saveBitmapToFile(String pathToFile, String filename, Bitmap bitmapToSave) {
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


    public static Bitmap loadImageFromFilesystem(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = Helpers.calculateInSampleSize(options, MemeLibConfig.MEME_FULLSCREEN_IMAGESIZE);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, options);
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    public static void openWebpageWithExternalBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void donateBitcoinRequest(Context context) {
        if (!BuildConfig.IS_GPLAY_BUILD) {
            String btcUri = String.format("bitcoin:%s?amount=%s&label=%s&message=%s",
                    "1B9ZyYdQoY9BxMe9dRUEKaZbJWsbQqfXU5", "0.01", "Have some coke, and a nice day", "Have some coke, and a nice day");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(btcUri));
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                openWebpageWithExternalBrowser(context, "https://gsantner.github.io/donate/#donate");
            }
        }
    }

    public static String readTextfileFromRawRes(Context context, @RawRes int rawRessourceId, String linePrefix, String linePostfix) {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = null;
        linePrefix = linePrefix == null ? "" : linePrefix;
        linePostfix = linePostfix == null ? "" : linePostfix;

        try {
            br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(rawRessourceId)));
            while ((line = br.readLine()) != null) {
                sb.append(linePrefix);
                sb.append(line);
                sb.append(linePostfix);
                sb.append("\n");
            }
        } catch (Exception ignored) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return sb.toString();
    }

    public static void showSnackBar(Activity a, @StringRes int stringId) {
        Snackbar.make(a.findViewById(android.R.id.content), stringId, Snackbar.LENGTH_SHORT).show();
    }

    public static String loadMarkdownFromRawForTextView(Context context, @RawRes int rawMdFile, String prepend) {
        try {
            return new SimpleMarkdownParser()
                    .parse(context.getResources().openRawResource(rawMdFile),
                            SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, prepend)
                    .replaceColor("#000001", ContextCompat.getColor(context, R.color.accent))
                    .removeMultiNewlines().replaceBulletCharacter("*").getHtml();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Show HTML a TextView in a scrollable Dialog
    public static void showDialogWithHtmlTextView(Context context, String html, @StringRes int resTitleId) {
        LinearLayout layout = new LinearLayout(context);
        TextView textView = new TextView(context);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        ScrollView root = new ScrollView(context);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        layout.setLayoutParams(layoutParams);

        layout.addView(textView);
        root.addView(layout);

        textView.setText(new SpannableString(Html.fromHtml(html)));
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(resTitleId)
                .setView(root);
        dialog.show();
    }
}
