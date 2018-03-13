/*
 * MemeTastic by Gregor Santner (http://gsantner.net)
 * Copyright (C) 2016-2018
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
 */
package io.github.gsantner.memetastic.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import net.gsantner.opoc.preference.GsPreferenceFragmentCompat;
import net.gsantner.opoc.preference.SharedPreferencesPropertyBackend;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.service.AssetUpdater;
import io.github.gsantner.memetastic.service.ThumbnailCleanupTask;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.MediaStoreUtils;
import io.github.gsantner.memetastic.util.PermissionChecker;

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
        toolbar.setTitle(R.string.settings__settings);
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

    protected void showFragment(String tag, boolean addToBackStack) {
        GsPreferenceFragmentCompat fragment = (GsPreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default:
                    fragment = new SettingsFragmentMaster();
                    toolbar.setTitle(R.string.settings__settings);
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
        public Boolean onPreferenceClicked(android.support.v7.preference.Preference preference) {
            if (isAdded() && preference.hasKey()) {
                Context context = getActivity();
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();


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
    }
}
