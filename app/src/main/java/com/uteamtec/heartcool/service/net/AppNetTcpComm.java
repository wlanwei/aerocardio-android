package com.uteamtec.heartcool.service.net;

import com.uteamtec.heartcool.MainConstant;
import com.uteamtec.heartcool.comm.TcpComm;
import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.net.module.AppNetTcpEcg;
import com.uteamtec.heartcool.service.net.module.AppNetTcpEcgMark;
import com.uteamtec.heartcool.service.net.module.AppNetTcpInfo;
import com.uteamtec.heartcool.service.net.module.AppNetTcpSms;
import com.uteamtec.heartcool.service.net.module.AppNetTcpUser;
import com.uteamtec.heartcool.service.stats.EcgMarkReport;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.ApiUrl;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by wd
 */
public final class AppNetTcpComm {

    private TcpComm comm;

    private AppNetTcpUser user;
    private AppNetTcpInfo info;
    private AppNetTcpEcg ecg;
    private AppNetTcpEcgMark mark;
    private AppNetTcpSms sms;

    private AppNetTcpComm() {
        comm = new TcpComm(MainConstant.TCP_SERVER_HOST, MainConstant.TCP_SERVER_PORT);
        comm.setOnConnectionChangedListener(new TcpComm.ConnectionChangedListener() {
            @Override
            public void onOnConnectionChanged(int state) {
                switch (state) {
                    case TcpComm.STATE_DISCONNECTED:
//                        L.e("AppNetTcpComm.STATE_DISCONNECTED");
                        User.getUser().setAppState(User.APPSTATE_DISCONNECTED);
                        User.getUser().setConnectedDeviceAppNet(false);
//                        ActivityStack.toast(R.string.offline);
                        break;
                    case TcpComm.STATE_CONNECTED:
//                        L.e("AppNetTcpComm.STATE_CONNECTED");
                        User.getUser().setAppState(User.APPSTATE_CONNECTED);
                        User.getUser().resetLastAppNetMessageTime();
                        User.getUser().setTimeLastLogin(System.currentTimeMillis());
                        AppNetTxQueue.put(AppMessage.createLoginMessage(User.getUser().getId(),
                                User.getUser().getKey()));
                        break;
                    case TcpComm.STATE_CONNECTING:
                        User.getUser().setAppState(User.APPSTATE_CONNECTING);
                        break;
                    default:
                        break;
                }
                if (ListenerMgr.getUserStateChangedListener() != null) {
                    ListenerMgr.getUserStateChangedListener().onAppStateChanged(User.getUser().getAppState());
                }
            }
        });
        comm.setOnDataListener(new TcpComm.DataListener() {
            @Override
            public void onReceivedData(byte[] data, int len) {
                AppNetTxCoder.getCoder().putBytes(data, len);
            }
        });

        user = new AppNetTcpUser();
        info = new AppNetTcpInfo();
        ecg = new AppNetTcpEcg();
        mark = new AppNetTcpEcgMark();
        sms = new AppNetTcpSms();
    }

    private static AppNetTcpComm _instance;

    private static AppNetTcpComm getInstance() {
        if (_instance == null) {
            synchronized (AppNetTcpComm.class) {
                if (_instance == null) {
                    _instance = new AppNetTcpComm();
                }
            }
        }
        return _instance;
    }

    private static TcpComm getTcpComm() {
        return getInstance().comm;
    }

    public static boolean connect(boolean force) {
        if (force || User.getUser().getAppState() == User.APPSTATE_DISCONNECTED) {
            try {
                getTcpComm().connect();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void disconnect(boolean force) {
        if (force || User.getUser().getAppState() == User.APPSTATE_CONNECTED ||
                User.getUser().getAppState() == User.APPSTATE_LOGIN) {
            getTcpComm().disconnect();
        }
    }

    public static boolean send(byte[] data) {
        return getTcpComm().send(data);
    }

    public static AppNetTcpUser getUser() {
        return getInstance().user;
    }

    public static AppNetTcpInfo getInfo() {
        return getInstance().info;
    }

    public static AppNetTcpEcg getEcg() {
        return getInstance().ecg;
    }

    public static AppNetTcpEcgMark getEcgMark() {
        return getInstance().mark;
    }

    public static AppNetTcpSms getSms() {
        return getInstance().sms;
    }

    /**
     * 紧急发送方式
     */
    public static void sendDetectionReport(EcgMarkReport report) {
        if (report == null) {
            return;
        }
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("uid", com.uteamtec.heartcool.model.User.getInstance().getUid()); //用户ID
        params.put("jlsj", report.jlsj); // 记录时间
        params.put("jcsc", report.jcsc); // 监测时长
        params.put("jcfk", report.jcfk); // 监测反馈
        params.put("yczb", report.yczb); // 异常指标
        params.put("pjxl", report.pjxl); // 平均心律
        params.put("xlfw", report.xlfw); // 心律范围
        params.put("jlfw", report.pjhx); // 节律范围
        params.put("smzl", report.smzl); // 睡眠质量
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

    /**
     * 紧急发送方式
     */
    public static void sendJudge() {
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("uid", com.uteamtec.heartcool.model.User.getInstance().getUid());
        fh.post(ApiUrl.SEND_WAY, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
//                jsonSend(s);
//                judgeContact(0);//紧急联系人,//判断是紧急情况还是发送报告要获取的联系人信息 0紧急情况 1报告
            }
        });
    }

}
