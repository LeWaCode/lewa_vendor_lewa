package com.lewa.PIM.util;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.ClassCastException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.R.integer;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.RosterData;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.QuickContactBadge;

import com.lewa.PIM.R;
import com.lewa.PIM.IM.IMClient;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.contacts.ContactsSettingsActivity.Prefs;
import com.lewa.PIM.contacts.LayoutQuickContactBadge.QCBadgeOnClickListener;
import com.lewa.os.util.LocationUtil;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.sim.SimCard;
import com.lewa.os.util.Util;

import com.android.internal.util.HanziToPinyin;
import com.android.internal.util.HanziToPinyin.Token;

public final class CommonMethod {
    private static final String TAG = "CommonMethod";
    private static int DBG_LEVEL = 2;

    private static final String YILIAO_CONTACTS_ACCOUNT_STATUS = "contacts_yiliao_account_status";
    private static final String LEWA_MSG_ON_OFF = "contacts_lewa_message_on_off";
    public static final String ACTION_LEWA_MSG_ON_OFF = "com.lewa.PIM.contacts_lewa_message_on_off";
    public static final String ACTION_LEWA_MSG_EMPTY = "com.lewa.PIM.contacts_lewa_message_empty";
    
    private static boolean mComposeMessageActivityState = false;
    private static boolean mNewMessageComposeActivityState = false;
    private static boolean mConversationListState = false;

    public static final int MIN_NUMBER_LENGTH_ALLOWED_TO_ADD_IP    = 7;
    public static final int MIN_NUMBER_LENGTH_INCLUDE_COUNTRY_CODE = 11;

    public static final String CONTENT_CONTACT_TYPE = "text/contact";

    private static final String LEWA_INTERCEPT_ADDTOBLACKLIST_ACTION = "android.provider.lewa.intercept.addToBlacklist";
    private static final String LEWA_INTERCEPT_DELETEFROMBLACKLIST_ACTION = "android.provider.lewa.intercept.deleteFromBlacklist";    
    
    private static final String[] NUMBERS_NOT_ALLOWED_TO_ADD_IP = new String[] {
        "400",
        "800",
        "95105105",
        "13800138000"
    };

    private static final String[] SPECIAL_PREFIX_IN_NUMBER = new String[] {
        "12520026",
        "12520"
    };

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
    //add by zenghuaying 2012.6.18
    private static final String[] IP_PREFIX = new String[]{
    	"17951","17911","12593","11808","10193","17909","17908","96688"
    };
    //add by zenghuaying fix bug #9947
    private static final int IP_PREFIX_LENGTH = 5;
    //add end
    /*
     * get all special phone number. 
     */
    private static final String AUTHORITY = "com.lewa.providers.location";
    private static final Uri SPECIAL_PHONE_URI = Uri.parse("content://" + AUTHORITY + "/special_phone");
    private static final String LOCATION = "location";
    private static final String PHONE_NO = "number";
    //modify by zenghuaying for Dialpad filter
    public static HashMap<String, String> mSpecialPhoneNameMap = new HashMap<String, String>();
    
    public final static class ContactInfo {
        public String mName;
        public String mNumber;
    }

    public enum EnmSimOperator {
        UNKNOWN,
        CHINA_MOBILE,
        CHINA_UNICOM,
        CHINA_TELECOM
    }
    public static class ContactInfoEntry {
        String data;
        boolean isPrimary;
        
        public String GetData(){
            return data;
        }
        
        public boolean isPrimary() {
            return isPrimary;
        }
    }

    public static class ContactInfoDetail {
        String mNname;
        ArrayList<ContactInfoEntry> mNumbers;
        ArrayList<ContactInfoEntry> mEmails;
        ArrayList<ContactInfoEntry> mAddress;

        public ContactInfoDetail() {
            mNname = null;
            mNumbers = new ArrayList<ContactInfoEntry>();
            mEmails = new ArrayList<ContactInfoEntry>();
            mAddress = new ArrayList<ContactInfoEntry>();
        }
        
        public ArrayList<ContactInfoEntry> getNumber(){
            return mNumbers;
        }
        
        public ArrayList<ContactInfoEntry> getEmails() {
            return mEmails;
        }
        
        public ArrayList<ContactInfoEntry> getAddress(){
            return mAddress;
        }
    }
    
    public static final void openDialpad(Context context, String number) {
        Uri dialUri = null;
        if (!TextUtils.isEmpty(number)) {
            dialUri = Uri.fromParts("tel", number, null);//Uri.parse("tel:" + number);
        }
        else {
            dialUri = Uri.fromParts("tel", "", null);//Uri.parse("tel:");
        }
        
        Intent dialpadIntent = new Intent("com.lewa.intent.action.DIALPAD", dialUri);
        context.startActivity(dialpadIntent);
    }

