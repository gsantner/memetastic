package io.github.gsantner.memetastic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;

public class AppSettings {
    private static final int MAX_FAVS = 50;

    private final SharedPreferences prefApp;
    private final Context context;

    public AppSettings(Context context) {
        this.context = context.getApplicationContext();
        prefApp = this.context.getSharedPreferences("app", Context.MODE_PRIVATE);
    }

    public Context getApplicationContext() {
        return context;
    }

    public void clearAppSettings() {
        prefApp.edit().clear().commit();
    }

    public String getKey(int stringKeyResourceId) {
        return context.getString(stringKeyResourceId);
    }

    public boolean isKeyEqual(String key, int stringKeyRessourceId) {
        return key.equals(getKey(stringKeyRessourceId));
    }

    private void setString(SharedPreferences pref, int keyRessourceId, String value) {
        pref.edit().putString(context.getString(keyRessourceId), value).apply();
    }

    private void setInt(SharedPreferences pref, int keyRessourceId, int value) {
        pref.edit().putInt(context.getString(keyRessourceId), value).apply();
    }

    private void setLong(SharedPreferences pref, int keyRessourceId, long value) {
        pref.edit().putLong(context.getString(keyRessourceId), value).apply();
    }

    private void setBool(SharedPreferences pref, int keyRessourceId, boolean value) {
        pref.edit().putBoolean(context.getString(keyRessourceId), value).apply();
    }

    private void setStringArray(SharedPreferences pref, int keyRessourceId, Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append("%%%");
            sb.append(value.toString());
        }
        setString(pref, keyRessourceId, sb.toString().replaceFirst("%%%", ""));
    }

    private String[] getStringArray(SharedPreferences pref, int keyRessourceId) {
        String value = pref.getString(context.getString(keyRessourceId), "%%%");
        if (value.equals("%%%")) {
            return new String[0];
        }
        return value.split("%%%");
    }

    private String getString(SharedPreferences pref, int ressourceId, String defaultValue) {
        return pref.getString(context.getString(ressourceId), defaultValue);
    }

    private String getString(SharedPreferences pref, int ressourceId, int ressourceIdDefaultValue) {
        return pref.getString(context.getString(ressourceId), context.getString(ressourceIdDefaultValue));
    }

    private boolean getBool(SharedPreferences pref, int ressourceId, boolean defaultValue) {
        return pref.getBoolean(context.getString(ressourceId), defaultValue);
    }

    private int getInt(SharedPreferences pref, int ressourceId, int defaultValue) {
        return pref.getInt(context.getString(ressourceId), defaultValue);
    }

    private long getLong(SharedPreferences pref, int ressourceId, long defaultValue) {
        return pref.getLong(context.getString(ressourceId), defaultValue);
    }


    public int getColor(SharedPreferences pref, String key, int defaultColor) {
        return pref.getInt(key, defaultColor);
    }

    public int getColorRes(@ColorRes int resColorId) {
        return ContextCompat.getColor(context, resColorId);
    }

    // Adds a String to a String array and cuts of the last values to match a maximal size
    private static String[] insertAndMaximize(String[] values, String value, int maxSize) {
        List<String> list;
        if (values == null)
            list = new ArrayList<String>();
        else
            list = new ArrayList<String>(Arrays.asList(values));
        list.add(0, value);
        while (list.size() > maxSize) {
            list.remove(maxSize - 1);
        }
        return list.toArray(new String[list.size()]);
    }

    public int getRenderQuality() {
        return getInt(prefApp, R.string.pref_key__render_quality, 900);
    }

    public void setRenderQuality(int value) {
        setInt(prefApp, R.string.pref_key__render_quality, value);
    }

    public void setLastSelectedFont(int value) {
        setInt(prefApp, R.string.pref_key__last_selected_font, value);
    }

    public int getLastSelectedFont() {
        return getInt(prefApp, R.string.pref_key__last_selected_font, 0);
    }

    public void setFavoriteMemes(String[] value) {
        setStringArray(prefApp, R.string.pref_key__memes_favourites, value);
    }

    public String[] getFavoriteMemes() {
        return getStringArray(prefApp, R.string.pref_key__memes_favourites);
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