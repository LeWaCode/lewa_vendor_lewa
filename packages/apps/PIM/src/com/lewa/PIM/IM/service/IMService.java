package com.lewa.PIM.IM.service;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;


import android.text.TextUtils;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import com.android.internal.telephony.TelephonyIntents;
import com.lewa.PIM.IM.event.EventManager;
import com.lewa.PIM.IM.provider.MmsPartProvider;
import com.lewa.PIM.IM.receiver.IMReceiver;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.mms.util.Recycler;
import com.lewa.PIM.mms.transaction.SmsReceiverService;
import com.lewa.PIM.mms.transaction.SmsReceiver;

/**
 * This service essentially plays the role of a "worker thread", allowing us to
 * store incoming messages to the database, update notifications, etc. without
 * blocking the main thread that SmsReceiver runs on.
 */
public abstract class IMService extends Service {

	private ServiceHandler mServiceHandler;
	private Looper mServiceLooper;
	private boolean mSending;

	private IMReceiver receiverInstance;
	protected IMService serviceInstance;
	protected final String TAG = "IMService";

	protected static Queue<Uri> smsqueue = new LinkedList<Uri>();

	// Indicates next message can be picked up and sent out.
	public static final String EXTRA_MESSAGE_SENT_SEND_NEXT = "SendNextMsg";

	public static final String ACTION_SEND_MESSAGE = "com.lewa.PIM.IM.SEND_MESSAGE";
	public static final String ACTION_USER_LOGIN = "com.lewa.PIM.IM.USER_LOGIN";
	public static final String ACTION_USER_LOGOUT = "com.lewa.PIM.IM.USER_LOGOUT";
	public static final String ACTION_CHECK_USER_REGISTER = "com.lewa.PIM.IM.CHECK_USER_REGISTER";
	public static final String ACTION_CHECK_USER_ONLINE_STATUS = "com.lewa.PIM.IM.CHECK_USER_ONLINE_STATUS";
	public static final String ACTION_START_SERVICE="com.lewa.PIM.IM.START_SERVICE";
	public static final String ACTION_STOP_SERVICE="com.lewa.PIM.IM.STOP_SERVICE";

	public static final int GeXinIM = 0;
	public static final int EcpIM = 1;
	public static final int MaxIMtype = EcpIM + 1;

	public static final int IM_REQUEST_RESULT_OK = 0;
	public static final int IM_REQUEST_RESULT_FAILED = 1;
	
	public static final int IM_SEND_MESSAGE_OK = 0;
	public static final int IM_SEND_MESSAGE_FAILED = 1;

	public static final int[] REQUEST_USER_LOGIN = new int[MaxIMtype];
	public static final int[] REQUEST_USER_LOGOUT = new int[MaxIMtype];
	public static final int[] REQUEST_CHECK_USER_REGISTER = new int[MaxIMtype];
	public static final int[] REQUEST_CHECK_USER_ONLINE_STATUS = new int[MaxIMtype];

	static {
		for (int i = 0; i < MaxIMtype; i++) {
			REQUEST_USER_LOGIN[i] = EventManager.inst().registerEventType(
					ACTION_USER_LOGIN + GetIMtypeName(i));
			REQUEST_USER_LOGOUT[i] = EventManager.inst().registerEventType(
					ACTION_USER_LOGOUT + GetIMtypeName(i));
			REQUEST_CHECK_USER_REGISTER[i] = EventManager.inst()
					.registerEventType(
							ACTION_CHECK_USER_REGISTER + GetIMtypeName(i));
			REQUEST_CHECK_USER_ONLINE_STATUS[i] = EventManager.inst()
					.registerEventType(
							ACTION_CHECK_USER_ONLINE_STATUS + GetIMtypeName(i));
		}
	}

	public static final String GetIMtypeName(int Imtype) {
		if (Imtype == GeXinIM) {
			return "_GeXin";
		} else if (Imtype == EcpIM) {
			return "_Ecp";
		} else {
			return "unknown";
		}
	}

	public Handler mToastHandler = new Handler();
	protected final IBinder mBinder = new LocalBinder();

