/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.speech.RecognitionService;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class LanguageSettings extends PreferenceActivity implements OnPreferenceChangeListener {
    // Begin, added by zhumeiquan for voice_input_output_settings, 20111121
    private static final String TAG = "VoiceInputOutputSettings";
    private static final String KEY_PARENT = "parent";
    private static final String KEY_VOICE_INPUT_CATEGORY = "voice_input_category";
    private static final String KEY_RECOGNIZER = "recognizer";
    private static final String KEY_RECOGNIZER_SETTINGS = "recognizer_settings";
    
    private PreferenceGroup mParent;
    private PreferenceCategory mVoiceInputCategory;
    private ListPreference mRecognizerPref;
    private PreferenceScreen mSettingsPref;
    
    private HashMap<String, ResolveInfo> mAvailableRecognizersMap;
    // End
    
    private static final String KEY_PHONE_LANGUAGE = "phone_language";
    private static final String KEY_KEYBOARD_SETTINGS_CATEGORY = "keyboard_settings_category";
    private static final String KEY_HARDKEYBOARD_CATEGORY = "hardkeyboard_category";
    private boolean mHaveHardKeyboard;

    private List<InputMethodInfo> mInputMethodProperties;
    private List<CheckBoxPreference> mCheckboxes;
    private Preference mLanguagePref;

    final TextUtils.SimpleStringSplitter mStringColonSplitter
            = new TextUtils.SimpleStringSplitter(':');
    
    private String mLastInputMethodId;
    private String mLastTickedInputMethodId;

    private AlertDialog mDialog = null;
    
    static public String getInputMethodIdFromKey(String key) {
        return key;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.language_settings);

        if (getAssets().getLocales().length == 1) {
            getPreferenceScreen().
                removePreference(findPreference(KEY_PHONE_LANGUAGE));
        } else {
            mLanguagePref = findPreference(KEY_PHONE_LANGUAGE);
        }

        Configuration config = getResources().getConfiguration();
        if (config.keyboard != Configuration.KEYBOARD_QWERTY) {
            getPreferenceScreen().removePreference(
                    getPreferenceScreen().findPreference(KEY_HARDKEYBOARD_CATEGORY));
        } else {
            mHaveHardKeyboard = true;
        }
        mCheckboxes = new ArrayList<CheckBoxPreference>();
        onCreateIMM();

        // Begin, added by zhumeiquan for voice_input_output_settings, 20111121
        mParent = (PreferenceGroup) findPreference(KEY_PARENT);
        mVoiceInputCategory = (PreferenceCategory) findPreference(KEY_VOICE_INPUT_CATEGORY);
        mRecognizerPref = (ListPreference) findPreference(KEY_RECOGNIZER);
        mRecognizerPref.setOnPreferenceChangeListener(this);
        mSettingsPref = (PreferenceScreen) findPreference(KEY_RECOGNIZER_SETTINGS);
        
        mAvailableRecognizersMap = new HashMap<String, ResolveInfo>();
        
        populateOrRemoveRecognizerPreference();        
        // End
    }
    
    private boolean isSystemIme(InputMethodInfo property) {
        return (property.getServiceInfo().applicationInfo.flags
                & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
    
    private void onCreateIMM() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mInputMethodProperties = imm.getInputMethodList();

        mLastInputMethodId = Settings.Secure.getString(getContentResolver(),
            Settings.Secure.DEFAULT_INPUT_METHOD);
        
        PreferenceGroup keyboardSettingsCategory = (PreferenceGroup) findPreference(
                KEY_KEYBOARD_SETTINGS_CATEGORY);
        
        int N = (mInputMethodProperties == null ? 0 : mInputMethodProperties
                .size());
        for (int i = 0; i < N; ++i) {
            InputMethodInfo property = mInputMethodProperties.get(i);
            String prefKey = property.getId();

            CharSequence label = property.loadLabel(getPackageManager());
            boolean systemIME = isSystemIme(property);
            // Add a check box.
            // Don't show the toggle if it's the only keyboard in the system, or it's a system IME.
            if (mHaveHardKeyboard || (N > 1 && !systemIME)) {
                CheckBoxPreference chkbxPref = new CheckBoxPreference(this);
                chkbxPref.setKey(prefKey);
                chkbxPref.setTitle(label);
                keyboardSettingsCategory.addPreference(chkbxPref);
                mCheckboxes.add(chkbxPref);
            }

            // If setting activity is available, add a setting screen entry.
            if (null != property.getSettingsActivity()) {
                PreferenceScreen prefScreen = new PreferenceScreen(this, null);
                String settingsActivity = property.getSettingsActivity();
                if (settingsActivity.lastIndexOf("/") < 0) {
                    settingsActivity = property.getPackageName() + "/" + settingsActivity;
                }
                prefScreen.setKey(settingsActivity);
                prefScreen.setTitle(label);
                if (N == 1) {
                    prefScreen.setSummary(getString(R.string.onscreen_keyboard_settings_summary));
                } else {
                    CharSequence settingsLabel = getResources().getString(
                            R.string.input_methods_settings_label_format, label);
                    prefScreen.setSummary(settingsLabel);
                }
                keyboardSettingsCategory.addPreference(prefScreen);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final HashSet<String> enabled = new HashSet<String>();
        String enabledStr = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_INPUT_METHODS);
        if (enabledStr != null) {
            final TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
            splitter.setString(enabledStr);
            while (splitter.hasNext()) {
                enabled.add(splitter.next());
            }
        }
        
        // Update the statuses of the Check Boxes.
        int N = mInputMethodProperties.size();
        for (int i = 0; i < N; ++i) {
            final String id = mInputMethodProperties.get(i).getId();
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(mInputMethodProperties
                    .get(i).getId());
            if (pref != null) {
                pref.setChecked(enabled.contains(id));
            }
        }
        mLastTickedInputMethodId = null;

        if (mLanguagePref != null) {
            Configuration conf = getResources().getConfiguration();
            String locale = conf.locale.getDisplayName(conf.locale);
            if (locale != null && locale.length() > 1) {
                locale = Character.toUpperCase(locale.charAt(0)) + locale.substring(1);
                
//liuhao fix #8641 launguage bug, ugly code
                String[] mSpecialLocaleCodes;
                String[] mSpecialLocaleNames;
                mSpecialLocaleCodes = getResources().getStringArray(R.array.special_locale_codes);
                mSpecialLocaleNames = getResources().getStringArray(R.array.special_locale_names);
                String code = conf.locale.toString();

                for (int i = 0; i < mSpecialLocaleCodes.length; i++) {
                    if (mSpecialLocaleCodes[i].equals(code)) {
                        locale = mSpecialLocaleNames[i];
                    }
                }
//liuhao fix end

                mLanguagePref.setSummary(locale);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        StringBuilder builder = new StringBuilder(256);
        StringBuilder disabledSysImes = new StringBuilder(256);

        int firstEnabled = -1;
        int N = mInputMethodProperties.size();
        for (int i = 0; i < N; ++i) {
            final InputMethodInfo property = mInputMethodProperties.get(i);
            final String id = property.getId();
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(id);
            boolean hasIt = id.equals(mLastInputMethodId);
            boolean systemIme = isSystemIme(property); 
            if (((N == 1 || systemIme) && !mHaveHardKeyboard) 
                    || (pref != null && pref.isChecked())) {
                if (builder.length() > 0) builder.append(':');
                builder.append(id);
                if (firstEnabled < 0) {
                    firstEnabled = i;
                }
            } else if (hasIt) {
                mLastInputMethodId = mLastTickedInputMethodId;
            }
            // If it's a disabled system ime, add it to the disabled list so that it
            // doesn't get enabled automatically on any changes to the package list
            if (pref != null && !pref.isChecked() && systemIme && mHaveHardKeyboard) {
                if (disabledSysImes.length() > 0) disabledSysImes.append(":");
                disabledSysImes.append(id);
            }
        }

        // If the last input method is unset, set it as the first enabled one.
        if (null == mLastInputMethodId || "".equals(mLastInputMethodId)) {
            if (firstEnabled >= 0) {
                mLastInputMethodId = mInputMethodProperties.get(firstEnabled).getId();
            } else {
                mLastInputMethodId = null;
            }
        }
        
        Settings.Secure.putString(getContentResolver(),
            Settings.Secure.ENABLED_INPUT_METHODS, builder.toString());
        Settings.Secure.putString(getContentResolver(),
                Settings.Secure.DISABLED_SYSTEM_INPUT_METHODS, disabledSysImes.toString());
        Settings.Secure.putString(getContentResolver(),
            Settings.Secure.DEFAULT_INPUT_METHOD,
            mLastInputMethodId != null ? mLastInputMethodId : "");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        
        // Input Method stuff
        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference instanceof CheckBoxPreference) {
            final CheckBoxPreference chkPref = (CheckBoxPreference) preference;
            final String id = getInputMethodIdFromKey(chkPref.getKey());
            if (chkPref.isChecked()) {
                InputMethodInfo selImi = null;
                final int N = mInputMethodProperties.size();
                for (int i=0; i<N; i++) {
                    InputMethodInfo imi = mInputMethodProperties.get(i);
                    if (id.equals(imi.getId())) {
                        selImi = imi;
                        if (isSystemIme(imi)) {
                            // This is a built-in IME, so no need to warn.
                            mLastTickedInputMethodId = id;
                            return super.onPreferenceTreeClick(preferenceScreen, preference);
                        }
                    }
                }
                chkPref.setChecked(false);
                if (selImi == null) {
                    return super.onPreferenceTreeClick(preferenceScreen, preference);
                }
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                mDialog = (new AlertDialog.Builder(this))
                        .setTitle(android.R.string.dialog_alert_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        chkPref.setChecked(true);
                                        mLastTickedInputMethodId = id;
                                    }
                        })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                        })
                        .create();
                mDialog.setMessage(getString(R.string.ime_security_warning,
                        selImi.getServiceInfo().applicationInfo.loadLabel(
                                getPackageManager())));
                mDialog.show();
            } else if (id.equals(mLastTickedInputMethodId)) {
                mLastTickedInputMethodId = null;
            }
        } else if (preference instanceof PreferenceScreen) {
            if (preference.getIntent() == null) {
                PreferenceScreen pref = (PreferenceScreen) preference;
                String activityName = pref.getKey();
                String packageName = activityName.substring(0, activityName
                        .lastIndexOf("."));
                int slash = activityName.indexOf("/");
                if (slash > 0) {
                    packageName = activityName.substring(0, slash);
                    activityName = activityName.substring(slash + 1);
                }
                if (activityName.length() > 0) {
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setClassName(packageName, activityName);
                    startActivity(i);
                }
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    // Begin, added by zhumeiquan for voice_input_output_settings, 20111121
    private void populateOrRemoveRecognizerPreference() {
        List<ResolveInfo> availableRecognitionServices = getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE), PackageManager.GET_META_DATA);
        int numAvailable = availableRecognitionServices.size();
        
        if (numAvailable == 0) {
            // No recognizer available - remove all related preferences.
            removePreference(mVoiceInputCategory);
            removePreference(mRecognizerPref);
            removePreference(mSettingsPref);
        } else if (numAvailable == 1) {
            // Only one recognizer available, so don't show the list of choices, but do
            // set up the link to settings for the available recognizer.
            removePreference(mRecognizerPref);
            
            // But first set up the available recognizers map with just the one recognizer.
            ResolveInfo resolveInfo = availableRecognitionServices.get(0);
            String recognizerComponent =
                    new ComponentName(resolveInfo.serviceInfo.packageName,
                            resolveInfo.serviceInfo.name).flattenToShortString();
            
            mAvailableRecognizersMap.put(recognizerComponent, resolveInfo);
            
            String currentSetting = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.VOICE_RECOGNITION_SERVICE);
            updateSettingsLink(currentSetting);
        } else {
            // Multiple recognizers available, so show the full list of choices.
            populateRecognizerPreference(availableRecognitionServices);
        }
    }

    private void removePreference(Preference pref) {
        if (pref != null) {
            mParent.removePreference(pref);
        }
    }
    
    private void populateRecognizerPreference(List<ResolveInfo> recognizers) {
        int size = recognizers.size();
        CharSequence[] entries = new CharSequence[size];
        CharSequence[] values = new CharSequence[size];
        
        // Get the current value from the secure setting.
        String currentSetting = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.VOICE_RECOGNITION_SERVICE);
        
        // Iterate through all the available recognizers and load up their info to show
        // in the preference. Also build up a map of recognizer component names to their
        // ResolveInfos - we'll need that a little later.
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = recognizers.get(i);
            String recognizerComponent =
                    new ComponentName(resolveInfo.serviceInfo.packageName,
                            resolveInfo.serviceInfo.name).flattenToShortString();
            
            mAvailableRecognizersMap.put(recognizerComponent, resolveInfo);

            entries[i] = resolveInfo.loadLabel(getPackageManager());
            values[i] = recognizerComponent;
        }
        
        mRecognizerPref.setEntries(entries);
        mRecognizerPref.setEntryValues(values);
        
        mRecognizerPref.setDefaultValue(currentSetting);
        mRecognizerPref.setValue(currentSetting);
        
        updateSettingsLink(currentSetting);
    }
    
    private void updateSettingsLink(String currentSetting) {
        ResolveInfo currentRecognizer = mAvailableRecognizersMap.get(currentSetting);
        ServiceInfo si = currentRecognizer.serviceInfo;
        XmlResourceParser parser = null;
        String settingsActivity = null;
        try {
            parser = si.loadXmlMetaData(getPackageManager(), RecognitionService.SERVICE_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No " + RecognitionService.SERVICE_META_DATA +
                        " meta-data for " + si.packageName);
            }
            
            Resources res = getPackageManager().getResourcesForApplication(
                    si.applicationInfo);
            
            AttributeSet attrs = Xml.asAttributeSet(parser);
            
            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            
            String nodeName = parser.getName();
            if (!"recognition-service".equals(nodeName)) {
                throw new XmlPullParserException(
                        "Meta-data does not start with recognition-service tag");
            }
            
            TypedArray array = res.obtainAttributes(attrs,
                    com.android.internal.R.styleable.RecognitionService);
            settingsActivity = array.getString(
                    com.android.internal.R.styleable.RecognitionService_settingsActivity);
            array.recycle();
        } catch (XmlPullParserException e) {
            Log.e(TAG, "error parsing recognition service meta-data", e);
        } catch (IOException e) {
            Log.e(TAG, "error parsing recognition service meta-data", e);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "error parsing recognition service meta-data", e);
        } finally {
            if (parser != null) parser.close();
        }
        
        if (settingsActivity == null) {
            // No settings preference available - hide the preference.
            Log.w(TAG, "no recognizer settings available for " + si.packageName);
            mSettingsPref.setIntent(null);
            mParent.removePreference(mSettingsPref);
        } else {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName(si.packageName, settingsActivity));
            mSettingsPref.setIntent(i);
            mRecognizerPref.setSummary(currentRecognizer.loadLabel(getPackageManager()));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecognizerPref) {
            String setting = (String) newValue;
            
            // Put the new value back into secure settings.
            Settings.Secure.putString(
                    getContentResolver(),
                    Settings.Secure.VOICE_RECOGNITION_SERVICE,
                    setting);
            
            // Update the settings item so it points to the right settings.
            updateSettingsLink(setting);
        }
        return true;
    }
    // End
}
