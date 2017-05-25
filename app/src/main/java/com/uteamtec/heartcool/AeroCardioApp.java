
/**
 * 南京熙健 ecg 开发支持库
 * Copyright (C) 2015 mhealth365.com All rights reserved.
 * create by lc  2015年6月16日 上午9:56:01
 */
package com.uteamtec.heartcool;

import android.app.Application;
import android.content.Intent;

import com.umeng.analytics.MobclickAgent;

import org.wd.blekit.BleContext;

public class AeroCardioApp extends Application {

    private static AeroCardioApp instance;

    public static AeroCardioApp getApplication() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        BleContext.setContext(this);

        startService(new Intent(getApplicationContext(), MainMgrService.class));

        MobclickAgent.enableEncrypt(true);
        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(
                this, "58510b7cc8957608fb000138", "WD",
                MobclickAgent.EScenarioType.E_UM_NORMAL,
                true
        ));
    }

}
