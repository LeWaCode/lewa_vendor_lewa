package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.PreferenceActivity;

import com.android.settings.R;

public class LewaPhoneSettings extends PreferenceActivity {
    private Preference mBlockSettings;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.lewa_phone_settings);
        mBlockSettings = (Preference)findPreference("block_settings_key");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference  == mBlockSettings) {
	    Intent intent = new Intent();
	    intent.setClassName("com.lewa.intercept", "com.lewa.intercept.MainActivity");
            startActivity(intent);
            return true;
        }
        return false;
    }
}
