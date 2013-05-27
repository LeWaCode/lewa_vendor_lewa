/*
 * Copyright (c) 2011 LewaTek
 * All rights reserved.
 * 
 * DESCRIPTION:
 *
 * WHEN          | WHO               | what, where, why
 * --------------------------------------------------------------------------------
 * 2012-05-09  | shenqi         | Create file
 */

package com.lewa.PIM.settings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import android.preference.ListPreference;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lewa.PIM.R;
import com.lewa.PIM.sim.SimCard;
import com.lewa.PIM.util.CommonMethod;

public class CallLogSettingsActivity extends PreferenceActivity
	implements    Preference.OnPreferenceChangeListener{


    private static final String CALL_LOG_CLICK_OPTION    = "call_log_click_option_key";
    private ListPreference mCallLogClickSetting;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.calllog_settings);
        mCallLogClickSetting = (ListPreference) findPreference(CALL_LOG_CLICK_OPTION);
	 mCallLogClickSetting.setOnPreferenceChangeListener(this);
	 
	 mPrefs = getSharedPreferences("call_log_settings", Context.MODE_PRIVATE);
	 
        int mClickOption = mPrefs.getInt("call_log_click_option", 0);
	 mCallLogClickSetting.setValueIndex(mClickOption);
	 updatePreferredCallLogOptionSummary(mClickOption);		
    }

   public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mCallLogClickSetting) {
            handleCallLogClickSettingChange(preference, objValue);
        } 
        // always let the preference setting proceed.
        return true;
    }

     private void handleCallLogClickSettingChange(Preference preference, Object objValue) {
		int callLogClickOption;
		
		 mPrefs = getSharedPreferences("call_log_settings", Context.MODE_PRIVATE);
		 Editor editor = mPrefs.edit();
		
		callLogClickOption = Integer.valueOf((String) objValue).intValue();
		
	 	editor.putInt("call_log_click_option", callLogClickOption).commit();

		updatePreferredCallLogOptionSummary(callLogClickOption);
      }
	 
	 private void updatePreferredCallLogOptionSummary(int callLogClickOption) {
        	String [] txts = getResources().getStringArray(R.array.call_log_click_entries);

                mCallLogClickSetting.setSummary(txts[callLogClickOption]);
        }
}
