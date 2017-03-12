package io.github.gsantner.memetastic.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.activity.MainActivity;
import io.github.gsantner.memetastic.activity.MemeCreateActivity;
import io.github.gsantner.memetastic.data.MemeOriginAssets;
import io.github.gsantner.memetastic.data.MemeOriginFavorite;
import io.github.gsantner.memetastic.data.MemeOriginInterface;
import io.github.gsantner.memetastic.util.Helpers;
import io.github.gsantner.memetastic.util.ImageLoaderTask;

/**
 * Adapter to show images in a Grid
 */
public class GridRecycleAdapter extends RecyclerView.Adapter<GridRecycleAdapter.ViewHolder> implements ImageLoaderTask.OnImageLoadedListener {
    private MemeOriginInterface mMemeObject;
    private int mShortAnimationDuration;
    private Activity mActivity;
    private App app;

    public GridRecycleAdapter(MemeOriginInterface memeObject, Activity act) {
        mMemeObject = memeObject;
        mShortAnimationDuration = -1;
        mActivity = act;
        app = (App) (mActivity.getApplication());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item__square_image, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    /**
     * adds/removes a meme template to/from the favorites depending on if it's already a favorite or not
     *
     * @param positionInGrid the position of the meme in the grid that should be toggled
     * @param imageButtonFav the ImageView of the clicked meme to toggle the favorite star
     * @param memeObj        the memes of the grid where the clicked meme is saved in
     */
    private void toggleFavorite(int positionInGrid, ImageView imageButtonFav, MemeOriginInterface memeObj) {
        if (app.settings.toggleFavorite(memeObj.getFilepath(positionInGrid))) {
            tintFavourite(imageButtonFav, true);
        } else {
            if (mMemeObject instanceof MemeOriginFavorite) {
                ((MemeOriginFavorite) mMemeObject).setFiles(app.settings.getFavoriteMemes());
                notifyDataSetChanged();
            } else {
                tintFavourite(imageButtonFav, false);
            }
        }
    }

    // sets up the view of the item at the position in the grid
    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int position = pos;
        holder.imageButtonFav.setVisibility(View.INVISIBLE);
        holder.imageView.setVisibility(View.INVISIBLE);
        ImageLoaderTask taskLoadImage;
        if (mMemeObject instanceof MemeOriginAssets) {
            taskLoadImage = new ImageLoaderTask(this, holder, true, mActivity.getAssets());
        } else {
            taskLoadImage = new ImageLoaderTask(this, holder, true);
        }
        taskLoadImage.execute(mMemeObject.getPath(position, true));
        holder.imageView.setTag(mMemeObject.getFilepath(position));

        tintFavourite(holder.imageButtonFav, app.settings.isFavorite(mMemeObject.getFilepath(position)));

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                onImageLongClicked(position, holder.imageButtonFav, mMemeObject);
                return true;
            }
        });
        holder.imageButtonFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite(position, holder.imageButtonFav, mMemeObject);
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMemeObject instanceof MemeOriginAssets) {
                    Intent intent = new Intent(mActivity, MemeCreateActivity.class);
                    intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, mMemeObject.getFilepath(position));
                    intent.putExtra(MemeCreateActivity.ASSET_IMAGE, mMemeObject.isAsset());
                    mActivity.startActivityForResult(intent, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
                } else {
                    if (mActivity instanceof MainActivity) {
                        ((MainActivity) mActivity).openImageViewActivityWithImage(mMemeObject.getFilepath(position));
                    }
                }
            }
        });
    }

    private void tintFavourite(ImageView iv, boolean isFav) {
        Helpers.setDrawableWithColorToImageView(iv,
                isFav ? R.drawable.ic_star_black_48px : R.drawable.ic_star_border_black_48px,
                isFav ? R.color.comic_yellow : R.color.comic_blue);
    }

    // gets and returns the count of available items in the grid
    @Override
    public int getItemCount() {
        return mMemeObject.getLength();
    }

    public void onImageLongClicked(final int position, final ImageView iv, final MemeOriginInterface memeObj) {
        Context context = iv.getContext().getApplicationContext();
        String pic = mMemeObject.getFilepath(position);
        if (!pic.contains(context.getString(R.string.app_name))) {
            pic = pic.substring(pic.lastIndexOf("_") + 1);
            pic = pic.substring(0, pic.indexOf("."));
        }
        Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content), pic, Snackbar.LENGTH_SHORT);
        if (memeObj instanceof MemeOriginAssets) {
            snackbar.setAction(R.string.main__mode__favs, new View.OnClickListener() {
                public void onClick(View v) {
                    toggleFavorite(position, iv, memeObj);
                }
            });
        }
        snackbar.show();
    }

    @Override
    public void onImageLoaded(Bitmap bitmap, ViewHolder holder) {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.fadeinfast);
        holder.imageView.startAnimation(animation);
        if (mMemeObject.showFavButton()) {
            holder.imageButtonFav.startAnimation(animation);
            holder.imageButtonFav.setVisibility(View.VISIBLE);
        }
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setVisibility(View.VISIBLE);
    }

    // contains the image view for the meme and the favorite button to access them
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageView imageButtonFav;

        // saves the instance of the image view of the meme and favorite button to access them later
        public ViewHolder(View itemView) {
            super(itemView);
            imageButtonFav = (ImageView) itemView.findViewById(R.id.item__square_image__image_bottom_end);
            imageView = (ImageView) itemView.findViewById(R.id.item__square_image__image);
            if (mShortAnimationDuration < 0)
                mShortAnimationDuration = imageView.getContext().getResources().getInteger(
                        android.R.integer.config_shortAnimTime);
        }
    }
}
