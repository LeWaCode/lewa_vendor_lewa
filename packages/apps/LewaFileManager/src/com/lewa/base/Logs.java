package com.lewa.base;

import android.util.Log;

public class Logs {
	public static final int LOG_INFO = 0;
	public static final int LOG_DEBUG = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_ERROR = 3;
	public static int LEVEL = LOG_INFO;
    public static String TAG = "";
    public static void setTag(String tag) {
        TAG = tag;
    }
	public static void setLevel(int level){
		LEVEL = level;
	}
	
    public static void i(String msg) {
        if (LEVEL <= 0) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (LEVEL <= 1) {
            Log.d(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (LEVEL <= 2) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (LEVEL <= 3) {
            Log.e(TAG, msg);
        }
    }
	public static void i(String tag,String msg){
		if(LEVEL<=0){
			Log.i(tag, msg);
		}
	}
	public static void d(String tag,String msg){
		if(LEVEL<=1){
			Log.d(tag, msg);
		}
	}
	public static void w(String tag,String msg){
		if(LEVEL<=2){
			Log.w(tag, msg);
		}
	}
	public static void e(String tag,String msg){
		if(LEVEL<=3){
			Log.e(tag, msg);
		}
	}
}
