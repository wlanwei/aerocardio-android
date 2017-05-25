package com.uteamtec.heartcool.messages;


import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;


/**
 * Created by liulingfeng on 2015/10/21.
 */
public class FeMessage {
    public static final byte HEADER[] = {(byte) 0x00, (byte) 0xaa, (byte) 0x00, (byte) 0xcc};

    public static final int TYPE_STREAM_ECG_1 = 0; //16 bit ecg
    public static final int TYPE_STREAM_ECG_3 = 1; //16 bit ecg
    public static final int TYPE_STREAM_ECG_12 = 2; //16 bit ecg
    public static final int TYPE_STREAM_ECG_2 = 3; //16 bit ecg
    public static final int TYPE_STATUS = 100;
    public static final int TYPE_ANALYSIS_PHSIO = 150;
    public static final int TYPE_REGISTER = 101;
    public static final int TYPE_REGISTER_ACK = 230; // wd: 寄存器ACK
    public static final int TYPE_PULSE = 231; // wd: 脉冲

    public static final int TYPE_RESET = 240;

    //TODO: add user input
    public static final int TYPE_USERINPUT = 102;

    public static final int TYPE_NULL = 0xff;
    private int type;
    private byte body[];

    public FeMessage(int type, byte body[]) {
        this.type = type;
        this.body = body;
    }

    /**
     * 监测报文是否有效
     *
     * @return boolean
     */
    public boolean isValid() {
        return true;
    }

    /**
     * 计算默认报文长度，用于coder编解码器使用
     *
     * @param type
     * @param streamLength
     * @return
     */
    public static int getBodyLength(int type, int streamLength, int resolution) {
        switch (type) {
            case TYPE_STREAM_ECG_1:
            case TYPE_STREAM_ECG_3:
            case TYPE_STREAM_ECG_12:
            case TYPE_STREAM_ECG_2:
                if (streamLength < 0 || resolution < 0) {
                    //return invalide body length
                    return -1;
                } else {
                    if (type == TYPE_STREAM_ECG_1) {
                        return 2 + resolution * streamLength * 1;
                    } else if (type == TYPE_STREAM_ECG_3) {
                        return 2 + resolution * streamLength * 3;
                    } else if (type == TYPE_STREAM_ECG_12) {
                        return 2 + resolution * streamLength * 12;
                    } else if (type == TYPE_STREAM_ECG_2) {
                        return 2 + resolution * streamLength * 2;
                    } else {
                        return 2 + resolution * streamLength * 1;
                    }
                }
            case TYPE_STATUS:
                return 2 + 4 + 1;
            case TYPE_ANALYSIS_PHSIO:
                return 2 + 8 + 1;
            case TYPE_REGISTER:
                return 8 + 2 + 6 + 16;
            case TYPE_REGISTER_ACK:
                return 8 + 2 + 6 + 16;
            case TYPE_USERINPUT:
                return 0;
            default:
                return -1;
        }
    }

