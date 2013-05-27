/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.lewa.PIM.mms.ui;

import com.lewa.PIM.PimApp;
import com.lewa.PIM.IM.service.IMService;
import com.lewa.PIM.mms.MmsConfig;
import com.lewa.PIM.R;
import com.lewa.PIM.mms.LogTag;
import com.lewa.PIM.mms.data.WorkingMessage;
import com.lewa.PIM.mms.model.MediaModel;
import com.lewa.PIM.mms.model.SlideModel;
import com.lewa.PIM.mms.model.SlideshowModel;
import com.lewa.PIM.mms.slideshow.MmsSlideShowListActivity;
import com.lewa.PIM.mms.transaction.MmsMessageSender;
import com.lewa.PIM.mms.util.AddressUtils;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.android.internal.telephony.ITelephony;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.google.android.mms.pdu.SendReq;
import android.database.sqlite.SqliteWrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RosterData;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * An utility class for managing messages.
 */
public class MessageUtils {
    interface ResizeImageResultCallback {
        void onResizeResult(PduPart part, boolean append);
    }

    private static final String TAG = LogTag.TAG;
    private static String sLocalNumber;

    // Cache of both groups of space-separated ids to their full
    // comma-separated display names, as well as individual ids to
    // display names.
    // TODO: is it possible for canonical address ID keys to be
    // re-used?  SQLite does reuse IDs on NULL id_ insert, but does
    // anything ever delete from the mmssms.db canonical_addresses
    // table?  Nothing that I could find.
    private static final Map<String, String> sRecipientAddress =
            new ConcurrentHashMap<String, String>(20 /* initial capacity */);


    /**
     * MMS address parsing data structures
     */
    // allowable phone number separators
    private static final char[] NUMERIC_CHARS_SUGAR = {
        '-', '.', ',', '(', ')', ' ', '/', '\\', '*', '#', '+'
    };

    private static HashMap numericSugarMap = new HashMap (NUMERIC_CHARS_SUGAR.length);

    static {
        for (int i = 0; i < NUMERIC_CHARS_SUGAR.length; i++) {
            numericSugarMap.put(NUMERIC_CHARS_SUGAR[i], NUMERIC_CHARS_SUGAR[i]);
        }
    }
    
    public static int MESSAGE_SDCARD_MIN_SIZE = 1024 * 10;
    
    private static PowerManager.WakeLock m_wakeLockObj = null;
    private static HashMap<String, String> mPotoIdMap = null; 
    private static HashMap<String, String> mImsNumberStateMap = null; 
    
    
    private MessageUtils() {
        // Forbidden being instantiated.
    }

