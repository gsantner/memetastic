package io.github.gsantner.memetastic.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

/**
 * Created by AVJEET on 06-10-2017.
 */

public class MemePagerAdapter extends FragmentPagerAdapter {
    int _totalCount;
    String[] _pageTitles;


    public MemePagerAdapter(FragmentManager fm, int totalCount, String[] pageTitles) {
        super(fm);
        _totalCount = totalCount;
        _pageTitles = pageTitles;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return _pageTitles[position];
    }

    @Override
    public Fragment getItem(int i) {
        return MemeFragment.newInstance(i);
    }

    @Override
    public int getCount() {
        return _totalCount;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
