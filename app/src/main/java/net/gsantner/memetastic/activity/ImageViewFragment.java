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
package net.gsantner.memetastic.activity;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.gsantner.memetastic.util.ContextUtils;
import net.gsantner.memetastic.util.PermissionChecker;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.R;


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
