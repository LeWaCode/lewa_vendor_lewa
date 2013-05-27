package com.lewa.face.util;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShutDownReceive extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(!action.equals(Intent.ACTION_SHUTDOWN)){
           return; 
        }
        ThemeUtil.unzipFontsBeforeReboot();
        
        Log.i("ShutDownReceive", "reboot now");
    }

}
