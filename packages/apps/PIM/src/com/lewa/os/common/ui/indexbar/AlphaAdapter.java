package com.lewa.os.common.ui.indexbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.lewa.PIM.R;

public class AlphaAdapter extends SimpleAdapter{
	public int pageIndex = 0, displayCount;
	private List<Map<String, ?>> alphalist;
	private int selectedPosition = -1;
	private Context context;
	private int b;

	public AlphaAdapter(Context context, List<? extends Map<String, ?>> data,
			int resource, String[] from, int[] to, int b) {
		super(context, data, resource, from, to);
		this.context = context;
		this.pageIndex=b;
		displayCount = data.size();
		//alphalist = data;
		//add by zenghuaying fix requirement #11068
		int iMax = data.size();
        alphalist = new ArrayList<Map<String, ?>>();
        
        for(int i=0;i<iMax;i++){
            alphalist.add(data.get(i));
        }
        //add end
	}
	
	class ViewHolder {
		TextView alaphabutton;
		
	}

	@Override
	public int getCount() {
	 
		
	    return alphalist.size();
	}

	@Override
	public Object getItem(int position) {
	    return alphalist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView button = (TextView )view.findViewById(R.id.alphabutton);
        String strIndex = ((HashMap)this.getItem(position)).get(IndexBar.BUTTONKEY).toString();
        button.setText(strIndex);
        if (((strIndex.charAt(0) >= 'A') && (strIndex.charAt(0) <= 'Z'))
                || ('#' == strIndex.charAt(0))){
            button.setBackgroundColor(context.getResources().getColor(R.color.alpha_background));
            button.setTextColor(0xffffffff);
        }
        else {
            button.setBackgroundResource(0);
            button.setTextColor(0xff000000);
        }
        
        return view;
    }
}
