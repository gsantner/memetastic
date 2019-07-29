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
package net.gsantner.memetastic.data;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static net.gsantner.memetastic.data.MemeConfig.Point;

/**
 * A memes settings
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MemeEditorElements implements Serializable {
    private List<EditorCaption> _captions;
    private EditorImage _imageMain; // There MUST be always top and bottom caption

    public MemeEditorElements(MemeData.Font font, Bitmap image) {
        _captions = new ArrayList<>();
        _captions.add(new EditorCaption(font, MemeConfig.Caption.TYPE_TOP));
        _captions.add(new EditorCaption(font, MemeConfig.Caption.TYPE_BOTTOM));
        _imageMain = new EditorImage(image);
    }

    public EditorImage getImageMain() {
        return _imageMain;
    }

    public void setImageMain(EditorImage imageMain) {
        _imageMain = imageMain;
    }

    public List<EditorCaption> getCaptions() {
        return _captions;
    }

    public EditorCaption getCaptionTop() {
        for (EditorCaption caption : _captions) {
            if (caption.getCaptionConf().getPositionType() == MemeConfig.Caption.TYPE_TOP) {
                return caption;
            }
        }
        return null;
    }

    public EditorCaption getCaptionBottom() {
        for (EditorCaption caption : _captions) {
            if (caption.getCaptionConf().getPositionType() == MemeConfig.Caption.TYPE_BOTTOM) {
                return caption;
            }
        }
        return null;
    }

    public void setFontToAll(MemeData.Font font) {
        for (EditorCaption caption : _captions) {
            caption.setFont(font);
        }
    }

    public static class EditorCaption implements Serializable {
        private MemeConfig.Caption _captionConf;
        private MemeData.Font _font = null; // !serializable

        private int _fontSize = MemeLibConfig.FONT_SIZES.DEFAULT;
        private int _textColor = MemeLibConfig.MEME_COLORS.DEFAULT_TEXT;
        private int _borderColor = MemeLibConfig.MEME_COLORS.DEFAULT_BORDER;
        private boolean _allCaps = true;
        private String _text = "";

        public EditorCaption(MemeData.Font font, int positionType) {
            _font = font;

            // TODO: Really load this from config if possible
            _captionConf = new MemeConfig.Caption();
            _captionConf.setPositionType(positionType);
            _captionConf.setText("");
            // end to-do
            _text = _captionConf.getText();
        }

        public EditorCaption(MemeData.Font font, MemeConfig.Caption captionConf) {
            _font = font;
            _captionConf = captionConf;
        }

        public MemeData.Font getFont() {
            return _font;
        }

        public void setFont(MemeData.Font font) {
            _font = font;
        }

        public int getFontSize() {
            return _fontSize;
        }

        public void setFontSize(int fontSize) {
            _fontSize = fontSize;
        }

        public Point getPositionInCanvas(float width, float height, float textWidth, float textHeight) {
            switch (_captionConf.getPositionType()) {
                case MemeConfig.Caption.TYPE_CUSTOM:
                default: {
                    Point point = _captionConf.getPosition();
                    if (point == null) {
                        point = new Point(0.5f, 0.5f);
                    }
                    return new Point(
                            width * point.getX() - textWidth * 0.5f,
                            height * point.getY() - textHeight * 0.5f);
                }

                case MemeConfig.Caption.TYPE_TOP:
                    return new Point((width - textWidth) * 0.5f, height / 15f);

                case MemeConfig.Caption.TYPE_BOTTOM:
                    return new Point((width - textWidth) * 0.5f, height - textHeight);
            }
        }

        public int getTextColor() {
            return _textColor;
        }

        public void setTextColor(int textColor) {
            _textColor = textColor;
        }

        public int getBorderColor() {
            return _borderColor;
        }

        public void setBorderColor(int borderColor) {
            _borderColor = borderColor;
        }

        public boolean isAllCaps() {
            return _allCaps;
        }

        public void setAllCaps(boolean allCaps) {
            _allCaps = allCaps;
        }

        public String getText() {
            return _text;
        }

        public void setText(String text) {
            _text = text;
        }

        @Override
        public String toString() {
            return _captionConf.getText();
        }

        public MemeConfig.Caption getCaptionConf() {
            return _captionConf;
        }

        public void setImgText(MemeConfig.Caption captionConf) {
            _captionConf = captionConf;
        }
    }


    public static class EditorImage implements Serializable {
        private Bitmap _image = null;
        private Bitmap _displayImage = null;
        private boolean _isTextSettingsGlobal = true;
        private int _rotationDeg = 0;
        private int _padding = 0;
        private int _paddingColor = MemeLibConfig.MEME_COLORS.WHITE;

        public EditorImage(Bitmap image) {
            _image = image;
        }

        public Bitmap getImage() {
            return _image;
        }

        public void setImage(Bitmap image) {
            _image = image;
        }

        public Bitmap getDisplayImage() {
            return _displayImage;
        }

        public void setDisplayImage(Bitmap displayImage) {
            _displayImage = displayImage;
        }

        public int getRotationDeg() {
            return _rotationDeg;
        }

        public void setRotationDeg(int rotationDeg) {
            _rotationDeg = rotationDeg;
        }

        public boolean isTextSettingsGlobal() {
            return _isTextSettingsGlobal;
        }

        public void setTextSettingsGlobal(boolean v) {
            _isTextSettingsGlobal = v;
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
        }
    }

}

