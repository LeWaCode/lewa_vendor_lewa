package com.lewa.spm.control;

import com.lewa.spm.util.Constants;

import android.content.Context;

public class InquirState {
    private int mMode;
	public Context mContext;
	
	public InquirState(Context ctx,int mode) {
        mMode=mode;
		mContext = ctx;
	}

	public boolean bluetoothState(){
		Bluetooth bt= new Bluetooth(mMode);
		return bt.isOnOff();
	}
	
	public boolean airplaneState(){
		ControlAirPlane  flyMode= new ControlAirPlane(mMode, mContext);
		return flyMode.isOnOff();
	}
	
	public boolean gpsState(){
		Gps g= new Gps(mMode, mContext);
		return g.isOnOff();
	}
	
	public boolean hapticState(){
		Haptic h= new Haptic(mMode, mContext);
		return h.isOnOff();
	}
	
	public boolean wifiState(){
		Wifi wf= new Wifi(mMode, mContext);
		return wf.isOnOff();
	}
	
	public boolean mobileDataState(){
		Mobile m= new Mobile(mMode, mContext);
		return m.isOnOff();
	}
	
	public boolean lightOfScreenAutoState(){
		Brightness b= new Brightness(mMode, mContext);
		return b.isOnOff();
	}
	
	public int lightOfScreenValue(){
		Brightness b= new Brightness(mMode, mContext);
		return b.getAdjustValue();
	}
	
	public int lockScreenValue(){
		Lockscreen l= new Lockscreen(mMode, mContext);
		return l.getAdjustValue();
	}
	
	public boolean syncState(){
		AutoSync a= new AutoSync(mMode);
		return a.isOnOff();
	}
}
