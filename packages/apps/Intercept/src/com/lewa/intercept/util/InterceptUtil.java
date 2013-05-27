package com.lewa.intercept.util;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lewa.intercept.BlackNameAddActivity;
import com.lewa.intercept.BlockCallActivity;
import com.lewa.intercept.BlockMsgActivity;
import com.lewa.intercept.BlockNameActivity;
import com.lewa.intercept.GetContactName;
import com.lewa.intercept.InterceptReceiver;
import com.lewa.intercept.R;
import com.lewa.intercept.WhiteNameAddActivity;
import com.lewa.intercept.WhiteNameListActivity;
import com.lewa.intercept.intents.Constants;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.QuickContactBadge;
import android.widget.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.widget.RemoteViews;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.provider.InterceptConstants;
import com.lewa.intercept.intents.InterceptIntents;

public class InterceptUtil {

//    static private Drawable sDefaultContactImage;
    public static final int STATE_READ = 1 ;
    public static int INTENTFLAG = 0 ;
    private static int DIALOG_ITEM_ADD_BY_HAND        = 0;
    private static int DIALOG_ITEM_ADD_FROM_CALLS     = 1;
    private static int DIALOG_ITEM_ADD_FROM_CONSTANTS = 2;
    
    private static int DIALOG_ITEM_SEND_MESSAGE       = 0;
    private static int DIALOG_ITEM_SEND_CALL          = 1;
    private static int DIALOG_ITEM_MODIFY_REMARKS     = 2;
    private static int DIALOG_ITEM_DEL_FROM_WHITELIST = 3;
    private static boolean addFlag = false;
    
    //public static long Block_CALL_ID ;

    /**
     * delete all record or delete by id
     *
     * @param context
     * @param uri
     */
    public static int delete(Context context, Uri uri, String where, String[] selectionArgs) {
        return context.getContentResolver().delete(uri, where, selectionArgs);
    }
    
    public static String formatTimeStamp(Context context, long when, boolean fullFormat) {
        String tempTime = new String();
        boolean b24 = DateFormat.is24HourFormat(context);

        if (DateUtils.isToday(when)) {

            if (b24) {
                tempTime = DateUtils.formatDateTime(context , when
                        , (DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR));
            } else {
                SimpleDateFormat formart = new SimpleDateFormat("ah:mm");
                tempTime = formart.format(when);
            }

        } else {
            tempTime = DateUtils.formatDateTime(
                    context, when, (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
        }

        return tempTime;
    }

    public static String removePrefix(String phoneNum) {
        if (phoneNum.contains("+86")) {
            phoneNum = phoneNum.replace("+86", "");
        }
        return phoneNum;
    }

    public static String getContactIDFromPhoneNum(Context context, String phoneNum) {
        String name = "";

        ContentResolver resolver = context.getContentResolver();
        //TODO
        Cursor iCur = resolver.query(
                InterceptConstants.CONTENT_URI, null, InterceptConstants.COLUMN_NUMBER + " = ?"
                , new String[] { phoneNum }, null);
        //Delete by chenqiang for SW1 #7896
//        Cursor pCur = resolver.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null
//                , ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?"
//                , new String[] { phoneNum }, null);

        if (iCur.moveToFirst()) {
            name = iCur.getString(iCur.getColumnIndex(InterceptConstants.COLUMN_NAME));
        }
        iCur.close();
        iCur = null;
        //Delete by chenqiang for SW1 #7896
//        if (name.equals("") && pCur.moveToFirst()) {
//            name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//        }
//        pCur.close();
//        pCur = null;

        return name;
    }

    public static void msgRecoverCvsByGroup(Context context, String address) {
        Cursor cursor = context.getContentResolver().query(
                InterceptConstants.MSG_CONTENT_URI, null, " address = ?"
                , new String[] { address }, null);
        while (cursor!=null && cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_ID));
            msgRecoverCvsById(context, id);
        }
        cursor.close();
    }

