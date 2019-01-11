/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License of this file: GNU GPLv3 (Commercial upon request)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
#########################################################*/
package net.gsantner.memetastic.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import net.gsantner.memetastic.service.AssetUpdater;
import net.gsantner.memetastic.service.ThumbnailCleanupTask;
import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.memetastic.util.MediaStoreUtils;
import net.gsantner.memetastic.util.PermissionChecker;
import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.R;

public class SettingsActivity extends AppCompatActivity {
    static final int ACTIVITY_ID = 10;

    static class RESULT {
        static final int NOCHANGE = -1;
        static final int CHANGE = 1;
        static final int CHANGE_RESTART = 2;
    }

    @BindView(R.id.settings__appbar)
    protected AppBarLayout appBarLayout;
    @BindView(R.id.settings__toolbar)
    protected Toolbar toolbar;

    private AppSettings appSettings;
    public static int activityRetVal = RESULT.NOCHANGE;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.settings__activity);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        appSettings = AppSettings.get();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_48px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.this.onBackPressed();
            }
        });
        activityRetVal = RESULT.NOCHANGE;
        showFragment(SettingsFragmentMaster.TAG, false);
    }

    @Override
    public void onBackPressed() {
        GsPreferenceFragmentCompat prefFrag = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(SettingsFragmentMaster.TAG);
        if (prefFrag != null && prefFrag.canGoBack()) {
            prefFrag.goBack();
            return;
        }
        super.onBackPressed();
    }

    protected void showFragment(String tag, boolean addToBackStack) {
        GsPreferenceFragmentCompat fragment = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default:
                    fragment = new SettingsFragmentMaster();
                    toolbar.setTitle(R.string.settings);
                    break;
            }
        }
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__fragment_container, fragment, tag).commit();
    }

    @Override
    protected void onStop() {
        setResult(activityRetVal);
        super.onStop();
    }

    public static class SettingsFragmentMaster extends GsPreferenceFragmentCompat {
        public static final String TAG = "SettingsFragmentMaster";

        @Override
        protected void onPreferenceChanged(SharedPreferences prefs, String key) {
            if (activityRetVal == RESULT.NOCHANGE) {
                activityRetVal = RESULT.CHANGE;
            }
        }

        @Override
        public int getPreferenceResourceForInflation() {
            return R.xml.preferences_master;
        }

        @Override
        public String getFragmentTag() {
            return TAG;
        }

        @Override
        protected SharedPreferencesPropertyBackend getAppSettings(Context context) {
            return new AppSettings(context);
        }

        @SuppressLint("ApplySharedPref")
        @Override
        public Boolean onPreferenceClicked(Preference preference, String key, int keyResId) {
            if (isAdded() && preference.hasKey()) {
                Context context = getActivity();
                AppSettings settings = AppSettings.get();


                if (eq(key, R.string.pref_key__memelist_view_type)) {
                    activityRetVal = RESULT.CHANGE_RESTART;
                }
                if (eq(key, R.string.pref_key__cleanup_thumbnails)) {
                    new ThumbnailCleanupTask(context).start();
                    return true;
                }
                if (eq(key, R.string.pref_key__is_overview_statusbar_hidden)) {
                    activityRetVal = RESULT.CHANGE_RESTART;
                }
                if (eq(key, R.string.pref_key__language)) {
                    activityRetVal = RESULT.CHANGE_RESTART;
                }
                if (eq(key, R.string.pref_key__download_assets_try)) {
                    if (PermissionChecker.doIfPermissionGranted(getActivity())) {
                        Date zero = new Date(0);
                        settings.setLastArchiveCheckDate(zero);
                        settings.setLastArchiveDate(zero);
                        settings.getDefaultPreferences().edit().commit();
                        new AssetUpdater.UpdateThread(context, true).start();
                        getActivity().finish();
                    }
                }
                if (eq(key, R.string.pref_key__is_show_in_gallery)) {
                    boolean showInGallery = settings.getDefaultPreferences().getBoolean(key, true);
                    File memeDirectory = AssetUpdater.getMemesDir(AppSettings.get());
                    File noMediaFile = new File(memeDirectory, ".nomedia");
                    if (showInGallery) {
                        noMediaFile.delete();
                        MediaStoreUtils.deleteFileFromMediaStore(context, noMediaFile);
                        File[] files = memeDirectory.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            MediaStoreUtils.deleteFileFromMediaStore(context, files[i]);
                            MediaStoreUtils.addFileToMediaStore(context, files[i]);
                        }
                    } else {
                        try {
                            noMediaFile.createNewFile();
                            MediaStoreUtils.addFileToMediaStore(context, noMediaFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreferenceScreenChanged(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
            super.onPreferenceScreenChanged(preferenceFragmentCompat, preferenceScreen);
            if (!TextUtils.isEmpty(preferenceScreen.getTitle())) {
                SettingsActivity a = (SettingsActivity) getActivity();
                if (a != null) {
                    a.toolbar.setTitle(preferenceScreen.getTitle());
                }
            }
        }

        @Override
        public synchronized void doUpdatePreferences() {
            super.doUpdatePreferences();
            setPreferenceVisible(R.string.pref_key__download_assets_try, false);
        }
    }
}
