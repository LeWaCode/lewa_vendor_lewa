package com.lewa.spm.app;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
/**
 * this class is used to get the running application list
 * @author Administrator
 *
 */
public class GetRunningApp {
	
	Context mContext;
	List<String> runningAppUid;

	public GetRunningApp(Context mContext) {
		this.mContext = mContext;
	}
	
	/**
	 * get the running application info
	 * @return
	 */
	public List<String> queryAllRunningAppInfo(){
		runningAppUid = new ArrayList<String>();
        ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);   
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();   
        PackageManager packageManager = mContext.getPackageManager();
        for (ActivityManager.RunningAppProcessInfo runningApp : list) {   
            if (packageManager.getLaunchIntentForPackage(runningApp.processName) != null) { 
				try {
//					ApplicationInfo appInfo = packageManager.getApplicationInfo(runningApp.processName,  PackageManager.GET_META_DATA);
//					runningAppUid.add(String.valueOf(appInfo.uid));
                                    runningAppUid.add(String.valueOf(runningApp.uid));
				} catch (Exception e) {
					e.printStackTrace();
				}  
            }   
        }
        list.clear();
		return runningAppUid;
	}
	
	public void release(){
		if(runningAppUid != null){
			runningAppUid.clear();
			runningAppUid = null;
		}
	}
}
