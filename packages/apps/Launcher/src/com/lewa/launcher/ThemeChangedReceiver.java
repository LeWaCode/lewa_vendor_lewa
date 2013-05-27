package com.lewa.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

public class ThemeChangedReceiver extends BroadcastReceiver{
    
    private static final String TAG = "ThemeChangedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Launcher.themeChanged = true;
    }

}
