package com.lewa.intercept.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lewa.intercept.R;

public class CheckBoxPreference extends LinearLayout {
    public CheckBox mCheckBox;
    public TextView titleText;
    public TextView summaryText;

    public CheckBoxPreference(Context context) {
        super(context);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreference);
        View view = View.inflate(context, R.layout.preference_checkbox, this);
        mCheckBox = (CheckBox) findViewById(R.id.preference_child_checkbox);
        titleText = (TextView) findViewById(R.id.preference_child_title);
        summaryText = (TextView) findViewById(R.id.preference_child_summary);
        if (!typeArray.getBoolean(R.styleable.CheckBoxPreference_show_summary, false)) {
            summaryText.setVisibility(View.VISIBLE);
        }
        if (!typeArray.getBoolean(R.styleable.CheckBoxPreference_enable_checkbox, false)) {
            mCheckBox.setVisibility(View.INVISIBLE);
        }
        Drawable buttonDrawable = typeArray.getDrawable(R.styleable.CheckBoxPreference_button);
        if (buttonDrawable != null) {
            mCheckBox.setButtonDrawable(buttonDrawable);
        }
        boolean checked = typeArray.getBoolean(R.styleable.CheckBoxPreference_checked, false);

        setChecked(checked);

        String title = typeArray.getString(R.styleable.CheckBoxPreference_title);
        int titleSize = typeArray.getDimensionPixelSize(R.styleable.CheckBoxPreference_textSize, 0);
        String summary = typeArray.getString(R.styleable.CheckBoxPreference_summary);

        if (title != null) {
            titleText.setText(title);
        }
        if (summary != null) {
            summaryText.setText(summary);
        }
        typeArray.recycle();
    }

    public void setChecked(boolean checked) {
        mCheckBox.setChecked(checked);
    }

    public void setSummary(int paramInt) {
        summaryText.setText(paramInt);
    }

    public void setSummary(String paramString) {
        summaryText.setText(paramString);
    }

    public void setSummary(String paramString, int paramInt) {
        summaryText.setText(paramString);
        summaryText.setTextColor(paramInt);
    }

    public void setTitle(String paramString) {
        titleText.setText(paramString);
    }
}
