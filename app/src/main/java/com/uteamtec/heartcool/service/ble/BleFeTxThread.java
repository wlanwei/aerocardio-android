package com.uteamtec.heartcool.service.ble;

import com.uteamtec.heartcool.messages.FeMessage;
import com.uteamtec.heartcool.messages.FeMessageCoder;
import com.uteamtec.heartcool.utils.L;

import org.wd.blekit.BleIOResponse;
import org.wd.blekit.BleManager;

/**
 * Fe消息队列发送线程
 * Created by wd
 */
public final class BleFeTxThread extends Thread {

    private static BleFeTxThread singleton;

    synchronized public static void startThread() {
        BleFeTxQueue.clear();
        if (singleton == null) {
            synchronized (BleFeTxThread.class) {
                if (singleton == null) {
                    singleton = new BleFeTxThread();
                    singleton.start();
                }
            }
        }
    }

    synchronized public static void stopThread() {
        if (singleton != null) {
            synchronized (BleFeTxThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
        BleFeTxQueue.clear();
    }

    private boolean _enabled = true;

    private BleFeTxThread() {
        _enabled = true;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        FeMessage msg;
        while (_enabled) {
            msg = BleFeTxQueue.take();
            if (msg != null) {
                L.e("BleFeTxThread.Write Type: " + msg.getType());
                BleManager.getClient().writeNoRsp(BleFeConstant.SERVICE_UUID, BleFeConstant.TX_UUID,
                        FeMessageCoder.encode(msg), new BleIOResponse.Write() {
                            @Override
                            public void onWriteSuccess() {
                                L.e("BleFeTxThread.Write(success)");
                            }

                            @Override
                            public void onWriteFail(String s) {
                                L.e("BleFeTxThread.Write(failed): " + s);
                            }
                        });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}