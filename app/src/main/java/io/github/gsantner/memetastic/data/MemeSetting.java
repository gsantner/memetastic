package io.github.gsantner.memetastic.data;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * A memes settings
 */
public class MemeSetting implements Serializable {
    public static interface OnMemeSettingChangedListener {
        void onMemeSettingChanged(MemeSetting memeSetting);
    }

    private OnMemeSettingChangedListener memeSettingChangedListener;
    private Bitmap displayImage;
    private int fontId;
    private MemeFont font;
    private Bitmap image;
    private int rotationDeg = 0;

    private int fontSize = MemeLibConfig.FONT_SIZES.DEFAULT;
    private int textColor = MemeLibConfig.MEME_COLORS.DEFAULT_TEXT;
    private int borderColor = MemeLibConfig.MEME_COLORS.DEFAULT_BORDER;
    private boolean allCaps = true;

    private String captionTop = "";
    private String captionBottom = "";

    private int padding = 0;
    private int paddingColor = MemeLibConfig.MEME_COLORS.WHITE;

    /**
     * Constructor
     *
     * @param font  A font
     * @param image A image
     */
    public MemeSetting(MemeFont font, Bitmap image) {
        this.image = image;
        this.font = font;
    }

    @Override
    public String toString() {
        return captionTop + "\n" + captionBottom;
    }

    /**
     * Notify listeners that ameme changed
     */
    public void notifyChangedListener() {
        if (memeSettingChangedListener != null) {
            memeSettingChangedListener.onMemeSettingChanged(this);
        }
    }

    /**
     * Get the memes font
     *
     * @return font
     */
    public MemeFont getFont() {
        return font;
    }

    /**
     * Set the memes font
     *
     * @param font font
     */
    public void setFont(MemeFont font) {
        this.font = font;
        notifyChangedListener();
    }

    /**
     * Gets the memes fontsize
     *
     * @return Font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Set the memes fontSize
     *
     * @param fontSize fontSize
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        notifyChangedListener();
    }

    /**
     * Get the texts color
     *
     * @return The texts color
     */
    public int getTextColor() {
        return textColor;
    }

    /**
     * Set text color
     *
     * @param textColor Text color
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
        notifyChangedListener();
    }

    /**
     * Get border color
     *
     * @return border color
     */
    public int getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color
     *
     * @param borderColor The border color
     */
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        notifyChangedListener();
    }

    /**
     * Sets the text if all caps
     *
     * @return All caps
     */
    public boolean isAllCaps() {
        return allCaps;
    }

    /**
     * Sets if all text caps
     *
     * @param allCaps All caps
     */
    public void setAllCaps(boolean allCaps) {
        this.allCaps = allCaps;
        notifyChangedListener();
    }

    /**
     * Gets the top text
     *
     * @return Top text
     */
    public String getCaptionTop() {
        return captionTop;
    }

    /**
     * Sets the top text
     *
     * @param captionTop top text
     */
    public void setCaptionTop(String captionTop) {
        this.captionTop = captionTop;
        notifyChangedListener();
    }

    /**
     * Gets the bottom text
     *
     * @return Bottom text
     */
    public String getCaptionBottom() {
        return captionBottom;
    }

    /**
     * Sets the bottom text
     *
     * @param captionBottom bottom text
     */
    public void setCaptionBottom(String captionBottom) {
        this.captionBottom = captionBottom;
        notifyChangedListener();
    }

    /**
     * Gets the font id
     *
     * @return Font id
     */
    public int getFontId() {
        return fontId;
    }

    /**
     * Sets the fontId
     *
     * @param fontId the font id
     */
    public void setFontId(int fontId) {
        this.fontId = fontId;
        notifyChangedListener();
    }

    /**
     * Gets the image to be displayed
     *
     * @return the image
     */
    public Bitmap getDisplayImage() {
        return displayImage;
    }

    /**
     * Sets the image to be displayed
     *
     * @param displayImage the image
     */
    public void setDisplayImage(Bitmap displayImage) {
        this.displayImage = displayImage;
    }

    /**
     * Gets the change listener
     *
     * @return THe listener
     */
    public OnMemeSettingChangedListener getMemeSettingChangedListener() {
        return memeSettingChangedListener;
    }

    /**
     * Sets the change listener
     *
     * @param memeSettingChangedListener The listener
     */
    public void setMemeSettingChangedListener(OnMemeSettingChangedListener memeSettingChangedListener) {
        this.memeSettingChangedListener = memeSettingChangedListener;
    }

    /**
     * Gets the image
     *
     * @return the image
     */
    public Bitmap getImage() {
        return image;
    }

    /**
     * Sets the image
     *
     * @param image image
     */
    public void setImage(Bitmap image) {
        this.image = image;
        notifyChangedListener();
    }

    public int getRotationDeg() {
        return rotationDeg;
    }

    public void setRotationDeg(int rotationDeg) {
        this.rotationDeg = rotationDeg;
        notifyChangedListener();
    }

    public int getPadding() {
        return padding;
    }

    /**
     * Set the padding of the picture. A value of 10 means the picture is grown by 10
     * percent (size * 1.1) and the background filled with paddingColor
     * @param padding padding
     */
    public void setPadding(int padding) {
        this.padding = padding;
        notifyChangedListener();
    }

    public int getPaddingColor() {
        return paddingColor;
    }

    /**
     * Set the color to be used for padding background. See padding
     * @param paddingColor the color
     */
    public void setPaddingColor(int paddingColor) {
        this.paddingColor = paddingColor;
        notifyChangedListener();
    }
}
