package io.github.gsantner.memetastic.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Contains the category name of the memes and all the available meme names in this category
 */
public class MemeCategory {
    private final String categoryName;
    private String[] imageNames;

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

    public void shuffleList() {
        List<String> l = new ArrayList<String>(Arrays.asList(imageNames));
        Collections.shuffle(l);
        imageNames = l.toArray(new String[l.size()]);
    }

    public MemeCategory orderByNameCaseInsensitive() {
        Arrays.sort(imageNames, new Comparator<String>() {
            @Override
            public int compare(String f1, String f2) {
                return f1.toLowerCase().compareTo(f2.toLowerCase());
            }
        });
        return this;
    }
}
