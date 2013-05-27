package com.lewa.intercept;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

public class GetContactName {
    public static final int MIN_NUMBER_LENGTH_INCLUDE_COUNTRY_CODE = 11;
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
    public static String getContactName(Context context, String name, String number) {
        String strWhere = null;
        if (!TextUtils.isEmpty(name)) {
            strWhere = Contacts.DISPLAY_NAME + "='" + name + "'";
        }
        else if (!TextUtils.isEmpty(number)) {
            number = PhoneNumberUtils.stripSeparators(number);
            if (TextUtils.isEmpty(number)) {
                return null;
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
                String ipPrefix = getIpPrefix(context, 0);
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
            return null;
        }
        //Log.d(TAG, "getContactId: name=" + name + " number=" + number + " where=" + strWhere);
        
        long contactId = 0;
        String contactName = "";
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
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
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

        return contactName;
    }  
    public static final String getChinaCallCode() {
        return "+86";
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
    private static final String[] SPECIAL_PREFIX_IN_NUMBER = new String[] {
        "12520026",
        "12520"
    };
    
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
    
    public static final String getIpPrefix(Context context, int simId) {
        SharedPreferences prefs = context.getSharedPreferences("ip_call_settings", Context.MODE_WORLD_READABLE);
        if (1 == simId) {
            return prefs.getString("sim2_ip_prefix", null);
        }
        else {
            return prefs.getString("sim1_ip_prefix", null);
        }
    }
}
