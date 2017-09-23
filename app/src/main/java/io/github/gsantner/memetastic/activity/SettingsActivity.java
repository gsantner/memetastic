package io.github.gsantner.memetastic.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.service.AssetUpdater;
import io.github.gsantner.memetastic.service.ThumbnailCleanupTask;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.PermissionChecker;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default:
                    fragment = new SettingsFragmentMaster();
                    toolbar.setTitle(R.string.settings__settings);
                    break;
            }
        }
        FragmentTransaction t = getFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__fragment_container, fragment, tag).commit();
    }

    @Override
    protected void onResume() {
        appSettings.registerPreferenceChangedListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        appSettings.unregisterPreferenceChangedListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (activityRetVal == RESULT.NOCHANGE) {
            activityRetVal = RESULT.CHANGE;
        }
    }

    @Override
    protected void onStop() {
        setResult(activityRetVal);
        super.onStop();
    }

    public static class SettingsFragmentMaster extends PreferenceFragment {
        public static final String TAG = "io.github.gsantner.memetastic.settings.SettingsFragmentMaster";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences_master);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                Context context = getActivity().getApplicationContext();
                AppSettings settings = AppSettings.get();
                String key = preference.getKey();

                if (key.equals(getString(R.string.pref_key__cleanup_thumbnails))) {
                    new ThumbnailCleanupTask(context).start();
                    return true;
                }
                if (key.equals(getString(R.string.pref_key__is_overview_statusbar_hidden))) {
                    activityRetVal = RESULT.CHANGE_RESTART;
                }
                if (key.equals(getString(R.string.pref_key__language))) {
                    activityRetVal = RESULT.CHANGE_RESTART;
                }
                if (key.equals(getString(R.string.pref_key__download_assets_try))) {
                    if (PermissionChecker.doIfPermissionGranted(getActivity())) {
                        Date zero = new Date(0);
                        settings.setLastArchiveCheckDate(zero);
                        settings.setLastArchiveDate(zero);
                        settings.getDefaultPreferences().edit().commit();
                        new AssetUpdater.UpdateThread(context, true).start();
                        getActivity().finish();
                    }
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }
    }
}
