package com.uteamtec.heartcool.service.ble;

import com.uteamtec.heartcool.messages.FeMessage;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;

/**
 * 蓝牙脉冲输出
 * Created by wd
 */
public final class BleFePulseTxThread extends Thread {

    private static BleFePulseTxThread singleton;

    public static void startThread() {
        BleFeTxQueue.clear();
        if (singleton == null) {
            synchronized (BleFePulseTxThread.class) {
                if (singleton == null) {
                    singleton = new BleFePulseTxThread();
                    singleton.start();
                }
            }
        } else {
            singleton.reset();
        }
    }

    public static void stopThread() {
        if (singleton != null) {
            synchronized (BleFePulseTxThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
        BleFeTxQueue.clear();
    }

    private boolean _enabled = true;
    private int _cnt = 0;

    private BleFePulseTxThread() {
        _enabled = true;
        _cnt = 0;
    }

    private void reset() {
        _cnt = 0;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (_enabled) {
            if (_cnt == 0 && !User.getUser().getIsDevReset()) {
                L.e("<BLE> W Reset");
                BleFeTxQueue.put(FeMessage.createResetMsg());
            }
            BleFeTxQueue.put(FeMessage.createPulseMsg());
            if (++_cnt >= 3) {
                _cnt = 0;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
