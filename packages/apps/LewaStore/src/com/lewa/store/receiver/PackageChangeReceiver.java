package com.lewa.store.receiver;

import com.lewa.store.utils.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackageChangeReceiver extends BroadcastReceiver{

	private String TAG=PackageChangeReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		synchronized (this) {
			if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
				Log.d(TAG, intent.getDataString()+ " package added");			
				String packageName=intent.getDataString();			
				//更新已安装
				Intent i = new Intent(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
				i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,Constants.UPDATE_INSTALLED_ITEMS_ID);
				i.putExtra("packageName",packageName);
			    context.sendBroadcast(i);
			}
		}		
	}	
}
