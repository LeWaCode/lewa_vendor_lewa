package com.lewa.launcher.version;

import java.util.Date;

import com.lewa.launcher.ApplicationsAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("test","---==="+new Date());
		if(ApplicationsAdapter.mLauncher!=null){
			new UpdateReminder(ApplicationsAdapter.mLauncher).runAutoUpdate();
		}
	}

}
