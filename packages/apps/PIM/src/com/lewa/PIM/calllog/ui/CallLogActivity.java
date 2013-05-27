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

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import com.lewa.os.ui.ListBaseActivity;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.util.ContactPhotoLoader;
import com.lewa.os.util.NumberLocationLoader;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.calllog.view.CallLogListAdapter;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.service.PimStart;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.R;

public final class CallLogActivity extends ListBaseActivity
        implements View.OnClickListener,
                ActivityResultReceiver,
                PendingContentLoader,
                PimEngine.DataEventListener {
    private static final String TAG = "CallLogActivity";

    private ViewGroup mMainSoftBar;
    private ViewGroup mCategorySoftBar;
    private CallLogListAdapter mLogAdapter;
    private ContactPhotoLoader mPhotoLoader;
    private NumberLocationLoader mNumLocationLoader;
    private int mContextMenuIdx = -1;
    private int mViewType = -1;

    private ActivityResultBridge mActivityResultBridge = null;

    public void cancelMissedCallNotification() {
        Log.d(TAG, "cancelMissedCallNotification");
        PimEngine.getInstance(this).setCallLogsRead();
        CommonMethod.cancelMissedCallNotification(this);
    }

    public void resetToInitialStatus() {
        Log.e(TAG, "resetToInitialStatus");
        if ((-1 != mViewType)
                || ((null != mMainSoftBar) && (View.VISIBLE != mMainSoftBar.getVisibility()))){
            categorizeCallLog(-1);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PimStart.start(this.getApplicationContext());
        //SimCard.hasIccCard(this, SimCard.GEMINI_SIM_1);
        //SimCard.hasIccCard(this, SimCard.GEMINI_SIM_2);
        //SimCard.getSimState(this, SimCard.GEMINI_SIM_1);
        //SimCard.getSimState(this, SimCard.GEMINI_SIM_2);

        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onCreate: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        if (!bDelayLoadContent) {
            setContentView(R.layout.calllog_main_list);
            initMainSoftBar();
            setupListView();
        }

        PimEngine.getInstance(this).addDataListenner(this);
    }

    @Override
    protected void onDestroy() {
        if (null != mPhotoLoader) {
            mPhotoLoader.stop();
        }

        if (null != mNumLocationLoader) {
            mNumLocationLoader.stop();
        }
        
        mActivityResultBridge = null;
        PimEngine.getInstance(this).removeDataListenner(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != mPhotoLoader) {
            mPhotoLoader.resume();
        }

        if (null != mNumLocationLoader) {
            mNumLocationLoader.resume();
        }
        
        //PimEngine.getInstance(this).setCallLogsRead();
        //CommonMethod.cancelMissedCallNotification(this);
    }

    @Override
    public void registerActivityResultBridge(ActivityResultBridge bridge) {
        mActivityResultBridge = bridge;
    }

    @Override
    public void handleActivityResult(
            ActivityResultReceiver realReceiver,
            int requestCode,
            int resultCode,
            Intent intent) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_SEARCH == keyCode) {
            openSearcher();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        if (SCROLL_STATE_FLING == scrollState) {
            if (null != mPhotoLoader) {
                mPhotoLoader.pause();
            }

            if (null != mNumLocationLoader) {
                mNumLocationLoader.pause();
            }
        }
        else {
            if (null != mPhotoLoader) {
                mPhotoLoader.resume();
            }

            if (null != mNumLocationLoader) {
                mNumLocationLoader.resume();
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                openSearcher();
                break;

            case R.id.btn_filter_calllog:
                createCategoryBtns();
                mMainSoftBar.setVisibility(View.GONE);
                break;

            case R.id.btn_open_dialpad:
                CommonMethod.openDialpad(this, null);
                break;
                
            case R.id.btn_categorize_all_calllog:
                categorizeCallLog(-1);
                break;
                
            case R.id.btn_categorize_out_calllog:
                categorizeCallLog(Calls.OUTGOING_TYPE);
                break;
                
            case R.id.btn_categorize_in_calllog:
                categorizeCallLog(Calls.INCOMING_TYPE);
                break;
                
            case R.id.btn_categorize_miss_calllog:
                categorizeCallLog(Calls.MISSED_TYPE);
                break;

            default:
                break;
        }
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

        if (null != mActivityResultBridge) {
            mActivityResultBridge.handleActivityEvent(
                    this,
                    ActivityResultBridge.EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU,
                    null);
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
            break;
            
        default:
            break;
        }
        
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (null != mLogAdapter) {
            List<CallLogGroup> logGroups = mLogAdapter.getListData();
            if ((null != logGroups) && (logGroups.size() > 0)) {
                getMenuInflater().inflate(R.menu.log_options_menu, menu);
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (null != mLogAdapter) {
            List<CallLogGroup> logGroups = mLogAdapter.getListData();
            if ((null != logGroups) && (logGroups.size() > 0)) {
                return super.onPrepareOptionsMenu(menu);
            }
            else {
                return false;
            }
        }
        return false;
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
    public void loadContent() {
        Log.d(TAG, "loadContent");
        getIntent().putExtra("delayloadcontent", false);
        setContentView(R.layout.calllog_main_list);
        initMainSoftBar();
        setupListView();
    }

    public void onDataEvent(PimEngine.DataEvent event, int state) {
        if ((PimEngine.DataEvent.LOAD_CALLLOGS == event)
                && (PimEngine.DataEventListener.LOAD_DATA_DONE == state)) {
            reloadCallLogs();
            if (Calls.OUTGOING_TYPE == mViewType) {
                setLogsEmptyHint(getResources().getText(R.string.outgoing_logs_empty));
            }
            else if (Calls.INCOMING_TYPE == mViewType) {
                setLogsEmptyHint(getResources().getText(R.string.incoming_logs_empty));
            }
            else if (Calls.MISSED_TYPE == mViewType){
                setLogsEmptyHint(getResources().getText(R.string.missed_logs_empty));
            }
            else {
                setLogsEmptyHint(getResources().getText(R.string.call_logs_empty));
            }
        }
        else if (PimEngine.DataEvent.CONTACTS_CHANGED == event) {
            if (null != mPhotoLoader) {
                mPhotoLoader.clear();
            }
        }
    }

    private void setupListView() {
        PimEngine pimEng = PimEngine.getInstance(this);
        List<CallLogGroup> logGroups = pimEng.loadCallLogs(false);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mNumLocationLoader = new NumberLocationLoader(this);
        mLogAdapter = new CallLogListAdapter(this, logGroups, mPhotoLoader, mNumLocationLoader);
        setListAdapter(mLogAdapter);
        if (pimEng.isLoadCallLogsInProgress()) {
            setLogsEmptyHint(null);
        }

        ListView lv = getListView();
        lv.setOnScrollListener(this);
        registerForContextMenu(lv);
    }
    
    private void initMainSoftBar() {
        //int[] btnIds    = {R.id.btn_search, R.id.btn_filter_calllog, R.id.btn_open_dialpad};
        //int[] btnResIds = {R.drawable.btn_search_s, R.drawable.btn_filter_calllog_s, R.drawable.btn_open_dialpad};
        //if (null == mFootBar) {
        //    mFootBar = new FootBar(this, btnIds, btnResIds, this);
        //    mFootBar.addToView(mMainView);
        //}
        //else {
        //    mFootBar.removeAllButtons();
        //    mFootBar.addButtons(btnIds, btnResIds, this);
        //}

        if (null == mMainSoftBar) {
            mMainSoftBar = (ViewGroup )findViewById(R.id.cl_main_softkey_bar);
            
            View btn = null;
            int count = mMainSoftBar.getChildCount();
            for (int i = 0; i < count; ++i) {
                btn = mMainSoftBar.getChildAt(i);
                btn.setOnClickListener(this);
            }
        }
        mMainSoftBar.setVisibility(View.VISIBLE);

        if (null != mCategorySoftBar) {
            mCategorySoftBar.setVisibility(View.GONE);
        }
    }

    private void categorizeCallLog(int logType) {
        if (null != mLogAdapter) {
            final List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
            if ((Calls.OUTGOING_TYPE == logType)
                    || (Calls.INCOMING_TYPE == logType)
                    || (Calls.MISSED_TYPE == logType)){
                final int count = logGroups.size();
                if (count > 0) {
                    final ArrayList<CallLogGroup> newValues = new ArrayList<CallLogGroup>(count);
                    for (int i = 0; i < count; i++) {
                        final CallLogGroup item = logGroups.get(i);
                        if (logType == item.getType()) {
                            newValues.add(item);
                        }
                    }
                    mLogAdapter.update(newValues, false);
                }
            }
            else {
                mLogAdapter.update(logGroups, false);
            }

            mViewType = logType;
            if (Calls.OUTGOING_TYPE == mViewType) {
                setLogsEmptyHint(getResources().getText(R.string.outgoing_logs_empty));
            }
            else if (Calls.INCOMING_TYPE == mViewType) {
                setLogsEmptyHint(getResources().getText(R.string.incoming_logs_empty));
            }
            else if (Calls.MISSED_TYPE == mViewType){
                setLogsEmptyHint(getResources().getText(R.string.missed_logs_empty));
            }
            else {
                setLogsEmptyHint(getResources().getText(R.string.call_logs_empty));
            }
        }
        initMainSoftBar();
    }

    private void createCategoryBtns() {
        if (null == mCategorySoftBar) {
//            int[] btnIds = {R.id.btn_categorize_all_calllog,
//                    R.id.btn_categorize_out_calllog,
//                    R.id.btn_categorize_in_calllog,
//                    R.id.btn_categorize_miss_calllog};
//            int[] btnResIds = {R.drawable.ic_log_all_normal,
//                    R.drawable.ic_log_out_normal,
//                    R.drawable.ic_log_in_normal,
//                    R.drawable.ic_log_missed_normal};
//            int[] btnBgResIds = {R.drawable.btn_log_type_all_s,
//                    R.drawable.btn_log_type_out_s,
//                    R.drawable.btn_log_type_in_s,
//                    R.drawable.btn_log_type_miss_s};
//            mCategorySoftBar = new FootBar(this, btnIds, btnResIds, btnBgResIds, this);
//            mCategorySoftBar.setBackgroundResource(R.drawable.bg_softkey_bar);
//            mCategorySoftBar.addToView(mMainView);
            mCategorySoftBar = (ViewGroup )findViewById(R.id.cl_category_softkey_bar);
            
            View btn = null;
            int count = mCategorySoftBar.getChildCount();
            for (int i = 0; i < count; ++i) {
                btn = mCategorySoftBar.getChildAt(i);
                btn.setOnClickListener(this);
            }
        }
        mCategorySoftBar.setVisibility(View.VISIBLE);
    }    

    private void openSearcher() {
        Intent searchLogIntent = new Intent("com.lewa.intent.action.SEARCH_LOG");
        startActivity(searchLogIntent);
        resetToInitialStatus();
    }

    private void reloadCallLogs() {
        if (null != mLogAdapter) {
            final List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
            mLogAdapter.update(logGroups, true);
        }
    }

    private void setLogsEmptyHint(CharSequence emptyHint) {
        View emptyView = getListView().getEmptyView();
        if (null != emptyView) {
            if (emptyView instanceof TextView) {
                ((TextView )emptyView).setText(emptyHint);
            }
            else {
                TextView txtEmpty = (TextView )emptyView.findViewById(R.id.txt_empty);
                if (null != txtEmpty) {
                    txtEmpty.setText(emptyHint);
                }

                ImageView imgEmpty = (ImageView )emptyView.findViewById(R.id.img_empty);
                if (null != imgEmpty) {
                    imgEmpty.setVisibility((null == emptyHint)? View.GONE : View.VISIBLE);
                }
            }
        }
    }
    

    private class ContextMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                final CallLogGroup clGroup = mLogAdapter.getItem(mContextMenuIdx);
                List<CallLogGroup> logGroups = mLogAdapter.getListData();
                logGroups.remove(clGroup);
                mLogAdapter.update(logGroups, false);
                PimEngine.getInstance(CallLogActivity.this).deleteCallLogs(clGroup.getLogIds());
            }
        }
    }


    private class OptionsMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                PimEngine.getInstance(CallLogActivity.this).deleteCallLogs((long[] )null);
                if (null != mLogAdapter) {
                    mLogAdapter.update(null, false);
                }
            }
        }
    }
}
