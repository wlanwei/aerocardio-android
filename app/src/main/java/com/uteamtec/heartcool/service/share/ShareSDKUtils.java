package com.uteamtec.heartcool.service.share;

import android.content.Context;

import com.uteamtec.heartcool.R;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by wd
 */
public final class ShareSDKUtils {

    public static void shareContent(Context context, String content) {
        if (context == null) {
            return;
        }
        ShareSDK.initSDK(context);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题：微信、QQ（新浪微博不需要标题）
        oks.setTitle(context.getString(R.string.product));  //最多30个字符

        // text是分享文本：所有平台都需要这个字段
        // oks.setText("哒哒影像~http://www.iinda.cn/");  //最多40个字符
        oks.setText(content);
        // imagePath是图片的本地路径：除Linked-In以外的平台都支持此参数
        //oks.setImagePath(Environment.getExternalStorageDirectory() + "/meinv.jpg");//确保SDcard下面存在此张图片

        //网络图片的url：所有平台
        oks.setImageUrl("http://www.lgstatic.com/thumbnail_300x300/image1/M00/00/2B/Cgo8PFTUXHGAFoeQAABomSrHT7w969.png");//网络图片rul

        // url：仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://www.b2b168.com/c168-12989004.html");   //网友点进链接后，可以看到分享的详情

        // Url：仅在QQ空间使用
        oks.setTitleUrl("http://www.b2b168.com/c168-12989004.html/");  //网友点进链接后，可以看到分享的详情

        // 启动分享GUI
        oks.show(context);
    }

}
