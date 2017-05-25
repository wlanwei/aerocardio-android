package com.uteamtec.heartcool.service.ble;

import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;

/**
 * 蓝牙业务主线程
 * Created by wd
 */
public final class BleFeThread extends Thread {

    private static BleFeThread singleton;

    public static void startThread() {
        if (singleton == null) {
            synchronized (BleFeThread.class) {
                if (singleton == null) {
                    singleton = new BleFeThread();
                    singleton.start();
                }
            }
        }
    }

    public static void stopThread() {
        if (singleton != null) {
            synchronized (BleFeThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
    }

    private boolean _enabled = true;

    private BleFeThread() {
        _enabled = true;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (_enabled) {
            //Take time break of 500 ms for every main task operation
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!_enabled) {
                break;
            }
            // FE timeout
            if (User.getUser().getFeState() == User.FESTATE_CONNECTED ||
                    User.getUser().getFeState() == User.FESTATE_REGISTERED) {
                //check if connections are alive
                if (System.currentTimeMillis() - User.getUser().getTimeLastFeMessage()
                        >= User.DEFAULT_INT_FEMSG) {
//                    BleFeComm.getClient().disconnect();
                    L.e("BleFeThread.FE timeout (failed)");
                    BleFeComm.getClient().reconnect(); //TODO: 不太清楚是不是应该disconnect()
                    continue;
                }
            }
            // FE reconnecting
            switch (User.getUser().getFeState()) {
                case User.FESTATE_DISCONNECTED:
//                    if (System.currentTimeMillis() - User.getUser().getTimeLastConnectFe() > User.DEFAULT_INT_CONNECT_FE) {
//                        L.i("<BLE> connect FE");
//                        try {
//                            Thread.sleep(200);
//                            User.getUser().setTimeLastConnectFe(System.currentTimeMillis());
//                            if (BleFeComm.getClient().isKeep()) {
//                                BleFeComm.getClient().connect();
//                            }
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    break;
                case User.FESTATE_REGISTERED:
//                    if (timeNow - UserMgr.getUser().getTimeLastRegAck()> User.DEFAULT_INT_REGACK) {
//                        UserMgr.getUser().setTimeLastRegAck(timeNow);
//                        try {
//                            feMsgTxQueue.put(FeMessage.createRegAckMsg(UserMgr.getUser().getUserDevice()));
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    break;
                default:
                    break;
            }
        }
    }

}