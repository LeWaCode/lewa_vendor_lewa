package com.lewa.PIM.service;

import com.lewa.PIM.R;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.mms.MsgPopup.DestktopMessageActivity;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.mms.transaction.MessagingNotification;
	
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RosterData;
import android.provider.Telephony.Sms;
import android.util.Log;

public class PimService extends Service {
    private static String TAG = "PimService";
    
    private final IBinder mBinder;
    private Handler mHandler;
    private CallLogObserver mCallLogObserver;
    private ContactObserver mContactObserver;
    private GroupObserver   mGroupObserver;
    private SmsObserver     mSmsObserver;
    private RosterDataObserver mRosterDataObserver;
    
    private Context         mContext;
    
    public PimService() {
        mBinder = new LocalBinder();
        mHandler = new Handler();
        mCallLogObserver = new CallLogObserver(mHandler);
        mContactObserver = new ContactObserver(mHandler);
        mGroupObserver   = new GroupObserver(mHandler);
        mSmsObserver 	 = new SmsObserver(mHandler);
        mRosterDataObserver = new RosterDataObserver(mHandler);
        
        mContext = this;
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    public void onCreate() {
        Log.d(TAG, "onCreate");
        
        PimEngine pimEngine = PimEngine.getInstance(this);
        pimEngine.loadCallLogs(false);
        pimEngine.loadDialpadItems(false);
    
        ContentResolver contentResolver = getContentResolver();
        contentResolver.registerContentObserver(CallLog.CONTENT_URI, true, mCallLogObserver);
        contentResolver.registerContentObserver(ContactsContract.Data.CONTENT_URI, true, mContactObserver);
        contentResolver.registerContentObserver(RosterData.CONTENT_URI, true, mRosterDataObserver);        
        contentResolver.registerContentObserver(ContactsContract.Groups.CONTENT_URI, true, mGroupObserver);
        contentResolver.registerContentObserver(Sms.CONTENT_URI, true, mSmsObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    	return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        ContentResolver contentResolver = getContentResolver();
        contentResolver.unregisterContentObserver(mCallLogObserver);
        contentResolver.unregisterContentObserver(mContactObserver);
        contentResolver.unregisterContentObserver(mGroupObserver);
        contentResolver.unregisterContentObserver(mSmsObserver);
        contentResolver.unregisterContentObserver(mRosterDataObserver);
    }

    class CallLogObserver extends ContentObserver {
        public CallLogObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            PimEngine.getInstance(PimService.this).loadCallLogs(true);
        }
    }

    class ContactObserver extends ContentObserver {
        public ContactObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            PimEngine pimEng = PimEngine.getInstance(PimService.this);
            pimEng.reloadCallLogsAndContacts();
            pimEng.notifyDataEvent(PimEngine.DataEvent.CONTACTS_CHANGED, 0);
        }
    }

    class GroupObserver extends ContentObserver {
        public GroupObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            
        }
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public PimService getService() {
            return PimService.this;
        }
    }
    
    public class SmsObserver extends ContentObserver{

		public SmsObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}
		
        public void onChange(boolean selfChange) {
        	String[] projection = new String[] { "_id", "person", "address","read", "body", "date" };
        	
        	try {
            	Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), 
						projection, 
						"read = 0 and seen = 0", 
						null, 
						"date desc");
            	
            	int count = cursor.getCount();            	
            	cursor.close();
            	
            	if (count > 0) {
                    SharedPreferences sp = getSharedPreferences("smsdialogvalue", Context.MODE_WORLD_READABLE);
                    String settingRemind = sp.getString("smsdialogvalue", "true");
                    
                    MessageUtils.AcquireWakeLock(mContext, 10000);  
                    
                    if (settingRemind.equals("false") || CommonMethod.isStartMsgPop() || MessageUtils.phoneIsInUse()){
                    	  
                    }
                    else{ 
                    	DestktopMessageActivity.setActionString("android.provider.Telephony.SMS_RECEIVED");
                    	
                        if (DestktopMessageActivity.getMessagePOPStart() == false) {                            
                            Intent i = new Intent(mContext, DestktopMessageActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(i);  
                            DestktopMessageActivity.setMessagePOPStart(true);
                        }else {
                            Intent intent = new Intent();
                            intent.setAction("com.lewa.PIM.mms.MsgPopup.POPUP_Receive");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.sendBroadcast(intent);
                        }
                    }
					//add by shenqi for notify  
					MessagingNotification.blockingUpdateNewMessageIndicator(mContext, true, false);
				}
			} catch (Exception e) {
				Log.e(TAG, "" + e.getMessage());				
			}
        }
    	
    }
    
    class RosterDataObserver extends ContentObserver {
        public RosterDataObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            PimEngine pimEng = PimEngine.getInstance(PimService.this);
            pimEng.notifyDataEvent(PimEngine.DataEvent.ROSTERDATA_CHANGED, 0);
        }
    }
}