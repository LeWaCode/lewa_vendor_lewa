/*
 * Copyright (c) 2011 LewaTek
 * All rights reserved.
 * 
 * DESCRIPTION:
 *
 * WHEN          | WHO               | what, where, why
 * --------------------------------------------------------------------------------
 * 2011-08-29  | GanFeng          | Create file
 */

package com.lewa.PIM.dialpad.data;

import java.util.Date;
import java.util.List;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.lewa.os.filter.FilterItem;
import com.lewa.os.util.Util;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.calllog.data.CallLog;
import com.lewa.PIM.util.CommonMethod;

import android.util.Log;

public final class DialpadItem extends FilterItem {
    public static final int CALLLOG_TYPE = 0;
    public static final int CONTACT_TYPE = 1;
    
    private int      mType;
    private long     mContactId;
    private long     mPhotoId;
    private String   mName;
    private String   mNumber;
    private String[] mAlphabetNames;
    //add by zenghuying
    private int mContactedTimes;
    private boolean mIsSpecialNum;
    /* 
        * public static final int INCOMING_TYPE = 1;
        * public static final int OUTGOING_TYPE = 2;
        * public static final int MISSED_TYPE = 3;
        */
    private final int CALL_TYPE_COUNT = 4;
    private boolean[] mCallTypeArr = {false, false, false, false};
    private int[]     mCallTypeSize = {0, 0, 0, 0};
    private int      mCallType = -1; //e.g Calls.MISSED_TYPE
    private Date     mDate;
    private int      mSize = 0;
    private String   mLocation;

    public DialpadItem(String name, String number) {
        mType = CONTACT_TYPE;
        mName = name;
        mNumber = number;
        mSize = 1;
    }
    //add by zenghuaying for special number
    public DialpadItem(String name, String number,String[] alphabetNames,boolean isSpecialNum){
        mType = CONTACT_TYPE;
        mIsSpecialNum = isSpecialNum;
        mName = name;
        mNumber = number;
        mAlphabetNames = alphabetNames;
        mSize = 1;
    }
    //add end

    public DialpadItem(String name, String[] alphabetNames, String number, long contactId, long photoId,int contactedTimes) {
        mType = CONTACT_TYPE;
        mContactId = contactId;
        mPhotoId = photoId;
        mName = name;
        mAlphabetNames = alphabetNames;
        mNumber = number;
        mSize = 1;
        mContactedTimes = contactedTimes;
    }

    public DialpadItem(CallLogGroup clGroup) {
        mType = CALLLOG_TYPE;
        mContactId = clGroup.getContactId();
        mPhotoId = clGroup.getPhotoId();
        mName = clGroup.getName();
        mNumber = clGroup.getNumber();
        //setCallType(clGroup.getType(), clGroup.getSize());
        mDate = clGroup.getDate();
        //mSize = clGroup.getSize();
        CallLog log;
        for (int ii = 0; ii < clGroup.getSize(); ii++) {
            log = clGroup.getLog(ii);
            setCallType(log.getType(), 1);
        }
    }

    public static final DialpadItem createLogTypeItem(String name, String number, int callType, long timestamp) {
        DialpadItem logTypeItem = new DialpadItem(null, number);
        logTypeItem.mType = CALLLOG_TYPE;
        logTypeItem.mContactId = 0;
        logTypeItem.mPhotoId = 0;
        logTypeItem.mName = name;
        logTypeItem.mNumber = number;
        logTypeItem.setCallType(callType, 0);
        logTypeItem.mDate = new Date(timestamp);
        logTypeItem.mSize = 1;
        return logTypeItem;
    }
    
    //add by zenghuaying fix bug #8908
    public void setName(String name){
        mName = name;
    }
    //add end
    public int getSize() {
        return mSize;
    }

    public int getCallTypeSize(int callType) {
        if (callType < CALL_TYPE_COUNT) {
            //Log.e("DialpadItem", "getCallTypeSize   callType=" + callType);
            return mCallTypeSize[callType];
        }
        return 0;
    }

    public int getType() {
        return mType;
    }

    public long getContactId() {
        return mContactId;
    }

    public void setContactId(long contactId) {
        mContactId = contactId;
    }

    public long getPhotoId() {
        return mPhotoId;
    }
    //add by zenghuaying
    public int getContactedTimes() {
        return mContactedTimes;
    }
    
    public boolean getIsSpecialNum(){
        return mIsSpecialNum;
    }
    
    public void setType(int type){
        mType = type;
    }
    //add end
    public void setPhotoId(long photoId) {
        mPhotoId = photoId;
    }

    public String getName() {
        return mName;
    }

    public String getNumber() {
        return mNumber;
    }

    public boolean hasCallType(int callType) {
        if (callType < CALL_TYPE_COUNT) {
            //Log.e("DialpadItem", "hasCallType   callType=" + callType);
            return mCallTypeArr[callType];
        }
        return false;
    }

    public int getCallType() {
        return mCallType;
    }

    public int setCallType(int callType, int size) {
        if (callType < CALL_TYPE_COUNT) {
            //Log.e("DialpadItem", "setCallType  callType=" + callType);
            mCallTypeArr[callType] = true;
            if (-1 == mCallType) {
                mCallType = callType;
            }
            mCallTypeSize[callType] += size;
            mSize += size;
            return mCallType;
        }
        return -1;
    }
    
    public Date getDate() {
        return mDate;
    }

    public void setDate(long timestamp) {
        if (null != mDate) {
            mDate.setTime(timestamp);
        }
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public static DialpadItem findLogTypeDialpad(List<DialpadItem> logTypeDialpads, String nameToFind, String numberToFind) {
        DialpadItem item = null;
        int nCount = logTypeDialpads.size();
        for (int i = 0; i < nCount; ++i) {
            item = logTypeDialpads.get(i);

            final String number = item.getNumber();
            //add by zenghuaying fix bug #8367
            final String name = item.getName();
            if(!TextUtils.isEmpty(name)){
                if (!TextUtils.isEmpty(nameToFind)) {
                    if(name.equals(nameToFind)){
                        break;
                    }
                }
            }else if(!TextUtils.isEmpty(number)) {
            //add end    
            //if (!TextUtils.isEmpty(number)) {
                if (!TextUtils.isEmpty(numberToFind)) {
                    //add by zenghuaying
                    if(CommonMethod.isStartWithIpPrefix(numberToFind)){
                        numberToFind = numberToFind.substring(5);
                    }
                    
                    if(number.contains(numberToFind)){
                    //add end
                    //if (PhoneNumberUtils.compare(number, numberToFind)) {
                        break;
                    }
                }
            }

            item = null;
        }

        return item;
    }

    @Override
    public int getFilterCount() {
        //name and number will take part in the filter, and only search the unknown number
        return ((CALLLOG_TYPE == mType)? ((null == mName)? 2 : 0) : 2);
        //return ((CALLLOG_TYPE == mType)? 0 : 2);
    }

    @Override
    public String getFilterContent(int field) {
        if (FilterItem.NAME_FIELD == field) {
            return mName;
        }
        else if (FilterItem.NUMBER_FIELD == field) {
            return mNumber;
        }
        else {
            return null;
        }
    }

    @Override
    public int getFilterMode(int field) {
        if (FilterItem.NAME_FIELD == field) {
            return FilterItem.DIGIT_MATCH_MODE;
        }
        else {
            return FilterItem.EXACT_MATCH_MODE;
        }
    }

    @Override
    public String[] getFilterAlphabetContents(int field) {
        if (FilterItem.NAME_FIELD == field) {
            return mAlphabetNames;
        }
        return null;
    }
}
