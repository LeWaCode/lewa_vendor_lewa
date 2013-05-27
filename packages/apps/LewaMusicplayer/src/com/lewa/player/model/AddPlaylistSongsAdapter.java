package com.lewa.player.model;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.AllTrackBrowserAdapter.TrackQueryHandler;
import com.lewa.player.ui.AddPlaylistSongsActivity;

public class AddPlaylistSongsAdapter extends ResourceCursorAdapter{
    private Context mContext;        
    private AddPlaylistSongsActivity mActivity;
    private TrackQueryHandler mQueryHandler;
    private String mConstraint = null;
    
    public class ItemViewHolder {
        TextView  mSongTitle;
        TextView  mArtist;
        public CheckBox  mCheckBox; 
//        CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
//        CharArrayBuffer artistBuffer = new CharArrayBuffer(128);
    }
    
    public AddPlaylistSongsAdapter(AddPlaylistSongsActivity activity, Cursor cursor) {        
        super(activity, R.layout.playlist_multiple_add_list_item, cursor, false);
        
        mActivity = activity;
        mQueryHandler = new TrackQueryHandler(activity);
        // TODO Auto-generated constructor stub
    }
    
    public TrackQueryHandler getQueryHandler() {
        return mQueryHandler;
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.playlist_multiple_add_list_item, parent, false);
        ItemViewHolder viewHolder = new ItemViewHolder();
        viewHolder.mSongTitle = (TextView)view.findViewById(R.id.song_name);
        viewHolder.mArtist = (TextView)view.findViewById(R.id.song_artist);
        viewHolder.mCheckBox = (CheckBox)view.findViewById(R.id.list_add_choice);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO Auto-generated method stub
        ItemViewHolder viewHolder = (ItemViewHolder)view.getTag();
        
//        cursor.copyStringToBuffer(1, viewHolder.nameBuffer); 
//        viewHolder.mSongName.setText(viewHolder.nameBuffer.data);
        int songTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        viewHolder.mSongTitle.setText(cursor.getString(songTitleIdx));
        int artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        String artist = cursor.getString(artistIdx);
        if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
            artist = context.getString(R.string.unknown_artist_name);
        }
        viewHolder.mArtist.setText(artist);
        viewHolder.mCheckBox.setTag(Integer.valueOf(cursor.getPosition()));
        
        if (mActivity.arrSongsIdSel.contains(new Integer((int)cursor.getInt(0)))) {
            viewHolder.mCheckBox.setChecked(true);
        }
        else {
            viewHolder.mCheckBox.setChecked(false);
        }
        
        viewHolder.mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                int position = ((Integer)buttonView.getTag()).intValue();
                mActivity.setItemState(position, isChecked);
            }
        });        
    }
    
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        // TODO Auto-generated method stub
        String s = constraint.toString();
        if ((s == null && mConstraint == null) ||
                (s != null && s.equals(mConstraint))) {
            return getCursor();
        }
        Cursor c = mActivity.getTrackCursor(mQueryHandler, s, true);
        mConstraint = s;
        return c;
    }

    public class TrackQueryHandler extends AsyncQueryHandler {
        
        class QueryArgs {
            public Uri uri;
            public String [] projection;
            public String selection;
            public String [] selectionArgs;
            public String orderBy;
        }

        public TrackQueryHandler(Context context) {
            super(context.getContentResolver());
            mContext = context;
        }
        
        public Cursor doQuery(Uri uri, String[] projection,
                String selection, String[] selectionArgs,
                String orderBy, boolean async) {
            if (async) {
                QueryArgs args = new QueryArgs();
                args.uri = uri;
                args.projection = projection;
                args.selection = selection;
                args.selectionArgs = selectionArgs;
                args.orderBy = orderBy;

                startQuery(0, args, uri, projection, selection, selectionArgs, orderBy);
                return null;
            }
            return MusicUtils.query(mContext, uri, projection, selection, selectionArgs, orderBy);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (mActivity != null && !mActivity.isFinishing()) {       
                if (cursor != null) {
                    mActivity.mAdapter.changeCursor(cursor);
                }
            }
        }
    }
}