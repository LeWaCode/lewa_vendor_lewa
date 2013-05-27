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

import java.util.ArrayList;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.RosterData;
import android.provider.ContactsContract.Intents.UI;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.lewa.PIM.R;
import com.lewa.PIM.dialpad.ui.DialpadActivity;
import com.lewa.PIM.calllog.ui.CallLogActivity;
import com.lewa.PIM.contacts.ContactsListActivity;
import com.lewa.PIM.contacts.LayoutQuickContactBadge;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.PIM.mms.ui.ConversationList;
import com.lewa.PIM.mms.ui.NewMessageComposeActivity;
import com.lewa.PIM.mms.ui.ConversationList.DeleteThreadListener;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.util.RosterResponseReceiver;
import com.lewa.os.ui.ViewPagerIndicator.OnPagerSlidingListener;
import com.lewa.os.ui.ViewPagerIndicatorActivity;

public class MainEntry extends ViewPagerIndicatorActivity implements OnPagerSlidingListener {
    
    private static final String MESSAGE_ENTRY_COMPONENT  = "com.lewa.PIM.ui.MessageEntryActivity";
    private static final String CALLLOG_ENTRY_COMPONENT  = "com.lewa.PIM.ui.CallLogEntryActivity";
    private static final String CONTACTS_ENTRY_COMPONENT = "com.lewa.PIM.ui.ContactsEntryActivity";
    private static final String DIALPAD_ENTRY_COMPONENT = "com.lewa.PIM.ui.DialpadEntryActivity";
    
	private static final String MESSAGE_ENTRY_ACTIVITY = "com.lewa.PIM.mms.ui.ConversationList";
	private static final String CONTACTS_ENTRY_ACTIVITY = "com.lewa.PIM.contacts.ContactsListyActivity";
    private static final String REQUEST_OPEN_DIALPAD = "com.lewa.intent.action.DIALPAD";

    private static final int TAB_INDEX_MESSAGE  = 0;
    private static final int TAB_INDEX_CALLLOG  = 1;
    private static final int TAB_INDEX_CONTACT  = 2;
    private static final int TAB_INDEX_FAVORITE = 3;

    private static final int QUER_LIST_ROSTER_DATA = 0;
    private static final String[] ROSTER_DATA_PROJECT = new String[]{RosterData.DISPLAY_NAME,
    																	RosterData.ROSTER_USER_ID,
																		RosterData.STATUS};

    
    private int mDetailType = 0; 
    private RosterResponseReceiver mResponseReceiver;
    private RosterDataListQueryHandler mQueryHandler;
    private Context mMainEntryContext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	mMainEntryContext = this;
        ArrayList<StartParameter> aClasses = new ArrayList<StartParameter>();

