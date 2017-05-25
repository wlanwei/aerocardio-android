package com.uteamtec.heartcool.messages;


import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.UserDevice;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Message for socket communication
 * Packet format:
 * HEADER (4 bytes) TYPE (2 bytes) LENGTH (2 bytes) DATA (LENGTH bytes)
 * <p>
 * Message format:
 * 1. LOGIN: USER (8 bytes) + REFCODE (32 bytes)
 * 2. LOGIN_ACK: RESULT = OK (00) NO (FF) (1 byte)
 * 3. REG: ID_DEV (8 bytes) + DEV_TYPE (2 bytes) + SPS (2 bytes)
 * 4. REG_ACK: RESULT = ID_DEV (8 bytes) + DEV_TYPE (2 bytes) + SPS (2 bytes) + OK (00) NO (FF) (1 byte)
 * 5. ACTIVATE: ID_DEV (8 bytes) + DEV_TYPE (2 bytes) + SPS (2 bytes) + KEY (16 bytes)
 * 6. ACTIVATE_ACK: RESULT = ID_DEV (8 bytes) + DEV_TYPE (2 bytes) + SPS (2 bytes) + OK (00) NO (FF) (1 byte) + CODE (16 bytes)
 * 7. STREAM: START_TIME (8 bytes) + STOP_TIME (8 bytes) + CHANNEL NUM (1 byte) + RESOLUTION (1 byte) + SPS (2 bytes) + DATA
 * 8. MARK: START_TIME (4 bytes) + STOP_TIME (4 bytes) + TYPE_GROUP ( 2 bytes) + TYPE (2 bytes) + VALUES (LENGTH - 12)
 *
 * @author liulingfeng
 */

public class AppMessage implements Serializable {
    public static final int TYPE_LOGIN = 1; //user login msg
    public static final int TYPE_LOGIN_ACK = 2; //user login msg ack
    public static final int TYPE_REG = 3; //register authorized device
    public static final int TYPE_REG_ACK = 4; //register authorized device ack
    public static final int TYPE_ACTIVATE = 5; //authorize and bind new device
    public static final int TYPE_ACTIVATE_ACK = 6; //authorize and bind new device ack
    public static final int TYPE_STREAM = 7; //send stream data
    public static final int TYPE_MARK = 8; //send to & back mark data
    public static final int TYPE_PULSE = 30;

    //header
    public static final byte HEADER[] = {(byte) 0x00, (byte) 0xAA, (byte) 0x00, (byte) 0xCC};

    //constant message
    public static final byte ACK_OK = (byte) 0x00;
    public static final byte ACK_NO = (byte) 0x00ff;

    //message structure
    private int type;
    private byte body[];

    public AppMessage(int type, byte body[]) {
        this.type = type;
        this.body = body;
    }


    public AppMessage decode(ByteBuffer buff) {
        return null;
    }

    public int getType() {
        return type;
    }

    public byte[] getBody() {
        return body;
    }


    public Ecg extractEcg() {
        if (type == TYPE_STREAM) {
            long startTime = MessageUtils.bytesToLong(body, 0);
            long stopTime = MessageUtils.bytesToLong(body, 8);
            int ecgType = (int) (body[16] & 0x00ff);
            int resolution = (int) (body[17] & 0x00ff);
            int sps = MessageUtils.bytesToShort(body, 18);
            int data[] = new int[(body.length - 20) / resolution];
            for (int m = 0; m < data.length; m++) {
                if (resolution == 2) {
                    data[m] = MessageUtils.bytesToShort(body, 20 + resolution * m);
                } else if (resolution == 3) {
                    data[m] = MessageUtils.bytesToDemiInt(body, 20 + resolution * m);
                }
            }
//			L.i("ecg type = " + Integer.toString(ecgType) + " data length = " + Integer.toString(data.length) + " sps = " + Integer.toString(sps));
            if (stopTime < startTime) {
                return new Ecg(ecgType, stopTime, startTime, sps, data);
            }
            return new Ecg(ecgType, startTime, stopTime, sps, data);
        } else {
            return null;
        }
    }

    public EcgMark extractMark() {
        if (type == TYPE_MARK) {
            long startTime = MessageUtils.bytesToLong(body, 0);
            long stopTime = MessageUtils.bytesToLong(body, 8);
            int typeGroup = MessageUtils.bytesToShort(body, 16);
            int type = MessageUtils.bytesToShort(body, 18);
            int val = MessageUtils.bytesToShort(body, 20);
            if (stopTime < startTime) {
                return new EcgMark(stopTime, startTime, typeGroup, type, val);
            }
            return new EcgMark(startTime, stopTime, typeGroup, type, val);
        } else {
            return null;
        }
    }

    /**
     * extract key from activate message send back by server
     *
     * @return
     */
    public UserDevice extractDevice() {
        if (type == TYPE_REG || type == TYPE_ACTIVATE) {
            byte id[] = new byte[8];
            System.arraycopy(body, 0, id, 0, 8);
            int model = MessageUtils.bytesToShort(body, 8);
            int streamlen = body[10] & 0xff;
            int sps = MessageUtils.bytesToShort(body, 11);
            byte key[] = new byte[16];
            System.arraycopy(body, 16, key, 0, 16);
            UserDevice dev = new UserDevice(new String(id), model);
            dev.setKey(key);
            dev.setSps(sps);
            dev.setStreamLen(streamlen);
            return dev;
        } else {
            return null;
        }
    }

