package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class IconCheckBoxPreference extends CheckBoxPreference {
    private Drawable mIcon;
    
    public IconCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public IconCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setLayoutResource(R.layout.icon_checkbox_preference);
        
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.IconCheckboxPreference, defStyle, 0); 
        mIcon = array.getDrawable(R.styleable.IconCheckboxPreference_cicon);
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);
        
        final ImageView imageView = (ImageView)view.findViewById(R.id.icon);
        if ((imageView != null) && (mIcon != null)) {
            imageView.setImageDrawable(mIcon);
        }
    }
}