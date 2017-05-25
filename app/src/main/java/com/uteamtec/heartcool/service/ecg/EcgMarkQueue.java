package com.uteamtec.heartcool.service.ecg;

import com.uteamtec.heartcool.MainConstant;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * EcgMark消息队列
 * Created by wd
 */
public final class EcgMarkQueue {

    private BlockingQueue<com.uteamtec.algorithm.types.EcgMark> marks; // 分析参数列表，采用阻塞queue

    private EcgMarkQueue() {
        marks = new ArrayBlockingQueue<>(MainConstant.DEFAULT_QUEUE_SIZE);
    }

    private static EcgMarkQueue _queue;

    protected static BlockingQueue<com.uteamtec.algorithm.types.EcgMark> getQueue() {
        if (_queue == null) {
            synchronized (EcgMarkQueue.class) {
                if (_queue == null) {
                    _queue = new EcgMarkQueue();
                }
            }
        }
        return _queue.marks;
    }

    synchronized public static void resetQueue() {
        if (_queue != null) {
            getQueue().clear();
        }
        _queue = null;
    }

//    public static boolean add(EcgMark mark) {
//        return mark != null && getQueue().add(mark.toEcgMark());
//    }

    public static boolean put(EcgMark mark) {
        if (mark != null) {
            try {
                getQueue().put(mark.toEcgMark());
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static EcgMark take() {
        try {
            return EcgMark.toEcgMark(getQueue().take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clear() {
        getQueue().clear();
    }

}
