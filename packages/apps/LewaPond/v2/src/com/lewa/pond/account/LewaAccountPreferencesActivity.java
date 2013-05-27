package com.lewa.pond.account;

import com.lewa.pond.R;

import android.os.Bundle;
import android.util.Log;
import android.preference.PreferenceActivity;

public class LewaAccountPreferencesActivity extends PreferenceActivity {
	
	public static final String TAG = "LewaAccountPreferencesActivity";
	
	@Override
	public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    Log.i(TAG, "onCreate");
	    addPreferencesFromResource(R.xml.account_preferences);
	}

	@Override
	public void onPause() {
	    super.onPause();
	}
}
