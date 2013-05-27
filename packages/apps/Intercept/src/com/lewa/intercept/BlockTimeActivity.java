package com.lewa.intercept;

import java.util.Calendar;

import com.lewa.intercept.R;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;
import android.util.Log;
import com.lewa.intercept.intents.Constants;
import android.database.Cursor;
import com.lewa.intercept.intents.InterceptIntents;
import android.provider.InterceptConstants;
import android.content.ContentValues;

public class BlockTimeActivity extends PreferenceActivity
        implements TimePickerDialog.OnTimeSetListener {
    private final int START_TIME_SETTING_DIALOG = 1;
    private final int END_TIME_SETTING_DIALOG = 2;

    // Preference define
    private CheckBoxPreference misSwitchPref;
    private CheckBoxPreference mSettingTimeCheckPref;
    private Preference mStartTimePref;
    private Preference mEndTimePref;
    private SharedPreferences settingPreference;

    private String timeClassfy = null;
    private int mHour;
    private int mMinute;
    private Calendar startCanledar;
    private Calendar endCanledar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.intercept_settings_time);
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        misSwitchPref = null;
        mSettingTimeCheckPref = null;
        mStartTimePref = null;
        mEndTimePref = null;
        settingPreference = null;
    }

    private void initUI() {

        misSwitchPref = (CheckBoxPreference) findPreference(Constants.KEY_SWITCH);
        mSettingTimeCheckPref = (CheckBoxPreference) findPreference(Constants.KEY_BLOCK_SET_TIME);
        mStartTimePref = (Preference) findPreference(Constants.KEY_START_TIME);
        mEndTimePref = (Preference) findPreference(Constants.KEY_END_TIME);

        changeSettingTimePref(misSwitchPref.getKey());

        misSwitchPref.setOnPreferenceClickListener(mAllDayCheckPrefListener);
        mSettingTimeCheckPref.setOnPreferenceClickListener(mSettingTimeCheckPrefListener);

        settingPreference = getSharedPreferences(Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_READABLE);

        int startHour = settingPreference.getInt(Constants.KEY_START_HOUR, 7);
        int startMunite = settingPreference.getInt(Constants.KEY_START_MINUTE, 0);

        int endHour = settingPreference.getInt(Constants.KEY_END_HOUR, 23);
        int endMunite = settingPreference.getInt(Constants.KEY_END_MINUTE, 0);

        startCanledar = Calendar.getInstance();
        endCanledar = Calendar.getInstance();

        startCanledar.set(Calendar.HOUR_OF_DAY, startHour);
        startCanledar.set(Calendar.MINUTE, startMunite);
        endCanledar.set(Calendar.HOUR_OF_DAY, endHour);
        endCanledar.set(Calendar.MINUTE, endMunite);

        String startTime = pad(startHour) + ":" + pad(startMunite);
        String endTime = pad(endHour) + ":" + pad(endMunite);

        mStartTimePref.setSummary(startTime);
        mEndTimePref.setSummary(endTime);
    }

    private Preference.OnPreferenceClickListener mAllDayCheckPrefListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference arg0) {
            savePreferences(changeSettingTimePref(misSwitchPref.getKey())
                    , mStartTimePref.getSummary(), mEndTimePref.getSummary());
            return true;
        }
    };

    private Preference.OnPreferenceClickListener mSettingTimeCheckPrefListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference arg0) {
            savePreferences(changeSettingTimePref(mSettingTimeCheckPref.getKey())
                    , mStartTimePref.getSummary(), mEndTimePref.getSummary());
            return true;
        }
    };

     /**
     * Update DataBase
     */
    public void updateDataBaseDND(boolean wholeDay,String startTime,String endTime) {
        Cursor cursor = getContentResolver().query(
                InterceptConstants.DND_CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            String selection = InterceptConstants.COLUMN_BLOCK_NAME_ID + " = ?";
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_SWITCH, wholeDay);
            values.put(InterceptConstants.COLUMN_START_TIME, startTime);
            values.put(InterceptConstants.COLUMN_END_TIME, endTime);

            getContentResolver().update(
                        InterceptConstants.DND_CONTENT_URI, values, selection, new String[] { "1"  });
            cursor.close();
            cursor = null;
        } else {
            Log.e(Constants.TAG, "table dnd doesn't exist");
        }
    }

    private boolean changeSettingTimePref(String key) {
        if (key.equals(misSwitchPref.getKey())) {
            boolean isChecked = misSwitchPref.isChecked();
            mSettingTimeCheckPref.setChecked(!isChecked);
            mSettingTimeCheckPref.setEnabled(!isChecked);
            return isChecked;
        } else if (key.equals(mSettingTimeCheckPref.getKey())) {
            boolean isChecked = mSettingTimeCheckPref.isChecked();
            misSwitchPref.setChecked(!isChecked);
            return !isChecked;
        }

        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mStartTimePref) {
            showDialog(START_TIME_SETTING_DIALOG);
        } else if (preference == mEndTimePref) {
            showDialog(END_TIME_SETTING_DIALOG);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case START_TIME_SETTING_DIALOG:
                timeClassfy = Constants.KEY_START_TIME;
                break;
            case END_TIME_SETTING_DIALOG:
                timeClassfy = Constants.KEY_END_TIME;
                break;

            default:
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case START_TIME_SETTING_DIALOG: {
                timeClassfy = Constants.KEY_START_TIME;
                int mHour = settingPreference.getInt(Constants.KEY_START_HOUR, 7);
                int mMinute = settingPreference.getInt(Constants.KEY_START_MINUTE, 0);
                return new TimePickerDialog(this, this, mHour, mMinute, DateFormat.is24HourFormat(this));
            }
            case END_TIME_SETTING_DIALOG: {
                timeClassfy = Constants.KEY_END_TIME;
                int mHour = settingPreference.getInt(Constants.KEY_END_HOUR, 23);
                int mMinute = settingPreference.getInt(Constants.KEY_END_MINUTE, 0);
                return new TimePickerDialog(this, this, mHour, mMinute, DateFormat.is24HourFormat(this));
            }
            default:
                break;
        }
        return null;
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        mHour = hour;
        mMinute = minute;
        if (timeClassfy == Constants.KEY_START_TIME) {
            startCanledar.set(Calendar.HOUR_OF_DAY, hour);
            startCanledar.set(Calendar.MINUTE, minute);
        } else if (timeClassfy == Constants.KEY_END_TIME) {
            endCanledar.set(Calendar.HOUR_OF_DAY, hour);
            endCanledar.set(Calendar.MINUTE, minute);
        }

        updateDisplay();

        savePreferences(changeSettingTimePref(mSettingTimeCheckPref.getKey()), mStartTimePref.getSummary(), mEndTimePref.getSummary());
    }

    private void savePreferences(boolean wholeDay, CharSequence startTime, CharSequence endTime) {
        SharedPreferences.Editor editor
                = getSharedPreferences(Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_WRITEABLE).edit();
        editor.putBoolean(Constants.KEY_SWITCH, wholeDay);
        editor.putString(Constants.KEY_START_TIME, startTime.toString());
        editor.putString(Constants.KEY_END_TIME, endTime.toString());
        int i = startTime.toString().indexOf(':');
        editor.putInt(Constants.KEY_START_HOUR
                , Integer.valueOf(startTime.subSequence(0, i++).toString()));
        editor.putInt(Constants.KEY_START_MINUTE
                , Integer.valueOf(startTime.subSequence(i, startTime.length()).toString()));
        i = endTime.toString().indexOf(':');
        editor.putInt(Constants.KEY_END_HOUR
                , Integer.valueOf(endTime.subSequence(0, i++).toString()));
        editor.putInt(Constants.KEY_END_MINUTE
                , Integer.valueOf(endTime.subSequence(i, endTime.length()).toString()));
        editor.commit();
        
        updateDataBaseDND(!wholeDay,startTime.toString(),endTime.toString());
    }

    private void updateDisplay() {
        if (timeClassfy == Constants.KEY_START_TIME) {
            mStartTimePref.setSummary(pad(mHour) + ":" + pad(mMinute));
        } else if (timeClassfy == Constants.KEY_END_TIME) {
            mEndTimePref.setSummary(pad(mHour) + ":" + pad(mMinute));
        }
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
}
