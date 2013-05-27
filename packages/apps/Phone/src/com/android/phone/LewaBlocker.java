/**
 * @author chenhengheng
 * it is common class for 360 intercept
 */

package com.android.phone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.provider.CallLog;
import android.provider.Telephony;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.InterceptConstants;
import com.android.internal.telephony.Call;
import android.provider.Settings;
import android.content.Intent;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import com.tencent.tmsecure.aresengine.dao.BlackListDao;
import com.tencent.tmsecure.aresengine.dao.WhiteListDao;
import com.tencent.tmsecure.common.ManagerCreator;
import com.tencent.tmsecure.module.update.ICheckListener;
import com.tencent.tmsecure.module.update.IUpdateListener;
import com.tencent.tmsecure.module.update.UpdateConfig;
import com.tencent.tmsecure.module.update.UpdateInfo;
import com.tencent.tmsecure.module.update.UpdateManager;
import com.tencent.tmsecure.module.update.CheckResult;

public class LewaBlocker {
    private static boolean DBUG = true;
    private static final String TAG = "LewaBlocker";

    private Context mContext;
    private UpdateManager mUpdateManager;
    private List<UpdateInfo> updateInfoList;
    private CheckResult mCheckResults;
    private Handler mHandler;

    private static final int MSG_HIDE_CHECK_PROGRESS = 0;
    private static final int MSG_HIDE_UPDATE_PROGRESS = 1;
    private static final int MSG_UPDATE_PROGRESS = 2;
    private static final int MSG_NETWORK_ERROR = 3;
    private static final int MSG_NONEED_TOUPDATE = 4;

    private static final int TYPE_CALL = 0;
    private static final int TYPE_SMS = 1;
    private static final int TYPE_BLACK = 2;

    private static final int STATE_READ = 0;
    private static final int SMS_TYPE_INBOX = 1;

    static Uri MSG_INBOX_URI = Uri.parse("content://sms/inbox");

    private final static int QUERY_SMS_BY_ADDRESS_TO_BLACKLIST = 0;
    private final static int DELETE_SMS_FROM_SMS = 1;

    private final static int QUERY_FROM_BLACKLIST = 2;
    private final static int DELETE_FROM_BLACKLIST = 3;
    public static final int MIN_NUMBER_LENGTH_INCLUDE_COUNTRY_CODE = 11;

    private static final String LEWA_INTERCEPT_INSERTBLACK2CACHE_ACTION = "android.provider.lewa.intercept.insertBlack2Cache";

    private ArrayList<ContentValues> mContentValuesList = new ArrayList<ContentValues>();

    private SmsListQueryHandler mQueryHandler;
    
    private static final String[] COUNTRY_CALL_CODES = new String[] {
        "+86", //China
        "+852", //Hongkong
        "+853", //Macao
        "+886", //Taiwan
        "+27", //South Africa
        "+31", //Netherlands
        "+33", //France
        "+44", //United Kiongdom
        "+49", //Germany
        "+55", //Brazil
        "+61", //Australia
        "+64", //NewZealand
        "+65", //Singapore
        "+81", //Japan
        "+82"  //Korea
    };
    
    private static final String[] SPECIAL_PREFIX_IN_NUMBER = new String[] {
        "12520026",
        "12520"
    };

    // Singleton instance
    private static LewaBlocker SINSTANCE;
    private static LewaFilter mLewaFilter;

    private LewaBlocker(Context context) {
        // Begin, Modified by zhumeiquan, 20120518
        //Blocker.init(context);
        mLewaFilter = LewaFilter.getInstance(context);
        // End        
    }

    public static LewaBlocker getInstance(Context context) {
        synchronized (LewaBlocker.class) {
            if (null == SINSTANCE) {
		 try{
                SINSTANCE = new LewaBlocker(context);
	 	 }
		 catch(Exception e) {
		 	e.printStackTrace();
			SINSTANCE = null;

	 	 }
            }
        }
        return SINSTANCE;
    }

