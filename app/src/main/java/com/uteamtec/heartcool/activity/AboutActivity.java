package com.uteamtec.heartcool.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.uteamtec.heartcool.R;

import java.util.Locale;

/**
 * 关于页面
 * Created by wd
 */
public class AboutActivity extends BaseAppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void initViews() {
        findViewById(R.id.activity_about_ll_return).setOnClickListener(this);

        try {
            PackageManager packageManager = getPackageManager();// 获取app包名服务
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            TextView.class.cast(findViewById(R.id.activity_about_tv_version)).setText(
                    String.format(Locale.getDefault(), "%s%s",
                            getString(R.string.version), packageInfo.versionName));
            TextView.class.cast(findViewById(R.id.activity_about_tv_version_num)).setText(
                    String.format(Locale.getDefault(), "%s%d",
                            getString(R.string.version_num), packageInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean enableCatchCrash() {
        return false;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_about_ll_return:
                finish();
                break;
        }
    }
}
