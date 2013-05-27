package com.lewa.spm.control;

import android.content.Context;
import android.provider.Settings;


public class Lockscreen extends ValueControl{
	
	public Lockscreen(int mode, Context mContext) {
		super(mode, mContext);
	}

	@Override
	public void adjust(int value) {
		 Settings.System.putInt(mContext.getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, value);
	}

	
	public int getAdjustValue() {
		return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,30000);
	}
}
