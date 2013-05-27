package com.lewa.store.nav;

import java.util.ArrayList;
import java.util.List;

import com.lewa.core.base.StorePackage;
import com.lewa.store.model.LewaNotification;
import com.lewa.store.push.LewaPushMessageInterface;
import com.lewa.store.push.LewaPushMessageInterface.LewaPushMessageSubscriberInterface;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

public class ApplicationManager extends Application implements
		LewaPushMessageSubscriberInterface {

	private String TAG = ApplicationManager.class.getSimpleName();

	private List<Activity> mainActivity = null;
	private LewaPushMessageInterface push = null;
	private LewaNotification lewaNotification = null;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Application onCreate()");
		mainActivity = new ArrayList<Activity>();
		lewaNotification = new LewaNotification(this);
		try {
			push = new LewaPushMessageInterface(this.getApplicationContext());
			push.setSubscriber(this);
		}catch(NoClassDefFoundError e){
			e.printStackTrace();
			Log.e(TAG,"LewaPushMessageInterface error");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Activity> MainActivity() {
		return mainActivity;
	}

	public void addActivity(Activity activity) {
		if (null != activity) {
			mainActivity.add(activity);
		}
	}

	public void finishAll() {
		for (Activity activity : mainActivity) {
			if (!activity.isFinishing()) {
				Log.i(TAG, "onDestroy " + activity);
				activity.finish();
			}
		}
		mainActivity = null;
	}

	@Override
	public void onPackagePushMessageReceive(StorePackage[] packages) {
		Log.d(TAG, String.format("got %d packages from push message",
				packages.length));
		for (int i = 0; i < packages.length; i++) {
			Log.d(TAG, "update for package: " + packages[i].getPackage());
		}
	}

	@Override
	public void onGeneralPushMessageReceive(String pushMessage) {
		Log.d(TAG, "general message: " + pushMessage);
		lewaNotification.notifyUpdateMessage();
	}
}
