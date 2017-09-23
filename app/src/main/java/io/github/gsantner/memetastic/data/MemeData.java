package io.github.gsantner.memetastic.data;

import android.graphics.Typeface;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue", "SpellCheckingInspection", "UnusedReturnValue", "JavaDoc", "FieldCanBeLocal"})
public class MemeData implements Serializable {
    private static final List<Font> _fonts = new ArrayList<>();
    private static final List<Image> _images = new ArrayList<>();
    private static final List<Image> _createdMemes = new ArrayList<>();
    private static final HashMap<String, List<Image>> _imagesWithTags = new HashMap<>();

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
    }

    public static class Font {
        public MemeConfig.Font conf;
        public File fullPath;
        public Typeface typeFace;
    }

    public static class Image {
        public MemeConfig.Image conf;
        public File fullPath;
        public boolean isTemplate;
    }
}
