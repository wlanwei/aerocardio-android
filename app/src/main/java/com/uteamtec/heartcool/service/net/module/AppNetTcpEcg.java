package com.uteamtec.heartcool.service.net.module;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.net.http.HttpTool;
import com.uteamtec.heartcool.service.utils.DateUtils;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ecg : 心电数据服务
 * Created by wd
 */
public final class AppNetTcpEcg extends AppNetTcpBase {

    public AppNetTcpEcg() {
    }

    /**
     * GET /v1/ecg/saveAppEcgAnalysis
     */
    public void saveAppEcgAnalysis(final String infoId, final DBDetection detection,
                                   final AppNetTcpCommListener<String> listener) {
        if (detection == null) {
            return;
        }
        HttpTool.JsonGET(getURL(new String[]{"v1", "ecg", "saveAppEcgAnalysis"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                    put("startTime", String.valueOf(detection.getStartTime()));
                    put("stopTime", String.valueOf(detection.getStopTime()));
                    put("avgHR", String.valueOf(detection.getHR()));
                    put("normalHRRange", String.valueOf(detection.getHRRange()));
                    put("monitoringFeedback", detection.getFeedback());
                    put("anomalyIndex", detection.getAbnormal());
                    put("breathRate", String.valueOf(detection.getBR()));
                    put("signalQuality", detection.getMarkStats().getSQ());
                }}),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            try {
                                listener.onResponse(response.getBoolean("sucess"), "");
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
     * GET /v1/ecg/queryAppEcgAnalysisByTime
     */
    public void queryAppEcgAnalysisByTime(final String infoId,
                                          final long startTime, final long stopTime,
                                          final AppNetTcpCommListener<List<DBDetection>> listener) {
        HttpTool.JsonArrayGET(getURL(new String[]{"v1", "ecg", "queryAppEcgAnalysisByTime"}, new HashMap<String, String>() {{
                    put("infoId", infoId);
                    put("startTime", String.valueOf(startTime));
                    put("stopTime", String.valueOf(stopTime));
                }}),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
//                        L.e(response.toString());
                        List<DBDetection> list = null;
                        if (listener != null) {
                            try {
                                if (response != null) {
                                    list = new ArrayList<>();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject obj = response.getJSONObject(i);
                                        DBDetection d = new DBDetection(
                                                obj.getLong("startTime"),
                                                obj.getLong("stopTime"),
                                                obj.optInt("avgHR"),
                                                obj.optInt("normalHRRange"),
                                                obj.optString("monitoringFeedback"),
                                                obj.optString("anomalyIndex")
                                        );
                                        d.setBR(obj.getInt("breathRate"));
                                        list.add(d);
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

    public void queryAppEcgAnalysisByTime(final String infoId,
                                          final String month,
                                          final AppNetTcpCommListener<List<DBDetection>> listener) {
        int y = Integer.parseInt(month.substring(0, 4));
        int m = Integer.parseInt(month.substring(5, 7));
        queryAppEcgAnalysisByTime(
                infoId,
                DateUtils.getTimesMonthMorning(y, m),
                DateUtils.getTimesMonthNight(y, m),
                listener
        );
    }

    /**
     * GET /v1/ecg/queryEcgByInfoIdAndTime
     */
    public void queryEcgByInfoIdAndTime(final String infoId,
                                        final long startTime, final long stopTime,
                                        final AppNetTcpCommListener<List<Ecg>> listener) {
        HttpTool.JsonArrayGET(getURL(new String[]{"v1", "ecg", "queryEcgByInfoIdAndTime"}, new HashMap<String, String>() {{
                    put("userInfoId", infoId);
                    put("startTime", String.valueOf(startTime));
                    put("stopTime", String.valueOf(stopTime));
                }}),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<Ecg> list = null;
                        if (listener != null) {
                            try {
                                if (response != null) {
                                    list = new ArrayList<>();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject obj = response.getJSONObject(i);
                                        Ecg e = new Ecg(
                                                obj.optInt("type"),
                                                obj.optLong("startTime"),
                                                obj.optLong("stopTime"),
                                                obj.optInt("sps"),
                                                null
                                        );
                                        JSONArray arr = obj.getJSONArray("data");
                                        e.setData(new int[arr.length()]);
                                        for (int j = 0; j < arr.length(); j++) {
                                            e.getData()[j] = arr.getInt(j);
                                        }
                                        list.add(e);
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
