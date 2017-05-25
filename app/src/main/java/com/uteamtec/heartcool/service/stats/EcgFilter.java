package com.uteamtec.heartcool.service.stats;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.ecg.EcgUtil;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Ecg信号过滤器 via EcgMark
 * Created by wd
 */
public final class EcgFilter {

    private ArrayList<Ecg> ecgList = new ArrayList<>();

    public EcgFilter() {
    }

    public synchronized void clear() {
        ecgList.clear();
    }

    public synchronized void put(Ecg e) {
        if (e != null) {
            ecgList.add(e);
        }
    }

    public synchronized ArrayList<Ecg> analyze(EcgMark m) {
        ArrayList<Ecg> result = new ArrayList<>();
        if (m != null) {
            Ecg e;
            Iterator<Ecg> i = ecgList.iterator();
            while (i.hasNext()) {
                e = i.next();
                if (e != null && EcgUtil.inEcgMarkTime(m, e)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    public synchronized Map<EcgMark, ArrayList<Ecg>> analyze(ArrayList<EcgMark> ms) {
        Map<EcgMark, ArrayList<Ecg>> result = new HashMap<>();
        if (ms != null && ms.size() > 0) {
            for (EcgMark m : ms) {
                ArrayList<Ecg> es = analyze(m);
                if (es != null && es.size() > 0) {
                    result.put(m, es);
                }
            }
        }
        return result;
    }

}
