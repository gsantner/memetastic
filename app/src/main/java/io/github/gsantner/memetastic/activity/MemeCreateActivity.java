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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeConfig;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.data.MemeEditorElements;
import io.github.gsantner.memetastic.data.MemeLibConfig;
import io.github.gsantner.memetastic.service.AssetUpdater;
import io.github.gsantner.memetastic.ui.FontItemAdapter;
import io.github.gsantner.memetastic.util.ActivityUtils;
import io.github.gsantner.memetastic.util.AndroidBug5497Workaround;
import io.github.gsantner.memetastic.util.AppCast;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ContextUtils;
import io.github.gsantner.memetastic.util.PermissionChecker;

/**
 * Activity for creating memes
 */
public class MemeCreateActivity extends AppCompatActivity implements ColorPickerDialogListener {
    //########################
    //## Static
    //########################
    public final static int RESULT_MEME_EDITING_FINISHED = 150;
    public final static String EXTRA_IMAGE_PATH = "MemeCreateActivity_EXTRA_IMAGE_PATH";
    public final static String EXTRA_MEMETASTIC_DATA = "MemeCreateActivity_EXTRA_MEMETASTIC_DATA";
    private static final String TAG = MemeCreateActivity.class.getSimpleName();
    //########################
    //## UI Binding
    //########################
    @BindView(R.id.fab)
    FloatingActionButton _fab;

    @BindView(R.id.toolbar)
    Toolbar _toolbar;

    @BindView(R.id.memecreate__activity__image)
    ImageView _imageEditView;

    @BindView(R.id.edit_bar)
    LinearLayout _editBar;

    @BindView(R.id.create_caption)
    EditText _create_caption;

    @BindView(R.id.memecreate__moar_controls__color_picker_for_padding)
    ColorPanelView _paddingColor;

    //#####################
    //## Members
    //#####################
    private static boolean _doubleBackToExitPressedOnce = false;
    private Bitmap _lastBitmap = null;
    private long _memeSavetime = -1;
    private App _app;
    private MemeEditorElements _memeEditorElements;
    private Bundle _savedInstanceState = null;
    boolean _bottomContainerVisible = false;
    private boolean _isBottom;
    private View _dialogView;

