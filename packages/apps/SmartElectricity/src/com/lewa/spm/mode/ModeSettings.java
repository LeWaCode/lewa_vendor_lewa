package com.lewa.spm.mode;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.control.InquirState;
import com.lewa.spm.control.SwitchState;

public class ModeSettings {
	public boolean flyModeSetted;
	public boolean wifiSetted;
	public boolean gpsSetted;
	public boolean bluetoothSetted;
	public boolean dataSetted;
	public boolean hapticSetted;
	public boolean keyBgLightSetted;
	public boolean brightnessSetted;
	public boolean autoSyncSetted;
	public boolean lockClearMemorySetted;
	public int brightnessValue;
	public int timeOutValue;
	public int mMode;

    Context mContex;

    public ModeSettings(Context ctx,int mode){
            mContex=ctx;
            mMode=mode;
        }

    public int getMode(){
        return mMode;
        }


    public void saveSettings(){
        String fn=null;
        if(mMode==Constants.SPM_MODE_OUT_ID){
            fn=Constants.DEV_SETTINGS_STORE_OUT;
        }else if(mMode==Constants.SPM_MODE_LONG_ID){
            fn=Constants.DEV_SETTINGS_STORE_LONG;
        }else{
            fn=Constants.DEV_SETTINGS_STORE_INTERVAL;
        }
        SharedStorageKeyValuePair saveValue = new SharedStorageKeyValuePair(mContex);
		saveValue.saveBoolean(fn,
				Constants.STR_AIRPLANE,
				flyModeSetted);
		saveValue.saveBoolean(fn,
				Constants.STR_BLUETOOTH,
				bluetoothSetted);
		saveValue.saveBoolean(fn,
				Constants.STR_GPS,
				gpsSetted);
		saveValue.saveBoolean(fn,
				Constants.STR_HAPTIC,
				hapticSetted);
		saveValue.saveBoolean(fn,
				Constants.STR_MOBILE,
				dataSetted);
		saveValue.saveBoolean(fn,
				Constants.STR_SYNC, autoSyncSetted);
		saveValue.saveBoolean(fn,
				Constants.STR_WIFI, wifiSetted);
		saveValue.saveBoolean(fn,
						Constants.STR_BRIGHTNESS,
						brightnessSetted);
		saveValue.saveInt(fn,
				Constants.STR_BRIGHTNESS_VALUE,
				brightnessValue);
		saveValue.saveInt(fn,
				Constants.STR_LOCKSCREEN, 
				timeOutValue);
        
    }

    public void getSettings(){
        String fn=null;
        if(mMode==Constants.SPM_MODE_OUT_ID){
            fn=Constants.DEV_SETTINGS_STORE_OUT;
        }else if(mMode==Constants.SPM_MODE_LONG_ID){
            fn=Constants.DEV_SETTINGS_STORE_LONG;
        }else{
            fn=Constants.DEV_SETTINGS_STORE_INTERVAL;
        }
        SharedStorageKeyValuePair saveValue = new SharedStorageKeyValuePair(mContex);
		InquirState inquireState = new InquirState(mContex,Constants.SPM_MODE_OUT_ID);
		flyModeSetted=saveValue.getBoolean(fn,Constants.STR_AIRPLANE);
		bluetoothSetted=saveValue.getBoolean(fn,Constants.STR_BLUETOOTH);
		gpsSetted=saveValue.getBoolean(fn,Constants.STR_GPS);
		hapticSetted=saveValue.getBoolean(fn,Constants.STR_HAPTIC);
		dataSetted=saveValue.getBoolean(fn,Constants.STR_MOBILE);
		autoSyncSetted=saveValue.getBoolean(fn,Constants.STR_SYNC);
		wifiSetted=saveValue.getBoolean(fn,Constants.STR_WIFI);
		brightnessSetted=saveValue.getBoolean(fn,Constants.STR_BRIGHTNESS);
		brightnessValue=saveValue.getInt(fn,Constants.STR_BRIGHTNESS_VALUE,inquireState.lightOfScreenValue());
		timeOutValue=saveValue.getInt(fn,Constants.STR_LOCKSCREEN, inquireState.lockScreenValue());
    }

    public void getSettingsFromDevStatus(ModeDevStatus status){
        flyModeSetted=status.flyModeOn;
		bluetoothSetted=status.bluetoothOn;
		gpsSetted=status.gpsOn;
		hapticSetted=status.hapticOn;
		dataSetted=status.dataOn;
		autoSyncSetted=status.autoSyncOn;
		wifiSetted=status.wifiOn;
		brightnessSetted=status.brightnessOn;
		brightnessValue=status.brightnessValue;
		timeOutValue=status.timeOutValue;
    }

    public static int restore_cause=Constants.BREAK_CAUSE_NONE;
    public static void restoreSetting(Context ctx,int mode){
        if(restore_cause==Constants.BREAK_CAUSE_NONE){
            return;
        }
        ModeSettings ms=new ModeSettings(ctx,mode);
        ms.getSettings();

         switch(restore_cause){
            case Constants.BREAK_CAUSE_FLYMODE:
                ms.flyModeSetted=true;
                break;
            case Constants.BREAK_CAUSE_WIFI:
                ms.wifiSetted=true;
                break;
            case Constants.BREAK_CAUSE_GPS:
                ms.gpsSetted=true;
                break;
            case Constants.BREAK_CAUSE_BLUETOOTH:
                ms.bluetoothSetted=true;
                break;
            case Constants.BREAK_CAUSE_DATA:
                 ms.dataSetted=true;
                break;
            case Constants.BREAK_CAUSE_HAPITIC:
                ms.hapticSetted=true;
                break;
            case Constants.BREAK_CAUSE_AUTOSYNC:
                ms.autoSyncSetted=true;
                break;
        }
        ms.saveSettings();
        restore_cause=Constants.BREAK_CAUSE_NONE;
        
    }

   

}
  
