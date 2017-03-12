package io.github.gsantner.memetastic.data;

/**
 * A memes settings
 *
 * @param <F> FontType
 * @param <I> ImageType
 */
public class MemeSetting<F, I> {
    public static interface OnMemeSettingChangedListener<F, I> {
        void onMemeSettingChanged(MemeSetting<F, I> memeSetting);
    }

    private OnMemeSettingChangedListener<F, I> memeSettingChangedListener;
    private I displayImage;
    private int fontId;
    private MemeFont<F> font;
    private I image;

    private int fontSize;
    private int textColor;
    private int borderColor;
    private boolean allCaps;

    private String captionTop;
    private String captionBottom;

    /**
     * Constructor
     *
     * @param font  A font
     * @param image A image
     */
    public MemeSetting(MemeFont<F> font, I image) {
        this.image = image;
        this.font = font;

        fontSize = MemeLibConfig.FONT_SIZES.DEFAULT;
        textColor = MemeLibConfig.MEME_COLORS.DEFAULT_TEXT;
        borderColor = MemeLibConfig.MEME_COLORS.DEFAULT_BORDER;
        captionBottom = "";
        captionTop = "";
        allCaps = true;
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
    public MemeFont<F> getFont() {
        return font;
    }

    /**
     * Set the memes font
     *
     * @param font font
     */
    public void setFont(MemeFont<F> font) {
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
    public I getDisplayImage() {
        return displayImage;
    }

    /**
     * Sets the image to be displayed
     *
     * @param displayImage the image
     */
    public void setDisplayImage(I displayImage) {
        this.displayImage = displayImage;
    }

    /**
     * Gets the change listener
     *
     * @return THe listener
     */
    public OnMemeSettingChangedListener<F, I> getMemeSettingChangedListener() {
        return memeSettingChangedListener;
    }

    /**
     * Sets the change listener
     *
     * @param memeSettingChangedListener The listener
     */
    public void setMemeSettingChangedListener(OnMemeSettingChangedListener<F, I> memeSettingChangedListener) {
        this.memeSettingChangedListener = memeSettingChangedListener;
    }

    /**
     * Gets the image
     *
     * @return the image
     */
    public I getImage() {
        return image;
    }

    /**
     * Sets the image
     *
     * @param image image
     */
    public void setImage(I image) {
        this.image = image;
        notifyChangedListener();
    }
}
