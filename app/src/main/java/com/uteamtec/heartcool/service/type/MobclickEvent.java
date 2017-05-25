package com.uteamtec.heartcool.service.type;

import android.content.Context;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wd
 */
public final class MobclickEvent {

    public static final String EventId_UserSignUp = "user_sign_up";
    public static final String EventId_UserSignIn = "user_sign_in";
    public static final String EventId_UserSignOut = "user_sign_out";
    public static final String EventId_UserForgot = "user_forgot";

    public static final String EventId_DetectionStart = "detection_start";
    public static final String EventId_DetectionRecord = "detection_record";

    public static final String EventId_DetectionHistory = "detection_history";

    private static Map<String, Long> timers = new HashMap<>();

    private static void onTimeBegin(String eventId) {
        timers.put(eventId, System.currentTimeMillis());
    }

    private static long onTimeEnd(String eventId) {
        Long l = timers.get(eventId);
        if (l != null) {
            return System.currentTimeMillis() - l;
        }
        return 0;
    }

    public static void onEvent(Context context, String eventId) {
        if (context != null && !TextUtils.isEmpty(eventId)) {
            MobclickAgent.onEvent(context, eventId);
        }
    }

    public static void onEventValueBegin(Context context, String eventId) {
        if (context != null && !TextUtils.isEmpty(eventId)) {
            onTimeBegin(eventId);
        }
    }

    public static void onEventValueEnd(Context context, String eventId) {
        if (context != null && !TextUtils.isEmpty(eventId)) {
            MobclickAgent.onEventValue(context, eventId, null, (int) onTimeEnd(eventId));
        }
    }

}
