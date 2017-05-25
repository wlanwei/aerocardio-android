package com.uteamtec.heartcool;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.messages.AppMessageCoder;
import com.uteamtec.heartcool.messages.FeMessage;
import com.uteamtec.heartcool.messages.FeMessageCoder;
import com.uteamtec.heartcool.messages.MessageUtils;
import com.uteamtec.heartcool.service.ble.BleFeComm;
import com.uteamtec.heartcool.service.ble.BleFePulseTxThread;
import com.uteamtec.heartcool.service.ble.BleFeThread;
import com.uteamtec.heartcool.service.ble.BleFeTxCoder;
import com.uteamtec.heartcool.service.ble.BleFeTxQueue;
import com.uteamtec.heartcool.service.ble.BleFeTxThread;
import com.uteamtec.heartcool.service.ecg.EcgDataPostProcessThread;
import com.uteamtec.heartcool.service.ecg.EcgDisplayThread;
import com.uteamtec.heartcool.service.ecg.EcgMarkPostProcessThread;
import com.uteamtec.heartcool.service.ecg.EcgMarkQueue;
import com.uteamtec.heartcool.service.ecg.EcgQueue;
import com.uteamtec.heartcool.service.listener.ListenerMgr;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetThread;
import com.uteamtec.heartcool.service.net.AppNetTxCoder;
import com.uteamtec.heartcool.service.net.AppNetTxQueue;
import com.uteamtec.heartcool.service.net.AppNetTxThread;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.type.UserSaveType;
import com.uteamtec.heartcool.utils.L;

import java.io.IOException;

/**
 * Created by Lingfeng
 * Maintained by
 * Last edit: 2015.11.11
 * 应用主服务
 */
//Data required:
//user: id, key, input (userName, userCode)
//device: id, type, sps, streamFramelength
//databuffer: dataraw, dataFiltered, marks
//dataCommBuffer: merged dataFiltered, transmit buffer queue
//User declared here

//When service started:
//1. get user id (imei of phone) & key (from sharedpreference or web login)
//2. establish socket connection
//    2.1 login by id+key
//    2.2 send data
//3. establish FE communication:
//    3.1 establish bluetoothLe connection
//    3.2 waiting for REGISTER message to register ECG device (locally for old and remotely for new device)
//    3.3 link ECG stream and mark buffers


public class MainMgrService extends Service {
//    public final static String INTENT_TYPE_REF = "type";
//    public final static int INTENT_TYPE_DEFAULT = 1; //normal starting
//    public final static int INTENT_TYPE_SETTING = 2;
//    public final static int INTENT_TYPE_MAIN = 3;
//    public final static int INTENT_TYPE_LOGIN = 4; //bound by login activity

    private static final String TAG = MainMgrService.class.getSimpleName();
    //service binder
    private MainMgrBinder mBinder = new MainMgrBinder();

    public class MainMgrBinder extends Binder {
        public MainMgrService getService() {
            return MainMgrService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Service -> onBind");
        onStartService();
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "Service -> onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Service -> onUnbind");
        onStopService();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "Service -> onCreate");
        super.onCreate();

        // 0. initialize user
        User.getUser().reset();

        //1. initialize all queues
        //初始化各个数据缓存

