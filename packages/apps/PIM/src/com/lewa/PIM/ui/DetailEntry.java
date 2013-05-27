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

package com.lewa.PIM.ui;

import java.lang.Exception;
import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RosterData;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.internal.telephony.CallerInfo;
import com.lewa.PIM.mms.ui.ComposeMessageActivity;
import com.lewa.PIM.mms.ui.NewMessageComposeActivity;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.util.CommonMethod.SelectorOnClickListener;
import com.lewa.PIM.util.RosterResponseReceiver;
import com.lewa.PIM.widget.ContactHeaderWidget;

import com.lewa.PIM.R;
import com.lewa.PIM.IM.IMClient;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.calllog.ui.LogDetailActivity;
import com.lewa.PIM.contacts.ViewContactActivity;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import com.lewa.os.ui.ViewPagerIndicator.OnPagerSlidingListener;
import com.lewa.os.ui.ViewPagerIndicatorActivity;

public class DetailEntry extends ViewPagerIndicatorActivity
        implements ContactHeaderWidget.ContactHeaderListener,
                SelectorOnClickListener,
                ActivityResultReceiver,
                OnPagerSlidingListener{

    public static final int LOG_DETAIL     = 1;
    public static final int CONTACT_DETAIL = 2;
    public static final int MESSAGE_DETAIL = 3;

    public static final int REQUEST_EDIT_CONTACT    = 2; //refer ViewContactActivity.REQUEST_EDIT_CONTACT
    public static final int REQUEST_NEW_CONTACT     = 0x10000;
    public static final int REQUEST_ADD_TO_CONTACTS = 0x10001;
    public static final int REQUEST_ON_NEW_INTENT   = 0x10002;

    public static final int TOKEN_QUERY_CONTACTS_TYPE = 1;
    public static final int TOKEN_QUERY_IMESSAGE_ONLINE = 2;
    public static final int ACTION_YILIAO_ONLINE_IMAGE_NULL   = 0;
    public static final int ACTION_YILIAO_ONLINE_IMAGE_ONLINE   = 1;
    public static final int ACTION_YILIAO_ONLINE_IMAGE_NO_ONLINE   = 2;
    
    //public static final String ACTION_YILIAO_ONLINE_IMAGE_STATE = "com.lewa.PIM.IM.YILIAO_ONLINE_IMAGE_STATE";
    //public static final String ACTION_IS_IMS_USER = "com.lewa.PIM.IM.IS_IMS_USER";
    
    //private static final String ACTION_YILIAO_STATUS_NUMBERS_DETAIL = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE_DETAIL";
    
    private int mDetailType = 0;
    private Intent mMsgIntent;
    private Intent mLogIntent;
    private Intent mContactIntent;
//    private ArrayList<String> strOnlineNumber = new ArrayList<String>();
    
    private ContactHeaderWidget mContactHeaderWidget;
    private static ContactHeaderWidget mStaticContactHeaderWidget;    
    private AttachmentReceiver myReceiver;
    private Context mDetailContext;
    private ContactsQueryHandler mQueryHandler;
    //private ResponseReceiver mResponseReceiver;
    
    private interface RosterDataQuery {
        String[] COLUMNS = new String[] {
            RosterData._ID,
            RosterData.NICK_NAME,
            RosterData.ROSTER_USER_ID,
            RosterData.PHOTO,
        };

        final int ROSTERDATA_ID = 0;
        final int ROSTERDATA_NICK_NAME = 1;
        final int ROSTERDATA_ROSTER_USER_ID = 2;
        final int ROSTERDATA_PHOTO = 3;
      
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mQueryHandler = new ContactsQueryHandler(this);
//        IntentFilter filterContact = new IntentFilter();
//        mResponseReceiver = CommonMethod.getResponseReceiver();
//        filterContact.addAction(CommonMethod.ACTION_YILIAO_STATUS_NUMBERS_DETAIL);
//        registerReceiver(mResponseReceiver, filterContact);

        try {
            Intent intent = getIntent();
            if (!initActivityStartParam(savedInstanceState, intent)) {
                super.onCreate(savedInstanceState);
                finish();
                return;
            }
            
            super.setOnTriggerPagerChange(this);
            if (mDetailType == MESSAGE_DETAIL) {
                ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
                Log.e("DetailEntry", "onCreate activity=" + activity);
                if (activity != null) {
                    activity.setIsCurrentPage(true);
                }
            }
        } catch (Exception e) {
            Log.e("DetailEntry", "onCreate Exception=" + e);
        }
        
        myReceiver = new AttachmentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lewa.PIM.mms.ComposeMessage.AddAttachment_Receiver");
        //filter.addAction(DetailEntry.ACTION_YILIAO_ONLINE_IMAGE_STATE);
        //filter.addAction(DetailEntry.ACTION_IS_IMS_USER);
        registerReceiver(myReceiver, filter);
        mDetailContext = this;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openIntent(intent);

        if (null != mLogIntent) {
            Activity logActivity = getItemActivity(LOG_DETAIL - 1);
            if (logActivity instanceof ActivityResultReceiver) {
                ActivityResultReceiver logActivityReceiver = (ActivityResultReceiver )logActivity;
                logActivityReceiver.handleActivityResult(
                        logActivityReceiver,
                        REQUEST_ON_NEW_INTENT,
                        Activity.RESULT_OK,
                        mLogIntent);
            }
        }

        if (null != mContactIntent) {
            Activity contactActivity = getItemActivity(CONTACT_DETAIL - 1);
            if (contactActivity instanceof ActivityResultReceiver) {
                ActivityResultReceiver contactActivityReceiver = (ActivityResultReceiver )contactActivity;
                contactActivityReceiver.handleActivityResult(
                        contactActivityReceiver,
                        REQUEST_ON_NEW_INTENT,
                        Activity.RESULT_OK,
                        mContactIntent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("DetailEntry", "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);
        if (REQUEST_EDIT_CONTACT == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                Uri contactLookupUri = intent.getData();
                if (null != contactLookupUri) {
                    long contactId = ContentUris.parseId(contactLookupUri);
                    if (contactId > 0) {
                        mContactHeaderWidget.bindFromContactLookupUri(
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                    }
                }
            }
        }
        else if (((REQUEST_NEW_CONTACT == requestCode) || (REQUEST_ADD_TO_CONTACTS == requestCode))
                && (Activity.RESULT_OK == resultCode)) {
            Uri contactLookupUri = intent.getData();
            if (null != contactLookupUri) {
                long contactId = ContentUris.parseId(contactLookupUri);
                if (contactId > 0) {
                    mContactHeaderWidget.bindFromContactLookupUri(
                            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                    mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_picture_other);
                    mContactHeaderWidget.showEditContact(true);
//                    mContactHeaderWidget.showAddToContacts(false);
                    setupOnNewIntentParams(contactId, null);
                }
            }
        }else if ((ComposeMessageActivity.REQUEST_CODE_ATTACH_IMAGE <= requestCode) &&
        		(ComposeMessageActivity.REQUEST_CODE_ADD_CONTACT >= requestCode)) {
        	ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
        	if (activity != null) {
				activity.onResult(requestCode, resultCode, intent);
			}
		}

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void registerActivityResultBridge(ActivityResultBridge bridge) {
    }

    @Override
    public void handleActivityResult(
            ActivityResultReceiver realReceiver,
            int requestCode, int resultCode, Intent intent) {
    }

    private void openIntent(Intent intent) {
        String action = intent.getAction();
        mDetailType = 0;
        mContactHeaderWidget.setPhotoImageDrawable(null);
        ContactHeaderWidget.setImsState(ACTION_YILIAO_ONLINE_IMAGE_NULL);

        if (action.equals(Intent.ACTION_SENDTO)) {
            Uri detailUri = intent.getData();
            String scheme = detailUri.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                if ((scheme.equals("sms") || scheme.equals("smsto"))
                        || (scheme.equals("mms") || scheme.equals("mmsto"))) {                    
                    ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
                    String number = detailUri.getSchemeSpecificPart();
                    long contactId = CommonMethod.getContactId(this, null, number);

                    setupOnNewIntentParams(contactId, number);
                    
                    mContactHeaderWidget.bindFromContactLookupUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                    if (0 == contactId) {
                        mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_header_unknow_other);
                        mContactHeaderWidget.showAddToContacts(true);
                        if (!TextUtils.isEmpty(number)) {
                            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                                number = getResources().getString(R.string.unknown);
                            }
                            else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                                number = getResources().getString(R.string.private_num);
                            }
                            else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                                number = getResources().getString(R.string.payphone);
                            }
                        }
                        mContactHeaderWidget.setDisplayName(number, null);
                        mContactHeaderWidget.setPhoneNumber(number);
                    }
                    else {
                        //mContactHeaderWidget.showStar(true);
                        mContactHeaderWidget.showEditContact(true);
                    }   
                    
                    activity.reLoadMessage(intent);
                    mDetailType = MESSAGE_DETAIL;
                }
            }
        }
        else if (action.equals(Intent.ACTION_VIEW)) {
            Uri detailUri = intent.getData();
            String scheme = detailUri.getScheme();
            String authority = detailUri.getAuthority();   
            if (!TextUtils.isEmpty(scheme)) {
                if ((scheme.equals("sms") || scheme.equals("smsto"))
                        || (scheme.equals("mms") || scheme.equals("mmsto"))) {
                    mDetailType = MESSAGE_DETAIL;
                } 
                else if (!TextUtils.isEmpty(authority) && authority.equals("mms-sms")) {
                    String number = intent.getStringExtra("number");                    
                    ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
                    long contactId = CommonMethod.getContactId(this, null, number);

                    setupOnNewIntentParams(contactId, number);
                    
                    mContactHeaderWidget.bindFromContactLookupUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                    if (0 == contactId) {
                        mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_header_unknow_other);
                        mContactHeaderWidget.showAddToContacts(true);
                        if (!TextUtils.isEmpty(number)) {
                            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                                number = getResources().getString(R.string.unknown);
                            }
                            else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                                number = getResources().getString(R.string.private_num);
                            }
                            else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                                number = getResources().getString(R.string.payphone);
                            }
                        }
                        mContactHeaderWidget.setDisplayName(number, null);
                        mContactHeaderWidget.setPhoneNumber(number);
                    }
                    else {
                        //mContactHeaderWidget.showStar(true);
                        mContactHeaderWidget.showEditContact(true);
                    }       
                    activity.setIsCurrentPage(true);
                    activity.reLoadMessage(intent);
                    mDetailType = MESSAGE_DETAIL;
                    
                }
                else {
                    mDetailType = CONTACT_DETAIL;
                }
            }            
        }
        else if (action.equals("com.lewa.intent.action.VIEW_PIM_DETAIL")) {
            mDetailType = intent.getIntExtra("type", 0);
            if (mDetailType == MESSAGE_DETAIL) {
                String number = intent.getStringExtra("number");  
                if (!TextUtils.isEmpty(number)) {
                    ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
                    long contactId = CommonMethod.getContactId(this, null, number);

                    setupOnNewIntentParams(contactId, number);
                    
                    mContactHeaderWidget.bindFromContactLookupUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
                    if (0 == contactId) {
                        mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_header_unknow_other);
                        mContactHeaderWidget.showAddToContacts(true);
                        if (!TextUtils.isEmpty(number)) {
                            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                                number = getResources().getString(R.string.unknown);
                            }
                            else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                                number = getResources().getString(R.string.private_num);
                            }
                            else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                                number = getResources().getString(R.string.payphone);
                            }
                        }
                        mContactHeaderWidget.setDisplayName(number, null);
                        mContactHeaderWidget.setPhoneNumber(number);
                    }
                    else {
                        //mContactHeaderWidget.showStar(true);
                        mContactHeaderWidget.showEditContact(true);
                    }       
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.putExtra("address", number);
                    activity.reLoadMessage(intent);                                   
                }
            }
        }

        if ((mDetailType >= LOG_DETAIL) && (mDetailType <= MESSAGE_DETAIL)) {
            setDisplayScreen(mDetailType - LOG_DETAIL);
        }
    }
    
    private boolean initActivityStartParam(Bundle bundle, Intent intent) {
        long contactId = 0;
        long rosterId = 0;
        String name = null;
        String number = null;
        Bitmap photo = null;
        String action = intent.getAction();
        boolean isFinish = false;
        if (action == null) {
            Log.e("DetailEntry", "initActivityStartParam   action == null");
            return false;
        }
        
        mDetailType = 0;
        if (action.equals(Intent.ACTION_SENDTO)) {
            Uri detailUri = intent.getData();
            if (detailUri == null) {
                Log.e("DetailEntry", "initActivityStartParam 1  detailUri == null");
                return false;
            }
            String scheme = detailUri.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                if ((scheme.equals("sms") || scheme.equals("smsto"))
                        || (scheme.equals("mms") || scheme.equals("mmsto"))) {
                    mDetailType = MESSAGE_DETAIL;
                    String adress = detailUri.getSchemeSpecificPart();
                    
                    if (TextUtils.isEmpty(adress)) {
                        Intent newIntent = new Intent(intent);
                        newIntent.setClass(this, NewMessageComposeActivity.class);                        
                        startActivity(newIntent);
                        isFinish = true;
                    }
                    mMsgIntent = intent;

                    number = detailUri.getSchemeSpecificPart();
                    mLogIntent = new Intent();
                    mLogIntent.putExtra("number", number);

                    contactId = CommonMethod.getContactId(this, name, number);
                    Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                    mContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
                    mContactIntent.putExtra("number", number);
                }
            }
        }
        else if (action.equals(Intent.ACTION_VIEW)) {
            Uri detailUri = intent.getData();
            if (detailUri == null) {
                Log.e("DetailEntry", "initActivityStartParam 2  detailUri == null");
                return false;
            }
            String scheme = detailUri.getScheme();
            String authority = detailUri.getAuthority(); 
            String path = detailUri.getPath();
            
            if (!TextUtils.isEmpty(scheme)) {
                if ((scheme.equals("sms") || scheme.equals("smsto"))
                        || (scheme.equals("mms") || scheme.equals("mmsto"))) {
                    mDetailType = MESSAGE_DETAIL;

                    mMsgIntent = intent;

                    number = detailUri.getSchemeSpecificPart();
                    mLogIntent = new Intent();
                    mLogIntent.putExtra("number", number);

                    contactId = CommonMethod.getContactId(this, name, number);
                    Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                    mContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
                } else if (!TextUtils.isEmpty(authority)) { 
                    if (authority.equals("mms-sms")) {
                        mDetailType = MESSAGE_DETAIL;
                        mMsgIntent = intent;
                        number = intent.getStringExtra("number");
                        
                        mLogIntent = new Intent();
                        mLogIntent.putExtra("number", number);

                        contactId = CommonMethod.getContactId(this, name, number);
                        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                        mContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);                        
                        mContactIntent.putExtra("number", number);
                    } else if (path.contains("roster")) {
                        rosterId = ContentUris.parseId(detailUri);
                        Cursor cursor = getContentResolver().query(RosterData.CONTENT_URI, RosterDataQuery.COLUMNS, RosterData.ROSTER_ID + "=" + rosterId, null, null);
                        try {
                            if (cursor.moveToFirst()) {
                                number = cursor.getString(RosterDataQuery.ROSTERDATA_ROSTER_USER_ID);
                                name = cursor.getString(RosterDataQuery.ROSTERDATA_NICK_NAME);
                                byte[] b = cursor.getBlob(RosterDataQuery.ROSTERDATA_PHOTO);
                                if  (b != null && b.length > 0) {
                                    photo = BitmapFactory.decodeByteArray(b, 0, b.length, null);
                                }
                                Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                                Cursor cursorPhone = getContentResolver().query(uri, new String[]{PhoneLookup._ID}, null,null, null);
                                try {
                                    if (cursorPhone.moveToFirst()) {
                                        contactId = cursorPhone.getLong(0);
                                    }
                                } finally {
                                    cursorPhone.close();
                                    cursorPhone = null;
                                }
                            }
                        } finally {
                            cursor.close();
                            cursor = null;
                        }
                        if (contactId != 0) {
                            Uri msgUri = null; //Uri.parse("smsto:" + info.mNumber);
                            if (number != null) {
                                msgUri = Uri.parse("smsto:" + number);
                            }
                            else {
                                msgUri = Uri.parse("smsto:");
                            }
                            mMsgIntent = new Intent(Intent.ACTION_VIEW, msgUri);
                            
                            mLogIntent = new Intent();
                            mLogIntent.putExtra("contact_id", contactId);
                            if (number != null) {
                                mLogIntent.putExtra("number", number);
                            }
                            
                            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                            mContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);                        
                            mContactIntent.putExtra("number", number);
                        } else {
                            mLogIntent = new Intent();
                            mLogIntent.putExtra("number", number);
                            
                            Uri msgUri = Uri.parse("smsto:" + number);
                            mMsgIntent = new Intent(Intent.ACTION_VIEW, msgUri);                            
                            
                            mContactIntent = intent;
                        }
                        mDetailType = CONTACT_DETAIL;  
                    } else {
                        contactId = ContentUris.parseId(detailUri);
                        CommonMethod.ContactInfo info = CommonMethod.getContactInfo(this, (int)contactId);
                        if (info != null) {
                            Uri msgUri = null; //Uri.parse("smsto:" + info.mNumber);
                            if (info.mNumber != null) {
                                msgUri = Uri.parse("smsto:" + info.mNumber);
                            }
                            else {
                                msgUri = Uri.parse("smsto:");
                            }
                            mMsgIntent = new Intent(Intent.ACTION_VIEW, msgUri);
                            
                            mLogIntent = new Intent();
                            mLogIntent.putExtra("contact_id", contactId);
                            if (info.mName != null) {
                                mLogIntent.putExtra("name", info.mName);
                            }
                            if (info.mNumber != null) {
                                mLogIntent.putExtra("number", info.mNumber);
                            }
                        }
                        else {
                            mMsgIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"));
                            mLogIntent = new Intent();
                            mLogIntent.putExtra("contact_id", contactId);
                        }
                        mContactIntent = intent;
                        mDetailType = CONTACT_DETAIL;
                    }
                } else {
                    Uri data = intent.getData();
                    if (data == null) {
                        Log.e("DetailEntry", "initActivityStartParam 4  data == null");
                        return false;
                    }
                    contactId = ContentUris.parseId(data);
                    CommonMethod.ContactInfo info = CommonMethod.getContactInfo(this, (int )contactId);
                    if (info != null) {
                        Uri msgUri = null; //Uri.parse("smsto:" + info.mNumber);
                        if (info.mNumber != null) {
                            msgUri = Uri.parse("smsto:" + info.mNumber);
                        }
                        else {
                            msgUri = Uri.parse("smsto:");
                        }
                        mMsgIntent = new Intent(Intent.ACTION_VIEW, msgUri);
                        
                        mLogIntent = new Intent();
                        mLogIntent.putExtra("contact_id", contactId);
                        if (info.mName != null) {
                            mLogIntent.putExtra("name", info.mName);
                        }
                        if (info.mNumber != null) {
                            mLogIntent.putExtra("number", info.mNumber);
                        }
                    }
                    else {
                        mMsgIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"));
                        mLogIntent = new Intent();
                        mLogIntent.putExtra("contact_id", contactId);
                    }
                    mContactIntent = intent;
                    mDetailType = CONTACT_DETAIL;
                }
            }            
        }
        else if (action.equals("com.lewa.intent.action.VIEW_PIM_DETAIL")) {
            mDetailType = intent.getIntExtra("type", 0);
            number = intent.getStringExtra("number");
            contactId = intent.getLongExtra("contact_id", 0);
            if (0 == contactId) {
                contactId = CommonMethod.getContactId(this, null, number);
                if (contactId > 0) {
                    intent.putExtra("contact_id", contactId);
                }
            }
            
            mLogIntent = intent;

            Uri msgUri = Uri.parse("smsto:" + number);
            mMsgIntent = new Intent(Intent.ACTION_VIEW, msgUri);
            
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            mContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
            mContactIntent.putExtra("number", number);
        }

        if ((mDetailType >= LOG_DETAIL) && (mDetailType <= MESSAGE_DETAIL)) {
            ArrayList<StartParameter> aClasses = new ArrayList<StartParameter>();

            if (null != mLogIntent) {
                mLogIntent.putExtra("delayloadcontent", true);
                if (mLogIntent.hasExtra("number")) {
                    number = mLogIntent.getStringExtra("number");
                    if (!TextUtils.isEmpty(number)) {
                        //modify by zenghuaying fix bug #11266
                        //number = CommonMethod.stripNumberSpecialPrefix(number);
                        mLogIntent.putExtra("number", number);
                    }
                }
            }
            aClasses.add(new StartParameter(LogDetailActivity.class, mLogIntent, R.string.call_log));

            if (null != mContactIntent) {
                mContactIntent.putExtra("delayloadcontent", true);
            }
            aClasses.add(new StartParameter(ViewContactActivity.class, mContactIntent, R.string.details));

            if (null != mMsgIntent) {
                mMsgIntent.putExtra("delayloadcontent", true);
            }
            aClasses.add(new StartParameter(ComposeMessageActivity.class, mMsgIntent, R.string.message));

            setupFlingParm(aClasses, R.layout.activity_detail, R.id.pager_detail_indicator, R.id.pager_detail_content);
            setDisplayScreen(mDetailType - LOG_DETAIL);

            requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            super.onCreate(bundle);

            mContactHeaderWidget = (ContactHeaderWidget )findViewById(R.id.pager_detail_header);
            mStaticContactHeaderWidget = mContactHeaderWidget;
            mContactHeaderWidget.setContactHeaderListener(this);
            mContactHeaderWidget.bindFromContactLookupUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
            if (contactId != 0) {
                //mContactHeaderWidget.showStar(true);
                mContactHeaderWidget.showEditContact(true);
                if (CommonMethod.getLWMsgOnoff(this) == true) {
                    mQueryHandler.startQuery(TOKEN_QUERY_CONTACTS_TYPE, null, RawContacts.CONTENT_URI, 
                                new String[]{RawContacts.CONTACT_ID, RawContacts.CONTACT_TYPE}, RawContacts.CONTACT_ID + "=" + contactId, null, null);
                }
//                if (CommonMethod.isYiliaoContact(this, contactId)) {
//                    mContactHeaderWidget.showGroupChat(true);                    
//                } else {
//                    mContactHeaderWidget.showInviteFriend(true);
//                }
            } else if (rosterId != 0) {
                if (photo != null)
                    mContactHeaderWidget.setPhoto(photo);
                else 
                    mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_list_picture);
                mContactHeaderWidget.setDisplayName(name, null);
                mContactHeaderWidget.showGroupChat(true);
                
            } else {
                mContactHeaderWidget.setNoPhotoResource(R.drawable.ic_contact_header_unknow_other);
                mContactHeaderWidget.showAddToContacts(true);
                number = mLogIntent.getStringExtra("number");
                if (!TextUtils.isEmpty(number)) {
                    if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                        number = getResources().getString(R.string.unknown);
                    }
                    else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                        number = getResources().getString(R.string.private_num);
                    }
                    else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                        number = getResources().getString(R.string.payphone);
                    }
                }
                mContactHeaderWidget.setDisplayName(number, null);
                mContactHeaderWidget.setPhoneNumber(number);
            }
            
            if (isFinish) {
                return false;
            }else {
                return true;                
            }
        }
        else {
            return false;
        }
    }

    private void setupOnNewIntentParams(long contactId, String number) {
        //Log.e("DetailEntry", "setupOnNewIntentParams: contactId=" + contactId + " number=" + number);
        if (null != mContactHeaderWidget) {
            if (0 == contactId) {
//                mContactHeaderWidget.showEditContact(false);
                mContactHeaderWidget.showAddToContacts(true);
            }
            else {
                mContactHeaderWidget.showEditContact(true);
//                mContactHeaderWidget.showAddToContacts(false);
            }
        }
        
        if (null != mLogIntent)  {
            mLogIntent.putExtra("contact_id", contactId);
            mLogIntent.putExtra("number", number);
        }
        
        if (null != mContactIntent) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            mContactIntent.setData(contactUri);
            mContactIntent.putExtra("number", number);
        }
    }

    @Override
    public void onPhotoClick(View view) {
    }
    
    @Override
    protected void onDestroy() {
        ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
        
        if (activity != null) {
            activity.onFinish();
        }
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);			
		}
