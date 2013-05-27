/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.PIM.mms.ui;

import com.lewa.PIM.PimApp;
import com.lewa.PIM.mms.MmsConfig;
import com.lewa.PIM.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.lewa.PIM.mms.templates.TemplatesListActivity;
import com.lewa.PIM.mms.util.Recycler;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.IM.IMClient;
import android.os.SystemProperties;

import android.content.res.Resources;

/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
public class MessagingPreferenceActivity extends PreferenceActivity {
    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String EXPIRY_TIME              = "pref_key_mms_expiry";
    public static final String PRIORITY                 = "pref_key_mms_priority";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    public static final String NOTIFICATION_ENABLED     = "pref_key_enable_notifications";
    public static final String SMS_SPLIT_MESSAGE        = "pref_key_sms_split_160";
    public static final String SMS_SPLIT_COUNTER        = "pref_key_sms_split_counter";
    public static final String NOTIFICATION_VIBRATE     = "pref_key_vibrate";
    public static final String NOTIFICATION_VIBRATE_WHEN= "pref_key_vibrateWhen";
    public static final String NOTIFICATION_RINGTONE    = "pref_key_ringtone";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    public static final String MMS_SAVE_LOCATION        = "pref_save_location";
    public static final String AUTO_DELETE              = "pref_key_auto_delete";
    public static final String BLACK_BACKGROUND         = "pref_key_mms_black_background";
    public static final String BACK_TO_ALL_THREADS      = "pref_key_mms_back_to_all_threads";
    public static final String SEND_ON_ENTER            = "pref_key_mms_send_on_enter";
    public static final String USER_AGENT               = "pref_key_mms_user_agent";
    public static final String USER_AGENT_CUSTOM        = "pref_key_mms_user_agent_custom";
    public static final String FULL_TIMESTAMP           = "pref_key_mms_full_timestamp";
    public static final String ONLY_MOBILE_NUMBERS      = "pref_key_mms_only_mobile_numbers";
    public static final String SENT_TIMESTAMP           = "pref_key_mms_use_sent_timestamp";
    public static final String SENT_TIMESTAMP_GMT_CORRECTION = "pref_key_mms_use_sent_timestamp_gmt_correction";
    public static final String MESSAGE_FONT_SIZE     = "pref_key_mms_message_font_size";
    public static final String EMAIL_ADDR_COMPLETION        = "pref_key_mms_email_addr_completion";
    public static final String NOTIFICATION_VIBRATE_PATTERN = "pref_key_mms_notification_vibrate_pattern";
    public static final String NOTIFICATION_VIBRATE_PATTERN_CUSTOM = "pref_key_mms_notification_vibrate_pattern_custom";
    public static final String NOTIFICATION_VIBRATE_CALL = "pref_key_mms_notification_vibrate_call";
    public static final String MANAGE_TEMPLATES = "pref_key_templates_manage";
    public static final String SHOW_GESTURE = "pref_key_templates_show_gesture";
    public static final String GESTURE_SENSITIVITY = "pref_key_templates_gestures_sensitivity";
    public static final String GESTURE_SENSITIVITY_VALUE = "pref_key_templates_gestures_sensitivity_value";
    public static final String SMS_REALTIME_DIALOG     ="pref_key_sms_realtime_dialog";
    
    public static final String IMS_AOUT_TO_SMS   	  	="prefs_ims_aout_to_sms_mode";
    public static final String IMS_CLOSE_STATE			="pref_key_ims_close";
    public static final String SMS_STORAGE_TO_SIM		="pref_key_storage_to_sim";
    public static final String IMS_FIRST_START			="pref_key_ims_first_start";
    public static final String IMS_FIRST_OPEN			="pref_key_ims_first_open";
    
    public static final String ACTION_YILIAO_FIRST_OPEN = "com.lewa.PIM.IM.YILIAO_FIRST_OPEN";

    public static final String LEWA_EASTER_EGGS_FIRST   ="pref_key_lewa_easter_eggs_first";  
    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;

    //private Preference mSmsLimitPref;
    private Preference mSmsDeliveryReportPref;
    //private Preference mMmsLimitPref;
    private Preference mMmsDeliveryReportPref;
    private Preference mMmsReadReportPref;
    private Preference mManageSimPref;
    private Preference mClearHistoryPref;
    private ListPreference mVibrateWhenPref;
    private Preference mManageTemplate;
    private Recycler mSmsRecycler;
    private Recycler mMmsRecycler;
    //private ListPreference mGestureSensitivity;
    private CheckBoxPreference mSmsRealtimeDialog; 
    private CheckBoxPreference mImsAoutToSms;
    private CheckBoxPreference mImsCloseState;    
    private CheckBoxPreference mSmsStorageToSim;  
    
