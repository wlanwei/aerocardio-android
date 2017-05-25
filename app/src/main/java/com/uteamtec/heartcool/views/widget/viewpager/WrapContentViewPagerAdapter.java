package com.uteamtec.heartcool.views.widget.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

public abstract class WrapContentViewPagerAdapter extends FragmentPagerAdapter {

    private SparseArray<Fragment> fragments = new SparseArray<>();

    public WrapContentViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public abstract Fragment getFragment(int position);

    @Override
    public Fragment getItem(int position) {
        Fragment f = getFragment(position);
        if (f != null) {
            fragments.put(position, f);
        }
        return f;
    }

}
