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

    public static class Config {
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

    public static class Image {
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

    public static class ImageText {
        private int _id; // -1 top, -2 bottom
        private String _text;

        public ImageText fromJson(JSONObject json) throws JSONException {
            setId(json.getInt("id"));
            setText(json.getString("text"));
            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("id", getId());
            root.put("text", getText());

            return root;
        }

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            _id = id;
        }

        public String getText() {
            return _text;
        }

        public void setText(String text) {
            _text = text;
        }
    }

    public static class Font {
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
}
