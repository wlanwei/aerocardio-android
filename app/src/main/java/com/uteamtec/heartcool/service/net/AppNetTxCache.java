package com.uteamtec.heartcool.service.net;

import com.uteamtec.heartcool.AeroCardioApp;
import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.service.cache.ACacheHelper;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by wd
 */
public final class AppNetTxCache {

    private final Object lock;
    private final Map<Integer, AppMessage> priority;
    private final BlockingQueue<AppMessage> queue;

    private final ACacheHelper<AppMessage> cache;

    private Timer timer = null;
    private TimerTask timerTask = null;

    private AppNetTxCacheThread thread = null;

    private AppNetTxCache() {
        lock = new Object();
        priority = new HashMap<>();
        queue = new LinkedBlockingQueue<>();

        cache = new ACacheHelper<>(AppMessage.class,
                AeroCardioApp.getApplication(), "AppMessage");
    }

    synchronized private void start() {
        stop();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                L.e("AppNetTxCache.queue queue.size: " + queue.size());
                if (!User.getUser().isConnectedDeviceAppNet() &&
                        queue.size() >= 50) {
                    synchronized (getCache().lock) {
                        List<AppMessage> list = new ArrayList<>();
                        queue.drainTo(list);
                        cache.putAll(list);
                    }
                    L.e("AppNetTxCache.drainTo queue.size: " + queue.size());
                } else if (User.getUser().isConnectedDeviceAppNet() &&
                        queue.isEmpty()) {
                    List<AppMessage> list = cache.drain();
                    if (list != null) {
                        synchronized (getCache().lock) {
                            queue.addAll(list);
                        }
                        L.e("AppNetTxCache.addAll queue.size: " + queue.size());
                    }
                }
            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 10000, 10000);
    }

    private void stop() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = null;

        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    synchronized private void startThread() {
        if (thread == null) {
            thread = new AppNetTxCacheThread();
            thread.start();
        }
    }

    synchronized private void stopThread() {
        if (thread != null) {
            thread.close();
        }
        thread = null;
    }

    private class AppNetTxCacheThread extends Thread {

        private boolean _enabled = true;

        private void close() {
            _enabled = false;
        }

        @Override
        public void run() {
            AppMessage msg;
            while (_enabled) {
                msg = AppNetTxQueue.poll();
                if (msg == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (!_enabled) {
                    break;
                }
                AppNetTxCache.put(msg);
            }
        }
    }

    private static AppNetTxCache _cache;

    private static AppNetTxCache getCache() {
        if (_cache == null) {
            synchronized (AppNetTxCache.class) {
                if (_cache == null) {
                    _cache = new AppNetTxCache();
                    _cache.start();
                }
            }
        }
        return _cache;
    }

    protected static void StartThread() {
        getCache().startThread();
    }

    protected static void StopThread() {
        getCache().stopThread();
    }

    private static boolean put(AppMessage msg) {
        if (msg != null) {
            synchronized (getCache().lock) {
                switch (msg.getType()) {
                    case AppMessage.TYPE_LOGIN:
                    case AppMessage.TYPE_REG:
                    case AppMessage.TYPE_ACTIVATE:
                    case AppMessage.TYPE_PULSE:
                        getCache().priority.put(msg.getType(), msg);
                        return true;
                    case AppMessage.TYPE_STREAM:
                    case AppMessage.TYPE_MARK:
                        return getCache().queue.add(msg);
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    protected static boolean hasPriority() {
        return getCache().priority.size() != 0;
    }

    protected static AppMessage poll() {
        AppMessage msg;
        synchronized (getCache().lock) {
            msg = getCache().priority.put(AppMessage.TYPE_PULSE, null);
            if (msg != null) {
                return msg;
            }
            msg = getCache().priority.put(AppMessage.TYPE_LOGIN, null);
            if (msg != null) {
                return msg;
            }
            msg = getCache().priority.put(AppMessage.TYPE_REG, null);
            if (msg != null) {
                return msg;
            }
            msg = getCache().priority.put(AppMessage.TYPE_ACTIVATE, null);
            if (msg != null) {
                return msg;
            }
        }
        try {
            return getCache().queue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
