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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.Collections;

import android.R.anim;
import android.R.bool;
import android.R.integer;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.provider.Contacts.PeopleColumns;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.AggregationSuggestions;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.Intents.UI;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.ProviderStatus;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Roster;
import android.provider.ContactsContract.RosterData;
import android.provider.ContactsContract.SearchSnippetColumns;
import android.provider.ContactsContract.User;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.ResourceCursorAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.PIM.R;
import com.lewa.PIM.IM.IMClient;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.IM.service.IMService;
import com.lewa.PIM.calllog.data.MissedCallLogInfo;
import com.lewa.PIM.contacts.ContactListItemView.ItemDimension;
import com.lewa.PIM.contacts.TextHighlightingAnimation.TextWithHighlighting;
import com.lewa.PIM.contacts.model.ContactsSource;
import com.lewa.PIM.contacts.model.Sources;
import com.lewa.PIM.contacts.ui.ContactsPreferences;
import com.lewa.PIM.contacts.ui.ContactsPreferencesActivity;
import com.lewa.PIM.contacts.ContactsSettingsActivity.Prefs;
import com.lewa.PIM.contacts.util.AccountSelectionUtil;
import com.lewa.PIM.contacts.util.Constants;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.engine.PimEngine.DataEvent;
import com.lewa.PIM.engine.PimEngine.DataEventListener;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.ui.ConversationList;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.mms.ui.NewMessageComposeActivity;
import com.lewa.PIM.mms.ui.ConversationList.DeleteThreadListener;
import com.lewa.PIM.mms.ui.Util;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.util.RosterResponseReceiver;
import com.lewa.os.common.ui.indexbar.IndexBar;
import com.lewa.os.common.ui.indexbar.Scroller_view;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.util.ImageUtil;

/*TODO(emillar) I commented most of the code that deals with modes and filtering. It should be
 * brought back in as we add back that functionality.
 */

/**
 * Displays a list of contacts. Usually is embedded into the ContactsActivity.
 */
