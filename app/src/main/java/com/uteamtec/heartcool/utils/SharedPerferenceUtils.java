package com.uteamtec.heartcool.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by lusuo on 2015/10/20.
 */
public class SharedPerferenceUtils {
    public static final String DEV_MAC = "dev_mac";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    /**
     * 文件放在该包下
     */
    public SharedPerferenceUtils(Context context) {
        super();
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // 设置数据
    public void setPreferenceValues(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }

    // 获取数据
    public String getPreferenceValues(String key){
        return sharedPreferences.getString(key, null);
    }

    public Set<String> getPreferenceValueSet(String key) {
        return sharedPreferences.getStringSet(key, null);
    }

    public void setPreferenceValueSet(String key, Set<String> values) {
        editor.putStringSet(key, values);
        editor.commit();
    }

    // 获取数据
    public String getPreferenceValues(String key, String defult){
        return sharedPreferences.getString(key, defult);
    }


}
