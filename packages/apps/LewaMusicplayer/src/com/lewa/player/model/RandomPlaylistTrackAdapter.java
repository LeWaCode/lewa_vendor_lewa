package com.lewa.player.model;


import java.util.HashMap;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class RandomPlaylistTrackAdapter extends BaseAdapter{
    
    TextView mTrackTitle;
    TextView mTrackArtist;
    TextView mTrackDuration;
    private long[] randomSongs;
    private Context mContext;
    private int total = 0;
    private LayoutInflater mInflater;
    HashMap<Integer, View> m = new HashMap<Integer, View>();
    
    public RandomPlaylistTrackAdapter(Context context, long[] randomTrack) {
        mContext = context;
      // TODO Auto-generated constructor stub
        randomSongs = randomTrack;
        if(randomSongs == null)
            return;
        total = randomSongs.length;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return total;
    }



    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // TODO Auto-generated method stub
    	view = m.get(position);
    	if(view == null) {
    	    view = LayoutInflater.from(mContext).inflate(R.layout.edit_track_list_item, null);
    	    mTrackTitle = (TextView)view.findViewById(R.id.line1);
    	    mTrackArtist = (TextView)view.findViewById(R.id.line2);
    	    mTrackDuration = (TextView)view.findViewById(R.id.duration);
            if(randomSongs != null) {
            	String ret[] = MusicUtils.getSongName(mContext, randomSongs[position]);
            	if(ret != null) {
                	mTrackTitle.setText(ret[0]);
                	mTrackArtist.setText(ret[1]);
                	mTrackDuration.setText(ret[2]);
            	}
            }
            m.put(position, view);
    	}
        return view;
    }

	@Override
	public Object getItem(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}    
}
