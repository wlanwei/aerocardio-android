package com.uteamtec.heartcool.service.net;

import com.uteamtec.heartcool.MainConstant;
import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.service.type.User;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by wd
 */
public final class AppNetTxQueue {

    private BlockingQueue<AppMessage> queue;

    private AppNetTxQueue() {
        queue = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
    }

    private static AppNetTxQueue _queue;

    private static BlockingQueue<AppMessage> getQueue() {
        if (_queue == null) {
            synchronized (AppNetTxQueue.class) {
                if (_queue == null) {
                    _queue = new AppNetTxQueue();
                }
            }
        }
        return _queue.queue;
    }

//    public static boolean add(AppMessage msg) {
//        return msg != null && getQueue().add(msg);
//    }

    public static boolean put(AppMessage msg) {
        if (msg != null) {
            if (msg.getType() == AppMessage.TYPE_STREAM &&
                    User.getUser().isInterruptAppNetEcg()) {
                return true;
            }
            try {
                getQueue().put(msg);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected static AppMessage poll() {
        try {
            return getQueue().poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clear() {
        getQueue().clear();
    }

}
