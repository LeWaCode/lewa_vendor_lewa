package com.lewa.PIM.calllog.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.lewa.PIM.engine.PimEngine;
import android.content.Context;

public class MissedCallLogInfo {
    
    private ArrayList<String> mNumbers;
    private String mName; 
    private long mContactId;
    private long mDate;
    private int mCount;
    
    public MissedCallLogInfo(long contactId, String name, String number, long date, int count){
        mName      = name;
        mContactId = contactId;
        mDate      = date;
        mCount     = count;

        mNumbers = new ArrayList<String>(3); //with 3 initial capacity
        mNumbers.add(number);
    }

    public static HashMap<Long, MissedCallLogInfo> queryMissedCallLogInfo(Context context) {
        return PimEngine.getInstance(context).loadMissedCallLogInfo(false);
    }

    public String getName() {
        return mName;
    }

    public void addNumber(String number) {
        if (!mNumbers.contains(number)) {
            mNumbers.add(number);
        }
    }
    public ArrayList<String> getNumbers() {
        return mNumbers;
    }
    
    public long getContactId() {
        return mContactId;
    }

    public void setContactId(long contactId) {
        mContactId = contactId;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }
}

