package io.github.gsantner.memetastic.data;

import java.io.File;

/**
 * Contains colors, fontsizes, locations, categorynames
 * and folders where fonts and memetemplates are saved
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

        public static final int DEFAULT_TEXT = WHITE;
        public static final int DEFAULT_BORDER = BLACK;

        public static int[] ALL = new int[]{
                /* Material colors */-769226, -1499549, -54125, -6543440, -10011977, -12627531, -14575885, -16537100, -16728876, -16738680, -11751600, -7617718, -3285959, -5317, -16121, -26624, -8825528, -10453621, -6381922
                , GRAY, BLACK, WHITE
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
