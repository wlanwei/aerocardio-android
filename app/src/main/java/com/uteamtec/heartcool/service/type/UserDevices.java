package com.uteamtec.heartcool.service.type;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wd
 */
public class UserDevices {

    private Map<String, UserDevice> devices = new HashMap<>();

    public UserDevices() {
    }

    public Map<String, UserDevice> getDevices() {
        return devices;
    }

    public void add(UserDevice d) {
        if (d != null && !TextUtils.isEmpty(d.getMacAddr())) {
            devices.put(d.getMacAddr(), d);
        }
    }

    public UserDevice get(String macAddr) {
        return devices.get(macAddr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UserDevices: \n");
        Iterator<String> i = devices.keySet().iterator();
        while (i.hasNext()) {
            sb.append("[");
            sb.append(devices.get(i.next()).toString());
            sb.append("]\n");
        }
        return sb.toString();
    }

    public Set<String> toStringSet() {
        Set<String> setStr = new HashSet<>();
        if (devices != null) {
            Iterator<String> i = devices.keySet().iterator();
            while (i.hasNext()) {
                setStr.add(devices.get(i.next()).toString());
            }
        }
        return setStr;
    }

    public List<UserDevice> getListDevices() {
        List<UserDevice> list = new ArrayList<>();
        Iterator<String> i = devices.keySet().iterator();
        while (i.hasNext()) {
            list.add(devices.get(i.next()));
        }
        return list;
    }

}
