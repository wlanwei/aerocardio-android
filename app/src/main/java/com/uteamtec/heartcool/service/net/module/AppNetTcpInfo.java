package com.uteamtec.heartcool.service.net.module;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.net.http.HttpTool;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.type.UserDevices;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * info : UserInfo服务
 * Created by wd
 */
public final class AppNetTcpInfo extends AppNetTcpBase {

    public AppNetTcpInfo() {
    }

    /**
     * GET /v1/info/bindDeviceByMacAddress
     */
    public void bindDeviceByMacAddress(final String infoId, final String macAddress,
                                       final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "info", "bindDeviceByMacAddress"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                    put("macAddress", macAddress);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            try {
                                listener.onResponse(response.getBoolean("sucess"), response.getString("message"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onResponse(false, e.getMessage());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.e(error.toString());
                        if (listener != null) {
                            listener.onResponse(false, error.toString());
                        }
                    }
                });
    }

    /**
     * GET /v1/info/validateUserInfoAndDevice
     */
    public void validateUserInfoAndDevice(final String infoId, final String macAddress,
                                          final AppNetTcpCommListener<UserDevices> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "info", "validateUserInfoAndDevice"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                    put("macAddress", macAddress);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserDevices ds = null;
                        if (listener != null) {
                            try {
                                if (response.getBoolean("sucess")) {
                                    ds = new UserDevices();
                                    JSONArray arr = response.getJSONArray("BoundedDevices");
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject obj = arr.getJSONObject(i);
                                        UserDevice d = new UserDevice(obj.getString("deviceId"),
                                                obj.getInt("deviceType"));
                                        d.setKey(obj.getString("deviceKey"));
                                        d.setKeyPair(obj.getString("deviceKeyPair"));
                                        d.setMacAddr(obj.getString("deviceMacAddr"));
                                        ds.add(d);
                                    }
                                    listener.onResponse(true, ds);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onResponse(false, ds);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.e(error.toString());
                        if (listener != null) {
                            listener.onResponse(false, null);
                        }
                    }
                });
    }

    /**
     * GET /v1/info/queryBindDeviceByInfoId
     */
    public void queryBindDeviceByInfoId(final String infoId,
                                        final AppNetTcpCommListener<UserDevice> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "info", "queryBindDeviceByInfoId"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserDevice d = null;
                        if (listener != null) {
                            try {
                                if (response.getBoolean("sucess")) {
                                    JSONObject obj = response.getJSONObject("device");
                                    if (obj != null) {
                                        d = new UserDevice(obj.getString("deviceId"),
                                                obj.getInt("model"));
                                        d.setKey(obj.getString("key"));
                                        d.setKeyPair(obj.getString("keyPair"));
                                        d.setMacAddr(obj.getString("macAddr"));
                                        d.setSps(obj.getInt("sps"));
                                        d.setStreamLen(obj.getInt("streamLength"));
                                    }
                                    listener.onResponse(true, d);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onResponse(false, d);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.e(error.toString());
                        if (listener != null) {
                            listener.onResponse(false, null);
                        }
                    }
                });
    }

}