//        if (mResponseReceiver != null) {
//            unregisterReceiver(mResponseReceiver);
//        }
        super.onDestroy();
    }

    @Override
    public void onDisplayNameClick(View view) {
    }

    @Override
    public void onAddToContactsClick(View view) {
        String number = mLogIntent.getStringExtra("number");
        CommonMethod.openSelectorForNewOrEditContact(this, number, this);
    }

    @Override
    public void onEditContactClick(View view) {
        if (null != mContactIntent) {
            Uri contactUri = mContactIntent.getData();
            if ((null != contactUri) && (ContentUris.parseId(contactUri) > 0)) {
                startActivityForResult(this, new Intent(Intent.ACTION_EDIT, contactUri), REQUEST_EDIT_CONTACT);
            }
        }
    }

    @Override
    public void onSelectorItemClick(int item) {
        if (SelectorOnClickListener.NEW_CONTACT_ITEM == item) {
            String number = mLogIntent.getStringExtra("number");
            startActivityForResult(this, CommonMethod.createNewContactIntent(number), REQUEST_NEW_CONTACT);
        }
        else if (SelectorOnClickListener.ADD_TO_CONTACTS_ITEM == item) {
            String number = mLogIntent.getStringExtra("number");
            startActivityForResult(this, CommonMethod.createAddToContactsIntent(number), REQUEST_ADD_TO_CONTACTS);
        }
    }

    @Override
    public void onChangePagerTrigger(int currentPage) {
        ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
        
        if(currentPage == (MESSAGE_DETAIL - 1)){
            activity.setIsCurrentPage(true);
        }
        else {
            activity.setIsCurrentPage(false);            
        }        
    }
    
    private class AttachmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	if (intent == null || intent.getAction() == null) {
				return;
			}
        	
        	if (intent.getAction().equals("com.lewa.PIM.mms.ComposeMessage.AddAttachment_Receiver")) {
                ComposeMessageActivity activity = (ComposeMessageActivity)getItemActivity(MESSAGE_DETAIL - 1);
                if (activity != null) {
                    int type = intent.getIntExtra("type", -1);
                    activity.addAttachment(mDetailContext, type, false);
                }				
			}
