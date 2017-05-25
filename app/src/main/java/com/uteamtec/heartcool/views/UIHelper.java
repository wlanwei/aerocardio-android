package com.uteamtec.heartcool.views;

import android.content.Intent;

import com.uteamtec.heartcool.BaseActivity;
import com.uteamtec.heartcool.activity.AeroCardioActivity;

/**
 * 界面统一管理工具类
 */
public class UIHelper {

    public static void goLoginActivity(BaseActivity context) {
        context.unregisterService();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
        context.finish();
    }
//
//	public static void goDebugActivity(BaseActivity context){
//		context.unregisterService();
//		Intent intent = new Intent(context, DebugActivity.class);
//		context.startActivity(intent);
//		context.finish();
//	}

    public static void goSettingActivity(BaseActivity context) {
        context.unregisterService();
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
        context.finish();
    }

    public static void goMainAeroCardioActivity(BaseActivity context) {
        context.unregisterService();
        Intent intent = new Intent(context, MainAeroCardioActivity.class);
        context.startActivity(intent);
        context.finish();
    }

    public static void goAeroCardioActivity(BaseActivity context) {
        context.unregisterService();
        Intent intent = new Intent(context, AeroCardioActivity.class);
        context.startActivity(intent);
        context.finish();
    }

}