	private int mResultCode;

	@Override
	public void onCreate() {
		// Temporarily removed for this duplicate message track down.
		// if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
		// Log.v(TAG, "onCreate");
		// }

		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.
		HandlerThread thread = new HandlerThread(TAG,
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		serviceInstance = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Temporarily removed for this duplicate message track down.
		// if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
		// Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras());
		// }
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		// Temporarily removed for this duplicate message track down.
		// if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
		Log.v(TAG, "onDestroy");
		// }
		mServiceLooper.quit();
	}

	public class LocalBinder extends Binder {
		public IMService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return IMService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		/**
		 * Handle incoming transaction requests. The incoming requests are
		 * initiated by the MMSC Server or by the MMS Client itself.
		 */
		@Override
		public void handleMessage(Message msg) {
			int serviceId = msg.arg1;
			ProcessMessage(msg);
			// NOTE: We MUST not call stopSelf() directly, since we need to
			// make sure the wake lock acquired by AlertReceiver is released.
			IMReceiver.finishStartingService(serviceInstance, serviceId);
		}
	}

	protected abstract void ProcessMessage(Message msg);

	public abstract boolean getLoginStatus();

	protected abstract boolean UserLogin();

	protected abstract boolean UserLogout();

	private void registerForServiceStateChanges() {
		Context context = getApplicationContext();
		unRegisterForServiceStateChanges();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
		Log.v(TAG, "registerForServiceStateChanges");
		context.registerReceiver(receiverInstance, intentFilter);
	}

	private void unRegisterForServiceStateChanges() {

		Log.v(TAG, "unRegisterForServiceStateChanges");
		try {
			Context context = getApplicationContext();
			context.unregisterReceiver(receiverInstance);
		} catch (IllegalArgumentException e) {
			// Allow un-matched register-unregister calls
		}
	}

	protected boolean checkNetworkConnect() {
		ConnectivityManager conManager = 
					(ConnectivityManager)serviceInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);		
		NetworkInfo wifInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);		
		if(mobInfo != null && wifInfo != null) {
			boolean mobState = mobInfo.isConnected();
			boolean wifiState = wifInfo.isConnected();
			return (mobState || wifiState);
		}
		else {
			return false;
		}
	}
	
	protected void resendMessageBySms(Bundle failedMessage) {

		int msgID = failedMessage.getInt("msgID");
		int mID = 0;
		String mDest = null;
		String mMessageText = null;
		long mThreadId = 0;
		Cursor cursor = null;
		Context context = getApplicationContext();
		Log.d(TAG, "Need send through sms again msgID =" + msgID);

		try {
			cursor = SqliteWrapper.query(context,
					context.getContentResolver(),
					MessageUtils.PduYlColumns.IMS_URI, new String[] {
							MessageUtils.PduYlColumns.ADDRESS,
							MessageUtils.PduYlColumns.BODY,
							MessageUtils.PduYlColumns.THREAD_ID,
							MessageUtils.PduYlColumns.ID}, "m_id = "
							+ msgID, null, "date DESC");

			if ((cursor == null) || !cursor.moveToFirst()) {
				return;
			} else {
				mDest = new String(cursor.getString(0));
				mMessageText = new String(cursor.getString(1));
				mThreadId = cursor.getLong(2);
				mID = cursor.getInt(3);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		try {		
			Sms.addMessageToUri(context.getContentResolver(),
					Uri.parse("content://sms/queued"), mDest, mMessageText,
					null, null, true /* read */, true, mThreadId);
		} catch (SQLiteException e) {
			SqliteWrapper.checkSQLiteException(context, e);
		}
		asyncDeleteFailedIMMessage(mID);
		
        // Notify the SmsReceiverService to send the message out
		context.sendBroadcast(new Intent(SmsReceiverService.ACTION_SEND_MESSAGE,
                null,
                context,
                SmsReceiver.class));
	}

	protected void handleReceivedMessage(Bundle recevieMessage) {

		Uri messageUri = storeMessage(recevieMessage);
		if (messageUri != null) {
			// Called off of the UI thread so ok to block.
			MessagingNotification.blockingUpdateNewMessageIndicator(this, true,
					false);
		}
	}

	protected void handleReceivedMmsMessage(Bundle recevieMessage) {

		Uri messageUri = storeMmsMessage(recevieMessage);
		if (messageUri != null) {
			// Called off of the UI thread so ok to block.
			MessagingNotification.blockingUpdateNewMessageIndicator(this, true,
					false);
		}
	}

	private Uri storeMessage(Bundle bundle) {

		// Store the message in the content provider.
		ContentValues values = new ContentValues();

		values.put(Sms.ERROR_CODE, 0);
		values.put(Inbox.BODY, bundle.getString("content"));
		values.put(Sms.ADDRESS, bundle.getString("phone"));

		// Make sure we've got a thread id so after the insert we'll be able to
		// delete
		// excess messages.
		Long threadId = (long) 0;
		String address = values.getAsString(Sms.ADDRESS);
		if (!TextUtils.isEmpty(address)) {
			Contact cacheContact = Contact.get(address, true);
			if (cacheContact != null) {
				address = cacheContact.getNumber();
			}
		}

		threadId = Threads.getOrCreateThreadId(this, address);
		values.put(Sms.THREAD_ID, threadId);

		ContentResolver resolver = this.getContentResolver();

		Uri insertedUri = SqliteWrapper.insert(this, resolver,
				MessageUtils.PduYlColumns.IMS_INBOX_URI, values);

		// Now make sure we're not over the limit in stored messages
		Recycler.getSmsRecycler().deleteOldMessagesByThreadId(
				getApplicationContext(), threadId);

		return insertedUri;
	}

	private Uri storeMmsMessage(Bundle bundle) {

		// Store the message in the content provider.
		ContentValues values = new ContentValues();
		Log.d(TAG, "receive Mms url = " + bundle.getStringArray("uri")[0]);
		values.put(MessageUtils.PduYlColumns.M_ID, bundle.getLong("mmsID"));
		values.put(Sms.ADDRESS, bundle.getString("phone"));
		values.put(MessageUtils.PduYlColumns.PATH,
				MmsPartProvider.getMmsFileDir());
		values.put(MessageUtils.PduYlColumns.FILE_NAME,
				Uri.parse(bundle.getStringArray("uri")[0]).getPathSegments()
						.get(0));
		values.put(MessageUtils.PduYlColumns.M_DATA_TYPE,
				bundle.getStringArray("mime")[0]);

		// Make sure we've got a thread id so after the insert we'll be able to
		// delete
		// excess messages.
		Long threadId = (long) 0;
		String address = values.getAsString(Sms.ADDRESS);
		if (!TextUtils.isEmpty(address)) {
			Contact cacheContact = Contact.get(address, true);
			if (cacheContact != null) {
				address = cacheContact.getNumber();
			}
		}

		threadId = Threads.getOrCreateThreadId(this, address);
		values.put(Sms.THREAD_ID, threadId);

		ContentResolver resolver = this.getContentResolver();

		Uri insertedUri = SqliteWrapper.insert(this, resolver,
				MessageUtils.PduYlColumns.IMS_INBOX_URI, values);

		// Now make sure we're not over the limit in stored messages
		Recycler.getSmsRecycler().deleteOldMessagesByThreadId(
				getApplicationContext(), threadId);

		return insertedUri;
	}

	private void asyncDelete(final Uri uri, final String selection,
			final String[] selectionArgs) {
		Log.d(TAG,"asyncDelete" + uri + " where" + selection);		
		new Thread(new Runnable() {
			public void run() {
				Context context = getApplicationContext();
				SqliteWrapper.delete(context, context.getContentResolver(), uri,
						selection, selectionArgs);
			}
		}).start();
	}

	private void asyncDeleteFailedIMMessage(int IMId) {
		//final String where = MessageUtils.PduYlColumns.M_ID + " = " + IMId;
		Uri delUri = ContentUris.withAppendedId(MessageUtils.PduYlColumns.IMS_URI, IMId);
		asyncDelete(delUri, null, null);
	}

}
