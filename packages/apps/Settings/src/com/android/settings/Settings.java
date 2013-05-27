package com.android.settings;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.lewa.os.ui.ViewPagerIndicatorActivity;

public class Settings extends ViewPagerIndicatorActivity {

	private static final int TAB_INDEX_WIRELESS  = 0;
       private static final int TAB_INDEX_PERSONAL = 1;
       private static final int TAB_INDEX_PHONE  = 2;
       private static final int TAB_INDEX_APPLICATION = 3;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayList<StartParameter> category = new ArrayList<StartParameter>();
        category.add(new StartParameter(LewaWirelessSettings.class, null, R.string.wireless_settings_title));
        category.add(new StartParameter(LewaPersonalSettings.class, null, R.string.personal_settings_title));
        category.add(new StartParameter(LewaPhoneSettings.class, null, R.string.phone_settings_title));
        category.add(new StartParameter(LewaApplicationSettings.class, null, R.string.application_settings_title));
        setupFlingParm(category, R.layout.settings_home, R.id.indicator, R.id.pager);
        
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

     @Override
    protected void onNewIntent(Intent intent) {
	preCreateopenIntent(intent);
        super.onNewIntent(intent);
    }
     private void preCreateopenIntent(Intent intent)  {
	  if(intent != null) {
		String action = intent.getAction();
		if (!TextUtils.isEmpty(action)) {
		    if (action.equals("android.settings.AIRPLANE_MODE_SETTINGS")) {
			 setDisplayScreen(TAB_INDEX_WIRELESS);
		    }	   
		}   
         }
     }
}

