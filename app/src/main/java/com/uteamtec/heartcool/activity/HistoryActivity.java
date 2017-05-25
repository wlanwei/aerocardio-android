package com.uteamtec.heartcool.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.adapter.HistoryAdapter;
import com.uteamtec.heartcool.model.HistoryData;
import com.uteamtec.heartcool.model.User;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.user.ColorfulRingProgressView;
import com.uteamtec.heartcool.user.TimePickerView;
import com.uteamtec.heartcool.utils.ApiUrl;
import com.uteamtec.heartcool.utils.NetWorkUtils;
import com.uteamtec.heartcool.views.MainAeroCardioActivity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;


public class HistoryActivity extends BaseActivity implements View.OnClickListener, TimePickerView.CallBack, HistoryAdapter.onShareClick {

    private TimePickerView tpv_calendar;//日历
    private ColorfulRingProgressView pv_avghr, pv_normal, pv_rhythm;//平均心律进度，正常心律进度，正常节律范围进度
    private ImageView iv_lastMonth, iv_nextMonth;//上个月，这个月
    private TextView tv_year, tv_nowMonth, tv_avghr, tv_normal, tv_startDate, tv_endDate, tv_time, tv_more;//当年,这月数据,平均心律，正常心律，节律范围，记录时间，监测时长
    private int nowYear = 0;//今年
    private int nowMonth = 0;
    private int nowData[];//= new int[]{21,22,23};
    private int[] datess = new int[32];
    private View inflate;//历史记录的布局
    private ImageView iv_historyClose;//历史记录关闭和显示,关闭
    private ListView lv_history;//显示历史记录的列表
    private TextView tv_historyDate;//历史记录的日期
    private LinearLayout ll_layout;//整个布局
    private ArrayList<HistoryData> al_history = new ArrayList<>();//历史记录
    private LinearLayout ll_last, ll_next;//上个月，下个月
    private HistoryAdapter ha_adapter = null;
    private Dialog alertDialog;
    private LinearLayout rl_close;//关闭
    private boolean isExit = false;
    public static int BACK_CODE = 1; //返回键点击次数
    private LinearLayout ll_history_cancel, ll_havaRecord, ll_noRecord;//关闭dialog，有测试记录，无测试记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initUI();
        String date = DateFormats.YYYY_MM.format(new Date());
        String[] split = date.split("-");
        String year = split[0];//年份
        String monthes = split[1];//月份
        String month = monthes.replace("0", "");
        nowYear = Integer.parseInt(year);
        nowMonth = Integer.parseInt(month);
        setMonth();//设置月份
        getDate();
    }

    private void initUI() {
        inflate = LayoutInflater.from(this).inflate(R.layout.history_data, null);
        tv_historyDate = (TextView) inflate.findViewById(R.id.history_data_tv_date);
        ll_history_cancel = (LinearLayout) inflate.findViewById(R.id.history_data_ll_cancel);
        lv_history = (ListView) inflate.findViewById(R.id.history_data_lv_data);
        tpv_calendar = (TimePickerView) findViewById(R.id.activity_history_tpv_calendar);//日历
        pv_avghr = (ColorfulRingProgressView) findViewById(R.id.activity_history_rp_progressAvghr);//平均心律进度
        pv_normal = (ColorfulRingProgressView) findViewById(R.id.activity_history_rp_progressNormalheart);//正常心律进度
        //pv_rhythm = (ColorfulRingProgressView) findViewById(R.id.activity_history_rp_progressRhythm);//正常节律范围进度
        iv_lastMonth = (ImageView) findViewById(R.id.activity_history_iv_iv_lastMonth);//上个月
        iv_nextMonth = (ImageView) findViewById(R.id.activity_history_iv_iv_nextMonth);//下个月
        tv_year = (TextView) findViewById(R.id.activity_main_tv_year);//现在时间
        tv_nowMonth = (TextView) findViewById(R.id.activity_history_iv_tv_month);//这个月
        tv_avghr = (TextView) findViewById(R.id.activity_history_tv_avghr);//平均心律
        tv_normal = (TextView) findViewById(R.id.activity_history_tv_normalHeart);//正常心律
        tv_startDate = (TextView) findViewById(R.id.activity_history_tv_startDate);//节律范围
        tv_endDate = (TextView) findViewById(R.id.activity_history_tv_endDate);//日期

        tv_time = (TextView) findViewById(R.id.activity_history_tv_time);//时间
        tv_more = (TextView) findViewById(R.id.activity_history_tv_more);//更多数据
        //iv_close = (ImageView) findViewById(R.id.activity_history_iv_setting);//跳转到设置界面
        rl_close = (LinearLayout) findViewById(R.id.activity_history_ll_setting);//跳转到setting界面
        ll_layout = (LinearLayout) findViewById(R.id.activity_history_ll);
        ll_last = (LinearLayout) findViewById(R.id.activity_history_iv_ll_last);//上个月
        ll_next = (LinearLayout) findViewById(R.id.activity_history_iv_ll_next);//下个月
        ll_havaRecord = (LinearLayout) findViewById(R.id.activity_history_ll_haveData);
        ll_noRecord = (LinearLayout) findViewById(R.id.activity_history_ll_nodata);
        ll_layout.setLongClickable(true);
        ll_layout.setOnTouchListener(new MyGestureListener(this));
        tv_more.setOnClickListener(this);
        rl_close.setOnClickListener(this);
        ll_last.setOnClickListener(this);
        ll_next.setOnClickListener(this);
        alertDialog = new Dialog(HistoryActivity.this, R.style.Transparent);
        alertDialog.setContentView(inflate);
        ha_adapter = new HistoryAdapter(al_history, HistoryActivity.this, this);
        lv_history.setAdapter(ha_adapter);
        ll_havaRecord.setVisibility(View.INVISIBLE);
        ll_noRecord.setVisibility(View.VISIBLE);

    }

    @Override
    public void onShareItem(int position) {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题：微信、QQ（新浪微博不需要标题）
        oks.setTitle("优护心衣");  //最多30个字符

        // text是分享文本：所有平台都需要这个字段
        // oks.setText("哒哒影像~http://www.iinda.cn/");  //最多40个字符
        oks.setText("平均心律:" + al_history.get(position).getAvghr() + "\n正常心律范围" + al_history.get(position).getNormal() + "%");
        // imagePath是图片的本地路径：除Linked-In以外的平台都支持此参数
        //oks.setImagePath(Environment.getExternalStorageDirectory() + "/meinv.jpg");//确保SDcard下面存在此张图片

        //网络图片的url：所有平台
        oks.setImageUrl("http://www.lgstatic.com/thumbnail_300x300/image1/M00/00/2B/Cgo8PFTUXHGAFoeQAABomSrHT7w969.png");//网络图片rul

        // url：仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://www.b2b168.com/c168-12989004.html");   //网友点进链接后，可以看到分享的详情

        // Url：仅在QQ空间使用
        oks.setTitleUrl("http://www.b2b168.com/c168-12989004.html/");  //网友点进链接后，可以看到分享的详情

        // 启动分享GUI
        oks.show(this);
    }

    public void bluetoothHis(View view) {
        ApiUrl.msg = "2";
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }

    /**
     * 获取当月那些天存在历史记录
     */
    private void getDate() {
        tv_year.setText(Integer.toString(nowYear));
        if (NetWorkUtils.isNetworkConnected(this)) {
            FinalHttp fh = new FinalHttp();
            AjaxParams params = new AjaxParams();
            params.put("uid", User.getInstance().getUid());
            Log.i("info", "-----ccc--uid--" + User.getInstance().getUid());
            if (nowMonth < 10) {
                params.put("jlsj", nowYear + "-0" + nowMonth);

            } else {
                params.put("jlsj", nowYear + "-" + nowMonth);

            }
            fh.post(ApiUrl.DATE_SHOW, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String result) {
                    super.onSuccess(result);
                    Log.i("info", "-----ccchis--" + result);
                    try {
                        JSONObject object = new JSONObject(result);
                        String cbm = object.getString("cbm");
                        if (cbm.equals("ERR")) {
                            nowData = new int[]{-1};
                            setData();
                            ll_havaRecord.setVisibility(View.INVISIBLE);
                            ll_noRecord.setVisibility(View.VISIBLE);
                        } else {
                            ll_havaRecord.setVisibility(View.VISIBLE);
                            ll_noRecord.setVisibility(View.INVISIBLE);
                            JSONArray array = object.getJSONArray("cms");
                            for (int i = 0; i < datess.length; i++) {
                                datess[i] = 0;
                            }

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String jlsj = obj.getString("StartTime");
                                Log.i("info", "----jlsj--" + jlsj);

                                String[] split = jlsj.split("-");
                                String year = split[0];
                                String month = split[1];
                                String dates = split[2];
                                String[] split1 = dates.split(" ");
                                String s = split1[0];
                                datess[Integer.parseInt(s)] = 1;
                            }
                            int n = 0;
                            for (int i = 0; i < datess.length; i++) {
                                if (datess[i] == 1) {
                                    n++;
                                }
                            }

                            nowData = new int[n];
                            int count = 0;
                            for (int i = 0; i < datess.length; i++) {
                                if (datess[i] == 1) {

                                    nowData[count] = i;
                                    count++;
                                }
                            }
                            setData();//设置日历

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_history_ll_setting://关闭
                Intent intent = new Intent(HistoryActivity.this, SettingActivity.class);//跳转到设置界面
                ApiUrl.msg = "2";
                startActivity(intent);
                finish();
                break;
            case R.id.activity_history_iv_ll_last://上个月
                tpv_calendar.setClickindex(0);//设置点击当天为0，清空上一个操作

                if (nowMonth > 1) {
                    nowMonth--;
                } else {
                    nowYear--;
                    nowMonth = 12;
                }

                getDate();//加载上一个月数据
                cleanData();

                break;
            case R.id.activity_history_iv_ll_next://下个月
                tpv_calendar.setClickindex(0);//设置点击当天为0，清空上一个操作
                if (nowMonth > 11) {
                    nowMonth = 1;
                    nowYear++;
                } else {
                    nowMonth++;
                }
                getDate();//加载下一个月数据
                cleanData();
                break;
            case R.id.activity_history_tv_more://更多数据
                if (tv_historyDate.getText().toString().equals("")) {//如果查询日期为空，那么就不显示dialog
                    Toast.makeText(HistoryActivity.this, getString(R.string.searchDate), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.show();
                }

                ll_history_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                break;
        }
    }

    /**
     * 清空数据
     */
    private void cleanData() {
        ha_adapter.clean();
        tv_historyDate.setText("");//设置时间为空
        tv_startDate.setText(null);
        tv_endDate.setText(null);
        tv_time.setText(null);
        tv_avghr.setText(null);
        tv_normal.setText(null);
//       tv_rhythm.setText(null);
        pv_normal.setPercent(0);
//       pv_rhythm.setPercent(0);
    }

    //点击历史记录
    @Override
    public void cb(int nowday) {
        tpv_calendar.setClickindex(nowday);
        tpv_calendar.invalidate();
        if (ha_adapter != null) {//如果不为空，就清除数据再重新加载新的数据
            ha_adapter.clean();
            ha_adapter.addAll(al_history);
        }
        historyData(nowday);
    }

    /**
     * 请求点击当天的数据
     *
     * @param nowday
     */
    private void historyData(int nowday) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            FinalHttp fh = new FinalHttp();
            AjaxParams params = new AjaxParams();
            params.put("uid", User.getInstance().getUid());
            Log.i("info", "---uid--61" + User.getInstance().getUid());
            String jlsj;
            if (nowMonth < 10) {
                jlsj = nowYear + "-0" + nowMonth;
            } else {
                jlsj = nowYear + "-" + nowMonth;
            }
            if (nowday < 10) {
                jlsj = jlsj + "-0" + nowday;
            } else {
                jlsj = jlsj + "-" + nowday;
            }

            params.put("starttime", jlsj);
            fh.post(ApiUrl.DATE_SHOW, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String result) {
                    super.onSuccess(result);
                    Log.i("info mess", "----ccc--" + result);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(result);
                        String cms = object.getString("cbm");
                        if (cms.equals("OK")) {
                            al_history = jsonHistory(object);
                            String startTime = al_history.get(0).getStartTime();
                            String endTime = al_history.get(0).getStartTime();
                            String timeLenth = al_history.get(0).getTimeLenth();
                            String avghr = al_history.get(0).getAvghr();
                            String normal = al_history.get(0).getNormal();
                            tv_startDate.setText(startTime);//设置这天的最近记录的日期
                            tv_endDate.setText(endTime);//设置这天的最近记录的日期
                            tv_time.setText(timeLenth);//设置这天的最近记录的时长
                            tv_avghr.setText(avghr);//设置这天的最近记录的平均心律
                            tv_normal.setText(normal + "%");////设置这天的最近记录的正常心律范围
                            //tv_rhythm.setText(rhythm + "%");////设置这天的最近记录的节律范围
                            pv_normal.setPercent(Float.parseFloat(normal));
                            //pv_rhythm.setPercent(Float.parseFloat(rhythm));
                            String date1 = al_history.get(0).getStartTime();
                            String[] split = date1.split(" ");
                            String s = split[0];
                            //String replace = s.replace("-", ".");
                            tv_historyDate.setText(s);
                            ha_adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(HistoryActivity.this, object.getString("cms"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 解析历史数据
     */

    private ArrayList<HistoryData> jsonHistory(JSONObject object) {
        try {
            // JSONObject object = new JSONObject(result);
            String cms = object.getString("cbm");
            JSONArray array = object.getJSONArray("cms");
            HistoryData historyData = null;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                historyData = new HistoryData();
                String startTime = obj.getString("StartTime");
                String endTime = obj.getString("OverTime");
                String[] split = startTime.split("-");
                /*String date1 = split[0];
                String date2 = split[1];
                String date3 = split[2];
                String date4 = split[3];
                String date5 = split[4];
                String date6 = split[5];
                String date = date1 + "-" + date2 + "-" + date3+" "+date4+":"+date5+":"+date6;*/
                historyData.setStartTime(startTime);//记录开始时间
                historyData.setEndTime(endTime);
                historyData.setTimeLenth(obj.getString("JCSC"));//记录时长
                historyData.setAvghr(obj.getString("PJXL"));//平均心律
                historyData.setNormal(obj.getString("XLFW"));//心律范围
                // historyData.setRhythm(obj.getString("JLFW"));//节律范围
                historyData.setFeedBack(obj.getString("JCFK"));//监测反馈
                historyData.setException(obj.getString("YCZB"));//异常指标
                historyData.setSleep(obj.getString("SCORE"));
                al_history.add(historyData);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return al_history;
    }


    private void setData() {
        tpv_calendar.setData(nowYear, nowMonth, nowData, this);
        setMonth();
    }

    private void setMonth() {//把对应数字月份转化成为大写月份
        if (nowMonth == 1) {
            tv_nowMonth.setText(getString(R.string.January));
        } else if (nowMonth == 2) {
            tv_nowMonth.setText(getString(R.string.Febuary));
        } else if (nowMonth == 3) {
            tv_nowMonth.setText(getString(R.string.March));
        } else if (nowMonth == 4) {
            tv_nowMonth.setText(getString(R.string.April));
        } else if (nowMonth == 5) {
            tv_nowMonth.setText(getString(R.string.May));
        } else if (nowMonth == 6) {
            tv_nowMonth.setText(getString(R.string.June));
        } else if (nowMonth == 7) {
            tv_nowMonth.setText(getString(R.string.July));
        } else if (nowMonth == 8) {
            tv_nowMonth.setText(getString(R.string.August));
        } else if (nowMonth == 9) {
            tv_nowMonth.setText(getString(R.string.September));
        } else if (nowMonth == 10) {
            tv_nowMonth.setText(getString(R.string.October));
        } else if (nowMonth == 11) {
            tv_nowMonth.setText(getString(R.string.November));
        } else if (nowMonth == 12) {
            tv_nowMonth.setText(getString(R.string.December));
        }

    }

    /**
     * 继承GestureListener，重写left和right方法
     */
    private class MyGestureListener extends GestureListener {
        public MyGestureListener(Context context) {
            super(context);
        }

        @Override
        public boolean left() {

            return super.left();
        }

        @Override
        public boolean right() {
            startActivity(new Intent(HistoryActivity.this, MainAeroCardioActivity.class));
            finish();
            //两个界面切换的动画
            overridePendingTransition(R.anim.tran_previous_in, R.anim.tran_previous_out);//进入动画和退出动画

            return super.right();
        }
    }

    //拦截系统返回键
    @Override
    public void onBackPressed() {
        if (BACK_CODE == 2) {
            BACK_CODE = 1;
            //返回键退出程序
            HistoryActivity.this.finish();
            System.exit(0);
            return;
        }
        Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
        BACK_CODE++;
    }
}
