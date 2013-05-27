package com.lewa.store.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import android.util.Log;

/**
 * 
 * @author ypzhu
 *
 * 日志辅助类
 */
public class LogHelper {
	
	private static final boolean LOG_D_SWITCH = true; // the switch of debug log
	private static final boolean IS_PRODUCTION_ENV = false; //production environment flag
	
	public static void debugLog(String tag, String logMsg){
		if(LOG_D_SWITCH) Log.d(tag, logMsg);
	}
	
	public static void errorLog(String tag, String logMsg){
		Log.e(tag, logMsg);
		if(IS_PRODUCTION_ENV) writeError2File(tag, logMsg);
	}

	private static void writeError2File(String tag, String logMsg) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd   hh:mm:ss");     
		String date = sDateFormat.format(new java.util.Date());
		StringBuffer sb = new StringBuffer();
		sb.append(date);
		sb.append("\t");
		sb.append(tag);
		sb.append("\t");
		sb.append(logMsg);
		sb.append("\n");
		String logName = "error.log";
		if (Constants.STORAGE_STATUS_OK == StorageCheck.checkAppDirsAndMkdirs()) {
			File logFile = new File(Constants.ERROR_LOG_DIR, logName);
			if(logFile.exists() && (float)logFile.length()/1024>1024){
				logFile.delete();
			}
			try {
				OutputStream os = new FileOutputStream(logFile,true);
				os.write(sb.toString().getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
