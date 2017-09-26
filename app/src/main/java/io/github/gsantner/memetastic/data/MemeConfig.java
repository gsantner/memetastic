package io.github.gsantner.memetastic.data;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue", "SpellCheckingInspection", "UnusedReturnValue", "JavaDoc", "FieldCanBeLocal"})
public class MemeConfig implements Serializable {

    public static class Config implements Serializable {
        private List<Font> _fonts;
        private List<Image> _images;

        public Config fromJson(JSONObject json) throws JSONException {
            List<Image> imagesList = new ArrayList<>();
            JSONArray jsonArr = json.getJSONArray("images");
            for (int i = 0; i < jsonArr.length(); i++) {
                try {
                    Image element = new Image().fromJson(jsonArr.getJSONObject(i));
                    imagesList.add(element);
                } catch (JSONException ignored) {
                }
            }
            setImages(imagesList);

            List<Font> fontList = new ArrayList<>();
            jsonArr = json.getJSONArray("fonts");
            for (int i = 0; i < jsonArr.length(); i++) {
                try {
                    Font element = new Font().fromJson(jsonArr.getJSONObject(i));
                    fontList.add(element);
                } catch (JSONException ignored) {
                }
            }
            setFonts(fontList);
            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (Image element : getImages()) {
                jsonArray.put(element.toJson());
            }
            root.put("images", jsonArray);

            jsonArray = new JSONArray();
            for (Font element : getFonts()) {
                jsonArray.put(element.toJson());
            }
            root.put("fonts", jsonArray);

            return root;
        }

        public List<Font> getFonts() {
            if (_fonts.isEmpty()) {
                setFonts(new ArrayList<Font>());
            }
            return _fonts;
        }

        public void setFonts(List<Font> fonts) {
            _fonts = fonts;
        }

        public List<Image> getImages() {
            if (_images.isEmpty()) {
                setImages(new ArrayList<Image>());
            }
            return _images;
        }

        public void setImages(List<Image> images) {
            _images = images;
        }
    }

    public static class Image implements Serializable {
        public final static String IMAGE_TAG_OTHER = "other";

        private List<String> _tags;
        private List<ImageText> _imageTexts;
        private String _title;
        private String _filename;

        public Image fromJson(JSONObject json) throws JSONException {
            setTitle(json.getString("title"));
            setFilename(json.getString("filename"));

            JSONArray jsonArr;
            List<String> tagsList = new ArrayList<>();
            if (json.has("tags")) {
                jsonArr = json.getJSONArray("tags");
                for (int i = 0; i < jsonArr.length(); i++) {
                    tagsList.add(jsonArr.getString(i));
                }
            }
            setTags(tagsList);

            List<ImageText> imageTexts = new ArrayList<>();
            if (json.has("image_texts")) {
                jsonArr = json.getJSONArray("image_texts");
                for (int i = 0; i < jsonArr.length(); i++) {
                    try {
                        ImageText element = new ImageText().fromJson(jsonArr.getJSONObject(i));
                        imageTexts.add(element);
                    } catch (JSONException ignored) {
                    }
                }
            }
            setImageTexts(imageTexts);

            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("title", getTitle());
            root.put("filename", getFilename());
            root.put("tags", new JSONArray(getTags()));

            return root;
        }


        public String getTitle() {
            if (TextUtils.isEmpty(_title)) {
                setTitle(getFilename());
            }
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public String getFilename() {
            return _filename;
        }

        public void setFilename(String filename) {
            _filename = filename;
        }

        public List<String> getTags() {
            if (_tags == null) {
                setTags(new ArrayList<String>());
            }
            return _tags;
        }

        public void setTags(List<String> tags) {
            _tags = tags;
        }

        public List<ImageText> getImageTexts() {
            return _imageTexts;
        }

        public void setImageTexts(List<ImageText> imageTexts) {
            _imageTexts = imageTexts;
        }
    }

    public static class ImageText implements Serializable {
        public static final int TYPE_TOP = 1;
        public static final int TYPE_BOTTOM = 2;
        public static final int TYPE_CUSTOM = 9;

        private String _text;
        private int _positionType;
        private Point _position; // opt ;
        private Point _size; // opt ; x = width, y = height

        public ImageText fromJson(JSONObject json) throws JSONException {
            setPositionType(json.getInt("id"));
            setText(json.getString("text"));
            if (json.has("position")) {
                _position = new Point().fromJson(json.getJSONObject("position"));
            }
            if (json.has("size")) {
                _size = new Point().fromJson(json.getJSONObject("size"));
            }
            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("id", getPositionType());
            root.put("text", getText());
            if (_position != null) {
                root.put("position", _position.toJson());
            }
            if (_size != null) {
                root.put("size", _size.toJson());
            }

            return root;
        }

        public Point getPosition() {
            return _position;
        }

        public void setPosition(Point position) {
            _position = position;
        }

        public Point getSize() {
            return _size;
        }

        public void setSize(Point size) {
            _size = size;
        }

        public int getPositionType() {
            return _positionType;
        }

        public void setPositionType(int positionType) {
            _positionType = positionType;
        }

        public String getText() {
            return _text;
        }

        public void setText(String text) {
            _text = text;
        }
    }

    public static class Font implements Serializable {
        public static final int FONT_TYPE__DEFAULT = 0;
        public static final int FONT_TYPE__COMIC = 1;

        private String _title;
        private String _filename;
        private int _fontType;

        public Font fromJson(JSONObject json) throws JSONException {
            setTitle(json.getString("title"));
            setFilename(json.getString("filename"));
            setFontType(json.optInt("font_type", 0));
            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("title", getTitle());
            root.put("filename", getFilename());
            if (getFontType() != 0) {
                root.put("font_type", getFontType());
            }
            return root;
        }

        public String getTitle() {
            if (TextUtils.isEmpty(_title)) {
                setTitle(getFilename());
            }
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public String getFilename() {
            return _filename;
        }

        public void setFilename(String filename) {
            _filename = filename;
        }

        public int getFontType() {
            return _fontType;
        }

        public void setFontType(int fontType) {
            _fontType = fontType;
        }
    }

    // For interoperability with PointF use public members
    public static class Point implements Serializable {
        public float x;
        public float y;

        public Point() {

        }

        public Point(float x, float y) {
            setX(x);
            setY(y);
        }

        public Point fromJson(JSONObject json) throws JSONException {
            setX((float) json.getDouble("x"));
            setY((float) json.getDouble("y"));
            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("x", x);
            root.put("y", y);
            return root;
        }

        public void set(float x, float y) {
            setX(x);
            setY(y);
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}
