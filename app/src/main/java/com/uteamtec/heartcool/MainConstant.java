package com.uteamtec.heartcool;

/**
 * Created by wd
 */
public final class MainConstant {
    //queue size
    public final static int DEFAULT_QUEUE_SIZE = 1024;

    //TCP服务器地址, 用户建立socket之后调用id和key进行登陆认证，这个认证和用户登录是两个流程
    public final static String TCP_SERVER_HOST = "120.27.135.115";
    //    public final static String TCP_SERVER_HOST = "112.124.70.101";
    public final static int TCP_SERVER_PORT = 9001;

}
