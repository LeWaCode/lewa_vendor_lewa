package com.lewa.PIM.mms.ui;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.data.Contact;
import com.lewa.PIM.mms.data.ContactList;
import com.lewa.PIM.mms.ui.SearchContactsItem;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.common.ui.indexbar.IndexBar;
import com.lewa.os.common.ui.indexbar.Scroller_view;
import com.lewa.os.common.ui.indexbar.IndexBar.IndexBarOnClicker;
import com.lewa.os.util.ContactPhotoLoader;

import android.R.integer;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Roster;
import android.provider.ContactsContract.RosterData;
import android.support.lewa.widget.CursorAdapter;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ComposeSearchContactsActivity extends Activity 
				implements TextWatcher{
	
    private ListView mListView;
    private EditText mSearchText;
    private LinearLayout mEmpty;
    private TextView mDialogText;
    private ImageView mImgSearchDelStr;
    private ImageView mMessageIndexSwitching;    
    private ImageView mContactsSearchDone;
    private TextView mSearchbarTip;
    private final static int RESULT_FOR_IP_CALL = 180;
    
    private char mPrevLetter = Character.MIN_VALUE;    
    private boolean mShowing;
    private boolean mIsInit;
    private boolean mPhoneNumberMode;
    //private boolean mSwitching;
    private boolean isForIpCall;
    private boolean mUnSelectAll;
    
    private WindowManager mWindowManager;
    private SearchContactsListAdapter mAdapter;
    private SearchContactsHandler mQueryHandler;
	private Context mComposeSreachContext;
	private ContactPhotoLoader mPhotoLoader;
	private RemoveWindow mRemoveWindow = new RemoveWindow();
	private Handler mHandler = new Handler();
	private SearchYLContactsHandler mQueryYLHandler;
	private IndexBar indexBar;
	private ViewGroup vGroup;
	
	private HashMap<String, String> mPhotoList;
    private HashMap<String, String> mRecipientsMap;
    private HashMap<String, String> mSelectMap;
    private HashMap<String, String> mYlUserMap;
    private HashMap<String, String> mInitRecipientMap;
    private ArrayList<String> marrFirstLetter = new ArrayList<String>();
    private ArrayList<String> numberData = new ArrayList<String>();
    
    private static final String[] PROJECTION_PHONE = {
        Phone._ID,                  // 0
        Phone.CONTACT_ID,           // 1
        Phone.TYPE,                 // 2
        Phone.NUMBER,               // 3
        Phone.DISPLAY_NAME,         // 4
        Data.MIMETYPE, 				// 5
    };

    private static final String[] PROJECTION_EMAIL = {
        Email._ID,                  // 0
        Email.CONTACT_ID,           // 1
        Email.TYPE,                 // 2
        Email.DATA,                 // 3
        Phone.DISPLAY_NAME,         // 4
        Data.MIMETYPE, 				// 5
    };
    
    private static final String[] PROJECTION_YL_PHONE = {
    	RosterData._ID,             // 0
    	RosterData.PHOTO,           // 1
    	RosterData.EMAIL,			// 2
    	RosterData.ROSTER_USER_ID,  // 3
    	RosterData.DISPLAY_NAME,    // 4
    };
    
    private static final int MENU_SELECT_ALL          = 0;
    private static final int MENU_UNSELECIT_ALL       = 1;
    private static final int SEARCH_YL_CONTACTS_QUERY_TOKEN       = 0;
    
    private static final String SORT_ORDER = Contacts.SORT_KEY_PRIMARY;
//    private static final String SORT_ORDER = Contacts.TIMES_CONTACTED + " DESC,"
//    										+ Contacts.DISPLAY_NAME + "," + Phone.TYPE;
    private static final String SORT_ORDER_EMAIL = Contacts.TIMES_CONTACTED + " DESC,"
    										+ Contacts.DISPLAY_NAME + "," + Email.TYPE;
    
    
    @Override
    protected void onDestroy() {
    	mPhotoLoader.stop();
        if ((null != mWindowManager) && (null != mDialogText)) {
            mWindowManager.removeView(mDialogText);
        }
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	mPhotoLoader.resume();
        super.onResume();
    }
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_search_contacts);
        mSearchText = (EditText)findViewById(R.id.search_contacts_editor_text);
        mSearchText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mSearchText.addTextChangedListener(this);
        mListView = (ListView)findViewById(R.id.contacts_search_list);        
        mListView.setCacheColorHint(0);
        mImgSearchDelStr = (ImageView)findViewById(R.id.img_search_category_cancel);
        mImgSearchDelStr.setVisibility(View.GONE);
        mContactsSearchDone = (ImageView)findViewById(R.id.contacts_search_done);
        mContactsSearchDone.setOnClickListener(mDonButtonClickListener);
        mMessageIndexSwitching = (ImageView)findViewById(R.id.message_index_switching);
        mMessageIndexSwitching.setOnClickListener(mDonButtonClickListener);
        mSearchbarTip = (TextView)findViewById(R.id.searchbar_tip);
        mComposeSreachContext = this;
        mUnSelectAll = false;
        mImgSearchDelStr.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mSearchText.setText("");
				mImgSearchDelStr.setVisibility(View.GONE);
			}        	
        });
        
        mPhoneNumberMode = true;
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                
            	CheckBox checkBox = (CheckBox) v.findViewById(R.id.contacts_list_choice);
                checkBox.setChecked(!checkBox.isChecked());      
            }
        });
        
        
        mEmpty = (LinearLayout)findViewById(R.id.empty);         
        
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.activity_background);
        this.getWindow().setBackgroundDrawable(drawable);
        
        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialogText = (TextView) inflate.inflate(R.layout.contacts_list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_picture);
        mPhotoList = MessageUtils.getPhotoIdByContactId(getContentResolver());
        
        mSelectMap = new HashMap<String, String>();
        mRecipientsMap = new HashMap<String, String>();
        mInitRecipientMap = new HashMap<String, String>();
        //add by zenghuaying 2012.7.2
        Intent intent = getIntent();
        isForIpCall = intent.getBooleanExtra("isForIpCall", false);
        numberData  = intent.getStringArrayListExtra("numberList");
        getRecipients(isForIpCall);
        	
        //add end
        
        mQueryHandler = new SearchContactsHandler(getContentResolver());
        mAdapter = new SearchContactsListAdapter(this, null);                
        mListView.setAdapter(mAdapter);        
        
        mQueryYLHandler = new SearchYLContactsHandler(getContentResolver());
        mYlUserMap = new HashMap<String, String>();    	
    	if (MessageUtils.isImsSwitch(this)) {
            getYlAllUser();
		}
        
        mIsInit = true;
		Message msg = new Message();
		msg.obj = (Object)null;
		mQueryHandler.sendMessage(msg);
		
		mListView.setOnScrollListener(mScrollListener);
		
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mHandler.post(new Runnable() {

            public void run() {
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
        }});        
    }
    
    public class SearchContactsListAdapter extends CursorAdapter {


        public SearchContactsListAdapter(Context context, Cursor c) {
            super(context, c, false);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        	SearchContactsItem item = (SearchContactsItem)view;
        	item.bind(cursor, this);
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.compose_search_contacts_item, null);
            return view;
        }
        
        public String getPhoneId(String contactId){
        	return mPhotoList.get(contactId);
        }             
        
        public ContactPhotoLoader getPhotoLoader(){
        	return mPhotoLoader;
        }
        
        public boolean getSelectByKey(String key){
        	boolean ret = false;
        	
        	if (mRecipientsMap.get(key) != null) {
        		if (!mUnSelectAll) {
    				mSelectMap.put(key, key);					
				}
				mRecipientsMap.remove(key);					
			}
        	
			if (mInitRecipientMap.get(key) != null) {
				mInitRecipientMap.remove(key);					
			}
        	
        	if (mSelectMap.get(key) != null) {
				ret = true;
			}        	
        	
            int count = mSelectMap.size() + mInitRecipientMap.size();
            
            String string;
            
            if (count > 0) {
                string = String.format(getResources().getString(R.string.message_selected_contacts_count),  count);            				
			}else {
				string = getResources().getString(R.string.search_bar_hint);
			}
            mSearchbarTip.setText(string);
        	return ret;
        }
        
        public void setSelectByKey(String key, boolean select){
        	
        	if (mRecipientsMap.get(key) != null) {
				mRecipientsMap.remove(key);				
			}
        	mUnSelectAll = false;
        	
        	if (select == true) {
				mSelectMap.put(key, key);
			}else {
				mSelectMap.remove(key);
			}
        	
            int count = mSelectMap.size() + mInitRecipientMap.size();
            String string;
            if (count > 0) {
                string = String.format(getResources().getString(R.string.message_selected_contacts_count),  count);            				
			}else {
				string = getResources().getString(R.string.search_bar_hint);
			}
            mSearchbarTip.setText(string);   
        }
        
        public boolean isYlUser(String number){
        	boolean ret = false;      
        	String s = MessageUtils.fixPhoneNumber(number);
        	if (mYlUserMap.get(s) != null) {
				ret = true;
			}        	
        	return ret;
        }
        
        public boolean isPhoneMode(){
        	return mPhoneNumberMode;
        }
    }
    
    private class SearchContactsHandler extends AsyncQueryHandler{

		public SearchContactsHandler(ContentResolver resolver) {
			super(resolver);
			// TODO Auto-generated constructor stub
		}		
		
		@Override
		public void handleMessage(Message msg) {
			Cursor cursor = null;
			ContentResolver resolver = mComposeSreachContext.getContentResolver();
			String select = (String)msg.obj;
	        String phone = "";
	        String cons = null;
	        String selection = null;
	        Uri uri;
	        
	        if (mPhoneNumberMode) {	        	
				if (!TextUtils.isEmpty(select) && select.length() > 0) {
					cons = select;
		            if (usefulAsDigits(cons)) {
		                phone = PhoneNumberUtils.convertKeypadLettersToDigits(cons);
		                if (phone.equals(cons)) {
		                    phone = "";
		                } else {
		                    phone = phone.trim();
		                }
		            }	            
//		            selection = String.format("%s=%s OR %s=%s OR %s=%s",
//		                    Phone.TYPE,
//		                    Phone.TYPE_MOBILE,
//		                    Phone.TYPE,
//		                    Phone.TYPE_WORK_MOBILE,
//		                    Phone.TYPE,
//		                    Phone.TYPE_MMS);	            
					uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(cons));
				}else {
					selection = "1=1) GROUP BY " + Contacts.DISPLAY_NAME + ", (" + CommonDataKinds.Phone.NUMBER;
					uri =  Phone.CONTENT_URI;
				}
							
		        Cursor phoneCursor =
		        	resolver.query(uri,
		                    PROJECTION_PHONE,
		                    selection,
		                    null,
		                    SORT_ORDER);
		        
		        if (!TextUtils.isEmpty(select) && select.length() > 0){
		            uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(cons));
		            selection = null;
		        } else {
		            uri = Data.CONTENT_URI;		
		            selection = Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'" + ") GROUP BY " +
		            											Phone.DISPLAY_NAME + ", (" + Email.DATA;
				}
		        
		       // if (mSwitching) {
	        	marrFirstLetter.clear();
				while (phoneCursor.moveToNext()) {						
                    CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
                    phoneCursor.copyStringToBuffer(4, nameBuffer);
                    //Added by GanFeng 20120129, lower case to upper case
                    if ((nameBuffer.data[0] >= 'a') && (nameBuffer.data[0] <= 'z')) {
                        nameBuffer.data[0] -= 0x20;
                    }
                    marrFirstLetter.add(String.copyValueOf(nameBuffer.data, 0, 1));
				}
				phoneCursor.moveToFirst();
				//}
		        
//	            Cursor addrCursor =
//	            	resolver.query(uri,
//	                        PROJECTION_EMAIL,
//	                        selection,
//	                        null,
//	                        SORT_ORDER_EMAIL);
	            
//	            cursor = new MergeCursor(new Cursor[] { phoneCursor, addrCursor });    
	            cursor = phoneCursor;  
	        }else {
	        	if (!TextUtils.isEmpty(select) && select.length() > 0){
	        		
		        	String seletion = RosterData.ROSTER_USER_ID + " like '%" + select +"%' or "+
		        						RosterData.DISPLAY_NAME + " like '%" + select +"%' ";
		        	
		        	cursor = getContentResolver().query(RosterData.CONTENT_URI, 
	        				PROJECTION_YL_PHONE, 
	        				seletion, 
	        				null, 
	        				Roster.SORT_KEY);	
	        	}else {
		        	cursor = getContentResolver().query(RosterData.CONTENT_URI, 
		        			PROJECTION_YL_PHONE, 
		        			null, 
		        			null, 
		        			RosterData.SORT_KEY);					
				}
			}
	        
			mAdapter.changeCursor(cursor);			
			
	        if (mIsInit == true && cursor.getCount() == 0) {
	            mEmpty.setVisibility(View.VISIBLE);
	        }else {
	            mEmpty.setVisibility(View.GONE);
	        }
	        mIsInit = false;
		}    	
    }

	@Override
	public void afterTextChanged(Editable s) {
		Message msg = new Message();
		msg.obj = (Object)s.toString();
		mQueryHandler.sendMessage(msg);
		String string = s.toString();
		if (!TextUtils.isEmpty(string) && string.length() > 0) {
			
			if (mImgSearchDelStr.getVisibility() == View.GONE){
				mImgSearchDelStr.setVisibility(View.VISIBLE);				
			}
		}else {
			mImgSearchDelStr.setVisibility(View.GONE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}
	
    private boolean usefulAsDigits(CharSequence cons) {
        int len = cons.length();

        for (int i = 0; i < len; i++) {
            char c = cons.charAt(i);

            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            if ((c == ' ') || (c == '-') || (c == '(') || (c == ')') || (c == '.') || (c == '+')
                    || (c == '#') || (c == '*')) {
                continue;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                continue;
            }
            if ((c >= 'a') && (c <= 'z')) {
                continue;
            }

            return false;
        }

        return true;
    }
    
    private OnScrollListener mScrollListener = new OnScrollListener() {
        
        @Override
        public void onScrollStateChanged(AbsListView listView, int paramInt) {
            // TODO Auto-generated method stub
            if (paramInt == SCROLL_STATE_FLING) {
                final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null && inputManager.isActive()) {
                    inputManager.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
                }                          
            }
        }
        
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            
            if (mAdapter.getCount() > 1  && ((null == indexBar) || !indexBar.isAlpha_menu_Showed())) {                
                Cursor cursor = (Cursor)mAdapter.getItem(firstVisibleItem);
                char firstLetter = cursor.getString(4).charAt(0);
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
    };
    
    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
        }
    }
    
    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        menu.add(0, MENU_SELECT_ALL, 0, R.string.menu_mark_all).
            setIcon(R.drawable.menu_icon_mark_all);

        menu.add(0, MENU_UNSELECIT_ALL, 0, R.string.menu_unmark_all).setIcon(
                R.drawable.menu_icon_unmark_all);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()){
        case MENU_SELECT_ALL:
            setAllItemState(true);
            break;

        case MENU_UNSELECIT_ALL:
            setAllItemState(false);
            break;
            
        default:
            break;
        }
        return true;
    }
    
    private void getRecipients(boolean isForIpCall){
	      if(!isForIpCall){
	        ContactList ls = NewMessageComposeActivity.GetRecipientsEditorText();
	        if (ls != null && ls.size() > 0) {
	            int count = ls.size();

	            for (int i = 0; i < count; i++) {
	                Contact contact = ls.get(i);
	                mRecipientsMap.put(contact.getNumber(), contact.getNumber());
	                if (CommonMethod.getContactId(mComposeSreachContext, contact.getName(), contact.getNumber()) > 0) {
						mInitRecipientMap.put(contact.getNumber(), contact.getNumber());
					}
	            }
	            
	            String string;
	            count = mInitRecipientMap.size();
	            if (count > 0) {
	                string = String.format(getResources().getString(R.string.message_selected_contacts_count),  count);            				
	    		}else {
	    			string = getResources().getString(R.string.search_bar_hint);
	    		}
	            mSearchbarTip.setText(string);
	        }
	      }else{
	    	  if(numberData != null && numberData.size() > 0){
	    			for(int i=0;i<numberData.size();i++){
	    				//mRecipientsMap.put(numberData.get(i), numberData.get(i));
	    				mSelectMap.put(numberData.get(i), numberData.get(i));	
	    			}
	    		}
	      }
	    }  
    
    private void setAllItemState(boolean b){
    	
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
             View v = mListView.getChildAt(i);
             if (null != v) {
                     CheckBox check = (CheckBox)v.findViewById(R.id.contacts_list_choice);
                     check.setChecked(b);                                                
            }
        } 
        
        if (b) {
        	quryAllNumber();
		}else {
			mSelectMap.clear();
		}
        mUnSelectAll = !b;
        
        mInitRecipientMap.clear();
        
        count = mSelectMap.size();
        String string;
        if (count > 0) {
            string = String.format(getResources().getString(R.string.message_selected_contacts_count),  count);            				
		}else {
			string = getResources().getString(R.string.search_bar_hint);
		}
        mSearchbarTip.setText(string);
    }
    
    private void quryAllNumber(){
    	
		String selection = "1=1) GROUP BY " + Contacts.DISPLAY_NAME + ", (" + CommonDataKinds.Phone.NUMBER;
		Uri uri =  Phone.CONTENT_URI;
		String key = "";        
        try {
            Cursor addrCursor =
	        	getContentResolver().query(uri,
					                    PROJECTION_PHONE,
					                    selection,
					                    null,
					                    SORT_ORDER);
            
            while (addrCursor.moveToNext()) {
            	key = addrCursor.getString(3);
            	mSelectMap.put(key, key);				
			}
            
            addrCursor.close();
            
            uri = Data.CONTENT_URI;		
            selection = Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'" + ") GROUP BY " +
														Phone.DISPLAY_NAME + ", (" + Email.DATA;
            addrCursor =
            	getContentResolver().query(uri,
			                        PROJECTION_EMAIL,
			                        selection,
			                        null,
			                        SORT_ORDER_EMAIL);
            
            while (addrCursor.moveToNext()) {
            	key = addrCursor.getString(3);
            	mSelectMap.put(key, key);				
			}            
            addrCursor.close();       
            
		} catch (Exception e) {
			Log.e("ComposeSearchContactsActivity", "query error.");
		}
    }
    
    private OnClickListener mDonButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.contacts_search_done:
			//case R.id.contacts_index_done:	
			{
		        ArrayList<String> list = new ArrayList<String>();
		        Iterator iter = mSelectMap.entrySet().iterator();
		        while (iter.hasNext()) {
		            Map.Entry entry = (Map.Entry)iter.next();
		            String string = (String)entry.getValue();
		            list.add(string);
		        }
		        
				if(!isForIpCall){
			        
			                              
			        Iterator iterator = mRecipientsMap.entrySet().iterator();
			        while (iterator.hasNext()) {
			            Map.Entry entry = (Map.Entry)iterator.next();
			            String string = (String)entry.getValue();
			            list.add(string);
			        }
			        
			        NewMessageComposeActivity.SetRecipientsEditorText("");
			        int iCount = list.size();
			        if (iCount > 0) {
			            String[] names = new String[iCount];
			            
			            for (int i = 0; i < iCount; i++) {
			                names[i] = list.get(i);
			            }            
			            NewMessageComposeActivity.SetRecipientsEditorText(TextUtils.join(",", names));            
			        }
			        
				}else {
					/**
					ArrayList<String> photoList = new ArrayList<String>();
			        Iterator photoIter = mPhotoList.entrySet().iterator();
			        while (photoIter.hasNext()) {
			            Map.Entry entry = (Map.Entry)photoIter.next();
			            String string = (String)entry.getValue();
			            photoList.add(string);
			        }*/
					SharedPreferences mPrefs = getSharedPreferences("unused_ip_data", Context.MODE_PRIVATE);
					String number;
					StringBuffer numBuffer = new StringBuffer();
					int size = list.size();
					if(list != null && size > 0){
						for(int i=0;i<size;i++){
							number = list.get(i);
							numBuffer.append(number + ",");
						}
						String numStr = numBuffer.toString();
						numStr = numBuffer.toString().substring(0, numStr.lastIndexOf(","));
						
						Editor editor = mPrefs.edit();
						//editor.clear().commit();
						editor.putString("unused_ip_numbers", numStr).commit();
					}
					Intent intent = new Intent();
					intent.putStringArrayListExtra("number_data", list.size() >0 ? list : null);
					
					setResult(RESULT_FOR_IP_CALL, intent);
				}  
			   
				finish();   
			}
				break;
				
			case R.id.message_index_switching:	
			{
                if (marrFirstLetter.size() > 0) {
                	
                	if (indexBar != null && indexBar.isAlpha_menu_Showed()) {
                		indexBar.hide((Activity)mComposeSreachContext);
					}
                	
                    indexBar = null;
                    
                    vGroup = (ViewGroup) findViewById(R.id.contact_list_framelayout);
                    indexBar = (IndexBar) LayoutInflater.from(mComposeSreachContext).inflate(R.layout.indexbar, null);
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
                            
                            mListView.setSelection(letterIndex); //marrFirstLetter.indexOf(head)));
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
				break;
				
			default:
				break;
			}
		}
	};	 
	
	private class SearchYLContactsHandler extends AsyncQueryHandler{

		public SearchYLContactsHandler(ContentResolver resolver) {
			super(resolver);
			// TODO Auto-generated constructor stub
		}
	
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			
			switch (token) {
			case SEARCH_YL_CONTACTS_QUERY_TOKEN:
				if (cursor != null) {
	                while (cursor.moveToNext()) {
	                    String phone = cursor.getString(0);
	                    String name  = cursor.getString(1);
	                    mYlUserMap.put(phone, name);
	                }		
	                cursor.close();
				}
				break;
	
			default:
				break;
			}
		}	
	} 
	   
	private void getYlAllUser(){
		mQueryYLHandler.startQuery(SEARCH_YL_CONTACTS_QUERY_TOKEN, null, RosterData.CONTENT_URI,
				new String[]{RosterData.ROSTER_USER_ID, RosterData.DISPLAY_NAME}, null, null, null);
	}
	
    @Override
    public void onBackPressed() {
        if(indexBar != null && indexBar.isAlpha_menu_Showed()) {
                indexBar.hide(this);
        } else{
            super.onBackPressed();
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
        } 
        return super.dispatchTouchEvent(ev);
    }
}
