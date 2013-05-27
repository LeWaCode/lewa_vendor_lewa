package com.lewa.player.model;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.outer.AlbumBrowserActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class AlbumBrowserAdapter extends SimpleCursorAdapter implements OnScrollListener{
    
//    private final Drawable mNowPlayingOverlay;
    private final BitmapDrawable mDefaultAlbumIcon;
    private int mAlbumIdIdx;
    private int mAlbumIdx;
    private int mArtistIdx;
    private int mSortKeyIdx;
    private int [] mSortKey;
    private final StringBuilder mStringBuilder = new StringBuilder();
    private final String mUnknownAlbum;
    private final String mUnknownArtist;
    private final String mAlbumSongSeparator;
    private final Object[] mFormatArgs = new Object[1];
    private AlbumBrowserActivity mActivity;
    private AsyncQueryHandler mQueryHandler;
    private AlbumBrowserAdapter mAdapter;
    private String mConstraint = null;
    private boolean mConstraintIsValid = false;
    private SongsCountLoader mSongsCountLoader;
    
    static class ViewHolder {
        ImageView album;
        TextView title;
        TextView artist;
        TextView trackCount;
        String currentSortKey;
    }
    
    class QueryHandler extends AsyncQueryHandler {
        QueryHandler(ContentResolver res) {
            super(res);
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            //Log.i("@@@", "query complete");
            if (mActivity != null) {
                mActivity.init(cursor);
            }
            
//            mSortKey = MusicUtils.storeSortKey(cursor, mSortKeyIdx);
        }
    }
    
    
	public AlbumBrowserAdapter(Context context, AlbumBrowserActivity currentactivity,
            int layout, Cursor cursor, String[] from, int[] to) {
	    super(context, layout, cursor, from, to);
	    mActivity = currentactivity;
        mQueryHandler = new QueryHandler(context.getContentResolver());
        mUnknownAlbum = context.getString(R.string.unknown_album_name);
        mUnknownArtist = context.getString(R.string.unknown_artist_name);
        mAlbumSongSeparator = context.getString(R.string.albumsongseparator);
        mAdapter = this;
        
        Resources r = context.getResources();
//        mNowPlayingOverlay = r.getDrawable(R.drawable.indicator_ic_mp_playing_list);

        Bitmap b = BitmapFactory.decodeResource(r, R.drawable.albumart_mp_unknown_list);
        mDefaultAlbumIcon = new BitmapDrawable(r, b);
        // no filter or dither, it's a lot faster and we can't tell the difference
        mDefaultAlbumIcon.setFilterBitmap(false);
        mDefaultAlbumIcon.setDither(false);
        getColumnIndices(cursor);
        context.getResources();
	}
	
	public void setActivity(AlbumBrowserActivity newactivity) {
        mActivity = newactivity;
    }
	
    public void setSongsCountLoader(SongsCountLoader loader) {
        mSongsCountLoader = loader;
    }
    
    public AsyncQueryHandler getQueryHandler() {
        return mQueryHandler;
    }
    
    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mAlbumIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID);
            mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
            mSortKeyIdx = cursor.getColumnIndexOrThrow("album_sort_key");
        }
    }    

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {   
        View view = LayoutInflater.from(context).inflate(R.layout.albums_list_item, null);
        ViewHolder vh = new ViewHolder();

        vh.album = (ImageView) view.findViewById(R.id.album_cover);
        vh.title = (TextView) view.findViewById(R.id.album_title);
        vh.artist = (TextView) view.findViewById(R.id.album_artistname);
        vh.trackCount = (TextView) view.findViewById(R.id.album_count);
        vh.currentSortKey = new String();
        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View itemView, Context context, Cursor cursor) {
        ViewHolder vh = (ViewHolder) itemView.getTag();
        
        boolean unknown = false; 
        String albumTitle = cursor.getString(mAlbumIdx);//cursor.getString(mAlbumIdx);
        if (albumTitle == null || albumTitle.equals(MediaStore.UNKNOWN_STRING)) {           
            albumTitle = mUnknownAlbum;
            unknown = true;
        }
        vh.title.setText(albumTitle);
        
        String artist = cursor.getString(mArtistIdx); 
        if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {           
            artist = mUnknownArtist;
            unknown = true;
        }
        vh.artist.setText(artist);
        
        StringBuilder builder = mStringBuilder;
        builder.delete(0, builder.length());
        final long id = cursor.getLong(mAlbumIdIdx);
        final long aid = cursor.getLong(mArtistIdx);
        String string = context.getResources().getQuantityString(R.plurals.Nsongs, 0, 0);
        vh.trackCount.setText(string);
        if (mSongsCountLoader != null) {
            mSongsCountLoader.loadSongsCount(vh.trackCount, (int)id);
        }
        
        if (unknown) {
            vh.album.setBackgroundDrawable(mDefaultAlbumIcon);
        } else {
            Drawable d = MusicUtils.getCachedArtwork(context, id , albumTitle, mDefaultAlbumIcon);
            vh.album.setBackgroundDrawable(d);
        }

        vh.album.setImageResource(R.drawable.album_play_selector);

        vh.album.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                long [] list = MusicUtils.getSongListForAlbum(mActivity, id, aid);
                MusicUtils.playAll(mActivity, list, 0);
            }
        });
        
//        if(mSortKey == null || mSortKey.length == 0)
//            return;
//        vh.currentSortKey = cursor.getString(mSortKeyIdx).substring(0, 1).toUpperCase();
//        int position = cursor.getPosition(); 
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
      /*  if (mActivity.isFinishing() && cursor != null) {
            cursor.close();
            cursor = null;
        }*/
        if (cursor != mActivity.getAlbumCursor()) {
            mActivity.setAlbumCursor(cursor);
            getColumnIndices(cursor);
            super.changeCursor(cursor);
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
        Cursor c = mActivity.getAlbumCursor(null, s);
        mConstraint = s;
        mConstraintIsValid = true;
        return c;
    }
    
    @Override
    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        if (mSongsCountLoader != null) {
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mSongsCountLoader.pause();
            } else {
                mSongsCountLoader.resume();
            }
        }
    }
}
