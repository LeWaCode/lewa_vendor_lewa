package com.lewa.player.ui;

import java.util.ArrayList;

import com.lewa.player.ExitApplication;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.model.NowPlayingCursor;
import com.lewa.player.model.TrackListAdapter;
import com.lewa.player.ui.view.TouchInterceptor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class CurrentPlaylistActivity extends Activity 
    implements View.OnCreateContextMenuListener, MusicUtils.Defs{
	
	private static final int REMOVE = CHILD_MENU_BASE;

    private int mSelectedPosition;
    private Context mContext;
    
    private TrackListAdapter mAdapter;
    private String mAlbumId;
    private String mArtistId;
    public Cursor mTrackCursor;
    private boolean mIsOrderChanged = false;
    private String mSortOrder;
    
    public TouchInterceptor ti;
    private String[] mCursorCols;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	 
		// TODO Auto-generated constructor stub
    	super.onCreate(savedInstanceState);
    	
		mContext = this;
		
		View inflate = LayoutInflater.from(this).inflate(R.layout.currentlist_view, null);
		setContentView(inflate);
        ti = (TouchInterceptor) inflate.findViewById(R.id.curlistTouch);
        //ti.setBackgroundDraw0xFF515151);
        ti.setCacheColorHint(0);

        ti.setOnItemClickListener(itemClickListen);
        ti.setFastScrollEnabled(true);
        
        ti.setDropListener(mDropListener);
        ti.setRemoveListener(mRemoveListener);
        ti.setDivider(null);
        ti.setOnCreateContextMenuListener(this);
//        this.invalidate();  
        
        mAdapter = (TrackListAdapter) getLastNonConfigurationInstance();

        if (mAdapter != null) {
            mAdapter.setActivity(this);
            ti.setAdapter(mAdapter);
        }        
        
		IntentFilter filterReceiver = new IntentFilter();
        filterReceiver.addAction(MediaPlaybackService.META_CHANGED);
        mContext.registerReceiver(mTrackListListener, filterReceiver);
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
		
    	registerProviderStatusObserver();
	}
    
    public int getCount() {
		// TODO Auto-generated method stub
    	if(mAdapter != null) {
    		return mAdapter.getCount();
    	}
		return 0;
	}
	
	protected BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
        
        @Override
         public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MediaPlaybackService.META_CHANGED)) {
                //mAdapter.bindView(null, context, mTrackCursor);
                mAdapter.notifyDataSetInvalidated();
                //ti.postInvalidate();
            }
        }

    };
    
    public void init(Cursor newCursor, boolean isLimited) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(newCursor); // also sets mTrackCursor
        
        if (mTrackCursor == null) {
           // MusicUtils.displayDatabaseError(this);
            //closeContextMenu();
           // mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        } else {
            ti.setSelection(getNowPlayingPos());
        }

        //MusicUtils.hideDatabaseError(this);
        
        setTitle();
        
    }
    
    public int getNowPlayingPos() {        
        
        long id = -1;
        int pos = -1;
        
        int audioIdIdx = 0;    
        
        if (MusicUtils.sService != null) {          
            try {
                id = MusicUtils.sService.getQueuePosition();
            } catch (RemoteException ex) {
            }
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToFirst();
            while (cursor != null && !cursor.isAfterLast()) {
                try {
                    audioIdIdx = cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Playlists.Members.AUDIO_ID);
                } catch (IllegalArgumentException ex) {
                    audioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                }
                if (cursor.getPosition() == id) {
                    pos = cursor.getPosition();
                    break;
                }
                cursor.moveToNext();
            }
        }
        return pos;
    }
    
    public boolean getIsOrderChanged() {
        return mIsOrderChanged;
    }
    
    public void setIsOrderChanged(boolean isChanged) {
        mIsOrderChanged = isChanged;
    }
    
    public TrackListAdapter getAdapter() {
        return mAdapter;
    }
    
    OnItemClickListener itemClickListen = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> arg0, View v, int position,
                long id) {
            // TODO Auto-generated method stub
            if (mTrackCursor.getCount() == 0) {
                return;
            }
            // When selecting a track from the queue, just jump there instead of
            // reloading the queue. This is both faster, and prevents accidentally
            // dropping out of party shuffle.
            /*if (mTrackCursor instanceof NowPlayingCursor) {
                if (MusicUtils.sService != null) {
                    try {
                        MusicUtils.sService.setQueuePosition(MusicUtils.sService.getQueue()[position]);
                        return;
                    } catch (RemoteException ex) {
                    }
                }
            }*/
            MusicUtils.playAll(mContext, mTrackCursor, position);
        }
        
    };
    
    private TouchInterceptor.DropListener mDropListener =
        new TouchInterceptor.DropListener() {
        public void drop(int from, int to) {
            NowPlayingCursor c = (NowPlayingCursor) mTrackCursor;
            c.moveItem(from, to);
            mAdapter.notifyDataSetChanged();
            ti.invalidateViews();
            mIsOrderChanged = true;
        }
    };
    
    private TouchInterceptor.RemoveListener mRemoveListener =
        new TouchInterceptor.RemoveListener() {
        public void remove(int which) {
            removePlaylistItem(which);
        }
    };
    
    public void removePlaylistItem(int which) {
        View v = ti.getChildAt(which - ti.getFirstVisiblePosition());
        if (v == null) {
            //Log.d(LOGTAG, "No view when removing playlist item " + which);
            return;
        }
        v.setVisibility(View.GONE);
        ti.invalidateViews();
        if (mTrackCursor instanceof NowPlayingCursor) {
            ((NowPlayingCursor)mTrackCursor).removeItem(which);
        } else {
            int colidx = mTrackCursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Playlists.Members._ID);
            mTrackCursor.moveToPosition(which);
            long id = mTrackCursor.getLong(colidx);
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                    id);
            mContext.getContentResolver().delete(
                    ContentUris.withAppendedId(uri, id), null, null);
        }
        v.setVisibility(View.VISIBLE);
        ti.invalidateViews();
    }
    
    public void updateList() {
        if(mAdapter != null) {
            getTrackCursor(mAdapter.getQueryHandler(), null, true);
        } else {
            initAdatper();
        }
    }
    
    public void initAdatper() {
        if (mAdapter == null) {

            //mAdapter = (TrackListAdapter) mContext.getLastNonConfigurationInstance();

            mAdapter = new TrackListAdapter(
                    mContext, // need to use application context to
                                // avoid leaks
                    this,
                    R.layout.edit_track_list_item,
                    null, // cursor
                    new String[] {}, new int[] {});
            
            // setTitle(R.string.working_songs);
            
            ti.setAdapter(mAdapter);
            getTrackCursor(mAdapter.getQueryHandler(), null, true);            
            
        } else {
            mTrackCursor = mAdapter.getCursor();
            // If mTrackCursor is null, this can be because it doesn't have
            // a cursor yet (because the initial query that sets its cursor
            // is still in progress), or because the query failed.
            // In order to not flash the error dialog at the user for the
            // first case, simply retry the query when the cursor is null.
            // Worst case, we end up doing the same query twice.
            if (mTrackCursor != null) {
                 init(mTrackCursor, false);
            } else {
                // setTitle(R.string.working_songs);
                getTrackCursor(mAdapter.getQueryHandler(), null, true);
            }
        }
        
    }
    
    public void setTrackCursor(Cursor cursor) {
        mTrackCursor = cursor;
        ti.setAdapter(mAdapter);
    }

    public Cursor getTrackCursor(
            TrackListAdapter.TrackQueryHandler queryhandler, String filter,
            boolean async) {
        
        mCursorCols = new String[] { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DURATION };

        if (queryhandler == null) {
            throw new IllegalArgumentException();
        }

        Cursor ret = null;
        mSortOrder = null;//MediaStore.Audio.Media.TITLE_KEY;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");

        if (MusicUtils.sService != null) {
            ret = new NowPlayingCursor(mContext,MusicUtils.sService, mCursorCols);
            if (ret.getCount() == 0) {
                // finish();
            }
        } else {
            // Nothing is playing.
        }
        // This special case is for the "nowplaying" cursor, which cannot be
        // handled
        // asynchronously using AsyncQueryHandler, so we do some extra
        // initialization here.
        if (ret != null && async) {
            init(ret, false);
            setTitle();
        }
        return ret;
    }


    private void setTitle() {

        CharSequence fancyName = null;
        if (mAlbumId != null) {
            int numresults = mTrackCursor != null ? mTrackCursor.getCount() : 0;
            if (numresults > 0) {
                mTrackCursor.moveToFirst();
                int idx = mTrackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                fancyName = mTrackCursor.getString(idx);
                // For compilation albums show only the album title,
                // but for regular albums show "artist - album".
                // To determine whether something is a compilation
                // album, do a query for the artist + album of the
                // first item, and see if it returns the same number
                // of results as the album query.
                String where = MediaStore.Audio.Media.ALBUM_ID + "='" + mAlbumId +
                        "' AND " + MediaStore.Audio.Media.ARTIST_ID + "=" + 
                        mTrackCursor.getLong(mTrackCursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.ARTIST_ID));
                Cursor cursor = MusicUtils.query(mContext, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Media.ALBUM}, where, null, null);
                if (cursor != null) {
                    if (cursor.getCount() != numresults) {
                        // compilation album
                        fancyName = mTrackCursor.getString(idx);
                    }    
                    cursor.deactivate();
                }
                if (fancyName == null || fancyName.equals(MediaStore.UNKNOWN_STRING)) {
                    fancyName = "unknow";
                }
            }
        } 

        if (fancyName != null) {
            //setTitle(fancyName);
        } else {
           // setTitle(R.string.tracks_title);
        }
    }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
    	unregisterProviderStatusObserver();
		super.onDestroy();
	}
    
    public void updateNowplayingCursor() {
    	updateList();    	
    }
    
