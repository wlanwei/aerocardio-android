package com.uteamtec.heartcool.views.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.uteamtec.heartcool.service.utils.DateFormats;

import java.util.Date;

/**
 * Created by wd
 */
public class CalendarListView extends com.kelin.calendarlistview.library.CalendarListView
        implements com.kelin.calendarlistview.library.CalendarListView.OnMonthChangedListener,
        com.kelin.calendarlistview.library.CalendarListView.onListPullListener,
        com.kelin.calendarlistview.library.CalendarListView.OnListItemClickListener {

    public interface OnChangedListener {

        void onRefresh();

        void onMonthChanged(String yearMonth);

        void onItemClick(String date, Object obj);
    }

    private OnChangedListener listener;

    public void setOnChangedListener(OnChangedListener listener) {
        this.listener = listener;
    }

    public OnChangedListener getOnChangedListener() {
        return this.listener;
    }

    private String month;

    public String getMonth() {
        return month;
    }

    public CalendarListView(Context context) {
        super(context, null);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    public CalendarListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        init();
    }

    private void init() {
        setOnListPullListener(this);
        setOnMonthChangedListener(this);
        setOnListItemClickListener(this);
        month = DateFormats.YYYY_MM.format(new Date());
    }

    @Override
    public void onMonthChanged(String yearMonth) {
        month = yearMonth;
        if (listener != null) {
            listener.onMonthChanged(yearMonth);
        }
    }

    @Override
    public void onRefresh() {
        if (listener != null) {
            listener.onRefresh();
        }
    }

    @Override
    public void onLoadMore() {
    }

    @Override
    public void onItemClick(String date, Object obj) {
        if (listener != null) {
            listener.onItemClick(date, obj);
        }
    }

}
