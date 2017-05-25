package com.uteamtec.heartcool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.utils.L;

import java.util.Random;

public class AeroCardioRegisterActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private EditText etPhone; // 手机号码
    private EditText etVerify; // 验证码

    private Button btnSendVerify; // 验证码

    private String code;// 验证码

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aerocardio_register);
    }

    @Override
    protected void initViews() {
        etPhone = (EditText) findViewById(R.id.aerocardio_register_tv_phone);
        etVerify = (EditText) findViewById(R.id.aerocardio_register_tv_verify);

        btnSendVerify = (Button) findViewById(R.id.aerocardio_register_btn_send_verify);
        btnSendVerify.setOnClickListener(this);

        findViewById(R.id.aerocardio_register_iv_back).setOnClickListener(this);
        findViewById(R.id.aerocardio_register_btn_next).setOnClickListener(this);
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
            case R.id.aerocardio_register_btn_send_verify:
                btnSendVerify.setEnabled(false);
                sendCode();
                break;
            case R.id.aerocardio_register_iv_back:
                onBackPressed();
                break;
            case R.id.aerocardio_register_btn_next:
                checkVerify();
                break;
        }
    }

    private static String randomNumeric(int length) {
        String str = "0123456789";
        Random random = new Random();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10);
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    /**
     * 点击获取验证码方法
     */
    private void sendCode() {
        AppNetTcpComm.getUser().isExistTelephone(
                etPhone.getText().toString(),
                new AppNetTcpCommListener<Boolean>() {
                    @Override
                    public void onResponse(boolean success, Boolean response) {
                        L.e("isExistTelephone -> success: " + success + " response:" + response);
                        if (success && response) {
                            btnSendVerify.setEnabled(true);
                            Toast.makeText(AeroCardioRegisterActivity.this,
                                    R.string.is_exist_telephone, Toast.LENGTH_SHORT).show();
                        } else {
                            code = randomNumeric(4);
                            AppNetTcpComm.getSms().sendMessage(etPhone.getText().toString(), code,
                                    new AppNetTcpCommListener<String>() {
                                        @Override
                                        public void onResponse(boolean success, String response) {
                                            L.e("sendCode -> success: " + success + " response:" + response);
                                            if (success) {
                                                Toast.makeText(AeroCardioRegisterActivity.this,
                                                        R.string.verify_code_send, Toast.LENGTH_SHORT).show();
                                                new MyCount(90000, 1000).start();
                                            } else {
                                                Toast.makeText(AeroCardioRegisterActivity.this,
                                                        response, Toast.LENGTH_SHORT).show();
                                                new MyCount(10000, 1000).start();
                                            }
                                        }
                                    });

                            //                            // TODO: 测试
//                            L.e("code: " + code);
//                            btnSendVerify.setEnabled(true);
                        }
                    }
                });
    }

    /**
     * 验证码校验方法
     */
    private void checkVerify() {
        if (code.equals(etVerify.getText().toString().trim())) {
            Toast.makeText(this, R.string.verify_code_success, Toast.LENGTH_SHORT).show();

            gotoNext();
        } else {
            Toast.makeText(this, R.string.verify_code_fail, Toast.LENGTH_SHORT).show();
        }
//        AppNetTcpComm.getSms().verifyCode(
//                etPhone.getText().toString(),
//                etVerify.getText().toString(),
//                new AppNetTcpCommListener<String>() {
//                    @Override
//                    public void onResponse(boolean success, String response) {
//                        L.e("verifyCode -> success: " + success + " response:" + response);
//                        if (success) {
//                            Toast.makeText(AeroCardioRegisterActivity.this,
//                                    response, Toast.LENGTH_SHORT).show();
//
//                            gotoNext();
//                        } else {
//                            Toast.makeText(AeroCardioRegisterActivity.this,
//                                    response, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
    }

    /**
     * 定义一个倒计时的内部类
     */
    private final class MyCount extends CountDownTimer {

        private MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            btnSendVerify.setText("重新发送");
            btnSendVerify.setEnabled(true);
            btnSendVerify.setBackgroundResource(R.drawable.register_back_select);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btnSendVerify.setText("" + millisUntilFinished / 1000 + "秒");
            btnSendVerify.setBackgroundResource(R.drawable.register_back);
            btnSendVerify.setEnabled(false);
        }
    }

    private void gotoNext() {
        Intent intent = new Intent(this, AeroCardioRegisterNextActivity.class);
        intent.putExtra("phone", etPhone.getText().toString());
        startActivity(intent);
        finish();
    }

}
