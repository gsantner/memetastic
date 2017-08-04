package io.github.gsantner.memetastic.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeCategory;
import io.github.gsantner.memetastic.data.MemeLibConfig;
import io.github.gsantner.memetastic.data.MemeOriginAssets;
import io.github.gsantner.memetastic.data.MemeOriginFavorite;
import io.github.gsantner.memetastic.data.MemeOriginInterface;
import io.github.gsantner.memetastic.data.MemeOriginStorage;
import io.github.gsantner.memetastic.ui.GridDecoration;
import io.github.gsantner.memetastic.ui.GridRecycleAdapter;
import io.github.gsantner.memetastic.util.Helpers;
import io.github.gsantner.memetastic.util.ThumbnailCleanupTask;
import io.github.gsantner.opoc.util.HelpersA;
import io.github.gsantner.opoc.util.SimpleMarkdownParser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener {

    public static final int REQUEST_LOAD_GALLERY_IMAGE = 50;
    public static final int REQUEST_TAKE_CAMERA_PICTURE = 51;
    public static final String IMAGE_PATH = "imagePath";
    private static boolean isShowingFullscreenImage = false;
    private boolean areTabsReady = false;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.main__activity__navview)
    NavigationView navigationView;

    @BindView(R.id.main__tabs)
    TabLayout tabLayout;

    @BindView(R.id.main__activity__recycler_view)
    RecyclerView recyclerMemeList;

    @BindView(R.id.main__activity__list_empty__layout)
    LinearLayout emptylistLayout;

    @BindView(R.id.main__activity__list_empty__text)
    TextView emptylistText;

    App app;
    private String cameraPictureFilepath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main__activity);

        // Bind UI
        app = (App) getApplication();
        ButterKnife.bind(this);

        // Setup toolbar
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.main__navdrawer__open, R.string.main__navdrawer__close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        tabLayout.setOnTabSelectedListener(this);

        // Setup Floating Action Button
        int gridColumns = Helpers.get().isInPortraitMode()
                ? app.settings.getGridColumnCountPortrait()
                : app.settings.getGridColumnCountLandscape();

        recyclerMemeList.setHasFixedSize(true);
        RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(this, gridColumns);
        recyclerMemeList.setLayoutManager(recyclerGridLayout);
        recyclerMemeList.addItemDecoration(new GridDecoration(10));

        for (String cat : getResources().getStringArray(R.array.meme_categories)) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(cat);
            tabLayout.addTab(tab);
        }
        areTabsReady = true;
        selectTab(app.settings.getLastSelectedTab(), app.settings.getDefaultMainMode());

        //
        // Actions based on build type or version
        //
        navigationView.getMenu().findItem(R.id.action_donate_bitcoin).setVisible(!BuildConfig.IS_GPLAY_BUILD);


        // Show first start dialog / changelog
        try {
            SimpleMarkdownParser mdParser = SimpleMarkdownParser.get().setDefaultSmpFilter(SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW);
            if (app.settings.isAppFirstStart(true)) {
                String html = mdParser.parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"), "").getHtml();
                html += mdParser.parse(getResources().openRawResource(R.raw.licenses_3rd_party), "").getHtml();

                HelpersA.get(this).showDialogWithHtmlTextView(R.string.licenses, html);
            } else if (app.settings.isAppCurrentVersionFirstStart()) {
                mdParser.parse(
                        getResources().openRawResource(R.raw.changelog), "");
                HelpersA.get(this).showDialogWithHtmlTextView(R.string.main__changelog, mdParser.getHtml());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (BuildConfig.IS_TEST_BUILD) {
            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.main__activity__navheader__image)).setImageResource(R.drawable.ic_launcher_test);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void selectTab(int pos, int mainMode) {
        MenuItem navItem = null;
        switch (mainMode) {
            case 0:
                pos = pos >= 0 ? pos : tabLayout.getTabCount() - 1;
                pos = pos < tabLayout.getTabCount() ? pos : 0;
                tabLayout.getTabAt(pos).select();
                break;
            case 1:
                navItem = navigationView.getMenu().findItem(R.id.action_mode_favs);
                break;
            case 2:
                navItem = navigationView.getMenu().findItem(R.id.action_mode_saved);
                break;
        }

        if (navItem != null) {
            navigationView.setCheckedItem(navItem.getItemId());
            onNavigationItemSelected(navItem);
        }
    }

    @Override
    protected void onResume() {
        if (isShowingFullscreenImage) {
            isShowingFullscreenImage = false;
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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
        new ThumbnailCleanupTask(this).start();

        super.onResume();
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean handleBarClick(MenuItem item) {
        MemeOriginInterface memeOriginObject = null;

        switch (item.getItemId()) {
            case R.id.action_about: {
                HelpersA.get(this).animateToActivity(AboutActivity.class, false, null);
                return true;
            }
            case R.id.action_settings: {
                HelpersA.get(this).animateToActivity(SettingsActivity.class, false, SettingsActivity.ACTIVITY_ID);
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
                Helpers.get().showDonateBitcoinRequest();
                return true;
            }
            case R.id.action_homepage_code: {
                Helpers.get().openWebpageInExternalBrowser(getString(R.string.app_www_source));
                return true;
            }
            case R.id.action_picture_from_gallery: {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                HelpersA.get(this).animateToActivity(i, false, REQUEST_LOAD_GALLERY_IMAGE);
                return true;
            }
            case R.id.action_picture_from_camera: {
                showCameraDialog();
                return true;
            }

            case R.id.action_mode_create: {
                selectTab(app.settings.getLastSelectedTab(), app.settings.getDefaultMainMode());
                toolbar.setTitle(R.string.app_name);
                break;
            }
            case R.id.action_mode_favs: {
                emptylistText.setText(R.string.main__nodata__favourites);
                memeOriginObject = new MemeOriginFavorite(app.settings.getFavoriteMemes(), getAssets());
                toolbar.setTitle(R.string.main__mode__favs);
                break;
            }
            case R.id.action_mode_saved: {
                emptylistText.setText(R.string.main__nodata__saved);
                File filePath = Helpers.get().getPicturesMemetasticFolder();
                filePath.mkdirs();
                memeOriginObject = new MemeOriginStorage(filePath, getString(R.string.dot_thumbnails));
                toolbar.setTitle(R.string.main__mode__saved);
                break;
            }
        }

        // Change mode
        tabLayout.setVisibility(item.getItemId() == R.id.action_mode_create ? View.VISIBLE : View.GONE);
        if (memeOriginObject != null) {
            drawer.closeDrawers();
            GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(memeOriginObject, this);
            setRecyclerMemeListAdapter(recyclerMemeAdapter);
            return true;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setRecyclerMemeListAdapter(RecyclerView.Adapter adapter) {
        recyclerMemeList.setAdapter(adapter);
        boolean isEmpty = adapter.getItemCount() == 0;
        emptylistLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerMemeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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
                HelpersA.get(this).showSnackBar(R.string.main__error_no_picture_selected, false);
            }
        }

        if (requestCode == REQUEST_TAKE_CAMERA_PICTURE) {
            if (resultCode == RESULT_OK) {
                onImageTemplateWasChosen(cameraPictureFilepath, false);
            } else {
                HelpersA.get(this).showSnackBar(R.string.main__error_no_picture_selected, false);
            }
        }
    }

    /**
     * Show the camera picker via intent
     * Source: http://developer.android.com/training/camera/photobasics.html
     */
    public void showCameraDialog() {
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
                HelpersA.get(this).showSnackBar(R.string.main__error_camera_cannot_start, false);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                }
                HelpersA.get(this).animateToActivity(takePictureIntent, false, REQUEST_TAKE_CAMERA_PICTURE);
            }
        }
    }

    public void onImageTemplateWasChosen(String filePath, boolean bIsAsset) {
        final Intent intent = new Intent(this, MemeCreateActivity.class);
        intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, filePath);
        intent.putExtra(MemeCreateActivity.ASSET_IMAGE, bIsAsset);
        HelpersA.get(this).animateToActivity(intent, false, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
    }

    public void openImageViewActivityWithImage(String imagePath) {
        isShowingFullscreenImage = true;

        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(IMAGE_PATH, imagePath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        HelpersA.get(this).animateToActivity(intent, false, null);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        MemeOriginInterface memeOriginObject = null;
        int tabPos = tab.getPosition();

        // Asset tabs
        if (tabPos >= 0 && tabPos < MemeLibConfig.MEME_CATEGORIES.ALL.length) {
            MemeCategory memeCategory = app.getMemeCategory(MemeLibConfig.MEME_CATEGORIES.ALL[tabPos]);
            memeOriginObject = new MemeOriginAssets(memeCategory, getAssets());
        }

        // Custom tab
        if (tabPos >= 0 && tabPos == MemeLibConfig.MEME_CATEGORIES.ALL.length) {
            File customFolder = Helpers.get().getPicturesMemetasticTemplatesCustomFolder();
            emptylistText.setText(R.string.main__nodata__custom_templates);
            memeOriginObject = new MemeOriginStorage(customFolder, getString(R.string.dot_thumbnails));
            ((MemeOriginStorage) memeOriginObject).setIsTemplate(true);
        }
        if (memeOriginObject != null) {
            if (areTabsReady) {
                app.settings.setLastSelectedTab(tabPos);
            }
            if (app.settings.isShuffleMemeCategories()) {
                memeOriginObject.shuffleList();
            }
            GridRecycleAdapter recyclerMemeAdapter = new GridRecycleAdapter(memeOriginObject, this);
            setRecyclerMemeListAdapter(recyclerMemeAdapter);
        }

    }

    private final RectF point = new RectF(0, 0, 0, 0);
    private static final int SWIPE_MIN_DX = 150;
    private static final int SWIPE_MAX_DY = 90;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!drawer.isDrawerOpen(GravityCompat.START) && !drawer.isDrawerVisible(GravityCompat.START) && tabLayout.getVisibility() == View.VISIBLE) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && event.getY() > (tabLayout.getY() * 2.2)) {
                point.set(event.getX(), event.getY(), 0, 0);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                point.set(point.left, point.top, event.getX(), event.getY());
                if (Math.abs(point.width()) > SWIPE_MIN_DX && Math.abs(point.height()) < SWIPE_MAX_DY) {
                    // R->L : L<-R
                    selectTab(tabLayout.getSelectedTabPosition() + (point.width() > 0 ? -1 : +1), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
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
