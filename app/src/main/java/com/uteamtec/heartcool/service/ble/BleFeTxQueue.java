package com.uteamtec.heartcool.service.ble;

import com.uteamtec.heartcool.MainConstant;
import com.uteamtec.heartcool.messages.FeMessage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wd
 */
public final class BleFeTxQueue {

    private BlockingQueue<FeMessage> queue;

    private BleFeTxQueue() {
        queue = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
    }

    private static BleFeTxQueue _queue;

    private static BlockingQueue<FeMessage> getQueue() {
        if (_queue == null) {
            synchronized (BleFeTxQueue.class) {
                if (_queue == null) {
                    _queue = new BleFeTxQueue();
                }
            }
        }
        return _queue.queue;
    }

    public static boolean add(FeMessage msg) {
        return msg != null && getQueue().add(msg);
    }

    public static boolean put(FeMessage msg) {
        if (msg != null) {
            try {
                getQueue().put(msg);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static FeMessage take() {
        try {
            return getQueue().take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clear() {
        getQueue().clear();
    }

}
