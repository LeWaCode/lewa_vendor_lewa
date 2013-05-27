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

import android.R.integer;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.lewa.os.ui.ActivityResultBridge;
import com.lewa.os.ui.ActivityResultBridge.ActivityResultReceiver;
import com.lewa.os.ui.ListBaseActivity;
import com.lewa.os.ui.PendingContentLoader;
import com.lewa.os.util.Util;
import com.lewa.os.view.ListBaseAdapter;
import com.lewa.PIM.calllog.data.CallLog;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.contacts.ui.EditContactActivity;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.mms.ui.MessageUtils;
import com.lewa.PIM.sim.SimCard;
import com.lewa.PIM.ui.DetailEntry;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.R;

public final class LogDetailActivity extends ListBaseActivity
        implements View.OnClickListener,
                ActivityResultReceiver,
                PendingContentLoader ,
                PimEngine.DataEventListener{
    private static final String TAG = "LogDetailActivity";
    
    private ArrayList<CallLog> mLogDetails;
    private LogDetailAdapter mAdapter;
    private int mContextMenuIdx = -1;
    private long mContactId = -1;
    private String mNumber; 
    private String mName;

    private ActivityResultBridge mActivityResultBridge = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        Log.d(TAG, "onCreate: bDelayLoadContent=" + String.valueOf(bDelayLoadContent));
        if (!bDelayLoadContent) {
            setContentView(R.layout.calllog_detail);
            setTitle(R.string.call_log);
            setupListView();
        }
	  PimEngine pimEng = PimEngine.getInstance(this);
	  pimEng.addDataListenner(this);
    }

      @Override
    public void onDataEvent(PimEngine.DataEvent event, int state) {
       // Log.d(TAG,"onDataEvent   event=" + event + ", state = " + state +"mNumber = " + mNumber );
        if ((PimEngine.DataEvent.LOAD_DIALPADS == event)
             && (PimEngine.DataEventListener.LOAD_DATA_DONE == state)
             &&(mAdapter != null)) {
               if (mContactId > 0) {
                        setupLogsByContactId(mContactId);
                }
		  else if(!TextUtils.isEmpty(mName))  {
		  	setupLogsByContactName(mName);
		  }
		  else if(!TextUtils.isEmpty(mNumber))  {
		  	 setupLogsByContactNumber(mNumber);		 
		  }
		  mAdapter.update(mLogDetails, true);
	  }	
    }
	  
    @Override
    protected void onDestroy() {
        mActivityResultBridge = null;
	 PimEngine pimEng = PimEngine.getInstance(this);
	 pimEng.removeDataListenner(this);
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_dialpad:
                CommonMethod.openDialpad(this, null);
                break;
                
            default:
                break;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        CallLog cl = (CallLog )l.getItemAtPosition(position);
        String name = cl.getName();
        String number = cl.getNumber();
        Log.d(TAG, "onListItemClick: " + position + " name=" + name + " number=" + number);
        CommonMethod.call(this, number);
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

        CallLog cl = mAdapter.getItem(menuInfo.position);
        long logId = cl.getId();
        String name = cl.getName();
        String number = cl.getNumber();
        Log.d(TAG, "onCreateContextMenu: id=" + logId + " name=" + name + " number=" + number);

        menu.setHeaderTitle(R.string.alert_dialog_title);
        menu.add(0, R.id.menu_call_contact, 0, R.string.call_contact);
        menu.add(0, R.id.menu_send_message, 0, R.string.send_message);
        menu.add(0, R.id.menu_delete_log,   0, R.string.delete_call_log);

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

        CallLog cl = mAdapter.getItem(menuInfo.position);
        long logId = cl.getId();
        String name = cl.getName();
        String number = cl.getNumber();
        Log.d(TAG, "onContextItemSelected: id=" + logId + " name=" + name + " number=" + number);
        switch (item.getItemId()) {
        case R.id.menu_call_contact:
            CommonMethod.call(this, number);
            break;
            
        case R.id.menu_send_message:
            CommonMethod.sendMessage(this, number, null);
            break;
            
        case R.id.menu_delete_log:
            CommonMethod.showConfirmDlg(this,
                    R.string.delete_call_log_confirm,
                    R.string.alert_dialog_title,
                    new ContextMenuDelListener());
            break;
            
        default:
            break;
        }
        
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((null != mLogDetails) && (mLogDetails.size() > 0)) {
            getMenuInflater().inflate(R.menu.log_detail_options_menu, menu);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ((null != mLogDetails) && (mLogDetails.size() > 0)) {
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
                    R.string.clear_call_log_confirm,
                    R.string.alert_dialog_title,
                    new OptionsMenuDelListener());
        }
        return true;
    }

    @Override
    public void loadContent() {
        Log.d(TAG, "loadContent");
        getIntent().putExtra("delayloadcontent", false);
        setContentView(R.layout.calllog_detail);
        //setTitle(R.string.call_log);
        setupListView();
    }

    @Override
    public void registerActivityResultBridge(ActivityResultBridge bridge) {
        //Log.d(TAG, "registerActivityResultBridge");
        mActivityResultBridge = bridge;
    }

    @Override
    public void handleActivityResult(ActivityResultBridge.ActivityResultReceiver realReceiver,
            int requestCode, int resultCode, Intent intent) {
        //Log.d(TAG, "handleActivityResult");
        if (null != mAdapter) { //the this object has initiated
            if ((DetailEntry.REQUEST_EDIT_CONTACT == requestCode)
                    && (Activity.RESULT_OK == resultCode)) {
                Uri contactLookupUri = intent.getData();
                if (null != contactLookupUri) {
                    long contactId = ContentUris.parseId(contactLookupUri);
			mContactId = contactId;
                    if (contactId > 0) {
                        setupLogsByContactId(contactId);
                        mAdapter.update(mLogDetails, true);
                    }
                }
            }
            else if (DetailEntry.REQUEST_ON_NEW_INTENT == requestCode) {
                setIntent(intent);
                long contactId = intent.getLongExtra("contact_id", 0);
		  mContactId = contactId;
                if (contactId > 0) {
                    setupLogsByContactId(contactId);
                }
                else {
                    String number = intent.getStringExtra("number");
                    setupLogsByNumber(number);
                }
                mAdapter.update(mLogDetails, true);
            }
        }
    }

    private void setupListView() {
        Intent startIntent = getIntent();
        String name      = startIntent.getStringExtra("name");
        String number    = startIntent.getStringExtra("number");
        long   contactId = startIntent.getLongExtra("contact_id", 0);
	 mContactId = contactId;
	 if(number != null ) { 
	 	mNumber = new String(number);
	 }
         if(name != null ){
	 	mName = new String(name);
	 }
	 
        init(contactId, name, number);
	

        mAdapter = new LogDetailAdapter(this, mLogDetails);
        setListAdapter(mAdapter);
        ListView lv = getListView();
        registerForContextMenu(lv);
    }

    private void init(long contactId, String name, String number) {
        mLogDetails = new ArrayList<CallLog>(12); //with 12 initial capacity

        List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
        int groupCount = 0;
        CallLogGroup clGroup = null;
        if (contactId > 0) {
            groupCount = logGroups.size();
            for (int i = 0; i < groupCount; ++i) {
                clGroup = logGroups.get(i);
                if (clGroup.getContactId() == contactId) {
                    final int clCount = clGroup.getSize();
                    for (int j = 0; j < clCount; ++j) {
                        mLogDetails.add(clGroup.getLog(j));
                    }
                }
            }
        }
        else if (!TextUtils.isEmpty(name)) {
            groupCount = logGroups.size();
            for (int i = 0; i < groupCount; ++i) {
                clGroup = logGroups.get(i);
                final String nameCmp = clGroup.getName();
                if (name.equals(nameCmp)) {
                    final int clCount = clGroup.getSize();
                    for (int j = 0; j < clCount; ++j) {
                        mLogDetails.add(clGroup.getLog(j));
                    }
                }
            }
        }
        else {
            if (!TextUtils.isEmpty(number)) {
                String chinaCallCode = CommonMethod.getChinaCallCode();
                if (number.startsWith(chinaCallCode)) {
                    number = number.substring(chinaCallCode.length());
                }
              //add by zenghuaying
                if(CommonMethod.isStartWithIpPrefix(this, number)){
                    number = number.substring(5);
                }
                //add end
                groupCount = logGroups.size();
                if (Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())) {
                    for (int i = 0; i < groupCount; ++i) {
                        clGroup = logGroups.get(i);
                        final String numberCmp = clGroup.getNumber();
                        PimEngine.getInstance(this);
                        if (number.equals(numberCmp)
                                || Util.isEmergencyNumber(numberCmp, PimEngine.getEmergencyNumbers())) {
                            final int clCount = clGroup.getSize();
                            for (int j = 0; j < clCount; ++j) {
                                mLogDetails.add(clGroup.getLog(j));
                            }
                        }
                    }
                }
                else {
                    for (int i = 0; i < groupCount; ++i) {
                        clGroup = logGroups.get(i);
                        final String numberCmp = clGroup.getNumber();
                        //modify by zenghuaying fix bug #8367
                        //if (number.equals(numberCmp)) {
                        if (numberCmp.contains(number)) {
                            final int clCount = clGroup.getSize();
                            for (int j = 0; j < clCount; ++j) {
                                mLogDetails.add(clGroup.getLog(j));
                            }
                        }
                    }
                }
            }
        }

        //mAdapter = new LogDetailAdapter(this, mLogDetails);
        //setListAdapter(mAdapter);
        //ListView lv = getListView();
        //registerForContextMenu(lv);
    }

    private long[] getLogIds() {
        int count = mLogDetails.size();
        long[] logIds = new long[count];
        for (int i = 0; i < count; ++i) {
            logIds[i] = mLogDetails.get(i).getId();
        }
        return logIds;
    }

    private void setupLogsByNumber(String number) {
        Log.i(TAG, "setupLogsByNumber: " + number);
        
        if (null == mLogDetails) {
            mLogDetails = new ArrayList<CallLog>(12); //with 12 initial capacity
        }
        else {
            mLogDetails.clear();
        }

        if (TextUtils.isEmpty(number)) {
            return;
        }

        Cursor logCursor = null;
        String ipPrefix = "";//CommonMethod.getIpPrefix(this, SimCard.GEMINI_SIM_1);
        String chinaCallCode = CommonMethod.getChinaCallCode();
        ArrayList<String> numbersArray = new ArrayList<String>(1); //with 1 initial capacity
        
        if (Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())) {
            String[] emergencyNumbers = PimEngine.getEmergencyNumbers();
            if ((null != emergencyNumbers) && (emergencyNumbers.length > 0)) {
                for (String emergencyNumber : emergencyNumbers) {
                    numbersArray.add(emergencyNumber);
                }
            }
            else {
                numbersArray.add(number);
                numbersArray.add("110");
                numbersArray.add("112");
                numbersArray.add("119");
                numbersArray.add("120");
                numbersArray.add("911");
            }
        }
        else {
            numbersArray.add(PhoneNumberUtils.stripSeparators(number));
        }
        
        try {
            String strWhere = Calls.NUMBER + " IN("
                    + CommonMethod.numberArrayToInClauseString(numbersArray, chinaCallCode, ipPrefix)
                    + ")";
            logCursor = getContentResolver().query(Calls.CONTENT_URI,
                    CallLog.PROJECTION,
                    strWhere,
                    null,
                    Calls.DEFAULT_SORT_ORDER);
            if ((null != logCursor) && (logCursor.getCount() > 0)) {
                String[] arrPrefix;
                if (TextUtils.isEmpty(ipPrefix)) {
                    arrPrefix = new String[1];
                    arrPrefix[0] = chinaCallCode;
                }
                else {
                    arrPrefix = new String[2];
                    arrPrefix[0] = ipPrefix;
                    arrPrefix[1] = chinaCallCode;
                }
                while (logCursor.moveToNext()) {
                    CallLog cl = CallLog.create(logCursor);
                    cl.removeNumberPrefix(arrPrefix);
                    mLogDetails.add(cl);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != logCursor) {
                logCursor.close();
                logCursor = null;
            }
        }
    }

    private void setupLogsByContactName(String name) {
    		List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
		int groupCount = 0;
        	CallLogGroup clGroup = null;	

		if (null == mLogDetails) {
			mLogDetails = new ArrayList<CallLog>(12); //with 12 initial capacity
		}
		else {
			mLogDetails.clear();
		}
		if (!TextUtils.isEmpty(name)) {
		    groupCount = logGroups.size();
		    for (int i = 0; i < groupCount; ++i) {
		        clGroup = logGroups.get(i);
		        final String nameCmp = clGroup.getName();
		        if (name.equals(nameCmp)) {
		            final int clCount = clGroup.getSize();
		            for (int j = 0; j < clCount; ++j) {
		                mLogDetails.add(clGroup.getLog(j));
		            }
		        }
		    }
	   	}		
    		
   }

    private void setupLogsByContactNumber(String number) {
//    	List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
//		int groupCount = 0;
        CallLogGroup clGroup = null;	
		if (null == mLogDetails) {
			mLogDetails = new ArrayList<CallLog>(12); //with 12 initial capacity
		}
		else {
			mLogDetails.clear();
		}
			
    	 if (!TextUtils.isEmpty(number)) {
            String chinaCallCode = CommonMethod.getChinaCallCode();
            if (number.startsWith(chinaCallCode)) {
                number = number.substring(chinaCallCode.length());
            }
            //add by zenghuaying
            if(CommonMethod.isStartWithIpPrefix(this, number)){
                number = number.substring(5);
            }
            //add end
            //groupCount = logGroups.size();
            if (Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())) {
                
                List<CallLogGroup> logGroups = PimEngine.getInstance(this).loadCallLogs(false);
                int groupCount = 0;
                groupCount = logGroups.size();
                
                for (int i = 0; i < groupCount; ++i) {
                    clGroup = logGroups.get(i);
                    final String numberCmp = clGroup.getNumber();
                    PimEngine.getInstance(this);
                    if (number.equals(numberCmp)
                            || Util.isEmergencyNumber(numberCmp, PimEngine.getEmergencyNumbers())) {
                        final int clCount = clGroup.getSize();
                        for (int j = 0; j < clCount; ++j) {
                            mLogDetails.add(clGroup.getLog(j));
                        }
                    }
                }
            }
            else {
              //modify by zenghuaying fix bug #8367
    //                    for (int i = 0; i < groupCount; ++i) {
    //                        clGroup = logGroups.get(i);
    //                        final String numberCmp = clGroup.getNumber();
    //                        //if (number.equals(numberCmp)) {
    //                        if (numberCmp.contains(number)) {
    //                            final int clCount = clGroup.getSize();
    //                            for (int j = 0; j < clCount; ++j) {
    //
    //                                mLogDetails.add(clGroup.getLog(j));
    //                            }
    //                        }
    //                    }
                
            Cursor logCursor = null;
            try {
                ArrayList<String> numbersArray = new ArrayList<String>(1);
                numbersArray.add(number);
                
                String numberStr = CommonMethod.numberArrayToLikeClauseString(numbersArray, chinaCallCode, this);
                String strWhere = Calls.NUMBER + numberStr;
                //end
                logCursor = getContentResolver().query(Calls.CONTENT_URI,
                        CallLog.PROJECTION,
                        strWhere,
                        null,
                        Calls.DEFAULT_SORT_ORDER);
                
                if ((null != logCursor) && (logCursor.getCount() > 0)) {
                    String[] arrPrefix;
                        arrPrefix = new String[1];
                        arrPrefix[0] = chinaCallCode;
                        
                    while (logCursor.moveToNext()) {
                        CallLog cl = CallLog.create(logCursor);
                        cl.removeNumberPrefix(arrPrefix);
                        mLogDetails.add(cl);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (null != logCursor) {
                    logCursor.close();
                    logCursor = null;
                }
            }
          //modify end
            }
	 	}
   }

    private void setupLogsByContactId(long contactId) {
        Log.i(TAG, "setupLogsByContactId: " + contactId);
        
        Cursor cursor = null;
        ArrayList<String> numbersArray = null;
        String ipPrefix = "";//CommonMethod.getIpPrefix(this, SimCard.GEMINI_SIM_1);
        String chinaCallCode = CommonMethod.getChinaCallCode();
        try {
            String strWhere = CommonDataKinds.Phone.CONTACT_ID + "=" + String.valueOf(contactId)
                    + ") GROUP BY " + Contacts.DISPLAY_NAME + ", (" + CommonDataKinds.Phone.NUMBER;
            cursor = getContentResolver().query(
                    CommonDataKinds.Phone.CONTENT_URI,
                    new String[] {Contacts.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.CONTACT_ID},
                    strWhere,
                    null,
                    null);
            if ((null != cursor) && (cursor.getCount() > 0)) {
                String name = null;
                String number = null;
                numbersArray = new ArrayList<String>(3); //with 3 initial capacity
                while (cursor.moveToNext()) {
                    name = cursor.getString(0);
                    number = PhoneNumberUtils.stripSeparators(cursor.getString(1));
                    Log.d(TAG, "name=" + name + "number="+ number);
                    if (!TextUtils.isEmpty(number)) {
                        numbersArray.add(number);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }

        if ((null == numbersArray) || (0 == numbersArray.size())) {
            return;
        }

        if (null == mLogDetails) {
            mLogDetails = new ArrayList<CallLog>(12); //with 12 initial capacity
        }
        else {
            mLogDetails.clear();
        }

        Cursor logCursor = null;
        try {
		//modify by zenghuaying fix bug #9947
//            String strWhere = Calls.NUMBER + " IN("
//                    + CommonMethod.numberArrayToInClauseString(numbersArray, chinaCallCode, ipPrefix)
//                    + ")";
            String numberStr = CommonMethod.numberArrayToLikeClauseString(numbersArray, chinaCallCode, this);
            String strWhere = Calls.NUMBER + numberStr;
            
            //end
            logCursor = getContentResolver().query(Calls.CONTENT_URI,
                    CallLog.PROJECTION,
                    strWhere,
                    null,
                    Calls.DEFAULT_SORT_ORDER);
            if ((null != logCursor) && (logCursor.getCount() > 0)) {
                String[] arrPrefix;
                if (TextUtils.isEmpty(ipPrefix)) {
                    arrPrefix = new String[1];
                    arrPrefix[0] = chinaCallCode;
                }
                else {
                    arrPrefix = new String[2];
                    arrPrefix[0] = ipPrefix;
                    arrPrefix[1] = chinaCallCode;
                }
                while (logCursor.moveToNext()) {
                    CallLog cl = CallLog.create(logCursor);
                    cl.removeNumberPrefix(arrPrefix);
                    mLogDetails.add(cl);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != logCursor) {
                logCursor.close();
                logCursor = null;
            }
        }
    }


    private class ContextMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                final CallLog cl = mAdapter.getItem(mContextMenuIdx);
                long logId = cl.getId();
                PimEngine.getInstance(LogDetailActivity.this).deleteCallLog(logId);
                mLogDetails.remove(cl);
                mAdapter.update(mLogDetails, false);
            }
        }
    }


    private class OptionsMenuDelListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_POSITIVE == which) {
                PimEngine.getInstance(LogDetailActivity.this).deleteCallLogs(getLogIds());
                //LogDetailActivity.this.finish();
                mLogDetails.clear();
                mAdapter.update(mLogDetails, false);
            }
        }
    }
    

    private class LogDetailAdapter extends ListBaseAdapter<CallLog> {
        private Context mContext;
        private SimpleDateFormat mTimeFormat;

        public LogDetailAdapter(Context context, List<CallLog> logs) {
            super(logs);
            mContext = context;
        }

        @Override
        protected View createItemView(CallLog cl, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            return layoutInflater.inflate(R.layout.calllog_detail_entry, null);
        }

        @Override
        protected void bindItemView(CallLog cl, int position, View itemView) {
            String number = cl.getNumber();
            //String date   = new SimpleDateFormat("MM/dd HH:mm").format(cl.getDate());
            int type      = cl.getType();
            long duration = cl.getDuration();

            String strDate;
            long timeStamp = cl.getDate().getTime();
            
//            if (DateUtils.isToday(timeStamp)) {
//                if (DateFormat.is24HourFormat(mContext)) {
//                    strDate = DateUtils.formatDateTime(
//                            mContext,
//                            timeStamp,
//                            (DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR));
//                }
//                else {
//                    if (null == mTimeFormat) {
//                        mTimeFormat = new SimpleDateFormat(mContext.getString(R.string.twelve_hour_time_format));
//                    }
//                    strDate = mTimeFormat.format(cl.getDate());
//                }
//            }
//            else {
//                strDate = DateUtils.formatDateTime(
//                        mContext,
//                        timeStamp,
//                        (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
//                if (DateFormat.is24HourFormat(mContext)) {
//                    strDate += DateUtils.formatDateTime(
//                            mContext,
//                            timeStamp,
//                            (DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR));
//                }
//                else {
//                    if (null == mTimeFormat) {
//                        mTimeFormat = new SimpleDateFormat(mContext.getString(R.string.twelve_hour_time_format));
//                    }
//                    strDate += mTimeFormat.format(cl.getDate());
//                }
//            }
            strDate = MessageUtils.formatTimeStampString( mContext, timeStamp, false);
            //strDate = CommonMethod.trim(strDate, new char[] {' ', '\u0020'});

            ItemViewHolder viewHolder = (ItemViewHolder )getViewHolder(itemView, cl);

            if (Calls.MISSED_TYPE == type) {
                viewHolder.mType.setImageResource(R.drawable.ic_log_missed);
            }
            else if (Calls.OUTGOING_TYPE == type) {
                viewHolder.mType.setImageResource(R.drawable.ic_log_out);
            }
            else if (Calls.INCOMING_TYPE == type) {
                viewHolder.mType.setImageResource(R.drawable.ic_log_in);
            }

            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                number = mContext.getResources().getString(R.string.unknown);
            }
            else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                number = mContext.getResources().getString(R.string.private_num);
            }
            else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                number = mContext.getResources().getString(R.string.payphone);
            }
            viewHolder.mDispNumber.setText(number);
            
            viewHolder.mDate.setText(strDate);
            viewHolder.mDuration.setText(CommonMethod.trim(formatDuration(duration), new char[] {' ', '\u0020'}));
        }

        @Override
        protected Object createViewHolder(View itemView, CallLog cl) {
            ItemViewHolder viewHolder = new ItemViewHolder();
            viewHolder.mDispNumber = (TextView )itemView.findViewById(R.id.txt_log_detail_displaynumber);
            viewHolder.mDate       = (TextView )itemView.findViewById(R.id.txt_log_detail_time);
            viewHolder.mDuration   = (TextView )itemView.findViewById(R.id.txt_log_detail_duration);
            viewHolder.mType       = (ImageView )itemView.findViewById(R.id.img_log_detail_type);
            //viewHolder.mDivider    = (ImageView )itemView.findViewById(R.id.img_log_detail_separator);
            //viewHolder.mMainArea   = itemView.findViewById(R.id.v_log_detail_item_main_area);
            return viewHolder;
        }

        private String formatDuration(long elapsedSeconds) {
            long minutes = 0;
            long seconds = 0;

            if (elapsedSeconds >= 60) {
                minutes = elapsedSeconds / 60;
                elapsedSeconds -= minutes * 60;
            }
            seconds = elapsedSeconds;

            return getString(R.string.callDetailsDurationFormat, minutes, seconds);
        }


        private class ItemViewHolder {
            TextView  mDispNumber;
            TextView  mDate;
            TextView  mDuration;
            ImageView mType;
            //ImageView mDivider;
            //View      mMainArea;
        }
    }
}
