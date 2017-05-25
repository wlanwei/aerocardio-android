package com.uteamtec.heartcool.comm;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

/**
 * 蓝牙4.0管理(设备是否支持，设备是否打开，设备扫描)
 */
public class BluetoothLeMgr {

    private Context ctx;
    private BluetoothAdapter adapter;

    private BluetoothLeMgrListener listener;

    private BluetoothAdapter.LeScanCallback leScanCallback;

    private CyclicThread cyclicTask;

    public BluetoothLeMgr(Context ctx) {
        this.ctx = ctx;

        adapter = BluetoothAdapter.getDefaultAdapter();

        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (listener != null) {
                    listener.OnFoundBleDevice(device, rssi, scanRecord);
                }
            }
        };

        cyclicTask = new CyclicThread();
        cyclicTask.start();
    }

    /**
     * 开始扫描蓝牙设备
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startLeScan() {
        if (adapter.isEnabled()) {
            adapter.startLeScan(leScanCallback);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopLeScan();
                }
            }, 2000); //shutdown the scan after 10 seconds
        }
    }

    /**
     *  停止扫描
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopLeScan(){
        if (adapter.isEnabled()) {
            adapter.stopLeScan(leScanCallback);
            if (listener != null) {
                listener.OnLeScanFinished(); //inform LeScan finishing
            }
        }
    }

    public BluetoothDevice getBleDevice(String addr) {
        if (addr != null && adapter != null) {
            return adapter.getRemoteDevice(addr);
        }
        else {
            return null;
        }
    }

    public interface BluetoothLeMgrListener {
        void OnFoundBleDevice(BluetoothDevice device, int rssi, byte[] scanRecord);
        void OnLeScanFinished();
        void OnBluetoothDisabled();
        void OnBluetoothEnabled();
    }

    public void setOnScanBluetoothDeviceListener(BluetoothLeMgrListener listener){
        this.listener = listener;
    }

    public boolean isBleEnabled() {
        if (adapter == null){
            return false;
        }
        else if (!adapter.isEnabled()) {
            return false;
        }
        else {
            return true;
        }
    }

    private class CyclicThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                if (adapter == null || !adapter.isEnabled()) {
                    if (listener != null) {
                        listener.OnBluetoothDisabled();
                    }
                }
                else {
                    listener.OnBluetoothEnabled();
                }
            }
        }
    }
}

