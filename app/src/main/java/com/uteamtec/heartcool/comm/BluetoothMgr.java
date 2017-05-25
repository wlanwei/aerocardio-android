package com.uteamtec.heartcool.comm;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

import java.util.Set;
import java.util.UUID;

/**
 * 蓝牙管理
 * Created by liulingfeng on 2015/9/26.
 */
public class BluetoothMgr {
    private static final String DEFAULT_NAME = "aerocardio";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter btAdapter;
    private Context context;
    private BluetoothComm btComm;
    private BluetoothLeComm bleComm;

    private Set<BluetoothDevice> btDevSet;
    private Set<BluetoothDevice> bleDevSet;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BluetoothMgr(Context context) {
        this.context = context;
        BluetoothManager bm = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bm.getAdapter();
    }

    public void startScan(){

    }

    public void stopScan(){

    }

    public void startLeScan(){

    }

    public void stopLeScan(){

    }

    public void bondDevice(BluetoothDevice device){
        device.getBondState();
    }

    public BluetoothDevice searchDevice(String devMac){
        boolean foundDevice = false;

        BluetoothDevice btDev = null;
        for (BluetoothDevice device : btDevSet) {
            if (device.getAddress().equals(devMac)) {
                btDev = device;
                break;
            }
        }
        if (btDev == null) {
            for (BluetoothDevice device : bleDevSet) {
                if (device.getAddress().equals(devMac)) {
                    btDev = device;
                    break;
                }
            }
        }
        return btDev;
    }

    public boolean isBleDevice(BluetoothDevice bleDev){
        if (bleDevSet.contains(bleDev)) {
            return true;
        }
        else {
            return false;
        }
    }
}
