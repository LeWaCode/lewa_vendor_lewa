package com.lewa.spm.control;


import com.lewa.spm.util.Constants;

import android.content.Context;
import android.net.Uri;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class Brightness extends SelectControl {

	public Brightness(int mode, Context mContext) {
		super(mode, mContext);
	}

	
	public void adjust(int value) {
		try {			
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                if(power.isScreenOn()){
                    power.setBacklightBrightness(value + Constants.MINIMUM_BACKLIGHT);
                }
                Settings.System.putInt(mContext.getContentResolver() , Settings.System.SCREEN_BRIGHTNESS, value + Constants.MINIMUM_BACKLIGHT);
            }
        } catch (RemoteException e) {
        }		
	}

	
	public void change(boolean closeOrOpen) {
		if(closeOrOpen){// automatic adjustment 
			 Settings.System.putInt(mContext.getContentResolver(),
	             Settings.System.SCREEN_BRIGHTNESS_MODE,
	                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		}else{
			Settings.System.putInt(mContext.getContentResolver(),
	                Settings.System.SCREEN_BRIGHTNESS_MODE,
	                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);	
		}
		Uri uri = android.provider.Settings.System.getUriFor(android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
		mContext.getContentResolver().notifyChange(uri, null);
	}

	
	public boolean isOnOff() {
		boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
	}

	
	public int getAdjustValue() {
		int nowBrightnessValue = 10;
        try {
			nowBrightnessValue = android.provider.Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return nowBrightnessValue - Constants.MINIMUM_BACKLIGHT;
	}
}
