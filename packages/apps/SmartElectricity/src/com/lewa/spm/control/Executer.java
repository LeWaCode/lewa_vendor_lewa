package com.lewa.spm.control;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.bluetooth.BluetoothAdapter;

import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;
import com.lewa.spm.mode.PowerSavingMode;
import com.lewa.spm.util.Constants;

public class Executer {
	private Context mContext;
	private int mMode;
	
	public Executer(Context ctx, int mode) {
		mContext = ctx;
		mMode = mode;
		
	}

    public void executSavingPower(){
        swithSavingMode();
    }
    
    public void entryUserMode(){
        swithUserMode();
    }
    
    void swithSavingMode(){
        PowerSavingMode.isExcuting=true;
        ModeSettings ms=new ModeSettings(mContext,mMode);
        InquirState inquirState = new InquirState(mContext,mMode);
	    SwitchState switchState = new SwitchState(mContext,mMode);
        ms.getSettings();
        if(ms.wifiSetted)
        {
            if(inquirState.wifiState()){
                mWifiSwithing=true;
                switchState.wifiState(false);
            }
        }
        if(ms.gpsSetted)
        {
            if(inquirState.gpsState()){
               switchState.gpsState(false);
            }
        }
        if(ms.bluetoothSetted)
        {
            if(inquirState.bluetoothState()){
                mBTSwithing=true;
                switchState.bluetoothState(false);
            }
        }
        if(ms.dataSetted)
        {
            if(inquirState.mobileDataState()){
                switchState.mobileDataState(false);
            }
        }
        if(ms.hapticSetted)
        {
            if(inquirState.hapticState()){
               switchState.hapticState(false);
            }
        }
        if(ms.autoSyncSetted)
        {
            if(inquirState.syncState()){
               switchState.syncState(false);
            }
        }
            switchState.lightOfScreenAutoState(ms.brightnessSetted);
    		if (ms.brightnessSetted == false){
                if(inquirState.lightOfScreenValue()>ms.brightnessValue){
    			    switchState.lightOfScreenValue(ms.brightnessValue);
                }
    		}
        if(inquirState.lockScreenValue()>ms.timeOutValue){
		    switchState.lockScreenValue(ms.timeOutValue);
        }
        
        if(ms.flyModeSetted)
        {
            if(!inquirState.airplaneState()){
                mflySwithing=true;
                switchState.airplaneState(true);
            }
        }
        ModeSettings.restoreSetting(mContext, mMode);
        if(!isDevSwitching()){
            noticeSwitchFinish(mContext);
        }
    }

    void swithUserMode(){
        PowerSavingMode.isExcuting=true;
        ModeDevStatus mds=new ModeDevStatus(mContext,mMode);
        InquirState inquirState = new InquirState(mContext,mMode);
	    SwitchState switchState = new SwitchState(mContext,mMode);
        mds.getStatus();

        if(inquirState.wifiState()!=mds.wifiOn){
             mWifiSwithing=true;
            switchState.wifiState(mds.wifiOn);
            
        }

        if(inquirState.gpsState()!=mds.gpsOn){
           switchState.gpsState(mds.gpsOn);
        }

        if(inquirState.bluetoothState()!=mds.bluetoothOn){
            if((!mds.bluetoothOn&&(Bluetooth.getState()==BluetoothAdapter.STATE_ON))||
                (mds.bluetoothOn&&(Bluetooth.getState()==BluetoothAdapter.STATE_OFF))){
                mBTSwithing=true;
                switchState.bluetoothState(mds.bluetoothOn);
            }
        }
        
        if(inquirState.mobileDataState()!=mds.dataOn){
            switchState.mobileDataState(mds.dataOn);
        }
  
        if(inquirState.hapticState()!=mds.hapticOn){
           switchState.hapticState(mds.hapticOn);
        }

        if(inquirState.syncState()!=mds.autoSyncOn){
           switchState.syncState(mds.autoSyncOn);
        }
        
        //switchState.lightOfScreenAutoState(mds.brightnessOn);
        if(!mds.brightnessOn){
            switchState.lightOfScreenValue(mds.brightnessValue);
        }
        switchState.lockScreenValue(mds.timeOutValue);
           
        if(inquirState.airplaneState()!=mds.flyModeOn){
            mflySwithing=true;
            switchState.airplaneState(mds.flyModeOn);
        }
         Log.i("lkr","Executer swithUserMode ");
        if(!isDevSwitching()){
            noticeSwitchFinish(mContext);
        }
    }

    private static boolean mflySwithing=false;
	private static boolean mWifiSwithing=false;
	private static boolean mGpsSwithing=false;
	private static boolean mBTSwithing=false;
	private static boolean mDataSwithing=false;
	private static boolean mHapticSwithing=false;
	private static boolean mAutoBrightSwithing=false;
	private static boolean mAutoSyncSwithing=false;

    
    public static boolean isDevSwitching(){
        boolean in=false;
        if(mflySwithing
            ||mWifiSwithing
            ||mGpsSwithing
            ||mBTSwithing
            ||mDataSwithing
            ||mHapticSwithing
            ||mAutoSyncSwithing){
            in =true;
        }
         //Log.i("lkr","isDevSwitching---in="+in);
        return in;
    }
    public static void setDevSwitchOver(Context ctx,int type){
        
        switch(type){
            case Constants.BREAK_CAUSE_FLYMODE:
                mflySwithing=false;
                break;
            case Constants.BREAK_CAUSE_WIFI:
                mWifiSwithing=false;
                break;
            case Constants.BREAK_CAUSE_GPS:
                mGpsSwithing=false;
                break;
            case Constants.BREAK_CAUSE_BLUETOOTH:
                 mBTSwithing=false;
                break;
            case Constants.BREAK_CAUSE_DATA:
                mDataSwithing=false;
                break;
            case Constants.BREAK_CAUSE_HAPITIC:
                mHapticSwithing=false;
                break;
            case Constants.BREAK_CAUSE_AUTOSYNC:
                mAutoSyncSwithing=false;
                break;
        }
        noticeSwitchFinish(ctx);
    }
    
    private static  void noticeSwitchFinish(Context ctx){
        
        if(!PowerSavingMode.isExcuting){
            return;
        }
        if(mflySwithing){
            return;
        }
        if(mWifiSwithing){
            return;
        }
        if(mGpsSwithing){
            return;
        }
        if(mBTSwithing){
            return;
        }
        if(mDataSwithing){
            return;
        }
        if(mHapticSwithing){
            return;
        }
        if(mAutoBrightSwithing){
            return;
        }
        if(mAutoSyncSwithing){
            return;
        }
        Log.i("lkr","Executer noticeSwitchFinish ");
        PowerSavingMode.isExcuting=false;
        Intent excuteFinish = new Intent(Constants.SPM_DEVS_SWITTCH_FINISH_ACTION);
        ctx.sendBroadcast(excuteFinish);
     }

}
