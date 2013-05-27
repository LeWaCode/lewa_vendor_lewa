package com.lewa.PIM.IM.receiver;

import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.content.SharedPreferences;
import com.lewa.PIM.mms.ui.MessagingPreferenceActivity;

import android.util.Log;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.IM.service.GeXinIMService;
import com.lewa.PIM.IM.service.IMService;

import im.gexin.talk.data.Const;
import im.gexin.talk.util.UiUtils;
import android.util.Log;

/**
 * Handle incoming GeXinIM Command. Just dispatches the work off to a Service.
 */
public class GeXinIMReceiver extends IMReceiver {
	private static GeXinIMReceiver sInstance;
	private static int mIMtype = IMService.GeXinIM;
	private static String TAG = "GexinReceiver";

	public static GeXinIMReceiver getInstance() {
		if (sInstance == null) {
			sInstance = new GeXinIMReceiver();
		}
		return sInstance;
	}

	protected void onReceiveWithPrivilege(Context context, Intent intent,
			boolean privileged) {
		 Bundle getBundle;
		 
		 SharedPreferences sp = context.getSharedPreferences(MessagingPreferenceActivity.IMS_CLOSE_STATE, Context.MODE_WORLD_READABLE);
		 String settingRemind = sp.getString(MessagingPreferenceActivity.IMS_CLOSE_STATE, "false");   
		  if (settingRemind.equals("false") && !intent.getAction().equals(IMService.ACTION_STOP_SERVICE)) {
		  	Log.d(TAG, "IM HAS CLOSE");	
		  	return;
		  }
		//Log.d(TAG, "receive Broadcast = " + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {			     
			Intent requireIntent = new Intent(IMService.ACTION_START_SERVICE);
			intent.setClass(context, GeXinIMService.class);
			beginStartingService(context, intent);
			Log.d(TAG, "BOOT_COMPLETED:,SERVERACTION_ACTIVITY_START");							        		
		} else if (intent.getAction().equals(Const.SERVER_ACTION_UPDATEUI)) {
			ArrayList<Bundle> bundleList = intent
			.getParcelableArrayListExtra("bundle");
			for (Bundle bundle : bundleList) {
				int cmd = bundle.getInt(Const.CMD, -2);
				switch (cmd) {
				case Const.UPDATEUI_ReceiveNewMsg:
					Intent notifyIntent = new Intent("com.lewa.PIM.IM.MESSAGE_RECEIVED");
					context.sendOrderedBroadcast(notifyIntent, null);
				default:
					break;
				}
			}
			intent.setClass(context, GeXinIMService.class);
			beginStartingService(context, intent);
		} else {
			if ((getBundle = intent.getExtras()) == null) {
				return;
			}
			int IMtype = getBundle
					.getInt("com.lewa.PIM.IM.content.EXTRA_IM_ID");
			if (IMtype == mIMtype) {
				intent.setClass(context, GeXinIMService.class);
				beginStartingService(context, intent);
				abortBroadcast();
			}
		}
	}

}
