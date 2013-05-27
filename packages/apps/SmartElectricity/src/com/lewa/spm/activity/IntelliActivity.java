package com.lewa.spm.activity;

import java.text.SimpleDateFormat;

import com.lewa.spm.calibrate.CalibrateBattery;
import com.lewa.spm.control.Executer;

import com.lewa.spm.mode.PowSavingAlarm;
import com.lewa.spm.mode.PowerSavingMode;
import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;
import com.lewa.spm.R;
import com.lewa.spm.service.ExecuteSmartMode;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;

public class IntelliActivity extends PreferenceActivity implements
		OnPreferenceClickListener{
	CheckBoxPreference mBedTimeCheck;
	Preference mSetTime, mInTheTimeMode, mBatteryCalibrate;
	// TextView of the battery calibration dialog;
	TextView mbatteryCalibrationMessageTxt,
			mbatteryCalibrationMessageDefineTxt;
	SharedStorageKeyValuePair mSharedStorageKV;
	ImageView mProgressImg;
	Intent modeChoiseIntent;
	int mBatteryLevel = 0;
	int hour;
	int minute;
	int classfy = -1;
	int mode = -1;
	Boolean powerChangeFlag = false;
	CharSequence spModeItems[] = null;
	String currentTime = null;
	String start_time = null;
	String end_time = null;

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				mBatteryLevel = intent.getIntExtra(Constants.STR_POWER_LEVEL, 0); 
                
			} else if (intent.getAction().equals(Constants.SPM_TIME_SETTTING_SAVE)) {
				int result_code = intent.getExtras().getInt("result_code");
				if (result_code == 1) {
					mSetTime.setSummary(intent.getExtras().getString("result").toString());
				}
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.intelligent_sp_setting);
		mSharedStorageKV = new SharedStorageKeyValuePair(this);
		initUI();
		registerReceiver();
	}

	private void registerReceiver() {
		IntentFilter currentIntentFilter = new IntentFilter();
		currentIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		currentIntentFilter.addAction(Constants.SPM_TIME_SETTTING_SAVE);
		registerReceiver(receiver, currentIntentFilter);
	}

	private void initUI() {
		mBedTimeCheck = (CheckBoxPreference) findPreference(Constants.KEY_BEDTIME);
		mSetTime = (Preference) findPreference(Constants.KEY_BEDTIME_SETTIME);
		mInTheTimeMode = (Preference) findPreference(Constants.KEY_BEDTIME_TIME_FRAME_MODE);
		mBatteryCalibrate = (Preference) findPreference(Constants.KEY_BATTERY_CALIBRATION);
		readDataFromSharedPreference();
		mSetTime.setOnPreferenceClickListener(this);
		mInTheTimeMode.setOnPreferenceClickListener(this);
		mBatteryCalibrate.setOnPreferenceClickListener(this);
		mBedTimeCheck
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (!(Boolean) newValue) {
							NotificationManager notifyMgr;
							notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
							notifyMgr.cancel(R.drawable.spm_mode_standby);
							PowSavingAlarm.cancelStartAlarm(IntelliActivity.this);
							PowSavingAlarm.cancelEndAlarm(IntelliActivity.this);
                            ExecuteSmartMode exe=new ExecuteSmartMode(IntelliActivity.this);
                            exe.onDestroy();	
                            mBedTimeCheck.setChecked(false);
                            mSharedStorageKV.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                    									Constants.INTTELLIGENT_MODE_CHECKED,
                    									false);
						} else {

                           mSharedStorageKV.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                    									Constants.INTTELLIGENT_MODE_CHECKED,
                    									true);
							start_time = mSharedStorageKV.getString(
                									Constants.SHARED_PREFERENCE_NAME,
                									Constants.KEY_BEDTIME_STARTTIME,
                									Constants.INTEL_TIME_DEF_FROM);
							end_time = mSharedStorageKV.getString(
                    									Constants.SHARED_PREFERENCE_NAME,
                    									Constants.KEY_BEDTIME_ENDTIME,
                    									Constants.INTEL_TIME_DEF_TO);

                            mBedTimeCheck.setChecked(true);
							boolean bInInterval = PowerSavingMode
									  .isShowOrNotByTimeCondition(start_time,end_time);
							if (bInInterval){
								showIfEnterDialog();
							} else {
								setAlarm(start_time, end_time);
							}
                            
						}
						return true;
					}
				});
	}

	private void readDataFromSharedPreference() {
		boolean modeChecked = mSharedStorageKV.getBoolean(
                        				Constants.SHARED_PREFERENCE_NAME,
                        				Constants.INTTELLIGENT_MODE_CHECKED);
		start_time = mSharedStorageKV.getString(
                                				Constants.SHARED_PREFERENCE_NAME,
                                				Constants.KEY_BEDTIME_STARTTIME,
                                				Constants.INTEL_TIME_DEF_FROM);
		end_time = mSharedStorageKV.getString(Constants.SHARED_PREFERENCE_NAME,
				                             Constants.KEY_BEDTIME_ENDTIME,
				                             Constants.INTEL_TIME_DEF_TO);

		mBedTimeCheck.setChecked(modeChecked);
		mSetTime.setSummary(start_time + " - " + end_time);
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		if (pref.getKey().equals(Constants.KEY_BEDTIME_SETTIME)) {
			Intent intent = new Intent(IntelliActivity.this, TimeSetting.class);
			startActivityForResult(intent, 10);
		} else if (pref.getKey().equals(Constants.KEY_BEDTIME_TIME_FRAME_MODE)) {
		
			Intent intent = new Intent();
			intent.putExtra(Constants.USER_DEFINED_EXTRA_NAME,
					getString(R.string.intelligent_time_frame_mode));
			intent.putExtra(Constants.USER_DEFINED_EXTRA_POSITION, Constants.SPM_MODE_ALARM_ID);
			intent.setClass(this, UserDefinedModeActivity.class);
			startActivityForResult(intent, 0);
		} else if (pref.getKey().equals(Constants.KEY_BATTERY_CALIBRATION)) {
			doBatteryCalibrate();
		}
		return true;
	}

	/**
	 * Calibrate the battery if the power is equals 100
	 */
	private void doBatteryCalibrate() {
		if (mBatteryLevel < 100) {
			Toast.makeText(this,
					getString(R.string.spm_battery_not_full_notification),
					Toast.LENGTH_SHORT).show();
		} else {
			showCalibrateBatteryDialog(
					getString(R.string.spm_battery_calibration_notification_message),
					getString(R.string.spm_battery_calibration_notification_message_define),
					getString(R.string.spm_battery_calibration_notification_message));
		}
	}

	private void showCalibrateBatteryDialog(String msg, String msgDefine,
			String type) {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater
				.inflate(
						R.layout.battery_calibration_message_box,
						(ViewGroup) findViewById(R.id.battery_calibration_message_linearlayout));
		mbatteryCalibrationMessageTxt = (TextView) layout
				.findViewById(R.id.battery_calibration_message_txt);
		mbatteryCalibrationMessageDefineTxt = (TextView) layout
				.findViewById(R.id.battery_calibration_message_define);
		mbatteryCalibrationMessageTxt.setText(msg);
		mbatteryCalibrationMessageDefineTxt.setText(msgDefine);
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(getString(R.string.spm_battery_calibration));
		builder.setView(layout);
		if (type.equals(getString(R.string.spm_battery_calibration_success))) {
			builder.setPositiveButton(getString(R.string.sp_btn_finish),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
		} else {
			builder.setPositiveButton(getString(R.string.sp_btn_calibration),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							CalibrateBattery cb = new CalibrateBattery();
							cb.deleteFile();
							dialog.dismiss();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							createCalibrateBatterySuccessDialog();
						}
					});
			builder.setNegativeButton(getString(R.string.sp_btn_no),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
		}
		builder.create().show();
	}

	/**
	 * show the dialog of calibrate battery
	 */
	private void createCalibrateBatterySuccessDialog() {
		CalibrateBattery cb = new CalibrateBattery();
		if (!(cb.queryFile())) {
			showCalibrateBatteryDialog(
					getString(R.string.spm_battery_calibration_success),
					getString(R.string.spm_battery_calibration_success_define),
					getString(R.string.spm_battery_calibration_success));
		}
	}

	private void showIfEnterDialog() {
        /*
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.in_the_time_dialog,
				(ViewGroup) findViewById(R.id.spm_in_the_time_linearlayout));
		TextView hintMessageTxt = (TextView) layout
				.findViewById(R.id.spm_in_the_time_message_txt);
				*/
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//builder.setView(layout);
		builder.setTitle(getString(R.string.spm_in_the_time_title));
		builder.setMessage(getString(R.string.spm_in_the_time_message));
		builder.setPositiveButton(getString(R.string.sp_btn_enter),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
					    TelephonyManager tm;
					    tm = (TelephonyManager) IntelliActivity.this.getSystemService(IntelliActivity.this.TELEPHONY_SERVICE);
            			boolean calling = (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE);
            			if (calling != true) {
                            ExecuteSmartMode exe=new ExecuteSmartMode(IntelliActivity.this);
                            exe.onCreate();
    						setAlarm(start_time, end_time);
            			} 
                        
                        mSharedStorageKV.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                        					Constants.SPM_ENTRY_INTILLI_MODE_ON_TIME,
                        					true);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(getString(R.string.sp_btn_unenter),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int arg1) {
					    mSharedStorageKV.saveBoolean(Constants.SHARED_PREFERENCE_NAME,
                        					Constants.SPM_ENTRY_INTILLI_MODE_ON_TIME,
                        					false);
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void setAlarm(String start_time, String end_time) {
		PowSavingAlarm.cancelStartAlarm(this);
		PowSavingAlarm.setStartTimeAlarm(this, start_time);
		PowSavingAlarm.cancelEndAlarm(this);
		PowSavingAlarm.setEndTimeAlarm(this, end_time);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

}
