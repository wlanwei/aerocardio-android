package com.uteamtec.heartcool.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.litesuits.orm.db.assit.QueryBuilder;
import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.activity.AeroCardioHistoryDetailActivity;
import com.uteamtec.heartcool.activity.AeroCardioSettingActivity;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.db.DBOrm;
import com.uteamtec.heartcool.service.listener.DataReceivedListener;
import com.uteamtec.heartcool.service.listener.DetectionListener;
import com.uteamtec.heartcool.service.listener.EcgMarkListener;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.listener.UserStateChangedListener;
import com.uteamtec.heartcool.service.major.DetectionService;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.stats.EcgMarkReport;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.MobclickEvent;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.views.widget.EcgView;
import com.uteamtec.heartcool.views.widget.WarningView;

import java.util.ArrayList;

/**
 * 主设备连接界面
 * Created by wd
 */
public abstract class AeroCardioFragment extends BaseFragment implements View.OnClickListener {

    private EcgView ecgView;
    private static final int LEN_DRAW_DATA = 1000;

    private View reconnectProgress;

    private TextView txHR;
    private TextView txHRAverage;
    private TextView txHRHealth;
    private TextView txBR;

    private int colorNormal;
    private int colorWarn;

    private TextView txTimer;

    private WarningView warningViewHard;
    private WarningView warningViewSoft;

    private ImageView imgOnline;

    private Button btnRecordStart;
    private Button btnRecordStop;

    public AeroCardioFragment() {
        super();
    }

    public AeroCardioFragment(BaseFragmentListener listener) {
        super(listener);
    }

    @Override
    protected int onCreateViewResource() {
        return R.layout.fragment_aerocardio;
    }

    @Override
    protected void initViews(View rootView) {
        if (hasRootView()) {
            ecgView = (EcgView) getRootView().findViewById(R.id.fragment_aerocardio_view_ecg);
            ecgView.setScale(10);
            ecgView.setFps(50);
            ecgView.resumeDraw();

            reconnectProgress = getRootView().findViewById(R.id.fragment_aerocardio_ll_reconnect);

            txHR = (TextView) getRootView().findViewById(R.id.fragment_aerocardio_tv_hr);// 即时心律
            txHRAverage = (TextView) getRootView().findViewById(R.id.fragment_aerocardio_tv_hr_average);// 平均心律
            txHRHealth = (TextView) getRootView().findViewById(R.id.fragment_aerocardio_tv_hr_health);// 心律正常范围
            txBR = (TextView) getRootView().findViewById(R.id.fragment_aerocardio_tv_br);// 实时呼吸率

            colorNormal = getResources().getColor(R.color.colorDetection);
            colorWarn = getResources().getColor(R.color.colorMore);

            txTimer = (TextView) getRootView().findViewById(R.id.fragment_aerocardio_tx_timer);

            warningViewHard = (WarningView) getRootView().findViewById(R.id.fragment_aerocardio_wv_hard);
            warningViewSoft = (WarningView) getRootView().findViewById(R.id.fragment_aerocardio_wv_soft);

            imgOnline = (ImageView) getRootView().findViewById(R.id.fragment_aerocardio_iv_online);

            btnRecordStart = (Button) getRootView().findViewById(R.id.fragment_aerocardio_btn_start);
            btnRecordStart.setOnClickListener(this);
            btnRecordStop = (Button) getRootView().findViewById(R.id.fragment_aerocardio_btn_stop);
            btnRecordStop.setOnClickListener(this);
        }
    }

    @Override
    public void onServiceConnected() {
        if (!hasRootView()) {
            return;
        }

        DetectionService.init();

        ListenerMgr.registerDetectionListener(detectionListener);
        ListenerMgr.registerDataReceivedListener(dataReceivedListener);
        ListenerMgr.registerEcgMarkListener(ecgMarkListener);
        ListenerMgr.registerUserStateChangedListener(userStateChangedListener);

        keepConnect();
//        startRecord();// 开始就记录
    }

    @Override
    public void onServiceDisconnected() {
        if (!hasRootView()) {
            return;
        }

        ListenerMgr.unregisterDetectionListener(detectionListener);
        ListenerMgr.unregisterDataReceivedListener(dataReceivedListener);
        ListenerMgr.unregisterEcgMarkListener(ecgMarkListener);
        ListenerMgr.unregisterUserStateChangedListener(userStateChangedListener);

        stopRecord();
    }

    public void onResume() {
        super.onResume();
        startDraw();
    }

