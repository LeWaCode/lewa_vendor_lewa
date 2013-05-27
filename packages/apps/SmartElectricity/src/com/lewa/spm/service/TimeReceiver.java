package com.lewa.spm.service;

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
import android.util.Log;
import android.widget.Toast;
import android.database.ContentObserver;
import android.os.Handler;
import android.content.ContentResolver;
import android.location.LocationManager;
import android.database.Cursor;
import android.content.ContentQueryMap;
import java.util.Observable;
import java.util.Observer;
import android.net.ConnectivityManager;
import android.os.BatteryManager;

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

public class TimeReceiver extends BroadcastReceiver {
	Executer modeExecuter;
	Context mContext;
	NotificationManager nm;
	TelephonyManager tm;
	private int mBatteryLevel = 0;
	String start_time = null;
	String end_time = null;
    
	private static final String EXTRA_CHARGING = "extra_charging";
	private static final String SPM_IS_CALLING_ACTION = "android.intent.action.PHONE_STATE";
	private static final String SPM_RECEIVER_NOTIFICATION_AND_SPM_ACTION = "com.lewa.powermanager.action";
	private static final String SPM_RECEIVER_NOTIFICATION_MODE_NOT_LONG = "status_bar_notice_power_msg";
	private static final String SPM_RECEIVER_NOTIFICATION_AND_SPM_ACTION_TOAST = "com.lewa.spm_notification_toast_action";
	private static final String SPM_RECEIVER_NOTIFICATION_AND_SPM_ACTION_TOAST_KEY = "spm_notification_toast_message";
    
    private static final int FILE_OPERATION_DELETE=0;
    private static final int FILE_OPERATION_QUERY=1;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		mContext = ctx;
        registerProviderObserver();

