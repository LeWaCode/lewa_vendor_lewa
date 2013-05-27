package com.lewa.face.widget;

/*
 * Copyright (C) 2009 The Android Open Source Project
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


import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class FontsPreferenceScreen extends Preference {

    private Drawable mThumbnail;
    private Drawable mStatusFlag;
    private ThemeBase mThemeBase;

    public FontsPreferenceScreen(Context context){
        this(context, null);
    }
    
    public FontsPreferenceScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontsPreferenceScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setLayoutResource(R.layout.preference_fonts);
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FontsPreferenceScreen, defStyle, 0);
        
        mThumbnail = a.getDrawable(R.styleable.FontsPreferenceScreen_thumbnail);
        mStatusFlag = a.getDrawable(R.styleable.FontsPreferenceScreen_flag);
        
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        
        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        if (thumbnail != null && mThumbnail != null) {
        	thumbnail.setImageDrawable(mThumbnail);
        }
        
        ImageView statusFlag = (ImageView) view.findViewById(R.id.status_flag);
        if(statusFlag != null && mStatusFlag != null){
        	statusFlag.setImageDrawable(mStatusFlag);
        }
    }
    
	public Drawable getmThumbnail() {
		return mThumbnail;
	}

	public void setmThumbnail(Drawable mThumbnail) {
		this.mThumbnail = mThumbnail;
	}

	public Drawable getmStatusFlag() {
		return mStatusFlag;
	}

	public void setmStatusFlag(Drawable mStatusFlag) {
		this.mStatusFlag = mStatusFlag;
	}

	public ThemeBase getmThemeBase() {
		return mThemeBase;
	}

	public void setmThemeBase(ThemeBase mThemeBase) {
		this.mThemeBase = mThemeBase;
	}
    
    
}
