package com.uteamtec.heartcool.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.service.net.AppNetTcpComm;
import com.uteamtec.heartcool.service.net.AppNetTcpCommListener;
import com.uteamtec.heartcool.service.type.MobclickEvent;
import com.uteamtec.heartcool.service.type.User;
import com.uteamtec.heartcool.service.utils.DateFormats;
import com.uteamtec.heartcool.utils.L;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * 个人信息
 * Created by wd
 */
public class AeroCardioPersonalActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private String phone; // 手机号码
    private String password; // 密码
    private boolean register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phone = getIntent().getStringExtra("phone");
        if (TextUtils.isEmpty(phone)) {
            phone = User.getUser().getUsername();
        }
        if (TextUtils.isEmpty(phone)) {
            finish();
            return;
        }
        password = getIntent().getStringExtra("password");
        if (TextUtils.isEmpty(password)) {
            password = User.getUser().getPassword();
        }
        if (TextUtils.isEmpty(password)) {
            finish();
            return;
        }
        register = getIntent().getBooleanExtra("register", false);

        setContentView(R.layout.activity_personal);
    }

    @Override
    protected void initViews() {
        findViewById(R.id.tx_personal_birthday).setOnClickListener(this);
        findViewById(R.id.btn_personal_save).setOnClickListener(this);
        findViewById(R.id.btn_personal_cancel).setOnClickListener(this);

        if (TextUtils.isEmpty(User.getUser().getIdString())) {
            showUserInfo(null);
        } else {
            AppNetTcpComm.getUser().queryAppUserInfoByInfoId(
                    User.getUser().getIdString(),
                    new AppNetTcpCommListener<JSONObject>() {
                        @Override
                        public void onResponse(boolean success, JSONObject response) {
                            L.e("queryAppUserInfoByInfoId -> success:" + success + " response:" + response);
                            if (success && response != null) {
                                showUserInfo(response);
                            } else {
                                showUserInfo(null);
                            }
                        }
                    });
        }
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
            case R.id.tx_personal_birthday:
                DialogFragment dialogfragment = new PersonalDatePickerDialog();
                dialogfragment.show(getFragmentManager(), "Theme");
                break;
            case R.id.btn_personal_save:
                final String name = ((EditText) findViewById(R.id.et_personal_name)).getText().toString();
                final String age = ((EditText) findViewById(R.id.et_personal_age)).getText().toString();
                final String sex = ((RadioButton) findViewById(R.id.rb_personal_sex_male)).isChecked() ? "men" : "female";
                final String birthday = ((TextView) findViewById(R.id.tx_personal_birthday)).getText().toString();
                final String address = ((EditText) findViewById(R.id.et_personal_address)).getText().toString();

                AppNetTcpComm.getUser().createUserOrUpdateByApp(
                        phone,
                        password,
                        name,
                        sex,
                        birthday,
                        age,
                        address,
                        new AppNetTcpCommListener<String>() {
                            @Override
                            public void onResponse(boolean success, String response) {
                                L.e("createUserByApp -> success: " + success + " response:" + response);
                                if (success) {
                                    if (register) {
                                        MobclickEvent.onEvent(AeroCardioPersonalActivity.this,
                                                MobclickEvent.EventId_UserSignUp);
                                    }
                                    User.getUser().reset(response, "");
                                    goMain();
                                } else {
                                    Toast.makeText(AeroCardioPersonalActivity.this,
                                            R.string.http_conn_net, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case R.id.btn_personal_cancel:
                onBackPressed();
                break;
        }
    }

    private void showUserInfo(JSONObject response) {
        if (response == null) {
            response = new JSONObject();
        }
        ((EditText) findViewById(R.id.et_personal_name)).setText(response.optString("realName", ""));

        String age = response.optString("age", "");
        String birthdate = response.optString("birthdate",
                DateFormats.YYYY_MM_DD.format(new Date()));

        if (TextUtils.isEmpty(age) && birthdate.length() >= 10) {
            try {
                int year = Integer.valueOf(birthdate.substring(0, 4));
                ((EditText) findViewById(R.id.et_personal_age)).setText(
                        String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - year));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                ((EditText) findViewById(R.id.et_personal_age)).setText(age);
            }
        } else {
            ((EditText) findViewById(R.id.et_personal_age)).setText(age);
        }

        if (response.optString("sex", "men").equals("men")) {
            ((RadioButton) findViewById(R.id.rb_personal_sex_male)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rb_personal_sex_female)).setChecked(true);
        }

        ((TextView) findViewById(R.id.tx_personal_birthday)).setText(birthdate);

        ((EditText) findViewById(R.id.et_personal_phonenum)).setText(phone);

        ((EditText) findViewById(R.id.et_personal_address)).setText(response.optString("address", ""));
    }

    public static class PersonalDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            try {
                TextView textView = (TextView) getActivity().findViewById(R.id.tx_personal_birthday);
                calendar.setTime(DateFormats.YYYY_MM_DD.parse(textView.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return new DatePickerDialog(getActivity(),
                        android.R.style.Theme_DeviceDefault_Light_Dialog_Alert, this, year, month, day);
            }
            return new DatePickerDialog(getActivity(), 0, this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TextView textView = (TextView) getActivity().findViewById(R.id.tx_personal_birthday);
            textView.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

            textView = (TextView) getActivity().findViewById(R.id.et_personal_age);
            textView.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - year));
        }
    }

    private void goMain() {
        if (User.getUser().hasPrevUserDevice()) {
            startActivity(new Intent(this, AeroCardioActivity.class));
        } else {
            startActivity(new Intent(this, AeroCardioSettingActivity.class));
        }
        finish();
    }

}