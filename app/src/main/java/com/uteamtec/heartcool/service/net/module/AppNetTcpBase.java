package com.uteamtec.heartcool.service.net.module;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Map;

/**
 * Created by wd
 */
abstract class AppNetTcpBase {

    private Uri.Builder getBaseUriBuilder(String url) {
        if (TextUtils.isEmpty(url)) {
            return new Uri.Builder().scheme("http").encodedAuthority("112.124.70.101:8097").appendPath("webService");
        }
        return new Uri.Builder().scheme("http").encodedAuthority(url);
    }

    public String getURL(String[] path, Map<String, String> params) {
        return getURL(null, path, params);
    }

    public String getURL(String url, String[] path, Map<String, String> params) {
        Uri.Builder builder = getBaseUriBuilder(url);
        if (path != null && path.length > 0) {
            for (String p : path) {
                builder.appendPath(p);
            }
        }
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.toString();
    }

}
