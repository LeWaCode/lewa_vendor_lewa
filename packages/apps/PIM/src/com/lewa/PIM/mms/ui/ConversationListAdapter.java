/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.lewa.PIM.mms.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.LogTag;
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.os.util.ContactPhotoLoader;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;

/**
 * The back-end data adapter for ConversationList.
 */
//TODO: This should be public class ConversationListAdapter extends ArrayAdapter<Conversation>
public class ConversationListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "ConversationListAdapter";
    private static final boolean LOCAL_LOGV = false;
    private boolean mBlackBackground;
    private boolean mMarkState;
    private HashMap<String, String> mSelectedMap;
    private Context mContext;

    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private ContactPhotoLoader mPhotoLoader;
    private HashMap<String, String> mPhotoList;
    
    private HashMap<ConversationListItem, ConversationListItem> mConversionList;

    public ConversationListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mMarkState = false;
        mSelectedMap = new HashMap<String, String>();
        mContext = context;
        mConversionList = new HashMap<ConversationListItem, ConversationListItem>();
        mPhotoList = MessageUtils.getPhotoId(mContext.getContentResolver());
    }
    
    public void setPhotoLoader(ContactPhotoLoader photoLade){
        mPhotoLoader = photoLade;
    }
    
    public int getPhotoIdToInt(String address){
        int photoId = 0;
        if ((mPhotoList != null) && (TextUtils.isEmpty(address) == false)) {   
            
        	String tempAddress = MessageUtils.fixPhoneNumber(address);

        	if (mPhotoList.get(tempAddress) != null) {
                photoId = Integer.parseInt(mPhotoList.get(tempAddress));
            }
        }
        return photoId;
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof ConversationListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }

        ConversationListItem headerView = (ConversationListItem) view;
        headerView.setPhotoLoader(mPhotoLoader);
        headerView.setAdapter(this);
        ImageView diveceView = (ImageView)view.findViewById(R.id.img_log_separator);
        Conversation conv = Conversation.from(context, cursor);
        ConversationListItemData ch = new ConversationListItemData(context, conv);
        diveceView.setVisibility(View.VISIBLE);
        
        if (mConversionList.get(headerView) == null) {
            mConversionList.put(headerView, headerView);
        }

        CheckBox check = (CheckBox)view.findViewById(R.id.contacts_list_choice);
        check.setOnCheckedChangeListener(mCheckedListener);
        check.setTag(cursor.getPosition());
        if (mMarkState) {
            long tid = conv.getThreadId();
            check.setVisibility(View.VISIBLE);
            if (mSelectedMap.get(tid+"") != null) {
                check.setChecked(true);
            }else{
                check.setChecked(false);
            }
        }else {
            check.setVisibility(View.GONE);
        }
        headerView.bind(context, ch);
    }

    private OnCheckedChangeListener mCheckedListener = new OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            Cursor cursor = (Cursor)getItem(Integer.parseInt(buttonView.getTag().toString()));
            Conversation conv = Conversation.from(mContext, cursor);
            long tid = conv.getThreadId();
            if (isChecked) {
                mSelectedMap.put(tid+"", tid+"");
            }else {
                mSelectedMap.remove(tid+"");
            }
        }        
    };
    
    public void onMovedToScrapHeap(View view) {
        ConversationListItem headerView = (ConversationListItem)view;
        headerView.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (LOCAL_LOGV) Log.v(TAG, "inflating new view");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mBlackBackground = prefs.getBoolean(MessagingPreferenceActivity.BLACK_BACKGROUND, false);
        return mFactory.inflate(R.layout.conversation_list_item, parent, false);
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }
    
    public void setMarkState(boolean b){
        mSelectedMap.clear();
        mMarkState = b;
    }
    
    public void delThreadIdFromSelectMap(String s){
        String[]list = TextUtils.split(s, ",");
        int count = list.length;
        
        for (int i = 0; i < count; i++) {
            mSelectedMap.remove(list[i]);
        }
    }
    
    public void setAllThreadState(boolean b){
        mSelectedMap.clear();
        
        if (b) {
            int count = getCount();
            for (int i = 0; i < count; i++) {
                Cursor cursor = (Cursor)getItem(i);
                Conversation conv = Conversation.from(mContext, cursor);
                long tid = conv.getThreadId();
                mSelectedMap.put(tid+"", tid+"");
            }            
        }
    }
    
    public int getSelectCount(){
        return mSelectedMap.size();
    }
    
    public long[] getSelectArray(){
       int count = mSelectedMap.size();
       long[] l = new long[count];
       
       Iterator iter = mSelectedMap.entrySet().iterator();
       for (int i = 0; iter != null && i < count; i++) {
           Map.Entry entry = (Map.Entry)iter.next();
           l[i] = Long.parseLong(entry.getValue().toString());        
       }
       return l;
    }
    
    public boolean getMarkState(){
        return mMarkState;
    }

    @Override
    protected void onContentChanged() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }
    
    public void removeAllList(){
        Iterator iterator = mConversionList.entrySet().iterator();
        int count = mConversionList.size();
        
        for (int i = 0; iterator != null && i < count; i++) {
            Map.Entry entry = (Map.Entry)iterator.next();
            ConversationListItem item = (ConversationListItem)entry.getValue();
            item.unbind();            
        }
        mConversionList.clear();
    }
    
    public void reloadPohotId(){
    	mPhotoList = MessageUtils.getPhotoId(mContext.getContentResolver());
    }
}