    private static final int CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG = 3;
    
    private Context mPreContext;   
    private boolean misRestore = false;
    private SharedPreferences mSp;
    private ImsFirstOpenReceiver mImsFirstOpenReceiver;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);

        setMessagePreferences();
        
        mImsFirstOpenReceiver = new ImsFirstOpenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_YILIAO_FIRST_OPEN);
        registerReceiver(mImsFirstOpenReceiver, filter);
    }

    private void setMessagePreferences() {
	 
        mManageSimPref = findPreference("pref_key_manage_sim_messages");
        //mSmsLimitPref = findPreference("pref_key_sms_delete_limit");
        mSmsDeliveryReportPref = findPreference("pref_key_sms_delivery_reports");
        mMmsDeliveryReportPref = findPreference("pref_key_mms_delivery_reports");
        mMmsReadReportPref = findPreference("pref_key_mms_read_reports");
        //mMmsLimitPref = findPreference("pref_key_mms_delete_limit");
        mClearHistoryPref = findPreference("pref_key_mms_clear_history");
        mVibrateWhenPref = (ListPreference) findPreference(NOTIFICATION_VIBRATE_WHEN);
        mManageTemplate = findPreference(MANAGE_TEMPLATES);
        //mGestureSensitivity = (ListPreference) findPreference(GESTURE_SENSITIVITY);
        mSmsRealtimeDialog = (CheckBoxPreference )findPreference("pref_key_sms_realtime_dialog");
        mImsAoutToSms = (CheckBoxPreference )findPreference("prefs_ims_aout_to_sms_mode");
        mImsCloseState = (CheckBoxPreference )findPreference("pref_key_ims_close");
        mSmsStorageToSim = (CheckBoxPreference )findPreference("pref_key_storage_to_sim");
        
        //Added by chenqiang for bug8509,20120717
        mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
        if("TD".equals(SystemProperties.get("ro.product.network"))) {
	  	mSp = getSharedPreferences(SMS_STORAGE_TO_SIM, Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor editor=mSp.edit();
		int location = 0;
		if (mSmsStorageToSim.isChecked()){
			 editor.putString(SMS_STORAGE_TO_SIM, "true");
			 location = 1;
		}else{
			 editor.putString(SMS_STORAGE_TO_SIM, "false");
			 location = 2;
		}
		editor.commit();    
		SmsManager.getDefault().setNewSmsIndication(location);        
	        
	  	mSmsStorageToSim.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		SharedPreferences mSp;
		@Override
		public boolean onPreferenceClick(Preference preference) {
			mSp = getSharedPreferences(SMS_STORAGE_TO_SIM, Context.MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor editor=mSp.edit();
			int mLocation = 0;

			if (mSmsStorageToSim.isChecked()){
				mLocation = 1;
				editor.putString(SMS_STORAGE_TO_SIM, "true");
			}else{
				mLocation = 2;
				editor.putString(SMS_STORAGE_TO_SIM, "false");
			}
		               
			 SmsManager.getDefault().setNewSmsIndication(mLocation);
		        return editor.commit();
	        }
	  	 });     	
	  }
	  else {
		    PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
           	 smsCategory.removePreference(mSmsStorageToSim);
	 }

	 
        
        mPreContext = this;
        
        mSp = getSharedPreferences("smsdialogvalue",Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor=mSp.edit();
        if (mSmsRealtimeDialog.isChecked()){
            editor.putString("smsdialogvalue", "true");
        }else{
            editor.putString("smsdialogvalue", "false");
        }
        editor.commit();

        if (false){
            mSp = getSharedPreferences(IMS_AOUT_TO_SMS, Context.MODE_WORLD_WRITEABLE);
            editor=mSp.edit();
            
            if (mImsAoutToSms.isChecked()){
                editor.putString(IMS_AOUT_TO_SMS, "true");
            }else{
                editor.putString(IMS_AOUT_TO_SMS, "false");
            }
            editor.commit();
            
            mImsCloseState.setChecked(MessageUtils.isImsSwitch(mPreContext));
            
            if (misRestore) {
                mSp = getSharedPreferences(IMS_CLOSE_STATE, Context.MODE_WORLD_WRITEABLE);
                editor=mSp.edit();
                editor.putString(IMS_CLOSE_STATE, "false");
                mImsCloseState.setChecked(false);            
                mImsAoutToSms.setEnabled(false);
                CommonMethod.setLWMsgOnoff(this, false);
                editor.commit();                        
            }

        }else{                     
            mSp = getSharedPreferences(IMS_CLOSE_STATE, Context.MODE_WORLD_WRITEABLE);
            editor=mSp.edit();
            editor.putString(IMS_CLOSE_STATE, "false");
            editor.putString(IMS_AOUT_TO_SMS, "false");            
            editor.commit();  
            CommonMethod.setLWMsgOnoff(this, false);
            
            PreferenceScreen screen = (PreferenceScreen) findPreference("pref_key_sms_mms_settings");
            PreferenceCategory imsSettingCategory = (PreferenceCategory) findPreference("pref_key_ims_settings");
            screen.removePreference(imsSettingCategory);   
        }

        if (!PimApp.getApplication().getTelephonyManager().hasIccCard()) {
            // No SIM card, remove the SIM-related prefs
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mManageSimPref);
        }

        boolean SMSDeliveryReport = Resources.getSystem()
                .getBoolean(com.android.internal.R.bool.config_sms_delivery_reports_support);
        if (!SMSDeliveryReport) {
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mSmsDeliveryReportPref);
            if (!PimApp.getApplication().getTelephonyManager().hasIccCard()) {
                getPreferenceScreen().removePreference(smsCategory);
            }
        }

        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            PreferenceCategory mmsOptions =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            getPreferenceScreen().removePreference(mmsOptions);

