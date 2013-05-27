package com.lewa.store.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LewaPushMessageReceiver extends BroadcastReceiver {

	public static final String TAG = "LewaPushMessageReceiver";
	
	public static final String KEY_INTENT_ACTION = "com.lewa.pond.push";
	
	public static final String INTENT_EXTRAS_KEY_APPLICATION_ID = "lewaApplicationId";
	public static final String INTENT_EXTRAS_KEY_APPLICATION_MESSAGE = "lewaApplicationMessage";
	public static final String INTENT_EXTRAS_KEY_APPLICATION_MESSAGE_TITLE = "lewaApplicationMessageTitle";
	
	public LewaPushMessageReceiverInterface consumer;
	
	public LewaPushMessageReceiver(LewaPushMessageReceiverInterface consumer) {
		this.consumer = consumer;
	}
	
	public interface LewaPushMessageReceiverInterface {
		public void onMessageReceive(Intent intent);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String applicationId = intent.getStringExtra(INTENT_EXTRAS_KEY_APPLICATION_ID);
		if (applicationId.equals(context.getPackageName())) {
			Log.d(TAG, "got broadcast with message: " + intent.getStringExtra(INTENT_EXTRAS_KEY_APPLICATION_MESSAGE));
			this.consumer.onMessageReceive(intent);
		}
	}
}
