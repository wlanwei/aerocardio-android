package com.uteamtec.heartcool.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by wd
 */
public abstract class BaseFragment extends Fragment {

    private View rootView;

    protected View getRootView() {
        return rootView;
    }

    protected boolean hasRootView() {
        return rootView != null;
    }

    private BaseFragmentListener listener = null;

    protected void setListener(BaseFragmentListener listener) {
        this.listener = listener;
    }

    public BaseFragment() {
    }

    public BaseFragment(BaseFragmentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.rootView = inflater.inflate(onCreateViewResource(), container, false);
        initViews(this.rootView);
        return this.rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (hasRootView() && listener != null) {
            listener.onService();
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getPageName());
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getPageName());
    }

    protected abstract String getPageName();

    protected abstract int onCreateViewResource();

    protected abstract void initViews(View rootView);

    public abstract void onServiceConnected();

    public abstract void onServiceDisconnected();

    public interface BaseFragmentListener {
        void onService();
    }

}