//            PreferenceCategory storageOptions =
//                (PreferenceCategory)findPreference("pref_key_storage_settings");
//            storageOptions.removePreference(findPreference("pref_key_mms_delete_limit"));
        } else {
            boolean MMSDeliveryReport = Resources.getSystem()
                    .getBoolean(com.android.internal.R.bool.config_mms_delivery_reports_support);
            boolean MMSReadReport = Resources.getSystem()
                    .getBoolean(com.android.internal.R.bool.config_mms_read_reports_support);
            if (!MMSDeliveryReport) {
                PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsOptions.removePreference(mMmsDeliveryReportPref);
            }
            if (!MMSReadReport) {
                PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsOptions.removePreference(mMmsReadReportPref);
            }
        }

        // If needed, migrate vibration setting from a previous version
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(NOTIFICATION_VIBRATE_WHEN) &&
                sharedPreferences.contains(NOTIFICATION_VIBRATE)) {
            int stringId = sharedPreferences.getBoolean(NOTIFICATION_VIBRATE, false) ?
                    R.string.prefDefault_vibrate_true :
                    R.string.prefDefault_vibrate_false;
            mVibrateWhenPref.setValue(getString(stringId));
            //Added by chenqiang for bug8509,20120717
            mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
        }

        mManageTemplate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MessagingPreferenceActivity.this,
                        TemplatesListActivity.class);
                startActivity(intent);
                return false;
            }
        });

        //String gestureSensitivity = String.valueOf(sharedPreferences.getInt(GESTURE_SENSITIVITY_VALUE, 3));

