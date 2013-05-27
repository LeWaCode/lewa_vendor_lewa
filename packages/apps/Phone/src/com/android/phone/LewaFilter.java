package com.android.phone;

import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.InterceptConstants;
import android.util.Log;

import com.tencent.tmsecure.aresengine.dao.BlackListDao;
import com.tencent.tmsecure.aresengine.dao.LewaAresEngineFactor;
import com.tencent.tmsecure.aresengine.dao.WhiteListDao;
import com.tencent.tmsecure.common.ManagerCreator;
import com.tencent.tmsecure.module.aresengine.DataFilter;
import com.tencent.tmsecure.module.aresengine.InComingCallFilter;
import com.tencent.tmsecure.module.aresengine.InComingSmsFilter;
import com.tencent.tmsecure.module.aresengine.AresEngineManager;
import com.tencent.tmsecure.module.aresengine.CallLogEntity;
import com.tencent.tmsecure.module.aresengine.ContactEntity;
import com.tencent.tmsecure.module.aresengine.DataIntercepterBuilder;
import com.tencent.tmsecure.module.aresengine.FilterConfig;
import com.tencent.tmsecure.module.aresengine.FilterResult;
import com.tencent.tmsecure.module.aresengine.InComingCallFilter;
import com.tencent.tmsecure.module.aresengine.InComingSmsFilter;
import com.tencent.tmsecure.module.aresengine.SmsEntity;
import com.tencent.tmsecure.module.aresengine.IIntelligentSmsChecker;
import com.tencent.tmsecure.module.aresengine.IIntelligentSmsChecker.CheckResult;

// Created by zhumeiquan for Tencent block
public class LewaFilter {
    private static final String TAG = "LewaFilter";
    private static ContactEntity mEntity;
    private DataFilter<SmsEntity> mIncomingSmsFilter;
    private DataFilter<CallLogEntity> mIncomingCallFilter; 
    private AresEngineManager mAresEngineManager;
    private static LewaFilter sInstance;
    private int mBlockMode;
   
    public LewaFilter(Context context) {        
        mAresEngineManager = ManagerCreator.getManager(AresEngineManager.class);
    	mAresEngineManager.setAresEngineFactor(new LewaAresEngineFactor(context.getApplicationContext()));
    	mIncomingCallFilter = DataIntercepterBuilder.createInComingCallIntercepterBuilder().getDataFilter();
    	mIncomingSmsFilter = DataIntercepterBuilder.createInComingSmsIntercepterBuilder().getDataFilter();
    	
        initSettings(context);        
    }

    public static LewaFilter getInstance(Context context) {
        synchronized (LewaFilter.class) {
            if (sInstance == null) {
                sInstance = new LewaFilter(context);
            }
        }
        return sInstance;
    }

    public void initSettings(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(InterceptConstants.DND_CONTENT_URI, null, null, null, null);

        if (cursor.getCount() == 0) {
            mBlockMode = InterceptConstants.BLOCK_MODE_SMART;
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_SWITCH, Integer.toString(InterceptConstants.BLOCK_SWITCH_ON_INT));
            values.put(InterceptConstants.COLUMN_SWITCH_MODE, mBlockMode);
            values.put(InterceptConstants.COLUMN_START_TIME, InterceptConstants.STARTTIME);
            values.put(InterceptConstants.COLUMN_END_TIME, InterceptConstants.ENDTIME);
            resolver.insert(InterceptConstants.DND_CONTENT_URI, values);
        } else {    // init the blockmode
            cursor.moveToFirst();
            mBlockMode = cursor.getInt(cursor.getColumnIndex(InterceptConstants.COLUMN_SWITCH_MODE));
        }
        cursor.close();

