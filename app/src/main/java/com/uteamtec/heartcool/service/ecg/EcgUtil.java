package com.uteamtec.heartcool.service.ecg;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.Locale;

/**
 * Ecg工具集
 * Created by wd
 */
public class EcgUtil {

    public static String getEcgMarkType(int typeGroup, int type) {
        return String.format(Locale.getDefault(), "%d-%d", typeGroup, type);
    }

    public static String getEcgMarkType(EcgMark m) {
        if (m != null) {
            return getEcgMarkType(m.getTypeGroup(), m.getType());
        }
        return "";
    }

    public static String getEcgMarkTime(EcgMark m) {
        if (m != null) {
            return "" + m.getStartTime() + "-" + m.getStopTime();
        }
        return "";
    }

    public static boolean validEcg(Ecg e) {
        return (e != null && e.getStartTime() >= 0 && e.getStartTime() <= e.getStopTime());
    }

    public static boolean validEcgMark(EcgMark m) {
        return (m != null && m.getStartTime() >= 0 && m.getStartTime() <= m.getStopTime());
    }

    public static boolean inEcgMarkTime(EcgMark m, Ecg e) {
        if (m != null && e != null) {
            if (m.getStartTime() > e.getStopTime() ||
                    m.getStopTime() < e.getStartTime()) {
                return false;
            }
            return true;
        }
        return false;
    }

}
