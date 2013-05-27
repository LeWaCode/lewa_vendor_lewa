package com.lewa.spm;

import java.util.ArrayList;
import java.util.Calendar;

import com.lewa.spm.element.ConsumeValue;
import com.lewa.spm.element.EnergyPreference;
import android.preference.*;
import com.lewa.spm.utils.PrefUtils;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;

public class SetPowerActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private static final String TAG = "SetPowerActivity";

	/** If there is no setting in the provider, use this. */
	private final String KEY_ENABLE_PREF = "enable_power_mode_pref";
    private final String KEY_LOW_POWER_PREF = "low_power_pref";
	
    private CheckBoxPreference mEnablePref;
    private EnergyPreference mEnergyPref;
	
    private boolean powerEnabled = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setintelpower);
	}
	
	private void initUI(){

		powerEnabled = getEnableState();
		
		mEnablePref = (CheckBoxPreference) findPreference(KEY_ENABLE_PREF);
		mEnablePref.setChecked(powerEnabled);
		
		mEnergyPref = (EnergyPreference) findPreference(KEY_LOW_POWER_PREF);
		mEnergyPref.setSummary(getString(R.string.spm_change_mode_by_power_value_summary)
				+PrefUtils.getInstance(this).getIntelLowPower()
				+getString(R.string.spm_change_mode_by_power_value_append));//energy level is 20%

		mEnergyPref.setEnabled(powerEnabled);//this function is fine!
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		initUI();
	}
	
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        Log.i(TAG, "onSharedPreferenceChanged key="+key);
        PrefUtils.getInstance(this).setIntelPowerBit(ConsumeValue.CLEAR_BIT);
    	if (key.equals(KEY_ENABLE_PREF)) {
            
    		boolean powerEnabled = preferences.getBoolean(key, true);
            
    		PrefUtils.getInstance(this).setModeOn(PrefUtils.INTEL_MODE_POWER_ON, powerEnabled);
            
    		mEnergyPref.setEnabled(powerEnabled);
    		
    		if(powerEnabled)
    			PrefUtils.getInstance(this).setIntelPowerBit(0);
            
        }else if (key.equals(KEY_LOW_POWER_PREF)) {
        	
        	mEnergyPref.setSummary(getString(R.string.spm_change_mode_by_power_value_summary)
    				+PrefUtils.getInstance(this).getIntelLowPower()
    				+getString(R.string.spm_change_mode_by_power_value_append));//energy level is 20%
        	PrefUtils.getInstance(this).setIntelPowerBit(0);
        }
    }
    
    private boolean getEnableState() {
    	return PrefUtils.getInstance(this).getModeOn(PrefUtils.INTEL_MODE_POWER_ON);        
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
			mEnergyPref.setSummary(getString(R.string.spm_change_mode_by_power_value_summary)
    				+PrefUtils.getInstance(this).getIntelLowPower()
    				+getString(R.string.spm_change_mode_by_power_value_append));//energy level is 20%
		}
		super.onWindowFocusChanged(hasFocus);
	}
}