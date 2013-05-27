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

package com.lewa.PIM.calllog.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter.FilterListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.lewa.os.filter.FilterItem;
import com.lewa.os.filter.SpannableFilterItemVisitor;
import com.lewa.os.ui.ListBaseActivity;
import com.lewa.os.util.ContactPhotoLoader;
import com.lewa.os.util.NumberLocationLoader;
import com.lewa.os.util.Util;
import com.lewa.os.view.ListBaseAdapter;
import com.lewa.PIM.calllog.data.CallLog;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.contacts.LayoutQuickContactBadge;
import com.lewa.PIM.contacts.LewaSearchBar;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.R;

public final class LogSearchActivity extends ListBaseActivity
        implements TextWatcher,
                View.OnFocusChangeListener,
                View.OnTouchListener,
                FilterListener,
                PimEngine.DataEventListener {
    private static final String TAG = "LogSearchActivity";

    private ViewGroup mMainView;
    private EditText mFilterEdit;
    private LogSearchListAdapter mLogAdapter;
    private ContactPhotoLoader mPhotoLoader;
    private NumberLocationLoader mNumLocationLoader;
    private int mContextMenuIdx = -1;
    private LewaSearchBar mLewaSearchBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mMainView = (ViewGroup )layoutInflater.inflate(R.layout.calllog_search_list, null);
        setContentView(mMainView);
        //setTitle(R.string.search_call_log);
        initSearchBar();

        //List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mLogAdapter = new LogSearchListAdapter(this, null, mPhotoLoader); //(this, logGroups, mPhotoLoader);
        setListAdapter(mLogAdapter);

        mNumLocationLoader = new NumberLocationLoader(this);

        ListView lv = getListView();
        lv.setOnScrollListener(this);
        lv.setOnFocusChangeListener(this);
        lv.setOnTouchListener(this);
        registerForContextMenu(lv);

        PimEngine.getInstance(this).addDataListenner(this);
    }

    @Override
    protected void onDestroy() {
        resetFilter();
        mPhotoLoader.stop();
        mNumLocationLoader.stop();
        PimEngine.getInstance(this).removeDataListenner(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPhotoLoader.resume();
        mNumLocationLoader.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        CallLogGroup clGroup = (CallLogGroup )l.getItemAtPosition(position);
        String name = clGroup.getName();
        String number = clGroup.getNumber();
        long contactId = clGroup.getContactId();
        Log.d(TAG, "onListItemClick: " + position + " name=" + name + " number=" + number + " contactId=" + contactId);
        //CommonMethod.viewLogDetail(this, name, number);
        CommonMethod.viewPimDetail(this, name, number, contactId, DetailEntry.LOG_DETAIL);
    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
        }

        if (SCROLL_STATE_FLING == scrollState) {
            mPhotoLoader.pause();
            mNumLocationLoader.pause();
        }
        else {
            mPhotoLoader.resume();
            mNumLocationLoader.resume();
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus && (view == getListView())) {
            hideSoftKeyboard();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == getListView()) {
            hideSoftKeyboard();
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((KeyEvent.KEYCODE_BACK == keyCode)
                && ((null != mFilterEdit) && TextUtils.isEmpty(mFilterEdit.getText().toString()))) {
            hideSoftKeyboard();
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfoIn) {
        super.onCreateContextMenu(menu, v, menuInfoIn);
        
        AdapterView.AdapterContextMenuInfo menuInfo = null;
        try {
             menuInfo = (AdapterView.AdapterContextMenuInfo) menuInfoIn;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfoIn", e);
            return;
        }

        mContextMenuIdx = menuInfo.position;
        
        CallLogGroup clGroup = mLogAdapter.getItem(menuInfo.position);
        String name = clGroup.getName();
        String number = clGroup.getNumber();
        long contactId = clGroup.getContactId();
        Log.d(TAG, "onCreateContextMenu: name=" + name + " number=" + number + " contactId=" + contactId);

        menu.add(0, R.id.menu_call_contact, 0, R.string.call_contact);
        menu.add(0, R.id.menu_send_message, 0, R.string.send_message);
        if (0 == contactId) {
            menu.add(0, R.id.menu_new_contact, 0, R.string.menu_newContact);
            menu.add(0, R.id.menu_add_to_contacts, 0, R.string.menu_add_to_contacts);
        }
        else {
            menu.add(0, R.id.menu_view_contact, 0, R.string.view_contact);
        }
        menu.add(0, R.id.menu_delete_log, 0, R.string.delete_call_log);
        if (CommonMethod.numberIsInBlacklist(this, number)) {
            menu.add(0, R.id.menu_clear_from_blacklist, 0, R.string.clear_from_blacklist);
        }
        else {
            menu.add(0, R.id.menu_add_to_blacklist, 0, R.string.add_to_blacklist);
        }
        
        if (!TextUtils.isEmpty(name)) {
            menu.setHeaderTitle(name);
        }
        else {
            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                number = getResources().getString(R.string.unknown);
            }
            else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                number = getResources().getString(R.string.private_num);
            }
            else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                number = getResources().getString(R.string.payphone);
            }
            menu.setHeaderTitle(number);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = null;
        try {
             menuInfo = (AdapterView.AdapterContextMenuInfo )item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfoIn", e);
            return super.onContextItemSelected(item);
        }

        CallLogGroup clGroup = mLogAdapter.getItem(menuInfo.position);
        String name = clGroup.getName();
        String number = clGroup.getNumber();
        long contactId = clGroup.getContactId();
        Log.d(TAG, "onContextItemSelected: name=" + name + " number=" + number + " contactId=" + contactId);
        switch (item.getItemId()) {
        case R.id.menu_new_contact:
            CommonMethod.newContact(this, number);
            break;

        case R.id.menu_add_to_contacts:
            CommonMethod.createContact(this, number);
            break;

        case R.id.menu_view_contact:
            CommonMethod.viewContact(this, contactId);
            break;
            
        case R.id.menu_call_contact:
            CommonMethod.call(this, number);
            break;
            
        case R.id.menu_send_message:
            CommonMethod.sendMessage(this, number, null);
            break;
            
        case R.id.menu_delete_log:
            CommonMethod.showConfirmDlg(this,
                    R.string.clear_call_log_confirm,
                    R.string.alert_dialog_title,
                    new ContextMenuDelListener());
            break;

        case R.id.menu_add_to_blacklist:
            CommonMethod.addToBlacklist(this, name, number);
            break;

        case R.id.menu_clear_from_blacklist:
            CommonMethod.clearFromBlacklist(this, name, number);
            
        default:
            break;
        }
        
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<CallLogGroup> logGroups = mLogAdapter.getListData();
        if ((null != logGroups) && (logGroups.size() > 0)) {
            getMenuInflater().inflate(R.menu.log_options_menu, menu);
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        List<CallLogGroup> logGroups = mLogAdapter.getListData();
        if ((null != logGroups) && (logGroups.size() > 0)) {
            return super.onPrepareOptionsMenu(menu);
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_delete_log == item.getItemId()) {
            CommonMethod.showConfirmDlg(this,
                    R.string.clear_all_call_log_confirm,
                    R.string.alert_dialog_title,
                    new OptionsMenuDelListener());
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String strKey = s.toString();
        if (!TextUtils.isEmpty(strKey)) {
            if (!mLogAdapter.isDataInit()) {
                final List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
                mLogAdapter.update(logGroups, true);
            }
            filterText(strKey);
        }
        else {
            mLogAdapter.update(null, true);
            //mMainView.setBackgroundResource(R.color.translucent_search_background);
            mLewaSearchBar.setSearchResult(getString(R.string.search_call_log));
        }
    }

    @Override
    public void onFilterComplete(int count) {
        /*if (count <= 0) {
            mMainView.setBackgroundResource(R.color.translucent_search_background);
        }
        else {
            mMainView.setBackgroundResource(R.drawable.activity_background);
        }*/
    	if(count >0) {
    		String string = "";
    		
    		if (count == 1) {
    			string = getString(R.string.search_calllog_count, count);
			}else {
				string = getString(R.string.search_calllog_counts, count);
			}
    		mLewaSearchBar.setSearchResult(string);
    	} else {
    		mLewaSearchBar.setSearchResult(getString(R.string.search_call_log));
    	}
    }

    public void onDataEvent(PimEngine.DataEvent event, int state) {
        if ((PimEngine.DataEvent.LOAD_CALLLOGS == event)
                && (PimEngine.DataEventListener.LOAD_DATA_DONE == state)) {
            reloadCallLogs();
        }
        else if (PimEngine.DataEvent.CONTACTS_CHANGED == event) {
            mPhotoLoader.clear();
        }
    }

    public void filterText(String key) {
        resetFilter();
        mLogAdapter.getFilter().filter(key, this);
    }
    
    public void exitSearch() {
        finish();
    }

    private void resetFilter() {
        int nCount = mLogAdapter.getCount();
        for (int i = 0; i < nCount; ++i) {
            final CallLogGroup clGroup = (CallLogGroup )mLogAdapter.getItem(i);
            clGroup.setMatchField(FilterItem.INVALID_FIELD);
            clGroup.setMatchKey(null);
        }
    }

    private void initSearchBar() {
        mFilterEdit = (EditText )findViewById(R.id.search_src_text);
        mFilterEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        //mFilterEdit.setHint(R.string.search_call_log);
        mLewaSearchBar = (LewaSearchBar)findViewById(R.id.search_plate);
        mLewaSearchBar.setSearchResult(getString(R.string.search_call_log));
        mFilterEdit.addTextChangedListener(this);
        mFilterEdit.requestFocus();

        ImageView imgCategory = (ImageView )findViewById(R.id.img_search_category);
        if (null != imgCategory) {
            imgCategory.setImageResource(R.drawable.ic_calllog_search_bar);
        }
    }

    private void reloadCallLogs() {
        //final List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
        //mLogAdapter.update(logGroups, true);

        if (null != mFilterEdit) {
            String strSearch = mFilterEdit.getText().toString();
            if (!TextUtils.isEmpty(strSearch)) {
                final List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
                mLogAdapter.update(logGroups, true);
                filterText(strSearch);
            }
            else {
                mLogAdapter.update(null, true);
            }
        }
    }

    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getListView().getWindowToken(), 0);
    }


    private class ContextMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                final CallLogGroup clGroup = mLogAdapter.getItem(mContextMenuIdx);
                List<CallLogGroup> logGroups = mLogAdapter.getListData();
                logGroups.remove(clGroup);
                mLogAdapter.update(logGroups, false);
                PimEngine.getInstance(LogSearchActivity.this).deleteCallLogs(clGroup.getLogIds());
            }
        }
    }


    private class OptionsMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                //PimEngine.getInstance(LogSearchActivity.this).deleteCallLogs(null);
                //mLogAdapter.update(null, false);
                int groupCount = mLogAdapter.getCount();
                if (groupCount > 0) {
                    ArrayList<Long> arrayLogIds = new ArrayList<Long>(groupCount);
                    for (int i = 0; i < groupCount; ++i) {
                        final CallLogGroup group = mLogAdapter.getItem(i);
                        final int clCount = group.getSize();
                        for (int j = 0; j < clCount; ++j) {
                            arrayLogIds.add(group.getLog(j).getId());
                        }
                    }
                    PimEngine.getInstance(LogSearchActivity.this).deleteCallLogs(arrayLogIds);
                    mLogAdapter.update(null, false);
                }
            }
        }
    }


    private class LogSearchListAdapter extends ListBaseAdapter<CallLogGroup> {
        private Context mContext;
        private SpannableFilterItemVisitor mItemVisitor;
        //private ContactPhotoLoader mPhotoLoader;
        private SimpleDateFormat mTimeFormat;

        public LogSearchListAdapter(Context context, List<CallLogGroup> clGroups, ContactPhotoLoader photoLoader) {
            super(clGroups);
            mContext = context;
            mItemVisitor = new SpannableFilterItemVisitor();
            //mPhotoLoader = photoLoader;
        }

        public boolean isDataInit() {
            return (null != mDataItems);
        }

        @Override
        protected View createItemView(CallLogGroup clGroup, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            return layoutInflater.inflate(R.layout.calllog_entry, null);
        }

        @Override
        protected void bindItemView(CallLogGroup clGroup, int position, View itemView) {
            CallLog cl = clGroup.getLog();
            String name    = cl.getName();
            String number  = cl.getNumber();
            //String date   = new SimpleDateFormat("MM/dd HH:mm").format(cl.getDate());
            int type       = cl.getType();
            long contactId = cl.getContactId();
            int count      = clGroup.getSize();
            boolean hasName = false;

            String strDate;
            long timeStamp = cl.getDate().getTime();
            if (DateUtils.isToday(timeStamp)) {
                if (DateFormat.is24HourFormat(mContext)) {
                    strDate = DateUtils.formatDateTime(
                            mContext,
                            timeStamp,
                            (DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR));
                }
                else {
                    if (null == mTimeFormat) {
                        mTimeFormat = new SimpleDateFormat(mContext.getString(R.string.twelve_hour_time_format));
                    }
                    strDate = mTimeFormat.format(cl.getDate());
                }
            }
            else {
                strDate = DateUtils.formatDateTime(
                        mContext,
                        timeStamp,
                        (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
            }
            strDate = CommonMethod.trim(strDate, new char[] {' ', '\u0020'});

            ItemViewHolder viewHolder = (ItemViewHolder )getViewHolder(itemView, clGroup);

            if (0 == contactId) {
                //mPhotoLoader.loadPhoto(viewHolder.mPhoto, cl.getPhotoId(), contactId);
                mPhotoLoader.loadSpecialPhoto(viewHolder.mPhoto, number);
                //viewHolder.mPhoto.assignContactFromPhone(number, true);
                //viewHolder.mPhoto.setOnClickListener(viewHolder.mPhoto);
                new LayoutQuickContactBadge.UnknownQCBOnClickListener(
                        mContext,
                        number,
                        viewHolder.mPhoto);
            }
            else {
                mPhotoLoader.loadPhoto(viewHolder.mPhoto, cl.getPhotoId());
                viewHolder.mPhoto.setOnClickListener(viewHolder.mPhoto);
                viewHolder.mPhoto.assignContactUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
            }

            mNumLocationLoader.loadLocation(viewHolder.mLocation, number);

            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                number = mContext.getResources().getString(R.string.unknown);
            }
            else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                number = mContext.getResources().getString(R.string.private_num);
            }
            else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                number = mContext.getResources().getString(R.string.payphone);
            }

            if (FilterItem.NAME_FIELD == clGroup.getMatchField()) {
                SpannableStringBuilder strBuilder = new SpannableStringBuilder();
                mItemVisitor.Visit(clGroup, strBuilder);
                viewHolder.mDispName.setText(strBuilder, TextView.BufferType.SPANNABLE);

                hasName = true;
            }
            else {
                if (!TextUtils.isEmpty(name)) {
                    hasName = true;
                }
                else {
                    if (Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())) {
                        name = mContext.getResources().getText(R.string.emergency_number).toString();
                        hasName = true;
                    }
                    else {
                        name = number;
                    }
                }
                viewHolder.mDispName.setText(name);
            }

            if (count > 1) {
                viewHolder.mDispCount.setText("(" + String.valueOf(count) + ")");
                viewHolder.mDispCount.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.mDispCount.setVisibility(View.GONE);
            }

            if (FilterItem.NUMBER_FIELD == clGroup.getMatchField()) {
                SpannableStringBuilder strBuilder = new SpannableStringBuilder();
                mItemVisitor.Visit(clGroup, strBuilder);
                viewHolder.mDispNumber.setText(strBuilder, TextView.BufferType.SPANNABLE);
                viewHolder.mDispNumber.setVisibility(View.VISIBLE);
            }
            else {
                if (hasName) {
                    viewHolder.mDispNumber.setText(number);
                    viewHolder.mDispNumber.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.mDispNumber.setText(null);
                    viewHolder.mDispNumber.setVisibility(View.GONE);
                }
            }

            viewHolder.mDate.setText(strDate);

            if (Calls.MISSED_TYPE == type) {
                viewHolder.mType.setImageResource(R.drawable.ic_log_missed);
            }
            else if (Calls.OUTGOING_TYPE == type) {
                viewHolder.mType.setImageResource(R.drawable.ic_log_out);
            }
            else if (Calls.INCOMING_TYPE == type) {
                viewHolder.mType.setImageResource(R.drawable.ic_log_in);
            }
        }

        @Override
        protected boolean isOriginalItemsFixed() {
            return true;
        }

        @Override
        protected Object createViewHolder(View itemView, CallLogGroup clGroup) {
            ItemViewHolder viewHolder = new ItemViewHolder();
            viewHolder.mDispName   = (TextView )itemView.findViewById(R.id.txt_log_displayname);
            viewHolder.mDispCount  = (TextView )itemView.findViewById(R.id.txt_log_displaycount);
            viewHolder.mDispNumber = (TextView )itemView.findViewById(R.id.txt_log_displaynumber);
            viewHolder.mLocation   = (TextView )itemView.findViewById(R.id.txt_number_location);
            viewHolder.mDate       = (TextView )itemView.findViewById(R.id.txt_log_time);
            viewHolder.mPhoto      = (QuickContactBadge )itemView.findViewById(R.id.img_log_thumnail);
            viewHolder.mType       = (ImageView )itemView.findViewById(R.id.img_log_type);
            return viewHolder;
        }


        private class ItemViewHolder {
            TextView  mDispName;
            TextView  mDispCount;
            TextView  mDispNumber;
            TextView  mLocation;
            TextView  mDate;
            QuickContactBadge mPhoto;
            ImageView mType;
        }
    }
}
