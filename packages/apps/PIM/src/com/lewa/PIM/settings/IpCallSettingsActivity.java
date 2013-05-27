/*
 * Copyright (c) 2011 LewaTek
 * All rights reserved.
 * 
 * DESCRIPTION:
 *
 * WHEN          | WHO               | what, where, why
 * --------------------------------------------------------------------------------
 * 2011-08-29  | GanFeng          | Create file
 */

package com.lewa.PIM.settings;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lewa.PIM.R;
import com.lewa.PIM.contacts.UnusedIPCallContactActivity;
import com.lewa.PIM.mms.ui.ComposeSearchContactsActivity;
import com.lewa.PIM.mms.ui.NewMessageComposeActivity;
import com.lewa.PIM.sim.SimCard;
import com.lewa.PIM.util.CommonMethod;

public class IpCallSettingsActivity extends PreferenceActivity {
    private static final int SIM1_IP_PREFIX = 0;
    private static final int SIM2_IP_PREFIX = 1;

    private static final String[] IP_PREFIXES_CN_MOBILE  = new String[] {"17951", "12593"};
    private static final String[] IP_PREFIXES_CN_UNICOM  = new String[] {"17911", "10193"};
    private static final String[] IP_PREFIXES_CN_TELECOM = new String[] {"17909", "17908"};

    private static final String KEY_AUTO_IP = "auto_ip";
    private static final String KEY_NUMBER_LOCATION = "number_location";
    private static final String KEY_SIM1_IP_PREFIX = "sim1_ip_prefix";
    private static final String KEY_SIM2_IP_PREFIX = "sim2_ip_prefix";
    //add by zenghuaying
    private static final String KEY_IMS_IP_CALL = "ims_ip_call";
    private static final String KEY_NOT_USE_IP_CALL_NUM = "not_use_ip_call_num";
    private static final int UNUSED_IP_SEETING = 190;
    //add end
    private Preference mImsIpCall;
    private Preference mNotUseIpCallNum;
    
    private CheckBoxPreference mAutoIp;
    private Preference mNumberLocation;
    private Preference mSim1IpPrefix;
    private Preference mSim2IpPrefix;    
    
    
    private String mSelProvince;
    private int mSelIdxProvince;
    private int mSelIdxCity;

    private int mSelIdxPrefix;

