package com.lewa.spm.control;

import android.content.Context;
import android.net.ConnectivityManager;

public class Mobile extends SwitchControl{
	
	private Context mContext;
	
	public Mobile(int mode, Context mContext) {
		super(mode, mContext);
		this.mContext = mContext;
	}
	
	
	public void change(boolean closeOrOpen) {
		ConnectivityManager connectManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE); 
		connectManager.setMobileDataEnabled(closeOrOpen);   
	}

	
	public boolean isOnOff() {
//		return false;
		ConnectivityManager connectManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectManager.getMobileDataEnabled();
	}
}
