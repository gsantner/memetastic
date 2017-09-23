package io.github.gsantner.memetastic.util;

import android.content.Context;
import android.os.Environment;

import net.gsantner.opoc.util.AppSettingsBase;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.service.AssetUpdater;

public class AppSettings extends AppSettingsBase {
    private static final int MAX_FAVS = 50;
    private static boolean PACKAGE_CHECKED = false;

    //#####################
    //## Methods
    //#####################
    private AppSettings(Context context) {
        super(context);
    }

    public static AppSettings get() {
        AppSettings appSettings = new AppSettings(App.get());


        /*
         * Check if a MemeTastic package ID was used to build the app.
         * If you release something based on MemeTastic you will want to remove the lines below.
         * In any case: You MUST release the full source code.
         *
         * If you publish an app based on MemeTastic you MUST
         *   Comply with the terms of GPLv3 - See https://www.gnu.org/licenses/gpl-3.0.html
         *   Keep existing copyright notices in the app and publish full source code
         *   Show that the app is `based on MemeTastic by MemeTastic developers and contributors`. Include a link to https://github.com/gsantner/memetastic
         *   Show that the app is not MemeTastic but an modified/custom version, and the original app developers or contributors are not responsible for modified versions
         *   Not use MemeTastic as app name
         *
         *  See more details at
         *  https://github.com/gsantner/memetastic/blob/master/README.md#licensing
         */
        if (!PACKAGE_CHECKED) {
            PACKAGE_CHECKED = true;
            String pkg = appSettings.getContext().getPackageName();
            if (!pkg.equals("io.github.gsantner.memetastic") && !pkg.equals("io.github.gsantner.memetastic.test")) {
                String message = "\n\n\n" +
                        "++++  WARNING: MemeTastic is licensed GPLv3.\n" +
                        "++++  If you distribute the app you MUST publish the full source code.\n" +
                        "++++  See https://github.com/gsantner/memetastic for more details.\n" +
                        "++++  This warning is placed in util/AppSettings.java->get()\n\n\n";
                throw new RuntimeException(message);
            }
        }
        return appSettings;
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
        int val = getInt(R.string.pref_key__render_quality_editor_percent, 24);
        return (int) (400 + (2100.0 * (val / 100.0)));
    }

    public int getThumbnailQualityReal() {
        // 24 should be 225. Mostly 3 will be on a phone, so 1080/3=360
        // Additional reduction of quality to ~2/3 is roughly 225
        // 150 is very fast loaded, but blurry, 200 is still a little blurry, 225 seems to be
        // a good tradeoff between quality (400-600) and speed (-125)
        int val = getInt(R.string.pref_key__thumbnail_quality__percent, 24);
        return (int) (100 + (939 * (val / 100.0)));
    }

    public void setLastUsedFont(String value) {
        setString(R.string.pref_key__last_used_font, value);
    }

    public String getLastUsedFont() {
        return getString(R.string.pref_key__last_used_font, "");
    }

    public void setFavoriteMemes(String[] value) {
        setStringArray(R.string.pref_key__favourite_meme_templates, value);
    }

    public String[] getFavoriteMemeTemplates() {
        return getStringArray(R.string.pref_key__favourite_meme_templates);
    }

    public void appendFavoriteMeme(String filepath) {
        String[] memes = insertAndMaximize(getFavoriteMemeTemplates(), filepath, MAX_FAVS);
        setFavoriteMemes(memes);
    }

    public boolean isFavorite(String filepath) {
        if (getFavoriteMemeTemplates() == null)
            return false;
        for (String s : getFavoriteMemeTemplates()) {
            if (s.equals(filepath))
                return true;
        }
        return false;
    }

    public boolean toggleFavorite(String filepath) {
        if (!isFavorite(filepath)) {
            appendFavoriteMeme(filepath);
            return true;
        }
        removeFavorite(filepath);
        return false;
    }

    public void removeFavorite(String filepath) {
        String[] favs = getFavoriteMemeTemplates();
        ArrayList<String> newFavs = new ArrayList<String>();

        for (String fav : favs) {
            if (!fav.equals(filepath))
                newFavs.add(fav);
        }
        setFavoriteMemes(newFavs.toArray(new String[newFavs.size()]));
    }

