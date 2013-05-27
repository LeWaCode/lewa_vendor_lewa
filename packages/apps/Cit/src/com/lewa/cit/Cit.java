/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.lewa.cit;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class Cit extends  PreferenceActivity implements OnPreferenceClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cit_preference);

        List<String> tools_list = new LinkedList<String>();
        List<String> pref_key = new LinkedList<String>();
        tools_list.add("com.android.development_hhh");
        tools_list.add("com.android.spare_parts_hhh");
        pref_key.add(new String("dev_tools"));
        pref_key.add(new String("spare_parts"));
        Preference p = null;
        PackageManager pm = null;
        int i = 0;
        for(i = 0 ;i <tools_list.size() ; i++) {
            try {
              p = findPreference(pref_key.get(i));
                pm = getPackageManager();
                pm.getPackageInfo(tools_list.get(i), PackageManager.GET_ACTIVITIES);
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    ((PreferenceGroup)findPreference("cit_tools")).removePreference(p);
                    e.printStackTrace();
              }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // TODO Auto-generated method stub
        return false;
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =  packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