        Intent messageIntent = new Intent();
        messageIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(ConversationList.class, messageIntent, R.string.message));

        Intent calllogIntent = new Intent();
        calllogIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(DialpadActivity.class,  calllogIntent, R.string.call_entry));

        Intent contactlistIntent = new Intent(UI.LIST_DEFAULT);
        contactlistIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(ContactsListActivity.class, contactlistIntent, R.string.contactsIconLabel));

        Intent favoriteIntent = new Intent(UI.LIST_STREQUENT_ACTION);
        favoriteIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(ContactsListActivity.class, favoriteIntent, R.string.contactsFavoritesLabel));

        setupFlingParm(aClasses, R.layout.activity_home, R.id.indicator, R.id.pager);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        preCreateopenIntent(getIntent());
        super.onCreate(savedInstanceState);
        super.setOnTriggerPagerChange(this);
        openIntent(getIntent());
        
        ComponentName component = getIntent().getComponent();
        if (null != component) {
            String className = component.getClassName();
            if (MESSAGE_ENTRY_COMPONENT.equals(className)) {
                ConversationList activity = (ConversationList)getItemActivity(TAB_INDEX_MESSAGE);
                activity.setIsCurrentPage(true);
            } 
        }
        mResponseReceiver = new RosterResponseReceiver();
        IntentFilter filterContact = new IntentFilter();        		
		filterContact.addAction(RosterResponseReceiver.ACTION_YILIAO_STATUS_NUMBERS_DETAIL);
		registerReceiver(mResponseReceiver, filterContact);        		

        mQueryHandler = new RosterDataListQueryHandler(getContentResolver());
        mQueryHandler.startQuery(QUER_LIST_ROSTER_DATA, 
        							null, 
        							RosterData.CONTENT_URI, 
        							ROSTER_DATA_PROJECT, 
        							null, 
        							null, 
        							null);
		
		//super.setIfPIM(true);
    }

    protected void onNewIntent(Intent intent) {
	preCreateopenIntent(intent);
        super.onNewIntent(intent);
        openIntent(intent);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return false;
//    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        LayoutQuickContactBadge.QCBadgeOnClickListener.cleanEnviromentMap();
        super.onDestroy();
        
        if (mResponseReceiver != null) {
			unregisterReceiver(mResponseReceiver);
		}
    }

    private void preCreateopenIntent(Intent intent)  {
        if (null == intent) {
            setDisplayScreen(TAB_INDEX_CONTACT);
            mDetailType = TAB_INDEX_CONTACT;
            return;
        }

        String action0 = intent.getAction();
        Log.e("MainEntry", "preCreateopenIntent  action0=" + action0);

        ComponentName component = intent.getComponent();
        if (null != component) {
            String className = component.getClassName();
            if (MESSAGE_ENTRY_COMPONENT.equals(className)) {
                setDisplayScreenNoSmoothScroll(TAB_INDEX_MESSAGE);
                mDetailType = TAB_INDEX_MESSAGE;
                return;
            }
            else if (CALLLOG_ENTRY_COMPONENT.equals(className) || DIALPAD_ENTRY_COMPONENT.equals(className)) {
                setDisplayScreenNoSmoothScroll(TAB_INDEX_CALLLOG);
                mDetailType = TAB_INDEX_CALLLOG;
                return;
            }
            else if (CONTACTS_ENTRY_COMPONENT.equals(className)) {
                setDisplayScreenNoSmoothScroll(TAB_INDEX_CONTACT);
                mDetailType = TAB_INDEX_CONTACT;
                return;
            }
        }

        int tabIdx = TAB_INDEX_CONTACT;
        String action = intent.getAction();
        String mimeType = intent.getType();
        Log.i("preCreateopenIntent", "openIntent type=" + mimeType);
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(Intent.ACTION_MAIN)) {
                if (("vnd.android.cursor.dir/mms".equals(mimeType))
                        || ("vnd.android-dir/mms-sms".equals(mimeType))
                        || ("vnd.android-dir/mms-sms-lw".equals(mimeType))) {
                    tabIdx = TAB_INDEX_MESSAGE;
                }
            }
            else if (action.equals(Intent.ACTION_VIEW)) {
                if ("vnd.android.cursor.dir/calls".equals(mimeType)) {
                    tabIdx = TAB_INDEX_CALLLOG;
                }
            }
        }

        setDisplayScreenNoSmoothScroll(tabIdx);
        mDetailType = tabIdx;
    }
	
    private void openIntent(Intent intent) {
        if (null == intent) {
            return;
        }

        String action0 = intent.getAction();
		ComponentName component = intent.getComponent();
		String className = null;
		if (null != component) {
           className = component.getClassName();
		}
        Log.e("MainEntry", "openIntent  action0=" + action0);
        if ((!TextUtils.isEmpty(action0) && action0.equals(REQUEST_OPEN_DIALPAD))||
			DIALPAD_ENTRY_COMPONENT.equals(className)) {
            Activity dialpad = getItemActivity(TAB_INDEX_CALLLOG);
            if (dialpad != null && dialpad instanceof DialpadActivity) {
                ((DialpadActivity )dialpad).showDialpad(true);
            }
        }

        
        if (null != className) {
          	if (CALLLOG_ENTRY_COMPONENT.equals(className) || DIALPAD_ENTRY_COMPONENT.equals(className)) {
                Uri telUri = intent.getData();
                if (telUri != null) {
                    String scheme = telUri.getScheme();
                    if ("tel".equals(scheme)) {
                        Activity logActivity = getItemActivity(TAB_INDEX_CALLLOG);
                        if (logActivity != null && logActivity instanceof DialpadActivity) {
                            ((DialpadActivity )logActivity).setNumber(telUri.getSchemeSpecificPart());
                        }
                    }
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        ConversationList activity = (ConversationList)getItemActivity(TAB_INDEX_MESSAGE);
        activity.setIsCurrentPage(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        ConversationList activity = (ConversationList)getItemActivity(TAB_INDEX_MESSAGE);
        if (mDetailType == TAB_INDEX_MESSAGE) {
            activity.setIsCurrentPage(true);
        }
        else {
            activity.setIsCurrentPage(false);
        }
        
        super.onResume();

        if (TAB_INDEX_CALLLOG == mDetailType) {
            Activity curPage = getItemActivity(TAB_INDEX_CALLLOG);
            if ((null != curPage) && (curPage instanceof DialpadActivity)) {
                ((DialpadActivity )curPage).cancelMissedCallNotification();
                //((DialpadActivity )curPage).resetToInitialStatus();
            }
        }
    }

    @Override
    public void onChangePagerTrigger(int currentPage) {
        ConversationList activity = (ConversationList)getItemActivity(TAB_INDEX_MESSAGE);
        
        if(currentPage == TAB_INDEX_MESSAGE){
            activity.setIsCurrentPage(true);
        }
        else {
            activity.setIsCurrentPage(false);            
        }
        mDetailType = currentPage;

        if ((TAB_INDEX_MESSAGE == currentPage) || (TAB_INDEX_CONTACT == currentPage)) {
            Activity logActivity = getItemActivity(TAB_INDEX_CALLLOG);
            if (logActivity instanceof DialpadActivity) {
                ((DialpadActivity )logActivity).resetToInitialStatus();
            }
        }

        if (TAB_INDEX_CALLLOG == currentPage) {
            Activity logActivity = getItemActivity(TAB_INDEX_CALLLOG);
            if (logActivity instanceof DialpadActivity) {
                ((DialpadActivity )logActivity).cancelMissedCallNotification();
            }
        }

        if ((TAB_INDEX_CALLLOG == currentPage) || (TAB_INDEX_FAVORITE == currentPage)) {
            Activity contactActivity = getItemActivity(TAB_INDEX_CONTACT);
            if (contactActivity instanceof ContactsListActivity) {
                ((ContactsListActivity )contactActivity).resetToInitialStatus();
            }
        }

        if (TAB_INDEX_CONTACT == currentPage) {
            Activity contactActivity = getItemActivity(TAB_INDEX_FAVORITE);
            if (contactActivity instanceof ContactsListActivity) {
                ((ContactsListActivity )contactActivity).resetToInitialStatus();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }
    
    private final class RosterDataListQueryHandler extends AsyncQueryHandler{

		public RosterDataListQueryHandler(ContentResolver contentResolver) {
			super(contentResolver);
			// TODO Auto-generated constructor stub
		}
		
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
            case QUER_LIST_ROSTER_DATA:{
            	
            	if (cursor == null || cursor.getCount() < 1) {
					break;
				}
            	
            	ArrayList<String> numberList = new ArrayList<String>();
            	while (cursor.moveToNext()) {
            		numberList.add(cursor.getString(1));
				}            	
            	CommonMethod.CheckUserOnlineStatus(mMainEntryContext, numberList);
            }
            break;
                
            default:
                Log.e("MainEntry", "onQueryComplete called with unknown token " + token);
            }
        }    	
    }
}

