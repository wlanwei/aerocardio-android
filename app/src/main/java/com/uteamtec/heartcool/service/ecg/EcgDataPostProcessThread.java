package com.uteamtec.heartcool.service.ecg;

import com.uteamtec.algorithm.dsp.DspEngineEx;
import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.service.major.DetectionService;
import com.uteamtec.heartcool.service.net.AppNetTxQueue;
import com.uteamtec.heartcool.service.type.User;

/**
 * Created by wd
 */
public final class EcgDataPostProcessThread extends Thread {

    private static EcgDataPostProcessThread singleton;

    public static void startThread() {
        stopThread();
        EcgQueue.resetRaw();
        EcgQueue.resetFiltered();
        EcgMarkQueue.resetQueue();
        synchronized (EcgDataPostProcessThread.class) {
            singleton = new EcgDataPostProcessThread();
            singleton.start();
        }
    }

    public static void stopThread() {
        if (singleton != null) {
            synchronized (EcgDataPostProcessThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
        EcgQueue.getFiltered().clear();
        EcgQueue.getRaw().clear();
    }

    public static void setResolution(int resolution) {
        if (singleton != null) {
            synchronized (EcgDataPostProcessThread.class) {
                if (singleton != null) {
                    singleton.resetResolution(resolution);
                }
            }
        }
    }

    private boolean _enabled = true;
    private DspEngineEx dspEngineEx = null; // 数据处理引擎
    private int resolution = -1;

    private EcgDataPostProcessThread() {
        _enabled = true;
        dspEngineEx = new DspEngineEx(EcgQueue.getRaw(), EcgQueue.getFiltered(),
                EcgMarkQueue.getQueue());
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    private void resetResolution(int resolution) {
        this.resolution = resolution;
    }

    @Override
    public void run() {
        Ecg ecg;
        while (_enabled) {
            try {
                while (_enabled) {
                    if (dspEngineEx == null) {
                        ecg = EcgQueue.getRaw().take();
                    } else {
                        ecg = EcgQueue.getFiltered().take();
                    }
                    if (ecg == null) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    // debug code, if connected device is null, should not start postprocess
                    if (!User.getUser().hasUserDevice() || resolution <= 0) {
                        continue;
                    }

                    // interrupt ecg
                    if (User.getUser().isInterruptAppNetEcg()) {
                        continue;
                    }

                    // add sps information for AppMessage transmission
                    ecg.setSps(User.getUser().getUserDevice().getSps());

                    if (resolution > 0 && DetectionService.isRecording()) {
                        // For ordinary device
                        AppNetTxQueue.put(AppMessage.createEcgMessage(ecg, resolution));
                    }

                    // display ecg
                    EcgQueue.getDisplay().put(ecg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}