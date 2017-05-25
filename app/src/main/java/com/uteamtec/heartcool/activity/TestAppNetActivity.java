package com.uteamtec.heartcool.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.EcgMarks;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.type.UserDevices;

import org.json.JSONObject;

import java.util.List;

/**
 * 网络请求测试页面
 * Created by wd
 */
public class TestAppNetActivity extends BaseAppCompatActivity {

    private static final String TAG = TestAppNetActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        validate();
//        isExistTelephone();
//        queryAppUserInfoByInfoId();
//        deletePatientByTelephone();
//        sendMessage();
//        updatePasswordByApp();
//        validate();
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return true;
    }

    @Override
    protected boolean enableServiceConnection() {
        return false;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    // ==========================================================================

//    private void login() {
//        AppNetTcpComm.getUser().login("13077784697", "123456",
//                new AppNetTcpCommListener<String>() {
//                    @Override
//                    public void onResponse(boolean success, String response) {
//                        Log.e(TAG, "login -> success:" + success + " response:" + response);
//                    }
//                });
//    }

    private void isExistTelephone() {
        AppNetTcpComm.getUser().isExistTelephone("13077784697", new AppNetTcpCommListener<Boolean>() {
            @Override
            public void onResponse(boolean success, Boolean response) {
                Log.e(TAG, "isExistTelephone -> success:" + success + " response:" + response);
                if (response) {
                    queryInfoIdByAppUserCode();
                } else {
                    createUserByApp();
                }
            }
        });
    }

    private void createUserByApp() {
        AppNetTcpComm.getUser().createUserOrUpdateByApp("13077784697", "123456",
                "realName", "men", "2016-01-01", "1", "XXXXXXXXX",
                new AppNetTcpCommListener<String>() {
                    @Override
                    public void onResponse(boolean success, String response) {
                        Log.e(TAG, "createUserByApp -> success:" + success + " response:" + response);
                    }
                });
    }

    private void queryAppUserInfoByInfoId() {
        AppNetTcpComm.getUser().queryAppUserInfoByInfoId("98865027",
                new AppNetTcpCommListener<JSONObject>() {
                    @Override
                    public void onResponse(boolean success, JSONObject response) {
                        Log.e(TAG, "createUserByApp -> success:" + success + " response:" + response);
                    }
                });
    }

    private void queryInfoIdByAppUserCode() {
        AppNetTcpComm.getUser().queryInfoIdByAppUserCode("13077784697", new AppNetTcpCommListener<String>() {
            @Override
            public void onResponse(boolean success, String response) {
                Log.e(TAG, "queryInfoIdByAppUserCode -> success:" + success + " response:" + response);
                if (success) {
//                    bindDeviceByMacAddress();
                    queryAppEcgAnalysisByTime();
                    queryEcgByInfoIdAndTime();
                    queryAppMarkCounts();
                }
            }
        });
    }

    private void deletePatientByTelephone() {
        AppNetTcpComm.getUser().deletePatientByTelephone("13077784691", new AppNetTcpCommListener<String>() {
            @Override
            public void onResponse(boolean success, String response) {
                Log.e(TAG, "deletePatientByTelephone -> success:" + success + " response:" + response);
            }
        });
    }

    private void updatePasswordByApp() {
        AppNetTcpComm.getUser().updatePasswordByApp("13077784697", "123456",
                new AppNetTcpCommListener<String>() {
                    @Override
                    public void onResponse(boolean success, String response) {
                        Log.e(TAG, "updatePasswordByApp -> success:" + success + " response:" + response);
                    }
                });
    }

    private void validate() {
        AppNetTcpComm.getUser().validate("13077784697", "123456",
                new AppNetTcpCommListener<String>() {
                    @Override
                    public void onResponse(boolean success, String response) {
                        Log.e(TAG, "validate -> success:" + success + " response:" + response);
                    }
                });
    }

    // ==========================================================================

    private void bindDeviceByMacAddress() {
        AppNetTcpComm.getInfo().bindDeviceByMacAddress("98865027", "80:EA:CA:00:00:1E",
                new AppNetTcpCommListener<String>() {
                    @Override
                    public void onResponse(boolean success, String response) {
                        Log.e(TAG, "bindDeviceByMacAddress -> success:" + success + " response:" + response);
                        if (success) {
                            validateUserInfoAndDevice();
                            queryBindDeviceByInfoId();
                        }
                    }
                });
    }

    private void validateUserInfoAndDevice() {
        AppNetTcpComm.getInfo().validateUserInfoAndDevice("98865027", "80:EA:CA:00:00:1E",
                new AppNetTcpCommListener<UserDevices>() {
                    @Override
                    public void onResponse(boolean success, UserDevices response) {
                        Log.e(TAG, "validateUserInfoAndDevice -> success:" + success + " response:" + response.toString());
                    }
                });
    }

    private void queryBindDeviceByInfoId() {
        AppNetTcpComm.getInfo().queryBindDeviceByInfoId("98865027",
                new AppNetTcpCommListener<UserDevice>() {
                    @Override
                    public void onResponse(boolean success, UserDevice response) {
                        Log.e(TAG, "queryBindDeviceByInfoId -> success:" + success + " response:" + response.toString());
                    }
                });
    }

    // ==========================================================================

    private void queryAppEcgAnalysisByTime() {
        AppNetTcpComm.getEcg().queryAppEcgAnalysisByTime("98865027", 0, System.currentTimeMillis(),
                new AppNetTcpCommListener<List<DBDetection>>() {
                    @Override
                    public void onResponse(boolean success, List<DBDetection> response) {
                        Log.e(TAG, "queryAppEcgAnalysisByTime -> success:" + success);
                        if (response != null) {
                            for (DBDetection d : response) {
                                Log.e(TAG, "queryAppEcgAnalysisByTime -> DBDetection: " + d.toString());
                            }
                        }
                    }
                });
    }

    private void queryEcgByInfoIdAndTime() {
        AppNetTcpComm.getEcg().queryEcgByInfoIdAndTime("98865027", 0, System.currentTimeMillis(),
                new AppNetTcpCommListener<List<Ecg>>() {
                    @Override
                    public void onResponse(boolean success, List<Ecg> response) {
                        Log.e(TAG, "queryEcgByInfoIdAndTime -> success:" + success);
                        if (response != null) {
                            for (Ecg e : response) {
                                Log.e(TAG, "queryEcgByInfoIdAndTime -> Ecg: " + e.toString());
                            }
                        }
                    }
                });
    }

    // ==========================================================================

    private void queryAppMarkCounts() {
        AppNetTcpComm.getEcgMark().queryAppMarkCounts("98865027", 0, System.currentTimeMillis(),
                new AppNetTcpCommListener<List<EcgMarks>>() {
                    @Override
                    public void onResponse(boolean success, List<EcgMarks> response) {
                        Log.e(TAG, "queryAppMarkCounts -> success:" + success);
                        if (response != null) {
                            for (EcgMarks ms : response) {
                                Log.e(TAG, "queryAppMarkCounts -> EcgMarks: " + ms.toString());
                            }
                        }
                    }
                });
    }

    // ==========================================================================

    private void sendMessage() {
        AppNetTcpComm.getSms().sendMessage("13036815439", "1234",
                new AppNetTcpCommListener<String>() {
                    @Override
                    public void onResponse(boolean success, String response) {
                        Log.e(TAG, "sendMessage -> success:" + success + " response: " + response);
                    }
                });
    }

}
