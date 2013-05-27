package com.lewa.spm.element;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Map;

import com.lewa.spm.R;
import com.lewa.spm.utils.PrefUtils;

public class EnergyPreference extends SeekBarPreference implements SeekBar.OnSeekBarChangeListener {

	private final String TAG = "EnergyPreference";
	
	Context mContext;
	
    private SeekBar mSeekBar;
    private TextView mTextView;
    
    private int mOldEnergy = 0;
    
    private final int MINIMUM_ENERGY = 0;
    private final int MAXIMUM_ENERGY = 100;
    
    private final String FINAL_STRING = "ABC";

    public EnergyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setDialogLayoutResource(R.layout.pref_dialog_enery);
        setDialogIcon(R.drawable.spm_energy_title_icon);
                        
        mOldEnergy = PrefUtils.getInstance(getContext()).getIntelLowPower();
    }

    @Override
    protected void onBindDialogView(View view) {

    	mOldEnergy = PrefUtils.getInstance(mContext).getIntelLowPower();
    	
        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(MAXIMUM_ENERGY - MINIMUM_ENERGY);        
        mSeekBar.setProgress(mOldEnergy);
        
        mTextView = (TextView) view.findViewById(R.id.tv_seekbar_value);
        setEnergy(mOldEnergy);
        mSeekBar.setOnSeekBarChangeListener(this);
        
        super.onBindDialogView(view);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        setEnergy(progress);
    }
    
    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);        
        if (positiveResult) {
        	PrefUtils.getInstance(mContext).setIntelLowPower(mSeekBar.getProgress());
        }
    }
 
    private void setEnergy(int mEnergy){    	
    	mTextView.setText(
    			mContext.getString(R.string.spm_energy_prefix)
    			+String.valueOf(mEnergy)
    			+mContext.getString(R.string.spm_energy_postfix));    	
    }
}

