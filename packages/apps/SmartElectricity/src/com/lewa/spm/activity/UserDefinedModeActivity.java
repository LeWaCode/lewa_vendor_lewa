package com.lewa.spm.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.lewa.spm.control.Brightness;
import com.lewa.spm.control.InquirState;
import com.lewa.spm.control.SwitchState;
import com.lewa.spm.service.ExecuteLongMode;
import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;
import com.lewa.spm.R;
import com.lewa.spm.control.Executer;


import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CheckBox;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.app.Activity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserDefinedModeActivity extends PreferenceActivity implements OnPreferenceClickListener,
    OnPreferenceChangeListener, OnClickListener{
		
	SwitchState switchState;
	SharedStorageKeyValuePair saveValue;

    MyCheckBoxPreference airPlaneCP;
	MyCheckBoxPreference  wifiCP;
    MyCheckBoxPreference gpsCP;
    MyCheckBoxPreference blueToothCP;
    MyCheckBoxPreference dataCP;
    MyCheckBoxPreference hapticCP;
    MyCheckBoxPreference autoSyncCP;

    PreferenceCategory  mOpenCat;
    PreferenceCategory  mCloseCat;
    PreferenceCategory  mSetmaxCat;
    
	
   
    ListPreference timeOutLP;
	Preference brightnessPrference;
    
	Button saveBtn, cancelBtn;

	int mMode;
    ModeSettings mModeSettings=null;
	
    boolean mFlyMode;
    boolean mWifi;
    boolean mGps;
    boolean mBT;
    boolean mData;
    boolean mHaptic;
    boolean mAutoSync;
    
    boolean mAutoBrightness;
    int mScreenLockValue;
    int mBrightnessValue=10;

    int mOldBrightnessValue=10;
    boolean mOldAutoBrightness;
    //
    SeekBar adjustSeekBar;


    String sleepRuleAction = "com.lewa.spm_notification_sleep_mode_rule";
	String longRuleAction = "com.lewa.spm_notification_long_mode_rule";
	String longRuleName = "longRule";
	String sleepRuleName = "sleepRule";
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mode_device_operate);
		setContentView(R.layout.user_defined_mode);
		String name = getIntent().getStringExtra(Constants.USER_DEFINED_EXTRA_NAME);
		setTitle(name);
		mMode = getIntent().getIntExtra(Constants.USER_DEFINED_EXTRA_POSITION,
            Constants.SPM_MODE_LONG_ID);
		saveValue = new SharedStorageKeyValuePair(this);
        //read user defined devices status
		mModeSettings= new ModeSettings(this,mMode);
        mModeSettings.getSettings();

        mFlyMode=mModeSettings.flyModeSetted;
        mWifi=mModeSettings.wifiSetted;
        mGps=mModeSettings.gpsSetted;
        mBT=mModeSettings.bluetoothSetted;
        mData=mModeSettings.dataSetted;
        mHaptic=mModeSettings.hapticSetted;
        mAutoSync=mModeSettings.autoSyncSetted;
        //
        mAutoBrightness=mModeSettings.brightnessSetted;
        mBrightnessValue=mModeSettings.brightnessValue;
        mScreenLockValue=mModeSettings.timeOutValue;
        //
		initUI();
        //
		switchState = new SwitchState(this,mMode);
		InquirState inquire = new InquirState(this,mMode);
		mOldAutoBrightness = inquire.lightOfScreenAutoState();
		mOldBrightnessValue = inquire.lightOfScreenValue();
	}

	public void initUI() {
        if(mMode== Constants.SPM_MODE_ALARM_ID){
            mOpenCat=(PreferenceCategory) findPreference("spm_custom_settings_openning");
            mCloseCat=(PreferenceCategory) findPreference("spm_custom_settings_closing");
            mSetmaxCat=(PreferenceCategory) findPreference("spm_custom_settings_setting_max");
            mOpenCat.setTitle(R.string.spm_custom_settings_openning_title_smart);
            mCloseCat.setTitle(R.string.spm_custom_settings_closing_title_smart);
            mSetmaxCat.setTitle(R.string.spm_custom_settings_setting_max_title_smart);
        }
        
        //items
		airPlaneCP = (MyCheckBoxPreference) findPreference(Constants.STR_AIRPLANE); 
		wifiCP = (MyCheckBoxPreference) findPreference(Constants.STR_WIFI);
		gpsCP = (MyCheckBoxPreference) findPreference(Constants.STR_GPS);
		blueToothCP = (MyCheckBoxPreference) findPreference(Constants.STR_BLUETOOTH);
		dataCP = (MyCheckBoxPreference) findPreference(Constants.STR_MOBILE);
		hapticCP = (MyCheckBoxPreference) findPreference(Constants.STR_HAPTIC);
		autoSyncCP = (MyCheckBoxPreference) findPreference("spm_dev_sync");

        //
		brightnessPrference = (Preference) findPreference(Constants.STR_BRIGHTNESS);
		timeOutLP = (ListPreference) findPreference(Constants.STR_LOCKSCREEN);
        //
		setInitValue();
        //button
		saveBtn = (Button) findViewById(R.id.spm_define_mode_save_btn);
		cancelBtn = (Button) findViewById(R.id.spm_define_mode_cancel_btn);
        //items events handler
		brightnessPrference.setOnPreferenceClickListener(this);
		timeOutLP.setOnPreferenceChangeListener(this);
        //botton
		saveBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	public void setInitValue() {
		airPlaneCP.setChecked(mModeSettings.flyModeSetted);
        wifiCP.setChecked(mModeSettings.wifiSetted);
        gpsCP.setChecked(mModeSettings.gpsSetted);
		blueToothCP.setChecked(mModeSettings.bluetoothSetted);
		dataCP.setChecked(mModeSettings.dataSetted);
		hapticCP.setChecked(mModeSettings.hapticSetted);
		autoSyncCP.setChecked(mModeSettings.autoSyncSetted);

        //
		timeOutLP.setValue(mModeSettings.timeOutValue+ "");
	}


	public boolean onPreferenceClick(Preference preference) {
	    if (preference.getKey().equals(Constants.STR_BRIGHTNESS)) {
			createBrightnessDialog();
            return true;
		}
		return false;
	}

	private void createBrightnessDialog() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.brightness_window,
				(ViewGroup) findViewById(R.id.spm_brightness_window_parent));
		CheckBox autoCheckBbox= (CheckBox) layout
				.findViewById(R.id.spm_brightness_automatic);
        
		adjustSeekBar = (SeekBar) layout
				.findViewById(R.id.spm_brightness_seekbar);
        //
		if (Build.MODEL.contains("S5830")) {
            autoCheckBbox.setChecked(false);
            autoCheckBbox.setVisibility(View.GONE);
			mAutoBrightness= false;
            adjustSeekBar.setEnabled(true);
            adjustSeekBar.setVisibility(View.VISIBLE);
		} else {
			autoCheckBbox.setVisibility(View.VISIBLE);
            autoCheckBbox.setChecked(mAutoBrightness); 
            if (autoCheckBbox.isChecked()) {
                adjustSeekBar.setVisibility(View.GONE);
                adjustSeekBar.setEnabled(false);
    		} else {
                 adjustSeekBar.setEnabled(true);
                 adjustSeekBar.setVisibility(View.VISIBLE);
    		}
		}
		adjustSeekBar.setProgress(mBrightnessValue);
        //
		autoCheckBbox
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean value) {
						if (value == true) {
							adjustSeekBar.setVisibility(View.GONE);
							mAutoBrightness=true;
                            
						} else {
							adjustSeekBar.setVisibility(View.VISIBLE);
							mAutoBrightness=false;
                            adjustSeekBar.setEnabled(true);
						}
						switchState.lightOfScreenAutoState(mAutoBrightness);
					}
				});
        //
		OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBark, int progress,
					boolean fromUser) {
				Brightness bt = new Brightness(mMode,UserDefinedModeActivity.this);
				bt.change(false);
				bt.adjust(progress);
				switchState.lightOfScreenValue(progress);
                //save brightness info 
                mBrightnessValue=progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBark) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBark) {
			}
		};
		adjustSeekBar.setOnSeekBarChangeListener(seekBarListener);
        //
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setTitle(getString(R.string.spm_screen_light_show))
				.setPositiveButton(getString(R.string.sp_btn_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						})
				.setNegativeButton(getString(R.string.sp_btn_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								switchState
										.lightOfScreenAutoState(mOldAutoBrightness);
								switchState
										.lightOfScreenValue(mOldBrightnessValue);
								dialog.cancel();
							}
						});

		builder.create().show();

	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean value;
        Log.i("lkr","onPreferenceChange key="+preference.getKey()+"newValue="+newValue);
		if (preference.getKey().equals(Constants.STR_LOCKSCREEN)) {
			final CharSequence[] entries = timeOutLP.getEntries();
			final CharSequence[] values = timeOutLP.getEntryValues();
			mScreenLockValue= Integer.parseInt(values[timeOutLP
					.findIndexOfValue(newValue.toString())].toString());
		}
		return true;
	}


    public void getSettings(){
        Log.i("lkr","getSettings ");
		
		mFlyMode=airPlaneCP.isChecked();

		mWifi=wifiCP.isChecked();

		mGps=gpsCP.isChecked();

		mBT=blueToothCP.isChecked();

		mData=dataCP.isChecked();

		mHaptic=hapticCP.isChecked();

		mAutoSync=autoSyncCP.isChecked();
		
    }

	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		if (view.getId() == saveBtn.getId()) {
            //get
            getSettings();
            //assign
			mModeSettings.flyModeSetted=mFlyMode;
            mModeSettings.wifiSetted=mWifi;
            mModeSettings.gpsSetted=mGps;
            mModeSettings.bluetoothSetted=mBT;
            mModeSettings.dataSetted=mData;
            mModeSettings.hapticSetted=mHaptic;
            mModeSettings.autoSyncSetted=mAutoSync;
            mModeSettings.brightnessSetted=mAutoBrightness;
            mModeSettings.brightnessValue=mBrightnessValue;
            mModeSettings.timeOutValue=mScreenLockValue;
            //save
            mModeSettings.saveSettings();
            //get the mode before change
            int currMode = saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                					Constants.STR_MODE_TYPE_NAME,
                					Constants.SPM_MODE_OUT_ID);

            if(currMode==mMode){
                 Executer execute = new Executer(this, mMode);
                 execute.executSavingPower();
            }

            
            /*
            if(mMode==Constants.SPM_MODE_LONG_ID){
                Intent i = new Intent(longRuleAction);
                Map<String, Object> settingMap = new HashMap<String, Object>();
                
        		settingMap.put(Constants.STR_AIRPLANE, mFlyMode);
        		settingMap.put(Constants.STR_MOBILE, mData);
        		settingMap.put(Constants.STR_LOCKSCREEN, mScreenLockValue);
        		settingMap.put(Constants.STR_HAPTIC, mHaptic);
        		settingMap.put(Constants.STR_BRIGHTNESS, mAutoBrightness);
        		settingMap.put(Constants.STR_SYNC, mAutoBrightness);
        		settingMap.put(Constants.STR_WIFI, mWifi);
        		settingMap.put(Constants.STR_GPS, mGps);
        		settingMap.put(Constants.STR_BLUETOOTH, mBT);

                ArrayList<Map<String, Object>> status = new ArrayList<Map<String, Object>>();
                status.add(settingMap);
				i.putExtra(longRuleName, status);
				sendBroadcast(i);
            }
            */
		} else {
			switchState.lightOfScreenValue(mOldBrightnessValue);
            if (!Build.MODEL.contains("S5830")) {
                 switchState.lightOfScreenAutoState(mOldAutoBrightness);
    		}
			setResult(RESULT_CANCELED, intent);   
		}
		finish();
	}
}
