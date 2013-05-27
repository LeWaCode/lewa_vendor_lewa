package com.lewa.PIM.IM;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.lewa.PIM.IM.event.EventManager;
import com.lewa.PIM.IM.service.IMService;
import com.lewa.PIM.IM.service.GeXinIMService;
import com.lewa.PIM.IM.service.IMService.LocalBinder;

public final class IMClient {
	private static IMService mService[] = new IMService[IMService.MaxIMtype];
	private static boolean mBound[] = new boolean[IMService.MaxIMtype];
	private static ServiceConnection mConnection[] = new ServiceConnection[IMService.MaxIMtype];

	static {
		/** Defines callbacks for service binding, passed to bindService() */
		mConnection[IMService.GeXinIM] = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// We've bound to LocalService, cast the IBinder and get
				// LocalService instance
				LocalBinder binder = (LocalBinder) service;
				mService[IMService.GeXinIM] = binder.getService();
				mBound[IMService.GeXinIM] = true;
				Log.d("IMClient", "GeXin onServiceConnected ");
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				Log.d("IMClient", "GeXin onServiceDisconnected");
				mBound[IMService.GeXinIM] = false;
			}
		};
		mConnection[IMService.EcpIM] = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				// We've bound to LocalService, cast the IBinder and get
				// LocalService instance
				LocalBinder binder = (LocalBinder) service;
				mService[IMService.EcpIM] = binder.getService();
				mBound[IMService.EcpIM] = true;
				Log.d("IMClient", " Ecp onServiceConnected ");
			}

			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				Log.d("IMClient", "Ecp onServiceDisconnected");
				mBound[IMService.EcpIM] = false;
			}
		};
	}

	public static void init(Context paramContext, int ImType) {
		if (!mBound[ImType]) {
			Context appContext = paramContext.getApplicationContext();
			appContext.bindService(getSeviceIntent(paramContext, ImType),
					mConnection[ImType], Context.BIND_AUTO_CREATE);
		}
	}

	public static void free(Context paramContext, int ImType) {
		if (!mBound[ImType]) {
			Context appContext = paramContext.getApplicationContext();
			appContext.unbindService(mConnection[ImType]);
		}
	}

	public static void sendMessage(Context paramContext,
			IMMessage paramIMMessage) {
		sendMessage(paramContext, paramIMMessage, null);
	}

	public static void sendMessage(Context paramContext,
			IMMessage paramIMMessage, String paramString) {
		if (paramIMMessage == null) {
			return;
		}
		try {
			paramIMMessage.checkFile();
			paramIMMessage.CheckMessageSize();
			paramIMMessage.checkIMType();
		} catch (IOException locala) {
			locala.printStackTrace();
			return;
		}
		Intent requireIntent = new Intent(IMService.ACTION_SEND_MESSAGE);
		paramIMMessage.CombineIntent(requireIntent);
		paramContext.sendOrderedBroadcast(requireIntent, null);
	}
	
	public static void UserLogin(Context paramContext, int paramImId,
			PendingIntent logoutIntent) {
		if (EventManager.inst().getListenerPendingIntentCount(
				IMService.REQUEST_USER_LOGIN[paramImId]) == 0) {
			Intent requireIntent = new Intent(IMService.ACTION_USER_LOGIN);
			requireIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IM_ID",
					paramImId);
			paramContext.sendOrderedBroadcast(requireIntent, null);
		} else {
			EventManager.inst().listenForEvent(logoutIntent,
					IMService.REQUEST_USER_LOGIN[paramImId]);
		}
	}

	public static void UserLogout(Context paramContext, int paramImId,
			PendingIntent logoutIntent) {
		if (EventManager.inst().getListenerPendingIntentCount(
				IMService.REQUEST_USER_LOGOUT[paramImId]) == 0) {
			Intent requireIntent = new Intent(IMService.ACTION_USER_LOGOUT);
			requireIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IM_ID",
					paramImId);
			paramContext.sendOrderedBroadcast(requireIntent, null);
		} else {
			EventManager.inst().listenForEvent(logoutIntent,
					IMService.REQUEST_USER_LOGOUT[paramImId]);
		}
	}

	public static void StopIMService(Context paramContext, int paramImId) {
		Intent requireIntent = new Intent(IMService.ACTION_STOP_SERVICE);
		requireIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IM_ID",
				paramImId);
		paramContext.sendOrderedBroadcast(requireIntent, null);

	}
	
	public static void CheckUserRegister(Context paramContext, int paramImId,ArrayList<String>  StringArray,
			PendingIntent logoutIntent) {
			
		EventManager.inst().cancelEvent(IMService.REQUEST_CHECK_USER_REGISTER[paramImId]);
		EventManager.inst().listenForEvent(logoutIntent,
					IMService.REQUEST_CHECK_USER_REGISTER[paramImId]);
		Intent requireIntent = new Intent(IMService.ACTION_CHECK_USER_REGISTER);
		requireIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IM_ID",
				paramImId);
		requireIntent.putStringArrayListExtra("com.lewa.PIM.IM.content.EXTRA_STRING_ARRAY",
				StringArray);
		paramContext.sendOrderedBroadcast(requireIntent, null);
	}
	
	public static void CheckUserOnlineStatus(Context paramContext, int paramImId,String[] StringArray,
			PendingIntent userOnlineStatusIntent) {

		EventManager.inst().cancelEvent(IMService.REQUEST_CHECK_USER_ONLINE_STATUS[paramImId]);
		EventManager.inst().listenForEvent(userOnlineStatusIntent,IMService.REQUEST_CHECK_USER_ONLINE_STATUS[paramImId]);
	 
		Intent requireIntent = new Intent(IMService.ACTION_CHECK_USER_ONLINE_STATUS);
		requireIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_IM_ID",
				paramImId);
		requireIntent.putExtra("com.lewa.PIM.IM.content.EXTRA_STRING_ARRAY",
				StringArray);
		paramContext.sendOrderedBroadcast(requireIntent, null);
	}

	public static boolean getLoginStatus(Context paramContext, int ImType) {
		boolean LoginStatus = false;

		Log.d("IMClient", "mBound = " + mBound);
		if (mBound[ImType]) {
			LoginStatus = mService[ImType].getLoginStatus();
		} else {
			init(paramContext, ImType);
		}
		return LoginStatus;
	}

	private static Intent getSeviceIntent(Context paramContext, int ImType) {
		if (ImType == IMMessage.GeXinIM) {
			return new Intent(paramContext, GeXinIMService.class);
		}
		return null;
	}
}
