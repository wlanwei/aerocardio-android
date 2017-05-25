package com.uteamtec.heartcool.service.ecg;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.major.DetectionService;
import com.uteamtec.heartcool.service.type.User;

/**
 * Created by wd
 */
public final class EcgDisplayThread extends Thread {

    private static EcgDisplayThread singleton;

    public static void startThread() {
        EcgQueue.getDisplay().clear();
        if (singleton == null) {
            synchronized (EcgDisplayThread.class) {
                if (singleton == null) {
                    singleton = new EcgDisplayThread();
                    singleton.start();
                }
            }
        }
    }

    public static void stopThread() {
        if (singleton != null) {
            synchronized (EcgDisplayThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
        EcgQueue.getDisplay().clear();
    }

    private boolean _enabled = true;

    private EcgDisplayThread() {
        _enabled = true;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (_enabled) {
            try {
                Ecg ecg = EcgQueue.getDisplay().take();
                if (User.getUser().getFeState() == User.FESTATE_REGISTERED) {
                    DetectionService.recordEcg(ecg);
                    if (ListenerMgr.getDataReceivedListener() != null) {
                        ListenerMgr.getDataReceivedListener().onReceivedEcgRaw(ecg);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}