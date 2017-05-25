package com.uteamtec.heartcool.service.listener;

/**
 * Created by wd
 */
public final class ListenerMgr {

    private ListenerMgr() {
    }

    // ================================================

    private static UserStateChangedListener userStateChangedListener;

    public static UserStateChangedListener getUserStateChangedListener() {
        return userStateChangedListener;
    }

    public static void registerUserStateChangedListener(UserStateChangedListener userStateChangedListener) {
        ListenerMgr.userStateChangedListener = userStateChangedListener;
    }

    public static void unregisterUserStateChangedListener(UserStateChangedListener userStateChangedListener) {
        if (ListenerMgr.userStateChangedListener == userStateChangedListener) {
            ListenerMgr.userStateChangedListener = null;
        }
    }

    // ================================================

    private static DataReceivedListener dataReceivedListener;

    public synchronized static DataReceivedListener getDataReceivedListener() {
        return dataReceivedListener;
    }

    public synchronized static void registerDataReceivedListener(DataReceivedListener dataReceivedListener) {
        ListenerMgr.dataReceivedListener = dataReceivedListener;
    }

    public synchronized static void unregisterDataReceivedListener(DataReceivedListener dataReceivedListener) {
        if (ListenerMgr.dataReceivedListener == dataReceivedListener) {
            ListenerMgr.dataReceivedListener = null;
        }
    }

    // ================================================

    private static BleDeviceScannedListener bleDeviceScannedListener;


    public static BleDeviceScannedListener getBleDeviceScannedListener() {
        return bleDeviceScannedListener;
    }

    public static void registerBleDeviceScannedListener(BleDeviceScannedListener bleDeviceScannedListener) {
        ListenerMgr.bleDeviceScannedListener = bleDeviceScannedListener;
    }

    public static void unregisterBleDeviceScannedListener(BleDeviceScannedListener bleDeviceScannedListener) {
        if (ListenerMgr.bleDeviceScannedListener == bleDeviceScannedListener) {
            ListenerMgr.bleDeviceScannedListener = null;
        }
    }

    // ================================================

    private static EcgMarkListener ecgMarkListener = null;

    public synchronized static boolean hasEcgMarkListener() {
        return ecgMarkListener != null;
    }

    public synchronized static EcgMarkListener getEcgMarkListener() {
        return ecgMarkListener;
    }

    public synchronized static void registerEcgMarkListener(EcgMarkListener ecgMarkListener) {
        ListenerMgr.ecgMarkListener = ecgMarkListener;
    }

    public synchronized static void unregisterEcgMarkListener(EcgMarkListener ecgMarkListener) {
        if (ListenerMgr.ecgMarkListener == ecgMarkListener) {
            ListenerMgr.ecgMarkListener = null;
        }
    }

    // ================================================

    private static DetectionListener detectionListener = null;

    public synchronized static boolean hasDetectionListener() {
        return detectionListener != null;
    }

    public synchronized static DetectionListener getDetectionListener() {
        return detectionListener;
    }

    public synchronized static void registerDetectionListener(DetectionListener detectionListener) {
        ListenerMgr.detectionListener = detectionListener;
    }

    public synchronized static void unregisterDetectionListener(DetectionListener detectionListener) {
        if (ListenerMgr.detectionListener == detectionListener) {
            ListenerMgr.detectionListener = null;
        }
    }

}