package com.uteamtec.heartcool.fragment.model;

import com.kelin.calendarlistview.library.BaseCalendarItemModel;

/**
 * Created by wd
 */
public final class HistoryCalendarItemModel extends BaseCalendarItemModel {

    private int count;
    private boolean warning;

    public HistoryCalendarItemModel() {
        super();
        count = 0;
        warning = false;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }
}
