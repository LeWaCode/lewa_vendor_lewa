package com.lewa.spm.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.spm.R;
import com.lewa.spm.adapter.DevInfo;
import com.lewa.spm.charging.CalculateChargingTime;
import com.lewa.spm.charging.ChargingHistory;

import com.lewa.spm.control.DeviceStatusMap;
import com.lewa.spm.control.Executer;
import com.lewa.spm.mode.ModesFixedState;

import com.lewa.spm.mode.PowerSavingMode;
import com.lewa.spm.util.CalcUtils;
import com.lewa.spm.util.Constants;
import com.lewa.spm.util.ConsumeValue;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.util.TimeUtils;
import com.lewa.spm.charging.ChargingAnimation;
import com.lewa.spm.service.ExecuteLongMode;
import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;

public class CurrModeActivity extends PreferenceActivity implements
		OnPreferenceClickListener, OnPreferenceChangeListener {
	TextView mBatteryRmainView;
	TextView mLifeDiffView;
	TextView mBatteryInfoTitleView;
	TextView mOrdionaryTxt;
	TextView mStandByTxt;
	TextView mAlarmClockTxt;
	TextView mBatteryPercentInfoView;

	ImageView mBatteryAnim;

	CheckBoxPreference modeLongCP;
	Preference mCustomSettings;
    //
    AnimationDrawable mBatteryAnimDrawable=null;

	private boolean mIsLongModeOn = false;
	// storage
	SharedStorageKeyValuePair mStorageKV;
    
	//check change timer
    Timer mCheckChangeTimer = null;
    ChackChangeTask mCheckChangeTask=null;

    
	
   
	static int mBatteryChargingType = Constants.BATTERY_PLUGGED_NONE;
	static boolean mBatteryChargingFull = false;
	static int mBatteryLevel;
	static int mBattaryStatus;
    static int mPluggType=-1;
    static boolean mChargePlugged=false;
    static boolean mChargeIgnoreFirst=false;
    
    
    boolean diffTimeChangeFlag = false;

    private static int preLevel = 0;
	private static int preStatus =BatteryManager.BATTERY_STATUS_UNKNOWN;
    private static Date mBatteryLevelStartTime =null;

    
    
    

	BroadcastReceiver currentInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			mStorageKV = new SharedStorageKeyValuePair(ctx);
            String action=intent.getAction();
            Log.i("lkr","CurrModeActivity onReceive action="+intent.getAction());
			if (action.equals(Constants.SPM_EXCUTE_FINISH_ACTION)){
			    boolean check = intent.getBooleanExtra(Constants.SPM_EXCUTE_FINISH_NAME,
                                                       false);
                String diff = intent.getStringExtra(Constants.LONG_MODE_SWITCH_TIME_DIFFERENCE);
                if(diff!=null){
                    if(mCheckChangeTimer==null){
                        diffTimeChangeFlag=true;
                        mCheckChangeTimer = new Timer();
                        mCheckChangeTask=new ChackChangeTask();
                        mCheckChangeTimer.schedule(mCheckChangeTask, 1000, 1000);
                       
                   }
               if(mBatteryChargingType == Constants.BATTERY_PLUGGED_NONE){
                        mLifeDiffView.setText(diff);
                    }else{
                        showChargingTime(mBatteryLevel);
                    }
                    
                }
                modeLongCP.setChecked(check);
                Log.i("lkr","CurrModeActivity--- check="+check);
			}else if(action.equals(Constants.SPM_DEVS_SWITTCH_FINISH_ACTION)){
			     Log.i("lkr","CurrModeActivity---SPM_DEVS_SWITTCH_FINISH_ACTION");
			    modeLongCP.setEnabled(true);
            }else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
			    mBatteryChargingFull = false;
                mChargePlugged=true;
                mChargeIgnoreFirst=true;
			     
				//mChargStartTime = new Date();
                /*
				mOldTime = mCurrentTime;

				showChargingTime(mPlugged, mBatteryLevel);
				mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
            						Constants.SPM_POWER_PLUGGED_STATE,
            						mPlugged);
            						*/
			} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
			    mBatteryLevelStartTime=null;
                mChargeIgnoreFirst=false;
                mBatteryChargingFull = false;
                mChargePlugged=false;
                mPluggType=-1;
                Log.i("lkr","CurrModeActivity onReceive mPluggType ACTION_POWER_DISCONNECTED="+mPluggType);
                mBattaryStatus=BatteryManager.BATTERY_STATUS_DISCHARGING;
                mBatteryChargingType=Constants.BATTERY_PLUGGED_NONE;
                mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
					            Constants.SPM_POWER_PLUGGED_TYPE,
					            mBatteryChargingType);
                mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
						           Constants.STR_CHARGE_STATUS,
						           Constants.BATTERY_PLUGGED_NONE);
				showRemainingTime();
				
			} else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
																					
				mBatteryLevel = intent
						.getIntExtra(Constants.STR_POWER_LEVEL, 0); 
				mBattaryStatus = intent.getIntExtra(
						                Constants.SPM_BATTERY_STATUS_TARGET,
						                BatteryManager.BATTERY_STATUS_UNKNOWN);    
                mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
                                    Constants.STR_POWER_LEVEL,
                                    mBatteryLevel);
				
                mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
					            Constants.STR_CHARGE_STATUS,
					            mBattaryStatus);
                //it seems the first is not correct,not save
                Log.i("lkr","CurrModeActivity onReceive mChargePlugged="+mChargePlugged);
                /*
                if(!mChargePlugged){ 
                   mBatteryChargingType=Constants.BATTERY_PLUGGED_NONE; 
                   mChargeIgnoreFirst=false;
                }else{
                     mPluggType = intent.getIntExtra(Constants.STR_POWER_PLUGGED, 
                                            mPluggType);
                     mBatteryChargingType = getChargingType(mPluggType);
                }
                */
                mPluggType = intent.getIntExtra(Constants.STR_POWER_PLUGGED, 
                                            mPluggType);
                     mBatteryChargingType = getChargingType(mPluggType);
                mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
				            Constants.SPM_POWER_PLUGGED_TYPE,
				            mBatteryChargingType);
                Log.i("lkr","CurrModeActivity onReceive mPluggType="+mPluggType);
                Log.i("lkr","CurrModeActivity onReceive "
                    +"mBatteryLevel="+mBatteryLevel+"  "
                     +"mBatteryChargingType="+mBatteryChargingType+"  "
                      +"mBattaryStatus="+mBattaryStatus+"  ");
                //
                boolean appInited=mStorageKV.getBoolean(Constants.SHARED_PREFERENCE_NAME,
                                                        Constants.APPLICATION_INITED);
				if (appInited == false) {
					mStorageKV.saveChargingHistory();
				}
                mBatteryPercentInfoView.setText(mBatteryLevel
                                               +Constants.INTEL_POWER_VALUE_SIGN);
                
				if (mBatteryChargingType==Constants.BATTERY_PLUGGED_NONE) {
					showRemainingTime();
                    return;
				} else {
					if (mBattaryStatus == BatteryManager.BATTERY_STATUS_FULL) {
						mBatteryChargingFull = true;
					} else {
						mBatteryChargingFull = false;
					}
                    Log.i("lkr","CurrModeActivity onReceive showChargingTime");
					//it seems the first is not correct,not show
                    showChargingTime(mBatteryLevel);
				}
               
				if ((preLevel != mBatteryLevel)
						|| ((preStatus != mBattaryStatus)
						&&( mBattaryStatus == BatteryManager.BATTERY_STATUS_FULL))
						) {
                    if(mBatteryLevelStartTime==null){
                        preLevel = mBatteryLevel;
					    preStatus = mBattaryStatus;
                        mBatteryLevelStartTime= new Date();
                        return;
                    }
					Date  currDate = new Date();
					long diff = (currDate.getTime() - mBatteryLevelStartTime.getTime()) / 1000;
					updateDateBasedOnChargingMode(mBatteryChargingType,
							                      String.valueOf(preLevel),
							                      diff);
					mBatteryLevelStartTime = currDate;
                    preLevel = mBatteryLevel;
					preStatus = mBattaryStatus;
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.current_mode_choise);
		setContentView(R.layout.current_mode_choise);
        
        mStorageKV = new SharedStorageKeyValuePair(this);
        //init application 
        boolean appInited=mStorageKV.getBoolean(Constants.SHARED_PREFERENCE_NAME,
                                                Constants.APPLICATION_INITED);
        if(!appInited){
                mStorageKV.operate();
                ModesFixedState mfs=new ModesFixedState();
                //save long mode defalt settings
                ModeSettings msl=new ModeSettings(this,Constants.SPM_MODE_LONG_ID);
                mfs.getModeDefaultSettings(msl);
                msl.saveSettings();
                //save interval mode defalt settings
                ModeSettings msi=new ModeSettings(this,Constants.SPM_MODE_ALARM_ID);
                mfs.getModeDefaultSettings(msi);
                msi.saveSettings();
                //save chharging default estimating time
                mStorageKV.saveChargingHistory();
                mStorageKV.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                                                Constants.APPLICATION_INITED,
                                                true);

                mBatteryLevel=mStorageKV.getInt(Constants.SHARED_PREFERENCE_NAME,
                                    Constants.STR_POWER_LEVEL,
                                    0);
				mBatteryChargingType=mStorageKV.getInt(Constants.SHARED_PREFERENCE_NAME,
            					            Constants.SPM_POWER_PLUGGED_TYPE,
            					            Constants.BATTERY_PLUGGED_NONE);
                mBattaryStatus=mStorageKV.getInt(Constants.SHARED_PREFERENCE_NAME,
                					            Constants.STR_CHARGE_STATUS,
                					            BatteryManager.BATTERY_STATUS_UNKNOWN);
        }
         //save the default value
		initMode();
        //
		initUI();
        //register intent receiver
		registeReceiver();
        //show
		if (mBatteryChargingType==Constants.BATTERY_PLUGGED_NONE) {
			showRemainingTime();
		} else {
		     Log.i("lkr","CurrModeActivity onCreate showChargingTime");
			showChargingTime(mBatteryLevel);
		}
        
	}

	private void initMode() {
        int mode;
		mIsLongModeOn = (Settings.System.getInt(this.getContentResolver(),
				Settings.System.POWERMANAGER_MODE_ON, 0) == 1);
		if (mIsLongModeOn) {
			mode = Constants.SPM_MODE_LONG_ID;
		} else {
			mode = mStorageKV.getInt(Constants.SHARED_PREFERENCE_NAME,
                    					Constants.STR_MODE_TYPE_NAME,
                    					Constants.SPM_MODE_OUT_ID);
			if (mode == Constants.SPM_MODE_OUT_ID) {
                ModeDevStatus mds=new ModeDevStatus(this,mode);
                mds.saveStatus();
			}
		}
		mStorageKV.saveInt(Constants.SHARED_PREFERENCE_NAME,
				            Constants.STR_MODE_TYPE_NAME,
				            mode);
	}

	private void initUI() {
        //battery pencent info
        mBatteryPercentInfoView = (TextView) findViewById(R.id.spm_battery_show_txt);
        //charging or remain title
        mBatteryInfoTitleView = (TextView) findViewById(R.id.life_or_charging_time);
        //time resume time 
		mBatteryRmainView = (TextView) findViewById(R.id.spm_life_time);
        
		mLifeDiffView = (TextView) findViewById(R.id.spm_life_diff_time);
		modeLongCP = (CheckBoxPreference) findPreference(Constants.KEY_MODE_LONG_CHECK);
		modeLongCP.setChecked(mIsLongModeOn);
		mCustomSettings = (Preference) findPreference(Constants.KEY_SET_LONG_MODE_PARA);
		modeLongCP.setOnPreferenceChangeListener(this);
		mCustomSettings.setOnPreferenceClickListener(this);
	}
    // show battery remaining time
	private void showRemainingTime() {
	    ChargingAnimation anim;
		int mode = mStorageKV.getInt(Constants.SHARED_PREFERENCE_NAME,
                        				Constants.STR_MODE_TYPE_NAME,
                        				Constants.SPM_MODE_OUT_ID);
		double remain = CalcUtils.getInstance(this).Lift(mode);
        String timeStr;

		int h = CalcUtils.getInstance(this).getHoursFromTime(remain,mBatteryLevel / 100f);
		int m = CalcUtils.getInstance(this).getMinutesFromString(remain,mBatteryLevel / 100f);
        timeStr =h
				+ getString(R.string.spm_hour)
				+ m
				+ getString(R.string.spm_minute);
	
        mBatteryAnim = (ImageView) findViewById(R.id.spm_battery_charging_anim);
		anim = new ChargingAnimation(getApplicationContext());
		mBatteryAnim.clearAnimation();
        mBatteryInfoTitleView.setText(getString(R.string.spm_life_time));
		mBatteryRmainView.setText(timeStr);
		mBatteryAnim.setBackgroundDrawable(anim
				.getDrawableBasedOnLevel(mBatteryLevel));
	}
	
    private void showChargingTime(int level) {
        
		mLifeDiffView.setTextColor(0x00000000);
		mBatteryRmainView.setTextColor(0xff006838);
		diffTimeChangeFlag = false;
        ChargingAnimation anim;
		mBatteryAnim = (ImageView) findViewById(R.id.spm_battery_charging_anim);
		anim = new ChargingAnimation(getApplicationContext());
		mBatteryAnim.clearAnimation();
		mBatteryInfoTitleView.setText(getString(R.string.spm_charging_time));
        Log.i("lkr","showChargingTime mBattaryStatus = "+mBattaryStatus);
        Log.i("lkr","showChargingTime mBatteryLevel = "+level);
        Log.i("lkr","showChargingTime mBatteryChargingType = "+mBatteryChargingType);
        if(level == 100){
            mBatteryAnim.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.spm_battery_charging_100_bg));
        }
		if ((mBattaryStatus == BatteryManager.BATTERY_STATUS_FULL)
				&& (level == 100)) {
			mBatteryRmainView
					.setText(getString(R.string.spm_charging_full));
		} else {
		    CalculateChargingTime cct = new CalculateChargingTime(this);
		    long time = 0;
            if (mBatteryChargingType==Constants.BATTERY_PLUGGED_USB){
                time = cct.estimateChargingTime(
                    					Constants.SPM_BATTERY_STATUS_USB_CHARGING_SP_NAME,
                    					level);
            }else if (mBatteryChargingType==Constants.BATTERY_PLUGGED_AC){
                time = cct.estimateChargingTime(
					                    Constants.SPM_BATTERY_STATUS_AC_CHARGING_SP_NAME,
					                    level);
            }else{
                return;
            }
            //Date  currDate = new Date();
		    //long past = (currDate.getTime() - mBatteryLevelStartTime.getTime()) / 1000;
    		String ChargingTime = TimeUtils.transferTime(this,time);
            Log.i("lkr","showChargingORemainingTime ChargingTime = "+ChargingTime);
			mBatteryRmainView.setText(ChargingTime);
            if(level != 100){
                mBatteryAnimDrawable = anim
    					.getAnimationBaseOnLevel(level);
    			mBatteryAnim.setBackgroundDrawable(mBatteryAnimDrawable);
            }
            
			mAnimHandler.postDelayed(mChargingAnimationRunnable, 1000);
		}
	}

    public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean value = (Boolean) newValue;
		modeLongCP.setEnabled(false);
        //
        ExecuteLongMode runLongMode = new ExecuteLongMode(this);
        int mode = mStorageKV.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);
        if(value){
            if((mode==Constants.SPM_MODE_LONG_ID)){
                return false;
            }
            runLongMode.onCreate();
        }else{
            if((mode==Constants.SPM_MODE_OUT_ID)){
                 return false;
            }
            runLongMode.onDestroy();
        }
        //
        diffTimeChangeFlag = true;
        
		mLifeDiffView.setTextColor(0xff006838);
        //
		mBatteryRmainView.setTextColor(0x00000000);

        mCheckChangeTimer = new Timer();
        mCheckChangeTask=new ChackChangeTask();
        mCheckChangeTimer.schedule(mCheckChangeTask, 1000, 1000);
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		Intent intent = new Intent();
		if (preference.getKey().equals(Constants.KEY_SET_LONG_MODE_PARA)) {
			intent.putExtra(Constants.USER_DEFINED_EXTRA_NAME, 
                getString(R.string.spm_mode_standby_set_para));
			intent.putExtra(Constants.USER_DEFINED_EXTRA_POSITION, Constants.SPM_MODE_LONG_ID);
		}
		intent.setClass(this, UserDefinedModeActivity.class);
		startActivityForResult(intent, 0);
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			break;
		case RESULT_CANCELED:
			break;
		default:
			break;
		}
	}

	private void registeReceiver() {
		IntentFilter currentIntentFilter = new IntentFilter();
		currentIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		currentIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
		currentIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        currentIntentFilter.addAction(Constants.SPM_EXCUTE_FINISH_ACTION);
        currentIntentFilter.addAction(Constants.SPM_DEVS_SWITTCH_FINISH_ACTION);
        //mode change operations finish
		registerReceiver(currentInfoReceiver, currentIntentFilter);
	}

	private int getChargingType(int plug) {
		int  currentState = Constants.BATTERY_PLUGGED_NONE;
		switch (plug) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			currentState = Constants.BATTERY_PLUGGED_AC;
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			currentState = Constants.BATTERY_PLUGGED_USB;
			break;
		}
		return currentState;
	}

	

	private void updateDateBasedOnChargingMode(int type,String level, long time) {
		int count = 0;
		long oldTime = 0;
		if (type==Constants.BATTERY_PLUGGED_AC) {
			oldTime = mStorageKV.getLong(
					Constants.SPM_BATTERY_STATUS_AC_CHARGING_SP_NAME,
					level,
					time);
			count = mStorageKV.getInt(
					Constants.SPM_BATTERY_STATUS_AC_CHARGING_COUNT_SP_NAME,
					level,
					10);
			mStorageKV.saveLong(
					Constants.SPM_BATTERY_STATUS_AC_CHARGING_SP_NAME,
					level,
					(oldTime * count + time) / (count + 1));
			mStorageKV.saveInt(
					Constants.SPM_BATTERY_STATUS_AC_CHARGING_COUNT_SP_NAME,
					level,
					count + 1);
		} else if (type==Constants.BATTERY_PLUGGED_USB) {
			oldTime = mStorageKV.getLong(
					Constants.SPM_BATTERY_STATUS_USB_CHARGING_SP_NAME,
					level,
					time);
			count = mStorageKV.getInt(
					Constants.SPM_BATTERY_STATUS_USB_CHARGING_COUNT_SP_NAME,
					level,
					10);
			mStorageKV.saveLong(
					Constants.SPM_BATTERY_STATUS_USB_CHARGING_SP_NAME,
					level,
					(oldTime * count + time) / (count + 1));
			mStorageKV.saveInt(
					Constants.SPM_BATTERY_STATUS_USB_CHARGING_COUNT_SP_NAME,
					level,
					count + 1);
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        mIsLongModeOn = (Settings.System.getInt(this.getContentResolver(),
				Settings.System.POWERMANAGER_MODE_ON, 0) == 1);
        Log.i("lkr","onResume mIsLongModeOn "+mIsLongModeOn);
        modeLongCP.setChecked(mIsLongModeOn);
    }


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(currentInfoReceiver);
	}
    
    //
	Handler mCheckChangeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (((String) msg.obj).equals(Constants.SPM_LIFE_DIFF_TXT_SHOW)) {
                // modeLongCP.setEnabled(true);
    			mLifeDiffView.setTextColor(0x00000000);
                mLifeDiffView.setText(null);
    			mBatteryRmainView.setTextColor(0xff006838);
    			diffTimeChangeFlag = false;     
    		    if (mBatteryChargingType==Constants.BATTERY_PLUGGED_NONE) {
					showRemainingTime();
				} else {
				    Log.i("lkr","CurrModeActivity handleMessage showChargingTime");
					showChargingTime(mBatteryLevel);
				}
		    }
		}

	};
        
    class ChackChangeTask  extends TimerTask{
        int idx;
        Looper mLooper;
        @Override
		public void run() {
			Message txtMsg;
            int mode;
			if (mLooper == null) {
				Looper.prepare();
				mLooper = Looper.myLooper();
			}
			txtMsg = new Message();
			if (diffTimeChangeFlag) {
				idx++;
                if (idx > 3) {
    				txtMsg.obj = Constants.SPM_LIFE_DIFF_TXT_SHOW;
    				mCheckChangeHandler.sendMessage(txtMsg);
                    if (mCheckChangeTask != null) {
             			mCheckChangeTask.quit();
             			mCheckChangeTask.cancel();
             			mCheckChangeTask = null;
             		}
             		if (mCheckChangeTimer != null) {
             			mCheckChangeTimer.cancel();
             			mCheckChangeTimer.purge();
             			mCheckChangeTimer = null;
             		}
                    idx = 0;
			   }
                
			} else{
				idx = 0;
			}
        }

        public void quit() {
    		if (mLooper != null) {
    			mLooper.quit();
    		}
		}
    }


    //a handle of animation
	private Handler mAnimHandler = new Handler();
	/**
	 * mChargingAnimationRunnable used to control the animation of charging
	 */
	private Runnable mChargingAnimationRunnable = new Runnable() {

		@Override
		public void run() {
		    if(mBatteryAnimDrawable!=null){
			    mBatteryAnimDrawable.start();
            }
		}
	};
}
