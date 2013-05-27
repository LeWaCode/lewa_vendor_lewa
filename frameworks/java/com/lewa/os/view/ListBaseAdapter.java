package com.lewa.os.view;

import java.util.ArrayList;
import java.util.List;

import com.lewa.os.filter.FilterItem;
import com.lewa.os.util.Util;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public abstract class ListBaseAdapter<T> extends BaseAdapter implements Filterable {
    private static final String TAG = "ListBaseAdapter";
    
    protected List<T> mDataItems;
    private ArrayList<T> mOriginalItems;
    private Filter mFilter;
    private boolean isFilterCancel;

    public ListBaseAdapter(List<T> dataItems) {
        mDataItems = dataItems;
        isFilterCancel = false;
    }

    protected abstract View createItemView(T item, int position);
    protected abstract void bindItemView(T item, int position, View itemView);

    protected Object createViewHolder(View itemView, T item) {
        return null;
    }

    public List<T> getListData() {
        return mDataItems;
    }

    //add by zenghuaying
    protected boolean isCallLogType(T item){
        return false;
    }
    protected boolean isSpecialNum(T item){
        return false;
    }
    //add end
    @Override
    public int getCount() {
        if (null != mDataItems) {
            return mDataItems.size();
        }
        else {
            return 0;
        }
    }

    @Override
    public T getItem(int position) {
        return mDataItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T item = getItem(position);
        if (null == convertView) {
            convertView = createItemView(item, position);
        }
        bindItemView(item, position, convertView);
        return convertView;
    }

    public void update(List<T> dataItems, boolean reload) {
        Log.e(TAG, "update: ");
        if (reload) {
            if (null != mOriginalItems) {
                synchronized (mOriginalItems) {
                    mOriginalItems.clear();
                    mOriginalItems = null;
                }
            }
        }
        else {
            if (isOriginalItemsFixed()) {
                if (null == mOriginalItems) {
                    mOriginalItems = new ArrayList<T>(mDataItems);
                }
            }
            else {
                if (null != mOriginalItems) {
                    synchronized (mOriginalItems) {
                        mOriginalItems.clear();
                        mOriginalItems = null;
                    }
                }
            }
        }
        isFilterCancel = true;
        mDataItems = dataItems;
        notifyDataSetChanged();
    }


    public void updateAndFilter(List<T> dataItems, boolean reload, String number, Filter.FilterListener listener) {
        Log.e(TAG, "updateAndFilter: ");
        if (reload) {
            if (null != mOriginalItems) {
                mOriginalItems.clear();
                mOriginalItems = null;
            }
        }
        else {
            if (isOriginalItemsFixed()) {
                if (null == mOriginalItems) {
                    mOriginalItems = new ArrayList<T>(mDataItems);
                }
            }
            else {
                if (null != mOriginalItems) {
                    mOriginalItems.clear();
                    mOriginalItems = null;
                }
            }
        }
        getFilter().filter(number, listener);
        mDataItems = dataItems;
        //notifyDataSetChanged();
    }
	
    protected boolean isOriginalItemsFixed() {
        return false;
    }

    protected Object getViewHolder(View itemView, T item) {
        Object viewHolder = itemView.getTag();
        if (null == viewHolder)
        {
            viewHolder = createViewHolder(itemView, item);
            itemView.setTag(viewHolder);
        }
        return viewHolder;
    }

    public Filter getFilter() {
        if (null == mFilter) {
            mFilter = new ListBaseFilter();
        }
        return mFilter;
    }


    private class ListBaseFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            //Log.i(TAG, "performFiltering: " + constraint);
            isFilterCancel = false;
            if (null == mOriginalItems) {
                if (null == mDataItems) {
                    return null;
                }
                
                synchronized (this) {
                    mOriginalItems = new ArrayList<T>(mDataItems);
                }
            }

            synchronized (mOriginalItems) {
                
                T dataItem = mOriginalItems.get(0);
                if ((0 == mOriginalItems.size()) || !(dataItem instanceof FilterItem)) {
                    return null;
                }
                //add by zenghuaying fix bug #9151
                if(Util.isStartWithIpPrefix(constraint.toString())){
                    constraint = constraint.subSequence(5, constraint.length());
                }
                //add end
                FilterResults results = new FilterResults();
                if (TextUtils.isEmpty(constraint)) {
                    synchronized (this) {
                        ArrayList<T> list = new ArrayList<T>(mOriginalItems);
                        results.values = list;
                        results.count = list.size();
                        for (int i = 0; i < results.count; ++i) {
                            final FilterItem item = (FilterItem )mOriginalItems.get(i);
                            item.setMatchField(FilterItem.INVALID_FIELD);
                            item.setMatchKey(null);
                            item.setMatchPos(-1);
                        }
                    }
                } else {
                    final int count = mOriginalItems.size();
                    final ArrayList<T> newValues = new ArrayList<T>(count);
                    for (int i = 0; i < count; i++) {
                        final FilterItem item = (FilterItem )mOriginalItems.get(i);
                        final int filterCount = item.getFilterCount();
                        for (int j = 0; j < filterCount; j++) {
                            final String content = item.getFilterContent(j);
                            if (!TextUtils.isEmpty(content)) {
                                final int mode = item.getFilterMode(j);
                                if (FilterItem.APPROXIMATE_MATCH_MODE == mode) {
                                    Util.MatchKey matchKey = Util.searchApproximateString(content, constraint);
                                    if (null != matchKey) {
                                        item.setMatchField(j);
                                        item.setMatchKey(matchKey.mMatchStr);
                                        item.setMatchPos(matchKey.mMatchPos);
                                        newValues.add((T )item);
                                        break;
                                    }
                                }
                                else if (FilterItem.DIGIT_MATCH_MODE == mode) {
                                    //String matchKey = Util.searchNumericString(content, constraint);
                                    Util.MatchKey matchKey = null;
                                    String[] alphabetContents = item.getFilterAlphabetContents(j);
                                    if (null != alphabetContents) {
                                        matchKey = Util.searchNumericString(alphabetContents, constraint);
                                    }
                                    else {
                                        matchKey = Util.searchNumericString(content, constraint);
                                    }
                                    
                                    if (null != matchKey) {
                                        item.setMatchField(j);
                                        item.setMatchKey(matchKey.mMatchStr);
                                        item.setMatchPos(matchKey.mMatchPos);
                                        newValues.add((T )item);
                                        break;
                                    }
                                }
                                else {
                                    if (content.contains(constraint)) {
                                        item.setMatchField(j);
                                        item.setMatchKey(constraint.toString());
                                        item.setMatchPos(content.indexOf(constraint.toString()));
                                        newValues.add((T )item);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (newValues.size() > 0) {
                        int endPos = 0;
                        int isFirstMatchEndPos = 0;
                        int secondPartEndPos = 0;
                        int isContactsEndPos = 0;
                        int isSpecialNumEndPos = 0;
                        
                        for (int i = (newValues.size() - 1); i >= endPos; --i) {
                            final FilterItem item = (FilterItem )newValues.get(i);
                            /**
                             * modify by zenghuaying for bug #5542
                             * 
                             * item type include two types(1.name matched;2.number matched)
                             * item display rule:
                             * a. first show name matched item
                             *    a1: show common contacts
                             *    a2: show normal contacts
                             *    a3: show special number contacts
                             * b. second show number matched item
                             *    b1: show common contacts
                             *    b2: show normal contacts
                             *    b3: show call log contacts
                             *    b4: show special number contacts
                             */
                            boolean isSpecialNum = isSpecialNum((T )item);
                            if (FilterItem.NAME_FIELD == item.getMatchField()) { //name matched
                                final FilterItem itemWithNameMatch = (FilterItem )newValues.remove(i);
                                if (1 == item.getMatchPos() && !isSpecialNum) {
                                    newValues.add(0, (T )itemWithNameMatch);
                                    isFirstMatchEndPos++;
                                } else if(!isSpecialNum){//name matched,contacts item
                                    newValues.add(isFirstMatchEndPos, (T )itemWithNameMatch);
                                    secondPartEndPos++;
                                }else{//name matched , special number item
                                    newValues.add(isFirstMatchEndPos + secondPartEndPos, (T )itemWithNameMatch);
                                    isSpecialNumEndPos++;
                                }
                                
                                ++endPos;
                                ++i;
                                
                            }//add by zenghuaying
                            else if(!isCallLogType((T )item) && !isSpecialNum){//number matched,contacts item
                                newValues.remove(i);
                                newValues.add((isFirstMatchEndPos + secondPartEndPos + isSpecialNumEndPos),(T )item);
                                isContactsEndPos++;
                                
                                ++endPos;
                                ++i;
                            }else if(!isSpecialNum){//number matched,call log item
                                newValues.remove(i);
                                newValues.add((isFirstMatchEndPos + secondPartEndPos + isContactsEndPos + isSpecialNumEndPos),(T )item);
                                
                                ++endPos;
                                ++i;
                            }//remain others is special number item
                            
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (null == results) {
                return;
            }

            if (isFilterCancel) {
                isFilterCancel = false;
                List<T> dateItems = (List<T>) results.values;
                if (results.count > 0) {
                    dateItems.clear();
                }
                Log.i(TAG, "publishResults: the filter already was cancelled.");
                return ;
            }

            Log.i(TAG, "publishResults: " + results.count);
            
            mDataItems = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            }
            else {
                notifyDataSetInvalidated();
            }
        }
    }
}
