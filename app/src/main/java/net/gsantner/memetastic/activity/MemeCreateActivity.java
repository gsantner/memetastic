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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import net.gsantner.memetastic.App;
import net.gsantner.memetastic.data.MemeConfig;
import net.gsantner.memetastic.data.MemeData;
import net.gsantner.memetastic.data.MemeEditorElements;
import net.gsantner.memetastic.data.MemeLibConfig;
import net.gsantner.memetastic.service.AssetUpdater;
import net.gsantner.memetastic.ui.FontItemAdapter;
import net.gsantner.memetastic.util.ActivityUtils;
import net.gsantner.memetastic.util.AppCast;
import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.memetastic.util.ContextUtils;
import net.gsantner.memetastic.util.PermissionChecker;
import net.gsantner.opoc.ui.TouchImageView;
import net.gsantner.opoc.util.ShareUtil;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import io.github.gsantner.memetastic.R;
import other.so.AndroidBug5497Workaround;

/**
 * Activity for creating memes
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
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

    @BindView(R.id.memecreate__activity__fullscreen_image)
    TouchImageView _fullscreenImageView;

    //#####################
    //## Members
    //#####################
    private static boolean _doubleBackToExitPressedOnce = false;
    private Bitmap _lastBitmap = null;
    private long _memeSavetime = -1;
    private File _predefinedTargetFile = null;
    private App _app;
    private MemeEditorElements _memeEditorElements;
    private Bundle _savedInstanceState = null;
    boolean _bottomContainerVisible = false;
    private boolean _isBottom;
    private View _dialogView;
    private boolean _savedAsMemeTemplate = false;

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
                (!getIntent().hasExtra(EXTRA_IMAGE_PATH)) && !(Intent.ACTION_EDIT.equals(action) && type.startsWith("image/"))) {
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
            if (!initMemeSettings(savedInstanceState)) {
                return;
            }
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
        try {
            if (!ActivityUtils.get(this).isInSplitScreenMode()) {
                _imageEditView.postDelayed(this::touchTopElement, 40);
            }
        } catch (Exception ignored) {
        }
    }

    private void initCaptionButtons() {
        final ImageButton buttonTextSettings = findViewById(R.id.settings_caption);
        final ImageButton buttonOk = findViewById(R.id.done_caption);
        buttonTextSettings.setColorFilter(R.color.black);
        buttonOk.setColorFilter(R.color.black);
    }


    public boolean initMemeSettings(Bundle savedInstanceState) {
        MemeData.Font lastUsedFont = getFont(_app.settings.getLastUsedFont());
        Bitmap bitmap = extractBitmapFromIntent(getIntent());
        if (bitmap == null) {
            finish();
            return false;
        }
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
        return true;
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
            if (!initMemeSettings(_savedInstanceState)) {
                return;
            }
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
        } else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_EDIT) && intent.getType().startsWith("image/")) {
            ShareUtil shu = new ShareUtil(this);
            _predefinedTargetFile = shu.extractFileFromIntent(intent);
            if (_predefinedTargetFile == null) {
                Toast.makeText(this, R.string.the_file_could_not_be_loaded, Toast.LENGTH_SHORT).show();
                finish();
            }
            bitmap = ContextUtils.get().loadImageFromFilesystem(_predefinedTargetFile, _app.settings.getRenderQualityReal());
        } else {
            String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
            bitmap = ContextUtils.get().loadImageFromFilesystem(new File(imagePath), _app.settings.getRenderQualityReal());
        }
        return bitmap;
    }

    // Text settings dialog
    @OnClick(R.id.settings_caption)
    public void openSettingsDialog() {
        ActivityUtils.get(this).hideSoftKeyboard();
        _dialogView = View.inflate(this, R.layout.ui__memecreate__text_settings, null);

        initTextSettingsPopupDialog(_dialogView);

        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.settings)
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
                .setOnDismissListener((di) -> {
                    _toolbar.setVisibility(View.VISIBLE);
                    _imageEditView.setPadding(0, 0, 0, 0);

                })
                .create();

        // Get some more space
        try {
            _toolbar.setVisibility(View.GONE);
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.TOP;
            android.graphics.Point p = new android.graphics.Point();
            getWindowManager().getDefaultDisplay().getSize(p);
            _imageEditView.setPadding(0, p.y / 2, 0, 0);
        } catch (Exception ignored) {

        }
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.creatememe__menu, menu);
        ContextUtils cu = new ContextUtils(getApplicationContext());
        cu.tintMenuItems(menu, true, Color.WHITE);
        cu.setSubMenuIconsVisiblity(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            case R.id.action_save_as_template: {
                if (!_savedAsMemeTemplate) {
                    File folder = AssetUpdater.getCustomAssetsDir(AppSettings.get());
                    String filename = String.format(Locale.getDefault(), "%s_%s.jpg", getString(R.string.app_name), AssetUpdater.FORMAT_MINUTE_FILE.format(new Date(_memeSavetime)));
                    File fullpath = new File(folder, filename);
                    folder.mkdirs();
                    _savedAsMemeTemplate = ContextUtils.get().writeImageToFile(fullpath, _memeEditorElements.getImageMain().getDisplayImage());
                }
                return true;
            }
            case R.id.action_appearance: {
                toggleMoarControls(false, false);
                ActivityUtils.get(this).hideSoftKeyboard();
                View focusedView = this.getCurrentFocus();
                if (focusedView != null) {
                    ActivityUtils.get(this).hideSoftKeyboard();
                }
                return true;
            }
            case R.id.action_show_original_image: {
                _fullscreenImageView.setImageBitmap(_memeEditorElements.getImageMain().getDisplayImage());
                _fullscreenImageView.setVisibility(View.VISIBLE);
                toggleMoarControls(true, true);
                return true;
            }
            case R.id.action_show_edited_image: {
                recreateImage(true);
                _fullscreenImageView.setImageBitmap(_lastBitmap);
                _fullscreenImageView.setVisibility(View.VISIBLE);
                toggleMoarControls(true, true);
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
        File fullpath = _predefinedTargetFile != null ? _predefinedTargetFile : new File(folder, filename);
        boolean wasSaved = ContextUtils.get().writeImageToFile(fullpath, _lastBitmap.copy(_lastBitmap.getConfig(), false));
        if (wasSaved && showDialog) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.successfully_saved)
                    .setMessage(R.string.saved_meme_successfully__appspecific)
                    .setNegativeButton(R.string.keep_editing, null)
                    .setNeutralButton(R.string.share_meme__appspecific, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            _app.shareBitmapToOtherApp(_lastBitmap, MemeCreateActivity.this);
                        }
                    })
                    .setPositiveButton(R.string.close, (dialog1, which) -> finish());
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

    private void initTextSettingsPopupDialog(View view) {
        SeekBar textSize = view.findViewById(R.id.meme_dialog__seek_font_size);
        View textBackGroundColor = view.findViewById(R.id.meme_dialog__color_picker_for_text);
        View textBorderColor = view.findViewById(R.id.meme_dialog__color_picker_for_border);
        Switch allCapsSwitch = view.findViewById(R.id.meme_dialog__toggle_all_caps);
        Spinner fontDropDown = view.findViewById(R.id.meme_dialog__dropdown_font);

        FontItemAdapter fontAdapter = new FontItemAdapter(this,
                android.R.layout.simple_list_item_1, MemeData.getFonts(),
                false, getString(R.string.font));
        fontDropDown.setAdapter(fontAdapter);
        fontAdapter.setSelectedFont(fontDropDown, _memeEditorElements.getCaptionTop().getFont());

        textBackGroundColor.setBackgroundColor(_memeEditorElements.getCaptionTop().getTextColor());
        textBorderColor.setBackgroundColor(_memeEditorElements.getCaptionTop().getBorderColor());

        allCapsSwitch.setChecked(_memeEditorElements.getCaptionTop().isAllCaps());
        textSize.setProgress(_memeEditorElements.getCaptionTop().getFontSize() - MemeLibConfig.FONT_SIZES.MIN);


        //listeners

        View.OnClickListener colorListeners = view1 -> {
            switch (view1.getId()) {
                case R.id.meme_dialog__color_picker_for_text:
                    if (_isBottom) {
                        showColorDialog(view1.getId(), _memeEditorElements.getCaptionBottom().getTextColor());
                    } else {
                        showColorDialog(view1.getId(), _memeEditorElements.getCaptionTop().getTextColor());
                    }
                    break;
                case R.id.meme_dialog__color_picker_for_border:
                    if (_isBottom) {
                        showColorDialog(view1.getId(), _memeEditorElements.getCaptionBottom().getBorderColor());
                    } else {
                        showColorDialog(view1.getId(), _memeEditorElements.getCaptionTop().getBorderColor());
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
                }
                if (!_isBottom || _memeEditorElements.getImageMain().isTextSettingsGlobal()) {
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
                }

                if (!_isBottom || _memeEditorElements.getImageMain().isTextSettingsGlobal()) {
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
        allCapsSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (_isBottom) {
                _memeEditorElements.getCaptionBottom().setAllCaps(isChecked);
            }
            if (!_isBottom || _memeEditorElements.getImageMain().isTextSettingsGlobal()) {
                _memeEditorElements.getCaptionTop().setAllCaps(isChecked);
            }
            onMemeEditorObjectChanged();
        });
    }

    private void initMoarControlsContainer() {
        final Button rotateButton = ButterKnife.findById(this, R.id.memecreate__moar_controls__rotate_plus_90deg);
        final SeekBar seekPaddingSize = ButterKnife.findById(this, R.id.memecreate__moar_controls__seek_padding_size);
        final ColorPanelView colorPickerPadding = ButterKnife.findById(this, R.id.memecreate__moar_controls__color_picker_for_padding);
        final CheckBox globalTextSettingsCheckbox = findViewById(R.id.memecreate__moar_controls__global_text_settings);

        // Apply existing settings
        _paddingColor.setColor(_memeEditorElements.getImageMain().getPaddingColor());
        seekPaddingSize.setProgress(_memeEditorElements.getImageMain().getPadding());
        globalTextSettingsCheckbox.setChecked(_memeEditorElements.getImageMain().isTextSettingsGlobal());


        //
        //  Add bottom sheet listeners
        //
        View.OnClickListener colorListener = v -> {
            showColorDialog(R.id.memecreate__moar_controls__color_picker_for_padding, _memeEditorElements.getImageMain().getPaddingColor());
            onMemeEditorObjectChanged();
        };
        globalTextSettingsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> _memeEditorElements.getImageMain().setTextSettingsGlobal(isChecked));
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
        rotateButton.setOnClickListener(v -> {
            _memeEditorElements.getImageMain().setRotationDeg((_memeEditorElements.getImageMain().getRotationDeg() + 90) % 360);
            onMemeEditorObjectChanged();
        });
    }

    private void showColorDialog(int id, @ColorInt int color) {
        ColorPickerDialog.newBuilder()
                .setDialogId(id)
                .setColor(color)
                .setPresets(MemeLibConfig.MEME_COLORS.ALL)
                .setCustomButtonText(R.string.palette)
                .setPresetsButtonText(R.string.presets)
                .setDialogTitle(R.string.select_color)
                .setSelectedButtonText(android.R.string.ok)
                .show(this);
    }

    @OnClick(R.id.memecreate__activity__fullscreen_image)
    public void onFullScreenImageClicked() {
        _fullscreenImageView.setVisibility(View.INVISIBLE);
        recreateImage(false);
        toggleMoarControls(true, false);
    }

    @OnLongClick(R.id.memecreate__activity__fullscreen_image)
    public boolean onFullScreenImageLongClicked() {
        _fullscreenImageView.setRotation((_fullscreenImageView.getRotation() + 90) % 360);
        return true;
    }


    @Override
    public void onColorSelected(int id, @ColorInt int colorInt) {
        switch (id) {
            case R.id.meme_dialog__color_picker_for_border: {// border color
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setBorderColor(colorInt);
                }
                if (!_isBottom || _memeEditorElements.getImageMain().isTextSettingsGlobal()) {
                    _memeEditorElements.getCaptionTop().setBorderColor(colorInt);
                }
                View view = _dialogView.findViewById(R.id.meme_dialog__color_picker_for_border);
                view.setBackgroundColor(colorInt);
                break;
            }
            case R.id.meme_dialog__color_picker_for_text: {// text background color
                if (_isBottom) {
                    _memeEditorElements.getCaptionBottom().setTextColor(colorInt);
                }
                if (!_isBottom || _memeEditorElements.getImageMain().isTextSettingsGlobal()) {
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

    public Bitmap renderMemeImageFromElements(Context c, MemeEditorElements memeEditorElements) {
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
                textString = getString(R.string.tap_here_to_add_caption);
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
        if (_lastBitmap != null) {
            _lastBitmap.recycle();
        }
        Bitmap bmp = renderMemeImageFromElements(this, _memeEditorElements);
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

    //////////////////////////////////////////////////////////////
    ////
    ///  Visibility etc
    //
    @OnClick(R.id.done_caption)
    public void settingsDone() {
        _editBar.setVisibility(View.GONE);
        ActivityUtils.get(this).hideSoftKeyboard();
        onMemeEditorObjectChanged();
    }

    @OnClick(R.id.memecreate__moar_controls__layout)
    void onBottomContainerClicked() {
        toggleMoarControls(true, false);
    }

    @Override
    public void onBackPressed() {
        boolean hasTextInput = !_create_caption.getText().toString().isEmpty() ||
                !_memeEditorElements.getCaptionBottom().getText().isEmpty() ||
                !_memeEditorElements.getCaptionTop().getText().isEmpty();

        if (_fullscreenImageView.getVisibility() == View.VISIBLE) {
            _fullscreenImageView.setVisibility(View.INVISIBLE);
            toggleMoarControls(true, false);
            return;
        }

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
        Snackbar.make(findViewById(android.R.id.content), R.string.press_back_again_to_stop_editing__appspecific, Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                _doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void touchTopElement() {
        onImageTouched(_imageEditView, MotionEvent.obtain(1, 1, MotionEvent.ACTION_DOWN, 0, 0, 0));
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
}
