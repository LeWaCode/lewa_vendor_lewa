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
import com.lewa.PIM.mms.data.Conversation;
import com.lewa.os.util.ContactPhotoLoader;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
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
public class ConversationDelListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "ConversationListAdapter";
    private static final boolean LOCAL_LOGV = false;
    private HashMap<String, String> mSelectedMap;
    private Context mContext;

    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private ContactPhotoLoader mPhotoLoader;
    private HashMap<String, String> mPhotoList;
    
    public ConversationDelListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mSelectedMap = new HashMap<String, String>();
        mContext = context;
        mPhotoList = MessageUtils.getPhotoId(mContext.getContentResolver());
    }
    
    public void setPhotoLoader(ContactPhotoLoader photoLade){
        mPhotoLoader = photoLade;
    }
     
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
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof ConversationDelListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }

        ConversationDelListItem headerView = (ConversationDelListItem) view;
        headerView.setPhotoLoader(mPhotoLoader);
        headerView.setAdapter(this);
        ImageView diveceView = (ImageView)view.findViewById(R.id.img_log_separator);
        Conversation conv = Conversation.from(context, cursor);
        ConversationListItemData ch = new ConversationListItemData(context, conv);
        diveceView.setVisibility(View.VISIBLE);

        CheckBox check = (CheckBox)view.findViewById(R.id.contacts_list_choice);
        check.setOnCheckedChangeListener(mCheckedListener);
        check.setTag(cursor.getPosition());
        check.setVisibility(View.VISIBLE);
        long tid = conv.getThreadId();
        if (mSelectedMap.get(tid+"") != null) {
            check.setChecked(true);
        }else{
            check.setChecked(false);
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
            
            String remove = mContext.getResources().getString(R.string.remove);
            Button btnDel = (Button)((Activity)mContext).findViewById(R.id.message_del_done);
            
            if (btnDel != null) {
                btnDel.setText(remove + "(" + mSelectedMap.size() + ")");
            } 
            
            ConversatinDelList list = (ConversatinDelList)mContext;
            if (null != list) {
                list.setBtnSelectState(getSelectAll());
            }
        }        
    };
    
    public void onMovedToScrapHeap(View view) {
        ConversationDelListItem headerView = (ConversationDelListItem)view;
        headerView.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (LOCAL_LOGV) Log.v(TAG, "inflating new view");
        return mFactory.inflate(R.layout.conversation_del_list_item, parent, false);
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationDelListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
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
        
        String remove = mContext.getResources().getString(R.string.remove);
        Button btnDel = (Button)((Activity)mContext).findViewById(R.id.message_del_done);
        
        if (btnDel != null) {
            btnDel.setText(remove + "(" + mSelectedMap.size() + ")");
        } 
    }
    
    public int getSelectCount(){
        return mSelectedMap.size();
    }
    
    public boolean getSelectAll(){
        boolean isSelectAll = false;
        
        isSelectAll = mSelectedMap.size() == mCursor.getCount() ? true : false;
        
        return isSelectAll;
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

    @Override
    protected void onContentChanged() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }
}
