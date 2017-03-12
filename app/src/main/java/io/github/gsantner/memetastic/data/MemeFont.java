package io.github.gsantner.memetastic.data;

import java.io.File;

/**
 * Contains the path and the name of a font
 */
public class MemeFont<F> {
    private final String filePath;
    private final String fontName;
    private F font;

    /**
     * Constructor that takes and saves the filepath to the font and the font itself
     *
     * @param filePath path where the font is saved
     * @param font     the platform specific font type for the font at the path
     */
    public MemeFont(String filePath, F font) {
        this.font = font;
        this.filePath = filePath;

        // Extract fontName
        String name = filePath;
        int sep = name.lastIndexOf(File.separator);
        name = name.substring(sep + 1);
        sep = name.lastIndexOf(".");
        fontName = name.substring(0, sep);

    }


    public String getFontName() {
        return fontName;
    }

    public F getFont() {
        return font;
    }
}