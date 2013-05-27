package com.lewa.spm.control;

import java.util.HashMap;
import java.util.Map;

import com.lewa.spm.util.Constants;

import android.content.Context;

public class DeviceStatusMap {
	Context mContext;
    private int mMode;
	InquirState mInquirState;
	public Map<String, Object> mDevStatuses;

	public DeviceStatusMap(Context ctx,int mode) {
		mContext = ctx;
        mMode=mode;
	}
	
	public synchronized Map<String, Object> getAllStatus(){
		mDevStatuses = new HashMap<String, Object>();
		mInquirState = new InquirState(mContext,mMode);
		mDevStatuses.put(Constants.STR_AIRPLANE, mInquirState.airplaneState());
		mDevStatuses.put(Constants.STR_WIFI, mInquirState.wifiState());
		mDevStatuses.put(Constants.STR_MOBILE, mInquirState.mobileDataState());
		if (mInquirState.lightOfScreenAutoState() == false){
			mDevStatuses.put(Constants.STR_BRIGHTNESS_VALUE, mInquirState.lightOfScreenValue());
		}
		mDevStatuses.put(Constants.STR_BRIGHTNESS, mInquirState.lightOfScreenAutoState());
		mDevStatuses.put(Constants.STR_LOCKSCREEN, mInquirState.lockScreenValue());
		mDevStatuses.put(Constants.STR_GPS, mInquirState.gpsState());
		mDevStatuses.put(Constants.STR_HAPTIC, mInquirState.hapticState());
		mDevStatuses.put(Constants.STR_BLUETOOTH, mInquirState.bluetoothState());
		mDevStatuses.put(Constants.STR_SYNC, mInquirState.syncState());
		return mDevStatuses;
	}
	
	public synchronized void release(){
		if (mDevStatuses != null){
			mDevStatuses.clear();
			mDevStatuses = null;
		}
	}
}
