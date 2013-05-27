package com.lewa.pond.account;

import java.util.Iterator;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * 账户服务
 * @author ypzhu
 * @author richfuns@gmial.com
 *
 */
public class LewaAccountsService extends Service {
	
	public static final String TAG = "LewaAccountsService";
	public static final String ACCOUNT_KEY = "account";
	public static final String ACCOUNT_TYPE = "com.lewa.pond";
	private LewaAccountAuthenticator saa;

	@Override
	public IBinder onBind(Intent intent) {
		String action = intent.getAction();
		Bundle extras = intent.getExtras();
		Log.d(TAG, "action: " + action);
		if (extras != null) {
			Iterator<String> iter = extras.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Log.d(TAG, String.format("key: %s => val: %s", key, extras.get(key).toString()));
			}
		}
		IBinder ret = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			ret = getSleepyAuthenticator().getIBinder();
		}
		return ret;
	}
	
	private LewaAccountAuthenticator getSleepyAuthenticator() {
		if (saa == null) {
			saa = new LewaAccountAuthenticator(this);			
		}
		return saa;
	}
}
