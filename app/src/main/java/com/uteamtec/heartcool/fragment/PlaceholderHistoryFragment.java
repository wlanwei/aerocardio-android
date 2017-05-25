package com.uteamtec.heartcool.fragment;

import android.os.Bundle;

/**
 * Created by wd
 */
public final class PlaceholderHistoryFragment extends HistoryFragment {

    private static PlaceholderHistoryFragment fragment;

    public PlaceholderHistoryFragment() {
        super();
    }

    public static PlaceholderHistoryFragment newInstance(BaseFragmentListener listener) {
        fragment = new PlaceholderHistoryFragment();
        fragment.setListener(listener);
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public static PlaceholderHistoryFragment getInstance() {
        return fragment;
    }

}
