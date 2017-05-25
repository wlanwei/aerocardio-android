package com.uteamtec.heartcool.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.BaseActivity;
import com.uteamtec.heartcool.MainMgrService;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.activity.GestureListener;
import com.uteamtec.heartcool.activity.HistoryActivity;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.listener.DataReceivedListener;
import com.uteamtec.heartcool.service.listener.DetectionListener;
import com.uteamtec.heartcool.service.listener.EcgMarkListener;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.listener.UserStateChangedListener;
import com.uteamtec.heartcool.service.major.DetectionService;
import com.uteamtec.heartcool.service.stats.EcgMarkReport;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.utils.CrashHandler;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.views.types.Record;
import com.uteamtec.heartcool.views.widget.EcgView;
import com.uteamtec.heartcool.views.widget.RingMeterView;
import com.uteamtec.heartcool.views.widget.WarningView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 测试BLE数据接收与发送的测试界面
 */
public class MainAeroCardioActivity extends BaseActivity {

    /*
     * For drawer menu
     */
    private DrawerLayout drawerLayout;
    private ListView drawer;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolBar;

    private String userName;
    private TextView userNameDrawer; //uer name display in drawer header


    ProgressBar reconnectProgress;
    TextView reconnectInfo;
    /*
     * data viewer
     */
    private static final int DEFAULT_DRAWDATA_LEN = 1000;
    RingMeterView brMeter;
    RingMeterView hrMeter;
    EcgView ecgView;
    BlockingQueue<Float> data;

    ListView recordList;
    List<Record> records;
//    RecordAdapter recordAdapter;

//    GridView warningList;
//    LinkedList<Warning> warnings;
//    WarningAdapter warningAdapter;

    ServiceConnection serviceConn;
    MainMgrService mainService;

    WarningCheckThread warningChkThrd;

    private ImageView buletooth_iv, setting_iv;

    private TextView realtimeheartrate_tv, averageHeartRate_tv, normalRange_tv, suspectedRisk_tv, detection_time_tv;//即时心律，平均心律，心律正常范围，节律正常范围,
    private boolean isDetectioning = false;  //是否正在监测
    private Button mButtonRecordStart;//开始记录
    private Button mButtonRecordStop;//停止记录
    private SharedPreferences.Editor edit;//保存发送短信的时间
    private SharedPreferences sf;

    private WarningView mWarningViewHard;
    private WarningView mWarningViewSoft;

    private TextView txTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("调用创建方法-->", "开始创建");

        super.onCreate(savedInstanceState);
        onCreated(R.layout.activity_main);

        isDetectioning = true;

        sf = getSharedPreferences("info", MODE_PRIVATE);
        edit = sf.edit();

        findView();
        init();

