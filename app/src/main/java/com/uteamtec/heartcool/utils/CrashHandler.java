package com.uteamtec.heartcool.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Global exception handler
 * Created by liulingfeng on 2015/9/22.
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    //CrashHandler实例
    private static CrashHandler instance;

    //程序的Context对象
    private Context mContext;
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex) {
        if (ex == null) {
            return;
        }
        MobclickAgent.reportError(mContext, ex);
        L.e("CrashHandler.uncaughtException: " + get(ex));
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            new Thread() {
                @Override
                public void run() {
                    if (mContext == null) {
                        return;
                    }
                    Looper.prepare();
                    new AlertDialog.Builder(mContext).setTitle("告诉你一个不幸的消息")
                            .setCancelable(false)
                            .setMessage("程序崩溃了..." + get(ex))
                            .setNeutralButton("我知道了", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MobclickAgent.onKillProcess(mContext);
                                    ActivityStack.exitApp();
                                }
                            }).create().show();
                    Looper.loop();
                }
            }.start();
        }
    }

    private String get(Throwable exception) {
        PrintWriter pw = null;
        try {
            StringWriter sw = new StringWriter();
            pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            Log.getStackTraceString(exception);
            return sw.toString();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

}
