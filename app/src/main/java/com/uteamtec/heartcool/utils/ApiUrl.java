package com.uteamtec.heartcool.utils;

/**
 * Created by admin on 2016/3/22.
 */
public class ApiUrl {

//    public final static String SERVICE_PORT = "http://120.27.29.91:8088/"; //端口地址
//    public final static String SERVICE_ADDRESS = SERVICE_PORT+"ecg/interface/"; //服务器地址
//    public final static String  LOGIN= SERVICE_ADDRESS+"User/Login.php"; //登录地址
//    public final static String  REGISTER= SERVICE_ADDRESS+"User/Register.php"; //注册地址
//    public final static String  VERIFICATION= SERVICE_ADDRESS+"user/mailtoReg.php"; //注册验证码接口
//    public final static String  MAILTO= SERVICE_ADDRESS+"user/mailto.php"; //注册验证码接口
//    public final static String  VERIFYCODE= SERVICE_ADDRESS+"user/verifyCode.php"; //忘记密码验证码校对接口
//    public final static String  ALTERPWD= SERVICE_ADDRESS+"user/AlterPwd.php"; //设置新密码接口
//    public final static String  RECORD= SERVICE_ADDRESS+"record/Record.php"; //监测收据存入接口
//    public final static String  PHONETOREG= SERVICE_ADDRESS+"user/phonetoReg.php"; //电话号码入接口
//    public final static String  PHONECODE= SERVICE_ADDRESS+"user/PhoneCode.php"; //找回密码发送手机验证码接口
//    public final static String  UPLOADFILE= SERVICE_ADDRESS+"uploadfile/"; //上传文件接口
//
//
//    public static final String emerSend_SEARCH=SERVICE_PORT+"ecg/interface/setting/SendWay.php";//紧急发送方式显示地址
//    public static final String emerSend_UPDATE=SERVICE_PORT+"ecg/interface/setting/SetSendWay.php";//紧急发送方式修改地址
//    public static final String emerContact_SEARCH=SERVICE_PORT+"ecg/interface/setting/GetContacts.php";//紧急联系人显示地址
//    public static final String emerContact_ADDCONTACT=SERVICE_PORT+"ecg/interface/setting/AddContacts.php";//紧急联系人添加地址
//    public static final String emerContact_UPCONTACT=SERVICE_PORT+"ecg/interface/setting/Select_Contacts.php";//紧急联系人修改地址
//    public static final String LANGUAGE_SHOW=SERVICE_PORT+"ecg/interface/setting/Lang.php";//语言显示地址
//    public static final String LANGUAGE_UPLANGUAGE=SERVICE_PORT+"ecg/interface/setting/SetLang.php";//语言修改地址


    //    public static final String PERSONAL_INFO=SERVICE_PORT+"ecg/interface/User/completeMessage.php";//个人信息完善
//    public static final String EMER_EMAIL=SERVICE_PORT+"ecg/interface/User/MailToContacts.php";//紧急情况邮件发送
    public static String msg = "";//setting返回界面的信息提示
//    public static String login="";//由注册界面跳转到登录界面信息提示
//    //http://120.27.29.91:8088/ecg/interface/record/SearchRecord.php?uid&&jlsj=2016-05-31


    public final static String SERVICE_PORT = "http://120.27.29.91:8088/"; //端口地址

    public final static String SERVICE_ADDRESS = SERVICE_PORT + "ecg/Test/"; //服务器地址

    public static final String DATE_SHOW = SERVICE_ADDRESS + "record/GetRecord.php";//记录日期显示地址

    public static final String DATE_RECORD = SERVICE_ADDRESS + "ecg/interface/record/SearchRecord.php";//搜索记录地址

    public final static String PHONE_CODE = SERVICE_ADDRESS + "User/PhoneCode.php"; //手机验证码接口

    public final static String VERIFY_CODE = SERVICE_ADDRESS + "User/VerifyCode.php"; //验证码校验接口

    public final static String REGISTER = SERVICE_ADDRESS + "User/Register.php"; //注册接口

    public final static String ALTER_PWD = SERVICE_ADDRESS + "User/AlterPwd.php"; //修改密码接口

    public final static String LOGIN = SERVICE_ADDRESS + "User/Login.php"; //登录接口

    public final static String SAVE_RECORD = SERVICE_ADDRESS + "Record/SaveRecord.php"; //监测收据存入接口

    // public final static String GET_RECORD = SERVICE_ADDRESS + "Record/GetRecord.php"; //查询监测记录接口

    public final static String ADD_CONTACTS = SERVICE_ADDRESS + "setting/AddContacts.php"; //增加联系人接口

    public final static String GET_CONTACTS = SERVICE_ADDRESS + "setting/GetContacts.php"; //查看联系人接口

    public final static String SELECT_CONTACTS = SERVICE_ADDRESS + "setting/Select_Contacts.php"; //选择联系人接口

    public final static String SEND_WAY = SERVICE_ADDRESS + "setting/SendWay.php"; //显示紧急情况发送接口

    public final static String SET_SENDWAY = SERVICE_ADDRESS + "setting/SetSendWay.php"; //设置紧急情况发送接口


}
