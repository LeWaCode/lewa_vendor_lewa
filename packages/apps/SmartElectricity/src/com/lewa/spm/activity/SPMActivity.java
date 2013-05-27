package com.lewa.spm.activity;

import java.util.ArrayList;

import com.lewa.spm.R;
import com.lewa.spm.util.Constants;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.lewa.os.ui.*;
import android.content.Intent;

public class SPMActivity extends ViewPagerIndicatorActivity implements ViewPagerIndicator.OnPagerSlidingListener {
    /** Called when the activity is first created. */
	private int currentScreen = 1;
	private static final int thirdScreen = 2;
    @Override
    public void onCreate(Bundle savedInstanceState) {  
    	 int screenId = getIntent().getIntExtra(Constants.SPM_NOTIFICATION, currentScreen);
         if (screenId == thirdScreen) {
             currentScreen = thirdScreen;
         } 

         if("a60".equalsIgnoreCase(Build.DEVICE)){
             currentScreen = 0;
         }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		nm.cancel(R.drawable.spm_mode_standby);
 		nm.cancel(R.drawable.spm_mode_alarm);
        ArrayList<StartParameter> category = new ArrayList<StartParameter>();
        
        if(!("a60".equalsIgnoreCase(Build.DEVICE))){
            category.add(new StartParameter(AppInfoActivity.class, null, R.string.spm_class_title_app));
        }
        category.add(new StartParameter(CurrModeActivity.class, null, R.string.spm_mode_standby));
        category.add(new StartParameter(IntelliActivity.class, null, R.string.spm_class_title_intelligent));
        setupFlingParm(category, R.layout.settings_home, R.id.indicator, R.id.pager);
        setDisplayScreen(currentScreen);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setOnTriggerPagerChange(this);
    }
    
    @Override
    public void onChangePagerTrigger(int arg0) {
        /*
        if (arg0 == 0) {
            Intent intent = new Intent();
            intent.setAction(AppInfoActivity.ACTION_START_LOAD);
            this.getBaseContext().sendBroadcast(intent);
        }
        */
    }
    
}
