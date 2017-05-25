package com.uteamtec.heartcool.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.adapter.DeviceListAdapter;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.listener.BleDeviceScannedListener;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.listener.UserStateChangedListener;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.type.UserSaveType;
import com.uteamtec.heartcool.utils.Emulator;
import com.uteamtec.heartcool.utils.L;

import java.util.ArrayList;

/**
 * 设备配置主界面
 * Created by wd
 */
public class AeroCardioSettingActivity extends BaseAppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    private int connectedIdx = -1;
    private int connectingIdx = -1;

    private int tryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerocardio_setting);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermissions();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resetDeviceList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private AlphaAnimation animationBoom;
    private AlphaAnimation animationFade;

    private ListView deviceList;
    private DeviceListAdapter deviceListAdapter;

    private Button research;
    private View ringSearch;

    @Override
    protected void initViews() {
        animationBoom = new AlphaAnimation(0.0f, 1.0f);
        animationBoom.setDuration(300);
        animationBoom.setInterpolator(new LinearInterpolator());
        animationBoom.setFillAfter(true);
        animationFade = new AlphaAnimation(1.0f, 0.0f);
        animationFade.setDuration(300);
        animationFade.setInterpolator(new LinearInterpolator());
        animationFade.setFillAfter(true);

        deviceList = (ListView) findViewById(R.id.aerocardio_setting_list_dev);
        deviceList.setVisibility(View.INVISIBLE);
        deviceList.setVerticalScrollBarEnabled(false);
        deviceListAdapter = new DeviceListAdapter(this, R.layout.item_dev_list,
                new ArrayList<UserDevice>());
        deviceList.setAdapter(deviceListAdapter);
        deviceList.setOnItemClickListener(this);

        findViewById(R.id.aerocardio_setting_close).setOnClickListener(this);

        research = (Button) findViewById(R.id.aerocardio_setting_btn_search);
        research.setVisibility(View.INVISIBLE);
        research.setOnClickListener(this);

        ringSearch = findViewById(R.id.aerocardio_setting_ring_search);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.ble_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return true;
    }

    @Override
    protected boolean enableServiceConnection() {
        return !Emulator.isEmulator();
    }

    @Override
    public void onServiceConnected() {
        BleFeComm.getClient().disconnect();

        resetDeviceList();

        ListenerMgr.registerBleDeviceScannedListener(bleDeviceScannedListener);
        ListenerMgr.registerUserStateChangedListener(userStateChangedListener);

        //initial state
        if (User.getUser().getFeState() == User.FESTATE_DISABLED) {
            ringSearch.setVisibility(View.GONE);
            research.setVisibility(View.VISIBLE);
            deviceList.setVisibility(View.VISIBLE);
        } else {
            ringSearch.setVisibility(View.VISIBLE);
            BleFeComm.startBleScan();
        }
    }

    @Override
    public void onServiceDisconnected() {
        ListenerMgr.unregisterUserStateChangedListener(userStateChangedListener);
        ListenerMgr.unregisterBleDeviceScannedListener(bleDeviceScannedListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.aerocardio_setting_close:
                gotoLogin();
                break;
            case R.id.aerocardio_setting_btn_search:
                if (!BleFeComm.getState().isEnableBle()) {
                    Toast.makeText(this, R.string.ble_disabled, Toast.LENGTH_SHORT).show();
                } else if (research.getVisibility() == View.VISIBLE) {
                    research.setText(getResources().getString(R.string.rescanning));
                    resetDeviceList();
                    BleFeComm.getClient().disconnect();
                    BleFeComm.startBleScan();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
        if (User.getUser().getFeState() == User.FESTATE_DISABLED) {
            Toast.makeText(this, getString(R.string.ble_disabled), Toast.LENGTH_SHORT).show();
            return;
        }
        final UserDevice dev = deviceListAdapter.getItem(position);
        if (dev == null || dev.getState() == UserDevice.STATE_OFF) {
            Toast.makeText(this, getString(R.string.device_off), Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getResources().getString(R.string.aerocardio_setting_confirm_title));
        dialogBuilder.setMessage(getResources().getString(R.string.aerocardio_setting_confirm_content));
        dialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BleFeComm.getClient().disconnect();
                connectingIdx = position;
                if (connectedIdx >= 0) {
                    deviceListAdapter.setState(connectedIdx, UserDevice.STATE_ON);
                    connectedIdx = -1;
                }
                BleFeComm.getClient().connect(dev.getMacAddr());
                deviceListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.create().show();
    }

    private void resetDeviceList() {
        if (isBindMainService()) {
            deviceListAdapter.clear();

            // boundedDevices
//            UserDevice deviceConnected = null;
//            if (BleFeComm.getClient().isConnected()) {
//                deviceConnected = BleFeComm.getUserDevice();
//            }
//            List<UserDevice> boundedDevices = BleFeComm.getBoundedDevices();
//            if (boundedDevices != null) {
//                for (UserDevice dev : boundedDevices) {
//                    dev.setState(UserDevice.STATE_OFF);
//                    if (deviceConnected != null &&
//                            dev.getMacAddr().equals(deviceConnected.getMacAddr())) {
//                        dev.setState(UserDevice.STATE_CONNECTED);
//                        connectedIdx = deviceListAdapter.getCount() - 1;
//                    }
//                    deviceListAdapter.add(dev);
//                }
//            }
            if (deviceList != null && deviceListAdapter != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private boolean isFinish = false;

    private synchronized void gotoLogin() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        BleFeComm.getClient().disconnect();
        onServiceDisconnected();
        startActivity(new Intent(this, AeroCardioLoginActivity.class));
        this.finish();
    }

    private synchronized void gotoMain() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        onServiceDisconnected();
        startActivity(new Intent(this, AeroCardioActivity.class));
        this.finish();
    }

    private BleDeviceScannedListener bleDeviceScannedListener = new BleDeviceScannedListener() {
        @Override
        public void onBleDeviceScanned(BluetoothDevice device) {
            if (TextUtils.isEmpty(device.getAddress()) ||
                    TextUtils.isEmpty(device.getName())) {
                return;
            }
            UserDevice scannedDev = new UserDevice(device.getAddress(), device.getName());
            scannedDev.setState(UserDevice.STATE_ON);
            deviceListAdapter.add(scannedDev);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onBleScanFinished() {
            if (ringSearch.getVisibility() == View.VISIBLE) {
                animationFade.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ringSearch.setVisibility(View.GONE);
                        deviceList.setVisibility(View.VISIBLE);

                        deviceListAdapter.notifyDataSetChanged();
                        deviceList.startAnimation(animationBoom);

                        research.setText(getResources().getString(R.string.rescan));
                        research.setVisibility(View.VISIBLE);
                        research.startAnimation(animationBoom);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                ringSearch.startAnimation(animationFade);
            } else {
                research.setText(getResources().getString(R.string.rescan));
                research.setVisibility(View.VISIBLE);
            }
        }
    };

    private UserStateChangedListener userStateChangedListener = new UserStateChangedListener() {
        @Override
        public void onDeviceRegistered(UserDevice device, int regResult) {
            L.e("AeroCardioSettingActivity -> onDeviceRegistered: " + regResult);
        }

        @Override
        public void onDeviceActivated(UserDevice device, int activateResult) {
            L.e("AeroCardioSettingActivity -> onDeviceActivated: " + activateResult);
        }

        @Override
        public void onLogin(int loginResult) {
        }

        @Override
        public void onAppStateChanged(int state) {
//            L.e("AeroCardioSettingActivity -> onAppStateChanged: " + state);
        }

        @Override
        public void onFeStateChanged(int state) {
            L.e("AeroCardioSettingActivity -> onFeStateChanged: " + state);
            switch (state) {
                case User.FESTATE_DISABLED:// ble is diabled, stop all animation
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ringSearch.setVisibility(View.GONE);
                            research.setText(getString(R.string.rescan));
                            research.setVisibility(View.VISIBLE);
                            deviceList.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                case User.FESTATE_DISCONNECTED:
                    if (connectedIdx >= 0) {
                        deviceListAdapter.setState(connectedIdx, UserDevice.STATE_ON);
                        connectedIdx = -1;
                    }
                    if (connectingIdx >= 0) {
                        deviceListAdapter.setState(connectingIdx, UserDevice.STATE_ON);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceListAdapter.notifyDataSetChanged();
                        }
                    });
                    break;
                case User.FESTATE_CONNECTING:
                    if (connectingIdx >= 0) {
                        deviceListAdapter.setState(connectingIdx, UserDevice.STATE_CONNECTING);
                    }
                    break;
                case User.FESTATE_CONNECTED:
                    if (connectingIdx >= 0) {
                        connectedIdx = connectingIdx;
                        deviceListAdapter.setState(connectedIdx, UserDevice.STATE_CONNECTED);
                        connectingIdx = -1;
                    } else if (connectedIdx >= 0) {
                        deviceListAdapter.setState(connectedIdx, UserDevice.STATE_CONNECTED);
                        connectingIdx = -1;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceListAdapter.notifyDataSetChanged();
                        }
                    });
                    break;
                case User.FESTATE_REGISTERED:
                    if (User.getUser().hasUserDevice() && BleFeComm.getClient().isConnected()) {
                        if (tryCount++ >= 5) {
                            return;
                        } else if (tryCount == 0) {
                            gotoLogin();
                            return;
                        } else if (tryCount != 1) {
                            return;
                        }

                        UserDevice dev = deviceListAdapter.getItem(connectedIdx);
                        if (dev != null) {
                            User.getUser().getUserDevice().setName(dev.getName());
                            User.getUser().getUserDevice().setMacAddr(dev.getMacAddr());
                            User.getUser().setPrevUserDevice(User.getUser().getUserDevice());
                        }
                        User.getUser().save(UserSaveType.Device); //store prev device
                        User.getUser().save(UserSaveType.BoundedDevice); //store bounded device

                        AppNetTcpComm.getInfo().bindDeviceByMacAddress(
                                User.getUser().getIdString(),
                                User.getUser().getUserDevice().getMacAddr(),
                                new AppNetTcpCommListener<String>() {
                                    @Override
                                    public void onResponse(boolean success, final String response) {
                                        L.e("bindDeviceByMacAddress -> success: " + success + " response:" + response);
                                        if (success) {
                                            gotoMain();
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(AeroCardioSettingActivity.this,
                                                            getString(R.string.http_conn_err, response),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            tryCount = -1;
                                        }
                                    }
                                });
                    }
                    break;
            }
        }
    };

}