//        	else if (intent.getAction().equals(DetailEntry.ACTION_YILIAO_ONLINE_IMAGE_STATE)) {
//				int state = intent.getIntExtra("state", ACTION_YILIAO_ONLINE_IMAGE_NULL);
//				
//				ContactHeaderWidget.setImsState(state);
//				if (state == ACTION_YILIAO_ONLINE_IMAGE_ONLINE) {
//					mContactHeaderWidget.setImessageStatus(true);
//				}else if (state == ACTION_YILIAO_ONLINE_IMAGE_NO_ONLINE) {
//					mContactHeaderWidget.setImessageStatus(false);
//				}else {
//					mContactHeaderWidget.setImessageHide();
//				}
//			}
//			else if (intent.getAction().equals(DetailEntry.ACTION_IS_IMS_USER)) {
//				boolean isIms = intent.getBooleanExtra("isIms", false);
//				if (isIms) {
//					mContactHeaderWidget.setPhotoImageResource(R.drawable.contact_head_imessage);
//				}
//			}
        }
    }

    @Override
    public void onInviteFriendClick(View view) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGroupChatClick(View view) {
        // TODO Auto-generated method stub
        
    };
    
    public class ContactsQueryHandler extends AsyncQueryHandler {
        Context mContext;
        ContactsQueryHandler(Context context) {
            super(context.getContentResolver());
            mContext = context;
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            try {
                switch (token){
                    case TOKEN_QUERY_CONTACTS_TYPE: {
                        if (cursor.moveToFirst() && cursor.getInt(1) == 1) {
//                            mContactHeaderWidget.setPhotoImageResource(R.drawable.contact_head_imessage);
//                            mContactHeaderWidget.setImessageStatus(false);
//                            startQuery(TOKEN_QUERY_IMESSAGE_ONLINE, null, Data.CONTENT_URI, new String[]{Phone.NUMBER}, 
//                                    Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
//                                    new String[] {String.valueOf(cursor.getLong(0))}, null);
                        }
                        break;
                    }
                    case TOKEN_QUERY_IMESSAGE_ONLINE: {
//                        ArrayList<String> arrNumbers = new ArrayList<String>();
//                        while (cursor.moveToNext()) {
//                            arrNumbers.add(cursor.getString(0));
//                        }
//                        Log.e("jxli", "numbers = " + arrNumbers.toString());                        
//                        if (arrNumbers != null && arrNumbers.size() > 0) {
//                            Intent intent= new Intent(ACTION_YILIAO_STATUS_NUMBERS_DETAIL); 
//                            PendingIntent mStatusIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
//                            String[] strNumbers = new String[arrNumbers.size()];
//                            arrNumbers.toArray(strNumbers);
//                            IMClient.CheckUserOnlineStatus(mContext, IMMessage.GeXinIM, strNumbers, mStatusIntent);
//                        }
                        break;
                    }
                }
            } finally {
                cursor.close();
                cursor = null;
            }
        }
    }
    
    public static void setImsUserState(int state){
    	ContactHeaderWidget.setImsState(state);    	
    	if (mStaticContactHeaderWidget != null) {
    		if (state == ACTION_YILIAO_ONLINE_IMAGE_ONLINE) {
    			mStaticContactHeaderWidget.setImessageStatus(true);
    		}else if (state == ACTION_YILIAO_ONLINE_IMAGE_NO_ONLINE) {
    			mStaticContactHeaderWidget.setImessageStatus(false);
    		}else {
    			mStaticContactHeaderWidget.setImessageHide();
    		}			
		}
    }    
}

