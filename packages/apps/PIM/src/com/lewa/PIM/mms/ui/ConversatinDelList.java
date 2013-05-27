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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Mms;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.mms.pdu.PduHeaders;
import com.lewa.PIM.R;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.mms.LogTag;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.util.DraftCache;
import com.lewa.PIM.mms.util.Recycler;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.util.ContactPhotoLoader;

/**
 * This activity provides a list view of existing conversations.
 */
public class ConversatinDelList extends ListActivity
        implements DraftCache.OnDraftChangedListener,
                android.view.View.OnClickListener,
                PendingContentLoader{
    private static final String TAG = "ConversationList";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG;

    private static final int THREAD_LIST_QUERY_TOKEN       = 1701;
    public static final int DELETE_CONVERSATION_TOKEN      = 1801;
    public static final int HAVE_LOCKED_MESSAGES_TOKEN     = 1802;
    private static final int DELETE_OBSOLETE_THREADS_TOKEN = 1803;
    private static final int HAVE_LOCKED_MESSAGES_TOKEN_MARK = 1804;

    // IDs of the main menu items.
    public static final int MENU_SELECT_ALL          = 0;
    public static final int MENU_UNSELECIT_ALL       = 1;

    private ThreadListQueryHandler mQueryHandler;
    private ConversationDelListAdapter mListAdapter;
    private SharedPreferences mPrefs;
    private Handler mHandler;
    private ListView mListView;    
    private Button mDelButton;
    private Button mMessageMark;
    
    private ContactPhotoLoader mPhotoLoader;
    private Context mConversationDelContext;
    
    static private final String CHECKED_MESSAGE_LIMITS = "checked_message_limits";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mConversationDelContext = this;
        setupContentViewHelper();        
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch (id) {
        case R.id.message_del_done:
            long[] arry = mListAdapter.getSelectArray();  
            if (arry.length > 0) {
                confirmDeleteThread(arry, mQueryHandler);                
            }
            break;
            
        case R.id.message_mark:
            if (mListAdapter.getSelectAll() == true) {
                setAllThreadState(false);
            }else {
                setAllThreadState(true);
            }
            setBtnSelectState(mListAdapter.getSelectAll());
            break;
            
        default:
            break;
        }
    }

    public void setBtnSelectState(boolean isAll){
        String btnStr = "";
        
        if (isAll == false){
            btnStr = getResources().getString(R.string.menu_mark_all);
            mMessageMark.setText(btnStr);
        }else {
            btnStr = getResources().getString(R.string.menu_unmark_all);
            mMessageMark.setText(btnStr);
        }
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
    
    private final ConversationDelListAdapter.OnContentChangedListener mContentChangedListener =
        new ConversationDelListAdapter.OnContentChangedListener() {
        public void onContentChanged(ConversationDelListAdapter adapter) {
            startAsyncQuery();
        }
    };

    private void setupContentViewHelper() {
        setContentView(R.layout.conversation_del_list_screen);
        mQueryHandler = new ThreadListQueryHandler(getContentResolver());

        mListView = getListView();
        mListView.setCacheColorHint(0);
        mListView.setOnKeyListener(mThreadListKeyListener);
        mDelButton = (Button)this.findViewById(R.id.message_del_done);
        mMessageMark = (Button)this.findViewById(R.id.message_mark);
        mDelButton.setOnClickListener(this);
        mMessageMark.setOnClickListener(this);
        
        initListAdapter();
        mHandler = new Handler();
//        boolean checkedMessageLimits = mPrefs.getBoolean(CHECKED_MESSAGE_LIMITS, false);
//        if (DEBUG) Log.v(TAG, "checkedMessageLimits: " + checkedMessageLimits);
//        if (!checkedMessageLimits || DEBUG) {
//            runOneTimeStorageLimitCheckForLegacyMessages();
//        }
    }
    
    private void initListAdapter() {
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mListAdapter = new ConversationDelListAdapter(this, null);
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
                if (Recycler.checkForThreadsOverLimit(ConversatinDelList.this)) {
                    if (DEBUG) Log.v(TAG, "checkForThreadsOverLimit TRUE");
                    // Dang, one or more of the threads are over the limit. Show an activity
                    // that'll encourage the user to manually turn on the setting. Delay showing
                    // this activity until a couple of seconds after the conversation list appears.
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(ConversatinDelList.this,
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
        if (null != mPhotoLoader) {
            mPhotoLoader.stop();            
        }
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
        
        if (null != mPhotoLoader) {
            mPhotoLoader.resume();
        }
        super.onResume();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        startAsyncQuery();
        
        if (!Conversation.loadingThreads()) {
            Contact.invalidateCache();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        DraftCache.getInstance().removeOnDraftChangedListener(this);
        if (null != mListAdapter) {
            mListAdapter.changeCursor(null);
        }
    }

    public void onDraftChanged(final long threadId, final boolean hasDraft) {
        // Run notifyDataSetChanged() on the main thread.
        mQueryHandler.post(new Runnable() {
            public void run() {
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("onDraftChanged: threadId=" + threadId + ", hasDraft=" + hasDraft);
                }
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void startAsyncQuery() {
        try {
            Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

        case MENU_SELECT_ALL:
            setAllThreadState(true);
            break;

        case MENU_UNSELECIT_ALL:
            setAllThreadState(false);
            break;
            
            default:
                return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        CheckBox checkBox = (CheckBox)v.findViewById(R.id.contacts_list_choice);
        checkBox.setChecked(!checkBox.isChecked());
    }

    @Override
    public void loadContent() {
        Log.d(TAG, "loadContent");
        getIntent().putExtra("delayloadcontent", false);
        setupContentViewHelper();
        startAsyncQuery();

        View emptyView = mListView.getEmptyView();
        if ((null != emptyView) && (emptyView instanceof TextView)) {
            ((TextView )emptyView).setText(null);
        }
    }    

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
                mListAdapter.changeCursor(cursor);
                setProgressBarIndeterminateVisibility(false);
                
                if (cursor.getCount() <= 0) {
                    finish();
                }
                break;

            case HAVE_LOCKED_MESSAGES_TOKEN:
                long threadId = (Long)cookie;
                confirmDeleteThreadDialog(new DeleteThreadListener(threadId, mQueryHandler,
                        ConversatinDelList.this), threadId == -1,
                        cursor != null && cursor.getCount() > 0,
                        ConversatinDelList.this);
                break;
            case HAVE_LOCKED_MESSAGES_TOKEN_MARK:
                String s = (String)cookie;
                confirmDeleteThreadDialog(new DeleteThreadListener(-2, s, mQueryHandler,
                        ConversatinDelList.this), true,
                        cursor != null && cursor.getCount() > 0,
                        ConversatinDelList.this);
                break;
                
            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }

            View emptyView = mListView.getEmptyView();
            if (null != emptyView) {
                if (emptyView instanceof TextView) {
                    ((TextView )emptyView).setText(R.string.recentMms_empty);
                }
                else {
                    TextView txtEmpty = (TextView )emptyView.findViewById(R.id.txt_empty);
                    if (null != txtEmpty) {
                        txtEmpty.setText(R.string.recentMms_empty);
                    }
                }
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
            case DELETE_CONVERSATION_TOKEN:
                // Make sure the conversation cache reflects the threads in the DB.
                Conversation.init(ConversatinDelList.this);

                // Update the notification for new messages since they
                // may be deleted.
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(ConversatinDelList.this,
                        false, false);
                // Update the notification for failed messages since they
                // may be deleted.
                MessagingNotification.updateSendFailedNotification(ConversatinDelList.this);

                // Make sure the list reflects the delete
                startAsyncQuery();
                
                if (cookie != null) {
                    mListAdapter.delThreadIdFromSelectMap(cookie.toString());
                }
                String remove = mConversationDelContext.getResources().getString(R.string.remove);
                mDelButton.setText(remove);
                break;
                
            default:
                break;
                
            }
        }
    }

    private void log(String format, Object... args) {
        String s = String.format(format, args);
        Log.d(TAG, "[" + Thread.currentThread().getId() + "] " + s);
    }
}
