package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.uteamtec.heartcool.R;

public class AeroCardioRegisterNextActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private String phone; // 手机号码

    private EditText etPsw; // 密码
    private EditText etPswAgain; // 确认密码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phone = getIntent().getStringExtra("phone");
        if (TextUtils.isEmpty(phone)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_aerocardio_register_next);
    }

    @Override
    protected void initViews() {
        etPsw = (EditText) findViewById(R.id.aerocardio_register_next_tv_password);
        etPswAgain = (EditText) findViewById(R.id.aerocardio_register_next_tv_password_again);

        findViewById(R.id.aerocardio_register_next_iv_back).setOnClickListener(this);
        findViewById(R.id.aerocardio_register_next_btn_finish).setOnClickListener(this);
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
            case R.id.aerocardio_register_next_iv_back:
                onBackPressed();
                break;
            case R.id.aerocardio_register_next_btn_finish:
                register();
                break;
        }
    }

    /**
     * 注册
     */
    private void register() {
        final String PSW = etPsw.getText().toString().trim();
        final String PSW_AGAIN = etPswAgain.getText().toString().trim();
        if (TextUtils.isEmpty(PSW) || !PSW.equals(PSW_AGAIN)) {
            Toast.makeText(this, R.string.diffPassword, Toast.LENGTH_SHORT).show();
            return;
        }
        gotoPersonal();
//        AppNetTcpComm.getUser().register(phone, PSW, new AppNetTcpCommListener<String>() {
//            @Override
//            public void onResponse(boolean success, String response) {
//                L.e("register -> success: " + success + " response:" + response);
//                Toast.makeText(AeroCardioRegisterNextActivity.this,
//                        response, Toast.LENGTH_SHORT).show();
//                if (success) {
//                    gotoPersonal();
//                }
//            }
//        });
    }

    private void gotoPersonal() {
        Intent intent = new Intent(this, AeroCardioPersonalActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("password", etPsw.getText().toString().trim());
        intent.putExtra("register", true);
        startActivity(intent);
        finish();
    }

}
