package com.uteamtec.heartcool.utils;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件的IO流操作
 */
public class FileUtils {

	public static void writeData(Context context, String fileName,final byte[] data) {
		if (SDCardUtils.checkSDCardAvailable()){
			// 文件根路径
			File fileRootPath = new File(SDCardUtils.getExternalFilesDirPath(context), SDCardUtils.FILEPATH);
			if (!fileRootPath.exists()){
				fileRootPath.mkdirs();
			}

			// 文件路径
			File filePath = new File(SDCardUtils.getExternalFilesDirPath(context), SDCardUtils.FILEPATH + fileName);
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(filePath, true);
				outStream.write(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}finally {
				IOUtils.closeQuietly(outStream);
			}

		}else{
			ToastUtil.showLong(context, "该手机不支持SDCard");
		}

	}

	public static void writeData(String fileName, byte[] data) {
		if (SDCardUtils.checkSDCardAvailable()){
			// 文件根路径
			File fileRootPath = new File(SDCardUtils.PATH+ SDCardUtils.FILEPATH);
			if (!fileRootPath.exists()){
				fileRootPath.mkdirs();
			}

			// 文件路径
			File filePath = new File(SDCardUtils.PATH+ SDCardUtils.FILEPATH + fileName);
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(filePath, true);
				outStream.write(data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}finally {
				IOUtils.closeQuietly(outStream);
			}
		}
	}

	public static byte[] readData(Context context, String fileName){
		byte[] data = null;
		if (SDCardUtils.checkSDCardAvailable()){
			// 文件路径
			File filePath = new File(SDCardUtils.getExternalFilesDirPath(context), SDCardUtils.FILEPATH + fileName);
			if (filePath.exists()){
				FileInputStream inStream = null;
				ByteArrayOutputStream stream = null;
				try {
					inStream = new FileInputStream(filePath);
					stream = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024 * 100];
					int length = -1;
					while ((length = inStream.read(buffer)) != -1) {
						stream.write(buffer, 0, length);
					}
					data = stream.toByteArray();
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}finally {
					if (stream != null)IOUtils.closeQuietly(stream);
					if (inStream != null)IOUtils.closeQuietly(inStream);
				}
			}
		}else{
			ToastUtil.showLong(context, "该手机不支持SDCard");
		}
		return data;
	}

	public static byte[] readData(String fileName){
		byte[] data = null;
		if (SDCardUtils.checkSDCardAvailable()){
			// 文件路径
			File filePath = new File(SDCardUtils.PATH+ SDCardUtils.FILEPATH + fileName);
			if (filePath.exists()){
				FileInputStream inStream = null;
				ByteArrayOutputStream stream = null;
				try {
					inStream = new FileInputStream(filePath);
					stream = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024 * 100];
					int length = -1;
					while ((length = inStream.read(buffer)) != -1) {
						stream.write(buffer, 0, length);
					}
					data = stream.toByteArray();
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}catch (IOException e) {
					e.printStackTrace();
				}finally {
					if (stream != null)IOUtils.closeQuietly(stream);
					if (inStream != null)IOUtils.closeQuietly(inStream);
				}
			}
		}
		return data;
	}

	public static boolean deleteData(Context context, String fileName) {
		boolean dele = false;
		if (SDCardUtils.checkSDCardAvailable()){
			// 文件路径
			File filePath = new File(SDCardUtils.getExternalFilesDirPath(context), SDCardUtils.FILEPATH + fileName);
			if (filePath.exists() && filePath.isFile()) {
				dele = filePath.delete();
			}
		}else{
			ToastUtil.showLong(context, "该手机不支持SDCard");
		}
		return dele;
	}

	public static boolean deleteData(String fileName) {
		boolean dele = false;
		if (SDCardUtils.checkSDCardAvailable()){
			// 文件路径
			File filePath = new File(SDCardUtils.PATH+ SDCardUtils.FILEPATH + fileName);
			if (filePath.exists() && filePath.isFile()) {
				dele = filePath.delete();
			}
		}
		return dele;
	}

	public static File[] getFiles(String dirName) {
		if (SDCardUtils.checkSDCardAvailable()) {
			return new File(SDCardUtils.PATH).listFiles();
		}
		else {
			return null;
		}
	}

	public static byte[] readLine(File file) {
		return null;
	}
}