    private SharedPreferences mPrefs;
    private String operatorPrefix;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.ipcall_settings);
        mAutoIp = (CheckBoxPreference)findPreference(KEY_AUTO_IP);
        mNumberLocation = (Preference)findPreference(KEY_NUMBER_LOCATION);
        mSim1IpPrefix = (Preference)findPreference(KEY_SIM1_IP_PREFIX);
        mSim2IpPrefix = (Preference)findPreference(KEY_SIM2_IP_PREFIX);
        mImsIpCall = (Preference)findPreference(KEY_IMS_IP_CALL);
        mNotUseIpCallNum = (Preference)findPreference(KEY_NOT_USE_IP_CALL_NUM);
        
        mPrefs = getSharedPreferences("ip_call_settings", Context.MODE_PRIVATE);

        boolean autoIpCall = mPrefs.getBoolean("auto_ip_call", false);
        mAutoIp.setChecked(autoIpCall);
        operatorPrefix = getResources().getString(R.string.ip_call_prefix);
        
        if (SimCard.SINGLE_MODE == SimCard.getPhoneMode()) {
            getPreferenceScreen().removePreference(mSim2IpPrefix);
            String ipPrefix = getIpPrefix(SIM1_IP_PREFIX);
            if (!TextUtils.isEmpty(ipPrefix)) {
                mSim1IpPrefix.setSummary(operatorPrefix + ipPrefix);
            }
        }
        
        String myLocation = mPrefs.getString("my_number_location", null);
        if (!TextUtils.isEmpty(myLocation)) {
            mNumberLocation.setSummary(myLocation);
        }
        
        String imsIpCall = mPrefs.getString("ims_ip_call", null);
        if(!TextUtils.isEmpty(imsIpCall)){
        	mImsIpCall.setSummary(imsIpCall); 
        }
        
        String notUseIpCall = mPrefs.getString("not_use_ip_call", null);
        if(!TextUtils.isEmpty(notUseIpCall)){
        	mNotUseIpCallNum.setSummary(notUseIpCall);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAutoIp) {
            boolean autoIpCall = mAutoIp.isChecked();
            mPrefs.edit().putBoolean("auto_ip_call", autoIpCall).commit();
            android.util.Log.i("zmq", "autoIpCall = "+autoIpCall);
            if (autoIpCall) {
                String myLocation = mPrefs.getString("my_number_location", null);
                if (TextUtils.isEmpty(myLocation)) {
                    openMyNumberProvinceEditor();
                } else if (TextUtils.isEmpty(getIpPrefix(SIM1_IP_PREFIX))) {
                    openIpPrefixEditor(SIM1_IP_PREFIX);
                }
            }
            return true;
        } else if (preference == mSim1IpPrefix) {
        android.util.Log.i("zmq", "mSim1IpPrefix = ");
            openIpPrefixEditor(SIM1_IP_PREFIX);
            return true;
        } else if (preference == mNumberLocation) {
        android.util.Log.i("zmq", "mNumberLocation = ");
            openMyNumberProvinceEditor();
            return true;
        }else if (preference == mImsIpCall){
        	openImsIpCallEditor();
        	return true;
        }else if (preference == mNotUseIpCallNum) {
			openUnusedIpCallNumEditor();
			return true;
		}
        return false;
    }

    private void openIpPrefixEditor(final int editorId) {
        final ArrayList<String> listPrefixes = new ArrayList<String>(IP_PREFIXES_CN_MOBILE.length + 1);
        CommonMethod.EnmSimOperator simOperator = CommonMethod.getSimOperator(this);
        if (CommonMethod.EnmSimOperator.CHINA_UNICOM == simOperator) {
            for (String prefix : IP_PREFIXES_CN_UNICOM) {
                listPrefixes.add(prefix);
            }
        }
        else if (CommonMethod.EnmSimOperator.CHINA_TELECOM == simOperator) {
            for (String prefix : IP_PREFIXES_CN_TELECOM) {
                listPrefixes.add(prefix);
            }
        }
        else {
            for (String prefix : IP_PREFIXES_CN_MOBILE) {
                listPrefixes.add(prefix);
            }
        }
        listPrefixes.add(getResources().getString(R.string.other_ip_prefix));

        mSelIdxPrefix = -1;
        final String ipPrefix = getIpPrefix(editorId);
        if (!TextUtils.isEmpty(ipPrefix)) {
            int count = listPrefixes.size();
            mSelIdxPrefix = count - 1;
            for (int i = 0; i < (count - 1); ++i) {
                if (ipPrefix.equals(listPrefixes.get(i))) {
                    mSelIdxPrefix = i;
                    break;
                }
            }
        }

        DialogInterface.OnClickListener prefixOnClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which >= 0) {
                    mSelIdxPrefix = which;
                    if (mSelIdxPrefix < (listPrefixes.size() - 1)) {
                        String strIpPrefix = listPrefixes.get(mSelIdxPrefix);
                        if (SIM2_IP_PREFIX == editorId) {
                            mPrefs.edit().putString("sim2_ip_prefix", strIpPrefix).commit();
                        } else {
                            mPrefs.edit().putString("sim1_ip_prefix", strIpPrefix).commit();
                        }

                        if (!TextUtils.isEmpty(strIpPrefix)) {
                            mSim1IpPrefix.setSummary(operatorPrefix + strIpPrefix);
                        } else {
                            mSim1IpPrefix.setSummary(R.string.has_not_been_set);
                        }
                    } else {
                        openCustomIpPrefixEditor(editorId, ipPrefix);
                    }
                }

                dialog.dismiss();
            }
        };
        
        String[] choiceItems = new String[listPrefixes.size()];
        for (int i = 0; i < listPrefixes.size(); ++i) {
            choiceItems[i] = listPrefixes.get(i);
        }
        new AlertDialog.Builder(this)
                .setTitle(
                        (SIM1_IP_PREFIX == editorId)? R.string.ip_prefix_sim1_description
                                : ((SIM2_IP_PREFIX == editorId)? R.string.ip_prefix_sim2_description
                                        : R.string.ip_prefix_description))
                .setSingleChoiceItems(choiceItems, mSelIdxPrefix, prefixOnClick)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(ipPrefix)) {
                            closeAutoIpCall();
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

    private void openCustomIpPrefixEditor(final int editorId, final String initPrefix) {
        View viewEditor = View.inflate(this, R.layout.edit_text, null);
        final EditText editPrefix = (EditText )viewEditor.findViewById(R.id.edt_text_field);
        editPrefix.setInputType(InputType.TYPE_CLASS_PHONE);
        if (!TextUtils.isEmpty(initPrefix)) {
            editPrefix.setText(initPrefix);
        } else {
            editPrefix.setHint(R.string.ip_prefix_hint);
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        (SIM1_IP_PREFIX == editorId)? R.string.ip_prefix_sim1_description
                                : ((SIM2_IP_PREFIX == editorId)? R.string.ip_prefix_sim2_description
                                        : R.string.ip_prefix_description))
                .setView(viewEditor)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String strIpPrefix = editPrefix.getText().toString().trim();
                        if (!TextUtils.isEmpty(strIpPrefix)) {
                            if (SIM2_IP_PREFIX == editorId) {
                                mPrefs.edit().putString("sim2_ip_prefix", strIpPrefix).commit();
                            } else {
                                mPrefs.edit().putString("sim1_ip_prefix", strIpPrefix).commit();
                            }

                            if (!TextUtils.isEmpty(strIpPrefix)) {
                                mSim1IpPrefix.setSummary(operatorPrefix + strIpPrefix);
                            } else {
                                mSim1IpPrefix.setSummary(R.string.has_not_been_set);
                            }                            
                        }
                        else {
                            Toast.makeText(IpCallSettingsActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                            if (TextUtils.isEmpty(initPrefix)) {
                                closeAutoIpCall();
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(initPrefix)) {
                            closeAutoIpCall();
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void openMyNumberProvinceEditor() {
        final String[] provinces = getResources().getStringArray(R.array.ip_call_location_provinces);
        final String myLocation = mPrefs.getString("my_number_location", null);
        mSelIdxProvince = -1;
        if (!TextUtils.isEmpty(myLocation)) {
            for (int i = 0; i < provinces.length; ++i) {
                if (myLocation.startsWith(provinces[i])) {
                    mSelIdxProvince = i;
                    break;
                }
            }
        }

        DialogInterface.OnClickListener provinceOnClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which >= 0) {
                    mSelIdxProvince = which;
                    if (mSelIdxProvince <= 3) { //BeiJing/ShangHai/TianJin/ChongQing was selected
                        String strMyLocation = provinces[mSelIdxProvince];
                        mPrefs.edit().putString("my_number_location", strMyLocation).commit();

                        if (!TextUtils.isEmpty(strMyLocation)) {
                            mNumberLocation.setSummary(strMyLocation);
                        } else {
                            mNumberLocation.setSummary(R.string.has_not_been_set);
                        }

                        if (TextUtils.isEmpty(getIpPrefix(SIM1_IP_PREFIX))) {
                            openIpPrefixEditor(SIM1_IP_PREFIX);
                        }
                    } else {
                        mSelProvince = provinces[mSelIdxProvince];
                        openMyNumberCityEditor(mSelIdxProvince);
                    }
                }

                dialog.dismiss();
            }
        };
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.my_number_location_description)
                .setSingleChoiceItems(provinces, mSelIdxProvince, provinceOnClick)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(myLocation)) {
                            closeAutoIpCall();
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }
    
    //add by zenghuaying for bug 7955 @2012.6.30
    private void openImsIpCallEditor() {
        final String[] items = getResources().getStringArray(R.array.ims_ip_call_setting_items);
        final String myImsSettings = mPrefs.getString("ims_ip_call", null);
        mSelIdxProvince = -1;
	
        if (!TextUtils.isEmpty(myImsSettings)) {
            for (int i = 0; i < items.length; ++i) {
                if (myImsSettings.startsWith(items[i])) {
                    mSelIdxProvince = i;
                    break;
                }
            }
        }
	 else {
	 	mSelIdxProvince = 0;
	 }

        DialogInterface.OnClickListener itemsOnClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (which >= 0) {
            		String strItem = items[which];
            		mPrefs.edit().putString("ims_ip_call", strItem).commit();
            		if (!TextUtils.isEmpty(strItem)) {
                        mImsIpCall.setSummary(strItem);
                    } else {
                    	mImsIpCall.setSummary(R.string.has_not_been_set);
                    }
				}
                dialog.dismiss();
            }
        };
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.ims_ip_call_description)
                .setSingleChoiceItems(items, mSelIdxProvince, itemsOnClick)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //if (TextUtils.isEmpty(myLocation)) {
                           // closeAutoIpCall();
                        //}
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }
    
    private void openUnusedIpCallNumEditor(){
    	Intent intent = new Intent(this,UnusedIPCallContactActivity.class);
        startActivityForResult(intent, UNUSED_IP_SEETING);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == 200) {
			boolean isBeenSetting = data.getBooleanExtra("unused_ip_call_setting", false);
			String summary;
			if (isBeenSetting) {
				summary = getResources().getString(R.string.has_been_set);
			}else{
				summary = getResources().getString(R.string.has_not_been_set);
			}
			mNotUseIpCallNum.setSummary(summary);
			mPrefs.edit().putString("not_use_ip_call", summary).commit();
		}
	}
    
    private void openMyNumberCityEditor(int province) {
        String[] tempCities;
        switch (province) {
        case 4: //AnHui
            tempCities = getResources().getStringArray(R.array.ip_call_location_anhui_cities);
            break;

        case 5:
            tempCities = getResources().getStringArray(R.array.ip_call_location_fujian_cities);
            break;
        
        case 6:
            tempCities = getResources().getStringArray(R.array.ip_call_location_gansu_cities);
            break;    
        
        case 7:
            tempCities = getResources().getStringArray(R.array.ip_call_location_guangdong_cities);
            break;

        case 8:
            tempCities = getResources().getStringArray(R.array.ip_call_location_guangxi_cities);
            break;
            
        case 9:
            tempCities = getResources().getStringArray(R.array.ip_call_location_guizhou_cities);
            break;
            
        case 10:
            tempCities = getResources().getStringArray(R.array.ip_call_location_hainan_cities);
            break;
            
        case 11:
            tempCities = getResources().getStringArray(R.array.ip_call_location_hebei_cities);
            break;
            
        case 12:
            tempCities = getResources().getStringArray(R.array.ip_call_location_henan_cities);
            break;
        
        case 13:
            tempCities = getResources().getStringArray(R.array.ip_call_location_heilongjiang_cities);
            break;
            
        case 14:
            tempCities = getResources().getStringArray(R.array.ip_call_location_hubei_cities);
            break;
            
        case 15:
            tempCities = getResources().getStringArray(R.array.ip_call_location_hunan_cities);
            break;
            
        case 16:
            tempCities = getResources().getStringArray(R.array.ip_call_location_jilin_cities);
            break;
            
        case 17:
            tempCities = getResources().getStringArray(R.array.ip_call_location_jiangsu_cities);
            break;
            
        case 18:
            tempCities = getResources().getStringArray(R.array.ip_call_location_jiangxi_cities);
            break;
            
        case 19:
            tempCities = getResources().getStringArray(R.array.ip_call_location_liaoning_cities);
            break;
            
        case 20:
            tempCities = getResources().getStringArray(R.array.ip_call_location_neimenggu_cities);
            break;
            
        case 21:
            tempCities = getResources().getStringArray(R.array.ip_call_location_ningxia_cities);
            break;
            
        case 22:
            tempCities = getResources().getStringArray(R.array.ip_call_location_qinghai_cities);
            break;
            
        case 23:
            tempCities = getResources().getStringArray(R.array.ip_call_location_shandong_cities);
            break;
            
        case 24:
            tempCities = getResources().getStringArray(R.array.ip_call_location_shan1xi_cities);
            break;
            
        case 25:
            tempCities = getResources().getStringArray(R.array.ip_call_location_shan3xi_cities);
            break;
            
        case 26:
            tempCities = getResources().getStringArray(R.array.ip_call_location_sichuan_cities);
            break;
            
        case 27:
            tempCities = getResources().getStringArray(R.array.ip_call_location_xizang_cities);
            break;
            
        case 28:
            tempCities = getResources().getStringArray(R.array.ip_call_location_xinjiang_cities);
            break;
            
        case 29:
            tempCities = getResources().getStringArray(R.array.ip_call_location_yunnan_cities);
            break;
            
        case 30:
            tempCities = getResources().getStringArray(R.array.ip_call_location_zhejiang_cities);
            break;
            
        default:
            return;
            //break;
        }
        
        final String[] cities = tempCities;
        final String myLocation = mPrefs.getString("my_number_location", null);
        mSelIdxCity = 0;
        if (!TextUtils.isEmpty(myLocation)) {
            for (int i = 0; i < cities.length; ++i) {
                if (myLocation.contains(cities[i])) {
                    mSelIdxCity = i;
                    break;
                }
            }
        }

        DialogInterface.OnClickListener cityOnClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which >= 0) {
                    mSelIdxCity = which;

                    String strMyLocation = mSelProvince + cities[mSelIdxCity];
                    mPrefs.edit().putString("my_number_location", strMyLocation).commit();

                    if (!TextUtils.isEmpty(strMyLocation)) {
                        mNumberLocation.setSummary(strMyLocation);
                    }
                    else {
                        mNumberLocation.setSummary(R.string.has_not_been_set);
                    }

                    if (TextUtils.isEmpty(getIpPrefix(SIM1_IP_PREFIX))) {
                        openIpPrefixEditor(SIM1_IP_PREFIX);
                    }
                }

                dialog.dismiss();
            }
        };
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.my_number_location_description)
                .setSingleChoiceItems(cities, mSelIdxCity, cityOnClick)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(myLocation)) {
                            closeAutoIpCall();
                        }
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    

	private String getIpPrefix(int prefixId) {
        if (SIM1_IP_PREFIX == prefixId) {
            return mPrefs.getString("sim1_ip_prefix", null);
        } else {
            return mPrefs.getString("sim2_ip_prefix", null);
        }
    }

    private void closeAutoIpCall() {
        mPrefs.edit().putBoolean("auto_ip_call", false).commit();
        mAutoIp.setChecked(false);
    }
}