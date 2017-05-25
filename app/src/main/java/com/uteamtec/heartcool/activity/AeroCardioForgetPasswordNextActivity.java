package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.MobclickEvent;
import com.uteamtec.heartcool.utils.L;

public class AeroCardioForgetPasswordNextActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private EditText etPwd; // 密码
    private EditText etPwdAgain; // 确认密码

    private String phone; // 手机号码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phone = getIntent().getStringExtra("phone");
        if (TextUtils.isEmpty(phone)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_aerocardio_forget_password_next);
    }

    @Override
    protected void initViews() {
        etPwd = (EditText) findViewById(R.id.aerocardio_forget_next_tv_password);
        etPwdAgain = (EditText) findViewById(R.id.aerocardio_forget_next_tv_password_next);

        findViewById(R.id.aerocardio_forget_next_iv_back).setOnClickListener(this);
        findViewById(R.id.aerocardio_forget_next_btn_finish).setOnClickListener(this);
    }

    @Override
    protected boolean enableBackPressedFinish() {
        return false;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aerocardio_forget_next_iv_back:
                onBackPressed();
                break;
            case R.id.aerocardio_forget_next_btn_finish:
                changePassword();
                break;
        }
    }

    /**
     * 修改密码方法
     */
    private void changePassword() {
        final String PSW = etPwd.getText().toString().trim();
        final String PSW_AGAIN = etPwdAgain.getText().toString().trim();
        if (TextUtils.isEmpty(PSW) || !PSW.equals(PSW_AGAIN)) {
            Toast.makeText(this,
                    R.string.diffPassword, Toast.LENGTH_SHORT).show();
            return;
        }
        AppNetTcpComm.getUser().updatePasswordByApp(phone, PSW,
                new AppNetTcpCommListener<String>() {
                    @Override
                    public void onResponse(boolean success, String response) {
                        L.e("updatePasswordByApp -> success:" + success + " response:" + response);
                        if (success) {
                            MobclickEvent.onEvent(AeroCardioForgetPasswordNextActivity.this,
                                    MobclickEvent.EventId_UserForgot);
                            Toast.makeText(AeroCardioForgetPasswordNextActivity.this,
                                    R.string.reset_password_success, Toast.LENGTH_SHORT).show();
                            gotoLogin();
                        } else {
                            Toast.makeText(AeroCardioForgetPasswordNextActivity.this,
                                    getString(R.string.password_fail_err, response),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void gotoLogin() {
        startActivity(new Intent(this, AeroCardioLoginActivity.class));
        finish();
    }

}
