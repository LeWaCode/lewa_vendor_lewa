package com.lewa.spm.mode;

import android.content.Context;

import android.graphics.drawable.Drawable;

import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.control.InquirState;
import com.lewa.spm.control.SwitchState;

public class ModeDevStatus {
	public boolean flyModeOn;
	public boolean wifiOn;
	public boolean gpsOn;
	public boolean bluetoothOn;
	public boolean dataOn;
	public boolean hapticOn;
	public boolean keyBgLighOn;
	public boolean brightnessOn;
	public boolean autoSyncOn;
	public boolean lockClearMemoryOn;
	public int brightnessValue;
	public int timeOutValue;
	public int mMode;
    Context mContex;


    public ModeDevStatus(Context ctx,int mode){
            mContex=ctx;
            mMode=mode;
        }

    public int getMode(){
            return mMode;
        }



    public void saveStatus(){
        String fn=null;
        if(mMode==Constants.SPM_MODE_OUT_ID){
            fn=Constants.DEV_STATUS_STORE_OUT;
        }else if(mMode==Constants.SPM_MODE_LONG_ID){
            fn=Constants.DEV_STATUS_STORE_LONG;
        }else{
            fn=Constants.DEV_STATUS_STORE_INTERVAL;
        }
        SharedStorageKeyValuePair saveValue = new SharedStorageKeyValuePair(mContex);
		InquirState inquireState = new InquirState(mContex,Constants.SPM_MODE_OUT_ID);
		saveValue.saveBoolean(fn,Constants.STR_AIRPLANE, inquireState.airplaneState());
		saveValue.saveBoolean(fn,Constants.STR_BLUETOOTH, inquireState.bluetoothState());
		saveValue.saveBoolean(fn,Constants.STR_GPS, inquireState.gpsState());
		saveValue.saveBoolean(fn,Constants.STR_HAPTIC, inquireState.hapticState());
		saveValue.saveBoolean(fn,Constants.STR_MOBILE, inquireState.mobileDataState());
		saveValue.saveBoolean(fn,Constants.STR_SYNC, inquireState.syncState());
		saveValue.saveBoolean(fn,Constants.STR_WIFI, inquireState.wifiState());
		saveValue.saveBoolean(fn,Constants.STR_BRIGHTNESS,inquireState.lightOfScreenAutoState());
		saveValue.saveInt(fn,Constants.STR_BRIGHTNESS_VALUE,inquireState.lightOfScreenValue());
		saveValue.saveInt(fn,Constants.STR_LOCKSCREEN, inquireState.lockScreenValue());
        
    }

    public void getStatus(){
        String fn=null;
        if(mMode==Constants.SPM_MODE_OUT_ID){
            fn=Constants.DEV_STATUS_STORE_OUT;
        }else if(mMode==Constants.SPM_MODE_LONG_ID){
            fn=Constants.DEV_STATUS_STORE_LONG;
        }else{
            fn=Constants.DEV_STATUS_STORE_INTERVAL;
        }
        SharedStorageKeyValuePair saveValue = new SharedStorageKeyValuePair(mContex);
		InquirState inquireState = new InquirState(mContex,Constants.SPM_MODE_OUT_ID);
		flyModeOn=saveValue.getBoolean(fn,Constants.STR_AIRPLANE);
		bluetoothOn=saveValue.getBoolean(fn,Constants.STR_BLUETOOTH);
		gpsOn=saveValue.getBoolean(fn,Constants.STR_GPS);
		hapticOn=saveValue.getBoolean(fn,Constants.STR_HAPTIC);
		dataOn=saveValue.getBoolean(fn,Constants.STR_MOBILE);
		autoSyncOn=saveValue.getBoolean(fn,Constants.STR_SYNC);
		wifiOn=saveValue.getBoolean(fn,Constants.STR_WIFI);
		brightnessOn=saveValue.getBoolean(fn,Constants.STR_BRIGHTNESS);
		brightnessValue=saveValue.getInt(fn,
				Constants.STR_BRIGHTNESS_VALUE,
				inquireState.lightOfScreenValue());
		timeOutValue=saveValue.getInt(fn,
				Constants.STR_LOCKSCREEN, 
				inquireState.lockScreenValue());
    }

    
}
  