package com.lewa.face;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;

import com.lewa.face.app.ThemeApplication;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeSDCardMonitor;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ViewPagerIndicatorActivity;


/**
 * 
 * @author fulw
 *
 */
public class FontMain extends ViewPagerIndicatorActivity implements com.lewa.os.ui.ViewPagerIndicator.OnPagerSlidingListener,ThemeSDCardMonitor.SDCardStatusListener{
    
    private ThemeSDCardMonitor themeSDCardMonitor;
    private boolean sdcardMounted = true;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	ThemeApplication.activities.add(this);
    	
        ArrayList<StartParameter> activities = new ArrayList<StartParameter>();
        activities.add(new StartParameter(com.lewa.face.local.Font.class, null,R.string.theme_font_local));
        activities.add(new StartParameter(com.lewa.face.online.Font.class , null,R.string.theme_font_online));
        
        super.setupFlingParm(activities, R.layout.theme_main_activity, R.id.indicator, R.id.pager);
        
        super.onCreate(savedInstanceState);
        
        themeSDCardMonitor = new ThemeSDCardMonitor(this);
        themeSDCardMonitor.addListener(this);

        
        super.setOnTriggerPagerChange(this);
    }

    @Override
    public void onStatusChanged(int sdcardStatus) {
        // TODO Auto-generated method stub
        if(sdcardStatus != ThemeConstants.MEDIA_MOUNTED){
            sdcardMounted = false;
        }
    }

    @Override
    public void onChangePagerTrigger(int position) {
        if(position == ThemeConstants.ONLINE_PAGE){
            com.lewa.face.online.Font activity = (com.lewa.face.online.Font) super.getItemActivity(position);
            if(activity.isFirstDisplay){
                Message message = activity.handler.obtainMessage();
                activity.handler.sendMessage(message);
            }
        }
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if(!sdcardMounted){
            sdcardMounted = true;
            ThemeUtil.createAlertDialog(this);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ThemeApplication.activities.remove(this);
        
        if(themeSDCardMonitor != null){
            themeSDCardMonitor.removeListener();
        }
        
        super.onDestroy();
    }
    
}