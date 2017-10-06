package io.github.gsantner.memetastic.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.data.MemeData;
import io.github.gsantner.memetastic.ui.GridDecoration;
import io.github.gsantner.memetastic.ui.MemeItemAdapter;
import io.github.gsantner.memetastic.util.AppSettings;
import io.github.gsantner.memetastic.util.ContextUtils;


public class MemeFragment extends Fragment {
    @BindView(R.id.main__fragment__recycler_view)
    RecyclerView _recyclerMemeList;

    @BindView(R.id.main__activity__list_empty__layout)
    LinearLayout _emptylistLayout;

    @BindView(R.id.main__activity__list_empty__text)
    TextView _emptylistText;

    App app;
    int position;
    String[] _tagKeys, _tagValues;
    private boolean _areTabsReady;



    private Unbinder unbinder;
    private List<MemeData.Image> imageList;


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

        app = (App) getActivity().getApplication();
        position=getArguments().getInt("pos");

        _tagKeys = getResources().getStringArray(R.array.meme_tags__keys);
        _tagValues = getResources().getStringArray(R.array.meme_tags__titles);

        int tabPos = position;
         imageList = new ArrayList<>();

        if (tabPos >= 0 && tabPos < _tagKeys.length) {
            imageList = MemeData.getImagesWithTag(_tagKeys[tabPos]);
        }

        app.settings.setLastSelectedTab(tabPos);

        if (app.settings.isShuffleTagLists()) {
            Collections.shuffle(imageList);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        // Bind UI

        unbinder=ButterKnife.bind(this,view);

        _emptylistText.setText(getString(R.string.main__nodata__custom_templates, getString(R.string.custom_templates_visual)));


        _recyclerMemeList.setHasFixedSize(true);
        _recyclerMemeList.setItemViewCacheSize(app.settings.getGridColumnCountPortrait() * app.settings.getGridColumnCountLandscape() * 2);
        _recyclerMemeList.setDrawingCacheEnabled(true);
        _recyclerMemeList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        _recyclerMemeList.addItemDecoration(new GridDecoration(1.7f));

        if (AppSettings.get().getMemeListViewType() == MemeItemAdapter.VIEW_TYPE__ROWS_WITH_TITLE) {
            RecyclerView.LayoutManager recyclerLinearLayout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            _recyclerMemeList.setLayoutManager(recyclerLinearLayout);
        } else {
            int gridColumns = ContextUtils.get().isInPortraitMode()
                    ? app.settings.getGridColumnCountPortrait()
                    : app.settings.getGridColumnCountLandscape();
            RecyclerView.LayoutManager recyclerGridLayout = new GridLayoutManager(getActivity(), gridColumns);

            _recyclerMemeList.setLayoutManager(recyclerGridLayout);
        }

        MemeItemAdapter recyclerMemeAdapter = new MemeItemAdapter(imageList, getActivity(), AppSettings.get().getMemeListViewType());

        setRecyclerMemeListAdapter(recyclerMemeAdapter);

        return view;
    }

    private void setRecyclerMemeListAdapter(MemeItemAdapter adapter) {
        adapter.setFilter("");
        _recyclerMemeList.setAdapter(adapter);
        boolean isEmpty = adapter.getItemCount() == 0;
        _emptylistLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        _recyclerMemeList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }


}
