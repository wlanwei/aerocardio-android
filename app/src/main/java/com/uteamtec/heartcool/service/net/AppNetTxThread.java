package com.uteamtec.heartcool.service.net;

import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.messages.AppMessageCoder;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;

/**
 * Created by wd
 */
public final class AppNetTxThread extends Thread {

    private static AppNetTxThread singleton;

    synchronized public static void startThread() {
        AppNetTxQueue.clear();
        AppNetTxCache.StartThread();
        if (singleton == null) {
            synchronized (AppNetTxThread.class) {
                if (singleton == null) {
                    singleton = new AppNetTxThread();
                    singleton.start();
                }
            }
        }
    }

    synchronized public static void stopThread() {
        if (singleton != null) {
            synchronized (AppNetTxThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
        AppNetTxQueue.clear();
        AppNetTxCache.StopThread();
    }

    private boolean _enabled = true;

    private AppNetTxThread() {
        _enabled = true;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        AppMessage msg;
        while (_enabled) {
            if (User.getUser().getAppState() != User.APPSTATE_LOGIN &&
                    User.getUser().getAppState() != User.APPSTATE_CONNECTED) {
                L.e("AppNetTxThread.Pass: " + User.getUser().getAppState());
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (System.currentTimeMillis() > pulseTime) {
                AppNetTxQueue.put(AppMessage.createPulseMessage());
                sendPulse();
            }
            if (!User.getUser().isConnectedDeviceAppNet() &&
                    !AppNetTxCache.hasPriority()) {
                L.e("AppNetTxThread.NotConnect...");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            msg = AppNetTxCache.poll();
            if (msg == null) {
                continue;
            }
//            L.e("AppNetTxThread.State: " + User.getUser().getAppState() +
//                    " Type: " + msg.getType());
            if (User.getUser().getAppState() == User.APPSTATE_CONNECTED &&
                    msg.getType() != AppMessage.TYPE_LOGIN) {
                continue;
            }
            L.e("AppNetTxThread.Send Type: " + msg.getType());
            if (!AppNetTcpComm.send(AppMessageCoder.encode(msg))) {
                L.e("AppNetTxThread.Send Failed!");
            }
        }
    }

    private static volatile long pulseTime = 0;

    public static void sendPulse() {
        pulseTime = System.currentTimeMillis() + 2000;
    }

}
