package com.uteamtec.heartcool.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.activity.AeroCardioHistoryDetailActivity;
import com.uteamtec.heartcool.fragment.model.HistoryCalendarItemAdapter;
import com.uteamtec.heartcool.fragment.model.HistoryCalendarItemModel;
import com.uteamtec.heartcool.fragment.model.HistoryListItemAdapter;
import com.uteamtec.heartcool.fragment.model.HistoryListItemModel;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.db.DBOrm;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.views.widget.CalendarListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * 主历史界面
 * Created by wd
 */
public abstract class HistoryFragment extends BaseFragment implements View.OnClickListener {

    private CalendarListView calendarListView;
    private HistoryCalendarItemAdapter calendarItemAdapter;
    private HistoryListItemAdapter listItemAdapter;

    private TextView txTitle;

    private final TreeMap<String, ArrayList<DBDetection>> data = new TreeMap<>(new Comparator<String>() {
        public int compare(String obj1, String obj2) {
            return obj2.compareTo(obj1);
        }
    });
    private TreeMap<String, List<HistoryListItemModel>> list = new TreeMap<>(new Comparator<String>() {
        public int compare(String obj1, String obj2) {
            return obj2.compareTo(obj1);
        }
    });

    public HistoryFragment() {
        super();
    }

    public HistoryFragment(BaseFragmentListener listener) {
        super(listener);
    }

    @Override
    protected int onCreateViewResource() {
        return R.layout.fragment_history;
    }

    @Override
    protected void initViews(View rootView) {
        if (hasRootView()) {
            calendarListView = (CalendarListView) getRootView().
                    findViewById(R.id.fragment_history_calendarlistview);
            calendarItemAdapter = new HistoryCalendarItemAdapter(getActivity());
            listItemAdapter = new HistoryListItemAdapter(getActivity());
            calendarListView.setCalendarListViewAdapter(calendarItemAdapter, listItemAdapter);
            calendarListView.setOnChangedListener(new CalendarListView.OnChangedListener() {
                @Override
                public void onRefresh() {
                    loadData();
                }

                @Override
                public void onMonthChanged(String yearMonth) {
                    loadData();
                    txTitle.setText(yearMonth);
                }

                @Override
                public void onItemClick(String date, Object obj) {
                    if (obj != null && obj instanceof HistoryListItemModel) {
                        final HistoryListItemModel model = (HistoryListItemModel) obj;
                        L.e("onItemClick " + date + " - " + model.getDetection().toString());
                        gotoHistoryDetailActivity(model.getDetection());
                    }
                }
            });
            listItemAdapter.setOnChangedListener(calendarListView.getOnChangedListener());

            txTitle = (TextView) getRootView().
                    findViewById(R.id.fragment_history_tx_year_month);
            txTitle.setText(calendarListView.getMonth());

            getRootView().findViewById(R.id.fragment_history_tv_left).setOnClickListener(this);
            getRootView().findViewById(R.id.fragment_history_tv_right).setOnClickListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();
    }

    @Override
    public void onServiceConnected() {
        if (!hasRootView()) {
            return;
        }
    }

    @Override
    public void onServiceDisconnected() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_history_tv_left:
                calendarListView.changeMonth(false);
                break;
            case R.id.fragment_history_tv_right:
                calendarListView.changeMonth(true);
                break;
        }
    }

    @Override
    protected String getPageName() {
        return HistoryFragment.class.getSimpleName();
    }

    private synchronized void loadData() {
        synchronized (data) {
            data.clear();
        }
        AppNetTcpComm.getEcg().queryAppEcgAnalysisByTime(
                User.getUser().getIdString(),
                calendarListView.getMonth(),
                new AppNetTcpCommListener<List<DBDetection>>() {
                    @Override
                    public void onResponse(boolean success, List<DBDetection> response) {
                        L.e("queryAppEcgAnalysisByTime -> success: " + success);
                        if (success && response != null) {
                            for (DBDetection d : response) {
                                L.e("queryAppEcgAnalysisByTime -> detection: " + d.toString());
                            }
                            parseData(response);
                        } else {
                            loadLocalData();
                        }
                    }
                });
    }

    private synchronized void loadLocalData() {
        synchronized (data) {
            data.clear();
        }
        new Thread() {
            @Override
            public void run() {
                ArrayList<DBDetection> ds = DBOrm.query(
                        new QueryBuilder<>(DBDetection.class)
                                .whereEquals("mac", BleFeComm.getClient().getMacAddress())
                                .whereAppendAnd()
                                .whereEquals("month", calendarListView.getMonth())
                                .appendOrderDescBy("id")
                );
                parseData(ds);
            }
        }.start();
    }

    private void parseData(List<DBDetection> ds) {
        if (ds == null) {
            return;
        }
        list.clear();
        synchronized (data) {
            String k;
            ArrayList<DBDetection> dl;
            List<HistoryListItemModel> hl;
            for (DBDetection d : ds) {
                k = d.getDate();

                dl = data.put(k, null);
                if (dl == null) {
                    dl = new ArrayList<>();
                }
                dl.add(d);
                data.put(k, dl);

                hl = list.put(k, null);
                if (hl == null) {
                    hl = new ArrayList<>();
                }
                hl.add(new HistoryListItemModel(d));
                list.put(k, hl);
            }
            for (List<DBDetection> l : data.values()) {
                Collections.reverse(l);
            }
            for (List<HistoryListItemModel> l : list.values()) {
                Collections.reverse(l);
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }

    private synchronized void refresh() {
        for (String k : data.keySet()) {
            HistoryCalendarItemModel item = calendarItemAdapter.getDayModelList().get(k);
            if (item != null) {
                item.setCount(data.get(k).size());
                item.setWarning(true);
            }
        }
        calendarItemAdapter.notifyDataSetChanged();

        listItemAdapter.setDateDataMap(list);
        listItemAdapter.notifyDataSetChanged();

        calendarListView.stopLoading();
    }

    private synchronized void gotoHistoryDetailActivity(final DBDetection detection) {
        if (detection == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), AeroCardioHistoryDetailActivity.class);
                if (detection.getId() > 0) {
                    intent.putExtra(DBDetection.class.getSimpleName(),
                            DBOrm.cascadeQueryById(detection.getId(), DBDetection.class));
                } else {
                    intent.putExtra(DBDetection.class.getSimpleName(), detection);
                }
                startActivity(intent);
            }
        }).start();
    }

}
