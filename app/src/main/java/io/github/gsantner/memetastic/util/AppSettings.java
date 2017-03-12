package io.github.gsantner.memetastic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AppSettings {

    public static class PREF {
        private static final int MAX_FAVS = 50;
        public static final String RENDER_QUALITY = "pref_key__renderQuality";
        public static final String LAST_SELECTED_FONT = "pref_key__last_selected_font";
        public static final String FAVOURITED_MEMES = "pref_key__favourite_memes";
        public static final String LAST_SELECTED_CATEGORY = "pref_key__last_selected_category";
        public static final String GRID_COLUMN_COUNT_PORTRAIT = "pref_key__grid_column_count_portrait";
        public static final String GRID_COLUMN_COUNT_LANDSCAPE = "pref_key__grid_column_count_landscape";
    }

    private Context context;
    private SharedPreferences pref;

    public AppSettings(Context context) {
        this.context = context.getApplicationContext();
        pref = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    // appends a string array to a string with questionmarks to seperate the strings
    private static String fromStringArray(String[] strings) {
        if (strings == null || strings.length == 0)
            return null;
        StringBuilder sb = new StringBuilder();
        for (String meme : strings) {
            sb.append(meme);
            sb.append("?");
        }
        String ret = sb.toString();
        return ret.substring(0, ret.length() - 1);
    }

    // splits a string that is seperated by questionmarks to a string array
    private static String[] toStringArray(String questionSeperatedString) {
        if (questionSeperatedString == null || questionSeperatedString.isEmpty())
            return null;
        return questionSeperatedString.split(Pattern.quote("?"));
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
        return pref.getInt(PREF.RENDER_QUALITY, 900);
    }

    public void setRenderQuality(int renderQuality) {
        pref.edit().putInt(PREF.RENDER_QUALITY, renderQuality).apply();
    }

    public void setLastSelectedFont(int lastSelectedFont) {
        pref.edit().putInt(PREF.LAST_SELECTED_FONT, lastSelectedFont).apply();
    }

    public int getLastSelectedFont() {
        return pref.getInt(PREF.LAST_SELECTED_FONT, 0);
    }

    public void setFavoriteMemes(String[] memes) {
        String str = fromStringArray(memes);
        pref.edit().putString(PREF.FAVOURITED_MEMES, str).apply();
    }

    public String[] getFavoriteMemes() {
        return toStringArray(pref.getString(PREF.FAVOURITED_MEMES, ""));
    }

    public void appendFavoriteMeme(String meme) {
        String[] memes = insertAndMaximize(getFavoriteMemes(), meme, PREF.MAX_FAVS);
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

    public void setLastSelectedCategory(int lastSelected) {
        pref.edit().putInt(PREF.LAST_SELECTED_CATEGORY, lastSelected).apply();
    }

    public int getLastSelectedCategory() {
        return pref.getInt(PREF.LAST_SELECTED_CATEGORY, 0);
    }

    public int getGridColumnCountPortrait() {
        int count = pref.getInt(PREF.GRID_COLUMN_COUNT_PORTRAIT, -1);
        if (count == -1) {
            count = 3 + (int) Math.max(0, 0.5 * (Helpers.getEstimatedScreenSizeInches(context) - 5.0));
            setGridColumnCountPortrait(count);
        }
        return count;
    }

    public void setGridColumnCountPortrait(int value) {
        pref.edit().putInt(PREF.GRID_COLUMN_COUNT_PORTRAIT, value).apply();
    }

    public int getGridColumnCountLandscape() {
        int count = pref.getInt(PREF.GRID_COLUMN_COUNT_LANDSCAPE, -1);
        if (count == -1) {
            count = (int) (getGridColumnCountPortrait() * 1.8);
            setGridColumnCountLandscape(count);
        }
        return count;
    }

    public void setGridColumnCountLandscape(int value) {
        pref.edit().putInt(PREF.GRID_COLUMN_COUNT_LANDSCAPE, value).apply();
    }
}