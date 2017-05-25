package com.uteamtec.heartcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.model.HistoryData;
import com.uteamtec.heartcool.user.ColorfulRingProgressView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/14 0014.
 */
public class HistoryAdapter extends BaseAdapter {
    private ArrayList<HistoryData> al_data;
    private Context context;
    private onShareClick onShareClick;

    public interface onShareClick {
        void onShareItem(int position);
    }

    public HistoryAdapter(ArrayList<HistoryData> al_data, Context context, onShareClick onShareClick) {
        this.al_data = al_data;
        this.context = context;
        this.onShareClick = onShareClick;
    }

    @Override
    public int getCount() {
        return al_data.size();
    }

    @Override
    public Object getItem(int position) {
        return al_data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HistoryHolder historyHolder = null;
        if (convertView == null) {
            historyHolder = new HistoryHolder();
            //初始化控件并设置tag
            convertView = LayoutInflater.from(context).inflate(R.layout.history_data_item, null);
            historyHolder.tv_startDate = (TextView) convertView.findViewById(R.id.history_data_item_tv_startDate);
            historyHolder.tv_endDate = (TextView) convertView.findViewById(R.id.history_data_item_tv_endDate);
            historyHolder.tv_timeLenth = (TextView) convertView.findViewById(R.id.history_data_item_tv_lenth);
            historyHolder.tv_feedBack = (TextView) convertView.findViewById(R.id.history_data_item_tv_feedBack);
            historyHolder.tv_exception = (TextView) convertView.findViewById(R.id.history_data_item_tv_exception);
            historyHolder.tv_sleep = (TextView) convertView.findViewById(R.id.history_data_item_tv_sleep);
            historyHolder.tv_avghr = (TextView) convertView.findViewById(R.id.record_data_tv_avghr);
            historyHolder.tv_normal = (TextView) convertView.findViewById(R.id.record_data_tv_normalHeart);
            //   historyHolder.tv_rhythm = (TextView) convertView.findViewById(R.id.record_data_tv_rhythm);
            historyHolder.pv_avghr = (ColorfulRingProgressView) convertView.findViewById(R.id.record_data_rp_progressAvghr);
            historyHolder.pv_normalHeart = (ColorfulRingProgressView) convertView.findViewById(R.id.record_data_rp_progressNormalheart);
            //    historyHolder.pv_rhythm = (ColorfulRingProgressView) convertView.findViewById(R.id.record_data_rp_progressRhythm);
            historyHolder.ll_share = (LinearLayout) convertView.findViewById(R.id.history_data_item_ll_share);//分享
            convertView.setTag(historyHolder);
        } else {
            historyHolder = (HistoryHolder) convertView.getTag();
        }
        //设置数据
        HistoryData historyData = al_data.get(position);
        historyHolder.tv_startDate.setText(historyData.getStartTime());
        historyHolder.tv_endDate.setText(historyData.getEndTime());
        historyHolder.tv_timeLenth.setText(historyData.getTimeLenth());
        historyHolder.tv_feedBack.setText(historyData.getFeedBack());
        historyHolder.tv_exception.setText(historyData.getException());
        historyHolder.tv_sleep.setText(historyData.getSleep());
        historyHolder.tv_avghr.setText(historyData.getAvghr() + "");
        historyHolder.tv_normal.setText(historyData.getNormal() + "%");
        //  historyHolder.tv_rhythm.setText(historyData.getRhythm() + "%");
        historyHolder.pv_avghr.setPercent(100);
        historyHolder.pv_normalHeart.setPercent(Float.parseFloat(historyData.getNormal()));
        historyHolder.ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShareClick != null) {
                    onShareClick.onShareItem(position);
                }
            }
        });
        //  historyHolder.pv_rhythm.setPercent(Float.parseFloat(historyData.getRhythm()));
        return convertView;
    }

    class HistoryHolder {
        TextView tv_startDate, tv_endDate, tv_timeLenth, tv_feedBack, tv_exception, tv_sleep, tv_avghr, tv_normal;//记录时间，监测时长，监测反馈，异常指标，平均心律，正常心律，节律范围
        ColorfulRingProgressView pv_avghr, pv_normalHeart, pv_rhythm;//平均心律进度，正常心律进度，节律正常范围进度
        LinearLayout ll_share;
    }

    public void clean() {
        this.al_data.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<HistoryData> historyDatas) {
        this.al_data.addAll(historyDatas);
        notifyDataSetChanged();
    }

}
