package com.lewa.store.push;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lewa.core.base.LewaComponentCache;
import com.lewa.core.base.LewaContentDAO;
import com.lewa.core.base.StorePackage;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 
 * Usage:
 * 		
 * 		class ExampleClass implements LewaPushMessageInterface.LewaPushMessageSubscriberInterface {
 * 
 * 		...
 * 		
 *		this.pushMessageInterface = new LewaPushMessageInterface(context);
 * 		this.pushMessageInterface.setSubscriber(this);
 * 
 * 		// for package updates, callback "onPackagePushMessageReceive" will be invoked
 *		// for general messages, callback "onGeneralPushMessageReceive" will be called (currently not in use)
 * 
 * 		...
 * 
 * @author vchoy
 *
 */
public class LewaPushMessageInterface implements LewaPushMessageReceiver.LewaPushMessageReceiverInterface {

	public static final String TAG = "LewaPushMessageInterface";
	
	public static final String TASK_UPDATES = "updates";
	
	//																updates[11,2,3] || <some message>
	public static final String REGEX_STORE_PUSH_MESSAGE_FORMAT = "^([\\w\\-]+)+\\[([^\\]]+)+\\]$";
	
	public Context context;
	public LewaPushMessageSubscriberInterface subscriber;
	public LewaPushMessageReceiver receiver;
	private LewaContentDAO dao;
	
	public LewaPushMessageInterface(Context context) {
		this.context = context;
		this.receiver = new LewaPushMessageReceiver(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(LewaPushMessageReceiver.KEY_INTENT_ACTION);
		context.registerReceiver(this.receiver, filter);
		LewaComponentCache cache = LewaComponentCache.getInstance(context);
		this.dao = (LewaContentDAO) cache.getComponent(LewaComponentCache.COMPONENT_DAO);
	}
	
	public interface LewaPushMessageSubscriberInterface {
		public void onPackagePushMessageReceive(StorePackage[] packages);
		public void onGeneralPushMessageReceive(String pushMessage);
	}
	
	public void setSubscriber(LewaPushMessageSubscriberInterface subscriber) {
		this.subscriber = subscriber;
	}

	@Override
	public void onMessageReceive(Intent intent) {
		String message = intent.getStringExtra(LewaPushMessageReceiver.INTENT_EXTRAS_KEY_APPLICATION_MESSAGE);
		Log.d(TAG, "got message: " + message);
		if (this.subscriber == null) {
			Log.w(TAG, "push subscriber not set! burying message: " + message);
			return;
		}
		if (message == null || message.equals("")) {
			Log.w(TAG, "empty message");
			return;
		}
		Pattern pattern = Pattern.compile(REGEX_STORE_PUSH_MESSAGE_FORMAT);
		Matcher matcher = pattern.matcher(message);
		boolean hasMatches = matcher.matches();
		String task = "";
		String meta = "";
		Log.d(TAG, String.format("number of groups: %d", matcher.groupCount()));
		if (hasMatches && matcher.groupCount() == 2) {
			task = matcher.group(1);
			meta = matcher.group(2);
			Log.d(TAG, String.format("task: %s, meta: %s", task, meta));
		}
		if (task.equals(TASK_UPDATES) && !meta.equals("")) {
			StorePackage[] packages = null;
			String[] packageIds = meta.split(",");
			packages = new StorePackage[packageIds.length];
			int length=packages.length;
			for (int i = 0; i < length; i++) {
				StorePackage pack = new StorePackage();
				pack.pkg = packageIds[i];
				packages[i] = pack;
			}
			this.subscriber.onPackagePushMessageReceive(packages);
		} else {
			this.subscriber.onGeneralPushMessageReceive(message);
		}
	}
}
