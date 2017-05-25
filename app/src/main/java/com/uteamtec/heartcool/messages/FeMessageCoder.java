package com.uteamtec.heartcool.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by liulingfeng on 2015/10/21.
 * Rebuilding by wd
 */
public class FeMessageCoder {

    private static final int DEFAULT_BUFFER_SIZE = 65535;
    private ByteBuffer buff;

    private DecodeThread decodeThread;

    private CodeCallback callback;

    private int streamLength;
    private int resolution;

    public FeMessageCoder() {
        buff = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        decodeThread = new DecodeThread();
        streamLength = -1;
        resolution = -1;
    }

    public void setStreamLength(int length) {
        this.streamLength = length;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
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
    public static byte[] encode(FeMessage msg) {
        if (msg == null) {
            return new byte[5];
        }
        byte packet[];
        if (msg.getBody() != null) {
            packet = new byte[5 + msg.getBody().length];
            byte msgBody[] = msg.getBody();

            System.arraycopy(FeMessage.HEADER, 0, packet, 0, 4);
            packet[4] = (byte) (msg.getType() & 0x00ff);
            System.arraycopy(msgBody, 0, packet, 5, msgBody.length);
        } else {
            packet = new byte[5];
            System.arraycopy(FeMessage.HEADER, 0, packet, 0, 4);
            packet[4] = (byte) (msg.getType() & 0x00ff);
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
        void onDecodeMessage(FeMessage msg);
    }

    private class DecodeThread extends Thread {

        private byte header[] = new byte[4];
        private byte dump[] = new byte[2];
        private byte para[] = new byte[16];
        private int type;
        private int bodyLength;

        private boolean enabled = true;

        private void quit() {
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
                if (getBufferAvailability() >= 6) {
//                    L.i("<FECODER> runs here");
                    readBytes(para, 5);
                    System.arraycopy(para, 0, header, 0, 4);

                    if (Arrays.equals(header, FeMessage.HEADER)) {
//                        L.i("<FECODER> header matched");
                        type = (int) (para[4] & 0x00ff);
                        if (type >= 0) {
                            //proceed valid message
                            bodyLength = FeMessage.getBodyLength(type, streamLength, resolution);

//                            L.i("<FECODER> body length = " + Integer.toString(bodyLength) + " type = " + Integer.toString(type));
                            if (bodyLength < 0) {
                                //invalid body length (for ecg stream)
                                getBytes(para, 4);
                            } else if (bodyLength == 0) {
                                if (callback != null) {
                                    //此处返回解码信息包
//                                    L.i("<FECODER>  send message to callback " );
                                    getBytes(para, 5);
                                    callback.onDecodeMessage(new FeMessage(type, null));
                                }
                            } else {
                                if (getBufferAvailability() >= bodyLength + 5) {
//                                L.i("<FECODER> buffer is sufficient, len = " + Integer.toString(bodyLength));
                                    getBytes(para, 5);
                                    byte body[] = new byte[bodyLength];
                                    getBytes(body, bodyLength);
                                    if (callback != null) {
                                        //此处返回解码信息包
//                                    L.i("<FECODER>  send message to callback " );
                                        callback.onDecodeMessage(new FeMessage(type, body));
                                    } else {
//                                    L.i("<FECODER> callback is not set");
                                    }
                                } else {
                                    //insufficient buffer
//                                L.i("<FECODER> body is insufficient");
                                    continue;
                                }
                            } //if bodylength
                        } else {
                            getBytes(para, 5); // invalid type = invalide message, dump the header and type
                        }
                    } else {
                        getBytes(para, 1); //if header mismatched, dump one byte
                    }
                }
            }  //while true
        } //run
    }
}