    public void setLastSelectedTab(int value) {
        setInt(R.string.pref_key__last_selected_tab, value);
    }

    public int getLastSelectedTab() {
        return getInt(R.string.pref_key__last_selected_tab, 0);
    }

    public int getGridColumnCountPortrait() {
        int count = getInt(R.string.pref_key__grid_column_count_portrait, -1);
        if (count == -1) {
            count = 3 + (int) Math.max(0, 0.5 * (ContextUtils.get().getEstimatedScreenSizeInches() - 5.0));
            setGridColumnCountPortrait(count);
        }
        return count;
    }

    public void setGridColumnCountPortrait(int value) {
        setInt(R.string.pref_key__grid_column_count_portrait, value);
    }

    public int getGridColumnCountLandscape() {
        int count = getInt(R.string.pref_key__grid_column_count_landscape, -1);
        if (count == -1) {
            count = (int) (getGridColumnCountPortrait() * 1.8);
            setGridColumnCountLandscape(count);
        }
        return count;
    }

    public void setGridColumnCountLandscape(int value) {
        setInt(R.string.pref_key__grid_column_count_landscape, value);
    }

    public boolean isAppFirstStart(boolean doSet) {
        boolean value = getBool(R.string.pref_key__app_first_start, true);
        if (doSet) {
            setBool(R.string.pref_key__app_first_start, false);
        }
        return value;
    }

    public boolean isAppCurrentVersionFirstStart() {
        int value = getInt(R.string.pref_key__app_first_start_current_version, -1);
        boolean isFirstStart = value != BuildConfig.VERSION_CODE && !BuildConfig.IS_TEST_BUILD;
        setInt(R.string.pref_key__app_first_start_current_version, BuildConfig.VERSION_CODE);
        if (isFirstStart) {
            setLastArchiveCheckDate(new Date(0));
        }
        return isFirstStart;
    }

    public boolean isAutoSaveMeme() {
        return getBool(R.string.pref_key__auto_save_meme, false);
    }

    public int getDefaultMainMode() {
        return getIntOfStringPref(R.string.pref_key__default_main_mode, 0);
    }

    public boolean isShuffleTagLists() {
        return getBool(R.string.pref_key__is_shuffle_meme_tags, false);
    }

    public boolean isEditorStatusBarHidden() {
        return getBool(R.string.pref_key__is_editor_statusbar_hidden, false);
    }

    public boolean isOverviewStatusBarHidden() {
        return getBool(R.string.pref_key__is_overview_statusbar_hidden, false);
    }

    public String getLanguage() {
        return getString(R.string.pref_key__language, "");
    }

    public void setSaveDirectory(String value) {
        setString(R.string.pref_key__save_directory, value);
    }

    public File getSaveDirectory() {
        String dir = getString(R.string.pref_key__save_directory, "");
        if (dir.isEmpty()) {

            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    , rstr(R.string.app_name).toLowerCase())
                    .getAbsolutePath();
            setSaveDirectory(dir);
        }
        return new File(dir);
    }

    public Date getLastAssetArchiveDate() throws ParseException {
        String date = getString(R.string.pref_key__last_asset_archive_date, "");
        if (date.isEmpty()) {
            return new Date(0);
        }
        return AssetUpdater.FORMAT_RFC3339.parse(date);
    }

    public void setLastArchiveCheckDate(Date value) {
        setString(R.string.pref_key__last_asset_archive_check_date, AssetUpdater.FORMAT_RFC3339.format(value));
    }

    public Date getLastAssetArchiveCheckDate() {
        String date = getString(R.string.pref_key__last_asset_archive_check_date, "");
        if (date.isEmpty()) {
            return new Date(0);
        }
        try {
            return AssetUpdater.FORMAT_RFC3339.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    public void setLastArchiveDate(Date value) {
        setString(R.string.pref_key__last_asset_archive_date, AssetUpdater.FORMAT_RFC3339.format(value));
    }

    public boolean isMigrated() {
        return getBool(R.string.pref_key__is_migrated, false);
    }

    public void setMigrated(boolean value) {
        setBool(R.string.pref_key__is_migrated, value);
    }
}
