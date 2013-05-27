package com.lewa.PIM.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.IM.IMClient;
import com.lewa.PIM.mms.ui.MessageUtils;

public class PimStart extends BroadcastReceiver {
    //private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    private static boolean sHasStart = false;

    public static void start(Context context) {
        if (!sHasStart) {
            Intent serviceIntent = new Intent(context, PimService.class);
            context.startService(serviceIntent);
            sHasStart = true;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent serviceIntent = new Intent(context, PimService.class);
            context.startService(serviceIntent);
            sHasStart = true;
	     if(MessageUtils.isImsSwitch(context)) { 
		    IMClient.StopIMService(context, IMMessage.GeXinIM);
	     	}
        }
    }
}