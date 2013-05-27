package com.lewa.player.model;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.CurrentPlaylistActivity;
import com.lewa.player.ui.outer.AllTrackBrowserActivity;
import com.lewa.player.ui.view.CurrentPlaylistView;



public class TrackListAdapter extends SimpleCursorAdapter {

    int mTitleIdx;
    int mArtistIdx;
    int mDurationIdx;
    int mAudioIdIdx;

    private final StringBuilder mBuilder = new StringBuilder();
    private final String mUnknownArtist;
    private CurrentPlaylistActivity mActivity;
    private AlphabetIndexer mIndexer;
    private Context mContext;
    private TrackQueryHandler mQueryHandler;
    private String mConstraint = null;
    private boolean mConstraintIsValid = false;
    private int mDragPaddingLeft;

	public TrackListAdapter(Context context, CurrentPlaylistActivity currentactivity,
            int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to);
        mActivity = currentactivity;
        getColumnIndices(cursor);
        mUnknownArtist = context.getString(R.string.unknown_artist_name);
        
        mQueryHandler = new TrackQueryHandler(context.getContentResolver());
        mDragPaddingLeft = 
            context.getResources().getDimensionPixelOffset(R.dimen.drag_list_padding_left);
    }
	
    public TrackQueryHandler getQueryHandler() {
        return mQueryHandler;
    }
    
    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            mDurationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            try {
                mAudioIdIdx = cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Playlists.Members.AUDIO_ID);
            } catch (IllegalArgumentException ex) {
                mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            }
            
            if (mIndexer != null) {
                mIndexer.setCursor(cursor);
            } /*else if (!mActivity.mEditMode && mActivity.mAlbumId == null) {
                String alpha = mActivity.getString(R.string.fast_scroll_alphabet);
            
                mIndexer = new MusicAlphabetIndexer(cursor, mTitleIdx, alpha);
            }*/
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = super.newView(context, cursor, parent);
        ImageView iv = (ImageView) v.findViewById(R.id.icon);
        iv.setVisibility(View.GONE);
        
        ViewHolder vh = new ViewHolder();
        vh.line1 = (TextView) v.findViewById(R.id.line1);
        vh.line2 = (TextView) v.findViewById(R.id.line2);
        vh.duration = (TextView) v.findViewById(R.id.duration);
        vh.play_indicator = (ImageView) v.findViewById(R.id.play_indicator);

        vh.buffer1 = new CharArrayBuffer(100);
        vh.buffer2 = new char[200];
        v.setTag(vh);
        v.setBackgroundResource(R.drawable.playlist_tile_drag);
        v.setPadding(mDragPaddingLeft, 0, 0, 0);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        
        ViewHolder vh = (ViewHolder) view.getTag();
        
        cursor.copyStringToBuffer(mTitleIdx, vh.buffer1);
        vh.line1.setText(vh.buffer1.data, 0, vh.buffer1.sizeCopied);
        
        int secs = cursor.getInt(mDurationIdx) / 1000;
        if (secs == 0) {
            vh.duration.setText("");
        } else {
            vh.duration.setText(MusicUtils.makeTimeString(context, secs));
        }
        
        final StringBuilder builder = mBuilder;
        builder.delete(0, builder.length());

        String name = cursor.getString(mArtistIdx);
        if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
            builder.append(mUnknownArtist);
        } else {
            builder.append(name);
        }
        int len = builder.length();
        if (vh.buffer2.length < len) {
            vh.buffer2 = new char[len];
        }
        builder.getChars(0, len, vh.buffer2, 0);
        vh.line2.setText(vh.buffer2, 0, len);

        ImageView iv = vh.play_indicator;
        long id = -1;
        if (MusicUtils.sService != null) {
            // TODO: IPC call on each bind??
            try {
                id = MusicUtils.sService.getQueuePosition();
            } catch (RemoteException ex) {
            }
        }
        
        // Determining whether and where to show the "now playing indicator
        // is tricky, because we don't actually keep track of where the songs
        // in the current playlist came from after they've started playing.
        //
        // If the "current playlists" is shown, then we can simply match by position,
        // otherwise, we need to match by id. Match-by-id gets a little weird if
        // a song appears in a playlist more than once, and you're in edit-playlist
        // mode. In that case, both items will have the "now playing" indicator.
        // For this reason, we don't show the play indicator at all when in edit
        // playlist mode (except when you're viewing the "current playlist",
        // which is not really a playlist)
        if (cursor.getPosition() == id) {
            iv.setImageResource(R.drawable.listplayingnow);
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.GONE);
        }
        //mListView.ti.invalidateViews();
    }
    
    public void setActivity(CurrentPlaylistActivity newactivity) {
        mActivity = newactivity;
    }
    
    @Override
    public void changeCursor(Cursor cursor) {
/*        if (cursor != null) {
            cursor.close();
            cursor = null;
        }*/
       if (cursor != mActivity.mTrackCursor) {
            mActivity.setTrackCursor(cursor);
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
    
    static class ViewHolder {
        TextView line1;
        TextView line2;
        TextView duration;
        ImageView play_indicator;
        CharArrayBuffer buffer1;
        char [] buffer2;
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
            return MusicUtils.query(mContext,
                    uri, projection, selection, selectionArgs, orderBy);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            //Log.i("@@@", "query complete: " + cursor.getCount() + "   " + mActivity);
            mActivity.init(cursor, cookie != null);
            mActivity.ti.setSelection(mActivity.getNowPlayingPos());
            if (token == 0 && cookie != null && cursor != null && cursor.getCount() >= 100) {
                QueryArgs args = (QueryArgs) cookie;
                startQuery(1, null, args.uri, args.projection, args.selection,
                        args.selectionArgs, args.orderBy);
            }
        }
    }
}