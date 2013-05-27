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

package com.lewa.spm.activity;

import android.preference.Preference;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.CheckBox;
import android.util.Log;
import android.view.View.OnClickListener;

import com.lewa.spm.R;

public class MyCheckBoxPreference extends Preference {
  
    private boolean mChecked;
    
    private AccessibilityManager mAccessibilityManager;
    
    private boolean mDisableDependentsState;
    private CheckBox mCheckBox;
    
    public MyCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
       
         setLayoutResource(R.layout.preference); 
         Log.i("lkr","MyCheckBoxPreference");
    }

    public MyCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    public MyCheckBoxPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mCheckBox = (CheckBox)view.findViewById(R.id.checkbox);
        Log.i("lkr","MyCheckBoxPreference onBindView mCheckBox="+mCheckBox);
        if(mCheckBox!=null){
            mCheckBox.setChecked(mChecked);
             mCheckBox.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        onCheckBoxClicked();
                    }
                });
        }
    }
    
    protected void onCheckBoxClicked() {
        if (isChecked()) {
            setChecked(false);
        } else {
            setChecked(true);
        }
    }
    /**
     * Sets the checked state and saves it to the {@link SharedPreferences}.
     * 
     * @param checked The checked state.
     */
    public void setChecked(boolean checked) {
        Log.i("lkr","MyCheckBoxPreference setChecked mChecked="+mChecked+" " +"checked="+checked);
        if (mChecked != checked) {
            mChecked = checked;
            callChangeListener(checked);
            persistBoolean(checked);
            notifyDependencyChange(false);
            notifyChanged();
        }
    }

    /**
     * Returns the checked state.
     * 
     * @return The checked state.
     */
    public boolean isChecked() {
        Log.i("lkr","MyCheckBoxPreference onBindView isChecked="+mChecked);
        return mChecked;
    }
}