    public static byte[] toBytes(AppMessage msg) {
        byte packet[] = new byte[8 + msg.getBody().length];
        byte msgBody[] = msg.getBody();
        int bodyLength = msg.getBody().length + 2;

        System.arraycopy(AppMessage.HEADER, 0, packet, 0, 4);
        System.arraycopy(MessageUtils.shortToBytes(msg.getType()), 0, packet, 4, 2);
        System.arraycopy(MessageUtils.shortToBytes(msgBody.length), 0, packet, 6, 2);
        System.arraycopy(msgBody, 0, packet, 8, msgBody.length);

        return packet;
    }

    public static AppMessage createLoginMessage(byte[] id, byte[] key) {
//        L.e("AppMessage.createLoginMessage");
        if (id == null || id.length < 8 || key == null || key.length < 8) {
            return null;
        }
        byte body[] = new byte[24];
        System.arraycopy(id, 0, body, 0, 8);
        System.arraycopy(key, 0, body, 8, 16);
        return new AppMessage(AppMessage.TYPE_LOGIN, body);
    }

    public static AppMessage createRegMessage(UserDevice dev) {
//        L.e("AppMessage.createRegMessage");
        byte body[] = new byte[16];
        System.arraycopy(dev.getId(), 0, body, 0, 8);

        System.arraycopy(MessageUtils.shortToBytes(dev.getModel()), 0, body, 8, 2);
        body[10] = (byte) (dev.getStreamLen() & 0x00ff);
        System.arraycopy(MessageUtils.shortToBytes(dev.getSps()), 0, body, 11, 2);

        return new AppMessage(AppMessage.TYPE_REG, body);
    }

    public static AppMessage createActivateMessage(UserDevice dev) {
//        L.e("AppMessage.createActivateMessage");
        byte body[] = new byte[32];
        System.arraycopy(dev.getId(), 0, body, 0, 8);

        System.arraycopy(MessageUtils.shortToBytes(dev.getModel()), 0, body, 8, 2);
        body[10] = (byte) (dev.getStreamLen() & 0x00ff);
        System.arraycopy(MessageUtils.shortToBytes(dev.getSps()), 0, body, 11, 2);

        System.arraycopy(dev.getKey(), 0, body, 16, 16);

        return new AppMessage(AppMessage.TYPE_ACTIVATE, body);
    }


    public static AppMessage createEcgMessage(Ecg ecg, int resolution) {
//        L.e("AppMessage.createEcgMessage");
        long startTime = ecg.getStartTime();
        long stopTime = ecg.getStopTime();
        int sps = ecg.getSps();
        int ecgType = ecg.getType();
        int data[] = ecg.getData();
        byte body[] = new byte[data.length * resolution + 20];
        int offset = 20;

//		L.i("<DATA> ecg to bytes, sps = " + Integer.toString(ecg.getSps()) + " resolution = " + Integer.toString(resolution));
        System.arraycopy(MessageUtils.longToBytes(startTime), 0, body, 0, 8);
        System.arraycopy(MessageUtils.longToBytes(stopTime), 0, body, 8, 8);
        body[16] = (byte) (ecgType & 0x00ff);
        body[17] = (byte) (resolution & 0x00ff);
        System.arraycopy(MessageUtils.shortToBytes(sps), 0, body, 18, 2);

        for (int m = 0; m < data.length; m++) {
            if (resolution == 2) {
                System.arraycopy(MessageUtils.shortToBytes(data[m]), 0, body, offset + resolution * m, resolution);
            } else if (resolution == 3) {
                System.arraycopy(MessageUtils.demiIntToBytes(data[m]), 0, body, offset + resolution * m, resolution);
            } else {
                return null;
            }
        }

        return new AppMessage(TYPE_STREAM, body);
    }

    public static AppMessage createMarkMessage(EcgMark mark) {
//        L.e("AppMessage.createMarkMessage");
        byte body[] = new byte[28];
        System.arraycopy(MessageUtils.longToBytes(mark.getStartTime()), 0, body, 0, 8);
        System.arraycopy(MessageUtils.longToBytes(mark.getStopTime()), 0, body, 8, 8);
        System.arraycopy(MessageUtils.shortToBytes(mark.getTypeGroup()), 0, body, 16, 2);
        System.arraycopy(MessageUtils.shortToBytes(mark.getType()), 0, body, 18, 2);
        System.arraycopy(MessageUtils.shortToBytes(mark.getValue()), 0, body, 20, 2);
        return new AppMessage(AppMessage.TYPE_MARK, body);
    }

    public static AppMessage createPulseMessage() {
//        L.e("AppMessage.createPulseMessage");
        return new AppMessage(AppMessage.TYPE_PULSE, null);
    }
}
