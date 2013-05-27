package com.lewa.spm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompeletReceiver extends BroadcastReceiver {	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i("BootCompeletReceiver", "ACTION_BOOT_COMPLETED");
			Intent mIntent = new Intent(Intent.ACTION_RUN);
			mIntent.setClass(context, MonitorService.class);
            context.startService(mIntent);
        }
	}

}