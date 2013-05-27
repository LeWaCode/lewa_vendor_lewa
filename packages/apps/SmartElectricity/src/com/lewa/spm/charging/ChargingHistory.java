package com.lewa.spm.charging;

import com.lewa.spm.util.SharedStorageKeyValuePair;

import android.content.Context;

public class ChargingHistory {

	Context mContext;
	SharedStorageKeyValuePair saveValue;
	// save battery is charging or not charging and target(first)
	final String SPM_BATTERY_STATUS_AC_CHARGING_SP_NAME = "battery_ac_charging_info";
	// save battery is charging or not charging and target(first)
	final String SPM_BATTERY_STATUS_USB_CHARGING_SP_NAME = "battery_usb_charging_info";
	long[] AC_time = { 10, 20, 16, 8, 6, 7, 10, 19, 15, 26, 30, 25, 25, 19, 29,
			30, 18, 15, 30, 40, 15, 20, 25, 30, 19, 15, 25, 25, 45, 40, 10, 45,
			35, 40, 45, 45, 45, 45, 45, 45, 45, 45, 60, 90, 60, 90, 14, 29, 16,
			14, 16, 14, 60, 15, 43, 60, 46, 59, 74, 76, 163, 106, 104, 89, 105,
			60, 193, 16, 60, 103, 106, 104, 119, 76, 119, 134, 136, 59, 105,
			74, 59, 75, 90, 29, 30, 43, 15, 44, 29, 30, 29, 30, 29, 46, 29, 44,
			15, 30, 30, 180, 2429 };
	long[] USB_time = { 74, 74, 74, 74, 74, 74, 74, 74, 74, 64, 169, 169, 129,
			190, 230, 150, 180, 190, 150, 170, 210, 138, 89, 89, 150, 169, 150,
			140, 150, 150, 160, 160, 160, 170, 130, 100, 259, 100, 149, 200,
			199, 140, 180, 170, 100, 200, 70, 140, 99, 100, 79, 170, 160, 150,
			140, 200, 160, 150, 179, 180, 88, 210, 179, 150, 179, 110, 200,
			169, 160, 130, 190, 179, 70, 150, 60, 60, 210, 170, 150, 160, 160,
			169, 150, 169, 159, 170, 150, 160, 149, 179, 169, 149, 138, 140,
			70, 130, 110, 80, 330, 200, 3446 };

	public ChargingHistory(Context ctx) {
		mContext = ctx;
	}

	public void saveAc() {// Battery is use AC charging
		saveBatteryBasedLevel(SPM_BATTERY_STATUS_AC_CHARGING_SP_NAME, AC_time);
	}

	public void saveUsb() {// Battery is use USB charging
		saveBatteryBasedLevel(SPM_BATTERY_STATUS_USB_CHARGING_SP_NAME, USB_time);
	}

	private void saveBatteryBasedLevel(String spName, long[] time) {
		saveValue = new SharedStorageKeyValuePair(mContext);
		for (int i = 1; i < 101; i++) {
			saveValue.saveLong(spName, String.valueOf(i), time[i]);
		}
	}
}
