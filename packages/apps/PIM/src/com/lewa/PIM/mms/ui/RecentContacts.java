package com.lewa.PIM.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.PIM.mms.transaction.MessagingNotification;
import com.lewa.PIM.mms.ui.ConversationList.DeleteThreadListener;
import com.lewa.PIM.util.CommonMethod;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecentContacts extends LinearLayout{
		
	private static final int THREAD_LIST_QUERY_TOKEN       = 1900;
	private static final int CALL_LOG_LIST_QUERY_TOKEN     = 1901;	
	
	private static final int TYPE_CALL_LOG     = 0;	
	private static final int TYPE_MESSAGE      = 1;	
	
	private static final int RECENT_CONTACTS_MAX_COUNT = 6;

	private LinearLayout mRecipientFirst;
	private LinearLayout mRecipientSecond;
	
	private static final String TAG = "RecentContacts";
	String[] callLogProjection = 
							new String[]{CallLog.Calls.NUMBER,
                    					CallLog.Calls.CACHED_NAME,
                    					CallLog.Calls.TYPE, 
                    					CallLog.Calls.DATE};
	
	private ThreadListQueryHandler mQueryHandler;
	private Context mRecentContext;
	
	private HashMap<String, RecentContactsItem> mMessageMap;
	private HashMap<String, RecentContactsItem> mCallLogMap;
	private ArrayList<RecentContactsItem> mRecentContactsMap;	
	private HashMap<String, TextView> mTextViewMap;
	
	private boolean mIsHdip = false;
	private boolean mMessageQury = false;
	private boolean mCallLogQury = false;
	private RecentContacts mRecentContacts;
	
	public RecentContacts(Context context, AttributeSet attrs) {
		super(context, attrs);
		mQueryHandler = new ThreadListQueryHandler(context.getContentResolver());
		mRecentContext = context;
		mRecentContacts = this;
	}

	public RecentContacts(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    	mRecipientFirst = (LinearLayout)findViewById(R.id.recipient_first);
    	mRecipientSecond = (LinearLayout)findViewById(R.id.recipient_second);
    	TextView recipientTextView;
    	
    	mTextViewMap = new HashMap<String, TextView>();
    	mRecentContactsMap = new ArrayList();
    	mMessageMap = new HashMap<String, RecentContactsItem>();
    	mCallLogMap = new HashMap<String, RecentContactsItem>();

    	recipientTextView = (TextView)findViewById(R.id.recipient_first_1);
    	mTextViewMap.put("0", recipientTextView);
    	
    	recipientTextView = (TextView)findViewById(R.id.recipient_first_2);
    	mTextViewMap.put("1", recipientTextView);
    	
    	recipientTextView = (TextView)findViewById(R.id.recipient_first_3);
    	mTextViewMap.put("2", recipientTextView);
    	
    	recipientTextView = (TextView)findViewById(R.id.recipient_second_1);
    	mTextViewMap.put("3", recipientTextView);
    	
    	recipientTextView = (TextView)findViewById(R.id.recipient_second_2);
    	mTextViewMap.put("4", recipientTextView);
    	
    	recipientTextView = (TextView)findViewById(R.id.recipient_second_3);
    	mTextViewMap.put("5", recipientTextView);    	    	
    }
    
	public void setHide(int visibility){
		
		if (visibility == View.VISIBLE ) {
		    if (mRecentContactsMap.size() > 0) {
	            setVisibility(visibility);                
            }
		}else {
            setVisibility(visibility);            
        }
	}

	public void getRecentContacts(Context context){
		getRecentMessage(context);
		getRecentCallLog(context);
	}
	
	private void getRecentMessage(Context context){
		Conversation.startQueryForAll(mQueryHandler, THREAD_LIST_QUERY_TOKEN);
	}
	
	private void getRecentCallLog(Context context){
		
		String selection = "1=1) GROUP BY " + CallLog.Calls.NUMBER + ", (" + CallLog.Calls.NUMBER;
        
		mQueryHandler.startQuery(CALL_LOG_LIST_QUERY_TOKEN, 
									null, 
									CallLog.Calls.CONTENT_URI, 
									callLogProjection, 
									selection, 
									null, 
									CallLog.Calls.DEFAULT_SORT_ORDER);			
	}
	
    final class ThreadListQueryHandler extends AsyncQueryHandler {
        public ThreadListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
            case THREAD_LIST_QUERY_TOKEN:{
            	
            	if (cursor != null && cursor.getCount() > 0) {
            		int index = 0;
            		
            		while (cursor.moveToNext() && index < RECENT_CONTACTS_MAX_COUNT) {
            			
                    	Conversation conv = Conversation.from(mRecentContext, cursor);
                    	ContactList recipients = conv.getRecipients();
                    	                    	
                    	if (recipients != null && recipients.size() != 1) {
							continue;
						}
                    	
                    	if (!recipients.get(0).existsInDatabase()) {
							continue;
						}
                    	
                    	int iMessagCount = mMessageMap.size();
                    	boolean existence = false;
                    	
                        for (int iIndex = 0; iIndex < iMessagCount; iIndex++) {
                            RecentContactsItem item = mMessageMap.get(iIndex + "");              
                            if (item.contactsId == recipients.get(0).getPersonId()) {
                                existence = true;
                                break;
                            }                            
                        }
                        
                        if (existence) {
                            continue;
                        }
                    	
                    	RecentContactsItem item = new RecentContactsItem();
                    	
                    	item.contactsId = recipients.get(0).getPersonId();
                    	item.name = recipients.formatNames(", ");
                    	item.numberArrayList.add(MessageUtils.fixPhoneNumber(recipients.get(0).getNumber()));
                    	item.threadId = conv.getThreadId();
                    	item.type = TYPE_MESSAGE;
                    	item.date = conv.getDate();
                    	
                    	mMessageMap.put(index + "", item);
                    	index++;							
					}
				}
            	mMessageQury = true;
            }
            break;
                
            case CALL_LOG_LIST_QUERY_TOKEN:{
            	
            	if (cursor != null && cursor.getCount() > 0) {
            		int index = 0;
            		
            		while (cursor.moveToNext() && index < RECENT_CONTACTS_MAX_COUNT) {
            			
            			RecentContactsItem item = new RecentContactsItem();                    	
                    	String phoneNumber = cursor.getString(0);
                    	
                    	boolean isContacts = false;
                    	Uri phonUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                    	
                        Cursor phonecursor = null;
                        ContentResolver cr = mRecentContext.getContentResolver();

                        try {
                        	phonecursor = cr.query(phonUri,
						                        new String[]{PhoneLookup._ID, PhoneLookup.DISPLAY_NAME},
						                        null,
						                        null,
						                        null);
                            
                            if (phonecursor.moveToFirst()) {
                                item.contactsId = phonecursor.getLong(0);
                            	item.name = phonecursor.getString(1);
								isContacts = true;
							}
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            if (null != phonecursor) {
                            	phonecursor.close();
                            }
                        }
                        
                        if (isContacts == false) {
							continue;
						}
                            
                        item.type = TYPE_CALL_LOG;
                    	item.numberArrayList.add(MessageUtils.fixPhoneNumber(phoneNumber));
                    	item.date = cursor.getLong(3);
                    	
                    	mCallLogMap.put(index + "", item);
                    	index++;
					}
				}
            	mCallLogQury = true;
            }
            break;
	            
            default:
                Log.e(TAG, "onQueryComplete called with unknown token " + token);
            }   
            
            updataRecentContacts();
        }
    }
    
    private class RecentContactsItem{
    	
    	public int type;
    	public long threadId;
    	public long date;
    	public String name;
    	public ArrayList<String> numberArrayList;
    	public boolean isAdd;
    	public long contactsId;
    	
    	RecentContactsItem(){
    		type = 1;
    		threadId = -1;
    		name = "";
    		isAdd = false;
    		numberArrayList = new ArrayList<String>();
    		contactsId = 0;
    	}
    }
    
    public void clearRecentContacts(){
    	mMessageMap.clear();
    	mCallLogMap.clear();
    }    
    
    private void updataRecentContacts(){
    	
    	if (!mCallLogQury || !mMessageQury) {
			return ;
		}
    	
    	mRecentContactsMap.clear();
    	sortContacts();
    	int icount = mRecentContactsMap.size();
    	
    	for (int iIndex = 0; iIndex < icount; iIndex++) {
			RecentContactsItem item = mRecentContactsMap.get(iIndex);	             
			
			if (item != null) {
				TextView text = mTextViewMap.get(iIndex + "");			
				text.setTag(item);
				text.setText(item.name);
				text.setOnClickListener(mRecentOnClickListener);            							
			}
		}
    	
    	if (icount <= 3 || !mIsHdip) {
    		mRecipientSecond.setVisibility(View.GONE);
		}else {
    		mRecipientSecond.setVisibility(View.VISIBLE);
		}
    	
    	if (icount == 0) {
    		this.setVisibility(View.GONE);
		}else {
			this.setVisibility(View.VISIBLE);
		}
    }
    
    public void setHideMdip(boolean b){
    	mIsHdip = b;   	
    }
    
    private OnClickListener mRecentOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			RecentContactsItem item = (RecentContactsItem)v.getTag();
			
            InputMethodManager inputMethodManager =
                (InputMethodManager)mRecentContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
			showContactInfoDialog(item);
		}
	};
	
	private void sortContacts(){
    	int iMessagCount = mMessageMap.size();
    	int iCallLogCount = mCallLogMap.size();    	    	
    	
		for (int i = 0; i < iCallLogCount; i++) {			
    		RecentContactsItem callLogItem = mCallLogMap.get(i + "");
			String callLogNumber = callLogItem.numberArrayList.get(0);
			
	    	for (int iIndex = 0; iIndex < iMessagCount; iIndex++) {
				RecentContactsItem item = mMessageMap.get(iIndex + "");	             
				String number = item.numberArrayList.get(0);
				
				if (callLogNumber.equals(number)) {
					item.isAdd = true;
					mRecentContactsMap.add(item);
					break;
				}				
			}
		}  

		if (mRecentContactsMap.size() < 6) {			
	    	for (int iIndex = 0; iIndex < iMessagCount; iIndex++) {
				RecentContactsItem item = mMessageMap.get(iIndex + "");	             
				
				if (item.isAdd == false) {
					item.isAdd = true;
					mRecentContactsMap.add(item);
					if (mRecentContactsMap.size() >= 6) {
						break;
					}
				}				
			}
		}
	}
	
    private void showContactInfoDialog(RecentContactsItem item) {

        final Cursor entryCursor = mRecentContext.getContentResolver().query(Data.CONTENT_URI,
                new String[] {
                    Data._ID,
                    Data.DATA1,
                    Data.DATA2,
                    Data.DATA3,
                    Data.MIMETYPE
                },
                Data.CONTACT_ID + "=? AND ("
                        + Data.MIMETYPE + "=?)",
                new String[] {
                    item.contactsId + "",
                    CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                },
                Data.DATA2
            );

        ContactEntryAdapter adapter = new ContactEntryAdapter(mRecentContext, entryCursor);

        AlertDialog.Builder builder = new AlertDialog.Builder(mRecentContext);
        builder.setIcon(R.drawable.ic_dialog_attach);
        builder.setTitle(item.name);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                entryCursor.moveToPosition(which);
                String value = entryCursor.getString(entryCursor.getColumnIndex(Data.DATA1));
                NewMessageComposeActivity.SetRecipientsEditorText(value);
                dialog.dismiss();
                mRecentContacts.setHide(View.GONE);
            }
        });
        builder.show();
    }
}
