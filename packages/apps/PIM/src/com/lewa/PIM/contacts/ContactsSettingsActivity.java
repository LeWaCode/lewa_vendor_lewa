package com.lewa.PIM.contacts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.lewa.PIM.R;
import com.lewa.PIM.IM.IMClient;
import com.lewa.PIM.IM.IMMessage;
import com.lewa.PIM.contacts.ContactsListActivity.ResponseReceiver;
import com.lewa.PIM.contacts.model.Sources;
import com.lewa.PIM.contacts.util.AccountSelectionUtil;
import com.lewa.PIM.util.CommonMethod;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RosterData;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class ContactsSettingsActivity  extends PreferenceActivity 
            implements OnPreferenceClickListener, OnPreferenceChangeListener{

    private SharedPreferences mePrefs;
    //static final String LABEL_ONLY_PHONES = "display_options_phones_only";
    public static final String LABEL_AUTO_RECOMM = "auto_recommend_mututal_friends";
    static final String LABEL_SWITCH_ACCOUNT = "switch_accounts";
    static final String LABEL_CHANGE_PASSWD = "change_passwd";
    static final String LABEL_FIND_SETTINGS = "whether_find_me_label";
    static final String LABEL_AUTO_MATCH = "match_contacts_number_auto";
    static final String LABEL_IMPORT_FROM_SIM = "import_from_sim";
    static final String LABEL_IMPORT_FROM_SDCARD = "import_from_sdcard";
    static final String LABEL_EXPORT_TO_SDCARD = "export_to_sdcard";
    static final String LABEL_SHARE_VISIBALE_CONTACTS = "share_visible_contacts";
    static final String LABEL_MERGE_DUPLICATE_CONTACTS = "merge_duplicate_contacts";
    static final String LABEL_BULK_DELETE_CONTACTS = "bulk_delete_contacts";
    static final String LABEL_ACCOUNT_SETTINGS = "account_settings";
    static final String YILIAO_CONTACTS_OPTIONS = "contacts_yiliao_show_options";
    
    private static final String ACTION_YILIAO_MATCH_NUMBERS = "com.lewa.PIM.IM.CHECK_USER_REGISTER_RESPONSE";
    private static final String YILIAO_CONTACTS_MATCH_FIRST = "contacts_yiliao_match_first";
    
//    private static final String CLAUSE_ONLY_VISIBLE = "(" + Contacts.IN_VISIBLE_GROUP + "=1 OR " + Contacts.IN_VISIBLE_GROUP + "=0)";; //Contacts.IN_VISIBLE_GROUP + "=1";
//    private static final String CLAUSE_ONLY_PHONES = Contacts.HAS_PHONE_NUMBER + "=1";
//    private static final String CLAUSE_ON_LEWA_MSG = Contacts.CONTACT_TYPE + "=1";
//    
//    private static final int CONTACTS_SHOW_LOCAL = 1;
//    private static final int CONTACTS_SHOW_YILIAO_ONLY = 2;
    
    private int mshowContactsYOption; 
    private boolean mDisplayOnlyPhones;
    private CheckBoxPreference mcprefOnlyPhones;
    private CheckBoxPreference mcprefAutoMatch;
    private Preference mprefSwitchAccount;
    private Preference mprefChangePasswd;
    private Preference mprefImportSim;
    private Preference mprefImportSDcard;
    private Preference mprefExportSDcard;
    private Preference mprefShareContacts;
    private Preference mprefMergeContacts;
    private Preference mprefBulkDelete;
    private Preference mprefAccountSett;
    private ListPreference mlprefPrivacySett;
    private WeakReference<ProgressDialog> mProgress;
 
    private String[] mPrivacySettStr = null;
    
    final String[] sLookupProjection = new String[] {
            Contacts.LOOKUP_KEY
    };
    
    public interface Prefs {
        public static final String LABEL_ONLY_PHONES = "display_options_phones_only";
        public static final boolean DISPLAY_ONLY_PHONES_DEFAULT = false;

        public static final String LABEL_AUTO_MATCH = "match_contacts_number_auto";
        public static final boolean DISPLAY_ONLY_YL_CONTACTS_DEFAULT = true;
    }

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mePrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.contacts_settings);
        
        mDisplayOnlyPhones = mePrefs.getBoolean(Prefs.LABEL_ONLY_PHONES,
                Prefs.DISPLAY_ONLY_PHONES_DEFAULT);
        mshowContactsYOption = mePrefs.getInt(YILIAO_CONTACTS_OPTIONS, ContactsListActivity.CONTACTS_SHOW_LOCAL);
        
        mcprefOnlyPhones = (CheckBoxPreference)findPreference(Prefs.LABEL_ONLY_PHONES);
        mcprefAutoMatch = (CheckBoxPreference)findPreference(Prefs.LABEL_AUTO_MATCH);
        mprefSwitchAccount = findPreference(LABEL_SWITCH_ACCOUNT);
        mprefChangePasswd = findPreference(LABEL_CHANGE_PASSWD);
        mlprefPrivacySett = (ListPreference)findPreference(LABEL_FIND_SETTINGS);
        mprefImportSim = findPreference(LABEL_IMPORT_FROM_SIM);
        mprefImportSDcard = findPreference(LABEL_IMPORT_FROM_SDCARD);
        mprefExportSDcard = findPreference(LABEL_EXPORT_TO_SDCARD);
        mprefShareContacts = findPreference(LABEL_SHARE_VISIBALE_CONTACTS);
        mprefMergeContacts = findPreference(LABEL_MERGE_DUPLICATE_CONTACTS);
        mprefBulkDelete = findPreference(LABEL_BULK_DELETE_CONTACTS);
        mprefAccountSett = findPreference(LABEL_ACCOUNT_SETTINGS);
        
        if (null != mcprefOnlyPhones)
            mcprefOnlyPhones.setOnPreferenceChangeListener(this);
        if (null != mcprefAutoMatch)
            mcprefAutoMatch.setOnPreferenceChangeListener(this);        
        if (null != mprefSwitchAccount)
            mprefSwitchAccount.setOnPreferenceClickListener(this);
        if (null != mprefChangePasswd)
            mprefChangePasswd.setOnPreferenceClickListener(this);
        if (null != mprefImportSim)
            mprefImportSim.setOnPreferenceClickListener(this);
        if (null != mprefImportSDcard)
            mprefImportSDcard.setOnPreferenceClickListener(this);
        if (null != mprefExportSDcard)
            mprefExportSDcard.setOnPreferenceClickListener(this);
        if (null != mprefShareContacts)
            mprefShareContacts.setOnPreferenceClickListener(this);
        if (null != mprefMergeContacts)
            mprefMergeContacts.setOnPreferenceClickListener(this);
        if (null != mprefBulkDelete)
            mprefBulkDelete.setOnPreferenceClickListener(this);
        if (null != mprefAccountSett)
            mprefAccountSett.setOnPreferenceClickListener(this);
        if (null != mlprefPrivacySett) {
            mlprefPrivacySett.setOnPreferenceChangeListener(this);
            int value = Integer.valueOf(mlprefPrivacySett.getValue());
            mPrivacySettStr = getResources().getStringArray(R.array.contacts_privacy_settings_entries);
            if (mPrivacySettStr != null && value >= 0 && value < mPrivacySettStr.length)
                mlprefPrivacySett.setSummary(mPrivacySettStr[value]);
        }
        if (null != mprefImportSim && !TelephonyManager.getDefault().hasIccCard()) {
            mprefImportSim.setEnabled(false);
        }
    }
    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        
        unregisterReceiver(mResponseReceiver);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_YILIAO_MATCH_NUMBERS);
        registerReceiver(mResponseReceiver, filter);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
         //TODO Auto-generated method stub
        if(preference.getKey().equals(LABEL_FIND_SETTINGS)){
            int value = Integer.valueOf((String)newValue);
            if(mPrivacySettStr != null && value >=0 && value < mPrivacySettStr.length)
                preference.setSummary(mPrivacySettStr[value]);
        } else if (preference.getKey().equals(Prefs.LABEL_ONLY_PHONES)) {
            setResult(RESULT_OK);
        } else if (preference.getKey().equals(Prefs.LABEL_AUTO_MATCH) && (Boolean)newValue == true) {
            mDisplayOnlyPhones = (Boolean)newValue;
            
            if (mePrefs.getBoolean(YILIAO_CONTACTS_MATCH_FIRST, false) == false) {
                SharedPreferences.Editor editor = mePrefs.edit();
                editor.putBoolean(YILIAO_CONTACTS_MATCH_FIRST, true);
                editor.commit();
            }
            
            if (matchYiliaoNumber(0)) {
                mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(this, null,
                        getText(R.string.yiliao_progress_match_yiliao_number)));
                mProgress.get().setCancelable(true);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // TODO Auto-generated method stub
        String key = preference.getKey();

        if (key.equals(LABEL_IMPORT_FROM_SIM)) {
            handleImportRequest(R.string.import_from_sim);
        } else if (key.equals(LABEL_IMPORT_FROM_SDCARD)) {
            handleImportRequest(R.string.import_from_sdcard);
        } else if (key.equals(LABEL_EXPORT_TO_SDCARD)) {
            Intent exportIntent = new Intent(this, ExportVCardActivity.class);
            startActivity(exportIntent);
        } else if (key.equals(LABEL_SHARE_VISIBALE_CONTACTS)) {
            doShareVisibleContacts();
        } else if (key.equals(LABEL_MERGE_DUPLICATE_CONTACTS)) {
            ArrayList<ArrayList<String>> similarIds = ContactHelper.querySimiliarIds(this);
            PageManager.setSimilarIds(similarIds);
            if (similarIds.size() == 0) {
                Toast.makeText(this, R.string.no_duplicated_contact, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, MergeActivity.class);
                intent.putStringArrayListExtra("ids", PageManager.getFirstIds());
                startActivity(intent);
            }
        } else if (key.equals(LABEL_BULK_DELETE_CONTACTS)) {
            final Intent intent = new Intent(ContactsListActivity.CONTACTS_MULTIPLE_DEL_ACTION);
            intent.setClass(this, ContactsListActivity.class);
            startActivity(intent);
        } else if (key.equals(LABEL_ACCOUNT_SETTINGS)) {
            final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
            intent.putExtra(ContactsListActivity.AUTHORITIES_FILTER_KEY, new String[] {
                ContactsContract.AUTHORITY
            });
            startActivity(intent);
        }        
        return false;
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
        }
        return super.onCreateDialog(id, bundle);
    }
    
    private ResponseReceiver mResponseReceiver = new ResponseReceiver();
    class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContentResolver resolver = getContentResolver();
            if (intent.getAction().equals(ACTION_YILIAO_MATCH_NUMBERS)){
                if (mProgress != null) {
                    Dialog dialog = mProgress.get();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    mProgress = null;
                }
            }
        }
    }
    
    private boolean matchYiliaoNumber(long contactId) {
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
                    if (arrRosterNumber.contains(number) == false)
                        arrNumbers.add(number);
                }
                
                if (arrNumbers != null && arrNumbers.size() > 0) {
                    Intent intent= new Intent(ACTION_YILIAO_MATCH_NUMBERS); 
                    PendingIntent mRespondIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                    IMClient.CheckUserRegister(this, IMMessage.GeXinIM, arrNumbers, mRespondIntent);
                    return true;
                }

            }
        } finally {
            cursor.close();
            cursor = null;
            rosterCursor.close();
            rosterCursor.close();
        }
        
        return false;
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
    
    private String getContactSelection() {
        String strSelection = null;
        if (mDisplayOnlyPhones) {
            strSelection = ContactsListActivity.CLAUSE_ONLY_VISIBLE + " AND " + ContactsListActivity.CLAUSE_ONLY_PHONES;
        } else {
            strSelection = ContactsListActivity.CLAUSE_ONLY_VISIBLE;
        }
        if (mshowContactsYOption == ContactsListActivity.CONTACTS_SHOW_YILIAO_ONLY) {
            strSelection = strSelection + " AND " + ContactsListActivity.CLAUSE_ON_LEWA_MSG;
        }
        return strSelection;
    }
}
