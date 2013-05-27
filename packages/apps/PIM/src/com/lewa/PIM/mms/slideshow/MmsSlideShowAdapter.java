package com.lewa.PIM.mms.slideshow;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.lewa.PIM.R;

class MmsSlideShowAdapter extends BaseAdapter{
	
	private LayoutInflater mInflater;
	private Context mSlideShowContext;
	private ArrayList<SlideShowItemData> mSlisdeShowItemDataList;	
	
	public MmsSlideShowAdapter(Context context){
		mSlideShowContext = context;
		mSlisdeShowItemDataList = new ArrayList<SlideShowItemData>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mSlisdeShowItemDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mSlisdeShowItemDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		SlideShowItemData data = mSlisdeShowItemDataList.get(position);
		
		if (v == null) {
			v =  mInflater.inflate(R.layout.mms_slid_show_item, parent, false);; 
		}
		bindView(v, data);
		
		return v;
	}
	
	public void bindView(View view, SlideShowItemData itemData){
		MmsSlideShowItem item = (MmsSlideShowItem) view;
		item.setActivity(mSlideShowContext);
		item.bind(itemData);
	}
	
	public void addSlidShowItemData(SlideShowItemData data){		
		mSlisdeShowItemDataList.add(data);
	}
	
	public void cleanData(){
		mSlisdeShowItemDataList.clear();
	}	
}