    /**
     * recover msg conversation by id
     *
     * @param context
     * @param id
     */
    public static void msgRecoverCvsById(Context context, long id) {
        String userNum = null;
        String msgDate = null;
        String msgType = null;
        String msgRead = null;
        String msgBody = null;

        Uri rowUri = ContentUris.appendId(InterceptConstants.MSG_CONTENT_URI.buildUpon(), id).build();
        Cursor cursor = context.getContentResolver().query(rowUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String userName = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_NAME));
                msgBody = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_BODY));
                msgDate = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_DATE));
                msgRead = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_READ));
                msgType = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_TYPE));
                if (userName.equals(null)) {
                    //TODO
                    userNum = userName;
                } else {
                    userNum = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_ADDRESS));
                }
            } while (cursor != null && cursor.moveToNext());
        }
        cursor.close();
        cursor = null;

        // insert this msg to inbox
        ContentValues values = new ContentValues();
        values.put("date", Long.parseLong(msgDate));
        values.put("read", STATE_READ);
        values.put("type", msgType);
        values.put("address", userNum);
        values.put("body", msgBody);
        context.getContentResolver().insert(InterceptConstants.MSG_INBOX_URI, values);

        // after insert.... delete this from list
        delete(context, rowUri, null, null);
    }

    /**
     * recover call conversation by id
     *
     * @param context
     * @param id
     */
    public static void callRecoverCvsById(Context context,long id) {
        String callDate = null;
        String userNum = null;

        Uri rowUri = ContentUris.appendId(InterceptConstants.CALL_CONTENT_URI.buildUpon(), id).build();
        Cursor cursor = context.getContentResolver().query(rowUri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            userNum = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_ADDRESS));
            callDate = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_DATE));
        }
        cursor.close();
        cursor = null;

        // insert this call to callLog
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, userNum);
        values.put(CallLog.Calls.DATE, callDate);
        values.put(CallLog.Calls.DURATION, "0");
        values.put(CallLog.Calls.TYPE, CallLog.Calls.MISSED_TYPE);
        values.put(CallLog.Calls.NEW, 1);// 0 read,1 not read
        context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

        delete(context, rowUri, null, null);
    }

    public static void callRecoverCvsByGroup(Context context, String address) {
        Cursor cursor = context.getContentResolver().query(
                InterceptConstants.CALL_CONTENT_URI, null, " address = ?"
                , new String[] { address }, null);
        while (cursor != null && cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_ID));
            callRecoverCvsById(context, id);
        }

        cursor.close();
        cursor = null;
    }

