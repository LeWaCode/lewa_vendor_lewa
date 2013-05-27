package com.lewa.search;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * This class defines the Activity on "search info setting" page.
 * @author		wangfan
 * @version	2012.07.04
 */

public class LewaSearchInfoSettingActivity  extends PreferenceActivity {
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  

    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.info_setting);
    }

}
