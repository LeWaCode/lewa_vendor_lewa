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
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.AddPlaylistSongsAdapter.ItemViewHolder;
import com.lewa.player.model.AddPlaylistSongsAdapter.TrackQueryHandler;
import com.lewa.player.model.AddPlaylistSongsAdapter.TrackQueryHandler.QueryArgs;
import com.lewa.player.ui.AddPlaylistActivity;

public class AddPlaylistAdapter extends CursorAdapter{
    private AddPlaylistActivity mActivity;
    private TrackQueryHandler mQueryHandler;
    private Context mContext;
    
    public class ItemViewHolder {
        TextView  mSongTitle;
        TextView  mArtist;
        CheckBox  mCheckBox; 
        TextView  mTimeText;
//        CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
//        CharArrayBuffer artistBuffer = new CharArrayBuffer(128);
    }
    
    public AddPlaylistAdapter(AddPlaylistActivity activity, Cursor cursor) {        
        super(activity, cursor, false);
        
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
        viewHolder.mTimeText = (TextView)view.findViewById(R.id.listitem_text_time);
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
        viewHolder.mArtist.setText(cursor.getString(artistIdx));
        int durationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        int secs = cursor.getInt(durationIdx) / 1000;
        if (secs == 0) {
            viewHolder.mTimeText.setText("");
        } else {
            viewHolder.mTimeText.setText(MusicUtils.makeTimeString(context, secs));
        }
        viewHolder.mTimeText.setVisibility(View.VISIBLE);
        viewHolder.mCheckBox.setVisibility(View.GONE);
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
