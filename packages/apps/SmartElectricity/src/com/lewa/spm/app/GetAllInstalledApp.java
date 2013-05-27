package com.lewa.spm.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.lewa.spm.adapter.AppInfo;

public class GetAllInstalledApp {
	Context mContext;
	AppCompare appUsageInfo;
	ArrayList<AppInfo> appList;

	public GetAllInstalledApp(Context ctx) {
		mContext = ctx;
	}

	/**
	 * get all installed application
	 * @return
	 */
	public ArrayList<AppInfo> getApp(){
		appUsageInfo = new AppCompare(mContext);
		appList = new ArrayList<AppInfo>();
		List<PackageInfo> packages = mContext.getPackageManager().getInstalledPackages(0);
		Double powerUsaged = null;
		int uid = 0;
                int packageSize = packages.size();
		for(int i=0;i<packageSize;i++) { 
			PackageInfo packageInfo = packages.get(i); 
//			if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0)
//			{
			uid = packageInfo.applicationInfo.uid;
			powerUsaged = appUsageInfo.getAppUsage(uid);
			if ((powerUsaged != null) && (powerUsaged > 0.0)){
				AppInfo tmpInfo = new AppInfo(); 
				tmpInfo.label = packageInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString(); 
				tmpInfo.packageName = packageInfo.packageName; 
                                tmpInfo.icon = mContext.getPackageManager().getDefaultActivityIcon();
//				tmpInfo.icon = packageInfo.applicationInfo.loadIcon(mContext.getPackageManager());
				tmpInfo.uid = uid;
				tmpInfo.powerUsage = powerUsaged;
				appList.add(tmpInfo);
			}
//	        }
	}
		packages.clear();
		return appList;
}
	
	public void release(){
		if (appList != null){
			appList.clear();
			appList = null;
		}
        if(appUsageInfo!=null){
		    appUsageInfo.release();
        }
	}
       
}