    public void onPause() {
        super.onPause();
        stopDraw();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_aerocardio_btn_start:
                MobclickEvent.onEvent(getActivity(),
                        MobclickEvent.EventId_DetectionStart);
                startRecord();
                break;
            case R.id.fragment_aerocardio_btn_stop:
                stopRecord();
                break;
            case R.id.dialog_detection_finish_tv_detail:
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<DBDetection> ds = DBOrm.cascadeQuery(
                                new QueryBuilder<>(DBDetection.class)
                                        .whereEquals("mac", BleFeComm.getClient().getMacAddress())
                                        .appendOrderDescBy("id").limit("1")
                        );
                        if (getActivity() != null && ds != null && ds.size() > 0) {
                            Intent intent = new Intent(getActivity(), AeroCardioHistoryDetailActivity.class);
                            intent.putExtra(DBDetection.class.getSimpleName(), ds.get(0));
                            getActivity().startActivity(intent);
                        }
                    }
                }).start();
                break;
            case R.id.dialog_detection_finish_iv_close:
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;
        }
    }

    @Override
    protected String getPageName() {
        return AeroCardioFragment.class.getSimpleName();
    }

    private void keepConnect() {
        if (User.getUser().hasUserDeviceAndMac()) {
            refreshProgress();
            BleFeComm.getClient().connect(User.getUser().getUserDevice().getMacAddr());
        } else if (User.getUser().hasPrevUserDevice()) {
            refreshProgress();
            BleFeComm.getClient().connect(User.getUser().getPrevUserDevice().getMacAddr());
        } else if (getActivity() != null) {
            L.e("AeroCardioFragment.NoDevice");
            startActivity(new Intent(getActivity(), AeroCardioSettingActivity.class));
            getActivity().finish();
            return;
        }
        refreshOnlineView();
    }

    private void startRecord() {
        DetectionService.startRecord();
        btnRecordStart.setVisibility(View.GONE);
        btnRecordStop.setVisibility(View.VISIBLE);
        MobclickEvent.onEventValueBegin(getActivity(), MobclickEvent.EventId_DetectionRecord);
    }

    private void stopRecord() {
        DetectionService.stopRecord();
        btnRecordStart.setVisibility(View.VISIBLE);
        btnRecordStop.setVisibility(View.GONE);
        MobclickEvent.onEventValueEnd(getActivity(), MobclickEvent.EventId_DetectionRecord);
    }

    private void refreshProgress() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    L.e("AeroCardioFragment.refreshProgress: " +
//                            BleFeComm.getClient().isConnected());
                    if (BleFeComm.getClient().isConnected()) {
                        ecgView.resumeDraw();
                        ecgView.setVisibility(View.VISIBLE);
                        reconnectProgress.setVisibility(View.INVISIBLE);
                    } else {
                        ecgView.pauseDraw();
                        ecgView.resetData(LEN_DRAW_DATA);
                        ecgView.setVisibility(View.INVISIBLE);
                        reconnectProgress.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void refreshOnlineView() {
//        if (getActivity() != null) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
////                    L.e("AeroCardioFragment.refreshOnlineView: " +
////                            User.getUser().isConnectedDeviceAppNet());
//                    imgOnline.setVisibility(User.getUser().isConnectedDeviceAppNet() ?
//                            View.INVISIBLE : View.VISIBLE);
//                }
//            });
//        }
    }

    public void startDraw() {
        if (BleFeComm.getClient().isConnected()) {
            ecgView.resumeDraw();
        }
    }

    public void stopDraw() {
        ecgView.pauseDraw();
    }

    private AlertDialog dialog = null;
    private View dialog_view = null;

    private void showFinishRecordDialog(EcgMarkReport report) {
        if (report == null) {
            return;
        }
        if (dialog == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            dialog_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_detection_finish, null);
            dialog_view.findViewById(R.id.dialog_detection_finish_tv_detail).
                    setOnClickListener(this);
            dialog_view.findViewById(R.id.dialog_detection_finish_iv_close).
                    setOnClickListener(this);
            builder.setView(dialog_view);
            dialog = builder.create();
        } else if (dialog.isShowing()) {
            dialog.dismiss();
        }
        TextView.class.cast(dialog_view.findViewById(R.id.dialog_detection_finish_tv_date)).
                setText(report.jlsj);
        TextView.class.cast(dialog_view.findViewById(R.id.dialog_detection_finish_tv_time)).
                setText(report.jcsc);
        dialog.show();
    }

    private DetectionListener detectionListener = new DetectionListener() {
        @Override
        public void onStart() {
            onTimerTick("00:00:00");
        }

        @Override
        public void onStop() {
            DetectionService.analyzeRecord();
        }

        @Override
        public void onAnalyze(final EcgMarkReport report) {
            if (report == null) {
                return;
            } else if (report.getMarkSize() == 0) {
                L.e("Empty report");
                Toast.makeText(getActivity(), R.string.no_detection_report,
                        Toast.LENGTH_SHORT).show();
                return;
            }

//                        if (report.averageHR <= 0) { // TODO: 这种判定不可靠
//                            AppNetTcpComm.sendJudge();
//                            WaringAlarm.playWaring(MainAeroCardioActivity.this);
//                        }

            // ================================这里是本地保存数据================================
            DetectionService.saveRecord();

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFinishRecordDialog(report);
                }
            });
        }

        @Override
        public void onSave(DBDetection detection) {
            if (detection == null) {
                return;
            }
            // ================================这里是网络上传数据================================
            AppNetTcpComm.getEcg().saveAppEcgAnalysis(
                    User.getUser().getIdString(), detection,
                    new AppNetTcpCommListener<String>() {
                        @Override
                        public void onResponse(boolean success, String response) {
                            L.e("saveAppEcgAnalysis -> success: " + success + " response:" + response);
                        }
                    });
        }

        @Override
        public void onTimerTick(final String hms) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txTimer.setText(hms);
                }
            });
        }
    };

    private DataReceivedListener dataReceivedListener = new DataReceivedListener() {
        @Override
        public void onReceivedEcgRaw(Ecg ecg) {
            if (ecg == null) {
                return;
            }
            int drawType;
            switch (ecg.getType()) {
                case Ecg.TYPE_SINGLE:
                    drawType = EcgView.DRAWTYPE_1;
                    break;
                case Ecg.TYPE_THREE:
                    drawType = EcgView.DRAWTYPE_3;
                    break;
                default:
                    drawType = EcgView.DRAWTYPE_1;
                    break;
            }
            if (ecgView.getDrawType() != drawType) {
                ecgView.resetDrawType(drawType);
                ecgView.resetData(LEN_DRAW_DATA);
            }
            ecgView.putData(ecg.getData());
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
    };

    private EcgMarkListener ecgMarkListener = new EcgMarkListener() {

        @Override
        public void onMarkUpdated() {
        }

        @Override
        public void onMarkLeadOff(String msg) {
            L.e("EcgMarkListener.onMarkLeadOff: " + msg);
            User.getUser().interruptAppNetEcg();
//            warningViewHard.showWarningUI(getActivity(), WarningView.WarningType.LEADOFF);// 取消显示导联脱落提示
        }

        @Override
        public void onMarkLowPower(String msg) {
            L.e("EcgMarkListener.onMarkLowPower: " + msg);
        }

        @Override
        public void onMarkShort(String msg) {
            L.e("EcgMarkListener.onMarkShort: " + msg);
            User.getUser().interruptAppNetEcg();
        }

        @Override
        public void onMarkUnplug(String msg) {
            L.e("EcgMarkListener.onMarkUnplug: " + msg);
            User.getUser().interruptAppNetEcg();
        }

        @Override
        public void onMarkHR(final int hr, final boolean hrWarn,
                             final int hrAverage,
                             final int hrHealth, final boolean healthWarn) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txHR.setText(String.valueOf(hr));
                    txHR.setTextColor(hrWarn ? colorWarn : colorNormal);
                    if (hrAverage >= 0) {
                        txHRAverage.setText(String.valueOf(hrAverage));  // 平均心律
                    }
                    if (hrHealth >= 0) {
                        txHRHealth.setText(String.valueOf(hrHealth));   // 正常
                        txHRHealth.setTextColor(healthWarn ? colorWarn : colorNormal);
                    }
                }
            });
        }

        @Override
        public void onMarkBR(final int br) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txBR.setText(String.valueOf(br));
                }
            });
        }

        @Override
        public void onMarkNoise(String msg) {
            warningViewSoft.showWarningUI(getActivity(), WarningView.WarningType.NOISE);
        }
    };

    private UserStateChangedListener userStateChangedListener = new UserStateChangedListener() {
        @Override
        public void onDeviceRegistered(UserDevice device, int regResult) {
            refreshOnlineView();
        }

        @Override
        public void onDeviceActivated(UserDevice device, int activateResult) {
            refreshOnlineView();
        }

        @Override
        public void onLogin(int loginResult) {
        }

        @Override
        public void onAppStateChanged(final int state) {
            L.e("AeroCardioFragment.onAppStateChanged state: " + state);
            refreshOnlineView();
        }

        @Override
        public void onFeStateChanged(int state) {
            L.e("AeroCardioFragment.onFeStateChanged state: " + state);
            refreshProgress();
        }
    };

}
