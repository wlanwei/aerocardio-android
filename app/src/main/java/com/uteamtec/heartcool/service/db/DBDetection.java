package com.uteamtec.heartcool.service.db;

import com.litesuits.orm.db.annotation.Default;
import com.litesuits.orm.db.annotation.Mapping;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.Relation;
import com.uteamtec.heartcool.service.stats.EcgMarkReport;
import com.uteamtec.heartcool.service.utils.DateFormats;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 监测主类
 * Created by wd
 */
@Table("test_detection")
public class DBDetection extends DBModel {

    private Date _date;

    private DBDetection(Date _date) {
        this._date = _date;
        this.marks = new ArrayList<>();
        this.year = DateFormats.YYYY.format(this._date);
        this.month = DateFormats.YYYY_MM.format(this._date);
        this.date = DateFormats.YYYY_MM_DD.format(this._date);
        this.startTime = this._date.getTime();
        this.stopTime = this.startTime;
    }

    public DBDetection(String infoId, String mac) {
        this(new Date());
        this.infoId = infoId;
        this.mac = mac;
    }

    public DBDetection(long startTime, long stopTime,
                       int HR, int HRRange, String feedback, String abnormal) {
        this(new Date(startTime));
        setDuration(stopTime - startTime);
        this.HR = HR;
        this.HRRange = HRRange;
        this.feedback = feedback;
        this.abnormal = abnormal;
    }

    @Mapping(Relation.OneToMany)
    private ArrayList<DBEcgMark> marks;

    @Mapping(Relation.OneToOne)
    private DBEcgMarkStats markStats;

    @NotNull
    private String infoId;

    @NotNull
    private String mac;

    @NotNull
    private String year;

    @NotNull
    private String month;

    @NotNull
    private String date;

    @NotNull
    private long startTime;

    @NotNull
    private long stopTime;

    @Default("'00:00:00'")
    @NotNull
    private String duration;

    @Default("各项指标均在正常范围内")
    @NotNull
    private String feedback;

    @Default("无")
    @NotNull
    private String abnormal;

    @Default("0")
    @NotNull
    private int HR;// 平均心律

    @Default("0")
    @NotNull
    private int HRRange; // 心律范围

    @Default("0")
    @NotNull
    private int BR;// 实时呼吸

    @Default("未知")
    @NotNull
    private String SQ;// 睡眠质量

    public ArrayList<DBEcgMark> getMarks() {
        return marks;
    }

    public boolean addMark(DBEcgMark mark) {
        if (mark != null && this.marks.add(mark)) {
            getMarkStats().addMark(mark);
            return true;
        }
        return false;
    }

    synchronized public DBEcgMarkStats getMarkStats() {
        if (markStats == null) {
            markStats = new DBEcgMarkStats(this);
        }
        return markStats;
    }

    public void setEcgMarkReport(EcgMarkReport report) {
        if (report != null) {
            setFeedback(report.jcfk);
            setAbnormal(report.yczb);
            setHR(report.HR);
            setHRRange(report.HRHealth);
            setBR(report.BR);
            setSQ(report.smzl);

            getMarkStats().setEcgMarkReport(report);
        }
    }

    public String getInfoId() {
        return infoId;
    }

    public void setInfoId(String infoId) {
        this.infoId = infoId;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }


    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDate() {
        return date;
    }

    public String getDateStrCN() {
        if (startTime > 0) {
            return DateFormats.YYYY_MM_DD_CN.format(new Date(startTime));
        } else if (stopTime > 0) {
            return DateFormats.YYYY_MM_DD_CN.format(new Date(stopTime));
        }
        return getDate();
    }

    public long getStartTime() {
        return startTime;
    }

    public String getStartTimeStr() {
        return DateFormats.HH_MM_SS.format(new Date(startTime));
    }

    public String getStartTimeStrCN() {
        return DateFormats.HH_MM_SS_CN.format(new Date(startTime));
    }

    public long getStopTime() {
        return stopTime;
    }

    public String getStopTimeStr() {
        return DateFormats.HH_MM_SS.format(new Date(stopTime));
    }

    public String getStopTimeStrCN() {
        return DateFormats.HH_MM_SS_CN.format(new Date(stopTime));
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        long seconds = duration / 1000;
        this.duration = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                seconds / 3600, seconds % 3600 / 60, seconds % 60);
        this.stopTime = this._date.getTime() + duration;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(String abnormal) {
        this.abnormal = abnormal;
    }

    public int getHR() {
        return HR;
    }

    public void setHR(int HR) {
        this.HR = HR;
    }

    public int getHRRange() {
        return HRRange;
    }

    public void setHRRange(int HRRange) {
        this.HRRange = HRRange;
    }

    public int getBR() {
        return BR;
    }

    public void setBR(int BR) {
        this.BR = BR;
    }

    public String getSQ() {
        return SQ;
    }

    public void setSQ(String SQ) {
        this.SQ = SQ;
    }

    public boolean save() {
        long id = DBOrm.cascadeSave(this);
        if (getId() <= 0) {
            setId(id);
        }
        return id > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DBDetection [id=" + id + ", year=" + year + ", " +
                "month=" + month + ", date=" + date + ", " +
                "startTime=" + getStartTimeStr() + ", stopTime=" + getStopTimeStr() + ", " +
                "duration=" + duration + ", feedback=" + feedback + ", abnormal=" + abnormal + ", " +
                "HR=" + HR + ", HRRange=" + HRRange + ", BR=" + BR + ", SQ=" + SQ
        );
        sb.append("] \n");
        sb.append(String.format(Locale.getDefault(), "DBEcgMark size: %d\n", marks.size()));
        return sb.toString();
    }

}
