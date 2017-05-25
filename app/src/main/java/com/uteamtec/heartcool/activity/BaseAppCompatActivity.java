package com.uteamtec.heartcool.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.uteamtec.heartcool.MainMgrService;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.utils.ActivityStack;
import com.uteamtec.heartcool.utils.CrashHandler;

public abstract class BaseAppCompatActivity extends AppCompatActivity implements ServiceConnection {

    private long BackPressedTime = 0;

    private ServiceConnection serviceConn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStack.addActivity(this);
        if (enableCatchCrash()) {
            CrashHandler.getInstance().init(this);// 传入参数必须为Activity，否则AlertDialog将不显示
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.setContentView(layoutResID);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindMainService();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.removeActivity(this);
        unbindMainService();
    }

    @Override
    public void onBackPressed() {
        if (enableBackPressedFinish()) {
            if (System.currentTimeMillis() - BackPressedTime <= 2000) {
                BackPressedTime = 0;
                finish();
                return;
            }
            Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
            BackPressedTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    protected abstract void initViews();

    protected boolean enableCatchCrash() {
        return true;
    }

    protected abstract boolean enableBackPressedFinish();

    protected abstract boolean enableServiceConnection();


    protected void bindMainService() {
        if (enableServiceConnection() && serviceConn == null) {
            serviceConn = this;
            bindService(new Intent(this, MainMgrService.class),
                    serviceConn, BIND_AUTO_CREATE);
        }
    }

    protected void unbindMainService() {
        if (serviceConn != null) {
            unbindService(serviceConn);
            serviceConn = null;
            stopService(new Intent(this, MainMgrService.class));
            onServiceDisconnected();
        }
    }

    protected boolean isBindMainService() {
        return (enableServiceConnection() && serviceConn != null);
    }

    public abstract void onServiceConnected();

    public abstract void onServiceDisconnected();

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        onServiceConnected();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        onServiceDisconnected();
    }

    /**
     * 完全退出应用
     */
    protected void exitApp() {
        ActivityStack.exitApp();
    }

}
