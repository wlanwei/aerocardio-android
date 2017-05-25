package com.uteamtec.heartcool.service.net.module;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.net.http.HttpTool;
import com.uteamtec.heartcool.service.type.EcgMark;
import com.uteamtec.heartcool.service.type.EcgMarks;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * mark : 数据标记服务
 * Created by wd
 */
public final class AppNetTcpEcgMark extends AppNetTcpBase {

    public AppNetTcpEcgMark() {
    }

    /**
     * GET /v1/mark/queryAppMarkCounts
     */
    public void queryAppMarkCounts(final String infoId, final long startTime, final long stopTime,
                                   final AppNetTcpCommListener<List<EcgMarks>> listener) {
        HttpTool.JsonArrayGET(getURL(new String[]{"v1", "mark", "queryAppMarkCounts"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                    put("startTime", String.valueOf(startTime));
                    put("stopTime", String.valueOf(stopTime));
                }}),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
//                        L.e(response.toString());
                        List<EcgMarks> list;
                        if (listener != null) {
                            try {
                                if (response != null) {
                                    list = new ArrayList<>();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject obj = response.getJSONObject(i);
                                        JSONArray arr = obj.optJSONArray("ecgmark");
                                        if (arr == null) {
                                            continue;
                                        } else if (obj.optInt("counts", 0) <= 0) {
                                            continue;
                                        }
                                        EcgMarks ms = new EcgMarks(obj.optString("message"));
                                        for (int j = 0; j < arr.length(); j++) {
                                            JSONObject o = arr.getJSONObject(j);
                                            EcgMark m = new EcgMark(
                                                    o.optLong("startTime"),
                                                    o.optLong("stopTime"),
                                                    o.optInt("typeGroup"),
                                                    o.optInt("type"),
                                                    o.optInt("value")
                                            );
                                            m.setDeviceId(o.optString("deviceId").getBytes());
                                            ms.add(m);
                                        }
                                        list.add(ms);
                                    }
                                    listener.onResponse(true, list);
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            listener.onResponse(false, null);
                        }
//                        if (response != null) {
//                            L.e(response.toString());
//                        }
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
