package com.uteamtec.heartcool.comm;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import com.uteamtec.heartcool.utils.L;

import java.util.List;
import java.util.UUID;

/**
 * BLE通讯类
 * Created by liulingfeng on 2015/9/24.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeComm {
    public static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    public static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    public static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    public static final int STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING;

    public static final int ERR_SERVICENOTFOUND = 1;
    public static final int ERR_CHARACTERISTICSMISSING = 2;
    public static final int ERR_SERVICEDISCOVERFAILED = 3;
    public static final int ERR_NOBLEDEVICE = 4;

    public static final UUID SERVICE_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb7");
    public static final UUID TX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cba");
    public static final UUID RX_UUID = UUID.fromString("0783b03e-8535-b5a0-7140-a304d2495cb8");

    private BluetoothDevice bleDevice;
    private BluetoothGatt gatt;
    private BluetoothGattService gattService;

    private BluetoothLeCommListener commListener;
    private BluetoothLeDataListener dataListener;
    private BluetoothGattCharacteristic txCharacteristic;
    private BluetoothGattCharacteristic rxCharacteristic;

    private Context ctx;

    private int state;


    private BluetoothGattCallback gattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED){
                state = newState;
                L.i("<BLE> connected BLE");
                gatt.discoverServices();

                if (commListener != null) {
                    commListener.onStateChanged(state);
                }

//                gattService = gatt.getService(SERVICE_UUID);
//                if (gattService != null) {
//                    rxCharacteristic = gattService.getCharacteristic(RX_UUID);
//                    txCharacteristic = gattService.getCharacteristic(TX_UUID);
//
//                    if (rxCharacteristic != null && txCharacteristic != null) {
//                        final int prop = rxCharacteristic.getProperties();
//                        if ((prop | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            //allowing characteristic notification (trigger onCharacteristicChanged)
//                            gatt.setCharacteristicNotification(rxCharacteristic, true);
//                        }
//                        L.i("<BLE> characteristics setting successful");
//                    }
//                    else {
//                        L.i("<BLE> characteristics not found");
//                        disconnect();
//                    }
//                }
//                else {
//                    L.i("<BLE> service not found");
//                    disconnect();
//                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                state = newState;
                L.i("<BLE> FE disconnected");

                if (commListener != null) {
                    commListener.onStateChanged(state);
                }
            }
            else if (newState == BluetoothProfile.STATE_CONNECTING){
                state = newState;
                L.i("<BLE> connecting FE");

                if (commListener != null) {
                    commListener.onStateChanged(state);
                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                boolean chMissing = true;
                boolean svMissing = true;

                gattService = gatt.getService(SERVICE_UUID);
                if (gattService != null) {
                    L.i("<BLE> discovered service");
                    svMissing = false;
                    //found service

                    //get service
                    txCharacteristic = gattService.getCharacteristic(TX_UUID);
                    rxCharacteristic = gattService.getCharacteristic(RX_UUID);

                    /*
                     *test code
                     */
                    List<BluetoothGattService> gattServices = gatt.getServices();
                    for (BluetoothGattService gattserv : gattServices) {
                        L.i("<TEST> serv uuid = " + gattserv.getUuid().toString());
                        List<BluetoothGattCharacteristic> chs = gattserv.getCharacteristics();
                        L.i("<TEST> chs num = " + Integer.toString(chs.size()));
                        for (BluetoothGattCharacteristic ch : chs) {
                            L.i("<TEST> char uuid = " + ch.getUuid().toString());
                        }
                    }

                    if (txCharacteristic != null && rxCharacteristic != null)  {
                        chMissing = false;
                        L.i("<BLE> discovered characteristics");
                        final int prop = rxCharacteristic.getProperties();
                        if ((prop | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

                           /*
                            * allowing characteristic notification (trigger onCharacteristicChanged)
                            */
                            gatt.setCharacteristicNotification(rxCharacteristic, true);
                        }

                    }
                }

                /*
                 *if service and characteristic not matching, close the connection
                 */
                if (svMissing) {
                    if (commListener != null) {
                        commListener.onErr(ERR_SERVICENOTFOUND);
                    }

                    L.i("<BLE> errs service not found, disconnect BLE");
                    disconnect();
                    return;
                }
                if (chMissing) {
                    if (commListener != null) {
                        commListener.onErr(ERR_CHARACTERISTICSMISSING);
                    }

                    L.i("<BLE> errs: no charac found, disconnect BLE");
                    disconnect();
                    return;
                }
            }
            else {
                L.i("<BLE> failed to search service");
                if (commListener != null) {
                    commListener.onErr(ERR_SERVICEDISCOVERFAILED);
                }
                disconnect();
            }
        }

        //接收数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic){
            if (dataListener != null && characteristic.equals(rxCharacteristic)) {
//                L.i("<BLE> <RX> received data from FE");
                dataListener.onReceivedData(characteristic.getValue());
            }
        }

    };

    public BluetoothLeComm(Context ctx){
        this.ctx = ctx;
        this.state = STATE_DISCONNECTED;
    }

    synchronized public void connect() {

        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }

        if (bleDevice != null) {
            L.i("<BLE> connect addr=" + bleDevice.getAddress());
            state = STATE_CONNECTING;
            if (commListener != null) {
                commListener.onStateChanged(state);
            }
            gatt = bleDevice.connectGatt(ctx, false, gattCallback);

        }
        else {
            L.i("<BLE> ble device is not specified");
            state = STATE_DISCONNECTED;
            if (commListener != null) {
                commListener.onStateChanged(state);
                commListener.onErr(ERR_NOBLEDEVICE);
            }
        }
    }


    synchronized public void connect(BluetoothDevice device) {
        this.bleDevice = device;

        if (gatt != null) {
            L.i("<BLE> closing gatt before new connection");
            gatt.disconnect();
            gatt.close();
        }
        gatt = bleDevice.connectGatt(ctx, false, gattCallback);
        state = STATE_CONNECTING;
        if (commListener != null) {
            commListener.onStateChanged(state);
        }
    }


    synchronized public void disconnect(){
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }

        state = STATE_DISCONNECTED;
        if (commListener != null) {
            commListener.onStateChanged(state);
        }
    }

    public void send(byte[] data){
        if (state == STATE_CONNECTED && txCharacteristic != null) {
            txCharacteristic.setValue(data);
            gatt.writeCharacteristic(txCharacteristic);
        }
    }

    public String getAddress() {
        return bleDevice.getAddress();
    }

    public void setBleDevice (BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public interface BluetoothLeCommListener{
        void onErr(int errCode);
        void onStateChanged(int status);
    }

    public void setBluetoothLeCommListener(BluetoothLeCommListener listener) {
        this.commListener = listener;
    }

    public interface BluetoothLeDataListener{
        void onReceivedData(byte[] data);
    }

    public void setBluetoothLeDataListener(BluetoothLeDataListener dataListener){
        this.dataListener = dataListener;
    }
}
