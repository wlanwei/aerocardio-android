package com.uteamtec.heartcool.fragment.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kelin.calendarlistview.library.BaseCalendarListAdapter;
import com.kelin.calendarlistview.library.CalendarHelper;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.views.widget.CalendarListView;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by wd
 */
public class HistoryListItemAdapter extends BaseCalendarListAdapter<HistoryListItemModel> {


    public HistoryListItemAdapter(Context context) {
        super(context);
    }

    private CalendarListView.OnChangedListener listener;

    public void setOnChangedListener(CalendarListView.OnChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public View getSectionHeaderView(String date, View convertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        if (convertView != null) {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        } else {
            convertView = getInflater().inflate(R.layout.listitem_history_record_header, null);
            headerViewHolder = new HeaderViewHolder();
            headerViewHolder.dayText = (TextView) convertView.findViewById(R.id.history_record_header_day);
            headerViewHolder.yearMonthText = (TextView) convertView.findViewById(R.id.history_record_header_year_month);
            convertView.setTag(headerViewHolder);
        }
        Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(date);
        headerViewHolder.dayText.setText(String.format(Locale.getDefault(), "%2d", calendar.get(Calendar.DAY_OF_MONTH)));
        headerViewHolder.yearMonthText.setText(DateFormats.YYYY_MM_CN.format(calendar.getTime()));
        return convertView;
    }

    @Override
    public View getItemView(HistoryListItemModel model, String date, int pos, View convertView, ViewGroup parent) {
        final ContentViewHolder contentViewHolder;
        if (convertView != null) {
            contentViewHolder = (ContentViewHolder) convertView.getTag();
        } else {
            convertView = getInflater().inflate(R.layout.listitem_history_record_content, null);
            contentViewHolder = new ContentViewHolder();
            contentViewHolder.startTimeText = (TextView) convertView.findViewById(R.id.history_record_item_tv_start_time);
            contentViewHolder.durationText = (TextView) convertView.findViewById(R.id.history_record_item_tv_duration);
            contentViewHolder.feedbackText = (TextView) convertView.findViewById(R.id.history_record_item_tv_feedback);
            contentViewHolder.exceptionText = (TextView) convertView.findViewById(R.id.history_record_item_tv_exception);
            contentViewHolder.hrAverageText = (TextView) convertView.findViewById(R.id.fragment_history_tv_hr);
            contentViewHolder.hrHealthText = (TextView) convertView.findViewById(R.id.fragment_history_tv_hr_health);
            convertView.findViewById(R.id.fragment_history_ll_result).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null && contentViewHolder.model != null &&
                            contentViewHolder.model.getDetection() != null) {
                        listener.onItemClick(contentViewHolder.model.getDetection().getDate(),
                                contentViewHolder.model);
                    }
                }
            });
            convertView.setTag(contentViewHolder);
        }
        contentViewHolder.model = model;
        if (model.getDetection() != null) {
            contentViewHolder.startTimeText.setText(model.getDetection().getDateStrCN() +
                    " " + model.getDetection().getStartTimeStrCN());
            contentViewHolder.durationText.setText(model.getDetection().getDuration());
            contentViewHolder.feedbackText.setText(model.getDetection().getFeedback());
            contentViewHolder.exceptionText.setText(model.getDetection().getAbnormal());
            contentViewHolder.hrAverageText.setText(String.valueOf(model.getDetection().getHR()));
            contentViewHolder.hrHealthText.setText(String.valueOf(model.getDetection().getHRRange()));
        }
        return convertView;
    }

    private static class HeaderViewHolder {
        TextView dayText;
        TextView yearMonthText;
    }

    private static class ContentViewHolder {
        HistoryListItemModel model;
        TextView startTimeText;
        TextView durationText;
        TextView feedbackText;
        TextView exceptionText;
        TextView hrAverageText;
        TextView hrHealthText;
    }

}