        //开始监测状态
        ecgView.resumeDraw();
        isDetectioning = true;
        mButtonRecordStart.setVisibility(View.GONE);
        mButtonRecordStop.setVisibility(View.VISIBLE);
    }

    @Override
    protected void init() {
        Log.i("初始化界面-->", "开始初始化");
        CrashHandler.getInstance().init(this);

        DetectionService.startRecord();

        serviceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mainService = ((MainMgrService.MainMgrBinder) service).getService();

                ListenerMgr.registerDetectionListener(new DetectionListener() {
                    @Override
                    public void onStart() {
                        onTimerTick("00:00:00");
                    }

                    @Override
                    public void onStop() {
                        DetectionService.analyzeRecord();
                    }

                    @Override
                    public void onAnalyze(EcgMarkReport report) {
                        if (report == null) {
                            return;
                        }
//                        if (report.averageHR <= 0) { // TODO: 这种判定不可靠
//                            AppNetTcpComm.sendJudge();
//                            WaringAlarm.playWaring(MainAeroCardioActivity.this);
//                        }

                        edit.putString("pjxl", getString(R.string.avaghr) + ":" + report.pjxl).commit();//保存报告的平均心律
                        edit.putString("smzl", getString(R.string.sleep) + report.smzl).commit();//保存报告的睡眠质量

                        // ================================这里是本地保存数据================================
                        DetectionService.saveRecord();

                        // ================================这里是网络上传数据================================
//                        AppNetTcpComm.sendDetectionReport(report);
                    }

                    @Override
                    public void onSave(DBDetection detection) {
                    }

                    @Override
                    public void onTimerTick(final String hms) {
                        if (isDetectioning) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txTimer.setText(hms);
                                }
                            });
                        }
                    }
                });

                ListenerMgr.registerDataReceivedListener(new DataReceivedListener() {
                    @Override
                    public void onReceivedEcgRaw(Ecg ecg) {
//                        L.i("<UI> ecg type = " + Integer.toString(ecg.getType()));
                        int drawType;
                        if (ecg.getType() == Ecg.TYPE_SINGLE) {
                            drawType = EcgView.DRAWTYPE_1;
                        } else if (ecg.getType() == Ecg.TYPE_THREE) {
                            drawType = EcgView.DRAWTYPE_3;
                        } else {
                            drawType = EcgView.DRAWTYPE_1;
                        }
//                            L.i("<UI> changing draw type = " + Integer.toString(ecgView.getDrawType()));
                        if (ecgView.getDrawType() != drawType) {
                            ecgView.resetDrawType(drawType);
                            data = new ArrayBlockingQueue<Float>(DEFAULT_DRAWDATA_LEN * ecgView.getDrawType());
                            for (int m = 0; m < DEFAULT_DRAWDATA_LEN * ecgView.getDrawType(); m++) {
                                data.add((float) 0);
                            }
                            ecgView.bindData(data);
                        }
                        int dataIn[] = ecg.getData();
                        for (int m = 0; m < dataIn.length; m++) {
                            try {
                                data.take();
                                data.put((float) dataIn[m]);
//                                Log.i("数据》",dataIn[m]+"");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onReceivedEcgFiltered(Ecg ecg) {
//                        L.i("<UI> received filtered ecg");
                    }

                    @Override
                    public void onReceivedMark(EcgMark mark) {
                    }

                    @Override
                    public void onInfo(String info) {
                    }
                });

                ListenerMgr.registerEcgMarkListener(new EcgMarkListener() {
                    @Override
                    public void onMarkUpdated() {
                    }

                    @Override
                    public void onMarkLeadOff(String msg) {
                        mWarningViewHard.showWarningUI(MainAeroCardioActivity.this,
                                WarningView.WarningType.LEADOFF);
                    }

                    @Override
                    public void onMarkLowPower(String msg) {
                    }

                    @Override
                    public void onMarkShort(String msg) {
                    }

                    @Override
                    public void onMarkUnplug(String msg) {
                    }

                    @Override
                    public void onMarkHR(final int hr, final boolean hrWarn,
                                         final int hrAverage,
                                         final int hrHealth, final boolean healthWarn) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hrMeter.setValue(hr);
                                if (isDetectioning) {
                                    realtimeheartrate_tv.setText("" + hr);
                                    averageHeartRate_tv.setText("" + hrAverage);  //平均心律
                                    normalRange_tv.setText("" + hrHealth);   //正常
                                }
                            }
                        });
                    }

                    @Override
                    public void onMarkBR(final int br) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                brMeter.setValue(br);
                                if (isDetectioning) {
                                    suspectedRisk_tv.setText("" + br);
                                }
                            }
                        });
                    }

                    @Override
                    public void onMarkNoise(String msg) {
                        mWarningViewSoft.showWarningUI(MainAeroCardioActivity.this,
                                WarningView.WarningType.NOISE);
                    }
                });

