/******************************************************************************
*     When        |      Who         |   What, Where,Why                      *
*-----------------------------------------------------------------------------*
*   2012-04-24    |      zhumeiquan   | Create the file for CDMA callforwarding *
******************************************************************************/

package com.android.phone;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.text.TextUtils;
import android.util.Log;

public class CdmaCallForwardOptions extends PreferenceActivity
    implements EditPhoneNumberPreference.OnDialogClosedListener {

    private static final String BUTTON_ACTIVATION_KEY = "button_cdma_activation_key";
    private static final String BUTTON_DEACTIVATION_KEY = "button_cdma_deactivation_key";
    private static final int CF_NUMBER_PREF_ID = 1;
    private static final boolean DBG = false;
    private static final String TAG = "CdmaCallForward";
    private static final String NUM_PROJECTION[] =  new String[]{"data1"};
    private int dialogTitleId;
    private EditPhoneNumberPreference mButtonActivation;
    private Preference mButtonDeactivation;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        addPreferencesFromResource(R.xml.cdma_callforward_options);
        
        dialogTitleId = getIntent().getIntExtra("cf_dialog_title_id", 0);
        mButtonActivation = (EditPhoneNumberPreference)findPreference("button_cdma_activation_key");
        if (mButtonActivation != null) {
            mButtonActivation.setDialogOnClosedListener(this);
            mButtonActivation.setParentActivity(this, 1, null);
            mButtonActivation.setDialogTitle(dialogTitleId);
        }
        mButtonDeactivation = findPreference("button_cdma_deactivation_key");
    }    

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
        .setMessage(R.string.no_feature_code)
        .setTitle(R.string.error_updating_title)
        .setNegativeButton(R.string.close_dialog, null)
        .create()
        .show();
    }

    private void transferToDial(boolean bActivate, String number) {
        String prefix = "";
        PhoneApp instance = PhoneApp.getInstance();
        switch (dialogTitleId) {
            case R.string.messageCFU: {
                prefix = bActivate ? instance.cfu_activation : instance.cfu_deactivation;
                break; 
            }
            
            case R.string.messageCFB: {
                prefix = bActivate ? instance.cfb_activation : instance.cfb_deactivation;
                break; 
            }
            
            case R.string.messageCFNRc: {
                prefix = bActivate ? instance.cfnrc_activation : instance.cfnrc_deactivation;
                break; 
            }
            
            case R.string.messageCFNRy: {
                prefix = bActivate ? instance.cfnry_activation : instance.cfnry_deactivation;
                break; 
            }
            
            default:
                break;
        }
            
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(prefix)) {
            Log.i(TAG, "feature code is null, return directly");
            showErrorDialog();
        } else {
            sb.append(prefix);
            if (!TextUtils.isEmpty(number)) {
                sb.append(number);
            }
            Uri uri = Uri.fromParts("tel", sb.toString(), null);
            Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, NUM_PROJECTION, null, null, null);
            if(cursor != null && cursor.moveToFirst()) {
                switch(requestCode) {
                    case 1: 
                        mButtonActivation.onPickActivityResult(cursor.getString(0));
                        break;
                    default:
                        break;
                }
            }
        }  
    }

    public void onDialogClosed(EditPhoneNumberPreference preference, int buttonClicked) {        
        if (buttonClicked == -1 && preference == mButtonActivation) {                  
            String s = mButtonActivation.getPhoneNumber();
            transferToDial(true, s);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mButtonActivation) {
            return true;
        } else if (preference == mButtonDeactivation) {
            transferToDial(false, null);
            return true;
        }
        return false;
    }
}
