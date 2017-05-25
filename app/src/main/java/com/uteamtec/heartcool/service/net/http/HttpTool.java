package com.uteamtec.heartcool.service.net.http;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.uteamtec.heartcool.AeroCardioApp;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * http工具集
 * Created by wd
 */
public final class HttpTool {

    private static HttpTool mInstance;

    private RequestQueue mRequestQueue;

    private HttpTool() {
        mRequestQueue = getRequestQueue();
    }

    private static synchronized HttpTool getInstance() {
        if (mInstance == null) {
            mInstance = new HttpTool();
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(AeroCardioApp.getApplication());
        }
        return mRequestQueue;
    }

    private static <T> void addToRequestQueue(Request<T> req) {
        getInstance().getRequestQueue().add(req);
    }

    public static void StringGET(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new StringRequest(Request.Method.GET, url, listener, errorListener));
    }

    public static void StringPOST(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new StringRequest(Request.Method.POST, url, listener, errorListener));
    }

    public static void StringPUT(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new StringRequest(Request.Method.PUT, url, listener, errorListener));
    }

    public static void StringDELETE(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new StringRequest(Request.Method.DELETE, url, listener, errorListener));
    }

    public static void JsonGET(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener));
    }

    public static void JsonPOST(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonPOST(url, null, listener, errorListener);
    }

    public static void JsonPOST(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new JsonObjectRequest(Request.Method.POST, url, jsonRequest, listener, errorListener));
    }

    public static void JsonPUT(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonPUT(url, null, listener, errorListener);
    }

    public static void JsonPUT(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new JsonObjectRequest(Request.Method.PUT, url, jsonRequest, listener, errorListener));
    }

    public static void JsonArrayGET(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        L.e(url);
        addToRequestQueue(new JsonArrayRequest(Request.Method.GET, url, null, listener, errorListener));
    }

}
