package com.lewa.store.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * 网络辅助
 * 
 * @author ypzhu
 * 
 */
public class NetHelper {

	/**
	 * Wifi check
	 * 
	 * @param activitiy
	 * @return
	 */
	public static boolean checkWifi(Activity activitiy) {
		WifiManager mWifiManager = (WifiManager) activitiy
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
		if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
			// System.out.println("**** WIFI is on");
			return true;
		} else {
			// System.out.println("**** WIFI is off");
			return false;
		}
	}
	
	/**
     * Wifi check
     * 
     * @param activitiy
     * @return
     */
    public static boolean checkWifi(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
            // System.out.println("**** WIFI is on");
            return true;
        } else {
            // System.out.println("**** WIFI is off");
            return false;
        }
    }

	/*
	 * 是否有可用网络(包括所有网络)
	 */
	public static boolean isAccessNetwork(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netWorkInfo = connectivity.getActiveNetworkInfo();
		if (netWorkInfo != null && netWorkInfo.isAvailable()) {
			return true;
		}
		return false;
	}
}