//    public static void initAvatarHead(
//            Context context, String address, QuickContactBadge mAvatarView) {
    public static void initAvatarHead(
            Context context, String address) {
        String recipient = "";
        long contactId = 0l;
        String lookupKey = "";

//        if (sDefaultContactImage == null) {
//            sDefaultContactImage = context.getResources().getDrawable(
//                    R.drawable.ic_contact_header_unknow);
//        }

        ContentResolver resolver = context.getContentResolver();
        Cursor pCur = resolver.query(
                Phone.CONTENT_URI, null, Phone.NUMBER + " = ?", new String[] { address }, null);

        if (pCur!= null && pCur.moveToFirst()) {
            contactId = pCur.getLong(pCur.getColumnIndex(Phone.CONTACT_ID));

            Cursor cursor = resolver.query(
                    Contacts.CONTENT_URI, null, Contacts._ID + " = ?"
                    , new String[] { contactId + "" }, null);

            lookupKey = pCur.getString(pCur.getColumnIndex(Contacts.LOOKUP_KEY));
            cursor.close();
            cursor = null;
        }
        pCur.close();
        pCur = null;

//        mAvatarView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));

//        if (mAvatarView.getDrawable() == null) {
//            mAvatarView.setImageDrawable(sDefaultContactImage);
//        }
    }

    public static void showNotification(Context context, Class mClass, int smsCount, int callCount ,String content) {
        Notification notification = null;
        RemoteViews contentView = null;

        NotificationManager mNotificationManager
                = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.layout.block_notification);
        if (smsCount > 0 || callCount > 0) {
            notification = new Notification(
                    R.drawable.block_notification_icon, content, System.currentTimeMillis());
            contentView = contentViewAdd(context, smsCount, callCount);
            notification.contentView = contentView;

            Intent notificationIntent = new Intent(context, mClass);
            if (InterceptReceiver.contentFlag == Constants.INTERCEPT_SMS) {
//                notificationIntent.putExtra("ifBlock", 0);
                INTENTFLAG = InterceptReceiver.contentFlag;
            } else if (InterceptReceiver.contentFlag == Constants.INTERCEPT_CALL){
//                notificationIntent.putExtra("ifBlock", 1);
                INTENTFLAG = InterceptReceiver.contentFlag;
            }
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            notification.contentIntent = contentIntent;

            mNotificationManager.notify(R.layout.block_notification, notification);
        } else {
            mNotificationManager.cancel(R.layout.block_notification);
        }
    }

    public static Intent sendMessage(Context context, String recipient, String message){
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "sendMessage ========recipient:"+recipient);
        }
        Uri msgUri = Uri.fromParts("smsto", recipient, null);
        Intent msgIntent = new Intent(Intent.ACTION_SENDTO, msgUri);
        msgIntent.putExtra("sms_body", "");
        return msgIntent;
    }
    
    public static Intent call(Context context,String number){
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "call ==========");
        }
        Uri callUri = null;
        callUri = Uri.fromParts("tel", number, null);
        Intent callIntent;
        callIntent = new Intent(Intent.ACTION_CALL, callUri);
        return callIntent;
    }
    
    public static int isInBlackOrWhiteList(Context context,String number){
        String selection = InterceptConstants.COLUMN_NUMBER + " = ?";
        String[] selectionArgs = new String[] {number};
        int result = 0;
        Cursor cursor = context.getContentResolver().query(
                InterceptConstants.CONTENT_URI, null, selection, selectionArgs, null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            String type = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_TYPE));
            if (type.equals(""+Constants.BLOCK_TYPE_BLACK)){
                result = Constants.BLOCK_TYPE_BLACK;
            }else if (type.equals(""+Constants.BLOCK_TYPE_WHITE)){
                result = Constants.BLOCK_TYPE_WHITE;
            }
        }
        cursor.close();
        cursor = null;
        return result;
    }
   
    // Woody Guo @ 2012/05/22
    // Dirty hack to fix the bug 6970: http://192.168.0.164/issues/6970
    public static long itemId;
    public static String number;
    public static int type;
    public static boolean isCloseActivity;
    public static Dialog createDeleteTipDialog(final Context context
            /* ,final String number,final long itemId,final int type */) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.dialog_tip));
        builder.setMessage(context.getString(R.string.delete_selected_item));

        builder.setPositiveButton(context.getString(R.string.intercept_btn_confirm)
                , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Log.i(Constants.TAG, "deleteTipDialog-onClick: itemId=" + itemId + "; number=" + number + "; type=" + type);
                int result = 0;
                Uri rowUri = null;
                switch (type) {
                    case Constants.BLOCK_TYPE_BLACK:
                        rowUri = ContentUris.appendId(InterceptConstants.CONTENT_URI.buildUpon(), itemId).build();
                        if (Constants.DBUG) {
                            Log.i(Constants.TAG, "rowUri:" + rowUri);
                        }
                        result = delete(context, rowUri, null, null);
                        break;
                    case Constants.BLOCK_TYPE_WHITE:
                        rowUri = ContentUris.appendId(InterceptConstants.CONTENT_URI.buildUpon(), itemId).build();
                        if (Constants.DBUG) {
                            Log.i(Constants.TAG, "rowUri:" + rowUri);
                        }
                        result = delete(context, rowUri, null, null);
                        break;
                    default:
                        break;
                }
                Intent intent = null;
                if (result > 0){
                    switch (type) {
                        case Constants.BLOCK_TYPE_BLACK:
                             intent = new Intent(InterceptIntents.LEWA_INTERCEPT_DELETEBLACKFROMCACHE_ACTION);
                             intent.putExtra("number", number);
                             context.sendBroadcast(intent);
                            break;
                        case Constants.BLOCK_TYPE_WHITE:
                            intent = new Intent(
                                InterceptIntents.LEWA_INTERCEPT_DELETEWHITEFROMCACHE_ACTION);
                            intent.putExtra("number", number);
                            context.sendBroadcast(intent);
                            break;
                        default:
                            break;
                    }
                }
                dialog.dismiss();
            }
           
        });

        builder.setNegativeButton(context.getString(
                R.string.intercept_btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        return alert;
    }

    public static Dialog createAdd2WhiteTipDialog(final Context context/*,final String number,final int type,final boolean isCloseActivity*/) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.msg_recovery_add_black));
        switch (type) {
            case Constants.BLOCK_TYPE_BLACK:
                builder.setMessage(context.getString(R.string.add2black_already_in_white));
                break;
            case Constants.BLOCK_TYPE_WHITE:
                builder.setMessage(context.getString(R.string.add2white_already_in_black));
                break;
            default:
                break;
        }
        String selection = InterceptConstants.COLUMN_NUMBER + " = ? ";
        final Cursor cursor = context.getContentResolver().query(
                InterceptConstants.CONTENT_URI, null,selection, new String[] {number}, null);
        builder.setPositiveButton(context.getString(R.string.intercept_btn_confirm)
                , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int result = 0;
                if (cursor != null && cursor.getCount() > 0){
                    switch (type) {
                        case Constants.BLOCK_TYPE_BLACK:
                            result = updateWhiteOrBlack(context,number,Constants.BLOCK_TYPE_BLACK);
                            break;
                        case Constants.BLOCK_TYPE_WHITE:
                            result = updateWhiteOrBlack(context,number,Constants.BLOCK_TYPE_WHITE);
                            break;
                        default:
                            break;
                    }
                }
//                String insertResult = null;
                Intent intent = null;
//                if (result > 0){
                    switch (type) {
                        case Constants.BLOCK_TYPE_BLACK:
                            intent = new Intent(
                                InterceptIntents.LEWA_INTERCEPT_DELETEWHITEFROMCACHE_ACTION);
                            intent.putExtra("number", number);
                            context.sendBroadcast(intent);
                            intent = new Intent(
                                InterceptIntents.LEWA_INTERCEPT_INSERTBLACK2CACHE_ACTION);
                            intent.putExtra("number", number);
                            intent.putExtra("type", Constants.BLOCK_TYPE_NUMBER_DEFAULT);
                            context.sendBroadcast(intent);
//                            insertResult = context.getResources().getString(R.string.addBlockSuccessed);
                            break;
                        case Constants.BLOCK_TYPE_WHITE:
                            intent = new Intent(
                                InterceptIntents.LEWA_INTERCEPT_DELETEBLACKFROMCACHE_ACTION);
                            intent.putExtra("number", number);
                            context.sendBroadcast(intent);
                            intent = new Intent(
                                InterceptIntents.LEWA_INTERCEPT_INSERTWHITE2CACHE_ACTION);
                            intent.putExtra("number", number);
                            context.sendBroadcast(intent);
//                            insertResult = context.getResources().getString(R.string.addWhiteSuccessed);
                            break;
                        default:
                            break;
                    }
                    if (isCloseActivity) {
                       ((Activity)context).finish();
                    }
//                }else {
//                    String insertFailed = null;
//                    switch (type) {
//                        case Constants.BLOCK_TYPE_BLACK:
//                            insertResult = context.getResources().getString(R.string.addBlockFailed);
//                            break;
//                        case Constants.BLOCK_TYPE_WHITE:
//                            insertResult = context.getResources().getString(R.string.addWhiteFailed);
//                            break;
//                        default:
//                            break;
//                    }
//                }
                dialog.dismiss();
                //Toast.makeText(context,insertResult, Toast.LENGTH_SHORT).show();
            }
           
        });

        builder.setNegativeButton(context.getString(
                R.string.intercept_btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        return alert;
    }

    
    public static void createWhite(final Context context,final String wnumber,final int type, final long itemId ,final String name) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.msg_recovery_add));
            builder.setMultiChoiceItems(new String[] {context.getString(R.string.intercept_recovery_about)}, null,
                    new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                boolean isChecked) {
                            addFlag = isChecked;
                        }
                    });
            builder.setPositiveButton(context.getString(R.string.intercept_btn_confirm)
                    , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (type == Constants.BLOCK_TYPE_BLACK) {
                        Intent intent = new Intent(InterceptIntents.LEWA_INTERCEPT_DELETEBLACKFROMCACHE_ACTION);
                        intent.putExtra("number", wnumber);
                        context.sendBroadcast(intent);
                        updateWhiteOrBlack(context,wnumber,Constants.BLOCK_TYPE_WHITE);
                    }

                    InterceptUtil.addWhiteNameToDB(context,name,wnumber);  
                    dialog.dismiss();
                    if (addFlag) {
                        addFlag = false;
                        InterceptUtil.msgRecoverCvsByGroup(context, wnumber);
                        InterceptUtil.callRecoverCvsByGroup(context,wnumber);
                    }
                }
               
            });
    
            builder.setNegativeButton(context.getString(
                    R.string.intercept_btn_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    
    public static void createAddOKDialog(final Context context ,final String number, final String content) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.msg_send_recovery_title));
        builder.setMessage(content);
        builder.setPositiveButton(context.getString(R.string.intercept_btn_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InterceptUtil.msgRecoverCvsByGroup(context, number);
                        InterceptUtil.callRecoverCvsByGroup(context,number);
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(context.getString(R.string.intercept_btn_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static int updateWhiteOrBlack(Context context,String number,int type) {
        ContentValues values = new ContentValues();
        String contact_name = GetContactName.getContactName(context, "", number);
        values.put(InterceptConstants.COLUMN_TYPE, type);
        values.put(InterceptConstants.COLUMN_NAME, contact_name);
        String selection = InterceptConstants.COLUMN_NUMBER + " = ?";
        int result = context.getContentResolver().update(
                InterceptConstants.CONTENT_URI, values, selection, new String[] { number });
        return result;
    }

    // Update DataBase
    public static boolean addBlockNameToDb(Context context,String name,String number,int mode) {
        boolean result = false;
        String selection = InterceptConstants.COLUMN_NUMBER + "=?";
        Cursor cursor = context.getContentResolver().query(
                InterceptConstants.CONTENT_URI, null,selection, new String[] {number}, null);
        if (cursor != null && cursor.getCount() > 0) {
            result = false;
        } else {
            Intent intent = new Intent(InterceptIntents.LEWA_INTERCEPT_INSERTBLACK2CACHE_ACTION);
            intent.putExtra("number", number);
            intent.putExtra("type", mode);
            context.sendBroadcast(intent);
            
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_NAME, name);
            values.put(InterceptConstants.COLUMN_NUMBER, number);
            values.put(InterceptConstants.COLUMN_TYPE, Constants.BLOCK_TYPE_BLACK);
            values.put(InterceptConstants.COLUMN_MODE, mode);
            values.put(InterceptConstants.COLUMN_PRIVACY, Constants.DEFAULT_BLOCK_PRIVACY);
            context.getContentResolver().insert(InterceptConstants.CONTENT_URI, values);

            result = true;
        }
        cursor.close();
        cursor = null;
        return result;
    }

    public static boolean addWhiteNameToDB(Context context,String name,String number) {
        boolean result = false;
        String selection = InterceptConstants.COLUMN_NUMBER + "=?";
        Cursor cursor = context.getContentResolver().query(
                InterceptConstants.CONTENT_URI, null,selection, new String[] {number}, null);
        if (cursor != null && cursor.getCount() > 0) {
            result = false;
        } else {
           // if ("".equals(name)) {
              //  name = number;
           // }
            Intent intent = new Intent(InterceptIntents.LEWA_INTERCEPT_INSERTWHITE2CACHE_ACTION);
            intent.putExtra("number", number);
            context.sendBroadcast(intent);
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_NAME, name);
            values.put(InterceptConstants.COLUMN_NUMBER, number);
            values.put(InterceptConstants.COLUMN_TYPE, Constants.BLOCK_TYPE_WHITE);
            values.put(InterceptConstants.COLUMN_MODE, Constants.BLOCK_TYPE_NUMBER_DEFAULT);
            values.put(InterceptConstants.COLUMN_PRIVACY, Constants.DEFAULT_BLOCK_PRIVACY);
            context.getContentResolver().insert(InterceptConstants.CONTENT_URI, values);
            result = true;
        }
        cursor.close();
        cursor = null;
        return result;
    }
    
    public static boolean addBlackNameToDB(Context context,String name,String number) {
        boolean result = false;
        String selection = InterceptConstants.COLUMN_NUMBER + "=?";
        Cursor cursor = context.getContentResolver().query(
                InterceptConstants.CONTENT_URI, null,selection, new String[] {number}, null);
        if (cursor != null && cursor.getCount() > 0) {
            result = false;
        } else {
           // if ("".equals(name)) {
           //     name = number;
           //  }
            Intent intent = new Intent(InterceptIntents.LEWA_INTERCEPT_INSERTBLACK2CACHE_ACTION);
            intent.putExtra("type", Constants.BLOCK_TYPE_NUMBER_DEFAULT);
            intent.putExtra("number", number);
            context.sendBroadcast(intent);
            
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_NAME, name);
            values.put(InterceptConstants.COLUMN_NUMBER, number);
            values.put(InterceptConstants.COLUMN_TYPE, Constants.BLOCK_TYPE_BLACK);
            values.put(InterceptConstants.COLUMN_MODE, Constants.BLOCK_TYPE_NUMBER_DEFAULT);
            values.put(InterceptConstants.COLUMN_PRIVACY, Constants.DEFAULT_BLOCK_PRIVACY);
            context.getContentResolver().insert(InterceptConstants.CONTENT_URI, values);

            result = true;
        }
        cursor.close();
        cursor = null;
        return result;
    }
    
    private static RemoteViews contentViewAdd(Context context, int smsCount, int callCount) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.block_notification);
