package com.lewa.pond.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class Meta {

	private Context mContext;

	public Meta(Context c) {
		this.mContext = c;
	}

	public String getMetaDataFromApplication(String key) {
		String msg = "";
		try {
			ApplicationInfo applicationInfo = mContext.getPackageManager()
					.getApplicationInfo(mContext.getPackageName(),
							PackageManager.GET_META_DATA);
			msg = applicationInfo.metaData.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}
}
