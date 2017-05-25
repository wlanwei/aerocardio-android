package com.uteamtec.heartcool.views;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uteamtec.heartcool.BaseActivity;
import com.uteamtec.heartcool.MainMgrService;
import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.utils.L;
import com.uteamtec.heartcool.views.widget.CheckIconView;
import com.uteamtec.heartcool.views.widget.RunningIconView;

// 登录界面
public class LoginActivity extends BaseActivity {
    private Button btnLogin;
    private CheckIconView checkIconView;
    private RunningIconView runningIconView;

    private EditText inputName;
    private EditText inputPassWord;
    private String loginResult;

    private MainMgrService mainMgrService;
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainMgrService = ((MainMgrService.MainMgrBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainMgrService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreated(R.layout.activity_login);

    }

    @Override
    protected void init() {
        Intent intent = new Intent(LoginActivity.this, MainMgrService.class);
        bindService(intent, serviceConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void findView() {
        btnLogin = (Button) findViewById(R.id.login);
        inputName = (EditText) findViewById(R.id.et_user_name);
        inputPassWord = (EditText) findViewById(R.id.et_pass_word);
        checkIconView = (CheckIconView) findViewById(R.id.loginCheckIcon);
        runningIconView = (RunningIconView) findViewById(R.id.loginRunningIcon);

    }

    @Override
    protected void bindListener() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputName.getText().toString().length() == 0 || inputPassWord.getText().toString().length() == 0) {
                    Toast.makeText(LoginActivity.this, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                L.i("返回值" + loginResult);


//                Map<String, Device> boundedDevices = new HashMap<>();
//                byte idBytes[] = null;
//                byte keyBytes[] = null;
//                idBytes = "00000013".getBytes();
//                keyBytes = "0000000000000000".getBytes();
//                mainMgrService.resetUser(idBytes, keyBytes, boundedDevices, null);


//                Config.putString(ConfigType.User,
//                        Config.PREF_USER_NAME, inputName.getText().toString());
//
                UIHelper.goSettingActivity(LoginActivity.this);


//                AlphaAnimation anime = new AlphaAnimation(1.0f, 0.0f);
//                anime.setDuration(400);
//                anime.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) { }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                        btnLogin.setVisibility(View.INVISIBLE);
//                        runningIconView.setVisibility(View.VISIBLE);
//                        ObjectAnimator.ofFloat(runningIconView, "alpha", 0.0f, 1.0f).setDuration(400).start();
//                        runningIconView.startInitAnimation();
//
//                        loginResult = null;
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                L.i("<UI> ui thread");
//                                loginResult = HttpService.login(inputName.getText().toString(), inputPassWord.getText().toString(), getApplicationContext());
//                                L.i("返回值"+loginResult);
//                                if (loginResult != null) {
//                                    SharedPerferenceUtils preferenceUtil = new SharedPerferenceUtils(getApplicationContext());
//
//                                    JSONObject jsonObj;
//                                    JSONTokener jsonTokener = new JSONTokener(loginResult);
//                                    String id = null;
//                                    String key = null;
//                                    Map<String, Device> boundedDevices = new HashMap<>();
//                                    try {
//                                        jsonObj = (JSONObject) jsonTokener.nextValue();
//
//                                        id = jsonObj.getString("infoId");
//                                        key = jsonObj.getString("infoKey");
//                                        L.i("<UI> get id = " + id + "key = " + key);
//                                        JSONArray jArray = jsonObj.getJSONArray("BoundedDevices");
//                                        for (int m = 0; m < jArray.length(); m ++){
//                                            JSONObject jObj = jArray.getJSONObject(m);
//                                            L.i("<JSON> jObj = " + jObj.toString(4));
//                                            String devId = jObj.getString("deviceId");
//                                            L.i("<JSON> dev id = " + devId);
//                                            String devKey = jObj.getString("deviceKey");
//                                            L.i("<JSON> dev key = " + devKey);
//                                            String devKeyPair = jObj.getString("deviceKeyPair");
//                                            L.i("<JSON> dev keyPair = " + devKeyPair);
//                                            String devMacAddr = jObj.getString("deviceMacAddr");
//                                            L.i("<JSON> dev macAddr = " + devMacAddr);
//                                            int devType = jObj.getInt("deviceType");
//                                            L.i("<JSON> dev type = " + Integer.toString(devType));
//                                            Device dev = new Device(devId.getBytes(), devType);
//                                            dev.setKey(devKey.getBytes());
//                                            dev.setKeyPair(devKeyPair.getBytes());
//                                            dev.setMacAddr(devMacAddr);
//                                            boundedDevices.put(devId, dev);
//                                        }
//
//                                        byte idBytes[] = null;
//                                        byte keyBytes[] = null;
//                                        if (id != null && key != null) {
//                                            idBytes = id.getBytes();
//                                            keyBytes = key.getBytes();
//                                            mainMgrService.resetUser(idBytes, keyBytes, boundedDevices, null);
//                                            preferenceUtil.setPreferenceValues(AeroCardioApp.PREF_USER_NAME, inputName.getText().toString());
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    inputPassWord.setText("");
//                                                }
//                                            });
//                                            UIHelper.goSettingActivity(LoginActivity.this);
//                                        }
//                                        else {
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    btnLogin.setAlpha(0);
//                                                    btnLogin.setVisibility(View.VISIBLE);
//                                                    runningIconView.setVisibility(View.INVISIBLE);
//                                                    ObjectAnimator animeAlpha = ObjectAnimator.ofFloat(btnLogin, "alpha", 0.0f, 1.0f);
//                                                    animeAlpha.setDuration(400);
//                                                    animeAlpha.setStartDelay(2000);
//                                                    Toast.makeText(LoginActivity.this, "登陆失败, 无账号密码", Toast.LENGTH_SHORT).show();
//                                                    animeAlpha.start();
//                                                }
//                                            });
//                                        }
//                                    }
//                                    catch (JSONException e) {
//                                        L.i("<UI> login failed, user name or passwd incorrect");
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                btnLogin.setAlpha(0);
//                                                btnLogin.setVisibility(View.VISIBLE);
//                                                runningIconView.setVisibility(View.INVISIBLE);
//                                                ObjectAnimator animeAlpha = ObjectAnimator.ofFloat(btnLogin, "alpha", 0.0f, 1.0f);
//                                                animeAlpha.setDuration(400);
//                                                animeAlpha.setStartDelay(2000);
//                                                Toast.makeText(LoginActivity.this, "登陆失败, json解析问题", Toast.LENGTH_SHORT).show();
//                                                animeAlpha.start();
//                                            }
//                                        });
//                                    }
//                                }
//                                else {
//                                    L.i("<UI> login failed, network failed");
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            btnLogin.setAlpha(0);
//                                            btnLogin.setVisibility(View.VISIBLE);
//                                            runningIconView.setVisibility(View.INVISIBLE);
//                                            ObjectAnimator animeAlpha = ObjectAnimator.ofFloat(btnLogin, "alpha", 0.0f, 1.0f);
//                                            animeAlpha.setDuration(400);
//                                            animeAlpha.setStartDelay(2000);
//                                            Toast.makeText(LoginActivity.this, "登陆失败, 网络故障", Toast.LENGTH_SHORT).show();
//                                            animeAlpha.start();
//                                        }
//                                    });
//                                }
//                            }
//                        }).start();
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) { }
//                });


//                btnLogin.startAnimation(anime);
            }
        });
    }

    @Override
    public void unregisterService() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mainMgrService != null && serviceConn != null) {
            unbindService(serviceConn);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
