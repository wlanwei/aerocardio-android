package com.uteamtec.heartcool.service.ble;

import com.uteamtec.heartcool.messages.FeMessageCoder;

/**
 * 编解码器，通过putbytes扔字节，通过回调函数获取pojo
 * Created by wd
 */
public final class BleFeTxCoder {

    private FeMessageCoder coder;

    private BleFeTxCoder() {
        coder = new FeMessageCoder();
    }

    private static BleFeTxCoder _coder;

    public static FeMessageCoder getCoder() {
        if (_coder == null) {
            synchronized (BleFeTxCoder.class) {
                if (_coder == null) {
                    _coder = new BleFeTxCoder();
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
