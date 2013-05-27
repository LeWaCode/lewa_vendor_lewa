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
import android.widget.Toast;


import com.lewa.spm.R;
import com.lewa.spm.adapter.DevInfo;
import com.lewa.spm.charging.CalculateChargingTime;
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


public class ExecuteLongMode {

	private Context mContext;
	private SharedStorageKeyValuePair saveValue; 
    

	public ExecuteLongMode(Context ctx) {
		mContext = ctx;
		saveValue = new SharedStorageKeyValuePair(mContext);
	}

    public void onCreate(){

        String toastStr= mContext.getString(R.string.spm_mode_choice_dialog_content_before);
        //get the mode before change
        int preMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","ExecuteLongMode onCreate preMode=" +preMode);
        
        if(preMode==Constants.SPM_MODE_OUT_ID)
        {
            ModeDevStatus mds=new ModeDevStatus(mContext,preMode);
            mds.saveStatus();
        }
        else{
            ExecuteSmartMode  exe=new ExecuteSmartMode(mContext);
            exe.onPause();
        }
         //get the remaing time  before change
		Double preRemain = CalcUtils.getInstance(mContext).Lift(preMode);
        Double currRemain;
        
        Settings.System.putInt(mContext.getContentResolver(),
            					Settings.System.POWERMANAGER_MODE_ON,
            					1);
       
        currRemain = CalcUtils.getInstance(mContext)
					                .Lift(Constants.SPM_MODE_LONG_ID);
        int mBatteryLevel = Integer.parseInt(BatteryInfo
					.getInformation(BatteryInfo.battCapacity));
            
		String diffTime= TimeUtils.calcLifeChangeDiff(mContext,
                                                    currRemain,
                                                    preRemain,
                                                    mBatteryLevel);
         //toast a change msg
       // Log.i("lkr","ExecuteLongMode onCreate " +diffTime);
		//if (diffTime.equals("0"))
        if(diffTime.subSequence(0, 1).equals("0")){
            toastStr=mContext.getString(R.string.spm_mode_same_toast);
		} else if (diffTime.subSequence(0, 1).equals("+")) {
		    toastStr=mContext.getString(R.string.spm_mode_choice_standy_by_toast)
                    +toastStr
					+ mContext.getString(R.string.spm_mode_choice_time_advance)
					+ diffTime.subSequence(1, diffTime.length());
		} else if (diffTime.subSequence(0, 1).equals("-")) {
		      toastStr=mContext.getString(R.string.spm_mode_choice_standy_by_toast)
                    +toastStr
					+ mContext.getString(R.string.spm_mode_choice_time_reduce)
					+ diffTime.subSequence(1, diffTime.length());
		}
        if(preMode!=Constants.SPM_MODE_ALARM_ID){
    		// save long idle mode
    		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
            					Constants.STR_TIME_BEFORE_MODE_TYPE_NAME,
            					preMode);
            // save long idle mode
    		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
            					Constants.STR_MODE_TYPE_NAME,
            					Constants.SPM_MODE_LONG_ID);
        }
        PowerSavingMode psm=new PowerSavingMode(mContext, Constants.SPM_MODE_LONG_ID);
        psm.execute();
 
        //
        int mBatteryChargingType = Constants.BATTERY_PLUGGED_NONE;  
        mBatteryChargingType=saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                        		            Constants.SPM_POWER_PLUGGED_TYPE,
                        		            mBatteryChargingType);
        if(mBatteryChargingType==Constants.BATTERY_PLUGGED_NONE){
        	Toast.makeText(mContext, toastStr,1000).show();
        }
        //
        notifyLongModeChange(mContext,true,diffTime);
        
    }

    

    public void onPause(){
        int mBatteryLevel = Integer.parseInt(BatteryInfo
					.getInformation(BatteryInfo.battCapacity));
        //get title:remaining
		String toastStr= mContext.getString(R.string.spm_mode_choice_dialog_content_before);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        int bMode=currMode;
        //get the remaing time  before change
        if(currMode==Constants.SPM_MODE_ALARM_ID){
            bMode=Constants.SPM_MODE_LONG_ID;
        }
		Double preRemain = CalcUtils.getInstance(mContext).Lift(bMode);
        Double currRemain;

        String diffTime;
        ModeDevStatus mds=new ModeDevStatus(mContext,currMode);
        mds.saveStatus();

        Settings.System.putInt(mContext.getContentResolver(),
            					Settings.System.POWERMANAGER_MODE_ON,
            					0);
        
		int tobeMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
       
		int aMode=tobeMode;
		if(currMode==Constants.SPM_MODE_ALARM_ID){
            aMode=Constants.SPM_MODE_ALARM_ID;
        }
		currRemain = CalcUtils.getInstance(mContext).Lift(aMode);
        //toast a change msg
		diffTime = TimeUtils.calcLifeChangeDiff(mContext,
				 currRemain,preRemain, mBatteryLevel);
        //Log.i("lkr","ExecuteLongMode onPause " +diffTime);
		if (diffTime.subSequence(0, 1).equals("+")){
            toastStr=mContext.getString(R.string.spm_mode_choice_exit_standy_by_toast)
                    +toastStr
					+ mContext.getString(R.string.spm_mode_choice_time_advance)
					+ diffTime.subSequence(1, diffTime.length());
		} else if (diffTime.subSequence(0, 1).equals("-")) {
		    toastStr=mContext.getString(R.string.spm_mode_choice_exit_standy_by_toast)
                    +toastStr
					+ mContext.getString(R.string.spm_mode_choice_time_reduce)
					+ diffTime.subSequence(1, diffTime.length());
		} else if(diffTime.subSequence(0, 1).equals("0")) {
		    toastStr=mContext.getString(R.string.spm_mode_same_toast);	
		}
        //
        int mBatteryChargingType = Constants.BATTERY_PLUGGED_NONE;  
        mBatteryChargingType=saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                        		            Constants.SPM_POWER_PLUGGED_TYPE,
                        		            mBatteryChargingType);
        
        if(mBatteryChargingType==Constants.BATTERY_PLUGGED_NONE){
        	Toast.makeText(mContext, toastStr,1000).show();
        }
        //
        notifyLongModeChange(mContext,false,diffTime); 
    }


    

    //only restore from inti mode
    public void onResume(){

        String toastStr= mContext.getString(R.string.spm_mode_choice_dialog_content_before);
        //get the mode before change
        int preMode = Constants.SPM_MODE_ALARM_ID;
         //get the remaing time  before change
		Double preRemain = CalcUtils.getInstance(mContext).Lift(preMode);
        Double currRemain;
        
        Settings.System.putInt(mContext.getContentResolver(),
            					Settings.System.POWERMANAGER_MODE_ON,
            					1);
       
        currRemain = CalcUtils.getInstance(mContext)
					                .Lift(Constants.SPM_MODE_LONG_ID);
        int mBatteryLevel = Integer.parseInt(BatteryInfo
					.getInformation(BatteryInfo.battCapacity));
            
		String diffTime= TimeUtils.calcLifeChangeDiff(mContext,
                                                    currRemain,
                                                    preRemain,
                                                    mBatteryLevel);
         //toast a change msg
         //Log.i("lkr","ExecuteLongMode onResume " +diffTime);
		//if (diffTime.equals("0"))
        if(diffTime.subSequence(0, 1).equals("0")){
            toastStr=mContext.getString(R.string.spm_mode_same_toast);
		} else if (diffTime.subSequence(0, 1).equals("+")) {
		    toastStr=mContext.getString(R.string.spm_mode_choice_standy_by_toast)
                    +toastStr
					+ mContext.getString(R.string.spm_mode_choice_time_advance)
					+ diffTime.subSequence(1, diffTime.length());
		} else if (diffTime.subSequence(0, 1).equals("-")) {
		      toastStr=mContext.getString(R.string.spm_mode_choice_standy_by_toast)
                    +toastStr
					+ mContext.getString(R.string.spm_mode_choice_time_reduce)
					+ diffTime.subSequence(1, diffTime.length());
		}
        int mBatteryChargingType = Constants.BATTERY_PLUGGED_NONE;  
        mBatteryChargingType=saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                        		            Constants.SPM_POWER_PLUGGED_TYPE,
                        		            mBatteryChargingType);
        if(mBatteryChargingType==Constants.BATTERY_PLUGGED_NONE){
        	Toast.makeText(mContext, toastStr,1000).show();
	}
        
        //
        notifyLongModeChange(mContext,true,diffTime);
        
        Executer execute = new Executer(mContext, Constants.SPM_MODE_LONG_ID);
        execute.entryUserMode();
        
   
        // save long idle mode
		saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_MODE_TYPE_NAME,
        					Constants.SPM_MODE_LONG_ID);
    }
    
    public void onDestroy(){
        
        int tobeMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID); 
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
        onPause();

        if(currMode==Constants.SPM_MODE_ALARM_ID){
            ExecuteSmartMode exe=new ExecuteSmartMode(mContext);
            exe.onResume();
        }else{   
            saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_SWITCH_BEFORE_MODE_TYPE_NAME,
        					Constants.SPM_MODE_OUT_ID);
            saveValue.saveInt(Constants.SHARED_PREFERENCE_NAME,
        					Constants.STR_MODE_TYPE_NAME,
        					tobeMode);
            PowerSavingMode psm=new PowerSavingMode(mContext,tobeMode);
            psm.execute();
        }
        
    }

   public static  void notifyLongModeChange(Context ctx,boolean check,String diff){
        Intent excuteFinish = new Intent(Constants.SPM_EXCUTE_FINISH_ACTION);
        excuteFinish.putExtra(Constants.SPM_EXCUTE_FINISH_NAME, check);
        excuteFinish.putExtra(Constants.LONG_MODE_SWITCH_TIME_DIFFERENCE, diff);
        ctx.sendBroadcast(excuteFinish);
   }


   public static void closeSettingsWhileNotIn(Context ctx){
        Settings.System.putInt(ctx.getContentResolver(),
            					Settings.System.POWERMANAGER_MODE_ON,
            					0);
        notifyLongModeChange(ctx,false,null);
   }


    

    public void executeBreak(int cause){
        //Log.i("lkr","executeBreak -------");
        SharedStorageKeyValuePair saveValue=new SharedStorageKeyValuePair(mContext);
        //get the mode before change
        int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        //Log.i("lkr","executeBreak -------currMode="+currMode);
        if(currMode!=Constants.SPM_MODE_LONG_ID){
            return;
        }
         
        int preMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_TIME_BEFORE_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
        ModeSettings ms=null;
        ModeSettings.restore_cause=Constants.BREAK_CAUSE_NONE;
        
        ModeDevStatus mds=new ModeDevStatus(mContext,preMode);
        if(preMode==Constants.SPM_MODE_OUT_ID){
            mds.getStatus();
            mds.saveStatus();
            onDestroy();
            return;
            
        }else{
             ms=new ModeSettings(mContext,currMode);
             ms.getSettings();
        }
        switch(cause){
            case Constants.BREAK_CAUSE_FLYMODE:
                mds.flyModeOn=false;
                if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.flyModeSetted){
                        ModeSettings.restore_cause=cause;
                        ms.flyModeSetted=false;
                    }
                    
                }
                break;
            case Constants.BREAK_CAUSE_WIFI:
                mds.wifiOn=true;
                if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.wifiSetted){
                        ModeSettings.restore_cause=cause;
                        ms.wifiSetted=false;
                    }
                    
                }
                break;
            case Constants.BREAK_CAUSE_GPS:
                 mds.gpsOn=true;
                 if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.gpsSetted){
                        ModeSettings.restore_cause=cause;
                        ms.gpsSetted=false;
                    }
                    
                }
                break;
            case Constants.BREAK_CAUSE_BLUETOOTH:
                 mds.bluetoothOn=true;
                 if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.bluetoothSetted){
                        ModeSettings.restore_cause=cause;
                        ms.bluetoothSetted=false;
                    }
                    
                }
                break;
            case Constants.BREAK_CAUSE_DATA:
                 mds.dataOn=true;
                 if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.dataSetted){
                        ModeSettings.restore_cause=cause;
                        ms.dataSetted=false;
                    }
                    
                }
                break;
            case Constants.BREAK_CAUSE_HAPITIC:
                 mds.hapticOn=true;
                 if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.hapticSetted){
                        ModeSettings.restore_cause=cause;
                        ms.hapticSetted=false;
                    }
                    
                }
                break;
            case Constants.BREAK_CAUSE_AUTOSYNC:
                 mds.autoSyncOn=true;
                 if(preMode==Constants.SPM_MODE_OUT_ID){
                    if(ms.autoSyncSetted){
                        ModeSettings.restore_cause=cause;
                        ms.autoSyncSetted=false;
                    }
                    
                }
                break;
        }
        mds.saveStatus();
         if(preMode!=Constants.SPM_MODE_OUT_ID){
            ms.saveSettings();
        }
        onDestroy();
      }
}
