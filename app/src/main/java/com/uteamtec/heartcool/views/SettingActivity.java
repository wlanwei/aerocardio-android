package com.uteamtec.heartcool.views;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uteamtec.heartcool.BaseActivity;
import com.uteamtec.heartcool.MainMgrService;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.activity.AeroCardioLoginActivity;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.listener.BleDeviceScannedListener;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.listener.UserStateChangedListener;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.utils.CrashHandler;
import com.uteamtec.heartcool.utils.Emulator;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.views.widget.CheckIconView;
import com.uteamtec.heartcool.views.widget.RunningIconView;

import java.util.LinkedList;
import java.util.List;

/**
 * 蓝牙设备管理界面
 */
public class SettingActivity extends BaseActivity {

    private long BackPressedTime = 0;

    private static final int MSG_TOMAIN = 0;

    private List<UserDevice> devices;
    private int connectedIdx = -1;
    private int connectingIdx = -1;

    private boolean isBleOff = false;

    private ListView deviceList;
    private DeviceListAdapter deviceListAdapter;

    private ImageView back;
    private Button research;

    private RelativeLayout ringSearch;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    //UI effects
    AlphaAnimation animeBoom;
    AlphaAnimation animeFade;

    private MainMgrService mainService;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mainService = ((MainMgrService.MainMgrBinder) service).getService();

            resetDeviceList();

