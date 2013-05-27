package com.lewa.player.model;

import com.lewa.player.MusicUtils;
import com.lewa.player.MusicUtils.ServiceToken;
import com.lewa.player.R;
import com.lewa.player.model.AlbumBrowserAdapter.QueryHandler;
import com.lewa.player.ui.outer.AlbumBrowserActivity;
import com.lewa.player.ui.outer.ArtistBrowserActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class ArtistBrowserAdapter extends SimpleCursorAdapter implements OnScrollListener{

    public ArtistBrowserAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to) {
        super(context, layout, c, from, to);
        // TODO Auto-generated constructor stub
    }
    
    private int mArtistIdIdx;
    private int mArtistIdx;
    private int mAlbumIdx;
    private int mSortKeyIdx;
    boolean mIsUnknownArtist;
    boolean mIsUnknownAlbum;
    private ArtistBrowserAdapter mAdapter;
    private final StringBuilder mStringBuilder = new StringBuilder();
    private int [] mSortKey;
    private String mUnknownArtist;
    private ArtistBrowserActivity mActivity;
    private AsyncQueryHandler mQueryHandler;
    private SongsCountLoader mSongsCountLoader;
    
    static class ViewHolder {
        ImageView play;
        TextView artist;
        TextView albumCount;
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

    
	public ArtistBrowserAdapter(Context context, ArtistBrowserActivity currentactivity,
            int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to); 
        mQueryHandler = new QueryHandler(context.getContentResolver());
        mActivity = currentactivity;
        mUnknownArtist = context.getString(R.string.unknown_artist_name);
        mAdapter = this;
	}
	
	public void setActivity(ArtistBrowserActivity newactivity) {
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
            mArtistIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
            mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            mSortKeyIdx = cursor.getColumnIndexOrThrow("artist_sort_key");
        }
    }
    

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.genres_list_item, null);
        ViewHolder vh = new ViewHolder();
        vh.play = (ImageView)view.findViewById(R.id.play);
        vh.artist = (TextView) view.findViewById(R.id.title);
        vh.albumCount = (TextView) view.findViewById(R.id.count);
        vh.currentSortKey = new String();
        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View itemView, final Context context, Cursor cursor) {
        ViewHolder vh = (ViewHolder) itemView.getTag();
        
        final int id = cursor.getInt(mArtistIdIdx);        
        vh.play.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final long [] list = MusicUtils.getSongListForArtist(context, id);
                MusicUtils.playAll(mActivity, list, 0);
            }
        });
        
        String artistName = cursor.getString(mArtistIdx);
        boolean unknown = artistName == null || artistName.equals(MediaStore.UNKNOWN_STRING);
        if (unknown) {
            artistName = mUnknownArtist;
        }
        vh.artist.setText(artistName);
        
        StringBuilder builder = mStringBuilder;
        builder.delete(0, builder.length());
                                  
//        count = cursor.getInt(mAlbumIdx);
        String string = context.getResources().getQuantityString(R.plurals.Nsongs, 0, 0);
        vh.albumCount.setText(string); 
        if (mSongsCountLoader != null) {
            mSongsCountLoader.loadSongsCount(vh.albumCount, (int)id);
        }
/*        vh.currentSortKey = cursor.getString(mSortKeyIdx).substring(0, 1).toUpperCase();
        if(unknown) {
            vh.currentSortKey = "#";
        }*/
        
//        if(mSortKey == null || mSortKey.length == 0)
//            return;
//        int position = cursor.getPosition(); 
        //bindSectionHeader(itemView, position, vh.currentSortKey);
    }
    
    private void bindSectionHeader(View itemView, int position, String header) {
        ViewGroup viewGroup = (ViewGroup)itemView.findViewById(R.id.track_header);
        TextView headerTxt = (TextView) itemView.findViewById(R.id.track_header_text);
        if(position <= mSortKey.length) {
            if (mSortKey[position] == position) {
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
        if (cursor != mActivity.getArtistCursor()) {
            mActivity.setArtistCursor(cursor);
            getColumnIndices(cursor);
            super.changeCursor(cursor);
        }
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


