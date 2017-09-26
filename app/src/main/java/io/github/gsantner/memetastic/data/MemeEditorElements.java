package io.github.gsantner.memetastic.data;

import android.graphics.Bitmap;

import java.io.Serializable;

import static io.github.gsantner.memetastic.data.MemeConfig.Point;

/**
 * A memes settings
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MemeEditorElements extends MemeEditorElementBase implements MemeEditorElementBase.OnMemeEditorObjectChangedListener, Serializable {
    private ElementText _captionTop;
    private ElementText _captionBottom;
    private ElementImage _imageMain;

    public MemeEditorElements(MemeData.Font font, Bitmap image) {
        _captionTop = new ElementText(font, MemeConfig.ImageText.TYPE_TOP);
        _captionBottom = new ElementText(font, MemeConfig.ImageText.TYPE_BOTTOM);
        _imageMain = new ElementImage(image);

        _captionTop.setChangedListener(this);
        _captionBottom.setChangedListener(this);
        _imageMain.setChangedListener(this);
    }

    @Override
    public void onMemeEditorObjectChanged(MemeEditorElementBase memeEditorObject) {
        notifyChangedListener();
    }

    @Override
    public String toString() {
        return _captionTop.toString() + "\n" + _captionBottom.toString();
    }

    public ElementText getCaptionTop() {
        return _captionTop;
    }

    public void setCaptionTop(ElementText captionTop) {
        _captionTop = captionTop;
        notifyChangedListener();
    }

    public ElementText getCaptionBottom() {
        return _captionBottom;
    }

    public void setCaptionBottom(ElementText captionBottom) {
        _captionBottom = captionBottom;
        notifyChangedListener();
    }

    public ElementImage getImageMain() {
        return _imageMain;
    }

    public void setImageMain(ElementImage imageMain) {
        _imageMain = imageMain;
        notifyChangedListener();
    }

    public static class ElementText extends MemeEditorElementBase {
        private MemeConfig.ImageText _imgText;
        private MemeData.Font _font = null; // !serializable

        private int _fontSize = MemeLibConfig.FONT_SIZES.DEFAULT;
        private int _textColor = MemeLibConfig.MEME_COLORS.DEFAULT_TEXT;
        private int _borderColor = MemeLibConfig.MEME_COLORS.DEFAULT_BORDER;
        private boolean _allCaps = true;

        public ElementText(MemeData.Font font, int positionType) {
            _font = font;
            _imgText = new MemeConfig.ImageText();
            notifyChangedListener();
        }

        public ElementText(MemeData.Font font, MemeConfig.ImageText imgText) {
            _font = font;
            _imgText = imgText;
            notifyChangedListener();
        }

        public MemeData.Font getFont() {
            return _font;
        }

        public void setFont(MemeData.Font font) {
            _font = font;
            notifyChangedListener();
        }

        public int getFontSize() {
            return _fontSize;
        }

        public void setFontSize(int fontSize) {
            _fontSize = fontSize;
            notifyChangedListener();
        }

        public Point getPositionInCanvas(float width, float height, float textWidth, float textHeight) {
            switch (_imgText.getPositionType()) {
                case MemeConfig.ImageText.TYPE_CUSTOM:
                default: {
                    Point point = _imgText.getPosition();
                    if (point == null) {
                        point = new Point(0.5f, 0.5f);
                    }
                    return new Point(
                            width * point.getX() - textWidth * 0.5f,
                            height * point.getY() - textHeight * 0.5f);
                }

                case MemeConfig.ImageText.TYPE_TOP:
                    return new Point((width - textWidth) * 0.5f, height / 15f);

                case MemeConfig.ImageText.TYPE_BOTTOM:
                    return new Point((width - textWidth) * 0.5f, height - textHeight);
            }
        }

        public int getTextColor() {
            return _textColor;
        }

        public void setTextColor(int textColor) {
            _textColor = textColor;
            notifyChangedListener();
        }

        public int getBorderColor() {
            return _borderColor;
        }

        public void setBorderColor(int borderColor) {
            _borderColor = borderColor;
            notifyChangedListener();
        }

        public boolean isAllCaps() {
            return _allCaps;
        }

        public void setAllCaps(boolean allCaps) {
            _allCaps = allCaps;
            notifyChangedListener();
        }

        public String getText() {
            return _imgText.getText();
        }

        public void setText(String text) {
            _imgText.setText(text);
            notifyChangedListener();
        }

        @Override
        public String toString() {
            return _imgText.getText();
        }
    }


    public static class ElementImage extends MemeEditorElementBase {
        private Bitmap _image = null;
        private Bitmap _displayImage = null;
        private int _rotationDeg = 0;
        private int _padding = 0;
        private int _paddingColor = MemeLibConfig.MEME_COLORS.WHITE;

        public ElementImage(Bitmap image) {
            _image = image;
        }

        public Bitmap getImage() {
            return _image;
        }

        public void setImage(Bitmap image) {
            _image = image;
            notifyChangedListener();
        }

        public Bitmap getDisplayImage() {
            return _displayImage;
        }

        public void setDisplayImage(Bitmap displayImage) {
            _displayImage = displayImage;
            notifyChangedListener();
        }

        public int getRotationDeg() {
            return _rotationDeg;
        }

        public void setRotationDeg(int rotationDeg) {
            _rotationDeg = rotationDeg;
            notifyChangedListener();
        }


        public int getPadding() {
            return _padding;
        }

        /**
         * Set the _padding of the picture. A value of 10 means the picture is grown by 10
         * percent (size * 1.1) and the background filled with _paddingColor
         *
         * @param padding _padding
         */
        public void setPadding(int padding) {
            _padding = padding;
            notifyChangedListener();
        }

        public int getPaddingColor() {
            return _paddingColor;
        }

        /**
         * Set the color to be used for _padding background. See _padding
         *
         * @param paddingColor the color
         */
        public void setPaddingColor(int paddingColor) {
            _paddingColor = paddingColor;
            notifyChangedListener();
        }
    }

}

