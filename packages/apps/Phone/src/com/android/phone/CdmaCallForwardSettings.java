/******************************************************************************
*     When        |      Who         |   What, Where,Why                      *
*-----------------------------------------------------------------------------*
*   2012-04-24    |      zhumeiquan   | Create the file for CDMA callforwarding *
******************************************************************************/
package com.android.phone;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;

public class CdmaCallForwardSettings extends PreferenceActivity {
    public static final String BUTTON_CFB_KEY = "button_cdma_cfb_key";
    public static final String BUTTON_CFNCA_KEY = "button_cdma_cfca_key";
    public static final String BUTTON_CFNRC_KEY = "button_cdma_cfnrc_key";
    public static final String BUTTON_CFNRY_KEY = "button_cdma_cfnry_key";
    public static final String BUTTON_CFU_KEY = "button_cdma_cfu_key";
    public static final String CF_DIALOG_TITLE_ID = "cf_dialog_title_id";
    public static final String CF_KEY = "cf_selection";

    private Preference mButtonCFB;
    private Preference mButtonCFCa;
    private Preference mButtonCFNRc;
    private Preference mButtonCFNRy;
    private Preference mButtonCFU;

    private void showCFOptions(int resId) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(this, CdmaCallForwardOptions.class);
        intent.putExtra("cf_dialog_title_id", resId);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        addPreferencesFromResource(R.xml.cdma_callforward_settings);        
        mButtonCFU = findPreference("button_cdma_cfu_key");
        mButtonCFB = findPreference("button_cdma_cfb_key");
        mButtonCFNRy = findPreference("button_cdma_cfnry_key");
        mButtonCFNRc = findPreference("button_cdma_cfnrc_key");
        mButtonCFCa = findPreference("button_cdma_cfca_key");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean flag = false;
        if (preference == mButtonCFU) {
            showCFOptions(R.string.messageCFU);
            flag = true;
        } else {
            if (preference == mButtonCFB) {
                showCFOptions(R.string.messageCFB);
                flag = true;
            } else {
                if (preference == mButtonCFNRy) {
                    showCFOptions(R.string.messageCFNRy);
                    flag = true;
                } else {
                    if(preference == mButtonCFNRc) {
                        showCFOptions(R.string.messageCFNRc);
                        flag = true;
                    } else {
                        if (preference == mButtonCFCa) {
                            if (PhoneApp.getInstance().cf_all_deactivation == null) {
                                new AlertDialog.Builder(this)
                                .setTitle(R.string.error_updating_title)
                                .setMessage(R.string.no_feature_code)
                                .setNegativeButton(R.string.close_dialog, null)
                                .create()
                                .show();
                                flag = false;
                            } else {
                                String s = PhoneApp.getInstance().cf_all_deactivation;
                                Uri uri = Uri.fromParts("tel", s, null);
                                Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                flag = true;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }
        }
        return flag;
    }
}
