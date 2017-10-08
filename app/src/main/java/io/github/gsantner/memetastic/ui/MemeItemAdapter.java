package io.github.gsantner.memetastic.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.activity.MainActivity;
import io.github.gsantner.memetastic.activity.MemeCreateActivity;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.service.ImageLoaderTask;
import io.github.gsantner.memetastic.util.ContextUtils;

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
    public void onBindViewHolder(final ViewHolder holder, final int pos) {
        final MemeData.Image imageData = _imageDataList.get(pos);
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
        holder.imageTitle.setTag(imageData);



        tintFavouriteImage(holder.imageButtonFav, _app.settings.isFavorite(imageData.fullPath.toString()));

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                MemeData.Image image = (MemeData.Image) v.getTag();
                Toast.makeText(v.getContext(), image.conf.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        holder.imageButtonFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemeData.Image image = (MemeData.Image) v.getTag();
                if (image.isTemplate) {
                    toggleFavorite(holder);
                }
            }
        });

        holder.imageTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleItemClick(v,pos);
            }
        });


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleItemClick(v,pos);
            }
        });

        holder.imageTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                MemeData.Image image = (MemeData.Image) view.getTag();

                if(!image.isTemplate){
                    holder.imageTitleEdit.setVisibility(View.VISIBLE);
                    holder.imageTitle.setVisibility(View.INVISIBLE);
                    holder.imageTitleEdit.setText(holder.imageTitle.getText());
                    holder.imageTitleEdit.requestFocus();
                    InputMethodManager imm = (InputMethodManager) _activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(holder.imageTitleEdit, InputMethodManager.SHOW_IMPLICIT);
                }

                return true;
            }
        });

        holder.imageTitleEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled=false;
                if(i== EditorInfo.IME_ACTION_DONE||keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                    holder.imageTitleEdit.setVisibility(View.GONE);
                    holder.imageTitle.setText(holder.imageTitleEdit.getText());
                    holder.imageTitle.setVisibility(View.VISIBLE);
                    holder.imageTitle.requestFocus();
                    InputMethodManager imm = (InputMethodManager) _activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(holder.imageTitleEdit.getWindowToken(), 0);
                    handled =true;
                }
                return handled;
            }
        });


    }

    // gets and returns the count of available items in the grid
    @Override
    public int getItemCount() {
        return _imageDataList.size();
    }

    @Override
    public void onImageLoaded(Bitmap bitmap, ViewHolder holder) {
        MemeData.Image dataImage = (MemeData.Image) holder.imageView.getTag();
        Animation animation = AnimationUtils.loadAnimation(_activity, R.anim.fadeinfast);
        holder.imageView.startAnimation(animation);
        if (dataImage.isTemplate) {
            holder.imageButtonFav.startAnimation(animation);
            holder.imageButtonFav.setVisibility(View.VISIBLE);
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

    public void handleItemClick(View v,int pos){
        MemeData.Image image = (MemeData.Image) v.getTag();

        if (image.isTemplate) {
            Intent intent = new Intent(_activity, MemeCreateActivity.class);
            intent.putExtra(MemeCreateActivity.EXTRA_IMAGE_PATH, image.fullPath.getAbsolutePath());
            intent.putExtra(MemeCreateActivity.EXTRA_MEMETASTIC_DATA, image);
            _activity.startActivityForResult(intent, MemeCreateActivity.RESULT_MEME_EDITING_FINISHED);
        } else {
            if (_activity instanceof MainActivity) {
                ((MainActivity) _activity).openImageViewActivityWithImage(pos, image.fullPath.getAbsolutePath());
            }
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
        String[] titleTokens;

        for (MemeData.Image image : _originalImageDataList) {
            // Tokenize the image title (split by everything that's not a word)
            if (image.conf != null && image.conf.getTitle() != null && !image.conf.getTitle().isEmpty()) {
                titleTokens = image.conf.getTitle().toLowerCase().split("[\\W_]");
            } else {
                titleTokens = image.fullPath.getName().toLowerCase().split("[\\W_]");
            }

            boolean allTokensFound = true;
            for (String filterToken : filterTokens) {
                boolean foundTokenInTitle = false;
                for (String titleToken : titleTokens) {
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

        @BindView(R.id.item_square_image_title_edit)
        public EditText imageTitleEdit;


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
