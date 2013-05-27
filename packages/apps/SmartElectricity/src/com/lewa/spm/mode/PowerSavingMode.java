package com.lewa.spm.mode;

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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lewa.spm.R;
import com.lewa.spm.adapter.DevInfo;
import com.lewa.spm.charging.ChargingHistory;

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

public class PowerSavingMode {
    public static boolean isExcuting=false;
	Context mContext;
    int mMode;
	public PowerSavingMode(Context ctx,int mode) {
		mContext = ctx;
        mMode=mode;
	}
    public int getMode(){
        return mMode;
    }
	public static String getName (Context mContext, int mode){
		switch (mode) {
		case Constants.SPM_MODE_LONG_ID:
			return mContext.getString(R.string.spm_mode_standby);
		case Constants.SPM_MODE_ALARM_ID:
			return mContext.getString(R.string.spm_alarm_clock);
		default:
			break;
		}
		return null;
	}
	
	public static Boolean isShowOrNotByTimeCondition(String startTime, String endTime){
		String currentTime = TimeUtils.getCurrentTime();
		return isInTimeInterval(startTime, endTime, currentTime);
    }

    	public static Boolean isInTimeInterval(String startTime, String endTime, String currentTime){
        if((currentTime.compareTo(endTime) == 0) || ((startTime.compareTo(endTime) == 0))){
            return false;
        }
        if(startTime.compareTo(endTime) < 0){
            if ((startTime.compareTo(currentTime) <= 0) && (currentTime.compareTo(endTime) < 0)){
                return true;
    	    }
        }else{
            if ((startTime.compareTo(currentTime) <= 0) && (currentTime.compareTo("23:59") <= 0)){
	            return true;
            }else if ((("00:00").compareTo(currentTime) <= 0) 
            && (currentTime.compareTo(endTime) < 0)
            && (currentTime.compareTo(startTime) > 0)){
    	        return true;
            }
        }
        return false;
    }
        
        
    public void notifyLongModeChange(boolean check){
        Intent excuteFinish = new Intent(Constants.SPM_EXCUTE_FINISH_ACTION);
        excuteFinish.putExtra(Constants.SPM_EXCUTE_FINISH_NAME, check);
        mContext.sendBroadcast(excuteFinish);
   }
    
	public void execute(){
        //Log.i("lkr","PowerSavingMode--execute-------mMode="+mMode);
		Executer execute = new Executer(mContext, mMode);
		switch (mMode) {
		case Constants.SPM_MODE_ALARM_ID:
            execute.executSavingPower();
			break;
		case Constants.SPM_MODE_LONG_ID:
			 execute.executSavingPower();
			break;
		case Constants.SPM_MODE_OUT_ID:
             execute.entryUserMode();
			break;
		default:
			break;
		}
	}
	
}
