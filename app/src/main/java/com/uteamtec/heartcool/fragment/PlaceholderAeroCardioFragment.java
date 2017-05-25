package com.uteamtec.heartcool.fragment;

import android.os.Bundle;

/**
 * Created by wd
 */
public final class PlaceholderAeroCardioFragment extends AeroCardioFragment {

    private static PlaceholderAeroCardioFragment fragment;

    public PlaceholderAeroCardioFragment() {
        super();
    }

    public static PlaceholderAeroCardioFragment newInstance(BaseFragmentListener listener) {
        fragment = new PlaceholderAeroCardioFragment();
        fragment.setListener(listener);
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public static PlaceholderAeroCardioFragment getInstance() {
        return fragment;
    }

}
