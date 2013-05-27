package com.lewa.player.ui.outer;


import com.lewa.os.ui.PendingContentLoader;
import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.MusicUtils.ServiceToken;
import com.lewa.player.model.AlbumBrowserAdapter;
import com.lewa.player.model.SongsCountLoader;
import com.lewa.player.online.DownLoadAllPicsAsync;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AlbumBrowserActivity extends ListActivity 
        implements View.OnCreateContextMenuListener, MusicUtils.Defs, PendingContentLoader{    
    
    private Cursor mAlbumCursor;
    private String mArtistId;
    private String mCurrentAlbumId;
    private String mCurrentAlbumName;
    private ServiceToken mToken;
    private AlbumBrowserAdapter mAdapter;
    private String mSortOrder;
    private SongsCountLoader mSongsCountLoader;
    private DownLoadAllPicsAsync mDownAlbumAsync;
    
    public AlbumBrowserActivity() {
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        
       super.onCreate(icicle);
       
       boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
       if (!bDelayLoadContent) {
           ListView albumList = getListView();
           albumList.setDivider(null);
           albumList.setCacheColorHint(0);
           albumList.setOnCreateContextMenuListener(this);
           albumList.setFastScrollEnabled(true);
           
           if(this.getIntent() != null) {
        	   mArtistId = this.getIntent().getStringExtra("artist");
           }
           mAdapter = (AlbumBrowserAdapter) getLastNonConfigurationInstance();
           initAdapter();
           
           IntentFilter f = new IntentFilter();
           f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
           f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
           f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
           f.addDataScheme("file");
           registerReceiver(mScanListener, f);
           
           ExitApplication exit = (ExitApplication) getApplication();  
           exit.addActivity(this);
       }
    }
    
    	@Override
    public void loadContent() {
    		// TODO Auto-generated method stub
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        if (bDelayLoadContent) {
        getIntent().putExtra("delayloadcontent", false);
        ListView albumList = getListView();
        albumList.setDivider(null);
        albumList.setCacheColorHint(0);
        albumList.setOnCreateContextMenuListener(this);
        albumList.setFastScrollEnabled(true);
        
        if(this.getIntent() != null) {
            mArtistId = this.getIntent().getStringExtra("artist");
        }
        mAdapter = (AlbumBrowserAdapter) getLastNonConfigurationInstance();
        initAdapter();
        
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
        registerProviderStatusObserver();
        }
    }
    
    public void initAdapter() {
       if (mAdapter == null) {
           //Log.i("@@@", "starting query");
           mAdapter = new AlbumBrowserAdapter(
               getApplication(),
               this,
               R.layout.track_list_item,
               mAlbumCursor,
               new String[] {},
               new int[] {});
           setListAdapter(mAdapter);
           mSongsCountLoader = new SongsCountLoader(this, SongsCountLoader.TYPE_ALBUM);
           //modify by zhaolei,120323,for artist album
           if(mArtistId != null) {
           mSongsCountLoader.setArtistId(Integer.valueOf(mArtistId));
           } else {
           mSongsCountLoader.setArtistId(-1);
           }
           //end
           mAdapter.setSongsCountLoader(mSongsCountLoader);
           setTitle(R.string.working_albums);
           getAlbumCursor(mAdapter.getQueryHandler(), null);
       } else {
           mAdapter.setActivity(this);
           setListAdapter(mAdapter);
           mAlbumCursor = mAdapter.getCursor();
           if (mAlbumCursor != null) {
           init(mAlbumCursor);
           } else {
           getAlbumCursor(mAdapter.getQueryHandler(), null);
           }
       }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, AlbumTrackBrowserActivity.class);
        intent.getBundleExtra("album");
        intent.putExtra("album", Long.valueOf(id).toString());
        intent.putExtra("artist", mArtistId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    public Cursor getAlbumCursor(AsyncQueryHandler async, String filter) {
        String[] cols = new String[] {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            "album_sort_key"
        };
        mSortOrder = "Upper(album_sort_key)";
        Cursor ret = null;
        StringBuilder where = new StringBuilder();
        where.append(" 1 = 1 ");
        where.append(MusicUtils.getWhereBuilder(this, "album_id", 0));
    
        if (mArtistId != null) {
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        
        //modifyed by zhaolei,120323,for artist album
        where.append(" AND " + "_id IN (select album_id from audio where artist_id=" + mArtistId +")");
        //end
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        if (async != null) {
            async.startQuery(0, null, uri,
                cols, where.toString(), null, mSortOrder);
        } else {
            ret = MusicUtils.query(this, uri,
                cols, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        }
        } else {
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }
        if (async != null) {
            async.startQuery(0, null,
                uri,
                cols, where.toString(), null, mSortOrder);
        } else {
            ret = MusicUtils.query(this, uri,
                cols, where.toString(), null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        }
        }
        return ret;
    }
    
    public void init(Cursor c) {
    
        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c); // also sets mAlbumCursor
    
        if (mAlbumCursor == null) {
            MusicUtils.displayDatabaseError(this);
            closeContextMenu();
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }
    
        MusicUtils.hideDatabaseError(this);    
    }
    
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(AlbumBrowserActivity.this);
            }
            mReScanHandler.sendEmptyMessage(0);
        }
    };
    
    
    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getAlbumCursor(mAdapter.getQueryHandler(), null);
            }
        }
    };
    
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
            if(mAdapter != null) {
                getAlbumCursor(mAdapter.getQueryHandler(), null);           
            }
            if(mAlbumCursor != null && mAlbumCursor.getCount() == 0) {
                MusicUtils.mHasSongs = false;
            }
        }
    };
    
    public Cursor getAlbumCursor() {
        return mAlbumCursor;
    }    
    
    public void setAlbumCursor(Cursor cursor) {
        mAlbumCursor = cursor;
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mAdapter != null) {
            mAdapter.changeCursor(mAlbumCursor);
        }
        if (mSongsCountLoader != null) {
            mSongsCountLoader.stop();
        }
        mAdapter = null;
        unregisterReceiverSafe(mScanListener);
        unregisterProviderStatusObserver();
        super.onDestroy();
    }
    
    private void unregisterReceiverSafe(BroadcastReceiver receiver) {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        // ignore
        }
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        if (mSongsCountLoader != null) {
            mSongsCountLoader.resume();
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfoIn) {
        menu.add(0, PLAY_SELECTION, 0, R.string.play_selection);    
        menu.add(0, DELETE_ITEM, 0, R.string.delete_item);
        SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        
        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfoIn;
        mAlbumCursor.moveToPosition(mi.position);
        mCurrentAlbumId = mAlbumCursor.getString(mAlbumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
        mCurrentAlbumName = mAlbumCursor.getString(mAlbumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
        boolean unknown = mCurrentAlbumName == null || mCurrentAlbumName.equals(MediaStore.UNKNOWN_STRING);
        if (unknown) {
            mCurrentAlbumName = getResources().getString(R.string.unknown_album_name);
        }
        menu.setHeaderTitle(mCurrentAlbumName);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int aid = -1;
        if(mArtistId != null) {
            aid = Integer.valueOf(mArtistId);
        }
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                // play the selected album
                long [] list = MusicUtils.getSongListForAlbum(this, Long.parseLong(mCurrentAlbumId), aid);
                MusicUtils.playAll(this, list, 0);
                return true;
            }
    
            case QUEUE: {
                long [] list = MusicUtils.getSongListForAlbum(this, Long.parseLong(mCurrentAlbumId), aid);
                MusicUtils.addToCurrentPlaylist(this, list);
                return true;
            }
        
            case NEW_PLAYLIST: {
                long [] list = MusicUtils.getSongListForAlbum(this, Long.parseLong(mCurrentAlbumId), aid);
                MusicUtils.addToNewPlaylist(this, list, NEW_PLAYLIST);
                return true;
            }
        
            case PLAYLIST_SELECTED: {
                long [] list = MusicUtils.getSongListForAlbum(this, Long.parseLong(mCurrentAlbumId), aid);
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, list, playlist);
                return true;
            }
            case DELETE_ITEM: {
                long [] list = MusicUtils.getSongListForAlbum(this, Long.parseLong(mCurrentAlbumId), aid);
                String f;
                if (android.os.Environment.isExternalStorageRemovable()) {
                    f = getString(R.string.delete_album_desc);
                } else {
                    f = getString(R.string.delete_album_desc_nosdcard);
                }
                String desc = String.format(f, mCurrentAlbumName);
                MusicUtils.deleteItems(this, desc, list);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
    
    public void getAllAlbumPics() {
    
        if(mAlbumCursor == null || (mAlbumCursor != null && mAlbumCursor.getCount() == 0)) {
            return;
        }
        
        mAlbumCursor.moveToFirst();    
        mDownAlbumAsync = new DownLoadAllPicsAsync();
        mDownAlbumAsync.execute(mAlbumCursor, AlbumBrowserActivity.this);
    }
    
    public DownLoadAllPicsAsync getDownloadAsync() {
        return mDownAlbumAsync;
    }
    
    public void setAdapter(AlbumBrowserAdapter adapter) {
        mAdapter = adapter;
    }

}
