package com.uteamtec.heartcool.service.stats;

import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.utils.L;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * EcgMark解析器
 * Created by wd
 */
public final class EcgMarkAnalyzer {

    private Timer timer = null;
    private TimerTask timerTask = null;
    private volatile long seconds = 0;

    public long getSeconds() {
        return seconds;
    }

    public String getSecondsFormat() {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                seconds / 3600, seconds % 3600 / 60, seconds % 60);
    }

    private int HR = 0;
    private long HRTotal = 0;
    private long HRCount = 0;
    private long HRHealthCount = 0;

    private int BR = 0;
    private long BRTotal = 0;
    private long BRCount = 0;
    private long BRHealthCount = 0;

    private long AbnormalCount = 0; // 异常个数
    private long NoiseCount = 0; // 噪声个数

    public EcgMarkAnalyzer() {
    }

    public synchronized void startRecord() {
        if (timer != null || timerTask != null) {
            return;
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                seconds++;
                if (ListenerMgr.hasDetectionListener()) {
                    ListenerMgr.getDetectionListener().onTimerTick(getSecondsFormat());
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    public synchronized void stopRecord() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public synchronized void clear() {
        seconds = 0;

        HRTotal = 0;
        HRCount = 0;
        HRHealthCount = 0;

        BRTotal = 0;
        BRCount = 0;
        BRHealthCount = 0;

        AbnormalCount = 0;
        NoiseCount = 0;
    }

    public synchronized void recordMark(EcgMark m) {
        if (m == null) {
            return;
        }
        switch (m.getTypeGroup()) {
            case EcgMark.TYPE_GROUP_STATUS:
                displayMark(m);
                break;
            case EcgMark.TYPE_GROUP_PHYSIO:
                final int VALUE = m.getValue();
                if (VALUE < 0) {
                    return;
                }
                switch (m.getType()) {
                    case EcgMark.PHYSIO_HR:
                        HR = VALUE;
                        HRTotal += VALUE;
                        HRCount++;
                        if (55 < VALUE && VALUE < 105) {
                            HRHealthCount++;
                        } else {
                            AbnormalCount++;
                        }
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkHR(
                                    VALUE, (55 < VALUE && VALUE < 105),
                                    getAverageHR(),
                                    getHealthHR(), (HRHealthCount * 100 / HRCount) >= 90);
                        }
                        break;
                    case EcgMark.PHYSIO_BR:
                        BR = VALUE;
                        BRTotal += VALUE;
                        BRCount++;
                        BRHealthCount++;
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkBR(VALUE);
                        }
                        break;
                    case EcgMark.PHYSIO_NOISE:
                        NoiseCount++;
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkNoise("噪声");
                        }
                        break;
                    case EcgMark.PHYSIO_USERINPUT:
                        break;
                    case EcgMark.PHYSIO_ABNORMAL:
                        AbnormalCount++;
                        break;
                }
                break;
            default:
                return;
        }
        if (ListenerMgr.hasEcgMarkListener()) {
            ListenerMgr.getEcgMarkListener().onMarkUpdated();
        }
    }

    public void displayMark(EcgMark m) {
        if (m == null) {
            return;
        }
        L.e("EcgMarkAnalyzer.displayMark: " + m.toString());
        final int VALUE = m.getValue();
        switch (m.getTypeGroup()) {
            case EcgMark.TYPE_GROUP_STATUS:
                switch (m.getType()) {
                    case EcgMark.STATUS_LEADOFF:
                        String msg = "接触不理想";
                        if (User.getUser().getUserDevice().getModel() == UserDevice.MODEL_20_1) {
                            msg = "接触不理想";
                        } else if (User.getUser().getUserDevice().getModel() == UserDevice.MODEL_20_3) {
                            boolean chn1Off = ((VALUE & 0x01) == 1);
                            boolean chn2Off = ((VALUE & 0x02) == 2);
                            boolean chn3Off = ((VALUE & 0x04) == 4);
                            String ch1 = "1";
                            String ch2 = "2";
                            String ch3 = "3";
                            String combo = (chn1Off ? ch1 : " ") + (chn2Off ? ch2 : " ") +
                                    (chn3Off ? ch3 : " ");
                            msg = "导联" + combo + "接触不理想";
                        }
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkLeadOff(msg);
                        }
                        break;
                    case EcgMark.STATUS_LOWPOWER:
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkLowPower("电量不足");
                        }
                        break;
                    case EcgMark.STATUS_SHORT:
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkShort("短路");
                        }
                        break;
                    case EcgMark.STATUS_CHARGING:
                        break;
                    case EcgMark.STATUS_UNPLUG:
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkUnplug("拔下");
                        }
                        break;
                    case EcgMark.STATUS_PLUG:
                        break;
                }
                break;
            case EcgMark.TYPE_GROUP_PHYSIO:
                if (VALUE < 0) {
                    return;
                }
                switch (m.getType()) {
                    case EcgMark.PHYSIO_HR:
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkHR(VALUE, true,
                                    -1, -1, true);
                        }
                        break;
                    case EcgMark.PHYSIO_BR:
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkBR(VALUE);
                        }
                        break;
                    case EcgMark.PHYSIO_NOISE:
                        if (ListenerMgr.hasEcgMarkListener()) {
                            ListenerMgr.getEcgMarkListener().onMarkNoise("噪声");
                        }
                        break;
                    case EcgMark.PHYSIO_USERINPUT:
                        break;
                    case EcgMark.PHYSIO_ABNORMAL:
                        break;
                }
                break;
        }
    }

    /**
     * 平均心率
     */
    public int getAverageHR() {
        if (HRCount <= 0) {
            return 0;
        }
        return (int) (HRTotal / HRCount);
    }

    /**
     * 是否正常心率
     */
    public boolean isHealthHR() {
        if (HRCount == 0) {
            return true;
        }
        return (55 < HR && HR < 105 && (float) (HRHealthCount / HRCount) >= 0.90f);
    }

    /**
     * 正常心率指标
     */
    public int getHealthHR() {
        if (HRCount <= 0) {
            return 0;
        }
        final int Q = (int) (HRHealthCount * 100 / HRCount);
        if (seconds <= 0) {
            return Q;
        }
        int minutes = (int) (seconds / 60);
        if (seconds % 60 != 0) {
            minutes++;
        }
        int T = Q;
        final int LEVEL = (int) (AbnormalCount / minutes);
        if (LEVEL < 1) {
            T -= 0;
        } else if (LEVEL < 5) {
            T -= 1;
        } else {
            T -= 5;
        }
        if (T <= 0) {
            return 0;
        }
        return T;
    }

    /**
     * 正常心率结论
     *
     * @return int
     * 0-各项指标均在正常范围内;
     * 1-心脏骤停;
     * 2-可能房性类异常;
     * 3-可能室性类异常
     * 4-部分指标不在正常范围内
     */
    public int getHealthHRLevel() {
        if (HR <= 0) {
            return 1;
        }
        // TODO: 还没有接口算法实现
//        if (this.HR <= 100 && this.HR >= 84 || this.HR <= 48) {
//            return 2;
//        } else if (this.HR > 100) {
//            return 3;
//        }
        return 0;
    }

    /**
     * 平均呼吸率
     */
    public int getAverageBR() {
        if (BRCount <= 0) {
            return 0;
        }
        return (int) (BRTotal / BRCount);
    }

    /**
     * 信号质量
     *
     * @return int 0-优; 1-良; 2-中; 3-差
     */
    public int getNoiseLevel() {
        return EcgMarkAlgorithm.getNoiseLevel(seconds, NoiseCount);
    }

    public EcgMarkReport getReport() {
        return new EcgMarkReport(this);
    }

}
