/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.lewa.PIM.contacts;

import com.lewa.PIM.R;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.widget.ContactHeaderWidget;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.lewa.PIM.contacts.Collapser.Collapsible;
import com.lewa.PIM.contacts.model.ContactsSource;
import com.lewa.PIM.contacts.model.Sources;
import com.lewa.PIM.contacts.model.ContactsSource.DataKind;
import com.lewa.PIM.contacts.ui.EditContactActivity;
import com.lewa.PIM.contacts.ui.SwitcherHorizontalScrollView;
import com.lewa.PIM.contacts.util.Constants;
import com.lewa.PIM.contacts.util.DataStatus;
import com.lewa.PIM.contacts.util.NotifyingAsyncQueryHandler;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.engine.PimEngine.DataEvent;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.layout.LayoutParameters;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.LocationUtil;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import com.lewa.os.ui.PendingContentLoader;

import android.R.anim;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.Entity.NamedContentValues;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.DisplayNameSources;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.Roster;
import android.provider.ContactsContract.RosterData;
import android.provider.ContactsContract.StatusUpdates;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Wysie
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.ComponentName;
/**
 * Displays the details of a specific contact.
 */
public class ViewContactActivity extends Activity
        implements View.OnCreateContextMenuListener, DialogInterface.OnClickListener,
        AdapterView.OnItemClickListener, NotifyingAsyncQueryHandler.AsyncQueryListener,
        ActivityResultReceiver,
        PendingContentLoader,
        PimEngine.DataEventListener{
    private static final String TAG = "ViewContact";

    private static final boolean SHOW_SEPARATORS = false;

    private static final int DIALOG_CONFIRM_DELETE = 1;
    private static final int DIALOG_CONFIRM_READONLY_DELETE = 2;
    private static final int DIALOG_CONFIRM_MULTIPLE_DELETE = 3;
    private static final int DIALOG_CONFIRM_READONLY_HIDE = 4;

    private static final int REQUEST_JOIN_CONTACT = 1;
    private static final int REQUEST_EDIT_CONTACT = 2;

    public static final int MENU_ITEM_MAKE_DEFAULT = 3;
    public static final int MENU_ITEM_CANCEL_DEFAULT = 4;
    public static final int MENU_ITEM_CALL = 5;
    public static final int MENU_ITEM_SHARE = 6;

    private static final String PLAIN_TEXT_TYPE = "text/plain";
    
    private static final String YILIAO_PHONE_CONTENT_ITEM_TYPE = "yiliao/phone";
    private static final String YILIAO_REMARK_CONTENT_ITEM_TYPE = "yiliao/remark";
    private static final String YILIAO_EMAIL_CONTENT_ITEM_TYPE = "yiliao/email";
    protected Uri mLookupUri;
    protected long mRosterId;
    private ContentResolver mResolver;
    private ViewAdapter mAdapter;
    private int mNumPhoneNumbers = 0;

    /**
     * A list of distinct contact IDs included in the current contact.
     */
    private ArrayList<Long> mRawContactIds = new ArrayList<Long>();

    private ArrayList<String> mPhoneNumbers = new ArrayList<String>();

    /* package */ ArrayList<ViewEntry> mRemarkEntries = new ArrayList<ViewEntry>();  //for yiliao, jxli
    /* package */ ArrayList<ViewEntry> mPhoneEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mYPhoneEntries = new ArrayList<ViewEntry>();  //for yiliao, jxli
    /* package */ ArrayList<ViewEntry> mSmsEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mEmailEntries = new ArrayList<ViewEntry>(); 
    /* package */ ArrayList<ViewEntry> mYEmailEntries = new ArrayList<ViewEntry>();//for yiliao, jxli
    /* package */ ArrayList<ViewEntry> mPostalEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mImEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mNicknameEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mOrganizationEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mGroupEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mOtherEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ArrayList<ViewEntry>> mSections = new ArrayList<ArrayList<ViewEntry>>();

    private Cursor mCursor;
    private Cursor mRosterCursor;
    
    protected ContactHeaderWidget mContactHeaderWidget;
    private NotifyingAsyncQueryHandler mHandler;

    protected LayoutInflater mInflater;

    protected int mReadOnlySourcesCnt;
    protected int mWritableSourcesCnt;
    protected boolean mAllRestricted;

    protected Uri mPrimaryPhoneUri = null;

    protected ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();

    private static final int TOKEN_ENTITIES = 0;
    private static final int TOKEN_STATUSES = 1;
    private static final int TOKEN_YILIAO = 2;

    private ActivityResultBridge mActivityResultBridge = null;

    private boolean mHasEntities = false;
    private boolean mHasStatuses = false;

    private long mNameRawContactId = -1;
    private int mDisplayNameSource = DisplayNameSources.UNDEFINED;

    private ArrayList<Entity> mEntities = Lists.newArrayList();
    private HashMap<Long, DataStatus> mStatuses = Maps.newHashMap();

    private String mUnknowNumber;
    /**
     * The view shown if the detail list is empty.
     * We set this to the list view when first bind the adapter, so that it won't be shown while
     * we're loading data.
     */
    private View mEmptyView;
    private RelativeLayout stickerPhotoItem;
    private LinearLayout itemLayout;
    private TextView stickerSetTxt,stickerShowTxt;
    private View startLine,endLine;
    private long mContactId;
    private Context mContext;
    
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null && !mCursor.isClosed()) {
                startEntityQuery();
            }
        }
    };

    public void onClick(DialogInterface dialog, int which) {
        closeCursor();
        getContentResolver().delete(mLookupUri, null, null);
        finish();
    }

    private ListView mListView;
    private boolean mShowSmsLinksForAllPhones;    
    
    //Wysie
    private SharedPreferences ePrefs;

    @Override
    public void registerActivityResultBridge(ActivityResultBridge bridge) {
        mActivityResultBridge = bridge;
    }

    @Override
    public void handleActivityResult(ActivityResultReceiver realReceiver,
            int requestCode, int resultCode, Intent intent) {
        if ((null != mHandler) //the this object has initiated
                && ((realReceiver == this)
                    || (DetailEntry.REQUEST_NEW_CONTACT == requestCode)
                    || (DetailEntry.REQUEST_ADD_TO_CONTACTS == requestCode)
                    || (DetailEntry.REQUEST_ON_NEW_INTENT == requestCode)
                    || (REQUEST_EDIT_CONTACT == requestCode))) {
            onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        ePrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        final Intent intent = getIntent();
        Uri data = intent.getData();
        String authority = data.getAuthority();
        String path = data.getPath();
        if (ContactsContract.AUTHORITY.equals(authority)) {
            if (path.contains("roster")) {
                mRosterId = ContentUris.parseId(data);
            } else {
            mLookupUri = data;
            if (ContentUris.parseId(data) == 0)
                mUnknowNumber = intent.getStringExtra("number");
            }
        } else if (android.provider.Contacts.AUTHORITY.equals(authority)) {
            final long rawContactId = ContentUris.parseId(data);
            mLookupUri = RawContacts.getContactLookupUri(getContentResolver(),
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));

        }
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = ViewContactActivity.this;
        mResolver = getContentResolver();
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onCreate: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        if (!bDelayLoadContent) {
            setupContentViewHelper();
        }

        // Build the list of sections. The order they're added to mSections dictates the
        // order they are displayed in the list.
        mSections.add(mRemarkEntries);
        mSections.add(mPhoneEntries);
        mSections.add(mYPhoneEntries);
        mSections.add(mSmsEntries);
        mSections.add(mEmailEntries);
        mSections.add(mYEmailEntries);
        mSections.add(mImEntries);
        mSections.add(mPostalEntries);
        mSections.add(mNicknameEntries);
        mSections.add(mOrganizationEntries);
        mSections.add(mGroupEntries);
        mSections.add(mOtherEntries);

        //TODO Read this value from a preference
        //mShowSmsLinksForAllPhones = true;
        PimEngine.getInstance(this).addDataListenner(this);
        queryRosterList(getContentResolver());
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        //Wysie: Read from preference
        mShowSmsLinksForAllPhones = !ePrefs.getBoolean("contacts_show_text_mobile_only", false);        
        
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onResume: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        if (!bDelayLoadContent) {
            startEntityQuery();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCursor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCursor();
        mActivityResultBridge = null;
        PimEngine.getInstance(this).removeDataListenner(this);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONFIRM_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .setCancelable(false)
                        .create();
            case DIALOG_CONFIRM_READONLY_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .setCancelable(false)
                        .create();
            case DIALOG_CONFIRM_MULTIPLE_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.multipleContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .setCancelable(false)
                        .create();
            case DIALOG_CONFIRM_READONLY_HIDE: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactWarning)
                        .setPositiveButton(android.R.string.ok, this)
                        .create();
            }

        }
        return null;
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, final Cursor cursor) {
        if (token == TOKEN_STATUSES) {
            try {
                // Read available social rows and consider binding
                readStatuses(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            considerBindData();
            return;
        }

        // One would think we could just iterate over the Cursor
        // directly here, as the result set should be small, and we've
        // already run the query in an AsyncTask, but a lot of ANRs
        // were being reported in this code nonetheless.  See bug
        // 2539603 for details.  The real bug which makes this result
        // set huge and CPU-heavy may be elsewhere.
        // TODO: if we keep this async, perhaps the entity iteration
        // should also be original AsyncTask, rather than ping-ponging
        // between threads like this.
        final ArrayList<Entity> oldEntities = mEntities;
        (new AsyncTask<Void, Void, ArrayList<Entity>>() {
            @Override
            protected ArrayList<Entity> doInBackground(Void... params) {
                ArrayList<Entity> newEntities = new ArrayList<Entity>(cursor.getCount());
                EntityIterator iterator = RawContacts.newEntityIterator(cursor);
                try {
                    while (iterator.hasNext()) {
                        Entity entity = iterator.next();
                        newEntities.add(entity);
                    }
                } finally {
                    iterator.close();
                }
                return newEntities;
            }

            @Override
            protected void onPostExecute(ArrayList<Entity> newEntities) {
                if (newEntities == null) {
                    // There was an error loading.
                    return;
                }
                synchronized (ViewContactActivity.this) {
                    if (mEntities != oldEntities) {
                        // Multiple async tasks were in flight and we
                        // lost the race.
                        return;
                    }
                    mEntities = newEntities;
                    mHasEntities = true;
                }
                considerBindData();
            }
        }).execute();
    }

    @Override
    public void loadContent() {
        Log.d(TAG, "loadContent");
        getIntent().putExtra("delayloadcontent", false);
        setupContentViewHelper();
        startEntityQuery();
    }

    private void setupContentViewHelper() {
        setContentView(R.layout.contact_card_layout);

        mContactHeaderWidget = (ContactHeaderWidget) findViewById(R.id.contact_header_widget);
        mContactHeaderWidget.showStar(true);
        mContactHeaderWidget.setExcludeMimes(new String[] {
            Contacts.CONTENT_ITEM_TYPE
        });
        mContactHeaderWidget.setSelectedContactsAppTabIndex(StickyTabs.getTab(getIntent()));
        mContactHeaderWidget.setVisibility(View.GONE);

        mHandler = new NotifyingAsyncQueryHandler(this, this);

        mListView = (ListView) findViewById(R.id.contact_data);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOnItemClickListener(this);
        // Don't set it to mListView yet.  We do so later when we bind the adapter.
        mEmptyView = findViewById(android.R.id.empty);
        
        stickerPhotoItem = (RelativeLayout)findViewById(R.id.sticker_photo_item);
        itemLayout = (LinearLayout)findViewById(R.id.item_layout);
        stickerSetTxt = (TextView)findViewById(R.id.stickerSet);
        stickerShowTxt = (TextView)findViewById(R.id.stickerShow);
        stickerPhotoItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Drawable drawable = ContactsUtils.getStickerPhoto(mResolver, mContactId);
				ContactsUtils.showStickerPhotoPreviewDialog(mContext,drawable);
			}
		});
        //startLine = (View)findViewById(R.id.start_line);
        endLine = (View)findViewById(R.id.end_line);
        endLine.setVisibility(View.GONE);
    }

    private long getRefreshedContactId() {
        Uri freshContactUri = Contacts.lookupContact(getContentResolver(), mLookupUri);
        if (freshContactUri != null) {
            return ContentUris.parseId(freshContactUri);
        }
        return -1;
    }

    /**
     * Read from the given {@link Cursor} and build a set of {@link DataStatus}
     * objects to match any valid statuses found.
     */
    private synchronized void readStatuses(Cursor cursor) {
        mStatuses.clear();

        // Walk found statuses, creating internal row for each
        while (cursor.moveToNext()) {
            final DataStatus status = new DataStatus(cursor);
            final long dataId = cursor.getLong(StatusQuery._ID);
            mStatuses.put(dataId, status);
        }

        mHasStatuses = true;
    }

    private static Cursor setupContactCursor(ContentResolver resolver, Uri lookupUri) {
        if (lookupUri == null) {
            return null;
        }
        final List<String> segments = lookupUri.getPathSegments();
        if (segments.size() != 4) {
            return null;
        }

        // Contains an Id.
        final long uriContactId = Long.parseLong(segments.get(3));
        final String uriLookupKey = Uri.encode(segments.get(2));
        final Uri dataUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, uriContactId),
                Contacts.Data.CONTENT_DIRECTORY);

        // This cursor has several purposes:
        // - Fetch NAME_RAW_CONTACT_ID and DISPLAY_NAME_SOURCE
        // - Fetch the lookup-key to ensure we are looking at the right record
        // - Watcher for change events
        Cursor cursor = resolver.query(dataUri,
                new String[] {
                    Contacts.NAME_RAW_CONTACT_ID,
                    Contacts.DISPLAY_NAME_SOURCE,
                    Contacts.LOOKUP_KEY,
                    Contacts.STARRED
                }, null, null, null);

        if (cursor.moveToFirst()) {
            String lookupKey =
                    cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
            if (!lookupKey.equals(uriLookupKey)) {
                // ID and lookup key do not match
                cursor.close();
                return null;
            }
            return cursor;
        } else {
            cursor.close();
            return null;
        }
    }

    private synchronized void startEntityQuery() {
        closeCursor();
        if (mRosterId != 0) {
            mRosterCursor = mResolver.query(RosterData.CONTENT_URI, null, RosterData.ROSTER_ID + "=" + mRosterId, null, null);
            bindData();
            return;
        }
        // Interprete mLookupUri
        mCursor = setupContactCursor(mResolver, mLookupUri);

        // If mCursor is null now we did not succeed in using the Uri's Id (or it didn't contain
        // a Uri). Instead we now have to use the lookup key to find the record
        if (mCursor == null && mLookupUri != null) {
            mLookupUri = Contacts.getLookupUri(getContentResolver(), mLookupUri);
            mCursor = setupContactCursor(mResolver, mLookupUri);
        }

        // If mCursor is still null, we were unsuccessful in finding the record
        if (mCursor == null) {
            mNameRawContactId = -1;
            mDisplayNameSource = DisplayNameSources.UNDEFINED;
            // TODO either figure out a way to prevent a flash of black background or
            // use some other UI than a toast
            
            //jxli
//            Toast.makeText(this, R.string.invalidContactMessage, Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "invalid contact uri: " + mLookupUri);
//            finish();
            if (mUnknowNumber != null)
                bindData();
            return;
        }

        final long contactId = ContentUris.parseId(mLookupUri);
        //add by zenghuaying 2012.08.08
        mContactId = contactId;
        long photoId = ContactsUtils.queryForPhotoFileId(mResolver,contactId);
        if(photoId > 0){
        	stickerPhotoItem.setVisibility(View.VISIBLE);
        	endLine.setVisibility(View.VISIBLE);
        	itemLayout.setVisibility(View.VISIBLE);
        }else{
        	stickerPhotoItem.setVisibility(View.GONE);
        	endLine.setVisibility(View.GONE);
        	itemLayout.setVisibility(View.GONE);
        }
        //add end
        mNameRawContactId =
                mCursor.getLong(mCursor.getColumnIndex(Contacts.NAME_RAW_CONTACT_ID));
        mDisplayNameSource =
                mCursor.getInt(mCursor.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE));

        mCursor.registerContentObserver(mObserver);

        // Clear flags and start queries to data and status
        mHasEntities = false;
        mHasStatuses = false;

        mHandler.startQuery(TOKEN_ENTITIES, null, RawContactsEntity.CONTENT_URI, null,
                RawContacts.CONTACT_ID + "=?", new String[] {
                    String.valueOf(contactId)
                }, null);
        final Uri dataUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                Contacts.Data.CONTENT_DIRECTORY);
        mHandler.startQuery(TOKEN_STATUSES, null, dataUri, StatusQuery.PROJECTION,
                        StatusUpdates.PRESENCE + " IS NOT NULL OR " + StatusUpdates.STATUS
                                + " IS NOT NULL", null, null);

        getRosterCursor(contactId);
        
        mContactHeaderWidget.bindFromContactLookupUri(mLookupUri);
    }
    
    private void getRosterCursor(long contactId) {
        Cursor rawContactCursor = mResolver.query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID}, RawContacts.CONTACT_ID + "=" + contactId, null, null);
        try {
            if (rawContactCursor.moveToFirst()) {
                long rawContactId = rawContactCursor.getLong(0);
                Cursor cursor = mResolver.query(Roster.CONTENT_URI, new String[]{Roster._ID}, Roster.CONTACT_ID + "=" + rawContactId, null, null);
                try {
                    if (cursor.moveToFirst()) {
                        long rosterId = cursor.getLong(0);
                        mRosterCursor = mResolver.query(RosterData.CONTENT_URI, null, RosterData.ROSTER_ID + "=" + rosterId, null, null);
                    }
                } finally {
                    cursor.close();
                }
            }
        } finally {
            rawContactCursor.close();
        }
    }

    private void closeCursor() {
        if (mCursor != null) {
            mCursor.unregisterContentObserver(mObserver);
            mCursor.close();
            mCursor = null;
        }
        
        if (mRosterCursor != null) {
            mRosterCursor.close();
            mRosterCursor = null;
        }
    }

    /**
     * Consider binding views after any of several background queries has
     * completed. We check internal flags and only bind when all data has
     * arrived.
     */
    private void considerBindData() {
        if (mHasEntities && mHasStatuses) {
            bindData();
        }
    }

    private void bindData() {

        // Build up the contact entries
//        if (mCursor != null)
        buildEntries();
        mPhoneNumbers.clear();
        buildYiliaoEntries();

        // Collapse similar data items in select sections.
        Collapser.collapseList(mPhoneEntries);
        Collapser.collapseList(mSmsEntries);
        Collapser.collapseList(mEmailEntries);
        Collapser.collapseList(mPostalEntries);
        Collapser.collapseList(mImEntries);

        for (ViewEntry entry : mPhoneEntries) {
            if ((null != entry) && !TextUtils.isEmpty(entry.data)) {
                Log.e(TAG, "bindData: phoneNumber=" + entry.data);
                mPhoneNumbers.add(entry.data);
            }
        }

        if (mAdapter == null) {
            mAdapter = new ViewAdapter(this, mSections);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setSections(mSections, SHOW_SEPARATORS);
        }
        mListView.setEmptyView(mEmptyView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuInflater inflater = getMenuInflater();
        if (mRosterId != 0) {
            inflater.inflate(R.menu.yiliao_view_contacts_options_menu, menu);
        } else {
            inflater.inflate(R.menu.view, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        if (mRosterId != 0) {
            if (mPhoneNumbers.size() > 0) {
                if (CommonMethod.blacklistContainsAnyNumber(this, mPhoneNumbers)) {
                    menu.findItem(R.id.menu_add_to_blacklist).setVisible(false);
                    menu.findItem(R.id.menu_clear_from_blacklist).setVisible(true);
                }
                else {
                    menu.findItem(R.id.menu_add_to_blacklist).setVisible(true);
                    menu.findItem(R.id.menu_clear_from_blacklist).setVisible(false);
                }
            }
            else {
                menu.findItem(R.id.menu_add_to_blacklist).setVisible(false);
                menu.findItem(R.id.menu_clear_from_blacklist).setVisible(false);
            }
            return true;
        }
        
        if (mCursor == null) {
            menu.findItem(R.id.menu_edit).setVisible(false);
            menu.findItem(R.id.menu_share).setVisible(false);
            menu.findItem(R.id.menu_options).setVisible(false);
            menu.findItem(R.id.menu_delete).setVisible(false);
            menu.findItem(R.id.menu_add_star).setVisible(false);
            menu.findItem(R.id.menu_remove_star).setVisible(false);
            
            if (!TextUtils.isEmpty(mUnknowNumber)) {
                if (CommonMethod.numberIsInBlacklist(this, mUnknowNumber)) {
                    menu.findItem(R.id.menu_add_to_blacklist).setVisible(false);
                    menu.findItem(R.id.menu_clear_from_blacklist).setVisible(true);
                }
                else {
                    menu.findItem(R.id.menu_add_to_blacklist).setVisible(true);
                    menu.findItem(R.id.menu_clear_from_blacklist).setVisible(false);
                }
            }
            else {
                menu.findItem(R.id.menu_add_to_blacklist).setVisible(false);
                menu.findItem(R.id.menu_clear_from_blacklist).setVisible(false);
            }
        } else {
            menu.findItem(R.id.menu_edit).setVisible(true);
            menu.findItem(R.id.menu_share).setVisible(true);
            menu.findItem(R.id.menu_options).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
            if(0 == mCursor.getInt(mCursor.getColumnIndex(Contacts.STARRED))) {
                menu.findItem(R.id.menu_add_star).setVisible(true);
                menu.findItem(R.id.menu_remove_star).setVisible(false);
            } else {
                menu.findItem(R.id.menu_add_star).setVisible(false);
                menu.findItem(R.id.menu_remove_star).setVisible(true);
            }
            
            if (mPhoneNumbers.size() > 0) {
                if (CommonMethod.blacklistContainsAnyNumber(this, mPhoneNumbers)) {
                    menu.findItem(R.id.menu_add_to_blacklist).setVisible(false);
                    menu.findItem(R.id.menu_clear_from_blacklist).setVisible(true);
                }
                else {
                    menu.findItem(R.id.menu_add_to_blacklist).setVisible(true);
                    menu.findItem(R.id.menu_clear_from_blacklist).setVisible(false);
                }
            }
            else {
                menu.findItem(R.id.menu_add_to_blacklist).setVisible(false);
                menu.findItem(R.id.menu_clear_from_blacklist).setVisible(false);
            }
        }
        
        // Only allow edit when we have at least one raw_contact id
        final boolean hasRawContact = (mRawContactIds.size() > 0);
        menu.findItem(R.id.menu_edit).setEnabled(hasRawContact);

        // Only allow share when unrestricted contacts available
        menu.findItem(R.id.menu_share).setEnabled(!mAllRestricted);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        // This can be null sometimes, don't crash...
        if (info == null) {
            Log.e(TAG, "bad menuInfo");
            return;
        }

        ViewEntry entry = ContactEntryAdapter.getEntry(mSections, info.position, SHOW_SEPARATORS);
        menu.setHeaderTitle(R.string.contactOptionsTitle);
        if (entry == null) {
            return;
        } else if (entry.mimetype.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
            //menu.add(0, MENU_ITEM_CALL, 0, R.string.menu_call).setIntent(entry.intent);
            //menu.add(0, 0, 0, R.string.menu_sendSMS).setIntent(entry.secondaryIntent);
            if (!entry.isPrimary) {
                //menu.add(0, MENU_ITEM_MAKE_DEFAULT, 0, R.string.menu_makeDefaultNumber);
            }
            else {
                //menu.add(0, MENU_ITEM_CANCEL_DEFAULT, 0, R.string.menu_cancelDefaultNumber);
            }
            
            if (!mAllRestricted) {
                menu.add(0, MENU_ITEM_SHARE, 0, R.string.menu_shareNumber);
            }
        } else if (entry.mimetype.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            menu.add(0, 0, 0, R.string.menu_sendEmail).setIntent(entry.intent);
            if (!entry.isPrimary) {
                //menu.add(0, MENU_ITEM_MAKE_DEFAULT, 0, R.string.menu_makeDefaultEmail);
            }
            else {
               // menu.add(0, MENU_ITEM_CANCEL_DEFAULT, 0, R.string.menu_cancelDefaultEmail);
            }
            
            if (!mAllRestricted) {
                menu.add(0, MENU_ITEM_SHARE, 0, R.string.menu_shareEmail);
            }
        } else if (entry.mimetype.equals(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
            menu.add(0, 0, 0, R.string.menu_viewAddress).setIntent(entry.intent);
            if (!mAllRestricted) {
                menu.add(0, MENU_ITEM_SHARE, 0, R.string.menu_shareAddress);
            }
        }

        if (null != mActivityResultBridge) {
            mActivityResultBridge.handleActivityEvent(
                    this,
                    ActivityResultBridge.EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU,
                    null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                Long rawContactIdToEdit = null;
                if (mRawContactIds.size() > 0) {
                    rawContactIdToEdit = mRawContactIds.get(0);
                } else {
                    // There is no rawContact to edit.
                    break;
                }
                Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                        rawContactIdToEdit);
                if (null != mActivityResultBridge) {
                    mActivityResultBridge.startActivityForResult(this,
                        new Intent(Intent.ACTION_EDIT, rawContactUri),
                        REQUEST_EDIT_CONTACT);
                }
                else {
                    startActivityForResult(new Intent(Intent.ACTION_EDIT, rawContactUri),
                            REQUEST_EDIT_CONTACT);
                }
                break;
            }

            case R.id.menu_add_to_blacklist: {
                if (mPhoneNumbers.size() > 0) {
                    CommonMethod.addToBlacklist(this, mPhoneNumbers);
                }
                else if (!TextUtils.isEmpty(mUnknowNumber)) {
                    CommonMethod.addToBlacklist(this, null, mUnknowNumber);
                }
                break;
            }

            case R.id.menu_clear_from_blacklist: {
                if (mPhoneNumbers.size() > 0) {
                    CommonMethod.clearFromBlacklist(this, mPhoneNumbers);
                }
                else if (!TextUtils.isEmpty(mUnknowNumber)) {
                    CommonMethod.clearFromBlacklist(this, null, mUnknowNumber);
                }
                break;
            }
            
            case R.id.menu_delete: {
                // Get confirmation
                if (mReadOnlySourcesCnt > 0 & mWritableSourcesCnt > 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_DELETE);
                } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_HIDE);
                } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
                    showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
                } else {
                    showDialog(DIALOG_CONFIRM_DELETE);
                }
                return true;
            }
//            case R.id.menu_join: {
//                showJoinAggregateActivity();
//                return true;
//            }
            case R.id.menu_options: {
                showOptionsActivity();
                return true;
            }
            case R.id.menu_share: {
                if (mAllRestricted) return false;

                // TODO: Keep around actual LOOKUP_KEY, or formalize method of extracting
                final String lookupKey = Uri.encode(mLookupUri.getPathSegments().get(2));
                final Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);

                // Launch chooser to share contact via
                final CharSequence chooseTitle = getText(R.string.share_via);
                final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

                try {
                    startActivity(chooseIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.menu_add_to_local_contacts: {
                mRosterCursor.moveToFirst();
                String name = mRosterCursor.getString(mRosterCursor.getColumnIndex(RosterData.DISPLAY_NAME));
                CommonMethod.newContact(this, name, mYPhoneEntries.get(0).data);
                return true;
            }

            case R.id.menu_add_star:
            case R.id.menu_remove_star:{
                ContentValues values = new ContentValues(1);
                values.put(Contacts.STARRED, mCursor.getInt(mCursor.getColumnIndex(Contacts.STARRED)) == 0 ? 1 : 0);
                getContentResolver().update(mLookupUri, values, null, null);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        
        Intent intent = item.getIntent();
        
        if (intent != null) {
            if (Intent.ACTION_SENDTO.equals(intent.getAction())) {
                try {
                    startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // TODO: handle exception
                    ComponentName comp = new ComponentName("com.android.email",  
                    "com.android.email.activity.Welcome");  
                    intent.setComponent(comp);  
                    intent.setAction("android.intent.action.MAIN");  
                    startActivity(intent);
                    return true;
                }
            }
        }
        
        switch (item.getItemId()) {
            case MENU_ITEM_MAKE_DEFAULT:
            case MENU_ITEM_CANCEL_DEFAULT: {
                //makeItemDefault(item);
                return true;
            }
            case MENU_ITEM_CALL: {
                StickyTabs.saveTab(this, getIntent());
                Uri data = item.getIntent().getData();
                CommonMethod.call(this, data.getSchemeSpecificPart());
//                startActivity(item.getIntent());
                return true;
            }
            case MENU_ITEM_SHARE: {
                shareContactInfo(item);
                return true;
            }
            default: {
                return super.onContextItemSelected(item);
            }
        }
    }

    private boolean shareContactInfo(MenuItem item) {
        ViewEntry entry = getViewEntryForMenuItem(item);
        if (mAllRestricted || entry == null) {
            return false;
        }

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(PLAIN_TEXT_TYPE);
        intent.putExtra(Intent.EXTRA_TEXT, entry.data);

        try {
            startActivity(Intent.createChooser(intent, getText(R.string.share_via)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean makeItemDefault(MenuItem item) {
        ViewEntry entry = getViewEntryForMenuItem(item);
        if (entry == null) {
            return false;
        }

        // Update the primary values in the data record.
        ContentValues values = new ContentValues(1);
        values.put(Data.IS_SUPER_PRIMARY, ((MENU_ITEM_MAKE_DEFAULT == item.getItemId())? 1 : 0));
        getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, entry.id),
                values, null, null);
        startEntityQuery();
        return true;
    }

    /**
     * Shows a list of aggregates that can be joined into the currently viewed aggregate.
     */
    public void showJoinAggregateActivity() {
        long freshId = getRefreshedContactId();
        if (freshId > 0) {
            String displayName = null;
            if (mCursor.moveToFirst()) {
                displayName = mCursor.getString(0);
            }
            Intent intent = new Intent(ContactsListActivity.JOIN_AGGREGATE);
            intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_ID, freshId);
            if (displayName != null) {
                intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_NAME, displayName);
            }
            startActivityForResult(intent, REQUEST_JOIN_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_JOIN_CONTACT) {
            if (resultCode == RESULT_OK && intent != null) {
                final long contactId = ContentUris.parseId(intent.getData());
                joinAggregate(contactId);
            }
        } else if (requestCode == REQUEST_EDIT_CONTACT) {
            //modify by zenghuaying fix bug #10850
            if (resultCode == EditContactActivity.RESULT_CLOSE_VIEW_ACTIVITY || resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                mLookupUri = intent.getData();
                if (mLookupUri == null) {
                    finish();
                }
            }
        } else if (((DetailEntry.REQUEST_NEW_CONTACT == requestCode) || (DetailEntry.REQUEST_ADD_TO_CONTACTS == requestCode))
                && (Activity.RESULT_OK == resultCode)) {
            mLookupUri = intent.getData();
            if (mLookupUri == null) {
                finish();
            }
            else {
                startEntityQuery();
            }
        } else if (DetailEntry.REQUEST_ON_NEW_INTENT == requestCode) {
            setIntent(intent);
            mLookupUri = intent.getData();
            if (null == mLookupUri) {
                finish();
            }
            else {
                if (0 == ContentUris.parseId(mLookupUri)) {
                    mUnknowNumber = intent.getStringExtra("number");
                }
                startEntityQuery();
            }
        }
    }

    private void joinAggregate(final long contactId) {
        Cursor c = mResolver.query(RawContacts.CONTENT_URI, new String[] {RawContacts._ID},
                RawContacts.CONTACT_ID + "=" + contactId, null, null);

        try {
            while(c.moveToNext()) {
                long rawContactId = c.getLong(0);
                setAggregationException(rawContactId, AggregationExceptions.TYPE_KEEP_TOGETHER);
            }
        } finally {
            c.close();
        }

        Toast.makeText(this, R.string.contactsJoinedMessage, Toast.LENGTH_LONG).show();
        startEntityQuery();
    }

    /**
     * Given a contact ID sets an aggregation exception to either join the contact with the
     * current aggregate or split off.
     */
    protected void setAggregationException(long rawContactId, int exceptionType) {
        ContentValues values = new ContentValues(3);
        for (long aRawContactId : mRawContactIds) {
            if (aRawContactId != rawContactId) {
                values.put(AggregationExceptions.RAW_CONTACT_ID1, aRawContactId);
                values.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId);
                values.put(AggregationExceptions.TYPE, exceptionType);
                mResolver.update(AggregationExceptions.CONTENT_URI, values, null, null);
            }
        }
    }

    private void showOptionsActivity() {
        final Intent intent = new Intent(this, ContactOptionsActivity.class);
        intent.setData(mLookupUri);
        startActivity(intent);
    }

    private ViewEntry getViewEntryForMenuItem(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return null;
        }

        return ContactEntryAdapter.getEntry(mSections, info.position, SHOW_SEPARATORS);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                try {
                    ITelephony phone = ITelephony.Stub.asInterface(
                            ServiceManager.checkService("phone"));
                    if (phone != null && !phone.isIdle()) {
                        // Skip out and let the key be handled at a higher level
                        break;
                    }
                } catch (RemoteException re) {
                    // Fall through and try to call the contact
                }

                int index = mListView.getSelectedItemPosition();
                if (index != -1) {
                    ViewEntry entry = ViewAdapter.getEntry(mSections, index, SHOW_SEPARATORS);
                    if (entry != null && entry.intent != null &&
                            entry.intent.getAction() == Intent.ACTION_CALL_PRIVILEGED) {
                        startActivity(entry.intent);
                        StickyTabs.saveTab(this, getIntent());
                        return true;
                    }

                //FIXME: I think this do same has mNumPhoneNumbers != 0 from Wysie need
                } else if (mPrimaryPhoneUri != null) {
                    // There isn't anything selected, call the default number
                    final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            mPrimaryPhoneUri);
                    startActivity(intent);
                    StickyTabs.saveTab(this, getIntent());
                    return true;
                } else if (mNumPhoneNumbers != 0) {
                    // There isn't anything selected; pick the correct number to dial.
                    long freshContactId = getRefreshedContactId();

                    if(!ContactsUtils.callOrSmsContact(freshContactId, this, false, StickyTabs.getTab(getIntent()))) {
                        signalError();
                        return false;
                    }
                }
                return false;
            }

            case KeyEvent.KEYCODE_DEL: {
                if (mReadOnlySourcesCnt > 0 & mWritableSourcesCnt > 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_DELETE);
                } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_HIDE);
                } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
                    showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
                } else {
                    showDialog(DIALOG_CONFIRM_DELETE);
                }
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ViewEntry entry = ViewAdapter.getEntry(mSections, position, SHOW_SEPARATORS);
        if (entry != null) {
            Intent intent = entry.intent;
            if (intent != null) {
                Uri data = intent.getData();
                if (Intent.ACTION_CALL_PRIVILEGED.equals(intent.getAction())) {
                    StickyTabs.saveTab(this, getIntent());
                } 
                if (Intent.ACTION_CALL_PRIVILEGED.equals(intent.getAction()) && Constants.SCHEME_TEL.equals(data.getScheme())) {
			//prevent somebody origining a unexcepted call, forbidden code by shenqi 
			//CommonMethod.call(this, data.getSchemeSpecificPart());
                } else {
                    try {
                        //modify by zenghuaying fix bug #9891
                        if(!Constants.SCHEME_IMTO.equals(data.getScheme())){
                            startActivity(intent);
                        }
                        //end
                    } catch (ActivityNotFoundException e) {
                        if (Intent.ACTION_SENDTO.equals(intent.getAction())) {
                            
                            ComponentName comp = new ComponentName("com.android.email",  
                            "com.android.email.activity.Welcome");  
                            intent.setComponent(comp);  
                            intent.setAction("android.intent.action.MAIN");  
                            startActivity(intent);
                            
                        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                            //modify by zenghuaying fix bug #10809
                            //Toast.makeText(this, R.string.no_gps_for_address, Toast.LENGTH_LONG).show();
                        }
                        Log.e(TAG, "No activity found for intent: " + intent);
                        signalError();
                    }
                }
            } else {
                signalError();
            }
        } else {
            signalError();
        }
    }

    /**
     * Signal an error to the user via a beep, or some other method.
     */
    private void signalError() {
        //TODO: implement this when we have the sonification APIs
    }

    /**
     * Build up the entries to display on the screen.
     *
     * @param personCursor the URI for the contact being displayed
     */
    private final void buildEntries() {
        // Clear out the old entries
        final int numSections = mSections.size();
        for (int i = 0; i < numSections; i++) {
            mSections.get(i).clear();
        }

        mRawContactIds.clear();

        mReadOnlySourcesCnt = 0;
        mWritableSourcesCnt = 0;
        mAllRestricted = true;
        mPrimaryPhoneUri = null;

        mWritableRawContactIds.clear();

        final Context context = this;
        final Sources sources = Sources.getInstance(context);

        // Build up method entries
        if (mLookupUri != null) {
            for (Entity entity: mEntities) {
                final ContentValues entValues = entity.getEntityValues();
                final String accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
                final long rawContactId = entValues.getAsLong(RawContacts._ID);

                // Mark when this contact has any unrestricted components
                final boolean isRestricted = entValues.getAsInteger(RawContacts.IS_RESTRICTED) != 0;
                if (!isRestricted) mAllRestricted = false;

                if (!mRawContactIds.contains(rawContactId)) {
                    mRawContactIds.add(rawContactId);
                }
                ContactsSource contactsSource = sources.getInflatedSource(accountType,
                        ContactsSource.LEVEL_SUMMARY);
                if (contactsSource != null && contactsSource.readOnly) {
                    mReadOnlySourcesCnt += 1;
                } else {
                    mWritableSourcesCnt += 1;
                    mWritableRawContactIds.add(rawContactId);
                }

                for (NamedContentValues subValue : entity.getSubValues()) {
                    final ContentValues entryValues = subValue.values;
                    entryValues.put(Data.RAW_CONTACT_ID, rawContactId);

                    final long dataId = entryValues.getAsLong(Data._ID);
                    final String mimeType = entryValues.getAsString(Data.MIMETYPE);
                    if (mimeType == null) continue;

                    final DataKind kind = sources.getKindOrFallback(accountType, mimeType, this,
                            ContactsSource.LEVEL_MIMETYPES);
                    if (kind == null) continue;

                    final ViewEntry entry = ViewEntry.fromValues(context, mimeType, kind,
                            rawContactId, dataId, entryValues);

                    final boolean hasData = !TextUtils.isEmpty(entry.data);
                    final boolean isSuperPrimary = entryValues.getAsInteger(
                            Data.IS_SUPER_PRIMARY) != 0;

                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build phone entries
                        mNumPhoneNumbers++;

                        entry.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_TEL, entry.data, null));
                        entry.secondaryIntent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts(Constants.SCHEME_SMSTO, entry.data, null));

                        // Remember super-primary phone
                        if (isSuperPrimary) mPrimaryPhoneUri = entry.uri;

                        //entry.isPrimary = isSuperPrimary;
                        
                        //add for yiliao, by jxli 
//                        String phone = entry.data.replace("+86", "").replace("-", "");
//                        if (phone != null && phone.length() > 0) {
//                            Cursor cursor = mResolver.query(RosterData.CONTENT_URI, null, RosterData.ROSTER_USER_ID+"="+phone, null, null);
//                            try {
//                                if(cursor.moveToFirst())
//                                    entry.label = getString(R.string.yiliao_phone_label);
//                            } finally {
//                                cursor.close();
//                                cursor = null;
//                            }
//                        }
                        //end
                        mPhoneEntries.add(entry);
                        
                        //Wysie: Workaround for the entry.type bug, since entry.type always returns -1
                        
                        Integer type = entryValues.getAsInteger(Phone.TYPE);
                        //Wysie: Bug here, entry.type always returns -1.

                        if ((type != null && type == CommonDataKinds.Phone.TYPE_MOBILE) || mShowSmsLinksForAllPhones) {
                            // Add an SMS entry
                            if (kind.iconAltRes > 0) {
                                entry.secondaryActionIcon = kind.iconAltRes;
                            }
                        }
                    } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build email entries
                        entry.intent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts(Constants.SCHEME_MAILTO, entry.data, null));
                        //entry.isPrimary = isSuperPrimary;
                        mEmailEntries.add(entry);

                        // When Email rows have status, create additional Im row
                        final DataStatus status = mStatuses.get(entry.id);
                        if (status != null) {
                            final String imMime = Im.CONTENT_ITEM_TYPE;
                            final DataKind imKind = sources.getKindOrFallback(accountType,
                                    imMime, this, ContactsSource.LEVEL_MIMETYPES);
                            final ViewEntry imEntry = ViewEntry.fromValues(context,
                                    imMime, imKind, rawContactId, dataId, entryValues);
                            imEntry.intent = ContactsUtils.buildImIntent(entryValues);
                            imEntry.applyStatus(status, false);
                            mImEntries.add(imEntry);
                        }
                    } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build postal entries
                        entry.maxLines = 4;
                        entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);                        

                        Intent i = startNavigation(entry.data);
                        
                        if (i != null) {
                            entry.secondaryIntent = i;
                            // Add a navigation entry
                            if (kind.iconAltRes > 0) {
                                entry.secondaryActionIcon = kind.iconAltRes;
                            }
                        }
                        
                        mPostalEntries.add(entry);
                        
                    } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build IM entries
                        entry.intent = ContactsUtils.buildImIntent(entryValues);
                        if (TextUtils.isEmpty(entry.label)) {
                            entry.label = getString(R.string.chat).toLowerCase();
                        }

                        // Apply presence and status details when available
                        final DataStatus status = mStatuses.get(entry.id);
                        if (status != null) {
                            entry.applyStatus(status, false);
                        }
                        mImEntries.add(entry);
                    } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType) &&
                            (hasData || !TextUtils.isEmpty(entry.label))) {
                        // Build organization entries
                        final boolean isNameRawContact = (mNameRawContactId == rawContactId);

                        final boolean duplicatesTitle =
                            isNameRawContact
                            && mDisplayNameSource == DisplayNameSources.ORGANIZATION
                            && (!hasData || TextUtils.isEmpty(entry.label));

                        if (!duplicatesTitle) {
                            entry.uri = null;
                            //modify by zenghuaying fix bug #10811
                            if(TextUtils.isEmpty(entry.data)){
                                entry.data = entry.label;
                            }else if(!TextUtils.isEmpty(entry.label)){
                                entry.data = entry.label +" "+entry.data;
                            }
                            int type = entry.type;
                            if(type == Organization.TYPE_OTHER || type == Organization.TYPE_CUSTOM){
                                entry.label = getString(R.string.otherOrganization).toLowerCase();
                            }else{//type == Organization.TYPE_WORK
                                entry.label = getString(R.string.ghostData_company).toLowerCase();
                            }
                            
//                            if (TextUtils.isEmpty(entry.label)) {
//                                entry.label = entry.data;
//                                entry.data = "";
//                            }
                            //end
                            mOrganizationEntries.add(entry);
                        }
                    } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build nickname entries
                        final boolean isNameRawContact = (mNameRawContactId == rawContactId);

                        final boolean duplicatesTitle =
                            isNameRawContact
                            && mDisplayNameSource == DisplayNameSources.NICKNAME;

                        if (!duplicatesTitle) {
                            entry.uri = null;
                            mNicknameEntries.add(entry);
                        }
                    } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build note entries
                        entry.uri = null;
                        entry.maxLines = 100;
                        mOtherEntries.add(entry);
                    } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build Website entries
                        entry.uri = null;
                        entry.maxLines = 10;
                        try {
                            WebAddress webAddress = new WebAddress(entry.data);
                            entry.intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(webAddress.toString()));
                        } catch (ParseException e) {
                            Log.e(TAG, "Couldn't parse website: " + entry.data);
                        }
                        mOtherEntries.add(entry);
                    } else if (SipAddress.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build SipAddress entries
                        entry.uri = null;
                        entry.maxLines = 1;
                        entry.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_SIP, entry.data, null));
                        mOtherEntries.add(entry);
                        // TODO: Consider moving the SipAddress into its own
                        // section (rather than lumping it in with mOtherEntries)
                        // so that we can reposition it right under the phone number.
                        // (Then, we'd also update FallbackSource.java to set
                        // secondary=false for this field, and tweak the weight
                        // of its DataKind.)
                    } else {
                        // Handle showing custom rows
                        entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);

                        // Use social summary when requested by external source
                        final DataStatus status = mStatuses.get(entry.id);
                        final boolean hasSocial = kind.actionBodySocial && status != null;
                        if (hasSocial) {
                            entry.applyStatus(status, true);
                        }

                        if (hasSocial || hasData) {
                            mOtherEntries.add(entry);
                        }
                    }
                }
            }
        }
    }

    private final void buildYiliaoEntries() {
        String text;
        
        if (mRosterId != 0 && null != mRosterCursor) {
            mRosterCursor.moveToFirst();
            text = mRosterCursor.getString(mRosterCursor.getColumnIndex(RosterData.REMARK));
            if (text != null) {
                ViewEntry entryRemark = new ViewEntry();
                entryRemark.data = text;
                entryRemark.label = getString(R.string.yiliao_remark_label);
                entryRemark.actionIcon = -1;
                entryRemark.mimetype = YILIAO_REMARK_CONTENT_ITEM_TYPE;
                entryRemark.maxLines = 1;
                mRemarkEntries.add(entryRemark);
                text = null;
            }
            
            text = mRosterCursor.getString(mRosterCursor.getColumnIndex(RosterData.EMAIL));
            if (text != null) {
                ViewEntry entryYEmail = new ViewEntry();
                entryYEmail.data = text;
                entryYEmail.label = getString(R.string.yiliao_email_label);
                entryYEmail.actionIcon = android.R.drawable.sym_action_email;
                entryYEmail.mimetype = YILIAO_EMAIL_CONTENT_ITEM_TYPE;
                entryYEmail.maxLines = 1;
                mYEmailEntries.add(entryYEmail);
                text = null;
            }
        }

        if (mRosterId != 0 && null == mCursor && null != mRosterCursor) {
            mPhoneNumbers.clear();
            
            text = mRosterCursor.getString(mRosterCursor.getColumnIndex(RosterData.ROSTER_USER_ID));
            if (text != null) {    
                mNumPhoneNumbers++;
                mPhoneNumbers.add(text);
                
                ViewEntry entryYPhone = new ViewEntry();
                entryYPhone.data = text;
                entryYPhone.label = getString(R.string.yiliao_phone_label);
                entryYPhone.actionIcon = R.drawable.ic_shortcut_reply_call;
                entryYPhone.secondaryActionIcon = R.drawable.ic_shortcut_reply_message;
                entryYPhone.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                        Uri.fromParts(Constants.SCHEME_TEL, text, null));
                entryYPhone.secondaryIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(Constants.SCHEME_SMSTO, text, null));               

//                entryYPhone.isPrimary = true;                
                entryYPhone.mimetype = YILIAO_PHONE_CONTENT_ITEM_TYPE;
                entryYPhone.maxLines = 1;
                mYPhoneEntries.add(entryYPhone);
            }
        }
    }
    static String buildActionString(DataKind kind, ContentValues values, boolean lowerCase,
            Context context) {
        if (kind.actionHeader == null) {
            return null;
        }
        CharSequence actionHeader = kind.actionHeader.inflateUsing(context, values);
        if (actionHeader == null) {
            return null;
        }
        return lowerCase ? actionHeader.toString().toLowerCase() : actionHeader.toString();
    }

    static String buildDataString(DataKind kind, ContentValues values, Context context) {
        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(context, values);
        return actionBody == null ? null : actionBody.toString();
    }

    /**
     * A basic structure with the data for a contact entry in the list.
     */
    static class ViewEntry extends ContactEntryAdapter.Entry implements Collapsible<ViewEntry> {
        public Context context = null;
        public String resPackageName = null;
        public int actionIcon = -1;
        public boolean isPrimary = false;
        public int secondaryActionIcon = -1;
        public Intent intent;
        public Intent secondaryIntent = null;
        public int maxLabelLines = 1;
        public ArrayList<Long> ids = new ArrayList<Long>();
        public int collapseCount = 0;

        public int presence = -1;

        public CharSequence footerLine = null;

        ViewEntry() {
        }

        /**
         * Build new {@link ViewEntry} and populate from the given values.
         */
        public static ViewEntry fromValues(Context context, String mimeType, DataKind kind,
                long rawContactId, long dataId, ContentValues values) {
            final ViewEntry entry = new ViewEntry();
            entry.context = context;
            entry.contactId = rawContactId;
            entry.id = dataId;
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            entry.mimetype = mimeType;
            entry.label = buildActionString(kind, values, false, context);
            entry.data = buildDataString(kind, values, context);

            if (kind.typeColumn != null && values.containsKey(kind.typeColumn)) {
                entry.type = values.getAsInteger(kind.typeColumn);
            }
            if (kind.iconRes > 0) {
                entry.resPackageName = kind.resPackageName;
                entry.actionIcon = kind.iconRes;
            }

            return entry;
        }

        /**
         * Apply given {@link DataStatus} values over this {@link ViewEntry}
         *
         * @param fillData When true, the given status replaces {@link #data}
         *            and {@link #footerLine}. Otherwise only {@link #presence}
         *            is updated.
         */
        public ViewEntry applyStatus(DataStatus status, boolean fillData) {
            presence = status.getPresence();
            if (fillData && status.isValid()) {
                this.data = status.getStatus().toString();
                this.footerLine = status.getTimestampLabel(context);
            }

            return this;
        }

        public boolean collapseWith(ViewEntry entry) {
            // assert equal collapse keys
            if (!shouldCollapseWith(entry)) {
                return false;
            }

            // Choose the label associated with the highest type precedence.
            if (TypePrecedence.getTypePrecedence(mimetype, type)
                    > TypePrecedence.getTypePrecedence(entry.mimetype, entry.type)) {
                type = entry.type;
                label = entry.label;
            }

            // Choose the max of the maxLines and maxLabelLines values.
            maxLines = Math.max(maxLines, entry.maxLines);
            maxLabelLines = Math.max(maxLabelLines, entry.maxLabelLines);

            // Choose the presence with the highest precedence.
            if (StatusUpdates.getPresencePrecedence(presence)
                    < StatusUpdates.getPresencePrecedence(entry.presence)) {
                presence = entry.presence;
            }

            // If any of the collapsed entries are primary make the whole thing primary.
            //isPrimary = entry.isPrimary ? true : isPrimary;

            // uri, and contactdId, shouldn't make a difference. Just keep the original.

            // Keep track of all the ids that have been collapsed with this one.
            ids.add(entry.id);
            collapseCount++;
            return true;
        }

        public boolean shouldCollapseWith(ViewEntry entry) {
            if (entry == null) {
                return false;
            }

            if (!ContactsUtils.shouldCollapse(context, mimetype, data, entry.mimetype,
                    entry.data)) {
                return false;
            }

            if (!TextUtils.equals(mimetype, entry.mimetype)
                    || !ContactsUtils.areIntentActionEqual(intent, entry.intent)
                    || !ContactsUtils.areIntentActionEqual(secondaryIntent, entry.secondaryIntent)
                    || actionIcon != entry.actionIcon) {
                return false;
            }

            return true;
        }
    }

    /** Cache of the children views of a row */
    static class ViewCache {
        public TextView label = null;
        public TextView data = null;
        public TextView location;
        public TextView footer = null;
        public ImageView actionIcon = null;
        public ImageView presenceIcon = null;
        public ImageView primaryIcon = null;
        public ImageView callSim_1 = null;
        public ImageView callSim_2 = null;
        public ImageView mmsSim_1 = null;
        public ImageView mmsSim_2 = null;
        public ImageView card2Dot = null;
        public int position = -1;
        public ImageView imsOnlineStae = null;
        public LinearLayout mTextLayout = null;
        
        public LinearLayout switcherLeft = null;
        public LinearLayout switcherRight = null;
        public SwitcherHorizontalScrollView switcher = null;
        public RelativeLayout details = null;
        public int mylImageWidth;
        public int mylImageMarg;
        
//        public View secondaryActionDivider = null;

        // Need to keep track of this too
        ViewEntry entry;
    }

    private final class ViewAdapter extends ContactEntryAdapter<ViewEntry>
            implements View.OnClickListener {


        ViewAdapter(Context context, ArrayList<ArrayList<ViewEntry>> sections) {
            super(context, sections, SHOW_SEPARATORS);
        }

        public void onClick(View v) {
            Intent intent = (Intent) v.getTag();
            startActivity(intent);
        }
        
        public int getCount() {
            if (mCursor == null && mRosterId == 0)
                return 1;
            return super.getCount();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewEntry entry = getEntry(mSections, position, false);
            View v;

            // Cache the children
            ViewCache views = new ViewCache();
            
            if (mCursor == null && mRosterId == 0)
                entry = new ViewEntry();
            
            Drawable mylImage = mContext.getResources().getDrawable(R.drawable.icon_contact_header_online);
            Resources resources = getResources();
            
            if (mCursor == null && mRosterId == 0 
                    || entry.mimetype.equals(Phone.CONTENT_ITEM_TYPE)
                    || entry.mimetype.equals(Constants.MIME_SMS_ADDRESS)
                    || entry.mimetype.equals(YILIAO_PHONE_CONTENT_ITEM_TYPE)) {
                    
                    v = mInflater.inflate(R.layout.list_phone_number_item, parent, false); 
                    views.switcher = (SwitcherHorizontalScrollView)v.findViewById(R.id.horizontal_scroll_view);
                    views.switcher.SetMove(false);
                    
                    views.details = (RelativeLayout)v.findViewById(R.id.details);
                    
                    views.details.setTag(position);
                    views.details.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                        
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v,
                                ContextMenuInfo menuInfo) {                            
                        }
                    });
                    
                    views.switcherLeft = (LinearLayout)v.findViewById(R.id.switcher_left);
                    views.switcherLeft.setVisibility(View.GONE);
                    views.location = (TextView) v.findViewById(R.id.belongs);

                    views.switcherRight = (LinearLayout)v.findViewById(R.id.switcher_right);
                    //views.switcherRight.setLayoutParams(new LinearLayout.LayoutParams(
                    //                                    200,
                    //                                    LayoutParams.FILL_PARENT));
                    
                    views.label = (TextView) v.findViewById(R.id.phoneModel);
                    views.data = (TextView) v.findViewById(R.id.phoneNo);
                    
                    views.primaryIcon = (ImageView) v.findViewById(R.id.primary_icon);
                    
                    views.mmsSim_1 = (ImageView) v.findViewById(R.id.message1_btn);
                    views.mmsSim_1.setOnClickListener(this);
                    
                    views.mmsSim_2 = (ImageView) v.findViewById(R.id.message2_btn);
                    views.mmsSim_2.setOnClickListener(this);
                    
                    views.callSim_1 = (ImageView)v.findViewById(R.id.call1_btn);
                    
                    views.callSim_2 = (ImageView)v.findViewById(R.id.call2_btn);
                    
                    views.card2Dot = (ImageView)v.findViewById(R.id.card2_dot);
                    views.card2Dot.setVisibility(View.GONE);
                    views.imsOnlineStae = (ImageView)v.findViewById(R.id.contact_yl_number_state);
                    views.mylImageWidth = mylImage.getIntrinsicWidth();
                    views.mylImageMarg = resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_yl_image_right);
                    views.mTextLayout = null;
                }else{
                // Create a new view if needed
                v = mInflater.inflate(R.layout.list_item_text_icons, parent, false);

                // Cache the children
                views.label = (TextView) v.findViewById(android.R.id.text2);
                views.data = (TextView) v.findViewById(android.R.id.text1);
                views.footer = (TextView) v.findViewById(R.id.footer);
                views.actionIcon = (ImageView) v.findViewById(R.id.action_icon);
                views.primaryIcon = (ImageView) v.findViewById(R.id.primary_icon);
                views.presenceIcon = (ImageView) v.findViewById(R.id.presence_icon);
                views.mmsSim_2 = (ImageView) v.findViewById(R.id.secondary_action_button);
                views.mmsSim_2.setOnClickListener(this);
                views.mylImageWidth = mylImage.getIntrinsicWidth();
                views.mylImageMarg = resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_yl_image_right);
                views.mTextLayout = (LinearLayout) v.findViewById(R.id.text_layout);
