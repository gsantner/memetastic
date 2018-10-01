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
package net.gsantner.memetastic.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.gsantner.memetastic.App;
import net.gsantner.memetastic.activity.MainActivity;
import net.gsantner.memetastic.activity.MemeCreateActivity;
import net.gsantner.memetastic.data.MemeData;
import net.gsantner.memetastic.service.ImageLoaderTask;
import net.gsantner.memetastic.util.ContextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.R;

/**
 * Adapter to show images in given view mode
 */
public class MemeItemAdapter extends RecyclerView.Adapter<MemeItemAdapter.ViewHolder> implements ImageLoaderTask.OnImageLoadedListener<MemeItemAdapter.ViewHolder> {
    public static final int VIEW_TYPE__PICTURE_GRID = 0;
    public static final int VIEW_TYPE__ROWS_WITH_TITLE = 1;

    private int _itemViewType = -1; // TODO: Do implement this as later settable on adapter, instead of using if/else on AppSettings
    private List<MemeData.Image> _originalImageDataList; // original data
    private List<MemeData.Image> _imageDataList; // filtered data (use this)
    private int _shortAnimationDuration;
    private Activity _activity;
    private App _app;


    public MemeItemAdapter(List<MemeData.Image> imageDataList, Activity activity, int itemViewType) {
        _originalImageDataList = imageDataList;
        _imageDataList = new ArrayList<>(imageDataList);
        _shortAnimationDuration = -1;
        _activity = activity;
        _app = (App) (_activity.getApplication());
        _itemViewType = itemViewType;
    }

    public void setOriginalImageDataList(List<MemeData.Image> originalImageDataList) {
        _originalImageDataList = originalImageDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (_itemViewType) {
            case VIEW_TYPE__ROWS_WITH_TITLE: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_with_title, parent, false);
                break;
            }

