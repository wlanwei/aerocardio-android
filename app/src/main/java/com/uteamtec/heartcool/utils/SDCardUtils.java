package com.uteamtec.heartcool.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * sdcard操作工具
 */
public class SDCardUtils {
	// SDCard的根目录
	public static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	// 配置文件路径
	public static final String FILEPATH = "/AeroCardio/";
	// 监测蓝牙设备是否支持
	public static boolean checkSDCardAvailable() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}
	// 缓存文件根路径
	public static File getExternalFilesDirPath(Context context){
		return context.getExternalFilesDir(null);
	}
}
