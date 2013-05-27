/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.os.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.net.Uri;
import android.content.Context;

public class LocationUtil {
    public static final String PHONE_TYPE_FIXED_LINE = Resources.getSystem().getString(com.android.internal.R.string.fixed_line);
    private static final String AUTHORITY = "com.lewa.providers.location";
    private static final Uri LOCATION_URI = Uri.parse("content://" + AUTHORITY + "/location");
    private static final Uri CARDTYPE_URI = Uri.parse("content://" + AUTHORITY + "/cardType");
    private static final Uri AREACODE_URI = Uri.parse("content://" + AUTHORITY + "/areacode");
    private static final Uri SPECIAL_PHONE_URI = Uri.parse("content://" + AUTHORITY + "/special_phone");
    private static final String PHONE_NO = "number";
    private static final String CARDTYPE = "cardType";
    private static final String LOCATION = "location";
    private static final String AREACODE = "areacode";

    private static String filterPhoneNo(String strPhoneNo) {            
        if (strPhoneNo == null) {
            return null;
        }

        strPhoneNo = strPhoneNo.replaceAll("\\-","");
        strPhoneNo = strPhoneNo.replaceAll("\\ ","");
        strPhoneNo = strPhoneNo.replaceAll("\\*","");
        //delete phone No prefix
        if (strPhoneNo.startsWith("+86")) {   
            strPhoneNo = strPhoneNo.substring(3);                    
        }
        else if (strPhoneNo.startsWith("17951")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if (strPhoneNo.startsWith("12593")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if (strPhoneNo.startsWith("17911")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if (strPhoneNo.startsWith("10193")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if (strPhoneNo.startsWith("17909")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if (strPhoneNo.startsWith("17908")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if (strPhoneNo.startsWith("96688")) {
            strPhoneNo = strPhoneNo.substring(5);
        }
        else if(strPhoneNo.startsWith("11808")){
	    strPhoneNo = strPhoneNo.substring(5);	
	}

        if(!isNumeric(strPhoneNo)) {
            return null;
        }
        return strPhoneNo;
    }

    private static boolean isNumeric(String str){
	    return str.matches("\\d*"); 
	}      

    private static String getPhoneNoKey(String phoneNoStr) {
        String strPhoneNoFilter = filterPhoneNo(phoneNoStr);
        if (strPhoneNoFilter == null) {
            return null;
        }
            
        if (strPhoneNoFilter.startsWith("1") && strPhoneNoFilter.length() >= 7) {
            return strPhoneNoFilter.subSequence(0, 7).toString();
        } else if (strPhoneNoFilter.startsWith("0") && strPhoneNoFilter.length() >= 3 && strPhoneNoFilter.charAt(1) <= '2') {
           return strPhoneNoFilter.subSequence(0, 3).toString();
        }else if (strPhoneNoFilter.startsWith("0") && strPhoneNoFilter.length() >= 4 ) {
           return strPhoneNoFilter.subSequence(0, 4).toString();
    	 }else  {		
           return null;
    	 }
    }

    private static String[] getPhoneLocationInternal(Context context,String phoneNo) {   
        String[] phoneLocation = new String[2];
        Cursor cursor = null;
        String selStrings = null;
        ContentResolver cr = context.getContentResolver();

        if (phoneNo != null) {
            selStrings = PHONE_NO + "='" + phoneNo + "'";
            try {
                cursor = cr.query(SPECIAL_PHONE_URI,
                        new String[] {LOCATION},
                        selStrings,
                        null,
                        null);
                if ((cursor != null) && cursor.moveToFirst()) {
                    phoneLocation[0] = cursor.getString(cursor.getColumnIndex(LOCATION));
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
        } 

        if (null == phoneLocation[0]) {
            String phoneNoKey = getPhoneNoKey(phoneNo);
            if (null == phoneNoKey) {
                return null;
            }

            cursor = null;
            try {
            	Uri uri = Uri.parse("content://" + AUTHORITY + "/location/" + phoneNoKey);
                    cursor = cr.query(uri,
                            null,
                            null, 
                            null,
                            null);

                if ((cursor != null) && cursor.moveToFirst()) {
                    phoneLocation[0] = cursor.getString(cursor.getColumnIndex(LOCATION));
                    phoneLocation[1] = cursor.getString(cursor.getColumnIndex(CARDTYPE));
                    
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
        }	
        return phoneLocation;
    }

    
    public static String getPhoneLocation(Context context,String phoneNo) {
    	String[] phoneLocation = getPhoneLocationInternal(context,phoneNo);
    	if(phoneLocation != null){
    		return phoneLocation[0];
    	}
    	else {
    		return null;
    	}
    }

    public static String getPhoneCardType(Context context, String phoneNo) {
    	String[] phoneLocation = getPhoneLocationInternal(context,phoneNo);
    	if(phoneLocation != null){
    		return phoneLocation[1];
    	}
    	else {
    		return null;
    	}
    }

    public static String[] getPhoneLocationAndCardType(Context context,String phoneNo) {   
    	String[] phoneLocation = getPhoneLocationInternal(context,phoneNo);
    	return phoneLocation;
    }
    
    public static String getSpecialPhone(Context context,String phoneNo) {   
        String phoneLocation = null;
        Cursor cursor = null;
        ContentResolver cr = context.getContentResolver();
        if (phoneNo != null) {
            try {
                cursor = cr.query(SPECIAL_PHONE_URI,
                        new String[] {LOCATION},
                        PHONE_NO + "='" + phoneNo + "'",
                        null,
                        null);
                if ((cursor != null) && cursor.moveToFirst()) {
                    phoneLocation = cursor.getString(cursor.getColumnIndex(LOCATION));                    
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
        }      
        return phoneLocation;
    }    



}
        
