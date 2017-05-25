package com.uteamtec.heartcool.service.net;

import com.uteamtec.heartcool.messages.AppMessage;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.utils.L;

/**
 * 网络业务主线程
 * Created by wd
 */
public final class AppNetThread extends Thread {

    private static AppNetThread singleton;

    public static void startThread() {
        if (singleton == null) {
            synchronized (AppNetThread.class) {
                if (singleton == null) {
                    singleton = new AppNetThread();
                    singleton.start();
                }
            }
        }
    }

    public static void stopThread() {
        if (singleton != null) {
            synchronized (AppNetThread.class) {
                if (singleton != null) {
                    singleton.close();
                    singleton = null;
                }
            }
        }
    }

    private boolean _enabled = true;

    private AppNetThread() {
        _enabled = true;
    }

    private void close() {
        this._enabled = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (_enabled) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!_enabled) {
                break;
            }
            // 如果user id与key未获取到
            if (User.getUser().getId() == null || User.getUser().getKey() == null) {
                continue;
            }
            // server timeout
            if (User.getUser().getAppState() == User.APPSTATE_CONNECTED ||
                    User.getUser().getAppState() == User.APPSTATE_LOGIN) {
                long timeDiff = System.currentTimeMillis() - User.getUser().getTimeLastAppMessage();
                if (timeDiff > User.DEFAULT_INT_APPMSG) {
                    L.e("AppNetThread.ServerTimeout: " + timeDiff);
                    AppNetTcpComm.disconnect(true);
                    continue;
                }
            }
            // reconnecting server
            switch (User.getUser().getAppState()) {
                case User.APPSTATE_CONNECTED:
                    // 如果没有登陆，则发送一次登陆请求
                    if (System.currentTimeMillis() - User.getUser().getTimeLastLogin() > User.DEFAULT_INT_LOGIN) {
//                        L.e("<APP> send login");
                        User.getUser().setTimeLastLogin(System.currentTimeMillis());
                        AppNetTxQueue.put(AppMessage.createLoginMessage(User.getUser().getId(),
                                User.getUser().getKey()));
                    }
                    break;
                case User.APPSTATE_DISCONNECTED:
                    // 如果没有连接，则尝试建立一次连接
                    if (System.currentTimeMillis() - User.getUser().getTimeLastConnectServer() > User.DEFAULT_INT_CONNECT_SERVER) {
//                        L.i("<APP> connect server");
                        User.getUser().setTimeLastConnectServer(System.currentTimeMillis());
                        AppNetTcpComm.connect(true);
                    }
                    break;
                case User.APPSTATE_CONNECTING:
//                        L.i("<APP> still connecting");
                    break;
                default:
                    break;
            }
        }
    }
}