    public static String getMessageDetails(Context context, Cursor cursor, int size) {
        if (cursor == null) {
            return null;
        }

        if ("mms".equals(cursor.getString(MessageListAdapter.COLUMN_MSG_TYPE))) {
            int type = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_TYPE);
            switch (type) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                    return getNotificationIndDetails(context, cursor);
                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                    return getMultimediaMessageDetails(context, cursor, size);
                default:
                    Log.w(TAG, "No details could be retrieved.");
                    return "";
            }
        } else {
            return getTextMessageDetails(context, cursor);
        }
    }

    private static String getNotificationIndDetails(Context context, Cursor cursor) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        long id = cursor.getLong(MessageListAdapter.COLUMN_ID);
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, id);
        NotificationInd nInd;

        try {
            nInd = (NotificationInd) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Mms Notification.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_notification));

        // From: ***
        String from = extractEncStr(context, nInd.getFrom());
        details.append('\n');
        details.append(res.getString(R.string.from_label));
        details.append(!TextUtils.isEmpty(from)? from:
                                 res.getString(R.string.hidden_sender_address));

        // Date: ***
        details.append('\n');
        details.append(res.getString(
                                R.string.expire_on,
                                MessageUtils.formatTimeStampString(
                                        context, nInd.getExpiry() * 1000L, true)));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = nInd.getSubject();
        if (subject != null) {
            details.append(subject.getString());
        }

        // Message class: Personal/Advertisement/Infomational/Auto
        details.append('\n');
        details.append(res.getString(R.string.message_class_label));
        details.append(new String(nInd.getMessageClass()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append(String.valueOf((nInd.getMessageSize() + 1023) / 1024));
        details.append(context.getString(R.string.kilobyte));

        return details.toString();
    }

    private static String getMultimediaMessageDetails(
            Context context, Cursor cursor, int size) {
        int type = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_TYPE);
        if (type == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            return getNotificationIndDetails(context, cursor);
        }

        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        long id = cursor.getLong(MessageListAdapter.COLUMN_ID);
        Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, id);
        MultimediaMessagePdu msg;

        try {
            msg = (MultimediaMessagePdu) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to load the message: " + uri, e);
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_message));

        if (msg instanceof RetrieveConf) {
            // From: ***
            String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
            details.append('\n');
            details.append(res.getString(R.string.from_label));
            details.append(!TextUtils.isEmpty(from)? from:
                                  res.getString(R.string.hidden_sender_address));
        }

        // To: ***
        details.append('\n');
        details.append(res.getString(R.string.to_address_label));
        EncodedStringValue[] to = msg.getTo();
        if (to != null) {
            details.append(EncodedStringValue.concat(to));
        }
        else {
            Log.w(TAG, "recipient list is empty!");
        }


        // Bcc: ***
        if (msg instanceof SendReq) {
            EncodedStringValue[] values = ((SendReq) msg).getBcc();
            if ((values != null) && (values.length > 0)) {
                details.append('\n');
                details.append(res.getString(R.string.bcc_label));
                details.append(EncodedStringValue.concat(values));
            }
        }

        // Date: ***
        details.append('\n');
        int msgBox = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_BOX);
        if (msgBox == Mms.MESSAGE_BOX_DRAFTS) {
            details.append(res.getString(R.string.saved_label));
        } else if (msgBox == Mms.MESSAGE_BOX_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        details.append(MessageUtils.formatTimeStampString(
                context, msg.getDate() * 1000L, true));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = msg.getSubject();
        if (subject != null) {
            String subStr = subject.getString();
            // Message size should include size of subject.
            size += subStr.length();
            details.append(subStr);
        }

        // Priority: High/Normal/Low
        details.append('\n');
        details.append(res.getString(R.string.priority_label));
        details.append(getPriorityDescription(context, msg.getPriority()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append((size - 1)/1000 + 1);
        details.append(" KB");

        return details.toString();
    }

    private static String getTextMessageDetails(Context context, Cursor cursor) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        
        if ("ims".equals(cursor.getString(MessageListAdapter.COLUMN_MSG_TYPE))){
            details.append(res.getString(R.string.mms_ims_message));
        }else {
            details.append(res.getString(R.string.text_message));
		}

        // Address: ***
        details.append('\n');
        int smsType = 0;
        
        if ("ims".equals(cursor.getString(MessageListAdapter.COLUMN_MSG_TYPE))){
        	smsType = cursor.getInt(MessageListAdapter.COLUMN_MMS_MESSAGE_BOX);
        }else {
        	smsType = cursor.getInt(MessageListAdapter.COLUMN_SMS_TYPE);
		}
        
        if (Sms.isOutgoingFolder(smsType)) {
            details.append(res.getString(R.string.to_address_label));
        } else {
            details.append(res.getString(R.string.from_label));
        }
        details.append(cursor.getString(MessageListAdapter.COLUMN_SMS_ADDRESS));

        // Date: ***
        details.append('\n');
        if (smsType == Sms.MESSAGE_TYPE_DRAFT) {
            details.append(res.getString(R.string.saved_label));
        } else if (smsType == Sms.MESSAGE_TYPE_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        long date = cursor.getLong(MessageListAdapter.COLUMN_SMS_DATE);
        details.append(MessageUtils.formatTimeStampString(context, date, true));

        // Error code: ***
        int errorCode = cursor.getInt(MessageListAdapter.COLUMN_SMS_ERROR_CODE);
        if (errorCode != 0) {
            details.append('\n')
                .append(res.getString(R.string.error_code_label))
                .append(errorCode);
        }

        return details.toString();
    }

    static private String getPriorityDescription(Context context, int PriorityValue) {
        Resources res = context.getResources();
        switch(PriorityValue) {
            case PduHeaders.PRIORITY_HIGH:
                return res.getString(R.string.priority_high);
            case PduHeaders.PRIORITY_LOW:
                return res.getString(R.string.priority_low);
            case PduHeaders.PRIORITY_NORMAL:
            default:
                return res.getString(R.string.priority_normal);
        }
    }

    public static int getAttachmentType(SlideshowModel model) {
        if (model == null) {
            return WorkingMessage.TEXT;
        }

        int numberOfSlides = model.size();
        if (numberOfSlides > 1) {
            return WorkingMessage.SLIDESHOW;
        } else if (numberOfSlides == 1) {
            // Only one slide in the slide-show.
            SlideModel slide = model.get(0);
            if (slide.hasVideo()) {
                return WorkingMessage.VIDEO;
            }

            if (slide.hasAudio() && slide.hasImage()) {
                return WorkingMessage.SLIDESHOW;
            }

            if (slide.hasAudio()) {
                return WorkingMessage.AUDIO;
            }

            if (slide.hasImage()) {
                return WorkingMessage.IMAGE;
            }

            if (slide.hasText()) {
                return WorkingMessage.TEXT;
            }
        }

        return WorkingMessage.TEXT;
    }

    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
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
            tempTime = CommonMethod.trim(tempTime, new char[] {' ', '\u0020'});
            tempTime = String.format("%s %s",  tempTime, formart.format(when));               
        }

        return tempTime;
    }

    public static String formatTimeStamp(Context context, long when, boolean fullFormat) {
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
            tempTime = DateUtils.formatDateTime(context,when,
                    (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
        }
        
        return CommonMethod.trim(tempTime, new char[] {' ', '\u0020'});
    }    
    /**
     * @parameter recipientIds space-separated list of ids
     */
    public static String getRecipientsByIds(Context context, String recipientIds,
                                            boolean allowQuery) {
        String value = sRecipientAddress.get(recipientIds);
        if (value != null) {
            return value;
        }
        if (!TextUtils.isEmpty(recipientIds)) {
            StringBuilder addressBuf = extractIdsToAddresses(
                    context, recipientIds, allowQuery);
            if (addressBuf == null) {
                // temporary error?  Don't memoize.
                return "";
            }
            value = addressBuf.toString();
        } else {
            value = "";
        }
        sRecipientAddress.put(recipientIds, value);
        return value;
    }

    private static StringBuilder extractIdsToAddresses(Context context, String recipients,
                                                       boolean allowQuery) {
        StringBuilder addressBuf = new StringBuilder();
        String[] recipientIds = recipients.split(" ");
        boolean firstItem = true;
        for (String recipientId : recipientIds) {
            String value = sRecipientAddress.get(recipientId);

            if (value == null) {
                if (!allowQuery) {
                    // when allowQuery is false, if any value from sRecipientAddress.get() is null,
                    // return null for the whole thing. We don't want to stick partial result
                    // into sRecipientAddress for multiple recipient ids.
                    return null;
                }

                Uri uri = Uri.parse("content://mms-sms/canonical-address/" + recipientId);
                Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                                               uri, null, null, null, null);
                if (c != null) {
                    try {
                        if (c.moveToFirst()) {
                            value = c.getString(0);
                            sRecipientAddress.put(recipientId, value);
                        }
                    } finally {
                        c.close();
                    }
                }
            }
            if (value == null) {
                continue;
            }
            if (firstItem) {
                firstItem = false;
            } else {
                addressBuf.append(";");
            }
            addressBuf.append(value);
        }

        return (addressBuf.length() == 0) ? null : addressBuf;
    }

    public static void selectAudio(Context context, int requestCode) {
        if (context instanceof Activity) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                    context.getString(R.string.select_audio));
            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void recordSound(Context context, int requestCode) {
        if (context instanceof Activity) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(ContentType.AUDIO_AMR);
            intent.setClassName("com.android.soundrecorder",
                    "com.android.soundrecorder.SoundRecorder");

            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }

    public static void selectVideo(Context context, int requestCode) {
        selectMediaByType(context, requestCode, ContentType.VIDEO_UNSPECIFIED);
    }

    public static void selectImage(Context context, int requestCode) {
        selectMediaByType(context, requestCode, ContentType.IMAGE_UNSPECIFIED);
    }

    private static void selectMediaByType(
            Context context, int requestCode, String contentType) {
         if (context instanceof Activity) {

            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);

            innerIntent.setType(contentType);

            Intent wrapperIntent = Intent.createChooser(innerIntent, null);

            ((Activity) context).startActivityForResult(wrapperIntent, requestCode);
        }
    }

    public static void viewSimpleSlideshow(Context context, SlideshowModel slideshow) {
        if (!slideshow.isSimple()) {
            throw new IllegalArgumentException(
                    "viewSimpleSlideshow() called on a non-simple slideshow");
        }
        SlideModel slide = slideshow.get(0);
        MediaModel mm = null;
        if (slide.hasImage()) {
            mm = slide.getImage();
        } else if (slide.hasVideo()) {
            mm = slide.getVideo();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String contentType;
        if (mm.isDrmProtected()) {
            contentType = mm.getDrmObject().getContentType();
        } else {
            contentType = mm.getContentType();
        }
        intent.setDataAndType(mm.getUri(), contentType);
        context.startActivity(intent);
    }

    public static void showErrorDialog(Context context,
            String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setIcon(R.drawable.ic_sms_mms_not_delivered);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * The quality parameter which is used to compress JPEG images.
     */
    public static final int IMAGE_COMPRESSION_QUALITY = 80;
    /**
     * The minimum quality parameter which is used to compress JPEG images.
     */
    public static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;

    public static Uri saveBitmapAsPart(Context context, Uri messageUri, Bitmap bitmap)
            throws MmsException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, IMAGE_COMPRESSION_QUALITY, os);

        PduPart part = new PduPart();

        part.setContentType("image/jpeg".getBytes());
        String contentId = "Image" + System.currentTimeMillis();
        part.setContentLocation((contentId + ".jpg").getBytes());
        part.setContentId(contentId.getBytes());
        part.setData(os.toByteArray());

        Uri retVal = PduPersister.getPduPersister(context).persistPart(part,
                        ContentUris.parseId(messageUri));

        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("saveBitmapAsPart: persisted part with uri=" + retVal);
        }

        return retVal;
    }

    /**
     * Message overhead that reduces the maximum image byte size.
     * 5000 is a realistic overhead number that allows for user to also include
     * a small MIDI file or a couple pages of text along with the picture.
     */
    public static final int MESSAGE_OVERHEAD = 5000;

    public static void resizeImageAsync(final Context context,
            final Uri imageUri, final Handler handler,
            final ResizeImageResultCallback cb,
            final boolean append) {

        // Show a progress toast if the resize hasn't finished
        // within one second.
        // Stash the runnable for showing it away so we can cancel
        // it later if the resize completes ahead of the deadline.
        final Runnable showProgress = new Runnable() {
            public void run() {
                Toast.makeText(context, R.string.compressing, Toast.LENGTH_SHORT).show();
            }
        };
        // Schedule it for one second from now.
        handler.postDelayed(showProgress, 1000);

        new Thread(new Runnable() {
            public void run() {
                final PduPart part;
                try {
                    UriImage image = new UriImage(context, imageUri);
                    part = image.getResizedImageAsPart(
                        MmsConfig.getMaxImageWidth(),
                        MmsConfig.getMaxImageHeight(),
                        MmsConfig.getMaxMessageSize() - MESSAGE_OVERHEAD);
                } finally {
                    // Cancel pending show of the progress toast if necessary.
                    handler.removeCallbacks(showProgress);
                }

                handler.post(new Runnable() {
                    public void run() {
                        cb.onResizeResult(part, append);
                    }
                });
            }
        }).start();
    }

    public static void showDiscardDraftConfirmDialog(Context context,
            OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.discard_message)
                .setMessage(R.string.discard_message_reason)
                .setPositiveButton(R.string.yes, listener)
                .setNegativeButton(R.string.no, null)
                .show();
    }

    public static String getLocalNumber() {
        if (null == sLocalNumber) {
            sLocalNumber = PimApp.getApplication().getTelephonyManager().getLine1Number();
        }
        return sLocalNumber;
    }

    public static boolean isLocalNumber(String number) {
        if (number == null) {
            return false;
        }

        // we don't use Mms.isEmailAddress() because it is too strict for comparing addresses like
        // "foo+caf_=6505551212=tmomail.net@gmail.com", which is the 'from' address from a forwarded email
        // message from Gmail. We don't want to treat "foo+caf_=6505551212=tmomail.net@gmail.com" and
        // "6505551212" to be the same.
        if (number.indexOf('@') >= 0) {
            return false;
        }

        return PhoneNumberUtils.compare(number, getLocalNumber());
    }

    public static void handleReadReport(final Context context,
            final long threadId,
            final String list,
            final int status,
            final Runnable callback) {
        String selection = Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
            + " AND " + Mms.READ + " = 0"
            + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES;

        if (threadId == -2) {
            selection = selection + " AND " + Mms.THREAD_ID + " IN(" + list + ")";            
        }else if(threadId != -1) {
            selection = selection + " AND " + Mms.THREAD_ID + " = " + threadId;
        }

        final Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                        Mms.Inbox.CONTENT_URI, new String[] {Mms._ID, Mms.MESSAGE_ID},
                        selection, null, null);

        if (c == null) {
            return;
        }

        final Map<String, String> map = new HashMap<String, String>();
        try {
            if (c.getCount() == 0) {
                if (callback != null) {
                    callback.run();
                }
                return;
            }

            while (c.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
                map.put(c.getString(1), AddressUtils.getFrom(context, uri));
            }
        } finally {
            c.close();
        }

        OnClickListener positiveListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                for (final Map.Entry<String, String> entry : map.entrySet()) {
                    MmsMessageSender.sendReadRec(context, entry.getValue(),
                                                 entry.getKey(), status);
                }

                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        OnClickListener negativeListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        OnCancelListener cancelListener = new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (callback != null) {
                    callback.run();
                }
                dialog.dismiss();
            }
        };

        confirmReadReportDialog(context, positiveListener,
                                         negativeListener,
                                         cancelListener);
    }

    private static void confirmReadReportDialog(Context context,
            OnClickListener positiveListener, OnClickListener negativeListener,
            OnCancelListener cancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.message_send_read_report);
        builder.setPositiveButton(R.string.yes, positiveListener);
        builder.setNegativeButton(R.string.no, negativeListener);
        builder.setOnCancelListener(cancelListener);
        builder.show();
    }

    public static String extractEncStrFromCursor(Cursor cursor,
            int columnRawBytes, int columnCharset) {
        String rawBytes = cursor.getString(columnRawBytes);
        int charset = cursor.getInt(columnCharset);

        if (TextUtils.isEmpty(rawBytes)) {
            return "";
        } else if (charset == CharacterSets.ANY_CHARSET) {
            return rawBytes;
        } else {
            return new EncodedStringValue(charset, PduPersister.getBytes(rawBytes)).getString();
        }
    }

    private static String extractEncStr(Context context, EncodedStringValue value) {
        if (value != null) {
            return value.getString();
        } else {
            return "";
        }
    }

    public static ArrayList<String> extractUris(URLSpan[] spans) {
        int size = spans.length;
        ArrayList<String> accumulator = new ArrayList<String>();

        for (int i = 0; i < size; i++) {
            accumulator.add(spans[i].getURL());
        }
        return accumulator;
    }

    /**
     * Play/view the message attachments.
     * TOOD: We need to save the draft before launching another activity to view the attachments.
     *       This is hacky though since we will do saveDraft twice and slow down the UI.
     *       We should pass the slideshow in intent extra to the view activity instead of
     *       asking it to read attachments from database.
     * @param context
     * @param msgUri the MMS message URI in database
     * @param slideshow the slideshow to save
     * @param persister the PDU persister for updating the database
     * @param sendReq the SendReq for updating the database
     */
    public static void viewMmsMessageAttachment(Context context, Uri msgUri,
            SlideshowModel slideshow) {
        boolean isSimple = (slideshow == null) ? false : slideshow.isSimple();
        if (isSimple) {
            // In attachment-editor mode, we only ever have one slide.
            MessageUtils.viewSimpleSlideshow(context, slideshow);
        } else {
            // If a slideshow was provided, save it to disk first.
            if (slideshow != null) {
                PduPersister persister = PduPersister.getPduPersister(context);
                try {
                    PduBody pb = slideshow.toPduBody();
                    persister.updateParts(msgUri, pb);
                    slideshow.sync(pb);
                } catch (MmsException e) {
                    Log.e(TAG, "Unable to save message for preview");
                    return;
                }
            }
            // Launch the slideshow activity to play/view.
            //Intent intent = new Intent(context, SlideshowActivity.class);
            Intent intent = new Intent(context, MmsSlideShowListActivity.class);
            intent.setData(msgUri);
            context.startActivity(intent);
        }
    }

    public static void viewMmsMessageAttachment(Context context, WorkingMessage msg) {
        SlideshowModel slideshow = msg.getSlideshow();
        if (slideshow == null) {
            throw new IllegalStateException("msg.getSlideshow() == null");
        }
        if (slideshow.isSimple()) {
            MessageUtils.viewSimpleSlideshow(context, slideshow);
        } else {
            Uri uri = msg.saveAsMms(false);
            viewMmsMessageAttachment(context, uri, slideshow);
        }
    }

    /**
     * Debugging
     */
    public static void writeHprofDataToFile(){
        String filename = Environment.getExternalStorageDirectory() + "/mms_oom_hprof_data";
        try {
            android.os.Debug.dumpHprofData(filename);
            Log.i(TAG, "##### written hprof data to " + filename);
        } catch (IOException ex) {
            Log.e(TAG, "writeHprofDataToFile: caught " + ex);
        }
    }

    public static boolean isAlias(String string) {
        if (!MmsConfig.isAliasEnabled()) {
            return false;
        }

        if (TextUtils.isEmpty(string)) {
            return false;
        }

        // TODO: not sure if this is the right thing to use. Mms.isPhoneNumber() is
        // intended for searching for things that look like they might be phone numbers
        // in arbitrary text, not for validating whether something is in fact a phone number.
        // It will miss many things that are legitimate phone numbers.
        if (Mms.isPhoneNumber(string)) {
            return false;
        }

        if (!isAlphaNumeric(string)) {
            return false;
        }

        int len = string.length();

        if (len < MmsConfig.getAliasMinChars() || len > MmsConfig.getAliasMaxChars()) {
            return false;
        }

        return true;
    }

    public static boolean isAlphaNumeric(String s) {
        char[] chars = s.toCharArray();
        for (int x = 0; x < chars.length; x++) {
            char c = chars[x];

            if ((c >= 'a') && (c <= 'z')) {
                continue;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                continue;
            }
            if ((c >= '0') && (c <= '9')) {
                continue;
            }

            return false;
        }
        return true;
    }




    /**
     * Given a phone number, return the string without syntactic sugar, meaning parens,
     * spaces, slashes, dots, dashes, etc. If the input string contains non-numeric
     * non-punctuation characters, return null.
     */
    private static String parsePhoneNumberForMms(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();

        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);

            // accept the first '+' in the address
            if (c == '+' && builder.length() == 0) {
                builder.append(c);
                continue;
            }

            if (Character.isDigit(c)) {
                builder.append(c);
                continue;
            }

            if (numericSugarMap.get(c) == null) {
                return null;
            }
        }
        return builder.toString();
    }

    /**
     * Returns true if the address passed in is a valid MMS address.
     */
    public static boolean isValidMmsAddress(String address) {
        String retVal = parseMmsAddress(address);
        return (retVal != null);
    }

    /**
     * parse the input address to be a valid MMS address.
     * - if the address is an email address, leave it as is.
     * - if the address can be parsed into a valid MMS phone number, return the parsed number.
     * - if the address is a compliant alias address, leave it as is.
     */
    public static String parseMmsAddress(String address) {
        // if it's a valid Email address, use that.
        if (Mms.isEmailAddress(address)) {
            return address;
        }

        // if we are able to parse the address to a MMS compliant phone number, take that.
        String retVal = parsePhoneNumberForMms(address);
        if (retVal != null) {
            return retVal;
        }

        // if it's an alias compliant address, use that.
        if (isAlias(address)) {
            return address;
        }

        // it's not a valid MMS address, return null
        return null;
    }

    private static void log(String msg) {
        Log.d(TAG, "[MsgUtils] " + msg);
    }
    
    static final String[] PHONES_SUMMARY_PROJECTION = new String[] {
        Contacts.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER,
        Contacts.SORT_KEY_PRIMARY, CommonDataKinds.Phone.CONTACT_ID,
        Photo.PHOTO_ID};
    
    public static HashMap<String, String> getPhotoIdByContactId(ContentResolver resolver) {
        HashMap<String, String> tempMap = new HashMap<String, String>();
        Cursor cursor = null;
        
        try {
            cursor = resolver.query(Phone.CONTENT_URI,
                    PHONES_SUMMARY_PROJECTION, null, null, "sort_key");

            while (cursor.moveToNext()) {
                String ContactId = cursor.getLong(3)+"";

                if (TextUtils.isEmpty(ContactId)) {
                    continue;
                }

                int pothoId = cursor.getInt(4);
                
                tempMap.put(ContactId, pothoId + "");
            }
            cursor.close();
            
        } catch (Exception e) {
            
            if (null != cursor) {
                cursor.close();
            }
        }        
        return tempMap;
    }
    
    public static void clearPhotoIdMap(){
    	
    	if (mPotoIdMap != null) {
        	mPotoIdMap.clear();
        	mPotoIdMap = null;			
		}
    }
    
    public static HashMap<String, String> getPhotoId(ContentResolver resolver) {
        
        Cursor cursor = null;
    	if (mPotoIdMap == null) {
			mPotoIdMap = new HashMap<String, String>();
		}
    	
        try {
        	
        	if (mPotoIdMap.size() == 0) {
                cursor = resolver.query(Phone.CONTENT_URI,
                        PHONES_SUMMARY_PROJECTION, null, null, "sort_key");

                while (cursor.moveToNext()) {
                    String number = cursor.getString(1);

                    if (TextUtils.isEmpty(number)) {
                        continue;
                    }

                    int pothoId = cursor.getInt(4);
                    String tempNumber = MessageUtils.fixPhoneNumber(number);
                    mPotoIdMap.put(tempNumber, pothoId + "");
                }
                cursor.close();				
			}            
        } catch (Exception e) {
            
            if (null != cursor) {
                cursor.close();
            }
        }        
        return mPotoIdMap;
    }

    public static boolean isURL(StringBuilder span){
        boolean Ret = false;        
        Matcher m = Patterns.WEB_URL.matcher(span);

        while (m.find()) {
            Ret = true;
            break;
        }        
        return Ret;
    }
    
    public static long getRecorderTime(MediaPlayer mediaplayer, String filePath){
    	long duration = 0;
    	if (mediaplayer != null) {
    		try {
    			mediaplayer.reset();
    			mediaplayer.setDataSource(filePath);
    			mediaplayer.prepare();
    			//mediaplayer.start();
    			duration = mediaplayer.getDuration();
			} catch (Exception e) {
				Log.e(TAG, "getRecorderTime() error: " + e.getMessage());
			}
		}
    	return duration;
    }
    
    public static boolean isYiliaoNumber(Context context, ArrayList<String> arrayList) {
        int size = arrayList.size();
        if (!isImsSwitch(context)){
        	return false;
        }
        
        if (size == 0 || size > 1) {
            return false;        	        	
        }
                        
        if (CommonMethod.isYiliaoNumber(context, arrayList) == false) {
			return false;
		}                
//        if (CommonMethod.isYiliaoOnline(context, arrayList.get(0)) == false) {
//			return false;
//		}

        return true;
    }    
    
    public static final int YL_IMAGE_TYPE = 0;
    public static final int YL_AUDIO_TYPE = 1;
    public static final int YL_VIDEO_TYPE = 2;
    
    private static String[] mTypeName ={"image", "audio", "video"};
        
    public static boolean checkMediaType(int type , String filePath){
    	boolean ret = false;
    	if (TextUtils.isEmpty(filePath)) {
			return ret;
		}
    	String[]list = filePath.split("/");
    	String name = list[list.length - 1];

    	if (TextUtils.isEmpty(name)) {
			return ret;
		}

    	int index = name.lastIndexOf(".");    	
    	if (index >= 0 && index + 1 < name.length()) {
    		name = name.substring(index + 1);
    		if (name.equals("ogg")) {
        		name = String.format("%s/%s", "application", name);				
			}else {
	    		name = String.format("%s/%s", mTypeName[type], name);				
			}
    		
    		switch (type) {    		
			case YL_IMAGE_TYPE:
				ret = ContentType.isImageType(name); 
				break;
				
			case YL_AUDIO_TYPE:
				ret = ContentType.isAudioType(name); 
				break;
				
			case YL_VIDEO_TYPE:
				if (ContentType.isVideoType(name)) {
					ret = true;
					break;
				}
				ret = ContentType.isSupportedVideoType(name); 
				break;

			default:
				break;
			}
		} 
    	return ret;
    }    
    
    public static String getFileName(String filePath){
    	if (TextUtils.isEmpty(filePath)) {
			return null;
		}
    	
    	String[]list = filePath.split("/");
    	String name = list[list.length - 1];
    	
    	if (TextUtils.isEmpty(name)) {
			return null;
		}  	
    	
    	return name;
    }  
    
    public static String getExtension(String name){
    	String extension = null;
    	
    	if (TextUtils.isEmpty(name)) {
			return extension;
		}
    	
    	int index = name.lastIndexOf("."); 
    	
    	if (index >= 0 && index + 1 < name.length()) {
			extension = name.substring(index + 1);
		}
    	return extension;
    }
    
    public static Bitmap getVideoThumbnail(ContentResolver cr, String fileName) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //select condition.
        String whereClause = MediaStore.Video.Media.DATA + " = '"
                + fileName + "'";
        Log.v(TAG, "where = " + whereClause);
        
        Cursor cursor = null;
        String videoId = "";
        try {
            //colection of results.
        	cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            new String[] { MediaStore.Video.Media._ID }, whereClause,
                            null, null);
            Log.v(TAG, "cursor = " + cursor);
            if (cursor == null || cursor.getCount() == 0) {
                    return null;
            }
            cursor.moveToFirst();
            //image id in image table.
            videoId = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Video.Media._ID));
            Log.v(TAG, "videoId = " + videoId);
            if (videoId == null) {
                    return null;
            }
            cursor.close();
			
		} catch (Exception e) {
			if (cursor != null) {
				cursor.close();
			}
			return null;
		}
        long videoIdLong = Long.parseLong(videoId);
        //via imageid get the bimap type thumbnail in thumbnail table.
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, videoIdLong,
                        Images.Thumbnails.MICRO_KIND, options);
        Log.v(TAG, "bitmap = " + bitmap);
        return bitmap;
    }
    
    public static boolean isImsSwitch(Context context){
    	
        SharedPreferences sp = context.getSharedPreferences(MessagingPreferenceActivity.IMS_CLOSE_STATE, Context.MODE_WORLD_READABLE);
        String settingRemind = sp.getString(MessagingPreferenceActivity.IMS_CLOSE_STATE, "false");
        
        if (settingRemind.equals("true")) {
        	return true;
		}else {
			return false;
		}
    }
    
    public final static String[] mPrefixPhonName = new String[]{"+86", "12520", "17951", "12593", "17911"};
    
    public static String fixPhoneNumber(String s){
    	String address = s;
    	
    	if (TextUtils.isEmpty(address)) {
			return s;
		}
    	
        address = address.replaceAll("-", "");
        
    	if (TextUtils.isEmpty(address)) {
			return s;
		}
        
        int count = mPrefixPhonName.length;
        
        for (int i = 0; i < count; i++) {
        	
            if (address.startsWith(mPrefixPhonName[i])) {
            	int length = mPrefixPhonName[i].length();
                address = address.substring(length);
                break;
            }			
		}
        
    	if (TextUtils.isEmpty(address)) {
			return s;
		}
    	
        return address;
    }    
        
    public static class PduYlColumns{
        //yl in box uri
        public static final Uri IMS_INBOX_URI = Uri.parse("content://mms-sms/pdu_yl_inbox");
        //yl out box uri
        public static final Uri IMS_OUTBOX_URI = Uri.parse("content://mms-sms/pdu_yl_outbox");
        //yl conversations uri
        public static final Uri IMS_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations/pdu_yl");        
        //yl uri
        public static final Uri IMS_URI = Uri.parse("content://mms-sms/pdu_yl");
        //yl search uri
        public static final Uri IMS_SEARCH_URI = Uri.parse("content://mms-sms/pdu_yl/search");        
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String ID = "_id"; 
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String THREAD_ID = "thread_id";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String M_ID = "m_id";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String PERSON = "person";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String ADDRESS = "address";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String CONTACTS_ID = "contacts_id";
        /**
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE = "date";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String READ = "read";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String STATUS = "status";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String MSG_BOX = "msg_box";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String BODY = "body";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String SUBJECT = "subject";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String LOCKED = "locked";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String ERROR_CODE = "error_code";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String SEEN = "seen";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String M_TYPE = "m_type";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String M_DATA_TYPE = "m_data_type";
        /**
         * <P>Type: INTEGER </P>
         */
        public static final String M_SIZE = "m_size";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String DATA = "data";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String PATH = "path";
        /**
         * <P>Type: TEXT </P>
         */
        public static final String FILE_NAME = "file_name";
    }
    
    public static boolean checkSDCard(Context context){
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String toshort = context.getResources().getString(R.string.no_sdcard_message);
			Toast.makeText(context, toshort, Toast.LENGTH_SHORT).show(); 
			return false;
		}		
		return true;
    }
    
    public static int getFileSize(String path){
    	int size = 0;
    	 File dF = new File(path);
         FileInputStream fis;
         try {
             fis = new FileInputStream(dF);
             size = fis.available();			
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage());
		}
    	return size;
    }
    
    public static boolean pimFirstStart(Context context){
        SharedPreferences sp = context.getSharedPreferences(MessagingPreferenceActivity.IMS_FIRST_START, Context.MODE_WORLD_READABLE);
        boolean bIsfirstStart = false;//sp.getBoolean(MessagingPreferenceActivity.IMS_FIRST_START, true);  
        
        
        if (bIsfirstStart) {
        	SharedPreferences.Editor editor = sp.edit();
        	editor.putBoolean(MessagingPreferenceActivity.IMS_FIRST_START, false);        	
        	editor.commit();
        	        	
        	if (isImsSwitch(context) == false) {
            	Intent intent = new Intent(context, ImsFreeMessageUserProtoclDialog.class);
            	context.startActivity(intent);
            	return true;				
			}else {
		        sp = context.getSharedPreferences(MessagingPreferenceActivity.IMS_FIRST_OPEN, Context.MODE_WORLD_READABLE);
	        	editor = sp.edit();
	        	editor.putBoolean(MessagingPreferenceActivity.IMS_FIRST_OPEN, false);
	        	editor.commit();	
			}
		}
        return false;
    }
    
    public static boolean isImsContatct(long contactId, Context context){
    	boolean isIms = false;
    	
    	if (contactId <= 0 || context == null) {
			return isIms;
		}
    	
    	Cursor cursor = null;
    	try {
    		
    		cursor = context.getContentResolver().
    								query(Contacts.CONTENT_URI, 
    								new String[]{Contacts.CONTACT_TYPE}, 
    								Contacts._ID + "=" + contactId, 
    								null, 
    								null);
    		
			int type = 0;
    		
    		if (cursor.moveToNext()) {
    			type = cursor.getInt(0);
			}
    		cursor.close();
    		
			if (type == 1) {
				isIms = true;
			}
			
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage());
			if (cursor != null) {
				cursor.close();
			}
		}
		return isIms;
    }
    
    public static void AcquireWakeLock(Context context, long milltime) {
        if (m_wakeLockObj == null) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            m_wakeLockObj = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, TAG);
            m_wakeLockObj.acquire(milltime);
        }else {
            m_wakeLockObj.acquire(milltime);			
		}               
    }
    
    public static boolean isMusicPlay(Context context){
    	AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    	
    	if (audioManager != null) {
        	return audioManager.isMusicActive();			
		}else {
			return false;
		}
    }
    
    public static int getAvailaleSize(){
    	int size = 0;
    	
    	try {
        	File path = Environment.getExternalStorageDirectory();
        	StatFs stat = new StatFs(path.getPath()); 
        	long blockSize = stat.getBlockSize(); 
        	long availableBlocks = stat.getAvailableBlocks();
        	size = (int)((availableBlocks * blockSize)/1024);			
		} catch (Exception e) {
			Log.e("MessageUtils", "" + e.getMessage());			
		}
					
    	return size; 
    }
    
    public static boolean checkSDCardSize(Context context){
    	boolean b = false;
		if (MessageUtils.getAvailaleSize() < MESSAGE_SDCARD_MIN_SIZE) {
			String toshort = context.getResources().getString(R.string.message_sd_card_min_size);
			Toast.makeText(context, toshort, Toast.LENGTH_SHORT).show();
			b = true;
		}
    	return b;
    }
    
