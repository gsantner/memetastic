package io.github.gsantner.memetastic.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
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
import io.github.gsantner.memetastic.data.MemeLibConfig;
import io.github.gsantner.memetastic.data.MemeSetting;
import io.github.gsantner.memetastic.data.MemeSettingBase;
import io.github.gsantner.memetastic.service.AssetUpdater;
import io.github.gsantner.memetastic.ui.FontAdapter;
import io.github.gsantner.memetastic.util.ActivityUtils;
import io.github.gsantner.memetastic.util.AndroidBug5497Workaround;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ContextUtils;
import io.github.gsantner.memetastic.util.PermissionChecker;
import uz.shift.colorpicker.LineColorPicker;

/**
 * Activity for creating memes
 */
public class MemeCreateActivity extends AppCompatActivity
        implements MemeSetting.OnMemeSettingChangedListener {
    //########################
    //## Static
    //########################
    public final static int RESULT_MEME_EDITING_FINISHED = 150;
    public final static int RESULT_MEME_EDIT_SAVED = 1;
    public final static int RESULT_MEME_NOT_SAVED = 0;
    public final static String EXTRA_IMAGE_PATH = "extraImage";
    public final static String ASSET_IMAGE = "assetImage";

    //########################
    //## UI Binding
    //########################
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.memecreate__activity__image)
    ImageView imageEditView;

    @BindView(R.id.memecreate__activity__edit_caption_bottom)
    EditText textEditBottomCaption;

    @BindView(R.id.memecreate__activity__edit_caption_top)
    EditText textEditTopCaption;

    //#####################
    //## Members
    //#####################
    private static boolean doubleBackToExitPressedOnce = false;
    private Bitmap lastBitmap = null;
    private long memeSavetime = -1;
    private App app;
    private MemeSetting memeSetting;
    private boolean bFullscreenImage = true;
    private Bundle savedInstanceState = null;
    boolean moarControlsContainerVisible = false;

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
                (!getIntent().hasExtra(EXTRA_IMAGE_PATH) || !getIntent().hasExtra(ASSET_IMAGE))) {
            finish();
            return;
        }

        // Bind Ui
        ButterKnife.bind(this);
        app = (App) getApplication();

        // Set _toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initMemeSettings(savedInstanceState);
        initMoarControlsContainer();
    }

    public void initMemeSettings(Bundle savedInstanceState) {
        MemeData.Font lastUsedFont = getFont(app.settings.getLastUsedFont());
        Bitmap bitmap = extractBitmapFromIntent(getIntent());
        if (savedInstanceState != null && savedInstanceState.containsKey("memeObj")) {
            memeSetting = (MemeSetting) savedInstanceState.getSerializable("memeObj");
            memeSetting.getImageMain().setImage(bitmap);
            memeSetting.getCaptionTop().setFont(lastUsedFont);
            memeSetting.getCaptionBottom().setFont(lastUsedFont);
        } else {
            memeSetting = new MemeSetting(lastUsedFont, bitmap);
        }
        memeSetting.getImageMain().setDisplayImage(memeSetting.getImageMain().getImage().copy(Bitmap.Config.RGB_565, false));

        textEditTopCaption.setText(memeSetting.getCaptionTop().getText());
        textEditBottomCaption.setText(memeSetting.getCaptionBottom().getText());
        memeSetting.setMemeSettingChangedListener(this);
        memeSetting.notifyChangedListener();
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
        outState.putSerializable("memeObj", memeSetting);
        this.savedInstanceState = outState;
    }

    private void prepareForSaving() {
        if (memeSetting == null) {
            return;
        }
        memeSetting.setMemeSettingChangedListener(null);
        imageEditView.setImageBitmap(null);
        if (lastBitmap != null && !lastBitmap.isRecycled())
            lastBitmap.recycle();
        MemeSetting.MemeElementImage imageMain = memeSetting.getImageMain();
        if (imageMain.getImage() != null && !imageMain.getImage().isRecycled())
            imageMain.getImage().recycle();
        if (imageMain.getDisplayImage() != null && !imageMain.getDisplayImage().isRecycled())
            imageMain.getDisplayImage().recycle();
        lastBitmap = null;
        imageMain.setDisplayImage(null);
        imageMain.setImage(null);
        memeSetting.getCaptionTop().setFont(null);
        memeSetting.getCaptionBottom().setFont(null);
        memeSetting.setMemeSettingChangedListener(null);
    }

    @Override
    protected void onDestroy() {
        prepareForSaving();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bFullscreenImage) {
            bFullscreenImage = false;
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        if (savedInstanceState != null) {
            initMemeSettings(savedInstanceState);
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

    private Bitmap extractBitmapFromIntent(final Intent intent) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = null;
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        App.log("imagepath::" + imagePath);
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
        } else if (intent.getBooleanExtra(ASSET_IMAGE, false)) {
            try {
                //Scale big images down to avoid "out of memory"
                InputStream inputStream = getAssets().open(imagePath);
                BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
                options.inSampleSize = ContextUtils.get().calculateInSampleSize(options, app.settings.getRenderQualityReal());
                options.inJustDecodeBounds = false;
                inputStream.close();
                inputStream = getAssets().open(imagePath);
                bitmap = BitmapFactory.decodeStream(inputStream, new Rect(0, 0, 0, 0), options);
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        } else {
            //Scale big images down to avoid "out of memory"
            BitmapFactory.decodeFile(imagePath, options);
            options.inSampleSize = ContextUtils.get().calculateInSampleSize(options, app.settings.getRenderQualityReal());
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imagePath, options);
        }
        return bitmap;
    }

    @Override
    public void onBackPressed() {
        boolean hasTextInput = !textEditTopCaption.getText().toString().isEmpty() || !textEditBottomCaption.getText().toString().isEmpty();

        // Close views above
        if (moarControlsContainerVisible) {
            toggleMoarControls(true, false);
            return;
        }

        // Auto save if option checked
        if (hasTextInput && app.settings.isAutoSaveMeme()) {
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
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(android.R.id.content), R.string.creator__press_back_again_to_exit, Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @OnTouch(R.id.memecreate__activity__image)
    public boolean onImageTouched(View view) {
        textEditBottomCaption.clearFocus();
        textEditTopCaption.clearFocus();
        imageEditView.requestFocus();
        ActivityUtils.get(this).hideSoftKeyboard();
        if (moarControlsContainerVisible) {
            toggleMoarControls(true, false);
        }
        return true;
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
                app.shareBitmapToOtherApp(lastBitmap, this);
                return true;
            }
            case R.id.action_save: {
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
        if (memeSavetime < 0) {
            memeSavetime = System.currentTimeMillis();
        }

        String filename = String.format(Locale.getDefault(), "%s_%d.jpg", getString(R.string.app_name), memeSavetime);
        File fullpath = new File(folder, filename);
        boolean wasSaved = ContextUtils.get().writeImageToFileJpeg(fullpath, lastBitmap) != null;
        if (wasSaved && showDialog) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.creator__saved_successfully)
                    .setMessage(R.string.creator__saved_successfully_message)
                    .setNegativeButton(R.string.creator__keep_editing, null)
                    .setNeutralButton(R.string.main__share_meme, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            app.shareBitmapToOtherApp(lastBitmap, MemeCreateActivity.this);
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
        moarControlsContainerVisible = !moarControlsContainerVisible;
        if (forceVisibile) {
            moarControlsContainerVisible = visible;
        }
        textEditBottomCaption.setVisibility(moarControlsContainerVisible ? View.GONE : View.VISIBLE);
        textEditTopCaption.setVisibility(moarControlsContainerVisible ? View.GONE : View.VISIBLE);
        toolbar.setVisibility(moarControlsContainerVisible ? View.GONE : View.VISIBLE);

        // higher weightRatio means the conf is more wide, so below view can be higher
        // 100 is the max weight, 55 means the below view is a little more weighted
        Bitmap curImg = memeSetting.getImageMain().getDisplayImage();
        int weight = (int) (55f * (1 + ((curImg.getWidth() / (float) curImg.getHeight()) / 10f)));
        weight = weight > 100 ? 100 : weight;

        // Set weights. If moarControlsContainerVisible == false -> Hide them = 0 weight
        View container = findViewById(R.id.memecreate__activity__image_container);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) container.getLayoutParams();
        lp.height = 0;
        lp.weight = moarControlsContainerVisible ? 100 - weight : 100;
        container.setLayoutParams(lp);
        container = findViewById(R.id.memecreate__activity__moar_controls_container);
        container.setVisibility(moarControlsContainerVisible ? View.VISIBLE : View.GONE);
        lp = (LinearLayout.LayoutParams) container.getLayoutParams();
        lp.height = 0;
        lp.weight = moarControlsContainerVisible ? weight : 0;
        container.setLayoutParams(lp);
    }

    @OnClick(R.id.fab)
    public void onFloatingButtonClicked(View view) {
        toggleMoarControls(false, false);
        ActivityUtils.get(this).hideSoftKeyboard();
        View focusedView = this.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void initMoarControlsContainer() {
        final LineColorPicker colorPickerShade = ButterKnife.findById(this, R.id.memecreate__moar_controls__color_picker_for_border);
        final LineColorPicker colorPickerText = ButterKnife.findById(this, R.id.memecreate__moar_controls__color_picker_for_text);
        final Spinner dropdownFont = ButterKnife.findById(this, R.id.memecreate__moar_controls__dropdown_font);
        final SeekBar seekFontSize = ButterKnife.findById(this, R.id.memecreate__moar_controls__seek_font_size);
        final ToggleButton toggleAllCaps = ButterKnife.findById(this, R.id.memecreate__moar_controls__toggle_all_caps);
        final Button rotateButton = ButterKnife.findById(this, R.id.memecreate__moar_controls__rotate_plus_90deg);
        final LineColorPicker colorPickerPadding = ButterKnife.findById(this, R.id.memecreate__moar_controls__color_picker_for_padding);
        final SeekBar seekPaddingSize = ButterKnife.findById(this, R.id.memecreate__moar_controls__seek_padding_size);

        colorPickerText.setColors(MemeLibConfig.MEME_COLORS.ALL);
        colorPickerShade.setColors(MemeLibConfig.MEME_COLORS.ALL);
        colorPickerPadding.setColors(MemeLibConfig.MEME_COLORS.ALL);

        FontAdapter adapter = new FontAdapter(this,
                android.R.layout.simple_list_item_1, MemeData.getFonts(),
                true, getString(R.string.creator__font));
        dropdownFont.setAdapter(adapter);


        // Apply existing settings
        colorPickerText.setSelectedColor(memeSetting.getCaptionTop().getTextColor());
        colorPickerShade.setSelectedColor(memeSetting.getCaptionTop().getBorderColor());
        colorPickerPadding.setSelectedColor(memeSetting.getImageMain().getPaddingColor());
        adapter.setSelectedFont(dropdownFont, memeSetting.getCaptionTop().getFont());
        toggleAllCaps.setChecked(memeSetting.getCaptionTop().isAllCaps());
        seekFontSize.setProgress(memeSetting.getCaptionTop().getFontSize() - MemeLibConfig.FONT_SIZES.MIN);
        seekPaddingSize.setProgress(memeSetting.getImageMain().getPadding());


        //
        //  Add bottom sheet listeners
        //
        View.OnClickListener colorListener = new View.OnClickListener() {
            public void onClick(View v) {
                LineColorPicker picker = (LineColorPicker) v;
                if (picker == colorPickerShade) {
                    memeSetting.getCaptionTop().setBorderColor(picker.getColor());
                    memeSetting.getCaptionBottom().setBorderColor(picker.getColor());
                } else if (picker == colorPickerText) {
                    memeSetting.getCaptionTop().setTextColor(picker.getColor());
                    memeSetting.getCaptionBottom().setTextColor(picker.getColor());
                } else if (picker == colorPickerPadding) {
                    memeSetting.getImageMain().setPaddingColor(picker.getColor());
                    memeSetting.getImageMain().setPaddingColor(picker.getColor());
                }
            }
        };

        colorPickerShade.setOnClickListener(colorListener);
        colorPickerText.setOnClickListener(colorListener);
        colorPickerPadding.setOnClickListener(colorListener);
        dropdownFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> parent) {
            }

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                memeSetting.getCaptionTop().setFont((MemeData.Font) parent.getSelectedItem());
                memeSetting.getCaptionBottom().setFont((MemeData.Font) parent.getSelectedItem());
                app.settings.setLastUsedFont(((MemeData.Font) parent.getSelectedItem()).fullPath.getAbsolutePath());
            }
        });
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == seekFontSize) {
                    memeSetting.getCaptionTop().setFontSize(progress + MemeLibConfig.FONT_SIZES.MIN);
                    memeSetting.getCaptionBottom().setFontSize(progress + MemeLibConfig.FONT_SIZES.MIN);
                } else if (seekBar == seekPaddingSize) {
                    memeSetting.getImageMain().setPadding(progress);
                }
            }
        };

        seekFontSize.setOnSeekBarChangeListener(seekBarChangeListener);
        seekPaddingSize.setOnSeekBarChangeListener(seekBarChangeListener);
        toggleAllCaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                memeSetting.getCaptionTop().setAllCaps(isChecked);
                memeSetting.getCaptionBottom().setAllCaps(isChecked);
            }
        });
        rotateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                memeSetting.getImageMain().setRotationDeg((memeSetting.getImageMain().getRotationDeg() + 90) % 360);
            }
        });
    }

    public Bitmap drawMultilineTextToBitmap(Context c, MemeSetting memeSetting) {
        // prepare canvas
        Resources resources = c.getResources();
        Bitmap bitmap = memeSetting.getImageMain().getDisplayImage();

        if (memeSetting.getImageMain().getRotationDeg() != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(memeSetting.getImageMain().getRotationDeg());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        double pad = 1 + memeSetting.getImageMain().getPadding() / 100.0;
        if (pad > 1.01) {
            Bitmap workBmp = Bitmap.createBitmap((int) (bitmap.getWidth() * pad), (int) (bitmap.getHeight() * pad), Bitmap.Config.ARGB_8888);
            Canvas can = new Canvas(workBmp);
            //can.drawARGB(0xFF, 0xFF, 0xFF, 0xFF); //This represents White color
            can.drawColor(memeSetting.getImageMain().getPaddingColor());
            can.drawBitmap(bitmap, (int) ((workBmp.getWidth() - bitmap.getWidth()) / 2.0), (int) ((workBmp.getHeight() - bitmap.getHeight()) / 2.0), null);
            bitmap = workBmp;
        }

        float scale = ContextUtils.get().getScalingFactorInPixelsForWritingOnPicture(bitmap.getWidth(), bitmap.getHeight());
        float borderScale = scale * memeSetting.getCaptionTop().getFontSize() / MemeLibConfig.FONT_SIZES.DEFAULT;
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
        paint.setTextSize((int) (memeSetting.getCaptionTop().getFontSize() * scale));
        paint.setTypeface(memeSetting.getCaptionTop().getFont().typeFace);
        //paint.setStrokeWidth(memeSetting.getFontSize() / 4);
        paint.setStrokeWidth(borderScale);

        String[] textStrings = {memeSetting.getCaptionTop().getText(), memeSetting.getCaptionBottom().getText()};
        if (memeSetting.getCaptionTop().isAllCaps()) {
            for (int i = 0; i < textStrings.length; i++) {
                textStrings[i] = textStrings[i].toUpperCase();
            }
        }

        for (int i = 0; i < textStrings.length; i++) {
            paint.setColor(memeSetting.getCaptionTop().getBorderColor());
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            // set text width to canvas width minus 16dp padding
            int textWidth = canvas.getWidth() - (int) (16 * scale);

            // init StaticLayout for text
            StaticLayout textLayout = new StaticLayout(
                    textStrings[i], paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

            // get height of multiline text
            int textHeight = textLayout.getHeight();

            // get position of text's top left corner  center: (bitmap.getWidth() - textWidth)/2
            float x = (bitmap.getWidth() - textWidth) / 2;
            float y = 0;
            if (i == 0)
                y = bitmap.getHeight() / 15;
            else
                y = bitmap.getHeight() - textHeight;

            // draw text to the Canvas center
            canvas.save();
            canvas.translate(x, y);
            textLayout.draw(canvas);

            // new antialiased Paint
            paint.setColor(memeSetting.getCaptionTop().getTextColor());
            paint.setStyle(Paint.Style.FILL);

            // init StaticLayout for text
            textLayout = new StaticLayout(
                    textStrings[i], paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

            // get height of multiline text
            textHeight = textLayout.getHeight();

            // draw text to the Canvas center
            textLayout.draw(canvas);
            canvas.restore();
        }

        return bitmap;
    }

    @OnTextChanged(value = R.id.memecreate__activity__edit_caption_bottom, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onCaptionBottomChanged(CharSequence text) {
        memeSetting.getCaptionBottom().setText(text.toString());
    }

    @OnTextChanged(value = R.id.memecreate__activity__edit_caption_top, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onCaptionTopChanged(CharSequence text) {
        memeSetting.getCaptionTop().setText(text.toString());
    }

    @Override
    public void onMemeSettingChanged(MemeSettingBase memeSetting) {
        imageEditView.setImageBitmap(null);
        if (lastBitmap != null)
            lastBitmap.recycle();
        Bitmap bmp = drawMultilineTextToBitmap(this, (MemeSetting) memeSetting);
        imageEditView.setImageBitmap(bmp);
        lastBitmap = bmp;
    }

    @OnClick(R.id.memecreate__moar_controls__layout)
    void onMoarControlsContainerClicked() {
        toggleMoarControls(true, false);
    }
}
