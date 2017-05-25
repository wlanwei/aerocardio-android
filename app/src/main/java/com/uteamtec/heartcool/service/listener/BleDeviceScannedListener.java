package com.uteamtec.heartcool.service.listener;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙扫描回调接口
 * Created by wd
 */
public interface BleDeviceScannedListener {

    void onBleDeviceScanned(BluetoothDevice device); //扫描到BLE设备

    void onBleScanFinished(); //扫描结束
}
