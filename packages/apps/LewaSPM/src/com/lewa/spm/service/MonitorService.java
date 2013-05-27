package com.lewa.spm.service;

import java.util.Timer;
import java.util.TimerTask;

import com.lewa.spm.device.DevStatus;
import com.lewa.spm.device.SwitchManager;
import com.lewa.spm.element.ConsumeValue;
import com.lewa.spm.utils.ModeUtils;
import com.lewa.spm.utils.PrefUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MonitorService extends Service{
	
	private final String TAG = "MonitorService";

	private Timer mTimer;

	private TimerTask mTimerTask;

	private final int TIMER_DELAY_RATE = 1000*60;
	
	private boolean intel_mode_time_on = false;
	private boolean intel_mode_power_on = false;
	
	private boolean intel_time_bit = false;							//no use because not check state frequently.
	private boolean intel_power_bit = false;
	
	private BroadcastReceiver mBattReceiver;

	private int preLevel = 0;
	
	private Handler mHandler;
	
	@Override
	public void onCreate() {
		
		mBattReceiver = new BroadcastReceiver() {

	        public void onReceive(Context context, Intent intent) {

	                Bundle bundle = intent.getExtras();
	                int curLevel = bundle.getInt("level");

                	if(preLevel!=curLevel){
                		Log.i(TAG, "onReceive() preLevel="+preLevel+" curLevel="+curLevel);
                		sendToClient(ConsumeValue.ACTION_UPDATE_UI_ENERGY,ConsumeValue.PARAM_ENERGY_UPDATE,true);
                		preLevel=curLevel;
                	
                		intel_mode_power_on = PrefUtils.getInstance(MonitorService.this).getModeOn(PrefUtils.INTEL_MODE_POWER_ON);
                    	intel_power_bit = (PrefUtils.getInstance(MonitorService.this).getIntelPowerBit()==ConsumeValue.CLEAR_BIT);
                    	
                    	Log.i(TAG, "onReceive() intel_mode_power_on="+intel_mode_power_on+" intel_power_bit="+intel_power_bit);
                    	
                    	if(intel_mode_power_on && intel_power_bit){
                    		Log.i(TAG, "enterAirMode for intel power!");
                    		PrefUtils.getInstance(MonitorService.this).setCurRunningMode(ConsumeValue.MODE_AIR);
                    		PrefUtils.getInstance(MonitorService.this).setIntelPowerBit(ConsumeValue.SET_BIT);
                    		ModeUtils.getInstance(MonitorService.this).enterMode(ConsumeValue.MODE_AIR);
	            			if(ModeUtils.getInstance(MonitorService.this).checkIntelPower()){
	            				sendToClient(ConsumeValue.ACTION_UPDATE_UI_INTELS_POWER,ConsumeValue.PARAM_INTEL_POWER_START,true);
	            			}else{
	            				sendToClient(ConsumeValue.ACTION_UPDATE_UI_INTELS_POWER,ConsumeValue.PARAM_INTEL_POWER_START,false);
	            			}
	            		}
                    	preLevel=curLevel;
                	}
	        };
		};
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		this.registerReceiver(mBattReceiver, filter);
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0){
					enterIntelTimeMode();
				}else{
					leaveIntelTimeMode();
				}
			}
		};
		
		initTimerAndTask();
		
		super.onCreate();
		Log.i(TAG,"onCreate()");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {		
		super.onStart(intent, startId);		
		if(PrefUtils.getInstance(this).getModeOn(PrefUtils.INTEL_MODE_TIME_ON)){
			try{
				mTimer.schedule(mTimerTask, 0, TIMER_DELAY_RATE);
			}catch(IllegalStateException ise){
				initTimerAndTask();
				mTimer.schedule(mTimerTask, 0, TIMER_DELAY_RATE);
			}
		}else{
			mTimerTask.cancel();
			mTimer.cancel();
		}
		Log.i(TAG,"onStart()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy()");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG,"onBind()");
		return null;
	}
	
	private void sendToClient(String actionName,String paramName,boolean paramValue){
		Intent mIntent = new Intent(actionName);
		mIntent.putExtra(paramName, paramValue);
		this.sendBroadcast(mIntent);
	}
	
	private void initTimerAndTask(){
		mTimer = new Timer();
		mTimerTask = new TimerTask(){
			 @Override
	         public void run() {
				 Log.i(TAG,"mTimerTask timemodeon="+PrefUtils.getInstance(MonitorService.this).getModeOn(PrefUtils.INTEL_MODE_TIME_ON));
				 
				 intel_mode_time_on = PrefUtils.getInstance(MonitorService.this).getModeOn(PrefUtils.INTEL_MODE_TIME_ON); 
				 intel_time_bit = (PrefUtils.getInstance(MonitorService.this).getIntelTimeBit()==ConsumeValue.CLEAR_BIT);
				 if(intel_mode_time_on){
					 if(ModeUtils.getInstance(MonitorService.this).checkIntelTime()){
						 if(intel_time_bit){
							 mHandler.sendEmptyMessage(0);
						 }
					 }else{
						 if(!intel_time_bit){
							 mHandler.sendEmptyMessage(1);
						 }
					 } 
				 }else{
					 Log.i(TAG,"onCreate() mTimerTask time mode off!");
				 }
			 }
		};
	}
	
	private void enterIntelTimeMode(){
		PrefUtils.getInstance(MonitorService.this)
	 	.setIntelTimePreDevStateList(DevStatus.getInstance(MonitorService.this).getAllDeviceStatus());							 
	 
		PrefUtils.getInstance(MonitorService.this).setIntelTimeBit(ConsumeValue.SET_BIT);
		ModeUtils.getInstance(MonitorService.this).enterMode(ConsumeValue.MODE_AIR);
		sendToClient(ConsumeValue.ACTION_UPDATE_UI_INTELS_TIME,ConsumeValue.PARAM_INTEL_TIME_START,true);
	}
	
	private void leaveIntelTimeMode(){		
		SwitchManager.getInstance(MonitorService.this)
	 	.setAllDeviceStatus(PrefUtils.getInstance(MonitorService.this).getIntelTimePreDevStateList());
	 
		PrefUtils.getInstance(MonitorService.this).setIntelTimeBit(ConsumeValue.CLEAR_BIT);
		ModeUtils.getInstance(MonitorService.this).enterMode(ConsumeValue.MODE_NORMAL);
		sendToClient(ConsumeValue.ACTION_UPDATE_UI_INTELS_TIME,ConsumeValue.PARAM_INTEL_TIME_START,false);
	}
}

