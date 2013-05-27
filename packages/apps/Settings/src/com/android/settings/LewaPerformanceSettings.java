package com.android.settings;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.R;

public class LewaPerformanceSettings extends PreferenceActivity {
    private static final String USE_16BPP_ALPHA_PREF = "pref_use_16bpp_alpha";
    private static final String USE_16BPP_ALPHA_PROP = "persist.sys.use_16bpp_alpha";
    private static final String PROMIXITY_SENSOR_PREF = "promixity_sensor_settings";
    private static final String USE_DITHERING_PREF = "pref_use_dithering";
    private static final String USE_DITHERING_PERSIST_PROP = "persist.sys.use_dithering";
    private static final String KEY_BLADE_PARTS = "blade_parts_settings";
    private static final String KEY_DEFY_PARTS = "defy_parts_settings";
    private static final String KEY_CPU_SETTINGS = "cpu_settings";
    private static final String KEY_SWAPPER_SETTINGS = "swapper_settings";

    private CheckBoxPreference mUse16bppAlphaPref;
    private CheckBoxPreference mPromixitySensorPref;
    private CheckBoxPreference mUseDitheringPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.lewa_performance_settings);

        mUse16bppAlphaPref = (CheckBoxPreference) findPreference(USE_16BPP_ALPHA_PREF);
        String use16bppAlpha = SystemProperties.get(USE_16BPP_ALPHA_PROP, "0");
        mUse16bppAlphaPref.setChecked("1".equals(use16bppAlpha));

        mUseDitheringPref = (CheckBoxPreference) findPreference(USE_DITHERING_PREF);
        String useDithering = SystemProperties.get(USE_DITHERING_PERSIST_PROP, "1");
        mUseDitheringPref.setChecked("1".equals(useDithering));
        
        mPromixitySensorPref = (CheckBoxPreference) findPreference(PROMIXITY_SENSOR_PREF);
        mPromixitySensorPref.setChecked(Settings.Secure.getInt(
                getContentResolver(),
                Settings.Secure.PROMIXITY_SENSOR_ENABLE, 1) != 0);
        if (!("Blade".equalsIgnoreCase(Build.MODEL) || "u880".equalsIgnoreCase(Build.MODEL) )) {
            getPreferenceScreen().removePreference(findPreference(KEY_BLADE_PARTS));
        }
        //Begin Added by jiangjiawen for defyparts,20120705
        if (!("MB525".equalsIgnoreCase(Build.MODEL) || "MB526".equalsIgnoreCase(Build.MODEL) )) {
            getPreferenceScreen().removePreference(findPreference(KEY_DEFY_PARTS));
        }
        //End add
        //Begin Added by chenqiang for bug 5264,20120411
        if ("u880".equalsIgnoreCase(Build.MODEL)) {
            getPreferenceScreen().removePreference(findPreference(KEY_CPU_SETTINGS));
        }
        //End add
        //Begin Added by jiangjiawen for bug 15100 20121120
        if (("MB525".equalsIgnoreCase(Build.MODEL) || "MB526".equalsIgnoreCase(Build.MODEL) )) {
            getPreferenceScreen().removePreference(findPreference(KEY_SWAPPER_SETTINGS));
        }
        //End add
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUse16bppAlphaPref) {
            SystemProperties.set(USE_16BPP_ALPHA_PROP,
                    mUse16bppAlphaPref.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mUseDitheringPref) {
            SystemProperties.set(USE_DITHERING_PERSIST_PROP,
                    mUseDitheringPref.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mPromixitySensorPref) {
            Settings.Secure.putInt(
                getContentResolver(),
                Settings.Secure.PROMIXITY_SENSOR_ENABLE,
                mPromixitySensorPref.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }    
}
