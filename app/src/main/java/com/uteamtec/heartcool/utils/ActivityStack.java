package com.uteamtec.heartcool.utils;

import android.app.Activity;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * Activity管理工具
 * Rebuilding by wd
 */
public final class ActivityStack {

    // 存放所有已开启但未被销毁的activity
    private static final List<Activity> allAct = new LinkedList<>();

    private ActivityStack() {
    }

    /**
     * 添加Activity
     */
    synchronized public static void addActivity(Activity activity) {
        allAct.add(activity);
    }

    /**
     * 移除Activity
     */
    synchronized public static void removeActivity(Activity at) {
        allAct.remove(at);
    }

    /**
     * 获取最后一个Activity
     */
    synchronized public static Activity getTopActivity() {
        if (allAct.isEmpty()) {
            return null;
        }
        return allAct.get(allAct.size() - 1);
    }

    /**
     * 退出应用
     */
    synchronized public static void exitApp() {
        try {
            for (Activity activity : allAct) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * 显示Toast
     */
    synchronized public static void toast(final int resId) {
        final Activity act = getTopActivity();
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, resId, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

