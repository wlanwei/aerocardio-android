package com.uteamtec.heartcool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.uteamtec.heartcool.utils.ActivityStack;
import com.uteamtec.heartcool.utils.CrashHandler;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStack.addActivity(this);
        // 传入参数必须为Activity，否则AlertDialog将不显示。
        CrashHandler.getInstance().init(this);
    }

    /*初始化activity*/
    protected void onCreated(int layout) {
        //设置无标题
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(layout);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        try {
            findView();
            init();
            bindListener();
        } catch (Exception e) {
        }
    }

    // 初始化
    protected abstract void init();

    // 获取控件
    protected abstract void findView();

    // 监听事件
    protected abstract void bindListener();

    public abstract void unregisterService();

    // 退出应用
    protected void exitApp() {
        ActivityStack.exitApp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.removeActivity(this);
    }
}
