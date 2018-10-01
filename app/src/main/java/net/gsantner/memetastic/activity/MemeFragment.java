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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.gsantner.memetastic.App;
import net.gsantner.memetastic.data.MemeData;
import net.gsantner.memetastic.ui.GridDecoration;
import net.gsantner.memetastic.ui.MemeItemAdapter;
import net.gsantner.memetastic.util.AppCast;
import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.memetastic.util.ContextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.gsantner.memetastic.R;


public class MemeFragment extends Fragment {
    @BindView(R.id.meme_fragment__recycler_view)
    RecyclerView _recyclerMemeList;

    @BindView(R.id.meme_fragment__list_empty_layout)
    LinearLayout _emptylistLayout;

    @BindView(R.id.meme_fragment__list_empty_text)
    TextView _emptylistText;

    App _app;
    int _tabPos;
    String[] _tagKeys, _tagValues;
    private Unbinder _unbinder;
    private List<MemeData.Image> _imageList;
    private MemeItemAdapter _recyclerMemeAdapter;


    public MemeFragment() {
        // Required empty public constructor
    }

    // newInstance constructor for creating fragment with arguments
    public static MemeFragment newInstance(int pagePos) {
        MemeFragment fragmentFirst = new MemeFragment();
        Bundle args = new Bundle();
        args.putInt("pos", pagePos);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _app = (App) getActivity().getApplication();
        _tabPos = getArguments().getInt("pos");

        _imageList = new ArrayList<>();
    }

    private void reloadAdapter() {
        _tagKeys = getResources().getStringArray(R.array.meme_tags__keys);
        _tagValues = getResources().getStringArray(R.array.meme_tags__titles);
        if (_tabPos >= 0 && _tabPos < _tagKeys.length) {
            _imageList = MemeData.getImagesWithTag(_tagKeys[_tabPos]);
        }

        if (_app.settings.isShuffleTagLists()) {
            Collections.shuffle(_imageList);
        }

        List<MemeData.Image> hiddenImages = new ArrayList<>();
        for (MemeData.Image image : _imageList) {
            if (_app.settings.isHidden(image.fullPath.getAbsolutePath())) {
                hiddenImages.add(image);
            }
        }
        _imageList.removeAll(hiddenImages);
        _recyclerMemeAdapter.setOriginalImageDataList(_imageList);
        _recyclerMemeAdapter.notifyDataSetChanged();
        setRecyclerMemeListAdapter(_recyclerMemeAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_meme, container, false);
        _unbinder = ButterKnife.bind(this, root);


        _recyclerMemeList.setHasFixedSize(true);
        _recyclerMemeList.setItemViewCacheSize(_app.settings.getGridColumnCountPortrait() * _app.settings.getGridColumnCountLandscape() * 2);
        _recyclerMemeList.setDrawingCacheEnabled(true);
        _recyclerMemeList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        _recyclerMemeList.addItemDecoration(new GridDecoration(1.7f));

        int a = AppSettings.get().getMemeListViewType();
        if (AppSettings.get().getMemeListViewType() == MemeItemAdapter.VIEW_TYPE__ROWS_WITH_TITLE) {
            RecyclerView.LayoutManager recyclerLinearLayout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            _recyclerMemeList.setLayoutManager(recyclerLinearLayout);
        } else {
            int gridColumns = ContextUtils.get().isInPortraitMode()
                    ? _app.settings.getGridColumnCountPortrait()
                    : _app.settings.getGridColumnCountLandscape();
            RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(getActivity(), gridColumns);

            _recyclerMemeList.setLayoutManager(recyclerGridLayout);
        }

        _emptylistText.setText(getString(R.string.no_custom_templates_description__appspecific, getString(R.string.custom_templates_visual)));
        _recyclerMemeAdapter = new MemeItemAdapter(_imageList, getActivity(), AppSettings.get().getMemeListViewType());
        setRecyclerMemeListAdapter(_recyclerMemeAdapter);

        return root;
    }

    private void setRecyclerMemeListAdapter(MemeItemAdapter adapter) {
        adapter.setFilter("");
        _recyclerMemeList.setAdapter(adapter);
        boolean isEmpty = adapter.getItemCount() == 0;
        _emptylistLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        _recyclerMemeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private BroadcastReceiver _localBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AppCast.ASSETS_LOADED.ACTION: {
                    reloadAdapter();
                    return;
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
        reloadAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_localBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_unbinder != null) {
            _unbinder.unbind();
        }
    }


}
