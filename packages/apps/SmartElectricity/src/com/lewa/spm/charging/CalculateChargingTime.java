package com.lewa.spm.charging;

import java.util.Calendar;
import com.lewa.spm.util.Constants;
import com.lewa.spm.util.SharedStorageKeyValuePair;

import android.content.Context;

public class CalculateChargingTime {

	Context mContext;
	SharedStorageKeyValuePair saveValue;
	public CalculateChargingTime(Context ctx) {
		mContext = ctx;
		saveValue = new SharedStorageKeyValuePair(mContext);
	}

	public long estimateChargingTime(String spName,int level){
		long chargingTime = 0;
		for (int i = level; i < 101; i ++){
			chargingTime = chargingTime + getBatteryCharginginfo(spName, String.valueOf(i));
		}
		// TODO: add by luoyongxing temporarily
		
		if(level == 100){
		    long almostFullStartTime = saveValue.getLong(Constants.SHARED_PREFERENCE_NAME,
                                                        Constants.SPM_ALMOST_FULL_START_TIME,
                                                        0);
		    long delta = (Calendar.getInstance().getTimeInMillis() - almostFullStartTime)/1000;
		    if(delta > 0&& delta < 10800 ){
                if(chargingTime > delta){
		            chargingTime -= delta;
                }else{
                    chargingTime = 0;
                }
                
		    }
		}
		
		return chargingTime;
	}
	
	private long getBatteryCharginginfo(String spName, String key){
		long value = saveValue.getLong(spName, key, 0);
		return value;
	}
}
