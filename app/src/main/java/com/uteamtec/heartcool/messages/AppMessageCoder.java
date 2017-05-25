package com.uteamtec.heartcool.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by liulingfeng on 2015/10/24.
 * Rebuilding by wd
 */
public class AppMessageCoder {

    private static final int DEFAULT_BUFFER_SIZE = 65535;
    private ByteBuffer buff;

    private DecodeThread decodeThread;

    private CodeCallback callback;

    public AppMessageCoder() {
        buff = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        decodeThread = new DecodeThread();
    }

    public void setCodeCallback(CodeCallback callback) {
        this.callback = callback;
    }

    synchronized public void flush() {
        this.buff.clear();
    }

    /**
     * add bytes to the buffer, if buffer is full, remove the oldest one and add
     *
     * @param input
     * @param len
     */
    synchronized public void putBytes(byte[] input, int len) {
        if (input == null || input.length == 0 || len == 0) {
            return;
        }
        len = Math.min(input.length, len);
        if (len > DEFAULT_BUFFER_SIZE) {
            return;
        }
        try {
            buff.put(input, 0, len);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * read, but not take bytes from the buffer
     *
     * @param output
     * @param len
     * @return actual read length
     */
    synchronized private int readBytes(byte output[], int len) {
        if (len <= 0 || output.length < len) {
            return -1;
        }
        final int POI = buff.position();
        buff.flip();
        int cnt = Math.min(buff.remaining(), len);
        if (cnt > 0) {
            try {
                buff.get(output, 0, cnt);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        buff.clear();
        buff.position(POI);
        return cnt;
    }

    /**
     * get bytes from the buffer,
     *
     * @param output
     * @param len
     * @return the actual read length of the buffer, length or the remaining of the buffer
     */
    synchronized private int getBytes(byte output[], int len) {
        if (len <= 0 || output.length < len) {
            return -1;
        }
        buff.flip();
        int cnt = Math.min(buff.remaining(), len);
        if (cnt > 0) {
            try {
                buff.get(output, 0, cnt);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        buff.compact();
        return cnt;
    }

    synchronized private int getBufferAvailability() {
        return buff.capacity() - buff.remaining();
    }

    /**
     * encode msg into byte array
     *
     * @param msg
     * @return
     */
    public static byte[] encode(AppMessage msg) {
        if (msg == null) {
            return new byte[8];
        }
        byte packet[];
        if (msg.getBody() == null) {
            packet = new byte[8];
        } else {
            packet = new byte[8 + msg.getBody().length];
        }

        byte msgBody[] = msg.getBody();

        System.arraycopy(AppMessage.HEADER, 0, packet, 0, 4);
        System.arraycopy(MessageUtils.shortToBytes(msg.getType()), 0, packet, 4, 2);
        if (msgBody != null) {
            System.arraycopy(MessageUtils.shortToBytes(msgBody.length), 0, packet, 6, 2);
            System.arraycopy(msgBody, 0, packet, 8, msgBody.length);
        } else {
            System.arraycopy(MessageUtils.shortToBytes(0), 0, packet, 6, 2);
        }

        return packet;
    }

    public void startDecode() {
        if (this.decodeThread != null) {
            this.decodeThread.quit();
        }
        this.decodeThread = new DecodeThread();
        this.decodeThread.start();
    }

    public void stopDecode() {
        if (this.decodeThread != null) {
            this.decodeThread.quit();
        }
    }

    public interface CodeCallback {
        void onDecodeMessage(AppMessage msg);
    }

    private class DecodeThread extends Thread {

        byte header[] = new byte[4];
        byte dump[] = new byte[2];
        byte para[] = new byte[16];
        int type;
        int bodyLength;

        private boolean enabled = true;

        public void quit() {
            enabled = false;
        }

        @Override
        public void run() {
            while (enabled) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!enabled) {
                    break;
                }
                if (getBufferAvailability() >= 8) {
                    //L.i("<RX> capacity before reading" + getBufferAvailability());
                    readBytes(para, 8);
                    //L.i("<RX> capacity after reading" + getBufferAvailability());
                    String str = "";
                    for (int m = 0; m < 6; m++) {
                        str += " " + Integer.toHexString(para[m]);
                    }
                    System.arraycopy(para, 0, header, 0, 4);

                    if (Arrays.equals(header, AppMessage.HEADER)) {
                        //L.i("<RX> header mached");
                        type = MessageUtils.bytesToShort(para, 4);
//                            L.i("<RX> type = " + type);
                        //L.i("<RX> capacity after reading" + Integer.toString(getBufferAvailability()));
                        if (type > 0) {

                            //proceed valid message
                            bodyLength = MessageUtils.bytesToShort(para, 6);
                            //L.i("<RX> length = " + bodyLength);
                            byte body[];
                            if (bodyLength == 0) {
                                getBytes(para, 8);
                                body = null;
                                if (callback != null) {
                                    //此处返回解码信息包
//                                        L.i("<RX> message decoded, msg type = " +Integer.toString(type));
                                    callback.onDecodeMessage(new AppMessage(type, body));
                                }
                            } else if (getBufferAvailability() >= bodyLength + 8) {
                                getBytes(para, 8);
                                //L.i("<RX> buffer sufficient");
                                body = new byte[bodyLength];
                                getBytes(body, bodyLength);
                                if (callback != null) {
                                    //此处返回解码信息包
//                                        L.i("<RX> message decoded, msg type = " +Integer.toString(type));
                                    callback.onDecodeMessage(new AppMessage(type, body));
                                }
                            } else {
                                //insufficient buffer
                                continue;
                            }
                        } else {
                            getBytes(para, 6); // invalid type = invalide message, dump the header and type
                        }
                    } else {
                        getBytes(para, 1); //if header mismatched, dump one byte
                    }
                }
            }  //while true
        } //run
    }
}