//                L.i("<UI> main connected service, set on user state changed");
                ListenerMgr.registerUserStateChangedListener(new UserStateChangedListener() {
                    @Override
                    public void onDeviceRegistered(UserDevice device, int regResult) {
                    }

                    @Override
                    public void onDeviceActivated(UserDevice device, int activateResult) {
                    }

                    @Override
                    public void onLogin(int loginResult) {
                    }

                    @Override
                    public void onAppStateChanged(int state) {
                        L.i("<UI> server state changed = " + Integer.toString(state));
                        if (state == User.APPSTATE_DISCONNECTED) {
//                            L.i("<UI> server disconnected");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reconnectProgress.setVisibility(View.VISIBLE);
                                    reconnectInfo.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reconnectProgress.setVisibility(View.INVISIBLE);
                                    reconnectInfo.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFeStateChanged(int state) {
                        if (state == User.FESTATE_DISCONNECTED) {
                            ecgView.pauseDraw();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reconnectProgress.setVisibility(View.VISIBLE);
                                    reconnectInfo.setVisibility(View.VISIBLE);
//                                    L.i("<UI> ble disconnected");
                                }
                            });
                        } else if (state == User.FESTATE_CONNECTED || state == User.FESTATE_REGISTERED) {
                            ecgView.resumeDraw();
                            reconnectProgress.setVisibility(View.INVISIBLE);
                            reconnectInfo.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent intent = new Intent(MainAeroCardioActivity.this, MainMgrService.class);
        bindService(intent, serviceConn, BIND_AUTO_CREATE);

//        warnings = new LinkedList<>();
//        warningAdapter = new WarningAdapter(MainAeroCardioActivity.this, R.layout.item_warning, warnings);

        records = new LinkedList<>();
//        recordAdapter = new RecordAdapter(MainAeroCardioActivity.this, R.layout.item_record, records);

        data = new ArrayBlockingQueue<>(DEFAULT_DRAWDATA_LEN);
        for (int m = 0; m < DEFAULT_DRAWDATA_LEN; m++) {
            data.add((float) 0);
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
//            if (tgDevice.getState()== TGDevice.STATE_CONNECTED||tgDevice.getState()==TGDevice.STATE_CONNECTING) {
//                Toast.makeText(getApplicationContext(),getString(R.string.stop),Toast.LENGTH_SHORT).show();
//                return false;
//            }else {
            startActivity(new Intent(MainAeroCardioActivity.this, HistoryActivity.class));
            finish();
            //两个界面切换的动画
            overridePendingTransition(R.anim.tran_in, R.anim.tran_out);//进入动画和退出动画
//            }


            return super.left();
        }

        @Override
        public boolean right() {
            //ToastText("向右滑");
            return super.right();
        }
    }

    @Override
    protected void findView() {

        //Drawer and toolBar
        toolBar = (Toolbar) findViewById(R.id.menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer = (ListView) findViewById(R.id.menu_drawer);

        buletooth_iv = (ImageView) findViewById(R.id.detection_iconbuletooth);
        buletooth_iv.setImageResource(R.drawable.b_bluetooths);
        setting_iv = (ImageView) findViewById(R.id.detection_iconsetting);
//        setting_iv.setImageResource(R.drawable.d_setting);
        realtimeheartrate_tv = (TextView) findViewById(R.id.heartrate_realtime_tv);//即时心律
        averageHeartRate_tv = (TextView) findViewById(R.id.heartrate_average_tv);//平均心律
        normalRange_tv = (TextView) findViewById(R.id.normalRange_tv);//心律正常范围
        suspectedRisk_tv = (TextView) findViewById(R.id.suspectedRisk_tv);//实时呼吸率
        detection_time_tv = (TextView) findViewById(R.id.detection_time_tv);//当天时间
        txTimer = (TextView) findViewById(R.id.main_tx_timer);// 记录时间
        detection_time_tv.setText(DateFormats.YYYY_MM_CN.format(new Date()));

        mWarningViewHard = (WarningView) findViewById(R.id.wv_hard);
        mWarningViewSoft = (WarningView) findViewById(R.id.wv_soft);

        mButtonRecordStart = (Button) findViewById(R.id.detectionstart_btn);
        mButtonRecordStop = (Button) findViewById(R.id.detectionstop_btn);

        mButtonRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ecgView.resumeDraw();
                isDetectioning = true;
                mButtonRecordStart.setVisibility(View.GONE);
                mButtonRecordStop.setVisibility(View.VISIBLE);

                DetectionService.resetRecord();
            }
        });
        mButtonRecordStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ecgView.pauseDraw();
                isDetectioning = false;
                mButtonRecordStart.setVisibility(View.VISIBLE);
                mButtonRecordStop.setVisibility(View.GONE);

                DetectionService.stopRecord();
            }
        });


        //需要监听左右滑动事件的view
        DrawerLayout view = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        view.setLongClickable(true);
        view.setOnTouchListener(new MyGestureListener(this));


        buletooth_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ActivityManager activityManager = (ActivityManager) getApplication()
//                        .getSystemService(Context.ACTIVITY_SERVICE);
//                List<ActivityManager.RunningServiceInfo> serviceList = activityManager
//                        .getRunningServices(Integer.MAX_VALUE);
//                if (serviceList.size()>0){
//                    Toast.makeText(MainAeroCardioActivity.this,"正在监测不能跳转",Toast.LENGTH_SHORT).show();
//                }else {
//
//                }
                startActivity(new Intent(MainAeroCardioActivity.this, SettingActivity.class));


            }
        });
//
//        setting_iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                ActivityManager activityManager = (ActivityManager) getApplication()
////                        .getSystemService(Context.ACTIVITY_SERVICE);
////                List<ActivityManager.RunningServiceInfo> serviceList = activityManager
////                        .getRunningServices(Integer.MAX_VALUE);
////                if (serviceList.size()>0){
////                    Toast.makeText(MainAeroCardioActivity.this,"正在监测不能跳转",Toast.LENGTH_SHORT).show();
////                }else {
////
////
////                }
//                startActivity(new Intent(MainAeroCardioActivity.this, com.uteamtec.heartcool.activity.SettingActivity.class));
//
//            }
//        });


