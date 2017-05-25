package com.uteamtec.heartcool.service.listener;

import com.uteamtec.heartcool.service.type.UserDevice;

/**
 * 用户接口
 * Created by wd
 */
public interface UserStateChangedListener {

    void onDeviceRegistered(UserDevice device, int regResult); //设备注册

    void onDeviceActivated(UserDevice device, int activateResult); //设备激活

    void onLogin(int loginResult); //用户登录结果

    void onAppStateChanged(int state);

    void onFeStateChanged(int state);
}
