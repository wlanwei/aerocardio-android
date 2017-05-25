package com.uteamtec.heartcool.model;

/**
 * Created by Administrator on 2016/3/14 0014.
 * 心电测试历史数据
 */
public class HistoryData {
    private String startTime;//记录开始日期
    private String endTime;//记录结束日期
    private String timeLenth;//记录时间
    private String feedBack;//监测反馈
    private String exception;//异常指标
    private String sleep;//睡眠质量
    private String avghr;//平均心律
    private String normal;//正常心律范围
    private int pvAvghr;//平均心律进度
    private int pvNormalHeart;//正常心律进度
    private int pvRhythm;//节律进度

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPvAvghr() {
        return pvAvghr;
    }

    public void setPvAvghr(int pvAvghr) {
        this.pvAvghr = pvAvghr;
    }

    public int getPvNormalHeart() {
        return pvNormalHeart;
    }

    public void setPvNormalHeart(int pvNormalHeart) {
        this.pvNormalHeart = pvNormalHeart;
    }

    public int getPvRhythm() {
        return pvRhythm;
    }

    public void setPvRhythm(int pvRhythm) {
        this.pvRhythm = pvRhythm;
    }

    public String getTimeLenth() {
        return timeLenth;
    }

    public void setTimeLenth(String timeLenth) {
        this.timeLenth = timeLenth;
    }

    public String getFeedBack() {
        return feedBack;
    }

    public void setFeedBack(String feedBack) {
        this.feedBack = feedBack;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getAvghr() {
        return avghr;
    }

    public void setAvghr(String avghr) {
        this.avghr = avghr;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    @Override
    public String toString() {
        return "HistoryData{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", timeLenth='" + timeLenth + '\'' +
                ", feedBack='" + feedBack + '\'' +
                ", exception='" + exception + '\'' +
                ", sleep='" + sleep + '\'' +
                ", avghr='" + avghr + '\'' +
                ", normal='" + normal + '\'' +
                ", pvAvghr=" + pvAvghr +
                ", pvNormalHeart=" + pvNormalHeart +
                ", pvRhythm=" + pvRhythm +
                '}';
    }
}
