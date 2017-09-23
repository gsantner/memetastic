package io.github.gsantner.memetastic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.gsantner.opoc.util.SimpleMarkdownParser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.service.AssetUpdater;
import io.github.gsantner.memetastic.ui.GridDecoration;
import io.github.gsantner.memetastic.ui.GridRecycleAdapter;
import io.github.gsantner.memetastic.util.ActivityUtils;
import io.github.gsantner.memetastic.util.AppCast;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ContextUtils;
import io.github.gsantner.memetastic.util.PermissionChecker;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener {
    public static final int REQUEST_LOAD_GALLERY_IMAGE = 50;
    public static final int REQUEST_TAKE_CAMERA_PICTURE = 51;
    public static final int REQUEST_SHOW_IMAGE = 52;
    public static final String IMAGE_PATH = "imagePath";

    private static boolean _isShowingFullscreenImage = false;
    private boolean _areTabsReady = false;

    @BindView(R.id.toolbar)
    Toolbar _toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout _drawer;

    @BindView(R.id.main__activity__navview)
    NavigationView _navigationView;

    @BindView(R.id.main__tabs)
    TabLayout _tabLayout;

    @BindView(R.id.main__activity__recycler_view)
    RecyclerView _recyclerMemeList;

    @BindView(R.id.main__activity__list_empty__layout)
    LinearLayout _emptylistLayout;

    @BindView(R.id.main__activity__list_empty__text)
    TextView _emptylistText;

    @BindView(R.id.main__activity__infobar)
    LinearLayout _infoBar;

    @BindView(R.id.main__activity__infobar__progress)
    ProgressBar _infoBarProgressBar;

    @BindView(R.id.main__activity__infobar__image)
    ImageView _infoBarImage;

    @BindView(R.id.main__activity__infobar__text)
    TextView _infoBarText;

    App app;
    private String cameraPictureFilepath = "";
    String[] _tagKeys, _tagValues;
    private int _currentMainMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextUtils.get().setAppLanguage(AppSettings.get().getLanguage());
        if (AppSettings.get().isOverviewStatusBarHidden()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.main__activity);

        // Bind UI
        app = (App) getApplication();
        ButterKnife.bind(this);

        // Setup _toolbar
        setSupportActionBar(_toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, _drawer, _toolbar, R.string.main__navdrawer__open, R.string.main__navdrawer__close);
        _drawer.addDrawerListener(toggle);
        toggle.syncState();
        _navigationView.setNavigationItemSelectedListener(this);
        _tabLayout.setOnTabSelectedListener(this);

        _tagKeys = getResources().getStringArray(R.array.meme_tags__keys);
        _tagValues = getResources().getStringArray(R.array.meme_tags__titles);

        // Setup Floating Action Button
        int gridColumns = ContextUtils.get().isInPortraitMode()
                ? app.settings.getGridColumnCountPortrait()
                : app.settings.getGridColumnCountLandscape();

        _recyclerMemeList.setHasFixedSize(true);
        _recyclerMemeList.setItemViewCacheSize(app.settings.getGridColumnCountPortrait() * app.settings.getGridColumnCountLandscape() * 2);
        _recyclerMemeList.setDrawingCacheEnabled(true);
        _recyclerMemeList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        _recyclerMemeList.addItemDecoration(new GridDecoration(1.7f));
        RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(this, gridColumns);
        _recyclerMemeList.setLayoutManager(recyclerGridLayout);

        for (String cat : _tagValues) {
            TabLayout.Tab tab = _tabLayout.newTab();
            tab.setText(cat);
            _tabLayout.addTab(tab);
        }
        _areTabsReady = true;
        selectTab(app.settings.getLastSelectedTab(), app.settings.getDefaultMainMode());

        _infoBarProgressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.accent), PorterDuff.Mode.SRC_IN);

        //
        // Actions based on build type or version
        //
        _navigationView.getMenu().findItem(R.id.action_donate_bitcoin).setVisible(!BuildConfig.IS_GPLAY_BUILD);


        // Show first start dialog / changelog
        try {
            SimpleMarkdownParser mdParser = SimpleMarkdownParser.get().setDefaultSmpFilter(SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW);
            if (app.settings.isAppFirstStart(true)) {
                String html = mdParser.parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"), "").getHtml();
                html += mdParser.parse(getResources().openRawResource(R.raw.licenses_3rd_party), "").getHtml();

                ActivityUtils.get(this).showDialogWithHtmlTextView(R.string.licenses, html);
            } else if (app.settings.isAppCurrentVersionFirstStart()) {
                mdParser.parse(
                        getResources().openRawResource(R.raw.changelog), "",
                        SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, SimpleMarkdownParser.FILTER_CHANGELOG);
                ActivityUtils.get(this).showDialogWithHtmlTextView(R.string.changelog, mdParser.getHtml());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        new AssetUpdater.LoadAssetsThread(this).start();

        if (PermissionChecker.doIfPermissionGranted(this)) {
            ContextUtils.checkForAssetUpdates(this);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void selectTab(int pos, int mainMode) {
        MenuItem navItem = null;
        switch (mainMode) {
            case 0:
                pos = pos >= 0 ? pos : _tabLayout.getTabCount() - 1;
                pos = pos < _tabLayout.getTabCount() ? pos : 0;
                _tabLayout.getTabAt(pos).select();
                break;
            case 1:
                navItem = _navigationView.getMenu().findItem(R.id.action_mode_favs);
                break;
            case 2:
                navItem = _navigationView.getMenu().findItem(R.id.action_mode_saved);
                break;
        }

        if (navItem != null) {
            _navigationView.setCheckedItem(navItem.getItemId());
            onNavigationItemSelected(navItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (_isShowingFullscreenImage) {
            _isShowingFullscreenImage = false;
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(_localBroadcastReceiver, AppCast.getLocalBroadcastFilter());

        if (SettingsActivity.activityRetVal == SettingsActivity.RESULT.CHANGE_RESTART) {
            SettingsActivity.activityRetVal = SettingsActivity.RESULT.NOCHANGE;
            recreate();
        }

        try {
            if (new Random().nextInt(10) > 2) {
                Method m = getClass().getMethod(new String(Base64.decode("Z2V0UGFja2FnZU5hbWU=", Base64.DEFAULT)));
                String ret = (String) m.invoke(this);
                if (!ret.equals(new String(Base64.decode("aW8uZ2l0aHViLmdzYW50bmVyLm1lbWV0YXN0aWM=", Base64.DEFAULT)))
                        && !ret.equals(new String(Base64.decode("aW8uZ2l0aHViLmdzYW50bmVyLm1lbWV0YXN0aWMudGVzdA==", Base64.DEFAULT)))) {
                    m = System.class.getMethod(new String(Base64.decode("ZXhpdA==", Base64.DEFAULT)), int.class);
                    m.invoke(null, 0);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_localBroadcastReceiver);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionChecker.checkPermissionResult(this, requestCode, permissions, grantResults)) {
            ContextUtils.checkForAssetUpdates(this);
        }
        selectTab(_tabLayout.getSelectedTabPosition(), _currentMainMode);
    }

    @Override
    public void onBackPressed() {
        if (_drawer.isDrawerOpen(GravityCompat.START)) {
            _drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean handleBarClick(MenuItem item) {
        List<MemeData.Image> imageList = null;

        switch (item.getItemId()) {
            case R.id.action_about: {
                ActivityUtils.get(this).animateToActivity(AboutActivity.class, false, null);
                return true;
            }
            case R.id.action_settings: {
                ActivityUtils.get(this).animateToActivity(SettingsActivity.class, false, SettingsActivity.ACTIVITY_ID);
                return true;
            }
            case R.id.action_exit: {
                finish();
                return true;
            }
            case R.id.action_recommend: {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.main__ready_to_memetastic, getString(R.string.app_www_source)));
                startActivity(Intent.createChooser(i, getString(R.string.main__share_meme)));
                return true;
            }
            case R.id.action_donate_bitcoin: {
                ContextUtils.get().showDonateBitcoinRequest(R.string.donate__bitcoin_id, R.string.donate__bitcoin_amount, R.string.donate__bitcoin_message, R.string.donate__bitcoin_url);
                return true;
            }
            case R.id.action_homepage_code: {
                ContextUtils.get().openWebpageInExternalBrowser(getString(R.string.app_www_source));
                return true;
            }
            case R.id.action_picture_from_gallery: {
                if (PermissionChecker.doIfPermissionGranted(this)) {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    ActivityUtils.get(this).animateToActivity(i, false, REQUEST_LOAD_GALLERY_IMAGE);
                }
                return true;
            }
            case R.id.action_picture_from_camera: {
                showCameraDialog();
                return true;
            }

            case R.id.action_mode_create: {
                _currentMainMode = 0;
                _emptylistText.setText(getString(R.string.main__nodata__custom_templates, getString(R.string.custom_templates_visual)));
                selectTab(app.settings.getLastSelectedTab(), app.settings.getDefaultMainMode());
                _toolbar.setTitle(R.string.app_name);
                break;
            }
            case R.id.action_mode_favs: {
                _currentMainMode = 1;
                imageList = new ArrayList<>();
                _emptylistText.setText(R.string.main__nodata__favourites);
                for (String fav : app.settings.getFavoriteMemeTemplates()) {
                    MemeData.Image img = MemeData.findImage(new File(fav));
                    if (img != null) {
                        imageList.add(img);
                    }
                }
                _toolbar.setTitle(R.string.main__mode__favs);
                break;
            }
            case R.id.action_mode_saved: {
                _currentMainMode = 2;
                _emptylistText.setText(R.string.main__nodata__saved);
                if (PermissionChecker.hasExtStoragePerm(this)) {
                    File folder = AssetUpdater.getMemesDir(AppSettings.get());
                    folder.mkdirs();
                    imageList = MemeData.getCreatedMemes();
                }
                _toolbar.setTitle(R.string.main__mode__saved);
                break;
            }
        }

        // Change mode
        _drawer.closeDrawers();
        _tabLayout.setVisibility(item.getItemId() == R.id.action_mode_create ? View.VISIBLE : View.GONE);
        if (imageList != null) {
            GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(imageList, this);
            setRecyclerMemeListAdapter(recyclerMemeAdapter);
            return true;
        }

        _drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setRecyclerMemeListAdapter(RecyclerView.Adapter adapter) {
        _recyclerMemeList.setAdapter(adapter);
        boolean isEmpty = adapter.getItemCount() == 0;
        _emptylistLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        _recyclerMemeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOAD_GALLERY_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    // String picturePath contains the path of selected Image
                    onImageTemplateWasChosen(picturePath, false);
                }
            } else {
                ActivityUtils.get(this).showSnackBar(R.string.main__error_no_picture_selected, false);
            }
        }

        if (requestCode == REQUEST_TAKE_CAMERA_PICTURE) {
            if (resultCode == RESULT_OK) {
                onImageTemplateWasChosen(cameraPictureFilepath, false);
            } else {
                ActivityUtils.get(this).showSnackBar(R.string.main__error_no_picture_selected, false);
            }
        }
        if (requestCode == REQUEST_SHOW_IMAGE) {
            selectTab(_tabLayout.getSelectedTabPosition(), _currentMainMode);
        }
    }

    /**
     * Show the camera picker via intent
     * Source: http://developer.android.com/training/camera/photobasics.html
     */
    public void showCameraDialog() {
        if (!PermissionChecker.doIfPermissionGranted(this)) {
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                // Create an image file name
                String imageFileName = getString(R.string.app_name) + "_" + System.currentTimeMillis();
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "Camera");
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);

                // Save a file: path for use with ACTION_VIEW intents
                cameraPictureFilepath = photoFile.getAbsolutePath();

            } catch (IOException ex) {
                ActivityUtils.get(this).showSnackBar(R.string.main__error_camera_cannot_start, false);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }
                ActivityUtils.get(this).animateToActivity(takePictureIntent, false, REQUEST_TAKE_CAMERA_PICTURE);
            }
        }
    }

    public void onImageTemplateWasChosen(String filePath, boolean bIsAsset) {
        final Intent intent = new Intent(this, MemeCreateActivity.class);
        intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, filePath);
        intent.putExtra(MemeCreateActivity.ASSET_IMAGE, bIsAsset);
        ActivityUtils.get(this).animateToActivity(intent, false, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
    }

    public void openImageViewActivityWithImage(String imagePath) {
        _isShowingFullscreenImage = true;

        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(IMAGE_PATH, imagePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        ActivityUtils.get(this).animateToActivity(intent, false, REQUEST_SHOW_IMAGE);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabPos = tab.getPosition();
        List<MemeData.Image> imageList = new ArrayList<>();

        if (tabPos >= 0 && tabPos < _tagKeys.length) {
            imageList = MemeData.getImagesWithTag(_tagKeys[tabPos]);
        }

        if (_areTabsReady) {
            app.settings.setLastSelectedTab(tabPos);
        }
        if (app.settings.isShuffleTagLists()) {
            Collections.shuffle(imageList);
        }

        GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(imageList, this);
        setRecyclerMemeListAdapter(recyclerMemeAdapter);
    }

    private final RectF point = new RectF(0, 0, 0, 0);
    private static final int SWIPE_MIN_DX = 150;
    private static final int SWIPE_MAX_DY = 90;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!_drawer.isDrawerOpen(GravityCompat.START) && !_drawer.isDrawerVisible(GravityCompat.START) && _tabLayout.getVisibility() == View.VISIBLE) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && event.getY() > (_tabLayout.getY() * 2.2)) {
                point.set(event.getX(), event.getY(), 0, 0);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                point.set(point.left, point.top, event.getX(), event.getY());
                if (Math.abs(point.width()) > SWIPE_MIN_DX && Math.abs(point.height()) < SWIPE_MAX_DY) {
                    // R->L : L<-R
                    selectTab(_tabLayout.getSelectedTabPosition() + (point.width() > 0 ? -1 : +1), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private BroadcastReceiver _localBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AppCast.DOWNLOAD_REQUEST_RESULT.ACTION: {
                    switch (intent.getIntExtra(AppCast.DOWNLOAD_REQUEST_RESULT.EXTRA_RESULT, AssetUpdater.UpdateThread.DOWNLOAD_REQUEST_RESULT__FAILED)) {
                        case AssetUpdater.UpdateThread.DOWNLOAD_REQUEST_RESULT__FAILED: {
                            updateInfoBar(0, R.string.downloading_failed, R.drawable.ic_file_download_white_32dp, 1000);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_REQUEST_RESULT__DO_DOWNLOAD_ASK: {
                            updateInfoBar(0, R.string.downloading, R.drawable.ic_file_download_white_32dp, 1);
                            showDownloadDialog();
                            break;
                        }
                    }
                    return;
                }
                case AppCast.DOWNLOAD_STATUS.ACTION: {
                    int percent = intent.getIntExtra(AppCast.DOWNLOAD_STATUS.EXTRA_PERCENT, 100);
                    switch (intent.getIntExtra(AppCast.DOWNLOAD_STATUS.EXTRA_STATUS, AssetUpdater.UpdateThread.DOWNLOAD_STATUS__FAILED)) {
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__DOWNLOADING: {
                            updateInfoBar(percent, R.string.downloading, R.drawable.ic_file_download_white_32dp, -1);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__FAILED: {
                            updateInfoBar(percent, R.string.downloading_failed, R.drawable.ic_mood_bad_black_256dp, 2000);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__UNZIPPING: {
                            updateInfoBar(percent, R.string.unzipping, R.drawable.ic_file_download_white_32dp, -1);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__FINISHED: {
                            updateInfoBar(percent, R.string.downloading_success, R.drawable.ic_gavel_white_48px, 2000);
                            break;
                        }
                    }
                    return;
                }
                case AppCast.ASSETS_LOADED.ACTION: {
                    selectTab(_tabLayout.getSelectedTabPosition(), _currentMainMode);
                    return;
                }
            }
        }
    };

    private void showDownloadDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.download_latest_assets_title)
                .setMessage(R.string.download_latest_assets_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AssetUpdater.UpdateThread(MainActivity.this, true).start();
                    }
                });
        dialog.show();
    }

    public void updateInfoBar(Integer percent, @StringRes Integer textResId, @DrawableRes Integer image, int hideInMillis) {
        _infoBar.setVisibility(View.VISIBLE);
        if (hideInMillis >= 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _infoBar.setVisibility(View.GONE);
                }
            }, hideInMillis);
        }
        if (percent != null) {
            _infoBarProgressBar.setProgress(percent);
        }
        if (textResId != null) {
            _infoBarText.setText(textResId);
        }
        if (image != null) {
            _infoBarImage.setImageResource(image);
        }
    }


    //########################
    //## Single line overrides
    //########################
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main__menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return handleBarClick(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleBarClick(item);
    }

}
