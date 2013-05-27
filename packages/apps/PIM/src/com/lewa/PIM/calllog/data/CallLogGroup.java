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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.filter.FilterItem;
import com.lewa.os.util.Util;

public final class CallLogGroup extends FilterItem { //implements Comparable<CallLogGroup> {
    ArrayList<CallLog> mLogs;

    public CallLogGroup(CallLog cl) {
        mLogs = new ArrayList<CallLog>(3); //with 3 initial capacity
        mLogs.add(cl);
    }

    public CallLog getLog() {
        return mLogs.get(0);
    }

    public CallLog getLog(int index) {
        return mLogs.get(index);
    }

    public void addLog(CallLog cl) {
        mLogs.add(cl);
    }

    public Date getDate() {
        return mLogs.get(0).getDate();
    }

    public long getDateInd() {
        return mLogs.get(0).getDateInd();
    }

    public String getName() {
        return mLogs.get(0).getName();
    }

    public String getNumber() {
        return mLogs.get(0).getNumber();
    }

    public int getType() {
      return mLogs.get(0).getType();
    }

    public long getContactId() {
        return mLogs.get(0).getContactId();
    }

    public long getPhotoId() {
        return mLogs.get(0).getPhotoId();
    }

    public int getSize() {
        return mLogs.size();
    }

    public long[] getLogIds() {
        int count = mLogs.size();
        long[] logIds = new long[count];
        for (int i = 0; i < count; ++i) {
            logIds[i] = mLogs.get(i).getId();
        }
        return logIds;
    }

    private int compareTo(CallLogGroup clGroup) {
        Date   date   = clGroup.getDate();
        String name   = clGroup.getName();
        String number = clGroup.getNumber();
        int    type   = clGroup.getType();
        return compareTo(date, name, number, type);
    }

    private int compareTo(Date dateToCmp, String nameToCmp, String numberToCmp, int typeToCmp) {
        Date date = getDate();
        int year  = date.getYear();
        int month = date.getMonth();
        int day   = date.getDay();
        int yearToCmp  = dateToCmp.getYear();
        int monthToCmp = dateToCmp.getMonth();
        int dayToCmp   = dateToCmp.getDay();
        
        if (year > yearToCmp) {
            return 1;
        }
        else if (year < yearToCmp) {
            return -1;
        }

        if (month > monthToCmp) {
            return 1;
        }
        else if (month < monthToCmp) {
            return -1;
        }

        if (day > dayToCmp) {
            return 1;
        }
        else if (day < dayToCmp) {
            return -1;
        }

        int cmpResult = 0;
        String name   = getName();
        String number = getNumber();
        int    type   = getType();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(nameToCmp)) {
            cmpResult = name.compareTo(nameToCmp);
            if (type != typeToCmp) {
                if (0 == cmpResult) {
                    cmpResult = (type > typeToCmp)? 1 : -1;
                }
            }
            return cmpResult;
        }
        else {
            cmpResult = number.compareTo(numberToCmp);
            if (type != typeToCmp) {
                if (0 == cmpResult) {
                    cmpResult = (type > typeToCmp)? 1 : -1;
                }
            }
            return cmpResult;
        }
    }

    private int compare(Date dateToCmp, String nameToCmp, String numberToCmp, int typeToCmp) {
        int type = getType();
        if (type != typeToCmp) {
            return ((type > typeToCmp)? 1 : -1);
        }
        
        long ms = (getDate().getTime() / 86400000); //24 hours contains 86400000 ms
        long msCmp = (dateToCmp.getTime() / 86400000);
        if (ms != msCmp) {
            return ((ms > msCmp)? 1 : -1);
        }
        
        String name = getName();
        if (!TextUtils.isEmpty(name)) {
            if (!TextUtils.isEmpty(nameToCmp)) {
                return name.compareTo(nameToCmp);
            }
            else {
                return 1;
            }
        }
        else {
            if (!TextUtils.isEmpty(nameToCmp)) {
                return -1;
            }
            else {
                String number = getNumber();
                if (!TextUtils.isEmpty(number)) {
                    if (!TextUtils.isEmpty(numberToCmp)) {
                        return number.compareTo(numberToCmp);
                    }
                    else {
                        return 1;
                    }
                }
                else {
                    if (!TextUtils.isEmpty(numberToCmp)) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            }
        }
    }

    public static CallLogGroup findGroup(List<CallLogGroup> logGroups, String nameToFind, String numberToFind, long dateIndToFind, int typeToFind) {
        int nCount = logGroups.size();
        if (0 == nCount) {
            return null;
        }

        //Log.d("CallLogGroup", "findGroup: name=" + nameToFind + " number=" + numberToFind + " date=" + dateIndToFind + " type=" + typeToFind);

        //long dateInd = 0;
        //int type = 0;
        CallLogGroup clGroup = null;
        for (int i = (nCount - 1); i >= 0; --i) {
            clGroup = logGroups.get(i);

            //dateInd = clGroup.getDateInd();
            //if (dateInd != dateIndToFind) {
            //    clGroup = null;
            //    break;
            //}

            //type = clGroup.getType();
            //if (type != typeToFind) {
            //    clGroup = null;
            //    continue;
            //}

            final String name = clGroup.getName();
            if (!TextUtils.isEmpty(name)) {
                //if (!TextUtils.isEmpty(nameToFind)) {
                //    if (0 == name.compareTo(nameToFind)) {
                //        break;
                //    }
                //}
                //else {
                //    final String number = clGroup.getNumber();
                //    if (!TextUtils.isEmpty(number)) {
                //        if (!TextUtils.isEmpty(numberToFind)) {
                //            if (0 == number.compareTo(numberToFind)) {
                //                break;
                //            }
                //        }
                //    }
                //}

                if (name.equals(nameToFind)) {
                    break;
                }

                final String number = clGroup.getNumber();
                if (!TextUtils.isEmpty(number)) {
                    if (PhoneNumberUtils.compare(number, numberToFind)) {
                        break;
                    }
                }
            }
            else {
                if (TextUtils.isEmpty(nameToFind)){
                    final String number = clGroup.getNumber();
                    if (!TextUtils.isEmpty(number)) {
                        if (!TextUtils.isEmpty(numberToFind)) {
                            //add by zenghuaying fix bug #8367
                            if(CommonMethod.isStartWithIpPrefix(numberToFind)){
                                numberToFind = numberToFind.substring(5);
                            }
                            if(number.contains(numberToFind)
                            //add end
                            //if (number.equals(numberToFind) //(0 == number.compareTo(numberToFind))
                                    || (Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())
                                            && Util.isEmergencyNumber(numberToFind, PimEngine.getEmergencyNumbers()))) {
                                break;
                            }
                        }
                    }
                }
            }

            clGroup = null;
        }

        return clGroup;
    }

    @Override
    public int getFilterCount() {
        return 2; //name and number will take part in the filter
    }

    @Override
    public String getFilterContent(int field) {
        if (FilterItem.NAME_FIELD == field) {
            return getName();
        }
        else if (FilterItem.NUMBER_FIELD == field) {
            return getNumber();
        }
        else {
            return null;
        }
    }

    @Override
    public int getFilterMode(int field) {
        if (FilterItem.NAME_FIELD == field) {
            return FilterItem.APPROXIMATE_MATCH_MODE;
        }
        else {
            return FilterItem.EXACT_MATCH_MODE;
        }
    }
}