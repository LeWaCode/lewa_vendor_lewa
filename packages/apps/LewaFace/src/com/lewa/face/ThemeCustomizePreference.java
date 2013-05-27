package com.lewa.face;

import com.lewa.face.app.ThemeApplication;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ThemeCustomizePreference extends PreferenceActivity{

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        ThemeApplication.activities.add(this);
        
        addPreferencesFromResource(R.xml.customize);

    }



}
