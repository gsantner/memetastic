package io.github.gsantner.memetastic.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.service.AssetUpdater;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.PermissionChecker;

public class ImageViewActivity extends AppCompatActivity {
    //########################
    //## UI Binding
    //########################
    @BindView(R.id.imageview_activity__view_pager)
    ViewPager _viewPager;

    @BindView(R.id.toolbar)
    Toolbar _toolbar;

    //#####################
    //## Members
    //#####################
    private File _imageFile;
    private Bitmap _bitmap = null;
    List<MemeData.Image> imageList = null;

    //#####################
    //## Methods
    //#####################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PermissionChecker.hasExtStoragePerm(this)) {
            File folder = AssetUpdater.getMemesDir(AppSettings.get());
            folder.mkdirs();
            imageList = MemeData.getCreatedMemes();
        }

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

        _viewPager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager()));
        _viewPager.setCurrentItem(getIntent().getIntExtra(MainActivity.IMAGE_POS, 0));



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.imageview__menu, menu);
        // menu.findItem(R.id.action_delete).setVisible(_imageFile != null);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
//        _expandedImageView.setImageBitmap(null);
//        if (_bitmap != null && !_bitmap.isRecycled())
//            _bitmap.recycle();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ImageViewFragment page = null;

        if (item.getItemId() == R.id.action_share || item.getItemId() == R.id.action_delete) {
            page = ((ImageViewFragment) _viewPager.getAdapter().instantiateItem(_viewPager, _viewPager.getCurrentItem()));
        }
        switch (item.getItemId()) {

            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_share: {
                if (page != null) {
                    _bitmap = page._bitmap;
                    ((App) getApplication()).shareBitmapToOtherApp(_bitmap, this);
                }
                return true;
            }

            case R.id.action_delete: {

                _imageFile = page._imageFile;

                if (_imageFile != null) {
                    deleteFile(_imageFile);
                    deleteFile(new File(getCacheDir(), _imageFile.getAbsolutePath().substring(1)));
                    MemeData.Image memeData = MemeData.findImage(_imageFile);
                    if (memeData != null) {
                        MemeData.getCreatedMemes().remove(memeData);
                    }
                }
                _viewPager.getAdapter().notifyDataSetChanged();
                finish();
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


    class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {
            return ImageViewFragment.newInstance(i, imageList.get(i).fullPath.getAbsolutePath());
        }

        @Override
        public int getCount() {
            return imageList.size();
        }
    }
}
