package com.android.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import java.io.InputStream;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.SparseArray;
import android.app.ActivityManager;
import android.app.NotificationManager;

import com.android.settings.R;

public class NotificationFirewall {

	public static final String NOTIFY_FIREWALL_PREFS = "NotifyBlackListPrefs";
	public static final String BLACK_LIST = "NotifyBlackList";

	private static NotificationFirewall sInstance;

	private static String TAG = "NotificationFirewall";
	private static boolean DBG = true;
	private SharedPreferences mSharedpreferences;
	private Context mContext = null;
	private NotificationManager mNotificationManager;
	private List<String> mBlackList = new ArrayList<String>();

	public static NotificationFirewall getInstance(Context context) {
		if (null == sInstance) {
			sInstance = new NotificationFirewall(context);
		}
		return sInstance;
	}

	public void applyRules() {
		// if(mBlackList.size() > 0) {
		mNotificationManager.addBlackList((String[]) mBlackList
				.toArray(new String[mBlackList.size()]));
		// }
	}

	public void saveRules() {
		StringBuilder blacklistStr = new StringBuilder();
		int i = 0;
		int j = mBlackList.size();
		while (i < j) {
			blacklistStr.append('|');
			blacklistStr.append(mBlackList.get(i));
			i++;
		}
		android.content.SharedPreferences.Editor editor = mSharedpreferences
				.edit();
		log(blacklistStr.toString());
		editor.putString(BLACK_LIST, blacklistStr.toString());
		editor.commit();

		return;
	}

	public void addBlockPackage(String packageName) {
		if (packageName.equals("com.android.phone")) {
			return;
		}
		if (mBlackList.contains(packageName)) {
			return;
		} else {
			mBlackList.add(packageName);
			applyRules();
			saveRules();
		}
		return;
	}

	public void removeBlockPackage(String packageName) {
		if (!mBlackList.contains(packageName)) {
			return;
		} else {
			mBlackList.remove(packageName);
			applyRules();
			saveRules();
		}
		return;
	}

	public void removeAllBlockPackage() {
		mBlackList.clear();
		applyRules();
		saveRules();
		return;
	}

	public void applySavedRules() {
		loadRules();
		applyRules();
		return;
	}

	public List<String> loadRules() {
		String notifyBlackList = mSharedpreferences.getString(BLACK_LIST, "");
		String notifyForbiddenPkg[] = parseUidsString(notifyBlackList);
		mBlackList.clear();
		mBlackList.addAll(Arrays.asList(notifyForbiddenPkg));
		return mBlackList;
	}

	public NotificationFirewall(Context context) {
		mContext = context;
		mSharedpreferences = mContext.getSharedPreferences(
				NOTIFY_FIREWALL_PREFS, Context.MODE_PRIVATE);
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		applySavedRules();
	}

	public String[] parseUidsString(String s) {
		String ai[] = new String[0];
		if (s.length() > 0) {
			StringTokenizer stringtokenizer = new StringTokenizer(s, "|");
			ai = new String[stringtokenizer.countTokens()];
			int i = 0;
			do {
				int j = ai.length;
				if (i >= j)
					break;
				String s1 = stringtokenizer.nextToken();
				if (!s1.equals(""))
					try {
						ai[i] = s1;
					} catch (Exception exception) {
						ai[i] = "";
					}
				i++;
			} while (true);
		}
		return ai;
	}

	private static void log(String msg) {
		if (DBG) {
			Log.e(TAG, msg);
		}
	}
}