package com.lewa.spm.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lewa.spm.R;
import com.lewa.spm.adapter.AppAdapter;
import com.lewa.spm.adapter.AppInfo;
import com.lewa.spm.app.GetAllInstalledApp;
import com.lewa.spm.util.Constants;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.util.Log;
/**
 * this class is used to get all applications list
 * @author Administrator
 *
 */
public class AppInfoActivity extends Activity implements OnItemClickListener{
	AppAdapter appAdapter=null;
	GetAllInstalledApp allInstalledApp=null;
	
	ListView mAppList=null;
	TextView mTimeTxt=null;
	Thread getAppThread = null;
    Thread loadAppIconThread = null;
	ArrayList<AppInfo> appInfos=null;
	String montageTime;
	String mCurrentPkgName;
	
	// default value is 0;(the 0 is disconnect ; the 1 is connected)
	int mStatsType = 0;

    public static final int STATUS_DESTROY= 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSE = 2;
    private int mRunningStatus=STATUS_DESTROY;
    
    
    // constant value that can be used to check return code from sub activity.
    public static final String ACTION_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";
    //public static final String ACTION_START_LOAD = "lewa.intent.action.START_LOAD";
    public static final int STATUS_NONE_LOADED = 0;
    public static final int STATUS_APP_LOADING = 1;
    public static final int STATUS_APP_LOADED = 2;
    public static final int STATUS_APP_ICON_LOADING = 3;
    public static final int STATUS_ALL_LOADED = 4;
    private int loadStatus = STATUS_NONE_LOADED;

    public static boolean started = false;
    
    public static void startup(){
        if(!started){
            started =true;
        } 
    }
    public void loadAppPics() {
        if (appAdapter != null) {
            for (int i = 0; i < appAdapter.getCount(); i++) {
                try {
                    ((AppInfo) (appAdapter.getItem(i))).icon =getPackageManager().getApplicationIcon(((AppInfo) (appAdapter.getItem(i))).packageName.toString());
                } catch (NameNotFoundException ex) {
                    Logger.getLogger(AppInfoActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    BroadcastReceiver chargingFullReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mContext, Intent intent) {
        	// if power is connected, calculate is stop;
        	if(mRunningStatus!=STATUS_RUNNING){
                return;
            }
            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                mStatsType = BatteryStats.STATS_SINCE_CHARGED;
            } else if (intent.getAction().equals(Constants.SP_INTENT_MONTAGE_TIME)) {
                montageTime = intent.getStringExtra(Constants.SPM_MONTAGE_TIME);
                mTimeTxt.setText(getString(R.string.spm_battery_life_calculator) + " " + montageTime);
            } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
            }/*else if(intent.getAction().equals(ACTION_START_LOAD)){
                if(loadStatus == STATUS_NONE_LOADED){
                    if(getAppThread==null){
                        return;
                    }
                    //getAppThread.start();
                }
            }
            */
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spm_running_app_list);
		registeReceiver();
        mRunningStatus=STATUS_RUNNING;
		mAppList = (ListView) findViewById(R.id.spm_running_app_list);
		mTimeTxt = (TextView) findViewById(R.id.spm_battery_life_time_text);
        mAppList.setOnItemClickListener(this);
        loadAppIconThread = new Thread(loadAppIconRunnable);
        getAppThread = new Thread(loadAppRunnable);
        //Log.i("Tt", "AppInfoActivity------------onCreate" );
        getAppThread.start();
       
	}

    @Override
	protected void onPause() {
	    super.onPause();
        //Log.i("Tt", "AppInfoActivity------------onPause" );
    }
     @Override
	protected void onResume() {
        super.onResume();
        //Log.i("Tt", "AppInfoActivity------------onResume" );
    }
	
	private void registeReceiver() {
		IntentFilter currentIntentFilter = new IntentFilter();
		currentIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		currentIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		currentIntentFilter.addAction(Constants.SP_INTENT_MONTAGE_TIME);
        //currentIntentFilter.addAction(ACTION_START_LOAD);
        registerReceiver(chargingFullReceiver, currentIntentFilter);
	}
	
	

	private void getApp(){//get application list
		allInstalledApp = new GetAllInstalledApp(this);
		//appInfos = new ArrayList<AppInfo>();
		appInfos = allInstalledApp.getApp();
		
		int size = appInfos.size();
        //Log.i("Tt", "AppInfoActivity------------size = " + size);
		double appInfoUsageBefore, appInfoUsageAfter;
		AppInfo appInfoTemp;
		for(int i = 0; i < size; i ++){
			for(int j = i; j < size; j ++){
				appInfoUsageBefore = appInfos.get(i).powerUsage;
				appInfoUsageAfter = appInfos.get(j).powerUsage;
				if(appInfoUsageBefore < appInfoUsageAfter){
						 appInfoTemp = appInfos.get(j);
						 appInfos.set(j, appInfos.get(i));
						 appInfos.set(i, appInfoTemp);
				}
			}
		}
       
    }
     
	// click the application list item and into the  corresponding application
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
		AppInfo app = (AppInfo) arg0.getItemAtPosition(pos);
		mCurrentPkgName = app.packageName;
		// Create intent to start new activity
		Intent intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package", mCurrentPkgName, null));
		// start new activity to display extended information
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
	    //Log.i("Tt", "AppInfoActivity------------onDestroy" );
        mRunningStatus=STATUS_DESTROY;
        release();
        super.onDestroy();
	}


    Handler getAppHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
            if((mRunningStatus!=STATUS_RUNNING)||(loadAppIconThread==null)){
                return;
            }
			if (msg.arg1 == STATUS_APP_LOADED){
				appAdapter = new AppAdapter(AppInfoActivity.this, appInfos);
		        mAppList.setAdapter(appAdapter);
                loadAppIconThread.start();
			}else if(msg.arg1 == STATUS_ALL_LOADED){
			    if((appAdapter==null)||
                    (mAppList==null)){
                    return;
                 }
			    appAdapter.notifyDataSetChanged();
                mAppList.requestLayout();
            }
		}
    	
    };
    Runnable loadAppRunnable = new Runnable(){
		@Override
		public void run() {
		    if((mRunningStatus!=STATUS_RUNNING)||(getAppThread==null)){
                return;
             }
		    loadStatus = STATUS_APP_LOADING;
			getApp();
            loadStatus = STATUS_APP_LOADED;
            Message msg = new Message();
            msg.arg1 = STATUS_APP_LOADED;
            getAppHandler.sendMessage(msg);
		}
    };
	
    
    Runnable loadAppIconRunnable = new Runnable(){
		@Override
		public void run() {
		    if((mRunningStatus!=STATUS_RUNNING)||(loadAppIconThread==null)){
                return;
            }
		    loadStatus = STATUS_APP_ICON_LOADING;
			loadAppPics();
            loadStatus = STATUS_ALL_LOADED;
            Message msg = new Message();
            msg.arg1 = STATUS_ALL_LOADED;
            getAppHandler.sendMessage(msg);
		}
    };
    
    private void release(){
		/**
		 * release the resource
		 */
        if (getAppThread != null){
			getAppThread.interrupt();
			getAppThread = null;
		}
        if(loadAppIconThread != null){
            loadAppIconThread.interrupt();
            loadAppIconThread = null;
        }
		if(appAdapter != null){
			appAdapter.release();
			appAdapter = null;
		}
		mAppList = null;
        if(appInfos != null){
		    appInfos.clear();
        }
		appInfos = null;
	
        if(allInstalledApp != null){
		    allInstalledApp.release();
        } 
        allInstalledApp=null;
    }
}