//        mGestureSensitivity.setSummary(gestureSensitivity);
//        mGestureSensitivity.setValue(gestureSensitivity);
//        mGestureSensitivity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                int value = Integer.parseInt((String) newValue);
//                sharedPreferences.edit().putInt(GESTURE_SENSITIVITY_VALUE, value).commit();
//                mGestureSensitivity.setSummary(String.valueOf(value));
//                return true;
//            }
//        });

        mSmsRecycler = Recycler.getSmsRecycler();
        mMmsRecycler = Recycler.getMmsRecycler();

        // Fix up the recycler's summary with the correct values
        //setSmsDisplayLimit();
        //setMmsDisplayLimit();
        
        mSmsRealtimeDialog.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            SharedPreferences mSp;
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mSp = getSharedPreferences("smsdialogvalue",Context.MODE_WORLD_WRITEABLE);
                SharedPreferences.Editor editor=mSp.edit();
                if (mSmsRealtimeDialog.isChecked()){
                    editor.putString("smsdialogvalue", "true");
                }else{
                    editor.putString("smsdialogvalue", "false");
                }
                return editor.commit();
            }
        });        
        
        mImsAoutToSms.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            SharedPreferences mSp;
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mSp = getSharedPreferences(IMS_AOUT_TO_SMS, Context.MODE_WORLD_WRITEABLE);
                SharedPreferences.Editor editor=mSp.edit();
                if (mImsAoutToSms.isChecked()){
                    editor.putString(IMS_AOUT_TO_SMS, "true");
                }else{
                    editor.putString(IMS_AOUT_TO_SMS, "false");
                }
                return editor.commit();
            }
        });    
        
        mImsCloseState.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            SharedPreferences mSp;
            @Override
            public boolean onPreferenceClick(Preference preference) {
            	
                if (MessageUtils.isImsSwitch(mPreContext) == false) {
                	mSp = getSharedPreferences(IMS_FIRST_OPEN, Context.MODE_WORLD_WRITEABLE);
                	boolean open = mSp.getBoolean(IMS_FIRST_OPEN, true);
                	if (open) {             
                		mImsCloseState.setChecked(false);                		
                    	Intent intent = new Intent(mPreContext, ImsFreeMessageUserProtoclDialog.class);
                    	startActivity(intent);                    	
                    	return false;						
					}
        		}
                
                mSp = getSharedPreferences(IMS_CLOSE_STATE, Context.MODE_WORLD_WRITEABLE);
                SharedPreferences.Editor editor=mSp.edit();
                boolean isOpen = mImsCloseState.isChecked();
                
                if (isOpen){
                    editor.putString(IMS_CLOSE_STATE, "true");
                }else{
                   IMClient.StopIMService(mPreContext, IMMessage.GeXinIM);
                   editor.putString(IMS_CLOSE_STATE, "false");		      
                }
                
                mImsAoutToSms.setEnabled(isOpen);
                editor.commit();
                CommonMethod.setLWMsgOnoff(mPreContext, isOpen);                
                return true;
            }
        });                        

        //Added by chenqiang for bug8509,20120717
        mVibrateWhenPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mVibrateWhenPref.setValue(newValue.toString());
                mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
                return true;
            }
        });
        //End

    }
	
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mImsFirstOpenReceiver != null) {
			unregisterReceiver(mImsFirstOpenReceiver);
		}
	}

//	private void setSmsDisplayLimit() {
//        mSmsLimitPref.setSummary(
//                getString(R.string.pref_summary_delete_limit,
//                        mSmsRecycler.getMessageLimit(this)));
//    }

//    private void setMmsDisplayLimit() {
//        mMmsLimitPref.setSummary(
//                getString(R.string.pref_summary_delete_limit,
//                        mMmsRecycler.getMessageLimit(this)));
//    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
//        if (preference == mSmsLimitPref) {
//            new NumberPickerDialog(this,
//                    mSmsLimitListener,
//                    mSmsRecycler.getMessageLimit(this),
//                    mSmsRecycler.getMessageMinLimit(),
//                    mSmsRecycler.getMessageMaxLimit(),
//                    R.string.pref_title_sms_delete).show();
//        } else if (preference == mMmsLimitPref) {
//            new NumberPickerDialog(this,
//                    mMmsLimitListener,
//                    mMmsRecycler.getMessageLimit(this),
//                    mMmsRecycler.getMessageMinLimit(),
//                    mMmsRecycler.getMessageMaxLimit(),
//                    R.string.pref_title_mms_delete).show();
//        } else 
        if (preference == mManageSimPref) {
            startActivity(new Intent(this, ManageSimMessages.class));
        } else if (preference == mClearHistoryPref) {
            showDialog(CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    private void restoreDefaultPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().clear().commit(); //.edit().clear().apply(); the android with lower version dosn't contain the apply method
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
        misRestore = true;
        setMessagePreferences();
        misRestore = false;
    }

    NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mSmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                //setSmsDisplayLimit();
            }
    };

    NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mMmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                //setMmsDisplayLimit();
            }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG:
                return new AlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.confirm_clear_search_title)
                    .setMessage(R.string.confirm_clear_search_text)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRecentSuggestions recent =
                                ((PimApp)getApplication()).getRecentSuggestions();
                            if (recent != null) {
                                recent.clearHistory();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();
        }
        return super.onCreateDialog(id);
    }
    
	  class ImsFirstOpenReceiver extends BroadcastReceiver {
		  private SharedPreferences mSp;
		  @Override
		  public void onReceive(Context context, Intent intent) {
		  	if (intent.getAction().equals(ACTION_YILIAO_FIRST_OPEN)){
		  		
	            mSp = getSharedPreferences(IMS_CLOSE_STATE, Context.MODE_WORLD_WRITEABLE);
	            SharedPreferences.Editor editor=mSp.edit();            
	            editor.putString(IMS_CLOSE_STATE, "true");            
	            mImsCloseState.setChecked(true);
	            editor.commit();
		      }
		  }
	}
}