        //2. initialize coders
        //FE coder
        BleFeTxCoder.getCoder().setCodeCallback(new FeMessageCoder.CodeCallback() {
            @Override
            public void onDecodeMessage(FeMessage msg) {
//                L.i("<FECODER> decoded message type = " + Integer.toString(msg.getType()));
//                String str = "";
//                for (byte b : msg.getBody()) {
//                    str += Integer.toHexString( b & 0x00ff) + " ";
//                }
//                L.i("<FECODER> decoded message = " + str);

                try {
                    handleFeMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //App coder
        AppNetTxCoder.getCoder().setCodeCallback(new AppMessageCoder.CodeCallback() {
            @Override
            public void onDecodeMessage(AppMessage msg) {
                handleAppMessage(msg);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "Service -> onDestroy");
        super.onDestroy();
        onStopService();
    }

    private void onStartService() {

        AppNetTxCoder.startDecode();
        BleFeTxCoder.startDecode();

        //Transmit threads, 消息队列发送线程
        AppNetTxThread.startThread();
        BleFeTxThread.startThread();

        //5. 后处理（用于数据转发，存储，和向activity传递）
        EcgDataPostProcessThread.startThread();
        EcgMarkPostProcessThread.startThread();

        //6. 启动 mainThread
        AppNetThread.startThread();
        BleFeThread.startThread();

        //test thread
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Random random = new Random();
//                long time = System.currentTimeMillis();
//                UserMgr.getUser().setConnectedDevice(UserMgr.getUser().getPrevDevice());
//                UserMgr.getUser().getUserDevice().setSps(500);
//                UserMgr.getUser().getUserDevice().setStreamLen(25);
//                UserMgr.getUser().setFeState(User.FESTATE_REGISTERED);
//                int cnt = 0;
//                int idx = 0;
//                while(true) {
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
////                    L.i("<THREAD> test thread");
//                    int val[] = new int[25];
//
//                    for (int m = 0; m < 25; m ++) {
////                        val[m] = Math.round((float) Math.cos(cnt/1000*Math.PI)*100);
//                        val[m] =  random.nextInt(100);
//                    }
//                    cnt --;
//                    Ecg ecg = new Ecg(Ecg.TYPE_SINGLE, time, time+50, 500, val);
//
//                    /*
//                     * putting data into coder
//                     */
//                    byte dat[] = BleFeTxCoder.getCoder().encode(FeMessage.createEcg(ecg, idx++));
//                    BleFeTxCoder.getCoder().putBytes(dat, dat.length);
//                    time+=50;
//                }
//            }
//        }).start();


        // start connection immediately after initialization
        if (User.getUser().hasPrevUserDevice()) {
            if (BleFeComm.getClient().connect(User.getUser().getPrevUserDevice().getMacAddr())) {
                User.getUser().setTimeLastConnectFe(System.currentTimeMillis());
            }
        }

        /*
         * enable data display immediately after initialization
         */
        EcgDisplayThread.startThread();
    }

    private void onStopService() {
        MobclickAgent.onProfileSignOff();

        AppNetThread.stopThread();
        BleFeThread.stopThread();

        EcgDataPostProcessThread.stopThread();
        EcgMarkPostProcessThread.stopThread();

        AppNetTxThread.stopThread();
        BleFeTxThread.stopThread();
        BleFePulseTxThread.stopThread();
        EcgDisplayThread.stopThread();

        BleFeComm.getClient().disconnect();
        AppNetTcpComm.disconnect(false);

        AppNetTxCoder.stopDecode();
        BleFeTxCoder.stopDecode();
    }

    /**
     * FE消息处理
     *
     * @param msg
     * @throws IOException
     */
    private void handleFeMessage(FeMessage msg) throws IOException {
//        L.i("<SERVICE> app state = " + Integer.toString(UserMgr.getUser().getAppState()));
//        L.e("<FeMessage> type = " + msg.getType());
        synchronized (User.getUser()) {
            switch (msg.getType()) {
                case FeMessage.TYPE_STREAM_ECG_1:
                case FeMessage.TYPE_STREAM_ECG_3:
                case FeMessage.TYPE_STREAM_ECG_12:
                case FeMessage.TYPE_STREAM_ECG_2:
                    if (User.getUser().getFeState() == User.FESTATE_REGISTERED) {
//                        L.e("FeMessage.TYPE_STREAM_ECG (success)");
                        //only process data stream after device is registered
                        L.i("<BLE> <RX> received ECG, stamp = " + MessageUtils.bytesToUnsignedShort(msg.getBody(), 0) +
                                " type = " + Integer.toString(msg.getType()));

                        User.getUser().resetLastFeMessageTime();
                        if (!User.getUser().isTimeInit()) {
                            //我们只在每一次建立连接后接收到第一个ecg数据的时候才进行时间标识，之后直到连接中断都不再获取系统时间
                            //这里很关键，第一数据接收设定起始，之后按照设备的sps和数据单元大小计算后续ecg数据流的时间，可以避免时间与数据不匹配
                            User.getUser().setIsTimeInit(true);
                            try {
//                                L.i("<BLE> init ECG after the connection");
                                EcgQueue.getRaw().put(msg.extractInitEcg(User.getUser(),
                                        System.currentTimeMillis())); //每次建立连接之后的第一个ECG数据
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
//                                L.i("<BLE> normal ECG");
                                EcgQueue.getRaw().put(msg.extractEcg(User.getUser()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        L.e("FeMessage.TYPE_STREAM_ECG (failed)");
                        BleFeComm.getClient().reconnect();// if stream data send prior to register, close the connection so the FE will reset itself
                    }

                    break;
                case FeMessage.TYPE_STATUS:
                    if (User.getUser().getFeState() == User.FESTATE_REGISTERED) {
                        L.e("FeMessage.TYPE_STATUS (success)");
                        User.getUser().resetLastFeMessageTime();
                        long timeNow = System.currentTimeMillis();
//                        String str = "";
//                        for (int m = 0; m < msg.getBody().length; m ++) {
//                            str += " " + Integer.toHexString( msg.getBody()[m] & 0x00ff);
//                        }
//                        L.i("<BLE> <RX> received mark, body = " + str);
                        EcgMark mark = msg.extractMark(timeNow);
                        L.i("<BLE> <RX> received mark, type = " + Integer.toString(mark.getType()));
                        EcgMarkQueue.put(msg.extractMark(timeNow));
                    } else {
                        L.e("FeMessage.TYPE_STATUS (failed)");
                        BleFeComm.getClient().reconnect();// if status data send prior to register, close the connection so the FE will reset itself
                    }
                    break;
                case FeMessage.TYPE_REGISTER:
                    User.getUser().resetLastFeMessageTime();
                    User.getUser().setIsDevReset(true); //device is reset

                    UserDevice newDev = msg.extractDevice();
                    BleFeTxCoder.getCoder().setStreamLength(newDev.getStreamLen());

                    //Set resolution
                    if (newDev.getModel() == UserDevice.MODEL_20_1 || newDev.getModel() == UserDevice.MODEL_20_3) {
                        BleFeTxCoder.getCoder().setResolution(2);
                        EcgDataPostProcessThread.setResolution(2);
                    } else if (newDev.getModel() == UserDevice.MODEL_20_3_HI) {
                        BleFeTxCoder.getCoder().setResolution(3);
                        EcgDataPostProcessThread.setResolution(3);
                    } else if (newDev.getModel() == UserDevice.MODEL_20_2_HI) {
                        BleFeTxCoder.getCoder().setResolution(3);
                        EcgDataPostProcessThread.setResolution(3);
                    }

                    UserDevice oldDev = User.getUser().getUserDevices().get(newDev.getMacAddr());
                    if (oldDev != null) {
                        User.getUser().updateUserDevice(oldDev);
                        L.e("FeMessage.TYPE_REGISTER -> oldDev: " + oldDev.toString());
                        User.getUser().save(UserSaveType.Device);

                        AppNetTxQueue.put(AppMessage.createRegMessage(oldDev));

                        if (true && !User.getUser().isConnectedDeviceAppNet()) {// TODO: 允许离线方案
                            User.getUser().setFeState(User.FESTATE_REGISTERED);
                            BleFeTxQueue.put(FeMessage.createRegAckMsg(User.getUser().getUserDevice()));
                        }
                    } else {// 如果是新设备，先获取key，然后在获取到key之后返回给fe
                        User.getUser().updateUserDevice(newDev);
                        L.e("FeMessage.TYPE_REGISTER -> newDev: " + newDev.toString());

                        AppNetTxQueue.put(AppMessage.createActivateMessage(newDev));

                        if (true && !User.getUser().isConnectedDeviceAppNet()) {// TODO: 允许离线方案
                            User.getUser().setFeState(User.FESTATE_REGISTERED);
                            BleFeTxQueue.put(FeMessage.createRegAckMsg(User.getUser().getUserDevice()));
                        }
                    }
                    // TODO: >>>>>>>>>>>>不然会一直TYPE_REGISTER
                    if (ListenerMgr.getUserStateChangedListener() != null) {
                        ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.FESTATE_REGISTERED);
                    }
                    // TODO: <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    break;
                case FeMessage.TYPE_USERINPUT:
                    if (User.getUser().getFeState() == User.FESTATE_REGISTERED) {
                        L.e("FeMessage.TYPE_USERINPUT (success)");
                        User.getUser().resetLastFeMessageTime();
                        EcgMarkQueue.put(msg.extractMark(System.currentTimeMillis()));
                    } else {
                        L.e("FeMessage.TYPE_USERINPUT (failed)");
                        BleFeComm.getClient().reconnect();// if status data send prior to register, close the connection so the FE will reset itself
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * app消息处理
     *
     * @param msg: AppMessage
     */
    private void handleAppMessage(AppMessage msg) {
//        L.e("AppMessage.handleAppMessage -> Type: " + msg.getType());
        switch (msg.getType()) {
            case AppMessage.TYPE_ACTIVATE_ACK:
                UserDevice dev = msg.extractDevice();
                if (dev == null) {
                    break;
                }
                byte[] key = dev.getKey();
                if (key != null && key.length > 15 &&
                        key[0] == 0 && key[1] == 0 && key[14] == 0 && key[15] == 0) {
                    L.e("AppMessage.TYPE_ACTIVATE_ACK (failed) -> dev: " + dev.toString());
                    BleFeComm.getClient().reconnect();// if reg failed, break the link (so FE will reset itself)
                } else {
                    L.e("AppMessage.TYPE_ACTIVATE_ACK (success) -> dev: " + dev.toString());
                    User.getUser().setConnectedDeviceAppNet(true);
//                    ActivityStack.toast(R.string.online);

                    User.getUser().updatePrevUserDevice(new UserDevice(dev));

                    if (User.getUser().hasUserDevice()) {
                        User.getUser().setFeState(User.FESTATE_REGISTERED);
                        BleFeTxQueue.put(FeMessage.createRegAckMsg(User.getUser().getUserDevice())); //send back regAck to FE
                        if (ListenerMgr.getUserStateChangedListener() != null) {
                            ListenerMgr.getUserStateChangedListener().onFeStateChanged(User.FESTATE_REGISTERED);
                        }
                    }
                    if (ListenerMgr.getUserStateChangedListener() != null) {
                        ListenerMgr.getUserStateChangedListener().onDeviceActivated(User.getUser().getUserDevice(),
                                AppMessage.ACK_OK);
                    }
                }
                break;
            case AppMessage.TYPE_REG_ACK:
                if (msg.getBody() == null || msg.getBody().length <= 8) {
                    break;
                }
                byte result = msg.getBody()[8];
                if (result == AppMessage.ACK_OK) {
                    L.e("AppMessage.TYPE_REG_ACK (success)");
                    User.getUser().setConnectedDeviceAppNet(true);
//                    ActivityStack.toast(R.string.online);

                    if (User.getUser().hasUserDevice()) {
                        if (User.getUser().getFeState() == User.FESTATE_CONNECTED) {
                            User.getUser().setFeState(User.FESTATE_REGISTERED);

                            User.getUser().setTimeLastRegAck(System.currentTimeMillis());
                            BleFeTxQueue.put(FeMessage.createRegAckMsg(User.getUser().getUserDevice()));
                        }
                        if (ListenerMgr.getUserStateChangedListener() != null) {
                            ListenerMgr.getUserStateChangedListener().onDeviceRegistered(User.getUser().getUserDevice(),
                                    AppMessage.ACK_OK);
                        }
                    }
                } else {
                    L.e("AppMessage.TYPE_REG_ACK (failed)");
                    BleFeComm.getClient().reconnect();//if reg failed, break the link (so FE will reset itself)
                }
                break;
            case AppMessage.TYPE_LOGIN_ACK:
                byte[] body = msg.getBody();
                if (body != null && body.length > 8 && body[8] == AppMessage.ACK_OK) {
                    L.e("AppMessage.TYPE_LOGIN_ACK (success)");
                    User.getUser().setAppState(User.APPSTATE_LOGIN);

                    if (User.getUser().hasUserDevice()) {
                        // if device is connected prior to login
                        AppNetTxQueue.put(AppMessage.createRegMessage(User.getUser().getUserDevice()));
                    }
                    /*
                     * login callback
                     */
                    if (ListenerMgr.getUserStateChangedListener() != null) {
                        ListenerMgr.getUserStateChangedListener().onAppStateChanged(User.APPSTATE_LOGIN);
                        ListenerMgr.getUserStateChangedListener().onLogin(0);
                    }
                } else {
                    L.e("AppMessage.TYPE_LOGIN_ACK (failed)");
                }
                break;
            case AppMessage.TYPE_MARK:
                // 处理mark
                EcgMark mark = msg.extractMark();
                if (mark != null && mark.getTypeGroup() == EcgMark.TYPE_GROUP_PHYSIO) {
                    BleFeTxQueue.put(FeMessage.createMarkMsg(mark));
                }
                break;
            case AppMessage.TYPE_PULSE:// 心跳包直接回传
                L.e("AppMessage.TYPE_PULSE (success)");
//                AppNetTxThread.sendPulse();
                break;
            default:
                return;
        }
        User.getUser().resetLastAppNetMessageTime();
    }

}
