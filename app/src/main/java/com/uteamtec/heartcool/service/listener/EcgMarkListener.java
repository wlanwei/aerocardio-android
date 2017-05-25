package com.uteamtec.heartcool.service.listener;

/**
 * Created by wd
 */
public interface EcgMarkListener {

    void onMarkUpdated();

    void onMarkLeadOff(String msg);

    void onMarkLowPower(String msg);

    void onMarkShort(String msg);

    void onMarkUnplug(String msg);

    void onMarkHR(int hr, boolean hrWarn,
                  int hrAverage,
                  int hrHealth, boolean healthWarn);

    void onMarkBR(int br);

    void onMarkNoise(String msg);

}