//        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//        RelativeLayout menuHeader = (RelativeLayout) inflater.inflate(R.layout.layout_menu_header, null);
//        userNameDrawer = (TextView) menuHeader.findViewById(R.id.user_name);
//
//        drawer.addHeaderView(menuHeader);
//        drawer.setAdapter(new MenuAdapter(this, R.layout.item_menu_drawer, R.id.menu_opt_info, getResources().getStringArray(R.array.menu)));
//        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    //do nothing
//                } else if (position == 1) {
//                    UIHelper.goSettingActivity(MainAeroCardioActivity.this);
//                } else if (position == 2) {
//                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainAeroCardioActivity.this);
//                    dialogBuilder.setTitle(getResources().getString(R.string.dialog_logout_title))
//                            .setMessage(getResources().getString(R.string.dialog_logout_subtitle))
//                            .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    mainService.resetUser(null, null, null, null);
//                                    mainService.disconnectBle();
//                                    try {
//                                        mainService.disconnectServer();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    UIHelper.goLoginActivity(MainAeroCardioActivity.this);
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    dialogBuilder.create().show();
//                }
//            }
//        });


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.drawer_menu_open, R.string.drawer_menu_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
//                ecgView.setZOrderOnTop(false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
//                invalidateOptionsMenu();
            }
        };

        //enable drawer indicator
        drawerToggle.setDrawerIndicatorEnabled(true);

        drawerLayout.setDrawerListener(drawerToggle);

        setSupportActionBar(toolBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        /*
         *N.B. the drawer indicator will work only when the navigationOnClickListener is implemented
         */
//        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (drawerLayout.isDrawerOpen(drawer)) {
//                    toolBar.invalidate();
//                    drawerLayout.closeDrawer(drawer);
//                } else {
//                    drawerLayout.openDrawer(drawer);
//                    toolBar.invalidate();
//                }
//            }
//        });


//        SharedPerferenceUtils preferenceUtil = new SharedPerferenceUtils(getApplicationContext());
//        userNameDrawer.setText(preferenceUtil.getPreferenceValues(AeroCardioApp.PREF_USER_NAME));

        ecgView = (EcgView) findViewById(R.id.ecgView);
        ecgView.setScale(10);
        ecgView.setFps(50);
        ecgView.bindData(data);
        ecgView.resumeDraw();

        reconnectProgress = (ProgressBar) findViewById(R.id.reconnect_progress);
        reconnectProgress.setVisibility(View.INVISIBLE);
        reconnectInfo = (TextView) findViewById(R.id.reconnect_info);
        reconnectInfo.setVisibility(View.INVISIBLE);

        brMeter = (RingMeterView) findViewById(R.id.br_meter);
        hrMeter = (RingMeterView) findViewById(R.id.hr_meter);

//        warningList = (GridView) findViewById(R.id.info_list);
//        warningList.setAdapter(warningAdapter);

        /*
         * cyclic warning checker thread
         */
        warningChkThrd = new WarningCheckThread();
        warningChkThrd.start();

        recordList = (ListView) findViewById(R.id.record_list);
//        recordList.setAdapter(recordAdapter);

        ecgView.resumeDraw();

        /*
         * test code
         */

        brMeter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                EcgMark mark = new EcgMark(System.currentTimeMillis(), System.currentTimeMillis(), EcgMark.TYPE_GROUP_PHYSIO, 5, 140);
//                mainService.sendMessageToFe(FeMessage.createMarkMsg(mark));


            }
        });
//
//        hrMeter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mainService != null) {
//                    EcgMark mark = new EcgMark(System.currentTimeMillis(), System.currentTimeMillis(), EcgMark.TYPE_GROUP_PHYSIO, EcgMark.PHYSIO_HR, 140);
//                    mainService.sendMessageToFe(FeMessage.createMarkMsg( mark ));
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main_aero_cardio, menu);
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        boolean isDrawerOpen = drawerLayout.isDrawerOpen(drawer);
//        menu.findItem(R.id.action_options).setVisible(!isDrawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (drawerToggle.onOptionsItemSelected(item)){
//            return true;
//        }

//        switch (item.getItemId()) {
//            case R.id.action_options:
//                Toast.makeText(MainAeroCardioActivity.this, "option menu", Toast.LENGTH_LONG).show();
//                break;
//        }
        return false;
    }


    @Override
    protected void bindListener() {
        Log.i("绑定监听", "设置监听");

    }

    @Override
    public void unregisterService() {
        ListenerMgr.unregisterUserStateChangedListener(null);
        ListenerMgr.unregisterBleDeviceScannedListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (warningChkThrd != null) {
            warningChkThrd.quit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListenerMgr.registerDataReceivedListener(null);
        ListenerMgr.registerEcgMarkListener(null);
        if (serviceConn != null) {
            unbindService(serviceConn);
            serviceConn = null;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private class WarningCheckThread extends Thread {
        private boolean enabled = true;

        public void quit() {
            enabled = false;
            this.interrupt();
        }

        @Override
        public void run() {
            while (true) {
                if (!enabled) {
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                for (int m = 0; m < warnings.size(); m++) {
//                    Warning w = warnings.get(m);
//                    if (System.currentTimeMillis() - w.getReceivedTime() > Warning.DEFAULT_LIFETIME) {
//                        warnings.remove(m);
//                    }
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        warningAdapter.notifyDataSetChanged();
//                    }
//                });
            }
        }
    }

}