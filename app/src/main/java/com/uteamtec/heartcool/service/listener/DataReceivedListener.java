package com.uteamtec.heartcool.service.listener;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.type.EcgMark;

/**
 * 数据接收监听接口
 * Created by wd
 */
public interface DataReceivedListener {

    void onReceivedEcgRaw(Ecg ecg); //收到原始数据

    void onReceivedEcgFiltered(Ecg ecg); //收到滤波数据

    void onReceivedMark(EcgMark mark); //收到标记数据

    void onInfo(String info); //调试接口
}