    public static final String getAutoIpCallString(Context context, String number,boolean isUsedForCall) {
        String ipPrefix = null;
        SharedPreferences prefs = context.getSharedPreferences("ip_call_settings", Context.MODE_WORLD_READABLE);
        boolean isAutoIpCallOpen = prefs.getBoolean("auto_ip_call", false);
        boolean isAllowedNumberToAddIp = isAllowedNumberToAddIp(context,number);
        if(isUsedForCall && isAutoIpCallOpen && isAllowedNumberToAddIp){
        	String imsIpCall = prefs.getString("ims_ip_call", "");
        	final String[] items = context.getResources().getStringArray(R.array.ims_ip_call_setting_items);
        	//any number choose,call anynumber will add ip prefix
        	if (imsIpCall.equals(items[1])) {
        		ipPrefix = prefs.getString("sim1_ip_prefix", null);
        		return ipPrefix;
			}
        }
        if (!isSpecialPhone(number) && isAllowedNumberToAddIp && isAutoIpCallOpen)
        {
            String myLocation = prefs.getString("my_number_location", null);
            if (!TextUtils.isEmpty(myLocation)) {
                String dstLocation = LocationUtil.getPhoneLocation(context, number);
                if (!TextUtils.isEmpty(dstLocation)) {
                    int compositeLocation = myLocation.indexOf('/');
                    if (-1 == compositeLocation) {
                        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(dstLocation);
                        StringBuilder dstLocationPinYin = new StringBuilder();
                        for (Token tmp : tokens) {
                            dstLocationPinYin.append(tmp.target);
                        }
                        if (!myLocation.equals(dstLocation) && (!myLocation.equalsIgnoreCase(dstLocationPinYin.toString()))) {
                            ipPrefix = prefs.getString("sim1_ip_prefix", null);
                        }
                    }
                    else {
                        boolean bSameLocation = false;
                        String[] arrCities = myLocation.split("/");
                        for (String city : arrCities) {
                            if (dstLocation.endsWith(city)) {
                                bSameLocation = true;
                                break;
                            }
                        }

                        if (!bSameLocation) {
                            ipPrefix = prefs.getString("sim1_ip_prefix", null);
                        }
                    }
                }
            }
        }
        return ipPrefix;
    }
    //add by zenghuaying 2012.6.28
    public static final boolean isStartWithIpPrefix(Context context,String number){
    	for(int i = 0;i<IP_PREFIX.length;i++){
    		if(number.startsWith(IP_PREFIX[i])){
    			return true;
    		}
    	}
    	//in case user set other IP prefix
    	String ipPrefix = getIpPrefix(context, SimCard.GEMINI_SIM_1);
			if(!TextUtils.isEmpty(ipPrefix)){
    		if(number.startsWith(ipPrefix)){
    			return true;
    		}
			}
    	return false;
    }
    
    public static final boolean isStartWithIpPrefix(String number){
        for(int i = 0;i<IP_PREFIX.length;i++){
            if(number.startsWith(IP_PREFIX[i])){
                return true;
            }
        }
        return false;
    }
    //add end
    
