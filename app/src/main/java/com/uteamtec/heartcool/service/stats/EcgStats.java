package com.uteamtec.heartcool.service.stats;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.ecg.EcgUtil;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Ecg信号统计模块
 * Created by wd
 */
public final class EcgStats {

    private volatile boolean isLock = false;

    private final EcgFilter filter;
    private final EcgMarkAnalyzer analyzer;
    private final EcgMarkCounter counter;
    private final EcgMarkRealTimer realTimer;

    public EcgFilter getFilter() {
        return filter;
    }

    public EcgMarkAnalyzer getAnalyzer() {
        return analyzer;
    }

    public EcgMarkCounter getCounter() {
        return counter;
    }

    public EcgMarkRealTimer getRealTimer() {
        return realTimer;
    }

    private Map<EcgMark, ArrayList<Ecg>> result;

    public EcgStats() {
        isLock = false;
        filter = new EcgFilter();
        analyzer = new EcgMarkAnalyzer();
        counter = new EcgMarkCounter();
        realTimer = new EcgMarkRealTimer();
        result = new HashMap<>();
    }

    private void lock() {
        isLock = true;
    }

    private void unlock() {
        isLock = false;
    }

    public synchronized void startRecord() {
        if (!isLock) {
            analyzer.startRecord();
            counter.start();
            realTimer.start();
        }
    }

    public synchronized void stopRecord() {
        if (!isLock) {
            analyzer.stopRecord();
            counter.stop();
            realTimer.stop();
        }
    }

    public synchronized void clear() {
        if (!isLock) {
            filter.clear();
            analyzer.clear();
            counter.clear();
            realTimer.clear();
            result.clear();
        }
    }

    public synchronized void recordEcg(Ecg e) {
        if (!isLock && EcgUtil.validEcg(e)) {
            filter.put(e);
        }
    }

    public synchronized void recordMark(EcgMark m) {
        if (!isLock && EcgUtil.validEcgMark(m)) {
            analyzer.recordMark(m);
            counter.put(m);
            realTimer.put(m);
        }
    }

    public synchronized void displayMark(EcgMark m) {
        if (EcgUtil.validEcgMark(m)) {
            analyzer.displayMark(m);
        }
    }

    public synchronized long count(int typeGroup, int type) {
        if (!isLock) {
            return counter.count(typeGroup, type);
        }
        return 0;
    }

    public synchronized boolean analyze() {
        if (isLock) {
            return false;
        }
        lock();
        result.clear();
        // TODO: 这里补充异常的mark类型
        // TODO: 本地不用存了
//        Map<EcgMark, ArrayList<Ecg>> rs;
//        for (int i = 0; i <= 2; i++) {
//            switch (i) {
//                case 0:
//                    rs = filter.analyze(counter.list(EcgMark.TYPE_GROUP_PHYSIO, EcgMark.PHYSIO_NOISE));
//                    break;
//                case 1:
//                    rs = filter.analyze(counter.list(EcgMark.TYPE_GROUP_PHYSIO, EcgMark.PHYSIO_USERINPUT));
//                    break;
//                case 2:
//                    rs = filter.analyze(counter.list(EcgMark.TYPE_GROUP_PHYSIO, EcgMark.PHYSIO_ABNORMAL));
//                    break;
//                default:
//                    continue;
//            }
//            if (rs != null && rs.size() > 0) {
//                result.putAll(rs);
//            }
//        }
        unlock();
        return true;
    }

    public synchronized Map<EcgMark, ArrayList<Ecg>> result() {
        if (!isLock) {
            return result;
        }
        return null;
    }

    public EcgMarkReport getReport() {
        if (!isLock) {
            EcgMarkReport report = analyzer.getReport();
            report.setMarkSize(counter.size());
            return report;
        }
        return null;
    }

}
