package io.github.gsantner.memetastic.activity;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.util.ContextUtils;
import io.github.gsantner.memetastic.util.PermissionChecker;


public class ImageViewFragment extends Fragment {


    @BindView(R.id.imageview_fragment__expanded_image)
    ImageView _expandedImageView;


    private static final String ARG_PARAM__POS = "pos";
    private static final String ARG_PARAM__IMAGE_PATH = "param2";


    private int _position;
    public String _imagePath;
    public File _imageFile;

    public Bitmap _bitmap;


    public ImageViewFragment() {
        // Required empty public constructor
    }


    public static ImageViewFragment newInstance(int position, String param2) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM__POS, position);
        args.putString(ARG_PARAM__IMAGE_PATH, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            _position = getArguments().getInt(ARG_PARAM__POS);
            _imagePath = getArguments().getString(ARG_PARAM__IMAGE_PATH);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_view, container, false);

        ButterKnife.bind(this, view);


        _imageFile = new File(_imagePath);
        if (PermissionChecker.hasExtStoragePerm(getActivity()) && _imageFile.exists()) {
            _bitmap = ContextUtils.get().loadImageFromFilesystem(_imageFile);
        }
        if (_bitmap == null) {
            _imageFile = null;
            _bitmap = ContextUtils.get().drawableToBitmap(
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_mood_bad_black_256dp));
        }
        _expandedImageView.setImageBitmap(_bitmap);

        return view;
    }

    @Override
    public void onDestroy() {
        _expandedImageView.setImageBitmap(null);
        if (_bitmap != null && !_bitmap.isRecycled())
            _bitmap.recycle();
        super.onDestroy();
    }

    @OnClick(R.id.imageview_fragment__expanded_image)
    public void onImageClicked() {
        getActivity().finish();
    }

}
