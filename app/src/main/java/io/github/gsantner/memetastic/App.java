package io.github.gsantner.memetastic;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.gsantner.memetastic.data.MemeCategory;
import io.github.gsantner.memetastic.data.MemeFont;
import io.github.gsantner.memetastic.data.MemeLibConfig;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.Helpers;

/**
 * The apps application object
 */
public class App extends Application {
    private volatile static App app;
    public AppSettings settings;
    List<MemeCategory> memeCategories;
    List<MemeFont> fonts;

    public static App get() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        settings = AppSettings.get();
        loadFonts();
        loadMemeNames();

        int a = settings.getRenderQualityReal();

        if (settings.isAppFirstStart()) {
            // Set default values (calculated in getters)
            settings.setGridColumnCountPortrait(settings.getGridColumnCountPortrait());
            settings.setGridColumnCountLandscape(settings.getGridColumnCountLandscape());
        }
    }

    public void loadFonts() {
        String FONT_FOLDER = MemeLibConfig.getPath(MemeLibConfig.Assets.FONTS, false);
        try {
            String[] fontFilenames = getAssets().list(FONT_FOLDER);
            FONT_FOLDER = MemeLibConfig.getPath(FONT_FOLDER, true);
            fonts = new ArrayList<>();

            for (int i = 0; i < fontFilenames.length; i++) {
                Typeface tf = Typeface.createFromAsset(getResources().getAssets(), FONT_FOLDER + fontFilenames[i]);
                fonts.add(new MemeFont(FONT_FOLDER + fontFilenames[i], tf));
            }
        } catch (IOException e) {
            log("Could not load fonts");
            fonts = new ArrayList<>();
        }
    }

    public void loadMemeNames() {
        String IMAGE_FOLDER = MemeLibConfig.getPath(MemeLibConfig.Assets.MEMES, false);
        try {
            String[] memeCategories = getAssets().list(IMAGE_FOLDER);
            IMAGE_FOLDER = MemeLibConfig.getPath(IMAGE_FOLDER, true);
            this.memeCategories = new ArrayList<MemeCategory>();

            for (String memeCat : memeCategories) {
                this.memeCategories.add(new MemeCategory(memeCat, getAssets().list(IMAGE_FOLDER + memeCat)));
            }
        } catch (IOException e) {
            log("Could not load images");
            memeCategories = new ArrayList<MemeCategory>();
        }
    }

    public List<MemeFont> getFonts() {
        return this.fonts;
    }

    // Get meme category object (parameter = foldername in assets)
    public MemeCategory getMemeCategory(String category) {
        for (MemeCategory cat : memeCategories) {
            if (cat.getCategoryName().equalsIgnoreCase(category))
                return cat;
        }
        return null;
    }

    public void shareBitmapToOtherApp(Bitmap bitmap, Activity activity) {
        File imageFile = Helpers.saveBitmapToFile(getCacheDir().getAbsolutePath(), getString(R.string.cached_picture_filename), bitmap);
        if (imageFile != null) {
            Uri imageUri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), imageFile);
            if (imageUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(imageUri, getContentResolver().getType(imageUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                activity.startActivity(Intent.createChooser(shareIntent, getString(R.string.main__share_meme_prompt)));
            }
        }
    }

    public static void log(String text) {
        if (BuildConfig.DEBUG) {
            Log.d("MemeTastic", text);
        }
    }
}
