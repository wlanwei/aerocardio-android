package com.uteamtec.heartcool.service.type;

import android.content.Context;
import android.content.SharedPreferences;

import com.uteamtec.heartcool.AeroCardioApp;

import java.util.Set;

/**
 * Created by wd
 */
public final class Config {

    public static final String Default = "default";
    public static final String Info = "info";
    public static final String User = "user";
    public static final String Device = "device";
    public static final String Personal = "personal";

    protected static final String PREF_USER_NAME = "USER_NAME";
    protected static final String PREF_USER_PASSWORD = "USER_PASSWORD";
    protected static final String PREF_USER_ID = "USER_ID";
    protected static final String PREF_USER_KEY = "USER_KEY";
    protected static final String PREF_PREV_DEVICE = "PREV_DEVICE";
    protected static final String PREF_BOUNDED_DEVICES = "BOUNDED_DEVICE_INFO";

    public static final String PREF_APP_FIRST = "APP_FIRST";

    public static final String PREF_LOGIN_REMEMBER = "LOGIN_REMEMBER";
    public static final String PREF_LOGIN_AUTO = "LOGIN_AUTO";
    public static final String PREF_LOGIN_SUCCESS = "LOGIN_SUCCESS";

    private Config() {
    }

    private static SharedPreferences getSharedPreferences(String type) {
        switch (type) {
            case Info:
                return AeroCardioApp.getApplication().
                        getSharedPreferences(Info, Context.MODE_PRIVATE);
            case Personal:
                return AeroCardioApp.getApplication().
                        getSharedPreferences(Personal, Context.MODE_PRIVATE);
            case User:
                return AeroCardioApp.getApplication().
                        getSharedPreferences(User, Context.MODE_PRIVATE);
            case Device:
                return AeroCardioApp.getApplication().
                        getSharedPreferences(Device, Context.MODE_PRIVATE);
            default:
                return AeroCardioApp.getApplication().
                        getSharedPreferences(Default, Context.MODE_PRIVATE);
        }
    }

    public static String getString(String type, String key, String value) {
        return getSharedPreferences(type).getString(key, value);
    }

    public static boolean putString(String type, String key, String value) {
        return getSharedPreferences(type).edit().putString(key, value).commit();
    }

    public static Set<String> getStringSet(String type, String key, Set<String> value) {
        return getSharedPreferences(type).getStringSet(key, value);
    }

    public static boolean putStringSet(String type, String key, Set<String> value) {
        return getSharedPreferences(type).edit().putStringSet(key, value).commit();
    }

    public static boolean getBoolean(String type, String key, boolean value) {
        return getSharedPreferences(type).getBoolean(key, value);
    }

    public static boolean putBoolean(String type, String key, boolean value) {
        return getSharedPreferences(type).edit().putBoolean(key, value).commit();
    }

    public static int getInt(String type, String key, int value) {
        return getSharedPreferences(type).getInt(key, value);
    }

    public static boolean putInt(String type, String key, int value) {
        return getSharedPreferences(type).edit().putInt(key, value).commit();
    }

}