    public static final void call(Context context, String number) {
        String ipPrefix = getAutoIpCallString(context, number,true);
        Uri callUri = null;
        if (!TextUtils.isEmpty(ipPrefix)) {
            //if (!number.startsWith(ipPrefix)) {
        	//avoid user manual add IP prefix  modify by zenghuaying
        	if (!isStartWithIpPrefix(context,number)) {
                String chinaCallCode = CommonMethod.getChinaCallCode();
                number = PhoneNumberUtils.stripSeparators(number); 
                if (number.startsWith(chinaCallCode)) {
                    number = number.substring(chinaCallCode.length());
                }
                callUri = Uri.fromParts("tel", ipPrefix + number, null);//Uri.parse("tel:" + ipPrefix + number);
            }
            else {
                callUri = Uri.fromParts("tel", number, null);//Uri.parse("tel:" + number);
            }
        }
        else {
            String chinaCallCode = CommonMethod.getChinaCallCode();
            number = PhoneNumberUtils.stripSeparators(number); 
            if (number.startsWith(chinaCallCode)) {
                number = number.substring(chinaCallCode.length());
            }
            callUri = Uri.fromParts("tel", number, null);//Uri.parse("tel:" + number);
        }
        Intent callIntent;
        if (!Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())) {
            callIntent = new Intent(Intent.ACTION_CALL, callUri);
        }
        else {
            callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, callUri);
        }
        context.startActivity(callIntent);
    }

    public static final void sendMessage(Context context, String recipient, String message) {
        Uri msgUri = Uri.fromParts("smsto", recipient, null);//Uri.parse("smsto:" + recipient);
        Intent msgIntent = new Intent(Intent.ACTION_SENDTO, msgUri); //"android.intent.action.SENDTO"
        if (!TextUtils.isEmpty(message)) {
            msgIntent.putExtra("sms_body", message);
        }
        context.startActivity(msgIntent);
    }

    public static final void enterContacts(Context context) {
        Intent contactsIntent = new Intent("com.lewa.intent.action.ENTER_CONTACTS");
        context.startActivity(contactsIntent);
    }

    public static final void createContact(Context context, String number) {
        Intent newContactIntent = null;
        if (!TextUtils.isEmpty(number)) {
            newContactIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT); //"android.intent.action.INSERT_OR_EDIT");
            newContactIntent.setType("vnd.android.cursor.item/contact");
            newContactIntent.putExtra("phone", number);
        }
        else {
            newContactIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI); //"android.intent.action.INSERT"
        }
        context.startActivity(newContactIntent);
    }

    public static final void newContact(Context context, String number) {
        Intent newContactIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        if (!TextUtils.isEmpty(number)) {
            newContactIntent.setType("vnd.android.cursor.dir/contact"); //("vnd.android.cursor.item/contact");
            newContactIntent.putExtra("phone", number);
        }
        context.startActivity(newContactIntent);
    }
    public static final void newContact(Context context, String name, String number) {
        Intent newContactIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        if (!TextUtils.isEmpty(number)) {
            newContactIntent.setType("vnd.android.cursor.dir/contact"); //("vnd.android.cursor.item/contact");
            newContactIntent.putExtra("phone", number);
            if (!TextUtils.isEmpty(name)) 
                newContactIntent.putExtra("name", name);
        }
        context.startActivity(newContactIntent);
    }
    public static final void viewContact(Context context, long contactId) {
        Uri viewContactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Intent viewContactIntent = new Intent(Intent.ACTION_VIEW, viewContactUri); //"android.intent.action.VIEW"
        context.startActivity(viewContactIntent);
    }

    public static final void viewLogDetail(Context context, String name, String number) {
        Intent viewLogIntent = new Intent("com.lewa.intent.action.VIEW_LOG_DETAIL");
        if (!TextUtils.isEmpty(name)) {
            viewLogIntent.putExtra("name", name);
        }
        else if (!TextUtils.isEmpty(number)) {
            viewLogIntent.putExtra("number", number);
        }
        context.startActivity(viewLogIntent);
    }

    public static final void viewPimDetail(Context context, String name, String number, long contactId, int type) {
        Intent viewDetailIntent = new Intent("com.lewa.intent.action.VIEW_PIM_DETAIL");
        if (!TextUtils.isEmpty(name)) {
            viewDetailIntent.putExtra("name", name);
        }

        if (!TextUtils.isEmpty(number)) {
            viewDetailIntent.putExtra("number", number);
        }
        viewDetailIntent.putExtra("contact_id", contactId);
        viewDetailIntent.putExtra("type", type);
        context.startActivity(viewDetailIntent);
    }

    public static final Intent createAddToContactsIntent(String number) {
        Intent addToContactsIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        addToContactsIntent.setType("vnd.android.cursor.item/contact");
        if (!TextUtils.isEmpty(number)) {
            addToContactsIntent.putExtra("phone", number);
        }
        return addToContactsIntent;
    }

    public static final Intent createNewContactIntent(String number) {
        Intent newContactIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        if (!TextUtils.isEmpty(number)) {
            newContactIntent.setType("vnd.android.cursor.dir/contact");
            newContactIntent.putExtra("phone", number);
        }
        return newContactIntent;
    }

    public static final void cancelMissedCallNotification(Context context) {
        try {
            TelephonyManager telMgr = (TelephonyManager )context.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> clsTelMgr = Class.forName(telMgr.getClass().getName());
            Method methodGetITelephony = clsTelMgr.getDeclaredMethod("getITelephony", (Class<?>[] )null);
            methodGetITelephony.setAccessible(true);
            Object objITelephony = methodGetITelephony.invoke(telMgr, (Object[] )null);
            if (null != objITelephony)
            {
                Class<?> clsITelephony = objITelephony.getClass();
                Method methodCancelMissedCallsNotification = clsITelephony.getDeclaredMethod("cancelMissedCallsNotification", (Class<?>[] )null);
                methodCancelMissedCallsNotification.setAccessible(true);
                methodCancelMissedCallsNotification.invoke(objITelephony, (Object[] )null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //NotificationMgr.getDefault().cancelMissedCallNotification();
        //NotificationManager notifyMgr = (NotificationManager )context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notifyMgr.cancel(1); //MISSED_CALL_NOTIFICATION
    }

    public static boolean findContactByNumber(Context context, String number) {
        boolean find = false;
        long startTime = System.currentTimeMillis();
        if (!TextUtils.isEmpty(number)) {
            String[] projection = {"COUNT(*) as contact_count"}; //{Contacts.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER};
            Cursor cursor = context.getContentResolver().query(
                    CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    Contacts.IN_VISIBLE_GROUP + "=1 AND " + CommonDataKinds.Phone.NUMBER + "='" + number + "'",
                    null,
                    null);
            if (null != cursor) {
                if (cursor.moveToNext() && (cursor.getInt(0) > 0)) {
                    find = true;
                }
                cursor.close();
            }
        }
        long endTime = System.currentTimeMillis();
        log("findContactByNumber " + number + " cost " + String.valueOf(endTime - startTime) + "ms");
        return find;
    }

    public static boolean contactsIsEmpty(Context context) {
        boolean bEmpty = true;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    RawContacts.CONTENT_URI,
                    new String[] {RawContacts.CONTACT_ID},
                    Contacts.DISPLAY_NAME + " NOTNULL AND " + RawContacts.DELETED + "=0",
                    null,
                    null);
            if ((null != cursor) && (cursor.getCount() > 0)) {
                bEmpty = false;
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

        return bEmpty;
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
                String ipPrefix = getIpPrefix(context, SimCard.GEMINI_SIM_1);
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
        //log("getContactId: name=" + name + " number=" + number + " where=" + strWhere);
        
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
    
    public static final String getIpPrefix(Context context, int simId) {
        SharedPreferences prefs = context.getSharedPreferences("ip_call_settings", Context.MODE_WORLD_READABLE);
        if (SimCard.GEMINI_SIM_2 == simId) {
            return prefs.getString("sim2_ip_prefix", null);
        }
        else {
            return prefs.getString("sim1_ip_prefix", null);
        }
    }

    public static final boolean getAutoIpCall(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ip_call_settings", Context.MODE_WORLD_READABLE);
        return prefs.getBoolean("auto_ip_call", false);
    }

    public static final String getChinaCallCode() {
        return "+86";
    }

    public static final String stripNumberPrefix(Context context, String number) {
        if (!TextUtils.isEmpty(number)) {
            if ('+' == number.charAt(0)) {
                int countryCodeLen = CommonMethod.getCountryCodeLength(number);
                if (countryCodeLen > 0) {
                    return number.substring(countryCodeLen);
                }
            }
            else if (number.length() >= (MIN_NUMBER_LENGTH_ALLOWED_TO_ADD_IP + 5)) { //consider the ipprefix's length is 5
                String ipPrefix = getIpPrefix(context, SimCard.GEMINI_SIM_1);
                if (!TextUtils.isEmpty(ipPrefix) && number.startsWith(ipPrefix)) {
                    return number.substring(ipPrefix.length());
                }
            }
        }

        return number;
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

    public static final void showConfirmDlg(Context context,
            int messageId,
            int titleId,
            DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(context)
                .setMessage(messageId)
                .setTitle(titleId)
                .setPositiveButton(android.R.string.ok, clickListener)
                .setNegativeButton(android.R.string.cancel, clickListener)
                .show();
    }

    public static final void showConfirmDlg(Context context,
            String message,
            String title,
            DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, clickListener)
                .setNegativeButton(android.R.string.cancel, clickListener)
                .show();
    }

    public interface SelectorOnClickListener {
        public static final int NEW_CONTACT_ITEM     = 0;
        public static final int ADD_TO_CONTACTS_ITEM = 1;
        
        void onSelectorItemClick(int item);
    }

    public static final void openSelectorForNewOrEditContact(
            Context context,
            CharSequence number,
            final SelectorOnClickListener selectorListener) {
        DialogInterface.OnClickListener itemOnClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (0 == which) {
                    selectorListener.onSelectorItemClick(SelectorOnClickListener.NEW_CONTACT_ITEM);
                }
                else if (1 == which){
                    selectorListener.onSelectorItemClick(SelectorOnClickListener.ADD_TO_CONTACTS_ITEM);
                }
            }
        };

        Resources res = context.getResources();
        CharSequence[] items;
        if (contactsIsEmpty(context)) {
            items = new String[1];
            items[0] = res.getText(R.string.menu_newContact);
        }
        else {
            items = new String[2];
            items[0] = res.getText(R.string.menu_newContact);
            items[1] = res.getText(R.string.menu_add_to_contacts);
        }
        
        new AlertDialog.Builder(context)
                .setTitle(number)
                .setItems(items, itemOnClick)
                .setNegativeButton(android.R.string.cancel, itemOnClick)
                .show();
    }

    public static final String arrayToString(long[] longArray, String separator, boolean addQuote) {
        StringBuilder strBuilder = new StringBuilder();
        if (addQuote) {
            for (int i = 0; i < longArray.length; ++i) {
                strBuilder.append("'" + longArray[i] + ",");
                if ((i + 1) < longArray.length) {
                    strBuilder.append(separator);
                }
            }
        }
        else {
            for (int i = 0; i < longArray.length; ++i) {
                strBuilder.append(longArray[i]);
                if ((i + 1) < longArray.length) {
                    strBuilder.append(separator);
                }
            }
        }
        return strBuilder.toString();
    }

    public static final String arrayToString(ArrayList<?> array, String separator, boolean addQuote) {
        StringBuilder strBuilder = new StringBuilder();
        int count = array.size();
        if (addQuote) {
            for (int i = 0; i < count; ++i) {
                strBuilder.append("'" + array.get(i).toString() + "'");
                if ((i + 1) < count) {
                    strBuilder.append(separator);
                }
            }
        }
        else {
            for (int i = 0; i < count; ++i) {
                strBuilder.append(array.get(i).toString());
                if ((i + 1) < count) {
                    strBuilder.append(separator);
                }
            }
        }
        return strBuilder.toString();
    }
    //add by zenghuaying fix bug #9947
    public static final String numberArrayToLikeClauseString(ArrayList<String> numberArray, String chinaCallCode, Context context){
        int count = numberArray.size();
        StringBuffer strBuffer = new StringBuffer();
        String numberStr;
        for(int i=0;i<count;i++){
            numberStr = numberArray.get(i);
            if(numberStr.startsWith(chinaCallCode)){
                numberStr = numberStr.substring(chinaCallCode.length());
            }
            if(isStartWithIpPrefix(context, numberStr)){
              numberStr = numberStr.substring(IP_PREFIX_LENGTH);
            }
            strBuffer.append(" LIKE '%"+numberStr+"%'");
            
            if(i+1 < count){
                strBuffer.append(" OR Calls.NUMBER ");
            }
        }
        
        return strBuffer.toString();
    }
    
    //add end
    public static final String numberArrayToInClauseString(ArrayList<String> numberArray, String chinaCallCode, String ipPrefix) {
        String number = null;
        int count = numberArray.size();
        int callCodeLen = chinaCallCode.length();
        StringBuilder strBuilder = new StringBuilder();
        if (TextUtils.isEmpty(ipPrefix)) {
            for (int i = 0; i < count; ++i) {
                number = numberArray.get(i);
                strBuilder.append("'" + number + "'");
                if (number.startsWith(chinaCallCode)) {
                    strBuilder.append(",'" + number.substring(callCodeLen) + "'");
                }
                else {
                    //add by zenghuaying
                    if (isStartWithIpPrefix(number)) {
                        strBuilder.append(",'" + number.substring(5) + "'");
                    }
                    //add end
                    
                    if (number.length() >= MIN_NUMBER_LENGTH_ALLOWED_TO_ADD_IP) {
                        strBuilder.append(",'" + chinaCallCode + number + "'");
                    }
                }
                
                if ((i + 1) < count) {
                    strBuilder.append(",");
                }
            }
        }
        else {
            int ipPrefixLen = ipPrefix.length();
            for (int i = 0; i < count; ++i) {
                number = numberArray.get(i);
                strBuilder.append("'" + number + "'");
                if (number.startsWith(chinaCallCode)) {
                    strBuilder.append(",'" + number.substring(callCodeLen) + "'");
                }
                else {
                  //modify by zenghuaying fix bug #8367
//                    if (number.startsWith(ipPrefix)) {
//                        strBuilder.append(",'" + number.substring(ipPrefixLen) + "'");
//                    }
                    if (isStartWithIpPrefix(number)) {
                        strBuilder.append(",'" + number.substring(ipPrefixLen) + "'");
                    }
                    //end
                    else {
                        if (number.length() >= MIN_NUMBER_LENGTH_ALLOWED_TO_ADD_IP) {
                            strBuilder.append(",'" + chinaCallCode + number + "'");
                        }
                    }
                }
                
                if ((i + 1) < count) {
                    strBuilder.append(",");
                }
            }
        }
        return strBuilder.toString();
    }

    public static final String numberArrayToInClauseStringwithTrim(ArrayList<String> numberArray) {
        String chinaCallCode = CommonMethod.getChinaCallCode();
        StringBuilder strBuilder = new StringBuilder();
        int count = numberArray.size();
        String number;
        for (int i = 0; i < count; ++i) {
            if (i > 0) {
                strBuilder.append(',');
            }
            number = PhoneNumberUtils.stripSeparators(numberArray.get(i)); 
            if (number.startsWith(chinaCallCode)) {
                number = number.substring(chinaCallCode.length());
            }
            strBuilder.append("'" + number + "'");
        }
        return strBuilder.toString();
    }

    public static final String numberArrayToInClauseString(ArrayList<String> numberArray) {
        StringBuilder strBuilder = new StringBuilder();
        int count = numberArray.size();
        for (int i = 0; i < count; ++i) {
            if (i > 0) {
                strBuilder.append(',');
            }
            
            strBuilder.append("'" + numberArray.get(i) + "'");
        }
        return strBuilder.toString();
    }

    public static final String trim(String strToTrim, char[] arrTrimKey) {
        char c = 0;
        boolean bMatch = false;
        int nCount = strToTrim.length();
        StringBuilder strBuilder = new StringBuilder(nCount);
        for (int i = 0; i < nCount; ++i) {
            c = strToTrim.charAt(i);
            for (char trimKey : arrTrimKey) {
                if (c == trimKey) {
                    bMatch = true;
                    break;
                }
            }

            if (bMatch) {
                bMatch = false;
            }
            else {
                strBuilder.append(c);
            }
        }
        return strBuilder.toString();
    }

    public static final EnmSimOperator getSimOperator(Context context) {
        EnmSimOperator simOperator = EnmSimOperator.UNKNOWN;
        TelephonyManager telMgr = (TelephonyManager )context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telMgr.getSubscriberId();
        if (null != imsi){
            log("getSimOperator: imsi=" + imsi);
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) { //china mobile
                simOperator = EnmSimOperator.CHINA_MOBILE;
            }
            else if (imsi.startsWith("46001")) { //china unicom
                simOperator = EnmSimOperator.CHINA_UNICOM;
            }
            else if (imsi.startsWith("46003")){ //china telecom
                simOperator = EnmSimOperator.CHINA_TELECOM;
            }
            else if (imsi.startsWith("460")) { //some china telecom sim
                simOperator = EnmSimOperator.CHINA_TELECOM;
            }
        }
        return simOperator;
    }
    
    public static final ContactInfo getContactInfo(Context context, int contactId) {
        ContactInfo info = null;
        String strWhere = null;
        strWhere = CommonDataKinds.Phone.CONTACT_ID + "=" + contactId;
        Cursor cursor = null;
        
        try {
            cursor = context.getContentResolver().query(
                    CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {Contacts.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER},
                    strWhere,
                    null,
                    null);
            if ((null != cursor) && cursor.moveToFirst()) {
                info = new ContactInfo();
                info.mName = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
                info.mNumber = cursor.getString(cursor.getColumnIndexOrThrow(CommonDataKinds.Phone.NUMBER));
                info.mNumber = PhoneNumberUtils.stripSeparators(info.mNumber);
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
        
        return info;
    }
    
    // get contact detail by Contacts._ID
    public static ContactInfoDetail getContactInfoDetail(Context context, long contactId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(RawContactsEntity.CONTENT_URI, null,
                RawContacts.CONTACT_ID + "=?",
                new String[] { String.valueOf(contactId) }, null);
        ContactInfoDetail detail = null;
        if (cur != null && cur.getCount() > 0) {
            detail = new ContactInfoDetail();
            cur.moveToFirst();
//            for (int i = 0; i < cur.getColumnCount(); i++) {
//                log("column = " + cur.getColumnName(i));
//            }

            do {
                String info = null;
                int column = cur.getColumnIndex(Data.MIMETYPE);
                String mimeType = cur.getString(column);
                int indexPrimary = cur
                        .getColumnIndex(Data.IS_SUPER_PRIMARY);
                boolean isPrimary = cur.getInt(indexPrimary) != 0;
                if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    column = cur.getColumnIndex(Data.DATA1);
                    info = cur.getString(column);

                    ContactInfoEntry infoEntry = new ContactInfoEntry();
                    infoEntry.data = info;
                    infoEntry.isPrimary = isPrimary;
                    detail.mNumbers.add(infoEntry);
                } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    column = cur.getColumnIndex(Data.DATA1);
                    info = cur.getString(column);

                    ContactInfoEntry infoEntry = new ContactInfoEntry();
                    infoEntry.data = info;
                    infoEntry.isPrimary = isPrimary;
                    detail.mEmails.add(infoEntry);
                } else if (StructuredPostal.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    column = cur.getColumnIndex(Data.DATA1);
                    info = cur.getString(column);

                    ContactInfoEntry infoEntry = new ContactInfoEntry();
                    infoEntry.data = info;
                    infoEntry.isPrimary = isPrimary;
                    detail.mAddress.add(infoEntry);
                }
            } while (cur.moveToNext());
        }
        if (cur != null)
            cur.close();
        return detail;
    }

    public static final void addToBlacklist(Context context, String name, String number) {
        if (!TextUtils.isEmpty(number)) {
            showAddToBlacklistUI(context, name, new String[] {number}, false);
        }
    }

    public static final void addToBlacklist(Context context, String name, String number, boolean IsimportSMS) {
        if (!TextUtils.isEmpty(number)) {
            showAddToBlacklistUI(context, name, new String[] {number}, IsimportSMS);
        }
    }

    public static final void addToBlacklist(Context context, ArrayList<String> numbers) {
        try {
            showAddToBlacklistUI(context, "", (String[])numbers.toArray(new String[0]), false);
        } catch (ClassCastException e) {
            loge("addToBlacklist e=" + e);
        }
    }

    private static final void showAddToBlacklistUI(Context context, String name, String[] numbers, boolean IsimportSMS) {
        Intent intent = new Intent("com.lewa.PIM.ui.AddtoBlackListActivity");
        //intent.putExtra("name", (name == null) ? "" : name);
        intent.putExtra("numberlist", numbers);
        /*
                 *  1:import the contact's all sms,(when operate at sms window, this must be 1);
                 *  0:not import the contact's sms;
                 */
        intent.putExtra("type", IsimportSMS);
        context.startActivity(intent);
    }

    public static final void requestAddToBlacklist(Context context, String name, String[] numbers, boolean IsimportSMS) {
        Intent intent = new Intent(LEWA_INTERCEPT_ADDTOBLACKLIST_ACTION);
        intent.putExtra("name", (name == null) ? "" : name);
        intent.putExtra("numberlist", numbers);
        for (String number : numbers) {
            log("number=" + number);
        }
        /*
                 *  1:import the contact's all sms,(when operate at sms window, this must be 1);
                 *  0:not import the contact's sms;
                 */
        intent.putExtra("type", IsimportSMS ? 1 : 0);
        log("IsimportSMS=" + IsimportSMS);
        context.sendBroadcast(intent);
    }

    public static final void clearFromBlacklist(Context context, String name, String number) {
        //if (!TextUtils.isEmpty(number)) {
        //    String uri = "content://com.lewa.providers.intercept/interceptData";
        //    context.getContentResolver().delete(Uri.parse(uri), "number='" + number + "'", null);
        //}
        if (!TextUtils.isEmpty(number)) {
            clearFromBlacklist(context, new String[] {number});
        }
    }

    public static final void clearFromBlacklist(Context context, ArrayList<String> numbers) {
        try {
            clearFromBlacklist(context, (String[])numbers.toArray(new String[0]));
        } catch (ClassCastException e) {
            loge("addToBlacklist e=" + e);
        }
    }

    public static final void clearFromBlacklist(Context context, String[] numbers) {
        Intent intent = new Intent(LEWA_INTERCEPT_DELETEFROMBLACKLIST_ACTION);
        intent.putExtra("numberlist", numbers);
        context.sendBroadcast(intent);
    }

    public static final boolean numberIsInBlacklist(Context context, String number) {
        boolean isBlack = false;
        Cursor cursor = null;
        String chinaCallCode = CommonMethod.getChinaCallCode();
        number = PhoneNumberUtils.stripSeparators(number); 
        if (number.startsWith(chinaCallCode)) {
            number = number.substring(chinaCallCode.length());
        }
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://com.lewa.providers.intercept/interceptData"),
                    new String[] {"COUNT(*) AS black_count"},
                    "number='" + number + "' and type=1 ",
                    null,
                    null);
            if ((null != cursor) && cursor.moveToFirst() && (cursor.getInt(0) > 0)) {
                isBlack = true;
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

        return isBlack;
    }

    public static final boolean blacklistContainsAnyNumber(Context context, ArrayList<String> numbers) {
        boolean isBlack = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://com.lewa.providers.intercept/interceptData"),
                    new String[] {"COUNT(*) AS black_count"},
                    "number IN(" + numberArrayToInClauseStringwithTrim(numbers) + ") and type=1",
                    null,
                    null);
            if ((null != cursor) && cursor.moveToFirst() && (cursor.getInt(0) > 0)) {
                isBlack = true;
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

        return isBlack;
    }

    public static final boolean nameIsInBlacklist(Context context, String name) {
        boolean isBlack = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    Uri.parse("content://com.lewa.providers.intercept/interceptData"),
                    new String[] {"COUNT(*) AS black_count"},
                    "name='" + name + "'",
                    null,
                    null);
            if ((null != cursor) && cursor.moveToFirst() && (cursor.getInt(0) > 0)) {
                isBlack = true;
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

        return isBlack;
    }

    public static boolean isStartMsgPop(){
		if (mComposeMessageActivityState || mNewMessageComposeActivityState || mConversationListState) {
			return true;
		}
		return false;
    }
    
    public static void setConversationListState(boolean state){
        mConversationListState = state;
    }
    
    public static void setComposeMessageActivityState(boolean state){
        mComposeMessageActivityState = state;
    }
    
    public static void setNewMessageComposeActivityState(boolean state){
        mNewMessageComposeActivityState = state;
    }
    /**
     * add by zenghuaying for request 7955
     * @param context
     * @param number
     * @return
     */
    private static boolean isUnusedIpCallNumber(Context context,String number){
    	SharedPreferences mPrefs = context.getSharedPreferences("unused_ip_data", Context.MODE_WORLD_READABLE);
    	String numStr = mPrefs.getString("unused_ip_numbers", "");
    	
        if(!TextUtils.isEmpty(numStr)){
        	String[] numbers = numStr.split(",");
        	for (String unusedIpNumber:numbers) {
				if(number.equals(unusedIpNumber)){
					return true;
				}
			}
        }
        
        return false;
    }
    
    
    private static boolean isAllowedNumberToAddIp(Context context,String number) {
    	//add by zenghuaying for request 7955
    	if(isUnusedIpCallNumber(context,number)){
    		return false;
    	}
        if (number.length() >= MIN_NUMBER_LENGTH_ALLOWED_TO_ADD_IP) {
            for (String notAllowedNumber : NUMBERS_NOT_ALLOWED_TO_ADD_IP) {
                if (number.startsWith(notAllowedNumber)) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
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
    public static boolean isYiliaoNumber(Context context, ArrayList<String> arrayList) {
        int size = arrayList.size();
        if (size == 0) 
            return false;        
        
        ContentResolver resolver = context.getContentResolver();
        String strNums = new String("");
        
        for (int i = 0; i < size; i++) {
            if (i == 0)
                strNums += "\'" + arrayList.get(i) + "\'";
            else
                strNums += ",\'" + arrayList.get(i) + "\'";
        }
        Cursor cursor = resolver.query(RosterData.CONTENT_URI, new String[]{RosterData._ID}, RosterData.ROSTER_USER_ID + " in (" + strNums + ")", null, null);
        try {
            if (cursor != null && cursor.getCount() > 0)
                return true;
        } finally {
            cursor.close();
            cursor = null;
        }
        return false;
    }
    
    public static boolean isMyselfOnline(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                "com.lewa.PIM_preferences", context.MODE_WORLD_READABLE);
        return sp.getBoolean(YILIAO_CONTACTS_ACCOUNT_STATUS, false);
    }
    
    public static boolean getLWMsgOnoff(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                "com.lewa.PIM_preferences", context.MODE_WORLD_READABLE);
        return sp.getBoolean(LEWA_MSG_ON_OFF, false);
    }
    
    public static void setLWMsgOnoff(Context context, boolean bOnoff) {
        SharedPreferences sp = context.getSharedPreferences(
                "com.lewa.PIM_preferences", context.MODE_WORLD_READABLE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(LEWA_MSG_ON_OFF, bOnoff);
        editor.commit();
        
        Intent intent = new Intent(ACTION_LEWA_MSG_ON_OFF);
        context.sendBroadcast(intent);
    }
    
    //param contactId: contact _id
    public static boolean isYiliaoContact(Context context, long contactId) {
        Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, 
                                                        new String[]{RawContacts.CONTACT_TYPE},
                                                        RawContacts.CONTACT_ID + "=?",
                                                        new String[]{String.valueOf(contactId)}, null);
        try {
            if (cursor.moveToFirst()) {
                if (cursor.getInt(0) != 0)
                    return true;
            }
        } finally {
            cursor.close();
            cursor = null;
        }
       
        return false;
    }
    
    public static String filterPhoneNumber(String phone) {
        String filterPhone = phone;
        if (filterPhone.startsWith("+86"))
        {   
            filterPhone = filterPhone.substring(3);                    
        } 
        filterPhone = filterPhone.replaceAll("-", "");
        return filterPhone;
    }
    
    public static void initSpecialPhoneNameMap(Context context) {        
        Cursor cursor = null;
        ContentResolver cr = context.getContentResolver();

            try {
                cursor = cr.query(SPECIAL_PHONE_URI,
                        new String[] {LOCATION, PHONE_NO},
                        " 1=1 ",
                        null,
                        null);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(LOCATION));
                    String number = cursor.getString(cursor.getColumnIndex(PHONE_NO));
                    mSpecialPhoneNameMap.put(number, name);                    
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

    public static String getSpecialPhone(String phoneNo){
        return mSpecialPhoneNameMap.get(phoneNo);
    }

    public static boolean isSpecialPhone(String phoneNo){        
        Set<String> keys = mSpecialPhoneNameMap.keySet();
        for (String item : keys) {
            if (phoneNo.matches("0*" + item + "\\d*")) {
                return true;
            }
        }
        loge("isSpecialPhone");
        return false;
    }

    public static void cleanSpecialPhoneMap(){
        mSpecialPhoneNameMap.clear();
    }

    static void loge(String msg) {
        if (DBG_LEVEL > 0) {
            Log.e(TAG, msg);
        }
    }
    
    static void log(String msg) {
        if (DBG_LEVEL > 2) {
            Log.i(TAG, msg);
        }
    }

    public static Boolean IsConnection(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING) {
            return true;
        } else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    public static boolean isWiFiActive(Context context) {
        WifiManager mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
            System.out.println("**** WIFI is on");
            return true;
        } else {
            System.out.println("**** WIFI is off");
            return false;
        }
    }
    
    public static boolean isYiliaoOnline(Context context, String number) {
    	boolean bRet = false;
        if (TextUtils.isEmpty(number)){
            return false;                	
        } 
        
        ContentResolver resolver = context.getContentResolver();
        String strNums = number;
        
        Cursor cursor = resolver.query(RosterData.CONTENT_URI, new String[]{RosterData.STATUS}, RosterData.ROSTER_USER_ID + " = '" + strNums + "' ", null, null);
        try {
            if (cursor.moveToNext()){
            	int state = cursor.getInt(0);
            	bRet = state == 1 ? true : false;            	
            }            	
        } finally {
            cursor.close();
            cursor = null;
        }
        return bRet;
    }        
    
    public static void CheckUserOnlineStatus(Context context, ArrayList<String> arrNumbers){
        Intent intent= new Intent(RosterResponseReceiver.ACTION_YILIAO_STATUS_NUMBERS_DETAIL); 
        PendingIntent mStatusIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        String[] strNumbers = new String[arrNumbers.size()];
        arrNumbers.toArray(strNumbers);
        IMClient.CheckUserOnlineStatus(context, IMMessage.GeXinIM, strNumbers, mStatusIntent);
    }
}