            case VIEW_TYPE__PICTURE_GRID:
            default: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item__square_image, parent, false);
                break;
            }
        }

        return new ViewHolder(v);
    }

    // sets up the view of the item
    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final MemeData.Image imageData = _imageDataList.get(holder.getAdapterPosition());
        if (imageData == null || imageData.fullPath == null || !imageData.fullPath.exists()) {
            holder.imageView.setImageResource(R.drawable.ic_mood_bad_black_256dp);
            holder.imageButtonFav.setVisibility(View.INVISIBLE);
            holder.imageTitle.setText("Meme");
            return;
        }
        holder.imageTitle.setText(imageData.conf.getTitle());
        holder.imageButtonFav.setVisibility(View.INVISIBLE);
        holder.imageView.setVisibility(View.INVISIBLE);
        ImageLoaderTask<ViewHolder> taskLoadImage = new ImageLoaderTask<>(this, _activity, true, holder);
        taskLoadImage.execute(imageData.fullPath);
        holder.imageView.setTag(imageData);
        holder.imageButtonFav.setTag(imageData);

        tintFavouriteImage(holder.imageButtonFav, _app.settings.isFavorite(imageData.fullPath.toString()));

        preparePopupMenu(holder);

        holder.imageButtonFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeData.Image image = (MemeData.Image) v.getTag();
                if (image.isTemplate) {
                    toggleFavorite(holder);
                }
            }
        });


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeData.Image image = (MemeData.Image) v.getTag();

                if (image.isTemplate) {
                    Intent intent = new Intent(_activity, MemeCreateActivity.class);
                    intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, image.fullPath.getAbsolutePath());
                    intent.putExtra(MemeCreateActivity.EXTRA_MEMETASTIC_DATA, image);
                    _activity.startActivityForResult(intent, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
                } else {
                    if (_activity instanceof MainActivity) {
                        ((MainActivity) _activity).openImageViewActivityWithImage(holder.getAdapterPosition(), image.fullPath.getAbsolutePath());
                    }
                }
            }
        });
    }

    // gets and returns the count of available items in the grid
    @Override
    public int getItemCount() {
        return _imageDataList.size();
    }

    @Override
    public void onImageLoaded(Bitmap bitmap, final ViewHolder holder) {
        MemeData.Image dataImage = (MemeData.Image) holder.imageView.getTag();
        Animation animation = AnimationUtils.loadAnimation(_activity, R.anim.fadeinfast);
        holder.imageView.startAnimation(animation);
        if (dataImage.isTemplate) {
            holder.imageButtonFav.startAnimation(animation);
            holder.imageButtonFav.setVisibility(View.VISIBLE);
        }

        if (_app.settings.isHidden(dataImage.fullPath.getAbsolutePath())) {
            holder.imageButtonFav.setVisibility(View.INVISIBLE);
            holder.imageView.setOnClickListener(null);
            preparePopupMenu(holder);
        }
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setVisibility(View.VISIBLE);
    }

    private void toggleFavorite(ViewHolder holder) {
        MemeData.Image dataImage = (MemeData.Image) holder.imageView.getTag();
        if (!dataImage.isTemplate) {
            return;
        }
        if (_app.settings.toggleFavorite(dataImage.fullPath.getAbsolutePath())) {
            tintFavouriteImage(holder.imageButtonFav, true);
        } else {
            tintFavouriteImage(holder.imageButtonFav, false);
        }
        int index = _imageDataList.indexOf(dataImage);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    private void preparePopupMenu(final ViewHolder holder) {
        final MemeData.Image imageData = (MemeData.Image) holder.imageView.getTag();
        final PopupMenu menu = new PopupMenu(_activity, holder.imageView);
        menu.inflate(R.menu.memeitemadapter__popup_menu);
        ContextUtils.popupMenuEnableIcons(menu);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.memeitemadapter__popup_menu__action_fav:
                        toggleFavorite(holder);
                        return true;
                    case R.id.memeitemadapter__popup_menu__action_hide:
                        int position = holder.getAdapterPosition();
                        toggleHidden(holder, position);
                        ((MainActivity) _activity).updateHiddenNavOption();
                        return true;
                    case R.id.memeitemadapter__popup_menu__action_title:
                        MemeData.Image image = (MemeData.Image) holder.imageView.getTag();
                        Toast.makeText(holder.imageView.getContext(), image.conf.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        View longClickView;
        switch (_itemViewType) {
            case VIEW_TYPE__ROWS_WITH_TITLE: {
                longClickView = holder.itemView;
                break;
            }
            case VIEW_TYPE__PICTURE_GRID:
            default: {
                longClickView = holder.imageView;
                break;
            }
        }

        longClickView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(final View v) {
                Menu itemMenu = menu.getMenu();
                boolean isHidden = _app.settings.isHidden(imageData.fullPath.toString());
                boolean isFav = _app.settings.isFavorite(imageData.fullPath.toString());
                boolean isTemplate = imageData.isTemplate;

                itemMenu.findItem(R.id.memeitemadapter__popup_menu__action_hide).setVisible(isTemplate)
                        .setTitle(isHidden ? R.string.unhide : R.string.hide);

                itemMenu.findItem(R.id.memeitemadapter__popup_menu__action_fav).setVisible(isTemplate)
                        .setTitle(isFav ? R.string.remove_favourite : R.string.favourite);

                menu.show();
                return true;
            }
        });

    }

    private void toggleHidden(ViewHolder holder, int position) {
        MemeData.Image image = (MemeData.Image) holder.imageView.getTag();
        String filePath = image.fullPath.getAbsolutePath();

        if (_app.settings.toggleHiddenMeme(filePath)) {
            _imageDataList.remove(image);
            notifyItemRemoved(position);
        } else {
            _imageDataList.remove(image);
            notifyItemRemoved(position);
            ((MainActivity) _activity).recreateFragmentsAfterUnhiding();
        }

        if (_imageDataList.isEmpty()) {
            ((MainActivity) _activity).selectCreateMainMode();
        }
    }


    private void tintFavouriteImage(ImageView iv, boolean isFav) {
        ContextUtils.setDrawableWithColorToImageView(iv,
                isFav ? R.drawable.ic_star_black_32dp : R.drawable.ic_star_border_black_32dp,
                isFav ? R.color.comic_yellow : R.color.comic_blue);
    }

    public void setFilter(String filter) {
        _imageDataList.clear();
        String[] filterTokens = filter.toLowerCase().split("[\\W_]");
        ArrayList<String> contentTokens = new ArrayList<>();

        for (MemeData.Image image : _originalImageDataList) {
            contentTokens.clear();

            // Tokenize filename
            contentTokens.addAll(Arrays.asList(image.fullPath.getName().toLowerCase().split("[\\W_]")));

            // Tokenize the image title (split by everything that's not a word)
            if (image.conf != null && image.conf.getTitle() != null && !image.conf.getTitle().isEmpty()) {
                contentTokens.addAll(Arrays.asList(image.conf.getTitle().toLowerCase().split("[\\W_]")));
            }

            // Tokenize tags
            if (image.conf != null && image.conf.getTags() != null) {
                contentTokens.addAll(image.conf.getTags());
            }


            boolean allTokensFound = true;
            for (String filterToken : filterTokens) {
                boolean foundTokenInTitle = false;
                for (String titleToken : contentTokens) {
                    if (titleToken.contains(filterToken)) {
                        foundTokenInTitle = true;
                    }
                }
                if (!foundTokenInTitle) {
                    allTokensFound = false;
                    break;
                }
            }

            if (allTokensFound) {
                _imageDataList.add(image);
            }
        }
        notifyDataSetChanged();
    }

    // contains the conf view for the meme and the favorite button to access them
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item__square_image__image)
        public ImageView imageView;

        @BindView(R.id.item__square_image__image_bottom_end)
        public ImageView imageButtonFav;

        @BindView(R.id.item_square_image_title)
        public TextView imageTitle;


        // saves the instance of the conf view of the meme and favorite button to access them later
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (_shortAnimationDuration < 0)
                _shortAnimationDuration = imageView.getContext().getResources().getInteger(
                        android.R.integer.config_shortAnimTime);
        }
    }
}
