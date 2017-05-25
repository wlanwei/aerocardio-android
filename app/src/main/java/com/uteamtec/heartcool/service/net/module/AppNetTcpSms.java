package com.uteamtec.heartcool.service.net.module;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.net.http.HttpTool;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * sendMessage : 短信服务
 * Created by wd
 */
public final class AppNetTcpSms extends AppNetTcpBase {

    public AppNetTcpSms() {
    }

    /**
     * GET /User/PhoneCode.php
     */
    public void sendCode(final String username, final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonPOST(getURL("120.27.29.91:8088/ecg/Test", new String[]{"User", "PhoneCode.php"},
                new HashMap<String, String>() {{
                    put("phone", username);
                    put("type", "1");
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            listener.onResponse(response.optString("cbm").equals("OK"),
                                    response.optString("cms"));
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
     * GET /User/VerifyCode.php
     */
    public void verifyCode(final String username, final String code,
                           final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonPOST(getURL("120.27.29.91:8088/ecg/Test", new String[]{"User", "VerifyCode.php"},
                new HashMap<String, String>() {{
                    put("phone", username);
                    put("code", code);
                    put("type", "1");
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            listener.onResponse(response.optString("cbm").equals("OK"),
                                    response.optString("cms"));
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
     * GET /v1/sendMessage/sendMessage
     */
    public void sendMessage(final String tel, final String code,
                            final AppNetTcpCommListener<String> listener) {
        HttpTool.StringPOST(getURL(new String[]{"v1", "sendMessage", "sendMessage"}, new HashMap<String, String>() {{
                    put("tel", tel);
                    put("code", code);
                }}),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (listener != null) {
                            listener.onResponse(response.startsWith("发送成功"), response);
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

}
