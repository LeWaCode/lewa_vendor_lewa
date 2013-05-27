package com.lewa.spm.util;

import com.lewa.spm.charging.ChargingHistory;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedStorageKeyValuePair {

	Context mContext;

	public SharedStorageKeyValuePair(Context ctx) {
		mContext = ctx;
	}

	public void saveString(String spName, String key, String value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void saveLong(String spName, String key, Long value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sPreferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public void saveInt(String spName, String key, int value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void saveBoolean(String spName, String key, Boolean value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor = sPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public String getString(String spName, String key, String value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_READABLE);
		return sPreferences.getString(key, value);
	}

	public long getLong(String spName, String key, long value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_READABLE);
		return sPreferences.getLong(key, value);
	}

	public int getInt(String spName, String key, int value) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_READABLE);
		int key_value;
		try {
			key_value = sPreferences.getInt(key, value);
		} catch (Exception e) {
			key_value = Integer.parseInt(sPreferences.getString(key,
					String.valueOf(value)));
			saveInt(spName, key, key_value);
			e.printStackTrace();
		}
		return key_value;
	}

	public Boolean getBoolean(String spName, String key) {
		SharedPreferences sPreferences = mContext.getSharedPreferences(spName,
				Context.MODE_WORLD_READABLE);
		return sPreferences.getBoolean(key, false);
	}
    //save history data
	public void saveChargingHistory() {
		ChargingHistory mSaveChargingHistory = new ChargingHistory(mContext);
		mSaveChargingHistory.saveAc();
		mSaveChargingHistory.saveUsb();
	}

	public void operate() {
		int versionNum = getInt(Constants.SHARED_PREFERENCE_NAME,
				                Constants.SPM_SHARED_PREFERENCE_VERSION_NAME,
				                0);
		if (versionNum != Constants.SPM_SHARED_PREFERENCE_VERSION_NUM) {
			SharedPreferences sPreferences = mContext.getSharedPreferences(
					Constants.SHARED_PREFERENCE_NAME,
					Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor editor = sPreferences.edit();
			editor.clear();
			editor.commit();
			if (versionNum == 0) {
				saveInt(Constants.SHARED_PREFERENCE_NAME,
						Constants.STR_MODE_TYPE_NAME, Constants.SPM_MODE_OUT_ID);
			}
			saveInt(Constants.SHARED_PREFERENCE_NAME,
					Constants.SPM_SHARED_PREFERENCE_VERSION_NAME,
					Constants.SPM_SHARED_PREFERENCE_VERSION_NUM);
		}
	}
}
