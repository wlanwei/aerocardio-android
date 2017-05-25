package com.uteamtec.heartcool.service.type;

import android.text.TextUtils;

import com.uteamtec.algorithm.types.Device;

import java.io.Serializable;

/**
 * Created by wd
 */
public class UserDevice extends Device implements Serializable {

    private Device device = null;

    public UserDevice(String id, int model) {
        super(id.getBytes(), model);
    }

    public UserDevice(String macAddr, String name) {
        super(macAddr, name);
    }

    public UserDevice(Device d) {
        super(d.getId(), d.getModel());
        this.device = d;
        if (d.getKey() != null) {
            setKey(d.getKey());
        }
        if (d.getKeyPair() != null) {
            setKeyPair(d.getKeyPair());
        }
        setMacAddr(d.getMacAddr());
        setName(d.getName());
        setSps(d.getSps());
        setState(d.getState());
        setStreamLen(d.getStreamLen());
    }

    public Device getDevice() {
        if (this.device == null) {
            this.device = new Device(getId(), getModel());
            this.device.setKey(getKey());
            this.device.setKeyPair(getKeyPair());
            this.device.setMacAddr(getMacAddr());
            this.device.setName(getName());
            this.device.setSps(getSps());
            this.device.setState(getState());
            this.device.setStreamLen(getStreamLen());
        }
        return this.device;
    }

    public void setId(String id) {
        if (!TextUtils.isEmpty(id)) {
            setId(id.getBytes());
        }
    }

    public String getIdStr() {
        if (super.getId() != null) {
            return new String(super.getId());
        }
        return "";
    }

    public void setKey(String k) {
        if (!TextUtils.isEmpty(k)) {
            setKey(k.getBytes());
        }
    }

    public String getKeyStr() {
        if (super.getKey() != null) {
            return new String(super.getKey());
        }
        return "";
    }

    public void setKeyPair(String kp) {
        if (!TextUtils.isEmpty(kp)) {
            setKeyPair(kp.getBytes());
        }
    }

    public String getKeyPairStr() {
        if (super.getKeyPair() != null) {
            return new String(super.getKeyPair());
        }
        return "";
    }

    @Override
    public String toString() {
        return getIdStr() + "|" + getModel() + "|" + getKeyStr() +
                "|" + getKeyPairStr() + "|" + getMacAddr() +
                "|" + getName() + "|" + getSps() + "|" + getStreamLen() + "|" + getState();
    }

    public static UserDevice fromString(String str) {
        String strs[] = str.split("\\|");
        if (strs.length >= 9) {
            UserDevice dev = new UserDevice(strs[0], Integer.valueOf(strs[1]));
            dev.setKey(strs[2]);
            dev.setKeyPair(strs[3]);
            dev.setMacAddr(strs[4]);
            dev.setName(strs[5]);
            dev.setSps(Integer.valueOf(strs[6]));
            dev.setStreamLen(Integer.valueOf(strs[7]));
            dev.setState(Integer.valueOf(strs[8]));
            return dev;
        }
        return null;
    }

    public UserDevice update(UserDevice d) {
        if (d == null) {
            return null;
        }
        if (d.getId() != null) {
            setId(d.getId());
        }
        if (d.getModel() > 0) {
            setModel(d.getModel());
        }
        if (d.getKey() != null) {
            setKey(d.getKey());
        }
        if (d.getKeyPair() != null) {
            setKeyPair(d.getKeyPair());
        }
        if (!TextUtils.isEmpty(d.getMacAddr())) {
            setMacAddr(d.getMacAddr());
        }
        if (!TextUtils.isEmpty(d.getName())) {
            setName(d.getName());
        }
        if (d.getSps() > 0) {
            setSps(d.getSps());
        }
        if (d.getStreamLen() > 0) {
            setStreamLen(d.getStreamLen());
        }
        if (d.getState() > 0) {
            setState(d.getState());
        }
        return this;
    }

}
