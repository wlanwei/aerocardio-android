package com.uteamtec.heartcool.fragment.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kelin.calendarlistview.library.BaseCalendarItemAdapter;
import com.kelin.calendarlistview.library.BaseCalendarItemModel;
import com.uteamtec.heartcool.R;

/**
 * Created by wd
 */
public final class HistoryCalendarItemAdapter extends BaseCalendarItemAdapter<HistoryCalendarItemModel> {

    public HistoryCalendarItemAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(String date, HistoryCalendarItemModel model, View convertView, ViewGroup parent) {
        ContentViewHolder contentViewHolder;
        if (convertView != null) {
            contentViewHolder = (ContentViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wight_calendar_item, null);
            contentViewHolder = new ContentViewHolder();
            contentViewHolder.dayNum = (TextView) convertView.findViewById(R.id.calendar_item_tv_num);
            contentViewHolder.dayCount = (TextView) convertView.findViewById(R.id.calendar_item_tv_count);
            convertView.setTag(contentViewHolder);
        }
        if (model != null) {
            contentViewHolder.dayNum.setText(model.getDayNumber());

            convertView.setBackgroundResource(com.kelin.calendarlistview.library.R.drawable.bg_shape_calendar_item_normal);

            if (model.isToday()) {
                contentViewHolder.dayNum.setTextColor(getContext().getResources().getColor(com.kelin.calendarlistview.library.R.color.red_ff725f));
                contentViewHolder.dayNum.setText(getContext().getResources().getString(com.kelin.calendarlistview.library.R.string.today));
            }

//            if (model.isHoliday()) {
//                contentViewHolder.dayNum.setTextColor(getContext().getResources().getColor(com.kelin.calendarlistview.library.R.color.red_ff725f));
//            }

            if (model.getStatus() == BaseCalendarItemModel.Status.DISABLE) {
                contentViewHolder.dayNum.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
            }

            if (!model.isCurrentMonth()) {
                contentViewHolder.dayNum.setTextColor(getContext().getResources().getColor(com.kelin.calendarlistview.library.R.color.gray_bbbbbb));
                convertView.setClickable(true);
            }

            if (model.getCount() > 0) {
                contentViewHolder.dayCount.setText(String.valueOf(model.getCount()));
                contentViewHolder.dayCount.setVisibility(View.VISIBLE);
            } else {
                contentViewHolder.dayCount.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    private static class ContentViewHolder {
        TextView dayNum;
        TextView dayCount;
    }

}
