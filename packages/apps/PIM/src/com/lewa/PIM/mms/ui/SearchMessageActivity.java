package com.lewa.PIM.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.RosterData;
import android.support.lewa.widget.CursorAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import com.lewa.PIM.R;
import com.lewa.os.util.ContactPhotoLoader;

public class SearchMessageActivity extends ListActivity implements 
                                                        TextWatcher ,
                                                        View.OnTouchListener{
	
	private static final int SEARCH_YL_CONTACTS_QUERY_TOKEN       = 0;
	
    private ViewGroup mMainView;
    private ListView mListView;
    private EditText mFilterEdit;
    private String mSearchStr;
    private Context mSearchContext;
    private TextView mSearchbarTip;
    private ImageView mImgSearchDelStr;
    
    private AsyncQueryHandler mQueryHandler;
    private SearchAdapter mAdapter;
    private ContactPhotoLoader mPhotoLoader;
    private SearchYLContactsHandler mQueryYLHandler;
    
    private HashMap<String, String> mPhotoList;
    private HashMap<String, String> mImsUserMap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mMainView = (ViewGroup )layoutInflater.inflate(R.layout.message_search_list, null);
        setContentView(mMainView);
        mListView = getListView();
        mListView.setOnTouchListener(this);
        mSearchStr = "";
        mSearchContext = this;
        mFilterEdit = (EditText )findViewById(R.id.search_src_text);
        mFilterEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        mFilterEdit.addTextChangedListener(this);
        
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.activity_background);
        this.getWindow().setBackgroundDrawable(drawable);
        
        mImgSearchDelStr = (ImageView)findViewById(R.id.img_search_category_cancel);
        mImgSearchDelStr.setOnClickListener(mSearchBarTipListener);
        mImgSearchDelStr.setVisibility(View.GONE);
        
        mSearchbarTip = (TextView)findViewById(R.id.searchbar_tip);
        
        mQueryHandler = new AsyncQueryHandler(getContentResolver()){

            @Override
            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                // TODO Auto-generated method stub
                if (cursor.getCount() > 0) {
//                    mMainView.setBackgroundResource(R.drawable.activity_background);                    
                    String str = "";
                    if (cursor.getCount() == 1) {
                    	str = mSearchContext.getResources().getString(R.string.search_message_count, cursor.getCount());						
					}else {
						str = mSearchContext.getResources().getString(R.string.search_message_counts, cursor.getCount());						
					}
                    mSearchbarTip.setText(str);
                }else {
                    //mMainView.setBackgroundResource(R.color.translucent_search_background);
                    String str = mSearchContext.getResources().getString(R.string.search_hint);
                    mSearchbarTip.setText(str);
                }
                mAdapter.changeCursor(cursor);
            }            
        };
        
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture, R.drawable.ic_contact_header_unknow);
        mPhotoList = MessageUtils.getPhotoId(getContentResolver());
        mQueryYLHandler = new SearchYLContactsHandler(getContentResolver());
        mImsUserMap = new HashMap<String, String>();        
    	if (MessageUtils.isImsSwitch(this)) {
            getYlAllUser();
		}
    	
        mAdapter = new SearchAdapter(this, null);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mScrollListener);
    }
    
    private OnClickListener mSearchBarTipListener = new OnClickListener(){
        @Override
        public void onClick(View v) {
        	mFilterEdit.setText("");
        	mImgSearchDelStr.setVisibility(View.GONE);
        }        
    };
    
    public int getPhotoIdToInt(String address){
        int photoId = 0;
        if ((mPhotoList != null) && (TextUtils.isEmpty(address) == false)) {
            
            address = address.replaceAll("-", "");
            if (address.startsWith("+86")) {
                address = address.substring(3);
            }
            if (mPhotoList.get(address) != null) {
                photoId = Integer.parseInt(mPhotoList.get(address));
            }
        }
        return photoId;
    }
    
    private OnScrollListener mScrollListener = new OnScrollListener() {
        
        @Override
        public void onScrollStateChanged(AbsListView listView, int paramInt) {
            // TODO Auto-generated method stub
            if (paramInt == SCROLL_STATE_FLING) {
                final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null && inputManager.isActive()) {
                    inputManager.hideSoftInputFromWindow(mFilterEdit.getWindowToken(), 0);
                }                          
            }
        }
        
        @Override
        public void onScroll(AbsListView v, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
                      
        }
    };
    
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
    public void afterTextChanged(Editable s) {

        String strKey = s.toString();
        mSearchStr = s.toString();
        
		if (!TextUtils.isEmpty(strKey) && strKey.length() > 0) {
			
			if (mImgSearchDelStr.getVisibility() == View.GONE){
				mImgSearchDelStr.setVisibility(View.VISIBLE);				
			}
		}else {
			mImgSearchDelStr.setVisibility(View.GONE);
		}        
        
        if (TextUtils.isEmpty(s)) {
            mAdapter.changeCursor(null);
            String str = mSearchContext.getResources().getString(R.string.search_hint);
            mSearchbarTip.setText(str);
            return;
        }

        String search = "%" + strKey + "%";
        Uri uri = MessageUtils.PduYlColumns.IMS_SEARCH_URI.buildUpon()
                    .appendQueryParameter("pattern", search).build();
        mQueryHandler.startQuery(0, null, uri, 
                null,
                null, null, null); 
        
//        Uri uri = Uri.parse("content://sms");
//        mQueryHandler.startQuery(0, null, uri, 
//                new String[]{"thread_id", "address", "body", "_id"},
//                "body like '" + search + "'", null, null);         
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        
    }
    
    private class SearchAdapter extends CursorAdapter {


        public SearchAdapter(Context context, Cursor c) {
            super(context, c, false);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            
            SearchItem item = (SearchItem)view;
            item.setPhotoLoader(mPhotoLoader);
            item.bindData(cursor, mSearchStr, mSearchContext);
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.message_search_item, parent, false);
            return v;
        }
             
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getClass() == mListView.getClass()) {
            InputMethodManager inputMethodManager = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(getListView().getWindowToken(), 0);                
            }
        }
        return false;
    }
    
    public boolean isImsUser(String number){
    	boolean b = false;
    	String s = MessageUtils.fixPhoneNumber(number);
    	if (mImsUserMap.get(s) != null) {
    		b = true;
		}
    	return b;
    }
    
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
	                    mImsUserMap.put(phone, name);
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
}
