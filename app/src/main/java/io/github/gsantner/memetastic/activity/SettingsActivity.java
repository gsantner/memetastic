package io.github.gsantner.memetastic.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ThumbnailCleanupTask;

public class SettingsActivity extends AppCompatActivity {
    static final int ACTIVITY_ID = 10;

    static class RESULT {
        static final int NOCHANGE = -1;
        static final int CHANGED = 1;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.settings__activity__seek_render_quality)
    SeekBar seekRenderQuality;

    @BindView(R.id.settings__activity__edit_columns_landscape)
    EditText columnsLandscape;

    @BindView(R.id.settings__activity__edit_columns_portrait)
    EditText columnsPortrait;

    private boolean settingsChanged = false;
    AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings__activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings = ((App) getApplication()).settings;

        loadSettings();
    }

    @OnClick({R.id.action_done})
    public void onFloatingActionClicked(View v) {
        switch (v.getId()) {
            case android.R.id.home:
            case R.id.action_done:
                setResult(isSettingsChanged() ? RESULT.CHANGED : RESULT.NOCHANGE);
                finish();
                break;
        }
    }


    @SuppressLint("SetTextI18n")
    public void loadSettings() {
        seekRenderQuality.setProgress(settings.getRenderQuality() - 400);
        columnsPortrait.setText(settings.getGridColumnCountPortrait() + "");
        columnsLandscape.setText(settings.getGridColumnCountLandscape() + "");
    }

    public int editTextToInt(EditText ed) {
        return Integer.parseInt(ed.getText().toString());
    }

    public boolean isSettingsChanged() {
        if (editTextToInt(columnsPortrait) != settings.getGridColumnCountPortrait()) {
            settings.setGridColumnCountPortrait(editTextToInt(columnsPortrait));
            settingsChanged = true;
        }
        if (editTextToInt(columnsLandscape) != settings.getGridColumnCountPortrait()) {
            settings.setGridColumnCountLandscape(editTextToInt(columnsLandscape));
            settingsChanged = true;
        }
        if (seekRenderQuality.getProgress() + 400 != settings.getRenderQuality()) {
            settings.setRenderQuality(seekRenderQuality.getProgress() + 400);
            settingsChanged = true;
        }

        return settingsChanged;
    }

    @OnClick(R.id.settings__activity__button_cleanup_thumbnails)
    public void startThumbnailCleanupThread() {
        new ThumbnailCleanupTask(getApplicationContext()).start();
    }
}