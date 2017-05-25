package com.uteamtec.heartcool.utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * 对IO流操作的工具类
 */

public class IOUtils {
	// 关闭URLConnection
	public static void closeConnection(URLConnection conn) {
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}
	// 判断closeable是否为null或已被关闭，并关闭closeable对象
	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {}
	}
}

