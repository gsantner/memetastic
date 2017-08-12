package io.github.gsantner.memetastic.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
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
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.Helpers;

public class ImageViewActivity extends AppCompatActivity {
    //########################
    //## UI Binding
    //########################
    @BindView(R.id.imageview_activity__expanded_image)
    ImageView expandedImageView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    //#####################
    //## Members
    //#####################
    private String imagePath;
    private Bitmap mBitmap = null;
    private App app;

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
        app = (App) getApplication();
        //Helpers.get().enableImmersiveMode(getWindow().getDecorView());

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        imagePath = getIntent().getStringExtra(MainActivity.IMAGE_PATH);
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            // Thumbnail
            imagePath = imagePath.replace(getString(R.string.app_name) + "_",
                    ".thumbnails" + File.separator + getString(R.string.app_name) + "_");
        }

        mBitmap = Helpers.get().loadImageFromFilesystem(imagePath);
        expandedImageView.setImageBitmap(mBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageview__menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        expandedImageView.setImageBitmap(null);
        if (mBitmap != null && !mBitmap.isRecycled())
            mBitmap.recycle();
        super.onDestroy();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share: {
                app.shareBitmapToOtherApp(mBitmap, this);
                return true;
            }

            case R.id.action_delete: {
                finish();
                File file = new File(imagePath);
                if (file.exists()) {
                    file.delete();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The image was clicked
     */
    @OnClick(R.id.imageview_activity__expanded_image)
    public void onImageClicked() {
        finish();
    }
}
