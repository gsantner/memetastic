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

        private List<String> _tags = new ArrayList<>();
        private List<Caption> _captions = new ArrayList<>();
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
            if (!tagsList.contains("all")) {
                tagsList.add("all");
            }
            setTags(tagsList);

            List<Caption> captions = new ArrayList<>();
            if (json.has("captions")) {
                jsonArr = json.getJSONArray("captions");
                for (int i = 0; i < jsonArr.length(); i++) {
                    try {
                        Caption element = new Caption().fromJson(jsonArr.getJSONObject(i));
                        captions.add(element);
                    } catch (JSONException ignored) {
                    }
                }
            }
            setCaptions(captions);

            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("title", getTitle());
            root.put("filename", getFilename());

            if (!_tags.isEmpty()) {
                root.put("tags", new JSONArray(getTags()));
            }

            if (!_captions.isEmpty()) {
                JSONArray jsonArray = new JSONArray();
                for (Caption caption : _captions) {
                    jsonArray.put(caption.toJson());
                }
                root.put("captions", jsonArray);
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

        public List<String> getTags() {
            if (_tags == null) {
                setTags(new ArrayList<String>());
            }
            return _tags;
        }

        public void setTags(List<String> tags) {
            _tags = tags;
        }

        public List<Caption> getCaptions() {
            return _captions;
        }

        public void setCaptions(List<Caption> captions) {
            _captions = captions;
        }
    }

    public static class Caption implements Serializable {
        public static final int TYPE_TOP = 1;
        public static final int TYPE_BOTTOM = 2;
        public static final int TYPE_CUSTOM = 9;

        private String _text = "";
        private int _positionType;
        private Point _position; // opt ;
        private Point _captionSize; // opt ; x = width, y = height

        public Caption fromJson(JSONObject json) throws JSONException {
            setPositionType(json.getInt("id"));
            setText(json.getString("text"));
            if (json.has("position")) {
                _position = new Point().fromJson(json.getJSONObject("position"));
            }
            if (json.has("size")) {
                _captionSize = new Point().fromJson(json.getJSONObject("size"));
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
            if (_captionSize != null) {
                root.put("size", _captionSize.toJson());
            }

            return root;
        }

        public Point getPosition() {
            return _position;
        }

        public void setPosition(Point position) {
            _position = position;
        }

        public Point getCaptionSize() {
            return _captionSize;
        }

        public void setCaptionSize(Point captionSize) {
            _captionSize = captionSize;
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
        private String _description;
        private String _filename;
        private int _fontType;

        public Font fromJson(JSONObject json) throws JSONException {
            setTitle(json.getString("title"));
            setFilename(json.getString("filename"));
            setFontType(json.optInt("font_type", 0));
            setDescription(json.optString("description", ""));
            return this;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject root = new JSONObject();
            root.put("title", getTitle());
            root.put("filename", getFilename());
            if (getFontType() != 0) {
                root.put("font_type", getFontType());
            }
            if (!TextUtils.isEmpty(getDescription())) {
                root.put("description", getDescription());
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

        public String getDescription() {
            return _description;
        }

        public void setDescription(String description) {
            _description = description;
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
