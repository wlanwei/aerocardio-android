package com.uteamtec.heartcool.service.db;

import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.Table;
import com.uteamtec.algorithm.types.Ecg;

/**
 * Ecg数据集
 * Created by wd
 */
@Table("test_ecg")
public class DBEcg extends DBModel {

    private Ecg ecg;

    public Ecg getEcg() {
        return ecg;
    }

    public DBEcg(Ecg ecg) {
        this.ecg = ecg;
        if (ecg != null) {
            this.type = ecg.getType();
            this.startTime = ecg.getStartTime();
            this.stopTime = ecg.getStopTime();
            this.sps = ecg.getSps();
            this.data = ecg.getData();
        }
    }

    @NotNull
    private int type;

    @NotNull
    private long startTime;

    @NotNull
    private long stopTime;

    @NotNull
    private int sps;

    @NotNull
    private int[] data;

    public int getType() {
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getSps() {
        return sps;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DBEcg [id=" + id + ", type=" + type + ", " +
                "startTime=" + startTime + ", stopTime=" + stopTime + ", " +
                "sps=" + sps + ", data=" + data.length
        );
        sb.append("] \n");
        return sb.toString();
    }

}
