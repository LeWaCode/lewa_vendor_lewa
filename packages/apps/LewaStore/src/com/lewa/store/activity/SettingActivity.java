package com.lewa.store.activity;

import com.lewa.store.R;
import com.lewa.store.adapter.PreferencesHelper;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.SystemHelper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	
	private String TAG=SettingActivity.class.getSimpleName();

	private PreferencesHelper spHelper = null;
	private ListPreference lp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.setting);

		spHelper = new PreferencesHelper(this, Constants.updateFlag);
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		lp = (ListPreference) findPreference("netsetting");
		int netCode = Integer.parseInt(spHelper.getStringNetValue("netsetting"));
		switch (netCode) {
		case 0:			
			spHelper.putValue(Constants.SETTING_NETWORK_FLAG, netCode);
			break;
		case 1:
			spHelper.putValue(Constants.SETTING_NETWORK_FLAG, netCode);
			break;
		default:
			break;
		}
		lp.setValue(netCode+"");
		CharSequence[] cs = lp.getEntries();
		for (int i = 0; i < cs.length; i++) {
			if (Integer.parseInt(lp.getValue()) == i) {
				lp.setSummary(cs[i]);
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("updateNotice")) {
			//Default should not notice update msg
			Boolean isUpdate = sharedPreferences.getBoolean("updateNotice",false);
			if (isUpdate) {
				spHelper.putValue(Constants.SETTING_IS_UPDATE_FLAG, true);
				Toast.makeText(SettingActivity.this,getString(R.string.setting_open_auto_update),Toast.LENGTH_SHORT).show();
			} else {
				spHelper.putValue(Constants.SETTING_IS_UPDATE_FLAG, false);
				Toast.makeText(SettingActivity.this,getString(R.string.setting_close_auto_update),Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("netsetting")) {//default,wifi code=0
			int netCode = Integer.parseInt(sharedPreferences.getString("netsetting", "0"));
			lp = (ListPreference) findPreference("netsetting");
			switch (netCode) {
			case 0:
				lp.setSummary(lp.getEntry());
				spHelper.putValue(Constants.SETTING_NETWORK_FLAG, netCode);
				spHelper.putValue("netsetting","0");
				System.out.println("Net work set "+ spHelper.getIntValue(Constants.SETTING_NETWORK_FLAG));
				break;
			case 1:
				lp.setSummary(lp.getEntry());
				spHelper.putValue(Constants.SETTING_NETWORK_FLAG, netCode);
				spHelper.putValue("netsetting","1");
				System.out.println("Net work set "+ spHelper.getIntValue(Constants.SETTING_NETWORK_FLAG));
				break;
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
