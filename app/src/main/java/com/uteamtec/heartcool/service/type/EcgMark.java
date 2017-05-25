package com.uteamtec.heartcool.service.type;

/**
 * Created by wd
 */
public final class EcgMark extends com.uteamtec.algorithm.types.EcgMark {

    public static final String MSG_ZC = "正常";
    public static final String MSG_TYPE_ZC = "ZC";

    public static final String MSG_XLGK = "心率过快";
    public static final String MSG_TYPE_XLGK = "XLGK";

    public static final String MSG_XLGH = "心率过缓";
    public static final String MSG_TYPE_XLGH = "XLGH";

    public static final String MSG_XLBQ = "心率不齐";
    public static final String MSG_TYPE_XLBQ = "XLBQ";

    public static final String MSG_SXZB = "室性早博";
    public static final String MSG_TYPE_SXZB = "SXZB";

    public static final String MSG_FXZB = "房性早博";
    public static final String MSG_TYPE_FXZB = "FXZB";

    public static final String MSG_SC = "室颤";
    public static final String MSG_TYPE_SC = "SZ";

    public static final String MSG_FC = "房颤";
    public static final String MSG_TYPE_FC = "FZ";

    public static String getMessage(String msg) {
        switch (msg.trim()) {
            case MSG_TYPE_XLGK:
                return MSG_XLGK;
            case MSG_TYPE_XLGH:
                return MSG_XLGH;
            case MSG_TYPE_XLBQ:
                return MSG_XLBQ;
            case MSG_TYPE_SXZB:
                return MSG_SXZB;
            case MSG_TYPE_FXZB:
                return MSG_FXZB;
            case MSG_TYPE_SC:
                return MSG_SC;
            case MSG_TYPE_FC:
                return MSG_FC;
            default:
                return MSG_ZC;
        }
    }

    public static String getMessageType(String type) {
        switch (type.trim()) {
            case MSG_XLGK:
                return MSG_TYPE_XLGK;
            case MSG_XLGH:
                return MSG_TYPE_XLGH;
            case MSG_XLBQ:
                return MSG_TYPE_XLBQ;
            case MSG_SXZB:
                return MSG_TYPE_SXZB;
            case MSG_FXZB:
                return MSG_TYPE_FXZB;
            case MSG_SC:
                return MSG_TYPE_SC;
            case MSG_FC:
                return MSG_TYPE_FC;
            default:
                return MSG_TYPE_ZC;
        }
    }

    private String message = MSG_ZC;

    private String messageType = MSG_TYPE_ZC;

    public EcgMark(long startTime, long stopTime, int typeGroup, int type, int value) {
        super(startTime, stopTime, typeGroup, type, value);
    }

    public static EcgMark toEcgMark(com.uteamtec.algorithm.types.EcgMark mark) {
        if (mark != null) {
            EcgMark m = new EcgMark(mark.getStartTime(), mark.getStopTime(),
                    mark.getTypeGroup(), mark.getType(), mark.getValue());
            m.setDeviceId(mark.getDeviceId());
            return m;
        }
        return null;
    }

    public com.uteamtec.algorithm.types.EcgMark toEcgMark() {
        return this;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType.trim();
        this.message = getMessage(messageType);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message.trim();
        this.messageType = getMessageType(message);
    }

    @Override
    public String toString() {
        return "EcgMark [" +
                "startTime=" + getStartTime() + ", stopTime=" + getStopTime() + ", " +
                "typeGroup=" + getTypeGroup() + ", type=" + getType() + ", value=" + getValue() + ", " +
                "messageType=" + getMessageType() + "] \n";
    }

}