            ListenerMgr.registerBleDeviceScannedListener(new BleDeviceScannedListener() {
                @Override
                public void onBleDeviceScanned(BluetoothDevice device) {
                    UserDevice scannedDev = new UserDevice(device.getAddress(), device.getName());
                    scannedDev.setState(UserDevice.STATE_ON);
                    boolean isInList = false;
                    for (UserDevice dev : devices) {
//                        Log.e("TAG", dev.getName() + " -- " + dev.getMacAddr());
                        //if device is already on the list (except for the connected one)
                        if (dev.getMacAddr().equals(scannedDev.getMacAddr())) {
                            isInList = true;
                            dev.setState(UserDevice.STATE_ON);
                            break;
                        }
                    }
                    if (!isInList) {
                        devices.add(scannedDev);
                    }
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
                        animeFade.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                ringSearch.setVisibility(View.GONE);
                                deviceList.setVisibility(View.VISIBLE);

                                deviceListAdapter.notifyDataSetChanged();
                                deviceList.startAnimation(animeBoom);

                                research.setText(getResources().getString(R.string.rescan));
                                research.setVisibility(View.VISIBLE);
                                research.startAnimation(animeBoom);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        ringSearch.startAnimation(animeFade);
                    } else {
                        research.setText(getResources().getString(R.string.rescan));
                        research.setVisibility(View.VISIBLE);
                    }
                }
            });

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
                    L.i("<UI> setting, app state changed = " + Integer.toString(state));
                }

                @Override
                public void onFeStateChanged(int state) {
                    if (state == User.FESTATE_DISABLED) {
                        isBleOff = true;
//                        L.i("<UI> ble is diabled, stop all animation");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ringSearch.setVisibility(View.GONE);
                                research.setText(getString(R.string.rescan));
                                research.setVisibility(View.VISIBLE);
                                deviceList.setVisibility(View.VISIBLE);
                            }
                        });
                    } else if (state == User.FESTATE_DISCONNECTED) {
                        isBleOff = false;
                        if (connectedIdx >= 0) {
                            devices.get(connectedIdx).setState(UserDevice.STATE_ON);
                            connectedIdx = -1;
                        }
                        if (connectingIdx >= 0) {
                            devices.get(connectingIdx).setState(UserDevice.STATE_ON);
                            connectingIdx = -1;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                deviceListAdapter.notifyDataSetChanged();
                            }
                        });
                    } else if (state == User.FESTATE_CONNECTED) {
                        isBleOff = false;
                        L.i("<UI> connection to idx = " + Integer.toString(connectingIdx));
                        if (connectingIdx >= 0) {
                            L.i("<UI> new connection established ");

                            devices.get(connectingIdx).setState(UserDevice.STATE_CONNECTED);

                            connectedIdx = connectingIdx;
                            connectingIdx = -1;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    deviceListAdapter.notifyDataSetChanged();
                                }
                            });
                        } else if (connectedIdx >= 0 && connectedIdx < devices.size()) {
                            L.i("<UI> old connection established ");
                            devices.get(connectingIdx).setState(UserDevice.STATE_CONNECTED);
                            deviceListAdapter.notifyDataSetChanged();
                            connectingIdx = -1;
                        }
                    } else if (state == User.FESTATE_REGISTERED) {
                        if (BleFeComm.getClient().isConnected()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    UIHelper.goAeroCardioActivity(SettingActivity.this);
//                                    UIHelper.goMainAeroCardioActivity(SettingActivity.this);
//                                    UIHelper.goDebugActivity(SettingActivity.this);
                                }
                            });
                        }
                    }
                }
            });

            //initial state
            if (User.getUser().getFeState() == User.FESTATE_DISABLED) {
                isBleOff = true;
                L.i("<UI> initial state: do nonthing");
                ringSearch.setVisibility(View.GONE);
                research.setVisibility(View.VISIBLE);
                deviceList.setVisibility(View.VISIBLE);
            } else {
                L.i("<UI> initial state: run ble scan");
                isBleOff = false;
                ringSearch.setVisibility(View.VISIBLE);
                BleFeComm.getClient().disconnect();
                BleFeComm.startBleScan();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.i("<UI> disconnect service");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.i("<UI> setting acti create");
        onCreated(R.layout.activity_settings);
        initPermissions();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else if (!Emulator.isEmulator()) {
            bindService(new Intent(SettingActivity.this, MainMgrService.class), serviceConn, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (!Emulator.isEmulator()) {
                bindService(new Intent(SettingActivity.this, MainMgrService.class), serviceConn, BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - BackPressedTime <= 2000) {
            BackPressedTime = 0;
            finish();
            return;
        }
        Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
        BackPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resetDeviceList();
    }

    @Override
    protected void onDestroy() {
        if (!Emulator.isEmulator() && serviceConn != null) {
            unbindService(serviceConn);
        }
        super.onDestroy();
    }

    @Override
    public void unregisterService() {
        ListenerMgr.unregisterUserStateChangedListener(null);
        ListenerMgr.unregisterBleDeviceScannedListener(null);
    }

    @Override
    protected void init() {
        CrashHandler.getInstance().init(this);

        animeBoom = new AlphaAnimation(0.0f, 1.0f);
        animeBoom.setDuration(300);
        animeBoom.setInterpolator(new LinearInterpolator());
        animeBoom.setFillAfter(true);
        animeFade = new AlphaAnimation(1.0f, 0.0f);
        animeFade.setDuration(300);
        animeFade.setInterpolator(new LinearInterpolator());
        animeFade.setFillAfter(true);

        devices = new LinkedList<>();
        deviceListAdapter = new DeviceListAdapter(SettingActivity.this, R.layout.item_dev_list, devices);
    }

    @Override
    protected void findView() {
        deviceList = (ListView) findViewById(R.id.list_dev);
        deviceList.setVisibility(View.INVISIBLE);
        deviceList.setVerticalScrollBarEnabled(false);

        back = (ImageView) findViewById(R.id.setting_back);

        research = (Button) findViewById(R.id.setting_btn_search);
        research.setVisibility(View.INVISIBLE);

        ringSearch = (RelativeLayout) findViewById(R.id.ring_search);
    }

    @Override
    protected void bindListener() {
        deviceList.setAdapter(deviceListAdapter);

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                L.i("设备地址", devices.get(position).getMacAddr());
                if (mainService != null && User.getUser().getFeState() != User.FESTATE_DISABLED) { // && devices.get(position).getState() != UserDevice.STATE_OFF) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingActivity.this);
                    dialogBuilder.setTitle(getResources().getString(R.string.new_conn_confirm_title));
                    dialogBuilder.setMessage(getResources().getString(R.string.new_conn_confirm_content));
                    dialogBuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deviceListAdapter.setReconnect(true);
                            BleFeComm.getClient().connect(devices.get(position).getMacAddr());
                            connectingIdx = position;
                            if (connectedIdx >= 0) {
                                devices.get(connectedIdx).setState(UserDevice.STATE_ON);
                                connectedIdx = -1;
                            }
                            devices.get(connectingIdx).setState(UserDevice.STATE_CONNECTING);
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
                } else {
                    if (User.getUser().getFeState() == User.FESTATE_DISABLED) {
                        Toast.makeText(SettingActivity.this, getString(R.string.ble_disabled), Toast.LENGTH_SHORT).show();
                    } else if (devices.get(position).getState() == UserDevice.STATE_OFF) {
                        Toast.makeText(SettingActivity.this, getString(R.string.device_off), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                UIHelper.goDebugActivity(SettingActivity.this);
//                UIHelper.goMainAeroCardioActivity(SettingActivity.this);
                startActivity(new Intent(SettingActivity.this,
                        AeroCardioLoginActivity.class));
                SettingActivity.this.finish();
            }
        });

        research.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBleOff) {
                    Toast.makeText(SettingActivity.this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (research.getVisibility() == View.VISIBLE) {
                    research.setText(getResources().getString(R.string.rescanning));
                    resetDeviceList();
                    BleFeComm.getClient().disconnect();
                    BleFeComm.startBleScan();
                }
            }
        });

    }

    public void resetDeviceList() {
        if (mainService != null) {
            devices.clear();
            //reset device list
            List<UserDevice> boundedDevices = BleFeComm.getBoundedDevices();
            if (boundedDevices != null) {
                for (UserDevice dev : boundedDevices) {
                    dev.setState(UserDevice.STATE_OFF);
                    devices.add(dev);
                }
            }

            UserDevice deviceConnected = BleFeComm.getUserDevice();
            if (deviceConnected != null) {
                for (int m = 0; m < devices.size(); m++) {
                    UserDevice dev = devices.get(m);
                    if (dev.getMacAddr().equals(deviceConnected)) {
                        dev.setState(UserDevice.STATE_CONNECTED);
                        connectedIdx = m;
                        break;
                    }
                }
            }

            deviceListAdapter.setReconnect(false);

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

    private class DeviceListAdapter extends ArrayAdapter<UserDevice> {
        private List<UserDevice> devices;
        private int resourceId;
        private ViewHolder vHolder;
        private LayoutInflater inflater;
        private boolean isReconnect;
        private int connectedIdx;
        private int connectingIdx;

        public DeviceListAdapter(Context ctx, int resourceId, List<UserDevice> devices) {
            super(ctx, resourceId, devices);
            this.resourceId = resourceId;
            this.devices = devices;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        public void setReconnect(boolean isReconnect) {
            this.isReconnect = isReconnect;
        }

        public int getCount() {
            return devices.size();
        }

        @Override
        public UserDevice getItem(int position) {
            return devices.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                vHolder = new ViewHolder();
                convertView = inflater.inflate(resourceId, null);
                vHolder.devIcon = (ImageView) convertView.findViewById(R.id.icon_dev);
                vHolder.devName = (TextView) convertView.findViewById(R.id.dev_name);
                vHolder.devMac = (TextView) convertView.findViewById(R.id.dev_mac);
                vHolder.runningIcon = (RunningIconView) convertView.findViewById(R.id.iconRunning);
                vHolder.checkIcon = (CheckIconView) convertView.findViewById(R.id.iconCheck);
                vHolder.connectedIcon = (ImageView) convertView.findViewById(R.id.iconConnected);

                convertView.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }

            UserDevice dev = devices.get(position);

            vHolder.devName.setText(dev.getName());
            vHolder.devMac.setText(dev.getMacAddr());

            if (dev.getState() == UserDevice.STATE_CONNECTED) {
                L.i("<UI> connected update device list");
                vHolder.devIcon.setImageResource(R.drawable.dev_on);
                vHolder.checkIcon.setVisibility(View.VISIBLE);
                vHolder.checkIcon.startInitAnimation();

                vHolder.connectedIcon.setVisibility(View.INVISIBLE);
                vHolder.runningIcon.setVisibility(View.INVISIBLE);
                vHolder.runningIcon.clearAnimation();

            } else if (dev.getState() == UserDevice.STATE_ON) {
//                L.i("<UI> device online");
                vHolder.devIcon.setImageResource(R.drawable.dev_on);

                vHolder.checkIcon.setVisibility(View.INVISIBLE);
                vHolder.checkIcon.clearAnimation();
                vHolder.connectedIcon.setVisibility(View.INVISIBLE);
                vHolder.runningIcon.setVisibility(View.INVISIBLE);
                vHolder.runningIcon.clearAnimation();
            } else if (dev.getState() == UserDevice.STATE_OFF) {
//                L.i("<UI> device off");
                vHolder.devIcon.setImageResource(R.drawable.dev_off);

                vHolder.checkIcon.setVisibility(View.INVISIBLE);
                vHolder.checkIcon.clearAnimation();

                vHolder.connectedIcon.setVisibility(View.INVISIBLE);
                vHolder.runningIcon.setVisibility(View.INVISIBLE);
                vHolder.runningIcon.clearAnimation();
            } else if (dev.getState() == UserDevice.STATE_CONNECTING) {
//                L.i("<UI> device connecting");
                vHolder.devIcon.setImageResource(R.drawable.dev_on);

                vHolder.checkIcon.clearAnimation();
                vHolder.checkIcon.setVisibility(View.INVISIBLE);

                vHolder.connectedIcon.setVisibility(View.INVISIBLE);

                vHolder.runningIcon.setVisibility(View.VISIBLE);
                vHolder.runningIcon.startInitAnimation();
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView devName;
        TextView devMac;
        ImageView devIcon;
        RunningIconView runningIcon;
        CheckIconView checkIcon;
        ImageView connectedIcon;
    }
}
