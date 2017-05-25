package com.uteamtec.heartcool.service.net;

import com.uteamtec.heartcool.messages.AppMessageCoder;

/**
 * 编解码器，通过putbytes扔字节，通过回调函数获取pojo
 * Created by wd
 */
public final class AppNetTxCoder {

    private AppMessageCoder coder;

    private AppNetTxCoder() {
        coder = new AppMessageCoder();
    }

    private static AppNetTxCoder _coder;

    public static AppMessageCoder getCoder() {
        if (_coder == null) {
            synchronized (AppNetTxCoder.class) {
                if (_coder == null) {
                    _coder = new AppNetTxCoder();
                }
            }
        }
        return _coder.coder;
    }

    public static void startDecode() {
        getCoder().flush();
        getCoder().startDecode();
    }

    public static void stopDecode() {
        getCoder().stopDecode();
        getCoder().flush();
    }

}
