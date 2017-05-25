package com.uteamtec.heartcool.service.db;

import com.litesuits.orm.db.annotation.Default;
import com.litesuits.orm.db.annotation.Mapping;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.Relation;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.ArrayList;
import java.util.Locale;

/**
 * EcgMark数据集
 * Created by wd
 */
@Table("test_ecg_mark")
public class DBEcgMark extends DBModel {

    private EcgMark mark;

    public EcgMark getMark() {
        return mark;
    }

    public DBEcgMark(EcgMark mark) {
        this.ecgs = new ArrayList<>();
        this.mark = mark;
        if (mark != null) {
            this.startTime = mark.getStartTime();
            this.stopTime = mark.getStopTime();
            this.typeGroup = mark.getTypeGroup();
            this.type = mark.getType();
            this.value = mark.getValue();
            this.messageType = mark.getMessageType();
        }
    }

    @Mapping(Relation.OneToMany)
    private ArrayList<DBEcg> ecgs;

    @NotNull
    private long startTime;

    @NotNull
    private long stopTime;

    @NotNull
    private int typeGroup;

    @NotNull
    private int type;

    @NotNull
    private int value;

    @Default(EcgMark.MSG_TYPE_ZC)
    @NotNull
    private String messageType;

    public ArrayList<DBEcg> getEcgs() {
        return ecgs;
    }

    public boolean addEcg(DBEcg ecg) {
        return this.ecgs.add(ecg);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public int getTypeGroup() {
        return typeGroup;
    }

    public void setTypeGroup(int typeGroup) {
        this.typeGroup = typeGroup;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DBEcgMark [id=" + id + ", " +
                "startTime=" + startTime + ", stopTime=" + stopTime + ", " +
                "typeGroup=" + typeGroup + ", type=" + type + ", value=" + value + ", " +
                "messageType=" + messageType
        );
        sb.append("] \n");
        sb.append(String.format(Locale.getDefault(), "DBEcg size: %d\n", ecgs.size()));
        return sb.toString();
    }

}