//        contentView.setTextViewText(R.id.block_msg, context.getString(R.string.intercept_unread_msg, smsCount));
        contentView.setImageViewResource(R.id.block_msg_icon, R.drawable.message_icon);
        contentView.setTextViewText(R.id.block_msg_count, "" + smsCount);
//        contentView.setTextViewText(R.id.block_call, context.getString(R.string.intercept_unseen_call, callCount));
        contentView.setImageViewResource(R.id.block_call_icon, R.drawable.phone_icon);
        contentView.setTextViewText(R.id.block_call_count, "" + callCount);

        return contentView;
    }
    
    public static String deleteBlank(String string){
        Pattern p = Pattern.compile("\\s*|\t|\r|\n"); 
        Matcher m = p.matcher(string); 
        String after = m.replaceAll(""); 
        return after;
    }
    
    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        String tempTime = new String();
        boolean b24 = DateFormat.is24HourFormat(context);

        // Basic settings for formatDateTim\() we want for all cases.    
        if (DateUtils.isToday(when)){
            
            if (b24) {
                tempTime = DateUtils.formatDateTime(context,when,
                        (DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR));
            }else {
                SimpleDateFormat formart = new SimpleDateFormat(context.getString(R.string.twelve_hour_time_format));
                tempTime = formart.format(when);
            }
            
        }else {
            SimpleDateFormat formart;            
            if (b24) {
                formart = new SimpleDateFormat("HH:mm");
                tempTime = DateUtils.formatDateTime(context,when,
                        (DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR));
            }else {
                tempTime = DateUtils.formatDateTime(context,when,
                        (DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR));                
                formart = new SimpleDateFormat(context.getString(R.string.twelve_hour_time_format));
            }
            tempTime = trim(tempTime, new char[] {' ', '\u0020'});
            tempTime = String.format("%s %s",  tempTime, formart.format(when));  
        }

        return tempTime;
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
    
