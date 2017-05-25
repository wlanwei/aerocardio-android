package com.uteamtec.heartcool.service.ecg;

import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.messages.FeMessage;
import com.uteamtec.heartcool.service.ble.BleFeTxQueue;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.major.DetectionService;
import com.uteamtec.heartcool.service.net.AppNetTxQueue;
import com.uteamtec.heartcool.service.type.EcgMark;

/**
 * Created by wd
 */
public final class EcgMarkPostProcessThread extends Thread {

    private static EcgMarkPostProcessThread singleton;

    public static void startThread() {
        EcgMarkQueue.clear();
        if (singleton == null) {
            synchronized (EcgMarkPostProcessThread.class) {
                if (singleton == null) {
                    singleton = new EcgMarkPostProcessThread();
                    singleton.start();
                }
            }
        }
    }

    public static void stopThread() {
        if (singleton != null) {
            synchronized (EcgMarkPostProcessThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
        EcgMarkQueue.clear();
    }

    private boolean _enabled = true;

    private EcgMarkPostProcessThread() {
        _enabled = true;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (_enabled) {
            EcgMark mark = EcgMarkQueue.take();
            if (mark == null) {
                continue;
            }
            if (DetectionService.isRecording()) {
                AppNetTxQueue.put(AppMessage.createMarkMessage(mark));
                DetectionService.recordMark(mark);
            } else {
                DetectionService.displayMark(mark);
            }
            if (ListenerMgr.getDataReceivedListener() != null) {
                ListenerMgr.getDataReceivedListener().onReceivedMark(mark);
            }
            if (mark.getTypeGroup() == EcgMark.TYPE_GROUP_PHYSIO) {
                BleFeTxQueue.put(FeMessage.createMarkMsg(mark));
            }
        }
    }

}