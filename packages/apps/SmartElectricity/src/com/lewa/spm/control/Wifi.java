/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.spm.control;

import android.content.Context;
import android.net.wifi.WifiManager;

public class Wifi extends SwitchControl{

	public Wifi(int mode, Context ctx) {
		super(mode, ctx);
	}


	public void change(boolean closeOrOpen) {
		
		((WifiManager) this.mContext.getSystemService("wifi")) 
		.setWifiEnabled(closeOrOpen);
	}

	

	@Override
	public void change() {
		// TODO Auto-generated method stub
		super.change();
	}


	public boolean isOnOff() {
		WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		boolean flag = false;
		switch (wm.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			flag = false;
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			flag = false;
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			flag = true;
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			flag = true;
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			break;
		default:
			break;
		}
		return flag;
	
	}

	public boolean getWifiState() {
		WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		boolean flag = false;
		switch (wm.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			flag = false;
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			flag = false;
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			flag = true;
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			flag = true;
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			break;
		default:
			break;
		}
		return flag;
	}

}
