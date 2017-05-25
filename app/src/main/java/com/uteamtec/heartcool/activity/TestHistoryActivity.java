package com.uteamtec.heartcool.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.fragment.BaseFragment;
import com.uteamtec.heartcool.fragment.HistoryFragment;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.views.widget.viewpager.WrapContentViewPager;
import com.uteamtec.heartcool.views.widget.viewpager.WrapContentViewPagerAdapter;

import java.util.Date;

/**
 * Created by wd
 */
public class TestHistoryActivity extends BaseAppCompatActivity
        implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private Toolbar toolbar;
    private TextView toolbar_title;
    private TextView toolbar_subtitle;
    private WrapContentViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerocardio);
    }

    @Override
    protected void initViews() {
        toolbar = (Toolbar) findViewById(R.id.aerocardio_toolbar);
        toolbar_title = (TextView) findViewById(R.id.aerocardio_toolbar_tx_title);
        toolbar_subtitle = (TextView) findViewById(R.id.aerocardio_toolbar_tx_subtitle);
        setSupportActionBar(toolbar);

        toolbar_title.setText(getString(R.string.testToday));

        viewPager = (WrapContentViewPager) findViewById(R.id.aerocardio_container);
        viewPager.setAdapter(new TestHistoryActivity.SectionsPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar_subtitle.setText(DateFormats.YYYY_MM_DD.format(new Date()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings_clear_db:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.history_detail_iv_back:
                break;
            case R.id.history_detail_iv_share:
                break;
        }
    }

    @Override
    protected boolean enableServiceConnection() {
        return true;
    }

    @Override
    public void onServiceConnected() {
        if (TestHistoryActivity.PlaceholderHistoryFragment.getInstance() != null) {
            TestHistoryActivity.PlaceholderHistoryFragment.getInstance().onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        if (TestHistoryActivity.PlaceholderHistoryFragment.getInstance() != null) {
            TestHistoryActivity.PlaceholderHistoryFragment.getInstance().onServiceDisconnected();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                toolbar_title.setText(getString(R.string.testToday));
                break;
            case 1:
                toolbar_title.setText(getString(R.string.testRecorder));
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static class PlaceholderHistoryFragment extends HistoryFragment {

        private static TestHistoryActivity.PlaceholderHistoryFragment fragment;

        public PlaceholderHistoryFragment() {
            super();
        }

        public static TestHistoryActivity.PlaceholderHistoryFragment newInstance(BaseFragmentListener listener) {
            fragment = new TestHistoryActivity.PlaceholderHistoryFragment();
            fragment.setListener(listener);
            fragment.setArguments(new Bundle());
            return fragment;
        }

        public static TestHistoryActivity.PlaceholderHistoryFragment getInstance() {
            return fragment;
        }

    }

    private final class SectionsPagerAdapter extends WrapContentViewPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(int position) {
            switch (position) {
                case 0:
                    return TestHistoryActivity.PlaceholderHistoryFragment.newInstance(new BaseFragment.BaseFragmentListener() {
                        @Override
                        public void onService() {
                            if (isBindMainService() && TestHistoryActivity.PlaceholderHistoryFragment.getInstance() != null) {
                                TestHistoryActivity.PlaceholderHistoryFragment.getInstance().onServiceConnected();
                            }
                        }
                    });
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return getString(R.string.testRecorder);
            }
            return null;
        }
    }

}