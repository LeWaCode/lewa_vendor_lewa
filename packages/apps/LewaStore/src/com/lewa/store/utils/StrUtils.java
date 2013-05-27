package com.lewa.store.utils;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class StrUtils {

	private static String TAG = StrUtils.class.getSimpleName();
	
	public static void doReverseOrder(List<String> list,int num){
		List<Integer> tempList=new ArrayList<Integer>();
		int a=0;
		for(int i=list.size()-1;i>=0;i--){
			if(a!=num){
				int b=Integer.parseInt(list.get(i));
				tempList.add(b);
				list.remove(i);
				a++;				
			}else{
				break;
			}			
		}
		for(int j=0;j<tempList.size();j++){
			list.add(tempList.get(j)+"");
		}
	}
	
	public static boolean isChineseStr(String str){
		boolean flag=false;
		try {
			if(str.getBytes().length!=str.length()){
				flag=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	// 根据地址获得文件名称
	public static String getFileNameFromUrl(String url) {
		String fileName = url.substring(url.lastIndexOf("\\/") + 1);
		return fileName;
	}
	
	public static int getRandomNumbers(){
		return (int) (Math.random()*100000+1);
	}
	
	/**
	 * 获得本地存储的文件名
	 * @param localFilePath
	 * @return
	 */
	public static String getLocalFileName(String localFilePath) {
		if (null != localFilePath) {
			return localFilePath.substring(localFilePath.lastIndexOf("/") + 1);
		}
		return null;
	}

	public static String getLocalApkName(String localfile) {
		String filename = "";
		try {
			filename = localfile.substring(localfile.lastIndexOf("/") + 1,
					localfile.length());
			filename = filename.replace("apk", "");
			filename = filename.replace(" ", "");
			filename = filename.replace(".", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filename;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileNameFromLocalPath(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

	// 处理版本
	public static int getAppVersion(String versionName) {
		String[] args = versionName.split(".");// 这里需要用到Version Code
		int total = 0;
		if (args.length > 0) {
			int length = args.length;
			for (int i = 0; i < length; i++) {
				total += Integer.parseInt(args[i]);
			}
		}
		return total;
	}

	// 根据动态Url,获取package id
	public static int getPackageIntId(String url) {

		if (null != url) {
			// Log.d(TAG,url);
			String[] array = url.split("\\/");
			int length = array.length;
			for (int i = 0; i < length; i++) {
				// System.out.println(array[i]);
			}
			try {
				return Integer.parseInt(array[5]);
			} catch (ArrayIndexOutOfBoundsException e) {
				// TODO: handle exception
				e.printStackTrace();
				return -1;
			}
		}
		return -1;
	}

	public static String getFileMbSize(long bytes) {
		DecimalFormat df = new DecimalFormat("###.##");
		float f;
		if (bytes < 1024 * 1024) {
			f = (float) ((float) bytes / (float) 1024);
			return df.format(new Float(f).doubleValue()) + " KB";
		} else {
			f = (float) ((float) bytes / (float) (1024 * 1024));
			return df.format(new Float(f).doubleValue()) + " MB";
		}
	}

	public static String replaceSpace(String str) {
		if (null != str) {
			return str.replaceAll(" ", "");
		}
		return "";
	}

	public static String replaceBr(String str) {
		if (null != str) {
			return str.trim().replace("\n", "").replace("\t", "");
		}
		return "";
	}
	
	public static void sleep(){
		int a=0;
		long t1=System.currentTimeMillis();
		for(int i=0;i<1999999;i++){
		   a+=i;
		}
		Log.e(TAG,"sleep time=="+(System.currentTimeMillis()-t1)+" ms");
	}

	/**
	 * 字符串编码转换的实现方法
	 * 
	 * @param str
	 *            待转换编码的字符串
	 * @param newCharset
	 *            目标编码
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String changeCharset(String str, String newCharset) {
		if (str != null) {
			// 用默认字符编码解码字符串。
			byte[] bs = str.getBytes();
			// 用新的字符编码生成字符串
			try {
				return new String(bs, newCharset);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