//    static public void addFromPIM(final Context context,String title,final String whiteORblack){
//        AlertDialog.Builder addDialogBuilder = new AlertDialog.Builder(context); 
//        CharSequence[] items = {
//                  context.getResources().getString(R.string.dialog_item_byhand)
//                , context.getResources().getString(R.string.dialog_item_from) };
//
//        addDialogBuilder.setTitle(title);
//        addDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
//            
//            @Override
//            public void onClick(DialogInterface dialog, int items) {
//                switch (items) {
//                case 0:
//                    if ("white".equalsIgnoreCase(whiteORblack)) {
//                        Intent intent = new Intent();
//                        intent.setClass(context, WhiteNameAddActivity.class);
//                        context.startActivity(intent);
//                    }else if ("black".equalsIgnoreCase(whiteORblack)) {
//                        Intent intent = new Intent();
//                        intent.setClass(context, BlackNameAddActivity.class);
//                        context.startActivity(intent);
//                    }
//                    
//                    break;
//
//                case 1:
//                        Intent intent = new Intent();
//                        intent.setClass(context, MainImportActivity.class);
//                        context.startActivity(intent);
//                    break;
//                }
//            }
//        });
//        AlertDialog addDialog = addDialogBuilder.create();
//        addDialog.show();
//    }
    public static void createRemoveDataDialog(final Context context,final String address,final String exchange) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.app_intercept_delete_title));
        if (exchange.equalsIgnoreCase("call")) {
             builder.setMessage(context.getString(R.string.app_intercept_call_delete_context));
                }else if (exchange.equalsIgnoreCase("msg")) {
                 builder.setMessage(context.getString(R.string.app_intercept_msg_delete_context));
                }

        builder.setPositiveButton(
                context.getString(R.string.intercept_btn_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (exchange.equalsIgnoreCase("call")) {
                    InterceptUtil.delete( context
                        , InterceptConstants.CALL_CONTENT_URI, " address = ?"
                        , new String[] { address });
                }else if (exchange.equalsIgnoreCase("msg")) {
                    context.getContentResolver().delete(InterceptConstants.MSG_CONTENT_URI, "address = ?", new String[]{ address});
                }
                
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(
                context.getString(R.string.intercept_btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        
        alert.show();
    }
    

    public static void checkUpdate(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (mobile == State.CONNECTED || wifi == State.CONNECTED) {
            context.registerReceiver(new UpdateDialogChangeReceiver(context), new IntentFilter("update_intercept_dialog"));
            Intent intent = new Intent("check_intelligence_intercept_library");
            context.sendBroadcast(intent);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
        }
    }
    
}

