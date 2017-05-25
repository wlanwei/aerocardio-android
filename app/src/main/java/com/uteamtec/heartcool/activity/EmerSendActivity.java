package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.model.User;
import com.uteamtec.heartcool.utils.ApiUrl;
import com.uteamtec.heartcool.utils.NetWorkUtils;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

public class EmerSendActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private CheckBox cb_sendSms, cb_callPhone, cb_sendEmail;//发送短信，拨打电话，发送邮件
    private Button bt_sure;//确定
    private int sms;//发送短信
    private String id, callPhone;//用户id，用户姓名，打电话，发邮件
    private int flagSend, flagCallphone, flagEmail;//判断是否勾选的标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emer_send);
        initUI();//初始化控件
        setData();//判断显示数据的状态

    }

    //初始化控件
    private void initUI() {
        cb_sendSms = (CheckBox) findViewById(R.id.activity_emer_send_cb_send);//发送短信
        cb_callPhone = (CheckBox) findViewById(R.id.activity_emer_send_cb_phone);//拨打电话
        //  cb_sendEmail = (CheckBox) findViewById(R.id.activity_emer_send_cb_email);//发送邮件
        bt_sure = (Button) findViewById(R.id.activity_emer_send_bt_sure);
        cb_sendSms.setOnCheckedChangeListener(this);
        cb_callPhone.setOnCheckedChangeListener(this);
        //cb_sendEmail.setOnCheckedChangeListener(this);
        bt_sure.setOnClickListener(this);

    }

    /**
     * 返回上一级界面
     *
     * @param view
     */
    public void wayReturn(View view) {
        onBackPressed();
    }

    //修改紧急发送方式状态
    private void update() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            AjaxParams params = new AjaxParams();
            params.put("uid", User.getInstance().getUid());
            params.put("fsdx", Integer.toString(flagSend));//0未勾选，1勾选//短信状态
            params.put("zdbd", Integer.toString(flagCallphone));
            FinalHttp fh = new FinalHttp();
            fh.post(ApiUrl.SET_SENDWAY, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String s) {
                    Log.i("info", "----update--" + s);
                    super.onSuccess(s);
                }

                @Override
                public void onLoading(long count, long current) {
                    super.onLoading(count, current);
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    super.onFailure(t, errorNo, strMsg);
                }
            });

        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }


    }

    //判断显示数据的状态
    private void setData() {
        setDownload();
    }

    //与服务器交互
    private void setDownload() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            AjaxParams params = new AjaxParams();
            params.put("uid", User.getInstance().getUid());
            FinalHttp fh = new FinalHttp();
            fh.post(ApiUrl.SEND_WAY, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    Log.i("info", "-----cccsend--" + s);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(s);
                        String cbm = object.getString("cbm");
                        if (cbm.equals("OK")) {
                            jsonDate(object);//数据解析
                        } else {
                            Toast.makeText(EmerSendActivity.this, object.getString("cms"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        } else {

            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }

    }

    //解析数据
    private void jsonDate(JSONObject object) {
        try {
            JSONObject obj = object.getJSONObject("cms");
            id = obj.getString("ID");
            sms = obj.getInt("FSDX");
            callPhone = obj.getString("ZDBD");
            if (sms == 0) {//判断短信的状态
                cb_sendSms.setChecked(false);
            } else if (sms == 1) {
                cb_sendSms.setChecked(true);
            }
            if (callPhone.equals("0")) {//判断拨打电话的状态
                cb_callPhone.setChecked(false);
            } else if (callPhone.equals("1")) {
                cb_callPhone.setChecked(true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //选择框监听
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.activity_emer_send_cb_send:
                if (isChecked) {
                    flagSend = 1;
                } else {
                    flagSend = 0;
                }
                break;
            case R.id.activity_emer_send_cb_phone:
                if (isChecked) {
                    flagCallphone = 1;
                } else {
                    flagCallphone = 0;
                }
                break;
           /* case R.id.activity_emer_send_cb_email:
                if (isChecked) {
                    flagEmail = 1;
                } else {
                    flagEmail = 0;
                }
                break;*/

        }
    }

    //确定按钮监听
    @Override
    public void onClick(View v) {

        update();//保存修改的设置
        Intent intent = new Intent(this, SettingActivity.class);//保存语言选择，并跳转到设置页面
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EmerSendActivity.this, SettingActivity.class);
        startActivity(intent);
        finish();
    }
}
