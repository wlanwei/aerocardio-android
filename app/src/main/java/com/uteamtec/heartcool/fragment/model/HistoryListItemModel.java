package com.uteamtec.heartcool.fragment.model;

import com.uteamtec.heartcool.service.db.DBDetection;

/**
 * Created by wd
 */
public final class HistoryListItemModel {

    private DBDetection detection;

    public DBDetection getDetection() {
        return detection;
    }

    public HistoryListItemModel(DBDetection detection) {
        this.detection = detection;
    }

}
