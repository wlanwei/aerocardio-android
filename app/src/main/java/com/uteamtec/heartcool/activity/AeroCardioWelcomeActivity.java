package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.Config;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.utils.L;

/**
 * 欢迎页面
 * Created by wd
 */
public class AeroCardioWelcomeActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (false) { // 测试
            gotoNetTest();
            return;
        }

        setContentView(R.layout.activity_aerocardio_welcome);
    }

    @Override
    protected void initViews() {
        // 默认是第一次进入应用
        if (Config.getBoolean(Config.Info, Config.PREF_APP_FIRST, true)) {
            Config.putBoolean(Config.Info, Config.PREF_APP_FIRST, false);
        }

        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (User.getUser().getId() != null) {
                    autoLogin();
                } else {
                    gotoLogin();
                }
                return false;
            }
        }).sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return false;
    }

    @Override
    protected boolean enableServiceConnection() {
        return false;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    private void autoLogin() {
        if (User.getUser().hasPrevUserDevice()) {
            gotoMain();
        } else {
            AppNetTcpComm.getInfo().queryBindDeviceByInfoId(
                    User.getUser().getIdString(),
                    new AppNetTcpCommListener<UserDevice>() {
                        @Override
                        public void onResponse(boolean success, UserDevice response) {
                            L.e("queryBindDeviceByInfoId -> success: " + success);
                            if (success && response != null) {
                                L.e("queryBindDeviceByInfoId -> response:" + response.toString());
                                User.getUser().updateUserDevice(response);
                                gotoMain();
                            } else {
                                gotoSetting();
                            }
                        }
                    });
        }
    }

    private void gotoNetTest() {
        startActivity(new Intent(this, TestAppNetActivity.class));
        finish();
    }

    private void gotoLogin() {
        // 设置是否自动登录
        Config.putBoolean(Config.Info, Config.PREF_LOGIN_AUTO,
                !TextUtils.isEmpty(User.getUser().getPassword()));

        startActivity(new Intent(this, AeroCardioLoginActivity.class));
        finish();
    }

    private void gotoMain() {
        startActivity(new Intent(this, AeroCardioActivity.class));
        this.finish();
    }

    private void gotoSetting() {
        startActivity(new Intent(this, AeroCardioSettingActivity.class));
        this.finish();
    }

}
