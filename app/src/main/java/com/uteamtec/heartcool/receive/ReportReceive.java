package com.uteamtec.heartcool.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;

import com.uteamtec.heartcool.model.Contact;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class ReportReceive extends BroadcastReceiver {

    private String contactName, contactPhone, contactEmail, contactOpt;//联系人姓名，联系人电话，联系人邮箱，联系人状态
    private Contact contact;
    private ArrayList<Contact> al_coantact = null;
    private SharedPreferences sf;
    private String pjxl = null;
    private String smzl = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        sf = context.getSharedPreferences(
                "info", context.MODE_PRIVATE);
        pjxl = sf.getString("pjxl", null);//获取平均心律
        smzl = sf.getString("smzl", null);//获取睡眠质量
        String hour = String.valueOf(Calendar.getInstance().get(
                Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(Calendar.getInstance().get(
                Calendar.MINUTE));
        String time = sf.getString(hour + ":" + minute, null);
        if (time != null) {

            for (int i = 1; i <= sf.getInt("size", 0); i++) {
                String string = sf.getString("phoneNumber" + i, null);
                Log.i("info", "----sms---" + string);
                sendMsg(string, pjxl + ";" + smzl);//遍历选中的联系人号码
            }
        }


    }

    private void sendMsg(String number, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
    }


}