//    public static void ReleaseWakeLock() {
//        if (m_wakeLockObj != null && m_wakeLockObj.isHeld()) {
//            m_wakeLockObj.release();
//            m_wakeLockObj = null;
//        }
//        Log.e("zhangwei", "ReleaseWakeLock() m_wakeLockObj = " + m_wakeLockObj);
//    }
    
    public static boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneInUse = !phone.isIdle();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }
    
    public static boolean isCHchar(String str){
        boolean isCh = false;
        int icount = str.length();
        
        for (int i = 0; i < icount; i++) {
             String s = str.substring(i, i+1);
             byte ch[] = s.getBytes();
             
             if (ch.length > 1) {
                isCh = true;
                break;
            }
        }                        
        return isCh;
    }
    
    public static void clearImsNumberStateMap(){
    	
    	if (mImsNumberStateMap != null) {
    		mImsNumberStateMap.clear();
    		mImsNumberStateMap = null;			
		}
    }
    
    public static boolean getImsNumberState(String number){
    	boolean state = false;
    	
    	if (mImsNumberStateMap != null && mImsNumberStateMap.size() > 0) {
			String tNumber = MessageUtils.fixPhoneNumber(number);
			String tString = mImsNumberStateMap.get(tNumber);
			
			if (tString == null || "false".equals(tString)) {
				state = false;
			}else {
				state = true;
			}			
		}
    	return state;
    }
    
    public static void queryImsNumberState(ContentResolver resolver) {
        
        Cursor cursor = null;
    	if (mImsNumberStateMap == null) {
    		mImsNumberStateMap = new HashMap<String, String>();
		}
    	
        try {        	
        	if (mImsNumberStateMap.size() == 0) {
                cursor = resolver.query(Phone.CONTENT_URI,
                        PHONES_SUMMARY_PROJECTION, null, null, "sort_key");
                
        		cursor = resolver.query(RosterData.CONTENT_URI, 
						new String[]{RosterData.ROSTER_USER_ID, RosterData.STATUS}, null, null, null);                

                while (cursor.moveToNext()) {
                	String number = MessageUtils.fixPhoneNumber(cursor.getString(0));
                	String state = cursor.getInt(1) == 1 ? "true" : "false";
                	mImsNumberStateMap.put(number, state); 
                }
                cursor.close();				
			}            
        } catch (Exception e) {
            
            if (null != cursor) {
                cursor.close();
            }
        }        
    }
}
