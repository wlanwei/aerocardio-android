package com.uteamtec.heartcool.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.utils.ApiUrl;
import com.uteamtec.heartcool.utils.DataCleanMangerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_cache_title;//清除缓存,是否清除缓存
    private ImageView iv_lanuage, iv_emerContact, iv_Send, iv_about, iv_close;//语言设置，紧急联系人，紧急情况发送方式，关于我们,关闭
    private Button bt_exit, bt_cache_sure, bt_cache_cancel;//退出,确定清除缓存，取消清除缓存
    private RelativeLayout rl_lanuage, rl_personal, rl_contact, rl_send,
            rl_explain, rl_about, rl_cache;//语言选择，联系人，发送方式，使用说明，关于我们
    public static int BACK_CODE = 1; //返回键点击次数
    private View cacheView;
    private AlertDialog dialog;
    private String totalCacheSize;//缓存大小
    private LinearLayout ll_close;//关闭

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initUI();//初始化控件
        copyExplain("instruction.pdf");//复制说明书到本地
    }

    //初始化控件
    private void initUI() {
        rl_cache = (RelativeLayout) findViewById(R.id.activity_setting_rl_cache);//清除缓存
        //  iv_lanuage=(ImageView)findViewById(R.id.activity_setting_iv_lSkip);//语言跳转
        iv_emerContact = (ImageView) findViewById(R.id.activity_setting_iv_eSkip);//紧急联系人跳转
        iv_Send = (ImageView) findViewById(R.id.activity_setting_iv_sSkip);//紧急情况发送方式
        iv_about = (ImageView) findViewById(R.id.activity_setting_iv_aSkip);//关于我们
        bt_exit = (Button) findViewById(R.id.activity_setting_bt_exit);//退出设置
        iv_close = (ImageView) findViewById(R.id.activity_setting_iv_close);//关闭设置
        ll_close = (LinearLayout) findViewById(R.id.activity_setting_ll_close);//关闭
        //  rl_lanuage=(RelativeLayout)findViewById(R.id.activity_setting_rl_language);//语言选择
        rl_personal = (RelativeLayout) findViewById(R.id.activity_setting_rl_personal);//个人信息
        rl_contact = (RelativeLayout) findViewById(R.id.activity_setting_rl_contact);//联系人
        rl_send = (RelativeLayout) findViewById(R.id.activity_setting_rl_send);//发送方式
        //   rl_explain=(RelativeLayout)findViewById(R.id.activity_setting_rl_explain);//使用说明
        rl_about = (RelativeLayout) findViewById(R.id.activity_setting_rl_about);//关于我们

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);//清除缓存的dailog
        cacheView = LayoutInflater.from(this).inflate(R.layout.dialog_cache_record, null);//清除缓存的布局
        tv_cache_title = (TextView) cacheView.findViewById(R.id.cache_record_tv_deleteCache);//标题
        bt_cache_sure = (Button) cacheView.findViewById(R.id.cache_record_bt_sure);//确认清除缓存
        bt_cache_cancel = (Button) cacheView.findViewById(R.id.cache_record_bt_cancel);//取消清除缓存
        builder.setView(cacheView);//把布局嵌入dailog
        dialog = builder.create();
        rl_personal.setOnClickListener(this);// 个人信息监听
        rl_cache.setOnClickListener(this);//清除缓存监听
        rl_contact.setOnClickListener(this);//紧急联系人监听
        rl_send.setOnClickListener(this);//紧急发送方式监听
        rl_about.setOnClickListener(this);//关于我们监听
        bt_exit.setOnClickListener(this);//退出设置
        ll_close.setOnClickListener(this);//关闭设置

    }

    //各跳转按钮的监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_setting_rl_personal:// 个人信息监听
                startActivity(new Intent(SettingActivity.this, AeroCardioPersonalActivity.class));
                finish();
                break;
            case R.id.activity_setting_rl_cache:////清除缓存监听
                dialog.show();
                try {
                    totalCacheSize = DataCleanMangerUtil.getTotalCacheSize(SettingActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bt_cache_sure.setOnClickListener(new View.OnClickListener() {//确定删除缓存
                    @Override
                    public void onClick(View v) {
                        DataCleanMangerUtil.clearAllCache(SettingActivity.this);
                        Toast.makeText(SettingActivity.this, getString(R.string.delete) + totalCacheSize + getString(R.string.caches), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                });
                bt_cache_cancel.setOnClickListener(new View.OnClickListener() {//取消删除缓存
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
            case R.id.activity_setting_rl_contact://紧急联系人监听
                Intent emerContact = new Intent(SettingActivity.this, EmerContactActivity.class);//跳转到紧急联系人界面
                startActivity(emerContact);
                finish();
                break;
            case R.id.activity_setting_rl_send://紧急发送方式监听
                Intent emerSend = new Intent(SettingActivity.this, EmerSendActivity.class);//跳转到紧急情况发送方式界面
                startActivity(emerSend);
                finish();
                break;
            case R.id.activity_setting_rl_about://关于我们监听
                Intent about = new Intent(SettingActivity.this, AboutActivity.class);//跳转到关于我们界面
                startActivity(about);

                break;
            case R.id.activity_setting_bt_exit://退出设置
                Intent exit = new Intent(SettingActivity.this, AeroCardioLoginActivity.class);//跳转到关于我们界面
                startActivity(exit);
                finish();
                break;
            case R.id.activity_setting_ll_close://关闭界面
                if (ApiUrl.msg.equals("1")) {
                    Intent detection = new Intent(SettingActivity.this, DetectionsActivity.class);//跳转到监测界面
                    startActivity(detection);
                    finish();
                } else if (ApiUrl.msg.equals("2")) {
                    Intent history = new Intent(SettingActivity.this, HistoryActivity.class);//跳转到历史界面
                    startActivity(history);
                    finish();
                }
                break;
        }
    }

    /**
     * 从assert中复制使用说明书
     *
     * @param explainName
     */
    private void copyExplain(String explainName) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ecg";
        File destFile = new File(path);//要拷贝的目标地址
        if (destFile.exists()) {
            Log.i("tag", "说明书" + destFile + "已存在");
            return;
        } else {
            destFile.mkdirs();
        }
        File files = new File(destFile, explainName);
        FileOutputStream out = null;
        InputStream in = null;
        try {
            in = getAssets().open(explainName);
            out = new FileOutputStream(files);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (null != out)
                    out.close();
                if (null != in)
                    in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
