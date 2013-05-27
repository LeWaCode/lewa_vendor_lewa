package com.lewa.PIM.mms.ui;

import java.util.ArrayList;
import java.util.List;

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

    public ListBaseAdapter(List<T> dataItems) {
        mDataItems = dataItems;
    }

    protected abstract View createItemView(T item, int position);
    protected abstract void bindItemView(T item, int position, View itemView);

    protected Object createViewHolder(View itemView, T item) {
        return null;
    }

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
        
        mDataItems = dataItems;
        notifyDataSetChanged();
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
            
            if (null == mOriginalItems) {
                synchronized (this) {
                    mOriginalItems = new ArrayList<T>(mDataItems);
                }
            }
            
            T dataItem = mOriginalItems.get(0);
            if ((0 == mOriginalItems.size()) || !(dataItem instanceof FilterItem)) {
                return null;
            }

            Log.i(TAG, "performFiltering: " + constraint + " OriginalItems.size=" + mOriginalItems.size());
            
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
                                String matchKey = Util.searchApproximateString(content, constraint);
                                if (null != matchKey) {
                                    item.setMatchField(j);
                                    item.setMatchKey(matchKey);
                                    newValues.add((T )item);
                                    break;
                                }
                            }
                            else if (FilterItem.DIGIT_MATCH_MODE == mode) {
                                String matchKey = Util.searchNumericString(content, constraint);
                                if (null != matchKey) {
                                    item.setMatchField(j);
                                    item.setMatchKey(matchKey);
                                    newValues.add((T )item);
                                    break;
                                }
                            }
                            else {
                                if (content.contains(constraint)) {
                                    item.setMatchField(j);
                                    item.setMatchKey(constraint.toString());
                                    newValues.add((T )item);
                                    break;
                                }
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (null == results) {
                return;
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