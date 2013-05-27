package com.lewa.PIM.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.bool;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.lewa.PIM.R;
import com.lewa.PIM.util.CommonMethod.ContactInfoEntry;

public class NumberListView extends ListView{

    private boolean mExpansion = true;
    private boolean mShowNumberList = false;
    
    private ArrayList<HashMap<String, String>> mlist = new ArrayList<HashMap<String,String>>();
    private ArrayList<HashMap<String, String>> mlisttemp = new ArrayList<HashMap<String,String>>();
    private NumberListViewAdapter mlistAdapter; 
    private Context mNumberListContext;
    private ListView mListView;
    
    public void CleanNumberList(){
        mlist.clear();
        mlisttemp.clear();
    }
    
    public void SetShowNumberList(boolean b){
        mShowNumberList = b;
    }
    
    public NumberListView(Context context) {
        super(context);
    }
    
    public NumberListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnItemClickListener(listviewListener);
    }
    
    public String GetSelectNumber(){
        if (false == mShowNumberList || mlisttemp.size() == 0) {
            return null;
        }

        HashMap<String, String> map = mlisttemp.get(0);
        String number = map.get("user_name");
        return number;
    }
    
    public void bindData(ArrayList<String> list){
        int count = list.size();
        
        for (int i = 0; i < count; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("user_name", list.get(i));
            mlist.add(map);
        }
        
        mlisttemp.add(mlist.get(0));
        mlistAdapter = new NumberListViewAdapter(
                this.getContext(), mlisttemp, R.layout.number_list_item, 
                new String[]{"user_name"}, new int[]{R.id.text_to});
        this.setAdapter(mlistAdapter);    
        mListView = this;
    }

    private OnItemClickListener listviewListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // TODO Auto-generated method stub
            if (mExpansion == false) {          
 
                numberRemove(position);
                mlistAdapter.notifyDataSetChanged();
                mExpansion = true;
                
                Intent msgIntent = new Intent();                
                String newNumber = GetSelectNumber();  
                msgIntent.putExtra("address", newNumber);  
                if (mNumberListContext != null) {
                    ((ComposeMessageActivity)mNumberListContext).reLoadMessage(msgIntent);   					
				}
            }
            else {
            	
            	if (mNumberListContext != null) {
                    final InputMethodManager inputManager = (InputMethodManager)
                    mNumberListContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager != null && inputManager.isActive()) {
                        inputManager.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
                    } 					
				}
                numberAdd(position);	
                mlistAdapter.notifyDataSetChanged();
                mExpansion = false;                   
            }
        }
    };
    
    private void numberAdd(int position){
        int dataCount = mlist.size();
        HashMap<String, String> map = mlisttemp.get(0);
        String number = map.get("user_name");
        String temp;
        
        for (int i = 0; i < dataCount; i++) {
            map = mlist.get(i);
            temp = map.get("user_name");
            if (number.equals(temp) == false) {
                mlisttemp.add(mlist.get(i));                
            }
        }    
    }
    
    private void numberRemove(int position) {                	
        int count = mlisttemp.size() - 1;
        
        if (position < 0 || position > count) {
			return;
		}
        
        HashMap<String, String> map = mlisttemp.get(position);
        String number = map.get("user_name");
        
        for (int i = 0; i <= count; i++) {
            mlisttemp.remove(0);
        }
        
        map = new HashMap<String, String>();
        map.put("user_name", number);        
        mlisttemp.add(map);
    }
    
    private class NumberListViewAdapter extends SimpleAdapter{
        
        protected LayoutInflater mInflater;
        public NumberListViewAdapter(Context context,
                List<? extends Map<String, ?>> data, int resource,
                String[] from, int[] to) {
            super(context, data, resource, from, to);
            
            mInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (position < 0 || position > mlisttemp.size()) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            View v;
            if(convertView == null) {
              v = mInflater.inflate(R.layout.number_list_item, parent, false);
            }
            else {
                v = convertView;                
            }
            
            bindView(position, v);
            return v;
        }
        
        private void bindView(int position, View view){
            NumberListItem listItem = (NumberListItem)view;
            HashMap<String, String> map = mlisttemp.get(position);
            String number = map.get("user_name");
            String to;
            
            if (position == 0) {
                to = getResources().getString(R.string.to_address_label);
            }
            else {
                to = new String("");
            }
            listItem.binder(position, number, to, mExpansion);
        } 
    };
    
    public void putNumberList(boolean b){
        if (b) {          
        	 
            numberRemove(0);
            mlistAdapter.notifyDataSetChanged();
            mExpansion = true;
        }
        else {            
            numberAdd(0);
            mlistAdapter.notifyDataSetChanged();
            mExpansion = false;                   
        }
    }
    
    public boolean isNumberListOpen(){
    	return mlisttemp.size() > 1 ? true : false;
    }
    
    public void setContext(Context context){
    	mNumberListContext = context;
    }
    
    public void updateSelectNumberImsState(){
    	mlistAdapter.notifyDataSetChanged();
    }
}