    //#####################
    //## Methods
    //#####################
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppSettings.get().isEditorStatusBarHidden()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.memecreate__activity);
        if (AppSettings.get().isEditorStatusBarHidden()) {
            AndroidBug5497Workaround.assistActivity(this);
        }

        // Quit activity if no conf was given
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (!(Intent.ACTION_SEND.equals(action) && type.startsWith("image/")) &&
                (!getIntent().hasExtra(EXTRA_IMAGE_PATH))) {
            finish();
            return;
        }

        // Stop if data is not loaded yet (Try load in onResume, recreate activity in broadcast)
        if (MemeData.isReady()) {
            // Bind Ui
            ButterKnife.bind(this);
            _app = (App) getApplication();

            // Set _toolbar
            setSupportActionBar(_toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            initMemeSettings(savedInstanceState);
            initMoarControlsContainer();
            initCaptionButtons();
        }
        if (savedInstanceState != null
                && savedInstanceState.containsKey("captionPosition")
                && savedInstanceState.containsKey("captionEditBar")
                && savedInstanceState.containsKey("captionText")) {
            _isBottom = savedInstanceState.getBoolean("captionPosition");
            _editBar.setVisibility(savedInstanceState.getBoolean("captionEditBar") ?
                    View.VISIBLE : View.GONE);
            _create_caption.setText(savedInstanceState.getString("captionText"));
        }
    }

    private void initCaptionButtons() {
        final ImageButton buttonTextSettings = findViewById(R.id.settings_caption);
        final ImageButton buttonOk = findViewById(R.id.done_caption);
        buttonTextSettings.setColorFilter(R.color.black);
        buttonOk.setColorFilter(R.color.black);
    }


    public void initMemeSettings(Bundle savedInstanceState) {
        MemeData.Font lastUsedFont = getFont(_app.settings.getLastUsedFont());
        Bitmap bitmap = extractBitmapFromIntent(getIntent());
        if (savedInstanceState != null && savedInstanceState.containsKey("memeObj")) {
            _memeEditorElements = (MemeEditorElements) savedInstanceState.getSerializable("memeObj");
            if (_memeEditorElements == null) {
                _memeEditorElements = new MemeEditorElements(lastUsedFont, bitmap);
            }
            _memeEditorElements.getImageMain().setImage(bitmap);
            _memeEditorElements.setFontToAll(lastUsedFont);
        } else {
            _memeEditorElements = new MemeEditorElements(lastUsedFont, bitmap);
        }
        _memeEditorElements.getImageMain().setDisplayImage(_memeEditorElements.getImageMain().getImage().copy(Bitmap.Config.RGB_565, false));
        onMemeEditorObjectChanged();
    }

    public MemeData.Font getFont(String filepath) {
        MemeData.Font font = MemeData.findFont(new File(filepath));
        if (font == null) {
            font = MemeData.getFonts().get(0);
        }
        return font;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        prepareForSaving();
        outState.putSerializable("memeObj", _memeEditorElements);
        outState.putBoolean("captionPosition", _isBottom);
        outState.putBoolean("captionEditBar", _editBar != null && _editBar.getVisibility() == View.VISIBLE);
        outState.putString("captionText", _create_caption != null ? _create_caption.getText().toString() : "");
        this._savedInstanceState = outState;
    }

    private void prepareForSaving() {
        if (_memeEditorElements == null) {
            return;
        }
        _imageEditView.setImageBitmap(null);
        if (_lastBitmap != null && !_lastBitmap.isRecycled())
            _lastBitmap.recycle();
        MemeEditorElements.EditorImage imageMain = _memeEditorElements.getImageMain();
        if (imageMain.getImage() != null && !imageMain.getImage().isRecycled())
            imageMain.getImage().recycle();
        if (imageMain.getDisplayImage() != null && !imageMain.getDisplayImage().isRecycled())
            imageMain.getDisplayImage().recycle();
        _lastBitmap = null;
        imageMain.setDisplayImage(null);
        imageMain.setImage(null);
        _memeEditorElements.setFontToAll(null);
    }

    @Override
    protected void onDestroy() {
        prepareForSaving();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // Checking if registered may not work always, therefore try to force it
            LocalBroadcastManager.getInstance(this).unregisterReceiver(_localBroadcastReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!MemeData.isReady()) {
            LocalBroadcastManager.getInstance(this).registerReceiver(_localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
            new AssetUpdater.LoadAssetsThread(this).start();
            return;
        }

        if (_savedInstanceState != null) {
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            initMemeSettings(_savedInstanceState);
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

    private BroadcastReceiver _localBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AppCast.ASSETS_LOADED.ACTION: {
                    recreate();
                }
            }
        }
    };

    private Bitmap extractBitmapFromIntent(final Intent intent) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND) && intent.getType().startsWith("image/")) {
            Uri imageURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageURI != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                } catch (IOException e) {
                    bitmap = null;
                    e.printStackTrace();
                }
            }
        } else {
            bitmap = ContextUtils.get().loadImageFromFilesystem(new File(imagePath), _app.settings.getRenderQualityReal());
        }
        return bitmap;
    }

    @Override
    public void onBackPressed() {
        boolean hasTextInput = !_create_caption.getText().toString().isEmpty() ||
                !_memeEditorElements.getCaptionBottom().getText().isEmpty() ||
                !_memeEditorElements.getCaptionTop().getText().isEmpty();

        // Close views above
        if (_bottomContainerVisible) {
            toggleMoarControls(true, false);
            return;
        }

        if (_editBar.getVisibility() != View.GONE) {
            settingsDone();
            return;
        }

        // Auto save if option checked
        if (hasTextInput && _app.settings.isAutoSaveMeme()) {
            if (saveMemeToFilesystem(false)) {
                finish();
                return;
            }
        }

        // Close if no input
        if (!hasTextInput) {
            finish();
            return;
        }

        // Else wait for double back-press
        if (_doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        _doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(android.R.id.content), R.string.creator__press_back_again_to_exit, Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                _doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @OnTouch(R.id.memecreate__activity__image)
    public boolean onImageTouched(View view, MotionEvent event) {
        if (_editBar.getVisibility() == View.VISIBLE && !_create_caption.getText().toString().isEmpty()) {
            onMemeEditorObjectChanged();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float heightOfPic = view.getMeasuredHeight();
            float heightOfEvent = event.getY();

            int position = (int) (heightOfEvent / heightOfPic * 100);

            _isBottom = position >= 50;

            _editBar.setVisibility(View.VISIBLE);

            String _areaCaption = _isBottom ?
                    _memeEditorElements.getCaptionBottom().getText() :
                    _memeEditorElements.getCaptionTop().getText();

            _create_caption.setText(_areaCaption);
            _create_caption.requestFocus();

            ActivityUtils.get(this).showSoftKeyboard();

            if (_bottomContainerVisible) {
                toggleMoarControls(true, false);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @OnClick(R.id.settings_caption)
    public void openSettingsDialog() {
        ActivityUtils.get(this).hideSoftKeyboard();
        _dialogView = View.inflate(this, R.layout.ui__memecreate__text_settings, null);

        initDialogViews(_dialogView);

        AlertDialog.Builder Builder = new AlertDialog.Builder(this);

        Builder.setTitle(R.string.settings__settings)
                //dialog _dialogView
                .setView(_dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //retrieve values of widgets
                        //return focus to _create_caption
                        _create_caption.requestFocus();
                    }
                })
                .create().show();
    }

    @OnClick(R.id.done_caption)
    public void settingsDone() {
        _editBar.setVisibility(View.GONE);
        ActivityUtils.get(this).hideSoftKeyboard();
        onMemeEditorObjectChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.creatememe__menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

        switch (item.getItemId()) {
            case R.id.action_share: {
                recreateImage(true);
                _app.shareBitmapToOtherApp(_lastBitmap, this);
                return true;
            }
            case R.id.action_save: {
                recreateImage(true);
                saveMemeToFilesystem(true);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveMemeToFilesystem(boolean showDialog) {
        if (!PermissionChecker.doIfPermissionGranted(this)) {
            return false;
        }

        File folder = AssetUpdater.getMemesDir(AppSettings.get());
        if (_memeSavetime < 0) {
            _memeSavetime = System.currentTimeMillis();
        }

        String filename = String.format(Locale.getDefault(), "%s_%s.jpg", getString(R.string.app_name), AssetUpdater.FORMAT_MINUTE_FILE.format(new Date(_memeSavetime)));
        File fullpath = new File(folder, filename);
        boolean wasSaved = ContextUtils.get().writeImageToFileJpeg(fullpath, _lastBitmap);
        if (wasSaved && showDialog) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.creator__saved_successfully)
                    .setMessage(R.string.creator__saved_successfully_message)
                    .setNegativeButton(R.string.creator__keep_editing, null)
                    .setNeutralButton(R.string.main__share_meme, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            _app.shareBitmapToOtherApp(_lastBitmap, MemeCreateActivity.this);
                        }
                    })
                    .setPositiveButton(R.string.main__yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
        }
        if (wasSaved) {
            MemeConfig.Image confImage = AssetUpdater.generateImageEntry(folder, filename, new String[0]);
            MemeData.Image dataImage = new MemeData.Image();
            dataImage.conf = confImage;
            dataImage.fullPath = fullpath;
            dataImage.isTemplate = false;
            MemeData.getCreatedMemes().add(dataImage);
        }
        return wasSaved;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionChecker.checkPermissionResult(this, requestCode, permissions, grantResults);
    }

    public void toggleMoarControls(boolean forceVisibile, boolean visible) {
        _bottomContainerVisible = !_bottomContainerVisible;
        if (forceVisibile) {
            _bottomContainerVisible = visible;
        }

        _create_caption.setVisibility(_bottomContainerVisible ? View.GONE : View.VISIBLE);
        _toolbar.setVisibility(_bottomContainerVisible ? View.GONE : View.VISIBLE);

        // higher weightRatio means the conf is more wide, so below _dialogView can be higher
        // 100 is the max weight, 55 means the below _dialogView is a little more weighted
        Bitmap curImg = _memeEditorElements.getImageMain().getDisplayImage();
        int weight = (int) (55f * (1 + ((curImg.getWidth() / (float) curImg.getHeight()) / 10f)));
        weight = weight > 100 ? 100 : weight;

        // Set weights. If _bottomContainerVisible == false -> Hide them = 0 weight
        View container = findViewById(R.id.memecreate__activity__image_container);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) container.getLayoutParams();
        lp.height = 0;
        lp.weight = _bottomContainerVisible ? 100 - weight : 100;
        container.setLayoutParams(lp);
        container = findViewById(R.id.memecreate__activity__moar_controls_container);
        container.setVisibility(_bottomContainerVisible ? View.VISIBLE : View.GONE);
        lp = (LinearLayout.LayoutParams) container.getLayoutParams();
        lp.height = 0;
        lp.weight = _bottomContainerVisible ? weight : 0;
        container.setLayoutParams(lp);
    }

    @OnClick(R.id.fab)
    public void onFloatingButtonClicked(View view) {
        toggleMoarControls(false, false);
        ActivityUtils.get(this).hideSoftKeyboard();
        View focusedView = this.getCurrentFocus();
        if (focusedView != null) {
            ActivityUtils.get(this).hideSoftKeyboard();
        }
    }

    private void initDialogViews(View view) {
        SeekBar textSize = view.findViewById(R.id.meme_dialog__seek_font_size);
        View textBackGroundColor = view.findViewById(R.id.meme_dialog__color_picker_for_text);
        View textBorderColor = view.findViewById(R.id.meme_dialog__color_picker_for_border);
        Switch allCapsSwitch = view.findViewById(R.id.meme_dialog__toggle_all_caps);
        Spinner fontDropDown = view.findViewById(R.id.meme_dialog__dropdown_font);

        FontItemAdapter fontAdapter = new FontItemAdapter(this,
                android.R.layout.simple_list_item_1, MemeData.getFonts(),
                false, getString(R.string.creator__font));
        fontDropDown.setAdapter(fontAdapter);
        fontAdapter.setSelectedFont(fontDropDown, _memeEditorElements.getCaptionTop().getFont());

        textBackGroundColor.setBackgroundColor(_memeEditorElements.getCaptionTop().getTextColor());
        textBorderColor.setBackgroundColor(_memeEditorElements.getCaptionTop().getBorderColor());

        allCapsSwitch.setChecked(_memeEditorElements.getCaptionTop().isAllCaps());
        textSize.setProgress(_memeEditorElements.getCaptionTop().getFontSize() - MemeLibConfig.FONT_SIZES.MIN);


        //listeners

        View.OnClickListener colorListeners = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.meme_dialog__color_picker_for_text:
                        if (_isBottom) {
                            showColorDialog(view.getId(), _memeEditorElements.getCaptionBottom().getTextColor());
                        } else {
                            showColorDialog(view.getId(), _memeEditorElements.getCaptionTop().getTextColor());
                        }
                        break;
                    case R.id.meme_dialog__color_picker_for_border:
                        if (_isBottom) {
                            showColorDialog(view.getId(), _memeEditorElements.getCaptionBottom().getBorderColor());
                        } else {
                            showColorDialog(view.getId(), _memeEditorElements.getCaptionTop().getBorderColor());
                        }
                }
            }
        };

        textBackGroundColor.setOnClickListener(colorListeners);
        textBorderColor.setOnClickListener(colorListeners);

        //drop down
        fontDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setFont((MemeData.Font)
                            parent.getSelectedItem());
                } else {
                    _memeEditorElements.getCaptionTop().setFont((MemeData.Font)
                            parent.getSelectedItem());
                }
                _app.settings.setLastUsedFont(((MemeData.Font) parent.getSelectedItem()).fullPath.getAbsolutePath());
                onMemeEditorObjectChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //seekBar
        textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setFontSize(progress + MemeLibConfig.FONT_SIZES.MIN);
                } else {
                    _memeEditorElements.getCaptionTop().setFontSize(progress + MemeLibConfig.FONT_SIZES.MIN);
                }
                onMemeEditorObjectChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //switch
        allCapsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setAllCaps(isChecked);
                } else {
                    _memeEditorElements.getCaptionTop().setAllCaps(isChecked);
                }
                onMemeEditorObjectChanged();
            }
        });
    }

    private void initMoarControlsContainer() {
        final Button rotateButton = ButterKnife.findById(this, R.id.memecreate__moar_controls__rotate_plus_90deg);
        final SeekBar seekPaddingSize = ButterKnife.findById(this, R.id.memecreate__moar_controls__seek_padding_size);
        final ColorPanelView colorPickerPadding = ButterKnife.findById(this, R.id.memecreate__moar_controls__color_picker_for_padding);
        // Apply existing settings
        _paddingColor.setColor(_memeEditorElements.getImageMain().getPaddingColor());
        seekPaddingSize.setProgress(_memeEditorElements.getImageMain().getPadding());


        //
        //  Add bottom sheet listeners
        //
        View.OnClickListener colorListener = new View.OnClickListener() {
            public void onClick(View v) {
                showColorDialog(R.id.memecreate__moar_controls__color_picker_for_padding,
                        _memeEditorElements.getImageMain().getPaddingColor());
                onMemeEditorObjectChanged();
            }
        };

        colorPickerPadding.setOnClickListener(colorListener);

        seekPaddingSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                _memeEditorElements.getImageMain().setPadding(progress);
                onMemeEditorObjectChanged();
            }
        });
        rotateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                _memeEditorElements.getImageMain().setRotationDeg((_memeEditorElements.getImageMain().getRotationDeg() + 90) % 360);
                onMemeEditorObjectChanged();
            }
        });
    }

    private void showColorDialog(int id, @ColorInt int color) {
        ColorPickerDialog.newBuilder()
                .setDialogId(id)
                .setColor(color)
                .setPresets(MemeLibConfig.MEME_COLORS.ALL)
                .setCustomButtonText(R.string.palette_colors)
                .setPresetsButtonText(R.string.preset_colors)
                .setDialogTitle(R.string.select_color)
                .setSelectedButtonText(android.R.string.ok)
                .show(this);
    }


    @Override
    public void onColorSelected(int id, @ColorInt int colorInt) {
        switch (id) {
            case R.id.meme_dialog__color_picker_for_border: {// border color
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setBorderColor(colorInt);
                } else {
                    _memeEditorElements.getCaptionTop().setBorderColor(colorInt);
                }
                View view = _dialogView.findViewById(R.id.meme_dialog__color_picker_for_border);
                view.setBackgroundColor(colorInt);
                break;
            }
            case R.id.meme_dialog__color_picker_for_text: {// text background color
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setTextColor(colorInt);
                } else {
                    _memeEditorElements.getCaptionTop().setTextColor(colorInt);
                }
                View view = _dialogView.findViewById(R.id.meme_dialog__color_picker_for_text);
                view.setBackgroundColor(colorInt);
                break;
            }
            case R.id.memecreate__moar_controls__color_picker_for_padding: { // padding color
                _memeEditorElements.getImageMain().setPaddingColor(colorInt);
                _memeEditorElements.getImageMain().setPaddingColor(colorInt);
                _paddingColor.setColor(colorInt);
                break;
            }
            default: {
                Log.i(TAG, "Wrong selection");
                break;
            }
        }
        onMemeEditorObjectChanged();

    }

    @Override
    public void onDialogDismissed(int id) {
    }

    public Bitmap makeMemeImageFromElements(Context c, MemeEditorElements memeEditorElements) {
        // prepare canvas
        Bitmap bitmap = memeEditorElements.getImageMain().getDisplayImage();

        if (memeEditorElements.getImageMain().getRotationDeg() != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(memeEditorElements.getImageMain().getRotationDeg());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        double pad = 1 + memeEditorElements.getImageMain().getPadding() / 100.0;
        if (pad > 1.01) {
            Bitmap workBmp = Bitmap.createBitmap((int) (bitmap.getWidth() * pad), (int) (bitmap.getHeight() * pad), Bitmap.Config.ARGB_8888);
            Canvas can = new Canvas(workBmp);
            //can.drawARGB(0xFF, 0xFF, 0xFF, 0xFF); //This represents White color
            can.drawColor(memeEditorElements.getImageMain().getPaddingColor());
            can.drawBitmap(bitmap, (int) ((workBmp.getWidth() - bitmap.getWidth()) / 2.0), (int) ((workBmp.getHeight() - bitmap.getHeight()) / 2.0), null);
            bitmap = workBmp;
        }

        float scale = ContextUtils.get().getScalingFactorInPixelsForWritingOnPicture(bitmap.getWidth(), bitmap.getHeight());
        float borderScale = scale * memeEditorElements.getCaptionTop().getFontSize() / MemeLibConfig.FONT_SIZES.DEFAULT;
        Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.RGB_565;
        }
        // resource bitmaps are immutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        // new antialiased Paint
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        paint.setStrokeWidth(borderScale);

        for (MemeEditorElements.EditorCaption caption : _memeEditorElements.getCaptions()) {
            String textString = caption.isAllCaps() ? caption.getText().toUpperCase() : caption.getText();

            if (TextUtils.isEmpty(textString)) {
                textString = getString(R.string.empty_caption_hint);
                paint.setTextSize((int) (scale * caption.getFontSize() * 5 / 8));
                paint.setTypeface(caption.getFont().typeFace);
                paint.setColor(caption.getBorderColor());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
            } else {
                paint.setTextSize((int) (scale * caption.getFontSize()));
                paint.setTypeface(caption.getFont().typeFace);
                paint.setColor(caption.getBorderColor());
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
            }


            // set text width to canvas width minus 16dp padding
            int textWidth = canvas.getWidth() - (int) (16 * scale);

            // init StaticLayout for text
            StaticLayout textLayout = new StaticLayout(
                    textString, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

            // get height of multiline text
            int textHeight = textLayout.getHeight();

            // get position of text in the canvas, this will depend in its internal location mode
            MemeConfig.Point where = caption.getPositionInCanvas(
                    bitmap.getWidth(), bitmap.getHeight(), textWidth, textHeight);

            // draw text to the Canvas center
            canvas.save();
            canvas.translate(where.x, where.y);
            textLayout.draw(canvas);

            // new antialiased Paint
            paint.setColor(caption.getTextColor());
            paint.setStyle(Paint.Style.FILL);

            // init StaticLayout for text
            textLayout = new StaticLayout(
                    textString, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

            // get height of multiline text
            textHeight = textLayout.getHeight();

            // draw text to the Canvas center
            textLayout.draw(canvas);
            canvas.restore();
        }

        return bitmap;
    }

    @OnTextChanged(value = R.id.create_caption, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onCaptionChanged(CharSequence text) {
        if (_isBottom) {
            _memeEditorElements.getCaptionBottom().setText(text.toString());
        } else {
            _memeEditorElements.getCaptionTop().setText(text.toString());
        }
        onMemeEditorObjectChanged();
    }

    public void onMemeEditorObjectChanged() {
        _imageEditView.setImageBitmap(null);
        if (_lastBitmap != null)
            _lastBitmap.recycle();
        Bitmap bmp = makeMemeImageFromElements(this, _memeEditorElements);
        _imageEditView.setImageBitmap(bmp);
        _lastBitmap = bmp;
    }

    // createForSaving == true will make template text elements empty
    public void recreateImage(boolean createForSaving) {
        if (createForSaving) {
            for (MemeEditorElements.EditorCaption caption : _memeEditorElements.getCaptions()) {
                if (TextUtils.isEmpty(caption.getText())) {
                    caption.setText(" ");
                }
            }
        }
        onMemeEditorObjectChanged();
    }

    @OnClick(R.id.memecreate__moar_controls__layout)
    void onBottomContainerClicked() {
        toggleMoarControls(true, false);
    }

}
