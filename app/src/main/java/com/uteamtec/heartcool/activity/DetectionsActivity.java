package com.uteamtec.heartcool.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.model.Contact;
import com.uteamtec.heartcool.model.User;
import com.uteamtec.heartcool.receive.ReportReceive;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.user.HeartCoolBrowser;
import com.uteamtec.heartcool.utils.ApiUrl;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class DetectionsActivity extends BaseActivity implements View.OnClickListener {

    BluetoothAdapter bluetoothAdapter;  //蓝牙填充器
    //    TextView tv;
    TGDevice tgDevice;
    final boolean rawEnabled = false;
    HeartCoolBrowser heartCoolBrowser;
    private int secondsTime = 0, minutesTime = 0, hoursTime = 0;//时间秒数，时间分钟数，时间小时数
    private int seconds = 0, totalHeartRate = 0, normalHeartRates = 0;

    private TextView realtimeheartrate_tv, averageHeartRate_tv, normalRange_tv, suspectedRisk_tv, hour_tv, minute_tv, seconds_tv, detection_time_tv;//即时心律，平均心律，实时呼吸率，节律正常范围,
    private LinearLayout setting_rl;//设置监听布局

    private Button mButtonRecordStart;//开始记录
    private Button mButtonRecordStop;//停止记录

    private boolean isDetectioning = false;  //是否正在监测
    private String recoretime; //开始监测时候的时间
    private String secondstr; //监测时长

    private MediaPlayer mp_mediaPlay;//音乐播放器

    private String id, sendSms, callPhone;//用户id,用户姓名，发短信，打电话，发邮件
    private ArrayList<Contact> al_coantact = null;
    private Contact contact;
    private SharedPreferences sf;
    private SharedPreferences.Editor edit;//保存发送短信的时间
    private String contactName, contactPhone, contactEmail, contactOpt;//联系人姓名，联系人电话，联系人邮箱，联系人状态
    public ArrayList<String> al_list = new ArrayList<>();
    public int size;
    private View detectionView;
    private Dialog dialog;
    private LinearLayout ll_dialog_close;//关闭dialog
    private TextView tv_dialog_startDate, tv_dialog_endDate, tv_dialog_time, tv_dialog_detail;//日期，时长，详情

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        bindViewById();//绑定控件

        sf = getSharedPreferences("info", MODE_PRIVATE);
        edit = sf.edit();

        //需要监听左右滑动事件的view
        LinearLayout view = (LinearLayout) this.findViewById(R.id.detection_layout);
        view.setLongClickable(true);
        view.setOnTouchListener(new MyGestureListener(this));
        //获取当前日期
        detection_time_tv.setText(DateFormats.YYYY_MM_CN.format(new Date()));
        //设置按钮监听
        setting_rl = (LinearLayout) findViewById(R.id.activity_detection_ll_setting);
        setting_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // startActivityForResult(new Intent(DetectionActivity.this,SettingActivity.class),1);
                if (tgDevice.getState() == TGDevice.STATE_CONNECTED || tgDevice.getState() == TGDevice.STATE_CONNECTING) {
                    Toast.makeText(getApplicationContext(), getString(R.string.stop), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(DetectionsActivity.this, SettingActivity.class);
                    ApiUrl.msg = "1";
                    startActivity(intent);
                    finish();
                }

            }
        });


        //蓝牙
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            /* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
        }


        mp_mediaPlay = MediaPlayer.create(this, R.raw.waring);//加载报警音乐资源
        try {
            mp_mediaPlay.prepare();//预准备
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void bluetooth(View view) {
        ApiUrl.msg = "1";
        startActivity(new Intent(DetectionsActivity.this, SettingActivity.class));

        finish();
    }

    //绑定控件
    private void bindViewById() {
        mButtonRecordStart = (Button) findViewById(R.id.detectionstart_btn);
        mButtonRecordStop = (Button) findViewById(R.id.detectionstop_btn);
//        hour_tv = (TextView) findViewById(R.id.hour_tv);
//        minute_tv = (TextView) findViewById(R.id.minute_tv);
//        seconds_tv = (TextView) findViewById(R.id.seconds_tv);//记录时间

        detection_time_tv = (TextView) findViewById(R.id.detection_time_tv); //获取当前的年月日时间
        heartCoolBrowser = (HeartCoolBrowser) findViewById(R.id.heartCoolBrowser);


        realtimeheartrate_tv = (TextView) findViewById(R.id.heartrate_realtime_tv);//即时心律
        averageHeartRate_tv = (TextView) findViewById(R.id.heartrate_average_tv);//平均心律
        normalRange_tv = (TextView) findViewById(R.id.normalRange_tv);//心律正常范围
        suspectedRisk_tv = (TextView) findViewById(R.id.suspectedRisk_tv);//节律正常范围
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        detectionView = LayoutInflater.from(this).inflate(R.layout.detection_dialog, null);
        ll_dialog_close = (LinearLayout) detectionView.findViewById(R.id.detection_dialog_ll_close);
        tv_dialog_startDate = (TextView) detectionView.findViewById(R.id.ddetection_dialog_tv_startDate);
        tv_dialog_endDate = (TextView) detectionView.findViewById(R.id.ddetection_dialog_tv_endDate);
        tv_dialog_time = (TextView) detectionView.findViewById(R.id.ddetection_dialog_tv_time);
        tv_dialog_detail = (TextView) detectionView.findViewById(R.id.detection_dialog_tv_detail);
        ll_dialog_close.setOnClickListener(this);
        tv_dialog_detail.setOnClickListener(this);

        builder.setView(detectionView);
        dialog = builder.create();
    }

    /**
     * 提示dialog
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detection_dialog_ll_close:
                dialog.dismiss();
                break;
            case R.id.detection_dialog_tv_detail:
                startActivity(new Intent(this, HistoryActivity.class));
                this.finish();
                break;
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
            //ToastText("向左滑");
            if (tgDevice.getState() == TGDevice.STATE_CONNECTED || tgDevice.getState() == TGDevice.STATE_CONNECTING) {
                Toast.makeText(getApplicationContext(), getString(R.string.stop), Toast.LENGTH_SHORT).show();
                return false;
            } else {
                startActivity(new Intent(DetectionsActivity.this, HistoryActivity.class));
                finish();
                //两个界面切换的动画
                overridePendingTransition(R.anim.tran_in, R.anim.tran_out);//进入动画和退出动画
            }


            return super.left();
        }

        @Override
        public boolean right() {
            //ToastText("向右滑");

            return super.right();
        }
    }

    /**
     * Handles messages from TGDevice
     */
    ProgressDialog progressDialog;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            progressDialog = ProgressDialog.show(DetectionsActivity.this, "Loading...", "Please wait...", true, false);

                            printfLog(tgDevice.getState() + "");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            printfLog("Connected.");
                            progressDialog.dismiss();
                            tgDevice.start();
                            printfLog(tgDevice.getState() + "");
                            printfLog(tgDevice.getState() + "");
                            isDetectioning = true;
                            mButtonRecordStart.setVisibility(View.GONE);
                            mButtonRecordStop.setVisibility(View.VISIBLE);
                            handler2.postDelayed(runnable, 1000);//每两秒执行一次runnable

                            recoretime = DateFormats.YYYY_MM_DD_HH_MM_SS.format(new Date());    //开始监测的时间

                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            printfLog("Can't find");
                            Toast.makeText(DetectionsActivity.this, getString(R.string.noConnect), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            printfLog("not paired");

                            break;
                        case TGDevice.STATE_DISCONNECTED:
//                            tv.append("Disconnected mang\n");
                            printfLog("Disconnected mang");
                            printfLog(tgDevice.getState() + "");

                            mButtonRecordStart.setVisibility(View.VISIBLE);
                            mButtonRecordStop.setVisibility(View.GONE);
                            handler2.removeCallbacks(runnable);

                            String yczb = null;   //心律异常指标
                            String jcfk;   //心律监测反馈
                            String smzl = null;//睡眠质量

                            int averageHeartRate = Integer.parseInt(averageHeartRate_tv.getText().toString());
                            if (averageHeartRate <= 0) {
                                yczb = getString(R.string.heartExp);//心律异常
                                jcfk = getString(R.string.arrest);//心脏骤停
                                judgeSend();//紧急发送方式
                                playMusic();//当心律为0时，发送报警音乐
                            } else if (averageHeartRate <= 100 && averageHeartRate >= 84 || averageHeartRate <= 48) {
                                yczb = getString(R.string.heartExp);//心律异常
                                jcfk = getString(R.string.scope);//部分指标不在正常范围内
                            } else if (averageHeartRate > 100) {
                                yczb = getString(R.string.heartExp);//心律异常
                                jcfk = getString(R.string.fibrillation);//房颤
                            } else {
                                jcfk = getString(R.string.indicators);//各项指标均在正常范围内
                                yczb = getString(R.string.no);//无
                            }


                            FinalHttp fh = new FinalHttp();
                            AjaxParams params = new AjaxParams();
                            params.put("uid", User.getInstance().getUid()); //用户ID
                            params.put("jlsj", recoretime); //记录时间
                            secondstr = hour_tv.getText().toString() + minute_tv.getText().toString() + seconds_tv.getText().toString();
                            params.put("jcsc", secondstr); //监测时长
                            params.put("jcfk", jcfk); //监测反馈
                            params.put("yczb", yczb); //异常指标
                            params.put("pjxl", String.valueOf(averageHeartRate)); //平均心律
                            params.put("xlfw", normalRange_tv.getText().toString()); //心律范围
                            params.put("jlfw", suspectedRisk_tv.getText().toString()); //节律范围
//                            if (Integer.parseInt(format) > 22 || Integer.parseInt(format) < 8) {
                            if (averageHeartRate <= 100 && averageHeartRate >= 50) {
                                smzl = getString(R.string.sleepQuality);
                                params.put("smzl", smzl);//睡眠质量
                            } else {
                                smzl = getString(R.string.no);
                                params.put("smzl", smzl);//睡眠质量
                            }
                            edit.putString("pjxl", getString(R.string.avaghr) + ":" + String.valueOf(averageHeartRate)).commit();//保存报告的平均心律
                            edit.putString("smzl", getString(R.string.sleep) + smzl).commit();//保存报告的睡眠质量
                            report();
                            fh.post(ApiUrl.SAVE_RECORD, params, new AjaxCallBack<String>() {
                                @Override
                                public void onSuccess(String result) {
                                    super.onSuccess(result);
                                    String str = result.substring(result.indexOf("{"), result.length());
                                    try {
                                        JSONObject object = new JSONObject(str);
                                        if (object.getString("cbm").equals("OK")) {
                                        } else {
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                    }

                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    break;
                case TGDevice.MSG_RAW_DATA:
                    break;
                case TGDevice.MSG_HEART_RATE:
                    seconds++;
                    realtimeheartrate_tv.setText(msg.arg1 + "");
                    heartCoolBrowser.addData((short) msg.arg1);
                    totalHeartRate = totalHeartRate + msg.arg1;
                    averageHeartRate_tv.setText(totalHeartRate / seconds + "");

                    if (msg.arg1 >= 60 && msg.arg1 <= 100) {
                        normalHeartRates++;
                    }
                    normalRange_tv.setText((normalHeartRates * 100) / seconds + "");
                    suspectedRisk_tv.setText((normalHeartRates * 100) / seconds + "");

                    break;
                case TGDevice.MSG_ATTENTION:
                    break;
                case TGDevice.MSG_MEDITATION:

                    break;
                case TGDevice.MSG_BLINK:
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                default:
                    break;
            }
        }
    };

    public void doStuff(View view) { //开始监测

        //初始化数据
        seconds = 0;
        totalHeartRate = 0;
        normalHeartRates = 0;
        secondsTime = 0;
        minutesTime = 0;
        hoursTime = 0;
        heartCoolBrowser.clearData();

        if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            tgDevice.connect(rawEnabled);
        //tgDevice.ena
    }

    public void doStop(View view) {   //停止监测
        if (tgDevice.getState() == TGDevice.STATE_CONNECTING) {
            tgDevice.close();
            printfLog("正在运行");
        }
        tgDevice.close();


        isDetectioning = false;
        handler2.removeCallbacks(runnable);


    }

    //发送报告
    private void report() {
        TimePicker timePicker = new TimePicker(DetectionsActivity.this);
        timePicker.setCurrentHour(8);
        timePicker.setCurrentMinute(30);
        String hour = String.valueOf(timePicker.getCurrentHour()) + ":" + String.valueOf(timePicker.getCurrentMinute());
        edit.putString(hour, hour).commit();
        AlarmManager aManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReportReceive.class);
        intent.setAction("sendSMS");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        // aManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        aManager.setRepeating(AlarmManager.RTC, 0, 60 * 1000, pendingIntent);
       /*Intent intent=new Intent(DetectionActivity.this, ReportService.class);
        startService(intent);*/
        judgeContact(1);//获取联系人号码，判断是紧急情况还是发送报告要获取的联系人信息 0紧急情况 1报告

    }

    //紧急发送方式
    private void judgeSend() {
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("uid", User.getInstance().getUid());
        fh.post(ApiUrl.SEND_WAY, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                jsonSend(s);
                judgeContact(0);//紧急联系人,//判断是紧急情况还是发送报告要获取的联系人信息 0紧急情况 1报告
            }
        });
    }

    //解析数据
    private void jsonSend(String result) {
        try {
            JSONObject object = new JSONObject(result);
            String cbm = object.getString("cbm");
            JSONObject obj = object.getJSONObject("cms");
            id = obj.getString("ID");
            sendSms = obj.getString("FSDX");
            callPhone = obj.getString("ZDBD");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    /**
//     * 紧急情况发送邮件
//     *
//     * @param id
//     * @param name
//     * @param email
//     * @param content
//     */
//    private void sendEmail(String id, String name, String email, String content) {
//        FinalHttp fh = new FinalHttp();
//        AjaxParams params = new AjaxParams();
//        params.put("uid", id);
//        params.put("name", name);
//        params.put("email", email);
//        params.put("content", content);
//        fh.post(ApiUrl.EMER_EMAIL, params, new AjaxCallBack<String>() {
//            @Override
//            public void onSuccess(String s) {
//                super.onSuccess(s);
//                Log.i("email", "-----email----" + s);
//            }
//        });
//    }

    /**
     * 解析数据
     *
     * @param result
     * @return
     */
    private ArrayList<Contact> jsonData(String result) {
        try {
            JSONObject object = new JSONObject(result);
            String cbm = object.getString("cbm");
            JSONArray array = object.getJSONArray("cms");
            al_coantact = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                contact = new Contact();
                JSONObject obj = array.getJSONObject(i);
                contact.setId(obj.getInt("ID"));
                contact.setName(obj.getString("NAME"));
                contact.setPhone(obj.getString("PHONE"));
                contact.setEmail(obj.getString("EMAIL"));
                contact.setCondition(obj.getString("OPT"));
                al_coantact.add(contact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return al_coantact;
    }

    private void judgeContact(final int type) {
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("uid", User.getInstance().getUid());
        fh.post(ApiUrl.GET_CONTACTS, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                al_coantact = jsonData(s);
                for (int i = 0; i < al_coantact.size(); i++) {
                    contactPhone = al_coantact.get(i).getPhone();
                    contactEmail = al_coantact.get(i).getEmail();
                    contactOpt = al_coantact.get(i).getCondition();
                    contactName = al_coantact.get(i).getName();
                    if (contactOpt.equals("1")) {
                        Log.i("info", "----contactphone--" + contactPhone);
                        if (type == 1) {
                            al_list.add(contactPhone);
                            size = al_list.size();
                            edit.putInt("size", size).commit();
                            edit.putString("phoneNumber" + size, contactPhone).commit();
                            Log.i("info", "----jjjj--" + al_list.size());
                            Log.i("info", "----jjjj--" + al_list);
                            Log.i("info", "----jjjj--" + contactPhone);
                        } else if (type == 0) {
                            if (callPhone.equals("1")) {//打电话为1，就给紧急联系人拨打电话
                                // Toast.makeText(DetectionActivity.this, contactPhone+"----"+callPhone, Toast.LENGTH_SHORT).show();
                                Intent intentCall = new Intent(Intent.ACTION_CALL);
                                intentCall.setAction(Intent.ACTION_CALL);//创建打电话的意图对象
                                intentCall.setData(Uri.parse("tel:" + contactPhone));
                                if (ActivityCompat.checkSelfPermission(DetectionsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                startActivity(intentCall);
                            }
                            if (sendSms.equals("1")) {//发送短信为1，就给紧急联系人发送短信
                                SmsManager manager = SmsManager.getDefault();
                                manager.sendTextMessage(contactPhone, null, getString(R.string.care), null, null);

                            }

//                            if (email.equals("1")) {//发邮件为1，就给紧急联系人发送邮件
//
//                                sendEmail(User.getInstance().getUid(), contactName, contactEmail, getString(R.string.care));
//
//                            }
                        }


                    }
                }
            }
        });
    }

    //开始报警
    private void playMusic() {
        if (mp_mediaPlay != null) {
            if (!mp_mediaPlay.isPlaying()) {
                mp_mediaPlay.start();
            }
        }

    }

    @Override
    public void onDestroy() {
//        tgDevice.close();
        if (tgDevice.getState() == TGDevice.STATE_CONNECTED || tgDevice.getState() == TGDevice.STATE_CONNECTING) {
            tgDevice.close();
            isDetectioning = false;
        }

        mp_mediaPlay.release();//释放音乐资源
        mp_mediaPlay = null;
        super.onDestroy();
    }

    public void printfLog(String s) {
        Log.i("DetectionsActivity-->", s);
    }

    Handler handler2 = new Handler();  //记录时间的handler
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //要做的事情
            if (isDetectioning) {
                printfLog("时间（秒）--》" + secondsTime);
                secondsTime++;
                if (secondsTime == 60) {
                    secondsTime = 0;
                    minutesTime++;
                }
                if (minutesTime == 60) {
                    minutesTime = 0;
                    hoursTime++;
                }
                if (hoursTime == 24) {
                    hoursTime = 0;
                }
                hour_tv.setText(conversionIntToString(hoursTime));
                minute_tv.setText(":" + conversionIntToString(minutesTime));
                seconds_tv.setText(":" + conversionIntToString(secondsTime));
                handler.postDelayed(this, 1000);
            }
        }
    };

    public String conversionIntToString(int x) {
        if (x < 10) {
            return "0" + x;
        } else {
            return x + "";
        }
    }


}
