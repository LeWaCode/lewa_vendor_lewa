package com.lewa.spm;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.lewa.spm.service.MonitorService;
import com.lewa.spm.utils.PrefUtils;

public class SetTimeActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener,TimePickerDialog.OnTimeSetListener {
	
	private final String TAG = "SetTimeActivity";
	
    //ok
    private static final int DIALOG_TIMEPICKER = 0;
    private int tpIndex = 0;//time picker which will open.
    
    //ok
    private CheckBoxPreference mEnablePref;
    private Preference mStartTimePref,mStopTimePref;
    
    //ok
    private final String KEY_ENABLE_PREF = "enable_time_mode_pref";
    private final String KEY_START_TIME_PREF = "start_time_pref";
    private final String KEY_STOP_TIME_PREF = "stop_time_pref";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		addPreferencesFromResource(R.xml.setinteltime);
	}	
	
    private void initUI() {
        boolean timeEnabled = getEnableState();
        
        //ok
        mEnablePref = (CheckBoxPreference) findPreference(KEY_ENABLE_PREF);
        mEnablePref.setChecked(timeEnabled);
         
        //ok
        mStartTimePref = findPreference(KEY_START_TIME_PREF);
        mStartTimePref.setSummary(getSummaryTime(PrefUtils.getInstance(this).getIntelTimeFrom()));
        
        //ok
        mStopTimePref = findPreference(KEY_STOP_TIME_PREF);
        mStopTimePref.setSummary(getSummaryTime(PrefUtils.getInstance(this).getIntelTimeTo()));
        
        //ok
        mStartTimePref.setEnabled(timeEnabled);
        mStopTimePref.setEnabled(timeEnabled);
        
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		String minuteFormat = "";
		
		if(minute<10){
			minuteFormat = "0" + minute;
		}else{
			minuteFormat = String.valueOf(minute);
		}
		
		if(tpIndex==0){//from time
			mStartTimePref.setSummary(hourOfDay+":"+minuteFormat);
			PrefUtils.getInstance(this).setIntelTimeFrom(hourOfDay+":"+minuteFormat);			
		}else{//to time
			mStopTimePref.setSummary(hourOfDay+":"+minuteFormat);
			PrefUtils.getInstance(this).setIntelTimeTo(hourOfDay+":"+minuteFormat);
		}
		this.startService(new Intent(this,MonitorService.class));
	}
	
    private boolean getEnableState() {
    	return PrefUtils.getInstance(this).getModeOn(PrefUtils.INTEL_MODE_TIME_ON);
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
		initUI();
        
	}
	
	//======================
    @Override
    public Dialog onCreateDialog(int id) {    	
        Dialog d = null;
        if(id==DIALOG_TIMEPICKER){
            d = new TimePickerDialog(this,this,0,0,DateFormat.is24HourFormat(this));
            d.setTitle(getResources().getString(R.string.spm_hour));            
        }
        return d;
    }

    @Override
    public void onPrepareDialog(int id, Dialog d) {
    	
    	TimePickerDialog timePicker = (TimePickerDialog)d;
    	
    	int initHour = 0;
    	int initMinute = 0;
    	String initStr = "";
    	if(tpIndex == 0){
    		initStr = getSummaryTime(PrefUtils.getInstance(this).getIntelTimeFrom());    		
    	}else{
    		initStr = getSummaryTime(PrefUtils.getInstance(this).getIntelTimeTo());    		
    	}
    	String temp[] = initStr.split(":");
		initHour = Integer.valueOf(temp[0]);
		initMinute = Integer.valueOf(temp[1]);
    	
        timePicker.updateTime( initHour, initMinute );
        
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    	Log.i(TAG, "onPreferenceTreeClick()");
        if (preference == mStartTimePref) {
        	tpIndex = 0;
            showDialog(DIALOG_TIMEPICKER);
        }else if(preference == mStopTimePref){
        	tpIndex = 1;
        	showDialog(DIALOG_TIMEPICKER);
        }
        return false;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        
    	Log.i(TAG, "onSharedPreferenceChanged="+key);
    	
    	if (key.equals(KEY_ENABLE_PREF)) {
            boolean timeEnabled = preferences.getBoolean(key, true);
            mStartTimePref.setEnabled(timeEnabled);
            mStopTimePref.setEnabled(timeEnabled);
            PrefUtils.getInstance(this).setModeOn(PrefUtils.INTEL_MODE_TIME_ON, timeEnabled);
        }else if (key.equals(KEY_START_TIME_PREF)) {
            mStartTimePref.setSummary(getSummaryTime(mStartTimePref.getSummary().toString()));
            PrefUtils.getInstance(this).setIntelTimeFrom(getSummaryTime(mStartTimePref.getSummary().toString()));
        }else if (key.equals(KEY_STOP_TIME_PREF)) {
            mStopTimePref.setSummary(getSummaryTime(mStopTimePref.getSummary().toString()));
            PrefUtils.getInstance(this).setIntelTimeTo(getSummaryTime(mStartTimePref.getSummary().toString()));
        }else{
        	Log.i(TAG, "onSharedPreferenceChanged(),no key match.");
        }
    }

    private String getSummaryTime(String summaryStr){
    	//TODO check minute 0 or 7 error
    	if(summaryStr.contains("am"))
    		return summaryStr.substring(0, summaryStr.indexOf("am"));
    	else
    		return summaryStr;
    }
    
}
