package com.lewa.store.adapter;

import com.lewa.store.utils.Constants;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences tools
 */
public class PreferencesHelper {

	private SharedPreferences sp;
	private SharedPreferences.Editor editor;

	private Context context;

	public PreferencesHelper(Context c, String name) {
		context = c;
		sp = context.getSharedPreferences(name, 0);
		editor = sp.edit();
	}

	public void putValue(String key, String value) {
		editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void putValue(String key, float value) {
		editor = sp.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public void putValue(String key, boolean value) {
		editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void putValue(String key, int value) {
		editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public String getStringValue(String key) {
		return sp.getString(key, null);
	}
	
	public String getStringNetValue(String key) {
		return sp.getString(key,"0");
	}

	public Float getFloatValue(String key) {
		return sp.getFloat(key, 0);
	}

	public boolean getBooleanValue(String key) {
		return sp.getBoolean(key, false);
	}

	public int getIntValue(String key) {
		return sp.getInt(key, Constants.SETTING_NETWORK_WIFI_OPEN);
	}
}
