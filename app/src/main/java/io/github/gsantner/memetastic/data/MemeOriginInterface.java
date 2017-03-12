package io.github.gsantner.memetastic.data;

/**
 * Interface that contains all necessary methods to access memes from different sources
 */
public interface MemeOriginInterface {

    public String getPath(int position, boolean bThumbnail);

    /**
     * gets and returns the number of available images
     *
     * @return the number of available images
     */
    public int getLength();

    /**
     * indicates if the image is saved in the assets or somewhere else
     *
     * @return true if the image is saved in the assets or somewhere else
     */
    public boolean isAsset();

    /**
     * creates and returns a valid path to a specific image
     *
     * @param position the position in the array/list of available images
     * @return the path to the image at the specified position
     */
    public String getFilepath(int position);

    /**
     * creates and returns a valid path to a specific thumbnail of an image
     *
     * @param position the position in the array/list of available images
     * @return the path to the thumbnail of the image at the specified position
     */
    public String getThumbnailPath(int position);

    /**
     * indicates the image should be able to be set as a favorite or not
     *
     * @return true if the image is favoritable, else false
     */
    public boolean showFavButton();
}
