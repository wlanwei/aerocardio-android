package com.uteamtec.heartcool.views.types;

/**
 * Created by liulingfeng on 2015/11/21.
 */
public class Record {
    private String date;
    private String rec;

    public Record(String date, String rec) {
        this.date = date;
        this.rec = rec;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRec() {
        return rec;
    }

    public void setRec(String rec) {
        this.rec = rec;
    }
}
