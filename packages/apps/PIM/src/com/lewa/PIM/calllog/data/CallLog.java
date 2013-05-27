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

package com.lewa.PIM.calllog.data;

import java.util.Date;

import android.database.Cursor;
import android.provider.CallLog.Calls;

public final class CallLog {
    public static final String[] PROJECTION;

    private long    mId;
    private String  mPeerNumber; //for multiple card mode
    private String  mNumber;
    private String  mName;
    //private String  mNumberLabel;
    private int     mNumberType;
    private int     mType;
    private boolean mNew;
    private Date    mDate;
    private long    mDuration;
    private long    mDateInd;
    private long    mContactId;
    private long    mPhotoId;

    static {
        PROJECTION = new String[9];
        PROJECTION[0]  = Calls._ID;
        PROJECTION[1]  = Calls.NUMBER;
        PROJECTION[2]  = Calls.CACHED_NAME;
        PROJECTION[3]  = Calls.CACHED_NUMBER_LABEL;
        PROJECTION[4]  = Calls.CACHED_NUMBER_TYPE;
        PROJECTION[5]  = Calls.TYPE;
        PROJECTION[6]  = Calls.NEW;
        PROJECTION[7]  = Calls.DATE; //"MAX(" + Calls.DATE + ") as max_date"; //Calls.DATE;
        PROJECTION[8]  = Calls.DURATION;
        //PROJECTION[9]  = "((date + 28800000) / 86400000) AS date_ind"; //24 hours contains 86400000 ms, GMT8 offset 8h=(3600000 * 8)=28800000 ms
        //PROJECTION[10] = "COUNT(*) AS call_count";
    }

    public static final CallLog create(Cursor cursor) {
        CallLog calllog      = new CallLog();
        calllog.mId          = cursor.getLong(0);
        calllog.mNumber      = cursor.getString(1);
        calllog.mName        = cursor.getString(2);
        //calllog.mNumberLabel = cursor.getString(3);
        calllog.mNumberType  = cursor.getInt(4);
        calllog.mType        = cursor.getInt(5);
        calllog.mNew         = (cursor.getInt(6) > 0)? true : false;
        calllog.mDate        = new Date(cursor.getLong(7));
        calllog.mDuration    = cursor.getLong(8);
        calllog.mDateInd     = (cursor.getLong(7) - ((long )calllog.mDate.getTimezoneOffset() * 60000)) / 86400000; //cursor.getLong(9);
        calllog.mContactId   = 0;
        calllog.mPhotoId     = 0;
        return calllog;
    }

    public long getId() {
        return mId;
    }

    public String getNumber() {
        return mNumber;
    }

    public void removeNumberPrefix(String[] arrPrefix) {
        for (String prefix : arrPrefix) {
            if (mNumber != null && mNumber.startsWith(prefix)) {
                mNumber = mNumber.substring(prefix.length());
                return;
            }
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getNumberType() {
        return mNumberType;
    }

    public int getType() {
        return mType;
    }

    public Date getDate() {
        return mDate;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getDateInd() {
        return mDateInd;
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

    public void setPhotoId(long photoId) {
        mPhotoId = photoId;
    }

    public boolean isNewMissedCall() {
        return (mNew && (Calls.MISSED_TYPE == mType));
    }
}