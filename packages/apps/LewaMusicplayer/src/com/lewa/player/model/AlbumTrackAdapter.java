package com.lewa.player.model;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.lewa.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.AllTrackBrowserAdapter.TrackQueryHandler;
import com.lewa.player.model.AllTrackBrowserAdapter.ViewHolder;
import com.lewa.player.model.AllTrackBrowserAdapter.TrackQueryHandler.QueryArgs;
import com.lewa.player.ui.outer.AlbumTrackBrowserActivity;
import com.lewa.player.ui.outer.AllTrackBrowserActivity;

public class AlbumTrackAdapter extends SimpleCursorAdapter{
	
    boolean mIsNowPlaying;
    boolean mDisableNowPlayingIndicator;

    int mTitleIdx;
    int mArtistIdx;
    int mDurationIdx;
    int mAudioIdIdx;
    int mAlbumIdx;
    int mSortKeyIdx;
    int [] mSortKey;
    private final StringBuilder mBuilder = new StringBuilder();
    private final String mUnknownArtist;
    private final String mUnknownAlbum;
   
    private AlbumTrackBrowserActivity mActivity = null;
    private TrackQueryHandler mQueryHandler;
    private String mConstraint = null;
    private boolean mConstraintIsValid = false;
    
    static class ViewHolder {
        TextView title;
        TextView duration;
        TextView artistAlbum;
        CharArrayBuffer titleBuffer;
        char [] artistAlbumBuffer;
        String currentSortKey;
    }
    
    public class TrackQueryHandler extends AsyncQueryHandler {
    class QueryArgs {
        public Uri uri;
        public String [] projection;
        public String selection;
        public String [] selectionArgs;
        public String orderBy;
    }

    TrackQueryHandler(ContentResolver res) {
        super(res);
    }
    
    public Cursor doQuery(Uri uri, String[] projection,
            String selection, String[] selectionArgs,
            String orderBy, boolean async) {
        if (async) {
            // Get 100 results first, which is enough to allow the user to start scrolling,
            // while still being very fast.
            Uri limituri = uri.buildUpon().appendQueryParameter("limit", "100").build();
            QueryArgs args = new QueryArgs();
            args.uri = uri;
            args.projection = projection;
            args.selection = selection;
            args.selectionArgs = selectionArgs;
            args.orderBy = orderBy;

            startQuery(0, args, limituri, projection, selection, selectionArgs, orderBy);
            return null;
        }
        return MusicUtils.query(mActivity,
                uri, projection, selection, selectionArgs, orderBy);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        //Log.i("@@@", "query complete: " + cursor.getCount() + "   " + mActivity);
        mActivity.init(cursor, cookie != null);
        if (token == 0 && cookie != null && cursor != null && cursor.getCount() >= 100) {
            QueryArgs args = (QueryArgs) cookie;
            startQuery(1, null, args.uri, args.projection, args.selection,
                    args.selectionArgs, args.orderBy);
        }
//        mSortKey = MusicUtils.storeSortKey(cursor, mSortKeyIdx);        
    }
}

        public AlbumTrackAdapter(Context context, AlbumTrackBrowserActivity currentactivity,
        int layout, Cursor cursor, String[] from, int[] to,
        boolean isnowplaying, boolean disablenowplayingindicator) {
        super(context, layout, cursor, from, to);
        mActivity = currentactivity;
        getColumnIndices(cursor);
        mIsNowPlaying = isnowplaying;
        mDisableNowPlayingIndicator = disablenowplayingindicator;
        mUnknownArtist = context.getString(R.string.unknown_artist_name);
        mUnknownAlbum = context.getString(R.string.unknown_album_name);            
        mQueryHandler = new TrackQueryHandler(context.getContentResolver());
    }

    public void setActivity(AlbumTrackBrowserActivity newactivity) {
        mActivity = newactivity;
    }
    
    public TrackQueryHandler getQueryHandler() {
        return mQueryHandler;
    }
    
    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            mDurationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            mSortKeyIdx = cursor.getColumnIndexOrThrow("sort_key");
            try {
                mAudioIdIdx = cursor.getColumnIndexOrThrow(
                MediaStore.Audio.Playlists.Members.AUDIO_ID);
            } catch (IllegalArgumentException ex) {
                mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            }               
        }
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = super.newView(context, cursor, parent);
        ViewHolder vh = new ViewHolder();
        
    vh.title = (TextView) view.findViewById(R.id.line1);
    vh.duration = (TextView) view.findViewById(R.id.duration);
    vh.artistAlbum = (TextView) view.findViewById(R.id.line2);
        vh.titleBuffer = new CharArrayBuffer(100);
        vh.artistAlbumBuffer = new char[200];
        vh.currentSortKey = new String();
        view.setTag(vh);
        return view;
    }
    
    @Override
    public void bindView(View itemView, Context context, Cursor cursor) {
    
        ViewHolder vh = (ViewHolder) itemView.getTag();
        
        cursor.copyStringToBuffer(mTitleIdx, vh.titleBuffer);
        vh.title.setText(vh.titleBuffer.data, 0, vh.titleBuffer.sizeCopied);
    
        int secs = cursor.getInt(mDurationIdx) / 1000;
        if (secs == 0) {
            vh.duration.setText("");
        } else {
            vh.duration.setText(MusicUtils.makeTimeString(context, secs));
        }
        
        final StringBuilder builder = mBuilder;
        builder.delete(0, builder.length());
        
        String artist = cursor.getString(mArtistIdx);
        String alumb = cursor.getString(mAlbumIdx);
        if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
            artist = mUnknownArtist;
        }
        builder.append(artist);
        if (alumb == null || alumb.equals(MediaStore.UNKNOWN_STRING)) {           
            alumb = mUnknownAlbum;                                                       
        }
        int len = builder.length();
        if (vh.artistAlbumBuffer.length < len) {
            vh.artistAlbumBuffer = new char[len];
        }
        builder.getChars(0, len, vh.artistAlbumBuffer, 0);
        vh.artistAlbum.setText(vh.artistAlbumBuffer, 0, len);
        
//        if(mSortKey == null || mSortKey.length == 0)
//            return;
//    vh.currentSortKey = cursor.getString(mSortKeyIdx).substring(0, 1).toUpperCase();
//    int position = cursor.getPosition();  
        
        //bindSectionHeader(itemView, position, vh.currentSortKey);
    }
        
    private void bindSectionHeader(View itemView, int position, String header) {
        ViewGroup viewGroup = (ViewGroup)itemView.findViewById(R.id.track_header);
        TextView headerTxt = (TextView) itemView.findViewById(R.id.track_header_text);
            if(position <= mSortKey.length) {
                if(mSortKey[position] == position) {
                    viewGroup.setVisibility(View.VISIBLE);
                    headerTxt.setText(header);
                } else {
                    viewGroup.setVisibility(View.GONE);
                }
            }
        }
    @Override
    public void changeCursor(Cursor cursor) {
        if (mActivity.isFinishing() && cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (cursor != mActivity.mTrackCursor) {
            mActivity.mTrackCursor = cursor;            
            super.changeCursor(cursor);
            getColumnIndices(cursor);
        }
    }
    
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String s = constraint.toString();
        if (mConstraintIsValid && (
                (s == null && mConstraint == null) ||
                (s != null && s.equals(mConstraint)))) {
            return getCursor();
        }
        Cursor c = mActivity.getTrackCursor(mQueryHandler, s, false);
        mConstraint = s;
        mConstraintIsValid = true;
        return c;
    }
}
