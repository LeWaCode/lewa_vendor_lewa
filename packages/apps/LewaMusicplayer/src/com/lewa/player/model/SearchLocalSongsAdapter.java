package com.lewa.player.model;

import com.lewa.player.MusicUtils;
import com.lewa.player.model.AddPlaylistSongsAdapter.TrackQueryHandler;
import com.lewa.player.model.AddPlaylistSongsAdapter.TrackQueryHandler.QueryArgs;
import com.lewa.player.ui.LewaSearchBar;
import com.lewa.player.ui.SearchLocalSongsActivity;

import com.lewa.player.R;

import android.R.string;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class SearchLocalSongsAdapter extends ResourceCursorAdapter {
    private Context mContext;
    private TrackQueryHandler mQueryHandler;
    private String mConstraint = null;
    private SearchLocalSongsActivity mActivity;
    private LewaSearchBar mLewaSearchBar;
    
    public SearchLocalSongsAdapter(Context context, int layout, Cursor c, LewaSearchBar searchBar) {
        super(context, layout, c);
        // TODO Auto-generated constructor stub
        mContext = context;
        mActivity = (SearchLocalSongsActivity)context;
        mQueryHandler = new TrackQueryHandler(mActivity);
        mLewaSearchBar = searchBar;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO Auto-generated method stub
        ImageView imageView = (ImageView)view.findViewById(R.id.icon);
        if (imageView != null)
            imageView.setVisibility(View.GONE);
        
        TextView songTitle = (TextView)view.findViewById(R.id.line1);
        if (songTitle != null) {
            int songTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            songTitle.setText(cursor.getString(songTitleIdx));
        }
        
        TextView artistAlbum = (TextView)view.findViewById(R.id.line2);
        if (artistAlbum != null) {
            String artist;
            String album;
            int artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            artist = cursor.getString(artistIdx);
            if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING))
                artist = mContext.getResources().getString(R.string.unknown_artist_name);
            
            int albumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            album = cursor.getString(albumIdx);
            if (album == null || album.equals(MediaStore.UNKNOWN_STRING))
                album = mContext.getResources().getString(R.string.unknown_album_name);
            
            artistAlbum.setText(artist + "-" + album);
        }
        
        TextView duration = (TextView)view.findViewById(R.id.duration);
        if (duration != null) {
            int durationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            String strDuration = MusicUtils.makeTimeString(mContext, cursor.getLong(durationIdx)/1000);
            duration.setText(strDuration);
        }
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
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int count = super.getCount();
        View view = mActivity.findViewById(R.id.emptyText);
//        if (count != 0) {
//            view.setVisibility(View.GONE);
//        } else {
//            view.setVisibility(View.VISIBLE);
//        }
        view.setVisibility(View.GONE);
            
        return count;
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
    
    @Override
    public void changeCursor(Cursor cursor) {
        
        if(cursor != null && mLewaSearchBar != null) {
            int count = cursor.getCount();
            if(count >0 ) {                
                mLewaSearchBar.setSearchResult(mContext.getResources().getQuantityString(
                        R.plurals.NNNsearchsong, count, count));
            } else {
                 mLewaSearchBar.setSearchResult(mContext.getString(R.string.search_bar_hint));
            }
        }
        
        super.changeCursor(cursor);
        
    }

}