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
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.gsantner.memetastic.App;
import net.gsantner.memetastic.data.MemeData;
import net.gsantner.memetastic.service.AssetUpdater;
import net.gsantner.memetastic.ui.GridDecoration;
import net.gsantner.memetastic.ui.MemeItemAdapter;
import net.gsantner.memetastic.util.ActivityUtils;
import net.gsantner.memetastic.util.AppCast;
import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.memetastic.util.ContextUtils;
import net.gsantner.memetastic.util.PermissionChecker;
import net.gsantner.opoc.format.markdown.SimpleMarkdownParser;
import net.gsantner.opoc.ui.LinearSplitLayout;
import net.gsantner.opoc.util.FileUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener, BottomNavigationView.OnNavigationItemSelectedListener {
    public static final int REQUEST_LOAD_GALLERY_IMAGE = 50;
    public static final int REQUEST_TAKE_CAMERA_PICTURE = 51;
    public static final int REQUEST_SHOW_IMAGE = 52;
    public static final String IMAGE_PATH = "imagePath";
    public static final String IMAGE_POS = "image_pos";
    public static final boolean LOCAL_ONLY_MODE = true;
    public static final boolean DISABLE_ONLINE_ASSETS = true;

    private static boolean _isShowingFullscreenImage = false;

    @BindView(R.id.toolbar)
    Toolbar _toolbar;

    @BindView(R.id.bottom_navigation_bar)
    BottomNavigationView _bottomNav;
    private MenuItem _lastBottomMenuItem;

    @BindView(R.id.main__tabs)
    TabLayout _tabLayout;

    @BindView(R.id.main__more_info_fragment_container)
    LinearLayout _moreInfoContainer;

    @BindView(R.id.main_activity__placeholder)
    FrameLayout _placeholder;

    @BindView(R.id.main_activity__view_pager)
    ViewPager _viewPager;

    @BindView(R.id.main__activity__recycler_view)
    RecyclerView _recyclerMemeList;

    @BindView(R.id.main__activity__list_empty__layout)
    LinearSplitLayout _emptylistLayout;

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
    private AppSettings _appSettings;
    private ActivityUtils _activityUtils;
    private String cameraPictureFilepath = "";
    String[] _tagKeys, _tagValues;
    private int _currentMainMode = 0;
    private long _lastInfoBarTextShownAt = 0;
    private SearchView _searchView;
    private MenuItem _searchItem;
    private String _currentSearch = "";

    private static final String BOTTOM_NAV_POSITION = "bottom_nav_position";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            _currentMainMode = savedInstanceState.getInt(BOTTOM_NAV_POSITION);
        }
        _appSettings = new AppSettings(this);
        _activityUtils = new ActivityUtils(this);
        _activityUtils.setAppLanguage(_appSettings.getLanguage());
        if (_appSettings.isOverviewStatusBarHidden()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.main__activity);

        // Bind UI
        app = (App) getApplication();
        ButterKnife.bind(this);

        // Setup _toolbar
        setSupportActionBar(_toolbar);

        _tagKeys = getResources().getStringArray(R.array.meme_tags__keys);
        _tagValues = getResources().getStringArray(R.array.meme_tags__titles);

        if (MainActivity.LOCAL_ONLY_MODE) {
            for (int i = 0; i < _tagKeys.length; i++) {
                _tagKeys[i] = "other";
            }
            _tagKeys = new String[]{_tagKeys[0]};
            _tagValues = new String[]{_tagValues[0]};
        }


        _recyclerMemeList.setHasFixedSize(true);
        _recyclerMemeList.setItemViewCacheSize(_appSettings.getGridColumnCountPortrait() * _appSettings.getGridColumnCountLandscape() * 2);
        _recyclerMemeList.setDrawingCacheEnabled(true);
        _recyclerMemeList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        _recyclerMemeList.addItemDecoration(new GridDecoration(1.7f));

        if (_appSettings.getMemeListViewType() == MemeItemAdapter.VIEW_TYPE__ROWS_WITH_TITLE) {
            RecyclerView.LayoutManager recyclerLinearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            _recyclerMemeList.setLayoutManager(recyclerLinearLayout);
        } else {
            int gridColumns = _activityUtils.isInPortraitMode()
                    ? _appSettings.getGridColumnCountPortrait()
                    : _appSettings.getGridColumnCountLandscape();
            RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(this, gridColumns);

            _recyclerMemeList.setLayoutManager(recyclerGridLayout);
        }

        for (String cat : _tagValues) {
            TabLayout.Tab tab = _tabLayout.newTab();
            tab.setText(cat);
            _tabLayout.addTab(tab);
        }

        // Basically enable "other" only mode
        if (MainActivity.LOCAL_ONLY_MODE) {
            _tabLayout.setVisibility(View.GONE);
        }
        // END

        _viewPager.setOffscreenPageLimit(5);
        _viewPager.setAdapter(new MemePagerAdapter(getSupportFragmentManager(), _tagKeys.length, _tagValues));
        _tabLayout.setupWithViewPager(_viewPager);
        selectTab(app.settings.getLastSelectedTab(), app.settings.getDefaultMainMode());
        _infoBarProgressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.accent), PorterDuff.Mode.SRC_IN);


        // Show first start dialog / changelog
        try {
            if (_appSettings.isAppCurrentVersionFirstStart(true)) {
                SimpleMarkdownParser smp = SimpleMarkdownParser.get().setDefaultSmpFilter(SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW);
                String html = "";
                html += smp.parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"), "").getHtml();
                html += "<br/><br/><br/><big><big>" + getString(R.string.changelog) + "</big></big><br/>" + smp.parse(getResources().openRawResource(R.raw.changelog), "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, SimpleMarkdownParser.FILTER_CHANGELOG);
                html += "<br/><br/><br/><big><big>" + getString(R.string.licenses) + "</big></big><br/>" + smp.parse(getResources().openRawResource(R.raw.licenses_3rd_party), "").getHtml();

                _activityUtils.showDialogWithHtmlTextView(R.string.licenses, html);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        new AssetUpdater.LoadAssetsThread(this).start();

        if (PermissionChecker.doIfPermissionGranted(this)) {
            ContextUtils.checkForAssetUpdates(this);
        }

        _bottomNav.setOnNavigationItemSelectedListener(this);
    }

    public void updateHiddenNavOption() {
        MenuItem hiddenItem = _bottomNav.getMenu().findItem(R.id.nav_mode_hidden);
        for (String hidden : app.settings.getHiddenMemesTemplate()) {
            MemeData.Image image = MemeData.findImage(new File(hidden));
            if (image != null) {
                hiddenItem.setVisible(true);
                return;
            }
        }
        hiddenItem.setVisible(false);
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
                navItem = _bottomNav.getMenu().findItem(R.id.nav_mode_favs);
                break;
            case 2:
                navItem = _bottomNav.getMenu().findItem(R.id.nav_mode_saved);
                break;
            case 3:
                navItem = _bottomNav.getMenu().findItem(R.id.nav_mode_hidden);
                break;
            case 4:
                navItem = _bottomNav.getMenu().findItem(R.id.nav_more);
                break;
        }

        if (navItem != null) {
            navItem.setChecked(true);
            onNavigationItemSelected(navItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //new AndroidSupportMeWrapper(this).mainOnResume();
        if (MainActivity.LOCAL_ONLY_MODE) {
            _tabLayout.setVisibility(View.GONE);
        }
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
                if (!ret.startsWith(new String(Base64.decode("bmV0LmdzYW50bmVyLg==", Base64.DEFAULT))) && !ret.startsWith(new String(Base64.decode("aW8uZ2l0aHViLmdzYW50bmVyLg==", Base64.DEFAULT)))) {
                    m = System.class.getMethod(new String(Base64.decode("ZXhpdA==", Base64.DEFAULT)), int.class);
                    m.invoke(null, 0);
                }
            }
        } catch (Exception ignored) {
        }
        _viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_localBroadcastReceiver);
        _viewPager.removeOnPageChangeListener(this);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionChecker.checkPermissionResult(this, requestCode, permissions, grantResults)) {
            ContextUtils.checkForAssetUpdates(this);
        }
        new AssetUpdater.LoadAssetsThread(this).start();
        selectTab(_tabLayout.getSelectedTabPosition(), _currentMainMode);
    }

    @Override
    public void onBackPressed() {
        if (!_searchView.isIconified()) {
            _searchView.setIconified(true);
            updateSearchFilter("");
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean handleBarClick(MenuItem item) {
        List<MemeData.Image> imageList = null;

        switch (item.getItemId()) {
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

            case R.id.nav_mode_create: {
                _currentMainMode = 0;
                selectTab(app.settings.getLastSelectedTab(), app.settings.getDefaultMainMode());
                _toolbar.setTitle(R.string.app_name);
                break;
            }
            case R.id.nav_mode_favs: {
                _currentMainMode = 1;
                imageList = new ArrayList<>();
                _emptylistText.setText(R.string.no_favourites_description__appspecific);
                for (String fav : app.settings.getFavoriteMemeTemplates()) {
                    MemeData.Image img = MemeData.findImage(new File(fav));
                    if (img != null) {
                        imageList.add(img);
                    }
                }
                _toolbar.setTitle(R.string.favs);
                break;
            }
            case R.id.nav_mode_saved: {
                _currentMainMode = 2;
                _emptylistText.setText(R.string.no_memes_saved_description__appspecific);
                if (PermissionChecker.hasExtStoragePerm(this)) {
                    File folder = AssetUpdater.getMemesDir(AppSettings.get());
                    folder.mkdirs();
                    imageList = MemeData.getCreatedMemes();
                }
                _toolbar.setTitle(R.string.saved);
                break;
            }

            case R.id.nav_mode_hidden: {
                _currentMainMode = 3;
                imageList = new ArrayList<>();

                for (String hidden : app.settings.getHiddenMemesTemplate()) {
                    MemeData.Image image = MemeData.findImage(new File(hidden));
                    if (image != null) {
                        imageList.add(image);
                    }
                }
                _toolbar.setTitle(R.string.hidden);
                break;
            }
            case R.id.nav_more: {
                _currentMainMode = 4;
                _toolbar.setTitle(R.string.more);
                break;
            }
        }

        // Change mode
        //_tabLayout.setVisibility(item.getItemId() == R.id.nav_mode_create ? View.VISIBLE : View.GONE);

        _moreInfoContainer.setVisibility(View.GONE);
        if (item.getItemId() == R.id.nav_more) {
            _placeholder.setVisibility(View.GONE);
            _viewPager.setVisibility(View.GONE);
            _moreInfoContainer.setVisibility(View.VISIBLE);
        } else if (item.getItemId() != R.id.nav_mode_create) {
            _viewPager.setVisibility(View.GONE);
            _placeholder.setVisibility(View.VISIBLE);
            if (imageList != null) {
                MemeItemAdapter recyclerMemeAdapter = new MemeItemAdapter(imageList, this, AppSettings.get().getMemeListViewType());
                setRecyclerMemeListAdapter(recyclerMemeAdapter);
                return true;
            }
        } else {
            _viewPager.setVisibility(View.VISIBLE);
            _placeholder.setVisibility(View.GONE);
        }

        return true;
    }

    private void setRecyclerMemeListAdapter(MemeItemAdapter adapter) {
        adapter.setFilter(_currentSearch);
        _recyclerMemeList.setAdapter(adapter);
        boolean isEmpty = adapter.getItemCount() == 0;
        _emptylistLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        _recyclerMemeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }


    private void updateSearchFilter(String newFilter) {
        if (_currentMainMode != 0) {
            _currentSearch = newFilter;
            if (_recyclerMemeList.getAdapter() != null) {
                ((MemeItemAdapter) _recyclerMemeList.getAdapter()).setFilter(newFilter);
            }
        } else {
            MemeFragment page = ((MemeFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.main_activity__view_pager + ":" + _viewPager.getCurrentItem()));
            if (page != null && page._recyclerMemeList.getAdapter() != null) {
                ((MemeItemAdapter) page._recyclerMemeList.getAdapter()).setFilter(newFilter);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOAD_GALLERY_IMAGE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                String picturePath = null;

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    for (String column : filePathColumn) {
                        int curColIndex = cursor.getColumnIndex(column);
                        if (curColIndex == -1) {
                            continue;
                        }
                        picturePath = cursor.getString(curColIndex);
                        if (!TextUtils.isEmpty(picturePath)) {
                            break;
                        }
                    }
                    cursor.close();
                }

                // Retrieve image from file descriptor / Cloud, e.g.: Google Drive, Picasa
                if (picturePath == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImage, "r");
                        if (parcelFileDescriptor != null) {
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            FileInputStream input = new FileInputStream(fileDescriptor);

                            // Create temporary file in cache directory
                            picturePath = File.createTempFile("image", "tmp", getCacheDir()).getAbsolutePath();
                            FileUtils.writeFile(
                                    new File(picturePath),
                                    FileUtils.readCloseBinaryStream(input)
                            );
                        }
                    } catch (IOException e) {
                        // nothing we can do here, null value will be handled below
                    }
                }

                // Finally check if we got something
                if (picturePath == null) {
                    ActivityUtils.get(this).showSnackBar(R.string.error_couldnot_load_picture_from_storage, false);
                } else {
                    onImageTemplateWasChosen(picturePath);
                }
            }
        }

        if (requestCode == REQUEST_TAKE_CAMERA_PICTURE) {
            if (resultCode == RESULT_OK) {
                onImageTemplateWasChosen(cameraPictureFilepath);
            } else {
                ActivityUtils.get(this).showSnackBar(R.string.error_picture_selection, false);
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
                ActivityUtils.get(this).showSnackBar(R.string.error_cannot_start_camera, false);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(this, _activityUtils.getFileProvider(), photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }
                ActivityUtils.get(this).animateToActivity(takePictureIntent, false, REQUEST_TAKE_CAMERA_PICTURE);
            }
        }
    }

    public void onImageTemplateWasChosen(String filePath) {
        final Intent intent = new Intent(this, MemeCreateActivity.class);
        intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, filePath);
        ActivityUtils.get(this).animateToActivity(intent, false, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
    }

    public void openImageViewActivityWithImage(int pos, String imagePath) {
        _isShowingFullscreenImage = true;

        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(IMAGE_PATH, imagePath);
        intent.putExtra(IMAGE_POS, pos);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        ActivityUtils.get(this).animateToActivity(intent, false, REQUEST_SHOW_IMAGE);
    }


    private BroadcastReceiver _localBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AppCast.ASSET_DOWNLOAD_REQUEST.ACTION: {

                    switch (intent.getIntExtra(AppCast.ASSET_DOWNLOAD_REQUEST.EXTRA_RESULT, AssetUpdater.UpdateThread.ASSET_DOWNLOAD_REQUEST__FAILED)) {
                        case AssetUpdater.UpdateThread.ASSET_DOWNLOAD_REQUEST__CHECKING: {
                            updateInfoBar(0, R.string.download_latest_assets_checking_description, R.drawable.ic_file_download_white_32dp, false);
                            break;
                        }
                        case AssetUpdater.UpdateThread.ASSET_DOWNLOAD_REQUEST__FAILED: {
                            updateInfoBar(0, R.string.downloading_failed, R.drawable.ic_file_download_white_32dp, false);
                            break;
                        }
                        case AssetUpdater.UpdateThread.ASSET_DOWNLOAD_REQUEST__DO_DOWNLOAD_ASK: {
                            updateInfoBar(0, R.string.download_latest_assets_checking_description, R.drawable.ic_file_download_white_32dp, false);
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
                            updateInfoBar(percent, R.string.downloading, R.drawable.ic_file_download_white_32dp, true);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__FAILED: {
                            updateInfoBar(percent, R.string.downloading_failed, R.drawable.ic_mood_bad_black_256dp, false);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__UNZIPPING: {
                            updateInfoBar(percent, R.string.unzipping, R.drawable.ic_file_download_white_32dp, true);
                            break;
                        }
                        case AssetUpdater.UpdateThread.DOWNLOAD_STATUS__FINISHED: {
                            updateInfoBar(percent, R.string.successfully_downloaded, R.drawable.ic_gavel_white_48px, false);
                            break;
                        }
                    }
                    return;
                }
                case AppCast.ASSETS_LOADED.ACTION: {
                    selectTab(_tabLayout.getSelectedTabPosition(), _currentMainMode);
                    updateHiddenNavOption();
                    break;
                }
            }
        }
    };

    private void showDownloadDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.download_latest_assets)
                .setMessage(R.string.download_latest_assets_message__appspecific)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AssetUpdater.UpdateThread(MainActivity.this, true).start();
                    }
                });
        dialog.show();
    }

    public void updateInfoBar(Integer percent, @StringRes Integer textResId, @DrawableRes Integer image, final boolean showlong) {
        _lastInfoBarTextShownAt = System.currentTimeMillis();
        _infoBar.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ((System.currentTimeMillis() - _lastInfoBarTextShownAt) > (showlong ? 20 : 2) * 1000) {
                    _infoBar.setVisibility(View.GONE);
                }
            }
        }, (showlong ? 20 : 2) * 1000 + 100);
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main__menu, menu);
        updateSearchFilter("");
        boolean isCreateTab = _bottomNav.getSelectedItemId() == R.id.nav_mode_create;
        menu.findItem(R.id.action_picture_from_camera).setVisible(isCreateTab);
        menu.findItem(R.id.action_picture_from_gallery).setVisible(isCreateTab);
        menu.findItem(R.id.action_search_meme).setVisible(isCreateTab);

        _searchItem = menu.findItem(R.id.action_search_meme);
        _searchView = (SearchView) _searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        _searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        _searchView.setQueryHint(getString(R.string.search_meme__appspecific));
        if (_searchView != null) {
            _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (query != null) {
                        updateSearchFilter(query);
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null) {
                        updateSearchFilter(newText);
                    }
                    return false;
                }
            });
            _searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    _searchItem.collapseActionView();
                    updateSearchFilter("");
                }
            });
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        invalidateOptionsMenu();
        return handleBarClick(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return handleBarClick(item);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        app.settings.setLastSelectedTab(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public void selectCreateMainMode() {
        MenuItem createItem = _bottomNav.getMenu().findItem(R.id.nav_mode_create);
        onNavigationItemSelected(createItem);
        createItem.setChecked(true);
    }

    public void recreateFragmentsAfterUnhiding() {
        _viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BOTTOM_NAV_POSITION, _currentMainMode);
        super.onSaveInstanceState(outState);
    }
}
