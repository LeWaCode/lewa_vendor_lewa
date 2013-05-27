package com.lewa.player.model;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.outer.PlaylistTrackBrowserActivity;

public class PlaylistTrackCursorAdapter extends SimpleCursorAdapter {
    
    int mTitleIdx;
    int mArtistIdx;
    int mDurationIdx;
    
    private PlaylistTrackBrowserActivity mActivity = null;
    private QueryHandler mQueryHandler;
    private String mConstraint = null;
    private boolean mConstraintIsValid = false;
    private int mDragPaddingLeft;
    
    static class ViewHolder {
        TextView title;
        TextView artist;
        TextView duration;
    }

    public class QueryHandler extends AsyncQueryHandler {
        
        class QueryArgs {
            public Uri uri;
            public String[] projection;
            public String selection;
            public String[] selectionArgs;
            public String orderBy;
        }
        QueryHandler(ContentResolver res) {
            super(res);
        }
        
        public Cursor doQuery(Uri uri, String[] projection, String selection,
                String[] selectionArgs, String orderBy, boolean async) {
            if (async) {
                // Get 100 results first, which is enough to allow the user to
                // start scrolling,
                // while still being very fast.
//                Uri limituri = uri.buildUpon()
//                        .appendQueryParameter("limit", "100").build();
                QueryArgs args = new QueryArgs();
                args.uri = uri;
                args.projection = projection;
                args.selection = selection;
                args.selectionArgs = selectionArgs;
                args.orderBy = orderBy;

                startQuery(0, args, uri, projection, selection,
                        selectionArgs, orderBy);
                return null;
            }
            return MusicUtils.query(mActivity, uri, projection, selection,
                    selectionArgs, orderBy);
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            //Log.i("@@@", "query complete: " + cursor.getCount() + "   " + mActivity);
            mActivity.init(cursor);
        }
    }

    public PlaylistTrackCursorAdapter(Context context, PlaylistTrackBrowserActivity currentactivity,
            int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to);
        mActivity = currentactivity;
        getColumnIndices(cursor);
        mQueryHandler = new QueryHandler(context.getContentResolver());
        mDragPaddingLeft = 
            context.getResources().getDimensionPixelOffset(R.dimen.drag_list_padding_left);
        
    }

    public void setActivity(PlaylistTrackBrowserActivity newactivity) {
        mActivity = newactivity;
    }
    
    public QueryHandler getQueryHandler() {
        return mQueryHandler;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(context).inflate(R.layout.edit_track_list_item, null);
        ViewHolder vh = new ViewHolder();
        vh.title = (TextView)view.findViewById(R.id.line1);
        vh.artist = (TextView)view.findViewById(R.id.line2);
        vh.duration = (TextView)view.findViewById(R.id.duration);
        view.setTag(vh);
        view.setBackgroundResource(R.drawable.playlist_tile_drag);
        view.setPadding(mDragPaddingLeft, 0, 0, 0);
        return view;
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO Auto-generated method stub
        ViewHolder vh = (ViewHolder) view.getTag();
        String title = cursor.getString(mTitleIdx);       
        vh.title.setText(title);
        String artist = cursor.getString(mArtistIdx);
        if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
            artist = context.getString(R.string.unknown_artist_name);
        }
        vh.artist.setText(artist);
        
        int secs = cursor.getInt(mDurationIdx) / 1000;
        if (secs == 0) {
            vh.duration.setText("");
        } else {
            vh.duration.setText(MusicUtils.makeTimeString(context, secs));
        }
    }

    @Override
    public void changeCursor(Cursor cursor) {
        if (mActivity.isFinishing() && cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (cursor != mActivity.getPlaylistCursor()) {
            mActivity.setPlaylistCursor(cursor);
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
        Cursor c = mActivity.getPlaylistCursor(null, s, true);
        mConstraint = s;
        mConstraintIsValid = true;
        return c;
    }
    
    @Override
    protected void onContentChanged() {
        // TODO Auto-generated method stub
        super.onContentChanged();
        mActivity.updateListData();
    }

    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            mDurationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        }
    }
}
