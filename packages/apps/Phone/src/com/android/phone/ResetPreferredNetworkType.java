package com.android.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings;
import android.util.Log;

public class ResetPreferredNetworkType extends BroadcastReceiver {
	
	private static final String TAG = "ResetNetwork";
	private static final String RESET_TD_NETWROK = "ro.product.network";
	
	private int networkType = -1;
	private Phone mPhone;
	private MyHandler mHandler;

	@Override
	public void onReceive(Context context, Intent intent) {
		if("TD".equals(SystemProperties.get(RESET_TD_NETWROK)))
			resetNetwork(context);
	}

	private void resetNetwork(Context context) {
        mPhone = PhoneFactory.getDefaultPhone();
        mHandler = new MyHandler();
		try {
			networkType = Settings.Secure.getInt(context.getContentResolver(),
					Settings.Secure.PREFERRED_NETWORK_MODE);
			if (networkType >= 0) {
				mPhone.setPreferredNetworkType(
						networkType,
						mHandler.obtainMessage(MyHandler.MESSAGE_SET_PREFERRED_NETWORK_TYPE));
			}
		} catch (SettingNotFoundException e) {

			e.printStackTrace();
		}
		Log.e(TAG, "preferredNetWork" + networkType);
	}

	private class MyHandler extends Handler {

		private static final int MESSAGE_SET_PREFERRED_NETWORK_TYPE = 1;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SET_PREFERRED_NETWORK_TYPE:
				handleSetPreferredNetworkTypeResponse(msg);
				break;
			}
		}

		private void handleSetPreferredNetworkTypeResponse(Message msg) {
			AsyncResult ar = (AsyncResult) msg.obj;
			if (ar.exception != null) {
				// Set UI to current state
				Log.i(TAG, "set preferred network type, exception="
						+ ar.exception);
			} else {
				Log.i(TAG, "set preferred network type done");
			}
		}
	}
}
