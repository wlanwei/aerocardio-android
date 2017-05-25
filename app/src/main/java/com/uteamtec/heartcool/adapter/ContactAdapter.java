package com.uteamtec.heartcool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.model.Contact;
import com.uteamtec.heartcool.model.User;
import com.uteamtec.heartcool.utils.ApiUrl;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.util.List;

/**
 * Created by Administrator on 2016/3/15 0015.
 */
public class ContactAdapter extends BaseAdapter {
    private List<Contact> al_contact;
    private int flagCondition;//设置联系人修改的状态
    private Context context;

    public ContactAdapter(List<Contact> al_contact, Context context) {

        this.al_contact = al_contact;
        this.context = context;
    }

    public void addAll(List<Contact> contacts) {//添加数据进紧急联系人
        this.al_contact.addAll(contacts);
        notifyDataSetChanged();
    }

    public void clean() {//清除数据
        this.al_contact.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return al_contact.size();
    }

    @Override
    public Object getItem(int position) {
        return al_contact.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ContactHolder contactHolder = null;
        if (contactHolder == null) {
            contactHolder = new ContactHolder();
            //初始化控件
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.emer_contact_item, null);
            contactHolder.tv_name = (TextView) convertView.findViewById(R.id.emer_contact_tv_item_name);
            contactHolder.tv_phone = (TextView) convertView.findViewById(R.id.emer_contact_tv_item_phone);
           // contactHolder.tv_email = (TextView) convertView.findViewById(R.id.emer_contact_tv_item_email);
            contactHolder.cb_select = (CheckBox) convertView.findViewById(R.id.emer_contact_tv_item_checkBox);
            convertView.setTag(contactHolder);//设置tag
        } else {
            contactHolder = (ContactHolder) convertView.getTag();//获取tag
        }
        final Contact contact = al_contact.get(position);//获取位置
        //设置具体数据
        contactHolder.tv_name.setText(contact.getName());
        contactHolder.tv_phone.setText(contact.getPhone());
      //  contactHolder.tv_email.setText(contact.getEmail());
        String condition = contact.getCondition();
        final int id = contact.getId();//联系人id
        if (condition.equals("0")) {//0为未勾选
            contactHolder.cb_select.setChecked(false);
        } else if (condition.equals("1")) {//1为勾选
            contactHolder.cb_select.setChecked(true);
        }
        contactHolder.cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {//选择矿的点击事件可以改变勾选状态
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    flagCondition = 1;//1为勾选状态
                } else {
                    flagCondition = 0;//0为未勾选状态
                }
                updateContact(User.getInstance().getUid(), id, flagCondition);//修改状态

            }

        });


        return convertView;
    }

    class ContactHolder {
        TextView tv_name, tv_phone, tv_email;//姓名，电话，邮件
        CheckBox cb_select;//是否选择
    }

    /**
     * @param uid 用户id
     * @param id  联系人id
     * @param opt 选择状态
     */
    private void updateContact(String uid, int id, int opt) {
        FinalHttp fh = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("uid", uid);
        params.put("id", Integer.toString(id));
        params.put("opt", Integer.toString(opt));
        fh.post(ApiUrl.SELECT_CONTACTS, params, new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);

            }
        });


    }

}