        //Log.i("lkr","TimerReceiver onReceive-------"+intent.getAction());
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
		nm = (NotificationManager) mContext
				.getSystemService(this.mContext.NOTIFICATION_SERVICE);
		String action=intent.getAction();
		if(action.equals("android.intent.action.SYNC")){
             //Log.i("lkr","TimerReceiver sync-------");
        }else if(action.equals("android.intent.action.AIRPLANE_MODE")){
            boolean isOn=intent.getBooleanExtra("state",false);
            //Log.i("lkr","TimerReceiver flymode-------"+isOn);
            if(!isOn){
                onFlyModeOff();
            }
            Executer.setDevSwitchOver(mContext,Constants.BREAK_CAUSE_FLYMODE);
                
        }else if(action.equals("android.net.wifi.WIFI_STATE_CHANGED")){
            int status=intent.getIntExtra("wifi_state",4);
             //Log.i("lkr","TimerReceiver wifi-------"+status);
             if(status==Constants.WIFI_STATE_ENABLING){
                
             }
             else if(status==Constants.WIFI_STATE_ENABLED){
                onWifiOn();
                Executer.setDevSwitchOver(mContext,Constants.BREAK_CAUSE_WIFI);
             }else if(status==Constants.WIFI_STATE_DISABLED){
                 Executer.setDevSwitchOver(mContext,Constants.BREAK_CAUSE_WIFI);
             }
        }else if(action.equals("android.bluetooth.adapter.action.STATE_CHANGED")){
            
            int status=intent.getIntExtra("android.bluetooth.adapter.extra.STATE",Constants.STATE_OFF);
             //Log.i("lkr","TimerReceiver bt-------"+status);
             if(status==Constants.STATE_ON){
                onBTOn();
                Executer.setDevSwitchOver(mContext,Constants.BREAK_CAUSE_BLUETOOTH);
             }else if(status==Constants.STATE_OFF){
                Executer.setDevSwitchOver(mContext,Constants.BREAK_CAUSE_BLUETOOTH);
             }
        }else if(action.equals(Constants.POWERSAVING_ACTION_NOTIFY_ON)){
            int type=intent.getIntExtra(Constants.POWERSAVING_DEV_TYPE,-1);
            if(type==Constants.DEV_DATA){
                //Log.i("lkr","TimerReceiverDEV_DATA");
                onDataOn();
            }else if(type==Constants.DEV_GPS){
                onGpsOn();
            }
        }
        //when long idle item pressed from status bar,toast the msg
        else if (intent.getAction().equals(SPM_RECEIVER_NOTIFICATION_AND_SPM_ACTION)) {
			ExecuteLongMode runLongMode = new ExecuteLongMode(mContext);
			int itfunzState = intent.getIntExtra("powerstate", 0);
           
			switch (itfunzState) {
			case Constants.STR_NOTIFICATION_SWITCH_OPEN_ID:
				runLongMode.onCreate();
				break;
			case Constants.STR_NOTIFICATION_SWITCH_CLOSE_ID:
                int mode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
                if((mode==Constants.SPM_MODE_OUT_ID)){
                    return;
                }
				runLongMode.onDestroy();
				break;
			default:
				break;
			}
            Intent i = new Intent(SPM_RECEIVER_NOTIFICATION_AND_SPM_ACTION_TOAST);
            mContext.sendBroadcast(i);
            Log.i("lkr","TimerReceiver SPM_ACTION-------itfunzState="+itfunzState);
		} else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
		    int mBatteryChargingType = Constants.BATTERY_PLUGGED_NONE;
            int mPluggType=-1;
            //get current battery mBatteryLevel
            mBatteryLevel = intent.getIntExtra(Constants.STR_POWER_LEVEL, 0);  
            mPluggType = intent.getIntExtra(Constants.STR_POWER_PLUGGED, 
                                            mPluggType);
            mBatteryChargingType = getChargingType(mPluggType);
            saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
			            Constants.SPM_POWER_PLUGGED_TYPE,
			            mBatteryChargingType);
		} else if (intent.getAction().equals(Constants.SPM_INTENT_ACTION_START_ALARM)) { 
		    //Log.i("lkr","TimerReceiver START_ALARM-------");
            tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			boolean calling = (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE);
            if(calling){
                return;
            }		
		    boolean modeChecked = saveValue.getBoolean(
                        				Constants.SHARED_PREFERENCE_NAME,
                        				Constants.INTTELLIGENT_MODE_CHECKED);
			String start_alarm = intent.getStringExtra(
                                        Constants.SPM_INTENT_ACTION_START_ALARM_EXTRA);
            
            if((start_alarm != null)&&(modeChecked == true)){
                if (start_alarm
						.equals(Constants.SPM_INTENT_ACTION_START_ALARM_EXTRA)) {
					ExecuteSmartMode exe=new ExecuteSmartMode(mContext);
                    exe.onCreate();
                    
                    
                }
             }

		} else if (intent.getAction().equals(
				Constants.SPM_INTENT_ACTION_END_ALARM)) {
            //Log.i("lkr","TimerReceiver END_ALARM-------");
				
			boolean modeChecked = saveValue.getBoolean(
                        				Constants.SHARED_PREFERENCE_NAME,
                        				Constants.INTTELLIGENT_MODE_CHECKED);
			String end_alarm = intent.getStringExtra(
                                Constants.SPM_INTENT_ACTION_END_ALARM_EXTRA);
			int PreMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
					                Constants.STR_TIME_BEFORE_MODE_TYPE_NAME,
					                Constants.SPM_MODE_OUT_ID);
			if ((end_alarm != null) && (modeChecked == true))  {
				ExecuteSmartMode exe=new ExecuteSmartMode(mContext);
                exe.onDestroy();
			}
            saveValue.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                        					Constants.SPM_ENTRY_INTILLI_MODE_ON_TIME,
                        					true);
			//nm.cancel(R.drawable.spm_mode_standby);
		}else if (intent.getAction().equals(SPM_IS_CALLING_ACTION)) {
			tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			boolean calling = (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE);
			if (calling == true) {
				PowSavingAlarm.cancelStartAlarm(mContext);
				PowSavingAlarm.cancelEndAlarm(mContext);
			} else {
			    boolean modeChecked = saveValue.getBoolean(
                        				Constants.SHARED_PREFERENCE_NAME,
                        				Constants.INTTELLIGENT_MODE_CHECKED);
                if(!modeChecked){
                    return;
                }
                //get the mode before change
                int mode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                        					Constants.STR_MODE_TYPE_NAME,
                        					Constants.SPM_MODE_OUT_ID);
                if(mode==Constants.SPM_MODE_ALARM_ID){
                    return;
                }
				String currentTime = TimeUtils.getCurrentTime();
				start_time = saveValue.getString(
						Constants.SHARED_PREFERENCE_NAME,
						Constants.KEY_BEDTIME_STARTTIME, "23:00");
				end_time = saveValue.getString(
						Constants.SHARED_PREFERENCE_NAME,
						Constants.KEY_BEDTIME_ENDTIME, "07:00");
				PowSavingAlarm.cancelStartAlarm(mContext);
				PowSavingAlarm.cancelEndAlarm(mContext);
                
                Log.i("TimerReciever","SPM_IS_CALLING_ACTION currentTime="+currentTime);
                Log.i("TimerReciever","SPM_IS_CALLING_ACTION currentTime="+start_time);
                Log.i("TimerReciever","SPM_IS_CALLING_ACTION currentTime="+end_time);
				if (PowerSavingMode.isInTimeInterval(start_time, end_time,
						currentTime)) {
					PowSavingAlarm.setStartTimeAlarm(mContext, start_time);
                    boolean entry=saveValue.getBoolean(Constants.SHARED_PREFERENCE_NAME,
                        					Constants.SPM_ENTRY_INTILLI_MODE_ON_TIME);
                    if(entry){
                        ExecuteSmartMode exe=new ExecuteSmartMode(mContext);
                        exe.onCreate();
                    }
				} else {
					PowSavingAlarm.setStartTimeAlarm(mContext, start_time);
				}
				PowSavingAlarm.setEndTimeAlarm(mContext, end_time);
			}

		} else if (intent.getAction().equals(
				"com.lewa.action.BATTERY_ALMOST_FULL")) {
			long almostFullTime = Calendar.getInstance().getTimeInMillis();
			saveValue.saveLong(Constants.SHARED_PREFERENCE_NAME,
					            Constants.SPM_ALMOST_FULL_START_TIME,
					            almostFullTime);

		} else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
		    boolean appInited=saveValue.getBoolean(Constants.SHARED_PREFERENCE_NAME,
		                                        Constants.APPLICATION_INITED);
            if(!appInited){
                saveValue.operate();
                ModesFixedState mfs=new ModesFixedState();
                //save long mode defalt settings
                ModeSettings msl=new ModeSettings(mContext,Constants.SPM_MODE_LONG_ID);
                mfs.getModeDefaultSettings(msl);
                msl.saveSettings();
                //save interval mode defalt settings
                ModeSettings msi=new ModeSettings(mContext,Constants.SPM_MODE_ALARM_ID);
                mfs.getModeDefaultSettings(msi);
                msi.saveSettings();
                //save chharging default estimating time
                saveValue.saveChargingHistory();
                saveValue.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                                                Constants.APPLICATION_INITED,
                                                true);
                return;
            }
            boolean modeChecked = saveValue.getBoolean(
                        				Constants.SHARED_PREFERENCE_NAME,
                        				Constants.INTTELLIGENT_MODE_CHECKED);
            int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
            
			start_time = saveValue.getString(Constants.SHARED_PREFERENCE_NAME,
					                    Constants.KEY_BEDTIME_STARTTIME, 
					                    "23:00");
			end_time = saveValue.getString(Constants.SHARED_PREFERENCE_NAME,
				                        Constants.KEY_BEDTIME_ENDTIME,
				                        "07:00");
			mBatteryLevel = Integer.parseInt(BatteryInfo
					.getInformation(BatteryInfo.battCapacity));
            boolean inInterval = PowerSavingMode
    						.isShowOrNotByTimeCondition(start_time, end_time);
            if(modeChecked == true){
                if((currMode==Constants.SPM_MODE_ALARM_ID)&&inInterval){
                    
                    //PowerSavingMode psm=new PowerSavingMode(mContext,
                   //                                       Constants.SPM_MODE_ALARM_ID);
                    //psm.execute();
                   // psm.notifyLongModeChange(false);
                   showNotification(mContext.getString(R.string.spm_notification_tag_title),
                                    mContext.getString(R.string.app_name),
                                    mContext.getString(R.string.spm_notification_sleep_time_title) 
                                      + TransferMode.consTransferMode(mContext,  Constants.SPM_MODE_ALARM_ID),
                                    R.drawable.spm_mode_standby);
                }else{
                    
                    nm.cancel(R.drawable.spm_mode_alarm);
                }
                PowSavingAlarm.setStartTimeAlarm(mContext, start_time);
                PowSavingAlarm.setEndTimeAlarm(mContext, end_time);

            }
            registerProviderObserver();
	}
}

    private int getChargingType(int plug) {
		int  currentState = Constants.BATTERY_PLUGGED_NONE;
		switch (plug) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			currentState = Constants.BATTERY_PLUGGED_AC;
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			currentState = Constants.BATTERY_PLUGGED_USB;
			break;
		}
		return currentState;
	}
    public void showNotification(String tickerText, String contentTitle,
    		String contentText, int id) {
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

    void onFlyModeOff(){
         //Log.i("lkr","onFlyModeOff -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onFlyModeOff -------currMode="+currMode);
        if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.flyModeSetted){
            return;
        }
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_FLYMODE); 
        
        //
       
    }
    void onWifiOn(){
       
         //Log.i("lkr","onWifiOn -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onWifiOn -------currMode="+currMode);
         if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.wifiSetted){
            return;
        }
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_WIFI); 
        
    }
    void onBTOn(){
       
         //Log.i("lkr","onBTOn -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onBTOn -------currMode="+currMode);
         if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.bluetoothSetted){
            return;
        } 
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_BLUETOOTH);
       
    }
    void onGpsOn(){
       
         //Log.i("lkr","onGpsOn -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onGpsOn -------currMode="+currMode);
         if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.gpsSetted){
            return;
        } 
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_GPS);
       
    }
    void onHapticOn(){
       
         //Log.i("lkr","onHapticOn -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onHapticOn -------currMode="+currMode);
        if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.hapticSetted){
            return;
        }
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_HAPITIC);
       
    }


    void onDataOn(){
       
         //Log.i("lkr","onDataOn -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onDataOn -------currMode="+currMode);
        if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.dataSetted){
            return;
        }
        
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_DATA);
         
        
       
    }
    void onSyncOn(){
       
         //Log.i("lkr","onSyncOn -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","onSyncOn -------currMode="+currMode);
        if(((currMode==Constants.SPM_MODE_OUT_ID)
            ||(currMode==Constants.SPM_MODE_ALARM_ID))
            &&(!PowerSavingMode.isExcuting)){
            return;
        }
        ModeSettings ms=new ModeSettings(mContext,currMode);  
        ms.getSettings();
        if(!ms.autoSyncSetted){
            return;
        }
        ExecuteLongMode exe=new ExecuteLongMode(mContext);
        exe.executeBreak(Constants.BREAK_CAUSE_AUTOSYNC);
       
       
    }

    class SPMObserver extends ContentObserver {
        SPMObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED), false, this);
        }

         @Override
         public void onChange(boolean selfChange) {
             if(selfChange){
             }
         }
    }  

    private ContentObserver mHapticObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
           //Log.i("lkr","mHapticObserver-------"+selfChange);
            boolean on=(Settings.System.getInt(mContext.getContentResolver(),
				Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 1);
            if(on){
                //Log.i("lkr","mHapticObserver-------");
                onHapticOn();
             }
        }
    };
    private boolean getDataState() {
        ConnectivityManager cm = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getMobileDataEnabled();
    }

        private ContentObserver mDataObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
           //Log.i("lkr","mDataObserver-------"+selfChange);
            boolean on=getDataState();
            if(on){
                //Log.i("lkr","mDataObserver-------");
                onDataOn();
             }
        }
    };

    

     private ContentObserver mGpsObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
           // Log.i("lkr","mHapticObserver-------"+selfChange);
            boolean on=(Settings.System.getInt(mContext.getContentResolver(),
				Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 1);
            if(on){
               // Log.i("lkr","mHapticObserver-------");
                onGpsOn();
             }
        }
    };
    private final class SettingsObserver implements Observer {
        
        public void update(Observable o, Object arg) {
           //Log.i("lkr","SettingsObserver-------"+o);
           
           ContentResolver res = mContext.getContentResolver();
            boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                    res, LocationManager.GPS_PROVIDER);
            if(gpsEnabled){
                //Log.i("lkr","SettingsObserver-------");
                onGpsOn();
            }else if(getDataState()){
                //Log.i("lkr","mDataObserver-------data");
                onDataOn();
             }
        }
    }
    
    private ContentQueryMap mContentQueryMap=null;

     private void registerProviderObserver() {
        mContext.getContentResolver().registerContentObserver(
                                    Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED),
                                        false, 
                                        mHapticObserver);

        mContext.getContentResolver().registerContentObserver(
                                    Settings.System.getUriFor(Settings.Secure.MOBILE_DATA),
                                        false, 
                                        mDataObserver);

        
         // listen for Location Manager settings changes
        /*Cursor settingsCursor = mContext.getContentResolver().query(Settings.Secure.CONTENT_URI, null,
                "(" + Settings.System.NAME + "=?)",
                new String[]{Settings.Secure.LOCATION_PROVIDERS_ALLOWED},
                null);
         /settingsCursor.close();
        //mContentQueryMap = new ContentQueryMap(settingsCursor, Settings.System.NAME, true, null);
        mContentQueryMap.addObserver(new SettingsObserver());
        */
    }
}