    public int getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }


    //以下的方法为从报文中解析特定数据的方法
    //FeMessage本身只是报文，不要在里面添加任何不必要的数据类型

    //For ECG stream data

    public Ecg extractInitEcg(User user, long timeInit) {
        int stamp;
        int data[];
        if (type == FeMessage.TYPE_STREAM_ECG_1 || type == FeMessage.TYPE_STREAM_ECG_3 || type == FeMessage.TYPE_STREAM_ECG_12 || type == FeMessage.TYPE_STREAM_ECG_2) {
            stamp = MessageUtils.bytesToUnsignedShort(body, 0);

            if (user.getUserDevice().getModel() == UserDevice.MODEL_20_3_HI) {
                //For 24 bit high resolution
                data = new int[(body.length - 2) / 3];
                for (int m = 0; m < data.length; m++) {
                    data[m] = MessageUtils.bytesToDemiInt(body, 2 + 3 * m);
                }
            } else if (user.getUserDevice().getModel() == UserDevice.MODEL_20_3 || user.getUserDevice().getModel() == UserDevice.MODEL_20_1) {
                //For 16 bit regular resolution
                data = new int[(body.length - 2) / 2];
                for (int m = 0; m < data.length; m++) {
                    data[m] = MessageUtils.bytesToShort(body, 2 + 2 * m);
                }

                /*
                 * debug code
                 */
//                data = new int[(body.length-2)/2];
//                for (int m = 0; m < data.length/3; m +=3) {
//                    int dval = MessageUtils.bytesToShort(body,2+2*m);
//                    data[m] = user.getLastEcgVal()[0] + dval;
//                    dval = MessageUtils.bytesToShort(body,2+2*(m+1));
//                    data[m+1] = user.getLastEcgVal()[1] + dval;
//                    dval = MessageUtils.bytesToShort(body,2+2*(m+2));
//                    data[m+2] = user.getLastEcgVal()[2] + dval;
//
//                    user.setLastEcgVal(data[m], data[m+1], data[m+2]);
//                }
            } else if (user.getUserDevice().getModel() == UserDevice.MODEL_20_2_HI) {
                //For 24 bit regular resolution
                data = new int[(body.length - 2) / 2];
                for (int m = 0; m < data.length / 3; m++) {
                    data[3 * m] = MessageUtils.bytesToDemiInt(body, 2 + 3 * (2 * m));
                    data[3 * m + 1] = MessageUtils.bytesToDemiInt(body, 2 + 3 * (2 * m + 1));
                    data[3 * m + 2] = data[3 * m + 1] - data[3 * m];
                }
            } else {
                return null;
            }

            int ecgType;
            if (type == FeMessage.TYPE_STREAM_ECG_1) {
                ecgType = Ecg.TYPE_SINGLE;
            } else if (type == FeMessage.TYPE_STREAM_ECG_3) {
                ecgType = Ecg.TYPE_THREE;
            } else if (type == FeMessage.TYPE_STREAM_ECG_12) {
                ecgType = Ecg.TYPE_FULL;
            } else if (type == FeMessage.TYPE_STREAM_ECG_2) {
                ecgType = Ecg.TYPE_THREE;
            } else {
                ecgType = Ecg.TYPE_SINGLE;
            }
            Ecg ecg = new Ecg(ecgType, -1, -1, -1, data);

            UserDevice dev = user.getUserDevice();
            int sps = dev.getSps();
            int streamLen = dev.getStreamLen();
            long timediff = streamLen * 1000 / sps;

            user.setTimeStreamPrev(timeInit);
            user.setStampStreamPrev(stamp);

            ecg.setSps(sps);
            ecg.setStartTime(timeInit);
            ecg.setStopTime(timeInit + timediff);

//            L.i("<DATA> sps = " + Integer.toString(sps) + " startT = " + Long.toString(timeInit) + " stopT = " + Long.toString(timeInit + timediff));
            return ecg;
        } else {
            return null;
        }
    }

    public Ecg extractEcg(User user) {
        int stamp;
        int data[];
        if (type == FeMessage.TYPE_STREAM_ECG_1 || type == FeMessage.TYPE_STREAM_ECG_3 || type == FeMessage.TYPE_STREAM_ECG_12 || type == FeMessage.TYPE_STREAM_ECG_2) {
            stamp = MessageUtils.bytesToUnsignedShort(body, 0);

            if (user.getUserDevice().getModel() == UserDevice.MODEL_20_3_HI) {
                //For 24 bit high resolution
                data = new int[(body.length - 2) / 3];
                for (int m = 0; m < data.length; m++) {
                    data[m] = MessageUtils.bytesToDemiInt(body, 2 + 3 * m);
                }
            } else if (user.getUserDevice().getModel() == UserDevice.MODEL_20_3 || user.getUserDevice().getModel() == UserDevice.MODEL_20_1) {
                //For 16 bit regular resolution
                data = new int[(body.length - 2) / 2];
                for (int m = 0; m < data.length; m++) {
                    data[m] = MessageUtils.bytesToShort(body, 2 + 2 * m);
                }

                /*
                 * debug code
                 */
//                data = new int[(body.length-2)/2];
//                for (int m = 0; m < data.length/3; m +=3) {
//                    int dval = MessageUtils.bytesToShort(body,2+2*m);
//                    data[m] = user.getLastEcgVal()[0] + dval;
//                    dval = MessageUtils.bytesToShort(body,2+2*(m+1));
//                    data[m+1] = user.getLastEcgVal()[1] + dval;
//                    dval = MessageUtils.bytesToShort(body,2+2*(m+2));
//                    data[m+2] = user.getLastEcgVal()[2] + dval;
//
//                    L.i("<TEST> dval = " + Integer.toString(dval));
//                    user.setLastEcgVal(data[m], data[m+1], data[m+2]);
//                }
            } else if (user.getUserDevice().getModel() == UserDevice.MODEL_20_2_HI) {
                //For 24 bit regular resolution
                data = new int[(body.length - 2) / 2];
                for (int m = 0; m < data.length / 3; m++) {
                    data[3 * m] = MessageUtils.bytesToDemiInt(body, 2 + 3 * (2 * m));
                    data[3 * m + 1] = MessageUtils.bytesToDemiInt(body, 2 + 3 * (2 * m + 1));
                    data[3 * m + 2] = data[3 * m + 1] - data[3 * m];
                }
            } else {
                return null;
            }

            int ecgType;
            if (type == FeMessage.TYPE_STREAM_ECG_1) {
                ecgType = Ecg.TYPE_SINGLE;
            } else if (type == FeMessage.TYPE_STREAM_ECG_3) {
                ecgType = Ecg.TYPE_THREE;
            } else if (type == FeMessage.TYPE_STREAM_ECG_12) {
                ecgType = Ecg.TYPE_FULL;
            } else if (type == FeMessage.TYPE_STREAM_ECG_2) {
                ecgType = Ecg.TYPE_THREE;
            } else {
                ecgType = Ecg.TYPE_SINGLE;
            }
//            L.i("<FECODER> ecg type = " + Integer.toString(ecgType));
            Ecg ecg = new Ecg(ecgType, -1, -1, -1, data);

            int stampPrev = user.getStampStreamPrev();
            long timePrev = user.getTimeStreamPrev();
            UserDevice dev = user.getUserDevice();
            int sps = dev.getSps();
            int streamLen = dev.getStreamLen();
            long timediff = streamLen * 1000 / sps;

            int stampDiff = stampPrev < stamp ? stamp - stampPrev : 65535 - stampPrev + stamp;

            long timeNow = timePrev + stampDiff * timediff;
            user.setTimeStreamPrev(timeNow);
            user.setStampStreamPrev(stamp);

            ecg.setSps(sps);
            ecg.setStartTime(timeNow);
            ecg.setStopTime(timeNow + timediff);

//            L.i("<DATA> sps = " + Integer.toString(sps) + " startT = " + Long.toString(timeNow) + " stopT = " + Long.toString(timeNow + timediff));
            return ecg;
        } else {
            return null;
        }
    }

    /**
     * 解析来自FE的mark数据 (从外部获取时间）
     */
    public EcgMark extractMark(long startTime) {
        if (type == FeMessage.TYPE_STATUS) {

            int markType = MessageUtils.bytesToShort(body, 0);
            int value = MessageUtils.bytesToInt(body, 2);

//            L.i("<SIGNAL> mark type = " + Integer.toString(markType) + "  value =" + Integer.toHexString(value));
            return new EcgMark(startTime, startTime, EcgMark.TYPE_GROUP_STATUS, markType, value);
        } else if (type == FeMessage.TYPE_USERINPUT) {
            return new EcgMark(startTime, startTime, EcgMark.TYPE_GROUP_PHYSIO, EcgMark.PHYSIO_USERINPUT, 0);
        } else {
            return null;
        }
    }


    /**
     * 从REGISTER信息中解析出设备信息
     *
     * @return UserDevice
     */
    public UserDevice extractDevice() {
        if (type == FeMessage.TYPE_REGISTER) {
            byte id[] = new byte[8];
            int model = 0;
            int streamLen = 0;
            int sps = 0;
            byte key[] = new byte[16];

            System.arraycopy(body, 0, id, 0, 8);
            model = MessageUtils.bytesToShort(body, 8);
//            L.i("<TEST> device model = " + Integer.toString((int) (body[8] & 0x00ff)) + " " + Integer.toString((int) (body[9] & 0x00ff)));
            streamLen = body[10] & 0x00ff;

            sps = MessageUtils.bytesToShort(body, 11);
//            L.i("<TEST> device sps = " + Integer.toString(sps));

            System.arraycopy(body, 16, key, 0, 16);

//            L.i("<TEST> device key = " + new String(body));

            UserDevice dev = new UserDevice(new String(id), model);

            dev.setSps(sps);
            dev.setStreamLen(streamLen);
            dev.setKey(key);
            return dev;
        } else {
            return null;
        }
    }


    public static byte[] toBytes(FeMessage msg) {
        byte packet[] = new byte[8 + msg.getBody().length];
        byte msgBody[] = msg.getBody();

        System.arraycopy(FeMessage.HEADER, 0, packet, 0, 4);
        System.arraycopy(MessageUtils.shortToBytes(msg.getType()), 0, packet, 4, 2);
        System.arraycopy(MessageUtils.shortToBytes(msgBody.length), 0, packet, 6, 2);

        System.arraycopy(msgBody, 0, packet, 8, msgBody.length);

        return packet;
    }


    /**
     * 创建认证反馈消息
     *
     * @return
     */
    public static FeMessage createRegAckMsg(UserDevice dev) {
//        L.e("FeMessage.createRegAckMsg");
        byte body[] = new byte[FeMessage.getBodyLength(FeMessage.TYPE_REGISTER_ACK, 0, 0)];
        System.arraycopy(dev.getId(), 0, body, 0, 8);
        /*test code */
        body[0] = (byte) 0x00ff;
        body[1] = (byte) 0x00ff;
        System.arraycopy(MessageUtils.shortToBytes(dev.getModel()), 0, body, 8, 2);
        body[10] = (byte) (dev.getStreamLen() & 0x00ff);
        System.arraycopy(MessageUtils.shortToBytes(dev.getSps()), 0, body, 11, 2);

        /*
         *send back the key pair
         */
        /*
         * test code
         */
        dev.setKeyPair("0000000000000000".getBytes());

        System.arraycopy(dev.getKeyPair(), 0, body, 16, 16);
        return new FeMessage(FeMessage.TYPE_REGISTER_ACK, body);
    }

    public static FeMessage createPulseMsg() {
//        L.e("FeMessage.createPulseMsg");
        return new FeMessage(FeMessage.TYPE_PULSE, null);
    }

    public static FeMessage createResetMsg() {
//        L.e("FeMessage.createResetMsg");
        return new FeMessage(FeMessage.TYPE_RESET, null);
    }

    /**
     * 创建标记信息
     *
     * @return
     */
    public static FeMessage createMarkMsg(EcgMark mark) {
        if (mark.getTypeGroup() == EcgMark.TYPE_GROUP_STATUS) {
            byte body[] = new byte[FeMessage.getBodyLength(FeMessage.TYPE_STATUS, 0, 0)];
            body[0] = 0;
            body[1] = 0;

            System.arraycopy(MessageUtils.shortToBytes(mark.getType()), 0, body, 2, 2);
            System.arraycopy(MessageUtils.shortToBytes(mark.getValue()), 0, body, 4, 2);

            return new FeMessage(FeMessage.TYPE_STATUS, body);
        } else if (mark.getTypeGroup() == EcgMark.TYPE_GROUP_PHYSIO) {
            byte body[] = new byte[FeMessage.getBodyLength(FeMessage.TYPE_ANALYSIS_PHSIO, 0, 0)];

            if (mark.getType() == EcgMark.PHYSIO_HR) {
                System.arraycopy(MessageUtils.shortToBytes(1), 0, body, 0, 2);
            } else if (mark.getType() == EcgMark.PHYSIO_BR) {
                System.arraycopy(MessageUtils.shortToBytes(2), 0, body, 0, 2);
            } else {
                System.arraycopy(MessageUtils.shortToBytes(mark.getType()), 0, body, 0, 2);
            }

            System.arraycopy(MessageUtils.shortToBytes(mark.getValue()), 0, body, 2, 2);

            return new FeMessage(FeMessage.TYPE_ANALYSIS_PHSIO, body);
        } else {
            return null;
        }
    }

    /*
     * Ecg
     */
    public static FeMessage createEcg(Ecg ecg, int idx) {
        byte body[] = new byte[2 + 50];
        System.arraycopy(MessageUtils.shortToBytes(idx), 0, body, 0, 2);
        int data[] = ecg.getData();
        for (int m = 0; m < data.length; m++) {
            System.arraycopy(MessageUtils.shortToBytes(data[m]), 0, body, 2 + 2 * m, 2);
        }
        return new FeMessage(TYPE_STREAM_ECG_1, body);
    }

    public static FeMessage createNull() {
        byte body[] = new byte[1];
        body[0] = (byte) 0x00ff;
        return new FeMessage(TYPE_NULL, body);
    }

}
