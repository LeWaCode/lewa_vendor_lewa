/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class ApplicationSettings extends PreferenceActivity implements
        DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_APP_INSTALL_LOCATION = "app_install_location";
    private static final String KEY_QUICK_LAUNCH = "quick_launch";

    // Begin, Added by zhumeiquan, change req, 20111120
    private static final String ENABLE_ADB = "enable_adb";    
    private static final String ADB_NOTIFY = "adb_notify";    
    private static final String KEEP_SCREEN_ON = "keep_screen_on";    
    private static final String ALLOW_MOCK_LOCATION = "allow_mock_location";    
    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String MOVE_ALL_APPS_PREF = "pref_move_all_apps";

    private CheckBoxPreference mEnableAdb;
    private CheckBoxPreference mAdbNotify;
    private CheckBoxPreference mKeepScreenOn;
    private CheckBoxPreference mAllowMockLocation;
    private CheckBoxPreference mKillAppLongpressBack;
    private CheckBoxPreference mMoveAllAppsPref;

    // To track whether Yes was clicked in the adb warning dialog
    private boolean mOkClicked;

    private Dialog mOkDialog;
	// End

    // App installation location. Default is ask the user.
    private static final int APP_INSTALL_AUTO = 0;
    private static final int APP_INSTALL_DEVICE = 1;
    private static final int APP_INSTALL_SDCARD = 2;
    
    private static final String APP_INSTALL_DEVICE_ID = "device";
    private static final String APP_INSTALL_SDCARD_ID = "sdcard";
    private static final String APP_INSTALL_AUTO_ID = "auto";
    
    private CheckBoxPreference mToggleAppInstallation;

    private ListPreference mInstallLocation;

    private DialogInterface mWarnInstallApps;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.application_settings);

        // Begin, Added by zhumeiquan, change req, 2011119
        mEnableAdb = (CheckBoxPreference) findPreference(ENABLE_ADB);
        mAdbNotify = (CheckBoxPreference) findPreference(ADB_NOTIFY);
        mKeepScreenOn = (CheckBoxPreference) findPreference(KEEP_SCREEN_ON);
        mAllowMockLocation = (CheckBoxPreference) findPreference(ALLOW_MOCK_LOCATION);
        mKillAppLongpressBack = (CheckBoxPreference) findPreference(KILL_APP_LONGPRESS_BACK);

        mMoveAllAppsPref = (CheckBoxPreference) findPreference(MOVE_ALL_APPS_PREF);        
        mMoveAllAppsPref.setChecked(Settings.Secure.getInt(getContentResolver(),            
        	Settings.Secure.ALLOW_MOVE_ALL_APPS_EXTERNAL, 0) == 1);
        // End
		
        mToggleAppInstallation = (CheckBoxPreference) findPreference(KEY_TOGGLE_INSTALL_APPLICATIONS);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());

        mInstallLocation = (ListPreference) findPreference(KEY_APP_INSTALL_LOCATION);
        // Is app default install location set?
        boolean userSetInstLocation = (Settings.System.getInt(getContentResolver(),
                Settings.Secure.SET_INSTALL_LOCATION, 0) != 0);
        if (!userSetInstLocation) {
            getPreferenceScreen().removePreference(mInstallLocation);
        } else {
            mInstallLocation.setValue(getAppInstallLocation());
            mInstallLocation.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String value = (String) newValue;
                    handleUpdateAppInstallLocation(value);
                    return false;
                }
            });
        }
				
        /*
        if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_NOKEYS) {
            // No hard keyboard, remove the setting for quick launch
            Preference quickLaunchSetting = findPreference(KEY_QUICK_LAUNCH);
            getPreferenceScreen().removePreference(quickLaunchSetting);
        }
        */
    }

    // Begin, Added by zhumeiquan, change req, 2011119
    @Override
    protected void onResume() {
        super.onResume();

        mEnableAdb.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0) != 0);
        
        mAdbNotify.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ADB_NOTIFY, 0) != 0);
                
        mKeepScreenOn.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STAY_ON_WHILE_PLUGGED_IN, 0) != 0);
        mAllowMockLocation.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0);
        mKillAppLongpressBack.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) != 0);
    }
    // End
	
    protected void handleUpdateAppInstallLocation(final String value) {
        if(APP_INSTALL_DEVICE_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_DEVICE);
        } else if (APP_INSTALL_SDCARD_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_SDCARD);
        } else if (APP_INSTALL_AUTO_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        } else {
            // Should not happen, default to prompt...
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        }
        mInstallLocation.setValue(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
		
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }

        if (mOkDialog != null) {
            mOkDialog.dismiss();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMoveAllAppsPref) {            
            Settings.Secure.putInt(getContentResolver(),                
                Settings.Secure.ALLOW_MOVE_ALL_APPS_EXTERNAL, mMoveAllAppsPref.isChecked() ? 1 : 0);            
            return true;        
        } else if (preference == mToggleAppInstallation) {
            if (mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }
            return true;
        }
			
        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mOkClicked = false;
                if (mOkDialog != null) {
                    mOkDialog.dismiss();
                }
                mOkDialog = new AlertDialog.Builder(this).setMessage(
                        getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mOkDialog.setOnDismissListener(this);
            } else {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
            }
        } else if (preference == mAdbNotify) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_NOTIFY,
                    mAdbNotify.isChecked() ? 1 : 0);
        } else if (preference == mKeepScreenOn) {
            Settings.System.putInt(getContentResolver(), Settings.System.STAY_ON_WHILE_PLUGGED_IN, 
                    mKeepScreenOn.isChecked() ? 
                    (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB) : 0);
        } else if (preference == mAllowMockLocation) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION,
                    mAllowMockLocation.isChecked() ? 1 : 0);
        } else if (preference == mKillAppLongpressBack) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.KILL_APP_LONGPRESS_BACK,
                    mKillAppLongpressBack.isChecked() ? 1 : 0);
        }
        return false;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnInstallApps && which == DialogInterface.BUTTON_POSITIVE) {
            setNonMarketAppsAllowed(true);
            mToggleAppInstallation.setChecked(true);
        } else if (dialog == mOkDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                mOkClicked = true;
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
            } else {
                // Reset the toggle
                mEnableAdb.setChecked(false);
            }
        }
    }

    // Begin, Added by zhumeiquan, req change, 20111119
    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (!mOkClicked) {
            mEnableAdb.setChecked(false);
        }
    }
    // End
	
    private void setNonMarketAppsAllowed(boolean enabled) {
        // Change the system setting
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 
                                enabled ? 1 : 0);
    }
    
    private boolean isNonMarketAppsAllowed() {
        return Settings.Secure.getInt(getContentResolver(), 
            Settings.Secure.INSTALL_NON_MARKET_APPS, 1) > 0;
    }

    private String getAppInstallLocation() {
        int selectedLocation = Settings.System.getInt(getContentResolver(),
                Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        if (selectedLocation == APP_INSTALL_DEVICE) {
            return APP_INSTALL_DEVICE_ID;
        } else if (selectedLocation == APP_INSTALL_SDCARD) {
            return APP_INSTALL_SDCARD_ID;
        } else  if (selectedLocation == APP_INSTALL_AUTO) {
            return APP_INSTALL_AUTO_ID;
        } else {
            // Default value, should not happen.
            return APP_INSTALL_AUTO_ID;
        }
    }

    private void warnAppInstallation() {
        mWarnInstallApps = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error_title))
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
