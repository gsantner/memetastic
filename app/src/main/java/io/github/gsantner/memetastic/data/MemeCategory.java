package io.github.gsantner.memetastic.data;

import java.io.File;

/**
 * Contains the category name of the memes and all the available meme names in this category
 */
public class MemeCategory {
    private final String categoryName;
    private final String[] imageNames;

    /**
     * Constructor that takes and saves the category name and the memes
     *
     * @param categoryName name of the category of the memes
     * @param imageNames   names of the memes as string array
     */
    public MemeCategory(String categoryName, String[] imageNames) {
        this.categoryName = categoryName;
        this.imageNames = imageNames;
    }

    /**
     * gets and returns the name of the category
     *
     * @return the name of the category
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * creates a valid path to the memes of this category
     *
     * @param trailingSlash true if the path should end with a slash
     * @return a valid path to the memes of this category
     */
    public String getFolderPath(boolean trailingSlash) {
        String ret = MemeLibConfig.getPath(MemeLibConfig.Assets.MEMES, true) + categoryName;
        if (trailingSlash)
            ret += File.separator;
        return ret;
    }

//    public String[] getImageNames() {
//        return imageNames;
//    }

    /**
     * creates a path to a specific meme of this category
     *
     * @param position the position of the meme in the array of memes of this category
     * @return the valid path to the meme at the position in the array of memes
     */
    public String getImagePath(int position) {
        return getFolderPath(true) + imageNames[position];
    }

}
