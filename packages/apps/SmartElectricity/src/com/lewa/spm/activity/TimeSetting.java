package com.lewa.spm.activity;

import com.lewa.spm.R;
import com.lewa.spm.mode.PowSavingAlarm;
import com.lewa.spm.mode.PowerSavingMode;
import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.service.ExecuteSmartMode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimeSetting extends PreferenceActivity implements OnClickListener,
		OnPreferenceClickListener, TimePickerDialog.OnTimeSetListener {

	Preference mBedTimeStartTime, mBedTimeEndTime;
	Button saveBtn, cancelBtn;
	SharedStorageKeyValuePair saveValue;
	int hour, minute;
	int classfy = -1;
	String start_time = null;
	String end_time = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.time_setting);
		setContentView(R.layout.user_defined_mode);
		setTitle(getString(R.string.bedtime_set_time_title));
		saveValue = new SharedStorageKeyValuePair(TimeSetting.this);
		mBedTimeStartTime = (Preference) findPreference(Constants.KEY_BEDTIME_STARTTIME);
		mBedTimeEndTime = (Preference) findPreference(Constants.KEY_BEDTIME_ENDTIME);
		saveBtn = (Button) findViewById(R.id.spm_define_mode_save_btn);
		cancelBtn = (Button) findViewById(R.id.spm_define_mode_cancel_btn);
		start_time = saveValue.getString(Constants.SHARED_PREFERENCE_NAME,
				Constants.KEY_BEDTIME_STARTTIME, Constants.INTEL_TIME_DEF_FROM);
		end_time = saveValue.getString(Constants.SHARED_PREFERENCE_NAME,
				Constants.KEY_BEDTIME_ENDTIME, Constants.INTEL_TIME_DEF_TO);
		mBedTimeStartTime.setSummary(start_time);
		mBedTimeEndTime.setSummary(end_time);
		mBedTimeStartTime.setOnPreferenceClickListener(this);
		mBedTimeEndTime.setOnPreferenceClickListener(this);
		saveBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		start_time = saveValue.getString(Constants.SHARED_PREFERENCE_NAME,
				Constants.KEY_BEDTIME_STARTTIME, Constants.INTEL_TIME_DEF_FROM);
		end_time = saveValue.getString(Constants.SHARED_PREFERENCE_NAME,
				Constants.KEY_BEDTIME_ENDTIME, Constants.INTEL_TIME_DEF_TO);
		mBedTimeStartTime.setSummary(start_time);
		mBedTimeEndTime.setSummary(end_time);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref.getKey().equals(Constants.KEY_BEDTIME_STARTTIME)) {
			classfy = Constants.BEDTIME_STARTTIME;
			hour = Integer.valueOf(mBedTimeStartTime.getSummary().toString()
					.substring(0, 2));
			minute = Integer.valueOf(mBedTimeStartTime.getSummary().toString()
					.substring(3, 5));
			showDialog(Constants.BEDTIME_STARTTIME);
		} else if (pref.getKey().equals(Constants.KEY_BEDTIME_ENDTIME)) {
			classfy = Constants.BEDTIME_ENDTIME;
			hour = Integer.valueOf(mBedTimeEndTime.getSummary().toString()
					.substring(0, 2));
			minute = Integer.valueOf(mBedTimeEndTime.getSummary().toString()
					.substring(3, 5));
			showDialog(Constants.BEDTIME_ENDTIME);
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.BEDTIME_STARTTIME:
			return new TimePickerDialog(this, this, hour, minute,
					DateFormat.is24HourFormat(this));
		case Constants.BEDTIME_ENDTIME:
			return new TimePickerDialog(this, this, hour, minute,
					DateFormat.is24HourFormat(this));
		default:
			break;
		}
		return null;
	}

	@Override
	public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
		String setTimeTemple = null;
		hour = arg1;
		minute = arg2;
		setTimeTemple = fullTimeShow(hour) + Constants.INTEL_TIME_SIGN
				+ fullTimeShow(minute);
		switch (classfy) {
		case Constants.BEDTIME_STARTTIME: {
			mBedTimeStartTime.setSummary(setTimeTemple);
			start_time = setTimeTemple;
			break;
		}
		case Constants.BEDTIME_ENDTIME: {
			mBedTimeEndTime.setSummary(setTimeTemple);
			end_time = setTimeTemple;
			break;
		}
		default:
			break;
		}
	}

	private String fullTimeShow(int time) {
		if (time < 10) {
			return ("0" + time);
		} else {
			return time + "";
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.spm_define_mode_save_btn:
			boolean inInterval = PowerSavingMode.isShowOrNotByTimeCondition(
					start_time, end_time);
			saveValue.saveString(Constants.SHARED_PREFERENCE_NAME,
					            Constants.KEY_BEDTIME_STARTTIME,
					            start_time);
			saveValue.saveString(Constants.SHARED_PREFERENCE_NAME,
					            Constants.KEY_BEDTIME_ENDTIME,
					            end_time);
            boolean isChecked=saveValue.getBoolean(Constants.SHARED_PREFERENCE_NAME,
						                              Constants.INTTELLIGENT_MODE_CHECKED);
            int currMOde= saveValue.getInt(Constants.SHARED_PREFERENCE_NAME,
                    									Constants.STR_MODE_TYPE_NAME,
                    									Constants.SPM_MODE_OUT_ID);

            if(inInterval){
               if(currMOde==Constants.SPM_MODE_ALARM_ID){
                    notifyChange();
    				this.finish();
                }else{
                    ifIsInTheTimeshowDialog(true);
                }
            }else{
                if(currMOde==Constants.SPM_MODE_ALARM_ID){
                    ifIsInTheTimeshowDialog(false);
                }else{
                    notifyChange();
    				this.finish();
                }
            }
			break;
		case R.id.spm_define_mode_cancel_btn:
            Intent i = new Intent(Constants.SPM_TIME_SETTTING_SAVE);
		    Bundle b = new Bundle();
			b.putInt("result_code", 0);
			i.putExtras(b);
			sendBroadcast(i);
			this.finish();
			break;
		default:
			break;
		}
	}

	private void ifIsInTheTimeshowDialog(boolean inInterval) {
        /*
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.in_the_time_dialog,
				(ViewGroup) findViewById(R.id.spm_in_the_time_linearlayout));
		TextView hintMessageTxt = (TextView) layout
				.findViewById(R.id.spm_in_the_time_message_txt);
				*/
		AlertDialog.Builder builder = new Builder(this);
		//builder.setView(layout);
		if (inInterval) {
			builder.setTitle(getString(R.string.spm_in_the_time_title));
			builder.setMessage(getString(R.string.spm_in_the_time_message));
			builder.setPositiveButton(getString(R.string.sp_btn_enter),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							excuteAndSetAlarm();
							dialog.dismiss();
							finish();
						}
					});
			builder.setNegativeButton(getString(R.string.sp_btn_unenter),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
						    notifyChange();
							dialog.dismiss();
							finish();
						}
					});
		} else {
			builder.setTitle(getString(R.string.spm_out_the_time_title));
			builder.setMessage(getString(R.string.spm_out_the_time_message));
			builder.setPositiveButton(getString(R.string.sp_btn_ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							exitAndSetAlarm();
							dialog.dismiss();
							finish();
						}
					});
			builder.setNegativeButton(getString(R.string.sp_btn_no),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
						    notifyChange();
							dialog.dismiss();
							finish();
						}
					});
		}
		builder.create().show();
	}

	private void excuteAndSetAlarm() {
         ExecuteSmartMode exe=new ExecuteSmartMode(this);
         exe.onCreate();
		 notifyChange();
	}

    private void exitAndSetAlarm() {
        ExecuteSmartMode exe=new ExecuteSmartMode(this);
        exe.onDestroy();
		notifyChange();
	}

    private void notifyChange(){
        PowSavingAlarm.cancelStartAlarm(TimeSetting.this);
		PowSavingAlarm.setStartTimeAlarm(TimeSetting.this, start_time);
		PowSavingAlarm.cancelEndAlarm(TimeSetting.this);
		PowSavingAlarm.setEndTimeAlarm(TimeSetting.this, end_time);
		Intent i = new Intent(Constants.SPM_TIME_SETTTING_SAVE);
		Bundle b = new Bundle();
		b.putInt("result_code", 1);
		b.putString("result", start_time + " - " + end_time);
		i.putExtras(b);
		sendBroadcast(i);
    }

}
