package com.uteamtec.heartcool.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.adapter.ContactAdapter;
import com.uteamtec.heartcool.model.Contact;
import com.uteamtec.heartcool.model.User;
import com.uteamtec.heartcool.utils.ApiUrl;
import com.uteamtec.heartcool.utils.NetWorkUtils;
import com.uteamtec.heartcool.utils.SiatuationAndTime;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class EmerContactActivity extends BaseActivity implements View.OnClickListener {
    private ListView lv_contactAdd;//添加联系人列表
    private TextView tv_add;//添加联系人
    private ImageView iv_add_close;//添加联系人关闭
    private View addView;//添加联系人布局
    private EditText et_name, et_phone;//添加联系人dialog的姓名，电话，email
    private String name, phone;//姓名，手机号码，邮箱
    private ArrayList<Contact> al_coantact;//联系人集合
    private Contact contact = null;//联系人实体类
    private ContactAdapter ca_adapter;//联系人适配器
    private Button bt_sure, bt_add_sure;//确定，添加联系人确认
    //private View view_contact_add;//联系人添加布局
    private Dialog dialoges;
    private LinearLayout ll_return, ll_add, ll_dialog_return;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emer_contact);
        initUI();
        getData();//加载数据

    }

    //初始化控件
    private void initUI() {
        lv_contactAdd = (ListView) findViewById(R.id.activity_emerContact_lv_lvEmerContact);//添加联系人列表
        bt_sure = (Button) findViewById(R.id.activity_emerContact_bt_sure);//确定
        ll_return = (LinearLayout) findViewById(R.id.activity_emerContact_ll_return);//返回
        ll_add = (LinearLayout) findViewById(R.id.activity_emerContact_ll_addContact);//添加新的联系人
        ll_return.setOnClickListener(this);
        ll_add.setOnClickListener(this);
        bt_sure.setOnClickListener(this);
        dialoges = new Dialog(this, R.style.Transparent);
        addView = LayoutInflater.from(this).inflate(R.layout.emer_contact_add, null);//添加联系人布局
        initDialogUI();  //初始化dialog的UI
        dialoges.setContentView(addView);//添加进dailog
        bt_add_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                phone = et_phone.getText().toString();
                if (!SiatuationAndTime.checkMobileNumber(phone)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.addEmerPhone), Toast.LENGTH_SHORT).show();
                } else {
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)) {//数据不为空才能保存
                        addData(name, phone);
                        dialoges.dismiss();
                    } else {
                        Toast.makeText(EmerContactActivity.this, getString(R.string.inputAddEmerContact), Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        iv_add_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialoges.dismiss();
            }
        });
        ll_dialog_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialoges.dismiss();
            }
        });

    }

    //监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_emerContact_ll_addContact://添加联系人
                Log.i("info", "-----info--" + 3);
                dialoges.show();
                break;
            case R.id.activity_emerContact_ll_return:
                Log.i("info", "-----info--" + 1);
                onBackPressed();
                Log.i("info", "-----info--" + 2);
                break;
            case R.id.activity_emerContact_bt_sure://确定
                Intent intent = new Intent(EmerContactActivity.this, SettingActivity.class);//跳转到设置页面
                startActivity(intent);
                finish();//结束activity
                break;
        }
    }

    //初始化dialog的UI

    private void initDialogUI() {
        ll_dialog_return = (LinearLayout) addView.findViewById(R.id.emer_contact_add_ll_return);//返回
        et_name = (EditText) addView.findViewById(R.id.emer_contact_add_et_name);//姓名
        et_phone = (EditText) addView.findViewById(R.id.emer_contact_add_et_phone);//手机号
        // et_email = (EditText) addView.findViewById(R.id.emer_contact_add_et_email);//邮箱
        bt_add_sure = (Button) addView.findViewById(R.id.emer_contact_add_bt_sure);//确定
        iv_add_close = (ImageView) addView.findViewById(R.id.emer_contact_add_iv_close);//关闭
    }

    //加载联系人数据
    private void getData() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            FinalHttp fh = new FinalHttp();
            AjaxParams params = new AjaxParams();
            params.put("uid", User.getInstance().getUid());
            fh.post(ApiUrl.GET_CONTACTS, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);

                    Log.i("contact", "-----contact--" + s);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(s);
                        String cbm = object.getString("cbm");
                        if (cbm.equals("OK")) {
                            al_coantact = jsonData(object);
                            ca_adapter = new ContactAdapter(al_coantact, getApplicationContext());
                            lv_contactAdd.setAdapter(ca_adapter);//为适配器设置数据
                            ca_adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(EmerContactActivity.this, object.getString("cms"), Toast.LENGTH_SHORT).show();
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

    /**
     * 解析数据
     *
     * @param
     * @return
     */
    private ArrayList<Contact> jsonData(JSONObject object) {
        try {
            /*JSONObject object = new JSONObject(result);
            String cbm=object.getString("cbm");*/
            JSONArray array = object.getJSONArray("cms");
            al_coantact = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                contact = new Contact();
                JSONObject obj = array.getJSONObject(i);
                contact.setId(obj.getInt("ID"));
                contact.setName(obj.getString("NAME"));
                contact.setPhone(obj.getString("PHONE"));
                contact.setCondition(obj.getString("OPT"));

                al_coantact.add(contact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return al_coantact;
    }

    /**
     * 添加联系人
     *
     * @param name  姓名
     * @param phone 电话
     */
    private void addData(String name, String phone) {
        if (NetWorkUtils.isNetworkConnected(this)) {
            FinalHttp fh = new FinalHttp();
            AjaxParams params = new AjaxParams();
            params.put("uid", User.getInstance().getUid());
            params.put("uname", name);
            params.put("uphone", phone);
            fh.post(ApiUrl.ADD_CONTACTS, params, new AjaxCallBack<String>() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    Log.i("info", "------ccc---message" + s);
                    getData();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.netWrong), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EmerContactActivity.this, SettingActivity.class);
        finish();
        startActivity(intent);
    }
}
