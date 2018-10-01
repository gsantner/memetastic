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

import android.graphics.Typeface;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;


@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue", "SpellCheckingInspection", "UnusedReturnValue", "JavaDoc", "FieldCanBeLocal"})
public class MemeData implements Serializable {
    private static final List<Font> _fonts = new ArrayList<>();
    private static final List<Image> _images = new ArrayList<>();
    private static final List<Image> _createdMemes = new ArrayList<>();
    private static final HashMap<String, List<Image>> _imagesWithTags = new HashMap<>();
    private static boolean _wasInit = false;
    private static final Object _wasInitSync = new Object();

    public static boolean isReady() {
        synchronized (_wasInitSync) {
            return !_fonts.isEmpty() && !_images.isEmpty() && _wasInit;
        }
    }

    public static void setWasInit(boolean value) {
        synchronized (_wasInitSync) {
            _wasInit = value;
        }
    }

    public static boolean wasInit() {
        return _wasInit;
    }

    public static List<Font> getFonts() {
        return _fonts;
    }

    public static List<Image> getImages() {
        return _images;
    }

    public static List<Image> getCreatedMemes() {
        for (Image image : _createdMemes) {
            image.isTemplate = false;
        }
        Collections.sort(_createdMemes, new Comparator<Image>() {
            @Override
            public int compare(Image mine, Image other) {
                return other.fullPath.compareTo(mine.fullPath);
            }
        });
        return _createdMemes;
    }

    public static void clearImagesWithTags() {
        _imagesWithTags.clear();
    }

    public static Image findImage(File filePath) {
        for (Image img : _images) {
            if (img.fullPath.equals(filePath)) {
                return img;
            }
        }
        for (Image img : _createdMemes) {
            if (img.fullPath.equals(filePath)) {
                return img;
            }
        }
        return null;
    }

    public static Font findFont(File filePath) {
        for (Font font : _fonts) {
            if (font.fullPath.equals(filePath)) {
                return font;
            }
        }
        return null;
    }

    public static synchronized List<Image> getImagesWithTag(String tag) {
        try {
            if (_imagesWithTags.containsKey(tag)) {
                return _imagesWithTags.get(tag);
            }
            boolean isOtherTag = tag.equals("other");
            List<Image> newlist = new ArrayList<>();
            for (Image image : getImages()) {
                for (String imgTag : image.conf.getTags()) {
                    if (imgTag.equals(tag)) {
                        newlist.add(image);
                        break;
                    }
                }
                if (isOtherTag && image.conf.getTags().isEmpty()) {
                    newlist.add(image);
                }
            }
            _imagesWithTags.put(tag, newlist);
            return newlist;
        } catch (ConcurrentModificationException ex) {
            return new ArrayList<>();
        }
    }

    public static class Font {
        public MemeConfig.Font conf;
        public File fullPath;
        public Typeface typeFace;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Font) {
                return fullPath.equals(((Font) obj).fullPath);
            }
            return super.equals(obj);
        }
    }

    public static class Image implements Serializable {
        public MemeConfig.Image conf;
        public File fullPath;
        public boolean isTemplate;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Image) {
                return fullPath.equals(((Image) obj).fullPath);
            }
            return super.equals(obj);
        }
    }
}
