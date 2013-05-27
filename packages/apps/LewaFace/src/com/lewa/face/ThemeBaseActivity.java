package com.lewa.face;

import java.util.ArrayList;

import com.lewa.face.app.ThemeApplication;
import com.lewa.face.local.LocalBaseActivity;
import com.lewa.face.online.OnlineBaseActivity;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeSDCardMonitor;
import com.lewa.face.util.ThemeUtil;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ViewPagerIndicatorActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import com.lewa.face.util.Logs;

/**
 * 
 * @author fulw
 *
 */
public abstract class ThemeBaseActivity extends ViewPagerIndicatorActivity implements com.lewa.os.ui.ViewPagerIndicator.OnPagerSlidingListener,ThemeSDCardMonitor.SDCardStatusListener{
    
    private static final String TAG = ThemeBaseActivity.class.getSimpleName();
    private ThemeSDCardMonitor themeSDCardMonitor;
    private boolean sdcardMounted = true;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
       
        ThemeApplication.activities.add(this);
        
        ArrayList<StartParameter> activities = new ArrayList<StartParameter>();
        
        initStartParameter(activities);
        
        super.setupFlingParm(activities, R.layout.theme_main_activity, R.id.indicator, R.id.pager);
        
        super.onCreate(savedInstanceState);
        
        if(!ThemeUtil.isSDCardEnable()){
            ThemeUtil.createAlertDialog(this);
            return;
        }
        
        initURL();
        
        themeSDCardMonitor = new ThemeSDCardMonitor(this);
        themeSDCardMonitor.addListener(this);

        
        super.setOnTriggerPagerChange(this);
        
        setActivityBirageResult();
    }

    @Override
    public void onChangePagerTrigger(int position) {
        if(position == ThemeConstants.ONLINE_PAGE){
            OnlineBaseActivity activity = (OnlineBaseActivity) super.getItemActivity(position);
            if(activity.isFirstDisplay){
                Message message = activity.handler.obtainMessage();
                activity.handler.sendMessage(message);
            }
        }
    }

    /**
     * 初始化在线连接url
     */
    protected void initURL(){
        DisplayMetrics displayMetrics = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        float density = displayMetrics.density;
        
        if (displayMetrics.widthPixels == 480 && displayMetrics.heightPixels == 854) {
            ThemeUtil.isWVGA = true;
        } else if (density == 1.0) {
            Log.i(TAG, "HVGA Screen");
            ThemeUtil.isWVGA = false;
            
        }else if(density == 1.5){
            Log.i(TAG, "WVGA Screen");
            ThemeUtil.isWVGA = true;
        }else {
            Log.i(TAG, "Others Screen");
            ThemeUtil.isWVGA = false;
        }
        
        ThemeUtil.initURL();
    }
    
    protected void setActivityBirageResult(){
        ((LocalBaseActivity) super.getItemActivity(ThemeConstants.LOCAL_PAGE)).setActivityBirageResult((ActivityResultBridge)this);
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
    
    
    
    
    @Override
    public void onStatusChanged(int sdcardStatus) {
        
        if(sdcardStatus != ThemeConstants.MEDIA_MOUNTED){
            sdcardMounted = false;
        }
        
    }
   
    /**
     * 根据不同的Intent请求，启动不同的Activity
     * @param activities
     */
    protected abstract void initStartParameter(ArrayList<StartParameter> activities);
    
}

