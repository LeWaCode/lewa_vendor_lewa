package com.lewa.spm.control;

import com.lewa.spm.util.Constants;

import android.content.Context;

public class SwitchState {
	public Context mContext;
    int mMode;
	
	public SwitchState(Context ctx,int mode) {
		mContext = ctx;
        mMode=mode;
	}

	public void bluetoothState(boolean devValue){
		Bluetooth  bt= new Bluetooth(mMode);
		bt.change(devValue);
	}
	
	public void syncState(boolean devValue){
		AutoSync  as= new AutoSync(mMode);
		as.change(devValue);
	}
	
	public void airplaneState(boolean devValue){
		ControlAirPlane flyMode= new ControlAirPlane(mMode, mContext);
		flyMode.change(devValue);
	}
	
	public void gpsState(boolean devValue){
		Gps  gps= new Gps(mMode, mContext);
		gps.change(devValue);
	}
	
	public void hapticState(boolean devValue){
		Haptic h= new Haptic(mMode, mContext);
		h.change(devValue);
	}
	
	public void wifiState(boolean devValue){
		Wifi  wf= new Wifi(mMode, mContext);
		wf.change(devValue);
	}
	
	public void mobileDataState(boolean devValue){
		Mobile m= new Mobile(mMode, mContext);
		m.change(devValue);
	}
	
	public void lightOfScreenAutoState(boolean devValue){
		Brightness  b= new Brightness(mMode, mContext);
		b.change(devValue);
	}
	
	public void lightOfScreenValue(int brightNum){
		Brightness b= new Brightness(mMode, mContext);
		b.adjust(brightNum);
	}
	
	public void lockScreenValue(int lockNum){
		Lockscreen  l= new Lockscreen(mMode, mContext);
		l.adjust(lockNum);
	}

}
