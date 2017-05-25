package com.uteamtec.heartcool.comm;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/** 网络通信管理器，用于监视网络状况
 * Created by liulingfeng on 2015/10/30.
 */
public class InetMgr {
    private ConnectivityManager connMgr;
    private CyclicThread cyclicThread;
    private InetStatusListener listener;
    public InetMgr(Context ctx){
       connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        cyclicThread = new CyclicThread();
        cyclicThread.start();
    }

    public void stopWork(){
        cyclicThread.quit();
    }

    public void setInetStatusListener(InetStatusListener listener) {
        this.listener = listener;
    }

    private class CyclicThread extends Thread{
        private boolean enabled = true;
        public void quit(){
            enabled = false;
            this.interrupt();
        }
        @Override
        public void run() {
            NetworkInfo inetInfo;
            while(true){
                if (!enabled) {
                    break;
                }
                try {
                    Thread.sleep(500);
                    inetInfo = connMgr.getActiveNetworkInfo();
                    if (inetInfo == null) {
                        if (listener != null) {
                            listener.onInetDisabled();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface InetStatusListener {
        void onInetDisabled();
    }
}