    /*Begin, Modified by zhumeiquan Following method is different with Tencent, replace 360 interface with Tencent, 20120518*/
    public boolean isBlockCall(Context context, String incomingNumber) {
        //int resultCode = Blocker.isBlockCall(context, incomingNumber);
        //boolean isFilter = Blocker.isBlock(resultCode);
        //return isFilter;
        return mLewaFilter.isBlockCall(context, incomingNumber);        
    }

    public boolean isBlockMessage(Context context, String incomingNumber, String body) {
        //int resultCode = Blocker.isBlockMessage(context, incomingNumber, body);
        //boolean isFilter = Blocker.isBlock(resultCode);
        //return isFilter;
        return mLewaFilter.isBlockMessage(context, incomingNumber, body);        
    }

    public void insertBlackNumber2Cache(Context context, String number, int type) {
        //Blocker.insertBlackNumber2Cache(context, number, type);
        mLewaFilter.insertBlackNumber2Cache(context, number, type);
    }

    public void updateBlackNumberInCache(Context context, String number, int newNumberType) {
        //Blocker.updateBlackNumberInCache(context, number, newNumberType);
        mLewaFilter.updateBlackNumberInCache(context, number, newNumberType);
    }      

    public void deleteBlackNumberFromCache(Context context, String number) {
        //Blocker.deleteBlackNumberFromCache(context, number);
        mLewaFilter.deleteBlackNumberFromCache(context, number);
    }

    public void insertWhiteNumber2Cache(Context context, String number) {
        //Blocker.insertWhiteNumber2Cache(context, number);
        mLewaFilter.insertWhiteNumber2Cache(context, number);

    }
    public void deleteWhiteNumberFromCache(Context context, String number) {
        //Blocker.deleteWhiteNumberFromCache(context, number);
        mLewaFilter.deleteWhiteNumberFromCache(context, number);

    }

    public void removeAllNumber(Context context,int type) {

        //String selection = InterceptConstants.COLUMN_TYPE + " = ?";
        //Cursor cursor = context.getContentResolver().query(InterceptConstants.CONTENT_URI, null,
               // selection, new String[]{ ""+type }, null);
       // while (cursor.moveToNext()) {
           // String address = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));
            if (type == InterceptConstants.BLOCK_TYPE_BLACK) {
                //Blocker.deleteBlackNumberFromCache(context, address);
               // mLewaFilter.deleteBlackNumberFromCache(context, address);
                BlackListDao.getInstance(context).clearAll();
            } else if (type == InterceptConstants.BLOCK_TYPE_WHITE) {
                //Blocker.deleteWhiteNumberFromCache(context, address);
                //mLewaFilter.deleteWhiteNumberFromCache(context, address);
                WhiteListDao.getInstance(context).clearAll();
            }
        //}               
       // cursor.close();
                
    }
        // End

