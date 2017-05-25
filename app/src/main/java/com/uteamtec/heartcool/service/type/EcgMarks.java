package com.uteamtec.heartcool.service.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by wd
 */
public final class EcgMarks {

    private String message;

    private List<EcgMark> marks = new ArrayList<>();

    public EcgMarks(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void add(EcgMark mark) {
        if (mark != null) {
            mark.setMessage(getMessage());
            marks.add(mark);
        }
    }

    public List<EcgMark> getMarks() {
        return marks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EcgMarks: \n");
        sb.append(String.format(Locale.getDefault(), "message: %s\n", getMessage()));
        Iterator<EcgMark> i = marks.iterator();
        while (i.hasNext()) {
            EcgMark m = i.next();
            sb.append("[");
            sb.append(String.format(Locale.getDefault(), "deviceId: %s, ", new String(m.getDeviceId())));
            sb.append(String.format(Locale.getDefault(), "type: %s, ", m.getType()));
            sb.append(String.format(Locale.getDefault(), "typeGroup: %d, ", m.getTypeGroup()));
            sb.append(String.format(Locale.getDefault(), "startTime: %d, ", m.getStartTime()));
            sb.append(String.format(Locale.getDefault(), "stopTime: %d, ", m.getStopTime()));
            sb.append(String.format(Locale.getDefault(), "value: %d, ", m.getValue()));
            sb.append(String.format(Locale.getDefault(), "message: %s, ", m.getMessage()));
            sb.append(String.format(Locale.getDefault(), "messageType: %s", m.getMessageType()));
            sb.append("]\n");
        }
        return sb.toString();
    }
}
