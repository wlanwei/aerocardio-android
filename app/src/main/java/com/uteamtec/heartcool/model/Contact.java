package com.uteamtec.heartcool.model;

import java.io.Serializable;

/**
 * 紧急联系人表
 * Created by Administrator on 2016/3/15 0015.
 */
public class Contact implements Serializable{
    public int id;
    private String name;//姓名
    private String phone;//电话
    private String email;//邮箱
    private String condition;//状态

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }
}
