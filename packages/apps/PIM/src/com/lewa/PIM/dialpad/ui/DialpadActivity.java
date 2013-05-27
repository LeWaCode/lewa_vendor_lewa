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

package com.lewa.PIM.dialpad.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter.FilterListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.CallerInfo;
import com.lewa.os.filter.FilterItem;
import com.lewa.os.ui.ListBaseActivity;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.util.ContactPhotoLoader;
import com.lewa.os.util.NumberLocationLoader;
import com.lewa.os.view.ListBaseAdapter;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.dialpad.data.DialpadItem;
import com.lewa.PIM.dialpad.view.DialpadListAdapter;
import com.lewa.PIM.dialpad.view.DialpadView;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.service.PimStart;
import com.lewa.PIM.settings.IpCallSettingsActivity;
import com.lewa.PIM.settings.CallLogSettingsActivity;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.R;
import com.lewa.os.util.LocationUtil;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;

public final class DialpadActivity extends ListBaseActivity
        implements DialpadView.DialpadOwner,
            View.OnClickListener,
            FilterListener,
            Callback,
	    PendingContentLoader,
	        ActivityResultReceiver,
            PimEngine.DataEventListener {
    private static final String TAG = "DialpadActivity";

    private static final int DBG_LEVEL = 0;
	
    private static final int RELOAD_DIALPADS       = 1;
    private static final int PROCESS_OUTGOING_CALL = 2;

    private static final int RELOAD_DIALPADS_TIMEOUT = 3;

    private static final int RELOAD_DIALPADS_DELAYTIME = 1500;

    private DialpadView mDialpad;
    private DialpadListAdapter mItemAdapter;
    private ContactPhotoLoader mPhotoLoader;
    private NumberLocationLoader mNumLocationLoader;
    private EditText numEditText;
    private Handler mHandler;

    private int mContextMenuIdx = -1;
    private DialpadItem mDialpadItem;
    
    //private String mOutgoingNumber;
    //private OutgoingCallReceiver mOutgoingCallReceiver;
    //private CallListener mCallListener;
    
    private boolean mIsResume = false;
    private ActivityResultBridge mActivityResultBridge = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialpad_main_list);
        //setTitle(R.string.dialpad);

        PimStart.start(this.getApplicationContext());

        PimEngine pimEng = PimEngine.getInstance(this);
        pimEng.loadCallLogs(false);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mNumLocationLoader = new NumberLocationLoader(this);
        final List<DialpadItem> logGroups = PimEngine.getInstance(this).loadDialpadItems(false);
        
        final int count = logGroups.size();
        if (count > 0) {
            final ArrayList<DialpadItem> newValues = new ArrayList<DialpadItem>(count);
            for (int i = 0; i < count; i++) {
                final DialpadItem item = logGroups.get(i);
                if (DialpadItem.CALLLOG_TYPE == item.getType()) {
                    newValues.add(item);
                }
            }
            mItemAdapter = new DialpadListAdapter(this, newValues,
                    mPhotoLoader, mNumLocationLoader,false);
        } else {
            mItemAdapter = new DialpadListAdapter(this, null,
                    mPhotoLoader, mNumLocationLoader,false);
        }


        //mItemAdapter = new DialpadListAdapter(this, pimEng.loadCallLogItems(false),
        //        mPhotoLoader, mNumLocationLoader);
        setListAdapter(mItemAdapter);
        
        ListView lv = getListView();
        lv.setOnScrollListener(this);
        registerForContextMenu(lv);

        View dialpadView = findViewById(R.id.dialpad_keyboard);
        mDialpad = new DialpadView(dialpadView, this);
        
        Intent dialIntent = getIntent();
        if (null != dialIntent) {
            Uri telUri = dialIntent.getData();
            if (null != telUri) {
                mDialpad.setNumber(telUri.getSchemeSpecificPart());
                mDialpad.showNumberUI();
            }
        }

        mHandler = new Handler(Looper.myLooper(), this);

        /*IntentFilter intentFilter = new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
        intentFilter.addCategory("android.intent.category.DEFAULT");
        mOutgoingCallReceiver = new OutgoingCallReceiver();
        registerReceiver(mOutgoingCallReceiver, intentFilter);

        mCallListener = new CallListener();
        TelephonyManager telephonyMgr = (TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        telephonyMgr.listen(mCallListener, PhoneStateListener.LISTEN_CALL_STATE);*/

        pimEng.addDataListenner(this);
    }

    @Override
	public void registerContextMenu(View view) {
		// TODO Auto-generated method stub
    	if(view != null){
    		numEditText = (EditText)view;
    		registerForContextMenu(numEditText);
    	}
	}

	@Override
    protected void onDestroy() {
        //unregisterReceiver(mOutgoingCallReceiver);
        //TelephonyManager telephonyMgr = (TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        //telephonyMgr.listen(mCallListener, PhoneStateListener.LISTEN_NONE);
        
        resetFilter();
        mPhotoLoader.stop();
        mNumLocationLoader.stop();
        PimEngine.getInstance(this).removeDataListenner(this);
        mActivityResultBridge = null;

        if (mPopupWin != null) {
            mPopupWin.dismiss();
            mPopupWin = null;
        }            
        
        super.onDestroy();        
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPhotoLoader.resume();
        mNumLocationLoader.resume();
        setVolumeControlStream(AudioManager.STREAM_SYSTEM);

        PimEngine.getInstance(this).setCallLogsRead();
        CommonMethod.cancelMissedCallNotification(this);

        if (mHandler.hasMessages(RELOAD_DIALPADS)) {
            log("onResume");
            mHandler.removeMessages(RELOAD_DIALPADS);
            reloadDialpadItems();
        }
        mIsResume = true;
    }

    @Override
    protected void onPause() {
    	 mDialpad.stopQueryLocation();
        super.onPause();
        mIsResume = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (hideCaidan()) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void loadContent() {
        log("loadContent");
	return;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        {
            super.onListItemClick(l, v, position, id);
	     DialpadItem item = (DialpadItem)l.getItemAtPosition(position);
            String number = item.getNumber();
	     long contactId = item.getContactId();
	     String name = item.getName();
	     SharedPreferences mPrefs = getSharedPreferences("call_log_settings", Context.MODE_PRIVATE);
		 
            int mClickOption = mPrefs.getInt("call_log_click_option", 0);

            log("onListItemClick: " + position + " " + number + "Clickoption = " + mClickOption );
            if (!TextUtils.isEmpty(number)) {
		 if(mClickOption == 0) {
		 	CommonMethod.viewPimDetail(this, name, number, contactId, (item.getType() == DialpadItem.CALLLOG_TYPE ? DetailEntry.LOG_DETAIL :DetailEntry.CONTACT_DETAIL));
		 }
		 else {
		 	  //Added by GanFeng 20120213, fix bug3391
			if ((null != mDialpad) && TextUtils.isEmpty(mDialpad.getNumber())) {
				long timeElapsed = Math.abs(System.currentTimeMillis() - mDialpad.getTimeStamp());
				if (timeElapsed < 1000) { //the elapsed time < 1S
				return;
			}
			}
			if ((null != mDialpad) && !TextUtils.isEmpty(mDialpad.getNumber())) {
				mDialpad.setNumber(null);
				mDialpad.showNumberUI();
			}
			call(number);
                        
		 }            
            }
        }
    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
            showDialpad(false);
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

    // Woody Guo @ 2012/05/24
    // Save logcat to a file in sdcard/data
    private final static String MAGIC_GENERATE_LOG_SDCARD = "*5392*564#";
    private final static String MAGIC_GENERATE_LOG_DATA = "*5392*5641#";
    private final static String LOG_FILE_SDCARD = "/sdcard/logcat.txt";
    private final static String LOG_FILE_DATA = "/data/local/logcat.txt";
    private void generateLog(final boolean saveToData) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                java.lang.Process p = null;
                try {
                    p = Runtime.getRuntime().exec("sh");
                    java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                    os.writeBytes("/system/bin/logcat > " + (saveToData ? LOG_FILE_DATA : LOG_FILE_SDCARD));
                    os.flush();
                    os.close();
                    Thread.sleep(3000);
                    p.destroy();
                    p = null;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            android.widget.Toast toast = android.widget.Toast.makeText(
                                    getApplicationContext()
                                    , String.format(getString(R.string.toast_log_generated)
                                            , (saveToData ? LOG_FILE_DATA : LOG_FILE_SDCARD))
                                    , android.widget.Toast.LENGTH_LONG);
                            toast.setGravity(android.view.Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });
                } catch (Exception e) {
                    // ignroe any exception
                } finally {
                    if (null != p) {
                        p.destroy();
                        p = null;
                    }
                }
            }
        };

        thread.start();
    }
    // END

    // added by huizhang 2012/7/30
    // Start app Cit
    private final static String MAGIC_CALL_CIT = "*5392*248#";
    private void StartCit(final boolean startCit) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    log("Start Cit Activity");
                    Intent intent=new Intent("com.lewa.cit");
                    startActivity(intent);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            android.widget.Toast toast = android.widget.Toast.makeText(
                                    getApplicationContext()
                                    ,"start cit"
                                    , android.widget.Toast.LENGTH_LONG);
                            toast.setGravity(android.view.Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    });
                } catch (Exception e) {
                    // ignroe any exception
                }
            }
        };

        thread.start();
    }
    // END

    @Override
    public void call(String number) {
        // Woody Guo @ 2012/05/24
        if (number.equals(MAGIC_GENERATE_LOG_DATA)) {
            generateLog(true);
            mDialpad.setNumber(null);
            return;
        }
        if (number.equals(MAGIC_GENERATE_LOG_SDCARD)) {
            generateLog(false);
            mDialpad.setNumber(null);
            return;
        }
        // END

        //added by huizhang @2012/7/30
        if (number.equals(MAGIC_CALL_CIT)) {

            StartCit(true);
            mDialpad.setNumber(null);
            return;
        }
        //END

        CommonMethod.call(this, number);
    }

    @Override
    public void enterContacts() {
        CommonMethod.enterContacts(this);
    }

    @Override
    public void createContact(String number) {
        //if (!CommonMethod.findContactByNumber(this, number)) {
            CommonMethod.createContact(this, number);
        //}
    }

    @Override
    public void newContact(String number) {
        //if (!CommonMethod.findContactByNumber(this, number)) {
            CommonMethod.newContact(this, number);
        //}
    }

    @Override
    public void sendMessage(String number) {
        //if (!CommonMethod.findContactByNumber(this, number)) {
            CommonMethod.sendMessage(this, number, null);
        //}
    }
    
        
	@Override
    public void loadAllContacts() { // called by beforetext changed.
        String number = mDialpad.getNumber();
        if (TextUtils.isEmpty(number)) { // enter first number, shold load contacts.
            log("loadAllContacts() loadDialpadItems...");
            final List<DialpadItem> items = PimEngine.getInstance(this).loadDialpadItems(false);

            mItemAdapter.setIsShowInFilterList(true);
            mItemAdapter.update(items, true);
        }
    }

    private static final String CAIDAN_NUMBER = "1102#";
    private PopupWindow mPopupWin;

    public boolean hideCaidan() {
       /* if (mPopupWin != null && mPopupWin.isShowing()) {
            System.out.println("xbs");
            mPopupWin.dismiss();
            return true;
        }*/
        return false;
    }

	@Override
    public void filterNumber(String number) {
        resetFilter();
        mItemAdapter.getFilter().filter(number, this);
        
        if (CAIDAN_NUMBER.equals(number)) {
            if (mPopupWin == null) {
                LayoutInflater inflater = LayoutInflater.from(this);
                View caidan = inflater.inflate(R.layout.caidan_layout, null);
                mPopupWin = new PopupWindow(caidan, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mPopupWin.setTouchable(true);
                
                View dialpadView = findViewById(R.id.dialpad_keyboard);
                mPopupWin.showAtLocation(dialpadView, Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);             
                
                caidan.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                    if (mPopupWin != null) {
                        mPopupWin.dismiss();
                        mPopupWin = null;
                    }                                        
                }
            });  
            }            
        }
    }

    @Override
    public void showDialpad(boolean show) {
        if (show) {
            mDialpad.show();
        }
        else {
            mDialpad.hide();
        }
    }

    @Override
    public String getLastDialedNumber() {
        List<CallLogGroup> calllogGroups = PimEngine.getInstance(this).loadCallLogs(false);
        int count = calllogGroups.size();
        CallLogGroup clGroup = null;
        for (int i = 0; i < count; ++i) {
            clGroup = calllogGroups.get(i);
            if (Calls.OUTGOING_TYPE == clGroup.getType()) {
                return clGroup.getNumber();
            }
        }
        return null;
    }

    private void resetFilter() {
        int nCount = mItemAdapter.getCount();
        for (int i = 0; i < nCount; ++i) {
            final DialpadItem item = (DialpadItem )mItemAdapter.getItem(i);
            item.setMatchField(FilterItem.INVALID_FIELD);
            item.setMatchKey(null);
        }
    }

    private void reloadDialpadItems() {
        String number = mDialpad.getNumber();
        if (TextUtils.isEmpty(number)) {
            categorizeCallLog(DialpadView.CALLLOG_TYPE_ALL);
            return ;
        } else {
            final List<DialpadItem> items = PimEngine.getInstance(this).loadDialpadItems(false);
            //loge("reloadDialpadItems  items size=" + items.size());
            resetFilter();
            mItemAdapter.updateAndFilter(items, true, number, this);
            //filterNumber(number);
        }
    }

    private void requestReloadingDialpads() {
        boolean isReloadTimeout = false;
        if (mHandler.hasMessages(RELOAD_DIALPADS_TIMEOUT)) {
            isReloadTimeout = true;
            mHandler.removeMessages(RELOAD_DIALPADS_TIMEOUT);
        }
        mHandler.sendEmptyMessageDelayed(RELOAD_DIALPADS_TIMEOUT, RELOAD_DIALPADS_DELAYTIME);
        loge("isReloadTimeout=" + isReloadTimeout);
        mHandler.removeMessages(RELOAD_DIALPADS);
        mHandler.sendEmptyMessageDelayed(RELOAD_DIALPADS, isReloadTimeout ? RELOAD_DIALPADS_DELAYTIME : 0);
    }

    /*private void processOutgoingCall() {
        if ((null != mItemAdapter) && !TextUtils.isEmpty(mOutgoingNumber)) {
            List<DialpadItem> dialpadItems = mItemAdapter.getListData();
            if (null == dialpadItems) {
                dialpadItems = new ArrayList<DialpadItem>();
            }
            
            log("processOutgoingCall: OutgoingNumber=" + mOutgoingNumber + " itemCount=" + dialpadItems.size());
            DialpadItem item = findDialpad(dialpadItems, mOutgoingNumber);
            if (null != item) {
                if (DialpadItem.CALLLOG_TYPE == item.getType()) {
                    dialpadItems.remove(item);
                    item.setCallType(Calls.OUTGOING_TYPE);
                    item.setDate(System.currentTimeMillis());
                    dialpadItems.add(0, item);
                }
                else {
                    String name = item.getName();
                    long contactId = item.getContactId();
                    long photoId = item.getPhotoId();
                    item = DialpadItem.createLogTypeItem(name, mOutgoingNumber, Calls.OUTGOING_TYPE, System.currentTimeMillis());
                    String location = LocationUtil.getPhoneLocation(this, mOutgoingNumber);
                    item.setLocation(location);
                    item.setContactId(contactId);
                    item.setPhotoId(photoId);
                    dialpadItems.add(0, item);
                }
            }
            else {
                item = DialpadItem.createLogTypeItem(null, mOutgoingNumber, Calls.OUTGOING_TYPE, System.currentTimeMillis());
                String location = LocationUtil.getPhoneLocation(this, mOutgoingNumber);
                item.setLocation(location);
                dialpadItems.add(0, item);
            }
            mItemAdapter.update(dialpadItems, false);
        }
    }*/

    private DialpadItem findDialpad(List<DialpadItem> dialpadItems, String numberToFind) {
        if (!TextUtils.isEmpty(numberToFind)) {
            for (DialpadItem item : dialpadItems) {
                if (null == item) {
                    continue;
                }
                
                if (numberToFind.equals(item.getNumber())) {
                    return item;
                }
            }
        }

        return null;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_dialpad:
                showDialpad(true);
                break;

            default:
                break;
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        hideCaidan();
            
        switch (keyCode) {
        case KeyEvent.KEYCODE_CALL: {
            if (null != mDialpad) {
                String number = mDialpad.getNumber();
                if (!TextUtils.isEmpty(number)) {
                    call(number);
                }
                else {
                    number = getLastDialedNumber();
                    if (!TextUtils.isEmpty(number)) {
                        mDialpad.setNumber(number);
                        mDialpad.show();
                    }
                }
            }
            
            //break;
            return true;
        }

        case KeyEvent.KEYCODE_BACK:{
            if (mPopupWin != null) {
                mPopupWin.dismiss();
                mPopupWin = null;
                return true;
            }             
        }

        default:
            break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfoIn) {
        super.onCreateContextMenu(menu, v, menuInfoIn);
        if(v.getId()== R.id.edt_dpkb_call_number){
        	menu.removeItem(android.R.id.switchInputMethod);
        	menu.removeItem(android.R.id.selectAll);
        	menu.removeItem(android.R.id.startSelectingText);

        	return;
        }
        if(menuInfoIn != null){
	        AdapterView.AdapterContextMenuInfo menuInfo = null;
	        try {
	             menuInfo = (AdapterView.AdapterContextMenuInfo) menuInfoIn;
	        } catch (ClassCastException e) {
	            log("bad menuInfoIn e = " + e);
	            return;
	        }
	
	        mContextMenuIdx = menuInfo.position;
	        mDialpadItem = mItemAdapter.getItem(mContextMenuIdx);
	        String name = mDialpadItem.getName();
	        String number = mDialpadItem.getNumber();
	        long contactId = mDialpadItem.getContactId();
	        log("onCreateContextMenu: name=" + name + " number=" + number + " contactId=" + contactId);
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
	        
	        if (DialpadItem.CALLLOG_TYPE == mDialpadItem.getType()) {
	            menu.add(0, R.id.menu_log_detail, 0, R.string.view_calllog_detail);
	        }
	        
	        menu.add(0, R.id.menu_send_message, 0, R.string.send_message);
	        if (0 == contactId) {
	            menu.add(0, R.id.menu_new_contact, 0, R.string.menu_newContact);
				//modify by zenghuaying fix bug #5705
                if(!CommonMethod.contactsIsEmpty(this)){
	            	menu.add(0, R.id.menu_add_to_contacts, 0, R.string.menu_add_to_contacts);
	        	}
  				//modify end
            }
	        else {
	            menu.add(0, R.id.menu_view_contact, 0, R.string.view_contact);
	        }
	
	        if (DialpadItem.CALLLOG_TYPE == mDialpadItem.getType()) {
	            menu.add(0, R.id.menu_delete_log, 0, R.string.delete_call_log);
	        }
	
	        if (CommonMethod.numberIsInBlacklist(this, number)) {
	            menu.add(0, R.id.menu_clear_from_blacklist, 0, R.string.clear_from_blacklist);
	        }
	        else {
	            menu.add(0, R.id.menu_add_to_blacklist, 0, R.string.add_to_blacklist);
	        }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = null;
        try {
             menuInfo = (AdapterView.AdapterContextMenuInfo )item.getMenuInfo();
        } catch (ClassCastException e) {
            log("bad menuInfoIn e=" + e);
            return super.onContextItemSelected(item);
        }

        if (mDialpadItem == null) {
            mDialpadItem = mItemAdapter.getItem(mContextMenuIdx);
        }
        String name = mDialpadItem.getName();
        String number = mDialpadItem.getNumber();
        long contactId = mDialpadItem.getContactId();
        log("onContextItemSelected: name=" + name + " number=" + number + " contactId=" + contactId);
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
            
        case R.id.menu_send_message:
            CommonMethod.sendMessage(this, number, null);
            break;

        case R.id.menu_delete_log:
            CommonMethod.showConfirmDlg(this,
                    R.string.clear_call_log_by_number_confirm,
                    R.string.alert_dialog_title,
                    new DelCallLogItemListener());
            break;

        case R.id.menu_log_detail:
            CommonMethod.viewPimDetail(this, name, number, contactId, DetailEntry.LOG_DETAIL);
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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dialpad_options_menu, menu);
        return true;
    }
	//add by zenghuaying fix bug #8479
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mItemAdapter.getCount() <= 0) {
            menu.removeItem(R.id.menu_delete_log);
        }else{
            menu.clear();
            getMenuInflater().inflate(R.menu.dialpad_options_menu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
	//add end
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_delete_log == item.getItemId()) {
            CommonMethod.showConfirmDlg(this,
                    R.string.clear_all_call_log_confirm,
                    R.string.alert_dialog_title,
                    new OptionsMenuDelListener());
        } else if (R.id.menu_ip_call_settings == item.getItemId()) {
            Intent intent = new Intent(this, IpCallSettingsActivity.class);
            startActivity(intent);
        } else if (R.id.menu_calllog_settings == item.getItemId()) {
            Intent  intent = new Intent(Intent.ACTION_MAIN);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.android.phone",
                        "com.android.phone.CallFeaturesSetting");
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onFilterComplete(int count) {
        if ((count <= 0) && !TextUtils.isEmpty(mDialpad.getNumber())) {
            getListView().setVisibility(View.VISIBLE);
            //mLvMenus.setVisibility(View.VISIBLE);
        }
        else {
            //mLvMenus.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (RELOAD_DIALPADS == msg.what) {
            loge("handleMessage   RELOAD_DIALPADS ");
            reloadDialpadItems();
            
        }
        /*else if (PROCESS_OUTGOING_CALL == msg.what) {
            processOutgoingCall();
        }*/
        
        return true;
    }
    
    @Override
    public void onDataEvent(PimEngine.DataEvent event, int state) {
        loge("onDataEvent   event=" + event + ", state = " + state);
        if ((PimEngine.DataEvent.LOAD_DIALPADS == event)
                && (PimEngine.DataEventListener.LOAD_DATA_DONE == state)) {
            //reloadDialpadItems();
            log("LOAD_DIALPADS");
            requestReloadingDialpads();
        }
        else if (PimEngine.DataEvent.CONTACTS_CHANGED == event) {
            mPhotoLoader.clear();
        }
    }

    private void deleteCallLog(DialpadItem dialpadItem) {
        if (dialpadItem == null) {
            return ;
        }
        
        String number = dialpadItem.getNumber();
        if (!TextUtils.isEmpty(number)) {
            if (number.length() < CommonMethod.MIN_NUMBER_LENGTH_ALLOWED_TO_ADD_IP) {
                getContentResolver().delete(Calls.CONTENT_URI, ("number='" + number + "'"), null);
            }
            else {
            	//add by zenghuaying 2012.6.28 for bug 7961
            	if(CommonMethod.isStartWithIpPrefix(this, number)){
            		number = number.substring(5);
            	}
                getContentResolver().delete(Calls.CONTENT_URI, ("number LIKE '%" + number + "'"), null);
            }
        }
    }

    public void openSearcher() {
        Intent searchLogIntent = new Intent("com.lewa.intent.action.SEARCH_LOG");
        startActivity(searchLogIntent);
    }

    public void categorizeCallLog(int logType) {
        if (null != mItemAdapter) {
            final List<DialpadItem> logGroups = PimEngine.getInstance(this).loadDialpadItems(false);

            final int count = logGroups.size();
            if (count > 0) {
                final ArrayList<DialpadItem> newValues = new ArrayList<DialpadItem>(count);
                for (int i = 0; i < count; i++) {
                    final DialpadItem item = logGroups.get(i);
                    if ((DialpadView.CALLLOG_TYPE_ALL == logType || item.hasCallType(logType))
                            && DialpadItem.CALLLOG_TYPE == item.getType()) {
                        //add by zenghuaying fix bug #8908
                        if(item.getContactId() == 0){
                            item.setName(null);
                        }
                        //add end
                        newValues.add(item);
                    }
                }
                mItemAdapter.setIsShowInFilterList(false);//add by zenghuaying
                mItemAdapter.setFilterCallType(logType);
                mItemAdapter.update(newValues, false);
                loge("categorizeCallLog  adapter update");
            }

            if (Calls.OUTGOING_TYPE == logType) {
                setLogsEmptyHint(getResources().getText(R.string.outgoing_logs_empty));
            }
            else if (Calls.INCOMING_TYPE == logType) {
                setLogsEmptyHint(getResources().getText(R.string.incoming_logs_empty));
            }
            else if (Calls.MISSED_TYPE == logType){
                setLogsEmptyHint(getResources().getText(R.string.missed_logs_empty));
            }
            else {
                setLogsEmptyHint(getResources().getText(R.string.call_logs_empty));
            }
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

    public void cancelMissedCallNotification() {
        log("cancelMissedCallNotification");
        PimEngine.getInstance(this).setCallLogsRead();
        CommonMethod.cancelMissedCallNotification(this);
    }

    public void resetToInitialStatus() {
        log("resetToInitialStatus");
        //mDialpad.resetToInitialStatus();
    }
    
    public void setNumber(String number) {
        log("setNumber   number=" + number);
        mDialpad.setNumber(number);
        mDialpad.show();
    }

    private class DelCallLogItemListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                //final DialpadItem dialpadItem = mItemAdapter.getItem(mContextMenuIdx);
                //List<DialpadItem> items = mItemAdapter.getListData();
                //items.remove(dialpadItem);
                //mItemAdapter.update(items, false);
                if (mDialpadItem != null && DialpadItem.CALLLOG_TYPE == mDialpadItem.getType()) {
                    List<DialpadItem> items = mItemAdapter.getListData();
                    items.remove(mDialpadItem);
                    mItemAdapter.update(items, false);
                }
                deleteCallLog(mDialpadItem);
            }
        }
    }
    

    private class DialpadMenuItem {
        int mIconResId;
        int mTitleResId;

        public DialpadMenuItem(int iconResId, int titleResId) {
            mIconResId  = iconResId;
            mTitleResId = titleResId;
        }
    }

    private class DialpadMenusAdapter extends ListBaseAdapter<DialpadMenuItem> {
        public DialpadMenusAdapter(List<DialpadMenuItem> items) {
            super(items);
        }

        @Override
        protected View createItemView(DialpadMenuItem item, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(DialpadActivity.this);
            return layoutInflater.inflate(R.layout.dialpad_menu_entry, null);
        }

        @Override
        protected void bindItemView(DialpadMenuItem item, int position, View itemView) {
            ItemViewHolder viewHolder = (ItemViewHolder )getViewHolder(itemView, item);
            viewHolder.mMenuIcon.setImageResource(item.mIconResId);
            viewHolder.mMenuTitle.setText(item.mTitleResId);
        }

        @Override
        protected Object createViewHolder(View itemView, DialpadMenuItem item) {
            ItemViewHolder viewHolder = new ItemViewHolder();
            viewHolder.mMenuIcon  = (ImageView )itemView.findViewById(R.id.img_dp_menu_icon);
            viewHolder.mMenuTitle = (TextView )itemView.findViewById(R.id.txt_dp_menu_title);
            return viewHolder;
        }


        private class ItemViewHolder {
            ImageView mMenuIcon;
            TextView  mMenuTitle;
        }
    }


    /*private class OutgoingCallReceiver extends BroadcastReceiver {
        private OutgoingCallReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            mOutgoingNumber = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
            mOutgoingNumber = CommonMethod.stripNumberPrefix(DialpadActivity.this, mOutgoingNumber);
            mDialpad.setNumber(null);
            mDialpad.showNumberUI();
            //log("OutgoingCallReceiver: outgoingNumber=" + mOutgoingNumber);
        }
    }*/
    

    /*private class CallListener extends PhoneStateListener {
        private CallListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            //log("CallListener: state=" + state + " incomingNumber=" + incomingNumber);
            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                if (!TextUtils.isEmpty(mOutgoingNumber)) {
                    mHandler.sendEmptyMessageDelayed(PROCESS_OUTGOING_CALL, 1000);
                }
            }
            else {
                mOutgoingNumber = null;
            }
        }
    }*/
    
    private class OptionsMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                PimEngine.getInstance(DialpadActivity.this).deleteCallLogs((long[] )null);
                if (null != mItemAdapter) {
                    mItemAdapter.update(null, false);
                }
            }
        }
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
    
    public void noViewPagerScroll() {
    	 if (null != mActivityResultBridge) {

             mActivityResultBridge.handleActivityEvent(
                     this,
                     ActivityResultBridge.EVT_ACTIVITY_ON_CREATE_CONTEXT_MENU,
                     null);
         }
    }

    void loge(String msg) {
        if (DBG_LEVEL > 0) {
            Log.e(TAG, msg);
        }
    }

    void log(String msg) {
        if (DBG_LEVEL > 2) {
            Log.i(TAG, msg);
        }
    }
    
}
