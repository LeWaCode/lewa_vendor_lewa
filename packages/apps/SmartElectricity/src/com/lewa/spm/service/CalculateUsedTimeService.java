//package com.lewa.spm.service;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import com.lewa.spm.control.Executer;
//import com.lewa.spm.entity.ModeChoice;
//import com.lewa.spm.util.Constants;
//import com.lewa.spm.util.TimeUtils;
//import com.lewa.spm.util.SharedStorageKeyValuePair;
//import com.lewa.spm.util.ShowNotification;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.IBinder;
//import android.util.Log;
//
//public class CalculateUsedTimeService extends Service{
//	ModeChoice modeChoice;
//	SharedStorageKeyValuePair saveValue;
//	Intent mIntent;
//	int level = 0;
//	Date mCurrentTime;
//	String currentTime = null;
//	String typeBasedOnCondition = null;
//	String currentType = null;
//	int time_mistiming_seconds;
//	int time_mistiming_minute;
//	int time_mistiming_hour;
//	int time_mistiming_date;
//	int plugged;
//	int lowPowerNotificationCount = -1;
//	int timeNotificationCount = -1;
//	public static int statesType = 1; // the default value of battery state is charging;(0 is charging ; 1 is plugged)
//	DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss"); 
//	
//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}
//	
//	BroadcastReceiver monitorPowerReceiver = new BroadcastReceiver() {
//		Context mContext;
//
//		@Override
//		public void onReceive(Context mContext, Intent intent) {
//			this.mContext = mContext;
//			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
//				level = intent.getIntExtra("level", 0);
//				plugged = intent.getIntExtra("plugged", 0);
//				saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_POWER_PLUGGED_STATE, plugged);
//				runBackToChangeMode();
//			}else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)){
//				saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_POWER_PLUGGED_STATE, 0);
//				runBackToChangeMode();
//			}else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
//				runBackToChangeMode();
//			}else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)){
//				runBackToChangeMode();
//			}
//		}
//	};
//	
//	
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		registeReceiver();
//		saveValue = new SharedStorageKeyValuePair(this);
//		saveValue.saveDataToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.STR_MODE_TYPE_NAME, Constants.MODE_ORDINARY_SAVE_POWER);
//		saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_LOW_POWER_COUNT, 0);
//		saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_TIME_COUNT, 0);
//	}
//
//	private void registeReceiver() {
//		IntentFilter currentIntentFilter = new IntentFilter();
//		currentIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//		currentIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//		currentIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
//		currentIntentFilter.addAction(Intent.ACTION_TIME_TICK);
//        registerReceiver(monitorPowerReceiver, currentIntentFilter);
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		unregisterReceiver(monitorPowerReceiver);
//	}
//	
//	private void runBackToChangeMode(){
//		modeChoice = new ModeChoice(this);
//		currentTime = TimeUtils.getCurrentTime();
//		typeBasedOnCondition = modeChoice.getModeTypeBasedOnTheCondition(currentTime, level);
//		currentType = saveValue.judgeStringValueIsNull(Constants.SHARED_PREFERENCE_NAME, Constants.STR_MODE_TYPE_NAME, Constants.MODE_ORDINARY_SAVE_POWER);
//		String type = null;
//		if (typeBasedOnCondition == null){
//			type = currentType;
//		}else{
//			type = typeBasedOnCondition;
//		}
//		if (currentType.equals(type)){
//		}else{
//			judgeIfNotification();
//			choiseModeExcute(type);
//			currentType = type;
//			saveValue.saveDataToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.STR_MODE_TYPE_NAME, type);
//		}
//	}
//	
//	private void choiseModeExcute(String modeType){
//		Executer modeExecuter = new Executer(this);
//		modeExecuter.execute(modeType);
//	}
//	
//	private void judgeIfNotification(){
//		boolean bedTimeCheckValue = saveValue.getBooleanValueFromSharedPreference(Constants.SHARED_PREFERENCE_NAME, Constants.KEY_BEDTIME);
//		boolean lowPowerCheckValue = saveValue.getBooleanValueFromSharedPreference(Constants.SHARED_PREFERENCE_NAME,Constants.KEY_LOW_POWER);
//		String bedTimeStart = saveValue.judgeStringValueIsNull(Constants.SHARED_PREFERENCE_NAME, Constants.KEY_BEDTIME_STARTTIME, Constants.INTEL_TIME_DEF_FROM);
//		String bedTimeEnd = saveValue.judgeStringValueIsNull(Constants.SHARED_PREFERENCE_NAME, Constants.KEY_BEDTIME_ENDTIME, Constants.INTEL_TIME_DEF_TO);
//		int lowLowPowerValue = saveValue.getIntValueFromSharedPreference(Constants.SHARED_PREFERENCE_NAME, Constants.KEY_LOW_POWER_SET_VALUE, Constants.INTEL_POWER_DEF);
//		int lowTempleCount = saveValue.getIntValueFromSharedPreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_LOW_POWER_COUNT, 0);
//		int timeTempleCount = saveValue.getIntValueFromSharedPreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_TIME_COUNT, 0);
//		if (bedTimeCheckValue == true){
//				timeNotificationCount = timeTempleCount;
//		}
//		if (lowPowerCheckValue == true){
//				lowPowerNotificationCount = lowTempleCount;
//		}
//		if ((lowPowerNotificationCount == 0) || (timeNotificationCount == 0)){
//			Log.e("Tt", "count = 0 show notification");
//			ShowNotification.showNotification(this, level, currentTime, lowLowPowerValue, bedTimeStart, bedTimeEnd );
//		}
//	}
//}
