package com.lewa.intercept;

import com.lewa.intercept.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.util.UpdateDialogChangeReceiver;

import android.provider.InterceptConstants;

public class BlockSettingActivity extends PreferenceActivity implements OnPreferenceClickListener {

    private CheckBoxPreference mInterceptNotifyPref;
    private CheckBoxPreference mInterceptPref;
    private CheckBoxPreference mInterceptOneRing;
    private Preference mBlockModePref;
    private Preference mBlockTimePref;
    private Preference mUpdate;
    private SharedPreferences settingPreference;
    private UpdateDialogChangeReceiver mUpdateDialogChangeReceiver;

    private int mCurrentMode;
    private int mIsSwitch;
    private String mStartTime;
    private String mEndTime;
    private boolean mResumeAfterPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.intercept_settings);
        initPrefUI();
        mUpdateDialogChangeReceiver = new UpdateDialogChangeReceiver(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(Constants.KEY_INTERCEPT)) {
            boolean isChecked = mInterceptPref.isChecked();
            Settings.System.putInt(this.getContentResolver(), Settings.System.INTERCEPT_SWITCH, isChecked ? 1 : 0);
        } else if (preference.getKey().equals(Constants.KEY_INTERCEPT_NOTIFY)) {
            boolean isChecked = mInterceptNotifyPref.isChecked();
            Settings.System.putInt(this.getContentResolver(), Settings.System.INTERCEPT_NOTIFICATION_SWITCH, isChecked ? 1 : 0);
        } else if (preference.getKey().equals(Constants.KEY_ONERING)){
            boolean isChecked = mInterceptOneRing.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.INTERCEPT_ONERING_SWITCH, isChecked ? 1:0);
        } else if (preference.getKey().equals("update")) {
            ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
            State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            if (mobile == State.CONNECTED || wifi == State.CONNECTED) {
                registerReceiver(mUpdateDialogChangeReceiver, new IntentFilter("update_intercept_dialog"));
                Intent intent = new Intent("check_intelligence_intercept_library");
                sendBroadcast(intent);
            } else {
                Toast.makeText(this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
            }
        }
        
        return false;
    }

    private void initPrefUI() {
        mInterceptNotifyPref = (CheckBoxPreference) findPreference(Constants.KEY_INTERCEPT_NOTIFY);
        mInterceptOneRing = (CheckBoxPreference) findPreference(Constants.KEY_ONERING);
        mInterceptPref = (CheckBoxPreference) findPreference(Constants.KEY_INTERCEPT);
        mBlockModePref = findPreference(Constants.KEY_BLOCK_MODE);
        mBlockTimePref = findPreference(Constants.KEY_BLOCK_SET_TIME);
        mUpdate = findPreference("update");
        settingPreference = getSharedPreferences(Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_READABLE);

        mInterceptNotifyPref.setOnPreferenceClickListener(this);
        mInterceptOneRing.setOnPreferenceClickListener(this);
        mInterceptPref.setOnPreferenceClickListener(this);
        mUpdate.setOnPreferenceClickListener(this);

        initPrefUIData();
    }

    private void initPrefUIData() {
        SharedPreferences sPreferences
                = getSharedPreferences(Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_READABLE);
        mCurrentMode = sPreferences.getInt(Constants.KEY_BLOCK_MODE, Constants.BLOCK_MODE_SMART);
        mIsSwitch = (sPreferences.getBoolean(
                Constants.KEY_SWITCH, Constants.BLOCK_SWITCH_ON_BOOLEAN))
                ? Constants.BLOCK_SWITCH_ON_INT : Constants.BLOCK_SWITCH_OFF_INT;
        mStartTime = (sPreferences.getString(Constants.KEY_START_TIME, Constants.STARTTIME));
        mEndTime = (sPreferences.getString(Constants.KEY_END_TIME, Constants.ENDTIME));

        modePreferenceSetSummary(mCurrentMode);

        timePreferenceSetSummary(mIsSwitch, mStartTime, mEndTime);
        mResumeAfterPause = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mUpdateDialogChangeReceiver);
        } catch(Exception e) {
        }
        mResumeAfterPause = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mResumeAfterPause) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences(
                Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_READABLE);
        int mode = prefs.getInt(Constants.KEY_BLOCK_MODE, Constants.BLOCK_MODE_SMART);
        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            modePreferenceSetSummary(mode);
        }

        int isSwitch = (prefs.getBoolean(
                Constants.KEY_SWITCH, Constants.BLOCK_SWITCH_ON_BOOLEAN))
                ? Constants.BLOCK_SWITCH_ON_INT : Constants.BLOCK_SWITCH_OFF_INT;
        String startTime = (prefs.getString(Constants.KEY_START_TIME, Constants.STARTTIME));
        String endTime = (prefs.getString(Constants.KEY_END_TIME, Constants.ENDTIME));

        if (isSwitch != mIsSwitch || !startTime.equals(mStartTime) || !endTime.equals(mEndTime)) {
            mIsSwitch = isSwitch;
            if (isSwitch == Constants.BLOCK_SWITCH_ON_INT) {
                timePreferenceSetSummary(isSwitch, null, null);
            } else {
                mStartTime = startTime;
                mEndTime = endTime;
                timePreferenceSetSummary(isSwitch, startTime, endTime);
            }
        }
    }

    /**
     * Update UI and pref
     * 
     * @param blockRule
     */
    private void modePreferenceSetSummary(int mode) {
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "modePreferenceSetSummary mode->" + mode);
        }

        switch (mode) {
            case Constants.BLOCK_MODE_SMART:
                mInterceptPref.setSummary(getResources().getString(R.string.intercept_blacksmart));
                mBlockModePref.setSummary(getResources().getString(R.string.intercept_blacksmart));
                break;
            case Constants.BLOCK_MODE_BLACKLIST:
                mInterceptPref.setSummary(getResources().getString(R.string.intercept_blackname));
                mBlockModePref.setSummary(getResources().getString(R.string.intercept_blackname));
                break;
            case Constants.BLOCK_MODE_OUT_OF_WHITELIST:
                mInterceptPref.setSummary(getResources().getString(R.string.intercept_only_accept_whitelist));
                mBlockModePref.setSummary(getResources().getString(R.string.intercept_only_accept_whitelist));
                break;
            case Constants.BLOCK_MODE_ALLNUM:
                mInterceptPref.setSummary(getResources().getString(R.string.intercept_allcall));
                mBlockModePref.setSummary(getResources().getString(R.string.intercept_allcall));
                break;
            case Constants.BLOCK_MODE_EXCEPT_CONTACT:
                mInterceptPref.setSummary(getString(R.string.intercept_nocontacts));
                mBlockModePref.setSummary(getResources().getString(R.string.intercept_nocontacts));
                break;
            default:
                return;
        }
    }

    private void timePreferenceSetSummary(
            int isSwitch, String startTime, String endTime) {
        switch (isSwitch) {
            case Constants.BLOCK_SWITCH_ON_INT:
                mBlockTimePref.setSummary(getResources().getString(R.string.intercept_time_allday));
                break;
            case Constants.BLOCK_SWITCH_OFF_INT:
                mBlockTimePref.setSummary(startTime + "--" + endTime);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        mInterceptPref = null;
        mBlockModePref = null;
        mBlockTimePref = null;
        settingPreference = null;
        super.onDestroy();
    }

    public void refreshUI() {
        initPrefUIData();
    }
}
