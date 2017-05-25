package com.uteamtec.heartcool.comm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by liulingfeng on 2015/9/22.
 * BluetoothComm is the common Bluetooth communication
 */
public class BluetoothComm {

    private static final String NAME_SDP = "UTEAMTECSDP";

    //UUIDs of the application
    private static final UUID UUID_SDP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int DEFAULT_IOBUFF_SIZE = 500;
    public static final int DEFAULT_LISTEN_TIME = 60;

    //status and err code
    public static final int CODE_ERR_ONCONNECTING = 1;
    public static final int CODE_ERR_DISCONNECT = 2;
    public static final int CODE_ERR_DEVICENOTFOUND = 3; //error when device is not found
    public static final int CODE_ERR_BLUETOOTHOFF = 4; //erro when bluetooth is off
    public static final int CODE_ERR_IOCLOSE = 5; //erro when IO close operation failed
    public static final int CODE_ERR_IOOPEN = 6; //erro when IO open operation failed

    public static final int STAT_OFF = 1;
    public static final int STAT_LISTENING = 2;
    public static final int STAT_IDLE = 3;
    public static final int STAT_CONNECTED = 4;
    public static final int STAT_DISCONNECT = 5;

    private Context context;
    private BluetoothAdapter btAdapter;
    private BluetoothServerSocket srvSkt;
    private BluetoothSocket skt;
    private BluetoothDevice btDev; //reference of connected bluetooth device (in slave mode)
    private String btDevMac; //address of connected bluetooth device

    private int status;

    private BluetoothCommListener listener;

    private ConnectedThread connectedThread;
    private ConnectThread connectThread;
    private AcceptThread acceptThread;

    public BluetoothComm(BluetoothAdapter btAdapter){

        this.btAdapter = btAdapter;

        if (btAdapter == null || !btAdapter.isEnabled()) {
            status = STAT_OFF;
        } else {
            status = STAT_LISTENING;
            acceptThread = new AcceptThread(this);
            acceptThread.start();
        }
    }

