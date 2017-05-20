package io.github.gsantner.memetastic.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.opoc.util.AppSettingsBase;

public class AppSettings extends AppSettingsBase {
    private static final int MAX_FAVS = 50;

    //#####################
    //## Methods
    //#####################
    private AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        return new AppSettings(App.get());
    }

    // Adds a String to a String array and cuts of the last values to match a maximal size
    private static String[] insertAndMaximize(String[] values, String value, int maxSize) {
        List<String> list;
        if (values == null)
            list = new ArrayList<>();
        else
            list = new ArrayList<>(Arrays.asList(values));
        list.add(0, value);
        while (list.size() > maxSize) {
            list.remove(maxSize - 1);
        }
        return list.toArray(new String[list.size()]);
    }

    public int getRenderQualityReal() {
        int val = getInt(prefApp, R.string.pref_key__render_quality__percent, 24);
        return (int) (400 + (2100.0 * (val / 100.0)));
    }

    public void setLastSelectedFont(int value) {
        setInt(prefApp, R.string.pref_key__last_selected_font, value);
    }

    public int getLastSelectedFont() {
        return getInt(prefApp, R.string.pref_key__last_selected_font, 0);
    }

    public void setFavoriteMemes(String[] value) {
        setStringArray(prefApp, R.string.pref_key__meme_favourites, value);
    }

    public String[] getFavoriteMemes() {
        return getStringArray(prefApp, R.string.pref_key__meme_favourites);
    }

    public void appendFavoriteMeme(String meme) {
        String[] memes = insertAndMaximize(getFavoriteMemes(), meme, MAX_FAVS);
        setFavoriteMemes(memes);
    }

    public boolean isFavorite(String name) {
        if (getFavoriteMemes() == null)
            return false;
        for (String s : getFavoriteMemes()) {
            if (s.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public boolean toggleFavorite(String name) {
        if (!isFavorite(name)) {
            appendFavoriteMeme(name);
            return true;
        }
        removeFavorite(name);
        return false;
    }

    public void removeFavorite(String name) {
        String[] favs = getFavoriteMemes();
        ArrayList<String> newFavs = new ArrayList<String>();

        for (String fav : favs) {
            if (!fav.equalsIgnoreCase(name))
                newFavs.add(fav);
        }
        setFavoriteMemes(newFavs.toArray(new String[newFavs.size()]));
    }

    public void setLastSelectedCategory(int value) {
        setInt(prefApp, R.string.pref_key__last_selected_category, value);
    }

    public int getLastSelectedCategory() {
        return getInt(prefApp, R.string.pref_key__last_selected_category, 0);
    }

    public int getGridColumnCountPortrait() {
        int count = getInt(prefApp, R.string.pref_key__grid_column_count_portrait, -1);
        if (count == -1) {
            count = 3 + (int) Math.max(0, 0.5 * (Helpers.getEstimatedScreenSizeInches(context) - 5.0));
            setGridColumnCountPortrait(count);
        }
        return count;
    }

    public void setGridColumnCountPortrait(int value) {
        setInt(prefApp, R.string.pref_key__grid_column_count_portrait, value);
    }

    public int getGridColumnCountLandscape() {
        int count = getInt(prefApp, R.string.pref_key__grid_column_count_landscape, -1);
        if (count == -1) {
            count = (int) (getGridColumnCountPortrait() * 1.8);
            setGridColumnCountLandscape(count);
        }
        return count;
    }

    public void setGridColumnCountLandscape(int value) {
        setInt(prefApp, R.string.pref_key__grid_column_count_landscape, value);
    }

    public boolean isAppFirstStart() {
        boolean value = getBool(prefApp, R.string.pref_key__app_first_start, true);
        setBool(prefApp, R.string.pref_key__app_first_start, false);
        return value;
    }

    public boolean isAppCurrentVersionFirstStart() {
        int value = getInt(prefApp, R.string.pref_key__app_first_start_current_version, -1);
        setInt(prefApp, R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
        return value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
    }
}