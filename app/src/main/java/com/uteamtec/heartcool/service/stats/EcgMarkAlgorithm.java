package com.uteamtec.heartcool.service.stats;

/**
 * Created by wd
 */
public final class EcgMarkAlgorithm {

    /**
     * 正常心率结论
     *
     * @return int
     * -1-时间太短;
     * 0-各项指标均在正常范围内;
     * 1-心脏骤停;
     * 2-可能房性类异常;
     * 3-可能室性类异常
     * 4-部分指标不在正常范围内
     */
    public static int getHealthHRLevel(long seconds, int HR,
                                       long XLBQ, long XLGS, long XLGH,
                                       long SXZB, long FXZB, long SC, long FC) {
        if (seconds <= 0) {
            return -1;
        }
        if (HR <= 0) {
            return 1;
        }
        if (XLBQ <= 0 && XLGS <= 0 && XLGH <= 0 &&
                SXZB <= 0 && FXZB <= 0 && SC <= 0 && FC <= 0) {
            if (55 < HR && HR < 105) {
                return 0;
            }
        } else if (SXZB > 0 || SC > 0 || FXZB > 0 || FC > 0) {
            int minutes = (int) (seconds / 60);
            if (seconds % 60 != 0) {
                minutes++;
            }
            if ((int) ((FXZB + FC) / minutes) > 5) {
                return 3;
            }
            if ((int) ((SXZB + SC) / minutes) > 5) {
                return 4;
            }
        }
        return 4;
    }

    /**
     * 信号质量
     *
     * @return int 0-优; 1-良; 2-中; 3-差
     */
    public static int getNoiseLevel(long seconds, long noiseCount) {
        if (seconds <= 0) {
            return 0;
        }
        int minutes = (int) (seconds / 60);
        if (seconds % 60 != 0) {
            minutes++;
        }
        final int LEVEL = (int) (noiseCount / minutes);
        if (LEVEL <= 1) {
            return 0;
        } else if (LEVEL <= 5) {
            return 1;
        } else if (LEVEL <= 10) {
            return 2;
        }
        return 3;
    }

}
