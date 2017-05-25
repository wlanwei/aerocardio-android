package com.uteamtec.heartcool.service.net.module;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.net.http.HttpTool;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * user : 用户服务
 * Created by wd
 */
public final class AppNetTcpUser extends AppNetTcpBase {

    public AppNetTcpUser() {
    }

    /**
     * GET /User/Login.php
     */
    public void login(final String username, final String password,
                      final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonPOST(getURL("120.27.29.91:8088/ecg/Test", new String[]{"User", "Login.php"},
                new HashMap<String, String>() {{
                    put("phone", username);
                    put("pwd", password);
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
     * GET /User/Register.php
     */
    public void register(final String username, final String password,
                         final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonPOST(getURL("120.27.29.91:8088/ecg/Test", new String[]{"User", "Register.php"},
                new HashMap<String, String>() {{
                    put("phone", username);
                    put("pwd", password);
                    put("repwd", password);
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
     * GET /v1/user/isExistTelephone
     */
    public void isExistTelephone(final String telephone, final AppNetTcpCommListener<Boolean> listener) {
        HttpTool.StringGET(getURL(new String[]{"v1", "user", "isExistTelephone"}, new HashMap<String, String>() {{
                    put("telephone", telephone);
                }}),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (listener != null) {
                            listener.onResponse(true, Boolean.parseBoolean(response));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        L.e(error.toString());
                        if (listener != null) {
                            listener.onResponse(false, Boolean.FALSE);
                        }
                    }
                });
    }

    /**
     * GET /v1/user/createUserOrUpdateByApp
     */
    public void createUserOrUpdateByApp(final String telephone, final String passWord, final String realName,
                                        final String sex, final String birthdate, final String age, final String address,
                                        final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "user", "createUserOrUpdateByApp"}, new HashMap<String, String>() {{
                    put("telephone", telephone);
                    put("passWord", passWord);
                    put("realName", realName);
                    put("sex", sex);
                    put("birthdate", birthdate);
                    put("age", age);
                    put("address", address);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            try {
                                if (response.getBoolean("sucess")) {
                                    listener.onResponse(true, response.getString("userInfoId"));
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onResponse(false, response.optString("userInfoId"));
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
     * GET /v1/user/queryAppUserInfoByInfoId
     */
    public void queryAppUserInfoByInfoId(final String infoId,
                                         final AppNetTcpCommListener<JSONObject> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "user", "queryAppUserInfoByInfoId"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            try {
                                if (response.getBoolean("sucess")) {
                                    JSONObject obj = response.getJSONObject("appUserInfo");
                                    if (obj != null) {
                                        listener.onResponse(true, obj);
                                    }
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onResponse(false, response);
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
     * GET /v1/user/queryInfoIdByAppUserCode
     */
    public void queryInfoIdByAppUserCode(final String telephone, final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "user", "queryInfoIdByAppUserCode"}, new HashMap<String, String>() {{
                    put("telephone", telephone);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            try {
                                if (response.getBoolean("sucess")) {
                                    listener.onResponse(true, response.getString("infoId"));
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onResponse(false, response.optString("message"));
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
     * GET /v1/user/deletePatientByTelephone
     */
    public void deletePatientByTelephone(final String telephone, final AppNetTcpCommListener<String> listener) {
        HttpTool.StringDELETE(getURL(new String[]{"v1", "user", "deletePatientByTelephone"}, new HashMap<String, String>() {{
                    put("telephone", telephone);
                }}),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (listener != null) {
                            listener.onResponse(response.contains("成功"), response);
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
     * PUT /v1/user/updatePasswordByApp
     */
    public void updatePasswordByApp(final String telephone, final String password,
                                    final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonPUT(getURL(new String[]{"v1", "user", "updatePasswordByApp"},
                new HashMap<String, String>() {{
                    put("telephone", telephone);
                    put("password", password);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            listener.onResponse(response.optBoolean("sucess", false),
                                    response.optString("message"));
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
     * PUT /v1/user/validate
     */
    public void validate(final String telephone, final String password,
                         final AppNetTcpCommListener<String> listener) {
        HttpTool.JsonGET(getURL(new String[]{"v1", "user", "validate"},
                new HashMap<String, String>() {{
                    put("userCode", telephone);
                    put("pwd", password);
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            listener.onResponse(response.optBoolean("sucess"),
                                    response.optString("message"));
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
