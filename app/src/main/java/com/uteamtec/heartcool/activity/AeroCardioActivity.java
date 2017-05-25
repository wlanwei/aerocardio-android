package com.uteamtec.heartcool.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.uteamtec.heartcool.fragment.PlaceholderAeroCardioFragment;
import com.uteamtec.heartcool.fragment.PlaceholderHistoryFragment;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.db.DBOrm;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.MobclickEvent;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevices;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.views.widget.viewpager.WrapContentViewPager;
import com.uteamtec.heartcool.views.widget.viewpager.WrapContentViewPagerAdapter;

import java.util.Date;

/**
 * 主要监测页面
 * Created by wd
 */
public class AeroCardioActivity extends BaseAppCompatActivity
        implements View.OnClickListener, ViewPager.OnPageChangeListener {


    private TextView toolbar_title;
    private TextView toolbar_subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerocardio);

        if (User.getUser().hasPrevUserDevice()) {
            AppNetTcpComm.getInfo().validateUserInfoAndDevice(
                    User.getUser().getIdString(),
                    User.getUser().getPrevUserDevice().getMacAddr(),
                    new AppNetTcpCommListener<UserDevices>() {
                        @Override
                        public void onResponse(boolean success, UserDevices response) {
                            L.e("validateUserInfoAndDevice -> success: " + success);
                            if (success && response != null) {
                                L.e("validateUserInfoAndDevice -> response:" + response.toString());
                            } else {
                                gotoSetting();
                            }
                        }
                    });
        }
    }

    @Override
    protected void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.aerocardio_toolbar);
        toolbar_title = (TextView) findViewById(R.id.aerocardio_toolbar_tx_title);
        toolbar_subtitle = (TextView) findViewById(R.id.aerocardio_toolbar_tx_subtitle);
        setSupportActionBar(toolbar);

        toolbar_title.setText(getString(R.string.testToday));

        WrapContentViewPager viewPager = (WrapContentViewPager) findViewById(R.id.aerocardio_container);
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
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
            case R.id.action_settings_about:
                gotoAbout();
                return true;
            case R.id.action_settings_reconnect_device:
                BleFeComm.getClient().reconnect();
                return true;
            case R.id.action_settings_change_device:
                AlertDialog.Builder dialog_change_device = new AlertDialog.Builder(this);
                dialog_change_device.setTitle(getResources().getString(R.string.aerocardio_setting_confirm_title));
                dialog_change_device.setMessage(getResources().getString(R.string.aerocardio_setting_confirm_content));
                dialog_change_device.setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                gotoSetting();
                            }
                        });
                dialog_change_device.setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog_change_device.create().show();
                return true;
            case R.id.action_settings_clear_db:
                DBOrm.clean();
                return true;
            case R.id.action_settings_personal:
                gotoPersonal();
                return true;
            case R.id.action_settings_logout:
                AlertDialog.Builder dialog_logout = new AlertDialog.Builder(this);
                dialog_logout.setTitle(getResources().getString(R.string.logout));
                dialog_logout.setMessage(getResources().getString(R.string.logout_ask));
                dialog_logout.setPositiveButton(getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                                MobclickEvent.onEvent(AeroCardioActivity.this,
                                        MobclickEvent.EventId_UserSignOut);
                                User.getUser().clear();
                                User.getUser().reset();
                                gotoLogin();
                            }
                        });
                dialog_logout.setNegativeButton(getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog_logout.create().show();
                return true;
            case R.id.action_settings_login:
                gotoLogin();
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
        if (PlaceholderAeroCardioFragment.getInstance() != null) {
            PlaceholderAeroCardioFragment.getInstance().onServiceConnected();
        }
        if (PlaceholderHistoryFragment.getInstance() != null) {
            PlaceholderHistoryFragment.getInstance().onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        if (PlaceholderAeroCardioFragment.getInstance() != null) {
            PlaceholderAeroCardioFragment.getInstance().onServiceDisconnected();
        }
        if (PlaceholderHistoryFragment.getInstance() != null) {
            PlaceholderHistoryFragment.getInstance().onServiceDisconnected();
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
        if (PlaceholderAeroCardioFragment.getInstance() != null) {
            if (position == 0) {
                PlaceholderAeroCardioFragment.getInstance().startDraw();
            } else {
                PlaceholderAeroCardioFragment.getInstance().stopDraw();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private final class SectionsPagerAdapter extends WrapContentViewPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(int position) {
            switch (position) {
                case 0:
                    return PlaceholderAeroCardioFragment.newInstance(new BaseFragment.BaseFragmentListener() {
                        @Override
                        public void onService() {
                            if (isBindMainService() && PlaceholderAeroCardioFragment.getInstance() != null) {
                                PlaceholderAeroCardioFragment.getInstance().onServiceConnected();
                            }
                        }
                    });
                case 1:
                    return PlaceholderHistoryFragment.newInstance(new BaseFragment.BaseFragmentListener() {
                        @Override
                        public void onService() {
                            if (isBindMainService() && PlaceholderHistoryFragment.getInstance() != null) {
                                PlaceholderHistoryFragment.getInstance().onServiceConnected();
                            }
                        }
                    });
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.testToday);
                case 1:
                    return getString(R.string.testRecorder);
            }
            return null;
        }
    }

    private void gotoAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void gotoSetting() {
        BleFeComm.getClient().disconnect();
        startActivity(new Intent(this, AeroCardioSettingActivity.class));
        finish();
    }

    private void gotoPersonal() {
        BleFeComm.getClient().disconnect();
        startActivity(new Intent(this, AeroCardioPersonalActivity.class));
    }

    private void gotoLogin() {
        BleFeComm.getClient().disconnect();
        startActivity(new Intent(this, AeroCardioLoginActivity.class));
        finish();
    }

}
