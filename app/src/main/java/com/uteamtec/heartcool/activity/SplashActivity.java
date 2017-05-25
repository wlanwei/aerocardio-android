package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.type.Config;

public class SplashActivity extends BaseActivity {

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Intent intent;
            // 默认是第一次进入应用
            if (Config.getBoolean(Config.Info, Config.PREF_APP_FIRST, true)) {
                intent = new Intent(SplashActivity.this, LoginsActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginsActivity.class);
//                intent = new Intent(SplashActivity.this, TestDBActivity.class);
//                intent = new Intent(SplashActivity.this, TestAppNetActivity.class);
//                intent = new Intent(SplashActivity.this, TestHistoryActivity.class);
            }

            Config.putBoolean(Config.Info, Config.PREF_APP_FIRST, false);
            Config.putBoolean(Config.Info, Config.PREF_LOGIN_AUTO, true);

            startActivity(intent);
            SplashActivity.this.finish();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.sendEmptyMessageDelayed(0, 1000);
    }
}
