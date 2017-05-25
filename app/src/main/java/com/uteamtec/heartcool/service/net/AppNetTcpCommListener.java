package com.uteamtec.heartcool.service.net;

/**
 * Created by wd
 */
public interface AppNetTcpCommListener<T> {
    void onResponse(boolean success, T response);
}
