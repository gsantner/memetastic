package io.github.gsantner.memetastic.data;

import android.graphics.Bitmap;

import java.io.Serializable;

import static io.github.gsantner.memetastic.data.MemeConfig.Point;

/**
 * A memes settings
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MemeEditorObject extends MemeEditorObjectBase implements MemeEditorObjectBase.OnMemeEditorObjectChangedListener, Serializable {
    private MemeElementText _captionTop;
    private MemeElementText _captionBottom;
    private MemeElementImage _imageMain;

    public MemeEditorObject(MemeData.Font font, Bitmap image) {
        _captionTop = new MemeElementText(font, MemeConfig.ImageText.TYPE_TOP);
        _captionBottom = new MemeElementText(font, MemeConfig.ImageText.TYPE_BOTTOM);
        _imageMain = new MemeElementImage(image);

        _captionTop.setChangedListener(this);
        _captionBottom.setChangedListener(this);
        _imageMain.setChangedListener(this);
    }

    @Override
    public void onMemeEditorObjectChanged(MemeEditorObjectBase memeEditorObject) {
        notifyChangedListener();
    }

    @Override
    public String toString() {
        return _captionTop.toString() + "\n" + _captionBottom.toString();
    }

    public MemeElementText getCaptionTop() {
        return _captionTop;
    }

    public void setCaptionTop(MemeElementText captionTop) {
        _captionTop = captionTop;
        notifyChangedListener();
    }

    public MemeElementText getCaptionBottom() {
        return _captionBottom;
    }

    public void setCaptionBottom(MemeElementText captionBottom) {
        _captionBottom = captionBottom;
        notifyChangedListener();
    }

    public MemeElementImage getImageMain() {
        return _imageMain;
    }

    public void setImageMain(MemeElementImage imageMain) {
        _imageMain = imageMain;
        notifyChangedListener();
    }

    public static class MemeElementText extends MemeEditorObjectBase {
        private String _text = "";
        private int _fontSize = MemeLibConfig.FONT_SIZES.DEFAULT;
        private int _textColor = MemeLibConfig.MEME_COLORS.DEFAULT_TEXT;
        private int _borderColor = MemeLibConfig.MEME_COLORS.DEFAULT_BORDER;
        private int _locationMode = MemeConfig.ImageText.TYPE_CUSTOM;
        private Point _location = new Point(); // (x, y)% for custom location
        private boolean _allCaps = true;
        private MemeData.Font _font = null;

        public MemeElementText(MemeData.Font font, int locationMode) {
            _font = font;
            _locationMode = locationMode;
            notifyChangedListener();
        }

        public MemeElementText(MemeData.Font font, float x, float y) {
            _font = font;
            _locationMode = MemeConfig.ImageText.TYPE_CUSTOM;
            _location.set(x, y);
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
            switch (_locationMode) {
                case MemeConfig.ImageText.TYPE_CUSTOM:
                default:
                    return new Point(
                            width * _location.getX() - textWidth * 0.5f,
                            height * _location.getY() - textHeight * 0.5f);

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
            return _text;
        }

        public void setText(String text) {
            _text = text;
            notifyChangedListener();
        }

        @Override
        public String toString() {
            return _text;
        }
    }


    public static class MemeElementImage extends MemeEditorObjectBase {
        private Bitmap _image = null;
        private Bitmap _displayImage = null;
        private int _rotationDeg = 0;
        private int _padding = 0;
        private int _paddingColor = MemeLibConfig.MEME_COLORS.WHITE;

        public MemeElementImage(Bitmap image) {
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

