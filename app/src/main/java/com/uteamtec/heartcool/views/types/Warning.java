package com.uteamtec.heartcool.views.types;

/**
 * Warning message for MainAeroCardioActivity
 * Created by liulingfeng on 2015/11/18.
 */
public class Warning {
    public static final int TYPE_NONETWORK = 1;
    public static final int TYPE_DICONNECT = 2;
    public static final int TYPE_DEVICEERR = 3;
    public static final int TYPE_LEADOFF = 4;
    public static final int TYPE_UNPLUG = 5;
    public static final int TYPE_NOISE = 6;
    public static final int TYPE_PHYSIO = 7;
    public static final int TYPE_LOWPOWER = 8;
    public static final int TYPE_SHORT = 8;

    public static final int DEFAULT_LIFETIME = 2000;

    private int type;
    private int val;
    private String info;
    private long receivedTime;


    public Warning(int type, int val, String info) {
        this.type = type;
        this.val = val;
        this.info = info;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(long receivedTime) {
        this.receivedTime = receivedTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