        setBlockMode(mBlockMode);
    }
    
    public boolean isInBlockTime(Context context) {
        Cursor cursor = context.getContentResolver().query(InterceptConstants.DND_CONTENT_URI, null, null, null, null);

        int mAllDay = InterceptConstants.BLOCK_SWITCH_ON_INT;
        String startTime = InterceptConstants.STARTTIME;
        String endTime = InterceptConstants.ENDTIME;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            mAllDay = cursor.getInt(cursor.getColumnIndex(InterceptConstants.COLUMN_SWITCH));
            startTime = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_START_TIME));
            endTime = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_END_TIME));
        }
        cursor.close();
        cursor = null;
        
       Calendar calendar = Calendar.getInstance();
    	 int sTime = Integer.parseInt(startTime.replace(":", ""));
    	 int eTime = Integer.parseInt(endTime.replace(":", ""));
      
    	 //int now = Integer.parseInt(calendar.get(Calendar.HOUR_OF_DAY)+""+calendar.get(Calendar.MINUTE));
     	 int now ;
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        
        if (m==0) {
             now = Integer.parseInt(h+"00");
        }else if(m<10&&m>0){
             now = Integer.parseInt(h+"0"+m);
        }else {
             now = Integer.parseInt(h+""+m);
        }

        if (sTime > eTime) {
            eTime += 2400;
            if(now < sTime) {
                now += 2400;
             }           
        }
        if (mAllDay == InterceptConstants.BLOCK_SWITCH_OFF_INT && (now < sTime || now > eTime)) {
    		return false;
    	}
        return true;
    }    

    public boolean isBlockCall(Context context, String incomingNumber) {  
        boolean shouldBlock = false;
        try {
            if (isInBlockTime(context)) {
                CallLogEntity callEntity = new CallLogEntity();
                callEntity.phonenum = incomingNumber;
                FilterResult result = mIncomingCallFilter.filter(callEntity);
                shouldBlock = (result != null && result.mState == FilterConfig.STATE_REJECTABLE);
        	}
        } catch (Exception e) {
            Log.e(TAG, "isBlockCall: e=" + e);
        }
    	return shouldBlock;
    }

    public boolean isBlockMessage(Context context, String incomingNumber, String body) {
        boolean shouldBlock = false;
        if (isInBlockTime(context)) {
            SmsEntity smsEntity = new SmsEntity();
            smsEntity.body = body;
            smsEntity.phonenum = incomingNumber;
            FilterResult filterRet = mIncomingSmsFilter.filter(smsEntity);
            if (filterRet.mState == FilterConfig.STATE_ENABLE) {
                IIntelligentSmsChecker smschecker = mAresEngineManager.getIntelligentSmsChecker();
                IIntelligentSmsChecker.CheckResult ret = smschecker.check(smsEntity);
                if (ret != null) {
                if (ret.mContentType == CheckResult.TYPE_LEGAL_ORGANIZATION
                        || ret.mContentType == CheckResult.TYPE_NORMAL) {
                    return false;
                } else if (ret.mContentType == CheckResult.TYPE_ADS
                    || ret.mContentType == CheckResult.TYPE_FRAUD
                    || ret.mContentType == CheckResult.TYPE_SEX
                    || ret.mContentType == CheckResult.TYPE_MO_CHARGE
                    || ret.mContentType == CheckResult.TYPE_MT_CHARGE) {
                    shouldBlock = true;
                }
                }
            } else {
                shouldBlock = (filterRet != null && filterRet.mState == FilterConfig.STATE_REJECTABLE);
            }
        }
        return shouldBlock;
    }
	
    public void insertBlackNumber2Cache (Context context, String number, int type) {
        mEntity = new ContactEntity();
        mEntity.phonenum = number;
        if (type == InterceptConstants.BLOCK_TYPE_NUMBER_DEFAULT) {
        	mEntity.enableForCalling = true;
        	mEntity.enableForSMS = true;
        } else if (type == InterceptConstants.BLOCK_TYPE_NUMBER_CALL) {
        	mEntity.enableForCalling = true;
        } else if (type == InterceptConstants.BLOCK_TYPE_NUMBER_MSG) {
        	mEntity.enableForSMS = true;
        }
        BlackListDao.getInstance(context).insert(mEntity);
    }

    public void updateBlackNumberInCache(Context context, String number, int newNumberType) {
        mEntity = new ContactEntity();
        mEntity.phonenum = number;
        if (newNumberType == InterceptConstants.BLOCK_TYPE_NUMBER_DEFAULT) {
        	mEntity.enableForCalling = true;
        	mEntity.enableForSMS = true;
        } else if (newNumberType == InterceptConstants.BLOCK_TYPE_NUMBER_CALL) {
        	mEntity.enableForCalling = true;
        } else if (newNumberType == InterceptConstants.BLOCK_TYPE_NUMBER_MSG) {
        	mEntity.enableForSMS = true;
        }
        BlackListDao.getInstance(context).update(mEntity);
    }

    public void deleteBlackNumberFromCache(Context context, String number) {
        mEntity = new ContactEntity();
        mEntity.phonenum = number;
        BlackListDao.getInstance(context).delete(mEntity);
    }

    public void insertWhiteNumber2Cache (Context context, String number) {
        mEntity = new ContactEntity();
        mEntity.phonenum = number;
        WhiteListDao.getInstance(context).insert(mEntity);
    }

    public void deleteWhiteNumberFromCache(Context context, String number) {
        mEntity = new ContactEntity();
        mEntity.phonenum = number;
        WhiteListDao.getInstance(context).delete(mEntity);
    } 

    public void setBlockMode(int blockMode) {
        mBlockMode = blockMode;

        FilterConfig smsConfig = new FilterConfig();
        FilterConfig callConfig = new FilterConfig();

        switch (blockMode) {
        case InterceptConstants.BLOCK_MODE_SMART:
        {
            smsConfig.set(InComingSmsFilter.WHITE_LIST,          FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.BLACK_LIST,          FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.SYS_CONTACT,         FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.INTELLIGENT_CHECKING,FilterConfig.STATE_ENABLE);
            smsConfig.set(InComingSmsFilter.STRANGER_SMS,        FilterConfig.STATE_ACCEPTABLE);
            mIncomingSmsFilter.setConfig(smsConfig);            

            //callConfig.set(InComingCallFilter.PRIVATE_CALL,      FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.WHITE_LIST,        FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.BLACK_LIST,        FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.SYS_CONTACT,       FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.STRANGER_CALL,     FilterConfig.STATE_ACCEPTABLE);
            mIncomingCallFilter.setConfig(callConfig);
            break;
        }

        case InterceptConstants.BLOCK_MODE_BLACKLIST:
        {
            smsConfig.set(InComingSmsFilter.WHITE_LIST,          FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.BLACK_LIST,          FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.SYS_CONTACT,         FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.INTELLIGENT_CHECKING,FilterConfig.STATE_DISABLE);
            smsConfig.set(InComingSmsFilter.STRANGER_SMS,        FilterConfig.STATE_ACCEPTABLE);
            mIncomingSmsFilter.setConfig(smsConfig);

            //callConfig.set(InComingCallFilter.PRIVATE_CALL,      FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.WHITE_LIST,        FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.BLACK_LIST,        FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.SYS_CONTACT,       FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.STRANGER_CALL,     FilterConfig.STATE_ACCEPTABLE);            
            mIncomingCallFilter.setConfig(callConfig);
            break;
        }

        case InterceptConstants.BLOCK_MODE_OUT_OF_WHITELIST:
        {
            smsConfig.set(InComingSmsFilter.WHITE_LIST,          FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.BLACK_LIST,          FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.SYS_CONTACT,         FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.INTELLIGENT_CHECKING,FilterConfig.STATE_DISABLE);
            smsConfig.set(InComingSmsFilter.STRANGER_SMS,        FilterConfig.STATE_REJECTABLE);           
            mIncomingSmsFilter.setConfig(smsConfig);
            
            callConfig.set(InComingCallFilter.WHITE_LIST,        FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.BLACK_LIST,        FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.SYS_CONTACT,       FilterConfig.STATE_REJECTABLE);            
            callConfig.set(InComingCallFilter.STRANGER_CALL,     FilterConfig.STATE_REJECTABLE);
            mIncomingCallFilter.setConfig(callConfig);
            break;
        }        

        case InterceptConstants.BLOCK_MODE_EXCEPT_CONTACT:
        {
            smsConfig.set(InComingSmsFilter.WHITE_LIST,          FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.BLACK_LIST,          FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.SYS_CONTACT,         FilterConfig.STATE_ACCEPTABLE);
            smsConfig.set(InComingSmsFilter.INTELLIGENT_CHECKING,FilterConfig.STATE_DISABLE);            
            smsConfig.set(InComingSmsFilter.STRANGER_SMS,        FilterConfig.STATE_REJECTABLE);           
            mIncomingSmsFilter.setConfig(smsConfig);

            //callConfig.set(InComingCallFilter.PRIVATE_CALL,      FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.WHITE_LIST,        FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.BLACK_LIST,        FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.SYS_CONTACT,       FilterConfig.STATE_ACCEPTABLE);
            callConfig.set(InComingCallFilter.STRANGER_CALL,     FilterConfig.STATE_REJECTABLE);
            mIncomingCallFilter.setConfig(callConfig);
            break;
        }

        case InterceptConstants.BLOCK_MODE_ALLNUM:
        {
            smsConfig.set(InComingSmsFilter.WHITE_LIST,          FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.BLACK_LIST,          FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.SYS_CONTACT,         FilterConfig.STATE_REJECTABLE);
            smsConfig.set(InComingSmsFilter.INTELLIGENT_CHECKING,FilterConfig.STATE_DISABLE);
            smsConfig.set(InComingSmsFilter.STRANGER_SMS,        FilterConfig.STATE_REJECTABLE);  
            mIncomingSmsFilter.setConfig(smsConfig);
            
            //callConfig.set(InComingCallFilter.PRIVATE_CALL,      FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.WHITE_LIST,        FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.BLACK_LIST,        FilterConfig.STATE_REJECTABLE);
            callConfig.set(InComingCallFilter.SYS_CONTACT,       FilterConfig.STATE_REJECTABLE);            
            callConfig.set(InComingCallFilter.STRANGER_CALL,     FilterConfig.STATE_REJECTABLE);
            mIncomingCallFilter.setConfig(callConfig);
            break;
        }

        default:
            break;
        }		
    }
}
