package com.uteamtec.heartcool.service.listener;

import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.stats.EcgMarkReport;

/**
 * Created by wd
 */
public interface DetectionListener {

    void onStart();

    void onStop();

    void onAnalyze(EcgMarkReport report);

    void onSave(DBDetection detection);

    void onTimerTick(String hms);

}
