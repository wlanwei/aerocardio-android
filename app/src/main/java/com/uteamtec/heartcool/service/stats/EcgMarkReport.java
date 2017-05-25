package com.uteamtec.heartcool.service.stats;

import com.uteamtec.heartcool.AeroCardioApp;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.utils.DateFormats;

import java.util.Date;

/**
 * Created by wd
 */
public final class EcgMarkReport {

    private long seconds = 0;

    public long getSeconds() {
        return seconds;
    }

    // 时间指标
    public String jlsj; // 记录时间
    public String jcsc; // 监测时长
    // 常规
    public int HR; // 平均心律
    public String pjxl; // 平均心律
    public int HRHealth; // 心律范围
    public String xlfw; // 心律范围
    public int BR; // 平均呼吸
    public String pjhx; // 平均呼吸
    public String smzl; // 睡眠质量
    // 异常指标
    public String jcfk; // 心律监测反馈
    public String yczb; // 心律异常指标

    public String xhzl; // 信号质量

    private int markSize = 0;

    public EcgMarkReport(EcgMarkAnalyzer analyzer) {
        if (analyzer != null) {
            this.seconds = analyzer.getSeconds();

            this.jlsj = DateFormats.YYYY_MM_DD_HH_MM_CN.format(new Date());
            this.jcsc = analyzer.getSecondsFormat();

            this.HR = analyzer.getAverageHR();
            this.pjxl = String.valueOf(this.HR);
            this.HRHealth = analyzer.getHealthHR();
            this.xlfw = String.valueOf(this.HRHealth);
            this.BR = analyzer.getAverageBR();
            this.pjhx = String.valueOf(this.BR);
            if (this.HR <= 100 && this.HR >= 50) {
                this.smzl = getString(R.string.sleepQuality);
            } else {
                this.smzl = getString(R.string.no);
            }

            switch (analyzer.getHealthHRLevel()) {
                case 0:
                    this.jcfk = getString(R.string.indicators);// 各项指标均在正常范围内
                    this.yczb = getString(R.string.no);// 无
                    break;
                case 1:
                    this.jcfk = getString(R.string.arrest);// 心脏骤停
                    this.yczb = getString(R.string.heartExp);// 心律异常
                    break;
                case 2:
                    this.jcfk = getString(R.string.maybe_af_paf);// 可能房性类异常
                    this.yczb = getString(R.string.heartExp);// 心律异常
                    break;
                case 3:
                    this.jcfk = getString(R.string.maybe_vf_pvc);// 可能室性类异常
                    this.yczb = getString(R.string.heartExp);// 心律异常
                    break;
                default:
                    this.jcfk = getString(R.string.scope);// 部分指标不在正常范围内
                    this.yczb = getString(R.string.scope);// 部分指标不在正常范围内
                    break;
            }

            switch (analyzer.getNoiseLevel()) {
                case 1:
                    this.xhzl = getString(R.string.level_better);// 良
                    break;
                case 2:
                    this.xhzl = getString(R.string.level_normal);// 中
                    break;
                case 3:
                    this.xhzl = getString(R.string.level_bad);// 差
                    break;
                default:
                    this.xhzl = getString(R.string.level_best);// 优
                    break;
            }
        }
    }

    private String getString(int resId) {
        return AeroCardioApp.getApplication().getString(resId);
    }

    public int getMarkSize() {
        return markSize;
    }

    public void setMarkSize(int markSize) {
        this.markSize = markSize;
    }

}
