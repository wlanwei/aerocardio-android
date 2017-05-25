package com.uteamtec.heartcool.service.ecg;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.MainConstant;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wd
 */
public final class EcgQueue {

    private BlockingQueue<Ecg> ecgRaw; // 原始ecg数据,采用阻塞queue
    private BlockingQueue<Ecg> ecgFiltered; // 滤波之后的ecg，采用阻塞queue
    private BlockingQueue<Ecg> ecgDisplay; // 传递到Main activity的信号波形

    private EcgQueue() {
        ecgRaw = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
        ecgFiltered = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
        ecgDisplay = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
    }

    private static EcgQueue _queue;

    private static EcgQueue getQueue() {
        if (_queue == null) {
            synchronized (EcgQueue.class) {
                if (_queue == null) {
                    _queue = new EcgQueue();
                }
            }
        }
        return _queue;
    }

    public static BlockingQueue<Ecg> getRaw() {
        return getQueue().ecgRaw;
    }

    synchronized protected static void resetRaw() {
        getQueue().ecgRaw.clear();
        getQueue().ecgRaw = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
    }

    public static BlockingQueue<Ecg> getFiltered() {
        return getQueue().ecgFiltered;
    }

    synchronized protected static void resetFiltered() {
        getQueue().ecgFiltered.clear();
        getQueue().ecgFiltered = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
    }

    public static BlockingQueue<Ecg> getDisplay() {
        return getQueue().ecgDisplay;
    }

}
