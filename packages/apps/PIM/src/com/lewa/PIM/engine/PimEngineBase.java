package com.lewa.PIM.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

import android.R.integer;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.util.HanziToPinyin;
import com.android.internal.util.HanziToPinyin.Token;
import com.lewa.PIM.calllog.data.CallLog;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.calllog.data.MissedCallLogInfo;
import com.lewa.PIM.contacts.ContactsUtils;
import com.lewa.PIM.dialpad.data.DialpadItem;
import com.lewa.PIM.sim.SimCard;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.background.AsyncLoader;
import com.lewa.os.util.PinyinUtil;
import com.lewa.os.util.LocationUtil;
import com.lewa.os.util.Util;

public class PimEngineBase extends PimEngine implements Callback {
    private static final String TAG = "PimEngineBase";
    private static final int DBG_LEVEL = 0;

    private static int sSerialLogsLoader     = 0;
    private static int sSerialDialpadsLoader = 0;

    private static final int REQUEST_LOAD_DELAY_TIME = 1500;

    private static final int MESSAGE_DELAY_TIME_PASSED = 1;

    private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
        Contacts.DISPLAY_NAME,
        CommonDataKinds.Phone.NUMBER,
        CommonDataKinds.Phone.CONTACT_ID,
        Contacts.PHOTO_ID,
        Contacts.TIMES_CONTACTED,//add by zenghuaying
        "sort_key"
    };

	private static ArrayList<DialpadItem> mSpecialNumItem =new ArrayList<DialpadItem>();

    
    /**
     * A soft cache for photos.  <number, location>
     */
    private final ConcurrentHashMap<String, String> mLocationCache =
            new ConcurrentHashMap<String, String>();
    
    protected Context mContext;
    protected ContentResolver mResolver;
    private ArrayList<CallLogGroup> mLogGroups;
    private ArrayList<CallLogGroup> mBgLogGroups;
    private ArrayList<DialpadItem> mDialpadItems;
    private ArrayList<DialpadItem> mBgLogDialpads;
    private ArrayList<DialpadItem> mBgContactDialpads;
    private HashMap<Long, MissedCallLogInfo> mMapMissedLog;
    private ArrayList<PimEngine.DataEventListener> mDataListeners;

    private boolean mLoadCallLogsInProgress = false;
    private boolean mLoadCallLogsContactsInProgress = false;

    private CallLogsLoader     mLogsLoader;
    private DialpadItemsLoader mDialpadsLoader;
    private final Object mLock = new Object();
    
    private Handler mHandler;
    private int mDelayTime = 0;

	private static ArrayList<DialpadItem> getspecialNumItem () {

		if(mSpecialNumItem.size() > 0) {
			return mSpecialNumItem;
		}
		
		//add by zenghuaying add special number
		Set<String> specialNumSet = CommonMethod.mSpecialPhoneNameMap.keySet();
		Iterator<String> iter = specialNumSet.iterator();
		String name = null;
        String number = null;
		String[] aPinyinNames = null;
		
		while (iter.hasNext()) {
			number = iter.next();
			name = CommonMethod.mSpecialPhoneNameMap.get(number);
			ArrayList<Token> nameTokens = HanziToPinyin.getInstance().get(name);
			StringBuilder sb = new StringBuilder();
			for (Token token : nameTokens) {
								   
				if (Token.PINYIN == token.type) {
					if (sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(token.target);
					sb.append(' ');
					sb.append(token.source);
				} else {
					if (sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(token.source);
				}
			}
			aPinyinNames = PinyinUtil.splitPinyin(sb.toString(), ' ');
			DialpadItem dItem = new DialpadItem(name, number,aPinyinNames,true);
			mSpecialNumItem.add(dItem);
		}
		return mSpecialNumItem;
		
	}
	
    public void setContext(Context context) {
        mContext = context;
        mResolver = context.getContentResolver();
        mHandler = new Handler(Looper.myLooper(), this);
    }

    public void addDataListenner(PimEngine.DataEventListener listener) {
        if (null == mDataListeners) {
            mDataListeners = new ArrayList<PimEngine.DataEventListener>(2);
        }
        mDataListeners.add(listener);
    }

    public void removeDataListenner(PimEngine.DataEventListener listener) {
        if (null != mDataListeners) {
            mDataListeners.remove(listener);
        }
    }

    public void deleteCallLog(long logId) {
        String strWhere = "_id=" + String.valueOf(logId);
        mResolver.delete(Calls.CONTENT_URI, strWhere, null);
    }

    public void deleteCallLogs(long[] logIds) {
        String strWhere = null;
        if ((null != logIds) && (logIds.length > 0)) {
            strWhere = "_id IN(" + CommonMethod.arrayToString(logIds, ",", false) + ")";
        }
        mResolver.delete(Calls.CONTENT_URI, strWhere, null);
    }

    public void deleteCallLogs(ArrayList<Long> arrayLogIds) {
        String strWhere = null;
        if ((null != arrayLogIds) && (arrayLogIds.size() > 0)) {
            strWhere = "_id IN(" + CommonMethod.arrayToString(arrayLogIds, ",", false) + ")";
        }
        mResolver.delete(Calls.CONTENT_URI, strWhere, null);
    }

    public void setCallLogsRead() {
        String[] projection = new String[] {"COUNT(*) as calls_count"};
        Cursor cursor = mResolver.query(Calls.CONTENT_URI, projection, "new=1 and type=3", null, null);
        if (null != cursor) {
            if (cursor.moveToNext() && (cursor.getInt(0) > 0)) {
                ContentValues values = new ContentValues();
                values.put("new", 0);
                mResolver.update(Calls.CONTENT_URI, values, "new=1 and type=3", null);
            }
            cursor.close();
        }
    }

    public boolean isLoadCallLogsInProgress() {
        return mLoadCallLogsInProgress;
    }

    public void reloadCallLogsAndContacts() {
        mLoadCallLogsContactsInProgress = true;
        loadCallLogs(true);
        loadDialpadItems(true);
    }

    public List<CallLogGroup> loadCallLogs(boolean load) {
        if (null == mLogGroups) {
            mLogGroups = new ArrayList<CallLogGroup>();
            load = true;
        }

        if (load) {
            if (null == mLogsLoader) {
                mLogsLoader = new CallLogsLoader();
            }
            mLogsLoader.load();
            mLoadCallLogsInProgress = true;
        }
        
        return mLogGroups;
    }

    public List<DialpadItem> loadDialpadItems(boolean load) {
        if (null == mDialpadItems) {
            mDialpadItems = new ArrayList<DialpadItem>();
            load = true;
        }

        if (load) {
            if (null == mDialpadsLoader) {
                mDialpadsLoader = new DialpadItemsLoader();
            }
            mDialpadsLoader.load();
        }
        
        return mDialpadItems;
    }

    private void clearDialpadItems(List<DialpadItem> dialpadItems, int type) {
        if (null != dialpadItems) {
            int count = dialpadItems.size();
            DialpadItem item = null;
            for (int i = (count -1); i >= 0; --i) {
                item = dialpadItems.get(i);
                if (type == item.getType()) {
                    dialpadItems.remove(i);
                }
            }
        }
    }

    private void setDataItemLocation(DialpadItem item) {
        if (item == null) {
            return ;
        }
        
        String number = item.getNumber();
        if (!TextUtils.isEmpty(number)) {
            String location;
            if (!mLocationCache.containsKey(number)) {
                location = LocationUtil.getPhoneLocation(mContext, number);
                mLocationCache.put(number, (null == location) ? "" : location);
                
            } else {
                location = mLocationCache.get(number);
            }
            
            item.setLocation(location);
        }
    }
    
    private void setupCallLogTypeDialpadItems() {
        if (null != mBgLogGroups) {
            int count = mBgLogGroups.size();
            if (count > 0) {
                if (null == mBgLogDialpads) {
                    mBgLogDialpads = new ArrayList<DialpadItem>(count);
                }
                else {
                    mBgLogDialpads.clear();
                    mBgLogDialpads.ensureCapacity(count);
                }

                DialpadItem item = null;
                CallLogGroup clGroup = null;
                for (int i = 0; i < count; ++i) {
                    clGroup = mBgLogGroups.get(i);
                    if (null == (item = DialpadItem.findLogTypeDialpad(mBgLogDialpads, clGroup.getName(), clGroup.getNumber()))) {
                        item = new DialpadItem(clGroup);
                        setDataItemLocation(item);
                        mBgLogDialpads.add(item);
                    } else {
                        log("setupCallLogTypeDialpadItems  clGroup.getType()= " + clGroup.getType());
                        CallLog log;
                        for (int ii = 0; ii < clGroup.getSize(); ii++) {
                            log = clGroup.getLog(ii);
                            item.setCallType(log.getType(), 1);
                        }
                    }
                }
            }
        }
    }

    public void notifyDataEvent(PimEngine.DataEvent event, int state) {
        if (null != mDataListeners) {
            for (int i = 0; i < mDataListeners.size(); ++i) {
                final PimEngine.DataEventListener listener = mDataListeners.get(i);
                  listener.onDataEvent(event, state);
            }
        }
    }

    private void sendDelayLoadMessage() {
        if (mHandler.hasMessages(MESSAGE_DELAY_TIME_PASSED)) {
            mHandler.removeMessages(MESSAGE_DELAY_TIME_PASSED);
        }
        mHandler.sendEmptyMessageDelayed(MESSAGE_DELAY_TIME_PASSED, REQUEST_LOAD_DELAY_TIME);
        mDelayTime = REQUEST_LOAD_DELAY_TIME + mDelayTime;
        loge("sendDelayLoadMessage mDelayTime = 1500 ");
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        // msg.what
        if (MESSAGE_DELAY_TIME_PASSED == msg.what) {
            loge("handleMessage mDelayTime = 0 ");
            mDelayTime = 0;
        }

        return true;
    }

    void loge(String msg) {
        if (DBG_LEVEL > 0) {
            Log.e(TAG, msg);
        }
    }

    void log(String msg) {
        if (DBG_LEVEL > 2) {
            Log.i(TAG, msg);
        }
    }

    private class CallLogsLoader extends AsyncLoader {
        @Override
        public void load() {
        	if(sSerialDialpadsLoader != 0 || sSerialLogsLoader != 0){// add by shenqi avoid the DialpadItem is not over
        	  loge("sSerialDialpadsLoader is loading .....");
			  mDelayTime = mDelayTime + 500;
			}
			
            synchronized(mLock) {
                ++sSerialLogsLoader;
            }
            loge("CallLogsLoader   load  mDelayTime=" + mDelayTime);
            super.load(null, mDelayTime);
            sendDelayLoadMessage();
        }
        
        @Override
        protected LoadResults performLoading() {
            LoadResults results = null;
			
            log("CallLogsLoader   performLoading  Enter");
            synchronized(mLock) {

                if (null == mBgLogGroups) {
                    mBgLogGroups = new ArrayList<CallLogGroup>();
                }
                else {
                    mBgLogGroups.clear();
                }

                Cursor cursor = mResolver.query(Calls.CONTENT_URI,
                        CallLog.PROJECTION,
                        null,
                        null,
                        Calls.DEFAULT_SORT_ORDER);
                if (null != cursor) {
                    ArrayList<String> numbersMissed = null;
                    ArrayList<String> numbersArray = null;
                    String ipPrefix = CommonMethod.getIpPrefix(mContext, SimCard.GEMINI_SIM_1);
                    String chinaCallCode = CommonMethod.getChinaCallCode();
                    String[] arrPrefix;
                    arrPrefix = new String[1];
                    arrPrefix[0] = chinaCallCode;
                    /**
                    if (TextUtils.isEmpty(ipPrefix)) {
                        arrPrefix = new String[1];
                        arrPrefix[0] = chinaCallCode;
                    }
                    else {
                        arrPrefix = new String[2];
                        arrPrefix[0] = ipPrefix;
                        arrPrefix[1] = chinaCallCode;
                    }*/
                    if (cursor.moveToFirst()) {
                        do {
                            CallLog cl = CallLog.create(cursor);
                            cl.removeNumberPrefix(arrPrefix);
                            CallLogGroup clGroup = CallLogGroup.findGroup(mBgLogGroups, cl.getName(), cl.getNumber(), cl.getDateInd(), cl.getType());
                            if (null != clGroup) {
                                clGroup.addLog(cl);
                            }
                            else {
                                mBgLogGroups.add(new CallLogGroup(cl));
                            }

                            if (cl.isNewMissedCall()) {
                                if (null == numbersMissed) {
                                    numbersMissed = new ArrayList<String>();
                                }
                                numbersMissed.add(cl.getNumber());
                            }

                            if (!TextUtils.isEmpty(cl.getNumber())) {
                                if (null == numbersArray) {
                                    numbersArray = new ArrayList<String>();
                                }

                                if (!numbersArray.contains(cl.getNumber())) {
                                    numbersArray.add(cl.getNumber());
                                }
                            }
                        } while (cursor.moveToNext());
                    }

                    cursor.close();

                    if (null != numbersMissed) {
                        String numbers = CommonMethod.numberArrayToInClauseString(numbersMissed, chinaCallCode, ipPrefix);
                        String strWhere = "REPLACE(TRIM(" + CommonDataKinds.Phone.NUMBER + "), '-', '') IN("
                                + numbers + ")" + " OR " + "REPLACE(TRIM(" + CommonDataKinds.Phone.NUMBER + "), ' ', '') IN("
                                + numbers + ")";
                        //Log.d(TAG, "numbersMissed: where=" + strWhere);
                        Cursor cursorPhone = mResolver.query(CommonDataKinds.Phone.CONTENT_URI,
                                new String[] {CommonDataKinds.Phone.CONTACT_ID,
                                        Contacts.DISPLAY_NAME, //ContactsContract.PhoneLookup.DISPLAY_NAME,
                                        CommonDataKinds.Phone.NUMBER},
                                strWhere,
                                null,
                                null);
                        if (null != cursorPhone) {
                            if (null == mMapMissedLog) {
                                mMapMissedLog = new HashMap<Long, MissedCallLogInfo>();
                            }
                            else {
                                mMapMissedLog.clear();
                            }
                            
                            while (cursorPhone.moveToNext()) {
                                long   contactId = cursorPhone.getLong(0);
                                String name      = cursorPhone.getString(1);
                                String number    = PhoneNumberUtils.stripSeparators(cursorPhone.getString(2)); //cursorPhone.getString(2);
                                int    count     = 0;
                                if (!TextUtils.isEmpty(name)) {
                                    for (int i = 0; i < numbersMissed.size(); ++i) {
                                        if (PhoneNumberUtils.compare(number, numbersMissed.get(i))) {
                                            numbersMissed.remove(i);
                                            ++count;
                                            --i;
                                        }
                                    }
                                    
                                    MissedCallLogInfo info = mMapMissedLog.get(Long.valueOf(contactId));
                                    if (null == info) {
                                        info = new MissedCallLogInfo(contactId, name, number, 0, count);
                                        mMapMissedLog.put(Long.valueOf(contactId), info);
                                    }else {
                                        info.addNumber(number);
                                        info.setCount(info.getCount() + count);
                                    }
                                }
                            }
                            
                            cursorPhone.close();
                        }
                    }

                    if (null != numbersArray) {
                        String numbers = CommonMethod.numberArrayToInClauseString(numbersArray, chinaCallCode, ipPrefix);
                        String strWhere = "REPLACE(TRIM(" + CommonDataKinds.Phone.NUMBER + "), '-', '') IN("
                                + numbers + ")" + " OR " + "REPLACE(TRIM(" + CommonDataKinds.Phone.NUMBER + "), ' ', '') IN("
                                + numbers + ")";
                        Cursor cursorPhone = mResolver.query(CommonDataKinds.Phone.CONTENT_URI,
                                new String[] {CommonDataKinds.Phone.CONTACT_ID,
                                        Contacts.PHOTO_ID,
                                        Contacts.DISPLAY_NAME, //ContactsContract.PhoneLookup.DISPLAY_NAME,
                                        CommonDataKinds.Phone.NUMBER},
                                strWhere,
                                null,
                                null);
                        if (null != cursorPhone) {
                            int groupCount = mBgLogGroups.size();
                            while (cursorPhone.moveToNext()) {
                                long   contactId = cursorPhone.getLong(0);
                                long   photoId   = cursorPhone.getLong(1);
                                String name      = cursorPhone.getString(2);
                                String number    = PhoneNumberUtils.stripSeparators(cursorPhone.getString(3));
                                if (number.startsWith(chinaCallCode)) {
                                    number = number.substring(chinaCallCode.length());
                                }
								//add by zenghuaying fix bug #9947
                                if(CommonMethod.isStartWithIpPrefix(mContext, number)){
                                    number = number.substring(5);
                                }
                                //add end
                                if (!TextUtils.isEmpty(name)) {
                                    for (int i = 0; i < groupCount; ++i) {
                                        final CallLogGroup group = mBgLogGroups.get(i);
                                        final int clCount = group.getSize();
                                        for (int j = 0; j < clCount; ++j) {
                                            final CallLog cl = group.getLog(j);
                                            if (0 == cl.getContactId()) {
                                                if (PhoneNumberUtils.compare(number, cl.getNumber())) {
                                                    cl.setContactId(contactId);
                                                    cl.setPhotoId(photoId);
                                                    cl.setName(name);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            cursorPhone.close();
                        }
                    }
                }

                setupCallLogTypeDialpadItems();
                
            }
            
            log("CallLogsLoader   performLoading  End");
            return results;
        }

        @Override
        protected void publishLoadResults(LoadResults results) {
            mLoadCallLogsInProgress = false;
            log("CallLogsLoader   publishLoadResults  Enter");
            synchronized(mLock) {
                clearDialpadItems(mDialpadItems, DialpadItem.CALLLOG_TYPE);
                if ((null != mBgLogDialpads) && (mBgLogDialpads.size() > 0)) {
                    //Added by GanFeng 20120222, the mDialpadItems may be null here
                    if (null == mDialpadItems) {
                        mDialpadItems = new ArrayList<DialpadItem>();
                    }
                    mDialpadItems.addAll(0, mBgLogDialpads);
                }
                mBgLogDialpads = null;
                
                //Added by shenqi for avoiding null point 
		  if(mLogGroups != null) {
	                mLogGroups.clear();
		  }
                mLogGroups = mBgLogGroups;
                mBgLogGroups = null;

                sSerialLogsLoader = 0;
            }

            log("CallLogsLoader   publishLoadResults  End lock  mLoadCallLogsContactsInProgress=" + mLoadCallLogsContactsInProgress);
            
            if (null != mDataListeners && mLoadCallLogsContactsInProgress == false) {
                for (int i = 0; i < mDataListeners.size(); ++i) {
                    final PimEngine.DataEventListener listener = mDataListeners.get(i);
                    listener.onDataEvent(PimEngine.DataEvent.LOAD_CALLLOGS,
                            PimEngine.DataEventListener.LOAD_DATA_DONE);
                    listener.onDataEvent(PimEngine.DataEvent.LOAD_DIALPADS,
                            PimEngine.DataEventListener.LOAD_DATA_DONE);
                    listener.onDataEvent(PimEngine.DataEvent.LOAD_MISSED_LOGS,
                            PimEngine.DataEventListener.LOAD_DATA_DONE);
                }
            }
        }
    }

    private class DialpadItemsLoader extends AsyncLoader {
        @Override
        public void load() {
            synchronized(mLock) {
                ++sSerialDialpadsLoader;
        }
        super.load(null, mDelayTime);
		sendDelayLoadMessage();
    }
        
        @Override
        protected LoadResults performLoading() {
            LoadResults results = null;
            //add by shenqi for sync with CallLogsLoader
			while(sSerialLogsLoader != 0){
				try {
                      Thread.sleep(200);
                } catch (InterruptedException er) {
                }			
			}
			log("DialpadItemsLoader   performLoading  Enter");
            synchronized(mLock) { //(mBgContactDialpads) {

                if (null == mBgContactDialpads) {
                    mBgContactDialpads = new ArrayList<DialpadItem>();
                }
                else {
                    mBgContactDialpads.clear();
                }

                //Modified by GanFeng 20120104, always display the contacts though it is invisible
                //String selectionWithGroupBy = Contacts.IN_VISIBLE_GROUP + "=1)"
                //    + " GROUP BY " + Contacts.DISPLAY_NAME + ", (" + CommonDataKinds.Phone.NUMBER;
                String selectionWithGroupBy = "1=1) GROUP BY " + Contacts.DISPLAY_NAME + ", (" + CommonDataKinds.Phone.NUMBER;
                Cursor cursor = mResolver.query(
                        CommonDataKinds.Phone.CONTENT_URI,
                        CONTACTS_SUMMARY_PROJECTION,
                        selectionWithGroupBy,
                        null,
                        Contacts.TIMES_CONTACTED +" DESC");//modify by zenghuaying
                        //"sort_key");
                if (null != cursor) {
                    String name = null;
                    String number = null;
                    String[] aPinyinNames = null;
                    long contactId = 0;
                    long photoId = 0;
                    //add by zenghuaying
                    int contactedTimes = 0;
                    DialpadItem dialpadItem = null;
                    
                    while (cursor.moveToNext()) {
                        name = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
                        number = cursor.getString(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.NUMBER));
                        number = PhoneNumberUtils.stripSeparators(number);
                        aPinyinNames = PinyinUtil.splitPinyin(cursor.getString(cursor.getColumnIndexOrThrow("sort_key")), ' ');
                        contactId = cursor.getLong(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.CONTACT_ID));
                        photoId = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts.PHOTO_ID));
                        //add by zenghuaying
                        contactedTimes = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.TIMES_CONTACTED));
                        dialpadItem = new DialpadItem(name, aPinyinNames, number, contactId, photoId,contactedTimes);
                        setDataItemLocation(dialpadItem);
                        mBgContactDialpads.add(dialpadItem);
                    }
                    
                    cursor.close();
                    //add by zenghuaying add special number
                    /*Set<String> specialNumSet = CommonMethod.mSpecialPhoneNameMap.keySet();
                    Iterator<String> iter = specialNumSet.iterator();
                    
                    while (iter.hasNext()) {
                        number = iter.next();
                        name = CommonMethod.mSpecialPhoneNameMap.get(number);
                        ArrayList<Token> nameTokens = HanziToPinyin.getInstance().get(name);
                        StringBuilder sb = new StringBuilder();
                        for (Token token : nameTokens) {
                            
                            if (Token.PINYIN == token.type) {
                                if (sb.length() > 0) {
                                    sb.append(' ');
                                }
                                sb.append(token.target);
                                sb.append(' ');
                                sb.append(token.source);
                            } else {
                                if (sb.length() > 0) {
                                    sb.append(' ');
                                }
                                sb.append(token.source);
                            }
                        }
                        aPinyinNames = PinyinUtil.splitPinyin(sb.toString(), ' ');
                        DialpadItem dItem = new DialpadItem(name, number,aPinyinNames,true);
                        mBgContactDialpads.add(dItem);
                    }
                    //add end
                    */
                }
             mBgContactDialpads.addAll(getspecialNumItem());
            }

            log("DialpadItemsLoader   performLoading  End");
            return results;
        }

        @Override
        protected void publishLoadResults(LoadResults results) {
            log("DialpadItemsLoader   publishLoadResults  Enter");
            synchronized(mLock) {
                clearDialpadItems(mDialpadItems, DialpadItem.CONTACT_TYPE);
                if ((null != mBgContactDialpads) && (mBgContactDialpads.size() > 0)) {
                    mDialpadItems.addAll(mBgContactDialpads);
                }
                mBgContactDialpads = null;
                sSerialDialpadsLoader = 0;
            }
            log("DialpadItemsLoader   publishLoadResults  End lock");
            
            if (null != mDataListeners) {
                for (int i = 0; i < mDataListeners.size(); ++i) {
                    final PimEngine.DataEventListener listener = mDataListeners.get(i);
                    listener.onDataEvent(PimEngine.DataEvent.LOAD_DIALPADS,
                            PimEngine.DataEventListener.LOAD_DATA_DONE);
                }
            }
            mLoadCallLogsContactsInProgress = false;
        }
    }
    
    public HashMap<Long, MissedCallLogInfo> loadMissedCallLogInfo(boolean forceQuery) {
        if (forceQuery) {
        }

        return mMapMissedLog;
    }

}