//                    views.secondaryActionDivider = v.findViewById(R.id.divider);

                // Set the text size of data row (phone number, email, address, etc.)
//                    float fontSize = Float.parseFloat(ePrefs.getString("misc_data_font_size", "14"));
//                    views.data.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);    
                }
                v.setTag(views);

            // Update the entry in the view cache
            views.entry = entry;
            views.position = position;
            // Bind the data to the view
            bindView(v, entry);
            return v;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            // getView() handles this
            throw new UnsupportedOperationException();
        }

        @Override
        protected void bindView(View view, ViewEntry entry) {
            final Resources resources = mContext.getResources();
            ViewCache views = (ViewCache) view.getTag();
            if (mCursor == null && mUnknowNumber == null && mRosterId == 0)
                return;
            
            // Set the label
            TextView label = views.label;
            setMaxLines(label, entry.maxLabelLines);
            label.setText(entry.label);

            // Set the data
            TextView data = views.data;
            if (data != null) {
                if ((mCursor == null)
                        || entry.mimetype.equals(Phone.CONTENT_ITEM_TYPE)
                        || entry.mimetype.equals(Constants.MIME_SMS_ADDRESS)
                        || entry.mimetype.equals(YILIAO_PHONE_CONTENT_ITEM_TYPE)) {
                    String number;
                    if (mCursor == null && mRosterId == 0) {
                        number = mUnknowNumber;
                        if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                            data.setText(mContext.getResources().getString(R.string.unknown));
                        }
                        else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                            data.setText(mContext.getResources().getString(R.string.private_num));
                        }
                        else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                            data.setText(mContext.getResources().getString(R.string.payphone));
                        } else {
                            data.setText(mUnknowNumber);
                        }
                    } else { 
                        number = entry.data;
                        data.setText(PhoneNumberUtils.formatNumber(entry.data));
                    }
                    //modify for optimizing query speed 
                    // Set the Location
                    TextView location = views.location;
                    /*String tmpLocation = LocationUtil.getPhoneLocation(view.getContext(),number); 
                    String strCardType = LocationUtil.getPhoneCardType(view.getContext(),number);
                     */
                    String[] Location = LocationUtil.getPhoneLocationAndCardType(view.getContext(),number);
                    if(Location != null && Location[0] != null){
                    	System.out.println(Location[0]);
                    	if (Location[1] != null){
                    		location.setText(Location[0] + "(" + Location[1] + ")");
                    	}
                    	else {
                    		location.setText(Location[0]);
                    	}
                    }                     
                } else {
                    data.setText(entry.data);
                }
                
                setMaxLines(data, entry.maxLines);
            }
            
            //add by zenghuaying for sticker photo item align 
            android.widget.RelativeLayout.LayoutParams setParams = 
				(android.widget.RelativeLayout.LayoutParams)stickerSetTxt.getLayoutParams();
            android.widget.RelativeLayout.LayoutParams showParams = 
				(android.widget.RelativeLayout.LayoutParams)stickerShowTxt.getLayoutParams();
            
            if (views.imsOnlineStae != null) {
            	String tNumber = MessageUtils.fixPhoneNumber(entry.data);
            	
    			android.widget.RelativeLayout.LayoutParams dataParams = 
    				(android.widget.RelativeLayout.LayoutParams)data.getLayoutParams();
    			
    			android.widget.RelativeLayout.LayoutParams labeParams = 
    				(android.widget.RelativeLayout.LayoutParams)label.getLayoutParams();
    			
				if (CommonMethod.getLWMsgOnoff(mContext) && 
						mStatusRosterMap.get(tNumber) != null && 
						mPhoneEntries.size() > 1) {
					
					ImsStateData tdata = mStatusRosterMap.get(tNumber);
					int resId = 0;
					
					if (tdata.state == 1) {
						resId = R.drawable.icon_contact_header_online;
					}else {
						resId = R.drawable.icon_contact_header_offline;								
					}
					views.imsOnlineStae.setImageResource(resId);
					views.imsOnlineStae.setVisibility(View.VISIBLE);					
                	dataParams.leftMargin = views.mylImageMarg;
                	labeParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
                	
                	setParams.leftMargin = views.mylImageMarg;
                	showParams.leftMargin = views.mylImageMarg;
				}else {					
					views.imsOnlineStae.setVisibility(View.GONE);	
					
                	if (CommonMethod.getLWMsgOnoff(mContext) == false) {
                    	dataParams.leftMargin = views.mylImageMarg;							
                    	labeParams.leftMargin = views.mylImageMarg;
					}else {
                    	dataParams.leftMargin = views.mylImageMarg + views.mylImageWidth;							
                    	labeParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
                    	
                    	setParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
                    	showParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
					}
				}
				data.setLayoutParams(dataParams);
				label.setLayoutParams(labeParams);
				
				stickerSetTxt.setLayoutParams(setParams);
				stickerShowTxt.setLayoutParams(showParams);
			}else {
    			android.widget.LinearLayout.LayoutParams dataParams = 
    				(android.widget.LinearLayout.LayoutParams)data.getLayoutParams();
    			
    			android.widget.LinearLayout.LayoutParams labeParams = 
    				(android.widget.LinearLayout.LayoutParams)views.mTextLayout.getLayoutParams();

            	if (CommonMethod.getLWMsgOnoff(mContext) == false) {
                	dataParams.leftMargin = views.mylImageMarg;							
                	labeParams.leftMargin = views.mylImageMarg;
                	
                	setParams.leftMargin = views.mylImageMarg;
                	showParams.leftMargin = views.mylImageMarg;
				}else {
                	dataParams.leftMargin = views.mylImageMarg + views.mylImageWidth;							
                	labeParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
                	
                	setParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
                	showParams.leftMargin = views.mylImageMarg + views.mylImageWidth;
				}            	
				data.setLayoutParams(dataParams);
				views.mTextLayout.setLayoutParams(labeParams);
				
				stickerSetTxt.setLayoutParams(setParams);
				stickerShowTxt.setLayoutParams(showParams);
			}            
            
            // Set the footer
            if (null != views.footer) {                
                if (!TextUtils.isEmpty(entry.footerLine)) {
                    views.footer.setText(entry.footerLine);
                    views.footer.setVisibility(View.VISIBLE);
                } else {
                    views.footer.setVisibility(View.GONE);
                }
            }

            // Set the primary icon
            views.primaryIcon.setVisibility(entry.isPrimary ? View.VISIBLE : View.GONE);
            // Set the action icon
            if (views.actionIcon != null) {                
                ImageView action = views.actionIcon;
                if (entry.actionIcon != -1) {
                    Drawable actionIcon;
                    if (entry.resPackageName != null) {
                        // Load external resources through PackageManager
                        actionIcon = mContext.getPackageManager().getDrawable(entry.resPackageName,
                                entry.actionIcon, null);
                    } else {
                        actionIcon = resources.getDrawable(entry.actionIcon);
                    }
                    action.setImageDrawable(actionIcon);
                    action.setVisibility(View.VISIBLE);
                } else {
                    // Things should still line up as if there was an icon, so make it invisible
                    action.setVisibility(View.INVISIBLE);
                }
                //jxli
                action.setVisibility(View.INVISIBLE);
            }

            // Set the presence icon
            if (null != views.presenceIcon) {
                Drawable presenceIcon = ContactPresenceIconUtil.getPresenceIcon(
                        mContext, entry.presence);
                ImageView presenceIconView = views.presenceIcon;
                if (presenceIcon != null) {
                    presenceIconView.setImageDrawable(presenceIcon);
                    presenceIconView.setVisibility(View.VISIBLE);
                } else {
                    presenceIconView.setVisibility(View.GONE);
                }                
            }

            // Set the secondary action button
            ImageView secondaryActionView = views.mmsSim_2;
            Drawable secondaryActionIcon = null;
            if (mCursor == null && mUnknowNumber != null)
            {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts(Constants.SCHEME_SMSTO, mUnknowNumber, null));
                secondaryActionView.setTag(smsIntent);
                secondaryActionView.setVisibility(View.VISIBLE);                
            } else {
                if (entry.secondaryActionIcon != -1) {
                    secondaryActionIcon = resources.getDrawable(entry.secondaryActionIcon);
                }
                if (entry.secondaryIntent != null && secondaryActionIcon != null) {
                    if (entry.mimetype.equals(Phone.CONTENT_ITEM_TYPE)
                            || entry.mimetype.equals(Constants.MIME_SMS_ADDRESS)) {
                        
                    }else{
                        secondaryActionView.setImageDrawable(secondaryActionIcon);                    
                    }
                    secondaryActionView.setTag(entry.secondaryIntent);
                    secondaryActionView.setVisibility(View.VISIBLE);
    //                views.secondaryActionDivider.setVisibility(View.VISIBLE);
                } else {
                    secondaryActionView.setVisibility(View.GONE);
    //                views.secondaryActionDivider.setVisibility(View.GONE);
                }
            }
            
            if (null != views.callSim_2) {
                views.callSim_2.setTag(views.position);
                
                views.callSim_2.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        
                    int position =  Integer.parseInt(v.getTag().toString(),10);
                    ViewEntry entry = ViewAdapter.getEntry(mSections, position, SHOW_SEPARATORS);
                    if (entry != null) {
                        Intent intent = entry.intent;
                        if (intent != null) {
                                Uri data = intent.getData();
                            if (Intent.ACTION_CALL_PRIVILEGED.equals(intent.getAction()) && Constants.SCHEME_TEL.equals(data.getScheme())) {                                
                                CommonMethod.call(mContext, data.getSchemeSpecificPart());
                            }else {
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Log.e(TAG, "No activity found for intent: " + intent);
                                    signalError();
                                }
                            }
                        } else {
                            signalError();
                        }
                    } else if (mUnknowNumber != null) {
                        Intent callIntent= new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_TEL, mUnknowNumber, null));
                        startActivity(callIntent);
                    } else {
                        signalError();
                    }
                        
                    }
                });
            }                     
        }

        private void setMaxLines(TextView textView, int maxLines) {
            if (maxLines == 1) {
                textView.setSingleLine(true);

                //Modified by GanFeng 20120111, fix bug1853
                textView.setEllipsize((TextUtils.TruncateAt.MARQUEE)); //(TextUtils.TruncateAt.END);
                textView.setSelected(true);
            } else {
                textView.setSingleLine(false);
                textView.setMaxLines(maxLines);
                textView.setEllipsize(null);
            }
        }
    }

    private interface StatusQuery {
        final String[] PROJECTION = new String[] {
                Data._ID,
                Data.STATUS,
                Data.STATUS_RES_PACKAGE,
                Data.STATUS_ICON,
                Data.STATUS_LABEL,
                Data.STATUS_TIMESTAMP,
                Data.PRESENCE,
        };

        final int _ID = 0;
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            ContactsSearchManager.startSearch(this, initialQuery);
        }
    }
    //Wysie
    public boolean isIntentAvailable(Intent intent) {
        final PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
        
    //Wysie: Navigation code. Adapted from rac2030's NavStarter.
    //http://code.google.com/p/andrac/source/browse/trunk/NavWidget/src/ch/racic/android/gnav/NavSearch.java
    public Intent startNavigation(String address) {
        address = address.replace('#', ' ');
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + address + "&dirflg=d&nav=1"));
        i.addFlags(0x10800000);
        i.setClassName("com.google.android.apps.m4ps", "com.google.android.maps.driveabout.app.NavigationActivity");
        
        if (isIntentAvailable(i)) {
            return i;
        }
        else {
            i.setClassName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.NavigationActivity");
            if (isIntentAvailable(i)) {
                return i;
            }
            else {
                return null;
            }
        }
    }
    
    private Handler mRosterHandler;
    private HashMap<String, ImsStateData> mStatusRosterMap = new HashMap<String, ImsStateData>();
    private RosterDataListQueryHandler mRosterDataListQueryHandler = null;
    private static final int QUER_LIST_ROSTER_DATA = 0; 
    private static final String[] ROSTER_DATA_PROJECT = new String[]{RosterData.DISPLAY_NAME,
																		RosterData.ROSTER_USER_ID,
																		RosterData.STATUS};
    
    private class ImsStateData{
    	public String number;
    	public int state;
    	ImsStateData(){
    		number = "";
    		state = -1;
    	}    	
    }
    
    public void queryRosterList(ContentResolver resolver){
    	
    	if (mRosterDataListQueryHandler == null) {
			mRosterDataListQueryHandler = new RosterDataListQueryHandler(resolver);
		}    	
    	mRosterDataListQueryHandler.startQuery(QUER_LIST_ROSTER_DATA, 
												null, 
												RosterData.CONTENT_URI, 
												ROSTER_DATA_PROJECT, 
												null, 
												null, 
												null);    	    				
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
            	
            	mStatusRosterMap.clear();
            	
            	while (cursor.moveToNext()) {
            		ImsStateData data = new ImsStateData();
            		data.number = MessageUtils.fixPhoneNumber(cursor.getString(1));
            		data.state = cursor.getInt(2);
            		mStatusRosterMap.put(data.number, data);
				}            	
       		 	
       		 	if (mAdapter != null) {
    				mAdapter.notifyDataSetChanged();
    			}
            }
            break;
                
            default:
                Log.e("MainEntry", "onQueryComplete called with unknown token " + token);
            }
        }    	
    }
    
    Runnable RefreshUIRunable = new Runnable() {
   	 public void run() {      		 
   		 	queryRosterList(mResolver);   	
   	 	}
    };
    
	@Override
	public void onDataEvent(DataEvent event, int state) {
		 if (PimEngine.DataEvent.ROSTERDATA_CHANGED == event) {
			 	if (mRosterHandler == null) {
			 		mRosterHandler = new Handler();
				}
			 
			 	mRosterHandler.removeCallbacks(RefreshUIRunable);
			 	mRosterHandler.postDelayed(RefreshUIRunable,1000);	
		}		
	}
}
