/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

public class DisplaySettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_ANIMATIONS = "animations";
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_BATTERY_STYLE = "battery_styles";
    private static final String ELECTRON_BEAM_ANIMATION_ON = "electron_beam_animation_on";
    private static final String ELECTRON_BEAM_ANIMATION_OFF = "electron_beam_animation_off";    
    //Begin added by panqianbo for #4890
    private static final String KEY_ALERTDIALOG = "alterdialogstyle";
    private ListPreference mAlertDiaog;
    private String[] mAlertDiaogStyles = null;
	private static final String KEY_NIGHTMODE = "nightmode";
	private ListPreference mNightMode;
	private String[] mNightColors = null; 
    //End

    private ListPreference mAnimations;
    private CheckBoxPreference mAccelerometer;
    private CheckBoxPreference mElectronBeamAnimationOn;
    private CheckBoxPreference mElectronBeamAnimationOff;    
    private float[] mAnimationScales;
    private ListPreference mBatteryStylePref;
    
    private String[] mBatteryStyles;

    private IWindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

        addPreferencesFromResource(R.xml.display_settings);

        mAnimations = (ListPreference) findPreference(KEY_ANIMATIONS);
        mAnimations.setOnPreferenceChangeListener(this);
        mAccelerometer = (CheckBoxPreference) findPreference(KEY_ACCELEROMETER);
        mAccelerometer.setPersistent(false);
        //Begin added by panqianbo for #4890
        mAlertDiaog = (ListPreference) findPreference(KEY_ALERTDIALOG);
        mAlertDiaog.setOnPreferenceChangeListener(this);
        mAlertDiaogStyles = getResources().getStringArray(R.array.alertdialog_sytle);
		mNightMode = (ListPreference) findPreference(KEY_NIGHTMODE);
		mNightMode.setOnPreferenceChangeListener(this);
		mNightColors = getResources().getStringArray(R.array.render_entries);
        //End

        /* Electron Beam control */
        Resources res = getResources();
        mElectronBeamAnimationOn = (CheckBoxPreference)findPreference(ELECTRON_BEAM_ANIMATION_ON);
        mElectronBeamAnimationOff = (CheckBoxPreference)findPreference(ELECTRON_BEAM_ANIMATION_OFF);
        if (res.getBoolean(com.android.internal.R.bool.config_enableScreenAnimation)) {
            mElectronBeamAnimationOn.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_ON,
                    res.getBoolean(com.android.internal.R.bool.config_enableScreenOnAnimation) ? 1 : 0) == 1);
            mElectronBeamAnimationOff.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_OFF,
                    res.getBoolean(com.android.internal.R.bool.config_enableScreenOffAnimation) ? 1 : 0) == 1);
        }
        
        if ("U880".equalsIgnoreCase(Build.MODEL)) {
            getPreferenceScreen().removePreference(mElectronBeamAnimationOn);
            getPreferenceScreen().removePreference(mElectronBeamAnimationOff);
            getPreferenceScreen().removePreference(mNightMode);
        }

        ListPreference screenTimeoutPreference =
            (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        screenTimeoutPreference.setValue(String.valueOf(Settings.System.getInt(
                resolver, SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE)));
        screenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(screenTimeoutPreference);

        mBatteryStylePref = (ListPreference) findPreference(KEY_BATTERY_STYLE);
        mBatteryStylePref.setOnPreferenceChangeListener(this);
        mBatteryStyles = getResources().getStringArray(R.array.battery_style_entries);
        int value = Settings.System.getInt(getContentResolver(), 
                Settings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mBatteryStylePref.setValue(value + "");
        mBatteryStylePref.setSummary(mBatteryStyles[value]);
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
            (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.valueOf(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            final int userPreference = Integer.valueOf(screenTimeoutPreference.getValue());
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateState(true);
    }

    private void updateState(boolean force) {
        int animations = 0;
        try {
            mAnimationScales = mWindowManager.getAnimationScales();
        } catch (RemoteException e) {
        }
        if (mAnimationScales != null) {
            if (mAnimationScales.length >= 1) {
                animations = ((int)(mAnimationScales[0]+.5f)) % 10;
            }
            if (mAnimationScales.length >= 2) {
                animations += (((int)(mAnimationScales[1]+.5f)) & 0x7) * 10;
            }
        }
        int idx = 0;
        int best = 0;
        CharSequence[] aents = mAnimations.getEntryValues();
        for (int i=0; i<aents.length; i++) {
            int val = Integer.parseInt(aents[i].toString());
            if (val <= animations && val > best) {
                best = val;
                idx = i;
            }
        }
        mAnimations.setValueIndex(idx);
        updateAnimationsSummary(mAnimations.getValue());
		//Begin added by panqianbo for #4890
		int val = Integer.valueOf(mAlertDiaog.getValue());
		/*int value = Settings.System.getInt(getContentResolver(),
                        Settings.System.ALERTDIALOG_STYLES, 0);*/
		
		int val_night = Integer.valueOf(mNightMode.getValue());
		
		int value111 = Settings.System.getInt(getContentResolver(),
                Settings.System.NIGHT_MODES, 0);
		

		try {   
		    if (mAlertDiaogStyles != null && val >= 0
				    && val < mAlertDiaogStyles.length) {
			    mAlertDiaog.setSummary(mAlertDiaogStyles[val]);
	        }
		    
		    if (mNightColors != null) {
		    	val_night = Math.max(0, val_night);
		    	val_night = Math.min(val_night, mNightColors.length-1);
			    mNightMode.setSummary(mNightColors[val_night]);
	        }
		} catch (NumberFormatException e) {
                Log.e(TAG, "could not persist AlertDiaogStyles setting", e);
        }	
		//End
        mAccelerometer.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) != 0);
    }

    private void updateAnimationsSummary(Object value) {
        CharSequence[] summaries = getResources().getTextArray(R.array.animations_summaries);
        CharSequence[] values = mAnimations.getEntryValues();
        for (int i=0; i<values.length; i++) {
            //Log.i("foo", "Comparing entry "+ values[i] + " to current "
            //        + mAnimations.getValue());
            if (values[i].equals(value)) {
                mAnimations.setSummary(summaries[i]);
                break;
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAccelerometer) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION,
                    mAccelerometer.isChecked() ? 1 : 0);        
            return true;
        }

        if (preference == mElectronBeamAnimationOn) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_ON, 
                    mElectronBeamAnimationOn.isChecked() ? 1 : 0);
            return true;
        }

        if (preference == mElectronBeamAnimationOff) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_OFF, 
                    mElectronBeamAnimationOff.isChecked() ? 1 : 0);
            return true;            
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_ANIMATIONS.equals(key)) {
            try {
                int value = Integer.parseInt((String) objValue);
                if (mAnimationScales.length >= 1) {
                    mAnimationScales[0] = value%10;
                }
                if (mAnimationScales.length >= 2) {
                    mAnimationScales[1] = (value/10)%10;
                }
                try {
                    mWindowManager.setAnimationScales(mAnimationScales);
                } catch (RemoteException e) {
                }
                updateAnimationsSummary(objValue);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist animation setting", e);
            }

        }
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(),
                        SCREEN_OFF_TIMEOUT, value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }

        if(KEY_BATTERY_STYLE.equals(key)) {
		int value = Integer.parseInt((String) objValue);
		try {		
		    Settings.System.putInt(getContentResolver(),
		            Settings.System.STATUS_BAR_BATTERY_STYLE, value);
		    preference.setSummary(mBatteryStyles[value]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }

        //Begin added by panqianbo for #4890
		if (KEY_ALERTDIALOG.equals(key)) {
			int val = Integer.valueOf((String) objValue);
			try {         	
				if (mAlertDiaogStyles != null && val >= 0
						&& val < mAlertDiaogStyles.length) {
					preference.setSummary(mAlertDiaogStyles[val]);
				}
				Settings.System.putInt(getContentResolver(),
                        Settings.System.ALERTDIALOG_STYLES, val);
			} catch (NumberFormatException e) {
                Log.e(TAG, "could not persist AlertDiaogStyles setting", e);
            }
        }
		if (KEY_NIGHTMODE.equals(key)) {
			int val = Integer.valueOf((String) objValue);
			try {         	
				Settings.System.putInt(getContentResolver(),
                        Settings.System.NIGHT_MODES, val);
				if (mNightColors != null) {
					val = Math.max(0, val);
					val = Math.min(val, mNightColors.length-1);
					preference.setSummary(mNightColors[val]);
				}
			} catch (NumberFormatException e) {
                Log.e(TAG, "could not persist AlertDiaogStyles setting", e);
            }
			RenderFXServiceRunning(this);
        }
        //End
        return true;
    }
    private boolean RenderFXServiceRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> svcList = am.getRunningServices(100);

        if (!(svcList.size() > 0))
            return false;

        for (RunningServiceInfo serviceInfo : svcList) {
            if (serviceInfo.service.getClassName().endsWith(".RenderFXService")) {
            	Intent intent = new Intent();
            	intent.setComponent(serviceInfo.service);
            	startService(intent);
                return true;
            }
        }
        return false;
    }
}
