package com.lewa.spm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;


import com.lewa.spm.R;
import com.lewa.spm.adapter.DevInfo;
import com.lewa.spm.charging.ChargingHistory;
import com.lewa.spm.util.BatteryInfo;

import com.lewa.spm.control.DeviceStatusMap;
import com.lewa.spm.control.Executer;
import com.lewa.spm.mode.ModesFixedState;

import com.lewa.spm.mode.PowerSavingMode;
import com.lewa.spm.util.CalcUtils;
import com.lewa.spm.util.Constants;
import com.lewa.spm.util.ConsumeValue;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.util.TimeUtils;
import com.lewa.spm.charging.ChargingAnimation;

import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;

import java.io.File;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.content.ContentResolver;
import android.location.LocationManager;
import android.database.Cursor;
import android.content.ContentQueryMap;
import java.util.Observable;
import java.util.Observer;
import android.net.ConnectivityManager;

import com.lewa.spm.activity.SPMActivity;
import com.lewa.spm.control.Executer;
import com.lewa.spm.util.BatteryInfo;

import com.lewa.spm.mode.ModesFixedState;
import com.lewa.spm.mode.PowSavingAlarm;
import com.lewa.spm.mode.PowerSavingMode;

import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.util.TimeUtils;
import com.lewa.spm.util.TransferMode;
import com.lewa.spm.R;
import com.lewa.spm.util.CalcUtils;

import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;


public class ExecuteSmartMode {

	private Context mContext;
	private SharedStorageKeyValuePair saveValue;
    

	public ExecuteSmartMode(Context ctx) {
		mContext = ctx;
		saveValue = new SharedStorageKeyValuePair(mContext);
	}
    
    public void onCreate(){

        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);	
        // execute the long mode
        //Log.i("lkr","ExecuteSmartMode---- currMode "+currMode);
	
         if(currMode==Constants.SPM_MODE_LONG_ID){
            ExecuteLongMode exe=new ExecuteLongMode(mContext);
            exe.onPause();
        }else{
            ModeDevStatus mds=new ModeDevStatus(mContext,currMode);
            mds.saveStatus();
        }
        // save the previous mode
		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
        					currMode);
		// save long idle mode
		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_MODE_TYPE_NAME,
        					Constants.SPM_MODE_ALARM_ID);
        showNotification(
					mContext.getString(R.string.spm_notification_tag_title),
					mContext.getString(R.string.app_name),
					mContext.getString(R.string.spm_notification_sleep_time_title)
							+ TransferMode.consTransferMode(mContext,
									Constants.SPM_MODE_ALARM_ID),
					R.drawable.spm_mode_standby);
        
        // execute the mode which you choice
		PowerSavingMode psm=new PowerSavingMode(mContext, Constants.SPM_MODE_ALARM_ID);
        psm.execute();
    }

    //only from idle
    public void onPause(){
        NotificationManager nm = (NotificationManager) mContext
				.getSystemService(this.mContext.NOTIFICATION_SERVICE);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_MODE_TYPE_NAME,
                					Constants.SPM_MODE_ALARM_ID);
        ModeDevStatus mds=new ModeDevStatus(mContext,currMode);
        mds.saveStatus();
        //
        nm.cancel(R.drawable.spm_mode_standby);  
    }



    
    //only from idle
    public void onResume(){
        /*
        // save the previous mode
		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
        					Constants.SPM_MODE_OUT_ID);
		// save long idle mode
		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_MODE_TYPE_NAME,
        					Constants.SPM_MODE_ALARM_ID);
        					*/
        Executer execute = new Executer(mContext, Constants.SPM_MODE_ALARM_ID);
        execute.entryUserMode();
        
        showNotification(
					mContext.getString(R.string.spm_notification_tag_title),
					mContext.getString(R.string.app_name),
					mContext.getString(R.string.spm_notification_sleep_time_title)
							+ TransferMode.consTransferMode(mContext,
									Constants.SPM_MODE_ALARM_ID),
					R.drawable.spm_mode_standby);
        //Intent excuteFinish = new Intent(Constants.SPM_DEVS_SWITTCH_FINISH_ACTION);
        //mContext.sendBroadcast(excuteFinish);
        
    }

    public void onDestroy(){
        int preMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
        if(currMode!=Constants.SPM_MODE_ALARM_ID){
            return;
        }
        
        NotificationManager nm = (NotificationManager) mContext
				.getSystemService(this.mContext.NOTIFICATION_SERVICE);
        nm.cancel(R.drawable.spm_mode_standby);
        saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
        if(preMode==Constants.SPM_MODE_OUT_ID){
            // execute the mode which you choice
            saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
            				Constants.STR_MODE_TYPE_NAME,
            				Constants.SPM_MODE_OUT_ID);
    		PowerSavingMode psm=new PowerSavingMode(mContext,
                                                  Constants.SPM_MODE_OUT_ID);
            psm.execute();
            ExecuteLongMode.closeSettingsWhileNotIn(mContext);
        }else{
            ExecuteLongMode exe=new ExecuteLongMode(mContext);
            exe.onResume();
        }
    }

    public void showNotification(String tickerText, String contentTitle,
    		String contentText, int id) {
    	NotificationManager nm = (NotificationManager) mContext
				.getSystemService(this.mContext.NOTIFICATION_SERVICE);	
    	Notification notification = new Notification(
    			R.drawable.spm_notification_pic, tickerText,
    			System.currentTimeMillis());
    	Intent it = new Intent(mContext, SPMActivity.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
    			it, 0);
    	notification.setLatestEventInfo(mContext, contentTitle, contentText,
    			contentIntent);
    	nm.notify(id, notification);
    }  

 }
