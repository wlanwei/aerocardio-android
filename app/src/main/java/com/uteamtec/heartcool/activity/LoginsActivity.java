package com.uteamtec.heartcool.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.type.UserSaveType;
import com.uteamtec.heartcool.utils.ApiUrl;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.utils.NetWorkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class LoginsActivity extends AppCompatActivity {

    private long BackPressedTime = 0;

    private EditText et_login_number, et_login_pwd; //，密码,用户名
    private CheckBox content_login_cBox_remember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logins);
        // 初始化控件
        et_login_pwd = (EditText) findViewById(R.id.login_userpassword_tv);
        et_login_number = (EditText) findViewById(R.id.login_username_tv);
        content_login_cBox_remember = (CheckBox) findViewById(R.id.content_login_cBox_remember);
        content_login_cBox_remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                L.e("content_login_cBox_remember: " + b);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sf = getSharedPreferences("info", Context.MODE_PRIVATE);//保存在文件名为info的xml文件里
        if (sf.getBoolean("rememberpwd", true)) { //是否选中记住密码
            et_login_number.setText(sf.getString("user", ""));
            et_login_pwd.setText(sf.getString("pwd", ""));
            content_login_cBox_remember.setChecked(true);
            if (sf.getBoolean("auto_login", false) && sf.getBoolean("success", false)) {
                toLogin();
            }
        } else {
            content_login_cBox_remember.setChecked(false);
        }
        content_login_cBox_remember.setSelected(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - BackPressedTime <= 2000) {
            BackPressedTime = 0;
            LoginsActivity.this.finish();
            return;
        }
        Toast.makeText(this, getString(R.string.exitApp), Toast.LENGTH_SHORT).show();
        BackPressedTime = System.currentTimeMillis();
    }

    //登录方法
    private void toLogin() {
//      判断EditText输入是否有效，并开始登录
        final String username = et_login_number.getText().toString().trim();
        final String userpwd = et_login_pwd.getText().toString().trim();
        //设置错误信息为空
        et_login_number.setError(null);
        et_login_pwd.setError(null);

        //清除其焦点状态
        et_login_number.clearFocus();
        et_login_pwd.clearFocus();


        if (TextUtils.isEmpty(username)) {
            et_login_number.setError("用户名不能为空");
            et_login_number.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userpwd)) {
            et_login_pwd.setError("密码不能为空");
            et_login_pwd.requestFocus();
            return;
        }

        //Xutils请求的参数（URL）
        RequestParams params = new RequestParams(ApiUrl.LOGIN);
        //添加参数
        params.addParameter("phone", username); //用户
        params.addParameter("pwd", userpwd); //用户密码
        Log.i("info", "----user---" + username + userpwd);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                //从服务器返回的值
                Log.d("TAG", "result ->" + result);
                //返回结果是否正确，没有进行判断
                try {
                    JSONObject object = new JSONObject(result);
                    String result1 = object.getString("cbm");
                    if ("OK".equals(result1)) {
                        SharedPreferences.Editor edit = getSharedPreferences("info", Context.MODE_PRIVATE).edit();
                        if (content_login_cBox_remember.isChecked()) {
                            edit.putBoolean("rememberpwd", true);
                            edit.putString("user", et_login_number.getText().toString());
                            edit.putString("pwd", et_login_pwd.getText().toString());
                            edit.putBoolean("success", true);
                        } else {
                            edit.putBoolean("rememberpwd", false);
                            edit.putString("user", "");
                            edit.putString("pwd", "");
                            edit.putBoolean("success", false);
                        }
                        edit.putBoolean("auto_login", false);
                        edit.apply();
                        JSONArray array = object.getJSONArray("cms");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
//                            User user = new User();
//                            String uid = obj.getString("uid");
//                            user.setUid(uid);
//                            user.setUser(obj.getString("user"));
//                            User.getInstance().setUid(uid);//获取uid
                        }
                        Toast.makeText(LoginsActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                        User.getUser().setUsername(et_login_number.getText().toString());
                        User.getUser().setPassword(et_login_pwd.getText().toString());
                        User.getUser().save(UserSaveType.Login);

                        MobclickAgent.onProfileSignIn(User.getUser().getUsername());

                        AppNetTcpComm.getUser().isExistTelephone(
                                et_login_number.getText().toString(),
                                new AppNetTcpCommListener<Boolean>() {
                                    @Override
                                    public void onResponse(boolean success, Boolean response) {
                                        L.e("isExistTelephone -> success: " + success + " response:" + response);
                                        if (success) {
                                            if (response) {
                                                AppNetTcpComm.getUser().queryInfoIdByAppUserCode(
                                                        et_login_number.getText().toString(),
                                                        new AppNetTcpCommListener<String>() {
                                                            @Override
                                                            public void onResponse(boolean success, String response) {
                                                                L.e("queryInfoIdByAppUserCode -> success: " + success + " response:" + response);
                                                                if (success) {
                                                                    User.getUser().reset(response, "");
                                                                    loginSuccess();
                                                                } else {
                                                                    Toast.makeText(LoginsActivity.this,
                                                                            getString(R.string.http_conn_err, response),
                                                                            Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                gotoPersonal();
                                            }
                                        } else {
                                            Toast.makeText(LoginsActivity.this,
                                                    R.string.http_conn_net, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(LoginsActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(LoginsActivity.this, R.string.http_conn_net, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public void login(View view) {  //登录按钮监听
        //调用登录方法
        toLogin();
    }

    public void register(View view) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            startActivity(new Intent(this, AeroCardioRegisterActivity.class));
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }
    }

    public void forgotPassword(View view) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            startActivity(new Intent(this, AeroCardioForgetPasswordActivity.class));
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void loginSuccess() {
        if (User.getUser().hasPrevUserDevice()) {
            gotoMain();
        } else {
            AppNetTcpComm.getInfo().queryBindDeviceByInfoId(
                    User.getUser().getIdString(),
                    new AppNetTcpCommListener<UserDevice>() {
                        @Override
                        public void onResponse(boolean success, UserDevice response) {
                            L.e("queryBindDeviceByInfoId -> success: " + success);
                            if (success && response != null) {
                                L.e("queryBindDeviceByInfoId -> response:" + response.toString());
                                User.getUser().updateUserDevice(response);
                                gotoMain();
                            } else {
                                gotoSetting();
                            }
                        }
                    });
        }
    }

    private boolean isFinish = false;

    private synchronized void gotoPersonal() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        startActivity(new Intent(this, AeroCardioPersonalActivity.class));
        this.finish();
    }

    private synchronized void gotoMain() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        startActivity(new Intent(this, AeroCardioActivity.class));
        this.finish();
    }

    private synchronized void gotoSetting() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        startActivity(new Intent(this, AeroCardioSettingActivity.class));
        this.finish();
    }

}