    private void insert(Context context, int type, ContentValues contentValues) {
        Uri uri = null;
        ContentResolver resolver = context.getContentResolver();

        switch (type) {
            case TYPE_CALL:
                try {
                    uri = resolver.insert(InterceptConstants.CALL_CONTENT_URI, contentValues);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TYPE_SMS:
                try {
                    uri = resolver.insert(InterceptConstants.MSG_CONTENT_URI, contentValues);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TYPE_BLACK:
                try {
                    uri = resolver.insert(InterceptConstants.CONTENT_URI, contentValues);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    /**
     * phone intercept
     * 
     * @param incomingNumber
     */
    public void interceptCall(Context context, String incomingNumber, String cause) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(InterceptConstants.COLUMN_CALL_NAME, "");
        contentValues.put(InterceptConstants.COLUMN_CALL_ADDRESS, incomingNumber);
        contentValues.put(InterceptConstants.COLUMN_CALL_LOCATION, "");
        contentValues.put(InterceptConstants.COLUMN_CALL_BLOCKTYPE, "");
        contentValues.put(InterceptConstants.COLUMN_CALL_CAUSE, cause);
        contentValues.put(InterceptConstants.COLUMN_CALL_DATE, new Date().getTime());
        contentValues.put(InterceptConstants.COLUMN_CALL_READ, STATE_READ);

        insert(context, TYPE_CALL, contentValues);
    }

    public void interceptMsg(Context context, String incomingNumber, String body) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(InterceptConstants.COLUMN_MSG_NAME, "");
        contentValues.put(InterceptConstants.COLUMN_MSG_ADDRESS, removePrefix(incomingNumber));
        contentValues.put(InterceptConstants.COLUMN_MSG_LOCATION, "");
        contentValues.put(InterceptConstants.COLUMN_MSG_SUBJECT, "");
        contentValues.put(InterceptConstants.COLUMN_MSG_BODY, body);
        contentValues.put(InterceptConstants.COLUMN_MSG_DATE, new Date().getTime());
        contentValues.put(InterceptConstants.COLUMN_MSG_READ, STATE_READ);
        contentValues.put(InterceptConstants.COLUMN_MSG_TYPE, SMS_TYPE_INBOX);

        insert(context, TYPE_SMS, contentValues);
    }


    /**
     * 
     * @param context
     * @param addressList
     * @param type
     *            1 black , 2 white
     * @return
     */
    public void deleteFromBlackOrWhiteList(Context context, String[] addressList, String type) {
        mQueryHandler = new SmsListQueryHandler(context.getContentResolver(), context);
        String selection = InterceptConstants.COLUMN_NUMBER + " =  ? and " + InterceptConstants.COLUMN_TYPE + " = ?";
        Uri uri = InterceptConstants.CONTENT_URI;
        if (addressList.length == 0) {
            return;
        }
        for (String address : addressList) {
            if (DBUG) {
                Log.d(TAG, "address:" + address);
            }
            address = removePrefix(address);
            startDeleteMessages(mQueryHandler, uri, selection, new String[] { address,type }, address,
                    DELETE_FROM_BLACKLIST);
        }
    }

    /**
     * 
     * @param context
     * @param addressList
     * @param type
     *            1 is delete sms , 0 is not
     * @return
     */
    public void addToBlackList(Context context, String[] addressList, String name, int type) {
        mQueryHandler = new SmsListQueryHandler(context.getContentResolver(), context, name, type);
        String selection = "address = ? or address = ?";
        String addressPrefix = null;
        String addressNoPrefix = null;
        Uri uri = MSG_INBOX_URI;
        if (addressList.length == 0) {
            return;
        }

        for (String address : addressList) {
            if (DBUG) {
                Log.d(TAG, "type:" + type);
                Log.d(TAG, "address:" + address);
            }
            address = address.contains("-") ? address.replaceAll("-", "") : address;
            if (address.contains("+86")) {
                addressPrefix = address;
                address = address.replace("+86", "");
                addressNoPrefix = address;
            } else {
                addressNoPrefix = address;
                addressPrefix = "+86" + address;
            }
            if (DBUG) {
                Log.d(TAG, "addressPrefix:" + addressPrefix);
                Log.d(TAG, "address:" + address);
            }
            String[] addressParams = new String[] { addressNoPrefix, addressPrefix };
            if (type == 0) {
                addToBlackOrWhiteList(context,address,name,InterceptConstants.BLOCK_TYPE_BLACK);
            } else if (type == 1) {
                startQueryMessages(mQueryHandler, uri, selection, addressParams, addressParams,
                        QUERY_SMS_BY_ADDRESS_TO_BLACKLIST);
            }
        }
    }

    /**
     * 
     * @param context
     * @param addressList
     * @param type
     *            1 is delete sms , 0 is not
     * @return
     */
    public void addToWhiteList(Context context, String[] addressList, String name) {
        //mQueryHandler = new SmsListQueryHandler(context.getContentResolver(), context, name, type);
        if (addressList.length == 0) {
            return;
        }

        for (String address : addressList) {
            if (DBUG) {
                Log.d(TAG, "address:" + address);
            }
            address = address.contains("-") ? address.replaceAll("-", "") : address;
            if (address.contains("+86")) {
                address = address.replace("+86", "");
            }
            if (DBUG) {
                Log.d(TAG, "address:" + address);
            }
            addToBlackOrWhiteList(context,address,name,InterceptConstants.BLOCK_TYPE_WHITE);
        }
    }

    public void addToBlackOrWhiteList(Context context, String number, String name,int type) {
        String selection = InterceptConstants.COLUMN_NUMBER + " = ?";
        Cursor cursor = context.getContentResolver().query(InterceptConstants.CONTENT_URI, null,
                selection, new String[] { number }, null);
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            int oriType = cursor.getInt(cursor.getColumnIndex(InterceptConstants.COLUMN_TYPE));
            if (oriType == type) {
                Toast.makeText(context, context.getResources().getString(R.string.hasblockwarning),                    
                    Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(InterceptConstants.COLUMN_TYPE, type);
                context.getContentResolver().update(InterceptConstants.CONTENT_URI, values, selection, new String[] { number });
                switch(type){
                case InterceptConstants.BLOCK_TYPE_BLACK:
                    insertBlackNumber2Cache(context,number,InterceptConstants.BLOCK_TYPE_NUMBER_DEFAULT);
                    deleteWhiteNumberFromCache(context, number);
                    break;
                case InterceptConstants.BLOCK_TYPE_WHITE:
                    insertWhiteNumber2Cache(context,number);
                    deleteBlackNumberFromCache(context, number);
                    break;
                }
                Toast.makeText(context, context.getResources().getString(R.string.addBlockSuccessed),
                    Toast.LENGTH_SHORT).show();
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_NAME, name);
            values.put(InterceptConstants.COLUMN_NUMBER, number);
            values.put(InterceptConstants.COLUMN_TYPE, type);
            values.put(InterceptConstants.COLUMN_MODE, InterceptConstants.BLOCK_TYPE_NUMBER_DEFAULT);
            values.put(InterceptConstants.COLUMN_PRIVACY, InterceptConstants.DEFAULT_BLOCK_PRIVACY);
            insert(context, TYPE_BLACK, values);

            switch(type){
                case InterceptConstants.BLOCK_TYPE_BLACK:
                    insertBlackNumber2Cache(context,number,InterceptConstants.BLOCK_TYPE_NUMBER_DEFAULT);
                    break;
                case InterceptConstants.BLOCK_TYPE_WHITE:
                    insertWhiteNumber2Cache(context,number);
                    break;
            }
            Toast.makeText(context, context.getResources().getString(R.string.addBlockSuccessed),
                    Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    // intercept switch
    public boolean interceptSwitch(Context context) {
        // 0 is close,1 is open;
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.INTERCEPT_SWITCH, 1) == 1;
    }

    // intercept notification switch
    public boolean interceptNotifySwitch(Context context) {
        // 0 is close,1 is open;
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.INTERCEPT_NOTIFICATION_SWITCH, 1) == 1;
    }

    public boolean notificationSwitch(Context context) {
        // 0 is close,1 is open;
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.INTERCEPT_NOTIFICATION_SWITCH, 1) == 1;
    }

    public boolean interceptOneRingSwitch(Context context) {
        // 0 is close,1 is open;
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.INTERCEPT_ONERING_SWITCH, 1) == 1;
    }
    public String removePrefix(String phoneNum) {
        phoneNum = phoneNum.replaceAll("-", "");
        if (phoneNum.contains("+86")) {
            phoneNum = phoneNum.replace("+86", "");
        }
        return phoneNum;
    }

    public static void startQueryMessages(AsyncQueryHandler handler, Uri uri, String selection, String[] selectionArgs, Object cookie, int token) {
        handler.startQuery(token, cookie, uri, null, selection, selectionArgs, null);
    }

    public static void startDeleteMessages(AsyncQueryHandler handler, Uri uri, String selection, String[] selectionArgs, Object cookie, int token) {
        handler.startDelete(token, cookie, uri, selection, selectionArgs);
    }

    // modify by chenhengheng, add incoming call is or not be intercepted, 2012-03-01
    public void block(Call c, Phone phone, String number) {
        if (interceptSwitch(phone.getContext())) {
            try {
                if (isBlockCall(phone.getContext(), number)) {
                    if (DBUG) {
                        Log.d(TAG, "intercept number:" + number);
                    }
                    c.hangup();
                    interceptCall(phone.getContext(), number, " ");
                    if (interceptNotifySwitch(phone.getContext())) {
                        Intent notifiyIntent = new Intent(InterceptConstants.LEWA_INTERCEPT_NOTIFICATION_ACTION);
                        notifiyIntent.putExtra("callCount", 1);
                        notifiyIntent.putExtra("number", number);
                        phone.getContext().sendBroadcast(notifiyIntent);
                    }
                    return;
                }
                if (DBUG)
                    Log.i(TAG, "Reject the incoming call in BL:" + number);
            } catch (Exception e) {
                // ignore
            }
        }

    }
    //start ,added by yuzhijian 20120716
    public boolean oneringblock(Call c, Phone phone, String number) {
        boolean flag = false;

        if (interceptSwitch(phone.getContext()) && interceptOneRingSwitch(phone.getContext())) {
            try {
                if (mLewaFilter.isInBlockTime(phone.getContext())
                        && !WhiteListDao.getInstance(phone.getContext())
                        .contains(number, 0) && (getContactId(phone.getContext(), "", number)==0)) {
                    c.hangup();
                    flag = true;
                }
                
            } catch (Exception e) {
                // ignore
            }
        }
        
        return flag;

    }
    //end

    private final class SmsListQueryHandler extends AsyncQueryHandler {
        Context mContext;
        int type = 0;
        String name;

        public SmsListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        public SmsListQueryHandler(ContentResolver contentResolver, Context context) {
            super(contentResolver);
            this.mContext = context;
        }

        public SmsListQueryHandler(ContentResolver contentResolver, Context context, String name, int type) {
            super(contentResolver);
            this.type = type;
            this.name = name;
            this.mContext = context;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case QUERY_SMS_BY_ADDRESS_TO_BLACKLIST:
                    if (DBUG) {
                        Log.d(TAG, "onQueryComplete");
                    }
                    String[] addressParmas = (String[]) cookie;
                    String address = addressParmas[0];
                    long threadId = 0l;
                    while (cursor.moveToNext()) {
                        ContentValues contentValues = new ContentValues();
                        String body = cursor.getString(cursor.getColumnIndex("body"));
                        threadId = cursor.getLong(1);
                        int read = cursor.getInt(cursor.getColumnIndex("read"));
                        int msg_type = cursor.getInt(cursor.getColumnIndex("type"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));

                        if (DBUG) {
                            Log.d(TAG, "threadId:" + threadId);
                            Log.d(TAG, "read:" + read);
                            Log.d(TAG, "date:" + date);
                        }
                        contentValues.put(InterceptConstants.COLUMN_MSG_NAME, "");
                        contentValues.put(InterceptConstants.COLUMN_MSG_ADDRESS,
                                removePrefix(address));
                        contentValues.put(InterceptConstants.COLUMN_MSG_LOCATION, "");
                        contentValues.put(InterceptConstants.COLUMN_MSG_SUBJECT, "");
                        contentValues.put(InterceptConstants.COLUMN_MSG_BODY, body.toString());
                        contentValues.put(InterceptConstants.COLUMN_MSG_DATE, date);
                        contentValues.put(InterceptConstants.COLUMN_MSG_READ, read);
                        contentValues.put(InterceptConstants.COLUMN_MSG_TYPE, msg_type);

                        mContentValuesList.add(contentValues);
                    }

                    Uri mUri = Uri.parse("content://sms/conversations/" + threadId);
                    if (DBUG) {
                        Log.d(TAG, "onQueryComplete threadId:" + threadId);
                        Log.d(TAG, "onQueryComplete address:" + address);
                    }

                    if (type == 1) {
                        startDeleteMessages(this, mUri, "(address = ? or address = ?) and type = "
                            + Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX,
                            addressParmas, address, DELETE_SMS_FROM_SMS);
                    }
                    break;
                case QUERY_FROM_BLACKLIST:
                    break;
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            String address = cookie.toString();
            switch (token) {
                case DELETE_SMS_FROM_SMS:
                    if (DBUG) {
                        Log.i(TAG, "onDeleteComplete result:" + result);
                    }
                    address = cookie.toString();
                    if (type == 1) {// type 1 is delete sms , 0 is not
                        if (mContentValuesList.size() > 0) {
                            for (ContentValues contentValues : mContentValuesList) {
                                insert(mContext, TYPE_SMS, contentValues);
                            }
                        }
                    }
                    addToBlackOrWhiteList(mContext,address,name,InterceptConstants.BLOCK_TYPE_BLACK);
                    mContentValuesList.clear();
                    break;
                case DELETE_FROM_BLACKLIST:
                    if (result > 0) {
                        address = cookie.toString();
                        // delete success
                        deleteBlackNumberFromCache(mContext, address);
                        Toast.makeText(mContext,
                                mContext.getResources().getString(R.string.delBlockSuccessed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext,
                                mContext.getResources().getString(R.string.delBlockFailed, address),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    public static long getContactId(Context context, String name, String number) {
        String strWhere = null;
        if (!TextUtils.isEmpty(name)) {
            strWhere = Contacts.DISPLAY_NAME + "='" + name + "'";
        }
        else if (!TextUtils.isEmpty(number)) {
            number = PhoneNumberUtils.stripSeparators(number);
            if (TextUtils.isEmpty(number)) {
                return 0;
            }

            String strippedNumber = "REPLACE(TRIM(" + CommonDataKinds.Phone.NUMBER + "), '-', '')";
            number = stripNumberSpecialPrefix(number);
            if ('+' == number.charAt(0)) {
                strWhere = strippedNumber + "='" + number + "'"
                        + " OR "
                        + strippedNumber + "='" + number.substring(getCountryCodeLength(number)) + "'";
            }
            else {
                String chinaCallCode = getChinaCallCode();
                String ipPrefix = null;//getIpPrefix(context, SimCard.GEMINI_SIM_1);
                if (TextUtils.isEmpty(ipPrefix)) {
                    strWhere = strippedNumber + "='" + number + "'"
                            + " OR "
                            + strippedNumber + "='" + chinaCallCode + number + "'";
                }
                else {
                    if (number.startsWith(ipPrefix)) {
                        strWhere = strippedNumber + "='" + number + "'"
                                + " OR "
                                + strippedNumber + "='" + number.substring(ipPrefix.length()) + "'";
                    }
                    else {
                        strWhere = strippedNumber + "='" + number + "'"
                                + " OR "
                                + strippedNumber + "='" + chinaCallCode + number + "'";
                    }
                }
            }
        }
        else {
            return 0;
        }
        //log("getContactId: name=" + name + " number=" + number + " where=" + strWhere);
        
        long contactId = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {Contacts.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.CONTACT_ID},
                    strWhere,
                    null,
                    null);
            if ((null != cursor) && cursor.moveToFirst()) {
                contactId = cursor.getLong(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.CONTACT_ID));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != cursor) {
                cursor.close();
            }
        }

        return contactId;
    }
    private static int getCountryCodeLength(String number) {
        if (number.length() >= MIN_NUMBER_LENGTH_INCLUDE_COUNTRY_CODE) {
            for (String countryCallCode : COUNTRY_CALL_CODES) {
                if (number.startsWith(countryCallCode)) {
                    return countryCallCode.length();
                }
            }
        }

        return 0;
    }
    
    public static final String stripNumberSpecialPrefix(String number) {
        if (number.length() >= 12) { //consider the min length of the number with special prefix  is 12
            for (String prefix : SPECIAL_PREFIX_IN_NUMBER) {
                if (number.startsWith(prefix)) {
                    return number.substring(prefix.length());
                }
            }
        }

        return number;
    }
    
    public static final String getChinaCallCode() {
        return "+86";
    }

    public void check(Context context) {
        mUpdateManager = ManagerCreator.getManager(UpdateManager.class);
        mContext = context;
        mHandler = new UpdateHanlder();
        checkUpdatelist();
    }

    private void checkUpdatelist() {
        Intent intent = new Intent("update_intercept_dialog");
        intent.putExtra("type", "startCheck");
        mContext.sendBroadcast(intent);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mUpdateManager.check(UpdateConfig.UPDATE_FLAG_SMS_CHECKER, new ICheckListener() {
                    @Override
                    public void onCheckEvent(int arg0) {
                        Message msg = Message.obtain(mHandler, MSG_NETWORK_ERROR);
                        msg.arg1 = arg0;
                        msg.sendToTarget();
                    }

                    @Override
                    public void onCheckStarted() {
                    }

                    @Override
                    public void onCheckCanceled() {
                    }
                    @Override
                    public void onCheckFinished(CheckResult result) {
                        mCheckResults = result;
                    }
                });
                mHandler.sendEmptyMessage(MSG_HIDE_CHECK_PROGRESS);
            }
        }).start();
    }

    public void updateData() {
       if (mCheckResults != null && mCheckResults.mUpdateInfoList!= null
                && mCheckResults.mUpdateInfoList.size() > 0) {
            Intent intent = new Intent("update_intercept_dialog");
            intent.putExtra("type", "startUpdate");
            mContext.sendBroadcast(intent);
             new Thread(new Runnable() {
                @Override
                public void run() {
                    if(null == mCheckResults) return;
                    mUpdateManager.update(mCheckResults.mUpdateInfoList,
                            new IUpdateListener() {
                                @Override
                                public void onProgressChanged(UpdateInfo arg0,
                                        int arg1) {
                                    Message msg = Message.obtain(mHandler,
                                            MSG_UPDATE_PROGRESS);
                                    msg.obj = arg0;
                                    msg.arg1 = arg1;
                                    msg.sendToTarget();
                                }

                                @Override
                                public void onUpdateEvent(UpdateInfo arg0,
                                        int arg1) {
                                    Message msg = Message.obtain(mHandler,
                                            MSG_NETWORK_ERROR);
                                    msg.arg1 = arg1;
                                    msg.sendToTarget();
                                }

                                @Override
                                public void onUpdateFinished() {
                                }

                                @Override
                                public void onUpdateStarted() {
                                }
                                @SuppressWarnings("unused")
                                public void onUpdateCanceled() {

                                }
                            });
                    mHandler.sendEmptyMessage(MSG_HIDE_UPDATE_PROGRESS);
                }
            }).start();
        } else {
            mHandler.sendEmptyMessage(MSG_NONEED_TOUPDATE);
        }
    }

    private class UpdateHanlder extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_HIDE_CHECK_PROGRESS:
                Intent intent = new Intent("update_intercept_dialog");
                intent.putExtra("type", "endCheck");
                if (mCheckResults != null && mCheckResults.mUpdateInfoList!= null
                        && mCheckResults.mUpdateInfoList.size() > 0) {
                    intent.putExtra("needToUpdate", true);
                } else {
                    intent.putExtra("needToUpdate", false);
                }
                mContext.sendBroadcast(intent);
                break;
            case MSG_HIDE_UPDATE_PROGRESS:
                intent = new Intent("update_intercept_dialog");
                intent.putExtra("type", "endUpdate");
                mContext.sendBroadcast(intent);
//                Toast.makeText(mContext, mContext.getResources().getString(R.string.updateSuccess), Toast.LENGTH_LONG).show();
                break;
            case MSG_UPDATE_PROGRESS:
//               Toast.makeText(mContext, mContext.getResources().getString(R.string.updateProgress), Toast.LENGTH_LONG).show();
                break;
            case MSG_NETWORK_ERROR:
                    Toast.makeText(mContext,  mContext.getResources().getString(R.string.networkError), Toast.LENGTH_LONG).show();
                break;
            case MSG_NONEED_TOUPDATE:
                Toast.makeText(mContext, mContext.getResources().getString(R.string.noNeedToUpdate), Toast.LENGTH_LONG).show();
                break;
            }
        }
    };

}
