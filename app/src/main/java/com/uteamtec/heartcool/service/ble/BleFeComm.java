package com.uteamtec.heartcool.service.ble;

import com.inuker.bluetooth.library.model.BleGattProfile;
import com.uteamtec.heartcool.service.ecg.EcgDataPostProcessThread;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.utils.L;

import org.wd.blekit.BleClient;
import org.wd.blekit.BleConnectListener;
import org.wd.blekit.BleIOResponse;
import org.wd.blekit.BleManager;
import org.wd.blekit.BleScanner;
import org.wd.blekit.BleScannerDevice;
import org.wd.blekit.BleScannerListener;
import org.wd.blekit.BleState;
import org.wd.blekit.BleStateListener;

import java.util.List;

/**
 * Created by wd
 */
public final class BleFeComm {

    private BleFeComm() {
        BleManager.getState().setListener(new BleStateListener() {
            @Override
            public void onBluetoothEnabled() {
                if (User.getUser().getFeState() == User.FESTATE_DISABLED) {
                    //only invoked when turn ble on
                    User.getUser().setFeState(User.FESTATE_DISCONNECTED);
                    if (ListenerMgr.getUserStateChangedListener() != null) {
                        ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.getUser().getFeState());
                    }
                }
            }

            @Override
            public void onBluetoothDisabled() {
                User.getUser().setFeState(User.FESTATE_DISABLED);
                if (ListenerMgr.getUserStateChangedListener() != null) {
                    ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.getUser().getFeState());
                }
            }
        });
        BleManager.getState().start();
        BleManager.getScanner().setListener(new BleScannerListener() {
            @Override
            public void onScannerStarted() {
                L.e("BleFeComm.onScannerStarted");
            }

            @Override
            public void onDeviceFounded(BleScannerDevice device) {
//                L.e("BleFeComm.onDeviceFounded: " + device.getName() + " - " + device.getAddress());
                if (ListenerMgr.getBleDeviceScannedListener() != null) {
                    ListenerMgr.getBleDeviceScannedListener().onBleDeviceScanned(device.device);
                }
            }

            @Override
            public void onScannerStopped() {
                L.e("BleFeComm.onScannerStopped");
                if (ListenerMgr.getBleDeviceScannedListener() != null) {
                    ListenerMgr.getBleDeviceScannedListener().onBleScanFinished();
                }
            }
        });
        BleManager.getClient().setListener(new BleConnectListener() {
            @Override
            public void onConnecting() {
                onDisconnected();
                L.e("BleFeComm.onConnecting");
                User.getUser().setFeState(User.FESTATE_CONNECTING);
                if (ListenerMgr.getUserStateChangedListener() != null) {
                    ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.getUser().getFeState());
                }
            }

            @Override
            public void onConnected() {
                L.e("BleFeComm.onConnected");
                User.getUser().setFeState(User.FESTATE_CONNECTED);

                User.getUser().resetLastFeMessageTime();
                User.getUser().setIsDevReset(false); //when connected, device is assumed to be not resetted

                BleFePulseTxThread.startThread();

                if (ListenerMgr.getUserStateChangedListener() != null) {
                    ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.getUser().getFeState());
                }
            }

            @Override
            public void onDisconnecting() {
                L.e("BleFeComm.onDisconnecting");
                User.getUser().setFeState(User.FESTATE_DISCONNECTING);

                if (ListenerMgr.getUserStateChangedListener() != null) {
                    ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.getUser().getFeState());
                }
            }

            @Override
            public void onDisconnected() {
                L.e("BleFeComm.onDisconnected");
                User.getUser().setFeState(User.FESTATE_DISCONNECTED);

                //unregister previous connected device
                User.getUser().setIsTimeInit(false); //set false here so the next connection will re-initialized the time
                User.getUser().setUserDevice(null); //if null, blecomm should keep last time connection
                //invalidate datapostprocess
                EcgDataPostProcessThread.setResolution(0);

                //reset feCoder
                BleFeTxCoder.getCoder().flush();
                BleFeTxCoder.getCoder().setResolution(-1);
                BleFeTxCoder.getCoder().setStreamLength(-1);
//                    UserMgr.getUser().setLastEcgVal(0, 0, 0);

                BleFePulseTxThread.stopThread();

                if (ListenerMgr.getUserStateChangedListener() != null) {
                    ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.getUser().getFeState());
                }
            }

            @Override
            public void onResponseSuccess(BleGattProfile profile) {
                L.e("BleFeComm.onResponseSuccess: " + profile);
                BleManager.getClient().notify(BleFeConstant.SERVICE_UUID, BleFeConstant.RX_UUID,
                        new BleIOResponse.Notify() {
                            @Override
                            public void onNotify(byte[] data) {
//                                L.e("<BLE> R = " + BleUtils.bytesToHex(data));
                                BleFeTxCoder.getCoder().putBytes(data, data.length);
                            }
                        });
            }

            @Override
            public void onResponseFailed() {
                L.e("BleFeComm.onResponseFailed");
            }

            @Override
            public void onRssi(Integer integer) {
                L.e("BleFeComm.onRssi: " + integer.toString());
            }
        });
    }

    private static void init() {
        if (_comm == null) {
            synchronized (BleFeComm.class) {
                if (_comm == null) {
                    _comm = new BleFeComm();
                }
            }
        }
    }

    private static BleFeComm _comm;

    public static BleState getState() {
        init();
        return BleManager.getState();
    }

    public static BleScanner getScanner() {
        init();
        return BleManager.getScanner();
    }

    public static BleClient getClient() {
        init();
        return BleManager.getClient();
    }

    /**
     * 开始扫描蓝牙设备
     */
    public static void startBleScan() {
        BleFeComm.getScanner().Scan(1000);
    }

    /**
     * 获取蓝牙设备列表
     */
    public static List<UserDevice> getBoundedDevices() {
        return User.getUser().getUserDevices().getListDevices();
    }

    /**
     * 获取连接的蓝牙设备
     */
    public static UserDevice getUserDevice() {
        return User.getUser().getUserDevice();
    }

}
