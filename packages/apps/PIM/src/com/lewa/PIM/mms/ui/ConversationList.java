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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.provider.Telephony.Sms.Inbox;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.mms.pdu.PduHeaders;
import com.lewa.PIM.R;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.engine.PimEngine.DataEvent;
import com.lewa.PIM.mms.LogTag;
import com.lewa.PIM.mms.MsgPopup.DestktopMessageActivity;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.transaction.SmsRejectedReceiver;
import com.lewa.PIM.mms.util.DraftCache;
import com.lewa.PIM.mms.util.Recycler;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import com.lewa.os.util.ContactPhotoLoader;

/**
 * This activity provides a list view of existing conversations.
 */
public class ConversationList extends ListActivity
        implements DraftCache.OnDraftChangedListener,
                android.view.View.OnClickListener,
                PendingContentLoader,
                ActivityResultReceiver,
                PimEngine.DataEventListener {
	
    private static final String TAG = "ConversationList";
    private static final boolean DEBUG = false;

    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    private static final int HAVE_LOCKED_MESSAGES_TOKEN_MARK = 1804;

    // IDs of the main menu items.
    public static final int MENU_COMPOSE_NEW          = 0;
    public static final int MENU_SEARCH               = 1;
    public static final int MENU_DELETE_ALL           = 3;
    public static final int MENU_PREFERENCES          = 4;
    public static final int MENU_MARK                 = 5;

    // IDs of the context menu items for the list of conversations.
    public static final int MENU_DELETE               = 0;
    public static final int MENU_VIEW                 = 1;
    public static final int MENU_VIEW_CONTACT         = 2;
    public static final int MENU_ADD_TO_CONTACTS      = 3;
    public static final int MENU_CALL_TO_CONTCTS      = 4;
    public static final int MENU_NEW_TO_CONTACTS      = 5;
    public static final int MENU_CLEAR_FROM_BLACKLIST = 6;
    public static final int MENU_ADD_TO_BLACKLIST     = 7;
    
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";
    
    private CharSequence mTitle;
    private SharedPreferences mPrefs;
    private Handler mHandler;
    private boolean mNeedToMarkAsSeen;
    private boolean mIsCurrentPage;
    
    private ViewGroup mMainSoftkeyBar;
    private ImageView mSearch;
    private ImageView mNewMessage;
    private ImageView mOpenDialpad;
    private RelativeLayout mMarkSoftkeyBar;
    private ImageView mMarkAll;
    private ImageView mUnMarkAll;
    private ImageView mDelMark;
    private LinearLayout mEmpty;
    private ListView mListView;
    
    private Context mConversationContext;    
    private ContactPhotoLoader mPhotoLoader;    
    private ConversationReceiver myReceiver;
    private ActivityResultBridge mActivityResultBridge = null;
    private ThreadListQueryHandler mQueryHandler;
    private ConversationListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mTitle = getString(R.string.app_label);
        
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onCreate: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        if (!bDelayLoadContent) {
            setupContentViewHelper();
        }
        mConversationContext = this;
        PimEngine.getInstance(this).addDataListenner(this);
        
        myReceiver = new ConversationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonMethod.ACTION_LEWA_MSG_EMPTY);
        filter.addAction(CommonMethod.ACTION_LEWA_MSG_ON_OFF);
        registerReceiver(myReceiver, filter);
        MessageUtils.pimFirstStart(this);
    }
    
    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        // TODO Auto-generated method stub
        if (code == KeyEvent.KEYCODE_SEARCH) {
            onSearchStart();
            return true;
        }
        return super.onKeyDown(code, event);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch (id) {
        case R.id.btn_search:
            onSearchStart();
            break;
            
        case R.id.btn_open_dialpad:
            CommonMethod.openDialpad(this, null);
            break;
            
        case R.id.btn_new_message:
            createNewMessage();
            break;

        case R.id.btn_mark_all:
            setAllThreadState(true);
            break;

        case R.id.btn_unmark_all:
            setAllThreadState(false);
            break;
        
        case R.id.btn_delete_mark:
            long[] arry = mListAdapter.getSelectArray();  
            if (arry.length > 0) {
                confirmDeleteThread(arry, mQueryHandler);                
            }
            break;
        
        default:
            break;
        }
    }
    
    public void setIsCurrentPage(boolean isCurrent){
        String string = "keyguard";
        KeyguardManager mKeyguardManager = (KeyguardManager)this.getSystemService(string);
        
        if ((isCurrent) && (!mKeyguardManager.inKeyguardRestrictedInputMode())){
            CommonMethod.setConversationListState(true);
            Conversation.markAllConversationsAsSeen(getApplicationContext());
            DestktopMessageActivity.colseMessagePOP();
        }
        else {
            CommonMethod.setConversationListState(false);            
        }
        mIsCurrentPage = isCurrent;
    }
    
    private void setAllThreadState(boolean b){
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mListView.getChildAt(i);
            if (v != null) {
                CheckBox check = (CheckBox)v.findViewById(R.id.contacts_list_choice);
                check.setChecked(b);                    
            }
        }
        
        mListAdapter.setAllThreadState(b);
    }
    
    private final ConversationListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationListAdapter adapter) {
            startAsyncQuery();
        }
    };

    private void setupContentViewHelper() {
        setContentView(R.layout.conversation_list_screen);

        mQueryHandler = new ThreadListQueryHandler(getContentResolver());

        mListView = getListView();
        mListView.setCacheColorHint(0);
        LayoutInflater inflater = LayoutInflater.from(this);

        mListView.setOnCreateContextMenuListener(mConvListOnCreateContextMenuListener);
        mListView.setOnKeyListener(mThreadListKeyListener);

        initListAdapter();

        mHandler = new Handler();
//        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
//        if (DEBUG) Log.v(TAG, "checkedMessageLimits: " + checkedMessageLimits);
//        if (!checkedMessageLimits || DEBUG) {
//            runOneTimeStorageLimitCheckForLegacyMessages();
//        }
        mMainSoftkeyBar = (ViewGroup )this.findViewById(R.id.cl_main_softkey_bar);
        mSearch = (ImageView)this.findViewById(R.id.btn_search);
        mSearch.setOnClickListener(this);
        mOpenDialpad = (ImageView)this.findViewById(R.id.btn_open_dialpad);
        mOpenDialpad.setOnClickListener(this);
        mNewMessage = (ImageView)this.findViewById(R.id.btn_new_message);
        mNewMessage.setOnClickListener(this);
        
        mMarkSoftkeyBar = (RelativeLayout)this.findViewById(R.id.cl_mark_softkey_bar);
        mMarkAll = (ImageView)this.findViewById(R.id.btn_mark_all);
        mMarkAll.setOnClickListener(this);
        mUnMarkAll = (ImageView)this.findViewById(R.id.btn_unmark_all);
        mUnMarkAll.setOnClickListener(this);
        mDelMark = (ImageView)this.findViewById(R.id.btn_delete_mark);
        mDelMark.setOnClickListener(this);
        
        mEmpty = (LinearLayout)findViewById(R.id.empty);
    }
    
    private void initListAdapter() {
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mListAdapter = new ConversationListAdapter(this, null);
        mListAdapter.setPhotoLoader(mPhotoLoader);
        
        mListAdapter.setOnContentChangedListener(mContentChangedListener);
        setListAdapter(mListAdapter);
        getListView().setRecyclerListener(mListAdapter);
    }

    /**
     * Checks to see if the number of MMS and SMS messages are under the limits for the
     * recycler. If so, it will automatically turn on the recycler setting. If not, it
     * will prompt the user with a message and point them to the setting to manually
     * turn on the recycler.
     */
    public synchronized void runOneTimeStorageLimitCheckForLegacyMessages() {
        if (Recycler.isAutoDeleteEnabled(this)) {
            if (DEBUG) Log.v(TAG, "recycler is already turned on");
            // The recycler is already turned on. We don't need to check anything or warn
            // the user, just remember that we've made the check.
            markCheckedMessageLimit();
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                if (Recycler.checkForThreadsOverLimit(ConversationList.this)) {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit TRUE");
                    // Dang, one or more of the threads are over the limit. Show an activity
                    // that'll encourage the user to manually turn on the setting. Delay showing
                    // this activity until a couple of seconds after the conversation list appears.
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(ConversationList.this,
                                    WarnOfStorageLimitsActivity.class);
                            startActivity(intent);
                        }
                    }, 2000);
                } else {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit silently turning on recycler");
                    // No threads were over the limit. Turn on the recycler by default.
                    runOnUiThread(new Runnable() {
                        public void run() {
//                            SharedPreferences.Editor editor = mPrefs.edit();
//                            editor.putBoolean(MessagingPreferenceActivity.AUTO_DELETE, true);
//                            editor.commit(); //editor.apply(); the android with lower version dosn't contain the apply method
                        }
                    });
                }
                // Remember that we don't have to do the check anymore when starting MMS.
                runOnUiThread(new Runnable() {
                    public void run() {
                        markCheckedMessageLimit();
                    }
                });
            }
        }).start();
    }

    /**
     * Mark in preferences that we've checked the user's message limits. Once checked, we'll
     * never check them again, unless the user wipe-data or resets the device.
     */
    private void markCheckedMessageLimit() {
        if (DEBUG) Log.v(TAG, "markCheckedMessageLimit");
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(CHECKED_MESSAGE_LIMITS, true);
        editor.commit(); //editor.apply(); the android with lower version dosn't contain the apply method
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Handle intents that occur after the activity has already been created.
        startAsyncQuery();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        CommonMethod.setConversationListState(false);
        PimEngine.getInstance(this).removeDataListenner(this);
        if (mListAdapter != null) {
            mListAdapter.removeAllList();            
        }
        
        if (null != mPhotoLoader) {
            mPhotoLoader.stop();            
        }
        mActivityResultBridge = null;
        
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);			
		}
        
        MessageUtils.clearPhotoIdMap();
        super.onDestroy();
    }
    
    @Override
	protected void onPause() {		
		super.onPause();
		CommonMethod.setConversationListState(false);
	}

	@Override
    protected void onResume() {
        super.onResume();
        
        if (null != mPhotoLoader) {
            mPhotoLoader.resume();
        }        
        CommonMethod.setConversationListState(true);
    }
    
    @Override
    protected void onStart() {
        super.onStart();

        MessagingNotification.cancelNotification(getApplicationContext(),
                SmsRejectedReceiver.SMS_REJECTED_NOTIFICATION_ID);

        DraftCache.getInstance().addOnDraftChangedListener(this);

        mNeedToMarkAsSeen = true;

        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onStart: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        
		MessageUtils.clearImsNumberStateMap();
		MessageUtils.queryImsNumberState(mConversationContext.getContentResolver());
		
        if (!bDelayLoadContent) {
            startAsyncQuery();
        }

        // We used to refresh the DraftCache here, but
        // refreshing the DraftCache each time we go to the ConversationList seems overly
        // aggressive. We already update the DraftCache when leaving CMA in onStop() and
        // onNewIntent(), and when we delete threads or delete all in CMA or this activity.
        // I hope we don't have to do such a heavy operation each time we enter here.

        // we invalidate the contact cache here because we want to get updated presence
        // and any contact changes. We don't invalidate the cache by observing presence and contact
        // changes (since that's too untargeted), so as a tradeoff we do it here.
        // If we're in the middle of the app initialization where we're loading the conversation
        // threads, don't invalidate the cache because we're in the process of building it.
        // TODO: think of a better way to invalidate cache more surgically or based on actual
        // TODO: changes we care about
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
        
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	        MessageUtils.checkSDCardSize(mConversationContext);
		}	
    }

    @Override
    protected void onStop() {
        super.onStop();

        DraftCache.getInstance().removeOnDraftChangedListener(this);
        if (null != mListAdapter) {
            mListAdapter.changeCursor(null);
        }

        if (mHandler != null){
            mHandler.removeCallbacks(RefreshUIRunable);
        }
    }

	@Override
	public void onBackPressed() {	
		if (null != mActivityResultBridge) {
             mActivityResultBridge.handleActivityEvent(
                     this,
                     ActivityResultBridge.EVT_ACTIVITY_ON_BACK_PRESSED,
                     null);
         }
	}
    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        // Run notifyDataSetChanged() on the main thread.
        mQueryHandler.post(new Runnable() {
            public void run() {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("onDraftChanged: threadId=" + threadId + ", hasDraft=" + hasDraft);
                }
                if (mListAdapter != null) {
                    mListAdapter.notifyDataSetChanged();					
				}
            }
        });
    }

    private void startAsyncQuery() {
        try {
            setTitle(getString(R.string.refreshing));
            setProgressBarIndeterminateVisibility(true);

            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if ((null == mListAdapter) || mListAdapter.getMarkState()) {
            return true;
        }
//        menu.add(0, MENU_COMPOSE_NEW, 0, R.string.menu_compose_new).setIcon(
//                com.android.internal.R.drawable.ic_menu_compose);

        if (mListAdapter.getCount() > 0) {
            menu.add(0, MENU_DELETE_ALL, 0, R.string.menu_delete_all).setIcon(
                    R.drawable.ic_menu_delete);
            
//            menu.add(0, MENU_MARK, 0, R.string.menu_mark).setIcon(
//                    R.drawable.menu_icon_select);
        }

//        menu.add(0, MENU_SEARCH, 0, android.R.string.search_go).
//            setIcon(android.R.drawable.ic_menu_search).
//            setAlphabeticShortcut(android.app.SearchManager.MENU_KEY);

        menu.add(0, MENU_PREFERENCES, 0, R.string.menu_preferences).setIcon(
                android.R.drawable.ic_menu_set_as );

        return true;
    }

    @Override
    public boolean onSearchRequested() {
        startSearch(null, false, null /*appData*/, false);
        return true;
    }

    private void setMarkStart(boolean b){
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mListView.getChildAt(i);
            if (v != null) {
                CheckBox check = (CheckBox)v.findViewById(R.id.contacts_list_choice);
                if (b) {
                    check.setVisibility(View.VISIBLE);                    
                }else {
                    check.setChecked(false);
                    check.setVisibility(View.GONE);                    
                }
            }
        }
        mListAdapter.setMarkState(b);
       if (b) {           
            mMainSoftkeyBar.setVisibility(View.GONE);
            mMarkSoftkeyBar.setVisibility(View.VISIBLE);
       }else {
           mMainSoftkeyBar.setVisibility(View.VISIBLE);
           mMarkSoftkeyBar.setVisibility(View.GONE);        
       }
    }
    
    private void onSearchStart(){
        Intent intent = new Intent(ConversationList.this, SearchMessageActivity.class);
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MENU_COMPOSE_NEW:
                createNewMessage();
                break;
            case MENU_SEARCH:
                onSearchStart();
                break;
            case MENU_DELETE_ALL:
                Intent delIntent = new Intent(this, ConversatinDelList.class);
                //startActivityIfNeeded(intent, -1);
                startActivity(delIntent);
                // The invalid threadId of -1 means all threads here.
                //confirmDeleteThread(-1L, mQueryHandler);
                break;
            case MENU_PREFERENCES: {
                Intent intent = new Intent(this, MessagingPreferenceActivity.class);
                //startActivityIfNeeded(intent, -1);
                startActivity(intent);
                }
                break;                
            case MENU_MARK: {
                setMarkStart(true);
                break;
            }
            default:
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Note: don't read the thread id data from the ConversationListItem view passed in.
        // It's unreliable to read the cached data stored in the view because the ListItem
        // can be recycled, and the same view could be assigned to a different position
        // if you click the list item fast enough. Instead, get the cursor at the position
        // clicked and load the data from the cursor.
        // (ConversationListAdapter extends CursorAdapter, so getItemAtPosition() should
        // return the cursor object, which is moved to the position passed in)
        if (mListAdapter.getMarkState()) {
            CheckBox check = (CheckBox)v.findViewById(R.id.contacts_list_choice);
            check.setChecked(!check.isChecked());          
        }else {
            Cursor cursor  = (Cursor) getListView().getItemAtPosition(position);
            Conversation conv = Conversation.from(this, cursor);
            long tid = conv.getThreadId();            
            ConversationListItem item = (ConversationListItem)v;
            openThread(tid, item.getConversationHeader());

        }
    }

    @Override
    public void loadContent() {
        Log.d(TAG, "loadContent");
        getIntent().putExtra("delayloadcontent", false);
        setupContentViewHelper();
        startAsyncQuery();

//        View emptyView = mListView.getEmptyView();
//        if ((null != emptyView) && (emptyView instanceof TextView)) {
//            ((TextView )emptyView).setText(null);
//        }
    }
    
    private void createNewMessage() {
        startActivity(NewMessageComposeActivity.createIntent(this, 0));
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void openThread(long threadId, ConversationListItemData ch) {

        if (null != ch) { 
            if (ch.getContacts().size() == 1){
                Contact contact = ch.getContacts().get(0);
                
                boolean isEffectiveAddress = PhoneNumberUtils.isWellFormedSmsAddress(contact.getNumber()) || 
                                                Mms.isEmailAddress(contact.getNumber());
                if (isEffectiveAddress) {
                    Intent intent = ComposeMessageActivity.createIntent(this, threadId);
                    intent.putExtra("number", contact.getNumber());
                    startActivity(intent);                    
                }else {
                    startActivity(NewMessageComposeActivity.createIntent(this, threadId));
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);                    
                }
                
            }else{
                startActivity(NewMessageComposeActivity.createIntent(this, threadId));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        }        
    }

    private void openThread(long threadId, Conversation conv) {

        if (null != conv) { 
            if (conv.getRecipients().size() == 1){
                Contact contact = conv.getRecipients().get(0);
                CommonMethod.viewPimDetail(this, contact.getName(), 
                        contact.getNumber(), 0, DetailEntry.MESSAGE_DETAIL);
            }else{
                startActivity(NewMessageComposeActivity.createIntent(this, threadId));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        }        
    }    
    
    public static Intent createAddContactIntent(String address) {
        // address must be a single recipient
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(Contacts.CONTENT_ITEM_TYPE);
        if (Mms.isEmailAddress(address)) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, address);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, address);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        return intent;
    }

    private final OnCreateContextMenuListener mConvListOnCreateContextMenuListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            if (mListAdapter.getMarkState()) {
                return;
            }
            
            Cursor cursor = mListAdapter.getCursor();
            if (cursor == null || cursor.getPosition() < 0) {
                return;
            }
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            ContactList recipients = conv.getRecipients();
            menu.setHeaderTitle(recipients.formatNames(","));

            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info.position >= 0) {
                menu.add(0, MENU_VIEW, 0, R.string.menu_view);

                // Only show if there's a single recipient
                if (recipients.size() == 1) {
                    // do we have this recipient in contacts?
                    if (recipients.get(0).existsInDatabase()) {
                        menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact);
                    } else {
                        menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts);
                        menu.add(0, MENU_NEW_TO_CONTACTS, 0, R.string.menu_newContact);                        
                    }
                    
                    menu.add(0, MENU_CALL_TO_CONTCTS, 0, R.string.call_contact);
                    Contact contact = recipients.get(0);
                    
                    if (CommonMethod.numberIsInBlacklist(mConversationContext, contact.getNumber())) {
                        menu.add(0, MENU_CLEAR_FROM_BLACKLIST, 0, R.string.clear_from_blacklist);
                    }
                    else {
                        menu.add(0, MENU_ADD_TO_BLACKLIST, 0, R.string.add_to_blacklist);
                    }
                }
                menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
            }
            
            if (null != mActivityResultBridge) {
                mActivityResultBridge.handleActivityEvent(
                        (Activity)mConversationContext,
                        ActivityResultBridge.EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU,
                        null);
            }           
        }                
    };        
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && cursor.getPosition() >= 0) {
            Conversation conv = Conversation.from(ConversationList.this, cursor);
            long threadId = conv.getThreadId();
            switch (item.getItemId()) {
            case MENU_DELETE: {
                confirmDeleteThread(threadId, mQueryHandler);
                break;
            }
            case MENU_VIEW: {
                openThread(threadId, conv);
                break;
            }
            case MENU_VIEW_CONTACT: {
                Contact contact = conv.getRecipients().get(0);
                Intent intent = new Intent(Intent.ACTION_VIEW, contact.getUri());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
                break;
            }
            case MENU_ADD_TO_CONTACTS: {
                String address = conv.getRecipients().get(0).getNumber();
                startActivity(createAddContactIntent(address));
                break;
            }
            
            case MENU_NEW_TO_CONTACTS: {
                String number = conv.getRecipients().get(0).getNumber();
                CommonMethod.newContact(this, number);
                break;
            }
            case MENU_CALL_TO_CONTCTS: {
                String address = conv.getRecipients().get(0).getNumber();
                CommonMethod.call(this, address);
                break;
            }            
            case MENU_ADD_TO_BLACKLIST:{
                CommonMethod.addToBlacklist(this, conv.getRecipients().get(0).getName(), 
                                            conv.getRecipients().get(0).getNumber(), true);
                conv.clearThreadId();
                break;                
            }

            case MENU_CLEAR_FROM_BLACKLIST:{
                CommonMethod.clearFromBlacklist(this, conv.getRecipients().get(0).getName(), 
                        conv.getRecipients().get(0).getNumber());
                break;                
            }
                
            default:
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // We override this method to avoid restarting the entire
        // activity when the keyboard is opened (declared in
        // AndroidManifest.xml).  Because the only translatable text
        // in this activity is "New Message", which has the full width
        // of phone to work with, localization shouldn't be a problem:
        // no abbreviated alternate words should be needed even in
        // 'wide' languages like German or Russian.

        super.onConfigurationChanged(newConfig);
        if (DEBUG) Log.v(TAG, "onConfigurationChanged: " + newConfig);
    }

    /**
     * Start the process of putting up a dialog to confirm deleting a thread,
     * but first start a background query to see if any of the threads or thread
     * contain locked messages so we'll know how detailed of a UI to display.
     * @param threadId id of the thread to delete or -1 for all threads
     * @param handler query handler to do the background locked query
     */
    public static void confirmDeleteThread(long threadId, AsyncQueryHandler handler) {
        Conversation.startQueryHaveLockedMessages(handler, threadId,
                HAVE_LOCKED_MESSAGES_TOKEN);
    }

    public static void confirmDeleteThread(long[] threadId, AsyncQueryHandler handler) {
        Conversation.startQueryHaveLockedMessages(handler, threadId,
                HAVE_LOCKED_MESSAGES_TOKEN_MARK);
    }

    /**
     * Build and show the proper delete thread dialog. The UI is slightly different
     * depending on whether there are locked messages in the thread(s) and whether we're
     * deleting a single thread or all threads.
     * @param listener gets called when the delete button is pressed
     * @param deleteAll whether to show a single thread or all threads UI
     * @param hasLockedMessages whether the thread(s) contain locked messages
     * @param context used to load the various UI elements
     */
    public static void confirmDeleteThreadDialog(final DeleteThreadListener listener,
            boolean deleteAll,
            boolean hasLockedMessages,
            Context context) {
        View contents = View.inflate(context, R.layout.delete_thread_dialog_view, null);
        TextView msg = (TextView)contents.findViewById(R.id.message);
        msg.setText(deleteAll
                ? R.string.confirm_delete_all_conversations
                        : R.string.confirm_delete_conversation);
        final CheckBox checkbox = (CheckBox)contents.findViewById(R.id.delete_locked);
        if (!hasLockedMessages) {
            checkbox.setVisibility(View.GONE);
        } else {
            listener.setDeleteLockedMessage(checkbox.isChecked());
            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listener.setDeleteLockedMessage(checkbox.isChecked());
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_dialog_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
        .setCancelable(true)
        .setPositiveButton(R.string.delete, listener)
        .setNegativeButton(R.string.no, null)
        .setView(contents)
        .show();
    }

    private final OnKeyListener mThreadListKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL: {
                        long id = getListView().getSelectedItemId();
                        if (id > 0) {
                            confirmDeleteThread(id, mQueryHandler);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    };

    public static class DeleteThreadListener implements OnClickListener {
        private final long mThreadId;
        private final AsyncQueryHandler mHandler;
        private final Context mContext;
        private boolean mDeleteLockedMessages;
        private final String mThreadIdList;

        public DeleteThreadListener(long threadId, AsyncQueryHandler handler, Context context) {
            mThreadId = threadId;
            mHandler = handler;
            mContext = context;
            mThreadIdList = null;
        }

        public DeleteThreadListener(long threadId, String threadList, AsyncQueryHandler handler, Context context) {
            mThreadId = threadId;
            mHandler = handler;
            mContext = context;
            mThreadIdList = threadList;
        }

        public void setDeleteLockedMessage(boolean deleteLockedMessages) {
            mDeleteLockedMessages = deleteLockedMessages;
        }

        public void onClick(DialogInterface dialog, final int whichButton) {
            MessageUtils.handleReadReport(mContext, mThreadId, mThreadIdList,
                    PduHeaders.READ_STATUS__DELETED_WITHOUT_BEING_READ, new Runnable() {
                public void run() {
                    int token = DELETE_CONVERSATION_TOKEN;
                    if (mThreadId == -1) {
                        Conversation.startDeleteAll(mHandler, token, mDeleteLockedMessages);
                        DraftCache.getInstance().refresh();
                    }else if (mThreadId == -2) {
                        Conversation.startDelete(mHandler, token, mDeleteLockedMessages,
                                mThreadIdList);
                        DraftCache.getInstance().refresh();                       
                    }else {
                        Conversation.startDelete(mHandler, token, mDeleteLockedMessages,
                                mThreadId);
                        DraftCache.getInstance().setDraftState(mThreadId, false);
                    }
                }
            });
            dialog.dismiss();
        }
    }

    private final class ThreadListQueryHandler extends AsyncQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:
                                
                if (cursor.getCount() > 0) {
                    mEmpty.setVisibility(View.GONE);
                }else {
                    mEmpty.setVisibility(View.VISIBLE);
                }
                
                mListAdapter.changeCursor(cursor);
                setTitle(mTitle);
                setProgressBarIndeterminateVisibility(false);

                if (mIsCurrentPage) {
//                    Conversation.markAllConversationsAsSeen(getApplicationContext());                        
                }

                if (mNeedToMarkAsSeen) {
                    mNeedToMarkAsSeen = false;
                    
                    // Delete any obsolete threads. Obsolete threads are threads that aren't
                    // referenced by at least one message in the pdu or sms tables.
                    Conversation.asyncDeleteObsoleteThreads(mQueryHandler,
                            DELETE_OBSOLETE_THREADS_TOKEN);
                }
                break;

            case HAVE_LOCKED_MESSAGES_TOKEN:
                long threadId = (Long)cookie;
                confirmDeleteThreadDialog(new DeleteThreadListener(threadId, mQueryHandler,
                        ConversationList.this), threadId == -1,
                        cursor != null && cursor.getCount() > 0,
                        ConversationList.this);
                break;
            case HAVE_LOCKED_MESSAGES_TOKEN_MARK:
                String s = (String)cookie;
                confirmDeleteThreadDialog(new DeleteThreadListener(-2, s, mQueryHandler,
                        ConversationList.this), true,
                        cursor != null && cursor.getCount() > 0,
                        ConversationList.this);
                break;
                
            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
            case DELETE_CONVERSATION_TOKEN:
                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ConversationList.this);

                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversationList.this,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.updateSendFailedNotification(ConversationList.this);

                // Make sure the list reflects the delete
                startAsyncQuery();
                
                if (cookie != null) {
                    mListAdapter.delThreadIdFromSelectMap(cookie.toString());
                }
                break;

            case DELETE_OBSOLETE_THREADS_TOKEN:
                // Nothing to do here.
                break;
            }
        }
    }

    private void log(String format, Object... args) {
        String s = String.format(format, args);
        Log.d(TAG, "[" + Thread.currentThread().getId() + "] " + s);
    }
    
    private class ConversationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null && mQueryHandler != null) {
                startAsyncQuery();                
            }
        }
    };

    Runnable RefreshUIRunable = new Runnable() {
	 public void run() {
	     mPhotoLoader.clear();
            Contact.invalidateCache();
            MessageUtils.clearPhotoIdMap();
            mListAdapter.reloadPohotId();
            startAsyncQuery();          
	 	}
    };
    
    Runnable RefreshIMSRunable = new Runnable() {
		public void run() {
			MessageUtils.clearImsNumberStateMap();
			MessageUtils.queryImsNumberState(mConversationContext.getContentResolver());
			startAsyncQuery();     
		}
    };
    
    @Override
    public void onDataEvent(DataEvent event, int state) {
    	
    	if (mQueryHandler == null || mListAdapter == null) {
			return;
		}
        if (PimEngine.DataEvent.CONTACTS_CHANGED == event) {
        	mHandler.removeCallbacks(RefreshUIRunable);
        	mHandler.postDelayed(RefreshUIRunable,600);
        }else if (PimEngine.DataEvent.ROSTERDATA_CHANGED == event) {
        	mHandler.removeCallbacks(RefreshIMSRunable);
        	mHandler.postDelayed(RefreshIMSRunable,2000);			
		}
    }

    @Override
    public void handleActivityResult(
            ActivityResultReceiver realReceiver,
            int requestCode,
            int resultCode,
            Intent intent) {
        
    }

    @Override
    public void registerActivityResultBridge(ActivityResultBridge bridge) {
        mActivityResultBridge = bridge;        
    }

}
