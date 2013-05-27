package com.lewa.player.ui.outer;

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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.lewa.os.ui.PendingContentLoader;
import com.lewa.player.ExitApplication;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.model.ArtistBrowserAdapter;
import com.lewa.player.model.SongsCountLoader;
import com.lewa.player.online.DownLoadAllPicsAsync;
import com.lewa.player.ui.artist.ArtistMainEntryActivity;

public class ArtistBrowserActivity extends ListActivity implements
        View.OnCreateContextMenuListener, MusicUtils.Defs, PendingContentLoader{
    private Cursor mArtistCursor;
    private String mCurrentArtistId;
    private String mCurrentArtistName;
    private ArtistBrowserAdapter mAdapter;
    private SongsCountLoader mSongsCountLoader;
    private DownLoadAllPicsAsync mDownArtistAsync;
    
    public ArtistBrowserActivity() {
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        
        if (!bDelayLoadContent) {
            ListView artistList = getListView();
            artistList.setDivider(null);
            artistList.setCacheColorHint(0);
            artistList.setOnCreateContextMenuListener(this);
            artistList.setFastScrollEnabled(true);
            
            mAdapter = (ArtistBrowserAdapter) getLastNonConfigurationInstance();
            initAdapter();
            
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
            f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            f.addDataScheme("file");
            registerReceiver(mScanListener, f);
            registerProviderStatusObserver();

        }
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
    }
    
	@Override
    public void loadContent() {
		// TODO Auto-generated method stub
        boolean bDelayLoadContent = getIntent().getBooleanExtra("delayloadcontent", false);
        if (bDelayLoadContent) {
            getIntent().putExtra("delayloadcontent", false);
            ListView artistList = getListView();
            artistList.setDivider(null);
            artistList.setCacheColorHint(0);
            artistList.setOnCreateContextMenuListener(this);
            artistList.setFastScrollEnabled(true);
            
            mAdapter = (ArtistBrowserAdapter) getLastNonConfigurationInstance();
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
            mAdapter = new ArtistBrowserAdapter(
                    getApplication(),
                    this,
                    R.layout.track_list_item_group,
                    mArtistCursor,
                    new String[] {},
                    new int[] {});
            setListAdapter(mAdapter);
            mSongsCountLoader = new SongsCountLoader(this, SongsCountLoader.TYPE_ARTIST);
            mAdapter.setSongsCountLoader(mSongsCountLoader);
//            setTitle(R.string.working_artists);
            getArtistCursor(mAdapter.getQueryHandler(), null);
        } else {
            mAdapter.setActivity(this);
            setListAdapter(mAdapter);
            mArtistCursor = mAdapter.getCursor();
            if (mArtistCursor != null) {
                init(mArtistCursor);
            } else {
                getArtistCursor(mAdapter.getQueryHandler(), null);
            }
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Intent intent = new Intent();
        intent.setClass(this, ArtistMainEntryActivity.class);
        intent.putExtra("artist", Long.valueOf(id).toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    private Cursor getArtistCursor(AsyncQueryHandler async, String filter) {

        String[] cols = new String[] {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                "artist_sort_key"
        };
        
        StringBuilder where = new StringBuilder();
        where.append("1=1");
        where.append(MusicUtils.getWhereBuilder(this, "artist_id", 0));
        
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            uri = uri.buildUpon().appendQueryParameter("filter", Uri.encode(filter)).build();
        }

        Cursor ret = null;
        if (async != null) {
            async.startQuery(0, null, uri,
                    cols, where.toString() , null, "Upper(artist_sort_key)");
            
        } else {
            ret = MusicUtils.query(this, uri,
                    cols, where.toString() , null, MediaStore.Audio.Artists.ARTIST_KEY);
        }
        
        return ret;
    }    
       
    public void init(Cursor c) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c); // also sets mArtistCursor

        if (mArtistCursor == null) {
            MusicUtils.displayDatabaseError(this);
            closeContextMenu();
            mReScanHandler.sendEmptyMessageDelayed(0, 1000);
            return;
        }

        // restore previous position
//        if (mLastListPosCourse >= 0) {
//            ExpandableListView elv = getExpandableListView();
//            elv.setSelectionFromTop(mLastListPosCourse, mLastListPosFine);
//            mLastListPosCourse = -1;
//        }

        MusicUtils.hideDatabaseError(this);
//        MusicUtils.updateButtonBar(this, R.id.artisttab);
//        setTitle();
    }
    
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(ArtistBrowserActivity.this);
            }
            mReScanHandler.sendEmptyMessage(0);
        }
    };

    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter != null) {
                getArtistCursor(mAdapter.getQueryHandler(), null);
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
                getArtistCursor(mAdapter.getQueryHandler(), null);               
            }
        }
    };

    public Cursor getArtistCursor() {
        return mArtistCursor;
    }
    
    public void setArtistCursor(Cursor cursor) {
        mArtistCursor = cursor;
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mAdapter != null) {
            mAdapter.changeCursor(mArtistCursor);
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
        mArtistCursor.moveToPosition(mi.position);
        mCurrentArtistId = mArtistCursor.getString(mArtistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
        mCurrentArtistName = mArtistCursor.getString(mArtistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
        boolean unknown = mCurrentArtistName == null || mCurrentArtistName.equals(MediaStore.UNKNOWN_STRING);
        if (unknown) {
            mCurrentArtistName = getResources().getString(R.string.unknown_artist_name);
        }
        menu.setHeaderTitle(mCurrentArtistName);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_SELECTION: {
                // play the selected album
                long [] list = MusicUtils.getSongListForArtist(this, Long.parseLong(mCurrentArtistId));
                MusicUtils.playAll(this, list, 0);
                return true;
            }

            case QUEUE: {
                long [] list = MusicUtils.getSongListForArtist(this, Long.parseLong(mCurrentArtistId));
                MusicUtils.addToCurrentPlaylist(this, list);
                return true;
            }

            case NEW_PLAYLIST: {
                long [] list = MusicUtils.getSongListForArtist(this, Long.parseLong(mCurrentArtistId));
                MusicUtils.addToNewPlaylist(this, list, NEW_PLAYLIST);
                return true;
            }

            case PLAYLIST_SELECTED: {
                long [] list = MusicUtils.getSongListForArtist(this, Long.parseLong(mCurrentArtistId));
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(this, list, playlist);
                return true;
            }
            case DELETE_ITEM: {
                long [] list = MusicUtils.getSongListForArtist(this, Long.parseLong(mCurrentArtistId));
                String f;
                if (android.os.Environment.isExternalStorageRemovable()) {
                    f = getString(R.string.delete_artist_desc);
                } else {
                    f = getString(R.string.delete_artist_desc_nosdcard);
                }
                String desc = String.format(f, mCurrentArtistName);
                MusicUtils.deleteItems(this, desc, list);
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
    
    public void getAllArtistPics() {        
        
        if(mArtistCursor == null || (mArtistCursor != null && mArtistCursor.getCount() == 0)) {
            return;
        }
        
        mArtistCursor.moveToFirst();    

        mDownArtistAsync = new DownLoadAllPicsAsync();
        mDownArtistAsync.execute(mArtistCursor, ArtistBrowserActivity.this);
    }
    
    public  void setDownloadAsync(DownLoadAllPicsAsync async) {
        mDownArtistAsync = async;
    }
    
    public  DownLoadAllPicsAsync getDownloadAsync() {
        return mDownArtistAsync;
    }
    
    public void setAdapter(ArtistBrowserAdapter adapter) {
        mAdapter = adapter;
    }
    
}

