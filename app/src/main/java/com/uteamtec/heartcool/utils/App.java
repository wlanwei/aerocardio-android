
/** 
 * 南京熙健 ecg 开发支持库 
 * Copyright (C) 2015 mhealth365.com All rights reserved.
 * create by lc  2015年6月16日 上午9:56:01 
 */ 
package com.uteamtec.heartcool.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class App extends Application {
	private SharedPreferences sf;


	@Override
	public void onCreate() {
		super.onCreate();
		sf=getSharedPreferences("info",Context.MODE_PRIVATE);
		boolean isChinese = sf.getBoolean("isChinese",false);
		boolean isEnglish = sf.getBoolean("isEnglish",false);
		boolean isArab = sf.getBoolean("isArab",false);
		if(isChinese==true && isEnglish==false && isArab==false){
			setLanuage(Locale.SIMPLIFIED_CHINESE);//设置中文
		}else if(isChinese==false && isEnglish==true && isArab==false){
			setLanuage(Locale.ENGLISH);//设置英文
		}else if(isChinese==false && isEnglish==false && isArab==true){
			setLanuage(new Locale("ar"));//设置阿拉伯语;
		}
	}
	/**
	 * 设置语言
	 * @param lanuage
	 */
	private void setLanuage(Locale lanuage) {
		Resources resources = getResources();//获得res资源对象
		Configuration configuration = resources.getConfiguration();//获得设置对象
		DisplayMetrics dm = resources.getDisplayMetrics();//获得屏幕参数：主要是分辨率，像素等
		configuration.locale = lanuage;
		resources.updateConfiguration(configuration, dm);
	}
}
