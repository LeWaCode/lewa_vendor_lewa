package com.lewa.player.model;

import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.ui.outer.GenreBrowserActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.sip.SimpleSessionDescription.Media;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class GenreBrowserAdapter extends SimpleCursorAdapter implements OnScrollListener{
    
//    private final Drawable mNowPlayingOverlay;

    private final StringBuilder mStringBuilder = new StringBuilder();
    private final String mUnknownGenre;
    private GenreBrowserActivity mActivity;
    private AsyncQueryHandler mQueryHandler;
    private GenreBrowserAdapter mAdapter;
    private String mConstraint = null;
    private boolean mConstraintIsValid = false;
    private int mGenreIdIdx;
    private int mGenreNameIdx;
    private Cursor mCursor;
    private SongsCountLoader mSongsCountLoader;
    
    public long[] unknownGenresList;
    
    public static class ViewHolder {
        ImageView play;
        public TextView title;
        TextView count;
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
        }
    }

    
	public GenreBrowserAdapter(Context context, GenreBrowserActivity currentactivity,
            int layout, Cursor cursor, String[] from, int[] to) {
	    super(context, layout, cursor, from, to);
	    mActivity = currentactivity;
        mQueryHandler = new QueryHandler(context.getContentResolver());
        mUnknownGenre = context.getString(R.string.unknown_genre_name);
        mAdapter = this;
        getColumnIndices(cursor);
        context.getResources();
        unknownGenresList = MusicUtils.getSongListForOtherGenre(mContext);
	}
	
	public void setSongsCountLoader(SongsCountLoader loader) {
	    mSongsCountLoader = loader;
	}
	
	public void setActivity(GenreBrowserActivity newactivity) {
        mActivity = newactivity;
    }
    
	
    public AsyncQueryHandler getQueryHandler() {
        unknownGenresList = MusicUtils.getSongListForOtherGenre(mContext);
        return mQueryHandler;
    }
    
    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mGenreIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID);
            mGenreNameIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);;
        }
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (mCursor != null) {
            if(mContext != null && unknownGenresList != null && unknownGenresList.length > 0) {
                return mCursor.getCount() + 1;
            } else {
                return mCursor.getCount();
            }
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View v = convertView;
        if (convertView == null) {
            if (position < getCount() - 1) {
                if (!mCursor.moveToPosition(position)) {
                    throw new IllegalStateException(
                            "couldn't move cursor to position " + position);
                }
                v = newView(mContext, mCursor, parent);
            } else {
                v = newView(mContext, null, parent);
            }
        } else {
            v = convertView;
        }
        mCursor.moveToPosition(position);
        if(unknownGenresList != null && unknownGenresList.length > 0) {
            if(position < getCount() - 1) {
                Cursor c = mCursor;
                bindView(v, mContext, c);
            } else {
                bindOtherView(mContext, v, parent);
            }
        } else {
            Cursor c = mCursor;
            bindView(v, mContext, c);
        }
        return v;
    }
    
    public void bindOtherView(Context context, View itemView, ViewGroup parent) {        
        ViewHolder vh = (ViewHolder) itemView.getTag();
        vh.play.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                final long[] list = unknownGenresList;
                MusicUtils.playAll(mActivity, list, 0);
            }
        });
        
        vh.title.setText(context.getResources().getString(R.string.unknown_genre_name));
        StringBuilder builder = mStringBuilder;
        builder.delete(0, builder.length());
        String string = context.getResources().getQuantityString(
                R.plurals.Nsongs, unknownGenresList.length, unknownGenresList.length);
        vh.count.setText(string);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.genres_list_item, null);
        ViewHolder vh = new ViewHolder();

        vh.play = (ImageView)view.findViewById(R.id.play);
        vh.title = (TextView)view.findViewById(R.id.title);
        vh.count = (TextView)view.findViewById(R.id.count);
        view.setTag(vh);
        return view;
    }

    @Override
    public void bindView(View itemView, Context context, final Cursor cursor) {
        
        ViewHolder vh = (ViewHolder) itemView.getTag();
        
        final long id = cursor.getLong(mGenreIdIdx);
        vh.play.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub                
                final long [] list = MusicUtils.getSongListForGenre(mActivity, id);
                MusicUtils.playAll(mActivity, list, 0);
            }
        });
        
        String genreTitle = cursor.getString(mGenreNameIdx);
        if (genreTitle != null && genreTitle.length() != 0) {
            char j = genreTitle.charAt(0);
            if(j >='0' &&  j<=9 || j>='A' && j<='Z' || j>='a' && j<='z'){
                
            } else if (genreTitle.substring(0, 1).matches("[\u4e00-\u9fa5]")) {
                
            } else {
                genreTitle = context.getResources().getString(R.string.unknown_genre_name);
            }
        }else {
            genreTitle = context.getResources().getString(R.string.unknown_genre_name);
        }
  
        vh.title.setText(genreTitle);
        
        StringBuilder builder = mStringBuilder;
        builder.delete(0, builder.length());
        String string = context.getResources().getQuantityString(R.plurals.Nsongs, 0, 0);
        vh.count.setText(string);  
        if (mSongsCountLoader != null) {
            mSongsCountLoader.loadSongsCount(vh.count, (int)id);
        }
    }
    
    
    @Override
    public void changeCursor(Cursor cursor) {
        if (mActivity.isFinishing() && cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (cursor != mActivity.getGenreCursor()) {
            mActivity.setGenreCursor(cursor);
            getColumnIndices(cursor);
            super.changeCursor(cursor);
        }
    }
    
    public void setCursor(Cursor cursor) {        
        mCursor = cursor;
    }
    
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String s = constraint.toString();
        if (mConstraintIsValid && (
                (s == null && mConstraint == null) ||
                (s != null && s.equals(mConstraint)))) {
            return getCursor();
        }
        Cursor c = mActivity.getGenreCursor(null, s);
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
