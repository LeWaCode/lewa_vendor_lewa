package com.lewa.PIM.util;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract.RosterData;
import android.util.Log;

public class RosterResponseReceiver extends BroadcastReceiver {    
	private static final String TAG = "ResponseReceiver";
    public static final String ACTION_YILIAO_STATUS_NUMBERS_DETAIL = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE_DETAIL";
    public static final String ACTION_YILIAO_STATUS_NUMBERS_ONLINE = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE_ONLINE";    

    @Override
    public void onReceive(Context context, Intent intent) {
    	if (intent.getAction().equals(ACTION_YILIAO_STATUS_NUMBERS_DETAIL)){
            ContentResolver resolver = context.getContentResolver();
            String number = intent.getStringExtra("phone");
            boolean isOnline = intent.getBooleanExtra("online", false);
            int isOnlineInt =  isOnline ? 1 : 0;
            
            ContentValues cv = new ContentValues();
            cv.put(RosterData.STATUS, isOnline == true ? 1 : 0);
            resolver.update(RosterData.CONTENT_URI, cv, RosterData.ROSTER_USER_ID + "=" + number + " AND " + RosterData.STATUS + "!=" + isOnlineInt, null);
            Log.v(TAG, "mRosterResponseReceiver onReceive() isOnline = " + isOnline);
            
            intent.setAction(ACTION_YILIAO_STATUS_NUMBERS_ONLINE);
            context.sendBroadcast(intent);
        }
    }
}