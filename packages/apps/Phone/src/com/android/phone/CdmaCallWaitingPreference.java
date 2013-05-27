/******************************************************************************
*     When        |      Who         |   What, Where,Why                      *
*-----------------------------------------------------------------------------*
*   2012-04-24    |      zhumeiquan   | Create the file for CDMA callwaiting  *
******************************************************************************/

package com.android.phone;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.text.TextUtils;

public class CdmaCallWaitingPreference extends PreferenceActivity {
    public static final String BUTTON_CW_OFF_KEY = "button_cdma_cw_off_key";
    public static final String BUTTON_CW_ON_KEY = "button_cdma_cw_on_key";
    private Preference mButtonCwOff;
    private Preference mButtonCwOn;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.cdma_call_waiting);
        
        mButtonCwOn = findPreference("button_cdma_cw_on_key");
        mButtonCwOff = findPreference("button_cdma_cw_off_key");
    }
    
    private void dialFc(String s) {
        if (!TextUtils.isEmpty(s)) {
            Uri uri = Uri.fromParts("tel", s, null);
            Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
        .setMessage(R.string.no_feature_code)
        .setTitle(R.string.error_updating_title)
        .setNegativeButton(R.string.close_dialog, null)
        .create()
        .show();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean flag = false;
        if (preference == mButtonCwOn) {
            if (PhoneApp.getInstance().cw_activation == null) {
                showErrorDialog();
                flag = false;
            } else {
                String s = PhoneApp.getInstance().cw_activation;
                dialFc(s);
                flag = true;
            }
        } else {
            if (preference == mButtonCwOff) {
                if(PhoneApp.getInstance().cw_deactivation == null) {
                    showErrorDialog();
                    flag = false;
                } else {
                    String s = PhoneApp.getInstance().cw_deactivation;
                    dialFc(s);
                    flag = true;
                }
            } else {
                flag = false;
            }
        }
        return flag;
    }
}
