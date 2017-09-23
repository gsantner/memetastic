package io.github.gsantner.memetastic.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ContextUtils;
import io.github.gsantner.memetastic.util.PermissionChecker;

public class ImageViewActivity extends AppCompatActivity {
    //########################
    //## UI Binding
    //########################
    @BindView(R.id.imageview_activity__expanded_image)
    ImageView _expandedImageView;

    @BindView(R.id.toolbar)
    Toolbar _toolbar;

    //#####################
    //## Members
    //#####################
    private File _imagePath;
    private Bitmap _bitmap = null;

    //#####################
    //## Methods
    //#####################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppSettings.get().isOverviewStatusBarHidden()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.imageview__activity);
        ButterKnife.bind(this);
        //ContextUtils.get().enableImmersiveMode(getWindow().getDecorView());

        setSupportActionBar(_toolbar);
        if (getSupportActionBar() != null) {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        _imagePath = new File(getIntent().getStringExtra(MainActivity.IMAGE_PATH));
        if (PermissionChecker.hasExtStoragePerm(this) && _imagePath.exists()) {
            _bitmap = ContextUtils.get().loadImageFromFilesystem(_imagePath);
        }
        if (_bitmap == null) {
            _imagePath = null;
            _bitmap = ContextUtils.get().drawableToBitmap(
                    ContextCompat.getDrawable(this, R.drawable.ic_mood_bad_black_256dp));
        }
        _expandedImageView.setImageBitmap(_bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageview__menu, menu);
        menu.findItem(R.id.action_delete).setVisible(_imagePath != null);
        return true;
    }

    @Override
    protected void onDestroy() {
        _expandedImageView.setImageBitmap(null);
        if (_bitmap != null && !_bitmap.isRecycled())
            _bitmap.recycle();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share: {
                ((App) getApplication()).shareBitmapToOtherApp(_bitmap, this);
                return true;
            }

            case R.id.action_delete: {
                finish();
                if (_imagePath != null) {
                    deleteFile(_imagePath);
                    deleteFile(new File(getCacheDir(), _imagePath.getAbsolutePath().substring(1)));
                    MemeData.Image memeData = MemeData.findImage(_imagePath);
                    if (memeData != null) {
                        MemeData.getCreatedMemes().remove(memeData);
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean deleteFile(File file) {
        return file.exists() && file.delete();
    }

    /**
     * The conf was clicked
     */
    @OnClick(R.id.imageview_activity__expanded_image)
    public void onImageClicked() {
        finish();
    }
}
