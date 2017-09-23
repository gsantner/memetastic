package io.github.gsantner.memetastic.data;

import java.io.File;

/**
 * Contains colors, fontsizes, categorynames and folders where fonts and memetemplates are saved
 */
@SuppressWarnings("WeakerAccess")
public class MemeLibConfig {
    public static class Assets {
        public static final String FONTS = "fonts";
        public static final String MEMES = "memes";
    }

    public static class FONT_SIZES {
        public static final int DEFAULT = 14;
        public static final int MIN = 4;
        public static final int MAX = 30;
    }

    public static class MEME_COLORS {
        public static final int BLACK = 0xff000000;
        public static final int WHITE = 0xffFFFFFF;
        public static final int GRAY = 0xff808080;
        public static final int RED = 0xffFF0000;
        public static final int ORANGE = 0xffFF7F00;
        public static final int YELLOW = 0xffFFFF00;
        public static final int GREEN = 0xff00FF00;
        public static final int BLUE = 0xff0000FF;
        public static final int INDIGO = 0xff4B0082;
        public static final int VIOLET = 0xff8B00FF;

        public static final int DEFAULT_TEXT = WHITE;
        public static final int DEFAULT_BORDER = BLACK;

        public static int[] ALL = new int[]{
                BLACK, WHITE, GRAY, RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET
        };
    }

    public static final int MEME_FULLSCREEN_MAX_IMAGESIZE = 2000;


    /**
     * Creates a valid path as a String
     *
     * @param base          the string that should be checked and/or modified to be a valid path
     * @param trailingSlash if the path should contain a slash at the end of it
     * @return the valid path as a string
     */
    public static String getPath(String base, boolean trailingSlash) {
        String separator = File.separator;
        base = base.replace("/", separator).replace("\\", separator);
        if (trailingSlash && !base.endsWith(separator)) {
            base += separator;
        }
        return base;
    }
}