//    public void closeCursor() {
//    	destroyView();
//    }

    private void registerProviderStatusObserver() {
        getContentResolver().registerContentObserver(Uri.parse("content://media"),
                true, mProviderStatusObserver);
    }

    private void unregisterProviderStatusObserver() {
    	getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }
    
    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            getTrackCursor(mAdapter.getQueryHandler(), null, true);
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = mAdapter.getCursor();
        int songId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        
        switch (item.getItemId()) {
        case PLAY_SELECTION: {
            // play the track
            int position = mSelectedPosition;
            MusicUtils.playAll(mContext, cursor, position);
            return true;
        }

//        case QUEUE: {
//            long [] list = new long[] { mSelectedId };
//            MusicUtils.addToCurrentPlaylist(this, list);
//            return true;
//        }

        case NEW_PLAYLIST: {
            
            MusicUtils.addToNewPlaylist(mContext, new long[]{ songId }, -1);
            return true;
        }

        case PLAYLIST_SELECTED: {
            long [] list = new long[] { songId };
            long playlist = item.getIntent().getLongExtra("playlist", 0);
            MusicUtils.addToPlaylist(mContext, list, playlist);
            return true;
        }

        case USE_AS_RINGTONE: {
            // Set the system setting to make this the current ringtone
            MusicUtils.setRingtone(mContext, songId);
            return true;
        }

        case DELETE_ITEM: {
            long[] list = new long[]{ songId };
            String f;
            if (android.os.Environment.isExternalStorageRemovable()) {
                f = getString(R.string.delete_song_desc);
            } else {
                f = getString(R.string.delete_song_desc_nosdcard);
            }
            String strTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String desc = String.format(f, strTitle);            
            MusicUtils.deleteItems(mContext, desc, list);
            return true;
        }
        
        case REMOVE: {
            removePlaylistItem(mSelectedPosition);
            return true;
        }
        default:
            break;
        }        

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfoIn) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection); 
        menu.add(0, DELETE_ITEM, 0, R.string.delete_item);        
        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu);
        menu.add(0, REMOVE, 0, R.string.remove_from_playlist);
        
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;
        mSelectedPosition =  mi.position;
        Cursor cursor = mAdapter.getCursor();        
        String strTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        menu.setHeaderTitle(strTitle);
        setIsOrderChanged(true);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
        menu.add(0, SAVE_TO_PLAYLIST, 0, R.string.save_to_playlist).setIcon(R.drawable.save_to_playlist);
        menu.add(0, SETTINGS, 0, R.string.settings).setIcon(R.drawable.setting);
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0) == 0) {
            menu.getItem(0).setTitle(R.string.sleep_start);
        } else {
            menu.getItem(0).setTitle(R.string.sleep_close);
        } 
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case SAVE_TO_PLAYLIST: {    
                Intent intent = new Intent();
                intent.setClass(mContext, AddPlaylistActivity.class); 
                long[] songsQueue = null;
                try {
                    songsQueue = MusicUtils.sService.getQueue();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (songsQueue != null) {
                    int count  = songsQueue.length;
                    ArrayList<Integer> songsId = new ArrayList<Integer>();
                    for (int i = 0; i < count; i++) {
                        songsId.add(new Integer((int)songsQueue[i]));
                    }
                    intent.putIntegerArrayListExtra("playlist_songs_id", songsId);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            
            case SETTINGS: {
                Intent musicPreferencesIntent = new Intent().setClass(mContext, MusicSetting.class);
                musicPreferencesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(musicPreferencesIntent);
                return true;
            }
            
            case SLEEP: {
                new SleepModeManager(mContext);
                return true;
            }
        }
        return false;     
    }
}
