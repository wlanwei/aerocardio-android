package com.uteamtec.heartcool.service.stats;


import android.util.Log;

import com.uteamtec.heartcool.service.ecg.EcgUtil;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * EcgMark数量统计
 * Created by wd
 */
public final class EcgMarkCounter {

    private static final String TAG = "EcgMarkCounter";

    private Map<String, MutableLong> counter = new HashMap<>();

    private Map<String, ArrayList<EcgMark>> marks = new HashMap<>();

    public EcgMarkCounter() {
    }

    public synchronized void clear() {
        counter.clear();
        marks.clear();
    }

    public synchronized void put(EcgMark mark) {
        if (mark != null) {
            String k = EcgUtil.getEcgMarkType(mark);
            MutableLong l = counter.put(k, new MutableLong(1));
            if (l != null) {
                counter.put(k, l.increment());
            }

            ArrayList<EcgMark> arr = marks.put(k, new ArrayList<EcgMark>());
            if (arr != null && arr.add(mark)) {
                marks.put(k, arr);
            }

            Log.e(TAG, String.format("EcgMark group=%d type=%d",
                    mark.getTypeGroup(), mark.getType()));
        }
    }

    public synchronized long count(int typeGroup, int type) {
        String k = EcgUtil.getEcgMarkType(typeGroup, type);
        if (counter.containsKey(k)) {
            return counter.get(k).get();
        }
        return 0;
    }

    public synchronized int size() {
        return counter.size();
    }

    public synchronized ArrayList<EcgMark> list(int typeGroup, int type) {
        String k = EcgUtil.getEcgMarkType(typeGroup, type);
        if (marks.containsKey(k)) {
            return marks.get(k);
        }
        return new ArrayList<>();
    }

    private DebugThread debugThread = null;

    public void start() {
        if (debugThread == null) {
            debugThread = new DebugThread(this);
            debugThread.start();
        }
    }

    public void stop() {
        if (debugThread != null) {
            debugThread.quit();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("------Stats-----\n");
        Iterator<String> i = counter.keySet().iterator();
        while (i.hasNext()) {
            String k = i.next();
            sb.append("Type: ");
            sb.append(k);
            sb.append(" Size: ");
            sb.append(counter.get(k).toString());
            sb.append("\n");
        }
        sb.append("----------------");
        return sb.toString();
    }

    private final class DebugThread extends Thread {

        private EcgMarkCounter _counter;

        private DebugThread(EcgMarkCounter e) {
            _counter = e;
        }

        public void quit() {
            _counter = null;
        }

        @Override
        public void run() {
            while (_counter != null) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (_counter != null) {
                    Log.e(TAG, _counter.toString());
                }
            }
        }
    }

}
