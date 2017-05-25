package com.uteamtec.heartcool.service.type;

import android.text.TextUtils;

import org.wd.blekit.BleUtils;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by wd
 */
public final class User extends com.uteamtec.algorithm.types.User implements Serializable {

    private User() {
    }

    private static User _user;

    public static User getUser() {
        if (_user == null) {
            synchronized (User.class) {
                if (_user == null) {
                    _user = new User();
                }
            }
        }
        return _user;
    }

    public String getIdString() {
        if (getId() != null) {
            return new String(getId());
        }
        return "";
    }

    public void setId(String id) {
        setId(id.getBytes());
    }

    public String getKeyString() {
        if (getKey() != null) {
            return new String(getKey());
        }
        return "";
    }

    public void setKey(String key) {
        if (TextUtils.isEmpty(key)) {
            key = "0000000000000000";
        }
        setKey(key.getBytes());
    }

    private String username = null;

    public String getUsername() {
        if (username == null) {
            username = Config.getString(Config.User,
                    Config.PREF_USER_NAME, "");
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String password = null;

    public String getPassword() {
        if (password == null) {
            password = Config.getString(Config.User,
                    Config.PREF_USER_PASSWORD, "");
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 保存用户，设备信息
     */
    public void save(UserSaveType type) {
        switch (type) {
            case None:
                break;
            case Login:
                Config.putString(Config.User, Config.PREF_USER_NAME, getUsername());
                Config.putString(Config.User, Config.PREF_USER_PASSWORD, getPassword());
                break;
            case IdAndKey:// store user id and key
                if (getId() != null) {
                    Config.putString(Config.User, Config.PREF_USER_ID, new String(getId()));
                } else {
                    Config.putString(Config.User, Config.PREF_USER_ID, null);
                }
                if (getKey() != null) {
                    Config.putString(Config.User, Config.PREF_USER_KEY, new String(getKey()));
                } else {
                    Config.putString(Config.User, Config.PREF_USER_KEY, null);
                }
                break;
            case Device:// store connected device as prev connected device
                UserDevice dev = getUserDevice();
                if (dev != null) {
                    Config.putString(Config.Device, Config.PREF_PREV_DEVICE, dev.toString());
                } else {
                    Config.putString(Config.Device, Config.PREF_PREV_DEVICE, null);
                }
                break;
            case BoundedDevice: // store boundedDevice
                UserDevices devices = getUserDevices();
                if (devices != null) {
                    Config.putStringSet(Config.Device, Config.PREF_BOUNDED_DEVICES, devices.toStringSet());
                } else {
                    Config.putStringSet(Config.Device, Config.PREF_BOUNDED_DEVICES, null);
                }
                break;
        }
    }

    public void clear() {
        Config.putString(Config.User, Config.PREF_USER_NAME, null);
        Config.putString(Config.User, Config.PREF_USER_PASSWORD, null);

        Config.putString(Config.User, Config.PREF_USER_ID, null);
        Config.putString(Config.User, Config.PREF_USER_KEY, null);

        Config.putString(Config.Device, Config.PREF_PREV_DEVICE, null);

        Config.putStringSet(Config.Device, Config.PREF_BOUNDED_DEVICES, null);
    }

    @Override
    public byte[] getId() {
        byte[] id = super.getId();
        if (id == null) {
            String idStr = Config.getString(Config.User,
                    Config.PREF_USER_ID, null);
            if (idStr != null) {
                id = idStr.getBytes();
            }
        }
        return id;
    }

    @Override
    public byte[] getKey() {
        byte[] key = super.getKey();
        if (key == null) {
            String keyStr = Config.getString(Config.User,
                    Config.PREF_USER_KEY, null);
            if (keyStr != null) {
                key = keyStr.getBytes();
            }
        }
        return key;
    }

    private UserDevice prevUserDevice = null;

    public UserDevice getPrevUserDevice() {
        if (prevUserDevice == null) {
            String str = Config.getString(Config.Device,
                    Config.PREF_PREV_DEVICE, null);
            if (!TextUtils.isEmpty(str)) {
                setPrevUserDevice(UserDevice.fromString(str));
            }
        }
        return prevUserDevice;
    }

    public void setPrevUserDevice(UserDevice d) {
        prevUserDevice = d;
        getUserDevices().add(d);
    }

    public UserDevice updatePrevUserDevice(UserDevice d) {
        if (d == null) {
            return d;
        } else if (prevUserDevice == null) {
            setPrevUserDevice(d);
            return d;
        }
        setPrevUserDevice(prevUserDevice.update(d));
        return prevUserDevice;
    }

    public boolean hasPrevUserDevice() {
        return (getPrevUserDevice() != null && BleUtils.validBleDeviceAddress(getPrevUserDevice().getMacAddr()));
    }

    private UserDevice userDevice = null;

    public UserDevice getUserDevice() {
        return userDevice;
    }

    public void setUserDevice(UserDevice d) {
        if (d != null) {
            setPrevUserDevice(d);
        }
        userDevice = d;
        getUserDevices().add(d);
    }

    public UserDevice updateUserDevice(UserDevice d) {
        if (d == null) {
            return d;
        } else if (userDevice == null) {
            setUserDevice(d);
            return d;
        }
        setUserDevice(userDevice.update(d));
        return userDevice;
    }

    /**
     * 存在默认设备
     */
    public boolean hasUserDevice() {
        return (getUserDevice() != null);
    }

    /**
     * 存在默认合法设备
     */
    public boolean hasUserDeviceAndMac() {
        return (hasUserDevice() && BleUtils.validBleDeviceAddress(getUserDevice().getMacAddr()));
    }

    private UserDevices userDevices = null;

    public UserDevices getUserDevices() {
        if (this.userDevices == null) {
            this.userDevices = new UserDevices();
            Set<String> setStr = Config.getStringSet(Config.Device,
                    Config.PREF_BOUNDED_DEVICES, null);
            if (setStr != null && setStr.size() > 0) {
                for (String str : setStr) {
                    this.userDevices.add(UserDevice.fromString(str));
                }
            }
        }
        return this.userDevices;
    }

    public void setUserDevices(UserDevices userDevices) {
        this.userDevices = userDevices;
    }

    /**
     * 重置用户信息
     */
    public void reset(String id, String key) {
        reset();
        setId(id);
        setKey(key);
        save(UserSaveType.IdAndKey);
    }

    /**
     * 重置用户信息
     */
    public void reset() {
        _user = new User();
    }

    public void resetLastFeMessageTime() {
        super.setTimeLastFeMessage(System.currentTimeMillis());
    }

    public void resetLastAppNetMessageTime() {
        super.setTimeLastAppMessage(System.currentTimeMillis());
    }

    /**
     * 是否设备桥接上网络
     */
    public boolean isConnectedDeviceAppNet() {
        return super.isConnectedDeviceRegistered();
    }

    /**
     * 设置是否设备桥接上网络
     */
    // TODO: 此处启动本地文件转存，以待恢复链接之后重新进行传输
    public void setConnectedDeviceAppNet(boolean conn) {
        super.setIsConnectedDeviceRegistered(conn);
    }

    private long interruptAppNetEcgTime = 0;

    /**
     * 中断Ecg传输
     */
    public void interruptAppNetEcg() {
        this.interruptAppNetEcgTime = System.currentTimeMillis() + 5000;
    }

    /**
     * 是否Ecg传输
     */
    public boolean isInterruptAppNetEcg() {
        return (System.currentTimeMillis() <= this.interruptAppNetEcgTime);
    }

}
