package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.Config;
import com.uteamtec.heartcool.service.type.MobclickEvent;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.type.UserDevice;
import com.uteamtec.heartcool.service.type.UserSaveType;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.utils.NetWorkUtils;

/**
 * 登录页面
 * Created by wd
 */
public class AeroCardioLoginActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private EditText etUsername, etPassword;
    private CheckBox cbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerocardio_login);
    }

    @Override
    protected void initViews() {
        etUsername = EditText.class.cast(findViewById(R.id.aerocardio_login_tv_username));
        etPassword = EditText.class.cast(findViewById(R.id.aerocardio_login_tv_password));

        findViewById(R.id.aerocardio_login_btn_login).setOnClickListener(this);
        findViewById(R.id.aerocardio_login_btn_register).setOnClickListener(this);
        findViewById(R.id.aerocardio_login_btn_forgot).setOnClickListener(this);

        cbRemember = CheckBox.class.cast(findViewById(R.id.aerocardio_login_cb_remember));
        // 是否选中记住密码
        if (Config.getBoolean(Config.Info,
                Config.PREF_LOGIN_REMEMBER, true)) {
            etUsername.setText(User.getUser().getUsername());
            etPassword.setText(User.getUser().getPassword());
            cbRemember.setChecked(true);
            if (Config.getBoolean(Config.Info,
                    Config.PREF_LOGIN_AUTO, false) &&
                    Config.getBoolean(Config.Info,
                            Config.PREF_LOGIN_SUCCESS, false)) {
                login();
            }
        } else {
            cbRemember.setChecked(false);
        }
        cbRemember.setSelected(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aerocardio_login_btn_login:
                login();
                break;
            case R.id.aerocardio_login_btn_register:
                gotoRegister();
                break;
            case R.id.aerocardio_login_btn_forgot:
                gotoForgot();
                break;
        }
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return true;
    }

    @Override
    protected boolean enableServiceConnection() {
        return false;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    private void login() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("用户名不能为空");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("密码不能为空");
            etPassword.requestFocus();
            return;
        }

        etUsername.setError(null);
        etPassword.setError(null);
        etUsername.clearFocus();
        etPassword.clearFocus();

        MobclickEvent.onEvent(this, MobclickEvent.EventId_UserSignIn);

        AppNetTcpComm.getUser().validate(username, password, new AppNetTcpCommListener<String>() {
            @Override
            public void onResponse(boolean success, String response) {
                L.e("validate -> success: " + success + " response:" + response);
                if (success) {
                    if (cbRemember.isChecked()) {
                        Config.putBoolean(Config.Info,
                                Config.PREF_LOGIN_REMEMBER, true);
                        Config.putBoolean(Config.Info,
                                Config.PREF_LOGIN_SUCCESS, true);
                    } else {
                        Config.putBoolean(Config.Info,
                                Config.PREF_LOGIN_REMEMBER, false);
                        Config.putBoolean(Config.Info,
                                Config.PREF_LOGIN_SUCCESS, false);
                    }
                    Config.putBoolean(Config.Info,
                            Config.PREF_LOGIN_AUTO, false);

                    User.getUser().setUsername(username);
                    User.getUser().setPassword(password);
                    User.getUser().save(UserSaveType.Login);

                    Toast.makeText(AeroCardioLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                    AppNetTcpComm.getUser().isExistTelephone(
                            username, new AppNetTcpCommListener<Boolean>() {
                                @Override
                                public void onResponse(boolean success, Boolean response) {
                                    L.e("isExistTelephone -> success: " + success + " response:" + response);
                                    if (success) {
                                        if (response) {
                                            AppNetTcpComm.getUser().queryInfoIdByAppUserCode(
                                                    username, new AppNetTcpCommListener<String>() {
                                                        @Override
                                                        public void onResponse(boolean success, String response) {
                                                            L.e("queryInfoIdByAppUserCode -> success: " + success + " response:" + response);
                                                            if (success) {
                                                                User.getUser().reset(response, "");
                                                                MobclickAgent.onProfileSignIn(response, username);
                                                                loginSuccess();
                                                            } else {
                                                                Toast.makeText(AeroCardioLoginActivity.this,
                                                                        getString(R.string.http_conn_err, response),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            gotoPersonal();
                                        }
                                    } else {
                                        Toast.makeText(AeroCardioLoginActivity.this,
                                                R.string.http_conn_net, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(AeroCardioLoginActivity.this,
                            getString(R.string.login_fail_err, response),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public void gotoRegister() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            startActivity(new Intent(this, AeroCardioRegisterActivity.class));
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoForgot() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            startActivity(new Intent(this, AeroCardioForgetPasswordActivity.class));
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isFinish = false;

    synchronized private void gotoPersonal() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        startActivity(new Intent(this, AeroCardioPersonalActivity.class));
    }

    synchronized private void gotoMain() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        startActivity(new Intent(this, AeroCardioActivity.class));
        finish();
    }

    synchronized private void gotoSetting() {
        if (isFinish) {
            return;
        }
        isFinish = true;
        startActivity(new Intent(this, AeroCardioSettingActivity.class));
        finish();
    }


}
