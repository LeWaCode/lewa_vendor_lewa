package com.lewa.launcher.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

public class VersionUtil {
	public static final String INFO_SERVER_CLOSED = "server closed";  
	private static final String url = "http://www.lewaos.com/release/getupdate.php";
	private static String remoteVersion = "";
	private static String apkUrl = "";
	private static String absoluteVersion = "";
	private static String note;
	
	public static String getNote() {
		return note;
	}
	public static String getUrl() {
		return apkUrl;
	}
	public static String getApkName() {
		return !"".equals(apkUrl)?apkUrl.substring(apkUrl.lastIndexOf("/")+1):"";
	}
	public static String getRemoteVersion() {
		return remoteVersion;
	}

	public static String getLocalVersion() {
		return absoluteVersion;
	}

	public static String getVersionAbstract(String absoluteVersion) {
		int first = absoluteVersion.indexOf(".");
		return absoluteVersion.substring(0,absoluteVersion.indexOf(".", first));
	}

	public static Boolean checkNew(Activity activity) throws Exception{
		Boolean result = true; 		
		try {
			String versionStr = httpPostResult();
			if (versionStr==null||"null".equals(versionStr.trim())||"".equals(versionStr)){
				throw new IllegalStateException(VersionUtil.INFO_SERVER_CLOSED);
			} else {
				remoteVersion = new JSONObject(versionStr).getString("ver");
				apkUrl = new JSONObject(versionStr).getString("url");
				note = new JSONObject(versionStr).getString("note");
				absoluteVersion = activity
				.getString(com.lewa.launcher.R.string.adw_version);
				absoluteVersion = absoluteVersion==null?"":absoluteVersion;
				return !remoteVersion.equals(absoluteVersion);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(e instanceof IllegalStateException){
				result = null;
				throw e;
			}else{
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	 public static String httpPostResult(){
		 	String result = "";
	        try {
	        	HttpURLConnection conn =  (HttpURLConnection) new URL(url).openConnection();
	        	conn.setReadTimeout(5000);
	            Log.i("test", "==="+String.valueOf(conn.getContentLength()));
	            InputStream is = conn.getInputStream();
	            result += convertStreamToString(is);
	            
	            
	        } catch (Exception e) {
	            e.printStackTrace();   
	        }
			return result;
	    }

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
}

	 
	    