//@SuppressWarnings("deprecation")
public class ContactsListActivity extends ListActivity
implements View.OnCreateContextMenuListener,
View.OnClickListener, View.OnKeyListener, TextWatcher, TextView.OnEditorActionListener,
OnFocusChangeListener, OnTouchListener,
ActivityResultReceiver,
DataEventListener,
PendingContentLoader,
ItemDimension,
ListView.OnScrollListener {

    public static class JoinContactActivity extends ContactsListActivity {

    }

    public static class ContactsSearchActivity extends ContactsListActivity {

    }

    private static final String TAG = "ContactsListActivity";

    private static final boolean ENABLE_ACTION_ICON_OVERLAYS = true;

    private static final String LIST_STATE_KEY = "liststate";
    private static final String SHORTCUT_ACTION_KEY = "shortcutAction";
    private final Activity contactsList = this;
    static final int MENU_ITEM_VIEW_CONTACT = 1;
    static final int MENU_ITEM_CALL = 2;
    static final int MENU_ITEM_EDIT_BEFORE_CALL = 3;
    static final int MENU_ITEM_SEND_SMS = 4;
    static final int MENU_ITEM_SEND_IM = 5;
    static final int MENU_ITEM_EDIT = 6;
    static final int MENU_ITEM_DELETE = 7;
    static final int MENU_ITEM_TOGGLE_STAR = 8;
    static final int MENU_ITEM_ADD_TO_BLACKLIST = 9;
    static final int MENU_ITEM_CLEAR_FROM_BLACKLIST = 10;
    static final int MENU_ITEM_SEND_CONTACT = 11;
    static final int MENU_ITEM_ADD_TO_LOCAL = 12;

    private static final int SUBACTIVITY_NEW_CONTACT = 1;
    private static final int SUBACTIVITY_VIEW_CONTACT = 2;
    private static final int SUBACTIVITY_DISPLAY_GROUP = 3;
    private static final int SUBACTIVITY_SEARCH = 4;
    private static final int SUBACTIVITY_FILTER = 5;
    private static final int SUBACTIVITY_SETTINGS = 6;

    private static final int TEXT_HIGHLIGHTING_ANIMATION_DURATION = 350;

    //begin for yiliao, add by jxli
    private static final String YILIAO_CONTACTS_OPTIONS = "contacts_yiliao_show_options";
    private static final String YILIAO_CONTACTS_MATCH_FIRST = "contacts_yiliao_match_first";
    private static final String YILIAO_CONTACTS_MATCH_NOT_REMIND = "contacts_yiliao_match_not_remind";
    private static final String YILIAO_CONTACTS_ACCOUNT_STATUS = "contacts_yiliao_account_status";

    private static final String ACTION_YILIAO_MATCH_NUMBERS = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE";
    private static final String ACTION_YILIAO_MATCH_NUMBERS_ONE = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE_ONE";
    private static final String ACTION_YILIAO_MANUALLY_REFRESH = "com.lewa.PIM.IM.MANUALLY_REFRESH";
    private static final String ACTION_YILIAO_STATUS_NUMBERS = "com.lewa.PIM.IM.USER_STATUS_RESPONSE";
    private static final String ACTION_YILIAO_MATCH_NUMBERS_EDIT = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE_EDIT";
    private static final String ACTION_YILIAO_SHOW_OPTIONS = "com.lewa.PIM.yiliao_show_options";

    public static final int CONTACTS_SHOW_LOCAL = 1;
    public static final int CONTACTS_SHOW_YILIAO_ONLY = 2;

    private boolean mbRefresh = false;
    private boolean mbStartInsertRoster= false;
    private long mLastRefreshTime = 0;
    private int mshowContactsYOption; 
    private boolean mbYiliaoOnline;
    private boolean mbLWMsgOnoff;

    private MenuItem mItemShowOptions;
    private MenuItem mItemCloseLWMsg;
    private MenuItem mItemManuallyRefresh;

    private ArrayList<String> arrOnlineNumbers = new ArrayList<String>();
    private ArrayList<Long> arrStatusContactId = new ArrayList<Long>();
    private ArrayList<Long> arrStatusRosterId = new ArrayList<Long>();

    static private Queue<String> queReisgerUser = new LinkedList<String>();

    private WeakReference<ProgressDialog> mProgress;
    private AlertDialog.Builder mYiliaoBuilder;

    //end
    private Context mContext;
    private ContentResolver mResolver;
    /**
     * The action for the join contact activity.
     * <p>
     * Input: extra field {@link #EXTRA_AGGREGATE_ID} is the aggregate ID.
     *
     * TODO: move to {@link ContactsContract}.
     */
    public static final String JOIN_AGGREGATE =
        "com.android.contacts.action.JOIN_AGGREGATE";

    /**
     * Used with {@link #JOIN_AGGREGATE} to give it the target for aggregation.
     * <p>
     * Type: LONG
     */
    public static final String EXTRA_AGGREGATE_ID =
        "com.android.contacts.action.AGGREGATE_ID";

    /**
     * Used with {@link #JOIN_AGGREGATE} to give it the name of the aggregation target.
     * <p>
     * Type: STRING
     */
    @Deprecated
    public static final String EXTRA_AGGREGATE_NAME =
        "com.android.contacts.action.AGGREGATE_NAME";

    public static final String AUTHORITIES_FILTER_KEY = "authorities";

    private static final Uri CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS =
        buildSectionIndexerUri(Contacts.CONTENT_URI);

    private static final Uri ROSTER_CONTENT_URI_WITH_LETTER_COUNTS =
        buildSectionIndexerUri(Roster.CONTENT_URI);

    public static final String CONTACTS_MULTIPLE_DEL_ACTION = "android.intent.action.MULTIPLE_DEL";

    /** Mask for picker mode */
    static final int MODE_MASK_PICKER = 0x80000000;
    /** Mask for no presence mode */
    static final int MODE_MASK_NO_PRESENCE = 0x40000000;
    /** Mask for enabling list filtering */
    static final int MODE_MASK_NO_FILTER = 0x20000000;
    /** Mask for having a "create new contact" header in the list */
    static final int MODE_MASK_CREATE_NEW = 0x10000000;
    /** Mask for showing photos in the list */
    static final int MODE_MASK_SHOW_PHOTOS = 0x08000000;
    /** Mask for hiding additional information e.g. primary phone number in the list */
    static final int MODE_MASK_NO_DATA = 0x04000000;
    /** Mask for showing a call button in the list */
    static final int MODE_MASK_SHOW_CALL_BUTTON = 0x02000000;
    /** Mask to disable quickcontact (images will show as normal images) */
    static final int MODE_MASK_DISABLE_QUIKCCONTACT = 0x01000000;
    /** Mask to show the total number of contacts at the top */
    static final int MODE_MASK_SHOW_NUMBER_OF_CONTACTS = 0x00800000;

    /** Unknown mode */
    static final int MODE_UNKNOWN = 0;

    /** Default mode */
    //Modified by GanFeng 20111213
    //add the MODE_MASK_CREATE_NEW, and remove it while SearchMode
    static int MODE_DEFAULT = 4
    | MODE_MASK_SHOW_PHOTOS
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS
    | MODE_MASK_SHOW_CALL_BUTTON
    | MODE_MASK_CREATE_NEW;

    static final int MODE_DEFAULT_NO_CREATE_NEW = 4
    | MODE_MASK_SHOW_PHOTOS
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS
    | MODE_MASK_SHOW_CALL_BUTTON;

    static final int MODE_DEFAULT_WITH_CREATE_NEW = 4
    | MODE_MASK_SHOW_PHOTOS
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS
    | MODE_MASK_SHOW_CALL_BUTTON
    | MODE_MASK_CREATE_NEW;

    static final int MODE_YILIAO = 100
    | MODE_MASK_SHOW_PHOTOS
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS
    | MODE_MASK_SHOW_CALL_BUTTON;

    /** Default notification mode */
    static final int MODE_DEFAULT_NOTIF = 5 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_NUMBER_OF_CONTACTS | MODE_MASK_SHOW_CALL_BUTTON;
    /** delete multiple contacts */
    static final int MODE_DEL_MULTIPLE = 6 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_NUMBER_OF_CONTACTS | MODE_MASK_SHOW_CALL_BUTTON;
    /** Custom mode */
    static final int MODE_CUSTOM = 8;
    /** Show all starred contacts */
    static final int MODE_STARRED = 20 | MODE_MASK_SHOW_PHOTOS;
    /** Show frequently contacted contacts */
    static final int MODE_FREQUENT = 30 | MODE_MASK_SHOW_PHOTOS;
    /** Show starred and the frequent */
    static final int MODE_STREQUENT = 35 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_CALL_BUTTON;
    /** Show all contacts and pick them when clicking */
    static final int MODE_PICK_CONTACT = 40 | MODE_MASK_PICKER | MODE_MASK_SHOW_PHOTOS
    | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all contacts as well as the option to create a new one */
    static final int MODE_PICK_OR_CREATE_CONTACT = 42 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW
    | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all people through the legacy provider and pick them when clicking */
    static final int MODE_LEGACY_PICK_PERSON = 43 | MODE_MASK_PICKER
    | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all people through the legacy provider as well as the option to create a new one */
    static final int MODE_LEGACY_PICK_OR_CREATE_PERSON = 44 | MODE_MASK_PICKER
    | MODE_MASK_CREATE_NEW | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all contacts and pick them when clicking, and allow creating a new contact */
    //Modified by GanFeng 20111208, remove the MODE_MASK_CREATE_NEW
    static final int MODE_INSERT_OR_EDIT_CONTACT = 45 | MODE_MASK_PICKER //| MODE_MASK_CREATE_NEW
    | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all phone numbers and pick them when clicking */
    static final int MODE_PICK_PHONE = 50 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE;
    /** Show all phone numbers through the legacy provider and pick them when clicking */
    static final int MODE_LEGACY_PICK_PHONE =
        51 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    /** Show all postal addresses and pick them when clicking */
    static final int MODE_PICK_POSTAL =
        55 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    /** Show all postal addresses and pick them when clicking */
    static final int MODE_LEGACY_PICK_POSTAL =
        56 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    static final int MODE_GROUP = 57 | MODE_MASK_SHOW_PHOTOS;
    /** Run a search query */
    static final int MODE_QUERY = 60 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_NO_FILTER
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    /** Run a search query in PICK mode, but that still launches to VIEW */
    static final int MODE_QUERY_PICK_TO_VIEW = 65 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_PICKER
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /** Show join suggestions followed by an A-Z list */
    static final int MODE_JOIN_CONTACT = 70 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE
    | MODE_MASK_NO_DATA | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;

    /** Run a search query in a PICK mode */
    static final int MODE_QUERY_PICK = 75 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_NO_FILTER
    | MODE_MASK_PICKER | MODE_MASK_DISABLE_QUIKCCONTACT | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /** Run a search query in a PICK_PHONE mode */
    static final int MODE_QUERY_PICK_PHONE = 80 | MODE_MASK_NO_FILTER | MODE_MASK_PICKER
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /** Run a search query in PICK mode, but that still launches to EDIT */
    static final int MODE_QUERY_PICK_TO_EDIT = 85 | MODE_MASK_NO_FILTER | MODE_MASK_SHOW_PHOTOS
    | MODE_MASK_PICKER | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /**
     * An action used to do perform search while in a contact picker.  It is initiated
     * by the ContactListActivity itself.
     */
    private static final String ACTION_SEARCH_INTERNAL = "com.android.contacts.INTERNAL_SEARCH";

    /** Maximum number of suggestions shown for joining aggregates */
    static final int MAX_SUGGESTIONS = 4;

    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
        Contacts._ID,                       // 0
        Contacts.DISPLAY_NAME_PRIMARY,      // 1
        Contacts.DISPLAY_NAME_ALTERNATIVE,  // 2
        Contacts.SORT_KEY_PRIMARY,          // 3
        Contacts.STARRED,                   // 4
        Contacts.TIMES_CONTACTED,           // 5
        Contacts.CONTACT_PRESENCE,          // 6
        Contacts.PHOTO_ID,                  // 7
        Contacts.LOOKUP_KEY,                // 8
        Contacts.PHONETIC_NAME,             // 9
        Contacts.HAS_PHONE_NUMBER,          // 10
        Contacts.CONTACT_TYPE,
    };
    static final String[] CONTACTS_SUMMARY_PROJECTION_FROM_EMAIL = new String[] {
        Contacts._ID,                       // 0
        Contacts.DISPLAY_NAME_PRIMARY,      // 1
        Contacts.DISPLAY_NAME_ALTERNATIVE,  // 2
        Contacts.SORT_KEY_PRIMARY,          // 3
        Contacts.STARRED,                   // 4
        Contacts.TIMES_CONTACTED,           // 5
        Contacts.CONTACT_PRESENCE,          // 6
        Contacts.PHOTO_ID,                  // 7
        Contacts.LOOKUP_KEY,                // 8
        Contacts.PHONETIC_NAME,             // 9
        // email lookup doesn't included HAS_PHONE_NUMBER in projection
        Contacts.CONTACT_TYPE,
    };

    static final String[] CONTACTS_SUMMARY_FILTER_PROJECTION = new String[] {
        Contacts._ID,                       // 0
        Contacts.DISPLAY_NAME_PRIMARY,      // 1
        Contacts.DISPLAY_NAME_ALTERNATIVE,  // 2
        Contacts.SORT_KEY_PRIMARY,          // 3
        Contacts.STARRED,                   // 4
        Contacts.TIMES_CONTACTED,           // 5
        Contacts.CONTACT_PRESENCE,          // 6
        Contacts.PHOTO_ID,                  // 7
        Contacts.LOOKUP_KEY,                // 8
        Contacts.PHONETIC_NAME,             // 9
        Contacts.HAS_PHONE_NUMBER,          // 10
        SearchSnippetColumns.SNIPPET_MIMETYPE, // 11
        SearchSnippetColumns.SNIPPET_DATA1,     // 12
        SearchSnippetColumns.SNIPPET_DATA4,     // 13
        Contacts.CONTACT_TYPE,
    };

    static final String[] LEGACY_PEOPLE_PROJECTION = new String[] {
        People._ID,                         // 0
        People.DISPLAY_NAME,                // 1
        People.DISPLAY_NAME,                // 2
        People.DISPLAY_NAME,                // 3
        People.STARRED,                     // 4
        PeopleColumns.TIMES_CONTACTED,      // 5
        People.PRESENCE_STATUS,             // 6
    };

    static final String[] CONTACTS_YILIAO_PROJECTION = new String[] {
        Roster._ID,
        Roster.DISPLAY_NAME,
        Roster.CONTACT_ID,
        Roster.NAME_USER_ID,
        Roster.SORT_KEY,
        Roster.REMARK,
        Roster.DURATION,
        Roster.PHOTO,
    };

    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX = 1;
    static final int SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX = 2;
    static final int SUMMARY_SORT_KEY_PRIMARY_COLUMN_INDEX = 3;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 4;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 5;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 6;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 7;
    static final int SUMMARY_LOOKUP_KEY_COLUMN_INDEX = 8;
    static final int SUMMARY_PHONETIC_NAME_COLUMN_INDEX = 9;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 10;
    static final int SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX = 11;
    static final int SUMMARY_SNIPPET_DATA1_COLUMN_INDEX = 12;
    static final int SUMMARY_SNIPPET_DATA4_COLUMN_INDEX = 13;


    static final String[] PHONES_PROJECTION = new String[] {
        Phone._ID, //0
        Phone.TYPE, //1
        Phone.LABEL, //2
        Phone.NUMBER, //3
        Phone.DISPLAY_NAME, // 4
        Phone.CONTACT_ID, // 5
    };
    static final String[] LEGACY_PHONES_PROJECTION = new String[] {
        Phones._ID, //0
        Phones.TYPE, //1
        Phones.LABEL, //2
        Phones.NUMBER, //3
        People.DISPLAY_NAME, // 4
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_TYPE_COLUMN_INDEX = 1;
    static final int PHONE_LABEL_COLUMN_INDEX = 2;
    static final int PHONE_NUMBER_COLUMN_INDEX = 3;
    static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 4;
    static final int PHONE_CONTACT_ID_COLUMN_INDEX = 5;

    static final String[] POSTALS_PROJECTION = new String[] {
        StructuredPostal._ID, //0
        StructuredPostal.TYPE, //1
        StructuredPostal.LABEL, //2
        StructuredPostal.DATA, //3
        StructuredPostal.DISPLAY_NAME, // 4
    };
    static final String[] LEGACY_POSTALS_PROJECTION = new String[] {
        ContactMethods._ID, //0
        ContactMethods.TYPE, //1
        ContactMethods.LABEL, //2
        ContactMethods.DATA, //3
        People.DISPLAY_NAME, // 4
    };
    static final String[] RAW_CONTACTS_PROJECTION = new String[] {
        RawContacts._ID, //0
        RawContacts.CONTACT_ID, //1
        RawContacts.ACCOUNT_TYPE, //2
    };
    static final class ContactNotificationInfo {
        long contact_id = 0;
        String mName;
        ArrayList<String> mNumber;
        int unReadSMSCount = 0;
        int unReadCallLogCount = 0;
    }     

    static final class AccountInfo {
        long mUserId = 0;
        String mName = null;  
        Drawable mPhoto = null;
    }  

    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
        }
    }

    private ArrayList<ContactNotificationInfo> notificationInfos = new ArrayList<ContactNotificationInfo>();
    private ArrayList<Integer> arrContactIdDel = new ArrayList<Integer>();
    private ArrayList<String> marrFirstLetter = new ArrayList<String>();
    Handler mHandler = new Handler();
    private boolean mReady;
    private boolean mShowing;
    private WindowManager mWindowManager;
    private char mPrevLetter = Character.MIN_VALUE;
    private RemoveWindow mRemoveWindow = new RemoveWindow();
    private TextView mDialogText;


    private int mItemMarginLeft  = Integer.MAX_VALUE;
    private int mItemMarginRight = Integer.MAX_VALUE;

    private String raw_contact_id_unread;
    private ViewGroup vGroup;
    private IndexBar indexBar;
    static final int POSTAL_ID_COLUMN_INDEX = 0;
    static final int POSTAL_TYPE_COLUMN_INDEX = 1;
    static final int POSTAL_LABEL_COLUMN_INDEX = 2;
    static final int POSTAL_ADDRESS_COLUMN_INDEX = 3;
    static final int POSTAL_DISPLAY_NAME_COLUMN_INDEX = 4;

    private static final int QUERY_TOKEN = 42;
    private static final int QUERY_TOKEN_NOTIF = 43;
    private static final int INSERT_TOKEN = 44;
    private static final int INSERT_TOKEN_MANUAL_REFRESH = 45;

    static final String KEY_PICKER_MODE = "picker_mode";

    private ContactItemListAdapter mAdapter;

    int mMode = MODE_DEFAULT;
    private int mDefModeBeforeSearch = MODE_UNKNOWN;

    private QueryHandler mQueryHandler;
    private boolean mJustCreated;
    private boolean mSyncEnabled;
    Uri mSelectedContactUri;

    //    private boolean mDisplayAll;
    private boolean mDisplayOnlyPhones;

    private Uri mGroupUri;

    private long mQueryAggregateId;

    private ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();
    private int  mWritableSourcesCnt;
    private int  mReadOnlySourcesCnt;

    /**
     * Used to keep track of the scroll state of the list.
     */
    private Parcelable mListState = null;

    private String mShortcutAction;

    /**
     * Internal query type when in mode {@link #MODE_QUERY_PICK_TO_VIEW}.
     */
    private int mQueryMode = QUERY_MODE_NONE;

    private static final int QUERY_MODE_NONE = -1;
    private static final int QUERY_MODE_MAILTO = 1;
    private static final int QUERY_MODE_TEL = 2;

    private int mProviderStatus = ProviderStatus.STATUS_NORMAL;

    private boolean mSearchMode;
    private boolean mSearchResultsMode;
    private boolean mShowNumberOfContacts;

    private boolean mShowSearchSnippets;
    private boolean mSearchInitiated;

    private String mInitialFilter;
    private ViewGroup quickLayoutVariable;

    //Modified by GanFeng 20120104, always display the contacts though it is invisible
    public static final String CLAUSE_ONLY_VISIBLE = "(" + Contacts.IN_VISIBLE_GROUP + "=1 OR " + Contacts.IN_VISIBLE_GROUP + "=0)";; //Contacts.IN_VISIBLE_GROUP + "=1";
    public static final String CLAUSE_ONLY_PHONES = Contacts.HAS_PHONE_NUMBER + "=1";
    public static final String CLAUSE_ON_LEWA_MSG = Contacts.CONTACT_TYPE + "=1";
    private boolean mDelModeMarked;

    /**
     * In the {@link #MODE_JOIN_CONTACT} determines whether we display a list item with the label
     * "Show all contacts" or actually show all contacts
     */
    private boolean mJoinModeShowAllContacts;

    /**
     * The ID of the special item described above.
     */
    private static final long JOIN_MODE_SHOW_ALL_CONTACTS_ID = -2;

    // Uri matcher for contact id
    private static final int CONTACTS_ID = 1001;
    private static final UriMatcher sContactsIdMatcher;

    private ContactPhotoLoader mPhotoLoader;

    final String[] sLookupProjection = new String[] {
            Contacts.LOOKUP_KEY
    };
    private static ExecutorService sImageFetchThreadPool;

    //Wysie
    private boolean mContacts = false;
    private boolean mFavs = false;
    private SharedPreferences ePrefs;
    private static boolean showContactsDialButton;
    private static boolean showContactsPic;
    private static boolean showFavsDialButton;
    private static boolean showFavsPic;
    private static boolean showDisplayHeaders;
    private MenuItem mClearFreqCalled;

    private static final int ROSTER_DATA_LIST_QUERY_TOKEN = 0;
    private static final int ROSTER_DATA_LIST_QUERY_STATE_TOKEN = 1;

    private ArrayList<Long> mStatusRosterIdList = new ArrayList<Long>();
    private RosterDataListQueryHandler mRosterDataListQueryHandler;
    private RosterResponseReceiver mRosterResponseReceiver;
    private View searchButton,positionButton;

    static {
        sContactsIdMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sContactsIdMatcher.addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACTS_ID);
    }

    private class DeleteClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (mSelectedContactUri != null) {
                getContentResolver().delete(mSelectedContactUri, null, null);
            }
        }
    }

    /**
     * A {@link TextHighlightingAnimation} that redraws just the contact display name in a
     * list item.
     */
    private static class NameHighlightingAnimation extends TextHighlightingAnimation {
        private final ListView mListView;

        private NameHighlightingAnimation(ListView listView, int duration) {
            super(duration);
            this.mListView = listView;
        }

        /**
         * Redraws all visible items of the list corresponding to contacts
         */
        @Override
        protected void invalidate() {
            int childCount = mListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View itemView = mListView.getChildAt(i);
                if (itemView instanceof ContactListItemView) {
                    final ContactListItemView view = (ContactListItemView)itemView;
                    view.getNameTextView().invalidate();
                }
            }
        }

        @Override
        protected void onAnimationStarted() {
            mListView.setScrollingCacheEnabled(false);
        }

        @Override
        protected void onAnimationEnded() {
            mListView.setScrollingCacheEnabled(true);
        }
    }

    // The size of a home screen shortcut icon.
    private int mIconSize;
    private ContactsPreferences mContactsPrefs;
    private int mDisplayOrder;
    private int mSortOrder;
    private boolean mHighlightWhenScrolling;
    private TextHighlightingAnimation mHighlightingAnimation;
    private SearchEditText mSearchEditText;
    private LewaSearchBar mLewaSearchBar;
    private AccountInfo mAccountInfo = new AccountInfo();
    /**
     * An approximation of the background color of the pinned header. This color
     * is used when the pinned header is being pushed up.  At that point the header
     * "fades away".  Rather than computing a faded bitmap based on the 9-patch
     * normally used for the background, we will use a solid color, which will
     * provide better performance and reduced complexity.
     */
    private int mPinnedHeaderBackgroundColor;

    private ActivityResultBridge mActivityResultBridge = null;

    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            checkProviderState(true);
        }
    };
    private ContentObserver mNotificationObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            Log.e("jxli", "mNotificationObserver, onChange");
            updateNotificationListView(true);
        }
    };

    //Added by GanFeng 20120111, fix bug1985
    public void resetToInitialStatus() {
        Log.e(TAG, "resetToInitialStatus");
        if (null != indexBar) {
            indexBar.hide(this);
        }

        if (null != mDialogText) {
            removeWindow();
        }
    }

    //Added by GanFeng 20120116, fix bug1780
    @Override
    public void registerActivityResultBridge(ActivityResultBridge bridge) {
        mActivityResultBridge = bridge;
    }

    @Override
    public void handleActivityResult(ActivityResultBridge.ActivityResultReceiver realReceiver,
            int requestCode, int resultCode, Intent intent) {
        onActivityResult(requestCode, resultCode, intent);
    }
    //End adding by GanFeng 20120116, fix bug1780

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //Wysie
        ePrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mshowContactsYOption = ePrefs.getInt(YILIAO_CONTACTS_OPTIONS, CONTACTS_SHOW_LOCAL);

        mIconSize = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        mContactsPrefs = new ContactsPreferences(this);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        mRosterDataListQueryHandler = new RosterDataListQueryHandler(getContentResolver());
        mRosterResponseReceiver = new RosterResponseReceiver();
        IntentFilter filterContact = new IntentFilter();        		
        filterContact.addAction(RosterResponseReceiver.ACTION_YILIAO_STATUS_NUMBERS_DETAIL);
        registerReceiver(mRosterResponseReceiver, filterContact);    
        PimEngine.getInstance(this).addDataListenner(this);
        // Resolve the intent
        final Intent intent = getIntent();

        // Allow the title to be set to a custom String using an extra on the intent
        String title = intent.getStringExtra(UI.TITLE_EXTRA_KEY);
        if (title != null) {
            setTitle(title);
        }

        String action = intent.getAction();
        String component = intent.getComponent().getClassName();

        // When we get a FILTER_CONTACTS_ACTION, it represents search in the context
        // of some other action. Let's retrieve the original action to provide proper
        // context for the search queries.
        //Modified by GanFeng 20111213
        //remove the MODE_MASK_CREATE_NEW from the MODE_DEFAULT for Search,
        //otherwise add the MODE_MASK_CREATE_NEW into the MODE_DEFAULT
        if (UI.FILTER_CONTACTS_ACTION.equals(action)) {
            mSearchMode = true;
            mShowSearchSnippets = true;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mInitialFilter = extras.getString(UI.FILTER_TEXT_EXTRA_KEY);
                String originalAction =
                    extras.getString(ContactsSearchManager.ORIGINAL_ACTION_EXTRA_KEY);
                if (originalAction != null) {
                    action = originalAction;
                }
                String originalComponent =
                    extras.getString(ContactsSearchManager.ORIGINAL_COMPONENT_EXTRA_KEY);
                if (originalComponent != null) {
                    component = originalComponent;
                }
            } else {
                mInitialFilter = null;
            }

            mDefModeBeforeSearch = MODE_DEFAULT;
            MODE_DEFAULT = MODE_DEFAULT_NO_CREATE_NEW;
        }
        else {
            MODE_DEFAULT = MODE_DEFAULT_WITH_CREATE_NEW;
        }

        Log.i(TAG, "Called with action: " + action);
        mMode = MODE_UNKNOWN;
        if (UI.LIST_DEFAULT.equals(action) || UI.FILTER_CONTACTS_ACTION.equals(action)) {
            //mMode = MODE_DEFAULT;
            mMode = MODE_DEFAULT_NO_CREATE_NEW;
            //if (UI.LIST_DEFAULT.equals(action)) 
            //    mMode = MODE_DEFAULT_NOTIF;                
            mContacts = true;
            //            LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //            mDialogText = (TextView) inflate.inflate(R.layout.contacts_list_position, null);
            //            mDialogText.setVisibility(View.INVISIBLE);
            //            
            //            mHandler.post(new Runnable() {
            //
            //                public void run() {
            //                    mReady = true;
            //                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            //                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
            //                            WindowManager.LayoutParams.TYPE_APPLICATION,
            //                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            //                                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            //                            PixelFormat.TRANSLUCENT);
            //                    mWindowManager.addView(mDialogText, lp);
            //                }});
            // When mDefaultMode is true the mode is set in onResume(), since the preferneces
            // activity may change it whenever this activity isn't running
            //for yiliao
            //            if (mshowContactsYOption == CONTACTS_SHOW_YILIAO_ONLY) {
            //                mMode = MODE_YILIAO;
            //            }
        } else if (UI.LIST_GROUP_ACTION.equals(action)) {
            mMode = MODE_GROUP;
            String groupName = intent.getStringExtra(UI.GROUP_NAME_EXTRA_KEY);
            if (TextUtils.isEmpty(groupName)) {
                finish();
                return;
            }
            buildUserGroupUri(groupName);
        } else if (UI.LIST_ALL_CONTACTS_ACTION.equals(action)) {
            mMode = MODE_CUSTOM;
            mDisplayOnlyPhones = false;
        } else if (UI.LIST_STARRED_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_STARRED;
        } else if (UI.LIST_FREQUENT_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_FREQUENT;
        } else if (UI.LIST_STREQUENT_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_STREQUENT;
            mFavs = true;
        } else if (UI.LIST_CONTACTS_WITH_PHONES_ACTION.equals(action)) {
            mMode = MODE_CUSTOM;
            mDisplayOnlyPhones = true;
        } else if (Intent.ACTION_PICK.equals(action)) {
            // XXX These should be showing the data from the URI given in
            // the Intent.
            final String type = intent.resolveType(this);
            if (Contacts.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_CONTACT;
            } else if (People.CONTENT_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PERSON;
            } else if (Phone.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_PHONE;
            } else if (Phones.CONTENT_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PHONE;
            } else if (StructuredPostal.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_POSTAL;
            } else if (ContactMethods.CONTENT_POSTAL_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_POSTAL;
            }
        } else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            if (component.equals("alias.DialShortcut")) {
                mMode = MODE_PICK_PHONE;
                mShortcutAction = Intent.ACTION_CALL;
                mShowSearchSnippets = false;
                setTitle(R.string.callShortcutActivityTitle);
            } else if (component.equals("alias.MessageShortcut")) {
                mMode = MODE_PICK_PHONE;
                mShortcutAction = Intent.ACTION_SENDTO;
                mShowSearchSnippets = false;
                setTitle(R.string.messageShortcutActivityTitle);
            } else if (mSearchMode) {
                mMode = MODE_PICK_CONTACT;
                mShortcutAction = Intent.ACTION_VIEW;
                setTitle(R.string.shortcutActivityTitle);
            } else {
                mMode = MODE_PICK_OR_CREATE_CONTACT;
                mShortcutAction = Intent.ACTION_VIEW;
                setTitle(R.string.shortcutActivityTitle);
            }
        } else if (Intent.ACTION_GET_CONTENT.equals(action)) {
            final String type = intent.resolveType(this);
            if (Contacts.CONTENT_ITEM_TYPE.equals(type)) {
                if (mSearchMode) {
                    mMode = MODE_PICK_CONTACT;
                } else {
                    mMode = MODE_PICK_OR_CREATE_CONTACT;
                }
            } else if (Phone.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_PICK_PHONE;
            } else if (Phones.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PHONE;
            } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_PICK_POSTAL;
            } else if (ContactMethods.CONTENT_POSTAL_ITEM_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_POSTAL;
            }  else if (People.CONTENT_ITEM_TYPE.equals(type)) {
                if (mSearchMode) {
                    mMode = MODE_LEGACY_PICK_PERSON;
                } else {
                    mMode = MODE_LEGACY_PICK_OR_CREATE_PERSON;
                }
            }

        } else if (Intent.ACTION_INSERT_OR_EDIT.equals(action)) {
            mMode = MODE_INSERT_OR_EDIT_CONTACT;
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            // See if the suggestion was clicked with a search action key (call button)
            if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG))) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                if (!TextUtils.isEmpty(query)) {
                    Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", query, null));
                    startActivity(newIntent);
                }
                finish();
                return;
            }

            // See if search request has extras to specify query
            if (intent.hasExtra(Insert.EMAIL)) {
                mMode = MODE_QUERY_PICK_TO_VIEW;
                mQueryMode = QUERY_MODE_MAILTO;
                mInitialFilter = intent.getStringExtra(Insert.EMAIL);
            } else if (intent.hasExtra(Insert.PHONE)) {
                mMode = MODE_QUERY_PICK_TO_VIEW;
                mQueryMode = QUERY_MODE_TEL;
                mInitialFilter = intent.getStringExtra(Insert.PHONE);
            } else {
                // Otherwise handle the more normal search case
                mMode = MODE_QUERY;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            }
            mSearchResultsMode = true;
        } else if (ACTION_SEARCH_INTERNAL.equals(action)) {
            String originalAction = null;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                originalAction = extras.getString(ContactsSearchManager.ORIGINAL_ACTION_EXTRA_KEY);
            }
            mShortcutAction = intent.getStringExtra(SHORTCUT_ACTION_KEY);

            if (Intent.ACTION_INSERT_OR_EDIT.equals(originalAction)) {
                mMode = MODE_QUERY_PICK_TO_EDIT;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            } else if (mShortcutAction != null && intent.hasExtra(Insert.PHONE)) {
                mMode = MODE_QUERY_PICK_PHONE;
                mQueryMode = QUERY_MODE_TEL;
                mInitialFilter = intent.getStringExtra(Insert.PHONE);
            } else {
                mMode = MODE_QUERY_PICK;
                mQueryMode = QUERY_MODE_NONE;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            }
            mSearchResultsMode = true;
            // Since this is the filter activity it receives all intents
            // dispatched from the SearchManager for security reasons
            // so we need to re-dispatch from here to the intended target.
        } else if (Intents.SEARCH_SUGGESTION_CLICKED.equals(action)) {
            Uri data = intent.getData();
            Uri telUri = null;
            if (sContactsIdMatcher.match(data) == CONTACTS_ID) {
                long contactId = Long.valueOf(data.getLastPathSegment());
                final Cursor cursor = queryPhoneNumbers(contactId);
                if (cursor != null) {
                    if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                        int phoneNumberIndex = cursor.getColumnIndex(Phone.NUMBER);
                        String phoneNumber = cursor.getString(phoneNumberIndex);
                        telUri = Uri.parse("tel:" + phoneNumber);
                    }
                    cursor.close();
                }
            }
            // See if the suggestion was clicked with a search action key (call button)
            Intent newIntent;
            if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG)) && telUri != null) {
                newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, telUri);
            } else {
                newIntent = new Intent(Intent.ACTION_VIEW, data);
            }
            startActivity(newIntent);
            finish();
            return;
        } else if (Intents.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED.equals(action)) {
            Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, intent.getData());
            startActivity(newIntent);
            finish();
            return;
        } else if (Intents.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED.equals(action)) {
            // TODO actually support this in EditContactActivity.
            String number = intent.getData().getSchemeSpecificPart();
            Intent newIntent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            newIntent.putExtra(Intents.Insert.PHONE, number);
            startActivity(newIntent);
            finish();
            return;
        } else if (CONTACTS_MULTIPLE_DEL_ACTION.equals(action)) {
            mPrevLetter = Character.MIN_VALUE;
            mMode = MODE_DEL_MULTIPLE;
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        if (JOIN_AGGREGATE.equals(action)) {
            if (mSearchMode) {
                mMode = MODE_PICK_CONTACT;
            } else {
                mMode = MODE_JOIN_CONTACT;
                mQueryAggregateId = intent.getLongExtra(EXTRA_AGGREGATE_ID, -1);
                if (mQueryAggregateId == -1) {
                    Log.e(TAG, "Intent " + action + " is missing required extra: "
                            + EXTRA_AGGREGATE_ID);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }

        if (mMode == MODE_UNKNOWN) {
            mMode = MODE_DEFAULT;
            mContacts = true;
        }

        //Added by GanFeng 20120215, fix bug1987
        if ((MODE_DEFAULT_NO_CREATE_NEW == mMode)
                || (MODE_DEFAULT_WITH_CREATE_NEW == mMode)
                || (MODE_DEL_MULTIPLE == mMode)
                || (MODE_YILIAO == mMode)){


            mHandler.post(new Runnable() {
                public void run() {
                    LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    mDialogText = (TextView) inflate.inflate(R.layout.contacts_list_position, null);
                    mDialogText.setVisibility(View.INVISIBLE);
                    mReady = true;
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT);
                    mWindowManager.addView(mDialogText, lp);
                }});
        }
        //Ended adding by GanFeng 20120215

        if (((mMode & MODE_MASK_SHOW_NUMBER_OF_CONTACTS) != 0 || mSearchMode)
                && !mSearchResultsMode) {
            mShowNumberOfContacts = true;
        }
        mShowNumberOfContacts = false;

        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onCreate: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        if (!bDelayLoadContent) {
            setupContentViewHelper();
            if (mSearchMode) {
                setupSearchView();
            }
            mQueryHandler = new QueryHandler(this);
            mJustCreated = true;
            mSyncEnabled = true;
        }

        mContext = this;
        mResolver = getContentResolver();

        //begin for yiliao, add by jxli
        if (isNormalMode()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_YILIAO_MATCH_NUMBERS);
            filter.addAction(ACTION_YILIAO_MATCH_NUMBERS_ONE);
            filter.addAction(ACTION_YILIAO_STATUS_NUMBERS);
            filter.addAction(ACTION_YILIAO_MATCH_NUMBERS_EDIT);
            filter.addAction(ACTION_YILIAO_MANUALLY_REFRESH);
            filter.addAction(CommonMethod.ACTION_LEWA_MSG_ON_OFF);
            registerReceiver(mResponseReceiver, filter);
        }
        if (isNormalMode() || mMode == MODE_STREQUENT) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(CommonMethod.ACTION_LEWA_MSG_ON_OFF);
            filter.addAction(ACTION_YILIAO_SHOW_OPTIONS);
            registerReceiver(mResponseReceiver, filter);
        }
        //end

        // Do this before setting the filter. The filter thread relies
        // on some state that is initialized in setDefaultMode
        if (mMode == MODE_DEFAULT
                || (MODE_DEFAULT_NO_CREATE_NEW == mMode)
                || (MODE_DEFAULT_WITH_CREATE_NEW == mMode)
                || mMode == MODE_DEFAULT_NOTIF
                || mMode == MODE_DEL_MULTIPLE
                || MODE_YILIAO == mMode) {
            // If we're in default mode we need to possibly reset the mode due to a change
            // in the preferences activity while we weren't running
            setDefaultMode();
        }       
    }

    private boolean isNormalMode() {
        if ((MODE_YILIAO == mMode || MODE_DEFAULT_NO_CREATE_NEW == mMode || MODE_DEFAULT_WITH_CREATE_NEW == mMode)
                && false == mSearchMode)
            return true;

        return false;
    }



    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void registerProviderStatusObserver() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(ProviderStatus.CONTENT_URI,
                false, mProviderStatusObserver);
        resolver.registerContentObserver(Uri.parse("content://sms"),
                true, mNotificationObserver);
        resolver.registerContentObserver(Uri.parse("content://sms-mms"),
                true, mNotificationObserver);
        resolver.registerContentObserver(CallLog.CONTENT_URI, true, mNotificationObserver);

    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void unregisterProviderStatusObserver() {
        getContentResolver().unregisterContentObserver(mProviderStatusObserver);
        getContentResolver().unregisterContentObserver(mNotificationObserver);

    }

    private void setupContentViewHelper() {
        //for yiliao
        mbYiliaoOnline = ePrefs.getBoolean(YILIAO_CONTACTS_ACCOUNT_STATUS, false);
        if (mbYiliaoOnline)
            IMClient.UserLogin(this, IMService.GeXinIM, null);

        mbLWMsgOnoff = CommonMethod.getLWMsgOnoff(this);

        if (mMode == MODE_JOIN_CONTACT) {
            setContentView(R.layout.contacts_list_content_join);
            TextView blurbView = (TextView)findViewById(R.id.join_contact_blurb);
            String blurb = getString(R.string.blurbJoinContactDataWith,
                    getContactDisplayName(mQueryAggregateId));
            blurbView.setText(blurb);
            mJoinModeShowAllContacts = true;
        } else if (mMode == MODE_DEL_MULTIPLE) {
            setContentView(R.layout.contacts_list_multiple_del_content);
            mSearchMode = true;
            Button deleteButton = (Button)findViewById(R.id.contacts_delete_done);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        if (arrContactIdDel.size() > 0) {
                            CommonMethod.showConfirmDlg(
                                    ContactsListActivity.this,
                                    R.string.delete_selected_contacts_confirm,
                                    R.string.alert_dialog_title,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (DialogInterface.BUTTON_POSITIVE == which) {
                                                multipleDeleteContacts();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }

            //Added by GanFeng 20120109, fix bug2858
            Button btnMark = (Button )findViewById(R.id.btn_contacts_mark);
            if (null != btnMark) {
                mDelModeMarked = true;
                btnMark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (R.id.btn_contacts_mark == v.getId()) {
                            if (mDelModeMarked) {
                                setAllItemState(true);
                                ((Button )v).setText(R.string.menu_unmark_all);
                            }
                            else {
                                setAllItemState(false);
                                ((Button )v).setText(R.string.menu_mark_all);
                            }
                            mDelModeMarked = !mDelModeMarked;
                        }
                    }
                });
            }
        } else if (mSearchMode) {
            setContentView(R.layout.contacts_search_content);
        } else if (mSearchResultsMode) {
            setContentView(R.layout.contacts_list_search_results);
            TextView titleText = (TextView)findViewById(R.id.search_results_for);
            titleText.setText(Html.fromHtml(getString(R.string.search_results_for,
                    "<b>" + mInitialFilter + "</b>")));
        } else {
            setContentView(R.layout.contacts_list_content);

            searchButton = findViewById(R.id.contacts_search);
            positionButton = findViewById(R.id.contacts_quick_position);
            View dialpadButton = findViewById(R.id.contacts_dialpad);
            View addButton = findViewById(R.id.contacts_new_contact);
            if (searchButton != null)
                searchButton.setOnClickListener(this);
            if (positionButton != null)
                positionButton.setOnClickListener(this);
            if (dialpadButton != null)
                dialpadButton.setOnClickListener(this);
            if (addButton != null)
                addButton.setOnClickListener(this);
            if (MODE_INSERT_OR_EDIT_CONTACT == mMode) {
                setTitle(R.string.menu_add_to_contacts);
                if (dialpadButton != null)
                    dialpadButton.setVisibility(View.GONE);
                if (addButton != null)
                    addButton.setVisibility(View.GONE);
            } 
        }

        setupListView();
        //begin for yiliao, add by jxli
        if (isNormalMode() 
                && ePrefs.getBoolean(YILIAO_CONTACTS_MATCH_NOT_REMIND, false) == false
                && ePrefs.getBoolean(YILIAO_CONTACTS_MATCH_FIRST, false) == false
                && mbLWMsgOnoff == true) {
            //            showYiliaoConfirmDialog();
            //for lewa message
            SharedPreferences.Editor editor = ePrefs.edit();
            editor.putBoolean(YILIAO_CONTACTS_MATCH_FIRST, true);
            editor.putBoolean(Prefs.LABEL_AUTO_MATCH, true);
            editor.commit();

            matchYiliaoNumber(0);
            //            if (matchYiliaoNumber(0)) {
            //                mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(mContext, null,
            //                        getText(R.string.yiliao_progress_match_yiliao_number)));
            //                mProgress.get().setCancelable(true);
            //            }
            //end
        }

        if (isNormalMode()) {
            getOnlinePeople(null);
        }
        //end
    }

    private void setupListView() {
        final ListView list = getListView();
        final LayoutInflater inflater = getLayoutInflater();

        mHighlightingAnimation =
            new NameHighlightingAnimation(list, TEXT_HIGHLIGHTING_ANIMATION_DURATION);

        // Tell list view to not show dividers. We'll do it ourself so that we can *not* show
        // them when an A-Z headers is visible.
        list.setDividerHeight(0);
        list.setOnCreateContextMenuListener(this);

        mAdapter = new ContactItemListAdapter(this);
        setListAdapter(mAdapter);

        if (list instanceof PinnedHeaderListView && mAdapter.getDisplaySectionHeadersEnabled()) {
            mPinnedHeaderBackgroundColor =
                getResources().getColor(R.color.pinned_header_background);
            PinnedHeaderListView pinnedHeaderList = (PinnedHeaderListView)list;
            View pinnedHeader = inflater.inflate(R.layout.list_section, list, false);
            pinnedHeaderList.setPinnedHeaderView(pinnedHeader);
        }

        list.setOnScrollListener(mAdapter);
        list.setOnKeyListener(this);
        list.setOnFocusChangeListener(this);
        list.setOnTouchListener(this);

        // We manually save/restore the listview state
        list.setSaveEnabled(false);
    }

    /**
     * Configures search UI.
     */
    private void setupSearchView() {
        mLewaSearchBar = (LewaSearchBar)findViewById(R.id.search_plate);
        mSearchEditText = (SearchEditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.setText(mInitialFilter);
        mSearchEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
    }

    private String getContactDisplayName(long contactId) {
        String contactName = null;
        Cursor c = getContentResolver().query(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                new String[] {Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                contactName = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (contactName == null) {
            contactName = "";
        }

        return contactName;
    }

    private int getSummaryDisplayNameColumnIndex() {
        if (mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            return SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX;
        } else {
            return SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX;
        }
    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        // TODO a better way of identifying the button
        case android.R.id.button1: {
            final int position = (Integer)v.getTag();
            Cursor c = mAdapter.getCursor();
            if (c != null) {
                c.moveToPosition(position);
                callContact(c);
            }
            break;
        }
        case android.R.id.button2: {
            mbYiliaoOnline = !mbYiliaoOnline;
            SharedPreferences.Editor editor = ePrefs.edit();
            editor.putBoolean(YILIAO_CONTACTS_ACCOUNT_STATUS, mbYiliaoOnline);
            editor.commit();

            ((ContactListItemView)(v.getParent())).setYiLiaoOnLineState(
                    mbYiliaoOnline ? ContactListItemView.STATE_YILIAO_ONLINE : ContactListItemView.STATE_YILIAO_OFFLINE);
            ((ContactListItemView)(v.getParent())).setYiLiaoState(
                    mbYiliaoOnline ? ContactListItemView.STATE_YILIAO_ONLINE : ContactListItemView.STATE_YILIAO_OFFLINE);

            if (mbYiliaoOnline)
                IMClient.UserLogin(this, IMService.GeXinIM, null);
            else
                IMClient.UserLogout(this, IMService.GeXinIM, null);

            break;
        }
        case R.id.text_show_option_item: {
            mshowContactsYOption = (mshowContactsYOption == CONTACTS_SHOW_LOCAL ? CONTACTS_SHOW_YILIAO_ONLY : CONTACTS_SHOW_LOCAL);
            SharedPreferences.Editor editor = ePrefs.edit();
            editor.putInt(YILIAO_CONTACTS_OPTIONS, mshowContactsYOption);
            editor.commit();
            ((TextView)v).setText(mshowContactsYOption == CONTACTS_SHOW_LOCAL ? R.string.show_phone_number : R.string.show_yiliao_number);
            mMode = (mshowContactsYOption == CONTACTS_SHOW_LOCAL ? MODE_DEFAULT_NO_CREATE_NEW : MODE_YILIAO);
            getListView().removeAllViewsInLayout();
            startQuery();
            break;
        }
        case R.id.contacts_search: {
            onSearchRequested();
            break;
        }
        case R.id.contacts_dialpad: {
            CommonMethod.openDialpad(this, null);
            break;
        }
        case R.id.contacts_new_contact: {
            final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            startActivity(intent);
            break;
        }
        case R.id.contacts_quick_position: {
            if (marrFirstLetter.size() > 0) {
                if (indexBar != null && indexBar.isAlpha_menu_Showed()) {
                    indexBar.hide((Activity)mContext);
                }

                indexBar = null;

                vGroup = (ViewGroup) this.findViewById(R.id.contact_list_framelayout);
                indexBar = (IndexBar) LayoutInflater.from(this).inflate(R.layout.indexbar, null);
                indexBar.setResrcIds(indexBar, vGroup, R.id.scroller,R.id.menu,R.id.alphabutton, R.id.left, R.id.right, -1);
                indexBar.load(vGroup,IndexBar.toHeadLetterMap(marrFirstLetter), R.layout.alphalist_item, new String[]{IndexBar.BUTTONKEY}, new int[]{R.id.alphabutton}, indexBar.new IndexBarOnClicker(){

                    @Override
                    public boolean invodeClick(String head) {
                        //if ((head.charAt(0) >= 'A') && (head.charAt(0) <= 'Z')){
                            int letterIndex = marrFirstLetter.indexOf(head);
                            if (head.equals("#"))
                                letterIndex = 0;
                            if (-1 == letterIndex) {
                                return false;
                            }
                            //}

                            mPrevLetter = Character.MIN_VALUE; //not to show the fast text

                            final ListView list = getListView();
                            list.setSelection(mAdapter.getRealQuickPosition(letterIndex)); //marrFirstLetter.indexOf(head)));
                            if(indexBar != null && indexBar.isAlpha_menu_Showed()){
                                onBackPressed();
                            }

                            return true;
                    }
                });       
                indexBar.show();

                //Added by GanFeng 20120111, fix bug1985
                if (null != mDialogText) {
                    removeWindow();
                    mPrevLetter = Character.MIN_VALUE;
                }
            }
        }
        }
    }    

    @Override
    public void onBackPressed() {
        if(indexBar != null && indexBar.isAlpha_menu_Showed()) {
            indexBar.hide(this);
        } else{
        	if (null != mActivityResultBridge) {
             mActivityResultBridge.handleActivityEvent(
                     this,
                     ActivityResultBridge.EVT_ACTIVITY_ON_BACK_PRESSED,
                     null);
         	}
            super.onBackPressed();
        }
    }

    private void setEmptyText() {
        if (mMode == MODE_JOIN_CONTACT || mMode == MODE_DEL_MULTIPLE || mSearchMode) {
            return;
        }

        TextView empty = (TextView) findViewById(R.id.emptyText);
        if (empty == null) {
            return;
        }

        if (mDisplayOnlyPhones) {
            empty.setText(getText(R.string.noContactsWithPhoneNumbers));
        } else if (mMode == MODE_STREQUENT || mMode == MODE_STARRED) {
            empty.setText(getText(R.string.noFavoritesHelpText));
        } else if (mMode == MODE_QUERY || mMode == MODE_QUERY_PICK
                || mMode == MODE_QUERY_PICK_PHONE || mMode == MODE_QUERY_PICK_TO_VIEW
                || mMode == MODE_QUERY_PICK_TO_EDIT) {
            empty.setText(getText(R.string.noMatchingContacts));
        } else {
            boolean hasSim = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
            .hasIccCard();
            boolean createShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());
            if (isSyncActive()) {
                if (createShortcut) {
                    // Help text is the same no matter whether there is SIM or not.
                    empty.setText(getText(R.string.noContactsHelpTextWithSyncForCreateShortcut));
                } else if (hasSim) {
                    empty.setText(getText(R.string.noContactsWithPhoneNumbers));
                } else {
                    empty.setText(getText(R.string.noContactsWithPhoneNumbers));
                }
            } else {
                if (createShortcut) {
                    // Help text is the same no matter whether there is SIM or not.
                    empty.setText(getText(R.string.noContactsHelpTextForCreateShortcut));
                } else if (hasSim) {
                    empty.setText(getText(R.string.noContactsWithPhoneNumbers));
                } else {
                    empty.setText(getText(R.string.noContactsWithPhoneNumbers));
                }
            }
        }
    }

    private boolean isSyncActive() {
        Account[] accounts = AccountManager.get(this).getAccounts();
        if (accounts != null && accounts.length > 0) {
            IContentService contentService = ContentResolver.getContentService();
            for (Account account : accounts) {
                try {
                    if (contentService.isSyncActive(account, ContactsContract.AUTHORITY)) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Could not get the sync status");
                }
            }
        }
        return false;
    }

    private void buildUserGroupUri(String group) {
        mGroupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, group);
    }

    /**
     * Sets the mode when the request is for "default"
     */
    private void setDefaultMode() {
        // Load the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //        mDisplayOnlyPhones = prefs.getBoolean(Prefs.DISPLAY_ONLY_PHONES,
        //                Prefs.DISPLAY_ONLY_PHONES_DEFAULT);
        mDisplayOnlyPhones = prefs.getBoolean(Prefs.LABEL_ONLY_PHONES,
                Prefs.DISPLAY_ONLY_PHONES_DEFAULT);
    }

    @Override
    protected void onDestroy() {
        //Added by GanFeng 20120110, fix bug2647
        if ((null != mWindowManager) && (null != mDialogText)) {
            mWindowManager.removeView(mDialogText);
        }
        PimEngine.getInstance(this).removeDataListenner(this);
        super.onDestroy();
        mPhotoLoader.stop();
        mReady = false;
        mActivityResultBridge = null;

        //Added by GanFeng 20111214, should recovery the MODE_DEFAULT
        if (MODE_UNKNOWN != mDefModeBeforeSearch) {
            MODE_DEFAULT = mDefModeBeforeSearch;
        }
        if (isNormalMode() || mMode == MODE_STREQUENT) {
            unregisterReceiver(mResponseReceiver);
        }

        if (mRosterResponseReceiver != null) {
            unregisterReceiver(mRosterResponseReceiver);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        quryImsState(ROSTER_DATA_LIST_QUERY_TOKEN);
        mContactsPrefs.registerChangeListener(mPreferencesChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterProviderStatusObserver();
        mReady = false;
        //if (bSetScrollListner == true
        //        && (mMode == MODE_DEFAULT_NOTIF
        //                || mMode == MODE_DEFAULT
        //                || (MODE_DEFAULT_NO_CREATE_NEW == mMode)
        //                || (MODE_DEFAULT_WITH_CREATE_NEW == mMode))) {
        //    getListView().setOnScrollListener(null);
        //    bSetScrollListner = false;
        //}

    }

    @Override
    protected void onResume() {
        super.onResume();

        showContactsDialButton = ePrefs.getBoolean("contacts_show_dial_button", true);
        showContactsPic = ePrefs.getBoolean("contacts_show_pic", true);
        showFavsDialButton = ePrefs.getBoolean("favs_show_dial_button", true);
        showFavsPic = ePrefs.getBoolean("favs_show_pic", true);
        showDisplayHeaders = ePrefs.getBoolean("contacts_show_alphabetical_separators", true);

        registerProviderStatusObserver();
        mPhotoLoader.resume();

        Activity parent = getParent();

        // See if we were invoked with a filter
        if (mSearchMode) {
            mSearchEditText.requestFocus();
        }

        if (!mSearchMode && !checkProviderState(mJustCreated)) {
            return;
        }

        if (mJustCreated) {
            // We need to start a query here the first time the activity is launched, as long
            // as we aren't doing a filter.
            startQuery();
        }
        mJustCreated = false;
        mSearchInitiated = false;
        mReady = true;
    }

    private void updateNotificationListView(boolean loadData) {
        if (null != mAdapter) {
            mAdapter.notifyDataSetInvalidated();
            if (loadData) {
                startQuery();
            }
        }
    }

    /**
     * Obtains the contacts provider status and configures the UI accordingly.
     *
     * @param loadData true if the method needs to start a query when the
     *            provider is in the normal state
     * @return true if the provider status is normal
     */
    private boolean checkProviderState(boolean loadData) {
        View importFailureView = findViewById(R.id.import_failure);
        if (importFailureView == null) {
            return true;
        }

        TextView messageView = (TextView) findViewById(R.id.emptyText);

        // This query can be performed on the UI thread because
        // the API explicitly allows such use.
        Cursor cursor = getContentResolver().query(ProviderStatus.CONTENT_URI, new String[] {
                ProviderStatus.STATUS, ProviderStatus.DATA1
        }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(0);
                if (status != mProviderStatus) {
                    mProviderStatus = status;
                    switch (status) {
                    case ProviderStatus.STATUS_NORMAL:
                        mAdapter.notifyDataSetInvalidated();
                        if (loadData) {
                            startQuery();
                        }
                        break;

                    case ProviderStatus.STATUS_CHANGING_LOCALE:
                        messageView.setText(R.string.locale_change_in_progress);
                        mAdapter.changeCursor(null);
                        mAdapter.notifyDataSetInvalidated();
                        break;

                    case ProviderStatus.STATUS_UPGRADING:
                        messageView.setText(R.string.upgrade_in_progress);
                        mAdapter.changeCursor(null);
                        mAdapter.notifyDataSetInvalidated();
                        break;

                    case ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY:
                        long size = cursor.getLong(1);
                        String message = getResources().getString(
                                R.string.upgrade_out_of_memory, new Object[] {size});
                        messageView.setText(message);
                        configureImportFailureView(importFailureView);
                        mAdapter.changeCursor(null);
                        mAdapter.notifyDataSetInvalidated();
                        break;
                    }
                }
            }
        } finally {
            cursor.close();
        }

        importFailureView.setVisibility(
                mProviderStatus == ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY
                ? View.VISIBLE
                        : View.GONE);
        return mProviderStatus == ProviderStatus.STATUS_NORMAL;
    }

    private void configureImportFailureView(View importFailureView) {

        OnClickListener listener = new OnClickListener(){

            public void onClick(View v) {
                switch(v.getId()) {
                case R.id.import_failure_uninstall_apps: {
                    startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                    break;
                }
                case R.id.import_failure_retry_upgrade: {
                    // Send a provider status update, which will trigger a retry
                    ContentValues values = new ContentValues();
                    values.put(ProviderStatus.STATUS, ProviderStatus.STATUS_UPGRADING);
                    getContentResolver().update(ProviderStatus.CONTENT_URI, values, null, null);
                    break;
                }
                }
            }};

            Button uninstallApps = (Button) findViewById(R.id.import_failure_uninstall_apps);
            uninstallApps.setOnClickListener(listener);

            Button retryUpgrade = (Button) findViewById(R.id.import_failure_retry_upgrade);
            retryUpgrade.setOnClickListener(listener);
    }

    private String getTextFilter() {
        if (mSearchEditText != null) {
            return mSearchEditText.getText().toString();
        }
        return null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!checkProviderState(false)) {
            return;
        }

        // The cursor was killed off in onStop(), so we need to get a new one here
        // We do not perform the query if a filter is set on the list because the
        // filter will cause the query to happen anyway
        if (TextUtils.isEmpty(getTextFilter())) {
            startQuery();
        } else {
            // Run the filtered query on the adapter
            mAdapter.onContentChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        // Save list state in the bundle so we can restore it after the QueryHandler has run
        if (mList != null) {
            icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        super.onRestoreInstanceState(icicle);
        // Retrieve list state. This will be applied after the QueryHandler has run
        mListState = icicle.getParcelable(LIST_STATE_KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mContactsPrefs.unregisterChangeListener();

        if (null != mAdapter) {
            mAdapter.setSuggestionsCursor(null);
            mAdapter.changeCursor(null);
        }

        if (mMode == MODE_QUERY) {
            // Make sure the search box is closed
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchManager.stopSearch();
        }

        //clear roster status
        // no need to clear roster status,remove code by shenqi
        //ContentValues values = new ContentValues();
        //values.put(RosterData.STATUS, 0);
        //mResolver.update(RosterData.CONTENT_URI, values, null, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // If Contacts was invoked by another Activity simply as a way of
        // picking a contact, don't show the options menu
        if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
            return false;
        }

        //Added by GanFeng 20120109, fix bug2858
        if (MODE_DEL_MULTIPLE == mMode || mSearchMode == true) {
            return false;
        }

        MenuInflater inflater = getMenuInflater();
        if (mMode == MODE_DEL_MULTIPLE) {
            inflater.inflate(R.menu.del_multiple_options_menu, menu);
        } else {
            inflater.inflate(R.menu.list, menu);
            mClearFreqCalled = menu.findItem(R.id.menu_clear_freq_called);
            mItemShowOptions = menu.findItem(R.id.menu_show_options);
            mItemCloseLWMsg = menu.findItem(R.id.menu_close_lw_msg);
            mItemManuallyRefresh = menu.findItem(R.id.menu_manually_refresh);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMode != MODE_DEL_MULTIPLE) {
            //Modified by GanFeng 20120116, fix bug1780
            //final boolean defaultMode = (mMode == MODE_DEFAULT);
            final boolean defaultMode = (((MODE_DEFAULT_NO_CREATE_NEW == mMode) || (MODE_DEFAULT_WITH_CREATE_NEW == mMode) || (MODE_YILIAO == mMode))
                    && !mSearchMode);
            //            menu.findItem(R.id.menu_display_groups).setVisible(defaultMode);

            if (mFavs && !ePrefs.getBoolean("favourites_hide_freq_called", false)) {       
                mClearFreqCalled.setVisible(true);
                //                menu.findItem(R.id.menu_delete_multiple).setVisible(false);
            }
            else {
                mClearFreqCalled.setVisible(false);
                //                menu.findItem(R.id.menu_delete_multiple).setVisible(true);
            }

            if (mbLWMsgOnoff) {
                mItemCloseLWMsg.setTitle(R.string.menu_close_lw_msg);
                mItemCloseLWMsg.setIcon(R.drawable.ic_menu_close_imessage);
                if (mshowContactsYOption == CONTACTS_SHOW_LOCAL) {
                    mItemShowOptions.setTitle(R.string.show_yiliao_number);
                    mItemShowOptions.setIcon(R.drawable.ic_menu_show_only_icontacts);
                } else {
                    mItemShowOptions.setTitle(R.string.show_phone_number);
                    mItemShowOptions.setIcon(R.drawable.ic_menu_show_all_contacts);
                }

                mItemShowOptions.setVisible(true);
                mItemManuallyRefresh.setVisible(true);
            } else {
                mItemCloseLWMsg.setTitle(R.string.menu_open_lw_msg);
                mItemCloseLWMsg.setIcon(R.drawable.ic_menu_open_imessage);

                mItemShowOptions.setVisible(false);
                mItemManuallyRefresh.setVisible(false);
            }

            if (mFavs) {
                //              menu.findItem(R.id.menu_add).setVisible(false);
                //              menu.findItem(R.id.menu_import_export).setVisible(false);
                //              menu.findItem(R.id.menu_merge_duplicated).setVisible(false);
                menu.findItem(R.id.menu_manually_refresh).setVisible(false);
                menu.findItem(R.id.menu_settings).setVisible(false);
            }

            //remove
            mItemShowOptions.setVisible(false);
            mItemCloseLWMsg.setVisible(false);
            //end
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        /*            case R.id.menu_display_groups: {
                final Intent intent = new Intent(this, ContactsPreferencesActivity.class);
                //startActivityForResult(intent, SUBACTIVITY_DISPLAY_GROUP);
                if (null != mActivityResultBridge) {
                    mActivityResultBridge.startActivityForResult(this, intent, SUBACTIVITY_DISPLAY_GROUP);
                }
                else {
                    startActivityForResult(intent, SUBACTIVITY_DISPLAY_GROUP);
                }
                return true;
            }*/
        /*            case R.id.menu_search: {
                onSearchRequested();
                return true;
            }*/
        /*           case R.id.menu_delete_multiple: {
                final Intent intent = new Intent(CONTACTS_MULTIPLE_DEL_ACTION);
                intent.setClass(this, ContactsListActivity.class);
                startActivity(intent);
                mPrevLetter = Character.MIN_VALUE; //Added by GanFeng 20120110, fix bug2766, not to show the fast text when return
                return true;
            }*/
        //            case R.id.menu_add: {
        //                final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        //                startActivity(intent);
        //                return true;
        //            }
        /*            case R.id.menu_import_export: {
                displayImportExportDialog();
                return true;
            }*/
        /*            case R.id.menu_accounts: {
                final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(AUTHORITIES_FILTER_KEY, new String[] {
                    ContactsContract.AUTHORITY
                });
                startActivity(intent);
                return true;
            }*/
        //Wysie
        case R.id.menu_clear_freq_called: {
            if (ePrefs.getBoolean("favourites_ask_before_clear", false)) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.fav_clear_freq);
                alert.setMessage(R.string.alert_clear_freq_called_msg);
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        clearFrequentlyCalled();
                    }});
                alert.show();
            }
            else {
                clearFrequentlyCalled();
            }            	
            return true;
        }
        //Wysie
        //            case R.id.menu_preferences: {
        //                //Use full classpath due tu duplicate classname
        //                startActivity(new Intent(this, com.lewa.PIM.contacts.ContactsPreferences.class));
        //                return true;
        //            }
        case R.id.menu_select_all: {
            setAllItemState(true);
            return true;
        }
        case R.id.menu_unselect_all: {
            setAllItemState(false);
            return true;
        }
        // Begin, added by zhumeiquan for Labi, 20111212
        case R.id.menu_labi_sync:
        {
            Intent intent = new Intent("android.settings.SYNC_SETTINGS");
            startActivity(intent);
            break;
        }

        /*            case R.id.menu_merge_duplicated:
            {
                ArrayList<ArrayList<String>> similarIds = ContactHelper.querySimiliarIds(this);
                PageManager.setSimilarIds(similarIds);
                if (similarIds.size() == 0) {
                    Toast.makeText(this, R.string.no_duplicated_contact, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, MergeActivity.class);
                    intent.putStringArrayListExtra("ids", PageManager.getFirstIds());
                    startActivity(intent);
                }
                break;
            }*/
        // End

        case R.id.menu_blacklist: {
            Intent intent = new Intent();
            intent.setClassName("com.lewa.intercept", "com.lewa.intercept.MainActivity");
            intent.putExtra("ifBlock", 2); //show the blacklist which is the 3rd page
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.quickcontact_missing_app, Toast.LENGTH_SHORT).show();
            }
            break;
        }

        case R.id.menu_settings: {
            Intent intent = new Intent(this, ContactsSettingsActivity.class);
            if (null != mActivityResultBridge) {
                mActivityResultBridge.startActivityForResult(this, intent, SUBACTIVITY_SETTINGS);
            }
            else {
                startActivityForResult(intent, SUBACTIVITY_SETTINGS);
            }
            return true;
        }

        /*            case R.id.menu_yiliao_sync: {
                Intent intent = new Intent("com.lewa.sync.telecom.third.party.action.main");
                startActivity(intent);
                break;
            }*/

        /*            case R.id.menu_share_contacts: {
                break;
            }*/

        case R.id.menu_show_options: {
            mshowContactsYOption = (mshowContactsYOption == CONTACTS_SHOW_LOCAL ? CONTACTS_SHOW_YILIAO_ONLY : CONTACTS_SHOW_LOCAL);
            SharedPreferences.Editor editor = ePrefs.edit();
            editor.putInt(YILIAO_CONTACTS_OPTIONS, mshowContactsYOption);
            editor.commit();

            Intent intent = new Intent(ACTION_YILIAO_SHOW_OPTIONS);
            sendBroadcast(intent);
            break;
        }

        case R.id.menu_manually_refresh: {
            //                if (matchYiliaoNumber(0)) {
                //                    mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(mContext, null,
            //                            getText(R.string.yiliao_progress_match_yiliao_number)));
            //                    mProgress.get().setCancelable(true);
            //                }
            matchYiliaoNumber(0, true);
            quryImsState(ROSTER_DATA_LIST_QUERY_STATE_TOKEN);
            break;
        }

        case R.id.menu_close_lw_msg: {
            mbLWMsgOnoff = (CommonMethod.getLWMsgOnoff(this) == true ? false : true);
            CommonMethod.setLWMsgOnoff(this, mbLWMsgOnoff);
            break;
        }

        default:
            break;
        }
        return false;
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
            return;
        }

        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            if (!mSearchMode && (mMode & MODE_MASK_NO_FILTER) == 0) {
                if ((mMode & MODE_MASK_PICKER) != 0) {
                    ContactsSearchManager.startSearchForResult(this, initialQuery,
                            SUBACTIVITY_FILTER);
                } else {
                    ContactsSearchManager.startSearch(this, initialQuery);
                }
            }
        }
    }

    /**
     * Performs filtering of the list based on the search query entered in the
     * search text edit.
     */
    protected void onSearchTextChanged() {
        // Set the proper empty string
        setEmptyText();

        Filter filter = mAdapter.getFilter();
        filter.filter(getTextFilter());
    }

    /**
     * Starts a new activity that will run a search query and display search results.
     */
    private void doSearch() {
        String query = getTextFilter();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        Intent intent = new Intent(this, SearchResultsActivity.class);
        Intent originalIntent = getIntent();
        Bundle originalExtras = originalIntent.getExtras();
        if (originalExtras != null) {
            intent.putExtras(originalExtras);
        }

        intent.putExtra(SearchManager.QUERY, query);
        if ((mMode & MODE_MASK_PICKER) != 0) {
            intent.setAction(ACTION_SEARCH_INTERNAL);
            intent.putExtra(SHORTCUT_ACTION_KEY, mShortcutAction);
            if (mShortcutAction != null) {
                if (Intent.ACTION_CALL.equals(mShortcutAction)
                        || Intent.ACTION_SENDTO.equals(mShortcutAction)) {
                    intent.putExtra(Insert.PHONE, query);
                }
            } else {
                switch (mQueryMode) {
                case QUERY_MODE_MAILTO:
                    intent.putExtra(Insert.EMAIL, query);
                    break;
                case QUERY_MODE_TEL:
                    intent.putExtra(Insert.PHONE, query);
                    break;
                }
            }
            startActivityForResult(intent, SUBACTIVITY_SEARCH);
        } else {
            intent.setAction(Intent.ACTION_SEARCH);
            startActivity(intent);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case R.string.import_from_sim:
        case R.string.import_from_sdcard: {
            return AccountSelectionUtil.getSelectAccountDialog(this, id);
        }
        case R.id.dialog_sdcard_not_found: {
            return new AlertDialog.Builder(this)
            .setTitle(R.string.no_sdcard_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.no_sdcard_message)
            .setPositiveButton(android.R.string.ok, null).create();
        }
        case R.id.dialog_delete_contact_confirmation: {
            return new AlertDialog.Builder(this)
            .setTitle(R.string.deleteConfirmation_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.deleteConfirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok,
                    new DeleteClickListener()).create();
        }
        case R.id.dialog_readonly_contact_hide_confirmation: {
            return new AlertDialog.Builder(this)
            .setTitle(R.string.deleteConfirmation_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.readOnlyContactWarning)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok,
                    new DeleteClickListener()).create();
        }
        case R.id.dialog_readonly_contact_delete_confirmation: {
            return new AlertDialog.Builder(this)
            .setTitle(R.string.deleteConfirmation_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.readOnlyContactDeleteConfirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok,
                    new DeleteClickListener()).create();
        }
        case R.id.dialog_multiple_contact_delete_confirmation: {
            return new AlertDialog.Builder(this)
            .setTitle(R.string.deleteConfirmation_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(R.string.multipleContactDeleteConfirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok,
                    new DeleteClickListener()).create();
        }
        }
        return super.onCreateDialog(id, bundle);
    }

    /**
     * Create a {@link Dialog} that allows the user to pick from a bulk import
     * or bulk export task across all contacts.
     */
     private void displayImportExportDialog() {
         // Wrap our context to inflate list items using correct theme
         final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Light);
         final Resources res = dialogContext.getResources();
         final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

         // Adapter that shows a list of string resources
         final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                 android.R.layout.simple_list_item_1) {
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 if (convertView == null) {
                     convertView = dialogInflater.inflate(android.R.layout.simple_list_item_1,
                             parent, false);
                 }

                 final int resId = this.getItem(position);
                 ((TextView)convertView).setText(resId);
                 return convertView;
             }
         };

         if (TelephonyManager.getDefault().hasIccCard()) {
             adapter.add(R.string.import_from_sim);
         }
         if (res.getBoolean(R.bool.config_allow_import_from_sdcard)) {
             adapter.add(R.string.import_from_sdcard);
         }
         if (res.getBoolean(R.bool.config_allow_export_to_sdcard)) {
             adapter.add(R.string.export_to_sdcard);
         }
         if (res.getBoolean(R.bool.config_allow_share_visible_contacts)) {
             adapter.add(R.string.share_visible_contacts);
         }

         final DialogInterface.OnClickListener clickListener =
             new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();

                 final int resId = adapter.getItem(which);
                 switch (resId) {
                 case R.string.import_from_sim:
                 case R.string.import_from_sdcard: {
                     handleImportRequest(resId);
                     break;
                 }
                 case R.string.export_to_sdcard: {
                     Context context = ContactsListActivity.this;
                     Intent exportIntent = new Intent(context, ExportVCardActivity.class);
                     context.startActivity(exportIntent);
                     break;
                 }
                 case R.string.share_visible_contacts: {
                     doShareVisibleContacts();
                     break;
                 }
                 default: {
                     Log.e(TAG, "Unexpected resource: " +
                             getResources().getResourceEntryName(resId));
                 }
                 }
             }
         };

         new AlertDialog.Builder(this)
         .setTitle(R.string.dialog_import_export)
         .setNegativeButton(android.R.string.cancel, null)
         .setSingleChoiceItems(adapter, -1, clickListener)
         .show();
     }

     private void doShareVisibleContacts() {
         final Cursor cursor = getContentResolver().query(Contacts.CONTENT_URI,
                 sLookupProjection, getContactSelection(), null, null);
         try {
             if (!cursor.moveToFirst()) {
                 Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
                 return;
             }

             StringBuilder uriListBuilder = new StringBuilder();
             int index = 0;
             for (;!cursor.isAfterLast(); cursor.moveToNext()) {
                 if (index != 0)
                     uriListBuilder.append(':');
                 uriListBuilder.append(cursor.getString(0));
                 index++;
             }
             Uri uri = Uri.withAppendedPath(
                     Contacts.CONTENT_MULTI_VCARD_URI,
                     Uri.encode(uriListBuilder.toString()));

             final Intent intent = new Intent(Intent.ACTION_SEND);
             intent.setType(Contacts.CONTENT_VCARD_TYPE);
             intent.putExtra(Intent.EXTRA_STREAM, uri);
             startActivity(intent);
         } finally {
             cursor.close();
         }
     }

     private void handleImportRequest(int resId) {
         // There's three possibilities:
         // - more than one accounts -> ask the user
         // - just one account -> use the account without asking the user
         // - no account -> use phone-local storage without asking the user
         final Sources sources = Sources.getInstance(this);
         final List<Account> accountList = sources.getAccounts(true, true);
         final int size = accountList.size();
         if (size > 1) {
             showDialog(resId);
             return;
         }

         AccountSelectionUtil.doImport(this, resId, (size == 1 ? accountList.get(0) : null));
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
         case SUBACTIVITY_NEW_CONTACT:
             if (resultCode == RESULT_OK) {
                 returnPickerResult(null, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                         data.getData(), (mMode & MODE_MASK_PICKER) != 0
                         ? Intent.FLAG_GRANT_READ_URI_PERMISSION : 0);
             }
             break;

         case SUBACTIVITY_VIEW_CONTACT:
             if (resultCode == RESULT_OK) {
                 mAdapter.notifyDataSetChanged();
             }
             break;

         case SUBACTIVITY_DISPLAY_GROUP:
         case SUBACTIVITY_SETTINGS:
             //Modified by GanFeng 20120116, fix bug1780
             // Mark as just created so we re-run the view query
             //mJustCreated = true;
             if (RESULT_OK == resultCode) {
                 setDefaultMode();
                 startQuery();
             }
             break;

         case SUBACTIVITY_FILTER:
         case SUBACTIVITY_SEARCH:
             // Pass through results of filter or search UI
             if (resultCode == RESULT_OK) {
                 setResult(RESULT_OK, data);
                 finish();
             }
             break;
         }
     }

     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
         // If Contacts was invoked by another Activity simply as a way of
         // picking a contact, don't show the context menu
         if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
             return;
         }

         AdapterView.AdapterContextMenuInfo info;
         try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         } catch (ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return;
         }

         Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
         long id = info.id;
         if (mMode != MODE_YILIAO) {
             Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
             long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), id);
             Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);

             // Setup the menu header
             menu.setHeaderTitle(cursor.getString(getSummaryDisplayNameColumnIndex()));

             // View contact details
             //final Intent viewContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
             //StickyTabs.setTab(viewContactIntent, getIntent());
             //menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact)
             //        .setIntent(viewContactIntent);

             //if (cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0) {
             //    // Calling contact
             //    menu.add(0, MENU_ITEM_CALL, 0, getString(R.string.menu_call));
             //    // Send SMS item
             //    menu.add(0, MENU_ITEM_SEND_SMS, 0, getString(R.string.menu_sendSMS));
             //}

             // Star toggling
             int starState = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
             if (starState == 0) {
                 menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_addStar);
             } else {
                 menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_removeStar);
             }

             // Contact editing
             menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_editContact)
             .setIntent(new Intent(Intent.ACTION_EDIT, rawContactUri));
             menu.add(0, MENU_ITEM_SEND_CONTACT, 0, R.string.send_contact);
             //modify by zenghuaying fix bug #8846
             if(mContacts){
                 menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_deleteContact);
             }
             //end
             setupBlacklistContextMenu(cursor, menu);
         } else {
             setupYiliaoContentMenu(cursor, menu);
         }

         if (null != mActivityResultBridge) {
             mActivityResultBridge.handleActivityEvent(
                     this,
                     ActivityResultBridge.EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU,
                     null);
         }
     }

     private void setupYiliaoContentMenu(Cursor cursor, ContextMenu menu) {
         long rosterId = cursor.getLong(cursor.getColumnIndex(Roster._ID));

         if (rosterId != 0) {
             Cursor rosterCursor = mResolver.query(RosterData.CONTENT_URI, new String[]{RosterData.ROSTER_USER_ID}, RosterData.ROSTER_ID + "=" + rosterId, null, null);
             try {
                 String phone;
                 int count = rosterCursor.getCount();
                 if (rosterCursor != null && count > 0) {
                     String name = cursor.getString(cursor.getColumnIndex(Roster.DISPLAY_NAME));
                     Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                     intent.setType("vnd.android.cursor.dir/contact");
                     intent.putExtra("name", name);
                     while(rosterCursor.moveToNext()){
                         phone = rosterCursor.getString(0);
                         intent.putExtra("phone", phone);
                     }
                     menu.add(0, MENU_ITEM_ADD_TO_LOCAL, 0, R.string.yiliao_add_to_local_contacts)
                     .setIntent(intent);

                     rosterCursor.moveToFirst();
                     if (count == 1) {
                         phone = rosterCursor.getString(0);
                         if (CommonMethod.numberIsInBlacklist(this, phone)) {
                             menu.add(0, MENU_ITEM_CLEAR_FROM_BLACKLIST, 0, R.string.clear_from_blacklist);
                         }
                         else {
                             menu.add(0, MENU_ITEM_ADD_TO_BLACKLIST, 0, R.string.add_to_blacklist);
                         }
                     }
                 } 
             } finally {
                 rosterCursor.close();
                 rosterCursor = null;
             }
         }
     }
     private void setupBlacklistContextMenu(Cursor cursor, ContextMenu menu) {
         if (((MODE_DEFAULT_NO_CREATE_NEW != mMode) && (MODE_DEFAULT_WITH_CREATE_NEW != mMode) && (MODE_STREQUENT != mMode) && (MODE_YILIAO != mMode))
                 || (null == cursor)
                 || (0 == cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX))) {
             return;
         }

         Cursor phonesCursor = null;
         phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
         if (null != phonesCursor) {
             int index = phonesCursor.getColumnIndex(Phone.NUMBER);
             if (1 == phonesCursor.getCount()) {
                 String number = phonesCursor.getString(index);
                 if (CommonMethod.numberIsInBlacklist(this, number)) {
                     menu.add(0, MENU_ITEM_CLEAR_FROM_BLACKLIST, 0, R.string.clear_from_blacklist);
                 } else {
                     menu.add(0, MENU_ITEM_ADD_TO_BLACKLIST, 0, R.string.add_to_blacklist);
                 }
             } else {
                 ArrayList<String> arrayList = new ArrayList<String>();                
                 do {
                     arrayList.add(phonesCursor.getString(index));
                 } while (phonesCursor.moveToNext());
                 if (CommonMethod.blacklistContainsAnyNumber(this, arrayList)) {
                     menu.add(0, MENU_ITEM_CLEAR_FROM_BLACKLIST, 0, R.string.clear_from_blacklist);
                 } else {
                     menu.add(0, MENU_ITEM_ADD_TO_BLACKLIST, 0, R.string.add_to_blacklist);
                 }
             }
         }

         if (null != phonesCursor) {
             phonesCursor.close();
         }
     }

     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info;
         try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         } catch (ClassCastException e) {
             Log.e(TAG, "bad menuInfo", e);
             return false;
         }

         Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

         switch (item.getItemId()) {
         case MENU_ITEM_TOGGLE_STAR: {
             // Toggle the star
             ContentValues values = new ContentValues(1);
             values.put(Contacts.STARRED, cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX) == 0 ? 1 : 0);
             final Uri selectedUri = this.getContactUri(info.position);
             getContentResolver().update(selectedUri, values, null, null);
             return true;
         }

         case MENU_ITEM_CALL: {
             callContact(cursor);
             return true;
         }

         case MENU_ITEM_SEND_SMS: {
             smsContact(cursor);
             return true;
         }

         case MENU_ITEM_SEND_CONTACT: {
             Intent intent = new Intent(Intent.ACTION_SEND);
             final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
             final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
             Uri uri = Contacts.getLookupUri(contactId, lookupKey);
             intent.setType(CommonMethod.CONTENT_CONTACT_TYPE);
             intent.putExtra(Intent.EXTRA_STREAM, uri);
             startActivity(intent);
             return true;
         }

         case MENU_ITEM_DELETE: {
             doContactDelete(getContactUri(info.position));
             return true;
         }

         case MENU_ITEM_ADD_TO_BLACKLIST: {
             addToBlacklist(cursor);
             return true;
         }
         case MENU_ITEM_CLEAR_FROM_BLACKLIST: {
             ClearFromBlacklist(cursor);
             return true;
         }
         }

         return super.onContextItemSelected(item);
     }

     /**
      * Event handler for the use case where the user starts typing without
      * bringing up the search UI first.
      */
     public boolean onKey(View v, int keyCode, KeyEvent event) {
         if (!mSearchMode && (mMode & MODE_MASK_NO_FILTER) == 0 && !mSearchInitiated) {
             int unicodeChar = event.getUnicodeChar();
             if (unicodeChar != 0) {
                 mSearchInitiated = true;
                 startSearch(new String(new int[]{unicodeChar}, 0, 1), false, null, false);
                 return true;
             }
         }
         return false;
     }

     /**
      * Event handler for search UI.
      */
     public void afterTextChanged(Editable s) {
         onSearchTextChanged();
     }

     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
     }

     public void onTextChanged(CharSequence s, int start, int before, int count) {
     }

     /**
      * Event handler for search UI.
      */
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         if (actionId == EditorInfo.IME_ACTION_DONE) {
             hideSoftKeyboard();
             if (TextUtils.isEmpty(getTextFilter())) {
                 finish();
             }
             return true;
         }
         return false;
     }

     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
         case KeyEvent.KEYCODE_CALL: {
             if (callSelection()) {
                 return true;
             }
             break;
         }

         case KeyEvent.KEYCODE_DEL: {
             if (deleteSelection()) {
                 return true;
             }
             break;
         }
         }

         return super.onKeyDown(keyCode, event);
     }

     private boolean deleteSelection() {
         if ((mMode & MODE_MASK_PICKER) != 0) {
             return false;
         }

         final int position = getListView().getSelectedItemPosition();
         if (position != ListView.INVALID_POSITION) {
             Uri contactUri = getContactUri(position);
             if (contactUri != null) {
                 doContactDelete(contactUri);
                 return true;
             }
         }
         return false;
     }

     /**
      * Prompt the user before deleting the given {@link Contacts} entry.
      */
     protected void doContactDelete(Uri contactUri) {
         mReadOnlySourcesCnt = 0;
         mWritableSourcesCnt = 0;
         mWritableRawContactIds.clear();

         Sources sources = Sources.getInstance(ContactsListActivity.this);
         Cursor c = getContentResolver().query(RawContacts.CONTENT_URI, RAW_CONTACTS_PROJECTION,
                 RawContacts.CONTACT_ID + "=" + ContentUris.parseId(contactUri), null,
                 null);
         if (c != null) {
             try {
                 while (c.moveToNext()) {
                     final String accountType = c.getString(2);
                     final long rawContactId = c.getLong(0);
                     ContactsSource contactsSource = sources.getInflatedSource(accountType,
                             ContactsSource.LEVEL_SUMMARY);
                     if (contactsSource != null && contactsSource.readOnly) {
                         mReadOnlySourcesCnt += 1;
                     } else {
                         mWritableSourcesCnt += 1;
                         mWritableRawContactIds.add(rawContactId);
                     }
                 }
             } finally {
                 c.close();
             }
         }

         mSelectedContactUri = contactUri;
         if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt > 0) {
             showDialog(R.id.dialog_readonly_contact_delete_confirmation);
         } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
             showDialog(R.id.dialog_readonly_contact_hide_confirmation);
         } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
             showDialog(R.id.dialog_multiple_contact_delete_confirmation);
         } else {
             showDialog(R.id.dialog_delete_contact_confirmation);
         }
     }

     /**
      * Dismisses the soft keyboard when the list takes focus.
      */
     public void onFocusChange(View view, boolean hasFocus) {
         if (view == getListView() && hasFocus) {
             hideSoftKeyboard();
         }
     }

     /**
      * Dismisses the soft keyboard when the list takes focus.
      */
     public boolean onTouch(View view, MotionEvent event) {
         if (view == getListView()) {
             hideSoftKeyboard();
         }
         return false;
     }

     /**
      * Dismisses the search UI along with the keyboard if the filter text is empty.
      */
     public boolean onKeyPreIme(int keyCode, KeyEvent event) {
         Log.i(TAG, "onKeyPreIme start");
         /**
        if (mSearchMode
                && (keyCode == KeyEvent.KEYCODE_BACK)
                && (MODE_DEL_MULTIPLE != mMode)
                && TextUtils.isEmpty(getTextFilter())) {
        	hideSoftKeyboard();
        	onBackPressed();
            return true;
        }*/
         return false;
     }

     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {

         hideSoftKeyboard();     

         if (mMode == MODE_DEL_MULTIPLE) {          
             CheckedItemInfo iteminfo = (CheckedItemInfo)v.getTag();;            
             iteminfo.checkbox.setChecked(!iteminfo.checkbox.isChecked());  
             return;
         } else if (mMode == MODE_DEFAULT_NOTIF && mSearchMode == false) {
             if (ContactItemListAdapter.ITEM_TYPE_NOTIFICATION == mAdapter.getItemViewType(position)) {
                 ItemInfoWithNotification itemInfo = (ItemInfoWithNotification)v.getTag();
                 ContactNotificationInfo notificationInfo = getNotificationInfo(itemInfo.contactId);
                 if (notificationInfo != null) {
                     if (getUnreadCallLogByContactId(itemInfo.contactId) != 0) {
                         if (notificationInfo.mName != null
                                 && notificationInfo.mNumber != null)
                             CommonMethod.viewPimDetail(this,
                                     notificationInfo.mName,
                                     notificationInfo.mNumber.get(0),
                                     itemInfo.contactId, DetailEntry.LOG_DETAIL);
                     } else {
                         if (notificationInfo.mNumber != null) {
                             Intent intent = new Intent(Intent.ACTION_SENDTO,
                                     Uri.fromParts(Constants.SCHEME_SMSTO,
                                             notificationInfo.mNumber.get(0),
                                             null));
                             startActivity(intent);
                         }
                     }
                 }
                 return;
             }
         }

         if (mSearchMode && mAdapter.isSearchAllContactsItemPosition(position)) {
             doSearch();
         } else if (mMode == MODE_INSERT_OR_EDIT_CONTACT || mMode == MODE_QUERY_PICK_TO_EDIT) {
             //Modified by GanFeng 20111208, remove the MODE_MASK_CREATE_NEW from the MODE_INSERT_OR_EDIT_CONTACT
             Intent intent;
             if (position == 0 && !mSearchMode && mMode != MODE_QUERY_PICK_TO_EDIT
                     && (MODE_MASK_CREATE_NEW == (mMode & MODE_MASK_CREATE_NEW))) {
                 intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
             } else {
                 intent = new Intent(Intent.ACTION_EDIT, getSelectedUri(position));
             }
             intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
             Bundle extras = getIntent().getExtras();
             if (extras != null) {
                 intent.putExtras(extras);
             }
             intent.putExtra(KEY_PICKER_MODE, (mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER);

             startActivity(intent);
             finish();
         } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW
                 && position == 0) {
             Intent newContact = new Intent(Intents.Insert.ACTION, Contacts.CONTENT_URI);
             startActivityForResult(newContact, SUBACTIVITY_NEW_CONTACT);
         } else if (mMode == MODE_JOIN_CONTACT && id == JOIN_MODE_SHOW_ALL_CONTACTS_ID) {
             mJoinModeShowAllContacts = false;
             startQuery();
         } else if (id > 0) {
             final Uri uri = getSelectedUri(position);
             if ((mMode & MODE_MASK_PICKER) == 0 || MODE_YILIAO == mMode) {
                 final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 StickyTabs.setTab(intent, getIntent());
                 startActivityForResult(intent, SUBACTIVITY_VIEW_CONTACT);
             } else if (mMode == MODE_JOIN_CONTACT) {
                 returnPickerResult(null, null, uri, 0);
             } else if (mMode == MODE_QUERY_PICK_TO_VIEW) {
                 // Started with query that should launch to view contact
                 final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 startActivity(intent);
                 finish();
             } else if (mMode == MODE_PICK_PHONE || mMode == MODE_QUERY_PICK_PHONE) {
                 Cursor c = (Cursor) mAdapter.getItem(position);
                 returnPickerResult(c, c.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX), uri,
                         Intent.FLAG_GRANT_READ_URI_PERMISSION);
             } else if ((mMode & MODE_MASK_PICKER) != 0) {
                 Cursor c = (Cursor) mAdapter.getItem(position);
                 returnPickerResult(c, c.getString(getSummaryDisplayNameColumnIndex()), uri,
                         Intent.FLAG_GRANT_READ_URI_PERMISSION);
             } else if (mMode == MODE_PICK_POSTAL
                     || mMode == MODE_LEGACY_PICK_POSTAL
                     || mMode == MODE_LEGACY_PICK_PHONE) {
                 returnPickerResult(null, null, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
             }
             //        } else if ((mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && (position == 0) && mSearchMode == false) {
             //            Intent intent = new Intent();
             //            intent.setClass(this, ContactsAccountInfoActivity.class);
             //            intent.putExtra("user_id", mAccountInfo.mUserId);
             //            startActivity(intent);
         } else {
             signalError();
         }
     }

     private void hideSoftKeyboard() {
         // Hide soft keyboard, if visible
         InputMethodManager inputMethodManager = (InputMethodManager)
         getSystemService(Context.INPUT_METHOD_SERVICE);
         inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
     }

     /**
      * @param selectedUri In most cases, this should be a lookup {@link Uri}, possibly
      *            generated through {@link Contacts#getLookupUri(long, String)}.
      */
     private void returnPickerResult(Cursor c, String name, Uri selectedUri, int uriPerms) {
         final Intent intent = new Intent();

         if (mShortcutAction != null) {
             Intent shortcutIntent;
             if (Intent.ACTION_VIEW.equals(mShortcutAction)) {
                 // This is a simple shortcut to view a contact.
                 shortcutIntent = new Intent(ContactsContract.QuickContact.ACTION_QUICK_CONTACT);
                 shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                         Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                 shortcutIntent.setData(selectedUri);
                 shortcutIntent.putExtra(ContactsContract.QuickContact.EXTRA_MODE,
                         ContactsContract.QuickContact.MODE_LARGE);
                 shortcutIntent.putExtra(ContactsContract.QuickContact.EXTRA_EXCLUDE_MIMES,
                         (String[]) null);

                 final Bitmap icon = framePhoto(loadContactPhoto(selectedUri, null));
                 if (icon != null) {
                     intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, scaleToAppIconSize(icon));
                 } else {
                     intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                             Intent.ShortcutIconResource.fromContext(this,
                                     R.drawable.ic_launcher_shortcut_contact));
                 }
             } else {
                 // This is a direct dial or sms shortcut.
                 String number = c.getString(PHONE_NUMBER_COLUMN_INDEX);
                 int type = c.getInt(PHONE_TYPE_COLUMN_INDEX);
                 String scheme;
                 int resid;
                 if (Intent.ACTION_CALL.equals(mShortcutAction)) {
                     scheme = Constants.SCHEME_TEL;
                     resid = R.drawable.badge_action_call;
                 } else {
                     scheme = Constants.SCHEME_SMSTO;
                     resid = R.drawable.badge_action_sms;
                 }

                 // Make the URI a direct tel: URI so that it will always continue to work
                 Uri phoneUri = Uri.fromParts(scheme, number, null);
                 shortcutIntent = new Intent(mShortcutAction, phoneUri);

                 intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                         generatePhoneNumberIcon(selectedUri, type, resid));
             }
             shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
             intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
             setResult(RESULT_OK, intent);
         } else {
             intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
             intent.addFlags(uriPerms);
             setResult(RESULT_OK, intent.setData(selectedUri));
         }
         finish();
     }

     private Bitmap framePhoto(Bitmap photo) {
         final Resources r = getResources();
         final Drawable frame = r.getDrawable(com.android.internal.R.drawable.quickcontact_badge);

         final int width = r.getDimensionPixelSize(R.dimen.contact_shortcut_frame_width);
         final int height = r.getDimensionPixelSize(R.dimen.contact_shortcut_frame_height);

         frame.setBounds(0, 0, width, height);

         final Rect padding = new Rect();
         frame.getPadding(padding);

         final Rect source = new Rect(0, 0, photo.getWidth(), photo.getHeight());
         final Rect destination = new Rect(padding.left, padding.top,
                 width - padding.right, height - padding.bottom);

         final int d = Math.max(width, height);
         final Bitmap b = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
         final Canvas c = new Canvas(b);

         c.translate((d - width) / 2.0f, (d - height) / 2.0f);
         frame.draw(c);
         c.drawBitmap(photo, source, destination, new Paint(Paint.FILTER_BITMAP_FLAG));

         return b;
     }

     /**
      * Generates a phone number shortcut icon. Adds an overlay describing the type of the phone
      * number, and if there is a photo also adds the call action icon.
      *
      * @param lookupUri The person the phone number belongs to
      * @param type The type of the phone number
      * @param actionResId The ID for the action resource
      * @return The bitmap for the icon
      */
     private Bitmap generatePhoneNumberIcon(Uri lookupUri, int type, int actionResId) {
         final Resources r = getResources();
         boolean drawPhoneOverlay = true;
         final float scaleDensity = getResources().getDisplayMetrics().scaledDensity;

         Bitmap photo = loadContactPhoto(lookupUri, null);
         if (photo == null) {
             // If there isn't a photo use the generic phone action icon instead
             Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
             if (phoneIcon != null) {
                 photo = phoneIcon;
                 drawPhoneOverlay = false;
             } else {
                 return null;
             }
         }

         // Setup the drawing classes
         Bitmap icon = createShortcutBitmap();
         Canvas canvas = new Canvas(icon);

         // Copy in the photo
         Paint photoPaint = new Paint();
         photoPaint.setDither(true);
         photoPaint.setFilterBitmap(true);
         Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
         Rect dst = new Rect(0,0, mIconSize, mIconSize);
         canvas.drawBitmap(photo, src, dst, photoPaint);

         // Create an overlay for the phone number type
         String overlay = null;
         switch (type) {
         case Phone.TYPE_HOME:
             overlay = getString(R.string.type_short_home);
             break;

         case Phone.TYPE_MOBILE:
             overlay = getString(R.string.type_short_mobile);
             break;

         case Phone.TYPE_WORK:
             overlay = getString(R.string.type_short_work);
             break;

         case Phone.TYPE_PAGER:
             overlay = getString(R.string.type_short_pager);
             break;

         case Phone.TYPE_OTHER:
             overlay = getString(R.string.type_short_other);
             break;
         }
         if (overlay != null) {
             Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
             textPaint.setTextSize(20.0f * scaleDensity);
             textPaint.setTypeface(Typeface.DEFAULT_BOLD);
             textPaint.setColor(r.getColor(R.color.textColorIconOverlay));
             textPaint.setShadowLayer(3f, 1, 1, r.getColor(R.color.textColorIconOverlayShadow));
             canvas.drawText(overlay, 2 * scaleDensity, 16 * scaleDensity, textPaint);
         }

         // Draw the phone action icon as an overlay
         if (ENABLE_ACTION_ICON_OVERLAYS && drawPhoneOverlay) {
             Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
             if (phoneIcon != null) {
                 src.set(0, 0, phoneIcon.getWidth(), phoneIcon.getHeight());
                 int iconWidth = icon.getWidth();
                 dst.set(iconWidth - ((int) (20 * scaleDensity)), -1,
                         iconWidth, ((int) (19 * scaleDensity)));
                 canvas.drawBitmap(phoneIcon, src, dst, photoPaint);
             }
         }

         return icon;
     }

     private Bitmap scaleToAppIconSize(Bitmap photo) {
         // Setup the drawing classes
         Bitmap icon = createShortcutBitmap();
         Canvas canvas = new Canvas(icon);

         // Copy in the photo
         Paint photoPaint = new Paint();
         photoPaint.setDither(true);
         photoPaint.setFilterBitmap(true);
         Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
         Rect dst = new Rect(0,0, mIconSize, mIconSize);
         canvas.drawBitmap(photo, src, dst, photoPaint);

         return icon;
     }

     private Bitmap createShortcutBitmap() {
         return Bitmap.createBitmap(mIconSize, mIconSize, Bitmap.Config.ARGB_8888);
     }

     /**
      * Returns the icon for the phone call action.
      *
      * @param r The resources to load the icon from
      * @param resId The resource ID to load
      * @return the icon for the phone call action
      */
     private Bitmap getPhoneActionIcon(Resources r, int resId) {
         Drawable phoneIcon = r.getDrawable(resId);
         if (phoneIcon instanceof BitmapDrawable) {
             BitmapDrawable bd = (BitmapDrawable) phoneIcon;
             return bd.getBitmap();
         } else {
             return null;
         }
     }

     private Uri getUriToQuery() {
         //        if (MODE_DEFAULT == mMode) {
         //            return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
         //        }

         switch(mMode) {
         case MODE_JOIN_CONTACT:
             return getJoinSuggestionsUri(null);
         case MODE_FREQUENT:
         case MODE_STARRED:
             return Contacts.CONTENT_URI;

             //case MODE_DEFAULT:
         case MODE_DEFAULT_NO_CREATE_NEW:
         case MODE_DEFAULT_WITH_CREATE_NEW:
         case MODE_DEFAULT_NOTIF:
         case MODE_DEL_MULTIPLE:
         case MODE_CUSTOM:
         case MODE_INSERT_OR_EDIT_CONTACT:
         case MODE_PICK_CONTACT:
         case MODE_PICK_OR_CREATE_CONTACT:{
             return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
         }
         case MODE_STREQUENT: {
             //Wysie: Return a different Uri if hide frequently called is enabled
             if (ePrefs.getBoolean("favourites_hide_freq_called", false)) {
                 return Contacts.CONTENT_URI;
             }
             else {
                 return Contacts.CONTENT_STREQUENT_URI;
             }
         }
         case MODE_LEGACY_PICK_PERSON:
         case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
             return People.CONTENT_URI;
         }
         case MODE_PICK_PHONE: {
             return buildSectionIndexerUri(Phone.CONTENT_URI);
         }
         case MODE_LEGACY_PICK_PHONE: {
             return Phones.CONTENT_URI;
         }
         case MODE_PICK_POSTAL: {
             return buildSectionIndexerUri(StructuredPostal.CONTENT_URI);
         }
         case MODE_LEGACY_PICK_POSTAL: {
             return ContactMethods.CONTENT_URI;
         }
         case MODE_QUERY_PICK_TO_VIEW: {
             if (mQueryMode == QUERY_MODE_MAILTO) {
                 return Uri.withAppendedPath(Email.CONTENT_FILTER_URI,
                         Uri.encode(mInitialFilter));
             } else if (mQueryMode == QUERY_MODE_TEL) {
                 return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI,
                         Uri.encode(mInitialFilter));
             }
             return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
         }
         case MODE_QUERY:
         case MODE_QUERY_PICK:
         case MODE_QUERY_PICK_TO_EDIT: {
             return getContactFilterUri(mInitialFilter);
         }
         case MODE_QUERY_PICK_PHONE: {
             return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI,
                     Uri.encode(mInitialFilter));
         }
         case MODE_GROUP: {
             return mGroupUri;
         }
         case MODE_YILIAO: {
             return ROSTER_CONTENT_URI_WITH_LETTER_COUNTS;//Roster.CONTENT_URI;
         }
         default: {
             throw new IllegalStateException("Can't generate URI: Unsupported Mode.");
         }
         }
     }

     /**
      * Build the {@link Contacts#CONTENT_LOOKUP_URI} for the given
      * {@link ListView} position, using {@link #mAdapter}.
      */
     private Uri getContactUri(int position) {
         if (position == ListView.INVALID_POSITION) {
             throw new IllegalArgumentException("Position not in list bounds");
         }

         final Cursor cursor = (Cursor)mAdapter.getItem(position);
         if (cursor == null) {
             return null;
         }

         switch(mMode) {
         case MODE_LEGACY_PICK_PERSON:
         case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
             final long personId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
             return ContentUris.withAppendedId(People.CONTENT_URI, personId);
         }

         default: {
             // Build and return soft, lookup reference
             final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
             final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
             return Contacts.getLookupUri(contactId, lookupKey);
         }
         }
     }

     /**
      * Build the {@link Uri} for the given {@link ListView} position, which can
      * be used as result when in {@link #MODE_MASK_PICKER} mode.
      */
     private Uri getSelectedUri(int position) {
         if (position == ListView.INVALID_POSITION) {
             throw new IllegalArgumentException("Position not in list bounds");
         }

         final long id = mAdapter.getItemId(position);
         switch(mMode) {
         case MODE_LEGACY_PICK_PERSON:
         case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
             return ContentUris.withAppendedId(People.CONTENT_URI, id);
         }
         case MODE_PICK_PHONE:
         case MODE_QUERY_PICK_PHONE: {
             return ContentUris.withAppendedId(Data.CONTENT_URI, id);
         }
         case MODE_LEGACY_PICK_PHONE: {
             return ContentUris.withAppendedId(Phones.CONTENT_URI, id);
         }
         case MODE_PICK_POSTAL: {
             return ContentUris.withAppendedId(Data.CONTENT_URI, id);
         }
         case MODE_LEGACY_PICK_POSTAL: {
             return ContentUris.withAppendedId(ContactMethods.CONTENT_URI, id);
         }
         case MODE_YILIAO: {
             return ContentUris.withAppendedId(Roster.CONTENT_URI, id);
         }
         default: {
             return getContactUri(position);
         }
         }
     }

     String[] getProjectionForQuery() {
         if (MODE_DEFAULT == mMode) {
             return mSearchMode
             ? CONTACTS_SUMMARY_FILTER_PROJECTION
                     : CONTACTS_SUMMARY_PROJECTION;
         }

         switch(mMode) {
         case MODE_JOIN_CONTACT:
         case MODE_STREQUENT:
         case MODE_FREQUENT:
         case MODE_STARRED:
             //case MODE_DEFAULT:
         case MODE_DEFAULT_NO_CREATE_NEW:
         case MODE_DEFAULT_WITH_CREATE_NEW:
         case MODE_DEFAULT_NOTIF:
         case MODE_CUSTOM:
         case MODE_INSERT_OR_EDIT_CONTACT:
         case MODE_GROUP:
         case MODE_PICK_CONTACT:
         case MODE_PICK_OR_CREATE_CONTACT: {
             return mSearchMode
             ? CONTACTS_SUMMARY_FILTER_PROJECTION
                     : CONTACTS_SUMMARY_PROJECTION;
         }
         case MODE_DEL_MULTIPLE: {
             return CONTACTS_SUMMARY_PROJECTION;//CONTACTS_SUMMARY_FILTER_PROJECTION;
         }
         case MODE_QUERY:
         case MODE_QUERY_PICK:
         case MODE_QUERY_PICK_TO_EDIT: {
             return CONTACTS_SUMMARY_FILTER_PROJECTION;
         }
         case MODE_LEGACY_PICK_PERSON:
         case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
             return LEGACY_PEOPLE_PROJECTION ;
         }
         case MODE_QUERY_PICK_PHONE:
         case MODE_PICK_PHONE: {
             return PHONES_PROJECTION;
         }
         case MODE_LEGACY_PICK_PHONE: {
             return LEGACY_PHONES_PROJECTION;
         }
         case MODE_PICK_POSTAL: {
             return POSTALS_PROJECTION;
         }
         case MODE_LEGACY_PICK_POSTAL: {
             return LEGACY_POSTALS_PROJECTION;
         }
         case MODE_QUERY_PICK_TO_VIEW: {
             if (mQueryMode == QUERY_MODE_MAILTO) {
                 return CONTACTS_SUMMARY_PROJECTION_FROM_EMAIL;
             } else if (mQueryMode == QUERY_MODE_TEL) {
                 return PHONES_PROJECTION;
             }
             break;
         }
         case MODE_YILIAO: {
             return CONTACTS_YILIAO_PROJECTION;
         }
         }

         // Default to normal aggregate projection
         return CONTACTS_SUMMARY_PROJECTION;
     }

     private Bitmap loadContactPhoto(Uri selectedUri, BitmapFactory.Options options) {
         Uri contactUri = null;
         if (Contacts.CONTENT_ITEM_TYPE.equals(getContentResolver().getType(selectedUri))) {
             // TODO we should have a "photo" directory under the lookup URI itself
             contactUri = Contacts.lookupContact(getContentResolver(), selectedUri);
         } else {

             Cursor cursor = getContentResolver().query(selectedUri,
                     new String[] { Data.CONTACT_ID }, null, null, null);
             try {
                 if (cursor != null && cursor.moveToFirst()) {
                     final long contactId = cursor.getLong(0);
                     contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
                 }
             } finally {
                 if (cursor != null) cursor.close();
             }
         }

         Cursor cursor = null;
         Bitmap bm = null;

         try {
             Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
             cursor = getContentResolver().query(photoUri, new String[] {Photo.PHOTO},
                     null, null, null);
             if (cursor != null && cursor.moveToFirst()) {
                 bm = ContactsUtils.loadContactPhoto(cursor, 0, options);
             }
         } finally {
             if (cursor != null) {
                 cursor.close();
             }
         }

         if (bm == null) {
             final int[] fallbacks = {
                     R.drawable.ic_contact_picture,
                     R.drawable.ic_contact_picture_2,
                     R.drawable.ic_contact_picture_3
             };
             bm = BitmapFactory.decodeResource(getResources(),
                     fallbacks[new Random().nextInt(fallbacks.length)]);
         }

         return bm;
     }

     /**
      * Return the selection arguments for a default query based on the
      * {@link #mDisplayOnlyPhones} flag.
      */
     private String getContactSelection() {
         String strSelection = null;
         if (mDisplayOnlyPhones) {
             strSelection = CLAUSE_ONLY_VISIBLE + " AND " + CLAUSE_ONLY_PHONES;
         } else {
             strSelection = CLAUSE_ONLY_VISIBLE;
         }
         if (mshowContactsYOption == CONTACTS_SHOW_YILIAO_ONLY) {
             strSelection = strSelection + " AND " + CLAUSE_ON_LEWA_MSG;
         }
         return strSelection;
     }

     private Uri getContactFilterUri(String filter) {
         Uri baseUri;
         if (!TextUtils.isEmpty(filter)) {
             baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(filter));
         } else {
             baseUri = Contacts.CONTENT_URI;
         }

         if (mAdapter.getDisplaySectionHeadersEnabled()) {
             return buildSectionIndexerUri(baseUri);
         } else {
             return baseUri;
         }
     }

     private Uri getRosterFilterUri(String filter) {
         Uri baseUri = null;
         if (!TextUtils.isEmpty(filter)) {
             baseUri = Uri.withAppendedPath(Roster.CONTENT_FILTER_URI, Uri.encode(filter));
         } else {
             baseUri = Roster.CONTENT_URI;
         }

         if (mAdapter.getDisplaySectionHeadersEnabled()) {
             return buildSectionIndexerUri(baseUri);
         } else {
             return baseUri;
         }
     }

     private Uri getPeopleFilterUri(String filter) {
         if (!TextUtils.isEmpty(filter)) {
             return Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(filter));
         } else {
             return People.CONTENT_URI;
         }
     }

     private static Uri buildSectionIndexerUri(Uri uri) {
         return uri.buildUpon()
         .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
     }

     private Uri getJoinSuggestionsUri(String filter) {
         Builder builder = Contacts.CONTENT_URI.buildUpon();
         builder.appendEncodedPath(String.valueOf(mQueryAggregateId));
         builder.appendEncodedPath(AggregationSuggestions.CONTENT_DIRECTORY);
         if (!TextUtils.isEmpty(filter)) {
             builder.appendEncodedPath(Uri.encode(filter));
         }
         builder.appendQueryParameter("limit", String.valueOf(MAX_SUGGESTIONS));
         return builder.build();
     }

     private String getSortOrder(String[] projectionType) {
         if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
             return Contacts.SORT_KEY_PRIMARY;
         } else {
             return Contacts.SORT_KEY_ALTERNATIVE;
         }
     }

     void startQuery() {

         if (mAdapter == null || mQueryHandler == null){
             return;
         }

         //account info
         if (mMode == MODE_DEFAULT_NO_CREATE_NEW)
         {
             Cursor cursor = getContentResolver().query(User.CONTENT_URI, null, null, null, null);
             if(cursor != null) {
                 try {
                     if (cursor.moveToLast()) {
                         mAccountInfo.mUserId = cursor.getLong(cursor.getColumnIndex(User._ID));
                         mAccountInfo.mName = cursor.getString(cursor.getColumnIndex(User.NICK_NAME));
                         byte[] b = cursor.getBlob(cursor.getColumnIndex(User.PHOTO));
                         if (b != null && b.length != 0) {
                             mAccountInfo.mPhoto = ImageUtil.byteToDrawable(b);
                         }
                     }
                 } finally {
                     cursor.close();
                     cursor = null;
                 }
             }
         }

         // Set the proper empty string
         if (mMode != MODE_DEL_MULTIPLE)
             setEmptyText();

         if (mSearchResultsMode) {
             TextView foundContactsText = (TextView)findViewById(R.id.search_results_found);
             if(foundContactsText == null) {
                 return;
             }
             foundContactsText.setText(R.string.search_results_searching);
         }

         if(mAdapter == null) {
             return;
         }
         mAdapter.setLoading(true);
         mAdapter.setNotificationCursor(null);

         // Cancel any pending queries
         mQueryHandler.cancelOperation(QUERY_TOKEN);
         mQueryHandler.cancelOperation(QUERY_TOKEN_NOTIF);
         mQueryHandler.setLoadingJoinSuggestions(false);

         mSortOrder = mContactsPrefs.getSortOrder();
         mDisplayOrder = mContactsPrefs.getDisplayOrder();

         // When sort order and display order contradict each other, we want to
         // highlight the part of the name used for sorting.
         mHighlightWhenScrolling = false;
         if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY &&
                 mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_ALTERNATIVE) {
             mHighlightWhenScrolling = true;
         } else if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_ALTERNATIVE &&
                 mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
             mHighlightWhenScrolling = true;
         }

         String[] projection = getProjectionForQuery();
         if (mSearchMode && TextUtils.isEmpty(getTextFilter()) && mMode != MODE_DEL_MULTIPLE) {
             mAdapter.changeCursor(new MatrixCursor(projection));
             return;
         }

         String callingPackage = getCallingPackage();
         Uri uri = getUriToQuery();
         if (!TextUtils.isEmpty(callingPackage)) {
             uri = uri.buildUpon()
             .appendQueryParameter(ContactsContract.REQUESTING_PACKAGE_PARAM_KEY,
                     callingPackage)
                     .build();
         }

         // Kick off the new query
         //        if (MODE_DEFAULT == mMode) {
         //            mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, getContactSelection(),
         //                    null, getSortOrder(projection));
         //            return;
         //        }

         switch (mMode) {
         //case MODE_DEFAULT:
         case MODE_DEFAULT_NO_CREATE_NEW:
         case MODE_DEFAULT_WITH_CREATE_NEW:
         case MODE_DEL_MULTIPLE:
         case MODE_GROUP:
         case MODE_CUSTOM:
         case MODE_PICK_CONTACT:
         case MODE_PICK_OR_CREATE_CONTACT:
         case MODE_INSERT_OR_EDIT_CONTACT:
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, getContactSelection(),
                     null, getSortOrder(projection));
             break;
         case MODE_DEFAULT_NOTIF:   
             //mergeUnreadContactInfo();
             String selectionEx = "";
             if (raw_contact_id_unread != null && raw_contact_id_unread.length() != 0) {
                 selectionEx = " AND " + Contacts._ID + " in (" + raw_contact_id_unread + ")";
                 mQueryHandler.startQuery(QUERY_TOKEN_NOTIF, null, uri, projection, getContactSelection()+ selectionEx,
                         null, getSortOrder(projection));
             }
             if (raw_contact_id_unread != null && raw_contact_id_unread.length() != 0)
                 selectionEx = " AND " + Contacts._ID + " not in (" + raw_contact_id_unread + ")";
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, getContactSelection()+ selectionEx,
                     null, getSortOrder(projection));
             break;
         case MODE_LEGACY_PICK_PERSON:
         case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null,
                     People.DISPLAY_NAME);
             break;
         }
         case MODE_PICK_POSTAL:
         case MODE_QUERY:
         case MODE_QUERY_PICK:
         case MODE_QUERY_PICK_PHONE:
         case MODE_QUERY_PICK_TO_VIEW:
         case MODE_QUERY_PICK_TO_EDIT: {
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null,
                     getSortOrder(projection));
             break;
         }

         case MODE_STARRED:
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                     projection, Contacts.STARRED + "=1", null,
                     getSortOrder(projection));
             break;

         case MODE_FREQUENT:
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                     projection,
                     Contacts.TIMES_CONTACTED + " > 0", null,
                     Contacts.TIMES_CONTACTED + " DESC, "
                     + getSortOrder(projection));
             break;

         case MODE_STREQUENT:
             //Wysie: Query is different if hide_freq_called is enabled
             String strWhere = Contacts.STARRED + "=1";
             if (mshowContactsYOption == CONTACTS_SHOW_YILIAO_ONLY) {
                 strWhere = strWhere + " AND " + CLAUSE_ON_LEWA_MSG;
             }
             if (ePrefs.getBoolean("favourites_hide_freq_called", false)) {
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, strWhere, null, getSortOrder(projection));
             }
             else {
                 mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, strWhere, null, null);
             }

             //mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null, null);
             break;

         case MODE_PICK_PHONE:
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                     projection, CLAUSE_ONLY_VISIBLE, null, getSortOrder(projection));
             break;

         case MODE_LEGACY_PICK_PHONE:
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                     projection, null, null, Phones.DISPLAY_NAME);
             break;

         case MODE_LEGACY_PICK_POSTAL:
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                     projection,
                     ContactMethods.KIND + "=" + android.provider.Contacts.KIND_POSTAL, null,
                     ContactMethods.DISPLAY_NAME);
             break;

         case MODE_JOIN_CONTACT:
             mQueryHandler.setLoadingJoinSuggestions(true);
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection,
                     null, null, null);
             break;
         case MODE_YILIAO: {
             mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection,
                     null, null, Roster.SORT_KEY);
             break;
         }
         }
     }

     /**
      * Called from a background thread to do the filter and return the resulting cursor.
      *
      * @param filter the text that was entered to filter on
      * @return a cursor with the results of the filter
      */
     Cursor doFilter(String filter) {
         final ContentResolver resolver = getContentResolver();
         String[] projection = getProjectionForQuery();
         if (mSearchMode && TextUtils.isEmpty(getTextFilter())) {
             if (mMode != MODE_DEL_MULTIPLE) {
                 return new MatrixCursor(projection);
             } else {
                 mSortOrder = mContactsPrefs.getSortOrder();
                 mDisplayOrder = mContactsPrefs.getDisplayOrder();

                 // When sort order and display order contradict each other, we want to
                 // highlight the part of the name used for sorting.
                 mHighlightWhenScrolling = false;
                 if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY &&
                         mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_ALTERNATIVE) {
                     mHighlightWhenScrolling = true;
                 } else if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_ALTERNATIVE &&
                         mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                     mHighlightWhenScrolling = true;
                 }
                 return resolver.query(getUriToQuery(), projection, getContactSelection(), null, getSortOrder(projection));
             }
         }

         if (MODE_DEFAULT == mMode) {
             Cursor cursor = resolver.query(getContactFilterUri(filter), projection,
                     getContactSelection(), null, getSortOrder(projection));

             return cursor;
             //            return resolver.query(getContactFilterUri(filter), projection,
             //                    getContactSelection(), null, getSortOrder(projection));
         }

         switch (mMode) {
         //case MODE_DEFAULT:
         case MODE_DEFAULT_NO_CREATE_NEW:
         case MODE_DEFAULT_WITH_CREATE_NEW:
         case MODE_DEFAULT_NOTIF:
         case MODE_DEL_MULTIPLE:
         case MODE_CUSTOM:
         case MODE_PICK_CONTACT:
         case MODE_PICK_OR_CREATE_CONTACT:
         case MODE_INSERT_OR_EDIT_CONTACT: {
             return resolver.query(getContactFilterUri(filter), projection,
                     getContactSelection(), null, getSortOrder(projection));
         }

         case MODE_LEGACY_PICK_PERSON:
         case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
             return resolver.query(getPeopleFilterUri(filter), projection, null, null,
                     People.DISPLAY_NAME);
         }

         case MODE_STARRED: {
             return resolver.query(getContactFilterUri(filter), projection,
                     Contacts.STARRED + "=1", null,
                     getSortOrder(projection));
         }

         case MODE_FREQUENT: {
             return resolver.query(getContactFilterUri(filter), projection,
                     Contacts.TIMES_CONTACTED + " > 0", null,
                     Contacts.TIMES_CONTACTED + " DESC, "
                     + getSortOrder(projection));
         }

         case MODE_STREQUENT: {
             Uri uri;
             if (!TextUtils.isEmpty(filter)) {
                 uri = Uri.withAppendedPath(Contacts.CONTENT_STREQUENT_FILTER_URI,
                         Uri.encode(filter));
             } else {
                 uri = Contacts.CONTENT_STREQUENT_URI;
             }
             return resolver.query(uri, projection, null, null, null);
         }

         case MODE_PICK_PHONE: {
             Uri uri = getUriToQuery();
             if (!TextUtils.isEmpty(filter)) {
                 uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(filter));
             }
             return resolver.query(uri, projection, CLAUSE_ONLY_VISIBLE, null,
                     getSortOrder(projection));
         }

         case MODE_LEGACY_PICK_PHONE: {
             //TODO: Support filtering here (bug 2092503)
             break;
         }

         case MODE_JOIN_CONTACT: {
             // We are on a background thread. Run queries one after the other synchronously
             Cursor cursor = resolver.query(getJoinSuggestionsUri(filter), projection, null,
                     null, null);
             mAdapter.setSuggestionsCursor(cursor);
             mJoinModeShowAllContacts = false;
             return resolver.query(getContactFilterUri(filter), projection,
                     Contacts._ID + " != " + mQueryAggregateId + " AND " + CLAUSE_ONLY_VISIBLE,
                     null, getSortOrder(projection));
         }
         case MODE_YILIAO: {
             return resolver.query(getRosterFilterUri(filter), projection,
                     null, null, Roster.SORT_KEY);
         }
         }
         throw new UnsupportedOperationException("filtering not allowed in mode " + mMode);
     }

     private Cursor getShowAllContactsLabelCursor(String[] projection) {
         MatrixCursor matrixCursor = new MatrixCursor(projection);
         Object[] row = new Object[projection.length];
         // The only columns we care about is the id
         row[SUMMARY_ID_COLUMN_INDEX] = JOIN_MODE_SHOW_ALL_CONTACTS_ID;
         matrixCursor.addRow(row);
         return matrixCursor;
     }

     /**
      * Calls the currently selected list item.
      * @return true if the call was initiated, false otherwise
      */
     boolean callSelection() {
         ListView list = getListView();
         if (list.hasFocus()) {
             Cursor cursor = (Cursor) list.getSelectedItem();
             return callContact(cursor);
         }
         return false;
     }

     boolean callContact(Cursor cursor) {
         return callOrSmsContact(cursor, false /*call*/);
     }

     boolean smsContact(Cursor cursor) {
         return callOrSmsContact(cursor, true /*sms*/);
     }

     /**
      * Calls the contact which the cursor is point to.
      * @return true if the call was initiated, false otherwise
      */
     boolean callOrSmsContact(Cursor cursor, boolean sendSms) {
         if (cursor == null) {
             return false;
         }

         switch (mMode) {
         case MODE_PICK_PHONE:
         case MODE_LEGACY_PICK_PHONE:
         case MODE_QUERY_PICK_PHONE: {
             String phone = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
             if (sendSms) {
                 ContactsUtils.initiateSms(this, phone);
             } else {
                 ContactsUtils.initiateCall(this, phone);
             }
             return true;
         }
         case MODE_PICK_POSTAL:
         case MODE_LEGACY_PICK_POSTAL: {
             return false;
         }

         default: {

             boolean hasPhone = cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;
             if (!hasPhone) {
                 // There is no phone number.
                 signalError();
                 return false;
             }

             String phone = null;
             Cursor phonesCursor = null;
             phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
             if (phonesCursor == null || phonesCursor.getCount() == 0) {
                 // No valid number
                 signalError();
                 return false;
             } else if (phonesCursor.getCount() == 1) {
                 // only one number, call it.
                 phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
             } else {
                 phonesCursor.moveToPosition(-1);
                 while (phonesCursor.moveToNext()) {
                     if (phonesCursor.getInt(phonesCursor.
                             getColumnIndex(Phone.IS_SUPER_PRIMARY)) != 0) {
                         // Found super primary, call it.
                         phone = phonesCursor.
                         getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                         break;
                     }
                 }
             }

             if (phone == null) {
                 // Display dialog to choose a number to call.
                 PhoneDisambigDialog phoneDialog = new PhoneDisambigDialog(
                         this, phonesCursor, sendSms, StickyTabs.getTab(getIntent()));
                 phoneDialog.show();
             } else {
                 if (sendSms) {
                     ContactsUtils.initiateSms(this, phone);
                 } else {
                     StickyTabs.saveTab(this, getIntent());
                     ContactsUtils.initiateCall(this, phone);
                 }
             }
             // Close the phoneCursor after its use
             if (phonesCursor != null) {
                 phonesCursor.close();
             }
         }
         }
         return true;
     }

     private void addToBlacklist(Cursor cursor) {
         if ((null == cursor) || mMode != MODE_YILIAO && (0 == cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX))) {
             return;
         }

         if (mMode != MODE_YILIAO) {
             Cursor phonesCursor = null;
             phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
             if (null != phonesCursor) {
                 if (1 == phonesCursor.getCount()) {
                     String name = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
                     String number = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                     CommonMethod.addToBlacklist(this, name, number);
                 } else {
                     int index = phonesCursor.getColumnIndex(Phone.NUMBER);
                     ArrayList<String> arrayList = new ArrayList<String>();                
                     do {
                         arrayList.add(phonesCursor.getString(index));
                     } while (phonesCursor.moveToNext());
                     CommonMethod.addToBlacklist(this, arrayList);
                 }
             }

             if (null != phonesCursor) {
                 phonesCursor.close();
             }
         } else {
             ArrayList<String> phoneList = queryYLNumbers(cursor.getLong(cursor.getColumnIndex(Roster._ID)));
             if (null != phoneList) {
                 if (1 == phoneList.size()) {
                     String name = cursor.getString(cursor.getColumnIndex(Roster.DISPLAY_NAME));
                     CommonMethod.addToBlacklist(this, name, phoneList.get(0));
                 } else {
                     CommonMethod.addToBlacklist(this, phoneList);
                 }
                 phoneList = null;
             }
         }
     }

     private void ClearFromBlacklist(Cursor cursor) {
         if ((null == cursor) || mMode != MODE_YILIAO && (0 == cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX))) {
             return;
         }

         if (mMode != MODE_YILIAO) {
             Cursor phonesCursor = null;
             phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
             if (null != phonesCursor) {
                 if (1 == phonesCursor.getCount()) {
                     String name = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
                     String number = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                     CommonMethod.clearFromBlacklist(this, name, number);
                 } else {
                     int index = phonesCursor.getColumnIndex(Phone.NUMBER);
                     ArrayList<String> arrayList = new ArrayList<String>();                
                     do {
                         arrayList.add(phonesCursor.getString(index));
                     } while (phonesCursor.moveToNext());
                     CommonMethod.clearFromBlacklist(this, arrayList);
                 }
             }

             if (null != phonesCursor) {
                 phonesCursor.close();
             }
         } else {
             ArrayList<String> phoneList = queryYLNumbers(cursor.getLong(cursor.getColumnIndex(Roster._ID)));
             if (null != phoneList) {
                 if (1 == phoneList.size()) {
                     String name = cursor.getString(cursor.getColumnIndex(Roster.DISPLAY_NAME));
                     CommonMethod.clearFromBlacklist(this, name, phoneList.get(0));
                 } else {
                     CommonMethod.clearFromBlacklist(this, phoneList);
                 }
                 phoneList = null;
             }
         }
     }

     private Cursor queryPhoneNumbers(ContentResolver resolver, long contactId) {
         Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
         Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);

         Cursor c = getContentResolver().query(dataUri,
                 new String[] {Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY,
                 RawContacts.ACCOUNT_TYPE, Phone.TYPE, Phone.LABEL, Phone.DISPLAY_NAME_PRIMARY},
                 Data.MIMETYPE + "=?", new String[] {Phone.CONTENT_ITEM_TYPE}, null);
         if (c != null) {
             if (c.moveToFirst()) {
                 return c;
             }
             c.close();
         }
         return null;
     }

     //query phone numbers for yiliao
     private ArrayList<String> queryYLNumbers(long rosterId) {
         Cursor cursor = mResolver.query(RosterData.CONTENT_URI, new String[]{RosterData.ROSTER_USER_ID}, 
                 RosterData.ROSTER_ID + "=" + rosterId, null, null);
         ArrayList<String> phoneList = new ArrayList<String>();
         try {
             while (cursor.moveToNext()) {
                 phoneList.add(cursor.getString(0));
             }
         } finally {
             cursor.close();
             cursor = null;
         }
         return phoneList;
     }

     // TODO: fix PluralRules to handle zero correctly and use Resources.getQuantityText directly
     protected String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
         if (count == 0) {
             return getString(zeroResourceId);
         } else {
             String format = getResources().getQuantityText(pluralResourceId, count).toString();
             return String.format(format, count);
         }
     }
     private Cursor queryPhoneNumbers(long contactId) {
         return queryPhoneNumbers(getContentResolver(), contactId);
     }

     /**
      * Signal an error to the user.
      */
     void signalError() {
         //TODO play an error beep or something...
     }

     Cursor getItemForView(View view) {
         ListView listView = getListView();
         int index = listView.getPositionForView(view);
         if (index < 0) {
             return null;
         }
         return (Cursor) listView.getAdapter().getItem(index);
     }
     /**
      * add by zenghuaying 2012.08.07
      * @param count
      * count = 0 means no contacts ,hide search and quickPositon button
      * count > 0 means have contacts,show search and quickPositon button
      */
     void footerDisplay(int count){
         //View searchButton = findViewById(R.id.contacts_search);
         //View positionButton = findViewById(R.id.contacts_quick_position);
         if(searchButton != null && positionButton != null){
             if (count > 0) {
                 searchButton.setVisibility(View.VISIBLE);
                 positionButton.setVisibility(View.VISIBLE);
             }else{
                 searchButton.setVisibility(View.GONE);
                 positionButton.setVisibility(View.GONE);
             }
         }

     }

     private static class QueryHandler extends AsyncQueryHandler {
         protected final WeakReference<ContactsListActivity> mActivity;
         protected boolean mLoadingJoinSuggestions = false;

         public QueryHandler(Context context) {
             super(context.getContentResolver());
             mActivity = new WeakReference<ContactsListActivity>((ContactsListActivity) context);
         }

         public void setLoadingJoinSuggestions(boolean flag) {
             mLoadingJoinSuggestions = flag;
         }

         @Override
         protected void onInsertComplete(int token, Object cookie,
                 Uri uri) {

             if(queReisgerUser.size() == 0) {
                 final ContactsListActivity activity = mActivity.get();
                 if (activity != null && !activity.isFinishing()) {
                     activity.getOnlinePeople(null);
                     if (activity.mProgress != null) {
                         Dialog dialog = activity.mProgress.get();
                         if (dialog != null && dialog.isShowing()) {
                             dialog.dismiss();
                         }
                         activity.mProgress = null;
                         Toast.makeText(activity, R.string.yiliao_progress_match_yiliao_number_done, Toast.LENGTH_SHORT).show();
                     }

                     if (token == INSERT_TOKEN_MANUAL_REFRESH) {
                         Toast.makeText(activity, R.string.yiliao_progress_match_yiliao_number_done, Toast.LENGTH_SHORT).show();
                     }
                     activity.mbStartInsertRoster = false;
                 }

             }
             else {
                 try {
                     Thread.sleep(200);// wait 200 ms
                 } catch (InterruptedException e) {
                 }
                 ContentValues value = new ContentValues(1);
                 value.put(RosterData.ROSTER_USER_ID, queReisgerUser.poll());
                 startInsert(token, null,RosterData.CONTENT_URI, value);
             }
         }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final ContactsListActivity activity = mActivity.get();
            if (activity != null &&  activity.mAdapter != null && !activity.isFinishing()) {
                if (cursor != null && token == QUERY_TOKEN_NOTIF) {

                     if (cursor.getCount() > 0) {
                         activity.mAdapter.setNotificationCursor(cursor);
                     } else {
                         cursor.close();
                         activity.mAdapter.setNotificationCursor(null);
                     }

                    return;
                }
                //add by zenghuaying for when no cotacts hide search and quickPostion button
                if(cursor!=null){
                    activity.footerDisplay(cursor.getCount());
                }
                // Whenever we get a suggestions cursor, we need to immediately kick off
                // another query for the complete list of contacts
                if (cursor != null && mLoadingJoinSuggestions) {
                    mLoadingJoinSuggestions = false;
                    if (cursor.getCount() > 0) {
                        activity.mAdapter.setSuggestionsCursor(cursor);
                    } else {
                        cursor.close();
                        activity.mAdapter.setSuggestionsCursor(null);
                    }

                     if (activity.mAdapter.mSuggestionsCursorCount == 0
                             || !activity.mJoinModeShowAllContacts) {
                         startQuery(QUERY_TOKEN, null, activity.getContactFilterUri(
                                 activity.getTextFilter()),
                                 CONTACTS_SUMMARY_PROJECTION,
                                 Contacts._ID + " != " + activity.mQueryAggregateId
                                 + " AND " + CLAUSE_ONLY_VISIBLE, null,
                                 activity.getSortOrder(CONTACTS_SUMMARY_PROJECTION));
                         return;
                     }

                    cursor = activity.getShowAllContactsLabelCursor(CONTACTS_SUMMARY_PROJECTION);
                }
                
                //init array of first letter for fast scroll
                //Modified by GanFeng 20111213, fix the bug2303
                if ((activity.mMode == MODE_DEFAULT_NOTIF)
                        || (activity.mMode == MODE_STREQUENT)
                        || (activity.mMode == MODE_DEFAULT)
                        || (activity.mMode == MODE_DEFAULT_NO_CREATE_NEW)
                        || (activity.mMode == MODE_DEFAULT_WITH_CREATE_NEW)
                        || (activity.mMode == MODE_DEL_MULTIPLE)
                        || (activity.mMode == MODE_PICK_CONTACT)
                        || (activity.mMode == MODE_PICK_OR_CREATE_CONTACT)
                        || (activity.mMode == MODE_INSERT_OR_EDIT_CONTACT)
                        || (activity.mMode == MODE_YILIAO)
                        //add by zenghuaying fix bug #10714
                        || (activity.mMode == MODE_PICK_PHONE)) {
                    activity.mPrevLetter = Character.MIN_VALUE;
                    activity.marrFirstLetter.clear();
                    if ((null != cursor) && (cursor.getCount() > 0)) {
                        Cursor cursorTmp = cursor;
                        cursor.moveToFirst();
                        int columnIndex = 1;
                        do {
                            CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
                            columnIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY);
                            cursor.copyStringToBuffer(columnIndex, nameBuffer);
                            
                            //Added by GanFeng 20120129, lower case to upper case
                            if ((nameBuffer.data[0] >= 'a') && (nameBuffer.data[0] <= 'z')) {
                                nameBuffer.data[0] -= 0x20;
                            }
                            activity.marrFirstLetter.add(String.copyValueOf(nameBuffer.data, 0, 1));
                        } while (cursor.moveToNext());
                        
                    }
                }

                 activity.mAdapter.changeCursor(cursor);

                 // Now that the cursor is populated again, it's possible to restore the list state
                 if (activity.mListState != null) {
                     activity.mList.onRestoreInstanceState(activity.mListState);
                     activity.mListState = null;
                 }
             } else {
                 if (cursor != null) {
                     cursor.close();
                 }
             }
         }
     }

     final static class ContactListItemCache {
         public View header;
         public TextView headerText;
         public View divider;
         public TextView nameView;
         public View callView;
         public ImageView callButton;
         public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
         public TextView labelView;
         public CharArrayBuffer labelBuffer = new CharArrayBuffer(128);
         public TextView dataView;
         public CharArrayBuffer dataBuffer = new CharArrayBuffer(128);
         public ImageView presenceView;
         public QuickContactBadge photoView;
         public ImageView nonQuickContactPhotoView;
         public CharArrayBuffer highlightedTextBuffer = new CharArrayBuffer(128);
         public TextWithHighlighting textWithHighlighting;
         public CharArrayBuffer phoneticNameBuffer = new CharArrayBuffer(128);
     }

     final static class PhotoInfo {
         public int position;
         public long photoId;

         public PhotoInfo(int position, long photoId) {
             this.position = position;
             this.photoId = photoId;
         }
         public QuickContactBadge photoView;
     }

     final static class PinnedHeaderCache {
         public TextView titleView;
         public ColorStateList textColor;
         public Drawable background;
     }

     final static class ItemInfoWithNotification {
         long contactId;
         ImageView photoView;
         TextView countTextView;
         TextView nameTextView;
         TextView infoTextView;
         QuickContactBadge quickContact;
         View dividerImageView;
         CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
         CharArrayBuffer phoneBuffer = new CharArrayBuffer(128);
     }

     final static class CheckedItemInfo {
         long contactId;
         ImageView photoView;
         TextView nameTextView;
         TextView infoTextView;
         View dividerImageView;
         CheckBox checkbox;
         boolean bChecked = false;
         CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
         CharArrayBuffer phoneBuffer = new CharArrayBuffer(128);
     }

     final static class AccountItemInfo {
         ImageView photoView;
         TextView nameTextView; 
         ImageView statusView;
     }
     private final class ContactItemListAdapter extends ResourceCursorAdapter
     implements /* SectionIndexer, */ OnScrollListener, PinnedHeaderListView.PinnedHeaderAdapter {
         private SectionIndexer mIndexer;
         private String mAlphabet;
         private boolean mLoading = true;
         private CharSequence mUnknownNameText;
         private boolean mDisplayPhotos = false;
         private boolean mDisplayCallButton = false;
         private boolean mDisplayAdditionalData = true;
         private HashMap<Long, SoftReference<Bitmap>> mBitmapCache = null;
         private HashSet<ImageView> mItemsMissingImages = null;
         private int mFrequentSeparatorPos = ListView.INVALID_POSITION;
         private boolean mDisplaySectionHeaders = true;
         private Cursor mSuggestionsCursor;
         private Cursor mNotificationCursor;
         private int mSuggestionsCursorCount;
         private int mNotificationCursorCount = 0;
         private ImageFetchHandler mHandler;
         private static final int FETCH_IMAGE_MSG = 1;
         private int cursorCount = 0;
         public static final int ITEM_TYPE_NORMAL = 0;
         public static final int ITEM_TYPE_NOTIFICATION = 1;
         public static final int ITEM_TYPE_MAX_COUNT = ITEM_TYPE_NOTIFICATION + 1;

         public ContactItemListAdapter(Context context) {
             super(context, R.layout.contacts_list_item, null, false);

             mHandler = new ImageFetchHandler();
             mAlphabet = context.getString(com.android.internal.R.string.fast_scroll_alphabet);

             mUnknownNameText = context.getText(android.R.string.unknownName);
             switch (mMode) {
             case MODE_LEGACY_PICK_POSTAL:
             case MODE_PICK_POSTAL:
             case MODE_LEGACY_PICK_PHONE:
             case MODE_PICK_PHONE:
             case MODE_STREQUENT:
             case MODE_FREQUENT:
                 mDisplaySectionHeaders = false;
                 break;
             }

             if (mSearchMode && (mMode == MODE_DEL_MULTIPLE)) {
                 mDisplaySectionHeaders = false;
             }

             // Do not display the second line of text if in a specific SEARCH query mode, usually for
             // matching a specific E-mail or phone number. Any contact details
             // shown would be identical, and columns might not even be present
             // in the returned cursor.
             if (mMode != MODE_QUERY_PICK_PHONE && mQueryMode != QUERY_MODE_NONE) {
                 mDisplayAdditionalData = false;
             }

             if ((mMode & MODE_MASK_NO_DATA) == MODE_MASK_NO_DATA) {
                 mDisplayAdditionalData = false;
             }

             if ((mMode & MODE_MASK_SHOW_CALL_BUTTON) == MODE_MASK_SHOW_CALL_BUTTON) {
                 mDisplayCallButton = true;
             }

             if ((mMode & MODE_MASK_SHOW_PHOTOS) == MODE_MASK_SHOW_PHOTOS) {
                 mDisplayPhotos = true;
                 setViewResource(R.layout.contacts_list_item_photo);
                 mBitmapCache = new HashMap<Long, SoftReference<Bitmap>>();
                 mItemsMissingImages = new HashSet<ImageView>();
             }

             if (mMode == MODE_STREQUENT || mMode == MODE_FREQUENT) {
                 mDisplaySectionHeaders = false;
             }           

         }

         private class ImageFetchHandler extends Handler {

             @Override
             public void handleMessage(Message message) {
                 if (ContactsListActivity.this.isFinishing()) {
                     return;
                 }
                 switch(message.what) {
                 case FETCH_IMAGE_MSG: {
                     final ImageView imageView = (ImageView) message.obj;
                     if (imageView == null) {
                         break;
                     }

                     final PhotoInfo info = (PhotoInfo)imageView.getTag();
                     if (info == null) {
                         break;
                     }

                     final long photoId = info.photoId;
                     if (photoId == 0) {
                         break;
                     }

                     SoftReference<Bitmap> photoRef = mBitmapCache.get(photoId);
                     if (photoRef == null) {
                         break;
                     }
                     Bitmap photo = photoRef.get();
                     if (photo == null) {
                         mBitmapCache.remove(photoId);
                         break;
                     }

                     // Make sure the photoId on this image view has not changed
                     // while we were loading the image.
                     synchronized (imageView) {
                         final PhotoInfo updatedInfo = (PhotoInfo)imageView.getTag();
                         long currentPhotoId = updatedInfo.photoId;
                         if (currentPhotoId == photoId) {
                             imageView.setImageBitmap(photo);
                             mItemsMissingImages.remove(imageView);
                         }
                     }
                     break;
                 }
                 }
             }

             public void clearImageFecthing() {
                 removeMessages(FETCH_IMAGE_MSG);
             }
         }

         public boolean getDisplaySectionHeadersEnabled() {
             return mDisplaySectionHeaders;
         }

         public void setSuggestionsCursor(Cursor cursor) {
             if (mSuggestionsCursor != null) {
                 mSuggestionsCursor.close();
             }
             mSuggestionsCursor = cursor;
             mSuggestionsCursorCount = cursor == null ? 0 : cursor.getCount();
         }

         public void setNotificationCursor(Cursor cursor) {
             if (mNotificationCursor != null) {
                 mNotificationCursor.close();
             }
             mNotificationCursor = cursor;
             mNotificationCursorCount = cursor == null ? 0 : cursor.getCount();
         }

         private SectionIndexer getNewIndexer(Cursor cursor) {
             /* if (Locale.getDefault().getLanguage().equals(Locale.JAPAN.getLanguage())) {
                return new JapaneseContactListIndexer(cursor, SORT_STRING_INDEX);
            } else { */
             return new AlphabetIndexer(cursor, getSummaryDisplayNameColumnIndex(), mAlphabet);
             /* } */
         }

         /**
          * Callback on the UI thread when the content observer on the backing cursor fires.
          * Instead of calling requery we need to do an async query so that the requery doesn't
          * block the UI thread for a long time.
          */
         @Override
         protected void onContentChanged() {
             CharSequence constraint = getTextFilter();
             if (!TextUtils.isEmpty(constraint)) {
                 // Reset the filter state then start an async filter operation
                 Filter filter = getFilter();
                 filter.filter(constraint);
             } else {
                 // Start an async query
                 startQuery();
             }
         }

         public void setLoading(boolean loading) {
             mLoading = loading;
         }

         @Override
         public boolean isEmpty() {
             if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
                 return true;
             }

             if (mSearchMode && mMode != MODE_DEL_MULTIPLE) {
                 return TextUtils.isEmpty(getTextFilter());
             } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW) {
                 // This mode mask adds a header and we always want it to show up, even
                 // if the list is empty, so always claim the list is not empty.
                 return false;
             } else {
                 if (mCursor == null || mLoading) {
                     // We don't want the empty state to show when loading.
                     return false;
                 } else {
                     return super.isEmpty();
                 }
             }
         }

         @Override
         public int getItemViewType(int position) {
             if (position == 0 && (mShowNumberOfContacts || (mMode & MODE_MASK_CREATE_NEW) != 0)) {
                 return IGNORE_ITEM_VIEW_TYPE;
             }

             if (isShowAllContactsItemPosition(position)) {
                 return IGNORE_ITEM_VIEW_TYPE;
             }

             if (isSearchAllContactsItemPosition(position)) {
                 return IGNORE_ITEM_VIEW_TYPE;
             }

             if (getSeparatorId(position) != 0) {
                 // We don't want the separator view to be recycled.
                 return IGNORE_ITEM_VIEW_TYPE;
             }
             int type = ITEM_TYPE_NORMAL; 
             if (position < mNotificationCursorCount)
                 type = ITEM_TYPE_NOTIFICATION;
             return type;
         }

         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (!mDataValid) {
                 throw new IllegalStateException(
                 "this should only be called when the cursor is valid");
             }

             // handle the total contacts item
             //            if (position == 0 && (mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && mSearchMode == false) {
             //                View view = getLayoutInflater().inflate(R.layout.contacts_list_show_options_item, parent, false);
             //                TextView textView = (TextView)view.findViewById(R.id.text_show_option_item);
             //                textView.setText(mshowContactsYOption == CONTACTS_SHOW_LOCAL ? R.string.show_phone_number : R.string.show_yiliao_number);
             //                textView.setOnClickListener(ContactsListActivity.this);
             //                return view;
             //            }
             /*            if (position == 0 && (mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && mSearchMode == false && mbLWMsgOnoff) {
                final ContactListItemView view = new ContactListItemView(mContext, null);
//                AccountItemInfo cache = new AccountItemInfo();
//                view.setTag(cache);
                bindAccountView(view, mContext);
                return view;
            }*/
             if (position == 0 && mShowNumberOfContacts) {
                 return getTotalContactCountView(parent);
             }

             if (position == 0 && (mMode & MODE_MASK_CREATE_NEW) != 0) {
                 // Add the header for creating a new contact
                 return getLayoutInflater().inflate(R.layout.create_new_contact, parent, false);
             }

             if (isShowAllContactsItemPosition(position)) {
                 return getLayoutInflater().
                 inflate(R.layout.contacts_list_show_all_item, parent, false);
             }

             if (isSearchAllContactsItemPosition(position)) {
                 return getLayoutInflater().
                 inflate(R.layout.contacts_list_search_all_item, parent, false);
             }

             // Handle the separator specially
             int separatorId = getSeparatorId(position);
             if (separatorId != 0) {
                 LinearLayout view = (LinearLayout)getLayoutInflater().
                 inflate(R.layout.list_separator, parent, false);
                 TextView textView = (TextView)view.findViewById(R.id.textview);
                 textView.setShadowLayer(1.0f, 1.0f, 1.2f, 0xffffffff);
                 textView.setText(separatorId);
                 return view;
             }

             boolean showingSuggestion = false;
             Cursor cursor;
             if (position < mNotificationCursorCount) {
                 cursor = mNotificationCursor;
             } else {
                 if (mSuggestionsCursorCount != 0
                         && position < mSuggestionsCursorCount + 2) {

                     showingSuggestion = true;
                     cursor = mSuggestionsCursor;
                 } else {
                     showingSuggestion = false;
                     cursor = mCursor;
                 }
             }

             int realPosition = getRealPosition(position);
             if (!cursor.moveToPosition(realPosition)) {
                 throw new IllegalStateException("couldn't move cursor to position " + position);
             }

             View v;
             int itemType = getItemViewType(position);
             if (convertView == null || convertView.getTag() == null) {
                 if (mMode == MODE_DEL_MULTIPLE) {
                     v = LayoutInflater.from(mContext).inflate(R.layout.contacts_list_multiple_del_item, null);
                     CheckedItemInfo view = new CheckedItemInfo();
                     view.photoView = (ImageView)v.findViewById(R.id.contact_item_photo);
                     view.nameTextView = (TextView)v.findViewById(R.id.contact_name);
                     view.infoTextView = (TextView)v.findViewById(R.id.contact_info);
                     view.checkbox = (CheckBox)v.findViewById(R.id.contacts_list_del_choice);
                     v.setTag(view);
                 } else if (itemType == ITEM_TYPE_NORMAL) {
                     v = newView(mContext, cursor, parent);
                 } else {
                     v = LayoutInflater.from(mContext).inflate(R.layout.contacts_list_item_notification, null);
                     ItemInfoWithNotification view = new ItemInfoWithNotification();
                     v.setTag(view);
                 }
             } else {
                 v = convertView;
             }
             if (mMode == MODE_DEL_MULTIPLE) {
                 bindMultipleCheckView(v, mContext, cursor, realPosition);
                 return v;
             }
             else if (itemType == ITEM_TYPE_NORMAL)
             {
                 if (mMode == MODE_YILIAO)
                     bindYiliaoView(v, mContext, cursor);
                 else
                     bindView(v, mContext, cursor);
             }
             else {
                 bindNotificationView(v, mContext, cursor);
             }

             //Wysie
             if (mContacts) {
                 mDisplaySectionHeaders = showDisplayHeaders;                
             }
             if (itemType == ITEM_TYPE_NORMAL)
                 bindSectionHeader(v, realPosition, mDisplaySectionHeaders && !showingSuggestion);
             return v;
         }
         //bind account view
         public void bindAccountView(View itemView, Context context) {
             final ContactListItemView view = (ContactListItemView)itemView;
             //            final AccountItemInfo cache = (AccountItemInfo) view.getTag(); 

             //            view.setSectionHeader(getString(R.string.yiliao_account_myself)); 
             //            
             //            if (cache.photoView == null) {
             //                cache.photoView = view.getPhotoView();
             //            }
             //            cache.photoView.setImageResource(R.drawable.ic_contact_list_picture);
             //            
             //            if (cache.nameTextView == null) {
             //                cache.nameTextView = view.getNameTextView();
             //            }
             //            cache.nameTextView.setText(mAccountInfo.mName);
             //            
             //            view.setStatusBtnClickListener(ContactsListActivity.this); 
             //            if (cache.statusView == null) {
             //                cache.statusView = view.getStatusView(android.R.id.button2);
             //            }
             //
             //            view.setYiLiaoOnLineState(ContactListItemView.STATE_YILIAO_ONLINE);
             //            view.setYiLiaoState(ContactListItemView.STATE_YILIAO_ONLINE);

             view.setSectionHeader(getString(R.string.yiliao_account_myself)); 
             if (mAccountInfo.mPhoto != null)
                 view.getPhotoView().setBackgroundDrawable(mAccountInfo.mPhoto);
             else
                 view.getPhotoView().setBackgroundResource(R.drawable.ic_contact_list_picture);
             view.getPhotoView().setImageResource(R.drawable.pim_imessage_flag);
             if (mAccountInfo.mName != null)
                 view.getNameTextView().setText(mAccountInfo.mName);
             else
                 view.getNameTextView().setText(R.string.yiliao_account_myself);
             //          view.setStatusBtnClickListener(ContactsListActivity.this); 

             //          view.getStatusView(android.R.id.button2);
             //          view.setYiLiaoOnLineState(mbYiliaoOnline ? ContactListItemView.STATE_YILIAO_ONLINE : ContactListItemView.STATE_YILIAO_OFFLINE);
             //          view.setYiLiaoState(mbYiliaoOnline ? ContactListItemView.STATE_YILIAO_ONLINE : ContactListItemView.STATE_YILIAO_OFFLINE);

         }

         //bind view for contact list item which will be multiple deleted
         public void bindMultipleCheckView(View itemView, Context context, Cursor cursor, int position) {
             final CheckedItemInfo view = (CheckedItemInfo)itemView.getTag();            
             view.checkbox.setTag(Integer.valueOf(position));
             view.contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);

             //set the contact photo
             long photoId = 0;
             if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
                 photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
             }
             mPhotoLoader.loadPhoto(view.photoView, photoId);

             //set the contact name
             int nameColumnIndex = getSummaryDisplayNameColumnIndex();
             cursor.copyStringToBuffer(nameColumnIndex, view.nameBuffer);            
             int size = view.nameBuffer.sizeCopied;
             if (size != 0) {
                 view.nameTextView.setText(view.nameBuffer.data, 0, size);
             } else {
                 view.nameTextView.setText(mUnknownNameText);
             }

             if (arrContactIdDel.contains(new Integer((int)view.contactId))) {
                 view.checkbox.setChecked(true);
             }
             else {
                 view.checkbox.setChecked(false);
             }

             view.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     // TODO Auto-generated method stub
                     int id = ((Integer)buttonView.getTag()).intValue();
                     setItemState(id, isChecked);
                 }
             });
         }

         //bind view for contact list item view with notification 
         public void bindNotificationView(View itemView, Context context, Cursor cursor) {
             final ItemInfoWithNotification view = (ItemInfoWithNotification)itemView.getTag();
             final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
             int unReadSMSCnt = getUnreadSMSByContactId(contactId);
             int unReadCallLogCnt = getUnreadCallLogByContactId(contactId);

             view.contactId = contactId;
             view.photoView = (ImageView)itemView.findViewById(R.id.contact_item_photo);
             view.countTextView = (TextView)itemView.findViewById(R.id.contact_item_notification_count);
             view.nameTextView = (TextView)itemView.findViewById(R.id.contact_name);
             view.infoTextView = (TextView)itemView.findViewById(R.id.contact_notification_info);
             view.quickContact = (QuickContactBadge)itemView.findViewById(R.id.contact_item_quickcontact);
             view.dividerImageView = (View)itemView.findViewById(R.id.contact_list_divider);

             int pos = cursor.getPosition();            
             if (pos == mNotificationCursorCount-1) {
                 view.dividerImageView.setVisibility(View.GONE);
             }
             //            //set sticker photo
             //            long photoId = 0;
             //            if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
             //                photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
             //            }
             //            mPhotoLoader.loadPhoto(view.photoView, photoId);
             // Set the photo, if requested
             if (mDisplayPhotos) {
                 boolean useQuickContact = (mMode & MODE_MASK_DISABLE_QUIKCCONTACT) == 0;

                 long photoId = 0;
                 if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
                     photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
                 }

                 ImageView viewToUse;
                 if (useQuickContact) {
                     // Build soft lookup reference

                     final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
                     view.quickContact.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
                     view.quickContact.setSelectedContactsAppTabIndex(StickyTabs.getTab(getIntent()));
                     view.quickContact.setExcludeMimes(new String[] {Contacts.CONTENT_ITEM_TYPE });
                     view.photoView.setVisibility(View.GONE);
                     view.quickContact.setVisibility(View.VISIBLE);
                     viewToUse = view.quickContact;
                 } else {
                     viewToUse = view.photoView;
                     view.photoView.setVisibility(View.VISIBLE);
                     view.quickContact.setVisibility(View.GONE);
                 }
                 mPhotoLoader.loadPhoto(viewToUse, photoId);
             }
             else {
                 if (view.photoView != null)
                     view.photoView.setVisibility(View.GONE);
                 if (view.quickContact != null)
                     view.quickContact.setVisibility(View.GONE);
             }
             //set notification count
             view.countTextView.setText(String.valueOf(unReadSMSCnt+unReadCallLogCnt));

             //set the contact name
             int nameColumnIndex = getSummaryDisplayNameColumnIndex();
             cursor.copyStringToBuffer(nameColumnIndex, view.nameBuffer);            
             int size = view.nameBuffer.sizeCopied;
             if (size != 0) {
                 view.nameTextView.setText(view.nameBuffer.data, 0, size);
             } else {
                 view.nameTextView.setText(mUnknownNameText);
             }

             //set contact notification info
             String strNotifInfo = "";
             if (unReadSMSCnt != 0)
                 strNotifInfo += unReadSMSCnt + mContext.getResources().getString(R.string.unread_message);
             if (unReadSMSCnt != 0 && unReadCallLogCnt != 0)
                 strNotifInfo += ", ";
             if (unReadCallLogCnt != 0)
                 strNotifInfo += unReadCallLogCnt + mContext.getResources().getString(R.string.miss_call);
             view.infoTextView.setText(strNotifInfo);
         }

         private View getTotalContactCountView(ViewGroup parent) {
             final LayoutInflater inflater = getLayoutInflater();
             View view = inflater.inflate(R.layout.total_contacts, parent, false);

             TextView totalContacts = (TextView) view.findViewById(R.id.totalContactsText);

             String text;
             int count = getRealCount();

             if (mSearchMode && !TextUtils.isEmpty(getTextFilter())) {
                 text = getQuantityText(count, R.string.listFoundAllContactsZero,
                         R.plurals.searchFoundContacts);
             } else {
                 if (mDisplayOnlyPhones) {
                     text = getQuantityText(count, R.string.listTotalPhoneContactsZero,
                             R.plurals.listTotalPhoneContacts);
                 } else {
                     text = getQuantityText(count, R.string.listTotalAllContactsZero,
                             R.plurals.listTotalAllContacts);
                 }
             }
             totalContacts.setText(text);
             view.findViewById(R.id.totalContactsLayout).setBackgroundResource(R.layout.app_list_corner_round);
             return view;
         }

         private boolean isShowAllContactsItemPosition(int position) {
             return mMode == MODE_JOIN_CONTACT && mJoinModeShowAllContacts
             && mSuggestionsCursorCount != 0 && position == mSuggestionsCursorCount + 2;
         }

         private boolean isSearchAllContactsItemPosition(int position) {
             return false;
             //            return mSearchMode && position == getCount() - 1;
         }

         private int getSeparatorId(int position) {
             int separatorId = 0;
             if (position == mFrequentSeparatorPos) {
                 separatorId = R.string.favoritesFrquentSeparator;
             }
             if (mSuggestionsCursorCount != 0) {
                 if (position == 0) {
                     separatorId = R.string.separatorJoinAggregateSuggestions;
                 } else if (position == mSuggestionsCursorCount + 1) {
                     separatorId = R.string.separatorJoinAggregateAll;
                 }
             }
             return separatorId;
         }

         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             final ContactListItemView view = new ContactListItemView(context, null);
             view.setOnCallButtonClickListener(ContactsListActivity.this);
             ContactListItemCache cache = new ContactListItemCache();
             view.setTag(cache);
             return view;
         }

         public void bindYiliaoView(View itemView, Context context, Cursor cursor) {
             final ContactListItemView view = (ContactListItemView)itemView;
             final ContactListItemCache cache = (ContactListItemCache) view.getTag();  
             int pos = cursor.getPosition();

             if (0 == pos) {
                 view.setHeaderIsBig(false);
             }
             else {
                 view.setHeaderIsBig(true);
             }


             int nameColumnIndex = 1;//cursor.getColumnIndex(Roster.DISPLAY_NAME);
             boolean highlightingEnabled = false;


             // Set the name
             cursor.copyStringToBuffer(nameColumnIndex, cache.nameBuffer);
             TextView nameView = view.getNameTextView();
             int size = cache.nameBuffer.sizeCopied;
             if (size != 0) {
                 if (highlightingEnabled) {
                     if (cache.textWithHighlighting == null) {
                         cache.textWithHighlighting =
                             mHighlightingAnimation.createTextWithHighlighting();
                     }
                     buildDisplayNameWithHighlighting(nameView, cursor, cache.nameBuffer,
                             cache.highlightedTextBuffer, cache.textWithHighlighting);
                 } else {
                     nameView.setText(cache.nameBuffer.data, 0, size);
                 }
             } else {
                 nameView.setText(mUnknownNameText);
             }



             //Wysie: Contacts or Favourites mode, check preferences
             if (mContacts || mFavs) {
                 if ((mContacts && showContactsPic) || (mFavs && showFavsPic)) {
                     mDisplayPhotos = true;
                 }
                 else {
                     mDisplayPhotos = false;
                 }
             }

             view.hideCallButton();
             // Set the photo, if requested
             if (mDisplayPhotos) {
                 ImageView viewToUse = view.getPhotoView();
                 //                mPhotoLoader.loadPhoto(viewToUse, photoId);
                 byte[] bytes = cursor.getBlob(cursor.getColumnIndex(Roster.PHOTO));
                 if (bytes != null && bytes.length > 0) {
                     viewToUse.setImageBitmap(ImageUtil.byteToBitmap(bytes));
                 } else {
                     viewToUse.setImageResource(R.drawable.ic_contact_list_picture);
                 }
             }
             else {
                 if (cache.photoView != null)
                     cache.photoView.setVisibility(View.GONE);
                 if (cache.nonQuickContactPhotoView != null)
                     cache.nonQuickContactPhotoView.setVisibility(View.GONE);
             }

             //TODO:get really status
             long rosterId = cursor.getLong(0);
             if (arrStatusRosterId.contains(rosterId))
                 view.setYiLiaoState(ContactListItemView.STATE_YILIAO_ONLINE);
             else 
                 view.setYiLiaoState(ContactListItemView.STATE_YILIAO_OFFLINE);
         }
         @Override
         public void bindView(View itemView, Context context, Cursor cursor) {
             final ContactListItemView view = (ContactListItemView)itemView;
             final ContactListItemCache cache = (ContactListItemCache) view.getTag();  
             int pos = cursor.getPosition();

             //            if (0 == pos) {
             //                view.setHeaderIsBig(false);
             //            }
             //            else {
             //                view.setHeaderIsBig(true);
             //            }
             view.setHeaderIsBig(true);

             int typeColumnIndex;
             int dataColumnIndex;
             int labelColumnIndex;
             int defaultType;
             int nameColumnIndex;
             int phoneticNameColumnIndex;
             boolean displayAdditionalData = mDisplayAdditionalData;
             boolean highlightingEnabled = false;
             switch(mMode) {
             case MODE_PICK_PHONE:
             case MODE_LEGACY_PICK_PHONE:
             case MODE_QUERY_PICK_PHONE: {
                 nameColumnIndex = PHONE_DISPLAY_NAME_COLUMN_INDEX;
                 phoneticNameColumnIndex = -1;
                 dataColumnIndex = PHONE_NUMBER_COLUMN_INDEX;
                 typeColumnIndex = PHONE_TYPE_COLUMN_INDEX;
                 labelColumnIndex = PHONE_LABEL_COLUMN_INDEX;
                 defaultType = Phone.TYPE_HOME;
                 break;
             }
             case MODE_PICK_POSTAL:
             case MODE_LEGACY_PICK_POSTAL: {
                 nameColumnIndex = POSTAL_DISPLAY_NAME_COLUMN_INDEX;
                 phoneticNameColumnIndex = -1;
                 dataColumnIndex = POSTAL_ADDRESS_COLUMN_INDEX;
                 typeColumnIndex = POSTAL_TYPE_COLUMN_INDEX;
                 labelColumnIndex = POSTAL_LABEL_COLUMN_INDEX;
                 defaultType = StructuredPostal.TYPE_HOME;
                 break;
             }
             default: {
                 nameColumnIndex = getSummaryDisplayNameColumnIndex();
                 if (mMode == MODE_LEGACY_PICK_PERSON
                         || mMode == MODE_LEGACY_PICK_OR_CREATE_PERSON) {
                     phoneticNameColumnIndex = -1;
                 } else {
                     phoneticNameColumnIndex = SUMMARY_PHONETIC_NAME_COLUMN_INDEX;
                 }
                 dataColumnIndex = -1;
                 typeColumnIndex = -1;
                 labelColumnIndex = -1;
                 defaultType = Phone.TYPE_HOME;
                 displayAdditionalData = false;
                 highlightingEnabled = mHighlightWhenScrolling && mMode != MODE_STREQUENT;
             }
             }

             // Set the name
             cursor.copyStringToBuffer(nameColumnIndex, cache.nameBuffer);
             TextView nameView = view.getNameTextView();
             int size = cache.nameBuffer.sizeCopied;
             if (size != 0) {
                 if (highlightingEnabled) {
                     if (cache.textWithHighlighting == null) {
                         cache.textWithHighlighting =
                             mHighlightingAnimation.createTextWithHighlighting();
                     }
                     buildDisplayNameWithHighlighting(nameView, cursor, cache.nameBuffer,
                             cache.highlightedTextBuffer, cache.textWithHighlighting);
                 } else {
                     nameView.setText(cache.nameBuffer.data, 0, size);
                 }
             } else {
                 nameView.setText(mUnknownNameText);
             }

             boolean hasPhone = cursor.getColumnCount() > SUMMARY_HAS_PHONE_COLUMN_INDEX
             && cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;


             //Wysie: Contacts or Favourites mode, check preferences
             if (mContacts || mFavs) {
                 if ((mContacts && showContactsDialButton) || (mFavs && showFavsDialButton)) {
                     mDisplayCallButton = true;
                 }
                 else {
                     mDisplayCallButton = false;
                 }
                 if ((mContacts && showContactsPic) || (mFavs && showFavsPic)) {
                     mDisplayPhotos = true;
                 }
                 else {
                     mDisplayPhotos = false;
                 }
             }
             // Make the call button visible if requested.
             if (mDisplayCallButton && hasPhone) {                
                 view.showCallButton(android.R.id.button1, pos);
             } else {
                 view.hideCallButton();
             }
             view.hideCallButton();
             // Set the photo, if requested
             if (mDisplayPhotos) {
                 boolean useQuickContact = (mMode & MODE_MASK_DISABLE_QUIKCCONTACT) == 0;
                 final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);

                 long photoId = 0;
                 if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
                     photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
                 }

                 ImageView viewToUse;
                 if (false/*useQuickContact*/) {
                     // Build soft lookup reference

                     final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
                     QuickContactBadge quickContact = view.getQuickContact();
                     quickContact.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
                     quickContact.setSelectedContactsAppTabIndex(StickyTabs.getTab(getIntent()));
                     viewToUse = quickContact;
                 } else {
                     viewToUse = view.getPhotoView();
                 }               
                 mPhotoLoader.loadPhoto(viewToUse, photoId);

                 //begin for yiliao, add by jxli
                 viewToUse.setImageDrawable(null);
                 if (mbLWMsgOnoff == false
                         || cursor.getInt(cursor.getColumnIndex(Contacts.CONTACT_TYPE)) == 0) {
                     view.setYiLiaoState(ContactListItemView.STATE_YILIAO_DISABLE); 

                     if (mbLWMsgOnoff == false) {
                         view.setYiLiaoOnLineState(ContactListItemView.STATE_YILIAO_NULL);						
                     }else {
                         view.setYiLiaoOnLineState(ContactListItemView.STATE_YILIAO_DISABLE);
                     }
                 } else {
                     if (mStatusRosterIdList.contains(contactId)){
                         view.setYiLiaoOnLineState(ContactListItemView.STATE_YILIAO_ONLINE);                    	
                     }
                     else {
                         view.setYiLiaoOnLineState(ContactListItemView.STATE_YILIAO_OFFLINE);                      	
                     }
                     //                    viewToUse.setImageResource(R.drawable.pim_imessage_flag);                   
                 }
                 //end

             }
             else {
                 if (cache.photoView != null)
                     cache.photoView.setVisibility(View.GONE);
                 if (cache.nonQuickContactPhotoView != null)
                     cache.nonQuickContactPhotoView.setVisibility(View.GONE);
             }

             if ((mMode & MODE_MASK_NO_PRESENCE) == 0) {
                 // Set the proper icon (star or presence or nothing)
                 int serverStatus;
                 if (!cursor.isNull(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX)) {
                     serverStatus = cursor.getInt(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX);
                     Drawable icon = ContactPresenceIconUtil.getPresenceIcon(mContext, serverStatus);
                     if (icon != null) {
                         view.setPresence(icon);
                     } else {
                         view.setPresence(null);
                     }
                 } else {
                     view.setPresence(null);
                 }
             } else {
                 view.setPresence(null);
             }

             if (mShowSearchSnippets) {
                 boolean showSnippet = false;
                 String snippetMimeType = cursor.getString(SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX);
                 if (Email.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                     String email = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                     if (!TextUtils.isEmpty(email)) {
                         view.setSnippet(email);
                         showSnippet = true;
                     }
                 } else if (Organization.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                     String company = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                     String title = cursor.getString(SUMMARY_SNIPPET_DATA4_COLUMN_INDEX);
                     if (!TextUtils.isEmpty(company)) {
                         if (!TextUtils.isEmpty(title)) {
                             view.setSnippet(company + " / " + title);
                         } else {
                             view.setSnippet(company);
                         }
                         showSnippet = true;
                     } else if (!TextUtils.isEmpty(title)) {
                         view.setSnippet(title);
                         showSnippet = true;
                     }
                 } else if (Nickname.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                     String nickname = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                     if (!TextUtils.isEmpty(nickname)) {
                         view.setSnippet(nickname);
                         showSnippet = true;
                     }
                 }

                 if (!showSnippet) {
                     view.setSnippet(null);
                 }
             }

             if (!displayAdditionalData) {
                 if (phoneticNameColumnIndex != -1) {

                     // Set the name
                     cursor.copyStringToBuffer(phoneticNameColumnIndex, cache.phoneticNameBuffer);
                     int phoneticNameSize = cache.phoneticNameBuffer.sizeCopied;
                     if (phoneticNameSize != 0) {
                         view.setLabel(cache.phoneticNameBuffer.data, phoneticNameSize);
                     } else {
                         view.setLabel(null);
                     }
                 } else {
                     view.setLabel(null);
                 }
                 return;
             }

             // Set the data.
             cursor.copyStringToBuffer(dataColumnIndex, cache.dataBuffer);

             size = cache.dataBuffer.sizeCopied;
             view.setData(cache.dataBuffer.data, size);

             // Set the label.
             if (!cursor.isNull(typeColumnIndex)) {
                 final int type = cursor.getInt(typeColumnIndex);
                 final String label = cursor.getString(labelColumnIndex);

                 if (mMode == MODE_LEGACY_PICK_POSTAL || mMode == MODE_PICK_POSTAL) {
                     // TODO cache
                     view.setLabel(StructuredPostal.getTypeLabel(context.getResources(), type,
                             label));
                 } else {
                     // TODO cache
                     view.setLabel(Phone.getTypeLabel(context.getResources(), type, label));
                 }
             } else {
                 view.setLabel(null);
             }
         }

         /**
          * Computes the span of the display name that has highlighted parts and configures
          * the display name text view accordingly.
          */
         private void buildDisplayNameWithHighlighting(TextView textView, Cursor cursor,
                 CharArrayBuffer buffer1, CharArrayBuffer buffer2,
                 TextWithHighlighting textWithHighlighting) {
             int oppositeDisplayOrderColumnIndex;
             if (mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                 oppositeDisplayOrderColumnIndex = SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX;
             } else {
                 oppositeDisplayOrderColumnIndex = SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX;
             }
             cursor.copyStringToBuffer(oppositeDisplayOrderColumnIndex, buffer2);

             textWithHighlighting.setText(buffer1, buffer2);
             textView.setText(textWithHighlighting);
         }

         private void bindSectionHeader(View itemView, int position, boolean displaySectionHeaders) {
             final ContactListItemView view = (ContactListItemView)itemView;
             final ContactListItemCache cache = (ContactListItemCache) view.getTag(); 

             //Log.i("jxli", "position = "+position + ", cursor_count = "+cursorCount);

             if (!displaySectionHeaders) {
                 view.setSectionHeader(null);
                 view.setDividerVisible(true);
             } else {
                 final int section = getSectionForPosition(position);
                 final int sectionPosCur = getPositionForSection(section);
                 final int sectionPosNext = getPositionForSection(section + 1);
                 if (sectionPosCur == position) {
                     String title = (String)mIndexer.getSections()[section];
                     view.setSectionHeader(title);    				
                 } else {
                     view.setDividerVisible(false);
                     view.setSectionHeader(null);
                 }

                 // move the divider for the last item in a section
                 if (sectionPosNext - 1 == position) {
                     view.setDividerVisible(false);                    
                 } else {
                     view.setDividerVisible(true);
                 }

             }
         }

        @Override
        public void changeCursor(Cursor cursor) {
            if(mSearchMode && cursor != null && mLewaSearchBar != null) {
                int count = cursor.getCount();
                if(count >0 ) {
                    String str = "";
                    if (count == 1) {
                        str = getString(R.string.search_content_count, count);
                    }else {
                        str = getString(R.string.search_content_counts, count);
                    }
                    mLewaSearchBar.setSearchResult(str);
                } else {
                    mLewaSearchBar.setSearchResult(getString(R.string.search_bar_hint));
                }
            }

             if (cursor != null) {
                 setLoading(false);
             }

             // Get the split between starred and frequent items, if the mode is strequent
             mFrequentSeparatorPos = ListView.INVALID_POSITION;            
             if (cursor != null && (cursorCount = cursor.getCount()) > 0
                     && mMode == MODE_STREQUENT) {
                 //Modified by GanFeng 20120109, fix bug3025
                 cursor.moveToPosition(-1); //cursor.move(-1);
                 for (int i = 0; cursor.moveToNext(); i++) {
                     int starred = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
                     if (starred == 0) {
                         //Modified by GanFeng 20120110, fix bug1375
                         //if (i > 0) {
                         // Only add the separator when there are starred items present
                         mFrequentSeparatorPos = i;
                         //}
                         break;
                     }
                 }
             }

             if (cursor != null && mSearchResultsMode) {
                 TextView foundContactsText = (TextView)findViewById(R.id.search_results_found);
                 String text = getQuantityText(cursor.getCount(), R.string.listFoundAllContactsZero,
                         R.plurals.listFoundAllContacts);
                 foundContactsText.setText(text);
             }

             super.changeCursor(cursor);
             // Update the indexer for the fast scroll widget
             updateIndexer(cursor);
         }

         private void updateIndexer(Cursor cursor) {
             if (cursor == null) {
                 mIndexer = null;
                 return;
             }

             if (mIndexer == null) {
                 mIndexer = getNewIndexer(cursor);
             } else {
                 /*
                if (Locale.getDefault().equals(Locale.JAPAN)) {
                    if (mIndexer instanceof JapaneseContactListIndexer) {
                        ((JapaneseContactListIndexer)mIndexer).setCursor(cursor);
                    } else {
                        mIndexer = getNewIndexer(cursor);
                    }
                } else {
                // This code leads to acore crash when searching a contact and clearing the query quickly.
                // Things seem to work without it, so I'm reverting to the original method for froyo
                    if (mIndexer instanceof AlphabetIndexer) {
                        ((AlphabetIndexer)mIndexer).setCursor(cursor);
                    } else {
                        mIndexer = getNewIndexer(cursor);
                    }
                }
                  */
             }
             Bundle bundle = cursor.getExtras();
             if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
                 String sections[] =
                     bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
                 int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
                 mIndexer = new ContactsSectionIndexer(sections, counts);
             } else {
                 mIndexer = null;
             }

         }

         /**
          * Run the query on a helper thread. Beware that this code does not run
          * on the main UI thread!
          */
         @Override
         public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
             return doFilter(constraint.toString());
         }

         public Object [] getSections() {
             if (mIndexer == null) {
                 return new String[] { " " };
             } else {
                 return mIndexer.getSections();
             }
         }

         public int getPositionForSection(int sectionIndex) {
             if (mIndexer == null) {
                 return -1;
             }

             return mIndexer.getPositionForSection(sectionIndex);
         }

         public int getSectionForPosition(int position) {
             if (mIndexer == null) {
                 return -1;
             }

             return mIndexer.getSectionForPosition(position);
         }

         @Override
         public boolean areAllItemsEnabled() {
             return mMode != MODE_STARRED
             && !mShowNumberOfContacts
             && mSuggestionsCursorCount == 0;
         }

         @Override
         public boolean isEnabled(int position) {
             if (mShowNumberOfContacts) {
                 if (position == 0) {
                     return false;
                 }
                 position--;
             }

             if (mSuggestionsCursorCount > 0) {
                 return position != 0 && position != mSuggestionsCursorCount + 1;
             }
             return position != mFrequentSeparatorPos;
         }

         //        @Override
         //        public int getCount() {
         //          if (!mDataValid) {
         //              return 0;
         //          }
         //          int superCount = super.getCount();
         //          superCount += mNotificationCursorCount;
         //          
         //          return superCount;          
         //        }

         @Override
         public int getViewTypeCount() {
             // TODO Auto-generated method stub
             return ITEM_TYPE_MAX_COUNT;
         }

        public int getCount() {
            if (!mDataValid) {
				startQuery();// add by shenqi for the db may be closed
                return 0;
            }
            int superCount = super.getCount()+mNotificationCursorCount;
            //            if ((mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && mSearchMode == false && mbLWMsgOnoff) {
            //                superCount += 1;
            //            }
            if (mShowNumberOfContacts && (mSearchMode || superCount > 0)) {
                // We don't want to count this header if it's the only thing visible, so that
                // the empty text will display.
                superCount++;
            }

             if (mSearchMode) {
                 // Last element in the list is the "Find
                 //                superCount++;
             }

             // We do not show the "Create New" button in Search mode
             if ((mMode & MODE_MASK_CREATE_NEW) != 0 && !mSearchMode) {
                 // Count the "Create new contact" line
                 superCount++;
             }

            if (mSuggestionsCursorCount != 0) {
                // When showing suggestions, we have 2 additional list items: the "Suggestions"
                // and "All contacts" headers.
                return mSuggestionsCursorCount + superCount + 2;
            }
            else if (mFrequentSeparatorPos != ListView.INVALID_POSITION) {
                // When showing strequent list, we have an additional list item - the separator.
                return superCount + 1;
            } else {
                return superCount;
            }
        }

         /**
          * Gets the actual count of contacts and excludes all the headers.
          */
         public int getRealCount() {
             int countExtra = mNotificationCursorCount;
             //            if ((mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && mSearchMode == false && mbLWMsgOnoff) {
             //                countExtra += 1;
             //            }
             return super.getCount()+countExtra;
         }

         public int getRealQuickPosition(int pos) {
             int realPos = pos + mNotificationCursorCount;
             //            if ((mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && mSearchMode == false && mbLWMsgOnoff) {
             //                realPos += 1;
             //            }
             if (MODE_MASK_CREATE_NEW == (mMode & MODE_MASK_CREATE_NEW)) {
                 ++realPos;
             }

             if ((ListView.INVALID_POSITION != mFrequentSeparatorPos)
                     && (pos >= mFrequentSeparatorPos)) {
                 ++realPos;
             }

             return realPos;

             //if ((MODE_MASK_CREATE_NEW == (mMode & MODE_MASK_CREATE_NEW)) && !mSearchMode) {
             //    return ((pos + 1) + mNotificationCursorCount);
             //}
             //else {
             //    return pos+mNotificationCursorCount;
             //}
         }

         //        private int getRealPosition(int pos) {
         //            if (mShowNumberOfContacts)
         //                pos--;
         //            if (pos >= mNotificationCursorCount)
         //                pos -= mNotificationCursorCount;
         //            return pos;
         //        }
         private int getRealPosition(int pos) {
             //            if ((mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode) && mSearchMode == false && mbLWMsgOnoff) {
             //                pos -= 1;
             //            }
             if (mShowNumberOfContacts) {
                 pos--;
             }

             if (pos >= mNotificationCursorCount)
                 pos -= mNotificationCursorCount;

             if ((mMode & MODE_MASK_CREATE_NEW) != 0 && !mSearchMode) {
                 return pos - 1;
             } else if (mSuggestionsCursorCount != 0) {
                 // When showing suggestions, we have 2 additional list items: the "Suggestions"
                 // and "All contacts" separators.
                 if (pos < mSuggestionsCursorCount + 2) {
                     // We are in the upper partition (Suggestions). Adjusting for the "Suggestions"
                     // separator.
                     return pos - 1;
                 } else {
                     // We are in the lower partition (All contacts). Adjusting for the size
                     // of the upper partition plus the two separators.
                     return pos - mSuggestionsCursorCount - 2;
                 }
             } else if (mFrequentSeparatorPos == ListView.INVALID_POSITION) {
                 // No separator, identity map
                 return pos;
             } else if (pos <= mFrequentSeparatorPos) {
                 // Before or at the separator, identity map
                 return pos;
             } else {
                 // After the separator, remove 1 from the pos to get the real underlying pos
                 return pos - 1;
             }
         }

         @Override
         public Object getItem(int pos) {
             if (mSuggestionsCursorCount != 0 && pos <= mSuggestionsCursorCount) {
                 mSuggestionsCursor.moveToPosition(getRealPosition(pos));
                 return mSuggestionsCursor;
                 //            } else if (pos == 0 && mSearchMode == false && mbLWMsgOnoff && (mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode)) {
                 //                return null;
             } else if (isSearchAllContactsItemPosition(pos)){
                 return null;
             } else {
                 if (pos < mNotificationCursorCount && mNotificationCursorCount != 0 && mNotificationCursor != null) {
                     mNotificationCursor.moveToPosition(pos);
                     return mNotificationCursor;
                 }
                 int realPosition = getRealPosition(pos);
                 if (realPosition < 0) {
                     return null;
                 }
                 return super.getItem(realPosition);
             }
         }

         @Override
         public long getItemId(int pos) {
             if (mSuggestionsCursorCount != 0 && pos < mSuggestionsCursorCount + 2) {
                 if (mSuggestionsCursor.moveToPosition(pos - 1)) {
                     return mSuggestionsCursor.getLong(mRowIDColumn);
                 } else {
                     return 0;
                 }
             } else if (isSearchAllContactsItemPosition(pos)) {
                 return 0;
                 //            } else if (pos == 0 && mSearchMode == false && mbLWMsgOnoff && (mMode == MODE_DEFAULT_NO_CREATE_NEW || MODE_YILIAO == mMode)) {
                 //                return 0;
             }
             if (pos < mNotificationCursorCount) {
                 if (mDataValid && mNotificationCursor != null) {
                     if (mNotificationCursor.moveToPosition(pos))
                         return mNotificationCursor.getLong(mNotificationCursor.getColumnIndex(Contacts._ID));
                 }
                 return 0;
             }
             int realPosition = getRealPosition(pos);
             if (realPosition < 0) {
                 return 0;
             }
             return super.getItemId(realPosition);
         }

         public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                 int totalItemCount) {
             if (view instanceof PinnedHeaderListView) {
                 ((PinnedHeaderListView)view).configureHeaderView(firstVisibleItem);
             }

             ContactsListActivity.this.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
         }

         public void onScrollStateChanged(AbsListView view, int scrollState) {
             if (mHighlightWhenScrolling) {
                 if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                     mHighlightingAnimation.startHighlighting();
                 } else {
                     mHighlightingAnimation.stopHighlighting();
                 }
             }

             if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                 mPhotoLoader.pause();
             } else if (mDisplayPhotos) {
                 mPhotoLoader.resume();
             }
         }

         /**
          * Computes the state of the pinned header.  It can be invisible, fully
          * visible or partially pushed up out of the view.
          */
         public int getPinnedHeaderState(int position) {
             if (mIndexer == null || mCursor == null || mCursor.getCount() == 0) {
                 return PINNED_HEADER_GONE;
             }

             int realPosition = getRealPosition(position);
             if (realPosition < 0) {
                 return PINNED_HEADER_GONE;
             }

             // The header should get pushed up if the top item shown
             // is the last item in a section for a particular letter.
             int section = getSectionForPosition(realPosition);
             int nextSectionPosition = getPositionForSection(section + 1);
             if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
                 return PINNED_HEADER_PUSHED_UP;
             }

             return PINNED_HEADER_VISIBLE;
         }

         /**
          * Configures the pinned header by setting the appropriate text label
          * and also adjusting color if necessary.  The color needs to be
          * adjusted when the pinned header is being pushed up from the view.
          */
         public void configurePinnedHeader(View header, int position, int alpha) {
             PinnedHeaderCache cache = (PinnedHeaderCache)header.getTag();
             if (cache == null) {
                 cache = new PinnedHeaderCache();
                 cache.titleView = (TextView)header.findViewById(R.id.header_text);
                 cache.textColor = cache.titleView.getTextColors();
                 cache.background = header.getBackground();
                 header.setTag(cache);
             }

             int realPosition = getRealPosition(position);
             int section = getSectionForPosition(realPosition);

             String title = (String)mIndexer.getSections()[section];
             cache.titleView.setText(title);

             if (alpha == 255) {
                 // Opaque: use the default background, and the original text color
                 header.setBackgroundDrawable(cache.background);
                 cache.titleView.setTextColor(cache.textColor);
             } else {
                 // Faded: use a solid color approximation of the background, and
                 // a translucent text color
                 header.setBackgroundColor(Color.rgb(
                         Color.red(mPinnedHeaderBackgroundColor) * alpha / 255,
                         Color.green(mPinnedHeaderBackgroundColor) * alpha / 255,
                         Color.blue(mPinnedHeaderBackgroundColor) * alpha / 255));

                 int textColor = cache.textColor.getDefaultColor();
                 cache.titleView.setTextColor(Color.argb(alpha,
                         Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
             }
         }
     }

     private ContactsPreferences.ChangeListener mPreferencesChangeListener =
         new ContactsPreferences.ChangeListener() {
         @Override
         public void onChange() {
             // When returning from DisplayOptions, onActivityResult ensures that we reload the list,
             // so we do not have to do anything here. However, ContactsPreferences requires a change
             // listener, otherwise it would not reload its settings.
         }
     };

     //Wysie: Method to clear frequently called                 
     private void clearFrequentlyCalled() {
         ContentValues values = new ContentValues();
         values.put(Contacts.TIMES_CONTACTED, "0");
         final String[] PROJECTION = new String[] { Contacts._ID };

         Cursor c = getContentResolver().query(Contacts.CONTENT_URI, PROJECTION, Contacts.TIMES_CONTACTED + " > 0", null, null); 	
         if(c.moveToFirst()) {
             do {
                 getContentResolver().update(ContentUris.withAppendedId(Contacts.CONTENT_URI, c.getLong(0)), values, null, null);
             } while(c.moveToNext());
         }
     }
     private void mergeUnreadContactInfo() {
         ContentResolver resolver = getContentResolver();
         HashMap<String, MmsInfoForContacts> map = ContactsUtils.queryUnreadSmsCount(resolver);
         Iterator iter = map.entrySet().iterator();
         notificationInfos.clear();
         while (iter.hasNext()){
             Map.Entry entry = (Map.Entry) iter.next(); 
             Object key = entry.getKey();
             MmsInfoForContacts info = map.get(key);
             if (info.mUnreadCount != 0) {
                 ContactNotificationInfo notificationInfo = new ContactNotificationInfo();
                 notificationInfo.contact_id = (long) Integer.parseInt(info.mId);
                 notificationInfo.mNumber = new ArrayList<String>();
                 notificationInfo.mNumber.addAll(info.mNumber);
                 notificationInfo.mName = info.mName;
                 notificationInfo.unReadSMSCount = info.mUnreadCount;
                 notificationInfos.add(notificationInfo);
             }
         }

         HashMap<Long, MissedCallLogInfo> mapCallLog = MissedCallLogInfo
         .queryMissedCallLogInfo(this);
         if (mapCallLog != null && mapCallLog.size() != 0) {
             iter = mapCallLog.entrySet().iterator();
             while (iter.hasNext()) {
                 Map.Entry entry = (Map.Entry) iter.next();
                 Object key = entry.getKey();
                 MissedCallLogInfo info = mapCallLog.get(key);
                 if (info.getCount() != 0) {
                     boolean isContain = false;
                     int count = notificationInfos.size();
                     for (int i = 0; i < count; i++) {
                         if (info.getContactId() == notificationInfos.get(i).contact_id) {
                             notificationInfos.get(i).unReadCallLogCount = info
                             .getCount();
                             isContain = true;
                             break;
                         }
                     }
                     if (isContain == false) {
                         ContactNotificationInfo notificationInfo = new ContactNotificationInfo();
                         notificationInfo.contact_id = info.getContactId();
                         notificationInfo.mNumber = new ArrayList<String>();
                         notificationInfo.mNumber.addAll(info.getNumbers());
                         notificationInfo.mName = info.getName();
                         notificationInfo.unReadCallLogCount = info.getCount();
                         notificationInfos.add(notificationInfo);
                         Log.e("jxli", "calllog number = "
                                 + notificationInfo.mNumber.get(0)
                                 + ", count = "
                                 + notificationInfo.unReadCallLogCount);
                     }
                 }
             }
         }

         raw_contact_id_unread = "";
         int count = notificationInfos.size();
         for (int i = 0; i < count; i++) {
             if (i == 0) {
                 raw_contact_id_unread += notificationInfos.get(i).contact_id;
                 continue;
             }    
             raw_contact_id_unread += ",";
             raw_contact_id_unread += notificationInfos.get(i).contact_id;
         }  
         Log.e("jxli", "raw_contact_id_unread = " + raw_contact_id_unread);           
     }

     private int getUnreadSMSByContactId(long contactId) {
         int count = notificationInfos.size();
         for (int i = 0; i < count; i++) {
             if (contactId == notificationInfos.get(i).contact_id) 
                 return notificationInfos.get(i).unReadSMSCount;
         }
         return 0;
     }

     private int getUnreadCallLogByContactId(long contactId) {
         int count = notificationInfos.size();
         for (int i = 0; i < count; i++) {
             if (contactId == notificationInfos.get(i).contact_id) 
                 return notificationInfos.get(i).unReadCallLogCount;
         }
         return 0;
     }
     private ArrayList<String> getUnreadNumberByContactId(long contactId) {
         ContactNotificationInfo info = getNotificationInfo(contactId);
         if (info != null)
             return info.mNumber;
         return null;
     }

     private ContactNotificationInfo getNotificationInfo(long contactId) {
         int count = notificationInfos.size();
         for (int i = 0; i < count; i++) {
             if (contactId == notificationInfos.get(i).contact_id)
                 return notificationInfos.get(i);
         }
         return null;
     }

    private void multipleDeleteContacts() {
        //modify by zenghuaying fix bug #7143
//        ContentResolver resolver = getContentResolver();        
//        int count = arrContactIdDel.size();
//        String strContactId = new String("");
//        
//        for (int i = 0; i < count; i++) {
//            if (i == 0)
//                strContactId += ((Integer)arrContactIdDel.get(i)).toString();
//            else
//                strContactId += "," + ((Integer)arrContactIdDel.get(i)).toString();
//            
//        }
//        resolver.delete(RawContacts.CONTENT_URI, RawContacts.CONTACT_ID + " in (" + strContactId + ")", null);
        
        BulkDeleteContactsTask bdTask = new BulkDeleteContactsTask(mContext);
        bdTask.execute("");
        //finish();
        //modify end
    }
    
    //add by zenghuaying fix bug #7143
    private class BulkDeleteContactsTask extends AsyncTask<Object, Integer, Integer> {
        private Context context;
        private ProgressDialog pdialog;
        public BulkDeleteContactsTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdialog = ProgressDialog.show(context, null, context.getString(R.string.msg_deleting), true, false);
        }        

        @SuppressWarnings("unchecked")
        @Override
        protected Integer doInBackground(Object... params) {
            ContentResolver resolver = getContentResolver();        
            int count = arrContactIdDel.size();
            StringBuffer strContactId = new StringBuffer();
            int groupSize = 300;
            int groupCount = (count/groupSize) + 1;
            int jMax = 0;
            int jStart = 0;
            
            for(int i = 0; i < groupCount; i++ ){
                jStart = i * groupSize;
                
                if(i == groupCount-1){
                    jMax = count;
                }else{
                    jMax = (i+1) * groupSize;
                }
                
                for(int j = jStart; j < jMax;j++){
                    if (j % groupSize ==  0)
                      strContactId.append(((Integer)arrContactIdDel.get(j)).toString());
                  else
                      strContactId.append( "," + ((Integer)arrContactIdDel.get(j)).toString());
                }
                resolver.delete(RawContacts.CONTENT_URI, RawContacts.CONTACT_ID + " in (" + strContactId.toString() + ")", null);
                strContactId = new StringBuffer();
            }
            
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            
            pdialog.dismiss();
            finish();
        }
    }
    //add end

     Runnable RefreshUIRunable = new Runnable() {
         public void run() {
             quryImsState(ROSTER_DATA_LIST_QUERY_TOKEN);
             ((ContactsListActivity)mContext).startQuery();
         }
     };

     @Override
     public void onDataEvent(DataEvent event, int state) {
         // TODO Auto-generated method stub
         //Log.e("jxli", "onDataEvent() event " + event);
         //if ((PimEngine.DataEvent.LOAD_MISSED_LOGS == event)
         //		&& (PimEngine.DataEventListener.LOAD_DATA_DONE == state)) {
         //	Log.e("jxli", "onDataEvent(), updateNotificationListView()");
         //	updateNotificationListView(true);
         //}
         if (PimEngine.DataEvent.CONTACTS_CHANGED == event && mPhotoLoader != null) {
             Log.e(TAG, "CONTACTS_CHANGED ");
             if(!mbStartInsertRoster) {
                 mPhotoLoader.clear();
             }
             //startQuery();
         }else if (PimEngine.DataEvent.ROSTERDATA_CHANGED == event) {
             Log.e(TAG, "ROSTERDATA_CHANGED");
             mHandler.removeCallbacks(RefreshUIRunable);
             mHandler.postDelayed(RefreshUIRunable,2000);	
         }
     }

     @Override
     public void loadContent() {
         Log.d(TAG, "loadContent");
         getIntent().putExtra("delayloadcontent", false);
         setupContentViewHelper();
         mQueryHandler = new QueryHandler(this);
         startQuery();
         mJustCreated = false;
         mSyncEnabled = true;
     }

     @Override
     public int getItemMarginLeft() {
         if ((MODE_DEFAULT_NO_CREATE_NEW == mMode) || (MODE_DEFAULT_WITH_CREATE_NEW == mMode) || (MODE_YILIAO == mMode)) {
             if (Integer.MAX_VALUE == mItemMarginLeft) {
                 mItemMarginLeft = getResources().getDimensionPixelOffset(R.dimen.list_item_divider_margin_left);
             }
             return mItemMarginLeft;
         }
         else {
             return 0;
         }
     }

     @Override
     public int getItemMarginRight() {
         if ((MODE_DEFAULT_NO_CREATE_NEW == mMode) || (MODE_DEFAULT_WITH_CREATE_NEW == mMode) || (MODE_YILIAO == mMode)) {
             if (Integer.MAX_VALUE == mItemMarginRight) {
                 mItemMarginRight = getResources().getDimensionPixelOffset(R.dimen.list_item_divider_margin_right);
             }
             return mItemMarginRight;
         }
         else {
             return 0;
         }
     }

     @Override
     public boolean dispatchTouchEvent(MotionEvent ev) {
         // TODO Auto-generated method stub
         float xf = ev.getRawX();
         float yf = ev.getRawY();
         Rect frame = new Rect();

         if (ev.getAction() == MotionEvent.ACTION_DOWN) {           
             if (indexBar != null && indexBar.isAlpha_menu_Showed()) {
                 // frame.set(indexBar.getLeft(), indexBar.getBottom(),
                 // indexBar.getRight(), displayHeight);
                 Scroller_view scroller_view = indexBar.getScrollerView();
                 scroller_view.getGlobalVisibleRect(frame);
                 // frame.set(scroller_view.getLeft(), scroller_view.getTop(),
                 // scroller_view.getRight(), scroller_view.getBottom());
                 if (!frame.contains((int) xf, (int) yf)) {
                     indexBar.hide(this);
                     return true;
                 }
             }
         } else if (ev.getAction() == MotionEvent.ACTION_MOVE){
             //Added by GanFeng 20120112, fix bug1911
             if (mSearchMode) {
                 if (mLewaSearchBar != null) {
                     mLewaSearchBar.getGlobalVisibleRect(frame);
                     if (!frame.contains((int) xf, (int) yf)) {
                         hideSoftKeyboard();
                     }
                 }
             }

             //if (bSetScrollListner == false
             //        && (mMode == MODE_DEFAULT_NOTIF
             //                || mMode == MODE_DEFAULT
             //                || (MODE_DEFAULT_NO_CREATE_NEW == mMode)
             //                || (MODE_DEFAULT_WITH_CREATE_NEW == mMode))) {
             //    getListView().setOnScrollListener(this);
             //    bSetScrollListner = true;
             //}
         }

         return super.dispatchTouchEvent(ev);
     }
     //end

     //for multiple delete contacts list view
     private void setAllItemState(boolean bSelect){
         ListView listView = getListView();
         int count =getListView().getChildCount();
         for (int i = 0; i < count; i++) {
             View v = listView.getChildAt(i);
             if (null != v) {
                 CheckedItemInfo itemInfo = (CheckedItemInfo)v.getTag();
                 CheckBox checkbox = itemInfo.checkbox;
                 checkbox.setChecked(bSelect);                                                
             }
         }

         arrContactIdDel.clear();   
         count = 0;
         Cursor cursor = mAdapter.getCursor();
         if (bSelect == true && cursor != null && cursor.getCount() != 0) {
             cursor.moveToFirst();
             do {
                 arrContactIdDel.add(new Integer((int)(cursor.getLong(SUMMARY_ID_COLUMN_INDEX))));
                 count++;
             } while (cursor.moveToNext());
         }

         String string = String.format("%s(%d)", getResources().getString(R.string.delete), count);           
         Button deleteButton = (Button)findViewById(R.id.contacts_delete_done);
         if (deleteButton != null)
             deleteButton.setText(string);
     }

     //set item state for multiple delete contacts
     private void setItemState(int position, boolean isSelect) {
         Cursor cursor = mAdapter.getCursor();
         if (!cursor.moveToPosition(position)) {
             throw new IllegalStateException("couldn't move cursor to position " + position);
         }
         final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
         if (isSelect == true) {
             if (!arrContactIdDel.contains(new Integer((int)contactId)))
                 arrContactIdDel.add(new Integer((int)contactId));

         } else {
             arrContactIdDel.remove(new Integer((int)contactId));
         }
         String string = String.format("%s(%d)", getResources().getString(R.string.delete), arrContactIdDel.size());           
         Button deleteButton = (Button)findViewById(R.id.contacts_delete_done);
         if (deleteButton != null)
             deleteButton.setText(string);
     }

     @Override
     public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
         // TODO Auto-generated method stub
         if ((null != mDialogText)
                 //&& ((MODE_DEL_MULTIPLE == mMode) || (MODE_DEFAULT_NO_CREATE_NEW == mMode) || (MODE_DEFAULT_WITH_CREATE_NEW == mMode) || (mMode == MODE_DEFAULT_NOTIF))
                 //&& mSearchMode == false
                 && ((null == indexBar) || !indexBar.isAlpha_menu_Showed())
                 && mReady && marrFirstLetter.size() != 0) {
             char firstLetter = marrFirstLetter.get(firstVisibleItem).charAt(0);
             if (Character.MIN_VALUE != mPrevLetter) {
                 if (!mShowing && firstLetter != mPrevLetter) {

                     mShowing = true;
                     mDialogText.setVisibility(View.VISIBLE);
                 }
                 mDialogText.setText(((Character)firstLetter).toString());
                 mHandler.removeCallbacks(mRemoveWindow);
                 mHandler.postDelayed(mRemoveWindow, 300);
             }
             mPrevLetter = firstLetter;
         }
     }

     @Override
     public void onScrollStateChanged(AbsListView arg0, int arg1) {
         // TODO Auto-generated method stub
     }

     private void removeWindow() {
         if (mShowing) {
             mShowing = false;
             mDialogText.setVisibility(View.INVISIBLE);
         }
     }

     //begin for yiliao, add by jxli
     private boolean matchYiliaoNumber(long contactId) {
         return matchYiliaoNumber(contactId, false);
     }

     private boolean matchYiliaoNumber(long contactId, boolean bShowPrompt) {
         if( System.currentTimeMillis() - mLastRefreshTime > 5000 ) {//avoid IM no response
             mbRefresh = false;
         }

         if (true == mbRefresh || false == ePrefs.getBoolean(Prefs.LABEL_AUTO_MATCH, Prefs.DISPLAY_ONLY_YL_CONTACTS_DEFAULT)) {		
             return false;
         }

         if (bShowPrompt == true && CommonMethod.isWiFiActive(this) == false && CommonMethod.IsConnection(this) == false) {
             Toast.makeText(this, R.string.prompt_nerwork_offline, Toast.LENGTH_SHORT).show();
             return false;
         }

         ArrayList<String> arrNumbers = new ArrayList<String>();
         ArrayList<String> arrRosterNumber = new ArrayList<String>();
         String strWhere = null;
         if (contactId != 0) 
             strWhere = CommonDataKinds.Phone.CONTACT_ID + "=" + contactId;

         Cursor cursor = getContentResolver().query(
                 CommonDataKinds.Phone.CONTENT_URI,
                 new String[] {CommonDataKinds.Phone.NUMBER},
                 strWhere,
                 null,
                 null);
         Cursor rosterCursor = getContentResolver().query(
                 RosterData.CONTENT_URI,
                 new String[] {RosterData.ROSTER_USER_ID},
                 null,
                 null,
                 null);
         try {
             if (cursor != null && cursor.getCount() > 0) {
                 if (rosterCursor != null && rosterCursor.getCount() > 0) {
                     while (rosterCursor.moveToNext()) {
                         String number = CommonMethod.filterPhoneNumber(rosterCursor.getString(0));
                         arrRosterNumber.add(number);
                     }
                 }
                 while (cursor.moveToNext()) {
                     String number = CommonMethod.filterPhoneNumber(cursor.getString(0));
                     if (number.length() == 11 && arrRosterNumber.contains(number) == false && arrNumbers.contains(number) == false)
                         arrNumbers.add(number);
                 }

                 if (arrNumbers != null && arrNumbers.size() > 0) {
                     mbRefresh = true;
                     mLastRefreshTime = System.currentTimeMillis(); 

                     Intent intent= new Intent();                     
                     if (bShowPrompt == true) {
                         intent.setAction(ACTION_YILIAO_MANUALLY_REFRESH);
                     } else if (contactId != 0) {
                         intent.setAction(ACTION_YILIAO_MATCH_NUMBERS_ONE);
                     } else {
                         intent.setAction(ACTION_YILIAO_MATCH_NUMBERS);
                     }
                     PendingIntent mRespondIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                     IMClient.CheckUserRegister(this, IMMessage.GeXinIM, arrNumbers, mRespondIntent);
                     return true;
                 }

             }
         } finally {
             cursor.close();
             cursor = null;
             rosterCursor.close();
             rosterCursor = null;
         }

         return false;
     }

     private void getOnlinePeople(ArrayList<String> arrNumbers) {
         //for IMessage
         if (true) return;

         arrStatusContactId.clear();
         arrStatusRosterId.clear();

         ArrayList<String> arrAllNumbers = arrNumbers;
         if (arrAllNumbers == null) {
             arrAllNumbers = new ArrayList<String>();
             Cursor cursor = getContentResolver().query(RosterData.CONTENT_URI, new String[]{RosterData.ROSTER_USER_ID}, null, null, null);
             try {
                 while (cursor.moveToNext()) {
                     String phone = CommonMethod.filterPhoneNumber(cursor.getString(0));
                     arrAllNumbers.add(phone);
                 }
             } finally {
                 cursor.close();
                 cursor = null;
             }
         }

         if (arrAllNumbers != null && arrAllNumbers.size() > 0) {
             Intent intent= new Intent(ACTION_YILIAO_STATUS_NUMBERS); 
             PendingIntent mStatusIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
             String[] strNumbers = new String[arrAllNumbers.size()];
             arrAllNumbers.toArray(strNumbers);
             IMClient.CheckUserOnlineStatus(this, IMMessage.GeXinIM, strNumbers, mStatusIntent);
         }
     }

     private ResponseReceiver mResponseReceiver = new ResponseReceiver();

     class ResponseReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             Log.d(TAG,"receiver intent = "+ intent.getAction());
             ContentResolver resolver = getContentResolver();
             if (intent.getAction().equals(ACTION_YILIAO_MATCH_NUMBERS)
                     || intent.getAction().equals(ACTION_YILIAO_MATCH_NUMBERS_ONE)
                     || intent.getAction().equals(ACTION_YILIAO_MANUALLY_REFRESH)){
                 int matchResult = (int)intent.getIntExtra("com.lewa.PIM.IM.result", 0); 
                 ArrayList<String> arrNumbers = intent.getExtras().getStringArrayList("register");
				 if(matchResult == IMService.IM_REQUEST_RESULT_OK){
                 if (arrNumbers != null && arrNumbers.size() > 0) {
                     String[] numbers = new String[arrNumbers.size()];
                     arrNumbers.toArray(numbers);
                     for (String number : numbers) {
                         queReisgerUser.offer(number);
                     }
                     ContentValues value = new ContentValues(1);			
                     value.put(RosterData.ROSTER_USER_ID, queReisgerUser.poll());
                     if (intent.getAction().equals(ACTION_YILIAO_MANUALLY_REFRESH)) {
                         mQueryHandler.startInsert(INSERT_TOKEN_MANUAL_REFRESH, null,RosterData.CONTENT_URI, value);
                     }
                     else {
                         mQueryHandler.startInsert(INSERT_TOKEN, null,RosterData.CONTENT_URI, value);
                     }   
                     mbStartInsertRoster = true;
                 }
                 else {
                     if (intent.getAction().equals(ACTION_YILIAO_MANUALLY_REFRESH)) {
                         Toast.makeText(context, R.string.yiliao_progress_match_yiliao_number_done, Toast.LENGTH_SHORT).show();
                     }
                 }
				 }
				 else {
				 	 if (intent.getAction().equals(ACTION_YILIAO_MANUALLY_REFRESH)) {
                         Toast.makeText(context, R.string.yiliao_progress_match_yiliao_number_fail, Toast.LENGTH_SHORT).show();
                     }
				 }
                 mbRefresh = false; 		
             } else if (intent.getAction().equals(ACTION_YILIAO_STATUS_NUMBERS)) {
                 String number = intent.getStringExtra("phone");
                 boolean isOnline = intent.getBooleanExtra("online", false);
                 int isOnlineInt =  isOnline ? 1 : 0;
                 if (isOnline == true) {
                     Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                     Cursor cursor = resolver.query(uri, new String[]{PhoneLookup._ID}, null, null, null);
                     try {
                         while (cursor.moveToNext()) {
                             arrStatusContactId.add(new Long(cursor.getLong(0)));
                         }
                     } finally {
                         cursor.close();
                         cursor = null;
                     }

                     cursor = resolver.query(RosterData.CONTENT_URI, new String[]{RosterData.ROSTER_ID}, RosterData.ROSTER_USER_ID + "=\'" + number + "\'", null, null);
                     try {
                         while (cursor.moveToNext()) {
                             arrStatusRosterId.add(new Long(cursor.getLong(0)));
                         }
                     } finally {
                         cursor.close();
                         cursor = null;
                     }
                 }
                 ContentValues cv = new ContentValues();
                 cv.put(RosterData.STATUS,isOnlineInt);
                 resolver.update(RosterData.CONTENT_URI, cv, RosterData.ROSTER_USER_ID + "=" + number + " AND " + RosterData.STATUS + "!=" + isOnlineInt , null);

                 if (mAdapter != null)
                     mAdapter.notifyDataSetChanged();
             } else if (intent.getAction().equals(ACTION_YILIAO_MATCH_NUMBERS_EDIT)) {
                 long contactId = intent.getLongExtra("contact_id", 0);
                 matchYiliaoNumber(contactId, false);
             } else if (intent.getAction().equals(CommonMethod.ACTION_LEWA_MSG_ON_OFF)) {
                 //for lewa message
                 SharedPreferences.Editor editor = ePrefs.edit();
                 editor.putBoolean(YILIAO_CONTACTS_MATCH_FIRST, true);
                 editor.putBoolean(Prefs.LABEL_AUTO_MATCH, true);
                 editor.commit();

                 mbLWMsgOnoff = CommonMethod.getLWMsgOnoff(context);
                 getListView().removeAllViewsInLayout();
                 startQuery();
             } else if (intent.getAction().equals(ACTION_YILIAO_SHOW_OPTIONS)) {
                 mshowContactsYOption = ePrefs.getInt(YILIAO_CONTACTS_OPTIONS, CONTACTS_SHOW_LOCAL);
                 getListView().removeAllViewsInLayout();
                 startQuery();
             }
         }
     }

     private void showYiliaoConfirmDialog() {
         mYiliaoBuilder = new AlertDialog.Builder(this);
         CheckBox checkBox = new CheckBox(this);
         checkBox.setText(R.string.remind_yiliao_match_number_hint);
         checkBox.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 // TODO Auto-generated method stub
                 boolean bRemind = ePrefs.getBoolean(YILIAO_CONTACTS_MATCH_NOT_REMIND, false);
                 SharedPreferences.Editor editor = ePrefs.edit();
                 editor.putBoolean(YILIAO_CONTACTS_MATCH_NOT_REMIND, !bRemind);
                 editor.commit();
             }
         });
         mYiliaoBuilder.setMessage(R.string.message_yiliao_match_number_hint)
         .setTitle(R.string.title_yiliao_match_number_hint)
         .setIcon(android.R.drawable.ic_dialog_alert)
         .setView(checkBox)
         .setPositiveButton(R.string.positive_yiliao_match_number_hint, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 SharedPreferences.Editor editor = ePrefs.edit();
                 editor.putBoolean(YILIAO_CONTACTS_MATCH_FIRST, true);
                 editor.putBoolean(Prefs.LABEL_AUTO_MATCH, true);
                 editor.commit();

                 ContentResolver resolver = getContentResolver();                                      
                 ContentValues userValues = new ContentValues();
                 userValues.put(User.USER_ID, "13681632371");
                 userValues.put(User.PASSWD, "1111");
                 userValues.put(User.NICK_NAME, mContext.getString(R.string.yiliao_account_myself));
                 userValues.put(User.REMARK, "hello baby");
                 userValues.put(User.ACCOUNT_TYPE, 1);
                 resolver.insert(User.CONTENT_URI, userValues);

                 if (matchYiliaoNumber(0)) {
                     mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(mContext, null,
                             getText(R.string.yiliao_progress_match_yiliao_number)));
                     mProgress.get().setCancelable(true);
                 }
             }
         })
         .setNegativeButton(R.string.negative_yiliao_match_number_hint, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 SharedPreferences.Editor editor = ePrefs.edit();
                 editor.putBoolean(Prefs.LABEL_AUTO_MATCH, false);
                 editor.commit();
             }
         })
         .show();
     }
     //end

     private static final String[] ROSTER_DATA_PROJECT = new String[]{RosterData.DISPLAY_NAME,
         RosterData.ROSTER_USER_ID,
         RosterData.STATUS};

     private void quryImsState(int tag){

         if (tag == ROSTER_DATA_LIST_QUERY_TOKEN) {
             mStatusRosterIdList.clear();    				
         }

         mRosterDataListQueryHandler.startQuery(tag, 
                 null, 
                 RosterData.CONTENT_URI, 
                 ROSTER_DATA_PROJECT, 
                 null, 
                 null, 
                 null);
     }

     private final class RosterDataListQueryHandler extends AsyncQueryHandler {
         public RosterDataListQueryHandler(ContentResolver contentResolver) {
             super(contentResolver);
         }

         @Override
         protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

             Log.e(TAG, " RosterDataListQueryHandler onQueryComplete called with token " + token);

             switch (token) {
             case ROSTER_DATA_LIST_QUERY_TOKEN:{
                 if (cursor == null || cursor.getCount() < 1) {
                     break;
                 }
                 ContentResolver resolver = getContentResolver();
                 Cursor contactCursor = null;

                 while (cursor.moveToNext()) {
                     try {

                         int state = cursor.getInt(2);	                    	
                         if (state == 0) {
                             continue;
                         }

                         String number = MessageUtils.fixPhoneNumber(cursor.getString(1));
                         Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                         contactCursor = resolver.query(uri, new String[]{PhoneLookup._ID}, null, null, null);

                         while (contactCursor.moveToNext()) {
                             mStatusRosterIdList.add(new Long(contactCursor.getLong(0)));										
                         }
                         contactCursor.close();
                     } catch (Exception e) {
                         Log.e(TAG, "" + e.getMessage());
                         if (contactCursor != null) {
                             contactCursor.close();
                         }
                     }
                 }            	
             }
             break;

             case ROSTER_DATA_LIST_QUERY_STATE_TOKEN:{

                 if (cursor == null || cursor.getCount() < 1) {
                     break;
                 }

                 ArrayList<String> numberList = new ArrayList<String>();
                 while (cursor.moveToNext()) {
                     numberList.add(cursor.getString(1));
                 }    		
                 CommonMethod.CheckUserOnlineStatus(mContext, numberList);
             }
             break;

             default:
                 Log.e(TAG, "onQueryComplete called with unknown token " + token);
                 break;
             }
         }
     }
}