    /*
    connect device previously connected
     */
    public synchronized void connect() {
        if (status == STAT_OFF) {
            if (listener != null) {
                listener.onErr(CODE_ERR_BLUETOOTHOFF);
            }
        }
        else {
            if (btDev == null) {
                if(listener != null) {
                    listener.onErr(CODE_ERR_DEVICENOTFOUND);
                }
            }
            else {
                try {
                    skt = btDev.createInsecureRfcommSocketToServiceRecord(UUID_SDP);
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onErr(CODE_ERR_IOOPEN);
                    }
                }
                //cancel all current threads and start a connectthread
                if (acceptThread != null) {
                    acceptThread.quit();
                }
                if (connectedThread != null) {
                    connectedThread.quit();
                }

                if (btDev != null) {
                    connectThread = new ConnectThread(this, skt);
                    connectThread.start();
                }
            }
        }
    }

    public synchronized void connect(String devMac) {
        if (status == STAT_OFF) {
            if (listener != null) {
                listener.onErr(CODE_ERR_BLUETOOTHOFF);
            }
        }
        else {
            //if dev has been connected before
            if (this.btDevMac.equals(devMac)) {
                connect();
            }
            else {
                Set<BluetoothDevice> btDevSet = btAdapter.getBondedDevices();
                boolean foundDevice = false;
                for (BluetoothDevice btDev : btDevSet) {
                    if (btDev.getAddress() == devMac) {
                        foundDevice = true;
                        this.btDev = btDev;
                        this.btDevMac = btDev.getAddress();
                        try {
                            skt = btDev.createInsecureRfcommSocketToServiceRecord(UUID_SDP);
                        } catch (IOException e) {
                            if (listener != null) {
                                listener.onErr(CODE_ERR_IOOPEN);
                            }
                        }
                    }
                }

                if (!foundDevice) {
                    if (listener != null) {
                        listener.onErr(CODE_ERR_DEVICENOTFOUND);
                    }
                } else {
                    if (skt != null) {
                        //cancel all current threads and start a connectthread
                        if (acceptThread != null) {
                            acceptThread.quit();
                        }
                        if (connectedThread != null) {
                            connectedThread.quit();
                        }

                        connectThread = new ConnectThread(this, skt);
                        connectThread.start();
                    }
                }

            }
        }
    }

    public synchronized void disconnect(){
        setStatus(STAT_DISCONNECT);

        if (connectedThread != null) {
            connectedThread.quit();
        }

        setStatus(STAT_IDLE);
    }

    public synchronized int send(byte data[], int len) {
        int ret = 0;
        if( status == STAT_CONNECTED && connectedThread != null) {
            connectedThread.send(data, len);
        }
        else {
            ret = -1;
        }
        return ret;
    }

    public synchronized void startlisten(){
        if (acceptThread != null) {
            acceptThread.quit();
        }

        this.setStatus(STAT_LISTENING);

        acceptThread = new AcceptThread(this);
        acceptThread.start();
    }

    public synchronized void stopListen(){
        if (acceptThread != null) {
            acceptThread.quit();
        }

        this.setStatus(STAT_IDLE);
    }

    public synchronized int getStatus() {
        return status;
    }

    public synchronized void setStatus(int status) {
        this.status = status;
        if (listener != null) {
            listener.onStatusChanged(status);
        }
    }


    private class ConnectedThread extends Thread {

        public static final int STAT_GO = 1;
        public static final int STAT_PAUSE = 2;
        public static final int STAT_QUIT = 3;

        private int stat;

        private BluetoothComm btComm;
        private BluetoothSocket skt;
        private InputStream input;
        private OutputStream output;

        private byte ioBuff[];
        int len;

        public ConnectedThread(BluetoothComm btComm, BluetoothSocket skt){
            this.btComm = btComm;
            this.skt = skt;
            try {
                input = skt.getInputStream();
                output = skt.getOutputStream();
            } catch (IOException e) {
                if (btComm.listener != null) {
                    btComm.listener.onErr(CODE_ERR_DISCONNECT);
                }
                btComm.setStatus(STAT_IDLE);
            }

            this.ioBuff = new byte[DEFAULT_IOBUFF_SIZE];

            stat = STAT_GO;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) { }

                synchronized (this) {
                    if (stat == STAT_GO) {
                        try {
                            len = input.read(ioBuff);
                        } catch (IOException e) {
                            btComm.setStatus(STAT_IDLE);
                            try {
                                skt.close();
                                if (btComm.listener != null) {
                                    btComm.listener.onErr(BluetoothComm.CODE_ERR_DISCONNECT);
                                }
                            } catch (IOException e1) { }
                        }
                        if (len > 0 && btComm.listener != null) {
                            btComm.listener.onReceivedData(ioBuff, len);
                        }
                    } else if (stat == STAT_PAUSE) {
                        //do nothing
                    }
                    else {
                        break;
                    }
                }
            }
        }

        public synchronized void go() {
            this.stat = STAT_GO;
        }

        public synchronized void pause() {
            this.stat = STAT_PAUSE;
        }

        public synchronized void quit() {
            this.stat = STAT_QUIT;
        }

        public synchronized int send(byte data[], int len) {
            int ret = 0;
            try {
                output.write(data, 0, len);
            }
            catch (IOException e) {
                ret = -1;
                btComm.setStatus(BluetoothComm.STAT_IDLE);
                try {
                    skt.close();
                } catch (IOException e1) { }
                if(btComm.listener != null) {
                    listener.onErr(CODE_ERR_DISCONNECT);
                }
            }

            return ret;
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothComm btComm;
        private BluetoothSocket skt;

        public ConnectThread(BluetoothComm btComm, BluetoothSocket skt) {
            this.btComm = btComm;
            this.skt = skt;
        }

        @Override
        public synchronized void run() {
            if( skt != null) {
                try {
                    skt.connect();
                } catch (IOException e) {
                    if (btComm.listener != null) {
                        listener.onErr(CODE_ERR_DISCONNECT);
                    }
                    return;
                }

                if (skt != null) {
                    connectedThread = new ConnectedThread(btComm, skt);
                    connectedThread.start();
                }
            }
        }
    }

    private class AcceptThread extends Thread {
        public final static int STAT_GO = 1;
        public final static int STAT_PAUSE = 2;
        public final static int STAT_QUIT = 3;

        private BluetoothComm btComm;
        private BluetoothServerSocket srvSkt;
        private BluetoothSocket skt;
        private int status;

        public AcceptThread(BluetoothComm btComm) {
            this.btComm = btComm;
            try {
                srvSkt = btComm.btAdapter.listenUsingRfcommWithServiceRecord(BluetoothComm.NAME_SDP, BluetoothComm.UUID_SDP);
            } catch (IOException e) {
                btComm.setStatus(BluetoothComm.STAT_IDLE);
                if (btComm.listener != null) {
                    btComm.listener.onErr(CODE_ERR_IOOPEN);
                }
            }

            if (srvSkt != null) {
                btComm.setStatus(BluetoothComm.STAT_LISTENING);
                status = STAT_GO;
            }
        }

        @Override
        public void run(){
            long tic = System.currentTimeMillis();
            while(true) {
                try {
                    Thread.sleep(1);
                }
                catch (InterruptedException e) { }

                if (System.currentTimeMillis() - tic > DEFAULT_LISTEN_TIME) {
                    btComm.setStatus(BluetoothComm.STAT_IDLE);
                    break;
                }
                else {
                    synchronized (this) {
                        if (status == STAT_GO) {
                            try {
                                skt = srvSkt.accept(2);
                            } catch (IOException e) {
                                try {
                                    skt.close();
                                }
                                catch (IOException e1) {
                                }
                                if (btComm.listener != null) {
                                    listener.onErr(0);
                                }
                                break;
                            }

                            //close srvskt first
                            try {
                                srvSkt.close();
                            }
                            catch (IOException e) { }

                            if (skt != null) { //a connection is established
                                connectedThread = new ConnectedThread(btComm, skt);
                                connectedThread.start();

                                btComm.setStatus(BluetoothComm.STAT_CONNECTED);

                                if(btComm.listener != null) {
                                    btComm.listener.onConnected(skt);
                                }

                                break; //quit the acceptthread
                            }
                            // otherwise continue
                        }
                        else if (status == STAT_PAUSE) {

                        }
                        else {
                            break;
                        }
                    }
                }
            }
        }

        public synchronized void go(){
            status = STAT_GO;
        }

        public synchronized void pause(){
            status = STAT_PAUSE;
        }

        public synchronized void quit(){
            status = STAT_QUIT;
        }

    }

    public interface BluetoothCommListener {
        void onConnected(BluetoothSocket skt); //return the mac address of the connected bluetooth device

        void onReceivedData(byte ioBuff[], int len); //send back the received data from the connection

        void onErr(int code);

        void onStatusChanged(int status);
    }

    public void setBluetoothCommListener(BluetoothCommListener listener) {
        this.listener = listener;
    }
}
