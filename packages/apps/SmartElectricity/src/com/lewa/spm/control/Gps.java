/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.spm.control;

import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings.Secure;

public class Gps extends SwitchControl{

	public Gps(int mode, Context mContext) {
		super(mode, mContext);
	}

	
	public void change(boolean closeOrOpen) {
		Secure.setLocationProviderEnabled(mContext.getContentResolver(),
				LocationManager.GPS_PROVIDER, closeOrOpen);
	}

	
	@Override
	public void change() {
		// TODO Auto-generated method stub
		super.change();
	}


	public boolean isOnOff() {
		LocationManager myLocationManager = (LocationManager )mContext.getSystemService(Context.LOCATION_SERVICE);
		return myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
}
