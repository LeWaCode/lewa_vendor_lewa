package com.lewa.spm.control;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class ControlAirPlane extends SwitchControl{
	
	public ControlAirPlane(int mode, Context ctx) {
		super(mode, ctx);
	}


	public void change(boolean closeOrOpen) {
	    if (closeOrOpen != isOnOff()){
	        Settings.System.putInt(mContext.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, closeOrOpen ? 1 : 0);
	        Log.d("Tt", "spm ----airplane----closeOrOpen = " + closeOrOpen);
	        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
	        intent.putExtra("state", closeOrOpen);
	        mContext.sendBroadcast(intent);	
	    }
	}

	public boolean isOnOff() {
		return (Settings.System.getInt(mContext.getContentResolver() , Settings.System.AIRPLANE_MODE_ON,0)==1);	
	}
}
