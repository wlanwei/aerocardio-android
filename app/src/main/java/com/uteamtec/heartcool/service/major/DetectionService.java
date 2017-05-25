package com.uteamtec.heartcool.service.major;

import android.util.Log;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.db.DBEcg;
import com.uteamtec.heartcool.service.db.DBEcgMark;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.stats.EcgMarkReport;
import com.uteamtec.heartcool.service.stats.EcgStats;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 核心-监测记录服务
 * Created by wd
 */
public final class DetectionService {

    private static final String TAG = DetectionService.class.getSimpleName();

    private EcgStats stats;
    private DBDetection detection;

    private DetectionService() {
        stats = new EcgStats();
        detection = new DBDetection(User.getUser().getIdString(),
                BleFeComm.getClient().getMacAddress());
    }

    private void reset() {
        stats.clear();
        detection = new DBDetection(User.getUser().getIdString(),
                BleFeComm.getClient().getMacAddress());
    }

    public EcgMarkReport analyze() {
        EcgMarkReport report = null;
        if (stats.analyze()) {
            report = stats.getReport();
            detection.setEcgMarkReport(report);
        }
        return report;
    }

    private DBDetection save() {
        Map<EcgMark, ArrayList<Ecg>> map = stats.result();
        if (map != null && map.size() > 0) {
            Iterator<EcgMark> i = map.keySet().iterator();
            while (i.hasNext()) {
                EcgMark k = i.next();
                DBEcgMark m = new DBEcgMark(k);
                for (Ecg e : map.get(k)) {
                    m.addEcg(new DBEcg(e));
                }
                Log.e(TAG, "EcgStats save DBEcgMark: " + m.toString());
                detection.addMark(m);
            }
        }
        Log.e(TAG, "EcgStats save DBDetection: " + detection.toString());
        detection.save();
        return detection;
    }

    private static DetectionService _instance;
    private static volatile boolean _isRunning = false;
    private static volatile boolean _isProcessing = false;

    private synchronized static boolean isRunning() {
        return _isRunning && !_isProcessing;
    }

    private synchronized static boolean isProcessing() {
        return _isRunning || _isProcessing;
    }

    public static boolean isRecording() {
        return isRunning();
    }

    public static void init() {
        if (_instance == null) {
            synchronized (DetectionService.class) {
                if (_instance == null) {
                    _instance = new DetectionService();
                }
            }
        }
    }

    /**
     * 开始记录
     */
    public static void startRecord() {
        if (isProcessing()) {
            return;
        }
        Log.e(TAG, "startRecord");
        resetRecord();
    }

    /**
     * 重置记录
     */
    public static void resetRecord() {
        Log.e(TAG, "resetRecord");
        synchronized (DetectionService.class) {
            if (_instance == null) {
                _instance = new DetectionService();
            } else {
                _instance.reset();
            }
            _instance.stats.startRecord();
            _isRunning = true;
        }
        if (ListenerMgr.hasDetectionListener()) {
            ListenerMgr.getDetectionListener().onStart();
        }
    }

    /**
     * 结束记录
     */
    public static void stopRecord() {
        Log.e(TAG, "stopRecord");
        if (_instance != null) {
            synchronized (DetectionService.class) {
                if (_instance != null) {
                    _instance.stats.stopRecord();
                    _instance.detection.setDuration(_instance.stats.
                            getAnalyzer().getSeconds() * 1000);
                }
            }
        }
        _isRunning = false;
        if (ListenerMgr.hasDetectionListener()) {
            ListenerMgr.getDetectionListener().onStop();
        }
    }

    /**
     * 分析记录
     */
    public static void analyzeRecord() {
        Log.e(TAG, "analyzeRecord start...");
        if (!isProcessing() && _instance != null) {
            _isProcessing = true;
            EcgMarkReport report = _instance.analyze();
            _isProcessing = false;
            if (report != null && ListenerMgr.hasDetectionListener()) {
                ListenerMgr.getDetectionListener().onAnalyze(report);
            }
        }
        Log.e(TAG, "analyzeRecord finish");
    }

    /**
     * 保存记录
     */
    public static void saveRecord() {
        Log.e(TAG, "saveRecord start...");
        if (isProcessing()) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                if (!isProcessing() && _instance != null) {
                    _isProcessing = true;
                    DBDetection d = _instance.save();
                    _isProcessing = false;
                    if (ListenerMgr.hasDetectionListener()) {
                        ListenerMgr.getDetectionListener().onSave(d);
                    }
                }
                Log.e(TAG, "saveRecord finish");
            }
        }.start();
    }

    /**
     * 记录Ecg
     */
    public static void recordEcg(Ecg e) {
        if (e != null && isRunning() && _instance != null) {
            _instance.stats.recordEcg(e);
        }
    }

    /**
     * 记录EcgMark
     */
    public static void recordMark(EcgMark m) {
        if (m != null && isRunning() && _instance != null) {
            _instance.stats.recordMark(m);
        }
    }

    /**
     * 显示EcgMark
     */
    public static void displayMark(EcgMark m) {
        if (m != null && _instance != null) {
            _instance.stats.displayMark(m);
        }
    }

}
