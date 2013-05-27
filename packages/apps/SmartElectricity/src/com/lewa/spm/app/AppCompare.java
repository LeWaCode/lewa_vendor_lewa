package com.lewa.spm.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import android.content.Context;

public class AppCompare implements Comparable<Object>{
	//the parameter of application is (appInfo.uid , percent of total)
	private List<HashMap<String, Object>> mAppPowerUsageList;
	private CalculateAppUsagePercent mCalculateAppUsage;
	private Context mContext;
	
	public AppCompare(Context ctx) {
		mContext = ctx;
		mCalculateAppUsage = new CalculateAppUsagePercent(mContext);
		mAppPowerUsageList = mCalculateAppUsage.refreshStats();
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	//get power usage of application based on application uid
	public Double getAppUsage(Integer uid) {
			for (int i = 0; i < mAppPowerUsageList.size(); i ++){
				HashMap<String, Object> map = mAppPowerUsageList.get(i);
				Set<String> set = map.keySet();
				Iterator<String> its = set.iterator();
				while (its.hasNext()){
					// key
					String key = its.next();
					if (uid == Integer.parseInt(key)){
						return Math.round((Double) map.get(key) * 10) / 10.0 ;
					}
				}
			}
		return 0.0;
	}
	
	
	public void release(){
		if(mAppPowerUsageList != null){
			for(HashMap<String, Object> hashMap: mAppPowerUsageList){
				hashMap.clear();
			}
			mAppPowerUsageList.clear();
			mAppPowerUsageList = null;
		}
		if(mCalculateAppUsage != null){
			mCalculateAppUsage.release();
		}
	}
	
}
